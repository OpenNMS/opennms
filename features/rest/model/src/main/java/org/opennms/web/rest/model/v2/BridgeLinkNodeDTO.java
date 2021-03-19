/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.model.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="bridgeLinkNode")
@JsonRootName("bridgeLinkNode")
public class BridgeLinkNodeDTO {
    private String bridgeLocalPort;

    private String bridgeLocalPortUrl;

    private List<BridgeLinkRemoteNodeDTO> bridgeLinkRemoteNodes = new ArrayList<BridgeLinkRemoteNodeDTO>();

    private String bridgeInfo;

    private String bridgeLinkCreateTime;

    private String bridgeLinkLastPollTime;

    @XmlElement(name="bridgeLocalPort")
    @JsonProperty("bridgeLocalPort")
    public String getBridgeLocalPort() {
        return bridgeLocalPort;
    }

    public void setBridgeLocalPort(String bridgeLocalPort) {
        this.bridgeLocalPort = bridgeLocalPort;
    }

    public BridgeLinkNodeDTO withBridgeLocalPort(String bridgeLocalPort) {
        this.bridgeLocalPort = bridgeLocalPort;
        return this;
    }

    @XmlElement(name="bridgeLocalPortUrl")
    @JsonProperty("bridgeLocalPortUrl")
    public String getBridgeLocalPortUrl() {
        return bridgeLocalPortUrl;
    }

    public void setBridgeLocalPortUrl(String bridgeLocalPortUrl) {
        this.bridgeLocalPortUrl = bridgeLocalPortUrl;
    }

    public BridgeLinkNodeDTO withBridgeLocalPortUrl(String bridgeLocalPortUrl) {
        this.bridgeLocalPortUrl = bridgeLocalPortUrl;
        return this;
    }

    @XmlElement(name="BridgeLinkRemoteNodes")
    @JsonProperty("BridgeLinkRemoteNodes")
    public List<BridgeLinkRemoteNodeDTO> getBridgeLinkRemoteNodes() {
        return bridgeLinkRemoteNodes;
    }

    public void setBridgeLinkRemoteNodes(List<BridgeLinkRemoteNodeDTO> bridgeLinkRemoteNodes) {
        this.bridgeLinkRemoteNodes = bridgeLinkRemoteNodes;
    }

    public BridgeLinkNodeDTO withBridgeLinkRemoteNodes(List<BridgeLinkRemoteNodeDTO> bridgeLinkRemoteNodes) {
        this.bridgeLinkRemoteNodes = bridgeLinkRemoteNodes;
        return this;
    }

    @XmlElement(name="bridgeInfo")
    @JsonProperty("bridgeInfo")
    public String getBridgeInfo() {
        return bridgeInfo;
    }

    public void setBridgeInfo(String bridgeInfo) {
        this.bridgeInfo = bridgeInfo;
    }

    public BridgeLinkNodeDTO withBridgeInfo(String bridgeInfo) {
        this.bridgeInfo = bridgeInfo;
        return this;
    }

    @XmlElement(name="bridgeLinkCreateTime")
    @JsonProperty("bridgeLinkCreateTime")
    public String getBridgeLinkCreateTime() {
        return bridgeLinkCreateTime;
    }

    public void setBridgeLinkCreateTime(String bridgeLinkCreateTime) {
        this.bridgeLinkCreateTime = bridgeLinkCreateTime;
    }

    public BridgeLinkNodeDTO withBridgeLinkCreateTime(String bridgeLinkCreateTime) {
        this.bridgeLinkCreateTime = bridgeLinkCreateTime;
        return this;
    }

    @XmlElement(name="bridgeLinkLastPollTime")
    @JsonProperty("bridgeLinkLastPollTime")
    public String getBridgeLinkLastPollTime() {
        return bridgeLinkLastPollTime;
    }

    public void setBridgeLinkLastPollTime(String bridgeLinkLastPollTime) {
        this.bridgeLinkLastPollTime = bridgeLinkLastPollTime;
    }

    public BridgeLinkNodeDTO withBridgeLinkLastPollTime(String bridgeLinkLastPollTime) {
        this.bridgeLinkLastPollTime = bridgeLinkLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgeLinkNodeDTO that = (BridgeLinkNodeDTO) o;
        return Objects.equals(bridgeLocalPort, that.bridgeLocalPort) && Objects.equals(bridgeLocalPortUrl, that.bridgeLocalPortUrl) && Objects.equals(bridgeLinkRemoteNodes, that.bridgeLinkRemoteNodes) && Objects.equals(bridgeInfo, that.bridgeInfo) && Objects.equals(bridgeLinkCreateTime, that.bridgeLinkCreateTime) && Objects.equals(bridgeLinkLastPollTime, that.bridgeLinkLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bridgeLocalPort, bridgeLocalPortUrl, bridgeLinkRemoteNodes, bridgeInfo, bridgeLinkCreateTime, bridgeLinkLastPollTime);
    }

    @Override
    public String toString() {
        return "BridgeLinkNodeDTO{" +
                "bridgeLocalPort='" + bridgeLocalPort + '\'' +
                ", bridgeLocalPortUrl='" + bridgeLocalPortUrl + '\'' +
                ", bridgeLinkRemoteNodes=" + bridgeLinkRemoteNodes +
                ", bridgeInfo='" + bridgeInfo + '\'' +
                ", bridgeLinkCreateTime='" + bridgeLinkCreateTime + '\'' +
                ", bridgeLinkLastPollTime='" + bridgeLinkLastPollTime + '\'' +
                '}';
    }
}
