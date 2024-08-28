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
package org.opennms.netmgt.snmp.proxy.common;

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
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

import io.opentracing.Span;

@XmlRootElement(name="snmp-request")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpRequestDTO implements RpcRequest {

    @XmlAttribute(name="location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlElement(name="agent")
    private SnmpAgentConfig agent;

    @XmlAttribute(name="description")
    private String description;

    @XmlElement(name="get")
    private List<SnmpGetRequestDTO> gets = new ArrayList<>(0);

    @XmlElement(name="walk")
    private List<SnmpWalkRequestDTO> walks = new ArrayList<>(0);

    @XmlElement(name="set")
    private List<SnmpSetRequestDTO> sets = new ArrayList<>(0);

    @XmlTransient
    private Long timeToLive;

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

    public SnmpAgentConfig getAgent() {
        return agent;
    }

    public void setAgent(SnmpAgentConfig agent) {
        this.agent = agent;
    }

    public void setGetRequests(List<SnmpGetRequestDTO> gets) {
        this.gets = gets;
    }

    public List<SnmpGetRequestDTO> getGetRequests() {
        return gets;
    }

    public void setWalkRequests(List<SnmpWalkRequestDTO> walks) {
        this.walks = walks;
    }

    public List<SnmpWalkRequestDTO> getWalkRequest() {
        return walks;
    }

    public void setSetRequests(List<SnmpSetRequestDTO> sets) {
        this.sets = sets;
    }

    public List<SnmpSetRequestDTO> getSetRequest() {
        return sets;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public Long getTimeToLiveMs() {
        return timeToLive;
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
    public int hashCode() {
        return Objects.hash(location, systemId, agent, gets, walks, sets, description, timeToLive);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SnmpRequestDTO other = (SnmpRequestDTO) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.systemId, other.systemId)
                && Objects.equals(this.agent, other.agent)
                && Objects.equals(this.gets, other.gets)
                && Objects.equals(this.walks, other.walks)
                && Objects.equals(this.sets, other.sets)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.timeToLive, other.timeToLive);
    }
}
