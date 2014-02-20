/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.poller;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Range of addresses to be included in this
 *  package.
 */

@XmlRootElement(name="include-range")
@XmlAccessorType(XmlAccessType.NONE)
public class IncludeRange implements Serializable {
    private static final long serialVersionUID = 4572031662471862536L;

    /**
     * Starting address of the range.
     */
    @XmlAttribute(name="begin")
    private String m_begin;

    /**
     * Ending address of the range.
     */
    @XmlAttribute(name="end")
    private String m_end;

    public IncludeRange() {
        super();
    }

    public IncludeRange(final String begin, final String end) {
        this();
        setBegin(begin);
        setEnd(end);
    }

    public String getBegin() {
        return m_begin;
    }
    
    public void setBegin(final String begin) {
        m_begin = begin;
    }
    
    public String getEnd() {
        return m_end;
    }
    
    public void setEnd(final String end) {
        m_end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_begin == null) ? 0 : m_begin.hashCode());
        result = prime * result + ((m_end == null) ? 0 : m_end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IncludeRange)) {
            return false;
        }
        IncludeRange other = (IncludeRange) obj;
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
        return "IncludeRange[begin=" + m_begin + ",end=" + m_end + "]";
    }
}
