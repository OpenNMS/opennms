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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

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
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.LinkdConfigFactory;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.VlanDao;
import org.opennms.netmgt.linkd.nb.Nms0002NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-linkd.xml",
        "classpath:/META-INF/opennms/applicationContext-linkdTest.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@JUnitTemporaryDatabase
public class Nms0002Test extends Nms0002NetworkBuilder implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

    private LinkdConfig m_linkdConfig;

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
        
    @Autowired
    private AtInterfaceDao m_atInterfaceDao;

    @Autowired
    private VlanDao m_vlanDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate.SQL", "WARN");
        p.setProperty("log4j.logger.org.hibernate.cfg", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);

        super.setNodeDao(m_nodeDao);
        super.setSnmpInterfaceDao(m_snmpInterfaceDao);
    }

    @Before
    public void setUpLinkdConfiguration() throws Exception {
        LinkdConfigFactory.init();
        final Resource config = new ClassPathResource("etc/linkd-configuration.xml");
        final LinkdConfigFactory factory = new LinkdConfigFactory(-1L, config.getInputStream());
        LinkdConfigFactory.setInstance(factory);
        m_linkdConfig = LinkdConfigFactory.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();
    }
    /*
     *
     *     nodelabel           |  snmpifname  | ifindex |       parent       |  parentif  | parentifindex 
     *     --------------------+--------------+---------+--------------------+------------+---------------
     *      r-de-pots-amka-001 | Fa6/0/0      |     164 | r-de-juet-luck-001 | ge-0/0/0.0 |           510
     *      r-de-juet-luck-001 | ge-0/0/2.100 |     549 | s-de-juet-luck-001 | Fa0/1      |         10001
     *      r-de-juet-luck-001 | ge-0/0/2.950 |     550 | s-de-juet-luck-001 | Fa0/1      |         10001     *  
     * 
     * The links are mostly from the bridge forwarding table protocol in fact the 
     * ge-0/0/2.100 and ge-0/0/2.950 are logical interface for vlan 100 and
     * vlan 950 on juniper router. with the same mac address 54e032ef3102
     * So both vlan 100 and vlan 950 will address the same link
     * No way to get layer 2 link because no STP is enabled on Juniper device
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt"),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".txt")
    })
    public void testNetworkLinksCiscoJuniperLldp() throws Exception {
        
        m_nodeDao.save(getRluck001());
        m_nodeDao.save(getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(true);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(false);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
       
        for ( LinkableNode linkableNode: m_linkd.getLinkableNodesOnPackage("example1")) {
            assertNotNull(linkableNode.getLldpChassisId());
        }

                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".txt")
    })
    public void testNetworkLinksCiscoJuniperVlan() throws Exception {
        
        m_nodeDao.save(getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(false);

        example1.setEnableVlanDiscovery(true);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
       
        assertEquals(7, m_vlanDao.countAll());
                        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt"),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".txt")
    })
    public void testNetworkLinksCiscoJuniperVlan1() throws Exception {
        
        m_nodeDao.save(getRluck001());
        m_nodeDao.save(getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(true);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt"),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".vlan100.txt")
    })
    public void testNetworkLinksCiscoJuniperVlan100() throws Exception {
        
        m_nodeDao.save(getRluck001());
        m_nodeDao.save(getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(true);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(2,m_dataLinkInterfaceDao.countAll());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt"),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".vlan950.txt")
    })
    public void testNetworkLinksCiscoJuniperVlan950() throws Exception {
        
        m_nodeDao.save(getRluck001());
        m_nodeDao.save(getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(true);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(2,m_dataLinkInterfaceDao.countAll());
    }

    /*
     * fixed a fake link found using cdp:
     * caused by duplicated ip address
s     * r-uk-nott-newt-103:Fa0:(1)<------>(4):Fa3:r-ro-suce-pict-001
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = RPict001_IP, port = 161, resource = "classpath:linkd/nms0002UkRoFakeLink/" + RPict001_NAME+".txt"),
            @JUnitSnmpAgent(host = RNewt103_IP, port = 161, resource = "classpath:linkd/nms0002UkRoFakeLink/" + RNewt103_NAME+".txt")
    })
    public void testCdpFakeLinkRoUk() throws Exception {
        
        m_nodeDao.save(getRPict001());
        m_nodeDao.save(getRNewt103());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(true);
        example1.setUseBridgeDiscovery(false);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerRo = m_nodeDao.findByForeignId("linkd", RPict001_NAME);
        final OnmsNode routerUk = m_nodeDao.findByForeignId("linkd", RNewt103_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerRo.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(routerUk.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerRo.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(routerUk.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
    }
        
}
