/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.linkd.Nms17216NetworkBuilder;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.opennms.netmgt.enlinkd.EnLinkdTestHelper.printLldpTopology;
import static org.opennms.netmgt.enlinkd.EnLinkdTestHelper.printLldpElement;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/applicationContext-enhancedLinkDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/applicationContext-enhancedLinkdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class Nms17216Test extends Nms17216NetworkBuilder implements InitializingBean {

    @Autowired
    private EnhancedLinkd m_linkd;

    @Autowired
    private EnhancedLinkdConfig m_linkdConfig;

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private LldpLinkDao m_topologyDao;
        
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate.SQL", "WARN");
        p.setProperty("log4j.logger.org.hibernate.cfg", "WARN");
        p.setProperty("log4j.logger.org.hibernate.impl", "WARN");
        p.setProperty("log4j.logger.org.hibernate.hql", "WARN");
        p.setProperty("log4j.logger.org.opennms.mock.snmp","WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt.linkd.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt.snmp", "WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt.filter", "WARN");
        p.setProperty("log4j.logger.org.hibernate", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);
    }

    @After
    public void tearDown() throws Exception {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();
    }
    
    /*
     * These are the links among the following nodes discovered using 
     * only the lldp protocol
     * switch1 Gi0/9 Gi0/10 Gi0/11 Gi0/12 ----> switch2 Gi0/1 Gi0/2 Gi0/3 Gi0/4
     * switch2 Gi0/19 Gi0/20              ----> switch3 Fa0/19 Fa0/20
     * 
     * here are the corresponding ifindex:
     * switch1 Gi0/9 --> 10109
     * switch1 Gi0/10 --> 10110
     * switch1 Gi0/11 --> 10111
     * switch1 Gi0/12 --> 10112
     * 
     * switch2 Gi0/1 --> 10101
     * switch2 Gi0/2 --> 10102
     * switch2 Gi0/3 --> 10103
     * switch2 Gi0/4 --> 10104
     * switch2 Gi0/19 --> 10119
     * switch2 Gi0/20 --> 10120
     * 
     * switch3 Fa0/19 -->  10019
     * switch3 Fa0/20 -->  10020
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
    })
    public void testNetwork17216LldpLinks() throws Exception {
        m_nodeDao.save(getSwitch1());
        m_nodeDao.save(getSwitch2());
        m_nodeDao.save(getSwitch3());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
 
        assertEquals(0,m_topologyDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        final List<LldpLink> topologyA = m_topologyDao.findAll();
        printLldpTopology(topologyA);
        assertEquals(4,topologyA.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }
        
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        final List<LldpLink> topologyB = m_topologyDao.findAll();
        printLldpTopology(topologyB);
        assertEquals(10,topologyA.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }
       
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        final List<LldpLink> topologyC = m_topologyDao.findAll();
        printLldpTopology(topologyC);
        assertEquals(12,topologyA.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }

    }
/*
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/router3-walk.txt")
    })
    public void testNetwork17216Switch4Router4CdpLinks() throws Exception {
        
        m_nodeDao.save(getSwitch4());
        m_nodeDao.save(getRouter3());

        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(true);

        
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router3.getId()));

        final List<TopologyElement> topologyA = m_topologyDao.getTopology();
        List<EndPoint> endpoints = printEndPointTopology(topologyA);
        List<Link> links = printLinkTopology(topologyA);
        assertEquals(0,topologyA.size());
        assertEquals(0, endpoints.size());
        assertEquals(0, links.size());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        endpoints = printEndPointTopology(topologyA);
        links = printLinkTopology(topologyA);
        assertEquals(2,topologyA.size());
        assertEquals(2, endpoints.size());
        assertEquals(1, links.size());

        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
        endpoints = printEndPointTopology(topologyA);
        links = printLinkTopology(topologyA);
        assertEquals(4,topologyA.size());
        assertEquals(6, endpoints.size());
        assertEquals(3, links.size());
        
    }
    */
}
