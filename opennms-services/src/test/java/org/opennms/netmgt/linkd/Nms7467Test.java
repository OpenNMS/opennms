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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.criterion.Restrictions;
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
import org.opennms.netmgt.dao.AtInterfaceDao;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpRouteInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.StpInterfaceDao;
import org.opennms.netmgt.dao.StpNodeDao;
import org.opennms.netmgt.dao.VlanDao;
import org.opennms.netmgt.linkd.nb.Nms7467NetworkBuilder;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsStpNode.BridgeBaseType;
import org.opennms.netmgt.model.OnmsStpNode.StpProtocolSpecification;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class Nms7467Test extends Nms7467NetworkBuilder implements InitializingBean {

    @Autowired
    private Linkd m_linkd;

    @Autowired
    private LinkdConfig m_linkdConfig;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private StpNodeDao m_stpNodeDao;
    
    @Autowired
    private StpInterfaceDao m_stpInterfaceDao;
    
    @Autowired
    private IpRouteInterfaceDao m_ipRouteInterfaceDao;
    
    @Autowired
    private AtInterfaceDao m_atInterfaceDao;
    
    @Autowired
    private VlanDao m_vlanDao;
    
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
        p.setProperty("log4j.logger.org.opennms.netmgt.config", "WARN");
        p.setProperty("log4j.logger.org.opennms.netmgt.config", "WARN");
        
        super.setNodeDao(m_nodeDao);
        super.setSnmpInterfaceDao(m_snmpInterfaceDao);
        MockLogAppender.setupLogging(p);

    }

    @After
    public void tearDown() throws Exception {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();
    }

    @Test
    public void testDefaultConfiguration() throws MarshalException, ValidationException, IOException {
        
        assertEquals(5, m_linkdConfig.getThreads());
        assertEquals(3600000, m_linkdConfig.getInitialSleepTime());
        assertEquals(18000000, m_linkdConfig.getSnmpPollInterval());
        assertEquals(1800000, m_linkdConfig.getDiscoveryLinkInterval());
        

        
        assertEquals(false, m_linkdConfig.isAutoDiscoveryEnabled());
        assertEquals(false, m_linkdConfig.enableDiscoveryDownload());
        assertEquals(true,m_linkdConfig.isVlanDiscoveryEnabled());
        assertEquals(true,m_linkdConfig.useCdpDiscovery());
        assertEquals(true,m_linkdConfig.useIpRouteDiscovery());
        assertEquals(true,m_linkdConfig.useBridgeDiscovery());
        assertEquals(true,m_linkdConfig.useOspfDiscovery());
        assertEquals(true,m_linkdConfig.useLldpDiscovery());

        assertEquals(true,m_linkdConfig.saveRouteTable());
        assertEquals(true,m_linkdConfig.saveStpNodeTable());
        assertEquals(true,m_linkdConfig.saveStpInterfaceTable());
        assertEquals(false,m_linkdConfig.forceIpRouteDiscoveryOnEthernet());
        
        Enumeration<org.opennms.netmgt.config.linkd.Package> iter = m_linkdConfig.enumeratePackage();
        org.opennms.netmgt.config.linkd.Package example1 = iter.nextElement();
        
        assertEquals(false, iter.hasMoreElements());   
        assertEquals("example1",example1.getName());
        assertEquals(false, example1.hasAutoDiscovery());
        assertEquals(false, example1.hasDiscovery_link_interval());
        assertEquals(false,example1.hasEnableDiscoveryDownload());
        assertEquals(false,example1.hasEnableVlanDiscovery());
        assertEquals(false,example1.hasForceIpRouteDiscoveryOnEthernet());
        assertEquals(false,example1.hasSaveRouteTable());
        assertEquals(false,example1.hasSaveStpInterfaceTable());
        assertEquals(false,example1.hasSaveStpNodeTable());
        assertEquals(false,example1.hasSnmp_poll_interval());
        assertEquals(false,example1.hasUseBridgeDiscovery());
        assertEquals(false,example1.hasUseCdpDiscovery());
        assertEquals(false,example1.hasUseIpRouteDiscovery());
        
        assertEquals(false, m_linkdConfig.isInterfaceInPackage(InetAddress.getByName(CISCO_C870_IP), example1));
        
        m_nodeDao.save(getCiscoC870());
        m_nodeDao.flush();
        
        m_linkdConfig.update();
        assertEquals(true, m_linkdConfig.isInterfaceInPackage(InetAddress.getByName(CISCO_C870_IP), example1));
        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=CISCO_C870_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_C870_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=DARWIN_10_8_IP, port=161, resource="classpath:linkd/nms7467/"+DARWIN_10_8_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource="classpath:linkd/nms7467/"+NETGEAR_SW_108_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=LINUX_UBUNTU_IP, port=161, resource="classpath:linkd/nms7467/"+LINUX_UBUNTU_IP+"-walk.txt")
    })
    public void testAllTogether() throws Exception {

        m_nodeDao.save(getCiscoC870());
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.save(getNetGearSw108());
        m_nodeDao.save(getDarwin108());       
        m_nodeDao.save(getLinuxUbuntu());
        m_nodeDao.save(getNodeWithoutSnmp(ACCESSPOINT_NAME, ACCESSPOINT_IP));
        m_nodeDao.save(getNodeWithoutSnmp(WORKSTATION_NAME, WORKSTATION_IP));

        m_nodeDao.flush();


        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);
        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);
        final OnmsNode mac = m_nodeDao.findByForeignId("linkd", DARWIN_10_8_NAME);
        final OnmsNode linux = m_nodeDao.findByForeignId("linkd", LINUX_UBUNTU_NAME);
        final OnmsNode ciscoap = m_nodeDao.findByForeignId("linkd", ACCESSPOINT_NAME);
        final OnmsNode workstation = m_nodeDao.findByForeignId("linkd", WORKSTATION_NAME);


        
        assertEquals(7, m_nodeDao.countAll());

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mac.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(linux.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(ciscoap.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(workstation.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(linux.getId()));
        
        final Collection<LinkableNode> linkables = m_linkd.getLinkableNodes();
        assertEquals(5, linkables.size());       

        assertEquals(0,m_dataLinkInterfaceDao.countAll());
                                       
        assertEquals(5, m_linkd.getLinkableNodesOnPackage("example1").size());

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(6,links.size());
        //
        final DataLinkInterface mactongsw108link = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(mac.getId(),4);
        
        assertEquals(mac.getId(), mactongsw108link.getNode().getId());
        assertEquals(4,mactongsw108link.getIfIndex().intValue());
        assertEquals(ngsw108.getId(), mactongsw108link.getNodeParentId());
        assertEquals(1, mactongsw108link.getParentIfIndex().intValue());        

        final DataLinkInterface ngsw108linktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(ngsw108.getId(), 8);
        
        assertEquals(ngsw108.getId(), ngsw108linktociscows.getNode().getId());
        assertEquals(8,ngsw108linktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), ngsw108linktociscows.getNodeParentId());
        assertEquals(9, ngsw108linktociscows.getParentIfIndex().intValue());

        final DataLinkInterface ciscorouterlinktociscows2 = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(ciscows.getId(), 52);
        assertEquals(ciscows.getId(), ciscorouterlinktociscows2.getNode().getId());
        assertEquals(52, ciscorouterlinktociscows2.getIfIndex().intValue());
        assertEquals(ciscorouter.getId(), ciscorouterlinktociscows2.getNodeParentId());
        assertEquals(3, ciscorouterlinktociscows2.getParentIfIndex().intValue());

        final DataLinkInterface linuxubuntulinktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(linux.getId(), 4);
        
        assertEquals(linux.getId(), linuxubuntulinktociscows.getNode().getId());
        assertEquals(4,linuxubuntulinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), linuxubuntulinktociscows.getNodeParentId());
        assertEquals(11, linuxubuntulinktociscows.getParentIfIndex().intValue());

        final DataLinkInterface workstationlinktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(workstation.getId(), -1);
        
        assertEquals(workstation.getId(), workstationlinktociscows.getNode().getId());
        assertEquals(-1,workstationlinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), workstationlinktociscows.getNodeParentId());
        assertEquals(47, workstationlinktociscows.getParentIfIndex().intValue());

        final DataLinkInterface ciscoaplinktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(ciscoap.getId(), -1);
        
        assertEquals(ciscoap.getId(), ciscoaplinktociscows.getNode().getId());
        assertEquals(-1, ciscoaplinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), ciscoaplinktociscows.getNodeParentId());
        assertEquals(47,ciscoaplinktociscows.getParentIfIndex().intValue());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt")
    })
    public void testCiscoWsC2948Collection() throws Exception {
        
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.flush();
        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        
        final OnmsNode ciscosw = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscosw.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscosw.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodes().size());
        LinkableNode linkNode = m_linkd.getLinkableNodes().iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has only one route with valid next hop must be valid but type is ethernet so skipped
        // but it is itself so 0
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 5 
        assertEquals(2,m_ipRouteInterfaceDao.countAll());
        
        assertEquals(5, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(CISCO_WS_C2948_IP)).getName();

        assertEquals("example1", packageName);
        
        assertEquals(58,linkNode.getBridgeIdentifiers().size());

        // has 1 stp node entry check the bridge identifier and protocol
        assertEquals(CISCO_WS_C2948_BRIDGEID,linkNode.getBridgeIdentifier(1));
        
        // has 50 stp entry che ifIndex must be different then -1
        // 
        assertEquals(50, linkNode.getStpInterfaces().get(1).size());

        // no cdp inteface also if the walk return several interfaces
        assertEquals("No cdp interface because no other node is there",0,linkNode.getCdpInterfaces().size());
        
        for (OnmsStpInterface stpiface: linkNode.getStpInterfaces().get(1)) {
            assertTrue("should have a valid ifindex", stpiface.getIfIndex().intValue() > 0);
            assertTrue("should have a valid bridgeport", stpiface.getBridgePort().intValue() > 0);
        }

        // This make shure that the ip/mac association is saved
        /*
        * nodelabel:ip:mac:ifindex:ifdescr
        *      
        * CISCO_WS_C2948_IP:172.20.1.7:0002baaacffe:3:me1
        */
        
        assertEquals("should have saved 1 ip to mac",1, m_linkd.getAtInterfaces(packageName).size());        
        AtInterface at = m_linkd.getAtInterfaces(packageName).get("0002baaacffe").get(0);
        assertEquals(CISCO_WS_C2948_IP,at.getIpAddress().getHostAddress());
        assertEquals(3, at.getIfIndex().intValue());
        // Now Let's test the database
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpRouteInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", ciscosw.getId()));

        // 2 route entry in database
        assertEquals(2, m_ipRouteInterfaceDao.findMatching(criteria).size());
        //0 atinterface in database
        assertEquals(0, m_atInterfaceDao.findAll().size());
        
        // 5 entry in vlan
        assertEquals(5, m_vlanDao.findAll().size());
 
        // 1 entry in stpnode
        assertEquals(1, m_stpNodeDao.countAll());
        
        OnmsStpNode stpnode = m_stpNodeDao.findByNodeAndVlan(ciscosw.getId(), 1);
        assertTrue(CISCO_WS_C2948_BRIDGEID.equals(stpnode.getBaseBridgeAddress()));
        assertEquals(50, stpnode.getBaseNumPorts().intValue());

        assertEquals(BridgeBaseType.TRANSPARENT_ONLY,stpnode.getBaseType());
        assertEquals(StpProtocolSpecification.IEEE8021D,stpnode.getStpProtocolSpecification());
        
        
        // 50 entry in stpinterface
        assertEquals(50, m_stpInterfaceDao.findAll().size());        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_C870_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_C870_IP+"-walk.txt")
    })
    public void testCiscoC870Collection() throws Exception {
        m_nodeDao.save(getCiscoC870());
        m_nodeDao.flush();
        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        
        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodes().size());
        LinkableNode linkNode = m_linkd.getLinkableNodes().iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 0 route (next hop must be valid!) 
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(CISCO_C870_IP)).getName();

        assertEquals("example1", packageName);
        
        assertEquals(6,linkNode.getBridgeIdentifiers().size());

        // has 1 stp node entry check the bridge identifier and protocol
        assertEquals(CISCO_C870_BRIDGEID,linkNode.getBridgeIdentifier(1));
        
        // has 50 stp entry che ifIndex must be different then -1
        // 
        assertEquals(1, linkNode.getStpInterfaces().get(1).size());

        // no cdp inteface also if the walk return several interfaces
        assertEquals("No cdp interface because no other node is there",0,linkNode.getCdpInterfaces().size());
        
        for (OnmsStpInterface stpiface: linkNode.getStpInterfaces().get(1)) {
            assertTrue("should have a valid ifindex", stpiface.getIfIndex().intValue() > 0);
            assertTrue("should have a valid bridgeport", stpiface.getBridgePort().intValue() > 0);
        }

        // This make shure that the ip/mac association is saved
        /*
        * nodelabel:ip:mac:ifindex:ifdescr
        *      
        * CISCO_C870:172.20.1.1:001f6cd034e7:12:Vlan1
        * CISCO_C870:172.20.2.1:001f6cd034e7:13:Vlan2
        * CISCO_C870:10.255.255.2:001f6cd034e7:12:Vlan1
        * CISCO_C870:65.41.39.146:00000c03b09e:14:BVI1
        */
        
        assertEquals("should have saved 2 ip to mac",2, m_linkd.getAtInterfaces(packageName).size());        
        
        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName).get("001f6cd034e7");
        assertEquals(3, ats.size());
        for (AtInterface at :ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.1"))
                assertEquals(12, at.getIfIndex().intValue());
            else if( at.getIpAddress().getHostAddress().equals("172.20.2.1"))
                assertEquals(13, at.getIfIndex().intValue());
            else if( at.getIpAddress().getHostAddress().equals("10.255.255.2"))
                assertEquals(12, at.getIfIndex().intValue());
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        ats = m_linkd.getAtInterfaces(packageName).get("00000c03b09e");
        assertEquals(1, ats.size());
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("65.41.39.146"))
                assertEquals(14, at.getIfIndex().intValue());
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //0 atinterface in database
        assertEquals(4, m_atInterfaceDao.findAll().size());

        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpRouteInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", ciscorouter.getId()));
        final List<OnmsIpRouteInterface> iproutes = m_ipRouteInterfaceDao.findMatching(criteria);
        // 7 route entry in database
        for (OnmsIpRouteInterface iproute: iproutes) {
            System.out.println(iproute.getRouteDest()+"/"+iproute.getRouteMask()+"/"+iproute.getRouteNextHop()+"/"+iproute.getRouteIfIndex());
        }
        assertEquals(7, iproutes.size());
        
        // 0 entry in vlan
        assertEquals(0, m_vlanDao.findAll().size());
 
        // 1 entry in stpnode
        assertEquals(1, m_stpNodeDao.countAll());
        
        OnmsStpNode stpnode = m_stpNodeDao.findByNodeAndVlan(ciscorouter.getId(), 1);
        assertTrue(CISCO_C870_BRIDGEID.equals(stpnode.getBaseBridgeAddress()));
        assertEquals(1, stpnode.getBaseNumPorts().intValue());

        assertEquals(BridgeBaseType.SRT,stpnode.getBaseType());
        assertEquals(StpProtocolSpecification.IEEE8021D,stpnode.getStpProtocolSpecification());
        
        
        // 1 entry in stpinterface
        assertEquals(1, m_stpInterfaceDao.findAll().size());        

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource="classpath:linkd/nms7467/"+NETGEAR_SW_108_IP+"-walk.txt")
    })
    public void testNetGearSw108Collection() throws Exception {
        m_nodeDao.save(getNetGearSw108());
        m_nodeDao.flush();

        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodes().size());
        LinkableNode linkNode = m_linkd.getLinkableNodes().iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 0 route (next hop must be valid!) no ip route table
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(NETGEAR_SW_108_IP)).getName();

        assertEquals("example1", packageName);
        
        assertEquals(9,linkNode.getBridgeIdentifiers().size());

        // has 1 stp node entry check the bridge identifier and protocol
        assertEquals(NETGEAR_SW_108_BRIDGEID,linkNode.getBridgeIdentifier(1));
        
        // has 8 stp entry che ifIndex must be different then -1
        // 
        assertEquals(8, linkNode.getStpInterfaces().get(1).size());

        // no cdp inteface also if the walk return several interfaces
        assertEquals("cdp not supported",0,linkNode.getCdpInterfaces().size());
        
        for (OnmsStpInterface stpiface: linkNode.getStpInterfaces().get(1)) {
            assertTrue("should have a valid ifindex", stpiface.getIfIndex().intValue() > 0);
            assertTrue("should have a valid bridgeport", stpiface.getBridgePort().intValue() > 0);
        }

        // This make shure that the ip/mac association is saved
        /*
        * nodelabel:ip:mac:ifindex:ifdescr
        *      
        * NETGEAR_SW_108:172.20.1.8:00223ff00b7b::
        * Run the spanning tree protocol
        * with bridge identifier: 00223ff00b7b
        * Transparent Bridge
        */
        
        assertEquals("should have saved 1 ip to mac",1, m_linkd.getAtInterfaces(packageName).size());        
        
        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName).get("00223ff00b7b");
        assertEquals(1, ats.size());
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.8"))
                assertTrue(at.getIfIndex().intValue() == -1);
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //1 atinterface in database: has itself in ipadress to media
        assertEquals(1, m_atInterfaceDao.findAll().size());

        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpRouteInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", ngsw108.getId()));
        final List<OnmsIpRouteInterface> iproutes = m_ipRouteInterfaceDao.findMatching(criteria);
        // 7 route entry in database
        for (OnmsIpRouteInterface iproute: iproutes) {
            System.out.println(iproute.getRouteDest()+"/"+iproute.getRouteMask()+"/"+iproute.getRouteNextHop()+"/"+iproute.getRouteIfIndex());
        }
        assertEquals(0, iproutes.size());
        
        // 0 entry in vlan
        assertEquals(0, m_vlanDao.findAll().size());
 
        // 1 entry in stpnode
        assertEquals(1, m_stpNodeDao.countAll());
        
        OnmsStpNode stpnode = m_stpNodeDao.findByNodeAndVlan(ngsw108.getId(), 1);
        assertTrue(NETGEAR_SW_108_BRIDGEID.equals(stpnode.getBaseBridgeAddress()));
        assertEquals(8, stpnode.getBaseNumPorts().intValue());

        assertEquals(BridgeBaseType.TRANSPARENT_ONLY,stpnode.getBaseType());
        assertEquals(StpProtocolSpecification.IEEE8021D,stpnode.getStpProtocolSpecification());
        
        
        // 50 entry in stpinterface
        assertEquals(8, m_stpInterfaceDao.findAll().size());        
        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=LINUX_UBUNTU_IP, port=161, resource="classpath:linkd/nms7467/"+LINUX_UBUNTU_IP+"-walk.txt")
    })
    public void testLinuxUbuntuCollection() throws Exception {
        m_nodeDao.save(getLinuxUbuntu());
        m_nodeDao.flush();
        
        final OnmsNode linux = m_nodeDao.findByForeignId("linkd", LINUX_UBUNTU_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(linux.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(linux.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodes().size());
        LinkableNode linkNode = m_linkd.getLinkableNodes().iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 0 route (next hop must be valid!) no ip route table
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(LINUX_UBUNTU_IP)).getName();

        assertEquals("example1", packageName);
              
        assertEquals(false, linkNode.isBridgeNode());
        
        assertEquals(0,linkNode.getBridgeIdentifiers().size());

        // no cdp inteface also if the walk return several interfaces
        assertEquals("cdp not supported",0,linkNode.getCdpInterfaces().size());
        
        // This make shure that the ip/mac association is saved
        /*
        * nodelabel:ip:mac:ifindex:ifdescr
        * LINUX_UBUNTU:172.20.1.14:406186e28b53:4:br0
        * 
        */
        
        assertEquals("should have saved 1 ip to mac",1, m_linkd.getAtInterfaces(packageName).size());        
        
        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName).get("406186e28b53");
        assertEquals(1, ats.size());
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.14"))
                assertTrue(at.getIfIndex().intValue() == 4);
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //0 atinterface in database
        assertEquals(0, m_atInterfaceDao.findAll().size());

        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpRouteInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", linux.getId()));
        final List<OnmsIpRouteInterface> iproutes = m_ipRouteInterfaceDao.findMatching(criteria);
        // 4 route entry in database
        for (OnmsIpRouteInterface iproute: iproutes) {
            System.out.println(iproute.getRouteDest()+"/"+iproute.getRouteMask()+"/"+iproute.getRouteNextHop()+"/"+iproute.getRouteIfIndex());
        }
        assertEquals(4, iproutes.size());
        
        // 0 entry in vlan
        assertEquals(0, m_vlanDao.findAll().size());
 
        // 0 entry in stpnode
        assertEquals(0, m_stpNodeDao.countAll());        
        
        // 0 entry in stpinterface
        assertEquals(0, m_stpInterfaceDao.findAll().size());        

    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DARWIN_10_8_IP, port=161, resource="classpath:linkd/nms7467/"+DARWIN_10_8_IP+"-walk.txt")
    })
    public void testDarwin108Collection() throws Exception {
        m_nodeDao.save(getDarwin108());
        m_nodeDao.flush();
        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);

        m_linkdConfig.update();

        final OnmsNode mac = m_nodeDao.findByForeignId("linkd", DARWIN_10_8_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mac.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mac.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodes().size());
        LinkableNode linkNode = m_linkd.getLinkableNodes().iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 1 route (next hop must be valid!) no ip route table
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(DARWIN_10_8_IP)).getName();

        assertEquals("example1", packageName);
              
        assertEquals(false, linkNode.isBridgeNode());
        
        assertEquals(0,linkNode.getBridgeIdentifiers().size());

        // no cdp inteface also if the walk return several interfaces
        assertEquals("cdp not supported",0,linkNode.getCdpInterfaces().size());
        
        // This make shure that the ip/mac association is saved
        /*
        * nodelabel:ip:mac:ifindex:ifdescr
        * DARWIN_10_8:172.20.1.28:0026b0ed8fb8:4:en0
        *  
        */
        
        assertEquals("should have saved 1 ip to mac",1, m_linkd.getAtInterfaces(packageName).size());        
        
        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName).get("0026b0ed8fb8");
        assertEquals(1, ats.size());
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.28"))
                assertTrue(at.getIfIndex().intValue() == 4);
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //0 atinterface in database
        assertEquals(0, m_atInterfaceDao.findAll().size());

        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpRouteInterface.class);
        criteria.createAlias("node", "node");
        criteria.add(Restrictions.eq("node.id", mac.getId()));
        final List<OnmsIpRouteInterface> iproutes = m_ipRouteInterfaceDao.findMatching(criteria);
        // 4 route entry in database
        for (OnmsIpRouteInterface iproute: iproutes) {
            System.out.println(iproute.getRouteDest()+"/"+iproute.getRouteMask()+"/"+iproute.getRouteNextHop()+"/"+iproute.getRouteIfIndex());
        }
        assertEquals(20, iproutes.size());
        
        // 0 entry in vlan
        assertEquals(0, m_vlanDao.findAll().size());
 
        // 0 entry in stpnode
        assertEquals(0, m_stpNodeDao.countAll());        
        
        // 0 entry in stpinterface
        assertEquals(0, m_stpInterfaceDao.findAll().size());
    }

    @Test
    public void testWorkStation() throws Exception {
        m_nodeDao.save(getNodeWithoutSnmp(WORKSTATION_NAME,WORKSTATION_IP));
        m_nodeDao.flush();
        final OnmsNode workstation = m_nodeDao.findByForeignId("linkd", WORKSTATION_NAME);

        assertTrue(!m_linkd.scheduleNodeCollection(workstation.getId()));
        
    }

    /*
     *  DARWIN_10_8:port4   ------> port 1 :NETGEAR_SW_108
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DARWIN_10_8_IP, port=161, resource="classpath:linkd/nms7467/"+DARWIN_10_8_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource="classpath:linkd/nms7467/"+NETGEAR_SW_108_IP+"-walk.txt")
    })
    public void testLinkDarwinNetgear() throws Exception {
        m_nodeDao.save(getNetGearSw108());
        m_nodeDao.save(getDarwin108());
        m_nodeDao.flush();

        final OnmsNode mac = m_nodeDao.findByForeignId("linkd", DARWIN_10_8_NAME);
        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mac.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mac.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        String macpackageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(DARWIN_10_8_IP)).getName();
        String ngsw108packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(NETGEAR_SW_108_IP)).getName();

        assertEquals("example1", macpackageName);
        assertEquals("example1", ngsw108packageName);
        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(1,links.size());
        
        final DataLinkInterface mactongsw108link = links.get(0);
        
        assertEquals(mac.getId(), mactongsw108link.getNode().getId());
        assertEquals(4,mactongsw108link.getIfIndex().intValue());
        assertEquals(ngsw108.getId(), mactongsw108link.getNodeParentId());
        assertEquals(1, mactongsw108link.getParentIfIndex().intValue());        
        
    }
    
    /*
     *  NETGEAR_SW_108:port8------> port 2/1 (ifindex 9):CISCO_WS_C2948_IP
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource="classpath:linkd/nms7467/"+NETGEAR_SW_108_IP+"-walk.txt")
    })
    public void testLinkNetgearCiscoWs() throws Exception {
    	Package example1 = m_linkdConfig.getPackage("example1");
        example1.setForceIpRouteDiscoveryOnEthernet(false);

    	m_nodeDao.save(getNetGearSw108());
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.flush();

        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));        
        
        String ciscowspackageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(CISCO_WS_C2948_IP)).getName();
        String ngsw108packageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(NETGEAR_SW_108_IP)).getName();

        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        assertEquals("example1", ciscowspackageName);
        assertEquals("example1", ngsw108packageName);
        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: links) {
        	printLink(link);
        }
        assertEquals(1,links.size());
        
        final DataLinkInterface ngsw108linktociscows = links.get(0);
        
        assertEquals(ngsw108.getId(), ngsw108linktociscows.getNode().getId());
        assertEquals(8,ngsw108linktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), ngsw108linktociscows.getNodeParentId());
        assertEquals(9, ngsw108linktociscows.getParentIfIndex().intValue());
                
    }

    /*
     * LINUX_UBUNTU:port4  ------> port 2/3 (ifindex 11):CISCO_WS_C2948_IP
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=LINUX_UBUNTU_IP, port=161, resource="classpath:linkd/nms7467/"+LINUX_UBUNTU_IP+"-walk.txt")
    })
    public void testLinuxUbuntuCiscoWs() throws Exception {
        m_nodeDao.save(getLinuxUbuntu());
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.flush();

        final OnmsNode linuxubuntu = m_nodeDao.findByForeignId("linkd", LINUX_UBUNTU_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(linuxubuntu.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(linuxubuntu.getId()));
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        String ciscowspackageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(CISCO_WS_C2948_IP)).getName();
        String linuxubuntupackageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(LINUX_UBUNTU_IP)).getName();

        assertEquals("example1", ciscowspackageName);
        assertEquals("example1", linuxubuntupackageName);
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(1,links.size());
        
        final DataLinkInterface linuxubuntulinktociscows = links.get(0);
        
        assertEquals(linuxubuntu.getId(), linuxubuntulinktociscows.getNode().getId());
        assertEquals(4,linuxubuntulinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), linuxubuntulinktociscows.getNodeParentId());
        assertEquals(11, linuxubuntulinktociscows.getParentIfIndex().intValue());
                
    }

    /*
     * WORKSTATION: linked to a wireless  ------> port 2/29 (ifindex 47):CISCO_WS_C2948_IP
     * should be the same port for cisco wireless device.....
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt")
    })
    public void testWorkstationCiscoWs() throws Exception {
        m_nodeDao.save(getNodeWithoutSnmp(WORKSTATION_NAME, WORKSTATION_IP));
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.flush();

        final OnmsNode workstation = m_nodeDao.findByForeignId("linkd", WORKSTATION_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(workstation.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));

        assertEquals(1, m_linkd.getLinkableNodes().size());
        LinkableNode linkNode = m_linkd.getLinkableNodes().iterator().next();
        
        // linkable node is not null
        assertTrue(linkNode != null);
        
        final Map<String, List<AtInterface>> mactoatinterfacemap = m_linkd.getAtInterfaces("example1");
        assertEquals(2,mactoatinterfacemap.size());
        
        assertEquals(1, mactoatinterfacemap.get(WORKSTATION_MAC).size());
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        String ciscowspackageName = m_linkdConfig.getFirstPackageMatch(InetAddress.getByName(CISCO_WS_C2948_IP)).getName();

        assertEquals("example1", ciscowspackageName);
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(1,links.size());
        
        final DataLinkInterface workstationlinktociscows = links.get(0);
        
        assertEquals(workstation.getId(), workstationlinktociscows.getNode().getId());
        assertEquals(-1,workstationlinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), workstationlinktociscows.getNodeParentId());
        assertEquals(47, workstationlinktociscows.getParentIfIndex().intValue());
                
    }

    @Test
    public void testGetNodeidFromIp() throws UnknownHostException, SQLException {
        m_nodeDao.save(getCiscoC870());
        m_nodeDao.flush();
        
        HibernateEventWriter db = (HibernateEventWriter)m_linkd.getQueryManager();
        
        final int nodeid = db.getNodeidFromIp(InetAddress.getByName(CISCO_C870_IP)).get(0);
        assertEquals(m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME).getId().intValue(), nodeid);
    }
    
    @Test 
    @Transactional
    public void testGetIfIndexByName() throws SQLException {
        m_nodeDao.save(getCiscoC870());
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.flush();

        OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        assertEquals("FastEthernet2", ciscorouter.getSnmpInterfaceWithIfIndex(3).getIfDescr());

        OnmsNode ciscosw = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);
        assertEquals("2/44", ciscosw.getSnmpInterfaceWithIfIndex(52).getIfName());

        HibernateEventWriter db = (HibernateEventWriter)m_linkd.getQueryManager();
        assertEquals(3, db.getIfIndexByName(ciscorouter.getId(), "FastEthernet2"));
        assertEquals(52,db.getIfIndexByName(ciscosw.getId(), "2/44"));
        
    }
    /*
     *
     * CISCO_C870:  FastEthernet2 3  ------> port  2/44 (ifindex 52):CISCO_WS_C2948_IP
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt"),
            @JUnitSnmpAgent(host=CISCO_C870_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_C870_IP+"-walk.txt")
    })
    public void testCiscoRouterCiscoWsUsingCdp() throws Exception {
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseCdpDiscovery(true);
        
        example1.setSaveRouteTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setEnableVlanDiscovery(false);

    	
    	m_nodeDao.save(getCiscoC870());
        m_nodeDao.save(getCiscoWsC2948());
        m_nodeDao.flush();

        assertEquals(2, m_nodeDao.countAll());
        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));
        
        final Collection<LinkableNode> linkables = m_linkd.getLinkableNodes();
        assertEquals(2, linkables.size());
        
        for (LinkableNode lnode: linkables) {
            assertEquals(true, lnode.hasCdpInterfaces());
            assertEquals(1, lnode.getCdpInterfaces().size());
        }
        

        assertEquals(0,m_dataLinkInterfaceDao.countAll());
                                        
        assertEquals(2, m_linkd.getLinkableNodesOnPackage("example1").size());

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        assertEquals(1,links.size());
        
        final DataLinkInterface link = links.get(0);
        
        printLink(link);
        
        assertEquals(ciscows.getId(), link.getNode().getId());
        assertEquals(52, link.getIfIndex().intValue());
        assertEquals(ciscorouter.getId(), link.getNodeParentId());
        assertEquals(3,link.getParentIfIndex().intValue());
    }
    
    /*
    *
    * ACCESSPOINT:  GigabitEthernet (but no ifindex because no snmp on) ------> port  2/39 (ifindex 47):CISCO_WS_C2948_IP
    * 
    */
   @Test
   @JUnitSnmpAgents(value={
           @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt")
   })
   public void testCiscoAccessPointCiscoWsUsingCdp() throws Exception {
       Package example1 = m_linkdConfig.getPackage("example1");
       example1.setUseLldpDiscovery(false);
       example1.setUseOspfDiscovery(false);
       example1.setUseIpRouteDiscovery(false);
       example1.setUseBridgeDiscovery(false);
       example1.setUseCdpDiscovery(true);
       
       example1.setSaveRouteTable(false);
       example1.setSaveStpNodeTable(false);
       example1.setSaveStpInterfaceTable(false);
       example1.setEnableVlanDiscovery(false);

       m_nodeDao.save(getCiscoWsC2948());
       m_nodeDao.save(getNodeWithoutSnmp(ACCESSPOINT_NAME, ACCESSPOINT_IP));
       m_nodeDao.flush();

       assertEquals(2, m_nodeDao.countAll());
       final OnmsNode ciscoap = m_nodeDao.findByForeignId("linkd", ACCESSPOINT_NAME);
       final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

       assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
       assertTrue(!m_linkd.scheduleNodeCollection(ciscoap.getId()));

       assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
       
       final Collection<LinkableNode> linkables = m_linkd.getLinkableNodes();
       assertEquals(1, linkables.size());
       
       for (LinkableNode lnode: linkables) {
           if (ciscows.getId() == lnode.getNodeId()) {
               assertEquals(true, lnode.hasCdpInterfaces());
               assertEquals(1, lnode.getCdpInterfaces().size());
           } else {
               assertTrue("Found node not added!!!!!",false);
           }
       }
       

       assertEquals(0,m_dataLinkInterfaceDao.countAll());
               
                       
       assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());

       assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

       
       final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
       assertEquals(1,links.size());
       
       final DataLinkInterface ciscoaplinktociscows = links.get(0);
       
       assertEquals(ciscoap.getId(), ciscoaplinktociscows.getNode().getId());
       assertEquals(-1, ciscoaplinktociscows.getIfIndex().intValue());
       assertEquals(ciscows.getId(), ciscoaplinktociscows.getNodeParentId());
       assertEquals(47,ciscoaplinktociscows.getParentIfIndex().intValue());
               
   }

}
