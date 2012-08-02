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
import org.opennms.netmgt.config.linkd.Iproutes;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.config.linkd.Vendor;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
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
public class LinkdNms1055Test extends LinkdNms1055NetworkBuilder implements InitializingBean {

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
//        p.setProperty("log4j.logger.org.hibernate.cfg", "WARN");
//        p.setProperty("log4j.logger.org.springframework","WARN");
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
     * Penrose: baseBridgeAddress = 80711f8fafd0
     * Penrose: stpDesignatedRoot = 001f12373dc0
     * Penrose: stpport/designatedbridge/designatedport 62/8000 001f12373dc0/8201 
     *          -----To Riovista - Root Spanning tree: ifindex 515 ---ge-1/2/1
     * Penrose: stpport/designatedbridge/designatedport 483/8000 0022830957d0/81e3
     *          -----this is a backbone port to a higher bridge: ifindex 2693 -- ae0
     *          ---- aggregated port almost sure the link to Delaware
     *          
     * Delaware: baseBridgeAddress = 0022830957d0
     * Delaware: stpDesignatedRoot = 001f12373dc0
     * Delaware: stpport/designatedbridge/designatedport 21/8000 001f12373dc0/822f 
     *          -----To Riovista - Root Spanning tree: ifindex 540 -- ge-0/2/0
     * Delaware: stpport/designatedbridge/designatedport 483/8000 0022830957d0/81e3
     *          -----this is a backbone port to a lower bridge: ifindex 658 ---ae0
     *          -----aggregated port almost sure the link to Penrose
     *
     * Riovista: baseBridgeAddress = 001f12373dc0
     * Riovista: stpDesignatedRoot = 001f12373dc0
     * Riovista: stpport/designatedbridge/designatedport 513/8000 001f12373dc0/8201 
     *          -----To Penrose ifindex 584 ---ge-0/0/0.0
     * Riovista: stpport/designatedbridge/designatedport 559/8000001f12373dc0/822f
     *          -----To Delaware ifindex 503 ---ge-0/0/46.0
     *
     *          
     * Phoenix: baseBridgeAddress = 80711fc414d0
     * Phoenix: Spanning Tree is disabled
     * 
     * Austin: baseBridgeAddress = 80711fc413d0
     * Austin: Spanning Tree is disabled
     * 
     * Sanjose: baseBridgeAddress = 002283d857d0
     * Sanjose: Spanning Tree is disabled
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt"),
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt"),
            @JUnitSnmpAgent(host=PHOENIX_IP, port=161, resource="classpath:linkd/nms1055/"+PHOENIX_NAME+"_"+PHOENIX_IP+".txt"),
            @JUnitSnmpAgent(host=AUSTIN_IP, port=161, resource="classpath:linkd/nms1055/"+AUSTIN_NAME+"_"+AUSTIN_IP+".txt"),
            @JUnitSnmpAgent(host=SANJOSE_IP, port=161, resource="classpath:linkd/nms1055/"+SANJOSE_NAME+"_"+SANJOSE_IP+".txt"),
            @JUnitSnmpAgent(host=RIOVISTA_IP, port=161, resource="classpath:linkd/nms1055/"+RIOVISTA_NAME+"_"+RIOVISTA_IP+".txt")
    })
    public void testNetwork1055Links() throws Exception {
        m_nodeDao.save(getPenrose());
        m_nodeDao.save(getDelaware());
        m_nodeDao.save(getPhoenix());
        m_nodeDao.save(getAustin());
        m_nodeDao.save(getSanjose());
        m_nodeDao.save(getRiovista());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        Iproutes iproutes = new Iproutes();
        Vendor juniper = new Vendor();
        juniper.setVendor_name("Juniper.junos");
        juniper.setSysoidRootMask(".1.3.6.1.4.1.2636.1.1.1");
        juniper.setClassName("org.opennms.netmgt.linkd.snmp.IpCidrRouteTable");
        juniper.addSpecific("2.25");
        juniper.addSpecific("2.29");
        juniper.addSpecific("2.57");
        juniper.addSpecific("2.10");
        iproutes.addVendor(juniper);
        m_linkdConfig.getConfiguration().setIproutes(iproutes);
        m_linkdConfig.update();

        
        final OnmsNode penrose = m_nodeDao.findByForeignId("linkd", PENROSE_NAME);
        final OnmsNode delaware = m_nodeDao.findByForeignId("linkd", DELAWARE_NAME);
        final OnmsNode phoenix = m_nodeDao.findByForeignId("linkd", PHOENIX_NAME);
        final OnmsNode austin = m_nodeDao.findByForeignId("linkd", AUSTIN_NAME);
        final OnmsNode sanjose = m_nodeDao.findByForeignId("linkd", SANJOSE_NAME);
        final OnmsNode riovista = m_nodeDao.findByForeignId("linkd", RIOVISTA_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(penrose.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delaware.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(phoenix.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(austin.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(sanjose.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(riovista.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(penrose.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delaware.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(phoenix.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(austin.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(sanjose.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(riovista.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(8,m_dataLinkInterfaceDao.countAll());                
    }
}
