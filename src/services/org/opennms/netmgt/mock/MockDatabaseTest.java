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
package org.opennms.netmgt.mock;

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 */
public class MockDatabaseTest extends TestCase {

    private MockNetwork m_network;
    private MockDatabase m_db;

    protected void setUp() throws Exception {
        super.setUp();

        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addInterface("192.168.1.2");
        
        m_db = new MockDatabase();
        m_db.populate(m_network);
        

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        m_db.drop();
    }
    
    public void testNodeQuery() {
        Querier querier = new Querier(m_db, "select * from node") {
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String label = rs.getString("nodeLabel");
                MockNode node = m_network.getNode(nodeId);
                assertNotNull(node);
                assertEquals(nodeId, node.getNodeId());
                assertEquals(label, node.getLabel());
            }
        };
        querier.execute();
        assertEquals(m_network.getNodeCount(), querier.getCount());
    }
    
    public void testIFQuery() {
        Querier querier = new Querier(m_db, "select * from ipInterface") {
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String ipAddr = rs.getString("ipAddr");
                MockInterface iface = m_network.getInterface(nodeId, ipAddr);
                assertNotNull(iface);
                assertEquals(nodeId, iface.getNodeId());
                assertEquals(ipAddr, iface.getIpAddr());
            }
        };
        querier.execute();
        assertEquals(m_network.getInterfaceCount(), querier.getCount());
    }
    
    public void testServiceQuery() {
        Querier querier = new Querier(m_db, "select nodeId, ipAddr, ifServices.status as status, ifServices.serviceId as serviceId, service.serviceName as serviceName from ifServices, service where ifServices.serviceId = service.serviceId;") {
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String ipAddr = rs.getString("ipAddr");
                int serviceId = rs.getInt("serviceId");
                String serviceName = rs.getString("serviceName");
                String status = rs.getString("status");
                MockService svc = m_network.getService(nodeId, ipAddr, serviceName);
                assertNotNull(svc);
                assertEquals(svc.getNodeId(), nodeId);
                assertEquals(svc.getIpAddr(), ipAddr);
                assertEquals(svc.getName(), serviceName);
                assertEquals(svc.getId(), serviceId);
                assertEquals("A", status);
            }
        };
        querier.execute();
        assertEquals(m_network.getServiceCount(), querier.getCount());
    }
    
    public void testCascade() {
        m_db.update("delete from node where nodeid = '1'");
        assertEquals(0, m_db.countRows("select * from node where nodeid = '1'"));
        assertEquals(0, m_db.countRows("select * from ipInterface where nodeid = '1'"));
        assertEquals(0, m_db.countRows("select * from ifServices where nodeid = '1'"));
    }
    
    public void testOutage() {
        final MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        Event svcLostEvent = MockUtil.createNodeLostServiceEvent("TEST", svc);
        
        m_db.writeEvent(svcLostEvent);
        m_db.createOutage(svc, svcLostEvent);
        m_db.createOutage(svc, svcLostEvent);
        assertEquals(2, m_db.countOutagesForService(svc));
        Querier querier = new Querier(m_db, "select * from outages") {
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String ipAddr = rs.getString("ipAddr");
                int serviceId = rs.getInt("serviceId");
                assertEquals(nodeId, svc.getNodeId());
                assertEquals(ipAddr, svc.getIpAddr());
                assertEquals(serviceId, svc.getId());
            }
        };
        querier.execute();
        assertEquals(2, querier.getCount());
        
    }
    
    public void testSetServiceStatus() {
        MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
        assertEquals('A', m_db.getServiceStatus(svc));
        m_db.setServiceStatus(svc, 'U');
        assertEquals('U', m_db.getServiceStatus(svc));
    }
    
    

}
