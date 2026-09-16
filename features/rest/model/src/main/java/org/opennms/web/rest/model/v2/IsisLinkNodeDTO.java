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

@XmlRootElement(name="isisLinkNode")
@JsonRootName("isisLinkNode")
@Schema(description = "One discovered IS-IS adjacency of the node. Most fields are display strings; isisCircIfIndex and isisISAdjNbrExtendedCircID are numeric.")
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
    @Schema(description = "ifIndex of the local IS-IS circuit: the raw ifIndex, not a display label.", example = "2")
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
    @Schema(description = "Administrative state of the circuit, as a word rather than the SNMP integer.", example = "on", allowableValues = {"on", "off"})
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
    @Schema(description = "Neighbour system id as a display label: the raw isisISAdjNeighSysID, or the node label followed by (isis system id:...) once the neighbour resolves to a node.", example = "core-rtr-01(isis system id:0000.0000.0002)")
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
    @Schema(description = "IS-IS level of the neighbour, as a word rather than the SNMP integer.", example = "l1L2IntermediateSystem", allowableValues = {"l1_IntermediateSystem", "l2IntermediateSystem", "l1L2IntermediateSystem", "unknown"})
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
    @Schema(description = "Relative URL of the neighbour's node page. The factory never assigns it, so it is absent from every response; isisISAdjUrl carries that link instead.", example = "element/linkednode.jsp?node=7")
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
    @Schema(description = "SNPA (subnetwork point of attachment) address of the neighbour, as reported.", example = "00:1b:21:3c:4d:5e")
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
    @Schema(description = "Neighbour port as a display label. When the remote interface resolves it is the port string, otherwise the fallback (Isis IS Adj Index: N) built from the adjacency index.", example = "GigabitEthernet0/2(ifindex:3)")
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
    @Schema(description = "Adjacency state, as a word rather than the SNMP integer.", example = "up", allowableValues = {"down", "initializing", "up", "failed"})
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
    @Schema(description = "isisISAdjNbrExtendedCircID as reported. Numeric, not a display label.", example = "3")
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
    @Schema(description = "Relative URL for the adjacency: the neighbour's linked-node page when the neighbour resolves to a node, otherwise the neighbour's SNMP interface page.", example = "element/linkednode.jsp?node=7")
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
    @Schema(description = "Create timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space.", example = "8/18/26, 1:16:57\u202fPM")
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
    @Schema(description = "Last-poll timestamp rendered as a locale-formatted display string, not epoch milliseconds. The separator before AM/PM is U+202F (narrow no-break space), not a plain space.", example = "8/18/26, 1:16:57\u202fPM")
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
