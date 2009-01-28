package org.opennms.netmgt.snmp;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

public class SnmpRowResult {
    private Map<Integer, SnmpResult> m_results;
    private int m_columns;

    public SnmpRowResult(int columns) {
        m_columns = columns;
        m_results = new HashMap<Integer, SnmpResult>(columns);
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
}
