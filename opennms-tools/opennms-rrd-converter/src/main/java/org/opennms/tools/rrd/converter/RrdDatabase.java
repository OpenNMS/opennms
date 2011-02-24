package org.opennms.tools.rrd.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jrobin.core.Archive;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.opennms.core.utils.LogUtils;

public class RrdDatabase extends BaseRrdDataSource {
    private final RrdDb m_rrd;
    private final List<RrdArchive> m_archives;

    public RrdDatabase(final RrdDb rrd) throws IOException {
        super(Arrays.asList(rrd.getDsNames()));
        m_rrd = rrd;
        m_archives = initializeArchives();
    }

    public RrdDatabase(File rrdFile, boolean readOnly) throws IOException, RrdException {
        super();
        m_rrd = new RrdDb(rrdFile, readOnly);
        setDsNames(Arrays.asList(m_rrd.getDsNames()));
        m_archives = initializeArchives();
    }

    protected List<RrdArchive> initializeArchives() throws IOException {
        final List<RrdArchive> archives = new ArrayList<RrdArchive>();
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            final RrdArchive archive = new RrdArchive(m_rrd.getArchive(i), getDsNames());
            archives.add(archive);
        }
        Collections.sort(archives);
        return archives;
    }

    public long getStartTime() throws IOException {
        long startTime = Long.MAX_VALUE;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            startTime = Math.min(m_rrd.getArchive(i).getStartTime(), startTime);
        }
        return startTime;
    }

    public long getEndTime() throws IOException {
        long endTime = 0;
        for (int i = 0; i < m_rrd.getArcCount(); i++) {
            endTime = Math.max(m_rrd.getArchive(i).getEndTime(), endTime);
        }
        return endTime;
    }

    public long getNativeStep() throws IOException {
        return m_rrd.getHeader().getStep();
    }

    public long getRows() throws IOException {
        return ((getEndTime() - getStartTime()) / getNativeStep());
    }

    public List<String> getDsNames() throws IOException {
        return Arrays.asList(m_rrd.getDsNames());
    }

    public RrdEntry getDataAt(final long timestamp) throws IOException {
        LogUtils.tracef(this, "archives size = %d", m_archives.size());
        for (int i = m_archives.size() - 1; i >= 0; i--) {
            final RrdArchive archive = m_archives.get(i);
            LogUtils.tracef(this, "trying archive %s", archive);
            if (archive.getStartTime() <= timestamp && timestamp < (archive.getEndTime() + getNativeStep())) {
                if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "%s <= %d < %d", archive.getStartTime(), timestamp, archive.getEndTime() + getNativeStep());
                return archive.getDataAt(timestamp);
            }
        }
        return new RrdEntry(timestamp, getDsNames());
    }

    public List<RrdEntry> getData(long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>();
        final List<RrdArchive> archives = new ArrayList<RrdArchive>();
        for (int archiveIndex = 0; archiveIndex < m_rrd.getArcCount(); archiveIndex++) {
            final Archive archive = m_rrd.getArchive(archiveIndex);
            if (archive.getConsolFun().equals("AVERAGE")) {
                archives.add(new RrdArchive(archive, getDsNames()));
            }
        }
        if (archives.size() == 0) return entries;
        // RrdArchives are Comparable, and sort by start time
        Collections.sort(archives);

        BaseRrdDataSource currentArchive = null;
        List<RrdEntry> currentArchiveEntries = null;
        final long rrdStartTime = getStartTime();
        for (int i = 0; i < getRows(); i++) {
            final long time = rrdStartTime + (i * getNativeStep());
            if (archives.size() > 0 && time >= archives.get(0).getStartTime()) {
                currentArchive = archives.remove(0);
                currentArchiveEntries = currentArchive.getData(getNativeStep());
                LogUtils.debugf(this, "timestamp boundary hit at %s: currentArchive = %s, entries = %d", new Date(time * 1000L), currentArchive, entries.size());
            }
            if (currentArchive == null) {
                LogUtils.warnf(this, "ran out of archives before we expected!");
                break;
            }
            if (currentArchiveEntries.size() > 0) {
                entries.add(currentArchiveEntries.remove(0));
            } else {
                LogUtils.warnf(this, "ran out of entries before we expected!");
            }
        }
        LogUtils.debugf(this, "total entries: %d", entries.size());
        return entries;
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
