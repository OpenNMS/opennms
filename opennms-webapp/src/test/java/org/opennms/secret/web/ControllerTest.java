//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.secret.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.secret.dao.impl.DataSourceDaoSimple;
import org.opennms.secret.dao.impl.NodeDaoSimple;
import org.opennms.secret.dao.impl.NodeInterfaceDaoSimple;
import org.opennms.secret.dao.impl.ServiceDaoSimple;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;
import org.opennms.secret.service.impl.DataSourceServiceImpl;
import org.opennms.secret.service.impl.NodeInterfaceServiceImpl;
import org.opennms.secret.service.impl.NodeServiceImpl;
import org.opennms.secret.service.impl.ServiceServiceImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class ControllerTest {

	private NodeServiceImpl nodeService;
	private NodeInterfaceServiceImpl ifService;
	private NodeController nodeController;
	private NodeInterfaceController ifController;
	private ModelAndView nodeMaV;
	private ModelAndView ifMaV;
    private String viewName = "testview";
    private DataSourceServiceImpl dataSourceService;
    private NodeInterfaceServiceImpl nodeIfService;
    private ServiceServiceImpl serviceService;
    
    private MockHttpSession m_session;
    private MockHttpServletRequest m_request;
    private MockHttpServletResponse m_response;

    @Before
	public void setUp() throws Exception {
		nodeService = new NodeServiceImpl();
		nodeService.setNodeDao(new NodeDaoSimple());
        
        dataSourceService = new DataSourceServiceImpl();
        dataSourceService.setDataSourceDao(new DataSourceDaoSimple());
        
        nodeIfService = new NodeInterfaceServiceImpl();
        nodeIfService.setNodeInterfaceDao(new NodeInterfaceDaoSimple());
        
        serviceService = new ServiceServiceImpl();
        serviceService.setServiceDao(new ServiceDaoSimple());
		
		nodeController = new NodeController();
        nodeController.setViewName(viewName);
		nodeController.setNodeService(nodeService);
        nodeController.setDataSourceService(dataSourceService);
        nodeController.setNodeInterfaceService(nodeIfService);
        nodeController.setServiceService(serviceService);
		nodeController.setNodeId(new Long(1l));
        
       m_session = new MockHttpSession();
       resetRequestResponse();
		
		nodeMaV = nodeController.handleRequest(m_request, m_response);
	}
    
    private void resetRequestResponse() {
        m_request = new MockHttpServletRequest();
        m_request.setSession(m_session);
        m_response = new MockHttpServletResponse();
    }

    @Test
    @Ignore("This is an un implemented feature")
	public void testNodeService() throws Exception {
		assertNotNull("nodeMaV was not null", nodeMaV);
		
		assertTrue(viewName + " did not match", nodeMaV.getViewName().equals(viewName));
		Node node = (Node)nodeMaV.getModel().get(NodeController.MODEL_NAME);
		assertNotNull(NodeController.MODEL_NAME + " returned a null", node);
		assertEquals("NodeID did not match requested NodeID", node.getNodeId(),new Long(1L));
		assertNotNull(node.getNodeLabel());
	}
	
    @Test
    @Ignore("This is an un implemented feature")
	public void testInterfaceService() throws Exception {
		ifService = new NodeInterfaceServiceImpl();
		
		ifController = new NodeInterfaceController();
		ifController.setNodeInterfaceService(ifService);
		ifMaV = ifController.execute();
		
		NodeInterface testInterface = new NodeInterface();
		testInterface.setNodeId(new Long(1l));
		testInterface.setIfIndex(new Long(1));
		
		assertTrue(ifMaV.getViewName().equals(NodeInterfaceController.IF_VIEW));
		HashSet set = (HashSet)ifMaV.getModel().get(NodeInterfaceController.MODEL_NAME);
		assertNotNull(NodeInterfaceController.MODEL_NAME + "returned an null HashSet", set);
		assertFalse("Interface did not match the requested Interface", set.isEmpty());
		assertContainsIf("Interface not found", testInterface, set);
		
	}

	private void assertContainsIf(String string, NodeInterface testInterface, HashSet set) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			NodeInterface iface = (NodeInterface) it.next();
			if (iface.getNodeId().equals(testInterface.getNodeId()) && iface.getIfIndex().equals(testInterface.getIfIndex()))
				return;
		}
		fail("Interface not found");
		
	}

}
