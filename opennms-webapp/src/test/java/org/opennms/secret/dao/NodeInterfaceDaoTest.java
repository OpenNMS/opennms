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