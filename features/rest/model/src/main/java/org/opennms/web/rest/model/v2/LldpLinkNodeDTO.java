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

@XmlRootElement(name="lldpLinkNode")
@JsonRootName("lldpLinkNode")
public class LldpLinkNodeDTO {

    private String   lldpLocalPort;

    private String   lldpLocalPortUrl;

    private String   lldpRemChassisId;

    private String   lldpRemChassisIdUrl;

    private String   lldpRemInfo;

    private String   ldpRemPort;

    private String   lldpRemPortUrl;

    private String   lldpCreateTime;

    private String   lldpLastPollTime;

    @XmlElement(name="lldpLocalPort")
    @JsonProperty("lldpLocalPort")
    public String getLldpLocalPort() {
        return lldpLocalPort;
    }

    public void setLldpLocalPort(String lldpLocalPort) {
        this.lldpLocalPort = lldpLocalPort;
    }

    public LldpLinkNodeDTO withLldpLocalPort(String lldpLocalPort) {
        this.lldpLocalPort = lldpLocalPort;
        return this;
    }

    @XmlElement(name="lldpLocalPortUrl")
    @JsonProperty("lldpLocalPortUrl")
    public String getLldpLocalPortUrl() {
        return lldpLocalPortUrl;
    }

    public void setLldpLocalPortUrl(String lldpLocalPortUrl) {
        this.lldpLocalPortUrl = lldpLocalPortUrl;
    }

    public LldpLinkNodeDTO withLldpLocalPortUrl(String lldpLocalPortUrl) {
        this.lldpLocalPortUrl = lldpLocalPortUrl;
        return this;
    }

    @XmlElement(name="lldpRemChassisId")
    @JsonProperty("lldpRemChassisId")
    public String getLldpRemChassisId() {
        return lldpRemChassisId;
    }

    public void setLldpRemChassisId(String lldpRemChassisId) {
        this.lldpRemChassisId = lldpRemChassisId;
    }

    public LldpLinkNodeDTO withLldpRemChassisId(String lldpRemChassisId) {
        this.lldpRemChassisId = lldpRemChassisId;
        return this;
    }

    @XmlElement(name="lldpRemChassisIdUrl")
    @JsonProperty("lldpRemChassisIdUrl")
    public String getLldpRemChassisIdUrl() {
        return lldpRemChassisIdUrl;
    }

    public void setLldpRemChassisIdUrl(String lldpRemChassisIdUrl) {
        this.lldpRemChassisIdUrl = lldpRemChassisIdUrl;
    }

    public LldpLinkNodeDTO withLldpRemChassisIdUrl(String lldpRemChassisIdUrl) {
        this.lldpRemChassisIdUrl = lldpRemChassisIdUrl;
        return this;
    }

    @XmlElement(name="lldpRemInfo")
    @JsonProperty("lldpRemInfo")
    public String getLldpRemInfo() {
        return lldpRemInfo;
    }

    public void setLldpRemInfo(String lldpRemInfo) {
        this.lldpRemInfo = lldpRemInfo;
    }

    public LldpLinkNodeDTO withLldpRemInfo(String lldpRemInfo) {
        this.lldpRemInfo = lldpRemInfo;
        return this;
    }

    @XmlElement(name="ldpRemPort")
    @JsonProperty("ldpRemPort")
    public String getLdpRemPort() {
        return ldpRemPort;
    }

    public void setLdpRemPort(String ldpRemPort) {
        this.ldpRemPort = ldpRemPort;
    }

    public LldpLinkNodeDTO withLdpRemPort(String ldpRemPort) {
        this.ldpRemPort = ldpRemPort;
        return this;
    }

    @XmlElement(name="lldpRemPortUrl")
    @JsonProperty("lldpRemPortUrl")
    public String getLldpRemPortUrl() {
        return lldpRemPortUrl;
    }

    public void setLldpRemPortUrl(String lldpRemPortUrl) {
        this.lldpRemPortUrl = lldpRemPortUrl;
    }

    public LldpLinkNodeDTO withLldpRemPortUrl(String lldpRemPortUrl) {
        this.lldpRemPortUrl = lldpRemPortUrl;
        return this;
    }

    @XmlElement(name="lldpCreateTime")
    @JsonProperty("lldpCreateTime")
    public String getLldpCreateTime() {
        return lldpCreateTime;
    }

    public void setLldpCreateTime(String lldpCreateTime) {
        this.lldpCreateTime = lldpCreateTime;
    }

    public LldpLinkNodeDTO withLldpCreateTime(String lldpCreateTime) {
        this.lldpCreateTime = lldpCreateTime;
        return this;
    }

    @XmlElement(name="lldpLastPollTime")
    @JsonProperty("lldpLastPollTime")
    public String getLldpLastPollTime() {
        return lldpLastPollTime;
    }

    public void setLldpLastPollTime(String lldpLastPollTime) {
        this.lldpLastPollTime = lldpLastPollTime;
    }

    public LldpLinkNodeDTO withLldpLastPollTime(String lldpLastPollTime) {
        this.lldpLastPollTime = lldpLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LldpLinkNodeDTO that = (LldpLinkNodeDTO) o;
        return Objects.equals(lldpLocalPort, that.lldpLocalPort) && Objects.equals(lldpLocalPortUrl, that.lldpLocalPortUrl) && Objects.equals(lldpRemChassisId, that.lldpRemChassisId) && Objects.equals(lldpRemChassisIdUrl, that.lldpRemChassisIdUrl) && Objects.equals(lldpRemInfo, that.lldpRemInfo) && Objects.equals(ldpRemPort, that.ldpRemPort) && Objects.equals(lldpRemPortUrl, that.lldpRemPortUrl) && Objects.equals(lldpCreateTime, that.lldpCreateTime) && Objects.equals(lldpLastPollTime, that.lldpLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lldpLocalPort, lldpLocalPortUrl, lldpRemChassisId, lldpRemChassisIdUrl, lldpRemInfo, ldpRemPort, lldpRemPortUrl, lldpCreateTime, lldpLastPollTime);
    }

    @Override
    public String toString() {
        return "LldpLinkNodeDTO{" +
                "lldpLocalPort='" + lldpLocalPort + '\'' +
                ", lldpLocalPortUrl='" + lldpLocalPortUrl + '\'' +
                ", lldpRemChassisId='" + lldpRemChassisId + '\'' +
                ", lldpRemChassisIdUrl='" + lldpRemChassisIdUrl + '\'' +
                ", lldpRemInfo='" + lldpRemInfo + '\'' +
                ", ldpRemPort='" + ldpRemPort + '\'' +
                ", lldpRemPortUrl='" + lldpRemPortUrl + '\'' +
                ", lldpCreateTime='" + lldpCreateTime + '\'' +
                ", lldpLastPollTime='" + lldpLastPollTime + '\'' +
                '}';
    }
}
