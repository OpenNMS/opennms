package org.opennms.netmgt.config.invd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class InvdIncludeRange implements Serializable, Comparable<InvdIncludeRange> {
	private static final long serialVersionUID = -7896946948510521309L;

	@XmlAttribute(name="begin",required=true)
	private String m_begin;
	
	@XmlAttribute(name="end",required=true)
	private String m_end;

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

	public int compareTo(InvdIncludeRange obj) {
        return new CompareToBuilder()
            .append(getBegin(), obj.getBegin())
            .append(getEnd(), obj.getEnd())            
            .toComparison();
    }
	
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdIncludeRange) {
        	InvdIncludeRange other = (InvdIncludeRange) obj;
            return new EqualsBuilder()
            	.append(getBegin(), other.getBegin())
            	.append(getEnd(), other.getEnd())            	
                .isEquals();
        }
        return false;
    }
	
}
