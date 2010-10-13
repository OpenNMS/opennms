/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                  "classpath*:/META-INF/opennms/component-dao.xml",
                                  "classpath:/daoNetworkElementFactoryTestContext.xml"})
@JUnitTemporaryDatabase()
//@Transactional
public class DaoNetworkElementFactoryTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    ApplicationContext m_appContext;
    
    NetworkElementFactory m_networkElemFactory;
    
    @Autowired
    DataSource m_dataSource;
    
    @Autowired
    SimpleJdbcTemplate m_jdbcTemplate;
    
    
    @Before
    public void setUp() {
        Vault.setDataSource(m_dataSource);
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    public void testGetNodeLabel() {
        m_networkElemFactory = NetworkElementFactory.getInstance(m_appContext);
        
        String nodeLabel = m_networkElemFactory.getNodeLabel(1);
        assertEquals("node1", nodeLabel);
        
        OnmsNode node = m_dbPopulator.getNode1();
        node.setLabel("Hello World");
        m_dbPopulator.getNodeDao().saveOrUpdate(node);
        
        nodeLabel = m_networkElemFactory.getNodeLabel(1);
        assertEquals("Hello World", nodeLabel);
    }
    
    @Test
    public void testGetIpPrimaryAddress() {
        m_networkElemFactory = NetworkElementFactory.getInstance(m_appContext);
        
        String ipAddress = m_networkElemFactory.getIpPrimaryAddress(1);
        assertEquals("192.168.1.1", ipAddress);
        
    }
    
    @Test
    public void testGetNode() {
        OnmsNode onmsNode = m_dbPopulator.getNode1();
        onmsNode.setLabel("Hello Node 1");
        
        m_dbPopulator.getNodeDao().saveOrUpdate(onmsNode);
        
        m_networkElemFactory = NetworkElementFactory.getInstance(m_appContext);
        Node node = m_networkElemFactory.getNode(1);
        
        assertEquals("Hello Node 1", node.m_label);
    }
    
    @Test
    public void testGetAllNodes() {
        
        m_networkElemFactory = NetworkElementFactory.getInstance(m_appContext);
        Node[] nodes = m_networkElemFactory.getAllNodes();
        
        assertEquals(6, nodes.length);
        
        OnmsNode node = new OnmsNode();
        node.setLabel("node 7");
        m_dbPopulator.getNodeDao().save(node);
        
        nodes = m_networkElemFactory.getAllNodes();
        assertEquals(7, nodes.length);
    }
    
    @Test
    public void testGetNodesLike() throws SQLException {
        OnmsNode onmsNode = m_dbPopulator.getNode1();
        onmsNode.setType("A");
        onmsNode.setLabel("newLabel");
        
        m_dbPopulator.getNodeDao().saveOrUpdate(onmsNode);
        
        
        assertEquals(1, getNetworkElementFactory().getNodesLike("label").length);
        
        assertEquals(1, getNetworkElementFactory().getNodesLike("label", 2).length);
    }
    
    @Test
    public void testGetNodesWithIpLike() throws SQLException {
        
        OnmsNode onmsNode = m_dbPopulator.getNode1();
        onmsNode.setType("A");
        onmsNode.addIpInterface(new OnmsIpInterface("172.20.1.104", onmsNode));
        onmsNode.setLabel("newLabel");
        
        m_dbPopulator.getNodeDao().saveOrUpdate(onmsNode);
        
        assertEquals(1, getNetworkElementFactory().getNodesWithIpLike("*.*.*.*").length);
        
        //IpLike and ServiceId
        
        Node[] nodes = getNetworkElementFactory().getNodesWithIpLike("*.*.*.*", 1);
        assertEquals(1, nodes.length);
        assertEquals(1, nodes[0].getNodeId());
        
        Node[] nodes2 = getNetworkElementFactory().getNodesWithIpLike("192.168.1.1", 3);
        assertEquals(0, nodes2.length);
    }
    
    @Test
    public void testGetNodesWithService() {
        assertEquals(6, getNetworkElementFactory().getNodesWithService(1).length);
        
        OnmsServiceType svcType = new OnmsServiceType("Service1");
        ServiceTypeDao svcDao = m_dbPopulator.getServiceTypeDao();
        svcDao.save(svcType);
        
        OnmsNode node1 = m_dbPopulator.getNode1();
        
        OnmsIpInterface iface = new OnmsIpInterface("172.20.1.1", node1);
        IpInterfaceDao ifaceDao = m_dbPopulator.getIpInterfaceDao();
        ifaceDao.save(iface);
        
        OnmsMonitoredService monSvc = new OnmsMonitoredService(iface, svcType);
        MonitoredServiceDao monSvcDao = m_dbPopulator.getMonitoredServiceDao();
        monSvcDao.save(monSvc);
        
        assertEquals(1, getNetworkElementFactory().getNodesWithService(4).length);
        
    }
    
    @Test
    public void testGetNodesWithPhysAddr() {
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='1'");
        m_jdbcTemplate.update("UPDATE snmpinterface SET snmpphysaddr='macAddr' WHERE nodeid='1'");
        
        
        assertEquals(1, getNetworkElementFactory().getNodesWithPhysAddr("macAddr").length);
        
        m_jdbcTemplate.update("INSERT INTO node (nodeid, nodetype, nodecreatetime) VALUES (77, 'A', now())");
        m_jdbcTemplate.update("INSERT INTO assets (id, nodeid, category, userlastmodified, lastmodifieddate) VALUES (77, 77, 'Unspecified', 'Donald', now())"); 
        
        assertEquals(1, getNetworkElementFactory().getNodesWithPhysAddr("macAddr").length);
    }
    
    @Test
    public void testGetNodesWithPhysAddrAtInterface() {
        assertTrue(false);
    }
    
    @Test
    public void testGetNodesWithPhysAddrFromSnmpInterface() {
        OnmsNode node = m_dbPopulator.getNode1();
        node.setType("A");
        m_dbPopulator.getNodeDao().saveOrUpdate(node);
        
        Set<OnmsSnmpInterface> snmpInterfaces = node.getSnmpInterfaces();
        OnmsSnmpInterface snmpIface = snmpInterfaces.iterator().next();
        snmpIface.setPhysAddr("macAddr");
        
        m_dbPopulator.getSnmpInterfaceDao().saveOrUpdate(snmpIface);
        
        assertEquals(1, getNetworkElementFactory().getNodesWithPhysAddrFromSnmpInterface("macAddr").length);
    }
    
    @Test
    public void testGetNodesWithIfAlias() {
        
        OnmsNode node1 = m_dbPopulator.getNode1();
        node1.setType("A");
        
        Set<OnmsSnmpInterface> snmpIfaces = node1.getSnmpInterfaces();
        OnmsSnmpInterface snmpIface = snmpIfaces.iterator().next();
        snmpIface.setIfAlias("ifAlias");
        m_dbPopulator.getSnmpInterfaceDao().saveOrUpdate(snmpIface);
        
        m_dbPopulator.getNodeDao().saveOrUpdate(node1);
        
        assertEquals(1, getNetworkElementFactory().getNodesWithIfAlias("ifAlias").length);
    }
    
    @Test
    public void testGetHostName() {
        OnmsNode node1 = m_dbPopulator.getNode1();
        OnmsIpInterface ipIface = node1.getIpInterfaceByIpAddress("192.168.1.1");
        ipIface.setIpHostName("HostName");
        m_dbPopulator.getIpInterfaceDao().saveOrUpdate(ipIface);
        
        assertEquals("HostName", getNetworkElementFactory().getHostname("192.168.1.1"));
    }
    
    
    @Test
    public void testGetInterface() {
        OnmsNode node1 = m_dbPopulator.getNode1();
        OnmsIpInterface ipIface = node1.getIpInterfaceByIpAddress("192.168.1.1");
        ipIface.setIpHostName("HostName");
        m_dbPopulator.getIpInterfaceDao().saveOrUpdate(ipIface);
        
        
        
        Interface iface1 = getNetworkElementFactory().getInterface(1);
        assertEquals("HostName", iface1.getHostname());
        
        Interface iface2 = getNetworkElementFactory().getInterface(1, "192.168.1.1");
        assertEquals("HostName", iface2.getHostname());
        
        Interface iface3 = getNetworkElementFactory().getInterface(1, "192.168.1.1", 1);
        assertEquals("HostName", iface3.getHostname());
    }
    
    @Test
    public void testGetSnmpInterface() {
        OnmsNode node1 = m_dbPopulator.getNode1();
        Set<OnmsSnmpInterface> snmpIfaces = node1.getSnmpInterfaces();
        
        assertTrue(snmpIfaces.size() > 0);
        
        OnmsSnmpInterface snmpIface = snmpIfaces.iterator().next();
        snmpIface.setIfAdminStatus(1);
        
        m_dbPopulator.getSnmpInterfaceDao().saveOrUpdate(snmpIface);
        
        Interface intf = getNetworkElementFactory().getSnmpInterface(1, 1);
        assertNotNull(intf);
        assertEquals("192.168.1.1", intf.getIpAddress());
    }
    
    @Test
    public void testGetInterfacesWithIpAddress() {
        assertEquals(1, getNetworkElementFactory().getInterfacesWithIpAddress("192.168.1.1").length);
    }
    
    @Test
    public void testGetInterfacesWithIfAlias() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeId, ipaddr, ifindex, ismanaged, iplastcapsdpoll, snmpinterfaceid) VALUES (1, '209.18.47.61', 3, 'D', now(), 9)");
        m_jdbcTemplate.update("UPDATE snmpinterface SET snmpifalias=3 WHERE id='9'");
        
        assertEquals(1, getNetworkElementFactory().getInterfacesWithIfAlias(1, "3").length);
    }
    
    @Test
    public void testNodeHasIfAliases() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeId, ipaddr, ifindex, ismanaged, iplastcapsdpoll, snmpinterfaceid) VALUES (1, '209.18.47.61', 3, 'D', now(), 9)");
        m_jdbcTemplate.update("UPDATE snmpinterface SET snmpifalias='_3', snmpifindex=1 WHERE id='9'");
        
        assertTrue(getNetworkElementFactory().nodeHasIfAliases(1));
    }
    
    @Test
    public void testGetAllInterfacesOnNode() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeId, ipaddr, ifindex, ismanaged, iplastcapsdpoll) VALUES (1, '172.20.1.1', 3, 'D', now())");
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeId, ipaddr, ifindex, ismanaged, iplastcapsdpoll) VALUES (1, '172.20.1.2', 3, 'D', now())");
        assertEquals(5, getNetworkElementFactory().getAllInterfacesOnNode(1).length);
    }
    
    @Test
    public void testGetAllSnmpInterfacesOnNode() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO snmpinterface (nodeid, ipaddr, snmpifindex) VALUES (1, '172.20.1.1', 45)");
        assertEquals(4, getNetworkElementFactory().getAllSnmpInterfacesOnNode(1).length);
    }
    
    @Test
    public void testGetActiveInterfacesOnNode() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeid, ipaddr, ifindex, ismanaged, iplastcapsdpoll) VALUES (1, '172.20.1.1', 3, 'A', now())");
        m_jdbcTemplate.update("INSERT INTO ipinterface (nodeid, ipaddr, ifindex, ismanaged, iplastcapsdpoll) VALUES (1, '172.20.1.2', 3, 'A', now())");
        assertEquals(5, getNetworkElementFactory().getActiveInterfacesOnNode(1).length);
    }
    
    @Test
    public void testGetAllDataLinks() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 2, 4, now())");
        assertEquals(4, getNetworkElementFactory().getAllDataLinks().length);
    }
    
    @Test
    public void testGetAllNodesByServiceId() throws SQLException {
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='1'");
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='2'");
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='3'");
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='4'");
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='5'");
        m_jdbcTemplate.update("UPDATE node SET nodetype='A' WHERE nodeid='6'");
        
        assertEquals(6, getNetworkElementFactory().getAllNodes(1).length);
    }
    
    @Test
    public void testGetAllServices() throws SQLException {
        assertEquals(30, getNetworkElementFactory().getAllServices().length);
//        assertEquals(30, NetworkElementFactory.getAllServices(null).length);
        
    }
    
    @Test
    public void testGetAtInterface() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO atinterface (nodeid, ipaddr, atphysaddr, status, sourcenodeid, ifindex, lastpolltime) VALUES (1, '172.20.1.1', 'macAddr1', 'G', 2, 1, now())");
        m_jdbcTemplate.update("INSERT INTO atinterface (nodeid, ipaddr, atphysaddr, status, sourcenodeid, ifindex, lastpolltime) VALUES (2, '172.20.1.2', 'macAddr2', 'G', 1, 2, now())");
        
        assertEquals(1, NetworkElementFactory.getAtInterface(1, "172.20.1.1", null).get_sourcenodeid());
        
        assertEquals(1, getNetworkElementFactory().getAtInterface(1, "172.20.1.1"));
    }
    
    @Test
    public void testGetIpRoute() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO iprouteinterface (nodeid, routedest, routemask, routenexthop, routeifindex, status, lastpolltime) VALUES (1, 'routeDest', 'routemask', 'nc', '1', 'G', now())");
        m_jdbcTemplate.update("INSERT INTO iprouteinterface (nodeid, routedest, routemask, routenexthop, routeifindex, status, lastpolltime) VALUES (1, 'routeDest2', 'routemask2', 'nc2', '2', 'G', now())");
        
        assertEquals(2, NetworkElementFactory.getIpRoute(1, null).length);
    }
    
    @Test
    public void testGetDataLinks() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 3, 4, now())");
        
        assertEquals(2, getNetworkElementFactory().getDataLinks(1, 3).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'D', 4, 4, now())");
        
        assertEquals(2, getNetworkElementFactory().getDataLinks(1, 3).length);
    }
    
    @Test
    public void testGetDataLinksOnInterface() {
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 3, 4, now())");
        
        
        assertEquals(2, getNetworkElementFactory().getDataLinksOnInterface(1, 3).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (2, 3, 'G', 1, 3, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (3, 3, 'G', 1, 3, now())");
        
        assertEquals(4, getNetworkElementFactory().getDataLinksOnInterface(1, 3).length);
    }
    
    @Test
    public void testGetDataLinksByNode() throws SQLException {
        
        assertEquals(2, getNetworkElementFactory().getDataLinks(1).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 3, 4, now())");
        
        assertEquals(4, getNetworkElementFactory().getDataLinks(1).length);
        
    }
    
    @Test
    public void testGetDataLinksFromNodeParentById()  {
        assertEquals(3, getNetworkElementFactory().getDataLinksFromNodeParent(1).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 3, 4, now())");
        
        assertEquals(3, getNetworkElementFactory().getDataLinksFromNodeParent(1).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'D', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'D', 3, 4, now())");
        
        assertEquals(3, getNetworkElementFactory().getDataLinksFromNodeParent(1).length);
    }
    
    @Test
    public void testGetDataLinksOnNodeByNodeId() throws SQLException {
        assertEquals(5, getNetworkElementFactory().getDataLinksOnNode(1).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 3, 4, now())");
        
        assertEquals(7, getNetworkElementFactory().getDataLinksOnNode(1).length);
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'D', 2, 4, now())");
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'D', 3, 4, now())");
        
        assertEquals(7, getNetworkElementFactory().getDataLinksOnNode(1).length);
    }
    
    @Test
    public void testIsParentNode() throws SQLException {
        assertTrue(getNetworkElementFactory().isParentNode(1));
        assertFalse(getNetworkElementFactory().isParentNode(6));
        
        m_jdbcTemplate.update("INSERT INTO datalinkinterface (nodeid, ifindex, status, nodeparentid, parentifindex, lastpolltime) VALUES (1, 3, 'G', 6, 4, now())");
        
        assertTrue(getNetworkElementFactory().isParentNode(6));
    }
    
    @Test
    public void testGetService() throws SQLException {
        //m_jdbcTemplate.update("INSERT INTO ifservices (nodeid, ipaddr, serviceid, status) VALUES (1, '192.168.1.1', 1, 'A')");
        Service svc = getNetworkElementFactory().getService(2);
        assertNotNull(svc);
        assertNotNull(svc.getServiceName());
        assertEquals(2, svc.getId());
        
        Service svc2 = getNetworkElementFactory().getService(3);
        assertNotNull(svc2);
        assertNotNull(svc2.getServiceName());
        assertEquals(3, svc2.getId());
        
        Service svc3 = getNetworkElementFactory().getService(2);
        assertNotNull(svc3);
        assertNotNull(svc3.getServiceName());
        assertEquals(2, svc3.getId());
        
        Service svc4 = getNetworkElementFactory().getService(3);
        assertNotNull(svc4);
        assertNotNull(svc4.getServiceName());
        assertEquals(3, svc4.getId());
        
        
        Service svc5 = getNetworkElementFactory().getService(1, "192.168.1.1", 2);
        assertNotNull(svc5);
        assertEquals(2, svc5.getId());
        assertEquals("192.168.1.1", svc5.getIpAddress());
        assertEquals(1, svc5.getIfIndex());
        
    }
    
    @Test
    public void testGetServiceIdFromName() throws SQLException {
        int serviceId = getNetworkElementFactory().getServiceIdFromName("ICMP");
        int htmlServiceId = getNetworkElementFactory().getServiceIdFromName("HTTP");
        
        assertEquals(1, serviceId);
        assertEquals(3, htmlServiceId);
    }
    
    @Test
    public void testGetServiceNameFromId() {
        String icmpService = getNetworkElementFactory().getServiceNameFromId(1);
        String snmpService = getNetworkElementFactory().getServiceNameFromId(2);
        
        assertEquals("ICMP", icmpService);
        assertEquals("SNMP", snmpService);
    }
    
    @Test
    public void testGetServiceIdToNameMap() {
        Map<Integer, String> idMap = getNetworkElementFactory().getServiceIdToNameMap();
        assertEquals(3, idMap.size());
        assertEquals("HTTP", idMap.get(3));
    }
    
    @Test
    public void testGetServiceNameToIdMap() {
        Map<String, Integer> nameMap = getNetworkElementFactory().getServiceNameToIdMap();
        assertEquals(3, nameMap.size());
        assertEquals(1, nameMap.get("ICMP").intValue());
    }
    
    @Test
    public void testGetServicesOnNode() throws SQLException {
        
        Service[] services = getNetworkElementFactory().getServicesOnNode(1);
        assertEquals(5, services.length);
        assertNotNull( services[0].getServiceName());
        assertNotNull(services[1].getServiceName());
        assertNotNull(services[2].getServiceName());
        
        
        Service[] services2 = getNetworkElementFactory().getServicesOnNode(1, 1);
        assertEquals(3, services2.length);
        assertNotNull(services2[0].getServiceName());
    }
    
    @Test
    public void testGetServicesOnInterface() throws SQLException {
        Service[] services = getNetworkElementFactory().getServicesOnInterface(1, "192.168.1.1");
        assertEquals(0, services.length);
        
        m_jdbcTemplate.update("UPDATE ifservices SET status='A' WHERE id=2");
        m_jdbcTemplate.update("UPDATE ifservices SET status='A' WHERE id=3");
        
        Service[] services2 = getNetworkElementFactory().getServicesOnInterface(1, "192.168.1.1");
        assertEquals(2, services2.length);
        
        m_jdbcTemplate.update("UPDATE ifservices SET status='D' WHERE id=2");
        
        Service[] services3 = getNetworkElementFactory().getServicesOnInterface(1, "192.168.1.1", false);
        assertEquals(1, services3.length);
        
        Service[] services4 = getNetworkElementFactory().getServicesOnInterface(1, "192.168.1.1", true);
        assertEquals(2, services4.length);
    }
    
    @Test
    public void testGetNodeIdsWithIpLike() throws SQLException {
        OnmsNode onmsNode1 = m_dbPopulator.getNode1();
        onmsNode1.setType("A");
        onmsNode1.addIpInterface(new OnmsIpInterface("172.20.1.104", onmsNode1));
        onmsNode1.setLabel("newLabel");
        
        OnmsNode onmsNode2 = m_dbPopulator.getNodeDao().get(2);
        onmsNode2.setType("A");
        onmsNode2.setLabel("label2");
        
        m_dbPopulator.getNodeDao().saveOrUpdate(onmsNode1);
        m_dbPopulator.getNodeDao().saveOrUpdate(onmsNode2);
        
        List<Integer> nodes = getNetworkElementFactory().getNodeIdsWithIpLike("192.168.*.*");
        assertEquals(2, nodes.size());
        
        List<Integer> nodes2 = getNetworkElementFactory().getNodeIdsWithIpLike("*.*.*.*");
        assertEquals(2, nodes2.size());
        
        
    }
    
    @Test
    public void testGetNodesLikeAndIpLike() throws SQLException {
        assertEquals(1, getNetworkElementFactory().getNodesLikeAndIpLike("node1", "192.168.1.1", 1).length);
        assertEquals(0, getNetworkElementFactory().getNodesLikeAndIpLike("node5", "192.168.1", 1).length);
        
    }
    
    @Test
    public void testGetNodesFromPhysaddr() throws SQLException {
        m_jdbcTemplate.update("INSERT INTO atinterface (nodeid, ipaddr, atphysaddr, status, sourcenodeid, ifindex, lastpolltime) VALUES (1, '192.168.1.1', 'macAddr', 'A', 1, 1, now())");
        m_jdbcTemplate.update("INSERT INTO atinterface (nodeid, ipaddr, atphysaddr, status, sourcenodeid, ifindex, lastpolltime) VALUES (2, '192.168.2.1', 'macAddr2', 'D', 2, 1, now())");
        
        assertEquals(1, getNetworkElementFactory().getNodesFromPhysaddr("macAddr").length);
        
        m_jdbcTemplate.update("INSERT INTO atinterface (nodeid, ipaddr, atphysaddr, status, sourcenodeid, ifindex, lastpolltime) VALUES (3, '192.168.3.1', 'macAddr3', 'A', 2, 1, now())");
        
        assertEquals(2, getNetworkElementFactory().getNodesFromPhysaddr("macAddr").length);
    }
    
    @Test
    public void textGetAllInterfaces() throws SQLException {
        
        assertEquals(18, getNetworkElementFactory().getAllInterfaces(false).length);
        
        Interface[] intfs2 = getNetworkElementFactory().getAllInterfaces(true);
        assertEquals(18, intfs2.length);
        
        int count = 0;
        for(int j = 0; j < intfs2.length; j++) {
            if(intfs2[j].getIfIndex() > 0) {
                count++;
            }
        }
        assertEquals(3, count);
        
        Interface[] intfs3 = getNetworkElementFactory().getAllInterfaces();
        assertEquals(18, intfs3.length);
        
        count = 0;
        for(int j = 0; j < intfs3.length; j++) {
            if(intfs2[j].getIfIndex() > 0) {
                count++;
            }
        }
        assertEquals(3, count);
    }
    
    @Test
    public void testGetAllManagedIpInterfaces() throws SQLException {
        m_jdbcTemplate.update("UPDATE ipinterface SET ismanaged='D' WHERE nodeid=1");
        assertEquals(15, getNetworkElementFactory().getAllManagedIpInterfaces(false).length);
        
        Interface[] intfs = getNetworkElementFactory().getAllManagedIpInterfaces(false);
        assertEquals(15, intfs.length);
        
        int count = 0;
        for(int i =0; i < intfs.length; i++) {
            if(intfs[i].getIfIndex() > 0) {
                count++;
            }
        }
        
        m_jdbcTemplate.update("UPDATE ipinterface SET ismanaged='M' WHERE nodeid=1");
        
        Interface[] intfs2 = getNetworkElementFactory().getAllManagedIpInterfaces(true);
        assertEquals(18, intfs2.length);
        
        count = 0;
        for(int j =0; j < intfs2.length; j++) {
            if(intfs2[j].getIfIndex() > 0) {
                count++;
            }
        }
        
        assertEquals(3, count);
    }
    
    @Test
    public void testGetNodesWithCategories() {
        Set<OnmsCategory> cats = new HashSet<OnmsCategory>();
        cats.add(m_dbPopulator.getCategoryDao().get(1));
        
        OnmsNode node1 = m_dbPopulator.getNode1();
        node1.setType("A");
        node1.setCategories(cats);
        
        m_dbPopulator.getNodeDao().saveOrUpdate(node1);
        
        List<String> categories = new ArrayList<String>();
        categories.add("Routers");
        
        
        assertEquals(1, getNetworkElementFactory().getNodesWithCategories(categories.toArray(new String[categories.size()]), false).length);
        assertEquals(1, getNetworkElementFactory().getNodesWithCategories(new TransactionTemplate(), categories.toArray(new String[categories.size()]), false).length);
    }
    
    
    
    private NetworkElementFactory getNetworkElementFactory() {
        return NetworkElementFactory.getInstance(m_appContext);
    }
    
    
}
