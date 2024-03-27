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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="bridgeLinkRemoteNode")
@JsonRootName("bridgeLinkRemoteNode")
public class BridgeLinkRemoteNodeDTO {

    private String bridgeRemote;
    private String bridgeRemoteUrl;
    private String bridgeRemotePort;
    private String bridgeRemotePortUrl;

    @XmlElement(name="bridgeRemote")
    @JsonProperty("bridgeRemote")
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
