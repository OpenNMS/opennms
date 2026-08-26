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

@XmlRootElement(name="cdpElementNode")
@JsonRootName("cdpElementNode")
@Schema(description = "The CDP process on the node itself. Omitted from EnlinkdDTO entirely, not serialized as null, when the node has no CDP element.")
public class CdpElementNodeDTO {
    private String cdpGlobalRun;

    private String cdpGlobalDeviceId;

    private String cdpGlobalDeviceIdFormat;

    private String cdpCreateTime;

    private String cdpLastPollTime;

    @XmlElement(name="cdpGlobalRun")
    @JsonProperty("cdpGlobalRun")
    @Schema(description = "Whether the CDP protocol is running on the node, as the word form of the SNMP TruthValue.", example = "true", allowableValues = {"true", "false"})
    public String getCdpGlobalRun() {
        return cdpGlobalRun;
    }

    public void setCdpGlobalRun(String cdpGlobalRun) {
        this.cdpGlobalRun = cdpGlobalRun;
    }

    public CdpElementNodeDTO withCdpGlobalRun(String cdpGlobalRun) {
        this.cdpGlobalRun = cdpGlobalRun;
        return this;
    }

    @XmlElement(name="cdpGlobalDeviceId")
    @JsonProperty("cdpGlobalDeviceId")
    @Schema(description = "cdpGlobalDeviceId as reported by the device.", example = "SEP001B213C4D5E")
    public String getCdpGlobalDeviceId() {
        return cdpGlobalDeviceId;
    }

    public void setCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        this.cdpGlobalDeviceId = cdpGlobalDeviceId;
    }

    public CdpElementNodeDTO withCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        this.cdpGlobalDeviceId = cdpGlobalDeviceId;
        return this;
    }

    @XmlElement(name="cdpGlobalDeviceIdFormat")
    @JsonProperty("cdpGlobalDeviceIdFormat")
    @Schema(description = "Format of cdpGlobalDeviceId. The v2 mapper never sets this field, so it is absent from every response even when the underlying element has it; the JSP UI shows serialNumber, macAddress or other.", example = "macAddress", allowableValues = {"serialNumber", "macAddress", "other"})
    public String getCdpGlobalDeviceIdFormat() {
        return cdpGlobalDeviceIdFormat;
    }

    public void setCdpGlobalDeviceIdFormat(String cdpGlobalDeviceIdFormat) {
        this.cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
    }

    public CdpElementNodeDTO withCdpGlobalDeviceIdFormat(String cdpGlobalDeviceIdFormat) {
        this.cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
        return this;
    }

    @XmlElement(name="cdpCreateTime")
    @JsonProperty("cdpCreateTime")
    @Schema(description = "Poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space. Treat it as an opaque label.", example = "8/18/26, 1:16:57\u202fPM")
    public String getCdpCreateTime() {
        return cdpCreateTime;
    }

    public void setCdpCreateTime(String cdpCreateTime) {
        this.cdpCreateTime = cdpCreateTime;
    }

    public CdpElementNodeDTO withCdpCreateTime(String cdpCreateTime) {
        this.cdpCreateTime = cdpCreateTime;
        return this;
    }

    @XmlElement(name="cdpLastPollTime")
    @JsonProperty("cdpLastPollTime")
    @Schema(description = "Poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space. Treat it as an opaque label.", example = "8/18/26, 1:16:57\u202fPM")
    public String getCdpLastPollTime() {
        return cdpLastPollTime;
    }

    public void setCdpLastPollTime(String cdpLastPollTime) {
        this.cdpLastPollTime = cdpLastPollTime;
    }

    public CdpElementNodeDTO withCdpLastPollTime(String cdpLastPollTime) {
        this.cdpLastPollTime = cdpLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CdpElementNodeDTO that = (CdpElementNodeDTO) o;
        return Objects.equals(cdpGlobalRun, that.cdpGlobalRun) && Objects.equals(cdpGlobalDeviceId, that.cdpGlobalDeviceId) && Objects.equals(cdpGlobalDeviceIdFormat, that.cdpGlobalDeviceIdFormat) && Objects.equals(cdpCreateTime, that.cdpCreateTime) && Objects.equals(cdpLastPollTime, that.cdpLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdpGlobalRun, cdpGlobalDeviceId, cdpGlobalDeviceIdFormat, cdpCreateTime, cdpLastPollTime);
    }

    @Override
    public String toString() {
        return "CdpElementNodeDTO{" +
                "cdpGlobalRun='" + cdpGlobalRun + '\'' +
                ", cdpGlobalDeviceId='" + cdpGlobalDeviceId + '\'' +
                ", cdpGlobalDeviceIdFormat='" + cdpGlobalDeviceIdFormat + '\'' +
                ", cdpCreateTime='" + cdpCreateTime + '\'' +
                ", cdpLastPollTime='" + cdpLastPollTime + '\'' +
                '}';
    }
}
