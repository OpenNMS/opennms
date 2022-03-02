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

package org.opennms.web.rest.model.v2;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name="bridgeElementNode")
@XmlAccessorType(XmlAccessType.NONE)
public class BridgeElementNodeDTO {

    private String  baseBridgeAddress;
    private Integer baseNumPorts;
    private String  baseType;
    private String  stpProtocolSpecification;
    private Integer stpPriority;
    private String  stpDesignatedRoot;
    private Integer stpRootCost;
    private Integer stpRootPort;
    private Integer vlan;
    private String  vlanname;
    private String  bridgeNodeCreateTime;
    private String  bridgeNodeLastPollTime;

    @XmlElement(name="baseBridgeAddress")
    @JsonProperty("baseBridgeAddress")
    public String getBaseBridgeAddress() {
        return baseBridgeAddress;
    }

    public void setBaseBridgeAddress(String baseBridgeAddress) {
        this.baseBridgeAddress = baseBridgeAddress;
    }

    public BridgeElementNodeDTO withBaseBridgeAddress(String baseBridgeAddress) {
        this.baseBridgeAddress = baseBridgeAddress;
        return this;
    }

    @XmlElement(name="baseNumPorts")
    @JsonProperty("baseNumPorts")
    public Integer getBaseNumPorts() {
        return baseNumPorts;
    }

    public void setBaseNumPorts(Integer baseNumPorts) {
        this.baseNumPorts = baseNumPorts;
    }

    public BridgeElementNodeDTO withBaseNumPorts(Integer baseNumPorts) {
        this.baseNumPorts = baseNumPorts;
        return this;
    }

    @XmlElement(name="baseType")
    @JsonProperty("baseType")
    public String getBaseType() {
        return baseType;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    public BridgeElementNodeDTO withBaseType(String baseType) {
        this.baseType = baseType;
        return this;
    }

    @XmlElement(name="stpProtocolSpecification")
    @JsonProperty("stpProtocolSpecification")
    public String getStpProtocolSpecification() {
        return stpProtocolSpecification;
    }

    public void setStpProtocolSpecification(String stpProtocolSpecification) {
        this.stpProtocolSpecification = stpProtocolSpecification;
    }

    public BridgeElementNodeDTO withStpProtocolSpecification(String stpProtocolSpecification) {
        this.stpProtocolSpecification = stpProtocolSpecification;
        return this;
    }

    @XmlElement(name="stpPriority")
    @JsonProperty("stpPriority")
    public Integer getStpPriority() {
        return stpPriority;
    }

    public void setStpPriority(Integer stpPriority) {
        this.stpPriority = stpPriority;
    }

    public BridgeElementNodeDTO withStpPriority(Integer stpPriority) {
        this.stpPriority = stpPriority;
        return this;
    }

    @XmlElement(name="stpDesignatedRoot")
    @JsonProperty("stpDesignatedRoot")
    public String getStpDesignatedRoot() {
        return stpDesignatedRoot;
    }

    public void setStpDesignatedRoot(String stpDesignatedRoot) {
        this.stpDesignatedRoot = stpDesignatedRoot;
    }

    public BridgeElementNodeDTO withStpDesignatedRoot(String stpDesignatedRoot) {
        this.stpDesignatedRoot = stpDesignatedRoot;
        return this;
    }

    @XmlElement(name="stpRootCost")
    @JsonProperty("stpRootCost")
    public Integer getStpRootCost() {
        return stpRootCost;
    }

    public void setStpRootCost(Integer stpRootCost) {
        this.stpRootCost = stpRootCost;
    }

    public BridgeElementNodeDTO withStpRootCost(Integer stpRootCost) {
        this.stpRootCost = stpRootCost;
        return this;
    }

    @XmlElement(name="stpRootPort")
    @JsonProperty("stpRootPort")
    public Integer getStpRootPort() {
        return stpRootPort;
    }

    public void setStpRootPort(Integer stpRootPort) {
        this.stpRootPort = stpRootPort;
    }

    public BridgeElementNodeDTO withStpRootPort(Integer stpRootPort) {
        this.stpRootPort = stpRootPort;
        return this;
    }

    @XmlElement(name="vlan")
    @JsonProperty("vlan")
    public Integer getVlan() {
        return vlan;
    }

    public void setVlan(Integer vlan) {
        this.vlan = vlan;
    }

    public BridgeElementNodeDTO withVlan(Integer vlan) {
        this.vlan = vlan;
        return this;
    }

    @XmlElement(name="vlanname")
    @JsonProperty("vlanname")
    public String getVlanname() {
        return vlanname;
    }

    public void setVlanname(String vlanname) {
        this.vlanname = vlanname;
    }

    public BridgeElementNodeDTO withVlanname(String vlanname) {
        this.vlanname = vlanname;
        return this;
    }

    @XmlElement(name="bridgeNodeCreateTime")
    @JsonProperty("bridgeNodeCreateTime")
    public String getBridgeNodeCreateTime() {
        return bridgeNodeCreateTime;
    }

    public void setBridgeNodeCreateTime(String bridgeNodeCreateTime) {
        this.bridgeNodeCreateTime = bridgeNodeCreateTime;
    }

    public BridgeElementNodeDTO withBridgeNodeCreateTime(String bridgeNodeCreateTime) {
        this.bridgeNodeCreateTime = bridgeNodeCreateTime;
        return this;
    }

    @XmlElement(name="bridgeNodeLastPollTime")
    @JsonProperty("bridgeNodeLastPollTime")
    public String getBridgeNodeLastPollTime() {
        return bridgeNodeLastPollTime;
    }

    public void setBridgeNodeLastPollTime(String bridgeNodeLastPollTime) {
        this.bridgeNodeLastPollTime = bridgeNodeLastPollTime;
    }

    public BridgeElementNodeDTO withBridgeNodeLastPollTime(String bridgeNodeLastPollTime) {
        this.bridgeNodeLastPollTime = bridgeNodeLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgeElementNodeDTO that = (BridgeElementNodeDTO) o;
        return Objects.equals(baseBridgeAddress, that.baseBridgeAddress) && Objects.equals(baseNumPorts, that.baseNumPorts) && Objects.equals(baseType, that.baseType) && Objects.equals(stpProtocolSpecification, that.stpProtocolSpecification) && Objects.equals(stpPriority, that.stpPriority) && Objects.equals(stpDesignatedRoot, that.stpDesignatedRoot) && Objects.equals(stpRootCost, that.stpRootCost) && Objects.equals(stpRootPort, that.stpRootPort) && Objects.equals(vlan, that.vlan) && Objects.equals(vlanname, that.vlanname) && Objects.equals(bridgeNodeCreateTime, that.bridgeNodeCreateTime) && Objects.equals(bridgeNodeLastPollTime, that.bridgeNodeLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseBridgeAddress, baseNumPorts, baseType, stpProtocolSpecification, stpPriority, stpDesignatedRoot, stpRootCost, stpRootPort, vlan, vlanname, bridgeNodeCreateTime, bridgeNodeLastPollTime);
    }

    @Override
    public String toString() {
        return "BridgeElementNodeDTO{" +
                "baseBridgeAddress='" + baseBridgeAddress + '\'' +
                ", baseNumPorts=" + baseNumPorts +
                ", baseType='" + baseType + '\'' +
                ", stpProtocolSpecification='" + stpProtocolSpecification + '\'' +
                ", stpPriority=" + stpPriority +
                ", stpDesignatedRoot='" + stpDesignatedRoot + '\'' +
                ", stpRootCost=" + stpRootCost +
                ", stpRootPort=" + stpRootPort +
                ", vlan=" + vlan +
                ", vlanname='" + vlanname + '\'' +
                ", bridgeNodeCreateTime='" + bridgeNodeCreateTime + '\'' +
                ", bridgeNodeLastPollTime='" + bridgeNodeLastPollTime + '\'' +
                '}';
    }
}
