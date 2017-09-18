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

package org.opennms.netmgt.snmp.proxy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

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

    @XmlTransient
    private Long timeToLive;

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
    public int hashCode() {
        return Objects.hash(location, systemId, agent, gets, walks, description, timeToLive);
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
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.timeToLive, other.timeToLive);
    }
}
