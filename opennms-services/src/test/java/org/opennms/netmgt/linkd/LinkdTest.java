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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_C870_BRIDGEID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_C870_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_C870_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_BRIDGEID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.CISCO_WS_C2948_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DARWIN_10_8_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DARWIN_10_8_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LINUX_UBUNTU_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.LINUX_UBUNTU_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NETGEAR_SW_108_BRIDGEID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NETGEAR_SW_108_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.NETGEAR_SW_108_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.WORKSTATION_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.WORKSTATION_NAME;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsStpInterface;
import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsStpNode.BridgeBaseType;
import org.opennms.netmgt.model.OnmsStpNode.StpProtocolSpecification;
import org.opennms.netmgt.model.topology.AtInterface;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.nb.Nms101NetworkBuilder;
import org.opennms.netmgt.nb.Nms7467NetworkBuilder;
import org.springframework.transaction.annotation.Transactional;

public class LinkdTest extends LinkdTestBuilder {

	Nms7467NetworkBuilder builder = new Nms7467NetworkBuilder();
	Nms101NetworkBuilder builder1 = new Nms101NetworkBuilder();

    @Test
    @Transactional
    public void testDefaultConfiguration() throws Exception {
    	m_nodeDao.save(builder1.getExampleCom());
    	m_nodeDao.save(builder1.getLaptop());
    	m_nodeDao.save(builder1.getCisco7200a());
    	m_nodeDao.save(builder1.getCisco7200b());
    	m_nodeDao.save(builder1.getCisco3700());
    	m_nodeDao.save(builder1.getCisco2691());
    	m_nodeDao.save(builder1.getCisco1700());
    	m_nodeDao.save(builder1.getCisco3600());
    	m_nodeDao.flush();

        assertEquals(true,m_linkdConfig.useBridgeDiscovery());
        assertEquals(true,m_linkdConfig.useOspfDiscovery());
        assertEquals(true,m_linkdConfig.useIpRouteDiscovery());
        assertEquals(true,m_linkdConfig.useLldpDiscovery());
        assertEquals(true,m_linkdConfig.useCdpDiscovery());
        assertEquals(true,m_linkdConfig.useIsIsDiscovery());
        
        assertEquals(true,m_linkdConfig.saveRouteTable());
        assertEquals(true,m_linkdConfig.saveStpNodeTable());
        assertEquals(true,m_linkdConfig.saveStpInterfaceTable());
        
        assertEquals(true, m_linkdConfig.isVlanDiscoveryEnabled());


        assertEquals(false, m_linkdConfig.isAutoDiscoveryEnabled());
        assertEquals(false, m_linkdConfig.forceIpRouteDiscoveryOnEthernet());

        assertEquals(false, m_linkdConfig.hasClassName(".1.3.6.1.4.1.2636.1.1.1.1.9"));
                
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.9.13.3.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.10.27.4.1.2.4"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.10.27.4.1.2.2"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.10.27.4.1.2.11"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.16.4.3.5"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ThreeComVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.16.4.3.6"));

        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.8.43"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.43.1.8.61"));

        assertEquals("org.opennms.netmgt.linkd.snmp.RapidCityVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.45.3.61.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.RapidCityVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.45.3.35.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.RapidCityVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.45.3.53.1"));
        
        assertEquals("org.opennms.netmgt.linkd.snmp.IntelVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.343.5.1.5"));

        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.1"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.3"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.7"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.8"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.11"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.6"));
        assertEquals("org.opennms.netmgt.linkd.snmp.Dot1qStaticVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.11.2.3.7.11.50"));

        
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.1.300"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.1.122"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.1.616"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.5.42"));
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.9.5.59"));

        assertEquals("org.opennms.netmgt.linkd.snmp.ExtremeNetworkVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.1916.2.11"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ExtremeNetworkVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.1916.2.14"));
        assertEquals("org.opennms.netmgt.linkd.snmp.ExtremeNetworkVlanTable", m_linkdConfig.getVlanClassName(".1.3.6.1.4.1.1916.2.63"));

        assertEquals("org.opennms.netmgt.linkd.snmp.IpCidrRouteTable", m_linkdConfig.getDefaultIpRouteClassName());
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.3224.1.51"));
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.9.1.569"));
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.9.5.42"));
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", m_linkdConfig.getIpRouteClassName(".1.3.6.1.4.1.8072.3.2.255"));

        final OnmsNode laptop = m_nodeDao.findByForeignId("linkd", "laptop");
        final OnmsNode cisco3600 = m_nodeDao.findByForeignId("linkd", "cisco3600");
        
        assertTrue(m_linkd.scheduleNodeCollection(laptop.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(cisco3600.getId()));

        SnmpCollection snmpCollLaptop = m_linkd.getSnmpCollection(laptop.getId(), laptop.getPrimaryInterface().getIpAddress(), laptop.getSysObjectId(), "example1");
        assertEquals(true, snmpCollLaptop.getCollectBridge());
        assertEquals(true, snmpCollLaptop.getCollectStp());
        assertEquals(true, snmpCollLaptop.getCollectCdp());
        assertEquals(true, snmpCollLaptop.getCollectIpRoute());
        assertEquals(true, snmpCollLaptop.getCollectOspf());
        assertEquals(true, snmpCollLaptop.getCollectLldp());

        assertEquals(false, snmpCollLaptop.collectVlanTable());
        
        assertEquals("org.opennms.netmgt.linkd.snmp.IpRouteTable", snmpCollLaptop.getIpRouteClass());
        assertEquals("example1", snmpCollLaptop.getPackageName());
        assertEquals(true, m_linkd.saveRouteTable("example1"));
        assertEquals(true, m_linkd.saveStpNodeTable("example1"));
        assertEquals(true, m_linkd.saveStpInterfaceTable("example1"));

        SnmpCollection snmpCollcisco3600 = m_linkd.getSnmpCollection(cisco3600.getId(), cisco3600.getPrimaryInterface().getIpAddress(), cisco3600.getSysObjectId(), "example1");

        assertEquals(true, snmpCollcisco3600.getCollectBridge());
        assertEquals(true, snmpCollcisco3600.getCollectStp());
        assertEquals(true, snmpCollcisco3600.getCollectCdp());
        assertEquals(true, snmpCollcisco3600.getCollectIpRoute());
        assertEquals(true, snmpCollcisco3600.getCollectOspf());
        assertEquals(true, snmpCollcisco3600.getCollectLldp());

        assertEquals(true, snmpCollcisco3600.collectVlanTable());
        assertEquals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable", snmpCollcisco3600.getVlanClass());
        
        assertEquals("org.opennms.netmgt.linkd.snmp.IpCidrRouteTable", snmpCollcisco3600.getIpRouteClass());
        assertEquals("example1", snmpCollcisco3600.getPackageName());

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.getForceIpRouteDiscoveryOnEthernet());
        
        final Enumeration<Package> pkgs = m_linkdConfig.enumeratePackage();
        example1 = pkgs.nextElement();
        assertEquals("example1", example1.getName());
        assertEquals(false, pkgs.hasMoreElements());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt")
    })
    public void testCiscoWsC2948Collection() throws Exception {
        
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();
        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);
        example1.setForceIpRouteDiscoveryOnEthernet(true);
        
        final OnmsNode ciscosw = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscosw.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscosw.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());
        LinkableNode linkNode = m_linkd.getLinkableNodesOnPackage("example1").iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has only one route with valid next hop must be valid but type is ethernet so skipped
        // but it is itself so 0
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 5 
        assertEquals(2,m_ipRouteInterfaceDao.countAll());
        
        assertEquals(5, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(CISCO_WS_C2948_IP)).getName();

        assertEquals("example1", packageName);
        
        assertEquals(1,linkNode.getBridgeIdentifiers().size());

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
        
        final List<AtInterface> atInterfaces = m_linkd.getAtInterfaces(packageName, "0002baaacffe");
        assertNotNull(atInterfaces);
        assertEquals(1, atInterfaces.size());
        AtInterface at = atInterfaces.get(0);
        assertEquals(CISCO_WS_C2948_IP,at.getIpAddress().getHostAddress());
        assertEquals(3, at.getIfIndex().intValue());
        // Now Let's test the database
        final Criteria criteria = new Criteria(OnmsIpRouteInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", ciscosw.getId()));

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
        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.flush();
        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);

        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ciscorouter.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());
        LinkableNode linkNode = m_linkd.getLinkableNodesOnPackage("example1").iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 0 route (next hop must be valid!) 
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(CISCO_C870_IP)).getName();

        assertEquals("example1", packageName);
        
        assertEquals(1,linkNode.getBridgeIdentifiers().size());

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

        final Set<String> macAddresses = m_linkd.getMacAddressesOnPackage(packageName);
        assertEquals(2, macAddresses.size());
        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName, "001f6cd034e7");
        assertNotNull(ats);
        
        assertEquals(3, ats.size());
        for (final AtInterface at :ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.1"))
                assertEquals(12, at.getIfIndex().intValue());
            else if( at.getIpAddress().getHostAddress().equals("172.20.2.1"))
                assertEquals(13, at.getIfIndex().intValue());
            else if( at.getIpAddress().getHostAddress().equals("10.255.255.2"))
                assertEquals(12, at.getIfIndex().intValue());
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        ats = m_linkd.getAtInterfaces(packageName, "00000c03b09e");
        assertEquals(1, ats.size());
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("65.41.39.146"))
                assertEquals(14, at.getIfIndex().intValue());
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //0 atinterface in database
        assertEquals(4, m_atInterfaceDao.countAll());

        final Criteria criteria = new Criteria(OnmsIpRouteInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", ciscorouter.getId()));
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
        m_nodeDao.save(builder.getNetGearSw108());
        m_nodeDao.flush();

        final OnmsNode ngsw108 = m_nodeDao.findByForeignId("linkd", NETGEAR_SW_108_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(ngsw108.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(ngsw108.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());
        LinkableNode linkNode = m_linkd.getLinkableNodesOnPackage("example1").iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 0 route (next hop must be valid!) no ip route table
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(NETGEAR_SW_108_IP)).getName();

        assertEquals("example1", packageName);
        
        assertEquals(1,linkNode.getBridgeIdentifiers().size());

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
        
        final Set<String> macAddresses = m_linkd.getMacAddressesOnPackage(packageName);
        assertNotNull(macAddresses);
        assertEquals(1, macAddresses.size());
        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName, "00223ff00b7b");
        
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.8"))
                assertTrue(at.getIfIndex().intValue() == -1);
            else 
                fail("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address");
        }

        
        // Now Let's test the database
        //1 atinterface in database: has itself in ipadress to media
        assertEquals(1, m_atInterfaceDao.findAll().size());

        final Criteria criteria = new Criteria(OnmsIpRouteInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", ngsw108.getId()));
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
        m_nodeDao.save(builder.getLinuxUbuntu());
        m_nodeDao.flush();
        
        final OnmsNode linux = m_nodeDao.findByForeignId("linkd", LINUX_UBUNTU_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(linux.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(linux.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());
        LinkableNode linkNode = m_linkd.getLinkableNodesOnPackage("example1").iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 0 route (next hop must be valid!) no ip route table
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(LINUX_UBUNTU_IP)).getName();

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
        
        final Set<String> macAddresses = m_linkd.getMacAddressesOnPackage(packageName);
        assertNotNull(macAddresses);
        assertEquals(1, macAddresses.size());

        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName, "406186e28b53");
        assertEquals("should have saved 1 ip to mac",1, ats.size());        
        
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.14"))
                assertTrue(at.getIfIndex().intValue() == 4);
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //0 atinterface in database
        assertEquals(0, m_atInterfaceDao.findAll().size());

        final Criteria criteria = new Criteria(OnmsIpRouteInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", linux.getId()));
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
        m_nodeDao.save(builder.getDarwin108());
        m_nodeDao.flush();
        
        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);

        m_linkdConfig.update();

        final OnmsNode mac = m_nodeDao.findByForeignId("linkd", DARWIN_10_8_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mac.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(mac.getId()));

        // linkd has 1 linkable node
        assertEquals(1, m_linkd.getLinkableNodesOnPackage("example1").size());
        LinkableNode linkNode = m_linkd.getLinkableNodesOnPackage("example1").iterator().next();
        
        // linkabble node is not null
        assertTrue(linkNode != null);
        
        // has 1 route (next hop must be valid!) no ip route table
        assertEquals(0,linkNode.getRouteInterfaces().size());
        // has 0 vlan 
        assertEquals(0, m_vlanDao.countAll());
        
        String packageName = m_linkdConfig.getFirstPackageMatch(InetAddressUtils.addr(DARWIN_10_8_IP)).getName();

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
        
        final Set<String> macAddresses = m_linkd.getMacAddressesOnPackage(packageName);
        assertNotNull(macAddresses);
        assertEquals(1, macAddresses.size());

        List<AtInterface> ats = m_linkd.getAtInterfaces(packageName, "0026b0ed8fb8");
        assertNotNull(ats);
        assertEquals("should have saved 1 ip to mac",1, ats.size());        
        
        for (AtInterface at : ats) {
            if( at.getIpAddress().getHostAddress().equals("172.20.1.28"))
                assertTrue(at.getIfIndex().intValue() == 4);
            else 
                assertTrue("ip: "+ at.getIpAddress().getHostAddress() + "does not match any known ip address", false);
        }

        
        // Now Let's test the database
        //0 atinterface in database
        assertEquals(0, m_atInterfaceDao.findAll().size());

        final Criteria criteria = new Criteria(OnmsIpRouteInterface.class);
        criteria.setAliases(Arrays.asList(new Alias[] {
            new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
        criteria.addRestriction(new EqRestriction("node.id", mac.getId()));
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
        m_nodeDao.save(builder.getNodeWithoutSnmp(WORKSTATION_NAME,WORKSTATION_IP));
        m_nodeDao.flush();
        final OnmsNode workstation = m_nodeDao.findByForeignId("linkd", WORKSTATION_NAME);

        assertTrue(!m_linkd.scheduleNodeCollection(workstation.getId()));
        
    }


    
    @Test
    public void testDefaultConfiguration2() throws MarshalException, ValidationException, IOException {
        
        assertEquals(5, m_linkdConfig.getThreads());
        assertEquals(3600000, m_linkdConfig.getInitialSleepTime());
        assertEquals(18000000, m_linkdConfig.getSnmpPollInterval());
        assertEquals(1800000, m_linkdConfig.getDiscoveryLinkInterval());
        

        
        assertEquals(false, m_linkdConfig.isAutoDiscoveryEnabled());
        assertEquals(true,m_linkdConfig.isVlanDiscoveryEnabled());
        assertEquals(true,m_linkdConfig.useCdpDiscovery());
        assertEquals(true,m_linkdConfig.useIpRouteDiscovery());
        assertEquals(true,m_linkdConfig.useBridgeDiscovery());
        assertEquals(true,m_linkdConfig.useOspfDiscovery());
        assertEquals(true,m_linkdConfig.useLldpDiscovery());
        assertEquals(true,m_linkdConfig.useIsIsDiscovery());

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
        assertEquals(false,example1.hasEnableVlanDiscovery());
        assertEquals(false,example1.hasForceIpRouteDiscoveryOnEthernet());
        assertEquals(false,example1.hasSaveRouteTable());
        assertEquals(false,example1.hasSaveStpInterfaceTable());
        assertEquals(false,example1.hasSaveStpNodeTable());
        assertEquals(false,example1.hasSnmp_poll_interval());
        assertEquals(false,example1.hasUseBridgeDiscovery());
        assertEquals(false,example1.hasUseCdpDiscovery());
        assertEquals(false,example1.hasUseIpRouteDiscovery());
        assertEquals(false,example1.hasUseIsisDiscovery());
        
        assertEquals(false, m_linkdConfig.isInterfaceInPackage(InetAddressUtils.addr(CISCO_C870_IP), example1));
        
        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();
        
        m_linkdConfig.update();
        
        assertEquals(true, m_linkdConfig.isInterfaceInPackage(InetAddressUtils.addr(CISCO_C870_IP), example1));
        assertEquals(true, m_linkdConfig.isInterfaceInPackage(InetAddressUtils.addr(CISCO_WS_C2948_IP), example1));
        
        final OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        final OnmsNode ciscows = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);
        assertTrue(m_linkd.scheduleNodeCollection(ciscorouter.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(ciscows.getId()));

        LinkableNode lciscorouter = m_linkd.removeNode("example1", InetAddressUtils.addr(CISCO_C870_IP));
        assertNotNull(lciscorouter);
        assertEquals(ciscorouter.getId().intValue(),lciscorouter.getNodeId() );

        assertEquals(1, m_linkd.getActivePackages().size());
    }
    

    @Test
    public void testGetNodeidFromIp() throws UnknownHostException, SQLException {
        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.flush();
        
        HibernateEventWriter db = (HibernateEventWriter)m_linkd.getQueryManager();
        
        final OnmsNode node = db.getNodeidFromIp(InetAddressUtils.addr(CISCO_C870_IP)).get(0);
        assertEquals(m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME).getId(), node.getId());
    }
    
    @Test 
    @Transactional
    public void testGetIfIndexByName() throws SQLException {
        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        OnmsNode ciscorouter = m_nodeDao.findByForeignId("linkd", CISCO_C870_NAME);
        assertEquals("FastEthernet2", ciscorouter.getSnmpInterfaceWithIfIndex(3).getIfDescr());

        OnmsNode ciscosw = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);
        assertEquals("2/44", ciscosw.getSnmpInterfaceWithIfIndex(52).getIfName());

        HibernateEventWriter db = (HibernateEventWriter)m_linkd.getQueryManager();
        assertEquals(3, db.getIfIndexByName(ciscorouter.getId(), "FastEthernet2"));
        assertEquals(52,db.getIfIndexByName(ciscosw.getId(), "2/44"));
        
    }
}
