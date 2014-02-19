/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * A rule which addresses belonging to this package must
 *  pass. This package is applied only to addresses that pass this
 *  filter.
 */

@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.NONE)
public class Filter implements Serializable {
    private static final long serialVersionUID = -5866771380302409645L;

    @XmlAttribute(name="name")
    private String m_name;

    @XmlValue()
    private String m_content;

    public Filter() {
        super();
    }

    public Filter(final String filter) {
        this();
        m_content = filter;
    }

    public String getName() {
        return m_content;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public String getContent() {
        return m_content == null? "" : m_content;
    }

    public void setContent(final String content) {
        m_content = content;
    }

    @Override
    public int hashCode() {
        final int prime = 683;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_content == null) ? 0 : m_content.hashCode());
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
        if (!(obj instanceof Filter)) {
            return false;
        }
        final Filter other = (Filter) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_content == null) {
            if (other.m_content != null) {
                return false;
            }
        } else if (!m_content.equals(other.m_content)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Filter [name=" + m_name + ", content=" + m_content + "]";
    }

}
