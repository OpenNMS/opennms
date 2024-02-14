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

@XmlRootElement(name="ospfElementNode")
@JsonRootName("ospfElementNode")
public class OspfElementNodeDTO {

    private String ospfRouterId;

    private Integer ospfVersionNumber;

    private String ospfAdminStat;

    private String ospfCreateTime;

    private String ospfLastPollTime;

    @XmlElement(name="ospfRouterId")
    @JsonProperty("ospfRouterId")
    public String getOspfRouterId() {
        return ospfRouterId;
    }

    public void setOspfRouterId(String ospfRouterId) {
        this.ospfRouterId = ospfRouterId;
    }

    public OspfElementNodeDTO withOspfRouterId(String ospfRouterId) {
        this.ospfRouterId = ospfRouterId;
        return this;
    }

    @XmlElement(name="ospfVersionNumber")
    @JsonProperty("ospfVersionNumber")
    public Integer getOspfVersionNumber() {
        return ospfVersionNumber;
    }

    public void setOspfVersionNumber(Integer ospfVersionNumber) {
        this.ospfVersionNumber = ospfVersionNumber;
    }

    public OspfElementNodeDTO withOspfVersionNumber(Integer ospfVersionNumber) {
        this.ospfVersionNumber = ospfVersionNumber;
        return this;
    }

    @XmlElement(name="ospfAdminStat")
    @JsonProperty("ospfAdminStat")
    public String getOspfAdminStat() {
        return ospfAdminStat;
    }

    public void setOspfAdminStat(String ospfAdminStat) {
        this.ospfAdminStat = ospfAdminStat;
    }

    public OspfElementNodeDTO withOspfAdminStat(String ospfAdminStat) {
        this.ospfAdminStat = ospfAdminStat;
        return this;
    }

    @XmlElement(name="ospfCreateTime")
    @JsonProperty("ospfCreateTime")
    public String getOspfCreateTime() {
        return ospfCreateTime;
    }

    public void setOspfCreateTime(String ospfCreateTime) {
        this.ospfCreateTime = ospfCreateTime;
    }

    public OspfElementNodeDTO withOspfCreateTime(String ospfCreateTime) {
        this.ospfCreateTime = ospfCreateTime;
        return this;
    }

    @XmlElement(name="ospfLastPollTime")
    @JsonProperty("ospfLastPollTime")
    public String getOspfLastPollTime() {
        return ospfLastPollTime;
    }

    public void setOspfLastPollTime(String ospfLastPollTime) {
        this.ospfLastPollTime = ospfLastPollTime;
    }

    public OspfElementNodeDTO withOspfLastPollTime(String ospfLastPollTime) {
        this.ospfLastPollTime = ospfLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OspfElementNodeDTO that = (OspfElementNodeDTO) o;
        return Objects.equals(ospfRouterId, that.ospfRouterId) && Objects.equals(ospfVersionNumber, that.ospfVersionNumber) && Objects.equals(ospfAdminStat, that.ospfAdminStat) && Objects.equals(ospfCreateTime, that.ospfCreateTime) && Objects.equals(ospfLastPollTime, that.ospfLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ospfRouterId, ospfVersionNumber, ospfAdminStat, ospfCreateTime, ospfLastPollTime);
    }

    @Override
    public String toString() {
        return "OspfElementNodeDTO{" +
                "ospfRouterId='" + ospfRouterId + '\'' +
                ", ospfVersionNumber=" + ospfVersionNumber +
                ", ospfAdminStat='" + ospfAdminStat + '\'' +
                ", ospfCreateTime='" + ospfCreateTime + '\'' +
                ", ospfLastPollTime='" + ospfLastPollTime + '\'' +
                '}';
    }
}
