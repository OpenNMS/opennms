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

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="bridgeLinkRemoteNode")
@JsonRootName("bridgeLinkRemoteNode")
@Schema(description = "The far end of a shared bridge segment. Every field is a display string.")
public class BridgeLinkRemoteNodeDTO {

    private String bridgeRemote;
    private String bridgeRemoteUrl;
    private String bridgeRemotePort;
    private String bridgeRemotePortUrl;

    @XmlElement(name="bridgeRemote")
    @JsonProperty("bridgeRemote")
    @Schema(description = "Remote end as a display label: the node label followed by (bridge base address:...) or (mac:...) when it resolves to a node, otherwise (nodeid:N) or (mac:...) alone.", example = "acc-sw-04(bridge base address:001b213c4d5e)")
    public String getBridgeRemote() {
        return bridgeRemote;
    }

    public void setBridgeRemote(String bridgeRemote) {
        this.bridgeRemote = bridgeRemote;
    }

    public BridgeLinkRemoteNodeDTO withBridgeRemote(String bridgeRemote) {
        this.bridgeRemote = bridgeRemote;
        return this;
    }

    @XmlElement(name="bridgeRemoteUrl")
    @JsonProperty("bridgeRemoteUrl")
    @Schema(description = "Relative URL of the remote node's linked-node page.", example = "element/linkednode.jsp?node=12")
    public String getBridgeRemoteUrl() {
        return bridgeRemoteUrl;
    }

    public void setBridgeRemoteUrl(String bridgeRemoteUrl) {
        this.bridgeRemoteUrl = bridgeRemoteUrl;
    }

    public BridgeLinkRemoteNodeDTO withBridgeRemoteUrl(String bridgeRemoteUrl) {
        this.bridgeRemoteUrl = bridgeRemoteUrl;
        return this;
    }

    @XmlElement(name="bridgeRemotePort")
    @JsonProperty("bridgeRemotePort")
    @Schema(description = "Remote port as a display label, with the bridge port number or ifIndex embedded in the string.", example = "GigabitEthernet0/24(ifindex:24)(bridgeport:24)")
    public String getBridgeRemotePort() {
        return bridgeRemotePort;
    }

    public void setBridgeRemotePort(String bridgeRemotePort) {
        this.bridgeRemotePort = bridgeRemotePort;
    }

    public BridgeLinkRemoteNodeDTO withBridgeRemotePort(String bridgeRemotePort) {
        this.bridgeRemotePort = bridgeRemotePort;
        return this;
    }

    @XmlElement(name="bridgeRemotePortUrl")
    @JsonProperty("bridgeRemotePortUrl")
    @Schema(description = "Relative URL of the remote SNMP interface page. Omitted when the remote ifIndex could not be resolved.", example = "element/snmpinterface.jsp?node=12&ifindex=24")
    public String getBridgeRemotePortUrl() {
        return bridgeRemotePortUrl;
    }

    public void setBridgeRemotePortUrl(String bridgeRemotePortUrl) {
        this.bridgeRemotePortUrl = bridgeRemotePortUrl;
    }

    public BridgeLinkRemoteNodeDTO withBridgeRemotePortUrl(String bridgeRemotePortUrl) {
        this.bridgeRemotePortUrl = bridgeRemotePortUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgeLinkRemoteNodeDTO that = (BridgeLinkRemoteNodeDTO) o;
        return Objects.equals(bridgeRemote, that.bridgeRemote) && Objects.equals(bridgeRemoteUrl, that.bridgeRemoteUrl) && Objects.equals(bridgeRemotePort, that.bridgeRemotePort) && Objects.equals(bridgeRemotePortUrl, that.bridgeRemotePortUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bridgeRemote, bridgeRemoteUrl, bridgeRemotePort, bridgeRemotePortUrl);
    }

    @Override
    public String toString() {
        return "BridgeLinkRemoteNodeDTO{" +
                "bridgeRemote='" + bridgeRemote + '\'' +
                ", bridgeRemoteUrl='" + bridgeRemoteUrl + '\'' +
                ", bridgeRemotePort='" + bridgeRemotePort + '\'' +
                ", bridgeRemotePortUrl='" + bridgeRemotePortUrl + '\'' +
                '}';
    }
}
