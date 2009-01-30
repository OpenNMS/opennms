package org.opennms.netmgt.snmp;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SnmpResult implements Comparable<SnmpResult> {
    private static final long serialVersionUID = 1L;

    private final SnmpObjId m_base;
    private final SnmpInstId m_instance;
    private final SnmpValue m_value;
    
    public SnmpResult(SnmpObjId base, SnmpInstId instance, SnmpValue value) {
        m_base = base;
        m_instance = instance;
        m_value = value;
    }

    public SnmpObjId getBase() {
        return m_base;
    }

    public SnmpInstId getInstance() {
        return m_instance;
    }

    public SnmpValue getValue() {
        return m_value;
    }

    public SnmpObjId getAbsoluteInstance() {
        return getBase().append(getInstance());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("base", getBase())
            .append("instance", getInstance())
            .append("value", getValue())
            .toString();
    }

    public int compareTo(SnmpResult other) {
        return new CompareToBuilder()
            .append(getBase(), other.getBase())
            .append(getInstance(), other.getInstance())
            .append(getValue(), other.getValue())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpResult) {
            SnmpResult other = (SnmpResult) obj;
            return new EqualsBuilder()
                .append(getBase(), other.getBase())
                .append(getInstance(), other.getInstance())
                .append(getValue(), other.getValue())
                .isEquals();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getBase())
            .append(getInstance())
            .append(getValue())
            .toHashCode();
    }
}
