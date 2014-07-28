package org.opennms.web.bridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

import static org.opennms.core.utils.InetAddressUtils.str;

@Transactional(readOnly=true)
public class BridgeElementFactory implements InitializingBean, BridgeElementFactoryInterface{

	Map<Integer,BridgeLinkNode> nodelinks = new HashMap<Integer,BridgeLinkNode>(); 

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

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    
    public static BridgeElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static BridgeElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(BridgeElementFactoryInterface.class);
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
		return nodelinks.values();
	}
	
	@Transactional 
	private void convertFromModel(int nodeid, BridgeBridgeLink link) {

		BridgeLinkNode linknode = new BridgeLinkNode();
		if (nodelinks.containsKey(link.getBridgePort())) {
				linknode = nodelinks.get(link.getBridgePort());
		} else {
			linknode.setBridgeLocalPort(getBridgePortString(link.getBridgePort(),link.getBridgePortIfIndex()));
			linknode.setBridgeLocalVlan(link.getVlan());
			linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeBridgeLinkCreateTime()));
			linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeBridgeLinkLastPollTime()));
			nodelinks.put(link.getBridgePort(), linknode);
		}
		
		BridgeLinkRemoteNode remlinknode = new BridgeLinkRemoteNode();
	
		remlinknode.setBridgeRemoteNode(link.getDesignatedNode().getLabel());
		remlinknode.setBridgeRemoteUrl(getNodeUrl(link.getDesignatedNode().getId()));
		
		remlinknode.setBridgeRemotePort(getPortString(m_snmpInterfaceDao.findByNodeIdAndIfIndex(link.getDesignatedNode().getId(), link.getDesignatedPortIfIndex())));
		remlinknode.setBridgeRemotePortUrl(getSnmpInterfaceUrl(link.getDesignatedNode().getId(), link.getDesignatedPortIfIndex()));
		
		remlinknode.setBridgeRemoteVlan(link.getDesignatedVlan());
		
		linknode.getBridgeLinkRemoteNodes().add(remlinknode);
	}
	
	@Transactional
	private void convertFromModel(int nodeid, BridgeMacLink link) {
		BridgeLinkNode linknode = new BridgeLinkNode();
		if (nodelinks.containsKey(link.getBridgePort())) {
				linknode = nodelinks.get(link.getBridgePort());
		} else {
			linknode.setBridgeLocalPort(getBridgePortString(link.getBridgePort(),link.getBridgePortIfIndex()));
			linknode.setBridgeLocalVlan(link.getVlan());
			linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeMacLinkCreateTime()));
			linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeMacLinkLastPollTime()));
			nodelinks.put(link.getBridgePort(), linknode);
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
			
	private OnmsSnmpInterface getFromPhysAddress(String physAddress) {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.eq("physAddr", physAddress);
        final List<OnmsSnmpInterface> nodes = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (nodes.size() == 1)
            return nodes.get(0);
        return null;
	}

	private String getSnmpInterfaceUrl(Integer nodeid,Integer ifindex) {
		if (ifindex != null && nodeid != null )
			return "element/snmpinterface.jsp?node="+nodeid+"&ifindex="+ifindex;
		return null;
	}

	private String getBridgePortString(Integer bridgePort, Integer ifindex) {
		if (ifindex != null)
			return "bridge port: "+ bridgePort + "(ifindex:"+ifindex+")";
		return "bridge port: "+ bridgePort;
	}

	private String getIpInterfaceUrl(OnmsIpInterface ip) {
		return "element/interface.jsp?node="+ip.getNode().getId()+"&intf="+str(ip.getIpAddress());
	}

	private String getPortString(OnmsSnmpInterface snmpiface) {
		return snmpiface.getIfName() + "(ifindex:"+ snmpiface.getIfIndex()+")";
	}
	
	private String getNodeUrl(Integer nodeid) {
			return "element/node.jsp?node="+nodeid;
	}
}
