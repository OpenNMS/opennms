/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

import io.opentracing.Span;

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

    private Map<String, String> tracingInfo = new HashMap<>();

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
    public Map<String, String> getTracingInfo() {
        return tracingInfo;
    }

    @Override
    public Span getSpan() {
        return null;
    }

    public void addTracingInfo(String key, String value) {
        tracingInfo.put(key, value);
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
