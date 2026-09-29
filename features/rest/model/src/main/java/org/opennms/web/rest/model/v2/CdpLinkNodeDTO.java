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

@XmlRootElement(name="cdpLinkNode")
@JsonRootName("cdpLinkNode")
@Schema(description = "One discovered CDP neighbour of the node. Every field is a display string, with ifIndexes embedded in the port labels and the timestamps locale-formatted rather than epoch milliseconds.")
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
    @Schema(description = "Local port as a display label built from the CDP interface name, the SNMP ifName, (ifindex:N) and the interface address, whichever of those resolved.", example = "GigabitEthernet0/1(ifindex:2)(10.10.1.1)")
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
    @Schema(description = "Relative URL of the local SNMP interface page. Omitted when the ifIndex could not be resolved.", example = "element/snmpinterface.jsp?node=1&ifindex=2")
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
    @Schema(description = "Neighbour device as a display label: the raw cdpCacheDeviceId, or the node label followed by (Cisco Device Id:...) once the neighbour resolves to a node.", example = "core-sw-01(Cisco Device Id:SEP001B213C4D5E)")
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
    @Schema(description = "Relative URL of the neighbour's linked-node page. Omitted when the neighbour does not resolve to a node.", example = "element/linkednode.jsp?node=7")
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
    @Schema(description = "Neighbour port as a display label, in the same form as cdpLocalPort.", example = "GigabitEthernet0/24(ifindex:24)")
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
    @Schema(description = "Relative URL of the neighbour's SNMP interface page. Omitted when the remote ifIndex could not be resolved.", example = "element/snmpinterface.jsp?node=7&ifindex=24")
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
    @Schema(description = "Neighbour platform and software version joined by an arrow, built as cdpCacheDevicePlatform + space-arrow-space + cdpCacheVersion.", example = "cisco WS-C3750G-24TS -> Cisco IOS Software, C3750 Software")
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
    @Schema(description = "Create timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space.", example = "8/18/26, 1:16:57\u202fPM")
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
    @Schema(description = "Last-poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space.", example = "8/18/26, 1:16:57\u202fPM")
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
