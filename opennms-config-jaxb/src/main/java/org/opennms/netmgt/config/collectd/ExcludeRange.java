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

import org.opennms.core.network.IPAddress;

/**
 * Range of addresses to be excluded from this
 *  package
 */

@XmlRootElement(name="exclude-range")
@XmlAccessorType(XmlAccessType.NONE)
public class ExcludeRange implements Serializable {
    private static final long serialVersionUID = -2415273488836500486L;

    /**
     * Starting address of the range
     */
    @XmlAttribute(name="begin")
    private String m_begin;

    /**
     * Ending address of the range
     */
    @XmlAttribute(name="end")
    private String m_end;

    public ExcludeRange() {
        super();
    }

    /**
     * Starting address of the range
     */
    public String getBegin() {
        return m_begin;
    }

    public IPAddress getBeginAsAddress() {
        return m_begin == null? null : new IPAddress(m_begin);
    }

    public void setBegin(final String begin) {
        m_begin = begin;
    }

    /**
     * Ending address of the range
     */
    public String getEnd() {
        return m_end;
    }

    public IPAddress getEndAsAddress() {
        return m_end == null? null : new IPAddress(m_end);
    }

    public void setEnd(final String end) {
        m_end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 101;
        int result = 1;
        result = prime * result + ((m_begin == null) ? 0 : m_begin.hashCode());
        result = prime * result + ((m_end == null) ? 0 : m_end.hashCode());
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
        if (!(obj instanceof ExcludeRange)) {
            return false;
        }
        final ExcludeRange other = (ExcludeRange) obj;
        if (m_begin == null) {
            if (other.m_begin != null) {
                return false;
            }
        } else if (!m_begin.equals(other.m_begin)) {
            return false;
        }
        if (m_end == null) {
            if (other.m_end != null) {
                return false;
            }
        } else if (!m_end.equals(other.m_end)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExcludeRange [begin=" + m_begin + ", end=" + m_end + "]";
    }

}
