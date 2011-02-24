package org.opennms.tools.rrd.converter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.opennms.core.utils.LogUtils;

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

    public RrdDatabase(File rrdFile, boolean readOnly) throws IOException, RrdException {
        super();
        m_rrd = new RrdDb(rrdFile, readOnly);
        setDsNames(Arrays.asList(m_rrd.getDsNames()));
        m_archives = initializeArchives();
        m_endTime = computeEndTime();
        m_startTime = computeStartTime();
        m_step = m_rrd.getHeader().getStep();
    }

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
