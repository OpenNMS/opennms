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
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jrobin.core.RrdDb;

public class RrdDatabase extends BaseRrdDataSource {
    private final RrdDb m_rrd;
    private final SortedSet<RrdArchive> m_archives;
    private final long m_endTime;
    private final long m_startTime;
    private final long m_step;

    public RrdDatabase(final RrdDb rrd) throws IOException {
        super(Arrays.asList(rrd.getDsNames()));
        m_rrd = rrd;
        m_archives = initializeArchives();
        m_endTime = computeEndTime();
        m_startTime = computeStartTime();
        m_step = m_rrd.getHeader().getStep();
    }

    private SortedSet<RrdArchive> initializeArchives() throws IOException {
        final SortedSet<RrdArchive> archives = new TreeSet<>();
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            final RrdArchive archive = new RrdArchive(m_rrd.getArchive(i), getDsNames());
            if (archive.isAverage()) {
                archives.add(archive);
            }
        }
        return archives;
    }

    private long computeStartTime() throws IOException {
        long startTime = Long.MAX_VALUE;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            startTime = Math.min(m_rrd.getArchive(i).getStartTime(), startTime);
        }
        return startTime;
    }

    private long computeEndTime() throws IOException {
        long endTime = 0;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            endTime = Math.max(m_rrd.getArchive(i).getEndTime(), endTime);
        }
        return endTime;
    }

    public long getStartTime() throws IOException {
        return m_startTime;
    }

    public long getEndTime() throws IOException {
        return m_endTime;
    }

    public long getNativeStep() throws IOException {
        return m_step;
    }

    public RrdEntry getDataAt(final long timestamp) throws IOException {
        if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "archives size = %d", m_archives.size());
        for (final RrdArchive archive : m_archives) {
            if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "trying archive %s", archive);
            if (archive.isValidTimestamp(timestamp)) {
                if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "%s <= %d < %d", archive.getStartTime(), timestamp, archive.getEndTime() + getNativeStep());
                return archive.getDataAt(timestamp);
            }
        }
        return new RrdEntry(timestamp, getDsNames());
    }

    public void close() throws IOException {
        m_rrd.close();
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("rrd", m_rrd)
            .toString();
    }
}
