package org.opennms.netmgt.config.invd.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class WmiCategory implements Serializable, Comparable<WmiCategory> {
	private static final long serialVersionUID = 2396858314673353357L;

	private static final WmiAsset[] OF_WMI_ASSETS = new WmiAsset[0];
	
	public WmiCategory() { }
	
	public WmiCategory(String name) {
		m_name = name;
	}
	
	@XmlAttribute(name="name", required=true)
	private String m_name;
	
	@XmlElement(name="wmi-asset")
	private List<WmiAsset> m_wmiAssets = new ArrayList<WmiAsset>();

	@XmlTransient
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@XmlTransient
	public List<WmiAsset> getWmiAssets() {
		return m_wmiAssets;
	}

	public void setWmiAssets(List<WmiAsset> wmiAssets) {
		m_wmiAssets = wmiAssets;
	}
	
	public void addWmiAsset(WmiAsset asset) {
		m_wmiAssets.add(asset);
	}
	
	public int compareTo(WmiCategory obj) {
        return new CompareToBuilder()
        	.append(getName(), obj.getName())
            .append(getWmiAssets().toArray(OF_WMI_ASSETS), obj.getWmiAssets().toArray(OF_WMI_ASSETS))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WmiCategory) {
        	WmiCategory other = (WmiCategory) obj;
            return new EqualsBuilder()
            	.append(getWmiAssets().toArray(OF_WMI_ASSETS), other.getWmiAssets().toArray(OF_WMI_ASSETS))
                .isEquals();
        }
        return false;
    }
}
