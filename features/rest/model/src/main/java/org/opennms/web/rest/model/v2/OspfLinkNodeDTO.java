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

@XmlRootElement(name="ospfLinkNode")
@JsonRootName("ospfLinkNode")
public class OspfLinkNodeDTO {

    private String ospfLocalPort;

    private String ospfLocalPortUrl;

    private String ospfRemRouterId;

    private String ospfRemRouterUrl;

    private String ospfRemPort;

    private String ospfRemPortUrl;

    private String ospfLinkInfo;

    private String ospfLinkCreateTime;

    private String ospfLinkLastPollTime;

    @XmlElement(name="ospfLocalPort")
    @JsonProperty("ospfLocalPort")
    public String getOspfLocalPort() {
        return ospfLocalPort;
    }

    public void setOspfLocalPort(String ospfLocalPort) {
        this.ospfLocalPort = ospfLocalPort;
    }

    public OspfLinkNodeDTO withOspfLocalPort(String ospfLocalPort) {
        this.ospfLocalPort = ospfLocalPort;
        return this;
    }

    @XmlElement(name="ospfLocalPortUrl")
    @JsonProperty("ospfLocalPortUrl")
    public String getOspfLocalPortUrl() {
        return ospfLocalPortUrl;
    }

    public void setOspfLocalPortUrl(String ospfLocalPortUrl) {
        this.ospfLocalPortUrl = ospfLocalPortUrl;
    }

    public OspfLinkNodeDTO withOspfLocalPortUrl(String ospfLocalPortUrl) {
        this.ospfLocalPortUrl = ospfLocalPortUrl;
        return this;
    }

    @XmlElement(name="ospfRemRouterId")
    @JsonProperty("ospfRemRouterId")
    public String getOspfRemRouterId() {
        return ospfRemRouterId;
    }

    public void setOspfRemRouterId(String ospfRemRouterId) {
        this.ospfRemRouterId = ospfRemRouterId;
    }

    public OspfLinkNodeDTO withOspfRemRouterId(String ospfRemRouterId) {
        this.ospfRemRouterId = ospfRemRouterId;
        return this;
    }

    @XmlElement(name="ospfRemRouterUrl")
    @JsonProperty("ospfRemRouterUrl")
    public String getOspfRemRouterUrl() {
        return ospfRemRouterUrl;
    }

    public void setOspfRemRouterUrl(String ospfRemRouterUrl) {
        this.ospfRemRouterUrl = ospfRemRouterUrl;
    }

    public OspfLinkNodeDTO withOspfRemRouterUrl(String ospfRemRouterUrl) {
        this.ospfRemRouterUrl = ospfRemRouterUrl;
        return this;
    }

    @XmlElement(name="ospfRemPort")
    @JsonProperty("ospfRemPort")
    public String getOspfRemPort() {
        return ospfRemPort;
    }

    public void setOspfRemPort(String ospfRemPort) {
        this.ospfRemPort = ospfRemPort;
    }

    public OspfLinkNodeDTO withOspfRemPort(String ospfRemPort) {
        this.ospfRemPort = ospfRemPort;
        return this;
    }

    @XmlElement(name="ospfRemPortUrl")
    @JsonProperty("ospfRemPortUrl")
    public String getOspfRemPortUrl() {
        return ospfRemPortUrl;
    }

    public void setOspfRemPortUrl(String ospfRemPortUrl) {
        this.ospfRemPortUrl = ospfRemPortUrl;
    }

    public OspfLinkNodeDTO withOspfRemPortUrl(String ospfRemPortUrl) {
        this.ospfRemPortUrl = ospfRemPortUrl;
        return this;
    }

    @XmlElement(name="ospfLinkInfo")
    @JsonProperty("ospfLinkInfo")
    public String getOspfLinkInfo() {
        return ospfLinkInfo;
    }

    public void setOspfLinkInfo(String ospfLinkInfo) {
        this.ospfLinkInfo = ospfLinkInfo;
    }

    public OspfLinkNodeDTO withOspfLinkInfo(String ospfLinkInfo) {
        this.ospfLinkInfo = ospfLinkInfo;
        return this;
    }

    @XmlElement(name="ospfLinkCreateTime")
    @JsonProperty("ospfLinkCreateTime")
    public String getOspfLinkCreateTime() {
        return ospfLinkCreateTime;
    }

    public void setOspfLinkCreateTime(String ospfLinkCreateTime) {
        this.ospfLinkCreateTime = ospfLinkCreateTime;
    }

    public OspfLinkNodeDTO withOspfLinkCreateTime(String ospfLinkCreateTime) {
        this.ospfLinkCreateTime = ospfLinkCreateTime;
        return this;
    }

    @XmlElement(name="ospfLinkLastPollTime")
    @JsonProperty("ospfLinkLastPollTime")
    public String getOspfLinkLastPollTime() {
        return ospfLinkLastPollTime;
    }

    public void setOspfLinkLastPollTime(String ospfLinkLastPollTime) {
        this.ospfLinkLastPollTime = ospfLinkLastPollTime;
    }

    public OspfLinkNodeDTO withOspfLinkLastPollTime(String ospfLinkLastPollTime) {
        this.ospfLinkLastPollTime = ospfLinkLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OspfLinkNodeDTO that = (OspfLinkNodeDTO) o;
        return Objects.equals(ospfLocalPort, that.ospfLocalPort) && Objects.equals(ospfLocalPortUrl, that.ospfLocalPortUrl) && Objects.equals(ospfRemRouterId, that.ospfRemRouterId) && Objects.equals(ospfRemRouterUrl, that.ospfRemRouterUrl) && Objects.equals(ospfRemPort, that.ospfRemPort) && Objects.equals(ospfRemPortUrl, that.ospfRemPortUrl) && Objects.equals(ospfLinkInfo, that.ospfLinkInfo) && Objects.equals(ospfLinkCreateTime, that.ospfLinkCreateTime) && Objects.equals(ospfLinkLastPollTime, that.ospfLinkLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ospfLocalPort, ospfLocalPortUrl, ospfRemRouterId, ospfRemRouterUrl, ospfRemPort, ospfRemPortUrl, ospfLinkInfo, ospfLinkCreateTime, ospfLinkLastPollTime);
    }

    @Override
    public String toString() {
        return "OspfLinkNodeDTO{" +
                "ospfLocalPort='" + ospfLocalPort + '\'' +
                ", ospfLocalPortUrl='" + ospfLocalPortUrl + '\'' +
                ", ospfRemRouterId='" + ospfRemRouterId + '\'' +
                ", ospfRemRouterUrl='" + ospfRemRouterUrl + '\'' +
                ", ospfRemPort='" + ospfRemPort + '\'' +
                ", ospfRemPortUrl='" + ospfRemPortUrl + '\'' +
                ", ospfLinkInfo='" + ospfLinkInfo + '\'' +
                ", ospfLinkCreateTime='" + ospfLinkCreateTime + '\'' +
                ", ospfLinkLastPollTime='" + ospfLinkLastPollTime + '\'' +
                '}';
    }
}
