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

@XmlRootElement(name="cdpElementNode")
@JsonRootName("cdpElementNode")
public class CdpElementNodeDTO {
    private String cdpGlobalRun;

    private String cdpGlobalDeviceId;

    private String cdpGlobalDeviceIdFormat;

    private String cdpCreateTime;

    private String cdpLastPollTime;

    @XmlElement(name="cdpGlobalRun")
    @JsonProperty("cdpGlobalRun")
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
