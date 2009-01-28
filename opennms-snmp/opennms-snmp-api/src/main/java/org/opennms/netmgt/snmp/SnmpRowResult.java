package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SnmpRowResult implements Comparable<SnmpRowResult> {
    private final Map<Integer, SnmpResult> m_results = new TreeMap<Integer,SnmpResult>();
    private int m_columns;

    public SnmpRowResult(int columns) {
        m_columns = columns;
    }

    public boolean isComplete() {
        if (m_results.size() == m_columns) {
            return true;
        }
        return false;
    }

    public int getColumns() {
        return m_columns;
    }

    public SnmpResult get(int column) {
        return m_results.get(column);
    }

    public List<SnmpResult> getResults() {
        return new ArrayList<SnmpResult>(m_results.values());
    }
    
    public void setResult(Integer column, SnmpResult result) {
        m_results.put(column, result);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("columns", m_columns)
            .append("results", m_results)
            .toString();
    }

    public int compareTo(SnmpRowResult other) {
        return new CompareToBuilder()
            .append(getResults(), other.getResults())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpRowResult) {
            SnmpRowResult other = (SnmpRowResult) obj;
            return new EqualsBuilder()
                .append(getResults(), other.getResults())
                .isEquals();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getResults())
            .toHashCode();
    }
}
