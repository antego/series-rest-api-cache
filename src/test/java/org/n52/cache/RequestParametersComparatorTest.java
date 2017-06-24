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

import org.junit.Assert;
import org.junit.Test;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;

import java.io.IOException;

public class RequestParametersComparatorTest {
    private RequestParametersComparator comparator = new RequestParametersComparator();

    @Test
    public void shouldReturnTrueIfDefaultValueAndEmptyValue() throws IOException {
        RequestSimpleParameterSet set1 = new RequestSimpleParameterSet();
        set1.setParameter(Parameters.FILTER_PLATFORM_TYPES, "stationary,insitu");

        RequestSimpleParameterSet set2 = new RequestSimpleParameterSet();

        Assert.assertTrue(comparator.compareParameterSets(set1, set2));
        Assert.assertTrue(comparator.compareParameterSets(set2, set1));
    }

    @Test
    public void shouldReturnTrueOnDifferentValueOrder() throws IOException {
        RequestSimpleParameterSet set1 = new RequestSimpleParameterSet();
        set1.setParameter(Parameters.SERVICES, "srv1, srv2");

        RequestSimpleParameterSet set2 = new RequestSimpleParameterSet();
        set2.setParameter(Parameters.SERVICES, "srv2, srv1");

        Assert.assertTrue(comparator.compareParameterSets(set1, set2));
        Assert.assertTrue(comparator.compareParameterSets(set2, set1));
    }
}