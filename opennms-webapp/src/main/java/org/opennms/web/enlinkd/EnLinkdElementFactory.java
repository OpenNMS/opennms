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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
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
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.CdpElement;
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
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly=true)
public class EnLinkdElementFactory implements InitializingBean, EnLinkdElementFactoryInterface{

	Map<Integer,BridgeLinkNode> bridgelinks = new HashMap<Integer,BridgeLinkNode>(); 

	Map<Integer, NodeLinkBridge> nodelinks = new HashMap<Integer,NodeLinkBridge>();
	
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
	private BridgeMacLinkDao m_bridgeMacLinkDao;
	
	@Autowired
	private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
	
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

    public static EnLinkdElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static EnLinkdElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(EnLinkdElementFactoryInterface.class);
    }

    @Override
	public OspfElementNode getOspfElement(int nodeId) {
		return convertFromModel(m_ospfElementDao.findByNodeId(Integer.valueOf(nodeId)));
	}
	
	private OspfElementNode convertFromModel(OspfElement ospf) {
		if (ospf ==  null)
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
		for (OspfLink link: m_ospfLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			nodelinks.addAll(convertFromModel(nodeId,link));
		}
		return nodelinks;
	}
	
	@Transactional
	private List<OspfLinkNode> convertFromModel(int nodeid, OspfLink link) {
        List<OspfLinkNode> linkNodes = new ArrayList<OspfLinkNode>();

        List<OspfElement> ospfElements = m_ospfElementDao.findAllByRouterId(link.getOspfRemRouterId());

        if (ospfElements.size() > 0) {
            for (OspfElement ospfElement : ospfElements) {
                OspfLinkNode linknode = new OspfLinkNode();
                linknode.setOspfIpAddr(str(link.getOspfIpAddr()));
                linknode.setOspfAddressLessIndex(link.getOspfAddressLessIndex());
                linknode.setOspfIfIndex(link.getOspfIfIndex());

                linknode.setOspfRemRouterId(getRemRouterIdString(str(link.getOspfRemRouterId()), ospfElement.getNode().getLabel()));
                linknode.setOspfRemRouterUrl(getNodeUrl(ospfElement.getNode().getId()));

                linknode.setOspfRemIpAddr(str(link.getOspfRemIpAddr()));
                linknode.setOspfRemAddressLessIndex(link.getOspfRemAddressLessIndex());

                if (ospfElement != null && linknode.getOspfRemIpAddr() != null)
                    linknode.setOspfRemPortUrl(getIpInterfaceUrl(ospfElement.getNode().getId(), linknode.getOspfRemIpAddr()));

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

        return linkNodes;
    }

    @Override
    public CdpElementNode getCdpElement(int nodeId) {
        return convertFromModel(m_cdpElementDao.findByNodeId(Integer.valueOf(nodeId)));
    }

    private CdpElementNode convertFromModel(CdpElement cdp) {
        if (cdp ==  null)
                return null;
        
        CdpElementNode cdpNode = new CdpElementNode();
        cdpNode.setCdpGlobalRun(TruthValue.getTypeString(cdp.getCdpGlobalRun().getValue()));
        cdpNode.setCdpGlobalDeviceId(cdp.getCdpGlobalDeviceId());
        cdpNode.setCdpCreateTime(Util.formatDateToUIString(cdp.getCdpNodeCreateTime()));
        cdpNode.setCdpLastPollTime(Util.formatDateToUIString(cdp.getCdpNodeLastPollTime()));
        
        return cdpNode;
    }
    
    @Override
    public List<CdpLinkNode> getCdpLinks(int nodeId) {
        List<CdpLinkNode> nodelinks = new ArrayList<CdpLinkNode>(); 
        for (CdpLink link: m_cdpLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
                nodelinks.add(convertFromModel(nodeId,link));
        }
        return nodelinks;
    }

    @Transactional
    private CdpLinkNode convertFromModel(int nodeid, CdpLink link) {
        CdpLinkNode linknode = new CdpLinkNode();
        linknode.setCdpLocalPort(getPortString(link.getCdpCacheIfIndex(),link.getCdpInterfaceName()));
        if (link.getCdpCacheIfIndex() != null)
            linknode.setCdpLocalPortUrl(getSnmpInterfaceUrl(nodeid, link.getCdpCacheIfIndex()));

        linknode.setCdpCacheAddressType(CiscoNetworkProtocolType.getTypeString(link.getCdpCacheAddressType().getValue()));
        linknode.setCdpCacheAddress(link.getCdpCacheAddress());
        
        linknode.setCdpCacheDeviceId(link.getCdpCacheDeviceId());
        linknode.setCdpCacheDevicePlatform(link.getCdpCacheDevicePlatform());
        linknode.setCdpCacheVersion(link.getCdpCacheVersion());
        linknode.setCdpCacheDevicePort(link.getCdpCacheDevicePort());
        
        CdpElement cdpCacheElement = m_cdpElementDao.findByGlobalDeviceId(link.getCdpCacheDeviceId());
        if (cdpCacheElement != null) {
            linknode.setCdpCacheDeviceUrl(getNodeUrl(cdpCacheElement.getNode().getId()));
            OnmsSnmpInterface cdpcachesnmp = getFromCdpCacheDevicePort(cdpCacheElement.getNode().getId(), link.getCdpCacheDevicePort());
            if (cdpcachesnmp != null) {
                linknode.setCdpCacheDevicePort(getPortString(cdpcachesnmp.getIfIndex(), link.getCdpCacheDevicePort()));
                linknode.setCdpCacheDevicePortUrl(getSnmpInterfaceUrl(cdpCacheElement.getNode().getId(), cdpcachesnmp.getIfIndex()));
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
	
	private LldpElementNode convertFromModel(LldpElement lldp) {
		if (lldp ==  null)
			return null;
		
		LldpElementNode lldpNode = new LldpElementNode();
		lldpNode.setLldpChassisIdString(getChassisIdString(lldp.getLldpChassisId(), lldp.getLldpChassisIdSubType()));
		lldpNode.setLldpSysName(lldp.getLldpSysname());
		lldpNode.setLldpCreateTime(Util.formatDateToUIString(lldp.getLldpNodeCreateTime()));
		lldpNode.setLldpLastPollTime(Util.formatDateToUIString(lldp.getLldpNodeLastPollTime()));
		
		return lldpNode;
	}

	@Override
	public List<LldpLinkNode> getLldpLinks(int nodeId) {
		List<LldpLinkNode> nodelinks = new ArrayList<LldpLinkNode>(); 
		for (LldpLink link: m_lldpLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			nodelinks.add(convertFromModel(nodeId, link));
		}
		return nodelinks;
	}
	
	@Transactional
	private LldpLinkNode convertFromModel(int nodeid, LldpLink link) {

                LldpLinkNode linknode = new LldpLinkNode();
                linknode.setLldpPortString(getPortString(link.getLldpPortId(), link.getLldpPortIdSubType()));
                linknode.setLldpPortDescr(link.getLldpPortDescr());
                linknode.setLldpPortUrl(getSnmpInterfaceUrl(Integer.valueOf(nodeid), link.getLldpPortIfindex()));

                linknode.setLldpRemSysName(link.getLldpRemSysname());
                linknode.setLldpRemPortString(getPortString(link.getLldpRemPortId(), link.getLldpRemPortIdSubType()));
                linknode.setLldpRemPortDescr(link.getLldpRemPortDescr());

                linknode.setLldpCreateTime(Util.formatDateToUIString(link.getLldpLinkCreateTime()));
                linknode.setLldpLastPollTime(Util.formatDateToUIString(link.getLldpLinkLastPollTime()));
		
                OnmsNode remNode = null; 
                
                List<LldpElement> lldpremelements= m_lldpElementDao.findByChassisId(link.getLldpRemChassisId(),link.getLldpRemChassisIdSubType());
                
                if (lldpremelements.size() == 1) {
                    remNode = lldpremelements.get(0).getNode();
                } else if (lldpremelements.size() > 1) {
                    linknode.setLldpRemChassisIdString(getChassisIdString("Found " + lldpremelements.size() + " nodes for", link.getLldpRemChassisId(), link.getLldpRemChassisIdSubType()));
                    return linknode;
                } else {
                    final Criteria criteria = new Criteria(OnmsNode.class).addRestriction(new EqRestriction("sysName", link.getLldpRemSysname()));
                    List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);
                    if (nodes.size() == 1) 
                        remNode=nodes.get(0);
                }
                
                if (remNode != null) {
                    linknode.setLldpRemChassisIdString(getChassisIdString(remNode.getLabel(), link.getLldpRemChassisId(), link.getLldpRemChassisIdSubType()));
                    linknode.setLldpRemChassisIdUrl(getNodeUrl(remNode.getId()));
                    if (link.getLldpRemPortIdSubType() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
                        try {
                            Integer remIfIndex = Integer.getInteger(link.getLldpRemPortId());
                            linknode.setLldpRemPortUrl(getSnmpInterfaceUrl(Integer.valueOf(remNode.getId()), remIfIndex));
                        } catch (Exception e) {
                        }
                    }
                } else {
                    linknode.setLldpRemChassisIdString(getChassisIdString(link.getLldpRemChassisId(), link.getLldpRemChassisIdSubType()));
                }
		return linknode;
	}

	public IsisElementNode getIsisElement(int nodeId) {
		return convertFromModel(m_isisElementDao.findByNodeId(Integer.valueOf(nodeId)));
	}
	
	private IsisElementNode convertFromModel(IsIsElement isis) {
		if (isis ==  null)
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
		for (IsIsLink link: m_isisLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			nodelinks.add(convertFromModel(nodeId,link));
		}
		return nodelinks;
	}
	
	@Transactional
	private IsisLinkNode convertFromModel(int nodeid, IsIsLink link) {
		IsisLinkNode linknode = new IsisLinkNode();
		linknode.setIsisCircIfIndex(link.getIsisCircIfIndex());
		linknode.setIsisCircAdminState(IsisAdminState.getTypeString(link.getIsisCircAdminState().getValue()));
		
		IsIsElement isiselement= m_isisElementDao.findByIsIsSysId(link.getIsisISAdjNeighSysID());
		if (isiselement != null) {
			linknode.setIsisISAdjNeighSysID(getAdjSysIDString(link.getIsisISAdjNeighSysID(),isiselement.getNode().getLabel()));
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
			IsIsLink adjLink = m_isisLinkDao.get(isiselement.getNode().getId(),link.getIsisISAdjIndex(),link.getIsisCircIndex());
			if (adjLink != null) {
				remiface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(isiselement.getNode().getId(), adjLink.getIsisCircIfIndex());
			}			
		}
		if (remiface == null) {
			remiface = getFromPhysAddress(link.getIsisISAdjNeighSNPAAddress());
		}
		
		if (remiface != null) {
			linknode.setIsisISAdjNeighPort(getPortString(remiface.getIfIndex(),remiface.getIfName()));
			linknode.setIsisISAdjUrl(getSnmpInterfaceUrl(remiface.getNode().getId(), remiface.getIfIndex()));
		} else {
			linknode.setIsisISAdjNeighPort("(Isis IS Adj Index: "+link.getIsisISAdjIndex()+ ")");
		}

		linknode.setIsisLinkCreateTime(Util.formatDateToUIString(link.getIsisLinkCreateTime()));
		linknode.setIsisLinkLastPollTime(Util.formatDateToUIString(link.getIsisLinkLastPollTime()));
		
		return linknode;
	}
	
    @Override
	public List<BridgeElementNode> getBridgeElements(int nodeId) {
		List<BridgeElementNode> nodes = new ArrayList<BridgeElementNode>(); 
		for (BridgeElement bridge: m_bridgeElementDao.findByNodeId(Integer.valueOf(nodeId))) {
			nodes.add(convertFromModel(bridge));
		}
		return nodes;
	}
	
	private BridgeElementNode convertFromModel(BridgeElement bridge) {
		if (bridge ==  null)
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
		for (OnmsIpInterface ip: m_ipInterfaceDao.findByNodeId(nodeId)) {
			for (IpNetToMedia ipnetomedia: m_ipNetToMediaDao.findByNetAddress(ip.getIpAddress())) {
				for (BridgeMacLink maclink: m_bridgeMacLinkDao.findByMacAddress(ipnetomedia.getPhysAddress())) {
					convertFromModel(nodeId, maclink, getNodePortString(str(ipnetomedia.getNetAddress()), ipnetomedia.getPhysAddress()));
				}
			}
		}
		return nodelinks.values();
	}

	@Transactional
	private void convertFromModel(int nodeid, BridgeMacLink link, String port) {
		if (!nodelinks.containsKey(link.getId())) {
			final NodeLinkBridge linknode = new NodeLinkBridge();
			final BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
			final Integer nodeId = link.getNode() == null? null : link.getNode().getId();
			final Integer bridgePortIfIndex = link.getBridgePortIfIndex();

			remlinknode.setBridgeRemoteNode(link.getNode().getLabel());
			remlinknode.setBridgeRemoteUrl(getNodeUrl(nodeId));
			final OnmsSnmpInterface remiface = bridgePortIfIndex == null? null : m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeId, bridgePortIfIndex);
			if (remiface != null) {
				remlinknode.setBridgeRemotePort(getPortString(remiface));
			} else {
				remlinknode.setBridgeRemotePort(getPortString(bridgePortIfIndex,null));
			}
			remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(nodeId, bridgePortIfIndex));
			remlinknode.setBridgeRemoteVlan(link.getVlan());

			linknode.setBridgeLinkRemoteNode(remlinknode);
			linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeMacLinkCreateTime()));
			linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeMacLinkLastPollTime()));
			nodelinks.put(link.getId(), linknode);
		} 
			
		nodelinks.get(link.getId()).getNodeLocalPorts().add(port);
	}

	@Override
	public Collection<BridgeLinkNode> getBridgeLinks(int nodeId) {
		for (BridgeMacLink link: m_bridgeMacLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			convertFromModel(nodeId,link);
		}
		for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			convertFromModel(nodeId,link);
		}
		for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findByDesignatedNodeId(Integer.valueOf(nodeId))) {
			convertFromModel(nodeId,link.getReverseBridgeBridgeLink());
		}
		return bridgelinks.values();
	}
	
	@Transactional 
	private void convertFromModel(int nodeid, BridgeBridgeLink link) {

		BridgeLinkNode linknode = new BridgeLinkNode();
		if (bridgelinks.containsKey(link.getBridgePort())) {
				linknode = bridgelinks.get(link.getBridgePort());
		} else {
			linknode.setBridgeLocalPort(getBridgePortString(link.getBridgePort(),link.getBridgePortIfIndex()));
			linknode.setBridgeLocalVlan(link.getVlan());
			linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeBridgeLinkCreateTime()));
			linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeBridgeLinkLastPollTime()));
			bridgelinks.put(link.getBridgePort(), linknode);
		}
		
		BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
	
		remlinknode.setBridgeRemoteNode(link.getDesignatedNode().getLabel());
		remlinknode.setBridgeRemoteUrl(getNodeUrl(link.getDesignatedNode().getId()));

        OnmsSnmpInterface remiface = getSnmpInterface(link.getDesignatedNode(), link.getDesignatedPortIfIndex());
        remlinknode.setBridgeRemotePort(getPortString(remiface));
        remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(remiface));
		
		remlinknode.setBridgeRemoteVlan(link.getDesignatedVlan());
		linknode.getBridgeLinkRemoteNodes().add(remlinknode);
	}
	
	@Transactional
	private void convertFromModel(int nodeid, BridgeMacLink link) {
		BridgeLinkNode linknode = new BridgeLinkNode();
		if (bridgelinks.containsKey(link.getBridgePort())) {
				linknode = bridgelinks.get(link.getBridgePort());
		} else {
			linknode.setBridgeLocalPort(getBridgePortString(link.getBridgePort(),link.getBridgePortIfIndex()));
			linknode.setBridgeLocalVlan(link.getVlan());
			linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeMacLinkCreateTime()));
			linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeMacLinkLastPollTime()));
			bridgelinks.put(link.getBridgePort(), linknode);
		}
		
		List<IpNetToMedia> ipnettomedias = m_ipNetToMediaDao.findByPhysAddress(link.getMacAddress());
		if (ipnettomedias.isEmpty()) {
			BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
			OnmsSnmpInterface snmp = getFromPhysAddress(link.getMacAddress());
			if (snmp == null) {
				remlinknode.setBridgeRemoteNode(link.getMacAddress()+ " No node associated in db");
			} else {
				remlinknode.setBridgeRemoteNode(snmp.getNode().getLabel());
				remlinknode.setBridgeRemoteUrl(getNodeUrl(snmp.getNode().getId()));
				
				remlinknode.setBridgeRemotePort(getPortString(snmp));
				remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(snmp.getNode().getId(),snmp.getIfIndex()));
			}
			linknode.getBridgeLinkRemoteNodes().add(remlinknode);
		}
		for (IpNetToMedia ipnettomedia: ipnettomedias) {
			BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
			List<OnmsIpInterface> ips = m_ipInterfaceDao.findByIpAddress(ipnettomedia.getNetAddress().getHostAddress());
			if (ips.isEmpty() ) {
				remlinknode.setBridgeRemoteNode(str(ipnettomedia.getNetAddress())+"/"+link.getMacAddress()+ " No node associated in db");
			} else if ( ips.size() > 1) {
				remlinknode.setBridgeRemoteNode(str(ipnettomedia.getNetAddress())+"/"+link.getMacAddress()+ " duplicated ip multiple node associated in db");
			}
			for (OnmsIpInterface ip: ips) {
				remlinknode.setBridgeRemoteNode(ip.getNode().getLabel());
				remlinknode.setBridgeRemoteUrl(getNodeUrl(ip.getNode().getId()));
				
				remlinknode.setBridgeRemotePort(str(ipnettomedia.getNetAddress())+"/"+link.getMacAddress());
				remlinknode.setBridgeRemotePortUrl(getIpInterfaceUrl(ip));
			}
			linknode.getBridgeLinkRemoteNodes().add(remlinknode);
		}
		
	}
			
	private String getAdjSysIDString(String adjsysid, String label) {
		return adjsysid + "("+label+")";
	}
	
	private String getChassisIdString(String sysname, String chassisId, LldpChassisIdSubType chassisType) {
		return sysname+ ": " + LldpChassisIdSubType.getTypeString(chassisType.getValue())+ ": " + chassisId;
	}

	private String getChassisIdString(String chassisId, LldpChassisIdSubType chassisType) {
		return LldpChassisIdSubType.getTypeString(chassisType.getValue())+ ": " + chassisId;
	}

	private String getPortString(String portId,LldpPortIdSubType type) {
		return LldpPortIdSubType.getTypeString(type.getValue()) + ": " + portId;
	}

    private OnmsSnmpInterface getFromCdpCacheDevicePort(Integer nodeid,
            String cdpCacheDevicePort) {
        final CriteriaBuilder builder = new CriteriaBuilder(
                                                            OnmsSnmpInterface.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.sql("snmpifalias = '" + cdpCacheDevicePort + "' OR snmpifname = '" + cdpCacheDevicePort + "' OR snmpifdescr = '" +cdpCacheDevicePort+"'").eq("node.id", nodeid);
        final List<OnmsSnmpInterface> nodes = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (nodes.size() == 1)
            return nodes.get(0);
        return null;

    }

	private OnmsSnmpInterface getFromPhysAddress(String physAddress) {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.eq("physAddr", physAddress);
        final List<OnmsSnmpInterface> nodes = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (nodes.size() == 1)
            return nodes.get(0);
        return null;
	}

	private String getNodePortString(String ip, String physaddr) {
		if (ip != null && physaddr != null)
			return ip + "(" + physaddr+")";
		return null;
	}
	private String getBridgePortString(Integer bridgePort, Integer ifindex) {
		if (ifindex != null)
			return "bridge port: "+ bridgePort + "(ifindex:"+ifindex+")";
		return "bridge port: "+ bridgePort;
	}

    private OnmsSnmpInterface getSnmpInterface(OnmsNode node, Integer ifIndex) {
        if (node != null && node.getId() != null && ifIndex != null) {
            OnmsSnmpInterface snmpInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(node.getId(), ifIndex);
            return snmpInterface;
        }
        return null;
    }

    private String getPortString(OnmsSnmpInterface iface) {
        if (iface != null) {
            return getPortString(iface.getIfIndex(), iface.getIfName());
        }
        return getPortString((Integer) null, (String) null);
    }

	private String getPortString(Integer ifindex, String portName) {
	    if (ifindex != null && portName != null) {
            return portName + "(ifindex:" + ifindex + ")";
        }
	    if (portName != null ) {
            return portName;
        }
	    if (ifindex != null) {
            return "(ifindex:" + ifindex + ")";
        }
	    return "(no interface found in db)";
	}
	
	private String getRemRouterIdString(String ip, String label) {
		return ip + "("+label+")";
	}
	
	private String getNodeUrl(Integer nodeid) {
			return "element/node.jsp?node="+nodeid;
	}	

    private String getSnmpInterfaceUrl(OnmsSnmpInterface iface) {
        if (iface != null) {
            return getSnmpInterfaceUrl(iface.getNode().getId(), iface.getIfIndex());
        }
        return null;
    }

	private String getSnmpInterfaceUrl(Integer nodeid,Integer ifindex) {
		if (ifindex != null && nodeid != null )
			return "element/snmpinterface.jsp?node="+nodeid+"&ifindex="+ifindex;
		return null;
	}

	private String getIpInterfaceUrl(Integer nodeid,String ipaddress) {
			return "element/interface.jsp?node="+nodeid+"&intf="+ipaddress;
	}

	private String getIpInterfaceUrl(OnmsIpInterface ip) {
		return "element/interface.jsp?node="+ip.getNode().getId()+"&intf="+str(ip.getIpAddress());
	}

}
