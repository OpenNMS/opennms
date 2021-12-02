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

package org.opennms.web.rest.v2;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.enlinkd.BridgeElementNode;
import org.opennms.web.enlinkd.BridgeLinkNode;
import org.opennms.web.enlinkd.BridgeLinkRemoteNode;
import org.opennms.web.enlinkd.CdpElementNode;
import org.opennms.web.enlinkd.CdpLinkNode;
import org.opennms.web.enlinkd.EnLinkdElementFactoryInterface;
import org.opennms.web.enlinkd.IsisElementNode;
import org.opennms.web.enlinkd.IsisLinkNode;
import org.opennms.web.enlinkd.LldpElementNode;
import org.opennms.web.enlinkd.LldpLinkNode;
import org.opennms.web.enlinkd.OspfElementNode;
import org.opennms.web.enlinkd.OspfLinkNode;
import org.opennms.web.rest.model.v2.BridgeElementNodeDTO;
import org.opennms.web.rest.model.v2.BridgeLinkNodeDTO;
import org.opennms.web.rest.model.v2.BridgeLinkRemoteNodeDTO;
import org.opennms.web.rest.model.v2.CdpElementNodeDTO;
import org.opennms.web.rest.model.v2.CdpLinkNodeDTO;
import org.opennms.web.rest.model.v2.EnlinkdDTO;
import org.opennms.web.rest.model.v2.IsisElementNodeDTO;
import org.opennms.web.rest.model.v2.IsisLinkNodeDTO;
import org.opennms.web.rest.model.v2.LldpElementNodeDTO;
import org.opennms.web.rest.model.v2.LldpLinkNodeDTO;
import org.opennms.web.rest.model.v2.OspfElementNodeDTO;
import org.opennms.web.rest.model.v2.OspfLinkNodeDTO;
import org.opennms.web.rest.v2.api.NodeLinkRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class NodeLinkRestService implements NodeLinkRestApi {

    private EnLinkdElementFactoryInterface enLinkdElementFactory;

    private NodeDao m_nodeDao;

    @Autowired
    public NodeLinkRestService(EnLinkdElementFactoryInterface enLinkdElementFactory, NodeDao m_nodeDao) {
        this.enLinkdElementFactory = enLinkdElementFactory;
        this.m_nodeDao = m_nodeDao;
    }

    @Override
    public EnlinkdDTO getEnlinkd(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return new EnlinkdDTO()
                .withLldpLinkNodeDTOs(getLldpLinks(nodeId))
                .withBridgeLinkNodeDTOS(getBridgeLinks(nodeId))
                .withCdpLinkNodeDTOS(getCdpLinks(nodeId))
                .withOspfLinkNodeDTOS(getOspfLinks(nodeId))
                .withIsisLinkNodeDTOS(getIsisLinks(nodeId))
                .withLldpElementNodeDTO(getLldpElem(nodeId))
                .withBridgeElementNodeDTOS(getBridgeElem(nodeId))
                .withCdpElementNodeDTO(getCdpElem(nodeId))
                .withOspfElementNodeDTO(getOspfelem(nodeId))
                .withIsisElementNodeDTO(getIsisElem(nodeId));
    }

    @Override
    public List<LldpLinkNodeDTO> getLldpLinks(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getLldpLinks(nodeId);
    }

    private List<LldpLinkNodeDTO> getLldpLinks(int nodeId) {
        return enLinkdElementFactory.getLldpLinks(nodeId).stream().map(this::mapLldpLindNodeToDTO).collect(Collectors.toList());
    }

    @Override
    public List<BridgeLinkNodeDTO> getBridgeLinks(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getBridgeLinks(nodeId);
    }

    private List<BridgeLinkNodeDTO> getBridgeLinks(int nodeId) {
        return enLinkdElementFactory.getBridgeLinks(nodeId).stream().map(this::mapBridgeLinkNodeToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CdpLinkNodeDTO> getCdpLinks(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getCdpLinks(nodeId);
    }

    private List<CdpLinkNodeDTO> getCdpLinks(int nodeId) {
        return enLinkdElementFactory.getCdpLinks(nodeId).stream().map(this::mapCdpLinkNodeToDTO).collect(Collectors.toList());
    }

    @Override
    public List<OspfLinkNodeDTO> getOspfLinks(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getOspfLinks(nodeId);
    }

    private List<OspfLinkNodeDTO> getOspfLinks(int nodeId) {
        return enLinkdElementFactory.getOspfLinks(nodeId).stream().map(this::mapOspfLinkNodeToDTO).collect(Collectors.toList());
    }

    @Override
    public List<IsisLinkNodeDTO> getIsisLinks(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getIsisLinks(nodeId);
    }

    private List<IsisLinkNodeDTO> getIsisLinks(int nodeId) {
        return enLinkdElementFactory.getIsisLinks(nodeId).stream().map(this::mapIsisLinkNodeToDTO).collect(Collectors.toList());
    }

    @Override
    public LldpElementNodeDTO getLldpElem(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getLldpElem(nodeId);
    }

    private LldpElementNodeDTO getLldpElem(int nodeId) {
        return mapLldElementNodeToDTO(enLinkdElementFactory.getLldpElement(nodeId));
    }

    @Override
    public List<BridgeElementNodeDTO> getBridgeElem(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getBridgeElem(nodeId);
    }

    private List<BridgeElementNodeDTO> getBridgeElem(int nodeId) {
        return enLinkdElementFactory.getBridgeElements(nodeId).stream().map(this::mapBridgeElementNodeToDTO).collect(Collectors.toList());
    }

    @Override
    public CdpElementNodeDTO getCdpElem(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getCdpElem(nodeId);
    }

    private CdpElementNodeDTO getCdpElem(int nodeId) {
        return mapCdpElementNodeToDTO(enLinkdElementFactory.getCdpElement(nodeId));
    }

    @Override
    public OspfElementNodeDTO getOspfElem(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getOspfelem(nodeId);
    }

    private OspfElementNodeDTO getOspfelem(int nodeId) {
        return mapOspfElementNodeToDTO(enLinkdElementFactory.getOspfElement(nodeId));
    }

    @Override
    public IsisElementNodeDTO getIsisElem(String nodeCriteria) {
        int nodeId = getNodeIdInDB(nodeCriteria);
        return getIsisElem(nodeId);
    }

    private IsisElementNodeDTO getIsisElem(int nodeId) {
        return mapIsisElementNodeToDTO(enLinkdElementFactory.getIsisElement(nodeId));
    }

    private int getNodeIdInDB(String nodeCriteria) {
        OnmsNode onmsNode = m_nodeDao.get(nodeCriteria);
        if (onmsNode == null) {
            throw new NoSuchElementException("Not able to find node with criteria : " + nodeCriteria);
        } else {
            return onmsNode.getId();
        }
    }

    private BridgeLinkNodeDTO mapBridgeLinkNodeToDTO(BridgeLinkNode bridgeLinkNode) {
        return bridgeLinkNode == null ? null : new BridgeLinkNodeDTO()
                .withBridgeLocalPort(bridgeLinkNode.getBridgeLocalPort())
                .withBridgeLocalPortUrl(bridgeLinkNode.getBridgeLocalPortUrl())
                .withBridgeLinkRemoteNodes(bridgeLinkNode.getBridgeLinkRemoteNodes().stream().map(this::mapBridgeLinkRemoteNodeToDTO).collect(Collectors.toList()))
                .withBridgeInfo(bridgeLinkNode.getBridgeInfo())
                .withBridgeLinkCreateTime(bridgeLinkNode.getBridgeLinkCreateTime())
                .withBridgeLinkLastPollTime(bridgeLinkNode.getBridgeLinkLastPollTime());
    }

    private BridgeElementNodeDTO mapBridgeElementNodeToDTO(BridgeElementNode bridgeElementNode) {
        return bridgeElementNode == null ? null : new BridgeElementNodeDTO()
                .withBaseBridgeAddress(bridgeElementNode.getBaseBridgeAddress())
                .withBaseNumPorts(bridgeElementNode.getBaseNumPorts())
                .withBaseType(bridgeElementNode.getBaseType())
                .withStpProtocolSpecification(bridgeElementNode.getStpProtocolSpecification())
                .withStpPriority(bridgeElementNode.getStpPriority())
                .withStpDesignatedRoot(bridgeElementNode.getStpDesignatedRoot())
                .withStpRootCost(bridgeElementNode.getStpRootCost())
                .withStpRootPort(bridgeElementNode.getStpRootPort())
                .withVlan(bridgeElementNode.getVlan())
                .withVlanname(bridgeElementNode.getVlanname());
    }

    private BridgeLinkRemoteNodeDTO mapBridgeLinkRemoteNodeToDTO(BridgeLinkRemoteNode bridgeLinkRemoteNode) {
        return bridgeLinkRemoteNode == null ? null : new BridgeLinkRemoteNodeDTO()
                .withBridgeRemote(bridgeLinkRemoteNode.getBridgeRemote())
                .withBridgeRemotePort(bridgeLinkRemoteNode.getBridgeRemotePort())
                .withBridgeRemoteUrl(bridgeLinkRemoteNode.getBridgeRemoteUrl())
                .withBridgeRemotePortUrl(bridgeLinkRemoteNode.getBridgeRemotePortUrl());
    }

    private CdpElementNodeDTO mapCdpElementNodeToDTO(CdpElementNode cdpElementNode) {
        return cdpElementNode == null ? null : new CdpElementNodeDTO()
                .withCdpGlobalRun(cdpElementNode.getCdpGlobalRun())
                .withCdpGlobalDeviceId(cdpElementNode.getCdpGlobalDeviceId())
                .withCdpGlobalDeviceId(cdpElementNode.getCdpGlobalDeviceId())
                .withCdpCreateTime(cdpElementNode.getCdpCreateTime())
                .withCdpLastPollTime(cdpElementNode.getCdpLastPollTime());
    }

    private CdpLinkNodeDTO mapCdpLinkNodeToDTO(CdpLinkNode cdpLinkNode) {
        return cdpLinkNode == null ? null : new CdpLinkNodeDTO()
                .withCdpLocalPort(cdpLinkNode.getCdpLocalPort())
                .withCdpLocalPortUrl(cdpLinkNode.getCdpLocalPortUrl())
                .withCdpCacheDevice(cdpLinkNode.getCdpCacheDevice())
                .withCdpCacheDeviceUrl(cdpLinkNode.getCdpCacheDeviceUrl())
                .withCdpCacheDevicePort(cdpLinkNode.getCdpCacheDevicePort())
                .withCdpCacheDevicePortUrl(cdpLinkNode.getCdpCacheDevicePortUrl())
                .withCdpCachePlatform(cdpLinkNode.getCdpCachePlatform())
                .withCdpCreateTime(cdpLinkNode.getCdpCreateTime())
                .withCdpLastPollTime(cdpLinkNode.getCdpLastPollTime());
    }

    private IsisElementNodeDTO mapIsisElementNodeToDTO(IsisElementNode isisElementNode) {
        return isisElementNode == null ? null : new IsisElementNodeDTO()
                .withIsisSysID(isisElementNode.getIsisSysID())
                .withIsisSysAdminState(isisElementNode.getIsisSysAdminState())
                .withIsisCreateTime(isisElementNode.getIsisCreateTime())
                .withIsisLastPollTime(isisElementNode.getIsisLastPollTime());
    }

    private IsisLinkNodeDTO mapIsisLinkNodeToDTO(IsisLinkNode isisLinkNode) {
        return isisLinkNode == null ? null : new IsisLinkNodeDTO()
                .withIsisCircIfIndex(isisLinkNode.getIsisCircIfIndex())
                .withIsisCircAdminState(isisLinkNode.getIsisCircAdminState())
                .withIsisISAdjNeighSysID(isisLinkNode.getIsisISAdjNeighSysID())
                .withIsisISAdjNeighSysType(isisLinkNode.getIsisISAdjNeighSysType())
                .withIsisISAdjNeighSysUrl(isisLinkNode.getIsisISAdjNeighSysUrl())
                .withIsisISAdjNeighSNPAAddress(isisLinkNode.getIsisISAdjNeighSNPAAddress())
                .withIsisISAdjNeighPort(isisLinkNode.getIsisISAdjNeighPort())
                .withIsisISAdjState(isisLinkNode.getIsisISAdjState())
                .withIsisISAdjNbrExtendedCircID(isisLinkNode.getIsisISAdjNbrExtendedCircID())
                .withIsisISAdjUrl(isisLinkNode.getIsisISAdjUrl())
                .withIsisLinkCreateTime(isisLinkNode.getIsisLinkCreateTime())
                .withIsisLinkLastPollTime(isisLinkNode.getIsisLinkLastPollTime());
    }

    private LldpElementNodeDTO mapLldElementNodeToDTO(LldpElementNode lldpElementNode) {
        return lldpElementNode == null ? null : new LldpElementNodeDTO()
                .withLldpChassisId(lldpElementNode.getLldpChassisId())
                .withLldpSysName(lldpElementNode.getLldpSysName())
                .withLldpCreateTime(lldpElementNode.getLldpCreateTime())
                .withLldpLastPollTime(lldpElementNode.getLldpLastPollTime());
    }

    private LldpLinkNodeDTO mapLldpLindNodeToDTO(LldpLinkNode lldpLinkNode) {
        return lldpLinkNode == null ? null : new LldpLinkNodeDTO()
                .withLldpLocalPort(lldpLinkNode.getLldpLocalPort())
                .withLldpLocalPortUrl(lldpLinkNode.getLldpLocalPortUrl())
                .withLldpRemChassisId(lldpLinkNode.getLldpRemChassisId())
                .withLldpRemChassisIdUrl(lldpLinkNode.getLldpRemChassisIdUrl())
                .withLdpRemPort(lldpLinkNode.getLldpRemPort())
                .withLldpRemInfo(lldpLinkNode.getLldpRemInfo())
                .withLldpRemPortUrl(lldpLinkNode.getLldpRemPortUrl())
                .withLldpCreateTime(lldpLinkNode.getLldpCreateTime())
                .withLldpLastPollTime(lldpLinkNode.getLldpLastPollTime());
    }

    private OspfElementNodeDTO mapOspfElementNodeToDTO(OspfElementNode ospfElementNode) {
        return ospfElementNode == null ? null : new OspfElementNodeDTO()
                .withOspfRouterId(ospfElementNode.getOspfRouterId())
                .withOspfVersionNumber(ospfElementNode.getOspfVersionNumber())
                .withOspfAdminStat(ospfElementNode.getOspfAdminStat())
                .withOspfCreateTime(ospfElementNode.getOspfCreateTime())
                .withOspfLastPollTime(ospfElementNode.getOspfLastPollTime());
    }

    private OspfLinkNodeDTO mapOspfLinkNodeToDTO(OspfLinkNode ospfLinkNode) {
        return ospfLinkNode == null ? null : new OspfLinkNodeDTO()
                .withOspfLocalPort(ospfLinkNode.getOspfLocalPort())
                .withOspfLocalPortUrl(ospfLinkNode.getOspfLocalPortUrl())
                .withOspfRemRouterId(ospfLinkNode.getOspfRemRouterId())
                .withOspfRemRouterUrl(ospfLinkNode.getOspfRemRouterUrl())
                .withOspfRemPort(ospfLinkNode.getOspfRemPort())
                .withOspfRemPortUrl(ospfLinkNode.getOspfRemPortUrl())
                .withOspfLinkInfo(ospfLinkNode.getOspfLinkInfo())
                .withOspfLinkCreateTime(ospfLinkNode.getOspfLinkCreateTime())
                .withOspfLinkLastPollTime(ospfLinkNode.getOspfLinkLastPollTime());
    }
}
