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
package org.opennms.netmgt.provision.detector.client.rpc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.PreDetectCallback;

import io.opentracing.Span;

@XmlRootElement(name = "detector-request")
@XmlAccessorType(XmlAccessType.NONE)
public class DetectorRequestDTO implements DetectRequest, RpcRequest {

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name = "class-name")
    private String className;

    @XmlAttribute(name = "address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress address;

    @XmlElement(name = "detector-attribute")
    private List<DetectorAttributeDTO> detectorAttributes = new ArrayList<>();

    @XmlElement(name = "runtime-attribute")
    private List<DetectorAttributeDTO> runtimeAttributes = new ArrayList<>();

    private Long timeToLiveMs;

    private Map<String, String> tracingInfo = new HashMap<>();

    private Span span;

    private PreDetectCallback preDetectCallback;

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

    public List<DetectorAttributeDTO> getDetectorAttributes() {
        return detectorAttributes;
    }

    public void setDetectorAttributes(List<DetectorAttributeDTO> attributes) {
        this.detectorAttributes = attributes;
    }

    public void addDetectorAttribute(String key, String value) {
        detectorAttributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addDetectorAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> this.addDetectorAttribute(e.getKey(), e.getValue()));
    }

    public Map<String, String> getAttributeMap() {
        return detectorAttributes.stream().collect(Collectors.toMap(DetectorAttributeDTO::getKey,
                DetectorAttributeDTO::getValue));
    }

    public void setRuntimeAttributes(List<DetectorAttributeDTO> attributes) {
        this.runtimeAttributes = attributes;
    }

    public void addRuntimeAttribute(String key, String value) {
        runtimeAttributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addRuntimeAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> this.addRuntimeAttribute(e.getKey(), e.getValue()));
    }

    @Override
    public Map<String, String> getRuntimeAttributes() {
        Map<String, String> runtimeAttributeMap = new HashMap<String, String>();
        for (DetectorAttributeDTO agentAttribute : runtimeAttributes) {
            runtimeAttributeMap.put(agentAttribute.getKey(), agentAttribute.getValue());
        }
        return runtimeAttributeMap;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public Long getTimeToLiveMs() {
        return this.timeToLiveMs;
    }

    public void setTimeToLiveMs(Long timeToLive) {
        this.timeToLiveMs = timeToLive;
    }

    @Override
    public Map<String, String> getTracingInfo() {
        return tracingInfo;
    }

    @Override
    public Span getSpan() {
        return this.span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }

    public void addTracingInfo(String key, String value) {
        tracingInfo.put(key, value);
    }

    public void setPreDetectCallback(PreDetectCallback preDetectCallback) {
        this.preDetectCallback = preDetectCallback;
    }

    @Override
    public void preDetect() {
        if(preDetectCallback != null) {
            preDetectCallback.preDetect();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, systemId, detectorAttributes, runtimeAttributes,
                className, address);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DetectorRequestDTO other = (DetectorRequestDTO) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.systemId, other.systemId)
                && Objects.equals(this.detectorAttributes, other.detectorAttributes)
                && Objects.equals(this.runtimeAttributes, other.runtimeAttributes)
                && Objects.equals(this.className, other.className)
                && Objects.equals(this.address, other.address);
    }

}
