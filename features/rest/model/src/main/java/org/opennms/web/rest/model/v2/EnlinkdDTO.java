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

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="enlinkd")
@JsonRootName("enlinkd")
public class EnlinkdDTO {

    List<LldpLinkNodeDTO> lldpLinkNodeDTOs;

    List<BridgeLinkNodeDTO> bridgeLinkNodeDTOS;

    List<CdpLinkNodeDTO> cdpLinkNodeDTOS;

    List<OspfLinkNodeDTO> ospfLinkNodeDTOS;

    List<IsisLinkNodeDTO> isisLinkNodeDTOS;

    LldpElementNodeDTO lldpElementNodeDTO;

    List<BridgeElementNodeDTO> bridgeElementNodeDTOS;

    CdpElementNodeDTO cdpElementNodeDTO;

    OspfElementNodeDTO ospfElementNodeDTO;

    IsisElementNodeDTO isisElementNodeDTO;


    @XmlElement(name="lldpLinkNodes")
    @JsonProperty("lldpLinkNodes")
    public List<LldpLinkNodeDTO> getLldpLinkNodeDTOs() {
        return lldpLinkNodeDTOs;
    }

    public void setLldpLinkNodeDTOs(List<LldpLinkNodeDTO> lldpLinkNodeDTOs) {
        this.lldpLinkNodeDTOs = lldpLinkNodeDTOs;
    }

    public EnlinkdDTO withLldpLinkNodeDTOs(List<LldpLinkNodeDTO> lldpLinkNodeDTOs) {
        this.lldpLinkNodeDTOs = lldpLinkNodeDTOs;
        return this;
    }

    @XmlElement(name="bridgeLinkNodes")
    @JsonProperty("bridgeLinkNodes")
    public List<BridgeLinkNodeDTO> getBridgeLinkNodeDTOS() {
        return bridgeLinkNodeDTOS;
    }

    public void setBridgeLinkNodeDTOS(List<BridgeLinkNodeDTO> bridgeLinkNodeDTOS) {
        this.bridgeLinkNodeDTOS = bridgeLinkNodeDTOS;
    }

    public EnlinkdDTO withBridgeLinkNodeDTOS(List<BridgeLinkNodeDTO> bridgeLinkNodeDTOS) {
        this.bridgeLinkNodeDTOS = bridgeLinkNodeDTOS;
        return this;
    }

    @XmlElement(name="cdpLinkNodes")
    @JsonProperty("cdpLinkNodes")
    public List<CdpLinkNodeDTO> getCdpLinkNodeDTOS() {
        return cdpLinkNodeDTOS;
    }

    public void setCdpLinkNodeDTOS(List<CdpLinkNodeDTO> cdpLinkNodeDTOS) {
        this.cdpLinkNodeDTOS = cdpLinkNodeDTOS;
    }

    public EnlinkdDTO withCdpLinkNodeDTOS(List<CdpLinkNodeDTO> cdpLinkNodeDTOS) {
        this.cdpLinkNodeDTOS = cdpLinkNodeDTOS;
        return this;
    }

    @XmlElement(name="ospfLinkNodes")
    @JsonProperty("ospfLinkNodes")
    public List<OspfLinkNodeDTO> getOspfLinkNodeDTOS() {
        return ospfLinkNodeDTOS;
    }

    public void setOspfLinkNodeDTOS(List<OspfLinkNodeDTO> ospfLinkNodeDTOS) {
        this.ospfLinkNodeDTOS = ospfLinkNodeDTOS;
    }

    public EnlinkdDTO withOspfLinkNodeDTOS(List<OspfLinkNodeDTO> ospfLinkNodeDTOS) {
        this.ospfLinkNodeDTOS = ospfLinkNodeDTOS;
        return this;
    }

    @XmlElement(name="isisLinkNodes")
    @JsonProperty("isisLinkNodes")
    public List<IsisLinkNodeDTO> getIsisLinkNodeDTOS() {
        return isisLinkNodeDTOS;
    }

    public void setIsisLinkNodeDTOS(List<IsisLinkNodeDTO> isisLinkNodeDTOS) {
        this.isisLinkNodeDTOS = isisLinkNodeDTOS;
    }

    public EnlinkdDTO withIsisLinkNodeDTOS(List<IsisLinkNodeDTO> isisLinkNodeDTOS) {
        this.isisLinkNodeDTOS = isisLinkNodeDTOS;
        return this;
    }

    @XmlElement(name="lldpElementNode")
    @JsonProperty("lldpElementNode")
    public LldpElementNodeDTO getLldpElementNodeDTO() {
        return lldpElementNodeDTO;
    }

    public void setLldpElementNodeDTO(LldpElementNodeDTO lldpElementNodeDTO) {
        this.lldpElementNodeDTO = lldpElementNodeDTO;
    }

    public EnlinkdDTO withLldpElementNodeDTO(LldpElementNodeDTO lldpElementNodeDTO) {
        this.lldpElementNodeDTO = lldpElementNodeDTO;
        return this;
    }


    @XmlElement(name="bridgeElementNodes")
    @JsonProperty("bridgeElementNodes")
    public List<BridgeElementNodeDTO> getBridgeElementNodeDTOS() {
        return bridgeElementNodeDTOS;
    }

    public void setBridgeElementNodeDTOS(List<BridgeElementNodeDTO> bridgeElementNodeDTOS) {
        this.bridgeElementNodeDTOS = bridgeElementNodeDTOS;
    }

    public EnlinkdDTO withBridgeElementNodeDTOS(List<BridgeElementNodeDTO> bridgeElementNodeDTOS) {
        this.bridgeElementNodeDTOS = bridgeElementNodeDTOS;
        return this;
    }

    @XmlElement(name="cdpElementNode")
    @JsonProperty("cdpElementNode")
    public CdpElementNodeDTO getCdpElementNodeDTO() {
        return cdpElementNodeDTO;
    }

    public void setCdpElementNodeDTO(CdpElementNodeDTO cdpElementNodeDTO) {
        this.cdpElementNodeDTO = cdpElementNodeDTO;
    }

    public EnlinkdDTO withCdpElementNodeDTO(CdpElementNodeDTO cdpElementNodeDTO) {
        this.cdpElementNodeDTO = cdpElementNodeDTO;
        return this;
    }

    @XmlElement(name="ospfElementNode")
    @JsonProperty("ospfElementNode")
    public OspfElementNodeDTO getOspfElementNodeDTO() {
        return ospfElementNodeDTO;
    }

    public void setOspfElementNodeDTO(OspfElementNodeDTO ospfElementNodeDTO) {
        this.ospfElementNodeDTO = ospfElementNodeDTO;
    }

    public EnlinkdDTO withOspfElementNodeDTO(OspfElementNodeDTO ospfElementNodeDTO) {
        this.ospfElementNodeDTO = ospfElementNodeDTO;
        return this;
    }

    @XmlElement(name="isisElementNode")
    @JsonProperty("isisElementNode")
    public IsisElementNodeDTO getIsisElementNodeDTO() {
        return isisElementNodeDTO;
    }

    public void setIsisElementNodeDTO(IsisElementNodeDTO isisElementNodeDTO) {
        this.isisElementNodeDTO = isisElementNodeDTO;
    }

    public EnlinkdDTO withIsisElementNodeDTO(IsisElementNodeDTO isisElementNodeDTO) {
        this.isisElementNodeDTO = isisElementNodeDTO;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnlinkdDTO that = (EnlinkdDTO) o;
        return Objects.equals(lldpLinkNodeDTOs, that.lldpLinkNodeDTOs) && Objects.equals(bridgeLinkNodeDTOS, that.bridgeLinkNodeDTOS) && Objects.equals(cdpLinkNodeDTOS, that.cdpLinkNodeDTOS) && Objects.equals(ospfLinkNodeDTOS, that.ospfLinkNodeDTOS) && Objects.equals(isisLinkNodeDTOS, that.isisLinkNodeDTOS) && Objects.equals(lldpElementNodeDTO, that.lldpElementNodeDTO) && Objects.equals(bridgeElementNodeDTOS, that.bridgeElementNodeDTOS) && Objects.equals(cdpElementNodeDTO, that.cdpElementNodeDTO) && Objects.equals(ospfElementNodeDTO, that.ospfElementNodeDTO) && Objects.equals(isisElementNodeDTO, that.isisElementNodeDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lldpLinkNodeDTOs, bridgeLinkNodeDTOS, cdpLinkNodeDTOS, ospfLinkNodeDTOS, isisLinkNodeDTOS, lldpElementNodeDTO, bridgeElementNodeDTOS, cdpElementNodeDTO, ospfElementNodeDTO, isisElementNodeDTO);
    }

    @Override
    public String toString() {
        return "EnlinkdDTO{" +
                "lldpLinkNodeDTOs=" + lldpLinkNodeDTOs +
                ", bridgeLinkNodeDTOS=" + bridgeLinkNodeDTOS +
                ", cdpLinkNodeDTOS=" + cdpLinkNodeDTOS +
                ", ospfLinkNodeDTOS=" + ospfLinkNodeDTOS +
                ", isisLinkNodeDTOS=" + isisLinkNodeDTOS +
                ", lldpElementNodeDTO=" + lldpElementNodeDTO +
                ", bridgeElementNodeDTOS=" + bridgeElementNodeDTOS +
                ", cdpElementNodeDTO=" + cdpElementNodeDTO +
                ", ospfElementNodeDTO=" + ospfElementNodeDTO +
                ", isisElementNodeDTO=" + isisElementNodeDTO +
                '}';
    }
}
