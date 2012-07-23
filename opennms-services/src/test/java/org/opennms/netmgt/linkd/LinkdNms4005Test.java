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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
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
        "classpath:/applicationContext-linkd-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkdNms4005Test implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

    @Autowired
    private LinkdConfig m_linkdConfig;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate.SQL", "WARN");
        MockLogAppender.setupLogging(p);

        NetworkBuilder nb = new NetworkBuilder();

        nb.addNode("cisco1").setForeignSource("linkd").setForeignId("cisco1").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        //nb.addInterface("10.1.1.1").setIsSnmpPrimary("P").setIsManaged("M")
        //.addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("66933c17555c");
        nb.addInterface("10.1.1.2").setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90010");
        nb.addInterface("10.1.2.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90000");
        //nb.addInterface("10.1.2.2").setIsSnmpPrimary("S").setIsManaged("M")
        //.addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90000");
        nb.addInterface("10.1.3.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90001");
        //nb.addInterface("10.1.3.2").setIsSnmpPrimary("S").setIsManaged("M")
        //.addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2027db90000");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco2").setForeignSource("linkd").setForeignId("cisco2").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        //nb.addInterface("10.1.2.1").setIsSnmpPrimary("P").setIsManaged("M")
        //.addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90000");
        nb.addInterface("10.1.2.2").setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90000");
        nb.addInterface("10.1.5.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90001");
        //nb.addInterface("10.1.5.2").setIsSnmpPrimary("S").setIsManaged("M")
        //.addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2027db90010");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco3").setForeignSource("linkd").setForeignId("cisco3").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType("A");
        //nb.addInterface("10.1.1.1").setIsSnmpPrimary("P").setIsManaged("M")
        //.addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90001");
        //nb.addInterface("10.1.3.1").setIsSnmpPrimary("S").setIsManaged("M")
        //.addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90001");
        nb.addInterface("10.1.3.2").setIsSnmpPrimary("P").setIsManaged("M")
        .addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2027db90000");
        nb.addInterface("10.1.4.1").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2027db90001");
        //nb.addInterface("10.1.4.2").setIsSnmpPrimary("S").setIsManaged("M")
        //.addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2037db90000");
        //nb.addInterface("10.1.5.1").setIsSnmpPrimary("S").setIsManaged("M")
        //.addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90001");
        nb.addInterface("10.1.5.2").setIsSnmpPrimary("S").setIsManaged("M")
        .addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2027db90010");
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
            @JUnitSnmpAgent(host="10.1.1.2", port=161, resource="classpath:linkd/10.1.1.2-walk.txt"),
            @JUnitSnmpAgent(host="10.1.2.2", port=161, resource="classpath:linkd/10.1.2.2-walk.txt"),
            @JUnitSnmpAgent(host="10.1.3.2", port=161, resource="classpath:linkd/10.1.3.2-walk.txt"),
            @JUnitSnmpAgent(host="10.1.4.2", port=161, resource="classpath:linkd/10.1.4.2-walk.txt")
    })
    public void testNms4005Network() throws Exception {
        final OnmsNode cisco1 = m_nodeDao.findByForeignId("linkd", "cisco1");
        final OnmsNode cisco2 = m_nodeDao.findByForeignId("linkd", "cisco2");
        final OnmsNode cisco3 = m_nodeDao.findByForeignId("linkd", "cisco3");

        assertTrue(m_linkd.scheduleNodeCollection(cisco1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3.getId()));

        assertTrue(m_linkd.runSingleCollection(cisco1.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco2.getId()));
        assertTrue(m_linkd.runSingleCollection(cisco3.getId()));

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        assertEquals("we should have found 3 data links", 3, ifaces.size());
    }

    /**
     * This test is the same as {@link #testNms4005Network()} except that it spawns multiple threads
     * for each scan to ensure that the upsert code is working properly.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host="10.1.1.2", port=161, resource="classpath:linkd/10.1.1.2-walk.txt"),
            @JUnitSnmpAgent(host="10.1.2.2", port=161, resource="classpath:linkd/10.1.2.2-walk.txt"),
            @JUnitSnmpAgent(host="10.1.3.2", port=161, resource="classpath:linkd/10.1.3.2-walk.txt"),
            @JUnitSnmpAgent(host="10.1.4.2", port=161, resource="classpath:linkd/10.1.4.2-walk.txt")
    })
    public void testNms4005NetworkWithThreads() throws Exception {
        final OnmsNode cisco1 = m_nodeDao.findByForeignId("linkd", "cisco1");
        final OnmsNode cisco2 = m_nodeDao.findByForeignId("linkd", "cisco2");
        final OnmsNode cisco3 = m_nodeDao.findByForeignId("linkd", "cisco3");

        assertTrue(m_linkd.scheduleNodeCollection(cisco1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3.getId()));

        final int NUMBER_OF_THREADS = 20;

        List<Thread> waitForMe = new ArrayList<Thread>();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread("NMS-4005-Test-Thread-" + i) {
                public void run() {
                    assertTrue(m_linkd.runSingleCollection(cisco1.getId()));
                }
            };
            thread.start();
            waitForMe.add(thread);
        }
        for (Thread thread : waitForMe) {
            thread.join();
        }
        waitForMe.clear();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread("NMS-4005-Test-Thread-" + i) {
                public void run() {
                    assertTrue(m_linkd.runSingleCollection(cisco2.getId()));
                }
            };
            thread.start();
            waitForMe.add(thread);
        }
        for (Thread thread : waitForMe) {
            thread.join();
        }
        waitForMe.clear();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread("NMS-4005-Test-Thread-" + i) {
                public void run() {
                    assertTrue(m_linkd.runSingleCollection(cisco3.getId()));
                }
            };
            thread.start();
            waitForMe.add(thread);
        }
        for (Thread thread : waitForMe) {
            thread.join();
        }
        waitForMe.clear();

        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        assertEquals("we should have found 3 data links", 3, ifaces.size());
    }
}
