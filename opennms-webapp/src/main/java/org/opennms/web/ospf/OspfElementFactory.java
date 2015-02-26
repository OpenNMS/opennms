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

package org.opennms.web.ospf;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfElementDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.web.api.Util;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Transactional(readOnly=true)
public class OspfElementFactory implements InitializingBean, OspfElementFactoryInterface{

	@Autowired
	private OspfElementDao m_ospfElementDao;
	
	@Autowired 
	private OspfLinkDao m_ospfLinkDao;
	
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

    public static OspfElementFactoryInterface getInstance(ServletContext servletContext) {
        return getInstance(WebApplicationContextUtils.getWebApplicationContext(servletContext));    
    }

    public static OspfElementFactoryInterface getInstance(ApplicationContext appContext) {
    	return appContext.getBean(OspfElementFactoryInterface.class);
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
			nodelinks.add(convertFromModel(nodeId,link));
		}
		return nodelinks;
	}
	
	@Transactional
	private OspfLinkNode convertFromModel(int nodeid, OspfLink link) {
		OspfLinkNode linknode = new OspfLinkNode();
		linknode.setOspfIpAddr(str(link.getOspfIpAddr()));
		linknode.setOspfAddressLessIndex(link.getOspfAddressLessIndex());
		linknode.setOspfIfIndex(link.getOspfIfIndex());
		
		OspfElement ospfelement= m_ospfElementDao.findByRouterId(link.getOspfRemRouterId());
		if (ospfelement != null) {
			linknode.setOspfRemRouterId(getRemRouterIdString(str(link.getOspfRemRouterId()),ospfelement.getNode().getLabel()));
			linknode.setOspfRemRouterUrl(getNodeUrl(ospfelement.getNode().getId()));
		} else {
			linknode.setOspfRemRouterId(str(link.getOspfRemRouterId()));
		}
		
		linknode.setOspfRemIpAddr(str(link.getOspfRemIpAddr()));
		linknode.setOspfRemAddressLessIndex(link.getOspfRemAddressLessIndex());
		
		if (ospfelement != null && linknode.getOspfRemIpAddr() != null) 
			linknode.setOspfRemPortUrl(getIpInterfaceUrl(ospfelement.getNode().getId(), linknode.getOspfRemIpAddr()));
		
		linknode.setOspfLinkCreateTime(Util.formatDateToUIString(link.getOspfLinkCreateTime()));
		linknode.setOspfLinkLastPollTime(Util.formatDateToUIString(link.getOspfLinkLastPollTime()));
		
		return linknode;
	}
	
	private String getRemRouterIdString(String ip, String label) {
		return ip + "("+label+")";
	}
	
	private String getIpInterfaceUrl(Integer nodeid,String ipaddress) {
			return "element/interface.jsp?node="+nodeid+"&intf="+ipaddress;
	}

	private String getNodeUrl(Integer nodeid) {
			return "element/node.jsp?node="+nodeid;
	}
}
