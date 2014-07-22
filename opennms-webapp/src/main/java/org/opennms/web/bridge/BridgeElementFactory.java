package org.opennms.web.bridge;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeElementDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly=true)
public class BridgeElementFactory implements InitializingBean, BridgeElementFactoryInterface{

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
	public List<BridgeLinkNode> getBridgeLinks(int nodeId) {
		List<BridgeLinkNode> nodelinks = new ArrayList<BridgeLinkNode>(); 
		for (BridgeMacLink link: m_bridgeMacLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			nodelinks.add(convertFromModel(nodeId,link));
		}
		for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findByNodeId(Integer.valueOf(nodeId))) {
			nodelinks.add(convertFromModel(nodeId,link));
		}
		for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findByDesignatedNodeId(Integer.valueOf(nodeId))) {
			nodelinks.add(convertFromModelReverse(nodeId,link));
		}
		return nodelinks;
	}
	
	@Transactional 
	private BridgeLinkNode convertFromModelReverse(int nodeid, BridgeBridgeLink link) {
		BridgeLinkNode linknode = new BridgeLinkNode();
		
		linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeBridgeLinkCreateTime()));
		linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeBridgeLinkLastPollTime()));
		return linknode;
	}
	
	@Transactional 
	private BridgeLinkNode convertFromModel(int nodeid, BridgeBridgeLink link) {
		BridgeLinkNode linknode = new BridgeLinkNode();
		
		linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeBridgeLinkCreateTime()));
		linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeBridgeLinkLastPollTime()));
		return linknode;
	}
	
	@Transactional
	private BridgeLinkNode convertFromModel(int nodeid, BridgeMacLink link) {
		BridgeLinkNode linknode = new BridgeLinkNode();

		linknode.setBridgeLinkCreateTime(Util.formatDateToUIString(link.getBridgeMacLinkCreateTime()));
		linknode.setBridgeLinkLastPollTime(Util.formatDateToUIString(link.getBridgeMacLinkLastPollTime()));
		
		return linknode;
	}
			
	private String getSnmpInterfaceUrl(Integer nodeid,Integer ifindex) {
		if (ifindex != null && nodeid != null )
			return "element/snmpinterface.jsp?node="+nodeid+"&ifindex="+ifindex;
		return null;
	}

	private String getPortString(OnmsSnmpInterface snmpiface) {
		return snmpiface.getIfName() + "(ifindex:"+ snmpiface.getIfIndex()+")";
	}
	
	private String getNodeUrl(Integer nodeid) {
			return "element/node.jsp?node="+nodeid;
	}
}
