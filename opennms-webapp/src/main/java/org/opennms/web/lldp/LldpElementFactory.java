package org.opennms.web.lldp;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.LldpLink.LldpPortIdSubType;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly=true)
public class LldpElementFactory implements InitializingBean, LldpElementFactoryInterface{

	@Autowired
	private LldpElementDao m_lldpElementDao;
	
	@Autowired 
	private LldpLinkDao m_lldpLinkDao;
	
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

    public static LldpElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static LldpElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(LldpElementFactoryInterface.class);
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
			nodelinks.add(convertFromModel(nodeId,link));
		}
		return nodelinks;
	}
	
	@Transactional
	private LldpLinkNode convertFromModel(int nodeid, LldpLink link) {
		LldpLinkNode linknode = new LldpLinkNode();
		linknode.setLldpPortString(getPortString(link.getLldpPortId(), link.getLldpPortIdSubType()));
		linknode.setLldpPortDescr(link.getLldpPortDescr());
		linknode.setLldpPortUrl(getSnmpInterfaceUrl(Integer.valueOf(nodeid), link.getLldpPortIfindex()));
		
		LldpElement lldpremelement= m_lldpElementDao.findByChassisId(link.getLldpRemChassisId(),link.getLldpRemChassisIdSubType());
		if (lldpremelement != null) 
			linknode.setLldpRemChassisIdString(getRemChassisIdString(lldpremelement.getNode().getLabel(),link.getLldpRemChassisId(), link.getLldpRemChassisIdSubType()));
		else
			linknode.setLldpRemChassisIdString(getRemChassisIdString(link.getLldpRemSysname(),link.getLldpRemChassisId(), link.getLldpRemChassisIdSubType()));
		linknode.setLldpRemSysName(link.getLldpRemSysname());
		if (lldpremelement != null)
			linknode.setLldpRemChassisIdUrl(getNodeUrl(lldpremelement.getNode().getId()));

		linknode.setLldpRemPortString(getPortString(link.getLldpRemPortId(), link.getLldpRemPortIdSubType()));
		linknode.setLldpRemPortDescr(link.getLldpRemPortDescr());
		if (lldpremelement != null && link.getLldpRemPortIdSubType() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
			try {
				Integer remIfIndex = Integer.getInteger(link.getLldpRemPortId());
				linknode.setLldpRemPortUrl(getSnmpInterfaceUrl(Integer.valueOf(lldpremelement.getNode().getId()), remIfIndex));
			} catch (Exception e) {
				
			}
		}
		linknode.setLldpCreateTime(Util.formatDateToUIString(link.getLldpLinkCreateTime()));
		linknode.setLldpLastPollTime(Util.formatDateToUIString(link.getLldpLinkLastPollTime()));
		
		return linknode;
	}

	private String getRemChassisIdString(String sysname, String chassisId, LldpChassisIdSubType chassisType) {
		return sysname+ ": " + LldpChassisIdSubType.getTypeString(chassisType.getValue())+ ": " + chassisId;
	}

	private String getChassisIdString(String chassisId, LldpChassisIdSubType chassisType) {
		return LldpChassisIdSubType.getTypeString(chassisType.getValue())+ ": " + chassisId;
	}

	private String getPortString(String portId,LldpPortIdSubType type) {
		return LldpPortIdSubType.getTypeString(type.getValue()) + ": " + portId;
	}
	
	private String getSnmpInterfaceUrl(Integer nodeid,Integer ifindex) {
		if (ifindex != null && nodeid != null )
			return "element/snmpinterface.jsp?node="+nodeid+"&ifindex="+ifindex;
		return null;
	}
	
	private String getNodeUrl(Integer nodeid) {
		if (nodeid != null)
			return "element/node.jsp?node="+nodeid;
		return null;
	}
}
