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
import org.opennms.netmgt.linkd.nb.Nms10205bNetworkBuilder;
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
public class Nms10205bTest extends Nms10205bNetworkBuilder implements InitializingBean {

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
     * 
MUMBAI_10.205.56.5: (LLDP is not supported on this device_family=m320)
===================
root@Mumbai> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.14     ge-0/0/1.0             Full      192.168.9.1      128    31 ---->Bangalore
192.168.5.18     ge-0/0/2.0             Full      192.168.20.1     128    34 ---->Bagmane
192.168.5.22     ge-0/1/1.0             Full      192.168.22.1     128    38 ---->Mysore
192.168.5.10     ge-0/1/2.0             Full      192.168.7.1      128    35 ---->Delhi

DELHI_10.205.56.7:
==================
admin@Delhi> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/1/6        00:23:9c:02:3b:40  ge-0/0/6.0   Space-EX-SW1
ge-1/1/5        80:71:1f:c7:0f:c0  ge-1/0/1     Bagmane
admin@Delhi> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.1.6      ge-1/0/1.0             Full      192.168.9.1      128    31  ---->Bangalore
192.168.5.9      ge-1/0/2.0             Full      192.168.5.1      128    39  ---->Mumbai
172.16.7.2       ge-1/1/6.0             Full      10.205.56.1      128    33  ---->Space_ex_sw1


BANGALORE_10.205.56.9: (LLDP is not supported on this device_family=m7i)
======================
root@Bangalore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.13     ge-0/0/0.0             Full      192.168.5.1      128    33  ---->Mumbai
192.168.1.5      ge-0/0/1.0             Full      192.168.7.1      128    32  ---->Delhi
172.16.9.2       ge-0/0/3.0             Full      10.205.56.2      128    34  ---->Space_ex_sw2
192.168.1.10     ge-0/1/0.0             Full      192.168.20.1     128    38  ---->Bagmane

Bagmane_10.205.56.20:
====================
admin@Bagmane> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/0/1        00:22:83:f1:67:c0  ge-1/1/5     Delhi
ge-1/0/3        00:26:88:6a:9a:80  ge-1/0/6.0   sw21
ge-1/0/2        2c:6b:f5:5d:c1:00  TO-BAMANE    J6350-2
admin@Bagmane> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.17     ge-1/0/0.0             Full      192.168.5.1      128    30 ----> Mumbai
172.16.20.2      ge-1/0/2.0             Full      10.205.56.42     128    31 ----> J6350_42
192.168.1.9      ge-1/0/4.0             Full      192.168.9.1      128    32 ----> Bangalore
192.168.1.14     ge-1/0/5.0             Full      192.168.22.1     128    33 ----> Mysore

Mysore_10.205.56.22:(LLDP is not supported on this device_family=m10i)
===================
admin@Mysore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.21     ge-0/0/1.0             Full      192.168.5.1      128    32 ----> Mumbai
192.168.1.13     ge-0/1/1.0             Full      192.168.20.1     128    38 ----> bagmane

Space-EX-SW1_10.205.56.1:
=========================
root@Space-EX-SW1> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info          System Name
ge-0/0/0.0         -                   00:21:59:cf:4c:00   ge-0/0/0.0         Space-EX-SW2
ge-0/0/6.0         -                   00:22:83:f1:67:c0   ge-1/1/6           Delhi
ge-0/0/4.0         -                   00:26:88:6a:9a:80   ge-2/0/33.0        sw21
root@Space-EX-SW1> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.2      ge-0/0/0.0             Full      10.205.56.2      128    34 ----> Space_ex_sw2
172.16.7.1       ge-0/0/6.0             Full      192.168.7.1      128    32 ----> Delhi

Space-EX-SW2_10.205.56.2: 
=========================
root@Space-EX-SW2> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info     System Name
ge-0/0/0.0         -                   00:23:9c:02:3b:40   TO-EX-SW1    Space-EX-SW1
me0.0              -                   00:26:88:6a:9a:80   ge-0/0/15.0  sw21
root@Space-EX-SW2> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.1      ge-0/0/0.0             Full      10.205.56.1      128    35 ----> Space_ex_sw1
172.16.9.1       ge-0/0/3.0             Full      192.168.9.1      128    32 ----> Bangalore

*/
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205b/"+MUMBAI_NAME+"_"+MUMBAI_IP+".txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205b/"+DELHI_NAME+"_"+DELHI_IP+".txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205b/"+BANGALORE_NAME+"_"+BANGALORE_IP+".txt"),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource="classpath:linkd/nms10205b/"+BAGMANE_NAME+"_"+BAGMANE_IP+".txt"),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource="classpath:linkd/nms10205b/"+MYSORE_NAME+"_"+MYSORE_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource="classpath:linkd/nms10205b/"+"J6350-42_"+J6350_42_IP+".txt"),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource="classpath:linkd/nms10205b/"+"SRX-100_"+SRX_100_IP+".txt")
    })
    public void testNetwork10205bLinks() throws Exception {
        m_nodeDao.save(getMumbai());
        m_nodeDao.save(getDelhi());
        m_nodeDao.save(getBangalore());
        m_nodeDao.save(getBagmane());
        m_nodeDao.save(getMysore());
        m_nodeDao.save(getSpaceExSw1());
        m_nodeDao.save(getSpaceExSw2());
        m_nodeDao.save(getJ635042());
        m_nodeDao.save(getSRX100());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        example1.setUseCdpDiscovery(false);
                
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(12, links.size());
        
        /*

                The topology layout:

Parentnode     ParentInterface                  Node            Interface               LinkdStrategy           id

Mumbai          ge-0/1/2.0      (519)  ----> Delhi             ge-1/0/2.0      (28503)  next hop router         800
Mumbai          ge-0/0/1.0      (507)  ----> Bangalore         ge-0/0/0.0      (2401)   next hop router         801
Mumbai          ge-0/0/2.0      (977)  ----> Bagmane           ge-1/0/0.0      (534)    next hop router         802
Mumbai          ge-0/1/1.0      (978)  ----> Mysore            ge-0/0/1.0      (508)    next hop router         803

Delhi           ge-1/0/1.0     (3674)  ----> Bangalore         ge-0/0/1.0      (2397)   next hop router         804
Delhi           ge-1/1/6.0     (17619) ----> Space_ex_sw1      ge-0/0/6.0      (528)    next hop router ****1   not saved
Delhi           ge-1/1/6       (28520) ----> Space-EX-SW1      ge-0/0/6.0      (528)    lldp            ****1   805
Delhi           ge-1/1/5       (28519) ----> Bagmane           ge-1/0/1        (513)    lldp                    811

Bangalore       ge-0/0/3.0     (2398)  ----> Space_ex_sw2      ge-0/0/3.0      (551)    next hop router         806
Bangalore       ge-0/1/0.0     (2396)  ----> Bagmane           ge-1/0/4.0      (1732)   next hop router         807

Bagmane         ge-1/0/5.0      (654)  ----> Mysore            ge-0/1/1.0      (520)    next hop router         808
Bagmane         ge-1/0/2        (514)  ----> J6350-2           ge-0/0/2.0      (549)    lldp            ****2   809
Bagmane         ge-1/0/2.0      (540)  ----> J6350_42          ge-0/0/2.0      (549)    next hop router ****2   not saved

Space-EX-SW1    ge-0/0/0.0      (1361)  ----> Space-EX-SW2     ge-0/0/0.0      (531)    lldp            ****3   810
Space_ex_sw1    ge-0/0/0.0      (1361)  ----> Space_ex_sw2     ge-0/0/0.0      (531)    next hop router ****3   810

        Here you clearly see 15 links but globally linkd saves only 12 nodes.
        The problem is that somewhere is stated that nodeid,ifindex must be unique.
        This means that the links with * are overwritten. because the iproute strategy follows the 
        lldp strategy then the route link is saved.
        
        Linkd is able to find the topology using the next hop router
        and lldp among the core nodes:
        mumbai, delhi, mysore,bangalore and bagmane
        
        Also is able to find the topology among the core nodes and the peripherals:
        space_ex_sw1, space_ex_sw2, j6350_42

        The bridge and RSTP topology information are
        unusuful, the devices supporting RSTP
        have themselves as designated bridge.
        
        But The link between Mysore and SRX-100 is lost        

         */
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            if (start == id) {
                checkLink(delhi, mumbai, 28503, 519, datalinkinterface);
            } else if (start+1 == id ) {
                checkLink(bangalore, mumbai, 2401, 507, datalinkinterface);
            } else if (start+2 == id ) {
            	checkLink(bagmane, mumbai, 534, 977, datalinkinterface);
            } else if (start+3 == id ) {
            	checkLink(mysore, mumbai, 508, 978, datalinkinterface);
            } else if (start+4 == id ) {
            	checkLink(bangalore, delhi, 2397, 3674, datalinkinterface);
            } else if (start+5 == id ) {
            	checkLink(spaceexsw1, delhi, 528, 28520, datalinkinterface);
            } else if (start+6 == id ) {
            	checkLink(spaceexsw2, bangalore, 551, 2398, datalinkinterface);
            } else if (start+7 == id ) {
            	checkLink(bagmane, bangalore, 1732, 2396, datalinkinterface);
            } else if (start+8 == id ) {
            	checkLink(mysore, bagmane, 520, 654, datalinkinterface);
            } else if (start+9 == id ) {
            	checkLink(j635042, bagmane, 549, 514, datalinkinterface);
            } else if (start+10 == id ) {
            	checkLink(spaceexsw2, spaceexsw1, 531, 1361, datalinkinterface);
            } else if (start+11 == id ) {
            	checkLink(bagmane, delhi, 513, 28519, datalinkinterface);
            } else  {
            	checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            }
        }
    }
    
    /*
     * 

MUMBAI_10.205.56.5: (LLDP is not supported on this device_family=m320)
===================

DELHI_10.205.56.7:
==================
admin@Delhi> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/1/6        00:23:9c:02:3b:40  ge-0/0/6.0   Space-EX-SW1
ge-1/1/5        80:71:1f:c7:0f:c0  ge-1/0/1     Bagmane

BANGALORE_10.205.56.9: (LLDP is not supported on this device_family=m7i)
======================

Bagmane_10.205.56.20:
====================
admin@Bagmane> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/0/1        00:22:83:f1:67:c0  ge-1/1/5     Delhi
ge-1/0/3        00:26:88:6a:9a:80  ge-1/0/6.0   sw21
ge-1/0/2        2c:6b:f5:5d:c1:00  TO-BAMANE    J6350-2

Mysore_10.205.56.22:(LLDP is not supported on this device_family=m10i)
===================

Space-EX-SW1_10.205.56.1:
=========================
root@Space-EX-SW1> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info          System Name
ge-0/0/0.0         -                   00:21:59:cf:4c:00   ge-0/0/0.0         Space-EX-SW2
ge-0/0/6.0         -                   00:22:83:f1:67:c0   ge-1/1/6           Delhi
ge-0/0/4.0         -                   00:26:88:6a:9a:80   ge-2/0/33.0        sw21

Space-EX-SW2_10.205.56.2: 
=========================
root@Space-EX-SW2> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info     System Name
ge-0/0/0.0         -                   00:23:9c:02:3b:40   TO-EX-SW1    Space-EX-SW1
me0.0              -                   00:26:88:6a:9a:80   ge-0/0/15.0  sw21

J6350-42_10.205.56.42:
=========================
does not support lldp rem table but
is linked to bagmane

SRX-100_10.205.56.23:
=========================
support LLDP 
it has a link to Mysore that does not support LLDP
 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205b/"+MUMBAI_NAME+"_"+MUMBAI_IP+".txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205b/"+DELHI_NAME+"_"+DELHI_IP+".txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205b/"+BANGALORE_NAME+"_"+BANGALORE_IP+".txt"),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource="classpath:linkd/nms10205b/"+BAGMANE_NAME+"_"+BAGMANE_IP+".txt"),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource="classpath:linkd/nms10205b/"+MYSORE_NAME+"_"+MYSORE_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource="classpath:linkd/nms10205b/"+"J6350-42_"+J6350_42_IP+".txt"),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource="classpath:linkd/nms10205b/"+"SRX-100_"+SRX_100_IP+".txt")
    })
    public void testNetwork10205bLldpLinks() throws Exception {
        m_nodeDao.save(getMumbai());
        m_nodeDao.save(getDelhi());
        m_nodeDao.save(getBangalore());
        m_nodeDao.save(getBagmane());
        m_nodeDao.save(getMysore());
        m_nodeDao.save(getSpaceExSw1());
        m_nodeDao.save(getSpaceExSw2());
        m_nodeDao.save(getJ635042());
        m_nodeDao.save(getSRX100());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);

        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        
        assertEquals(4, links.size());

        // Linkd is able to find all the lldp links
        // 
        
        //Delhi           ge-1/1/5        (28519) ----> Bagmane           ge-1/0/1        (513)   514     lldp
        //Delhi           ge-1/1/6        (28520) ----> Space-EX-SW1      ge-0/0/6.0      (528)   515     lldp
        //Bagmane         ge-1/0/2        (514)   ----> J6350-2           ge-0/0/2.0      (549)   516     lldp
        //Space-EX-SW1    ge-0/0/0.0      (1361)  ----> Space-EX-SW2      ge-0/0/0.0      (531)   517     lldp
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            if (start == id) {
            	checkLink(bagmane, delhi, 513, 28519, datalinkinterface);
            } else if (start+1 == id ) {
            	checkLink(spaceexsw1, delhi, 528, 28520, datalinkinterface);
            } else if (start+2 == id ) {
            	checkLink(j635042, bagmane, 549, 514, datalinkinterface);
            } else if (start+3 == id ) {
            	checkLink(spaceexsw2, spaceexsw1, 531, 1361, datalinkinterface);
            } else {
            	checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            }
        }
    }
    
    /*
     * 
MUMBAI_10.205.56.5: 
===================
root@Mumbai> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.14     ge-0/0/1.0             Full      192.168.9.1      128    31 ---->Bangalore
192.168.5.18     ge-0/0/2.0             Full      192.168.20.1     128    34 ---->Bagmane
192.168.5.22     ge-0/1/1.0             Full      192.168.22.1     128    38 ---->Mysore
192.168.5.10     ge-0/1/2.0             Full      192.168.7.1      128    35 ---->Delhi

DELHI_10.205.56.7:
==================
admin@Delhi> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.1.6      ge-1/0/1.0             Full      192.168.9.1      128    31  ---->Bangalore
192.168.5.9      ge-1/0/2.0             Full      192.168.5.1      128    39  ---->Mumbai
172.16.7.2       ge-1/1/6.0             Full      10.205.56.1      128    33  ---->Space_ex_sw1

BANGALORE_10.205.56.9:
======================
root@Bangalore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.13     ge-0/0/0.0             Full      192.168.5.1      128    33  ---->Mumbai
192.168.1.5      ge-0/0/1.0             Full      192.168.7.1      128    32  ---->Delhi
172.16.9.2       ge-0/0/3.0             Full      10.205.56.2      128    34  ---->Space_ex_sw2
192.168.1.10     ge-0/1/0.0             Full      192.168.20.1     128    38  ---->Bagmane

Bagmane_10.205.56.20:
====================
admin@Bagmane> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.17     ge-1/0/0.0             Full      192.168.5.1      128    30 ----> Mumbai
172.16.20.2      ge-1/0/2.0             Full      10.205.56.42     128    31 ----> J6350_42
192.168.1.9      ge-1/0/4.0             Full      192.168.9.1      128    32 ----> Bangalore
192.168.1.14     ge-1/0/5.0             Full      192.168.22.1     128    33 ----> Mysore

Mysore_10.205.56.22:
===================
admin@Mysore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.21     ge-0/0/1.0             Full      192.168.5.1      128    32 ----> Mumbai
192.168.1.13     ge-0/1/1.0             Full      192.168.20.1     128    38 ----> bagmane

Space-EX-SW1_10.205.56.1:
=========================
root@Space-EX-SW1> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.2      ge-0/0/0.0             Full      10.205.56.2      128    34 ----> Space_ex_sw2
172.16.7.1       ge-0/0/6.0             Full      192.168.7.1      128    32 ----> Delhi

Space-EX-SW2_10.205.56.2: 
=========================
root@Space-EX-SW2> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.1      ge-0/0/0.0             Full      10.205.56.1      128    35 ----> Space_ex_sw1
172.16.9.1       ge-0/0/3.0             Full      192.168.9.1      128    32 ----> Bangalore

*/
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205b/"+MUMBAI_NAME+"_"+MUMBAI_IP+".txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205b/"+DELHI_NAME+"_"+DELHI_IP+".txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205b/"+BANGALORE_NAME+"_"+BANGALORE_IP+".txt"),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource="classpath:linkd/nms10205b/"+BAGMANE_NAME+"_"+BAGMANE_IP+".txt"),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource="classpath:linkd/nms10205b/"+MYSORE_NAME+"_"+MYSORE_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource="classpath:linkd/nms10205b/"+"J6350-42_"+J6350_42_IP+".txt"),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource="classpath:linkd/nms10205b/"+"SRX-100_"+SRX_100_IP+".txt")
    })
    public void testNetwork10205bOspfLinks() throws Exception {
        m_nodeDao.save(getMumbai());
        m_nodeDao.save(getDelhi());
        m_nodeDao.save(getBangalore());
        m_nodeDao.save(getBagmane());
        m_nodeDao.save(getMysore());
        m_nodeDao.save(getSpaceExSw1());
        m_nodeDao.save(getSpaceExSw2());
        m_nodeDao.save(getJ635042());
        m_nodeDao.save(getSRX100());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setForceIpRouteDiscoveryOnEthernet(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(true);

        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setSaveRouteTable(false);
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
             
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(11, links.size());
        
        /*

                The ospf topology layout:

Parentnode     ParentInterface                  Node            Interface               LinkdStrategy           id

Mumbai          ge-0/1/2.0      (519)  ----> Delhi             ge-1/0/2.0      (28503)  next hop router         800
Mumbai          ge-0/0/1.0      (507)  ----> Bangalore         ge-0/0/0.0      (2401)   next hop router         801
Mumbai          ge-0/0/2.0      (977)  ----> Bagmane           ge-1/0/0.0      (534)    next hop router         802
Mumbai          ge-0/1/1.0      (978)  ----> Mysore            ge-0/0/1.0      (508)    next hop router         803

Delhi           ge-1/0/1.0     (3674)  ----> Bangalore         ge-0/0/1.0      (2397)   next hop router         804
Delhi           ge-1/1/6.0     (17619) ----> Space_ex_sw1      ge-0/0/6.0      (528)    next hop router         805

Bangalore       ge-0/1/0.0     (2396)  ----> Bagmane           ge-1/0/4.0      (1732)   next hop router         806
Bangalore       ge-0/0/3.0     (2398)  ----> Space_ex_sw2      ge-0/0/3.0      (551)    next hop router         807

Bagmane         ge-1/0/5.0      (654)  ----> Mysore            ge-0/1/1.0      (520)    next hop router         808
Bagmane         ge-1/0/2.0      (540)  ----> J6350_42          ge-0/0/2.0      (549)    next hop router         809

Space_ex_sw1    ge-0/0/0.0      (1361)  ----> Space_ex_sw2     ge-0/0/0.0      (531)    next hop router         810
        
         */
        
        int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int id = datalinkinterface.getId().intValue();
            if (start == id ) {
                checkLink(delhi, mumbai, 28503, 519, datalinkinterface);
            } else if (start+1 == id) {
                checkLink(bangalore, mumbai, 2401, 507, datalinkinterface);
            } else if (start+2 == id) {
                checkLink(bagmane, mumbai, 534, 977, datalinkinterface);            
            } else if (start+3 == id) {
                checkLink(mysore, mumbai, 508, 978, datalinkinterface);
            } else if (start+4 == id) {
                checkLink(bangalore, delhi, 2397, 3674, datalinkinterface);
            } else if (start+5 == id) {
                checkLink(spaceexsw1, delhi, 528, 17619, datalinkinterface);
            } else if (start+6 == id ) {
                checkLink(bagmane, bangalore, 1732, 2396, datalinkinterface);
            } else if (start+7 == id) {
                checkLink(spaceexsw2, bangalore, 551, 2398, datalinkinterface);
            } else if (start+8 == id) {
                checkLink(mysore, bagmane, 520, 654, datalinkinterface);
            } else if (start+9 == id) {
                checkLink(j635042, bagmane, 549, 540, datalinkinterface);
            } else if (start+10 == id) {
                checkLink(spaceexsw2, spaceexsw1, 531, 1361, datalinkinterface);
            } else {
                checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            }
        }
    }
}
