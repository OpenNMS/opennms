/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockService.SvcMgmtStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 */
public class MockDatabaseTest extends TestCase {

    private MockNetwork m_network;
    private MockDatabase m_db;
    private MockDatabase m_secondDb;

    @Override
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
        // set the initial status to N as a test
        m_network.addService("HTTP").setMgmtStatus(SvcMgmtStatus.NOT_POLLED);
        m_network.addInterface("192.168.1.2");
        m_network.addPathOutage(1, InetAddressUtils.addr("192.168.1.1"), "ICMP");
        
        m_db = new MockDatabase();
        m_db.populate(m_network);
        

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        m_db.drop();
        if (m_secondDb != null) m_secondDb.drop();
    }
    
    public void testNodeQuery() {
        Querier querier = new Querier(m_db, "select * from node") {
            @Override
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
    
    public void testMultipleDatabases() throws Exception {
    		m_secondDb = new MockDatabase(m_db.getTestDatabase() + "_test2");
    	
    		Querier secondQuerier = new Querier(m_secondDb, "select * from node");
    		secondQuerier.execute();
    		Querier querier = new Querier(m_db, "select * from node");
    		querier.execute();
    		assertFalse(secondQuerier.getCount() == querier.getCount());
    		
    		MockNode node = m_network.getNode(1);
    		m_secondDb.writeNode(node);
    		secondQuerier = new Querier(m_secondDb, "select * from node");
    		secondQuerier.execute();
    		assertEquals(1, secondQuerier.getCount());
    		
    }
    
    public void testIFQuery() {
        Querier querier = new Querier(m_db, "select * from ipInterface") {
            @Override
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
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String ipAddr = rs.getString("ipAddr");
                int serviceId = rs.getInt("serviceId");
                String serviceName = rs.getString("serviceName");
                String status = rs.getString("status");
                MockService svc = m_network.getService(nodeId, ipAddr, serviceName);
                assertNotNull(svc);
                assertEquals("Assertion failed: " + svc, svc.getNodeId(), nodeId);
                assertEquals("Assertion failed: " + svc, svc.getIpAddr(), ipAddr);
                assertEquals("Assertion failed: " + svc, svc.getSvcName(), serviceName);
                assertEquals("Assertion failed: " + svc, svc.getId(), serviceId);
                assertEquals("Assertion failed: " + svc, svc.getMgmtStatus().toDbString(), status);
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
        Event svcLostEvent = MockEventUtil.createNodeLostServiceEvent("TEST", svc);

        m_db.writeEvent(svcLostEvent);
        m_db.createOutage(svc, svcLostEvent);
        assertEquals(1, m_db.countOutagesForService(svc));
        Querier querier = new Querier(m_db, "select * from outages") {
            @Override
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
        assertEquals(1, querier.getCount());
    }

    public void testUpdateNodeSequence() {
        int maxNodeId = m_db.getJdbcTemplate().queryForInt("select max(nodeid) from node");
        int nextSeqNum = m_db.getJdbcTemplate().queryForInt("select nextval('nodeNxtId')");
        assertTrue(nextSeqNum > maxNodeId);
        
    }
    
    public void testSetServiceStatus() {
        MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
        assertEquals('A', m_db.getServiceStatus(svc));
        m_db.setServiceStatus(svc, 'U');
        assertEquals('U', m_db.getServiceStatus(svc));
    }
    
    

}
