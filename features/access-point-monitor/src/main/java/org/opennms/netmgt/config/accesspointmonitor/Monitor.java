package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * Monitor class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class Monitor implements Serializable, Comparable<Monitor> {
    private static final long serialVersionUID = -128483514208208854L;

    @XmlAttribute(name = "service")
    private String m_service;

    @XmlAttribute(name = "class-name")
    private String m_className;

    public Monitor() {

    }

    public Monitor(Monitor copy) {
        if (copy.m_service != null) {
            m_service = new String(copy.m_service);
        }
        if (copy.m_className != null) {
            m_className = new String(copy.m_className);
        }
    }

    @XmlTransient
    public String getService() {
        return m_service;
    }

    public void setService(String service) {
        m_service = service;
    }

    @XmlTransient
    public String getClassName() {
        return m_className;
    }

    public void setClassName(String className) {
        m_className = className;
    }

    @Override
    public int compareTo(Monitor obj) {
        return new CompareToBuilder()
            .append(getService(), obj.getService())
            .append(getClassName(), obj.getClassName())
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_className == null) ? 0 : m_className.hashCode());
        result = prime * result + ((m_service == null) ? 0 : m_service.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Monitor) {
            Monitor other = (Monitor) obj;
            return new EqualsBuilder()
                .append(getService(), other.getService())
                .append(getClassName(), other.getClassName())
                .isEquals();
        }
        return false;
    }
}
