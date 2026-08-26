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

@XmlRootElement(name="lldpElementNode")
@JsonRootName("lldpElementNode")
@Schema(description = "The LLDP agent on the node itself. Omitted from EnlinkdDTO entirely, not serialized as null, when the node has no LLDP element. Every field is a display string.")
public class LldpElementNodeDTO {

    private String lldpChassisId;

    private String lldpSysName;

    private String lldpCreateTime;

    private String lldpLastPollTime;

    @XmlElement(name="lldpChassisId")
    @JsonProperty("lldpChassisId")
    @Schema(description = "Chassis id of the local LLDP agent as a display label: the id subtype and value in parentheses, for example (macAddress:00:1b:21:3c:4d:5e). Not a bare identifier.", example = "(macAddress:sc-001011)")
    public String getLldpChassisId() {
        return lldpChassisId;
    }

    public void setLldpChassisId(String lldpChassisId) {
        this.lldpChassisId = lldpChassisId;
    }

    public LldpElementNodeDTO withLldpChassisId(String lldpChassisId) {
        this.lldpChassisId = lldpChassisId;
        return this;
    }

    @XmlElement(name="lldpSysName")
    @JsonProperty("lldpSysName")
    @Schema(description = "lldpLocSysName reported by the node.", example = "scale-dist-001")
    public String getLldpSysName() {
        return lldpSysName;
    }

    public void setLldpSysName(String lldpSysName) {
        this.lldpSysName = lldpSysName;
    }

    public LldpElementNodeDTO withLldpSysName(String lldpSysName) {
        this.lldpSysName = lldpSysName;
        return this;
    }

    @XmlElement(name="lldpCreateTime")
    @JsonProperty("lldpCreateTime")
    @Schema(description = "Create timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space.", example = "8/18/26, 1:16:57\u202fPM")
    public String getLldpCreateTime() {
        return lldpCreateTime;
    }

    public void setLldpCreateTime(String lldpCreateTime) {
        this.lldpCreateTime = lldpCreateTime;
    }

    public LldpElementNodeDTO withLldpCreateTime(String lldpCreateTime) {
        this.lldpCreateTime = lldpCreateTime;
        return this;
    }

    @XmlElement(name="lldpLastPollTime")
    @JsonProperty("lldpLastPollTime")
    @Schema(description = "Last-poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space.", example = "8/18/26, 1:16:57\u202fPM")
    public String getLldpLastPollTime() {
        return lldpLastPollTime;
    }

    public void setLldpLastPollTime(String lldpLastPollTime) {
        this.lldpLastPollTime = lldpLastPollTime;
    }

    public LldpElementNodeDTO withLldpLastPollTime(String lldpLastPollTime) {
        this.lldpLastPollTime = lldpLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LldpElementNodeDTO that = (LldpElementNodeDTO) o;
        return Objects.equals(lldpChassisId, that.lldpChassisId) && Objects.equals(lldpSysName, that.lldpSysName) && Objects.equals(lldpCreateTime, that.lldpCreateTime) && Objects.equals(lldpLastPollTime, that.lldpLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lldpChassisId, lldpSysName, lldpCreateTime, lldpLastPollTime);
    }

    @Override
    public String toString() {
        return "LldpElementNodeDTO{" +
                "lldpChassisId='" + lldpChassisId + '\'' +
                ", lldpSysName='" + lldpSysName + '\'' +
                ", lldpCreateTime='" + lldpCreateTime + '\'' +
                ", lldpLastPollTime='" + lldpLastPollTime + '\'' +
                '}';
    }
}
