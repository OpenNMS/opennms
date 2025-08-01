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

@XmlRootElement(name="isisLinkNode")
@JsonRootName("isisLinkNode")
public class IsisLinkNodeDTO {

    private Integer isisCircIfIndex;

    private String  isisCircAdminState;

    private String  isisISAdjNeighSysID;

    private String  isisISAdjNeighSysType;

    private String  isisISAdjNeighSysUrl;

    private String  isisISAdjNeighSNPAAddress;

    private String  isisISAdjNeighPort;

    private String  isisISAdjState;

    private Integer isisISAdjNbrExtendedCircID;

    private String  isisISAdjUrl;

    private String  isisLinkCreateTime;

    private String  isisLinkLastPollTime;

    @XmlElement(name="isisCircIfIndex")
    @JsonProperty("isisCircIfIndex")
    public Integer getIsisCircIfIndex() {
        return isisCircIfIndex;
    }

    public void setIsisCircIfIndex(Integer isisCircIfIndex) {
        this.isisCircIfIndex = isisCircIfIndex;
    }

    public IsisLinkNodeDTO withIsisCircIfIndex(Integer isisCircIfIndex) {
        this.isisCircIfIndex = isisCircIfIndex;
        return this;
    }

    @XmlElement(name="isisCircAdminState")
    @JsonProperty("isisCircAdminState")
    public String getIsisCircAdminState() {
        return isisCircAdminState;
    }

    public void setIsisCircAdminState(String isisCircAdminState) {
        this.isisCircAdminState = isisCircAdminState;
    }

    public IsisLinkNodeDTO withIsisCircAdminState(String isisCircAdminState) {
        this.isisCircAdminState = isisCircAdminState;
        return this;
    }

    @XmlElement(name="isisISAdjNeighSysID")
    @JsonProperty("isisISAdjNeighSysID")
    public String getIsisISAdjNeighSysID() {
        return isisISAdjNeighSysID;
    }

    public void setIsisISAdjNeighSysID(String isisISAdjNeighSysID) {
        this.isisISAdjNeighSysID = isisISAdjNeighSysID;
    }

    public IsisLinkNodeDTO withIsisISAdjNeighSysID(String isisISAdjNeighSysID) {
        this.isisISAdjNeighSysID = isisISAdjNeighSysID;
        return this;
    }

    @XmlElement(name="isisISAdjNeighSysType")
    @JsonProperty("isisISAdjNeighSysType")
    public String getIsisISAdjNeighSysType() {
        return isisISAdjNeighSysType;
    }

    public void setIsisISAdjNeighSysType(String isisISAdjNeighSysType) {
        this.isisISAdjNeighSysType = isisISAdjNeighSysType;
    }

    public IsisLinkNodeDTO withIsisISAdjNeighSysType(String isisISAdjNeighSysType) {
        this.isisISAdjNeighSysType = isisISAdjNeighSysType;
        return this;
    }

    @XmlElement(name="isisISAdjNeighSysUrl")
    @JsonProperty("isisISAdjNeighSysUrl")
    public String getIsisISAdjNeighSysUrl() {
        return isisISAdjNeighSysUrl;
    }

    public void setIsisISAdjNeighSysUrl(String isisISAdjNeighSysUrl) {
        this.isisISAdjNeighSysUrl = isisISAdjNeighSysUrl;
    }

    public IsisLinkNodeDTO withIsisISAdjNeighSysUrl(String isisISAdjNeighSysUrl) {
        this.isisISAdjNeighSysUrl = isisISAdjNeighSysUrl;
        return this;
    }

    @XmlElement(name="isisISAdjNeighSNPAAddress")
    @JsonProperty("isisISAdjNeighSNPAAddress")
    public String getIsisISAdjNeighSNPAAddress() {
        return isisISAdjNeighSNPAAddress;
    }

    public void setIsisISAdjNeighSNPAAddress(String isisISAdjNeighSNPAAddress) {
        this.isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
    }

    public IsisLinkNodeDTO withIsisISAdjNeighSNPAAddress(String isisISAdjNeighSNPAAddress) {
        this.isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
        return this;
    }

    @XmlElement(name="isisISAdjNeighPort")
    @JsonProperty("isisISAdjNeighPort")
    public String getIsisISAdjNeighPort() {
        return isisISAdjNeighPort;
    }

    public void setIsisISAdjNeighPort(String isisISAdjNeighPort) {
        this.isisISAdjNeighPort = isisISAdjNeighPort;
    }

    public IsisLinkNodeDTO withIsisISAdjNeighPort(String isisISAdjNeighPort) {
        this.isisISAdjNeighPort = isisISAdjNeighPort;
        return this;
    }

    @XmlElement(name="isisISAdjState")
    @JsonProperty("isisISAdjState")
    public String getIsisISAdjState() {
        return isisISAdjState;
    }

    public void setIsisISAdjState(String isisISAdjState) {
        this.isisISAdjState = isisISAdjState;
    }

    public IsisLinkNodeDTO withIsisISAdjState(String isisISAdjState) {
        this.isisISAdjState = isisISAdjState;
        return this;
    }

    @XmlElement(name="isisISAdjNbrExtendedCircID")
    @JsonProperty("isisISAdjNbrExtendedCircID")
    public Integer getIsisISAdjNbrExtendedCircID() {
        return isisISAdjNbrExtendedCircID;
    }

    public void setIsisISAdjNbrExtendedCircID(Integer isisISAdjNbrExtendedCircID) {
        this.isisISAdjNbrExtendedCircID = isisISAdjNbrExtendedCircID;
    }

    public IsisLinkNodeDTO withIsisISAdjNbrExtendedCircID(Integer isisISAdjNbrExtendedCircID) {
        this.isisISAdjNbrExtendedCircID = isisISAdjNbrExtendedCircID;
        return this;
    }

    @XmlElement(name="isisISAdjUrl")
    @JsonProperty("isisISAdjUrl")
    public String getIsisISAdjUrl() {
        return isisISAdjUrl;
    }

    public void setIsisISAdjUrl(String isisISAdjUrl) {
        this.isisISAdjUrl = isisISAdjUrl;
    }

    public IsisLinkNodeDTO withIsisISAdjUrl(String isisISAdjUrl) {
        this.isisISAdjUrl = isisISAdjUrl;
        return this;
    }

    @XmlElement(name="isisLinkCreateTime")
    @JsonProperty("isisLinkCreateTime")
    public String getIsisLinkCreateTime() {
        return isisLinkCreateTime;
    }

    public void setIsisLinkCreateTime(String isisLinkCreateTime) {
        this.isisLinkCreateTime = isisLinkCreateTime;
    }

    public IsisLinkNodeDTO withIsisLinkCreateTime(String isisLinkCreateTime) {
        this.isisLinkCreateTime = isisLinkCreateTime;
        return this;
    }

    @XmlElement(name="isisLinkLastPollTime")
    @JsonProperty("isisLinkLastPollTime")
    public String getIsisLinkLastPollTime() {
        return isisLinkLastPollTime;
    }

    public void setIsisLinkLastPollTime(String isisLinkLastPollTime) {
        this.isisLinkLastPollTime = isisLinkLastPollTime;
    }

    public IsisLinkNodeDTO withIsisLinkLastPollTime(String isisLinkLastPollTime) {
        this.isisLinkLastPollTime = isisLinkLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsisLinkNodeDTO that = (IsisLinkNodeDTO) o;
        return Objects.equals(isisCircIfIndex, that.isisCircIfIndex) && Objects.equals(isisCircAdminState, that.isisCircAdminState) && Objects.equals(isisISAdjNeighSysID, that.isisISAdjNeighSysID) && Objects.equals(isisISAdjNeighSysType, that.isisISAdjNeighSysType) && Objects.equals(isisISAdjNeighSysUrl, that.isisISAdjNeighSysUrl) && Objects.equals(isisISAdjNeighSNPAAddress, that.isisISAdjNeighSNPAAddress) && Objects.equals(isisISAdjNeighPort, that.isisISAdjNeighPort) && Objects.equals(isisISAdjState, that.isisISAdjState) && Objects.equals(isisISAdjNbrExtendedCircID, that.isisISAdjNbrExtendedCircID) && Objects.equals(isisISAdjUrl, that.isisISAdjUrl) && Objects.equals(isisLinkCreateTime, that.isisLinkCreateTime) && Objects.equals(isisLinkLastPollTime, that.isisLinkLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isisCircIfIndex, isisCircAdminState, isisISAdjNeighSysID, isisISAdjNeighSysType, isisISAdjNeighSysUrl, isisISAdjNeighSNPAAddress, isisISAdjNeighPort, isisISAdjState, isisISAdjNbrExtendedCircID, isisISAdjUrl, isisLinkCreateTime, isisLinkLastPollTime);
    }

    @Override
    public String toString() {
        return "IsisLinkNodeDTO{" +
                "isisCircIfIndex=" + isisCircIfIndex +
                ", isisCircAdminState='" + isisCircAdminState + '\'' +
                ", isisISAdjNeighSysID='" + isisISAdjNeighSysID + '\'' +
                ", isisISAdjNeighSysType='" + isisISAdjNeighSysType + '\'' +
                ", isisISAdjNeighSysUrl='" + isisISAdjNeighSysUrl + '\'' +
                ", isisISAdjNeighSNPAAddress='" + isisISAdjNeighSNPAAddress + '\'' +
                ", isisISAdjNeighPort='" + isisISAdjNeighPort + '\'' +
                ", isisISAdjState='" + isisISAdjState + '\'' +
                ", isisISAdjNbrExtendedCircID=" + isisISAdjNbrExtendedCircID +
                ", isisISAdjUrl='" + isisISAdjUrl + '\'' +
                ", isisLinkCreateTime='" + isisLinkCreateTime + '\'' +
                ", isisLinkLastPollTime='" + isisLinkLastPollTime + '\'' +
                '}';
    }
}
