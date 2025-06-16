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

@XmlRootElement(name="cdpLinkNode")
@JsonRootName("cdpLinkNode")
public class CdpLinkNodeDTO {

    private String  cdpLocalPort;

    private String  cdpLocalPortUrl;

    private String cdpCacheDevice;

    private String cdpCacheDeviceUrl;

    private String cdpCacheDevicePort;

    private String cdpCacheDevicePortUrl;

    private String cdpCachePlatform;

    private String cdpCreateTime;

    private String cdpLastPollTime;

    @XmlElement(name="cdpLocalPort")
    @JsonProperty("cdpLocalPort")
    public String getCdpLocalPort() {
        return cdpLocalPort;
    }

    public void setCdpLocalPort(String cdpLocalPort) {
        this.cdpLocalPort = cdpLocalPort;
    }

    public CdpLinkNodeDTO withCdpLocalPort(String cdpLocalPort) {
        this.cdpLocalPort = cdpLocalPort;
        return this;
    }

    @XmlElement(name="cdpLocalPortUrl")
    @JsonProperty("cdpLocalPortUrl")
    public String getCdpLocalPortUrl() {
        return cdpLocalPortUrl;
    }

    public void setCdpLocalPortUrl(String cdpLocalPortUrl) {
        this.cdpLocalPortUrl = cdpLocalPortUrl;
    }

    public CdpLinkNodeDTO withCdpLocalPortUrl(String cdpLocalPortUrl) {
        this.cdpLocalPortUrl = cdpLocalPortUrl;
        return this;
    }

    @XmlElement(name="cdpCacheDevice")
    @JsonProperty("cdpCacheDevice")
    public String getCdpCacheDevice() {
        return cdpCacheDevice;
    }

    public void setCdpCacheDevice(String cdpCacheDevice) {
        this.cdpCacheDevice = cdpCacheDevice;
    }

    public CdpLinkNodeDTO withCdpCacheDevice(String cdpCacheDevice) {
        this.cdpCacheDevice = cdpCacheDevice;
        return this;
    }

    @XmlElement(name="cdpCacheDeviceUrl")
    @JsonProperty("cdpCacheDeviceUrl")
    public String getCdpCacheDeviceUrl() {
        return cdpCacheDeviceUrl;
    }

    public void setCdpCacheDeviceUrl(String cdpCacheDeviceUrl) {
        this.cdpCacheDeviceUrl = cdpCacheDeviceUrl;
    }

    public CdpLinkNodeDTO withCdpCacheDeviceUrl(String cdpCacheDeviceUrl) {
        this.cdpCacheDeviceUrl = cdpCacheDeviceUrl;
        return this;
    }

    @XmlElement(name="cdpCacheDevicePort")
    @JsonProperty("cdpCacheDevicePort")
    public String getCdpCacheDevicePort() {
        return cdpCacheDevicePort;
    }

    public void setCdpCacheDevicePort(String cdpCacheDevicePort) {
        this.cdpCacheDevicePort = cdpCacheDevicePort;
    }

    public CdpLinkNodeDTO withCdpCacheDevicePort(String cdpCacheDevicePort) {
        this.cdpCacheDevicePort = cdpCacheDevicePort;
        return this;
    }

    @XmlElement(name="cdpCacheDevicePortUrl")
    @JsonProperty("cdpCacheDevicePortUrl")
    public String getCdpCacheDevicePortUrl() {
        return cdpCacheDevicePortUrl;
    }

    public void setCdpCacheDevicePortUrl(String cdpCacheDevicePortUrl) {
        this.cdpCacheDevicePortUrl = cdpCacheDevicePortUrl;
    }

    public CdpLinkNodeDTO withCdpCacheDevicePortUrl(String cdpCacheDevicePortUrl) {
        this.cdpCacheDevicePortUrl = cdpCacheDevicePortUrl;
        return this;
    }

    @XmlElement(name="cdpCachePlatform")
    @JsonProperty("cdpCachePlatform")
    public String getCdpCachePlatform() {
        return cdpCachePlatform;
    }

    public void setCdpCachePlatform(String cdpCachePlatform) {
        this.cdpCachePlatform = cdpCachePlatform;
    }

    public CdpLinkNodeDTO withCdpCachePlatform(String cdpCachePlatform) {
        this.cdpCachePlatform = cdpCachePlatform;
        return this;
    }

    @XmlElement(name="cdpCreateTime")
    @JsonProperty("cdpCreateTime")
    public String getCdpCreateTime() {
        return cdpCreateTime;
    }

    public void setCdpCreateTime(String cdpCreateTime) {
        this.cdpCreateTime = cdpCreateTime;
    }

    public CdpLinkNodeDTO withCdpCreateTime(String cdpCreateTime) {
        this.cdpCreateTime = cdpCreateTime;
        return this;
    }

    @XmlElement(name="cdpLastPollTime")
    @JsonProperty("cdpLastPollTime")
    public String getCdpLastPollTime() {
        return cdpLastPollTime;
    }

    public void setCdpLastPollTime(String cdpLastPollTime) {
        this.cdpLastPollTime = cdpLastPollTime;
    }

    public CdpLinkNodeDTO withCdpLastPollTime(String cdpLastPollTime) {
        this.cdpLastPollTime = cdpLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CdpLinkNodeDTO that = (CdpLinkNodeDTO) o;
        return Objects.equals(cdpLocalPort, that.cdpLocalPort) && Objects.equals(cdpLocalPortUrl, that.cdpLocalPortUrl) && Objects.equals(cdpCacheDevice, that.cdpCacheDevice) && Objects.equals(cdpCacheDeviceUrl, that.cdpCacheDeviceUrl) && Objects.equals(cdpCacheDevicePort, that.cdpCacheDevicePort) && Objects.equals(cdpCacheDevicePortUrl, that.cdpCacheDevicePortUrl) && Objects.equals(cdpCachePlatform, that.cdpCachePlatform) && Objects.equals(cdpCreateTime, that.cdpCreateTime) && Objects.equals(cdpLastPollTime, that.cdpLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdpLocalPort, cdpLocalPortUrl, cdpCacheDevice, cdpCacheDeviceUrl, cdpCacheDevicePort, cdpCacheDevicePortUrl, cdpCachePlatform, cdpCreateTime, cdpLastPollTime);
    }

    @Override
    public String toString() {
        return "CdpLinkNodeDTO{" +
                "cdpLocalPort='" + cdpLocalPort + '\'' +
                ", cdpLocalPortUrl='" + cdpLocalPortUrl + '\'' +
                ", cdpCacheDevice='" + cdpCacheDevice + '\'' +
                ", cdpCacheDeviceUrl='" + cdpCacheDeviceUrl + '\'' +
                ", cdpCacheDevicePort='" + cdpCacheDevicePort + '\'' +
                ", cdpCacheDevicePortUrl='" + cdpCacheDevicePortUrl + '\'' +
                ", cdpCachePlatform='" + cdpCachePlatform + '\'' +
                ", cdpCreateTime='" + cdpCreateTime + '\'' +
                ", cdpLastPollTime='" + cdpLastPollTime + '\'' +
                '}';
    }
}
