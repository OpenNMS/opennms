package org.opennms.tools.rrd.converter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.LogUtils;

class RrdEntry {
    public class DsValue {
        final String m_key;
        final Double m_value;
        
        public DsValue(final String key, final Double value) {
            m_key = key;
            m_value = value;
        }
        
        public String getKey() {
            return m_key;
        }
        
        public Double getValue() {
            return m_value;
        }
    }

    private TreeMap<String, Double> m_entryMap;
    private long m_timestamp;
    private List<String> m_dsNames;

    RrdEntry(final long timestamp, final List<String> dsNames) {
        m_entryMap = new TreeMap<String,Double>();
        m_timestamp = timestamp;
        m_dsNames = dsNames;
    }
    
    public Double getValue(final String dsName) {
        return m_entryMap.get(dsName);
    }

    protected void setValue(final String dsName, final double sample) {
        Map<String,Double> dsEntries = m_entryMap;
        dsEntries.put(dsName, sample);
    }

    public long getTimestamp() {
        return m_timestamp;
    }

    public Set<DsValue> getEntries() {
        final Set<DsValue> dsValues = new LinkedHashSet<DsValue>();
        for (final String dsName : m_dsNames) {
            final DsValue dsValue = new DsValue(dsName,m_entryMap.get(dsName));
            dsValues.add(dsValue);
        }
        return dsValues;
    }
    
    public List<String> getDsNames() {
        return m_dsNames;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("timestamp", m_timestamp)
            .append("entries", m_entryMap)
            .toString();
    }

    public void coalesceWith(final RrdEntry otherEntry) {
        for (final String key : m_dsNames) {
            final Double myValue = m_entryMap.get(key);
            final Double otherValue = otherEntry.getValue(key);
            if (LogUtils.isTraceEnabled(this)) LogUtils.tracef(this, "key = %s, myValue = %s, otherValue = %s", key, myValue, otherValue);
            setValue(key, coalesce(myValue, otherValue));
        }
    }
    
    protected Double coalesce(final Double a, final Double b) {
        if (a != null && !Double.isNaN(a)) return a;
        if (b == null) return Double.NaN;
        return b;
    }
}