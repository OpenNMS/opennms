/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist.requisition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;


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
    
    @XmlAttribute(name = "parent-foreign-source")
    protected String parentForeignSource;

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
        return m_interfaces;
    }

    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces a {@link java.util.List} object.
     */
    public void setInterfaces(Collection<RequisitionInterface> interfaces) {
        if (interfaces == null) {
            interfaces = new TreeSet<RequisitionInterface>();
        }
        m_interfaces.clear();
        m_interfaces.addAll(interfaces);
    }

    /**
     * <p>getInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public RequisitionInterface getInterface(String ipAddress) {
        for (RequisitionInterface iface : m_interfaces) {
            if (iface.getIpAddr().equals(ipAddress)) {
                return iface;
            }
        }
        return null;
    }

    /**
     * <p>removeInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public void deleteInterface(RequisitionInterface iface) {
        m_interfaces.remove(iface);
    }

    /**
     * <p>deleteInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void deleteInterface(String ipAddress) {
        Iterator<RequisitionInterface> i = m_interfaces.iterator();
        while (i.hasNext()) {
            RequisitionInterface iface = i.next();
            if (iface.getIpAddr().equals(ipAddress)) {
                i.remove();
                break;
            }
        }
    }

    /**
     * <p>putInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public void putInterface(RequisitionInterface iface) {
        m_interfaces.remove(iface);
        m_interfaces.add(0, iface);
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
        return m_categories;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(Collection<RequisitionCategory> categories) {
        if (categories == null) {
            categories = new TreeSet<RequisitionCategory>();
        }
        m_categories.clear();
        m_categories.addAll(categories);
    }

    /**
     * <p>getCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public RequisitionCategory getCategory(String category) {
        for (RequisitionCategory cat : m_categories) {
            if (cat.getName().equals(category)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * <p>deleteCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void deleteCategory(RequisitionCategory category) {
        m_categories.remove(category);
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
     * <p>putCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void putCategory(RequisitionCategory category) {
        m_categories.remove(category);
        m_categories.add(0, category);
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
        return m_assets;
    }

    /**
     * <p>setAssets</p>
     *
     * @param assets a {@link java.util.List} object.
     */
    public void setAssets(Collection<RequisitionAsset> assets) {
        if (assets == null) {
            assets = new TreeSet<RequisitionAsset>();
        }
        m_assets.clear();
        m_assets.addAll(assets);
    }

    /**
     * <p>getAsset</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public RequisitionAsset getAsset(String name) {
        for (RequisitionAsset asset : m_assets) {
            if (asset.getName().equals(name)) {
                return asset;
            }
        }
        return null;
    }

    /**
     * <p>deleteAsset</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void deleteAsset(String name) {
        Iterator<RequisitionAsset> i = m_assets.iterator();
        while (i.hasNext()) {
            RequisitionAsset asset = i.next();
            if (asset.getName().equals(name)) {
                i.remove();
                break;
            }
        }
    }

    /**
     * <p>deleteAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public void deleteAsset(RequisitionAsset asset) {
        m_assets.remove(asset);
    }

    /**
     * <p>putAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public void putAsset(RequisitionAsset asset) {
        m_assets.remove(asset);
        m_assets.add(0, asset);
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
     * <p>Getter for the field <code>parentForeignSource</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentForeignSource() {
        return parentForeignSource;
    }

    /**
     * <p>Setter for the field <code>parentForeignSource</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setParentForeignSource(String value) {
        parentForeignSource = value;
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

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("interfaces", m_interfaces)
    		.append("categories", m_categories)
    		.append("assets", m_assets)
    		.append("building", building)
    		.append("city", city)
    		.append("foreign-id", foreignId)
    		.append("node-label", nodeLabel)
    		.append("parent-foreign-source", parentForeignSource)
    		.append("parent-foreign-id", parentForeignId)
    		.append("parent-node-label", parentNodeLabel)
    		.toString();
    }
}
