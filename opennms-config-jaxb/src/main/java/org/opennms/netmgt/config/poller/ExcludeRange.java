package org.opennms.netmgt.config.poller;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Range of addresses to be excluded from this
 *  package.
 */

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class ExcludeRange implements Serializable {
    private static final long serialVersionUID = 3711255115020435826L;

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

    public ExcludeRange() {
        super();
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
        if (!(obj instanceof ExcludeRange)) {
            return false;
        }
        ExcludeRange other = (ExcludeRange) obj;
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
        return "ExcludeRange[begin=" + m_begin + ",end=" + m_end + "]";
    }
}
