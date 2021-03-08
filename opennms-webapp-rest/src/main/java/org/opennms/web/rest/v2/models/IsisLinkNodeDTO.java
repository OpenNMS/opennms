/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.models;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="isisLinkNode")
@JsonRootName("isisLinkNode")
public class IsisLinkNodeDTO {

    @XmlElement(name="isisCircIfIndex")
    @JsonProperty("isisCircIfIndex")
    private Integer isisCircIfIndex;

    @XmlElement(name="isisCircAdminState")
    @JsonProperty("isisCircAdminState")
    private String  isisCircAdminState;

    @XmlElement(name="isisISAdjNeighSysID")
    @JsonProperty("isisISAdjNeighSysID")
    private String  isisISAdjNeighSysID;

    @XmlElement(name="isisISAdjNeighSysType")
    @JsonProperty("isisISAdjNeighSysType")
    private String  isisISAdjNeighSysType;

    @XmlElement(name="isisISAdjNeighSysUrl")
    @JsonProperty("isisISAdjNeighSysUrl")
    private String  isisISAdjNeighSysUrl;

    @XmlElement(name="isisISAdjNeighSNPAAddress")
    @JsonProperty("isisISAdjNeighSNPAAddress")
    private String  isisISAdjNeighSNPAAddress;

    @XmlElement(name="isisISAdjNeighPort")
    @JsonProperty("isisISAdjNeighPort")
    private String  isisISAdjNeighPort;

    @XmlElement(name="isisISAdjState")
    @JsonProperty("isisISAdjState")
    private String  isisISAdjState;

    @XmlElement(name="isisISAdjNbrExtendedCircID")
    @JsonProperty("isisISAdjNbrExtendedCircID")
    private Integer isisISAdjNbrExtendedCircID;

    @XmlElement(name="isisISAdjUrl")
    @JsonProperty("isisISAdjUrl")
    private String  isisISAdjUrl;

    @XmlElement(name="isisLinkCreateTime")
    @JsonProperty("isisLinkCreateTime")
    private String  isisLinkCreateTime;

    @XmlElement(name="isisLinkLastPollTime")
    @JsonProperty("isisLinkLastPollTime")
    private String  isisLinkLastPollTime;

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
}
