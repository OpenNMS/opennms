package org.opennms.netmgt.config.invd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="parameter")
public class InvdServiceParameter implements Serializable, Comparable<InvdServiceParameter> {
	private static final long serialVersionUID = -4048530074872147176L;

	@XmlAttribute(name="key",required=true)
	private String m_key;
	
	@XmlAttribute(name="value",required=true)
	private String m_value;

	@XmlTransient
	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}

	@XmlTransient
	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}
	
	public int compareTo(InvdServiceParameter obj) {
        return new CompareToBuilder()
            .append(getKey(), obj.getKey())
            .append(getValue(), obj.getValue())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdServiceParameter) {
        	InvdServiceParameter other = (InvdServiceParameter) obj;
            return new EqualsBuilder()
            	.append(getKey(), other.getKey())
            	.append(getValue(), other.getValue())
                .isEquals();
        }
        return false;
    }
}
