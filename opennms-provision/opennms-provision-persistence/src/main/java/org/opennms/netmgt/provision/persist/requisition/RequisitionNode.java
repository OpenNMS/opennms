package org.opennms.netmgt.provision.persist.requisition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_interfaces", "m_categories", "m_assets" })
@XmlRootElement(name = "node")
public class RequisitionNode {

    @XmlElement(name = "interface")
    protected List<RequisitionInterface> m_interfaces;
    @XmlElement(name="category")
    protected List<RequisitionCategory> m_categories;
    @XmlElement(name="asset")
    protected List<RequisitionAsset> m_assets;
    
    @XmlAttribute
    protected String building;

    @XmlAttribute
    protected String city;

    @XmlAttribute(name = "foreign-id", required = true)
    protected String foreignId;

    @XmlAttribute(name = "node-label", required = true)
    protected String nodeLabel;
    
    @XmlAttribute(name = "parent-foreign-id")
    protected String parentForeignId;

    @XmlAttribute(name = "parent-node-label")
    protected String parentNodeLabel;

    public List<RequisitionInterface> getInterfaces() {
        if (m_interfaces == null) {
            m_interfaces = new ArrayList<RequisitionInterface>();
        }
        return m_interfaces;
    }

    public void setInterfaces(List<RequisitionInterface> interfaces) {
        m_interfaces = interfaces;
    }

    public RequisitionInterface getInterface(String ipAddress) {
        if (m_interfaces != null) {
            for (RequisitionInterface iface : m_interfaces) {
                if (iface.getIpAddr().equals(ipAddress)) {
                    return iface;
                }
            }
            
        }
        return null;
    }

    public void deleteInterface(String ipAddress) {
        if (m_interfaces != null) {
            Iterator<RequisitionInterface> i = m_interfaces.iterator();
            while (i.hasNext()) {
                RequisitionInterface iface = i.next();
                if (iface.getIpAddr().equals(ipAddress)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    public void putInterface(RequisitionInterface iface) {
        Iterator<RequisitionInterface> iterator = m_interfaces.iterator();
        while (iterator.hasNext()) {
            RequisitionInterface existingIface = iterator.next();
            if (existingIface.getIpAddr().equals(iface.getIpAddr())) {
                iterator.remove();
            }
        }
        m_interfaces.add(iface);
    }

    public List<RequisitionCategory> getCategories() {
        if (m_categories == null) {
            m_categories = new ArrayList<RequisitionCategory>();
        }
        return m_categories;
    }

    public void setCategories(List<RequisitionCategory> categories) {
        m_categories = categories;
    }

    public RequisitionCategory getCategory(String category) {
        if (m_categories != null) {
            for (RequisitionCategory cat : m_categories) {
                if (cat.getName().equals(category)) {
                    return cat;
                }
            }
            
        }
        return null;
    }

    public void deleteCategory(String category) {
        if (m_categories != null) {
            Iterator<RequisitionCategory> i = m_categories.iterator();
            while (i.hasNext()) {
                RequisitionCategory cat = i.next();
                if (cat.getName().equals(category)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    public void putCategory(RequisitionCategory category) {
        Iterator<RequisitionCategory> iterator = m_categories.iterator();
        while (iterator.hasNext()) {
            RequisitionCategory existing = iterator.next();
            if (existing.getName().equals(category.getName())) {
                iterator.remove();
            }
        }
        m_categories.add(category);
    }

    public List<RequisitionAsset> getAssets() {
        if (m_assets == null) {
            m_assets = new ArrayList<RequisitionAsset>();
        }
        return m_assets;
    }

    public void setAssets(List<RequisitionAsset> assets) {
        m_assets = assets;
    }

    public RequisitionAsset getAsset(String name) {
        if (m_assets != null) {
            for (RequisitionAsset asset : m_assets) {
                if (asset.getName().equals(name)) {
                    return asset;
                }
            }
            
        }
        return null;
    }

    public void deleteAsset(String name) {
        if (m_assets != null) {
            Iterator<RequisitionAsset> i = m_assets.iterator();
            while (i.hasNext()) {
                RequisitionAsset asset = i.next();
                if (asset.getName().equals(name)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    public void putAsset(RequisitionAsset asset) {
        Iterator<RequisitionAsset> iterator = m_assets.iterator();
        while (iterator.hasNext()) {
            RequisitionAsset existing = iterator.next();
            if (existing.getName().equals(asset.getName())) {
                iterator.remove();
            }
        }
        m_assets.add(asset);
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String value) {
        building = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String value) {
        city = value;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String value) {
        foreignId = value;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String value) {
        nodeLabel = value;
    }

    public String getParentForeignId() {
        return parentForeignId;
    }

    public void setParentForeignId(String value) {
        parentForeignId = value;
    }

    public String getParentNodeLabel() {
        return parentNodeLabel;
    }

    public void setParentNodeLabel(String value) {
        parentNodeLabel = value;
    }

}
