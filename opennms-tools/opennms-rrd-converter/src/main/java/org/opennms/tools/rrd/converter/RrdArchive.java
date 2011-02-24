package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public RrdArchive(final Archive archive, final List<String> dsNames) throws IOException {
        super(dsNames);
        m_archive = archive;
        m_step = m_archive.getArcStep();
        m_startTime = m_archive.getStartTime();
        m_endTime = m_archive.getEndTime();
        m_rows = m_archive.getRows();
        m_consolFun = getArchive().getConsolFun();
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
        final RrdEntry entry = new RrdEntry(timestamp, getDsNames());
        int i = 0;
        for (final String dsName : getDsNames()) {
            final double value = getValueForRow(row, dsName);
            entry.setValue(dsName, value);
            i++;
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
