/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-linkd.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/applicationContext-linkd-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkdTest extends LinkdNetworkBuilder implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    @Autowired
    private LinkdConfig m_linkdConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        // MockLogAppender.setupLogging(true);
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate.SQL", "WARN");
        p.setProperty("log4j.logger.org.hibernate.cfg", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);

        super.setNodeDao(m_nodeDao);
        super.setSnmpInterfaceDao(m_snmpInterfaceDao);

        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("test.example.com").setForeignSource("linkd").setForeignId("1").setSysObjectId(".1.3.6.1.4.1.1724.81").setType("A");
        nb.addInterface("192.168.1.10").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("laptop").setForeignSource("linkd").setForeignId("laptop").setSysObjectId(".1.3.6.1.4.1.8072.3.2.255").setType("A");
        nb.addInterface("10.1.1.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(10).setIfType(6).setCollectionEnabled(true).setIfSpeed(1000000000).setPhysAddr("065568ae696c");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco7200a").setForeignSource("linkd").setForeignId("cisco7200a").setSysObjectId(".1.3.6.1.4.1.9.1.222").setType("A");
        nb.addInterface("10.1.1.1").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(1000000000).setPhysAddr("ca0497a80038");
        nb.addInterface("10.1.2.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("ca0497a8001c");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco7200b").setForeignSource("linkd").setForeignId("cisco7200b").setSysObjectId(".1.3.6.1.4.1.9.1.222").setType("A");
        nb.addInterface("10.1.2.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(4).setIfType(6).setCollectionEnabled(true).setIfSpeed(10000000).setPhysAddr("ca0597a80038");
        nb.addInterface("10.1.3.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("ca0597a8001c");
        nb.addInterface("10.1.4.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("ca0597a80000");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco3700").setForeignSource("linkd").setForeignId("cisco3700").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.3.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(10000000).setPhysAddr("c20197a50000");
        nb.addInterface("10.1.6.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(3).setIfType(6).setCollectionEnabled(false).setIfSpeed(1000000000).setPhysAddr("c20197a50001");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco2691").setForeignSource("linkd").setForeignId("cisco2691").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.4.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(4).setIfType(6).setCollectionEnabled(false).setIfSpeed(10000000).setPhysAddr("c00397a70001");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("c00397a70000");
        nb.addInterface("10.1.7.1").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("c00397a70010");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco1700").setForeignSource("linkd").setForeignId("cisco1700").setSysObjectId(".1.3.6.1.4.1.9.1.200").setType("A");
        nb.addInterface("10.1.5.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("d00297a60000");
        m_nodeDao.save(nb.getCurrentNode());

        /*
        nb.addNode("cisco1700b").setForeignSource("linkd").setForeignId("cisco1700b").setSysObjectId(".1.3.6.1.4.1.9.1.200").setType("A");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c00397a70000");
        m_nodeDao.save(nb.getCurrentNode());
         */

        nb.addNode("cisco3600").setForeignSource("linkd").setForeignId("cisco3600").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        nb.addInterface("10.1.6.2").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("cc0097a30000");
        nb.addInterface("10.1.7.2").setIsSnmpPrimary("S").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("cc0097a30010");
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();

        for (Package pkg : Collections.list(m_linkdConfig.enumeratePackage())) {
            pkg.setForceIpRouteDiscoveryOnEthernet(true);
        }
    }

    @After
    public void tearDown() throws Exception {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host="10.1.5.1", port=161, resource="classpath:linkd/cisco1700b.properties"),
        @JUnitSnmpAgent(host="10.1.5.2", port=161, resource="classpath:linkd/cisco1700.properties")
    })
    public void testSimpleConnection() throws Exception {
        m_nodeDao.delete(m_nodeDao.findByForeignId("linkd", "cisco2691"));
        m_nodeDao.flush();

        final NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("cisco1700b").setForeignSource("linkd").setForeignId("cisco1700b").setSysObjectId(".1.3.6.1.4.1.9.1.200").setType("A");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("P").setIsManaged("M")
            .addSnmpInterface(2).setIfType(6).setCollectionEnabled(false).setIfSpeed(100000000).setPhysAddr("c00397a70000");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        final OnmsNode cisco1700 = m_nodeDao.findByForeignId("linkd", "cisco1700");
        final OnmsNode cisco1700b = m_nodeDao.findByForeignId("linkd", "cisco1700b");

        LogUtils.debugf(this, "cisco1700  = %s", cisco1700);
        LogUtils.debugf(this, "cisco1700b = %s", cisco1700b);

        assertTrue(m_linkd.scheduleNodeCollection(cisco1700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco1700b.getId()));

        assertTrue(m_linkd.runSingleCollection(cisco1700.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco1700b.getId()));

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        assertEquals("we should have found 1 data link", 1, ifaces.size());
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host="10.1.1.1", port=161, resource="classpath:linkd/cisco7200a.properties"),
        @JUnitSnmpAgent(host="10.1.1.2", port=161, resource="classpath:linkd/laptop.properties"),
        @JUnitSnmpAgent(host="10.1.2.2", port=161, resource="classpath:linkd/cisco7200b.properties"),
        @JUnitSnmpAgent(host="10.1.3.2", port=161, resource="classpath:linkd/cisco3700.properties"),
        @JUnitSnmpAgent(host="10.1.4.2", port=161, resource="classpath:linkd/cisco2691.properties"),
        @JUnitSnmpAgent(host="10.1.5.2", port=161, resource="classpath:linkd/cisco1700.properties"),
        @JUnitSnmpAgent(host="10.1.6.2", port=161, resource="classpath:linkd/cisco3600.properties")
    })
    public void testFakeCiscoNetwork() throws Exception {
        final OnmsNode laptop = m_nodeDao.findByForeignId("linkd", "laptop");
        final OnmsNode cisco7200a = m_nodeDao.findByForeignId("linkd", "cisco7200a");
        final OnmsNode cisco7200b = m_nodeDao.findByForeignId("linkd", "cisco7200b");
        final OnmsNode cisco3700 = m_nodeDao.findByForeignId("linkd", "cisco3700");
        final OnmsNode cisco2691 = m_nodeDao.findByForeignId("linkd", "cisco2691");
        final OnmsNode cisco1700 = m_nodeDao.findByForeignId("linkd", "cisco1700");
        final OnmsNode cisco3600 = m_nodeDao.findByForeignId("linkd", "cisco3600");

        assertTrue(m_linkd.scheduleNodeCollection(laptop.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200a.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco7200b.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco2691.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco1700.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3600.getId()));

        assertTrue(m_linkd.runSingleCollection(laptop.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco7200a.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco7200b.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco3700.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco2691.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco1700.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco3600.getId()));

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 9 data links", 9, ifaces.size());
    }
    
    @Test
    public void testDiscoveryOspfGetSubNetAddress() throws Exception {
        DiscoveryLink discovery = m_linkd.getDiscoveryLink("example1");
        OspfNbrInterface ospfinterface = new OspfNbrInterface(InetAddress.getByName("192.168.9.1"));
        ospfinterface.setOspfNbrIpAddr(InetAddress.getByName("192.168.15.45"));

        ospfinterface.setOspfNbrNetMask(InetAddress.getByName("255.255.255.0"));        
        assertEquals(InetAddress.getByName("192.168.15.0"), discovery.getSubnetAddress(ospfinterface));
        
        ospfinterface.setOspfNbrNetMask(InetAddress.getByName("255.255.0.0"));
        assertEquals(InetAddress.getByName("192.168.0.0"), discovery.getSubnetAddress(ospfinterface));

        ospfinterface.setOspfNbrNetMask(InetAddress.getByName("255.255.255.252"));
        assertEquals(InetAddress.getByName("192.168.15.44"), discovery.getSubnetAddress(ospfinterface));

        ospfinterface.setOspfNbrNetMask(InetAddress.getByName("255.255.255.240"));
        assertEquals(InetAddress.getByName("192.168.15.32"), discovery.getSubnetAddress(ospfinterface));

    }
    
    @Test
    @Transactional
    public void testDefaultConfiguration() throws Exception {
        assertEquals(true,m_linkdConfig.useBridgeDiscovery());
        assertEquals(true,m_linkdConfig.useOspfDiscovery());
        assertEquals(true,m_linkdConfig.useIpRouteDiscovery());
        assertEquals(true,m_linkdConfig.useLldpDiscovery());
        assertEquals(true,m_linkdConfig.useCdpDiscovery());
        
        assertEquals(true,m_linkdConfig.saveRouteTable());
        assertEquals(true,m_linkdConfig.saveStpNodeTable());
        assertEquals(true,m_linkdConfig.saveStpInterfaceTable());
        
        assertEquals(true, m_linkdConfig.isVlanDiscoveryEnabled());


        assertEquals(false, m_linkdConfig.isAutoDiscoveryEnabled());
        assertEquals(false, m_linkdConfig.enableDiscoveryDownload());
        assertEquals(false, m_linkdConfig.forceIpRouteDiscoveryOnEthernet());

        assertEquals(false, m_linkdConfig.hasClassName(".1.3.6.1.4.1.2636.1.1.1.1.9"));
                
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.9.13.3.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.10.27.4.1.2.4"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.10.27.4.1.2.2"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.10.27.4.1.2.11"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.16.4.3.5"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.16.4.3.6"));

        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.8.43"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.8.61"));

        assertEquals("org.opennms.netmgt.linkd.snmp.RapidCityVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.45.3.61.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.RapidCityVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.45.3.35.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.RapidCityVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.45.3.53.1"));
        
        assertEquals("org.opennms.netmgt.linkd.snmp.IntelVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.343.5.1.5"));

        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.3"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.7"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.8"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.11"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.6"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.50"));

        
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.1.300"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.1.122"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.1.616"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.5.42"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.5.59"));

        assertEquals("org.opennms.netmgt.linkd.snmp.ExtremeNetworkVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.1916.2.11"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ExtremeNetworkVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.1916.2.14"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ExtremeNetworkVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.1916.2.63"));

        assertEquals("org.opennms.netmgt.linkd.snmp.IpCidrRouteTable", m_linkdConfig.getDefaultIpRouteClassName());
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.3224.1.51"));
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.9.1.569"));
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.9.5.42"));
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.8072.3.2.255"));

        final OnmsNode laptop = m_nodeDao.findByForeignId("linkd", "laptop");
        final OnmsNode cisco3600 = m_nodeDao.findByForeignId("linkd", "cisco3600");
        
        assertTrue(m_linkd.scheduleNodeCollection(laptop.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3600.getId()));

        SnmpCollection snmpCollLaptop = m_linkd.getSnmpCollection(laptop.getId(), laptop.getPrimaryInterface().getIpAddress(), laptop.getSysObjectId(), "example1");
        assertEquals(true, snmpCollLaptop.getCollectBridge());
        assertEquals(true, snmpCollLaptop.getCollectStp());
        assertEquals(true, snmpCollLaptop.getCollectCdp());
        assertEquals(true, snmpCollLaptop.getCollectIpRoute());
        assertEquals(true, snmpCollLaptop.getCollectOspfTable());
        assertEquals(true, snmpCollLaptop.getCollectLldpTable());

        assertEquals(false, snmpCollLaptop.collectVlanTable());
        
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", snmpCollLaptop.getIpRouteClass());
        assertEquals("example1", snmpCollLaptop.getPackageName());
        assertEquals(true, snmpCollLaptop.getSaveIpRouteTable());
        assertEquals(true, snmpCollLaptop.getSaveStpNodeTable());
        assertEquals(true, snmpCollLaptop.getSaveStpInterfaceTable());

        SnmpCollection snmpCollcisco3600 = m_linkd.getSnmpCollection(cisco3600.getId(), cisco3600.getPrimaryInterface().getIpAddress(), cisco3600.getSysObjectId(), "example1");

        assertEquals(true, snmpCollcisco3600.getCollectBridge());
        assertEquals(true, snmpCollcisco3600.getCollectStp());
        assertEquals(true, snmpCollcisco3600.getCollectCdp());
        assertEquals(true, snmpCollcisco3600.getCollectIpRoute());
        assertEquals(true, snmpCollcisco3600.getCollectOspfTable());
        assertEquals(true, snmpCollcisco3600.getCollectLldpTable());

        assertEquals(true, snmpCollcisco3600.collectVlanTable());
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", snmpCollcisco3600.getVlanClass());
        
        assertEquals("org.opennms.netmgt.linkd.snmp.IpCidrRouteTable", snmpCollcisco3600.getIpRouteClass());
        assertEquals("example1", snmpCollcisco3600.getPackageName());
        
        assertEquals(true, snmpCollcisco3600.getSaveIpRouteTable());
        assertEquals(true, snmpCollcisco3600.getSaveStpNodeTable());
        assertEquals(true, snmpCollcisco3600.getSaveStpInterfaceTable());


        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(true, example1.getForceIpRouteDiscoveryOnEthernet());
        
        final Enumeration<Package> pkgs = m_linkdConfig.enumeratePackage();
        example1 = pkgs.nextElement();
        assertEquals("example1", example1.getName());
        assertEquals(false, pkgs.hasMoreElements());
    }
}
