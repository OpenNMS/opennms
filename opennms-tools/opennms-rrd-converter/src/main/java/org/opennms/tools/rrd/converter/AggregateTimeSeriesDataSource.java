package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.LogUtils;
import org.springframework.util.Assert;

public class AggregateTimeSeriesDataSource implements TimeSeriesDataSource {
    private final List<? extends TimeSeriesDataSource> m_dataSources;

    public AggregateTimeSeriesDataSource(final List<? extends TimeSeriesDataSource> dataSources) {
        Assert.notNull(dataSources);
        m_dataSources = dataSources;
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

    public long getRows() throws IOException {
        return (getEndTime() - getStartTime()) / getNativeStep();
    }

    public List<String> getDsNames() throws IOException {
        final List<String> dsNames = new ArrayList<String>();
        for (final TimeSeriesDataSource ds : m_dataSources) {
            for (final String dsName : ds.getDsNames()) {
                if (!dsNames.contains(dsName)) {
                    dsNames.add(dsName);
                }
            }
        }
        return dsNames;
    }

    public RrdEntry getDataAt(final long timestamp) throws IOException {
        final RrdEntry entry = new RrdEntry(timestamp, getDsNames());
        for (final TimeSeriesDataSource ds : m_dataSources) {
            final RrdEntry thisEntry = ds.getDataAt(timestamp);
            entry.coalesceWith(thisEntry);
        }
        return entry;
    }

    public List<RrdEntry> getData(final long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>();

        final long rrdStartTime = getStartTime();
        for (long time = rrdStartTime; time <= getEndTime(); time += step) {
            entries.add(getDataAt(time));
        }

        LogUtils.debugf(this, "total entries: %d", entries.size());
        return entries;
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
