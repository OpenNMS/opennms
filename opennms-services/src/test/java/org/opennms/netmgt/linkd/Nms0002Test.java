/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.nb.TestNetworkBuilder.RDeEssnBrue_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RDeEssnBrue_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RDeEssnBrue_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RNewt103_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RNewt103_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RNewt103_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RPict001_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RPict001_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.RPict001_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Rluck001_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Rluck001_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Rluck001_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue081_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue081_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue081_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue121_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue121_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue121_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue142_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue142_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue142_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue165_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue165_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SDeEssnBrue165_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Sluck001_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Sluck001_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Sluck001_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Sluck001_SNMP_RESOURCE_VLAN100;
import static org.opennms.netmgt.nb.TestNetworkBuilder.Sluck001_SNMP_RESOURCE_VLAN950;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.nb.Nms0002NetworkBuilder;

public class Nms0002Test extends LinkdTestBuilder {

	Nms0002NetworkBuilder builder = new Nms0002NetworkBuilder();
    /*
     *
     *     nodelabel           |  snmpifname  | ifindex |       parent       |  parentif  | parentifindex 
     *     --------------------+--------------+---------+--------------------+------------+---------------
     *      r-de-pots-amka-001 | Fa6/0/0      |     164 | r-de-juet-luck-001 | ge-0/0/0.0 |           510
     *      r-de-juet-luck-001 | ge-0/0/2.100 |     549 | s-de-juet-luck-001 | Fa0/1      |         10001
     *      r-de-juet-luck-001 | ge-0/0/2.950 |     550 | s-de-juet-luck-001 | Fa0/1      |         10001     *  
     * 
     * The links are mostly from the bridge forwarding table protocol in fact the 
     * ge-0/0/2.100 and ge-0/0/2.950 are logical interface for vlan 100 and
     * vlan 950 on juniper router. with the same mac address 54e032ef3102
     * So both vlan 100 and vlan 950 will address the same link
     * No way to get layer 2 link because no STP is enabled on Juniper device
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = Rluck001_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = Sluck001_SNMP_RESOURCE)
    })
    public void testNetworkLinksCiscoJuniperLldp() throws Exception {
        
        m_nodeDao.save(builder.getRluck001());
        m_nodeDao.save(builder.getSluck001());
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

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
       
        for ( LinkableNode linkableNode: m_linkd.getLinkableNodesOnPackage("example1")) {
            assertNotNull(linkableNode.getLldpChassisId());
        }

                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = Sluck001_SNMP_RESOURCE)
    })
    public void testNetworkLinksCiscoJuniperVlan() throws Exception {
        
        m_nodeDao.save(builder.getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(false);

        example1.setEnableVlanDiscovery(true);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
       
        assertEquals(7, m_vlanDao.countAll());
                        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = Rluck001_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = Sluck001_SNMP_RESOURCE)
    })
    public void testNetworkLinksCiscoJuniperVlan1() throws Exception {
        
        m_nodeDao.save(builder.getRluck001());
        m_nodeDao.save(builder.getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(true);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = Rluck001_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = Sluck001_SNMP_RESOURCE_VLAN100)
    })
    public void testNetworkLinksCiscoJuniperVlan100() throws Exception {
        
        m_nodeDao.save(builder.getRluck001());
        m_nodeDao.save(builder.getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(true);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(2,m_dataLinkInterfaceDao.countAll());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = Rluck001_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = Sluck001_SNMP_RESOURCE_VLAN950)
    })
    public void testNetworkLinksCiscoJuniperVlan950() throws Exception {
        
        m_nodeDao.save(builder.getRluck001());
        m_nodeDao.save(builder.getSluck001());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseBridgeDiscovery(true);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerJuniper = m_nodeDao.findByForeignId("linkd", Rluck001_NAME);
        final OnmsNode switchCisco = m_nodeDao.findByForeignId("linkd", Sluck001_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerJuniper.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switchCisco.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerJuniper.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switchCisco.getId()));
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        assertEquals(2,m_dataLinkInterfaceDao.countAll());
    }

    /*
     * fixed a fake link found using cdp:
     * caused by duplicated ip address
s     * r-uk-nott-newt-103:Fa0:(1)<------>(4):Fa3:r-ro-suce-pict-001
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = RPict001_IP, port = 161, resource = RPict001_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = RNewt103_IP, port = 161, resource = RNewt103_SNMP_RESOURCE)
    })
    public void testCdpFakeLinkRoUk() throws Exception {
        
        m_nodeDao.save(builder.getRPict001());
        m_nodeDao.save(builder.getRNewt103());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseIsisDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(true);
        example1.setUseBridgeDiscovery(false);

        example1.setEnableVlanDiscovery(false);

        example1.setSaveStpNodeTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);

        final OnmsNode routerRo = m_nodeDao.findByForeignId("linkd", RPict001_NAME);
        final OnmsNode routerUk = m_nodeDao.findByForeignId("linkd", RNewt103_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(routerRo.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(routerUk.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(routerRo.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(routerUk.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
                
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
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
            @JUnitSnmpAgent(host = RDeEssnBrue_IP, port = 161, resource = RDeEssnBrue_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SDeEssnBrue081_IP, port = 161, resource = SDeEssnBrue081_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SDeEssnBrue121_IP, port = 161, resource = SDeEssnBrue121_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SDeEssnBrue142_IP, port = 161, resource = SDeEssnBrue142_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SDeEssnBrue165_IP, port = 161, resource = SDeEssnBrue165_SNMP_RESOURCE)
    })
    public void testCiscoAlcatelEssnBrueLldp() {
        
        m_nodeDao.save(builder.getRDeEssnBrue());
        m_nodeDao.save(builder.getSDeEssnBrue081());
        m_nodeDao.save(builder.getSDeEssnBrue121());
        m_nodeDao.save(builder.getSDeEssnBrue142());
        m_nodeDao.save(builder.getSDeEssnBrue165());
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
