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
import java.util.Collection;
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
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
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
public class LinkdNms17216Test extends LinkdNms17216NetworkBuilder implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

    @Autowired
    private LinkdConfig m_linkdConfig;

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

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
        p.setProperty("log4j.logger.org.hibernate.cfg", "WARN");
        p.setProperty("log4j.logger.org.springframework","WARN");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "WARN");
        MockLogAppender.setupLogging(p);

        super.setNodeDao(m_nodeDao);
        super.setSnmpInterfaceDao(m_snmpInterfaceDao);
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
     * Here we add cdp discovery and all test lab devices
     * To the previuos links discovered by lldp
     * should be added the followings discovered with cdp:
     * switch3 Fa0/23 Fa0/24 ---> switch5 Fa0/1 Fa0/9
     * router1 Fa0/0 ----> switch1 Gi0/1
     * router2 Serial0/0/0 ----> router1 Serial0/0/0
     * router3 Serial0/0/1 ----> router2 Serial0/0/1
     * router4 GigabitEthernet0/1 ----> router3   GigabitEthernet0/0
     * switch4 FastEthernet0/1    ----> router3   GigabitEthernet0/1
     * 
     * here are the corresponding ifindex:
     * switch1 Gi0/1 -->  10101
     * 
     * switch3 Fa0/23 -->  10023
     * switch3 Fa0/24 -->  10024
     *
     * switch5 Fa0/1 -->  10001
     * switch5 Fa0/13 -->  10013
     * 
     * router1 Fa0/0 -->  7
     * router1 Serial0/0/0 --> 13
     * router1 Serial0/0/1 --> 14
     * 
     * router2 Serial0/0/0 --> 12
     * router2 Serial0/0/1 --> 13
     * 
     * router3 Serial0/0/1 --> 13
     * router3 GigabitEthernet0/0 --> 8
     * router3 GigabitEthernet0/1 --> 9
     * 
     * router4 GigabitEthernet0/1  --> 3
     * 
     * switch4 FastEthernet0/1 --> 10001
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH1_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH2_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH3_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH4_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH5_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource="classpath:linkd/nms17216/"+ROUTER1_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource="classpath:linkd/nms17216/"+ROUTER2_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/"+ROUTER3_NAME+"-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource="classpath:linkd/nms17216/"+ROUTER4_NAME+"-walk.txt")
    })
    public void testNetwork17216Links() throws Exception {
        
        m_nodeDao.save(getSwitch1());
        m_nodeDao.save(getSwitch2());
        m_nodeDao.save(getSwitch3());
        m_nodeDao.save(getSwitch4());
        m_nodeDao.save(getSwitch5());
        m_nodeDao.save(getRouter1());
        m_nodeDao.save(getRouter2());
        m_nodeDao.save(getRouter3());
        m_nodeDao.save(getRouter4());

        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseLldpDiscovery(true);
        
        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode switch5 = m_nodeDao.findByForeignId("linkd", SWITCH5_NAME);
        final OnmsNode router1 = m_nodeDao.findByForeignId("linkd", ROUTER1_NAME);
        final OnmsNode router2 = m_nodeDao.findByForeignId("linkd", ROUTER2_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        final OnmsNode router4 = m_nodeDao.findByForeignId("linkd", ROUTER4_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch5.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router4.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router4.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());

        HibernateEventWriter query = (HibernateEventWriter)m_linkd.getQueryManager();
        
        List<Integer> nodeids = query.getNodeidFromIp(null, InetAddress.getByName("172.16.50.2"));
        
        assertEquals(1, nodeids.size());
        assertEquals(switch4.getId(),nodeids.get(0));

        nodeids = query.getNodeidFromIp(null, InetAddress.getByName("172.16.50.1"));
        assertEquals(1, nodeids.size());
        assertEquals(router3.getId(),nodeids.get(0));

        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(9, nodes.size());
        
        for (LinkableNode node: nodes) {
            switch(node.getNodeId()) {
                case 1: assertEquals(5, node.getCdpInterfaces().size());
                break;
                case 2: assertEquals(6, node.getCdpInterfaces().size());
                break;
                case 3: assertEquals(4, node.getCdpInterfaces().size());
                break;
                case 4: assertEquals(1, node.getCdpInterfaces().size());
                break;
                case 5: assertEquals(2, node.getCdpInterfaces().size());
                break;
                case 6: assertEquals(2, node.getCdpInterfaces().size());
                break;
                case 7: assertEquals(2, node.getCdpInterfaces().size());
                break;
                case 8: assertEquals(3, node.getCdpInterfaces().size());
                break;
                case 9: assertEquals(1, node.getCdpInterfaces().size());
                break;
                default: assertEquals(-1, node.getNodeId());
                break;
            }
        }        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(13,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();

        int start=getStartPoint(datalinkinterfaces);

        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                // switch1 gi0/9 -> switch2 gi0/1 --lldp --cdp
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
            } else if (linkid == start+1 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
            } else if (linkid == start+2) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
            } else if (linkid == start+3) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
            } else if (linkid == start+4) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
            } else if (linkid == start+5) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
            } else if (linkid == start+6) {
                // switch1 gi0/1 -> router1 Fa0/20 --cdp
                checkLink(router1, switch1, 7, 10101, datalinkinterface);
            } else if (linkid == start+7) {
                // switch3 Fa0/1 -> switch5 Fa0/23 --cdp
                checkLink(switch5, switch3, 10001, 10023, datalinkinterface);
            } else if (linkid == start+8) {
                // switch3 gi0/1 -> switch5 Fa0/20 --cdp
                checkLink(switch5, switch3, 10013, 10024, datalinkinterface);
            } else if (linkid == start+9) {
                //switch4 FastEthernet0/1    ----> router3   GigabitEthernet0/1
                checkLink(router3, switch4, 9, 10001, datalinkinterface);
            } else if (linkid == start+10) {
                checkLink(router2, router1, 12, 13, datalinkinterface);
            } else if (linkid == start+11) {
                checkLink(router3, router2, 13, 13, datalinkinterface);
            } else if (linkid == start+12) {
                checkLink(router4, router3, 3, 8, datalinkinterface);
            } else {
                // error
                checkLink(switch1,switch1,-1,-1,datalinkinterface);
            }      
        }
    }

}
