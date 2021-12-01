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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpElement.CdpGlobalDeviceIdFormat;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfElement.Status;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly = true)
public class EnLinkdElementFactory implements InitializingBean,
        EnLinkdElementFactoryInterface {

    private final static Logger LOG = LoggerFactory.getLogger(EnLinkdElementFactory.class);

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
    private BridgeTopologyService m_bridgeTopologyService;

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

    private OspfElementNode convertFromModel(OspfElement ospf) {
        if (ospf == null) {
            return null;
        }

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
        var snmpInterfaces = new SnmpInterfaceCache();
        List<OspfLinkNode> nodelinks = new ArrayList<OspfLinkNode>();
        for (OspfLink link : m_ospfLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.add(convertFromModel(nodeId, link, snmpInterfaces));
        }
        return nodelinks;
    }

    public OspfLinkNode create(int nodeid, OspfLink link, SnmpInterfaceCache snmpInterfaceCache) {
        OspfLinkNode linknode = new OspfLinkNode();
        OnmsSnmpInterface snmpiface = null;
        String ipaddr = str(link.getOspfIpAddr());

        // set local info
        if (link.getOspfIfIndex() != null) {
            snmpiface = snmpInterfaceCache.get(nodeid, link.getOspfIfIndex());
        } else if (link.getOspfAddressLessIndex() > 0) {
            snmpiface = snmpInterfaceCache.get(nodeid, link.getOspfAddressLessIndex());
        }

        if (snmpiface != null) {
            if (link.getOspfAddressLessIndex() > 0) {
                linknode.setOspfLocalPort(getPortString(snmpiface,
                                                        "address less", null));
            } else {
                linknode.setOspfLocalPort(getPortString(snmpiface, "ip",
                                                        ipaddr));
            }
            linknode.setOspfLocalPortUrl(getSnmpInterfaceUrl(nodeid,
                                                             snmpiface.getIfIndex()));
        } else if (link.getOspfAddressLessIndex() > 0) {
            linknode.setOspfLocalPort(getPortString(link.getOspfAddressLessIndex(),
                                                    "address less", null));
        } else if (link.getOspfIfIndex() != null && ipaddr != null) {
            linknode.setOspfLocalPort(getPortString(link.getOspfIfIndex(),
                                                    "ip", ipaddr));
            linknode.setOspfLocalPortUrl(getIpInterfaceUrl(nodeid, ipaddr));
        } else if (ipaddr != null) {
            linknode.setOspfLocalPort(getIdString("ip", ipaddr));
            linknode.setOspfLocalPortUrl(getIpInterfaceUrl(nodeid, ipaddr));
        }

        if (link.getOspfIpMask() != null) {
            linknode.setOspfLinkInfo(getIdString("mask", str(link.getOspfIpMask())));
        } else {
            linknode.setOspfLinkInfo(getIdString("No mask",null));
        }

        linknode.setOspfLinkCreateTime(Util.formatDateToUIString(link.getOspfLinkCreateTime()));
        linknode.setOspfLinkLastPollTime(Util.formatDateToUIString(link.getOspfLinkLastPollTime()));

        return linknode;

    }

    public OspfLinkNode convertFromModel(int nodeid, OspfLink link, SnmpInterfaceCache snmpInterfaceCache) {
        OspfLinkNode linknode = create(nodeid, link, snmpInterfaceCache);

        Integer remNodeid = null;
        String remNodeLabel = null;

        // set rem info

        List<OspfElement> remOspfElements = m_ospfElementDao.findAllByRouterId(link.getOspfRemRouterId());
        if (remOspfElements.size() == 1) {
            OspfElement remOspfElement = remOspfElements.iterator().next();
            remNodeid = remOspfElement.getNode().getId();
            remNodeLabel = remOspfElement.getNode().getLabel();
        }
        // set rem Router id
        if (remNodeid != null) {
            linknode.setOspfRemRouterId(getHostString(remNodeLabel,
                                                      "router id",
                                                      str(link.getOspfRemRouterId())));
            linknode.setOspfRemRouterUrl(getNodeUrl(remNodeid));
        } else {
            linknode.setOspfRemRouterId(getIdString("router id",str(link.getOspfRemRouterId())));
        }

        String remipaddr = str(link.getOspfRemIpAddr());
        OnmsSnmpInterface remsnmpiface = null;

        if (remNodeid != null) {
            if (link.getOspfRemAddressLessIndex() > 0) {
                remsnmpiface = snmpInterfaceCache.get(remNodeid, link.getOspfAddressLessIndex());
            } else {
                OnmsIpInterface remipiface = m_ipInterfaceDao.findByNodeIdAndIpAddress(remNodeid,
                                                                                       remipaddr);
                if (remipiface != null) {
                    remsnmpiface = remipiface.getSnmpInterface();
                }
            }
        }

        if (remsnmpiface != null) {
            if (link.getOspfRemAddressLessIndex() > 0) {
                linknode.setOspfRemPort(getPortString(remsnmpiface,
                                                      "address less", null));
            } else {
                linknode.setOspfRemPort(getPortString(remsnmpiface, "ip",
                                                      remipaddr));
            }
            linknode.setOspfRemPortUrl(getSnmpInterfaceUrl(remNodeid,
                                                           remsnmpiface.getIfIndex()));
        } else if (link.getOspfAddressLessIndex() > 0) {
            linknode.setOspfRemPort(getPortString(link.getOspfRemAddressLessIndex(),
                                                  "address less", null));
        } else if (remipaddr != null) {
            linknode.setOspfRemPort(getIdString("ip",remipaddr));
            if (remNodeid != null) {
                linknode.setOspfRemPortUrl(getIpInterfaceUrl(remNodeid, remipaddr));
            }
        }
        return linknode;
    }

    @Override
    public CdpElementNode getCdpElement(int nodeId) {
        return convertFromModel(m_cdpElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    private CdpElementNode convertFromModel(CdpElement cdp) {
        if (cdp == null) {
            return null;
        }

        CdpElementNode cdpNode = new CdpElementNode();
        cdpNode.setCdpGlobalRun(TruthValue.getTypeString(cdp.getCdpGlobalRun().getValue()));
        cdpNode.setCdpGlobalDeviceId(cdp.getCdpGlobalDeviceId());

        if (cdp.getCdpGlobalDeviceIdFormat() != null) {
            cdpNode.setCdpGlobalDeviceIdFormat(CdpGlobalDeviceIdFormat.getTypeString(cdp.getCdpGlobalDeviceIdFormat().getValue()));
        } else {
            cdpNode.setCdpGlobalDeviceIdFormat("&nbsp");
        }
        
        cdpNode.setCdpCreateTime(Util.formatDateToUIString(cdp.getCdpNodeCreateTime()));
        cdpNode.setCdpLastPollTime(Util.formatDateToUIString(cdp.getCdpNodeLastPollTime()));

        return cdpNode;
    }

    @Override
    public List<CdpLinkNode> getCdpLinks(int nodeId) {
        var snmpInterfaces = new SnmpInterfaceCache();
        List<CdpLinkNode> nodelinks = new ArrayList<CdpLinkNode>();
        for (CdpLink link : m_cdpLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.add(convertFromModel(nodeId, link, snmpInterfaces));
        }
        Collections.sort(nodelinks);
        return nodelinks;
    }

    public CdpLinkNode create(int nodeid, CdpLink link, SnmpInterfaceCache snmpInterfaceCache) {
        CdpLinkNode linknode = new CdpLinkNode();
        linknode.setCdpLocalPort(getPortString(link.getCdpInterfaceName(),
                                               link.getCdpCacheIfIndex(),
                                               null,
                                               null));
        
        OnmsSnmpInterface snmpiface = snmpInterfaceCache.get(nodeid, link.getCdpCacheIfIndex());
        if (snmpiface != null) {
        Set<OnmsIpInterface> ipifaces = snmpiface.getIpInterfaces();
            if (ipifaces.isEmpty() || ipifaces.size() > 1) {
                linknode.setCdpLocalPort(getPortString(snmpiface,null,null));
            } else {
                linknode.setCdpLocalPort(getPortString(snmpiface,"ip",str(ipifaces.iterator().next().getIpAddress())));
            }
            linknode.setCdpLocalPortUrl(getSnmpInterfaceUrl(nodeid,
                                                            link.getCdpCacheIfIndex()));
        }
 
        linknode.setCdpCreateTime(Util.formatDateToUIString(link.getCdpLinkCreateTime()));
        linknode.setCdpLastPollTime(Util.formatDateToUIString(link.getCdpLinkLastPollTime()));
        linknode.setCdpCachePlatform(link.getCdpCacheDevicePlatform()+ " -> " + link.getCdpCacheVersion());
        
        return linknode;
    }        
        
    public CdpLinkNode convertFromModel(int nodeid, CdpLink link, SnmpInterfaceCache snmpInterfaceCache) {
        CdpLinkNode linknode = create(nodeid, link, snmpInterfaceCache);
        linknode.setCdpCacheDevice(link.getCdpCacheDeviceId());
        linknode.setCdpCacheDevicePort(getPortString(link.getCdpCacheDevicePort(), null,
                                                     CiscoNetworkProtocolType.getTypeString(link.getCdpCacheAddressType().getValue()),
                                 link.getCdpCacheAddress()));

        CdpElement cdpCacheElement = m_cdpElementDao.findByGlobalDeviceId(link.getCdpCacheDeviceId());
        if (cdpCacheElement != null) {
            linknode.setCdpCacheDevice(getHostString(cdpCacheElement.getNode().getLabel(), "Cisco Device Id", 
                                                     link.getCdpCacheDeviceId()));
            linknode.setCdpCacheDeviceUrl(getNodeUrl(cdpCacheElement.getNode().getId()));
            OnmsSnmpInterface cdpcachesnmp = getFromCdpCacheDevicePort(cdpCacheElement.getNode().getId(),
                                                                       link.getCdpCacheDevicePort());
            if (cdpcachesnmp != null) {
                linknode.setCdpCacheDevicePort(getPortString(cdpcachesnmp,         
                                                     CiscoNetworkProtocolType.getTypeString(link.getCdpCacheAddressType().getValue()),
                                                     link.getCdpCacheAddress()));
                linknode.setCdpCacheDevicePortUrl(getSnmpInterfaceUrl(cdpCacheElement.getNode().getId(),
                                                                      cdpcachesnmp.getIfIndex()));
            }
        }

        return linknode;
    }

    @Override
    public LldpElementNode getLldpElement(int nodeId) {
        return convertFromModel(m_lldpElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    private LldpElementNode convertFromModel(LldpElement lldp) {
 
        if (lldp == null) {
            return null;
        }

        LldpElementNode lldpNode = new LldpElementNode();
        lldpNode.setLldpChassisId(getIdString(
                   LldpChassisIdSubType.getTypeString(lldp.getLldpChassisIdSubType().getValue()),               
                   lldp.getLldpChassisId()));

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

    private LldpLinkNode create(int nodeid, LldpLink link) {
        LldpLinkNode linknode = new LldpLinkNode();
        linknode.setLldpLocalPort(getPortString(link.getLldpPortDescr(), 
                                                 link.getLldpPortIfindex(),
                                                 LldpPortIdSubType.getTypeString(link.getLldpPortIdSubType().getValue()),
                                                 link.getLldpPortId()
                                                 ));
        linknode.setLldpLocalPortUrl(getSnmpInterfaceUrl(Integer.valueOf(nodeid),
                                                    link.getLldpPortIfindex()));
        linknode.setLldpRemInfo(link.getLldpRemSysname());
        linknode.setLldpCreateTime(Util.formatDateToUIString(link.getLldpLinkCreateTime()));
        linknode.setLldpLastPollTime(Util.formatDateToUIString(link.getLldpLinkLastPollTime()));
        return linknode;
    }
    
    private LldpLinkNode convertFromModel(int nodeid, LldpLink link) {
        LldpLinkNode linknode = create(nodeid, link);

        linknode.setLldpRemChassisId(getIdString(
                                              LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue()),               
                                              link.getLldpRemChassisId()
                                          ));

        linknode.setLldpRemPort(getPortString(link.getLldpRemPortDescr(), 
                                              null,
                                              LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue()),
                                              link.getLldpRemPortId()
                                              ));


        OnmsNode remNode = null;

        List<LldpElement> lldpremelements = m_lldpElementDao.findByChassisId(link.getLldpRemChassisId(),
                                                                             link.getLldpRemChassisIdSubType());

        if (lldpremelements.size() == 1) {
            remNode = lldpremelements.get(0).getNode();
        } else {
            final Criteria criteria = new Criteria(OnmsNode.class).addRestriction(new EqRestriction(
                                                                                                    "sysName",
                                                                                                    link.getLldpRemSysname()));
            List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);
            if (nodes.size() == 1)
                remNode = nodes.get(0);
        }

        if (remNode != null) {
            linknode.setLldpRemChassisId(getHostString(remNode.getLabel(),
                                                       LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue()),
                                                       link.getLldpRemChassisId()
                                                                  ));
            linknode.setLldpRemChassisIdUrl(getNodeUrl(remNode.getId()));
            if (link.getLldpRemPortIdSubType() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
                try {
                    Integer remIfIndex = SystemProperties.getInteger(link.getLldpRemPortId());
                    linknode.setLldpRemPortUrl(getSnmpInterfaceUrl(Integer.valueOf(remNode.getId()),
                                                                   remIfIndex));
                } catch (Exception e) {
                }
            }
        }
        return linknode;
    }

    public IsisElementNode getIsisElement(int nodeId) {
        return convertFromModel(m_isisElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    private IsisElementNode convertFromModel(IsIsElement isis) {
        if (isis == null) {
            return null;
        }
        IsisElementNode isisNode = new IsisElementNode();
        isisNode.setIsisSysID(isis.getIsisSysID());
        isisNode.setIsisSysAdminState(IsIsElement.IsisAdminState.getTypeString(isis.getIsisSysAdminState().getValue()));
        isisNode.setIsisCreateTime(Util.formatDateToUIString(isis.getIsisNodeCreateTime()));
        isisNode.setIsisLastPollTime(Util.formatDateToUIString(isis.getIsisNodeLastPollTime()));

        return isisNode;
    }

    @Override
    public List<IsisLinkNode> getIsisLinks(int nodeId) {
        var snmpInterfaces = new SnmpInterfaceCache();
        List<IsisLinkNode> nodelinks = new ArrayList<IsisLinkNode>();
        for (IsIsLink link : m_isisLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
            nodelinks.add(convertFromModel(nodeId, link, snmpInterfaces));
        }
        Collections.sort(nodelinks);
        return nodelinks;
    }

    private IsisLinkNode convertFromModel(int nodeid, IsIsLink link, SnmpInterfaceCache snmpInterfaceCache) {
        IsisLinkNode linknode = new IsisLinkNode();
        linknode.setIsisCircIfIndex(link.getIsisCircIfIndex());
        linknode.setIsisCircAdminState(IsisAdminState.getTypeString(link.getIsisCircAdminState().getValue()));

        IsIsElement isiselement = m_isisElementDao.findByIsIsSysId(link.getIsisISAdjNeighSysID());
        if (isiselement != null) {
            linknode.setIsisISAdjNeighSysID(getHostString(isiselement.getNode().getLabel(), 
                                                          "ISSysID", 
                                                          link.getIsisISAdjNeighSysID()
                                                              ));
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
                remiface = snmpInterfaceCache.get(isiselement.getNode().getId(), adjLink.getIsisCircIfIndex());
            }
        }
        if (remiface == null) {
            remiface = getFromPhysAddress(link.getIsisISAdjNeighSNPAAddress());
        }

        if (remiface != null) {
            linknode.setIsisISAdjNeighPort(getPortString(remiface,null,null));
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
    
    private BridgeLinkNode convertFromModel(
            Integer nodeid,
            SharedSegment segment,
            BridgeElementCache bridgeElementCache,
            SnmpInterfaceCache snmpInterfaceCache,
            Map<String, List<IpNetToMedia>> physAddrToIpNetToMedias,
            Map<String, List<OnmsIpInterface>> ipAddressToIpInterfaces,
            Map<String, OnmsSnmpInterface> physAddrToSnmpInterface
    ) throws BridgeTopologyException {
        
        BridgeLinkNode linknode = new BridgeLinkNode();
        BridgePort bridgePort = segment.getBridgePort(nodeid);
        final OnmsSnmpInterface iface = snmpInterfaceCache.get(bridgePort.getNodeId(), bridgePort.getBridgePortIfIndex());
        if (iface != null) {
            linknode.setBridgeLocalPort(getPortString(iface,"bridgeport", bridgePort.getBridgePort().toString()));
            linknode.setBridgeLocalPortUrl(getSnmpInterfaceUrl(bridgePort.getNodeId(),
                                                               bridgePort.getBridgePortIfIndex()));
        } else {
            linknode.setBridgeLocalPort(getPortString("port",bridgePort.getBridgePortIfIndex(),"bridgeport", bridgePort.getBridgePort().toString()));
        }
        BridgeElement bridgeElement = bridgeElementCache.get(bridgePort.getNodeId(), bridgePort.getVlan());
        if (bridgeElement != null) {
            linknode.setBridgeInfo(bridgeElement.getVlanname());
        }
        return addBridgeRemotesNodes(nodeid, null, linknode, segment, bridgeElementCache, snmpInterfaceCache, physAddrToIpNetToMedias, ipAddressToIpInterfaces, physAddrToSnmpInterface);
    }

    private BridgeLinkNode convertFromModel(
            Integer nodeid,
            String mac,
            List<OnmsIpInterface> ipaddrs,
            SharedSegment segment,
            BridgeElementCache bridgeElementCache,
            SnmpInterfaceCache snmpInterfaceCache,
            Map<String, List<IpNetToMedia>> physAddrToIpNetToMedias,
            Map<String, List<OnmsIpInterface>> ipAddressToIpInterfaces,
            Map<String, OnmsSnmpInterface> physAddrToSnmpInterface
    ) {
        BridgeLinkNode linknode = new BridgeLinkNode();
        
        if (ipaddrs.size() == 0) {
            linknode.setBridgeLocalPort(getIdString("mac", mac));
        } else if (ipaddrs.size() == 1 ) {
            OnmsIpInterface ipiface =ipaddrs.iterator().next();
            if (ipiface != null) {
                OnmsSnmpInterface snmpiface = ipiface.getSnmpInterface();
                if (snmpiface !=  null) {
                    linknode.setBridgeLocalPort(getPortString(snmpiface,"mac",mac));
                    linknode.setBridgeLocalPortUrl(getSnmpInterfaceUrl(snmpiface.getNodeId(), snmpiface.getIfIndex()));
                } else {
                    linknode.setBridgeLocalPort(getPortString(str(ipiface.getIpAddress()),ipiface.getIfIndex(),"mac",mac));
                    linknode.setBridgeLocalPortUrl(getIpInterfaceUrl(ipiface.getNodeId(),str(ipiface.getIpAddress())));
                }
            }
        } else {
            linknode.setBridgeLocalPort(getPortString(getIpListAsStringFromIpInterface(ipaddrs), null, "mac", mac));
        }
        
        return addBridgeRemotesNodes(nodeid, mac, linknode, segment, bridgeElementCache, snmpInterfaceCache, physAddrToIpNetToMedias, ipAddressToIpInterfaces, physAddrToSnmpInterface);
    }

    private BridgeLinkNode addBridgeRemotesNodes(
            Integer nodeid,
            String mac,
            BridgeLinkNode linknode,
            SharedSegment segment,
            BridgeElementCache bridgeElementCache,
            SnmpInterfaceCache snmpInterfaceCache,
            Map<String, List<IpNetToMedia>> physAddrToIpNetToMedias,
            Map<String, List<OnmsIpInterface>> ipAddressToIpInterfaces,
            Map<String, OnmsSnmpInterface> physAddrToSnmpInterface
    ) {

        linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(segment.getCreateTime()));
        linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(segment.getLastPollTime()));

        for (BridgePort remport : segment.getBridgePortsOnSegment()) {
            if (nodeid.intValue() == remport.getNodeId().intValue()) {
                continue;
            }
            final BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
            final BridgeElement remBridgeElement = bridgeElementCache.get(remport.getNodeId(),remport.getVlan());
            if (remBridgeElement != null) {
                remlinknode.setBridgeRemote(getHostString(remBridgeElement.getNode().getLabel(), "bridge base address", remBridgeElement.getBaseBridgeAddress()));
            } else {
                remlinknode.setBridgeRemote(getIdString("nodeid", remport.getNodeId().toString()));
            }
            remlinknode.setBridgeRemoteUrl(getNodeUrl(remport.getNodeId()));

            final OnmsSnmpInterface remiface = snmpInterfaceCache.get(remport.getNodeId(), remport.getBridgePortIfIndex());

            if (remiface != null) {
                remlinknode.setBridgeRemotePort(getPortString(remiface,"bridgeport",remport.getBridgePort().toString()));
                remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(remport.getNodeId(),
                                                                       remport.getBridgePortIfIndex()));
            } else {
                remlinknode.setBridgeRemotePort(getPortString(null,remport.getBridgePortIfIndex(),
                                                              "bridgeport", remport.getBridgePort().toString()));
            }
            linknode.getBridgeLinkRemoteNodes().add(remlinknode);
        }
                
        Map<String, List<IpNetToMedia>> macsToIpNetTOMediaMap = new HashMap<String, List<IpNetToMedia>>();
        for (String sharedmac: segment.getMacsOnSegment()) {
            if (sharedmac.equals(mac)) {
                continue;
            }
            var ipNetToMedias = physAddrToIpNetToMedias.get(sharedmac);
            if (ipNetToMedias == null) {
                ipNetToMedias = Collections.emptyList();
            }
            macsToIpNetTOMediaMap.put(sharedmac, ipNetToMedias);
        }
       
        for (String sharedmac: macsToIpNetTOMediaMap.keySet()) {
            final BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
            if (macsToIpNetTOMediaMap.get(sharedmac).isEmpty()) {
                var snmp = physAddrToSnmpInterface.get(sharedmac);
                if (snmp == null) {
                    remlinknode.setBridgeRemote(getIdString("mac", sharedmac));
                } else {
                    remlinknode.setBridgeRemote(getHostString(snmp.getNode().getLabel(),"mac",sharedmac));
                    remlinknode.setBridgeRemoteUrl(getNodeUrl(snmp.getNode().getId()));

                    remlinknode.setBridgeRemotePort(getPortString(snmp,null,null));
                    remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(snmp.getNode().getId(),
                                                                           snmp.getIfIndex()));
                }
                linknode.getBridgeLinkRemoteNodes().add(remlinknode);
                continue;
            }

            List<OnmsIpInterface> remipaddrs = new ArrayList<OnmsIpInterface>();
            for (IpNetToMedia ipnettomedia : macsToIpNetTOMediaMap.get(sharedmac)) {
                var byIpAddress = ipAddressToIpInterfaces.get(ipnettomedia.getNetAddress().getHostAddress());
                if (byIpAddress == null) {
                    byIpAddress = Collections.emptyList();
                }
                remipaddrs.addAll(byIpAddress);
            }
            
            if (remipaddrs.size() == 0) { 
                remlinknode.setBridgeRemote(getIdString("mac", sharedmac));
                remlinknode.setBridgeRemotePort(getIpListAsStringFromIpNetToMedia(macsToIpNetTOMediaMap.get(sharedmac)));
                linknode.getBridgeLinkRemoteNodes().add(remlinknode);
                continue;
            }

            if (remipaddrs.size() == 1) {
                OnmsIpInterface remiface = remipaddrs.iterator().next();
                remlinknode.setBridgeRemote(getHostString(remiface.getNode().getLabel(), "mac", sharedmac));
                remlinknode.setBridgeRemoteUrl(getNodeUrl(remiface.getNodeId()));
                OnmsSnmpInterface remsnmpiface = remiface.getSnmpInterface();
                if (remsnmpiface != null) {
                    remlinknode.setBridgeRemotePort(getPortString(remsnmpiface, "ip", str(remiface.getIpAddress())));
                    remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(remiface.getNodeId(), remsnmpiface.getIfIndex()));
                } else {
                    remlinknode.setBridgeRemotePort(getPortString(str(remiface.getIpAddress()), remiface.getIfIndex(), null, null));
                    remlinknode.setBridgeRemotePortUrl(getIpInterfaceUrl(remiface.getNodeId(), str(remiface.getIpAddress())));
                }
                linknode.getBridgeLinkRemoteNodes().add(remlinknode);
                continue;
            }
            Set<String> labels = new HashSet<String>();
            for (OnmsIpInterface remiface: remipaddrs) {
                labels.add(remiface.getNode().getLabel());
            }
            if (labels.size() == 1) {
                remlinknode.setBridgeRemote(getHostString(labels.iterator().next(),"mac",sharedmac));
                remlinknode.setBridgeRemoteUrl(getNodeUrl(remipaddrs.iterator().next().getNodeId()));
            }
            remlinknode.setBridgeRemotePort(getIpListAsStringFromIpNetToMedia(macsToIpNetTOMediaMap.get(sharedmac)));
            linknode.getBridgeLinkRemoteNodes().add(remlinknode);
        }        
        return linknode;
    }

    @Override
    public Collection<BridgeLinkNode> getBridgeLinks(int nodeId) {
        List<BridgeLinkNode> bridgelinks = new ArrayList<BridgeLinkNode>();
        var bridgeElementCache = new BridgeElementCache();
        var snmpInterfaceCache = new SnmpInterfaceCache();
        // maps phys addresses into lists IpNetToMedia instances
        // -> contains entries for all phys addresses that occur in the further processing
        var physAddrToIpNetToMedias = m_ipNetToMediaDao.findByMacLinksOfNode(nodeId).stream().collect(Collectors.groupingBy(IpNetToMedia::getPhysAddress));
        // maps host addresses into lists of OnmsIpInterface instances
        // -> contains entries for all host addresses that occur in the further processing
        var ipAddressToIpInterfaces = m_ipInterfaceDao.findByMacLinksOfNode(nodeId).stream().collect(Collectors.groupingBy(itf -> itf.getIpAddress().getHostAddress()));
        // maps phys addresses into OnmsSnmpInterface instances
        // -> contains entries for all phys addresses that occur in the further processing
        var physAddrToSnmpInterface =
                uniqueMap(m_snmpInterfaceDao.findByMacLinksOfNode(nodeId), OnmsSnmpInterface::getPhysAddr, OnmsSnmpInterface::getId);

        for (SharedSegment segment: m_bridgeTopologyService.getSharedSegments(nodeId)) {
            try {
                bridgelinks.add(
                        convertFromModel(
                                nodeId,
                                segment,
                                bridgeElementCache,
                                snmpInterfaceCache,
                                physAddrToIpNetToMedias,
                                ipAddressToIpInterfaces,
                                physAddrToSnmpInterface
                        )
                );
            } catch (BridgeTopologyException e) {
                e.printStackTrace();
            }
        }
        if (bridgelinks.size() > 0 ) {
            Collections.sort(bridgelinks);
            LOG.debug("getBridgeLinks: node:[{}] is bridge found {} bridgelinks", nodeId, bridgelinks.size());
            return bridgelinks;
        }
        
        Map<String, List<OnmsIpInterface>> mactoIpNodeMap = new HashMap<String, List<OnmsIpInterface>>();
        m_ipInterfaceDao.findByNodeId(nodeId).stream().forEach( ip -> {
            LOG.debug("getBridgeLinks: node:[{}] is host found ip:{}", nodeId, str(ip.getIpAddress()));
            m_ipNetToMediaDao.findByNetAddress(ip.getIpAddress()).stream().forEach( ipnettomedia -> {
                if (!mactoIpNodeMap.containsKey(ipnettomedia.getPhysAddress())) {
                    mactoIpNodeMap.put(ipnettomedia.getPhysAddress(),
                                   new ArrayList<OnmsIpInterface>());
                }
                mactoIpNodeMap.get(ipnettomedia.getPhysAddress()).add(ip);
                LOG.debug("getBridgeLinks: node:[{}] is host found ip:{} mac:{}", nodeId, str(ip.getIpAddress()),ipnettomedia.getPhysAddress());
            });
        });

        for (String mac : mactoIpNodeMap.keySet()) {
            SharedSegment segment = m_bridgeTopologyService.getSharedSegment(mac);
            if (segment.isEmpty()) {
                continue;
            }
            bridgelinks.add(
                    convertFromModel(
                            nodeId,
                            mac,
                            mactoIpNodeMap.get(mac),
                            segment,
                            bridgeElementCache,
                            snmpInterfaceCache,
                            physAddrToIpNetToMedias,
                            ipAddressToIpInterfaces,
                            physAddrToSnmpInterface
                    )
            );
        }
        Collections.sort(bridgelinks);
        return bridgelinks;
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

    
    private String getPortString(OnmsSnmpInterface snmpiface, String addrtype, String addr) {
        StringBuffer sb = new StringBuffer("");
        if (snmpiface != null) {
            sb.append(snmpiface.getIfName());
            sb.append("(");
            sb.append(snmpiface.getIfAlias());
            sb.append(")");
            sb.append("(ifindex:");
            sb.append(snmpiface.getIfIndex());
            sb.append(")");
        }
       sb.append(getIdString(addrtype, addr));
       return sb.toString();
    } 

    private String getPortString(String ifname, Integer ifindex, String addrtype,String addr) {
        StringBuffer sb = new StringBuffer("");
        if (ifname != null) {
            sb.append(ifname);
        }
        if (ifindex != null) {
            sb.append("(ifindex:");
            sb.append(ifindex);
            sb.append(")");
        }
        sb.append(getIdString(addrtype, addr));
        return sb.toString();
    }

    private String getPortString(Integer ifindex, String addrtype,String addr) {
        StringBuffer sb = new StringBuffer("");
        if (ifindex != null) {
            sb.append("(ifindex:");
            sb.append(ifindex);
            sb.append(")");
        }
        sb.append(getIdString(addrtype, addr));
        return sb.toString();
    }

    private String getHostString(String label, String addrtype, String addr) {
        StringBuffer sb = new StringBuffer(label);
        if (addrtype != null && !label.equals(addr)) {
            sb.append(getIdString(addrtype, addr));
        }
        return sb.toString();
    }

    
    private String getIdString(String addrtype, String addr) {
        StringBuffer sb = new StringBuffer("");
        if (addrtype != null ) {
            sb.append("(");
            if ("ip".equals(addrtype)) {
                sb.append(addr);
            } else if (addr == null) {
                sb.append(addrtype);
            } else {
                sb.append(addrtype);
                sb.append(":");
                sb.append(addr);
            }
            sb.append(")");
        }
        return sb.toString();
    }
    
    private String getNodeUrl(Integer nodeid) {
        return "element/linkednode.jsp?node=" + nodeid;
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
        
    private String getIpListAsStringFromIpInterface(List<OnmsIpInterface> ipinterfaces) {
        Set<String> ipstrings = new HashSet<String>();
        ipinterfaces.stream().forEach(ipinterface -> ipstrings.add(str(ipinterface.getIpAddress())));
        return getIpList(ipstrings);
    }
    
    private String getIpListAsStringFromIpNetToMedia(List<IpNetToMedia> ipnettomedias) {
        Set<String> ipstrings = new HashSet<String>();
        ipnettomedias.stream().forEach( ipnettomedia -> ipstrings.add(str(ipnettomedia.getNetAddress())));
        return getIpList(ipstrings);
    }

    private String getIpList(Set<String> ipstrings) {
        StringBuffer sb = new StringBuffer("(");
        boolean start = true;
        for (String ipstring: ipstrings) {
            if (start) {
                start=false;
            } else {
                sb.append(":");
            }
            sb.append(ipstring);
        }
        sb.append(")");
        return sb.toString();
        
    }

    private class SnmpInterfaceCache {
        private Map<Integer, Map<Integer, OnmsSnmpInterface>> map = new HashMap<>();
        public OnmsSnmpInterface get(int nodeId, Integer ifIdx) {
            if (ifIdx == null) {
                return null;
            } else {
                return map.computeIfAbsent(nodeId, n ->
                        uniqueMap(m_snmpInterfaceDao.findByNodeId(n), OnmsSnmpInterface::getIfIndex, OnmsSnmpInterface::getId)
                ).get(ifIdx);
            }
        }
    }

    private class BridgeElementCache {
        private Map<Integer, Map<Integer, BridgeElement>> map = new HashMap<>();
        public BridgeElement get(int nodeId, Integer vlan) {
            if (vlan == null) {
                return null;
            } else {
                return map.computeIfAbsent(nodeId, n ->
                        uniqueMap(m_bridgeElementDao.findByNodeId(n), BridgeElement::getVlan, BridgeElement::getId)
                ).get(vlan);
            }
        }
    }

    /**
     * Creates a map that contains entries for those keys that have a unique value.
     */
    private static <K, V, I> Map<K, V> uniqueMap(List<V> list, Function<V, K> toKey, Function<V, I> toId) {
        return list
                .stream()
                .collect(
                        Collectors.toMap(
                                toKey,
                                Function.identity(),
                                (v1, v2) -> v1 != null && v2 != null && toId.apply(v1).equals(toId.apply(v2)) ? v1 : null
                        )
                );
    }

}
