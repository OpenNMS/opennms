package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.Assert;

public class SnmpRowResult implements Comparable<SnmpRowResult> {
    private final Map<SnmpObjId, SnmpResult> m_results = new TreeMap<SnmpObjId,SnmpResult>();
    private SnmpInstId m_instance;
    private int m_columnCount;

    public SnmpRowResult(int columnCount, SnmpInstId instance) {
        m_instance = instance;
        m_columnCount = columnCount;
    }

    public boolean isComplete(SnmpObjId... ignoreColumns) {
        if (m_results.size() == m_columnCount) {
            return true;
        } else if (m_results.size() > 0 && ignoreColumns.length > 0) {
            /* 
             * short-circuit if the table result is telling us to consider
             * certain SnmpObjId's as finished
             */
            int total = m_results.size();
            for (SnmpObjId col : ignoreColumns) {
                if (!m_results.containsKey(col)) {
                    total++;
                }
            }
            return total == m_columnCount;
        }
        return false;
    }

    public int getColumnCount() {
        return m_columnCount;
    }

    public List<SnmpResult> getResults() {
        return new ArrayList<SnmpResult>(m_results.values());
    }
    
    public void addResult(SnmpObjId column, SnmpResult result) {
        Assert.isTrue(m_instance.equals(result.getInstance()), "unexpected result "+result+" passed to row with instance "+m_instance);
        m_results.put(column, result);
    }
    

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("columnCount", m_columnCount)
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

    public SnmpInstId getInstance() {
        return m_instance;
    }

    /**
     * @param base
     * @return
     */
    public SnmpValue getValue(SnmpObjId base) {
        for(SnmpResult result : getResults()) {
            if (base.equals(result.getBase())) {
                return result.getValue();
            }
        }
        
        return null;
    }
}
