package org.opennms.netmgt.config.invd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="scanner")
public class InvdScanner implements Serializable, Comparable<InvdScanner> {
	private static final long serialVersionUID = -7715022643629461310L;

	@XmlAttribute(name="service",required=true)
	private String m_service;
	
	@XmlAttribute(name="class-name", required=true)
	private String m_className;

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
	
	public int compareTo(InvdScanner obj) {
        return new CompareToBuilder()
            .append(getService(), obj.getService())
            .append(getClassName(), obj.getClassName())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdScanner) {
        	InvdScanner other = (InvdScanner) obj;
            return new EqualsBuilder()
            	.append(getService(), other.getService())
            	.append(getClassName(), other.getClassName())
                .isEquals();
        }
        return false;
    }
}
