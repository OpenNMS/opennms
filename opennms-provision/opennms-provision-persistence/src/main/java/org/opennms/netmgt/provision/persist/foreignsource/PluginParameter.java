package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
public class PluginParameter implements Serializable, Comparable<PluginParameter> {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="key")
    private String m_key = null;

    @XmlAttribute(name="value")
    private String m_value = null;

    public PluginParameter() {
    }
    
    public PluginParameter(String key, String value) {
        m_key = key;
        m_value = value;
    }

    public PluginParameter(Entry<String, String> e) {
        m_key = e.getKey();
        m_value = e.getValue();
    }

    public String getKey() {
        return m_key;
    }
    public String getValue() {
        return m_value;
    }
    public void setKey(String key) {
        m_key = key;
    }
    public void setValue(String value) {
        m_value = value;
    }

    public int compareTo(PluginParameter obj) {
        return new CompareToBuilder()
            .append(getKey(), obj.getKey())
            .toComparison();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("key", getKey())
            .append("value", getValue())
            .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PluginParameter) {
            PluginParameter other = (PluginParameter) obj;
            return new EqualsBuilder()
                .append(getKey(), other.getKey())
                .append(getValue(), other.getValue())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(701, 1873)
            .append(getKey())
            .append(getValue())
            .toHashCode();
      }

}
