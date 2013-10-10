/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.jrobin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The Class AggregateTimeSeriesDataSource.
 * <p>Copied from opennms-tools/opennms-rrd-converter.</p>
 */
public class AggregateTimeSeriesDataSource extends BaseRrdDataSource {
    
    /** The m_data sources. */
    private final List<? extends TimeSeriesDataSource> m_dataSources;

    /**
     * Instantiates a new aggregate time series data source.
     *
     * @param dataSources the data sources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public AggregateTimeSeriesDataSource(final List<? extends TimeSeriesDataSource> dataSources) throws IOException {
        assertNotNull(dataSources, "dataSources cannot be null!");
        m_dataSources = dataSources;
        ArrayList<String> dsNames = new ArrayList<String>();
        for (final TimeSeriesDataSource ds : m_dataSources) {
            for (final String dsName : ds.getDsNames()) {
                if (!dsNames.contains(dsName)) {
                    dsNames.add(dsName);
                }
            }
        }
        setDsNames(dsNames);
    }

    /**
     * Assert not null.
     *
     * @param o the o
     * @param message the message
     */
    private void assertNotNull(final Object o, final String message) {
        if (o == null) {
            throw new IllegalStateException(message);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getStartTime()
     */
    public long getStartTime() throws IOException {
        long startTime = Long.MAX_VALUE;
        for (final TimeSeriesDataSource ds : m_dataSources) {
            startTime = Math.min(ds.getStartTime(), startTime);
        }
        return startTime;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getEndTime()
     */
    public long getEndTime() throws IOException {
        long endTime = 0;
        for (final TimeSeriesDataSource ds : m_dataSources) {
            endTime = Math.max(ds.getEndTime(), endTime);
        }
        return endTime;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getNativeStep()
     */
    public long getNativeStep() throws IOException {
        long nativeStep = Long.MAX_VALUE;
        for (final TimeSeriesDataSource ds : m_dataSources) {
            nativeStep = Math.min(ds.getNativeStep(), nativeStep);
        }
        return nativeStep;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getDataAt(long)
     */
    public RrdEntry getDataAt(final long timestamp) throws IOException {
        final RrdEntry entry = new RrdEntry(timestamp, getDsNames());
        for (final TimeSeriesDataSource ds : m_dataSources) {
            final RrdEntry thisEntry = ds.getDataAt(timestamp);
            entry.coalesceWith(thisEntry);
        }
        return entry;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#close()
     */
    public void close() throws IOException {
        for (final TimeSeriesDataSource ds : m_dataSources) {
            ds.close();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
        .append("dataSources", m_dataSources)
        .toString();
    }
}
