package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.LogUtils;

public abstract class BaseRrdDataSource implements TimeSeriesDataSource {
    private Map<String,Integer> m_dsNames = new TreeMap<String,Integer>();

    public BaseRrdDataSource(final List<String> dsNames) {
        setDsNames(dsNames);
    }
    
    public BaseRrdDataSource() {
    }

    public void setDsNames(final List<String> dsNames) {
        for (int i = 0; i < dsNames.size(); i++) {
            m_dsNames.put(dsNames.get(i), i);
        }
    }
    
    public List<String> getDsNames() throws IOException {
        return new ArrayList<String>(m_dsNames.keySet());
    }
    
    protected Integer getDsIndex(final String dsName) {
        return m_dsNames.get(dsName);
    }

    protected int getRowNumberForTimestamp(final long timestamp) throws IOException {
        final long arcStep = getNativeStep();
        final long offset = timestamp - getStartTime();
        final int row = (int)(offset/arcStep);
//        final long offsetStep = offset % arcStep;
//        final long time = timestamp - offsetStep;
//        if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "timestamp = %d, aliased timestamp = %d, offset = %d, offsetStep = %d, row = %d", timestamp, time, offset, offsetStep, row);
        return row;
    }

    protected boolean isValidTimestamp(final long timestamp) throws IOException {
        return getStartTime() <= timestamp && timestamp < getEndTime() + getNativeStep();
    }

    public List<RrdEntry> getData(final long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>(getRows());
        for (long time = getStartTime(); time < getEndTime() + getNativeStep(); time += step) {
            entries.add(getDataAt(time));
        }
        LogUtils.debugf(this, "total entries: %d", entries.size());
        return entries;
    }

    public int getRows() throws IOException {
        return (int)((getEndTime() - getStartTime()) / getNativeStep());
    }

    /*
    public abstract long getStartTime() throws IOException;
    public abstract long getEndTime() throws IOException;
    public abstract long getNativeStep() throws IOException;
    public abstract long getRows() throws IOException;
    public abstract RrdEntry getDataAt(long timestamp) throws IOException;
    public abstract List<RrdEntry> getData(long step) throws IOException;
    */
    
}
