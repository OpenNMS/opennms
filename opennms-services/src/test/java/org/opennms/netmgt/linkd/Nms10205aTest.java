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
import org.opennms.netmgt.linkd.nb.Nms10205aNetworkBuilder;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-linkdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class Nms10205aTest extends Nms10205aNetworkBuilder implements InitializingBean {

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
     *  The 
     *  MUMBAI:port ge 0/1/3:ip 192.168.5.5   ------> CHENNAI:port ge 4/0/2: ip 192.168.5.6
     *  MUMBAI:port ge 0/1/2:ip 192.168.5.9   ------> DELHI:port ge 1/0/2: ip 192.168.5.10
     *  MUMBAI:port ge 0/0/1:ip 192.168.5.13   ------> BANGALORE:port ge 0/0/0: ip 192.168.5.14
     *  DELHI:port ge 1/0/1:ip 192.168.1.5     ------> BANGALORE:port ge 0/0/1: ip 192.168.1.6
     *  DELHI:port ge 1/1/6:ip 172.16.7.1     ------> Space-EX-SW1: port 0/0/6: ip 172.16.7.1 ???? same ip address
     *  CHENNAI:port ge 4/0/3:ip 192.168.1.1  ------> DELHI: port ge 1/1/0: ip 192.168.1.2
     *  
     *  a lot of duplicated ip this is a clear proof that linkd is not able to 
     *  gather topology of this lab using the useBridgeTopology and ip routes.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205/"+MUMBAI_NAME+"_"+MUMBAI_IP+".txt"),
            @JUnitSnmpAgent(host=CHENNAI_IP, port=161, resource="classpath:linkd/nms10205/"+CHENNAI_NAME+"_"+CHENNAI_IP+".txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205/"+DELHI_NAME+"_"+DELHI_IP+".txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205/"+BANGALORE_NAME+"_"+BANGALORE_IP+".txt"),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource="classpath:linkd/nms10205/"+BAGMANE_NAME+"_"+BAGMANE_IP+".txt"),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource="classpath:linkd/nms10205/"+MYSORE_NAME+"_"+MYSORE_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205/"+SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource="classpath:linkd/nms10205/"+SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_41_IP, port=161, resource="classpath:linkd/nms10205/"+J6350_41_NAME+"_"+J6350_41_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource="classpath:linkd/nms10205/"+"J6350-42_"+J6350_42_IP+".txt"),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource="classpath:linkd/nms10205/"+"SRX-100_"+SRX_100_IP+".txt"),
            @JUnitSnmpAgent(host=SSG550_IP, port=161, resource="classpath:linkd/nms10205/"+SSG550_NAME+"_"+SSG550_IP+".txt")
    })
    public void testNetwork10205Links() throws Exception {
        m_nodeDao.save(getMumbai());
        m_nodeDao.save(getChennai());
        m_nodeDao.save(getDelhi());
        m_nodeDao.save(getBangalore());
        m_nodeDao.save(getBagmane());
        m_nodeDao.save(getMysore());
        m_nodeDao.save(getSpaceExSw1());
        m_nodeDao.save(getSpaceExSw2());
        m_nodeDao.save(getJ635041());
        m_nodeDao.save(getJ635042());
        m_nodeDao.save(getSRX100());
        m_nodeDao.save(getSGG550());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        example1.setUseCdpDiscovery(false);
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode chennai = m_nodeDao.findByForeignId("linkd", CHENNAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635041 = m_nodeDao.findByForeignId("linkd", J6350_41_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);
        final OnmsNode ssg550 = m_nodeDao.findByForeignId("linkd", SSG550_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(chennai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635041.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ssg550.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(chennai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635041.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ssg550.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        
        assertEquals(9, links.size());
        
        // Linkd is able to find partially the topology using the next hop router
        // among the core nodes:
        // mumbai, chennai, delhi, mysore,bangalore and bagmane
        // the link between chennai and delhi is lost 
        // the link between chennai and bagmane is lost
        // the link between bagmane and delhi is lost
        // I checked the walks and no route info
        // is there for discovering the link.
        // I have to guess that linkd is working as expected
        
        // The bridge and RSTP topology information are
        // unusuful, the devices supporting RSTP
        // have themselves as designated bridge.
        
        // Other links are lost...
        // no routing entry and no bridge 
        // forwarding
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            if (start == id ) {
                checkLink(delhi, mumbai, 28503, 519, datalinkinterface);
            } else if (start+1 == id ) {
                checkLink(bangalore, mumbai, 2401, 507, datalinkinterface);
            } else if (start+2 == id ) {
                checkLink(bagmane, mumbai, 534, 977, datalinkinterface);
            } else if (start+3 == id ) {
                checkLink(mysore, mumbai, 508, 978, datalinkinterface);
            } else if (start+4 == id ) {
                checkLink(chennai, mumbai, 528, 520, datalinkinterface);
            } else if (start+5 == id ) {
                checkLink(mysore, chennai, 505, 517, datalinkinterface);
            } else if (start+6 == id ) {
               checkLink(bangalore, delhi, 2397, 3674, datalinkinterface);
            } else if (start+7 == id ) {
                checkLink(bagmane, bangalore, 1732, 2396, datalinkinterface);
            } else if (start+8 == id ) {
                checkLink(mysore, bagmane, 520, 654, datalinkinterface);
            } else {
                checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            }
        }
    }
    
    
    /*
     *  
     *  Get only ospf links.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205/"+MUMBAI_NAME+"_"+MUMBAI_IP+".txt"),
            @JUnitSnmpAgent(host=CHENNAI_IP, port=161, resource="classpath:linkd/nms10205/"+CHENNAI_NAME+"_"+CHENNAI_IP+".txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205/"+DELHI_NAME+"_"+DELHI_IP+".txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205/"+BANGALORE_NAME+"_"+BANGALORE_IP+".txt"),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource="classpath:linkd/nms10205/"+BAGMANE_NAME+"_"+BAGMANE_IP+".txt"),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource="classpath:linkd/nms10205/"+MYSORE_NAME+"_"+MYSORE_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205/"+SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource="classpath:linkd/nms10205/"+SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_41_IP, port=161, resource="classpath:linkd/nms10205/"+J6350_41_NAME+"_"+J6350_41_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource="classpath:linkd/nms10205/"+"J6350-42_"+J6350_42_IP+".txt"),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource="classpath:linkd/nms10205/"+"SRX-100_"+SRX_100_IP+".txt"),
            @JUnitSnmpAgent(host=SSG550_IP, port=161, resource="classpath:linkd/nms10205/"+SSG550_NAME+"_"+SSG550_IP+".txt")
    })
    public void testNetwork10205OspfLinks() throws Exception {
        m_nodeDao.save(getMumbai());
        m_nodeDao.save(getChennai());
        m_nodeDao.save(getDelhi());
        m_nodeDao.save(getBangalore());
        m_nodeDao.save(getBagmane());
        m_nodeDao.save(getMysore());
        m_nodeDao.save(getSpaceExSw1());
        m_nodeDao.save(getSpaceExSw2());
        m_nodeDao.save(getJ635041());
        m_nodeDao.save(getJ635042());
        m_nodeDao.save(getSRX100());
        m_nodeDao.save(getSGG550());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        
        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode chennai = m_nodeDao.findByForeignId("linkd", CHENNAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635041 = m_nodeDao.findByForeignId("linkd", J6350_41_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);
        final OnmsNode ssg550 = m_nodeDao.findByForeignId("linkd", SSG550_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(chennai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635041.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ssg550.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(chennai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635041.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ssg550.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        
        assertEquals(9, links.size());  
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            if (start == id ) {
                checkLink(chennai, mumbai, 528, 520, datalinkinterface);
            } else if (start+1 == id ) {
                checkLink(delhi, mumbai, 28503, 519, datalinkinterface);
            } else if (start+2 == id ) {
                checkLink(bangalore, mumbai, 2401, 507, datalinkinterface);
            } else if (start+3 == id ) {
                checkLink(bagmane, mumbai, 534, 977, datalinkinterface);
            } else if (start+4 == id ) {
                checkLink(mysore, mumbai, 508, 978, datalinkinterface);
            } else if (start+5 == id ) {
                checkLink(mysore, chennai, 505, 517, datalinkinterface);
            } else if (start+6 == id ) {
               checkLink(bangalore, delhi, 2397, 3674, datalinkinterface);
            } else if (start+7 == id ) {
                checkLink(bagmane, bangalore, 1732, 2396, datalinkinterface);
            } else if (start+8 == id ) {
                checkLink(mysore, bagmane, 520, 654, datalinkinterface);
            } else {
                checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            }
        }

    }


}
