//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.util.Iterator;
import java.util.Set;

import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;

public class NodeDaoTest extends AbstractDaoTestCase {
    
    public void testSave() {
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        OnmsNode node = new OnmsNode(distPoller);
        node.setLabel("MyFirstNode");
        getNodeDao().save(node);
    }

    public void testCreate() throws InterruptedException {
        OnmsDistPoller distPoller = getDistPoller();
        
        OnmsNode node = new OnmsNode(distPoller);
        node.setLabel("MyFirstNode");
        node.getAssetRecord().setDisplayCategory("MyCategory");
        getNodeDao().save(node);
        
        
        System.out.println("BEFORE GET");
        OnmsDistPoller dp = getDistPoller();
        assertSame(distPoller, dp);
        System.out.println("AFTER GET");
        Set nodes = getNodeDao().findNodes(dp);
        assertEquals(7, nodes.size());
        System.out.println("AFTER GETNODES");
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            OnmsNode retrieved = (OnmsNode) it.next();
            System.out.println("category for "+retrieved.getId()+" = "+retrieved.getAssetRecord().getDisplayCategory());
            if (node.getId().intValue() == 5) {
                assertEquals("MyFirstNode", retrieved.getLabel());
                assertEquals("MyCategory", retrieved.getAssetRecord().getDisplayCategory());
            }
        }
        System.out.println("AFTER Loop");
        
    }
    
    public void testQuery() throws InterruptedException {
        
        OnmsNode n = getNodeDao().get(1);
        assertNotNull(n);
        assertNotNull(n.getIpInterfaces());
        assertEquals(3, n.getIpInterfaces().size());
        
    }
    
    public void testQuery2() {
        OnmsNode n = getNodeDao().get(6);
        assertNotNull(n);
        assertEquals(3, n.getIpInterfaces().size());
        assertNotNull(n.getAssetRecord());
        assertEquals("category1", n.getAssetRecord().getDisplayCategory());
    }

    private OnmsDistPoller getDistPoller() {
        OnmsDistPoller distPoller = getDistPollerDao().load("localhost");
        assertNotNull(distPoller);
        return distPoller;
    }
}
