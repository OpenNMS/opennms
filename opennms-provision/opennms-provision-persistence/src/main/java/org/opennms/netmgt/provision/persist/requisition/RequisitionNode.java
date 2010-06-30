package org.opennms.netmgt.provision.persist.requisition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>RequisitionNode class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_interfaces", "m_categories", "m_assets" })
@XmlRootElement(name = "node")
public class RequisitionNode {

    @XmlElement(name = "interface")
    protected List<RequisitionInterface> m_interfaces = new ArrayList<RequisitionInterface>();
    @XmlElement(name="category")
    protected List<RequisitionCategory> m_categories = new ArrayList<RequisitionCategory>();
    @XmlElement(name="asset")
    protected List<RequisitionAsset> m_assets = new ArrayList<RequisitionAsset>();
    
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

    /**
     * <p>getInterfaceCount</p>
     *
     * @return a int.
     */
    @XmlTransient
    public int getInterfaceCount() {
        return (m_interfaces == null)? 0 : m_interfaces.size();
    }

    /* backwards-compat with ModelImport */
    /**
     * <p>getInterface</p>
     *
     * @return an array of {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} objects.
     */
    @XmlTransient
    public RequisitionInterface[] getInterface() {
        return getInterfaces().toArray(new RequisitionInterface[] {});
    }

    /**
     * <p>getInterfaces</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RequisitionInterface> getInterfaces() {
        if (m_interfaces == null) {
            m_interfaces = new ArrayList<RequisitionInterface>();
        }
        return m_interfaces;
    }

    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces a {@link java.util.List} object.
     */
    public void setInterfaces(List<RequisitionInterface> interfaces) {
        m_interfaces = interfaces;
    }

    /**
     * <p>getInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
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

    /**
     * <p>removeInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public void removeInterface(RequisitionInterface iface) {
        if (m_interfaces != null) {
            Iterator<RequisitionInterface> i = m_interfaces.iterator();
            while (i.hasNext()) {
                RequisitionInterface ri = i.next();
                if (ri.getIpAddr().equals(iface.getIpAddr())) {
                    i.remove();
                    break;
                }
            }
        }
    }

    /**
     * <p>deleteInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
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

    /**
     * <p>insertInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public void insertInterface(RequisitionInterface iface) {
        Iterator<RequisitionInterface> iterator = m_interfaces.iterator();
        while (iterator.hasNext()) {
            RequisitionInterface existingIface = iterator.next();
            if (existingIface.getIpAddr().equals(iface.getIpAddr())) {
                iterator.remove();
            }
        }
        m_interfaces.add(0, iface);
    }

    /**
     * <p>putInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
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

    /**
     * <p>getCategoryCount</p>
     *
     * @return a int.
     */
    @XmlTransient
    public int getCategoryCount() {
        return (m_categories == null)? 0 : m_categories.size();
    }

    /* backwards compatibility with ModelImport */
    /**
     * <p>getCategory</p>
     *
     * @return an array of {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} objects.
     */
    @XmlTransient
    public RequisitionCategory[] getCategory() {
        return m_categories.toArray(new RequisitionCategory[] {});
    }

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RequisitionCategory> getCategories() {
        if (m_categories == null) {
            m_categories = new ArrayList<RequisitionCategory>();
        }
        return m_categories;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<RequisitionCategory> categories) {
        m_categories = categories;
    }

    /**
     * <p>getCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
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

    /**
     * <p>removeCategory</p>
     *
     * @param cat a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void removeCategory(RequisitionCategory cat) {
        if (m_assets != null) {
            Iterator<RequisitionCategory> i = m_categories.iterator();
            while (i.hasNext()) {
                RequisitionCategory a = i.next();
                if (a.getName().equals(cat.getName())) {
                    i.remove();
                    break;
                }
            }
        }
    }

    /**
     * <p>deleteCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void deleteCategory(RequisitionCategory category) {
        if (m_categories != null) {
            Iterator<RequisitionCategory> i = m_categories.iterator();
            while (i.hasNext()) {
                RequisitionCategory cat = i.next();
                if (cat.getName().equals(category.getName())) {
                    i.remove();
                    break;
                }
            }
        }
    }

    /**
     * <p>deleteCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
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

    /**
     * <p>insertCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void insertCategory(RequisitionCategory category) {
        Iterator<RequisitionCategory> iterator = m_categories.iterator();
        while (iterator.hasNext()) {
            RequisitionCategory existing = iterator.next();
            if (existing.getName().equals(category.getName())) {
                iterator.remove();
            }
        }
        m_categories.add(0, category);
    }

    /**
     * <p>putCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
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

    /**
     * <p>getAssetCount</p>
     *
     * @return a int.
     */
    @XmlTransient
    public int getAssetCount() {
        return (m_assets == null)? 0 : m_assets.size();
    }
    
    /* backwards compatibility with ModelImport */
    /**
     * <p>getAsset</p>
     *
     * @return an array of {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} objects.
     */
    @XmlTransient
    public RequisitionAsset[] getAsset() {
        return m_assets.toArray(new RequisitionAsset[] {});
    }

    /**
     * <p>getAssets</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RequisitionAsset> getAssets() {
        if (m_assets == null) {
            m_assets = new ArrayList<RequisitionAsset>();
        }
        return m_assets;
    }

    /**
     * <p>setAssets</p>
     *
     * @param assets a {@link java.util.List} object.
     */
    public void setAssets(List<RequisitionAsset> assets) {
        m_assets = assets;
    }

    /**
     * <p>getAsset</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
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

    /**
     * <p>removeAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public void removeAsset(RequisitionAsset asset) {
        if (m_assets != null) {
            Iterator<RequisitionAsset> i = m_assets.iterator();
            while (i.hasNext()) {
                RequisitionAsset a = i.next();
                if (a.getName().equals(asset.getName())) {
                    i.remove();
                    break;
                }
            }
        }
    }
    
    /**
     * <p>deleteAsset</p>
     *
     * @param name a {@link java.lang.String} object.
     */
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

    /**
     * <p>insertAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public void insertAsset(RequisitionAsset asset) {
        Iterator<RequisitionAsset> iterator = m_assets.iterator();
        while (iterator.hasNext()) {
            RequisitionAsset existing = iterator.next();
            if (existing.getName().equals(asset.getName())) {
                iterator.remove();
            }
        }
        m_assets.add(0, asset);
    }
    
    /**
     * <p>putAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
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

    /**
     * <p>Getter for the field <code>building</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBuilding() {
        return building;
    }

    /**
     * <p>Setter for the field <code>building</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setBuilding(String value) {
        building = value;
    }

    /**
     * <p>Getter for the field <code>city</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCity() {
        return city;
    }

    /**
     * <p>Setter for the field <code>city</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setCity(String value) {
        city = value;
    }

    /**
     * <p>Getter for the field <code>foreignId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        return foreignId;
    }

    /**
     * <p>Setter for the field <code>foreignId</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setForeignId(String value) {
        foreignId = value;
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return nodeLabel;
    }

    /**
     * <p>Setter for the field <code>nodeLabel</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setNodeLabel(String value) {
        nodeLabel = value;
    }

    /**
     * <p>Getter for the field <code>parentForeignId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentForeignId() {
        return parentForeignId;
    }

    /**
     * <p>Setter for the field <code>parentForeignId</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setParentForeignId(String value) {
        parentForeignId = value;
    }

    /**
     * <p>Getter for the field <code>parentNodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentNodeLabel() {
        return parentNodeLabel;
    }

    /**
     * <p>Setter for the field <code>parentNodeLabel</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setParentNodeLabel(String value) {
        parentNodeLabel = value;
    }

}
