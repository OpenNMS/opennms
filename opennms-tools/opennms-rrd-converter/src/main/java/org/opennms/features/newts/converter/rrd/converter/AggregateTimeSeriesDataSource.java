/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.newts.converter.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class AggregateTimeSeriesDataSource extends BaseRrdDataSource {
    private final List<? extends TimeSeriesDataSource> m_dataSources;

    public AggregateTimeSeriesDataSource(final List<? extends TimeSeriesDataSource> dataSources) throws IOException {
        assertNotNull(dataSources, "dataSources cannot be null!");
        m_dataSources = dataSources;
        ArrayList<String> dsNames = new ArrayList<>();
        for (final TimeSeriesDataSource ds : m_dataSources) {
            for (final String dsName : ds.getDsNames()) {
                if (!dsNames.contains(dsName)) {
                    dsNames.add(dsName);
                }
            }
        }
        setDsNames(dsNames);
    }

    private void assertNotNull(final Object o, final String message) {
        if (o == null) {
            throw new IllegalStateException(message);
        }
    }

    public long getStartTime() throws IOException {
        long startTime = Long.MAX_VALUE;
        for (final TimeSeriesDataSource ds : m_dataSources) {
            startTime = Math.min(ds.getStartTime(), startTime);
        }
        return startTime;
    }

    public long getEndTime() throws IOException {
        long endTime = 0;
        for (final TimeSeriesDataSource ds : m_dataSources) {
            endTime = Math.max(ds.getEndTime(), endTime);
        }
        return endTime;
    }

    public long getNativeStep() throws IOException {
        long nativeStep = Long.MAX_VALUE;
        for (final TimeSeriesDataSource ds : m_dataSources) {
            nativeStep = Math.min(ds.getNativeStep(), nativeStep);
        }
        return nativeStep;
    }

    public RrdEntry getDataAt(final long timestamp) throws IOException {
        final RrdEntry entry = new RrdEntry(timestamp, getDsNames());
        for (final TimeSeriesDataSource ds : m_dataSources) {
            final RrdEntry thisEntry = ds.getDataAt(timestamp);
            entry.coalesceWith(thisEntry);
        }
        return entry;
    }

    public void close() throws IOException {
        for (final TimeSeriesDataSource ds : m_dataSources) {
            ds.close();
        }
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("dataSources", m_dataSources)
            .toString();
    }
}
