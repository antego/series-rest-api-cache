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
import org.hibernate.search.annotations.*;
import org.joda.time.Interval;
import org.n52.io.request.RequestParameterSet;

@Indexed
public class SeriesMeta {
    private RequestParameterSet parameters;

    @Field private long seriesStart;

    @Field private long seriesEnd;

    public SeriesMeta() {
    }

    public SeriesMeta(RequestParameterSet parameters) {
        this.parameters = parameters;
        Interval interval = Interval.parse(parameters.getTimespan());
        seriesStart = interval.getStartMillis();
        seriesEnd = interval.getEndMillis();
    }

    public RequestParameterSet getParameters() {
        return parameters;
    }

    public void setParameters(RequestParameterSet parameters) {
        this.parameters = parameters;
    }

    public long getSeriesStart() {
        return seriesStart;
    }

    public void setSeriesStart(long seriesStart) {
        this.seriesStart = seriesStart;
    }

    public long getSeriesEnd() {
        return seriesEnd;
    }

    public void setSeriesEnd(long seriesEnd) {
        this.seriesEnd = seriesEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriesMeta)) return false;

        SeriesMeta that = (SeriesMeta) o;

        if (seriesStart != that.seriesStart) return false;
        if (seriesEnd != that.seriesEnd) return false;
        return parameters != null ? parameters.equals(that.parameters) : that.parameters == null;
    }

    @Override
    public int hashCode() {
        int result = parameters != null ? parameters.hashCode() : 0;
        result = 31 * result + (int) (seriesStart ^ (seriesStart >>> 32));
        result = 31 * result + (int) (seriesEnd ^ (seriesEnd >>> 32));
        return result;
    }
}
