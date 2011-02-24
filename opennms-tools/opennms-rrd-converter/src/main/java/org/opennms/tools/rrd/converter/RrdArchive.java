package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jrobin.core.Archive;
import org.jrobin.core.Robin;
import org.opennms.core.utils.LogUtils;
import org.springframework.util.Assert;

public class RrdArchive extends BaseRrdDataSource implements Comparable<RrdArchive> {

    private Archive m_archive;

    public RrdArchive(final Archive archive, final List<String> dsNames) {
        super(dsNames);
        m_archive = archive;
    }

    public Archive getArchive() {
        return m_archive;
    }

    public RrdEntry getDataAt(final long timestamp) throws IOException {
        final Integer row = getRowNumberForTimestamp(timestamp);
        if (row != null) {
            return getDataForRowWithTimestamp(row, timestamp);
        }
        return new RrdEntry(timestamp, getDsNames());
    }

    public double getValueForRow(final int row, final String dsName) throws IOException {
        final Robin r = m_archive.getRobin(getDsIndex(dsName));
        return r.getValue(row);
    }
    
    public RrdEntry getDataForRowWithTimestamp(final int row, final long timestamp) throws IOException {
        final RrdEntry entry = new RrdEntry(timestamp, getDsNames());
        if (getStartTime() <= timestamp && timestamp < getEndTime() + getNativeStep()) {
            int i = 0;
            for (final String dsName : getDsNames()) {
                final double value = getValueForRow(row, dsName);
                entry.setValue(dsName, value);
                i++;
            }
        }
        return entry;
    }

    public List<RrdEntry> getData(final long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>();
        final long arcStep = m_archive.getArcStep();
        Assert.isTrue(arcStep % step == 0, "archive step (" + arcStep + ") must be evenly divisible by step");
        final long repeat = arcStep / step;
        for (int row = 0; row < m_archive.getRows(); row++) {
            for (int repeatedRow = 0; repeatedRow < repeat; repeatedRow++) {
                final long offsetInSeconds = (row * arcStep) + (repeatedRow * step);
                entries.add(getDataForRowWithTimestamp(row, getStartTime() + offsetInSeconds));
            }
        }
        return entries;
    }

    public long getNativeStep() throws IOException {
        return m_archive.getArcStep();
    }
    
    public long getStartTime() throws IOException {
        return m_archive.getStartTime();
    }

    public long getEndTime() throws IOException {
        return m_archive.getEndTime();
    }
    
    public long getRows() throws IOException {
        return m_archive.getRows();
    }
    
    public void close() throws IOException {
    }

    protected boolean isAverage() throws IOException {
        return getArchive().getConsolFun().equals("AVERAGE");
    }

    public String toString() {
        try {
            return new ToStringBuilder(this)
                .append("startTime", new Date(m_archive.getStartTime() * 1000L))
                .append("endTime", new Date(m_archive.getEndTime() * 1000L))
                
                .toString();
        } catch (final IOException e) {
            LogUtils.warnf(this, e, "Unable to generate toString()");
            return super.toString();
        }
    }
    
    public int compareTo(final RrdArchive archive) {
        try {
            return new CompareToBuilder()
                .append(this.getStartTime(), archive.getStartTime())
                .append(this.getNativeStep(), archive.getNativeStep())
                .append(this.getRows(), archive.getRows())
                .toComparison();
        } catch (final IOException e) {
            LogUtils.warnf(this, e, "Unable to generate compareTo(%s)", archive);
            return -1;
        }
    }
}
