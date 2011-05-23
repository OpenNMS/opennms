package org.opennms.netmgt.config.invd.wmi;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="wmi-asset-property")
public class WmiAssetProperty implements Serializable, Comparable<WmiAssetProperty> {
	private static final long serialVersionUID = -1604361739472097046L;

	public WmiAssetProperty() { }
	
	public WmiAssetProperty(String name, String property) {
		this.m_name = name;
		this.m_wmiProperty = property;
	}
	
	@XmlAttribute(name="name",required=true)
	private String m_name;
	
	@XmlAttribute(name="wmi-property",required=true)
	private String m_wmiProperty;

	@XmlTransient
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@XmlTransient
	public String getWmiProperty() {
		return m_wmiProperty;
	}

	public void setWmiProperty(String wmiProperty) {
		m_wmiProperty = wmiProperty;
	}

	public int compareTo(WmiAssetProperty obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getWmiProperty(), obj.getWmiProperty())
            .toComparison();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WmiAssetProperty) {
        	WmiAssetProperty other = (WmiAssetProperty) obj;
            return new EqualsBuilder()
            	.append(getName(), other.getName())
            	.append(getWmiProperty(), other.getWmiProperty())
                .isEquals();
        }
        return false;
    }
}
