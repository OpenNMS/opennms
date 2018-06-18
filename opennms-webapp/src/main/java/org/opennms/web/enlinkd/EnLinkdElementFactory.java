/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfElementDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpElement.CdpGlobalDeviceIdFormat;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly = true)
public class EnLinkdElementFactory implements InitializingBean,
        EnLinkdElementFactoryInterface {

    @Autowired
    private OspfElementDao m_ospfElementDao;

    @Autowired
    private OspfLinkDao m_ospfLinkDao;

    @Autowired
    private LldpElementDao m_lldpElementDao;

    @Autowired
    private LldpLinkDao m_lldpLinkDao;

    @Autowired
    private CdpElementDao m_cdpElementDao;

    @Autowired
    private CdpLinkDao m_cdpLinkDao;

    @Autowired
    private BridgeElementDao m_bridgeElementDao;

    @Autowired
    private BridgeTopologyDao m_bridgetopologyDao;

    @Autowired
    private IpNetToMediaDao m_ipNetToMediaDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private IsIsElementDao m_isisElementDao;

    @Autowired
    private IsIsLinkDao m_isisLinkDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    public static EnLinkdElementFactoryInterface getInstance(
            ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));
    }

    public static EnLinkdElementFactoryInterface getInstance(
            ApplicationContext appContext) {
        return appContext.getBean(EnLinkdElementFactoryInterface.class);
    }

    @Override
    public OspfElementNode getOspfElement(int nodeId) {
        return convertFromModel(m_ospfElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    @SuppressWarnings("deprecation")
    private OspfElementNode convertFromModel(OspfElement ospf) {
        if (ospf == null)
            return null;

        OspfElementNode ospfNode = new OspfElementNode();
        ospfNode.setOspfRouterId(str(ospf.getOspfRouterId()));
        ospfNode.setOspfVersionNumber(ospf.getOspfVersionNumber());
        ospfNode.setOspfAdminStat(Status.getTypeString(ospf.getOspfAdminStat().getValue()));
        ospfNode.setOspfCreateTime(Util.formatDateToUIString(ospf.getOspfNodeCreateTime()));
        ospfNode.setOspfLastPollTime(Util.formatDateToUIString(ospf.getOspfNodeLastPollTime()));

        return ospfNode;
    }

    @Override
    public List<OspfLinkNode> getOspfLinks(int nodeId) {
        List<OspfLinkNode> nodelinks = new ArrayList<OspfLinkNode>();
        for (OspfLink link : m_ospfLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.addAll(convertFromModel(nodeId, link));
        }
        return nodelinks;
    }

    @Transactional
    @SuppressWarnings("deprecation")
    private List<OspfLinkNode> convertFromModel(int nodeid, OspfLink link) {
        List<OspfLinkNode> linkNodes = new ArrayList<OspfLinkNode>();

        List<OspfElement> ospfElements = m_ospfElementDao.findAllByRouterId(link.getOspfRemRouterId());

        if (ospfElements.size() > 0) {
            for (OspfElement ospfElement : ospfElements) {
                OspfLinkNode linknode = new OspfLinkNode();
                linknode.setOspfIpAddr(str(link.getOspfIpAddr()));
                linknode.setOspfAddressLessIndex(link.getOspfAddressLessIndex());
                linknode.setOspfIfIndex(link.getOspfIfIndex());

                linknode.setOspfRemRouterId(getRemRouterIdString(str(link.getOspfRemRouterId()),
                                                                 ospfElement.getNode().getLabel()));
                linknode.setOspfRemRouterUrl(getNodeUrl(ospfElement.getNode().getId()));

                linknode.setOspfRemIpAddr(str(link.getOspfRemIpAddr()));
                linknode.setOspfRemAddressLessIndex(link.getOspfRemAddressLessIndex());

                if (ospfElement != null
                        && linknode.getOspfRemIpAddr() != null)
                    linknode.setOspfRemPortUrl(getIpInterfaceUrl(ospfElement.getNode().getId(),
                                                                 linknode.getOspfRemIpAddr()));

                linknode.setOspfLinkCreateTime(Util.formatDateToUIString(link.getOspfLinkCreateTime()));
                linknode.setOspfLinkLastPollTime(Util.formatDateToUIString(link.getOspfLinkLastPollTime()));

                linkNodes.add(linknode);
            }

        } else {
            OspfLinkNode linknode = new OspfLinkNode();
            linknode.setOspfIpAddr(str(link.getOspfIpAddr()));
            linknode.setOspfAddressLessIndex(link.getOspfAddressLessIndex());
            linknode.setOspfIfIndex(link.getOspfIfIndex());

            linknode.setOspfRemRouterId(str(link.getOspfRemRouterId()));

            linknode.setOspfRemIpAddr(str(link.getOspfRemIpAddr()));
            linknode.setOspfRemAddressLessIndex(link.getOspfRemAddressLessIndex());

            linknode.setOspfLinkCreateTime(Util.formatDateToUIString(link.getOspfLinkCreateTime()));
            linknode.setOspfLinkLastPollTime(Util.formatDateToUIString(link.getOspfLinkLastPollTime()));

            linkNodes.add(linknode);
        }
        Collections.sort(linkNodes);
        return linkNodes;
    }

    @Override
    public CdpElementNode getCdpElement(int nodeId) {
        return convertFromModel(m_cdpElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    @SuppressWarnings("deprecation")
    private CdpElementNode convertFromModel(CdpElement cdp) {
        if (cdp == null)
            return null;

        CdpElementNode cdpNode = new CdpElementNode();
        cdpNode.setCdpGlobalRun(TruthValue.getTypeString(cdp.getCdpGlobalRun().getValue()));
        cdpNode.setCdpGlobalDeviceId(cdp.getCdpGlobalDeviceId());
        if (cdp.getCdpGlobalDeviceIdFormat() != null)
            cdpNode.setCdpGlobalDeviceIdFormat(CdpGlobalDeviceIdFormat.getTypeString(cdp.getCdpGlobalDeviceIdFormat().getValue()));
        else
            cdpNode.setCdpGlobalDeviceIdFormat("&nbsp");
        cdpNode.setCdpCreateTime(Util.formatDateToUIString(cdp.getCdpNodeCreateTime()));
        cdpNode.setCdpLastPollTime(Util.formatDateToUIString(cdp.getCdpNodeLastPollTime()));

        return cdpNode;
    }

    @Override
    public List<CdpLinkNode> getCdpLinks(int nodeId) {
        List<CdpLinkNode> nodelinks = new ArrayList<CdpLinkNode>();
        for (CdpLink link : m_cdpLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.add(convertFromModel(nodeId, link));
        }
        Collections.sort(nodelinks);
        return nodelinks;
    }

    @Transactional
    @SuppressWarnings("deprecation")
    private CdpLinkNode convertFromModel(int nodeid, CdpLink link) {
        CdpLinkNode linknode = new CdpLinkNode();
        linknode.setCdpLocalPort(getPortString(link.getCdpCacheIfIndex(),
                                               link.getCdpInterfaceName(), null));
        if (link.getCdpCacheIfIndex() != null)
            linknode.setCdpLocalPortUrl(getSnmpInterfaceUrl(nodeid,
                                                            link.getCdpCacheIfIndex()));

        linknode.setCdpCacheAddressType(CiscoNetworkProtocolType.getTypeString(link.getCdpCacheAddressType().getValue()));
        linknode.setCdpCacheAddress(link.getCdpCacheAddress());

        linknode.setCdpCacheDeviceId(link.getCdpCacheDeviceId());
        linknode.setCdpCacheDevicePlatform(link.getCdpCacheDevicePlatform());
        linknode.setCdpCacheVersion(link.getCdpCacheVersion());
        linknode.setCdpCacheDevicePort(link.getCdpCacheDevicePort());

        CdpElement cdpCacheElement = m_cdpElementDao.findByGlobalDeviceId(link.getCdpCacheDeviceId());
        if (cdpCacheElement != null) {
            linknode.setCdpCacheDeviceUrl(getNodeUrl(cdpCacheElement.getNode().getId()));
            OnmsSnmpInterface cdpcachesnmp = getFromCdpCacheDevicePort(cdpCacheElement.getNode().getId(),
                                                                       link.getCdpCacheDevicePort());
            if (cdpcachesnmp != null) {
                linknode.setCdpCacheDevicePort(getPortString(cdpcachesnmp.getIfIndex(),
                                                             link.getCdpCacheDevicePort(),cdpcachesnmp.getIfAlias()));
                linknode.setCdpCacheDevicePortUrl(getSnmpInterfaceUrl(cdpCacheElement.getNode().getId(),
                                                                      cdpcachesnmp.getIfIndex()));
            }
        }

        linknode.setCdpCreateTime(Util.formatDateToUIString(link.getCdpLinkCreateTime()));
        linknode.setCdpLastPollTime(Util.formatDateToUIString(link.getCdpLinkLastPollTime()));
        return linknode;
    }

    @Override
    public LldpElementNode getLldpElement(int nodeId) {
        return convertFromModel(m_lldpElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    @SuppressWarnings("deprecation")
    private LldpElementNode convertFromModel(LldpElement lldp) {
        if (lldp == null)
            return null;

        LldpElementNode lldpNode = new LldpElementNode();
        lldpNode.setLldpChassisIdString(getChassisIdString(lldp.getLldpChassisId(),
                                                           lldp.getLldpChassisIdSubType()));
        lldpNode.setLldpSysName(lldp.getLldpSysname());
        lldpNode.setLldpCreateTime(Util.formatDateToUIString(lldp.getLldpNodeCreateTime()));
        lldpNode.setLldpLastPollTime(Util.formatDateToUIString(lldp.getLldpNodeLastPollTime()));

        return lldpNode;
    }

    @Override
    public List<LldpLinkNode> getLldpLinks(int nodeId) {
        List<LldpLinkNode> nodelinks = new ArrayList<LldpLinkNode>();
        for (LldpLink link : m_lldpLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.add(convertFromModel(nodeId, link));
        }
        Collections.sort(nodelinks);
        return nodelinks;
    }

    @Transactional
    @SuppressWarnings("deprecation")
    private LldpLinkNode convertFromModel(int nodeid, LldpLink link) {

        LldpLinkNode linknode = new LldpLinkNode();
        linknode.setLldpPortString(getPortString(link.getLldpPortId(),
                                                 link.getLldpPortIdSubType()));
        linknode.setLldpPortDescr(link.getLldpPortDescr());
        linknode.setLldpPortUrl(getSnmpInterfaceUrl(Integer.valueOf(nodeid),
                                                    link.getLldpPortIfindex()));

        linknode.setLldpRemSysName(link.getLldpRemSysname());
        linknode.setLldpRemPortString(getPortString(link.getLldpRemPortId(),
                                                    link.getLldpRemPortIdSubType()));
        linknode.setLldpRemPortDescr(link.getLldpRemPortDescr());

        linknode.setLldpCreateTime(Util.formatDateToUIString(link.getLldpLinkCreateTime()));
        linknode.setLldpLastPollTime(Util.formatDateToUIString(link.getLldpLinkLastPollTime()));

        OnmsNode remNode = null;

        List<LldpElement> lldpremelements = m_lldpElementDao.findByChassisId(link.getLldpRemChassisId(),
                                                                             link.getLldpRemChassisIdSubType());

        if (lldpremelements.size() == 1) {
            remNode = lldpremelements.get(0).getNode();
        } else if (lldpremelements.size() > 1) {
            linknode.setLldpRemChassisIdString(getChassisIdString("Found "
                                                                          + lldpremelements.size()
                                                                          + " nodes for",
                                                                  link.getLldpRemChassisId(),
                                                                  link.getLldpRemChassisIdSubType()));
            return linknode;
        } else {
            final Criteria criteria = new Criteria(OnmsNode.class).addRestriction(new EqRestriction(
                                                                                                    "sysName",
                                                                                                    link.getLldpRemSysname()));
            List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);
            if (nodes.size() == 1)
                remNode = nodes.get(0);
        }

        if (remNode != null) {
            linknode.setLldpRemChassisIdString(getChassisIdString(remNode.getLabel(),
                                                                  link.getLldpRemChassisId(),
                                                                  link.getLldpRemChassisIdSubType()));
            linknode.setLldpRemChassisIdUrl(getNodeUrl(remNode.getId()));
            if (link.getLldpRemPortIdSubType() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
                try {
                    Integer remIfIndex = Integer.getInteger(link.getLldpRemPortId());
                    linknode.setLldpRemPortUrl(getSnmpInterfaceUrl(Integer.valueOf(remNode.getId()),
                                                                   remIfIndex));
                } catch (Exception e) {
                }
            }
        } else {
            linknode.setLldpRemChassisIdString(getChassisIdString(link.getLldpRemChassisId(),
                                                                  link.getLldpRemChassisIdSubType()));
        }
        return linknode;
    }

    public IsisElementNode getIsisElement(int nodeId) {
        return convertFromModel(m_isisElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    @SuppressWarnings("deprecation")
    private IsisElementNode convertFromModel(IsIsElement isis) {
        if (isis == null)
            return null;

        IsisElementNode isisNode = new IsisElementNode();
        isisNode.setIsisSysID(isis.getIsisSysID());
        isisNode.setIsisSysAdminState(IsIsElement.IsisAdminState.getTypeString(isis.getIsisSysAdminState().getValue()));
        isisNode.setIsisCreateTime(Util.formatDateToUIString(isis.getIsisNodeCreateTime()));
        isisNode.setIsisLastPollTime(Util.formatDateToUIString(isis.getIsisNodeLastPollTime()));

        return isisNode;
    }

    @Override
    public List<IsisLinkNode> getIsisLinks(int nodeId) {
        List<IsisLinkNode> nodelinks = new ArrayList<IsisLinkNode>();
        for (IsIsLink link : m_isisLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.add(convertFromModel(nodeId, link));
        }
        Collections.sort(nodelinks);
        return nodelinks;
    }

    @Transactional
    @SuppressWarnings("deprecation")
    private IsisLinkNode convertFromModel(int nodeid, IsIsLink link) {
        IsisLinkNode linknode = new IsisLinkNode();
        linknode.setIsisCircIfIndex(link.getIsisCircIfIndex());
        linknode.setIsisCircAdminState(IsisAdminState.getTypeString(link.getIsisCircAdminState().getValue()));

        IsIsElement isiselement = m_isisElementDao.findByIsIsSysId(link.getIsisISAdjNeighSysID());
        if (isiselement != null) {
            linknode.setIsisISAdjNeighSysID(getAdjSysIDString(link.getIsisISAdjNeighSysID(),
                                                              isiselement.getNode().getLabel()));
            linknode.setIsisISAdjUrl(getNodeUrl(isiselement.getNode().getId()));
        } else {
            linknode.setIsisISAdjNeighSysID(link.getIsisISAdjNeighSysID());
        }
        linknode.setIsisISAdjNeighSysType(IsisISAdjNeighSysType.getTypeString(link.getIsisISAdjNeighSysType().getValue()));

        linknode.setIsisISAdjNeighSNPAAddress(link.getIsisISAdjNeighSNPAAddress());
        linknode.setIsisISAdjState(IsisISAdjState.get(link.getIsisISAdjState().getValue()).toString());
        linknode.setIsisISAdjNbrExtendedCircID(link.getIsisISAdjNbrExtendedCircID());

        OnmsSnmpInterface remiface = null;
        if (isiselement != null) {
            IsIsLink adjLink = m_isisLinkDao.get(isiselement.getNode().getId(),
                                                 link.getIsisISAdjIndex(),
                                                 link.getIsisCircIndex());
            if (adjLink != null) {
                remiface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(isiselement.getNode().getId(),
                                                                     adjLink.getIsisCircIfIndex());
            }
        }
        if (remiface == null) {
            remiface = getFromPhysAddress(link.getIsisISAdjNeighSNPAAddress());
        }

        if (remiface != null) {
            linknode.setIsisISAdjNeighPort(getPortString(remiface.getIfIndex(),
                                                         remiface.getIfName(), remiface.getIfAlias()));
            linknode.setIsisISAdjUrl(getSnmpInterfaceUrl(remiface.getNode().getId(),
                                                         remiface.getIfIndex()));
        } else {
            linknode.setIsisISAdjNeighPort("(Isis IS Adj Index: "
                    + link.getIsisISAdjIndex() + ")");
        }

        linknode.setIsisLinkCreateTime(Util.formatDateToUIString(link.getIsisLinkCreateTime()));
        linknode.setIsisLinkLastPollTime(Util.formatDateToUIString(link.getIsisLinkLastPollTime()));

        return linknode;
    }

    @Override
    public List<BridgeElementNode> getBridgeElements(int nodeId) {
        List<BridgeElementNode> nodes = new ArrayList<BridgeElementNode>();
        for (BridgeElement bridge : m_bridgeElementDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodes.add(convertFromModel(bridge));
        }
        return nodes;
    }

    @SuppressWarnings("deprecation")
    private BridgeElementNode convertFromModel(BridgeElement bridge) {
        if (bridge == null)
            return null;

        BridgeElementNode bridgeNode = new BridgeElementNode();

        bridgeNode.setBaseBridgeAddress(bridge.getBaseBridgeAddress());
        bridgeNode.setBaseNumPorts(bridge.getBaseNumPorts());
        bridgeNode.setBaseType(BridgeDot1dBaseType.getTypeString(bridge.getBaseType().getValue()));

        bridgeNode.setVlan(bridge.getVlan());
        bridgeNode.setVlanname(bridge.getVlanname());

        if (bridge.getStpProtocolSpecification() != null)
            bridgeNode.setStpProtocolSpecification(BridgeDot1dStpProtocolSpecification.getTypeString(bridge.getStpProtocolSpecification().getValue()));
        bridgeNode.setStpPriority(bridge.getStpPriority());
        bridgeNode.setStpDesignatedRoot(bridge.getStpDesignatedRoot());
        bridgeNode.setStpRootCost(bridge.getStpRootCost());
        bridgeNode.setStpRootPort(bridge.getStpRootPort());

        bridgeNode.setBridgeNodeCreateTime(Util.formatDateToUIString(bridge.getBridgeNodeCreateTime()));
        bridgeNode.setBridgeNodeLastPollTime(Util.formatDateToUIString(bridge.getBridgeNodeLastPollTime()));

        return bridgeNode;
    }

    @Override
    public Collection<NodeLinkBridge> getNodeLinks(int nodeId) {
        Map<String, Set<InetAddress>> mactoIpMap = new HashMap<String, Set<InetAddress>>();
        for (OnmsIpInterface ip : m_ipInterfaceDao.findByNodeId(nodeId)) {
            for (IpNetToMedia ipnetomedia : m_ipNetToMediaDao.findByNetAddress(ip.getIpAddress())) {
                if (!mactoIpMap.containsKey(ipnetomedia.getPhysAddress()))
                    mactoIpMap.put(ipnetomedia.getPhysAddress(),
                                   new HashSet<InetAddress>());
                mactoIpMap.get(ipnetomedia.getPhysAddress()).add(ip.getIpAddress());
            }
        }
        List<NodeLinkBridge> nodelinks = new ArrayList<NodeLinkBridge>();
        for (String mac : mactoIpMap.keySet()) {
            SharedSegment segment = m_bridgetopologyDao.getHostSharedSegment(mac);
            if (segment.isEmpty())
                continue;
            if (!segment.containsMac(mac))
                continue;
            nodelinks.add(convertFromModel(mac,segment,
                                           getNodePortString(mactoIpMap.get(mac),
                                                             mac)));
        }
        Collections.sort(nodelinks);
        return nodelinks;
    }

    @Transactional
    private NodeLinkBridge convertFromModel(String mac,
            SharedSegment segment, String port) {
        final NodeLinkBridge linknode = new NodeLinkBridge();
        linknode.setNodeLocalPort(port);
        
        for (BridgePort link : segment.getBridgePortsOnSegment()) {
            final BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
            final Integer rempnodeId = link.getNodeId();
            final Integer rembridgePortIfIndex = link.getBridgePortIfIndex();
            remlinknode.setBridgeRemoteNode(m_nodeDao.get(rempnodeId).getLabel());
            remlinknode.setBridgeRemoteUrl(getNodeUrl(rempnodeId));

            final OnmsSnmpInterface remiface = rembridgePortIfIndex == null
                                                                        ? null
                                                                        : m_snmpInterfaceDao.findByNodeIdAndIfIndex(rempnodeId,
                                                                                                                    rembridgePortIfIndex);
            if (remiface != null) {
                remlinknode.setBridgeRemotePort(getPortString(rembridgePortIfIndex,
                                                              remiface.getIfName(),
                                                              remiface.getIfAlias()));
            } else {
                remlinknode.setBridgeRemotePort(getPortString(rembridgePortIfIndex,
                                                              null, null));
            }
            remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(rempnodeId,
                                                                   rembridgePortIfIndex));
            remlinknode.setBridgeRemoteVlan(link.getVlan());
            linknode.getBridgeLinkRemoteNodes().add(remlinknode);
        }
                
        Map<String, List<IpNetToMedia>> sharedmacs = new HashMap<String, List<IpNetToMedia>>();
        for (String shredmac: segment.getMacsOnSegment()) {
            if (shredmac.equals(mac))
                continue;
            sharedmacs.put(shredmac, new ArrayList<IpNetToMedia>());
            sharedmacs.get(shredmac).addAll(m_ipNetToMediaDao.findByPhysAddress(shredmac));
        }
       
        Map<String, List<OnmsIpInterface>> sharedhosts = new HashMap<String, List<OnmsIpInterface>>();
        for (String shredmac: sharedmacs.keySet()) {
            if (sharedmacs.get(shredmac).isEmpty()) {
                BridgeLinkSharedHost remlinknode = new BridgeLinkSharedHost();
                OnmsSnmpInterface snmp = getFromPhysAddress(shredmac);
                if (snmp == null) {
                    remlinknode.setSharedHost(shredmac
                            + " No ip address found");
                } else {
                    remlinknode.setSharedHost(snmp.getNode().getLabel());
                    remlinknode.setSharedHostUrl(getNodeUrl(snmp.getNode().getId()));

                    remlinknode.setSharedHostPort(getPortString(snmp.getIfIndex(),snmp.getIfName(),snmp.getIfAlias()));
                    remlinknode.setSharedHostPortUrl(getSnmpInterfaceUrl(snmp.getNode().getId(),
                                                                           snmp.getIfIndex()));
                }
                linknode.getBridgeLinkSharedHost().add(remlinknode);
                continue;
            }
            sharedhosts.put(shredmac, new ArrayList<OnmsIpInterface>());
            for (IpNetToMedia ipnettomedia : sharedmacs.get(shredmac))
                sharedhosts.get(shredmac).addAll(m_ipInterfaceDao.findByIpAddress(ipnettomedia.getNetAddress().getHostAddress()));
        }

        for (String shredmac: sharedhosts.keySet()) {
            BridgeLinkSharedHost remlinknode = new BridgeLinkSharedHost();
            Set<InetAddress> ips = new HashSet<InetAddress>();
            if (sharedhosts.get(shredmac).isEmpty()) {
                for (IpNetToMedia ipnettomedia: sharedmacs.get(shredmac)) {
                    ips.add(ipnettomedia.getNetAddress());
                }
                remlinknode.setSharedHost(getNodePortString(ips, shredmac)+ " No node found");
                linknode.getBridgeLinkSharedHost().add(remlinknode);
                continue;
            }
            OnmsIpInterface first = null;
            boolean multiplenodeids = false;
            for (OnmsIpInterface ip: sharedhosts.get(shredmac)) {
                if (first == null )
                    first = ip;
                if (first.getNode().getId().intValue() != ip.getNode().getId().intValue() )
                    multiplenodeids = true;
                ips.add(ip.getIpAddress());
            }
            if (multiplenodeids) {
                remlinknode.setSharedHost(getNodePortString(ips, shredmac)  + " duplicated ip multiple node associated in db");
            } else {
                remlinknode.setSharedHost(first.getNode().getLabel());
                remlinknode.setSharedHostUrl(getNodeUrl(first.getNode().getId()));
            }
            remlinknode.setSharedHostPort(getNodePortString(ips, shredmac));
            if (ips.size() == 1) {
                remlinknode.setSharedHostPortUrl(getIpInterfaceUrl(first));
            } 
            linknode.getBridgeLinkSharedHost().add(remlinknode);
        }        
        return linknode;
    }

    @Override
    public Collection<BridgeLinkNode> getBridgeLinks(int nodeId) {
        List<BridgeLinkNode> bridgelinks = new ArrayList<BridgeLinkNode>();
        for (SharedSegment segment: m_bridgetopologyDao.getBridgeSharedSegments(nodeId)) {
            bridgelinks.add(convertFromModel(nodeId, segment));
        }
        Collections.sort(bridgelinks);
        return bridgelinks;
    }

    @Transactional
    private BridgeLinkNode convertFromModel(int nodeid, SharedSegment segment) {
        final BridgeLinkNode linknode = new BridgeLinkNode();
        for (BridgePort link : segment.getBridgePortsOnSegment()) {
            final Integer rempnodeId = link.getNodeId();
            final Integer rembridgePortIfIndex = link.getBridgePortIfIndex();
            final OnmsSnmpInterface remiface = rembridgePortIfIndex == null
                    ? null
                    : m_snmpInterfaceDao.findByNodeIdAndIfIndex(rempnodeId,
                                                                rembridgePortIfIndex);
            if (link.getNodeId().intValue() == nodeid) {
                if (remiface != null) {
                    linknode.setNodeLocalPort(getPortString(rembridgePortIfIndex,
                                                            remiface.getIfName(),
                                                            remiface.getIfAlias()));
                } else {
                    linknode.setNodeLocalPort(getPortString(rembridgePortIfIndex,
                                                                  null, null));
                }
                linknode.setBridgeLocalVlan(link.getVlan());
                continue;
            }
            final BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
            remlinknode.setBridgeRemoteNode(m_nodeDao.get(rempnodeId).getLabel());
            remlinknode.setBridgeRemoteUrl(getNodeUrl(rempnodeId));

            if (remiface != null) {
                remlinknode.setBridgeRemotePort(getPortString(rembridgePortIfIndex,
                                                              remiface.getIfName(),
                                                              remiface.getIfAlias()));
            } else {
                remlinknode.setBridgeRemotePort(getPortString(rembridgePortIfIndex,
                                                              null, null));
            }
            remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(rempnodeId,
                                                                   rembridgePortIfIndex));
            remlinknode.setBridgeRemoteVlan(link.getVlan());
            linknode.getBridgeLinkRemoteNodes().add(remlinknode);
        }

        Map<String, List<IpNetToMedia>> sharedmacs = new HashMap<String, List<IpNetToMedia>>();
        for (String shredmac: segment.getMacsOnSegment()) {
            sharedmacs.put(shredmac, new ArrayList<IpNetToMedia>());
            sharedmacs.get(shredmac).addAll(m_ipNetToMediaDao.findByPhysAddress(shredmac));
        }
       
        Map<String, List<OnmsIpInterface>> sharedhosts = new HashMap<String, List<OnmsIpInterface>>();
        for (String shredmac: sharedmacs.keySet()) {
            if (sharedmacs.get(shredmac).isEmpty()) {
                BridgeLinkSharedHost remlinknode = new BridgeLinkSharedHost();
                OnmsSnmpInterface snmp = getFromPhysAddress(shredmac);
                if (snmp == null) {
                    remlinknode.setSharedHost(shredmac
                            + " No ip address found");
                } else {
                    remlinknode.setSharedHost(snmp.getNode().getLabel());
                    remlinknode.setSharedHostUrl(getNodeUrl(snmp.getNode().getId()));

                    remlinknode.setSharedHostPort(getPortString(snmp.getIfIndex(),snmp.getIfName(),snmp.getIfAlias()));
                    remlinknode.setSharedHostPortUrl(getSnmpInterfaceUrl(snmp.getNode().getId(),
                                                                           snmp.getIfIndex()));
                }
                linknode.getBridgeLinkSharedHost().add(remlinknode);
                continue;
            }
            sharedhosts.put(shredmac, new ArrayList<OnmsIpInterface>());
            for (IpNetToMedia ipnettomedia : sharedmacs.get(shredmac))
                sharedhosts.get(shredmac).addAll(m_ipInterfaceDao.findByIpAddress(ipnettomedia.getNetAddress().getHostAddress()));
        }

        for (String shredmac: sharedhosts.keySet()) {
            BridgeLinkSharedHost remlinknode = new BridgeLinkSharedHost();
            Set<InetAddress> ips = new HashSet<InetAddress>();
            if (sharedhosts.get(shredmac).isEmpty()) {
                for (IpNetToMedia ipnettomedia: sharedmacs.get(shredmac)) {
                    ips.add(ipnettomedia.getNetAddress());
                }
                remlinknode.setSharedHost(getNodePortString(ips, shredmac)+ " No node found");
                linknode.getBridgeLinkSharedHost().add(remlinknode);
                continue;
            }
            OnmsIpInterface first = null;
            boolean multiplenodeids = false;
            for (OnmsIpInterface ip: sharedhosts.get(shredmac)) {
                if (first == null )
                    first = ip;
                if (first.getNode().getId().intValue() != ip.getNode().getId().intValue() )
                    multiplenodeids = true;
                ips.add(ip.getIpAddress());
            }
            if (multiplenodeids) {
                remlinknode.setSharedHost(getNodePortString(ips, shredmac)  + " duplicated ip multiple node associated in db");
            } else {
                remlinknode.setSharedHost(first.getNode().getLabel());
                remlinknode.setSharedHostUrl(getNodeUrl(first.getNode().getId()));
            }
            remlinknode.setSharedHostPort(getNodePortString(ips, shredmac));
            if (ips.size() == 1) {
                remlinknode.setSharedHostPortUrl(getIpInterfaceUrl(first));
            } 
            linknode.getBridgeLinkSharedHost().add(remlinknode);
        }        

        return linknode;

    }


    private String getAdjSysIDString(String adjsysid, String label) {
        return adjsysid + "(" + label + ")";
    }

    private String getChassisIdString(String sysname, String chassisId,
            LldpChassisIdSubType chassisType) {
        return sysname + ": "
                + LldpChassisIdSubType.getTypeString(chassisType.getValue())
                + ": " + chassisId;
    }

    private String getChassisIdString(String chassisId,
            LldpChassisIdSubType chassisType) {
        return LldpChassisIdSubType.getTypeString(chassisType.getValue())
                + ": " + chassisId;
    }

    private String getPortString(String portId, LldpPortIdSubType type) {
        return LldpPortIdSubType.getTypeString(type.getValue()) + ": "
                + portId;
    }

    private OnmsSnmpInterface getFromCdpCacheDevicePort(Integer nodeid,
            String cdpCacheDevicePort) {
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsSnmpInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.sql(
            "snmpifalias = ? OR snmpifname = ? OR snmpifdescr = ?", 
            new Object[] { cdpCacheDevicePort, cdpCacheDevicePort, cdpCacheDevicePort },
            new Type[] { Type.STRING, Type.STRING, Type.STRING }
        ).eq("node.id", nodeid);
        final List<OnmsSnmpInterface> nodes = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (nodes.size() == 1)
            return nodes.get(0);
        return null;

    }

    private OnmsSnmpInterface getFromPhysAddress(String physAddress) {
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsSnmpInterface.class);
        builder.eq("physAddr", physAddress);
        final List<OnmsSnmpInterface> nodes = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (nodes.size() == 1)
            return nodes.get(0);
        return null;
    }

    private String getNodePortString(Set<InetAddress> ips, String physaddr) {
        String port = "";
        if (ips.size() > 1)
            port += "multiple ip addresses";
        if (ips.size() == 1)
            port += str(ips.iterator().next());
        if (physaddr != null)
            port += "(" + physaddr + ")";
        return port;
    }

    private String getPortString(Integer ifindex, String ifName, String ifAlias) {
        if (ifindex == null && ifName == null && ifAlias == null)
            return null;
        String port = "";
        if (ifName != null) 
            port += ifName;
        if (ifindex != null )
            port += "(ifindex:" + ifindex + ")";
        if (ifAlias != null) {
            port += "(ifalias: " + ifAlias + ")";
        }
        return port;
    }

    private String getRemRouterIdString(String ip, String label) {
        return ip + "(" + label + ")";
    }

    private String getNodeUrl(Integer nodeid) {
        return "element/node.jsp?node=" + nodeid;
    }

    private String getSnmpInterfaceUrl(Integer nodeid, Integer ifindex) {
        if (ifindex != null && nodeid != null)
            return "element/snmpinterface.jsp?node=" + nodeid + "&ifindex="
                    + ifindex;
        return null;
    }

    private String getIpInterfaceUrl(Integer nodeid, String ipaddress) {
        return "element/interface.jsp?node=" + nodeid + "&intf=" + ipaddress;
    }

    private String getIpInterfaceUrl(OnmsIpInterface ip) {
        return "element/interface.jsp?node=" + ip.getNode().getId()
                + "&intf=" + str(ip.getIpAddress());
    }

}
