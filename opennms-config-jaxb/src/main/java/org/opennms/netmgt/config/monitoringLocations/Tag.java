package org.opennms.netmgt.config.monitoringLocations;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="tag")
@XmlAccessorType(XmlAccessType.NONE)
public class Tag implements Serializable {
    private static final long serialVersionUID = 8901386414645760214L;

    @XmlAttribute(name="name")
    private String m_name;
    
    public Tag() {
    }

    public Tag(final String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }
    
    public void setName(final String name) {
        m_name = name.intern();
    }

    @Override
    public int hashCode() {
        final int prime = 811;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Tag)) {
            return false;
        }
        final Tag other = (Tag) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Tag [name=" + m_name + "]";
    }
}
