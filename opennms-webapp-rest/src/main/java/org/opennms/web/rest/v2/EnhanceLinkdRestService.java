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
import org.opennms.web.rest.model.v2.CdpElementNodeDTO;
import org.opennms.web.rest.v2.api.EnhanceLinkdRestApi;
import org.opennms.web.rest.model.v2.BridgeLinkNodeDTO;
import org.opennms.web.rest.model.v2.BridgeLinkRemoteNodeDTO;
import org.opennms.web.rest.model.v2.CdpLinkNodeDTO;
import org.opennms.web.rest.model.v2.EnlinkdDTO;
import org.opennms.web.rest.model.v2.IsisElementNodeDTO;
import org.opennms.web.rest.model.v2.IsisLinkNodeDTO;
import org.opennms.web.rest.model.v2.LldpElementNodeDTO;
import org.opennms.web.rest.model.v2.LldpLinkNodeDTO;
import org.opennms.web.rest.model.v2.OspfElementNodeDTO;
import org.opennms.web.rest.model.v2.OspfLinkNodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class EnhanceLinkdRestService implements EnhanceLinkdRestApi {

    @Autowired
    private EnLinkdElementFactoryInterface enLinkdElementFactory;

    @Autowired
    private NodeDao m_nodeDao;

    @Override
    public EnlinkdDTO getEnlinkd(int nodeId) {
        return new EnlinkdDTO()
                .withOspfLinkNodeDTOS(getOspfLinks(nodeId))
                .withLldpLinkNodeDTOs(getLldpLinks(nodeId))
                .withBridgeLinkNodeDTOS(getBridgelinks(nodeId))
                .withCdpLinkNodeDTOS(getCdpLinks(nodeId))
                .withOspfLinkNodeDTOS(getOspfLinks(nodeId))
                .withIsisLinkNodeDTOS(getIsisLinks(nodeId))
                .withLldpElementNodeDTO(getLldpelem(nodeId))
                .withCdpElementNodeDTO(getCdpelem(nodeId))
                .withOspfElementNodeDTO(getOspfelem(nodeId))
                .withIsisElementNodeDTO(getIsiselem(nodeId))
                ;
    }

    @Override
    public List<LldpLinkNodeDTO> getLldpLinks(int nodeId) {
        checkNodeInDB(nodeId);
        return enLinkdElementFactory.getLldpLinks(nodeId).stream().map(n -> mapLldpLindNodeToDTO(n)).collect(Collectors.toList());
    }

    @Override
    public List<BridgeLinkNodeDTO> getBridgelinks(int nodeId) {
        checkNodeInDB(nodeId);
        return enLinkdElementFactory.getBridgeLinks(nodeId).stream().map(n -> mapBridgeLinkNodeToDTO(n)).collect(Collectors.toList());
    }

    @Override
    public List<CdpLinkNodeDTO> getCdpLinks(int nodeId) {
        checkNodeInDB(nodeId);
        return enLinkdElementFactory.getCdpLinks(nodeId).stream().map(n -> mapCdpLinkNodeToDTO(n)).collect(Collectors.toList());
    }

    @Override
    public List<OspfLinkNodeDTO> getOspfLinks(int nodeId) {
        checkNodeInDB(nodeId);
        return enLinkdElementFactory.getOspfLinks(nodeId).stream().map(n -> mapOspfLinkNodeToDTO(n)).collect(Collectors.toList());
    }

    @Override
    public List<IsisLinkNodeDTO> getIsisLinks(int nodeId) {
        checkNodeInDB(nodeId);
        return enLinkdElementFactory.getIsisLinks(nodeId).stream().map(n -> mapIsisLinkNodeToDTO(n)).collect(Collectors.toList());
    }

    @Override
    public LldpElementNodeDTO getLldpelem(int nodeId) {
        checkNodeInDB(nodeId);
        return mapLldElementNodeToDTO(enLinkdElementFactory.getLldpElement(nodeId));
    }

    @Override
    public CdpElementNodeDTO getCdpelem(int nodeId) {
        checkNodeInDB(nodeId);
        return mapCdpElementNodeToDTO(enLinkdElementFactory.getCdpElement(nodeId));
    }

    @Override
    public OspfElementNodeDTO getOspfelem(int nodeId) {
        checkNodeInDB(nodeId);
        return mapOspfElementNodeToDTO(enLinkdElementFactory.getOspfElement(nodeId));
    }

    @Override
    public IsisElementNodeDTO getIsiselem(int nodeId) {
        checkNodeInDB(nodeId);
        return mapIsisElementNodeToDTO(enLinkdElementFactory.getIsisElement(nodeId));
    }

    private void checkNodeInDB(int nodeId) {
        if (m_nodeDao.get(nodeId) == null) {
            throw new NoSuchElementException("Node Id does not exist in database: " + nodeId);
        }
    }

    private BridgeLinkNodeDTO mapBridgeLinkNodeToDTO(BridgeLinkNode bridgeLinkNode) {
        return bridgeLinkNode == null ? null : new BridgeLinkNodeDTO()
                .withBridgeLocalPort(bridgeLinkNode.getBridgeLocalPort())
                .withBridgeLocalPortUrl(bridgeLinkNode.getBridgeLocalPortUrl())
                .withBridgeLinkRemoteNodes(bridgeLinkNode.getBridgeLinkRemoteNodes().stream().map(n -> mapBridgeLinkRemoteNodeToDTO(n)).collect(Collectors.toList()))
                .withBridgeInfo(bridgeLinkNode.getBridgeInfo())
                .withBridgeLinkCreateTime(bridgeLinkNode.getBridgeLinkCreateTime())
                .withBridgeLinkLastPollTime(bridgeLinkNode.getBridgeLinkLastPollTime());
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
                .withLdpRemPort(lldpLinkNode.getLldpRemPort())
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
