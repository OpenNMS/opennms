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
