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
package org.opennms.netmgt.wsman.eventlog.rpc;

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

import io.opentracing.Span;

/**
 * Asks the Minion at {@code location} to read Windows event log records over WS-Man.
 * The endpoint (URL, credentials, timeouts) travels as attributes so the Minion needs
 * no access to wsman-config.xml.
 */
@XmlRootElement(name = "wsman-eventlog-request")
@XmlAccessorType(XmlAccessType.NONE)
public class EventLogRequestDTO implements RpcRequest {

    public static final String DEFAULT_RESOURCE_URI = "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*";

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name = "system-id")
    private String systemId;

    @XmlAttribute(name = "resource-uri")
    private String resourceUri = DEFAULT_RESOURCE_URI;

    @XmlAttribute(name = "retries")
    private int retries = 1;

    @XmlElement(name = "endpoint-attribute")
    private List<EndpointAttributeDTO> endpointAttributes = new ArrayList<>();

    @XmlElement(name = "query")
    private List<EventLogQueryDTO> queries = new ArrayList<>();

    private Long timeToLiveMs;

    private Map<String, String> tracingInfo = new HashMap<>();

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public List<EventLogQueryDTO> getQueries() {
        return queries;
    }

    public void addQuery(EventLogQueryDTO query) {
        queries.add(query);
    }

    public Map<String, String> getEndpointAttributes() {
        final Map<String, String> map = new HashMap<>();
        for (EndpointAttributeDTO attribute : endpointAttributes) {
            map.put(attribute.getKey(), attribute.getValue());
        }
        return map;
    }

    public void setEndpointAttributes(Map<String, String> attributes) {
        endpointAttributes = new ArrayList<>();
        attributes.forEach((k, v) -> endpointAttributes.add(new EndpointAttributeDTO(k, v)));
    }

    @Override
    public Long getTimeToLiveMs() {
        return timeToLiveMs;
    }

    public void setTimeToLiveMs(Long timeToLiveMs) {
        this.timeToLiveMs = timeToLiveMs;
    }

    @Override
    public Map<String, String> getTracingInfo() {
        return tracingInfo;
    }

    public void addTracingInfo(String key, String value) {
        tracingInfo.put(key, value);
    }

    @Override
    public Span getSpan() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, systemId, resourceUri, retries, endpointAttributes, queries);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventLogRequestDTO)) {
            return false;
        }
        final EventLogRequestDTO other = (EventLogRequestDTO) obj;
        return Objects.equals(location, other.location)
                && Objects.equals(systemId, other.systemId)
                && Objects.equals(resourceUri, other.resourceUri)
                && retries == other.retries
                && Objects.equals(endpointAttributes, other.endpointAttributes)
                && Objects.equals(queries, other.queries);
    }
}
