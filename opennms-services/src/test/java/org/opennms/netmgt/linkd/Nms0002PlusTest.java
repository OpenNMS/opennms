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
import org.opennms.netmgt.model.DataLinkInterface;
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
public class Nms0002PlusTest extends Nms0002NetworkBuilder implements InitializingBean {

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

    /*      Alcatel Lucent due
     *           nodelabel      | snmpifname | ifindex  |       parent       | parentif | parentifindex 
     *      --------------------+------------+----------+--------------------+----------+---------------
     *       s-de-essn-brue-121 | management | 13600001 | r-de-essn-brue-001 | Po121    |           364
     *       r-de-essn-brue-001 | Te2/4/4    |      301 | r-de-essn-glad-004 | Te1/4    |             4
     *       r-de-essn-brue-001 | Tu8        |      313 | r-de-hann-tre5-021 | Tu8      |            12
     *       r-de-essn-brue-001 | Te1/4/4    |      148 | r-de-essn-ruhr-004 | Te1/4    |             4
     *       r-de-essn-brue-001 | Tu9        |      315 | r-de-hann-tre7-020 | Tu9      |            18
     *       s-de-essn-brue-121 | management | 13600001 | r-de-essn-brue-001 | Po121A   |           525
     *       s-de-essn-brue-147 | management | 13600001 | r-de-essn-brue-001 | Po147    |           376
     *       
     *       
     *       Those are the detected links....the local link are with s-121 and s-147
     *       we have a walk from s-165
     *       On the other side there are a lot of cdp connection on the cisco.
     *       
     *       The actual walks are inconsistent....the alcatel has a link to router using lldp but the cisco does not!
     *       LLDP
     *       link from r-de-essn-brue-01:GigabitEthernet1/3/11:(ifindex 107) to s-de-essn-brue-165::(ifindex 1025)
     *       link from r-de-essn-brue-01:GigabitEthernet2/3/11:(ifindex 260) to s-de-essn-brue-165:Alcatel-Lucent 2/25 (ifindex 2025)
     *
     *       STP
     *       link from r-de-essn-brue-01:Port-channel165:(ifindex 381) to s-de-essn-brue-165:Dynamic Aggregate Number 10 ref 40000010 size 2:(ifindex 40000010)
     *
     *       ifindex 381 correspond to bridgeport: 5826 ---96c2
     *       
     *                  96c2 ----> 1730 but 16c2---> 5826
     *       I found from Qbridge that an interface 40000010 is used...this is not the bridge id....it is the
     *       ifindex...Dynamic Aggregate Number 10 ref 40000010 size 2  ----mac 0:e0:b1:bf:58:4c
     *       
     *       stp info...the stp root port is 40000010
     *
     *       Alcatel 165
     *       the interface number is 171 port. 6x(26+2 module port) + management+ loopback+ aggregate
     *       the bridge port number is 156 each module ethernet interface have 26 associated bridge port
     *       the stpport has 155 entries ....no way of linking the stpport to the bridgeport
     *       with criteria....
     *       
     *       bridge port to ifindex ---- M is the module id
     *       index 01-26
     *       bridge port {M}{(M-1)*index+3+index} ---> {M}0{index} 
     *       Modulo 1       1-1001        26--1026
     *       Modulo 2       129-2001     154--2026
     *       Modulo 3       257-3001     282--3026
     *       Modulo 4       385-4001     410--4026
     *       Modulo 5       513-5001     538--5026
     *       Modulo 6       641-6001     666--3026
     *       
     *       stp  155 port
     *       Modulo 1      1-1           26-26 ---manca 25 --25 ---the interface used in port channel
     *       Modulo 2      33-129        58-154---manca 57 --25 ---the interface used in port channel
     *       Modulo 3      65-257        90-282            --26          
     *       Modulo 4      97-385        122-410           --26
     *       Modulo 5      129-513       154-538           --26
     *       Modulo 6      161-641       186-666           --26
     *       aggregated    1034                            -- 1
     *       
     *       designated port = 1->7400->1024
     *       N=26
     *       formula....ifindex = M*1000+port
     *                  bridgeport = (M-1)*(100+N+2) + port
     *                  stpport = (M-1)*(N+6)+port
     *                  
     *       formula....ifindex = M*1000+port
     *                  bridgeport = (M-1)*(128) + port
     *                  stpport = (M-1)*(32)+port
     *                  (30+2)^2=900+120+4=1024
     *                  
     *                  ifindex=40000010=40000000+10=40*1000*1000+10
     *                  1034=(M-1)*32+port= 1024+10=32*32+10=
     *                  M=32---> stpport=31*128+10=3978
     *                  ifindex=31010
     *                  
     *                  
     *                  designate bridge 96c2---> bridgeport=1730---che non esiste su cisco
     *                  ancora mi trovo che la designated port e' una aggregata
     *                  ma sul router ho una chiara indicazione che la porta
     *                  
     */      
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = RDeEssnBrue_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + RDeEssnBrue_NAME+ ".txt"),
            @JUnitSnmpAgent(host = SDeEssnBrue081_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue081_NAME+ ".txt"),
            @JUnitSnmpAgent(host = SDeEssnBrue121_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue121_NAME+ ".txt"),
            @JUnitSnmpAgent(host = SDeEssnBrue142_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue142_NAME+ ".txt"),
            @JUnitSnmpAgent(host = SDeEssnBrue165_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue165_NAME+ ".txt")
    })
    public void testCiscoAlcatelEssnBrueLldp() {
        
        m_nodeDao.save(getRDeEssnBrue());
        m_nodeDao.save(getSDeEssnBrue081());
        m_nodeDao.save(getSDeEssnBrue121());
        m_nodeDao.save(getSDeEssnBrue142());
        m_nodeDao.save(getSDeEssnBrue165());
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

        HibernateEventWriter queryManager = (HibernateEventWriter)m_linkd.getQueryManager();

        assertEquals(107, queryManager.getFromSysnameIfName(RDeEssnBrue_NAME, "Gi1/3/11").getIfIndex().intValue());
        assertEquals(260, queryManager.getFromSysnameIfName(RDeEssnBrue_NAME, "Gi2/3/11").getIfIndex().intValue());

        assertEquals(1025, queryManager.getFromSysnameIfIndex(SDeEssnBrue165_NAME, 1025).getIfIndex().intValue());
        assertEquals(2025, queryManager.getFromSysnameIfName(SDeEssnBrue165_NAME, "2/25").getIfIndex().intValue());

        final OnmsNode routerCisco = m_nodeDao.findByForeignId("linkd", RDeEssnBrue_NAME);
        final OnmsNode swicthAlu081 = m_nodeDao.findByForeignId("linkd", SDeEssnBrue081_NAME);
        final OnmsNode swicthAlu121 = m_nodeDao.findByForeignId("linkd", SDeEssnBrue121_NAME);
        final OnmsNode swicthAlu142 = m_nodeDao.findByForeignId("linkd", SDeEssnBrue142_NAME);
        final OnmsNode swicthAlu165 = m_nodeDao.findByForeignId("linkd", SDeEssnBrue165_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerCisco.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(swicthAlu081.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(swicthAlu121.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(swicthAlu142.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(swicthAlu165.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerCisco.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(swicthAlu081.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(swicthAlu121.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(swicthAlu142.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(swicthAlu165.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            printLink(link);
        }

        assertEquals(6,m_dataLinkInterfaceDao.countAll());

    }

    
}
