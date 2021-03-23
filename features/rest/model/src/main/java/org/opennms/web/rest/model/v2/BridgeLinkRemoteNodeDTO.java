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
