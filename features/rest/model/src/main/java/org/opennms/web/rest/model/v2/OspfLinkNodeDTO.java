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
