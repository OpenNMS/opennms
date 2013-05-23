package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * IpRange class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class IpRange implements Serializable, Comparable<IpRange> {
    private static final long serialVersionUID = -982213514208208854L;

    @XmlAttribute(name = "begin", required = true)
    private String m_begin;

    @XmlAttribute(name = "end", required = true)
    private String m_end;

    public IpRange() {

    }

    public IpRange(IpRange copy) {
        if (copy.m_begin != null) {
            m_begin = new String(copy.m_begin);
        }
        if (copy.m_end != null) {
            m_end = new String(copy.m_end);
        }
    }

    public IpRange(String begin, String end) {
        m_begin = begin;
        m_end = end;
    }

    @XmlTransient
    public String getBegin() {
        return m_begin;
    }

    public void setBegin(String begin) {
        m_begin = begin;
    }

    @XmlTransient
    public String getEnd() {
        return m_end;
    }

    public void setEnd(String end) {
        m_end = end;
    }

    @Override
    public int compareTo(IpRange obj) {
        return new CompareToBuilder()
            .append(getBegin(), obj.getBegin())
            .append(getEnd(), obj.getEnd())
            .toComparison();
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
        if (obj instanceof IpRange) {
            IpRange other = (IpRange) obj;
            return new EqualsBuilder()
                .append(getBegin(), other.getBegin())
                .append(getEnd(), other.getEnd())
                .isEquals();
        }
        return false;
    }
}
