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

import org.n52.io.request.Parameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class DataCache {
    private String[] matchingParameters = {
            Parameters.EXPANDED,
            Parameters.FORMAT,
            Parameters.FILTER_PLATFORM_TYPES,
            Parameters.FILTER_VALUE_TYPES,
            Parameters.SERVICES,
            Parameters.PLATFORMS,
            Parameters.CATEGORIES,
            Parameters.PHENOMENA,
            Parameters.STATIONS,
    };

    private Map<String, String> defaultValues = new HashMap<>();

    {
        defaultValues.put(Parameters.EXPANDED, Boolean.toString(Parameters.DEFAULT_EXPANDED));
        defaultValues.put(Parameters.FILTER_PLATFORM_TYPES, "stationary,insitu");
        defaultValues.put(Parameters.FILTER_VALUE_TYPES, "quantity");
    }

    abstract boolean isDataCached(RequestParameterSet parameters);

    abstract Optional<DataCollection<Data<AbstractValue< ? >>>> getData(RequestParameterSet parameters);

    abstract void putDataForParameters(RequestParameterSet parameters, DataCollection<Data<AbstractValue< ? >>> data);

    protected boolean compareParameterSets(RequestParameterSet parametersA, RequestParameterSet parametersB) {
        for (String paramName : matchingParameters) {
            if (compareParameter(parametersA, parametersB, paramName)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    protected boolean compareParameter(RequestParameterSet parametersA,
                                       RequestParameterSet parametersB,
                                       String paramName) {
        if (parametersA.containsParameter(paramName) &&
                !parametersB.containsParameter(paramName)) {
            if (defaultValues.containsKey(paramName)) {
                return compareParameterValues(parametersA.getAsString(paramName), defaultValues.get(paramName));
            }
            return false;
        }
        if (parametersB.containsParameter(paramName) &&
                !parametersA.containsParameter(paramName)) {
            if (defaultValues.containsKey(paramName)) {
                return compareParameterValues(parametersB.getAsString(paramName), defaultValues.get(paramName));
            }
            return false;
        }
        if (!parametersA.containsParameter(paramName) && !parametersB.containsParameter(paramName)) {
            return true;
        }
        return compareParameterValues(parametersA.getAsString(paramName), parametersA.getAsString(paramName));
    }

    protected boolean compareParameterValues(String valueA, String valueB) {
        String[] argumentsA = valueA.split(",");
        String[] argumentsB = valueB.split(",");
        Arrays.sort(argumentsA);
        Arrays.sort(argumentsB);
        return Arrays.equals(argumentsA, argumentsB);
    }
}
