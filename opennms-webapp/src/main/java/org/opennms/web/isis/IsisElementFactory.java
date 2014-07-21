package org.opennms.web.isis;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly=true)
public class IsisElementFactory implements InitializingBean, IsisElementFactoryInterface{

	@Autowired
	private IsIsElementDao m_isisElementDao;
	
	@Autowired 
	private IsIsLinkDao m_isisLinkDao;
	
	@Autowired
	private NodeDao m_nodeDao;
	
	@Autowired
	private SnmpInterfaceDao m_snmpInterfaceDao;
	
	@Autowired
	private PlatformTransactionManager m_transactionManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    public static IsisElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static IsisElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(IsisElementFactoryInterface.class);
    }

    @Override
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
		
		link.setIsisISAdjNeighSNPAAddress(link.getIsisISAdjNeighSNPAAddress());
		link.setIsisISAdjState(IsisISAdjState.get(link.getIsisISAdjState().getValue()));
		linknode.setIsisISAdjNbrExtendedCircID(link.getIsisISAdjNbrExtendedCircID());
		
		OnmsSnmpInterface remiface = null;
		if (isiselement != null) {
			IsIsLink adjLink = m_isisLinkDao.get(isiselement.getNode().getId(),link.getIsisISAdjIndex(),link.getIsisCircIndex());
			if (adjLink != null) {
				remiface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(isiselement.getNode().getId(), adjLink.getIsisCircIfIndex());
			}			
		}
		if (remiface == null) {
			remiface = getFromSNPAAddress(link.getIsisISAdjNeighSNPAAddress());
		}
		
		if (remiface != null) {
			linknode.setIsisISAdjNeighPort(getPortString(remiface));
			linknode.setIsisISAdjUrl(getSnmpInterfaceUrl(remiface.getNode().getId(), remiface.getIfIndex()));
		} else {
			linknode.setIsisISAdjNeighPort("(Isis IS Adj Index: "+link.getIsisISAdjIndex()+ ")");
		}

		linknode.setIsisLinkCreateTime(Util.formatDateToUIString(link.getIsisLinkCreateTime()));
		linknode.setIsisLinkLastPollTime(Util.formatDateToUIString(link.getIsisLinkLastPollTime()));
		
		return linknode;
	}
	
	private OnmsSnmpInterface getFromSNPAAddress(String snpaaddress) {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.eq("physAddr", snpaaddress);
        final List<OnmsSnmpInterface> nodes = m_snmpInterfaceDao.findMatching(builder.toCriteria());

        if (nodes.size() == 1)
            return nodes.get(0);
        return null;
	}
	
	private String getAdjSysIDString(String adjsysid, String label) {
		return adjsysid + "("+label+")";
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
