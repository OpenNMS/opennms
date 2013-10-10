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
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jrobin.core.RrdDb;

/**
 * The Class RrdDatabase.
 * <p>Copied from opennms-tools/opennms-rrd-converter.</p>
 */
public class RrdDatabase extends BaseRrdDataSource {
    
    /** The m_rrd. */
    private final RrdDb m_rrd;
    
    /** The m_archives. */
    private final SortedSet<RrdArchive> m_archives;
    
    /** The m_end time. */
    private final long m_endTime;
    
    /** The m_start time. */
    private final long m_startTime;
    
    /** The m_step. */
    private final long m_step;

    /**
     * Instantiates a new rrd database.
     *
     * @param rrd the rrd
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public RrdDatabase(final RrdDb rrd) throws IOException {
        super(Arrays.asList(rrd.getDsNames()));
        m_rrd = rrd;
        m_archives = initializeArchives();
        m_endTime = computeEndTime();
        m_startTime = computeStartTime();
        m_step = m_rrd.getHeader().getStep();
    }

    /**
     * Initialize archives.
     *
     * @return the sorted set
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private SortedSet<RrdArchive> initializeArchives() throws IOException {
        final SortedSet<RrdArchive> archives = new TreeSet<RrdArchive>();
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            final RrdArchive archive = new RrdArchive(m_rrd.getArchive(i), getDsNames());
            if (archive.isAverage()) {
                archives.add(archive);
            }
        }
        return archives;
    }

    /**
     * Compute start time.
     *
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private long computeStartTime() throws IOException {
        long startTime = Long.MAX_VALUE;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            startTime = Math.min(m_rrd.getArchive(i).getStartTime(), startTime);
        }
        return startTime;
    }

    /**
     * Compute end time.
     *
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private long computeEndTime() throws IOException {
        long endTime = 0;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            endTime = Math.max(m_rrd.getArchive(i).getEndTime(), endTime);
        }
        return endTime;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getStartTime()
     */
    public long getStartTime() throws IOException {
        return m_startTime;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getEndTime()
     */
    public long getEndTime() throws IOException {
        return m_endTime;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getNativeStep()
     */
    public long getNativeStep() throws IOException {
        return m_step;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getDataAt(long)
     */
    public RrdEntry getDataAt(final long timestamp) throws IOException {
        for (final RrdArchive archive : m_archives) {
            if (archive.isValidTimestamp(timestamp)) {
                return archive.getDataAt(timestamp);
            }
        }
        return new RrdEntry(timestamp, getDsNames());
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#close()
     */
    public void close() throws IOException {
        m_rrd.close();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
        .append("rrd", m_rrd)
        .toString();
    }
}
