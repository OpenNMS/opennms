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
package org.opennms.netmgt.collection.client.rpc;

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

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.dto.CollectionAttributeDTO;

import io.opentracing.Span;

@XmlRootElement(name = "collector-request")
@XmlAccessorType(XmlAccessType.NONE)
public class CollectorRequestDTO implements RpcRequest {

    @XmlElement(name = "agent", type=CollectionAgentDTO.class)
    private CollectionAgent agent;

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name = "class-name")
    private String className;

    @XmlAttribute(name = "attributes-need-unmarshaling")
    private Boolean attributesNeedUnmarshaling;

    @XmlElement(name = "attribute")
    private List<CollectionAttributeDTO> attributes = new ArrayList<>();

    private Long timeToLiveMs;

    private Map<String, String> tracingInfo = new HashMap<>();

    public CollectionAgent getAgent() {
        return agent;
    }

    public void setAgent(CollectionAgent agent) {
        this.agent = agent;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
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

    public Boolean getAttributesNeedUnmarshaling() {
        return attributesNeedUnmarshaling;
    }

    public void setAttributesNeedUnmarshaling(Boolean attributesNeedUnmarshaling) {
        this.attributesNeedUnmarshaling = attributesNeedUnmarshaling;
    }

    public List<CollectionAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<CollectionAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, Object value) {
        attributes.add(new CollectionAttributeDTO(key, value));
    }

    public void addAttributes(Map<String, Object> attributes) {
        attributes.entrySet().stream().forEach(e -> addAttribute(e.getKey(), e.getValue()));
    }

    public Map<String, Object> getParameters(ServiceCollector collector) {
        if (Boolean.TRUE.equals(attributesNeedUnmarshaling)) {
            final Map<String, String> parms = new HashMap<>();
            attributes.stream().forEach(a -> parms.put(a.getKey(), a.getValue()));
            return collector.unmarshalParameters(parms);
        } else {
            final Map<String, Object> parms = new HashMap<>();
            attributes.stream().forEach(a -> parms.put(a.getKey(), a.getValueOrContents()));
            return parms;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(agent, location, systemId, className, timeToLiveMs, attributesNeedUnmarshaling);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof CollectorRequestDTO)) {
            return false;
        }
        CollectorRequestDTO other = (CollectorRequestDTO) obj;
        return Objects.equals(this.agent, other.agent)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.systemId, other.systemId)
                && Objects.equals(this.className, other.className)
                && Objects.equals(this.timeToLiveMs, other.timeToLiveMs)
                && Objects.equals(this.attributes, other.attributes)
                && Objects.equals(this.attributesNeedUnmarshaling, other.attributesNeedUnmarshaling);
    }
}
