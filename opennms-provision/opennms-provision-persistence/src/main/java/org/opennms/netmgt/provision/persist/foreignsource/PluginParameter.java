package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.provision.support.PluginWrapper;

/**
 * <p>PluginParameter class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_key", "m_value" })
public class PluginParameter implements Serializable, Comparable<PluginParameter> {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="key")
    private String m_key = null;

    @XmlAttribute(name="value")
    private String m_value = null;

    @XmlTransient
    private PluginConfig m_parent = null;

    /**
     * <p>Constructor for PluginParameter.</p>
     */
    public PluginParameter() {
    }
    
    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public PluginParameter(String key, String value) {
        m_key = key;
        m_value = value;
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param e a {@link java.util.Map.Entry} object.
     */
    public PluginParameter(Entry<String, String> e) {
        m_key = e.getKey();
        m_value = e.getValue();
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public PluginParameter(PluginConfig pluginConfig, String key, String value) {
        this(key, value);
        m_parent = pluginConfig;
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param set a {@link java.util.Map.Entry} object.
     */
    public PluginParameter(PluginConfig pluginConfig, Entry<String, String> set) {
        this(set);
        m_parent = pluginConfig;
    }

    /**
     * <p>setPluginConfig</p>
     *
     * @param pc a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void setPluginConfig(PluginConfig pc) {
        m_parent = pc;
    }

    /**
     * <p>getKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKey() {
        return m_key;
    }
    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValue() {
        return m_value;
    }
    /**
     * <p>setKey</p>
     *
     * @param key a {@link java.lang.String} object.
     */
    public void setKey(String key) {
        m_key = key;
    }
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setValue(String value) {
        m_value = value;
    }

    /**
     * <p>getAvailableParameterKeys</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getAvailableParameterKeys() {
        Set<String> keys = new TreeSet<String>();
        if (m_parent != null) {
            try {
                PluginWrapper pw = new PluginWrapper(m_parent.getPluginClass());
                keys = pw.getOptionalKeys();
                for (PluginParameter p : m_parent.getParameters()) {
                    if (p.getKey() != getKey()) {
                        keys.remove(p.getKey());
                    }
                }
            } catch (ClassNotFoundException e) {
                // we just let it return the empty set
            }
        }
        return keys;
    }

    /**
     * <p>compareTo</p>
     *
     * @param obj a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginParameter} object.
     * @return a int.
     */
    public int compareTo(PluginParameter obj) {
        return new CompareToBuilder()
            .append(getKey(), obj.getKey())
            .toComparison();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("key", getKey())
            .append("value", getValue())
            .toString();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(701, 1873)
            .append(getKey())
            .append(getValue())
            .toHashCode();
      }

}
