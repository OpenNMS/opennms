/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.pagesequence;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Currently only used for HTTP form parameters.
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_key","m_value"})
public class Parameter implements Serializable {
    private static final long serialVersionUID = 5655167778463737674L;

    @XmlAttribute(name="key")
    private String m_key;

    @XmlAttribute(name="value")
    private String m_value;

    public Parameter() {
        super();
    }

    public String getKey() {
        return m_key;
    }

    public String getValue() {
        return m_value;
    }

    public void setKey(final String key) {
        m_key = key == null? null : key.intern();
    }

    public void setValue(final String value) {
        m_value = value == null? null : value.intern();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
        if (!(obj instanceof Parameter)) {
            return false;
        }
        final Parameter other = (Parameter) obj;
        if (m_key == null) {
            if (other.m_key != null) {
                return false;
            }
        } else if (!m_key.equals(other.m_key)) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) {
                return false;
            }
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Parameter [key=" + m_key + ", value=" + m_value + "]";
    }

}
