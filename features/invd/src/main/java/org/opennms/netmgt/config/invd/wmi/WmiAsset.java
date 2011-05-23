package org.opennms.netmgt.config.invd.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class WmiAsset implements Serializable, Comparable<WmiAsset> {
	private static final long serialVersionUID = 3776699102623990953L;

	private static final WmiAssetProperty[] OF_WMI_ASSET_PROPS = new WmiAssetProperty[0];
	
	public WmiAsset() { }
	
	public WmiAsset(String name, String wmiClass, String prop, Integer recheck) {
		m_name = name;
		m_wmiClass = wmiClass;
		m_nameProperty = prop;
		m_recheckInterval = recheck;
	}
	
	@XmlAttribute(name="name", required=true)
	private String m_name;
	
	@XmlAttribute(name="wmiClass", required=true)
	private String m_wmiClass;
	
	@XmlAttribute(name="nameProperty", required=true)
	private String m_nameProperty;
	
	@XmlAttribute(name="recheckInterval", required=true)
	private Integer m_recheckInterval;
	
	@XmlElement(name="wmi-asset-property")
	private List<WmiAssetProperty> m_wmiAssetProperties = new ArrayList<WmiAssetProperty>();
	
	@XmlTransient
	public String getWmiClass() {
		return m_wmiClass;
	}

	public void setWmiClass(String wmiClass) {
		m_wmiClass = wmiClass;
	}

	@XmlTransient
	public String getNameProperty() {
		return m_nameProperty;
	}

	public void setNameProperty(String nameProperty) {
		m_nameProperty = nameProperty;
	}

	@XmlTransient
	public Integer getRecheckInterval() {
		return m_recheckInterval;
	}

	public void setRecheckInterval(Integer recheckInterval) {
		m_recheckInterval = recheckInterval;
	}
	
	@XmlTransient
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@XmlTransient
	public List<WmiAssetProperty> getWmiAssetProperties() {
		return m_wmiAssetProperties;
	}

	public void setWmiAssetProperties(List<WmiAssetProperty> wmiAssetProperties) {
		m_wmiAssetProperties = wmiAssetProperties;
	}
	
	public void addWmiAssetProperty(WmiAssetProperty assetProp) {
		m_wmiAssetProperties.add(assetProp);
	}

	public int compareTo(WmiAsset obj) {
        return new CompareToBuilder()
        	.append(getName(), obj.getName())
        	.append(getWmiClass(), obj.getWmiClass())
        	.append(getNameProperty(), obj.getNameProperty())
        	.append(getRecheckInterval(), obj.getRecheckInterval())
        	.append(getWmiAssetProperties().toArray(OF_WMI_ASSET_PROPS), obj.getWmiAssetProperties().toArray(OF_WMI_ASSET_PROPS))
            .toComparison();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WmiAsset) {
        	WmiAsset other = (WmiAsset) obj;
            return new EqualsBuilder()
            	.append(getName(), other.getName())
            	.append(getWmiClass(), other.getWmiClass())
        		.append(getNameProperty(), other.getNameProperty())
        		.append(getRecheckInterval(), other.getRecheckInterval())
        		.append(getWmiAssetProperties().toArray(OF_WMI_ASSET_PROPS), other.getWmiAssetProperties().toArray(OF_WMI_ASSET_PROPS))
                .isEquals();
        }
        return false;
    }
    
    
    
                	
}
