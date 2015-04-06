/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.ACCESSPOINT_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.ACCESSPOINT_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.WORKSTATION_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.WORKSTATION_MAC;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.WORKSTATION_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_C870_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_C870_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_C870_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DARWIN_10_8_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DARWIN_10_8_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DARWIN_10_8_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LINUX_UBUNTU_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LINUX_UBUNTU_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LINUX_UBUNTU_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NETGEAR_SW_108_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NETGEAR_SW_108_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NETGEAR_SW_108_SNMP_RESOURCE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.AtInterface;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.nb.Nms7467NetworkBuilder;

public class Nms7467Test extends LinkdTestBuilder {

	Nms7467NetworkBuilder builder = new Nms7467NetworkBuilder();
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=CISCO_C870_IP, port=161, resource=CISCO_C870_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DARWIN_10_8_IP, port=161, resource=DARWIN_10_8_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource=NETGEAR_SW_108_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=LINUX_UBUNTU_IP, port=161, resource=LINUX_UBUNTU_SNMP_RESOURCE)
    })
    // mrgarrison:172.20.1.5:-1    -------- ciscoswitch:172.20.1.7:47 ---cdp 
    // workstation:172.20.1.101:-1 -------- ciscoswitch:172.20.1.7:47 ---bridge 
    // cisco870:172.20.1.1:3       -------- ciscoswitch:172.20.1.7:52 ---cdp 
    // cisco870:172.20.1.1:1       -------- ciscoswitch:172.20.1.7:52 ---bridge 
    // cisco870:172.20.1.1:13      -------- ciscoswitch:172.20.1.7:52 ---bridge 
    // cisco870:172.20.1.1:12      -------- ciscoswitch:172.20.1.7:52 ---bridge 
    // linuxubuntu:172.20.1.14:4   -------- ciscoswitch:172.20.1.7:11 ---bridge 
    // ng108switch:172.20.1.8:8    -------- ciscoswitch:172.20.1.7:9  ---bridge 
    // darwin108:172.20.1.28:4     -------- ng108switch:172.20.1.8:1  ---bridge 
    public void testAllLink() throws Exception {

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(true);
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseCdpDiscovery(true);
        example1.setForceIpRouteDiscoveryOnEthernet(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);

        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.save(builder.getNetGearSw108());
        m_nodeDao.save(builder.getDarwin108());       
        m_nodeDao.save(builder.getLinuxUbuntu());
        m_nodeDao.save(builder.getNodeWithoutSnmp(ACCESSPOINT_NAME, ACCESSPOINT_IP));
        m_nodeDao.save(builder.getNodeWithoutSnmp(WORKSTATION_NAME, WORKSTATION_IP));

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
        
        final Collection<LinkableNode> linkables = m_linkd.getLinkableNodesOnPackage("example1");
        assertEquals(5, linkables.size());       

        assertEquals(0,m_dataLinkInterfaceDao.countAll());
                                       
        assertEquals(5, m_linkd.getLinkableNodesOnPackage("example1").size());

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(8,m_dataLinkInterfaceDao.countAll());
        
        //
        final DataLinkInterface mactongsw108link = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(mac.getId(),4).iterator().next();
        
        assertEquals(mac.getId(), mactongsw108link.getNode().getId());
        assertEquals(4,mactongsw108link.getIfIndex().intValue());
        assertEquals(ngsw108.getId(), mactongsw108link.getNodeParentId());
        assertEquals(1, mactongsw108link.getParentIfIndex().intValue());        

        final DataLinkInterface ngsw108linktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(ngsw108.getId(), 8).iterator().next();
        
        assertEquals(ngsw108.getId(), ngsw108linktociscows.getNode().getId());
        assertEquals(8,ngsw108linktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), ngsw108linktociscows.getNodeParentId());
        assertEquals(9, ngsw108linktociscows.getParentIfIndex().intValue());

        final DataLinkInterface ciscorouterlinktociscows2 = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(ciscows.getId(), 52).iterator().next();
        assertEquals(ciscows.getId(), ciscorouterlinktociscows2.getNode().getId());
        assertEquals(52, ciscorouterlinktociscows2.getIfIndex().intValue());
        assertEquals(ciscorouter.getId(), ciscorouterlinktociscows2.getNodeParentId());
        assertEquals(3, ciscorouterlinktociscows2.getParentIfIndex().intValue());

        final DataLinkInterface linuxubuntulinktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(linux.getId(), 4).iterator().next();
        
        assertEquals(linux.getId(), linuxubuntulinktociscows.getNode().getId());
        assertEquals(4,linuxubuntulinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), linuxubuntulinktociscows.getNodeParentId());
        assertEquals(11, linuxubuntulinktociscows.getParentIfIndex().intValue());

        final DataLinkInterface workstationlinktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(workstation.getId(), -1).iterator().next();
        
        assertEquals(workstation.getId(), workstationlinktociscows.getNode().getId());
        assertEquals(-1,workstationlinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), workstationlinktociscows.getNodeParentId());
        assertEquals(47, workstationlinktociscows.getParentIfIndex().intValue());

        final DataLinkInterface ciscoaplinktociscows = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(ciscoap.getId(), -1).iterator().next();
        
        assertEquals(ciscoap.getId(), ciscoaplinktociscows.getNode().getId());
        assertEquals(-1, ciscoaplinktociscows.getIfIndex().intValue());
        assertEquals(ciscows.getId(), ciscoaplinktociscows.getNodeParentId());
        assertEquals(47,ciscoaplinktociscows.getParentIfIndex().intValue());
        

        Thread.sleep(5000);

        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(linux.getId()));

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        for (OnmsAtInterface onmsat: m_atInterfaceDao.findAll()) {
            printAtInterface(onmsat);
        }
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll())
            printLink(link);

        assertEquals(8,m_dataLinkInterfaceDao.countAll());

    }

    // mrmakay:172.20.1.1:13      -------- ciscoswitch:172.20.1.7:52 ---bridge
    // mrmakay:172.20.2.1:12      -------- ciscoswitch:172.20.1.7:52 ---bridge 
    // the point is that all three interface share the same mac address "001f6cd034e7"
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=CISCO_C870_IP, port=161, resource=CISCO_C870_SNMP_RESOURCE)
    })
    public void testBridgeLinkCiscoSwitchVsRouter() throws Exception {

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(true);
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setForceIpRouteDiscoveryOnEthernet(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);

        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.save(builder.getCiscoWsC2948());

        m_nodeDao.flush();


        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        for (OnmsAtInterface onmsat: m_atInterfaceDao.findAll()) {
            printAtInterface(onmsat);
        }
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll())
            printLink(link);
        
        assertEquals(2,m_dataLinkInterfaceDao.countAll());

    }

    /*
     *  DARWIN_10_8:port4   ------> port 1 :NETGEAR_SW_108
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DARWIN_10_8_IP, port=161, resource=DARWIN_10_8_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource=NETGEAR_SW_108_SNMP_RESOURCE)
    })
    public void testLinkDarwinNetgear() throws Exception {
        m_nodeDao.save(builder.getNetGearSw108());
        m_nodeDao.save(builder.getDarwin108());
        m_nodeDao.flush();

        final OnmsNode mac = m_nodeDao.findByForeignId("linkd", DARWIN_10_8_NAME);
        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mac.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mac.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        String macpackageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(DARWIN_10_8_IP)).getName();
        String ngsw108packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(NETGEAR_SW_108_IP)).getName();

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
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource=NETGEAR_SW_108_SNMP_RESOURCE)
    })
    public void testLinkNetgearCiscoWs() throws Exception {
    	Package example1 = m_linkdConfig.getPackage("example1");
        example1.setForceIpRouteDiscoveryOnEthernet(false);

    	m_nodeDao.save(builder.getNetGearSw108());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));        
        
        String ciscowspackageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(CISCO_WS_C2948_IP)).getName();
        String ngsw108packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(NETGEAR_SW_108_IP)).getName();

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
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=LINUX_UBUNTU_IP, port=161, resource=LINUX_UBUNTU_SNMP_RESOURCE)
    })
    public void testLinuxUbuntuCiscoWs() throws Exception {
        m_nodeDao.save(builder.getLinuxUbuntu());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        final OnmsNode linuxubuntu = m_nodeDao.findByForeignId("linkd", LINUX_UBUNTU_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(linuxubuntu.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(linuxubuntu.getId()));
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        String ciscowspackageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(CISCO_WS_C2948_IP)).getName();
        String linuxubuntupackageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(LINUX_UBUNTU_IP)).getName();

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
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE)
    })
    public void testLinkWorkstationCiscoWs() throws Exception {
        m_nodeDao.save(builder.getNodeWithoutSnmp(WORKSTATION_NAME, WORKSTATION_IP));
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        final OnmsNode workstation = m_nodeDao.findByForeignId("linkd", WORKSTATION_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(!m_linkd.scheduleNodeCollection(workstation.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));

        assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());
        LinkableNode linkNode = m_linkd.getLinkableNodesOnPackage("example1").iterator().next();
        
        // linkable node is not null
        assertTrue(linkNode != null);
        
        final Set<String> macAddresses = m_linkd.getMacAddressesOnPackage("example1");
        assertEquals(2, macAddresses.size());

        //final Map<String, List<AtInterface>> mactoatinterfacemap = m_linkd.getAtInterfaces("example1");
        //assertEquals(2,mactoatinterfacemap.size());

        final List<AtInterface> ats = m_linkd.getAtInterfaces("example1", WORKSTATION_MAC);
        assertEquals(1, ats.size());
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        String ciscowspackageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(CISCO_WS_C2948_IP)).getName();

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
    
    /*
     *
     * CISCO_C870:  FastEthernet2 3  ------> port  2/44 (ifindex 52):CISCO_WS_C2948_IP
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=CISCO_C870_IP, port=161, resource=CISCO_C870_SNMP_RESOURCE)
    })
    public void testLinkCiscoRouterCiscoWsUsingCdp() throws Exception {
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseIsisDiscovery(false);
        example1.setUseCdpDiscovery(true);
        
        example1.setSaveRouteTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setEnableVlanDiscovery(false);

    	
    	m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        assertEquals(2, m_nodeDao.countAll());
        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));
        
        final Collection<LinkableNode> linkables = m_linkd.getLinkableNodesOnPackage("example1");
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
           @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE)
   })
   public void testLinkCiscoAccessPointCiscoWsUsingCdp() throws Exception {
       Package example1 = m_linkdConfig.getPackage("example1");
       example1.setUseLldpDiscovery(false);
       example1.setUseOspfDiscovery(false);
       example1.setUseIpRouteDiscovery(false);
       example1.setUseBridgeDiscovery(false);
       example1.setUseIsisDiscovery(false);
       example1.setUseCdpDiscovery(true);
       
       example1.setSaveRouteTable(false);
       example1.setSaveStpNodeTable(false);
       example1.setSaveStpInterfaceTable(false);
       example1.setEnableVlanDiscovery(false);

       m_nodeDao.save(builder.getCiscoWsC2948());
       m_nodeDao.save(builder.getNodeWithoutSnmp(ACCESSPOINT_NAME, ACCESSPOINT_IP));
       m_nodeDao.flush();

       assertEquals(2, m_nodeDao.countAll());
       final OnmsNode ciscoap = m_nodeDao.findByForeignId("linkd", ACCESSPOINT_NAME);
       final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

       assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));
       assertTrue(!m_linkd.scheduleNodeCollection(ciscoap.getId()));

       assertTrue(m_linkd.runSingleSnmpCollection(ciscows.getId()));
       
       final Collection<LinkableNode> linkables = m_linkd.getLinkableNodesOnPackage("example1");
       assertEquals(1, linkables.size());
       
       for (LinkableNode lnode: linkables) {
           if (ciscows.getId() == lnode.getNodeId()) {
               assertEquals(true, lnode.hasCdpInterfaces());
               assertEquals(2, lnode.getCdpInterfaces().size());
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
