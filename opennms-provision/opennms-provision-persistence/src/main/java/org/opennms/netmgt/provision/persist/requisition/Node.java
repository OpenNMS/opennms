package org.opennms.netmgt.provision.persist.requisition;

import java.util.ArrayList;
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
public class Node {

    @XmlElement(name = "interface")
    protected List<Interface> m_interfaces;
    @XmlElement(name="category")
    protected List<Category> m_categories;
    @XmlElement(name="asset")
    protected List<Asset> m_assets;
    
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

    public List<Interface> getInterfaces() {
        if (m_interfaces == null) {
            m_interfaces = new ArrayList<Interface>();
        }
        return m_interfaces;
    }

    public void setInterfaces(List<Interface> interfaces) {
        m_interfaces = interfaces;
    }

    public List<Category> getCategories() {
        if (m_categories == null) {
            m_categories = new ArrayList<Category>();
        }
        return m_categories;
    }

    public void setCategories(List<Category> categories) {
        m_categories = categories;
    }

    public List<Asset> getAssets() {
        if (m_assets == null) {
            m_assets = new ArrayList<Asset>();
        }
        return m_assets;
    }

    public void setAssets(List<Asset> assets) {
        m_assets = assets;
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
