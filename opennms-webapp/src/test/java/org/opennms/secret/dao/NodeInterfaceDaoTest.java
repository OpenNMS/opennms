package org.opennms.secret.dao;

import java.util.Collection;

import org.opennms.secret.dao.impl.NodeDaoSimple;
import org.opennms.secret.dao.impl.NodeInterfaceDaoSimple;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;

import junit.framework.TestCase;


public class NodeInterfaceDaoTest extends TestCase {

	   protected void setUp() throws Exception {
	        super.setUp();
	    }

	    protected void tearDown() throws Exception {
	        super.tearDown();
	    }

	    
	    public void testGetInterface(){
	    	NodeInterfaceDao dao = new NodeInterfaceDaoSimple();
	    	NodeInterface ni = dao.getNodeInterface(new Long(1L));
	    	assertNotNull(ni);
	    	assertEquals("Node-1", ni.getIpHostName());
            
	    }
        
        public void testGetInterfaces() {
            NodeDao nodeDao = new NodeDaoSimple();
            Node node = nodeDao.getNode(new Long(1));
            NodeInterfaceDao nodeinterfacedao = new NodeInterfaceDaoSimple();
            Collection interfaces = nodeinterfacedao.getNodeInterfaces(node);
            assertNotNull(interfaces);
            
            
        }
}