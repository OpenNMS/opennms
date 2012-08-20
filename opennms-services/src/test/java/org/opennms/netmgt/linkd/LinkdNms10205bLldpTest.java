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
public class LinkdNms10205bLldpTest extends LinkdNms10205bNetworkBuilder implements InitializingBean {

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
        example1.setUseLldpDiscovery(true);
        
        m_linkdConfig.update();
        
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
        for (final DataLinkInterface datalinkinterface: links) {
            switch(datalinkinterface.getId().intValue()) {
            case 569: checkLink(bagmane, delhi, 513, 28519, datalinkinterface);
            break;
            case 570: checkLink(spaceexsw1, delhi, 528, 28520, datalinkinterface);
            break;
            case 571: checkLink(j635042, bagmane, 549, 514, datalinkinterface);
            break;
            case 572: checkLink(spaceexsw2, spaceexsw1, 531, 1361, datalinkinterface);
            break;
            default: checkLink(mumbai,mumbai,-1,-1,datalinkinterface);
            break;                
            }
        }
    }
}
