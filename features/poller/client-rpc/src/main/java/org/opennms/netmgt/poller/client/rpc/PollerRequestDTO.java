/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.client.rpc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.PollerRequest;

@XmlRootElement(name = "poller-request")
@XmlAccessorType(XmlAccessType.NONE)
public class PollerRequestDTO implements RpcRequest, PollerRequest{

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name = "class-name")
    private String className;

    @XmlAttribute(name = "service-name")
    private String serviceName;

    @XmlAttribute(name = "address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress address;

    @XmlAttribute(name = "node-id")
    private Integer nodeId;

    @XmlAttribute(name = "node-label")
    private String nodeLabel;

    @XmlAttribute(name = "node-location")
    private String nodeLocation;

    @XmlElement(name = "attribute")
    private List<PollerAttributeDTO> attributes = new ArrayList<>();

    private Long timeToLiveMs;

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getSvcName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public String getIpAddr() {
        return InetAddressUtils.str(address);
    }

    @Override
    public int getNodeId() {
        return nodeId == null ? 0 : nodeId;
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
    public String getNodeLocation() {
        return nodeLocation;
    }

    public void setNodeLocation(String nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public List<PollerAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PollerAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, Object value) {
        attributes.add(new PollerAttributeDTO(key, value));
    }

    public void addAttributes(Map<String, Object> attributes) {
        attributes.entrySet().stream().forEach(e -> addAttribute(e.getKey(), e.getValue()));
    }

    @Override
    public Map<String, Object> getMonitorParameters() {
        Map<String, Object> pollerAttributeMap = new HashMap<>();
        for (PollerAttributeDTO attribute : attributes) {
            if (attribute.getContents() != null) {
                pollerAttributeMap.put(attribute.getKey(), attribute.getContents());
            } else {
                pollerAttributeMap.put(attribute.getKey(), attribute.getValue());
            }
        }
        return pollerAttributeMap;
    }

    public void setTimeToLiveMs(Long timeToLiveMs) {
        this.timeToLiveMs = timeToLiveMs;
    }

    @Override
    public Long getTimeToLiveMs() {
        return timeToLiveMs;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PollerRequestDTO)) {
            return false;
        }
        PollerRequestDTO castOther = (PollerRequestDTO) other;
        return Objects.equals(location, castOther.location) 
                && Objects.equals(systemId, castOther.systemId)
                && Objects.equals(className, castOther.className)
                && Objects.equals(serviceName, castOther.serviceName)
                && Objects.equals(address, castOther.address)
                && Objects.equals(nodeId, castOther.nodeId)
                && Objects.equals(nodeLabel, castOther.nodeLabel)
                && Objects.equals(timeToLiveMs, castOther.timeToLiveMs)
                && Objects.equals(attributes, castOther.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, systemId, className, serviceName,
                address, nodeId, nodeLabel, attributes, timeToLiveMs);
    }

}
