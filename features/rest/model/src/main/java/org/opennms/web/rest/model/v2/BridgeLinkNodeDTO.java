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

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="bridgeLinkNode")
@JsonRootName("bridgeLinkNode")
@Schema(description = "One shared bridge segment the node takes part in, with the node's own end in bridgeLocalPort and the other ends in BridgeLinkRemoteNodes. Note that one field name is capitalised where the rest are not.")
public class BridgeLinkNodeDTO {
    private String bridgeLocalPort;

    private String bridgeLocalPortUrl;

    private List<BridgeLinkRemoteNodeDTO> bridgeLinkRemoteNodes = new ArrayList<BridgeLinkRemoteNodeDTO>();

    private String bridgeInfo;

    private String bridgeLinkCreateTime;

    private String bridgeLinkLastPollTime;

    @XmlElement(name="bridgeLocalPort")
    @JsonProperty("bridgeLocalPort")
    @Schema(description = "Local end of the shared segment as a display label. Depending on what resolved this is a bridge port ((bridgeport:N)), a MAC ((mac:...)), or a port string with (ifindex:N) embedded.", example = "GigabitEthernet0/1(ifindex:2)(bridgeport:1)")
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
    @Schema(description = "Relative URL of the local SNMP or IP interface page. Omitted when nothing resolved.", example = "element/snmpinterface.jsp?node=1&ifindex=2")
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
    @Schema(name = "BridgeLinkRemoteNodes", description = "Other ends of the shared segment. Note the wire name is capitalised: BridgeLinkRemoteNodes, unlike every other field here. Always present, empty when the segment has no resolved remote end.")
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
    @Schema(description = "VLAN name of the bridge element behind the local port, when one was found.", example = "default")
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
    @Schema(description = "Poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space. Treat it as an opaque label.", example = "8/18/26, 1:16:57\u202fPM")
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
    @Schema(description = "Poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space. Treat it as an opaque label.", example = "8/18/26, 1:16:57\u202fPM")
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
