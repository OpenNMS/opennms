package org.opennms.netmgt.config.invd.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="wmi-inventory")
public class WmiInventory implements Serializable, Comparable<WmiInventory> {
	private static final long serialVersionUID = 3764639575400186064L;
	private static final WmiCategory[] OF_WMI_CATEGORIES = new WmiCategory[0];
	
	public WmiInventory() { }
	
	public WmiInventory(String name) {
		m_name = name;
	}
	
	@XmlAttribute(name="name", required=true)
	private String m_name;
	
	@XmlElement(name="wmi-category")
	private List<WmiCategory> m_wmiCategories = new ArrayList<WmiCategory>();

	@XmlTransient
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@XmlTransient
	public List<WmiCategory> getWmiCategories() {
		return m_wmiCategories;
	}

	public void setWmiAssets(List<WmiCategory> wmiCategories) {
		m_wmiCategories = wmiCategories;
	}
	
	public void addWmiCategory(WmiCategory category) {
		m_wmiCategories.add(category);
	}
	
	public int compareTo(WmiInventory obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getWmiCategories().toArray(OF_WMI_CATEGORIES), obj.getWmiCategories().toArray(OF_WMI_CATEGORIES))
            .toComparison();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WmiInventory) {
        	WmiInventory other = (WmiInventory) obj;
            return new EqualsBuilder()
            	.append(getName(), other.getName())
            	.append(getWmiCategories().toArray(OF_WMI_CATEGORIES), other.getWmiCategories().toArray(OF_WMI_CATEGORIES))            	
                .isEquals();
        }
        return false;
    }
}
