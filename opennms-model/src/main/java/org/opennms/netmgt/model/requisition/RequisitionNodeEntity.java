/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.requisition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="requisition_nodes")
public class RequisitionNodeEntity {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="requisitionNodeSequence", sequenceName="requisitionnodenxtid")
    @GeneratedValue(generator="requisitionNodeSequence")
    private Integer id;

    @Column(name="foreignid") // TODO MVR unique in requisition
    private String foreignId;

    @Column(name="nodelabel")
    protected String nodeLabel;

    @Column(name="location")
    protected String location;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="foreignsource")
    private RequisitionEntity requisition;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy="node")
    protected List<RequisitionInterfaceEntity> interfaces = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "requisition_node_categories",
            joinColumns=@JoinColumn(name = "node_id", referencedColumnName = "id"))
    @Column(name="name")
    protected Set<String> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "requisition_node_assets",
            joinColumns=@JoinColumn(name = "node_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "key", unique = true)
    @Column(name = "value", unique = true, nullable = false)
    private Map<String, String> assets = new HashMap<>();

    @Column(name="parent_foreignsource")
    protected String parentForeignSource;

    @Column(name="parent_foreignid")
    protected String parentForeignId;

    @Column(name="parent_nodelabel")
    protected String parentNodeLabel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RequisitionEntity getRequisition() {
        return requisition;
    }

    public void setRequisition(RequisitionEntity requisition) {
        this.requisition = requisition;
    }

    public void setParentForeignSource(String value) {
        parentForeignSource = value != null && "".equals(value.trim()) ? null : value;
    }

    public void setParentForeignId(String value) {
        parentForeignId = value != null && "".equals(value.trim()) ? null : value;
    }

    public void setParentNodeLabel(String value) {
        parentNodeLabel = value != null && "".equals(value.trim()) ? null : value;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBuilding() {
        return getAssets().get("building");
    }

    public void setBuilding(String building) {
        getAssets().put("building", building);
    }

    public String getCity() {
        return getAssets().get("city");
    }

    public void setCity(String city) {
        getAssets().put("city", city);
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    // TODO MVR should be replaced by getRequisition()
    public String getForeignSource() {
        // TODO MVR verify that this actually works
        return getRequisition().getForeignSource();
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getParentForeignSource() {
        return parentForeignSource;
    }

    public String getParentForeignId() {
        return parentForeignId;
    }

    public String getParentNodeLabel() {
        return parentNodeLabel;
    }

    public List<RequisitionInterfaceEntity> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<RequisitionInterfaceEntity> interfaces) {
        this.interfaces = interfaces;
    }

    public void addInterface(RequisitionInterfaceEntity newInterface) {
        Objects.requireNonNull(newInterface);
        if (!interfaces.contains(newInterface)) {
            newInterface.setNode(this);
            interfaces.add(newInterface);
        }
    }

    public void removeInterface(RequisitionInterfaceEntity iface) {
        if (interfaces.remove(iface)) {
            iface.setNode(null); // remove parent relationship
        }
    }

    public RequisitionInterfaceEntity getInterface(String ipAddress) {
        return this.interfaces.stream()
                .filter(i -> i.getIpAddress().equals(ipAddress))
                .findFirst().orElse(null);
    }

    public void removeIpAddress(String ipAddress) {
        this.interfaces.stream()
                .filter(i -> i.getIpAddress().equals(ipAddress))
                .findFirst()
                .ifPresent(i -> interfaces.remove(i));
    }

    public Map<String, String> getAssets() {
        return assets;
    }

    public void setAssets(Map<String, String> assets) {
        this.assets = assets;
    }

    public void addAsset(String key, String value) {
        this.assets.put(key, value);
    }

    public void removeAsset(String key) {
        assets.remove(key);
    }

    //    public List<OnmsRequisitionAsset> getAssets() {
//        return assets;
//    }
//
//    public void setAssets(List<OnmsRequisitionAsset> assets) {
//        this.assets = assets;
//    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public void addCategory(String category) {
        categories.add(category);
    }

    public void removeCategory(String category) {
        categories.remove(category);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof RequisitionNodeEntity)) return false;
        if (getId() != null) {
            final RequisitionNodeEntity other = (RequisitionNodeEntity) obj;
            return getId().equals(other.getId());
        }
        return super.equals(obj);
    }

//    @Override
//    public String toString() {
//        return "RequisitionNode [interfaces=" + m_interfaces
//                + ", categories=" + m_categories + ", assets=" + m_assets
//                + ", building=" + building + ", city=" + city
//                + ", foreignId=" + foreignId + ", nodeLabel=" + nodeLabel
//                + ", parentForeignSource=" + parentForeignSource
//                + ", parentForeignId=" + parentForeignId
//                + ", parentNodeLabel=" + parentNodeLabel
//                + ", location=" + location + "]";
//    }

}
