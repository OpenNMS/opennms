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

import java.util.Collection;
import java.util.Date;
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
import org.opennms.netmgt.config.LinkdConfigFactory;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.linkd.nb.Nms0001NetworkBuilder;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
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
public class Nms0001Test extends Nms0001NetworkBuilder implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

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
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt")
    })
    public void testIsIsLinks() throws Exception {
        
        m_nodeDao.save(getFroh());
        m_nodeDao.save(getOedipus());
        m_nodeDao.save(getSiegFrie());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpNodeTable(false);

        final OnmsNode froh = m_nodeDao.findByForeignId("linkd", FROH_NAME);
        final OnmsNode oedipus = m_nodeDao.findByForeignId("linkd", OEDIPUS_NAME);
        final OnmsNode siegfrie = m_nodeDao.findByForeignId("linkd", SIEGFRIE_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(froh.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(oedipus.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(siegfrie.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(froh.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(oedipus.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(siegfrie.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(3, nodes.size());
        
        for (LinkableNode node: nodes) {
            assertEquals(2, node.getIsisInterfaces().size());
            switch(node.getNodeId()) {
                case 1: assertEquals(FROH_ISIS_SYS_ID, node.getIsisSysId());
                break;
                case 2: assertEquals(OEDIPUS_ISIS_SYS_ID, node.getIsisSysId());
                break;
                case 3: assertEquals(SIEGFRIE_ISIS_SYS_ID, node.getIsisSysId());
                break;
                default: assertEquals(-1, node.getNodeId());
                break;
            }
        }        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(3,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();

        int start=getStartPoint(datalinkinterfaces);

        /*
         * 
         * These are the links among the following nodes discovered using 
         * only the isis protocol
         * froh:ae1.0(599):10.1.3.6/30       <-->    oedipus:ae1.0(578):10.1.3.5/30
         * froh:ae2.0(600):10.1.3.2/30       <-->    siegfrie:ae2.0(552):10.1.3.1/30
         * oedipus:ae0.0(575):10.1.0.10/30   <-->    siegfrie:ae0.0(533):10.1.0.9/30
         * 
         */
        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {
            
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                checkLink(froh, oedipus, 599, 578, datalinkinterface);
            } else if (linkid == start+1 ) {
                checkLink(froh, siegfrie, 600, 552, datalinkinterface);
            } else if (linkid == start+2) {
                checkLink(oedipus, siegfrie, 575, 533, datalinkinterface);
            } else {
                // error
                checkLink(froh,froh,-1,-1,datalinkinterface);
            } 
            
        }
        
        DataLinkInterface iface = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(froh.getId(), Integer.valueOf(599)).iterator().next();
        iface.setNodeParentId(oedipus.getId());
        iface.setParentIfIndex(578);
        iface.setStatus(StatusType.ACTIVE);
        iface.setLastPollTime(new Date());
        m_dataLinkInterfaceDao.saveOrUpdate(iface);
        
        assertEquals(3, m_dataLinkInterfaceDao.countAll());
    }
}
