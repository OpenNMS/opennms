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
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.linkd.Iproutes;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.config.linkd.Vendor;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.DataLinkInterface;
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
public class LinkdNms10205Test extends LinkdNms10205NetworkBuilder implements InitializingBean {

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
     * 
     *  MUMBAI:port ge 0/1/3:ip 192.168.5.5   ------> CHENNAI:port ge 4/0/2: ip 192.168.5.6
     *  MUMBAI:port ge 0/1/2:ip 192.168.5.9   ------> DELHI:port ge 1/0/2: ip 192.168.5.10
     *  MUMBAI:port ge 0/0/1:ip 192.168.5.13   ------> BANGALORE:port ge 0/0/0: ip 192.168.5.14
     *  DELHI:port ge 1/0/1:ip 192.168.1.5     ------> BANGALORE:port ge 0/0/1: ip 192.168.1.6
     *  DELHI:port ge 1/1/6:ip 172.16.7.1     ------> Space-EX-SW1: port 0/0/6: ip 172.16.7.1 ???? same ip address
     *  CHENNAI:port ge 4/0/3:ip 192.168.1.1  ------> DELHI: port ge 1/1/0: ip 192.168.1.2
     *  
     *  a lot of duplicated ip this is a clear proof that linkd is not able to 
     *  gather topology.
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205/"+ MUMBAI_IP +"-walk.txt"),
            @JUnitSnmpAgent(host=CHENNAI_IP, port=161,resource="classpath:linkd/nms10205/"+ CHENNAI_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205/"+DELHI_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205/"+SPACE_EX_SW1_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205/"+BANGALORE_IP+"-walk.txt")
    })
    public void testNetwork10205Links() throws Exception {
        m_nodeDao.save(getMumbai());
        m_nodeDao.save(getChennai());
        m_nodeDao.save(getDelhi());
        m_nodeDao.save(getSpaceExSw1());
        m_nodeDao.save(getBangalore());
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
        juniper.addSpecific("2.30");
        juniper.addSpecific("2.9");
        juniper.addSpecific("2.10");
        iproutes.addVendor(juniper);
        m_linkdConfig.getConfiguration().setIproutes(iproutes);
        m_linkdConfig.update();

        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode chennai = m_nodeDao.findByForeignId("linkd", CHENNAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(chennai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(chennai.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(1,links.size());
                
    }
}
