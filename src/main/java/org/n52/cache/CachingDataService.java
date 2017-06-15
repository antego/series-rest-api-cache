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

import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: JavaDoc
 *
 */
public class CachingDataService extends ParameterService<ParameterOutput>
        implements DataService<Data<AbstractValue< ? >>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingDataService.class);

    private ParameterService<ParameterOutput> parameterService;

    private DataService<Data<AbstractValue< ? >>> dataService;

    private DataCache cache;

    public CachingDataService(ParameterService<ParameterOutput> parameterService,
                              DataService<Data<AbstractValue< ? >>> dataService,
                              DataCache cache) {
        this.parameterService = parameterService;
        this.dataService = dataService;
        this.cache = cache;
    }

    @Override
    public OutputCollection<ParameterOutput> getExpandedParameters(IoParameters query) {
        return parameterService.getExpandedParameters(query);
    }

    @Override
    public OutputCollection<ParameterOutput> getCondensedParameters(IoParameters query) {
        return parameterService.getCondensedParameters(query);
    }

    @Override
    public OutputCollection<ParameterOutput> getParameters(String[] items, IoParameters query) {
        return parameterService.getParameters(items, query);
    }

    @Override
    public ParameterOutput getParameter(String item, IoParameters query) {
        return parameterService.getParameter(item, query);
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return parameterService.exists(id, parameters);
    }

    @Override
    public boolean supportsRawData() {
        return false;
    }

    @Override
    public RawDataService getRawDataService() {
        return null;
    }

    @Override
    public DataCollection<Data<AbstractValue<?>>> getData(RequestParameterSet parameters) {
        return dataService.getData(parameters);
    }
}
