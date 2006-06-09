package org.opennms.secret.web;

import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.opennms.secret.dao.DataSourceDao;
import org.opennms.secret.dao.NodeInterfaceDao;
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

import sun.print.resources.serviceui;

public class ControllerTest extends TestCase {

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

	protected void setUp() throws Exception {
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

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testNodeService() throws Exception {
		assertNotNull("nodeMaV was not null", nodeMaV);
		
		assertTrue(viewName + " did not match", nodeMaV.getViewName().equals(viewName));
		Node node = (Node)nodeMaV.getModel().get(NodeController.MODEL_NAME);
		assertNotNull(NodeController.MODEL_NAME + " returned a null", node);
		assertEquals("NodeID did not match requested NodeID", node.getNodeId(),new Long(1L));
		assertNotNull(node.getNodeLabel());
	}
	
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
