/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.Objects;
import java.util.TreeSet;

import javax.xml.bind.ValidationException;
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

    @XmlAttribute(name = "location")
    protected String m_location;

    @XmlElement(name = "interface")
    protected List<RequisitionInterface> m_interfaces = new ArrayList<>();
    @XmlElement(name="category")
    protected List<RequisitionCategory> m_categories = new ArrayList<>();
    @XmlElement(name="asset")
    protected List<RequisitionAsset> m_assets = new ArrayList<>();
    
    @XmlAttribute(name = "building")
    protected String m_building;

    @XmlAttribute(name = "city")
    protected String m_city;

    @XmlAttribute(name = "foreign-id", required = true)
    protected String m_foreignId;

    @XmlAttribute(name = "node-label", required = true)
    protected String m_nodeLabel;
    
    @XmlAttribute(name = "parent-foreign-source")
    protected String m_parentForeignSource;

    @XmlAttribute(name = "parent-foreign-id")
    protected String m_parentForeignId;

    @XmlAttribute(name = "parent-node-label")
    protected String m_parentNodeLabel;

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
            interfaces = new TreeSet<>();
        }
        if (m_interfaces == interfaces) return;
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
    public boolean deleteInterface(final RequisitionInterface iface) {
        return m_interfaces.remove(iface);
    }

    /**
     * <p>deleteInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public boolean deleteInterface(final String ipAddress) {
        final Iterator<RequisitionInterface> i = m_interfaces.iterator();
        while (i.hasNext()) {
            final RequisitionInterface iface = i.next();
            if (iface.getIpAddr().equals(ipAddress)) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * <p>putInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     */
    public void putInterface(RequisitionInterface iface) {
        deleteInterface(iface.getIpAddr());
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
            categories = new TreeSet<>();
        }
        if (m_categories == categories) return;
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
    public boolean deleteCategory(final RequisitionCategory category) {
        return m_categories.remove(category);
    }

    /**
     * <p>deleteCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public boolean deleteCategory(final String category) {
        if (m_categories != null) {
            final Iterator<RequisitionCategory> i = m_categories.iterator();
            while (i.hasNext()) {
                final RequisitionCategory cat = i.next();
                if (cat.getName().equals(category)) {
                    i.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>putCategory</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public void putCategory(RequisitionCategory category) {
        deleteCategory(category.getName());
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
            assets = new TreeSet<>();
        }
        if (m_assets == assets) return;
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
    public boolean deleteAsset(final String name) {
        final Iterator<RequisitionAsset> i = m_assets.iterator();
        while (i.hasNext()) {
            final RequisitionAsset asset = i.next();
            if (asset.getName().equals(name)) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * <p>deleteAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public boolean deleteAsset(final RequisitionAsset asset) {
        return m_assets.remove(asset);
    }

    /**
     * <p>putAsset</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public void putAsset(RequisitionAsset asset) {
        deleteAsset(asset.getName());
        m_assets.add(0, asset);
    }

    /**
     * <p>Getter for the field <code>location</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocation() {
        return m_location;
    }

    /**
     * <p>Setter for the field <code>location</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setLocation(String value) {
        m_location = value;
    }

    /**
     * <p>Getter for the field <code>building</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBuilding() {
        return m_building;
    }

    /**
     * <p>Setter for the field <code>building</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setBuilding(String value) {
        m_building = value;
    }

    /**
     * <p>Getter for the field <code>city</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCity() {
        return m_city;
    }

    /**
     * <p>Setter for the field <code>city</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setCity(String value) {
        m_city = value;
    }

    /**
     * <p>Getter for the field <code>foreignId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        return m_foreignId;
    }

    /**
     * <p>Setter for the field <code>foreignId</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setForeignId(String value) {
        m_foreignId = value;
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>Setter for the field <code>nodeLabel</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setNodeLabel(String value) {
        m_nodeLabel = value;
    }

    /**
     * <p>Getter for the field <code>parentForeignSource</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentForeignSource() {
        return m_parentForeignSource;
    }

    /**
     * <p>Setter for the field <code>parentForeignSource</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setParentForeignSource(String value) {
        m_parentForeignSource = value != null && "".equals(value.trim()) ? null : value;
    }

    /**
     * <p>Getter for the field <code>parentForeignId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentForeignId() {
        return m_parentForeignId;
    }

    /**
     * <p>Setter for the field <code>parentForeignId</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setParentForeignId(String value) {
        m_parentForeignId = value != null && "".equals(value.trim()) ? null : value;
    }

    /**
     * <p>Getter for the field <code>parentNodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParentNodeLabel() {
        return m_parentNodeLabel;
    }

    /**
     * <p>Setter for the field <code>parentNodeLabel</code>.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setParentNodeLabel(String value) {
        m_parentNodeLabel = value != null && "".equals(value.trim()) ? null : value;
    }

    public void validate() throws ValidationException {
        if (m_nodeLabel == null) {
            throw new ValidationException("Requisition node 'node-label' is a required attribute!");
        }
        if (m_foreignId == null) {
            throw new ValidationException("Requisition node 'foreign-id' is a required attribute!");
        }
        if (m_foreignId.contains("/")) {
            throw new ValidationException("Node foreign ID (" + m_foreignId + ") contains invalid characters. ('/' is forbidden.)");
        }
        if (m_interfaces != null) {
            for (final RequisitionInterface iface : m_interfaces) {
                iface.validate();
            }
        }
        if (m_categories != null) {
            for (final RequisitionCategory cat : m_categories) {
                cat.validate();
            }
        }
        if (m_assets != null) {
            for (final RequisitionAsset asset : m_assets) {
                asset.validate();
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_building, m_city, m_foreignId, m_assets,
                m_categories, m_interfaces, m_nodeLabel, m_nodeLabel,
                m_parentForeignId, m_parentForeignSource, m_parentNodeLabel, m_location);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof RequisitionNode)) return false;
        final RequisitionNode other = (RequisitionNode) obj;
        return Objects.equals(this.m_building, other.m_building) &&
                Objects.equals(this.m_city, other.m_city) &&
                Objects.equals(this.m_foreignId, other.m_foreignId) &&
                Objects.equals(this.m_assets, other.m_assets) &&
                Objects.equals(this.m_categories, other.m_categories) &&
                Objects.equals(this.m_interfaces, other.m_interfaces) &&
                Objects.equals(this.m_nodeLabel, other.m_nodeLabel) &&
                Objects.equals(this.m_parentForeignId, other.m_parentForeignId) &&
                Objects.equals(this.m_parentForeignSource, other.m_parentForeignSource) &&
                Objects.equals(this.m_parentNodeLabel, other.m_parentNodeLabel) &&
                Objects.equals(this.m_location, other.m_location);
    }

    @Override
    public String toString() {
        return "RequisitionNode [interfaces=" + m_interfaces
                + ", categories=" + m_categories + ", assets=" + m_assets
                + ", building=" + m_building + ", city=" + m_city
                + ", foreignId=" + m_foreignId + ", nodeLabel=" + m_nodeLabel
                + ", parentForeignSource=" + m_parentForeignSource
                + ", parentForeignId=" + m_parentForeignId
                + ", parentNodeLabel=" + m_parentNodeLabel
                + ", location=" + m_location + "]";
    }

}
