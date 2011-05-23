package org.opennms.netmgt.config.invd.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="wmi-invscan-config")
public class WmiInvScanConfig implements Serializable, Comparable<WmiInvScanConfig> {
	private static final long serialVersionUID = -3151219955710189299L;
	private static final WmiInventory[] OF_INVENTORIES = new WmiInventory[0];
	
	@XmlElement(name="wmi-inventory")
	private List<WmiInventory> m_wmiInventories = new ArrayList<WmiInventory>();

	@XmlTransient
	public List<WmiInventory> getWmiInventories() {
		return m_wmiInventories;
	}

	public void setWmiInventories(List<WmiInventory> wmiInventories) {
		m_wmiInventories = wmiInventories;
	}

	public void addWmiInventory(WmiInventory inventory) {
		m_wmiInventories.add(inventory);
	}
	
	public int compareTo(WmiInvScanConfig obj) {
        return new CompareToBuilder()
            .append(getWmiInventories().toArray(OF_INVENTORIES), obj.getWmiInventories().toArray(OF_INVENTORIES))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WmiInvScanConfig) {
        	WmiInvScanConfig other = (WmiInvScanConfig) obj;
            return new EqualsBuilder()
            	.append(getWmiInventories().toArray(OF_INVENTORIES), other.getWmiInventories().toArray(OF_INVENTORIES))
                .isEquals();
        }
        return false;
    }
}
