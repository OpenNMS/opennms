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

@XmlRootElement(name="isisElementNode")
@JsonRootName("isisElementNode")
public class IsisElementNodeDTO {
    private String isisSysID;

    private String isisSysAdminState;

    private String isisCreateTime;

    private String isisLastPollTime;

    @XmlElement(name="isisSysID")
    @JsonProperty("isisSysID")
    public String getIsisSysID() {
        return isisSysID;
    }

    public void setIsisSysID(String isisSysID) {
        this.isisSysID = isisSysID;
    }

    public IsisElementNodeDTO withIsisSysID(String isisSysID) {
        this.isisSysID = isisSysID;
        return this;
    }

    @XmlElement(name="isisSysAdminState")
    @JsonProperty("isisSysAdminState")
    public String getIsisSysAdminState() {
        return isisSysAdminState;
    }

    public void setIsisSysAdminState(String isisSysAdminState) {
        this.isisSysAdminState = isisSysAdminState;
    }

    public IsisElementNodeDTO withIsisSysAdminState(String isisSysAdminState) {
        this.isisSysAdminState = isisSysAdminState;
        return this;
    }

    @XmlElement(name="isisCreateTime")
    @JsonProperty("isisCreateTime")
    public String getIsisCreateTime() {
        return isisCreateTime;
    }

    public void setIsisCreateTime(String isisCreateTime) {
        this.isisCreateTime = isisCreateTime;
    }

    public IsisElementNodeDTO withIsisCreateTime(String isisCreateTime) {
        this.isisCreateTime = isisCreateTime;
        return this;
    }

    @XmlElement(name="isisLastPollTime")
    @JsonProperty("isisLastPollTime")
    public String getIsisLastPollTime() {
        return isisLastPollTime;
    }

    public void setIsisLastPollTime(String isisLastPollTime) {
        this.isisLastPollTime = isisLastPollTime;
    }

    public IsisElementNodeDTO withIsisLastPollTime(String isisLastPollTime) {
        this.isisLastPollTime = isisLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsisElementNodeDTO that = (IsisElementNodeDTO) o;
        return Objects.equals(isisSysID, that.isisSysID) && Objects.equals(isisSysAdminState, that.isisSysAdminState) && Objects.equals(isisCreateTime, that.isisCreateTime) && Objects.equals(isisLastPollTime, that.isisLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isisSysID, isisSysAdminState, isisCreateTime, isisLastPollTime);
    }

    @Override
    public String toString() {
        return "IsisElementNodeDTO{" +
                "isisSysID='" + isisSysID + '\'' +
                ", isisSysAdminState='" + isisSysAdminState + '\'' +
                ", isisCreateTime='" + isisCreateTime + '\'' +
                ", isisLastPollTime='" + isisLastPollTime + '\'' +
                '}';
    }
}
