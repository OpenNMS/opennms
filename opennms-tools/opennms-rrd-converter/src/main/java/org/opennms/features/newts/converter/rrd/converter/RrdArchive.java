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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jrobin.core.Archive;
import org.jrobin.core.Robin;

public class RrdArchive extends BaseRrdDataSource implements Comparable<RrdArchive> {

    private Archive m_archive;
    private final long m_step;
    private final long m_startTime;
    private final long m_endTime;
    private final int m_rows;
    private final String m_consolFun;
    private final Map<Integer,RrdEntry> m_data;

    public RrdArchive(final Archive archive, final List<String> dsNames) throws IOException {
        super(dsNames);
        m_archive = archive;
        m_step = m_archive.getArcStep();
        m_startTime = m_archive.getStartTime();
        m_endTime = m_archive.getEndTime();
        m_rows = m_archive.getRows();
        m_consolFun = getArchive().getConsolFun();
        
        m_data = new TreeMap<Integer,RrdEntry>();
        for (int row = 0; row < m_rows; row++) {
            m_data.put(row, getRawDataForRowWithTimestamp(row, getStartTime() + (row * m_step)));
        }

    }

    public Archive getArchive() {
        return m_archive;
    }

    public RrdEntry getDataAt(final long timestamp) throws IOException {
        if (isValidTimestamp(timestamp)) {
        final int row = getRowNumberForTimestamp(timestamp);
            if (row >= 0) {
                return getDataForRowWithTimestamp(row, timestamp);
            }
        }
        return new RrdEntry(timestamp, getDsNames());
    }

    public double getValueForRow(final int row, final String dsName) throws IOException {
        final Robin r = m_archive.getRobin(getDsIndex(dsName));
        return r.getValue(row);
    }
    
    private RrdEntry getDataForRowWithTimestamp(final int row, final long timestamp) throws IOException {
        final RrdEntry rawEntry = m_data.get(row);
        if (rawEntry == null) {
            return new RrdEntry(timestamp, getDsNames());
        }
        if (timestamp == rawEntry.getTimestamp()) {
            return rawEntry;
        } else {
            try {
                final RrdEntry newEntry = (RrdEntry)rawEntry.clone();
                newEntry.setTimestamp(timestamp);
                return newEntry;
            } catch (final CloneNotSupportedException e) {
                LogUtils.warnf(this, e, "unable to clone entry: %s", rawEntry);
                return new RrdEntry(timestamp, getDsNames());
            }
        }
    }

    private RrdEntry getRawDataForRowWithTimestamp(final int row, final long timestamp) throws IOException {
        final RrdEntry entry = new RrdEntry(timestamp, getDsNames());
        for (final String dsName : getDsNames()) {
            final double value = getValueForRow(row, dsName);
            entry.setValue(dsName, value);
        }
        return entry;
    }

    @Override
    public List<RrdEntry> getData(final long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>(getRows());
        final long arcStep = m_archive.getArcStep();
        assertTrue(arcStep % step == 0, "archive step (" + arcStep + ") must be evenly divisible by step");
        final long repeat = arcStep / step;
        for (int row = 0; row < m_archive.getRows(); row++) {
            for (int repeatedRow = 0; repeatedRow < repeat; repeatedRow++) {
                final long offsetInSeconds = (row * arcStep) + (repeatedRow * step);
                entries.add(getDataForRowWithTimestamp(row, getStartTime() + offsetInSeconds));
            }
        }
        return entries;
    }

    private void assertTrue(final boolean b, final String message) {
        if (!b) {
            throw new IllegalStateException(message);
        }
    }

    public long getNativeStep() throws IOException {
        return m_step;
    }
    
    public long getStartTime() throws IOException {
        return m_startTime;
    }

    public long getEndTime() throws IOException {
        return m_endTime;
    }
    
    @Override
    public int getRows() throws IOException {
        return m_rows;
    }
    
    public void close() throws IOException {
    }

    protected boolean isAverage() throws IOException {
        return m_consolFun.equals("AVERAGE");
    }

    public String toString() {
        try {
            return new ToStringBuilder(this)
                .append("dsNames", getDsNames())
                .append("startTime", new Date(m_archive.getStartTime() * 1000L))
                .append("endTime", new Date(m_archive.getEndTime() * 1000L))
                .append("step", getNativeStep())
                .toString();
        } catch (final IOException e) {
            LogUtils.warnf(this, e, "Unable to generate toString()");
            return super.toString();
        }
    }
    
    public int compareTo(final RrdArchive archive) {
        try {
            /* Go from highest to lowest resolution (step).
             * If two archives have the same resolution, then pick the one with the earliest start time first.
             */
            return new CompareToBuilder()
                .append(this.getNativeStep(), archive.getNativeStep())
                .append(this.getStartTime(), archive.getStartTime())
                .toComparison();
        } catch (final IOException e) {
            LogUtils.warnf(this, e, "Unable to generate compareTo(%s)", archive);
            return -1;
        }
    }
}
