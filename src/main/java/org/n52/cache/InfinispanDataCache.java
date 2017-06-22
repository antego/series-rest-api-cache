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

import java.util.List;
import java.util.Optional;

public class InfinispanDataCache extends DataCache {
    EmbeddedCacheManager cacheManager;
    Cache<SeriesMeta, DataCollection<Data<AbstractValue<?>>>> cache;

    public void initCache() {
        cacheManager = new DefaultCacheManager();
        cache = cacheManager.getCache();
    }

    public void stopCache() {
        cacheManager.stop();
    }

    @Override
    public boolean isDataCached(RequestParameterSet parameters) {
        Interval searchInterval = Interval.parse(parameters.getTimespan());
        long startTime = searchInterval.getStartMillis();
        long endTime = searchInterval.getEndMillis();
        QueryFactory qf = Search.getQueryFactory(cache);
        Query query = qf.from(SeriesMeta.class).having("seriesStart")
                .lte(startTime).and().having("seriesEnd").gte(endTime)
                .toBuilder().build();

        List<SeriesMeta> results = query.list();
        System.out.println("Found " + results.size() + " matches");
        return false;
    }

    @Override
    public Optional<DataCollection<Data<AbstractValue<?>>>> getData(RequestParameterSet parameters) {
        return null;
    }

    @Override
    public void putDataForParameters(RequestParameterSet parameters, DataCollection<Data<AbstractValue<?>>> data) {
        SeriesMeta meta = new SeriesMeta();
        meta.setParameters(parameters);
        Interval timespan = Interval.parse(parameters.getTimespan());
        meta.setSeriesStart(timespan.getStartMillis());
        meta.setSeriesStart(timespan.getEndMillis());
        cache.put(meta, data);
    }
}
