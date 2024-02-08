/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
public class MockDatabaseIT extends TestCase {

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
        Querier querier = new Querier(m_db, "select node.nodeid as nodeId, ipinterface.ipaddr as ipAddr, ifServices.status as status, ifServices.serviceId as serviceId, service.serviceName as serviceName from ifServices, ipinterface, node, service where ifServices.serviceId = service.serviceId and ipinterface.id = ifServices.ipInterfaceId and node.nodeid = ipinterface.nodeid;") {
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
                assertEquals("Assertion failed: " + svc, svc.getSvcId(), serviceId);
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
        assertEquals(0, m_db.countRows("select * from ifServices, ipInterface, node where ifServices.ipInterfaceId = ipInterface.id and ipInterface.nodeid = node.nodeId and node.nodeid = '1'"));
    }

    public void testOutage() {
        final MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        Event svcLostEvent = MockEventUtil.createNodeLostServiceEvent("TEST", svc);

        m_db.writeEvent(svcLostEvent);
        m_db.createOutage(svc, svcLostEvent);
        assertEquals(1, m_db.countOutagesForService(svc));
        Querier querier = new Querier(m_db, "select node.nodeid as nodeid, ipinterface.ipaddr as ipaddr, ifservices.serviceid as serviceid from outages, ifservices, ipinterface, node where outages.ifserviceid = ifservices.id and ifservices.ipinterfaceid = ipinterface.id and ipinterface.nodeid = node.nodeid") {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String ipAddr = rs.getString("ipAddr");
                int serviceId = rs.getInt("serviceId");
                assertEquals(nodeId, svc.getNodeId());
                assertEquals(ipAddr, svc.getIpAddr());
                assertEquals(serviceId, svc.getSvcId());
            }
        };
        querier.execute();
        assertEquals(1, querier.getCount());
    }

    public void testUpdateNodeSequence() {
        int maxNodeId = m_db.getJdbcTemplate().queryForObject("select max(nodeid) from node", Integer.class);
        int nextSeqNum = m_db.getJdbcTemplate().queryForObject("select nextval('nodeNxtId')", Integer.class);
        assertTrue(nextSeqNum > maxNodeId);
        
    }
    
    public void testSetServiceStatus() {
        MockService svc = m_network.getService(1, "192.168.1.1", "SMTP");
        assertEquals('A', m_db.getServiceStatus(svc));
        m_db.setServiceStatus(svc, 'U');
        assertEquals('U', m_db.getServiceStatus(svc));
    }
    
    

}
