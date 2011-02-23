package org.opennms.tools.rrd.converter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;

class RrdEntry {
    private TreeMap<String, Double> m_entryMap;
    private long m_timestamp;
    private List<String> m_dsNames;

    RrdEntry(final long timestamp, final List<String> dsNames) {
        m_entryMap = new TreeMap<String,Double>();
        m_timestamp = timestamp;
        m_dsNames = dsNames;
    }
    
    public Map<String,Double> getMap() {
        return m_entryMap;
    }

    public double getValue(final String dsName) {
        return m_entryMap.get(dsName);
    }

    protected void setValue(final String dsName, final double sample) {
        Map<String,Double> dsEntries = getMap();
        dsEntries.put(dsName, sample);
    }

    public long getTimestamp() {
        return m_timestamp;
    }

    protected void createSample(final RrdDb outputRrd) throws IOException, RrdException {
        final Sample s = outputRrd.createSample(m_timestamp);
        final double[] values = new double[m_dsNames.size()];
        for (int i = 0; i < m_dsNames.size(); i++) {
            final String string = m_dsNames.get(i);
            if (string != null) {
                final Double value = getMap().get(string);
                if (value != null) values[i] = value;
            }
        }
        s.setValues(values);
        s.update();
    }
}