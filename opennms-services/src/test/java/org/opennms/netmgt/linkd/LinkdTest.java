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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
        "classpath:/applicationContext-minimal-conf.xml",
        "classpath:/applicationContext-linkd-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkdTest implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

    @Autowired
    private NodeDao m_nodeDao;

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
        MockLogAppender.setupLogging(p);

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
    @Ignore
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
        assertEquals("we should have found 2 data link", 2, ifaces.size());
    }

    @Test
    @Ignore
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
        assertEquals("we should have found 6 data links", 6, ifaces.size());
    }
}
