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

package org.opennms.netmgt.collection.dto;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.InetAddrUtils;

@XmlRootElement(name = "agent")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionAgentDTO implements CollectionAgent {

    @XmlElement(name = "attribute")
    private List<CollectionAttributeDTO> attributes = new ArrayList<>();

    @XmlAttribute(name = "address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress address;

    @XmlAttribute(name = "store-by-fs")
    private Boolean storeByForeignSource;

    @XmlAttribute(name = "node-id")
    private int nodeId;

    @XmlAttribute(name = "node-label")
    private String nodeLabel;

    @XmlAttribute(name = "foreign-source")
    private String foreignSource;

    @XmlAttribute(name = "foreign-id")
    private String foreignId;

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name = "storage-resource-path")
    private String storageResourcePath;

    @XmlAttribute(name = "sys-up-time")
    private long sysUpTime;

    public CollectionAgentDTO() { }

    public CollectionAgentDTO(CollectionAgent agent) {
        Objects.requireNonNull(agent);
        for (String attribute : agent.getAttributeNames()) {
            setAttribute(attribute, agent.getAttribute(attribute));
        }
        address = agent.getAddress();
        storeByForeignSource = agent.isStoreByForeignSource();
        nodeId = agent.getNodeId();
        nodeLabel = agent.getNodeLabel();
        foreignSource = agent.getForeignSource();
        foreignId = agent.getForeignId();
        location = agent.getLocationName();
        setStorageResourcePath(agent.getStorageResourcePath());
        sysUpTime = agent.getSavedSysUpTime();
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.stream()
                    .map(a -> a.getKey())
                    .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getAttribute(String property) {
        for (CollectionAttributeDTO attribute : attributes) {
            if (!attribute.getKey().equals(property)) {
                continue;
            }
            return (V)attribute.getValueOrContents();
        }
        return null;
    }

    @Override
    public Object setAttribute(String property, Object value) { 
        // Update the existing attribute if one already exists
        for (CollectionAttributeDTO attribute : attributes) {
            if (attribute.getKey().equals(property)) {
                Object previousValue = attribute.getValueOrContents();
                attribute.setValueOrContents(value);
                return previousValue;
            }
        }
        // We didn't match an existing attribute, create a new one
        attributes.add(new CollectionAttributeDTO(property, value));
        return null;
    }

    @Override
    public Boolean isStoreByForeignSource() {
        return storeByForeignSource;
    }

    public void setStoreByForeignSource(Boolean storeByForeignSource) {
        this.storeByForeignSource = storeByForeignSource;
    }

    @Override
    public String getHostAddress() {
        return address != null ? InetAddrUtils.str(address) : null;
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    @Override
    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    @Override
    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    @Override
    public String getLocationName() {
        return location;
    }

    public void setLocationName(String location) {
        this.location = location;
    }

    @Override
    public ResourcePath getStorageResourcePath() {
        return storageResourcePath != null ? ResourcePath.fromString(storageResourcePath) : null;
    }

    public void setStorageResourcePath(ResourcePath storageResourcePath) {
        this.storageResourcePath = storageResourcePath != null ? ResourcePath.toString(storageResourcePath) : null;
    }

    @Override
    public long getSavedSysUpTime() {
        return sysUpTime;
    }

    @Override
    public void setSavedSysUpTime(long sysUpTime) {
        this.sysUpTime = sysUpTime;
    }

    @Override
    public String toString() {
        return String.format("CollectionAgentDTO[attributes=%s, address=%s, storeByForeignSource=%s, "
                + "nodeId=%d, nodeLabel=%s, foreignSource=%s, foreignId=%s, location=%s, storageDir=%s, "
                + "sysObjectId=%s, sysUpTime=%d]",
                attributes, address != null ? InetAddrUtils.str(address) : null, storeByForeignSource,
                nodeId, nodeLabel, foreignSource, foreignId, location, storageResourcePath, sysUpTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, address, storeByForeignSource,
                nodeId, nodeLabel, foreignSource, foreignId, location,
                storageResourcePath, sysUpTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof CollectionAgentDTO)) {
            return false;
        }
        CollectionAgentDTO other = (CollectionAgentDTO) obj;
        return Objects.equals(this.attributes, other.attributes) &&
                Objects.equals(this.address, other.address) &&
                Objects.equals(this.storeByForeignSource, other.storeByForeignSource) &&
                Objects.equals(this.nodeId, other.nodeId) &&
                Objects.equals(this.nodeLabel, other.nodeLabel) &&
                Objects.equals(this.foreignSource, other.foreignSource) &&
                Objects.equals(this.foreignId, other.foreignId) &&
                Objects.equals(this.location, other.location) &&
                Objects.equals(this.storageResourcePath, other.storageResourcePath) &&
                Objects.equals(this.sysUpTime, other.sysUpTime);
    }
}
