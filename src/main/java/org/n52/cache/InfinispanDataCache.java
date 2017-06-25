/*
 * Copyright (C) 2015-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.cache;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.joda.time.Interval;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InfinispanDataCache extends DataCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanDataCache.class);
    private EmbeddedCacheManager cacheManager;
    private Cache<SeriesMeta, DataCollection<Data<AbstractValue<?>>>> cache;
    private RequestParametersComparator comparator = new RequestParametersComparator();

    public void initCache() {
        cacheManager = new DefaultCacheManager();
        cache = cacheManager.getCache();
    }

    public void stopCache() {
        cacheManager.stop();
    }

    @Override
    public boolean isDataCached(RequestParameterSet parameters) {
        return getSeriesMetaForParameters(parameters).isPresent();
    }

    @Override
    public Optional<DataCollection<Data<AbstractValue<?>>>> getData(RequestParameterSet parameters) {
        Optional<SeriesMeta> meta = getSeriesMetaForParameters(parameters);
        if (meta.isPresent()) {
            DataCollection<Data<AbstractValue<?>>> collection = cache.get(meta.get());
            if (collection != null) {
                LOGGER.debug("Found data collection for meta info with parameters {}",
                        meta.get().getParameters());
                trimCollectionToTimespan(collection, parameters.getTimespan());
                return Optional.of(collection);
            } else {
                LOGGER.debug("Can't find collection for meta info with parameters {}",
                        meta.get().getParameters());
            }
        }
        return Optional.empty();
    }

    @Override
    public void putDataForParameters(RequestParameterSet parameters, DataCollection<Data<AbstractValue<?>>> data) {
        SeriesMeta meta = new SeriesMeta();
        meta.setParameters(parameters);
        Interval timespan = Interval.parse(parameters.getTimespan());
        long startTime = meta.getSeriesStart();
        meta.setSeriesStart(timespan.getStartMillis());
        meta.setSeriesStart(timespan.getEndMillis());

        // Prepend series
        QueryFactory qf = Search.getQueryFactory(cache);
        Query queryForPrependSeries = qf.from(SeriesMeta.class).having(SeriesMeta.SERIES_END_FIELD)
                .gte(startTime).toBuilder().build();
        List<DataCollection<Data<AbstractValue<?>>>> seriesForPrepend = new ArrayList<>();
        List<SeriesMeta> metaOfPrependSeries = queryForPrependSeries.list();
        // Fetch data for prepend
        metaOfPrependSeries.forEach(seriesMeta -> seriesForPrepend.add(cache.get(seriesMeta)));
        prependSeries(data, seriesForPrepend);
        // Remove prepended series
        metaOfPrependSeries.forEach(seriesMeta -> {
            if (cache.remove(seriesMeta) == null) {
                LOGGER.debug("Tried to remove non existed data");
            }
        });
    }

    private void prependSeries(DataCollection<Data<AbstractValue<?>>> data,
                               List<DataCollection<Data<AbstractValue<?>>>> collections) {
        Set<String> adjacentSeriesIds = collections.stream()
                .flatMap(dataCol -> dataCol.getAllSeries().keySet().stream())
                .collect(Collectors.toSet());

        for (String seriesId : adjacentSeriesIds) {
            Data<AbstractValue<?>> seriesToPrepend = null;
            for (DataCollection<Data<AbstractValue<?>>> collectionToPrepend : collections) {
                Data<AbstractValue<?>> series = collectionToPrepend.getSeries(seriesId);
                if (series == null) {
                    continue;
                }
                if (seriesToPrepend == null) {
                    seriesToPrepend = series;
                    continue;
                }
                if (series.getValues().get(0).getTimestamp() < seriesToPrepend.getValues().get(0).getTimestamp()) {
                    seriesToPrepend = series;
                }
            }

            if (!data.getAllSeries().containsKey(seriesId)) {
                data.addNewSeries(seriesId, seriesToPrepend);
            } else {
                Data<AbstractValue<?>> oldSeries = data.getSeries(seriesId);
                List<AbstractValue<?>> valuesToPrepend = seriesToPrepend.getValues();
                OptionalInt marginIdx = IntStream.range(0, valuesToPrepend.size())
                        .filter(i -> valuesToPrepend.get(i).getTimestamp()
                                >= oldSeries.getValues().get(0).getTimestamp())
                        .findFirst();
                oldSeries.addValues(valuesToPrepend
                        .subList(0, marginIdx.orElse(valuesToPrepend.size())).toArray(new AbstractValue<?>[0]));
            }
        }
    }

    private Optional<SeriesMeta> getSeriesMetaForParameters(RequestParameterSet parameters) {
        Interval searchInterval = Interval.parse(parameters.getTimespan());
        long startTime = searchInterval.getStartMillis();
        long endTime = searchInterval.getEndMillis();
        QueryFactory qf = Search.getQueryFactory(cache);
        Query query = qf.from(SeriesMeta.class).having(SeriesMeta.SERIES_START_FIELD)
                .lte(startTime).and().having(SeriesMeta.SERIES_END_FIELD).gte(endTime)
                .toBuilder().build();

        List<SeriesMeta> results = query.list();
        LOGGER.debug("Found {} matches for timespan {}",
                results.size(), parameters.getTimespan());
        for (SeriesMeta meta : results) {
            if (comparator.compareParameterSets(meta.getParameters(), parameters)) {
                return Optional.of(meta);
            }
        }
        return Optional.empty();
    }

    private void trimCollectionToTimespan(DataCollection<Data<AbstractValue<?>>> collection, String timespan) {
        Interval searchInterval = Interval.parse(timespan);
        long startTime = searchInterval.getStartMillis();
        long endTime = searchInterval.getEndMillis();
        for (Data<AbstractValue<?>> seriesData : collection.getAllSeries().values()) {
            List<AbstractValue<?>> newData = new ArrayList<>(seriesData.getValues());
            newData.removeIf(abstractValue -> {
                long valueTimestamp = abstractValue.getTimestamp();
                return valueTimestamp >= startTime && valueTimestamp <= endTime;
            });
            seriesData.setValues(newData.toArray(new AbstractValue<?>[0]));
        }
    }
}
