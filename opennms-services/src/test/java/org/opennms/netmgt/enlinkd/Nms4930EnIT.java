/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DLINK2_SNMP_RESOURCE;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.opennms.netmgt.nb.Nms4930NetworkBuilder;

/*
 * port 24 of A is connected on port 10 of B 
dlink_DES-3026.properties
A)  00 1E 58 A3 2F CD —> 0.30.88.163.47.205 
self entry with bridgeport 0 and status SELF(4)
dlink_DES-3026.properties:.1.3.6.1.2.1.17.7.1.2.2.1.2.400.0.30.88.163.47.205 = INTEGER: 0
dlink_DES-3026.properties:.1.3.6.1.2.1.17.7.1.2.2.1.3.400.0.30.88.163.47.205 = INTEGER: 4

forwarding entry on B: learned(3) on port 10
dlink_DGS-3612G.properties:.1.3.6.1.2.1.17.7.1.2.2.1.2.400.0.30.88.163.47.205 = INTEGER: 10
dlink_DGS-3612G.properties:.1.3.6.1.2.1.17.7.1.2.2.1.3.400.0.30.88.163.47.205 = INTEGER: 3

BFT
   61 rows .1.3.6.1.2.1.17.7.1.2.2.1.2 {port}    —> 57 + 1 self + 2 duplicated + 1 without a valid status 
   60 rows .1.3.6.1.2.1.17.7.1.2.2.1.3 {learned} -> 57 + 1 self + 2 duplicated

   Port    ->BFT
 INTEGER: 0—>1

 INTEGER: 1—>1
 INTEGER: 2—>2
 INTEGER: 3—>1
 INTEGER: 4—>8
 INTEGER: 5—>2
 INTEGER: 6—>14
 INTEGER: 24—>30

   total number of valid mac entry= 57     
   total number of valid bft entry= 58

mac and port without learned
.1.3.6.1.2.1.17.7.1.2.2.1.2.437.0.4.97.116.70.3 = INTEGER: 24

2 mac with more then one entry…
0.25.47.15.206.25 —- 1 entry in bridgemaclink <—port 12 of bridge B
.1.3.6.1.2.1.17.7.1.2.2.1.2.423.0.25.47.15.206.25 = INTEGER: 24
.1.3.6.1.2.1.17.7.1.2.2.1.2.437.0.25.47.15.206.25 = INTEGER: 24
.1.3.6.1.2.1.17.7.1.2.2.1.3.423.0.25.47.15.206.25 = INTEGER: 3
.1.3.6.1.2.1.17.7.1.2.2.1.3.437.0.25.47.15.206.25 = INTEGER: 3

100.22.141.250.141.72— 2 entries in bridgemaclink<— port 10 of bridge B
.1.3.6.1.2.1.17.7.1.2.2.1.2.423.100.22.141.250.141.72 = INTEGER: 5
.1.3.6.1.2.1.17.7.1.2.2.1.2.554.100.22.141.250.141.72 = INTEGER: 2
.1.3.6.1.2.1.17.7.1.2.2.1.3.423.100.22.141.250.141.72 = INTEGER: 3
.1.3.6.1.2.1.17.7.1.2.2.1.3.554.100.22.141.250.141.72 = INTEGER: 3

dlink_DGS-3612G
B)  00 19 5B 12 59 80  —> 0.25.91.18.89.128

dlink_DGS-3612G.properties:.1.3.6.1.2.1.17.7.1.2.2.1.2.400.0.25.91.18.89.128 = INTEGER: 0
dlink_DGS-3612G.properties:.1.3.6.1.2.1.17.7.1.2.2.1.3.400.0.25.91.18.89.128 = INTEGER: 4

BFT
    1170 righe bridge .1.3.6.1.2.1.17.7.1.2.2.1.2 {port}
    1172 righe bridge .1.3.6.1.2.1.17.7.1.2.2.1.3 {learned}

INTEGER: 0      1

INTEGER: 1     35
INTEGER: 2     71
INTEGER: 3     29
INTEGER: 5    142
INTEGER: 6     47
INTEGER: 7      5
INTEGER: 8    123
INTEGER: 10   362
INTEGER: 12   163

   total number of valid mac entry= 976 + 1 self     
   total number of valid bft entry= 977 + 1 self


learned without port
.1.3.6.1.2.1.17.7.1.2.2.1.3.421.0.12.110.63.159.62 = INTEGER: 3
.1.3.6.1.2.1.17.7.1.2.2.1.3.434.160.11.186.21.140.140 = INTEGER: 3

learned with two different port
.1.3.6.1.2.1.17.7.1.2.2.1.2.250.0.33.85.50.21.128 = INTEGER: 8
.1.3.6.1.2.1.17.7.1.2.2.1.2.251.0.33.85.50.21.128 = INTEGER: 2
—————————

Backbone link Bridge A port 24 ---> Bridge B port 10
A INTEGER: 24:30 <—> B INTEGER: 10:362   ---> 
ubftA=         362={mac: on B:10 && not on A}         + 
                   {mac: on B:10 && not on A:24 && A0}+
                   {mac: on B:10 && on A:0}           +
                   {mac: on B:10 && on A:24}          =
ubftA=          30={mac: on A:24 && not on B}         +  
                   {mac: on A:24 && not on B:10}      +
                   {mac: on A:24 && on B:10}          =

48  ={mac: on B:10 && not on A:24}      +
     {mac: on A:24 && not on B:10}      +
     {mac: on A:24 && on B:10}          =
     {mac: on B:10 && on A:24}          +
    
21  ={mac: on B:10 && not on A:24 && not on A0}
1   ={mac: on B:10 && on A:0}
18  ={mac: on A:24 && not on B:10}
8   ={mac learned on B&A on A:24}={mac learned on A&B on B:10}

332 ={mac: on B:10 && not on A}

  4 ={mac: on A:24 && not on B}
    
ebft={mac: on B:10 not learned on A}   + 
     {mac learned on B&A on A:24}          +
     {mac learned on A not learned on B}   +  
     {mac learned on A&B on B:10}          =

ebftA=A INTEGER: 24  =  {mac: on A:24 && not on B} +  
                        {mac: on A:24 && on B:10}  = 12

ebftA=B INTEGER: 10  = {mac: on B:10 && not on A}  +
                       {mac: on B:10 && on A:24}   +
                       {mac: on B:10 && on A:0}    = 341


A INTEGER: 1    —>     1
A INTEGER: 2    —>     2
A INTEGER: 3    —>     1
A INTEGER: 4    —>     8
A INTEGER: 5    —>     2
A INTEGER: 6    —>    14

B INTEGER: 1   —>     35
B INTEGER: 2    —>    71
B INTEGER: 3    —>    29
B INTEGER: 5    —>   142
B INTEGER: 6    —>    47
B INTEGER: 7    —>     5
B INTEGER: 8    —>   123
B INTEGER: 12   —>   163

total bft entry ->   996

Total common macs: 48


28.175.247.99.211.133   A:24 B:1
44.39.215.231.204.157   A:24 B:2
0.27.17.234.145.51      A:24 B:2
0.28.240.199.177.167    A:24 B:2
0.28.240.209.132.63     A:24 B:2
0.38.90.172.103.30      A:24 B:2
28.175.247.2.208.85     A:24 B:3
0.19.73.251.29.180      A:24 B:3
0.12.41.220.192.118     A:24 B:5
0.128.72.52.204.255     A:24 B:5
0.29.229.164.96.74      A:24 B:12
0.26.129.0.25.120       A:24 B:12
0.25.47.15.206.25       A:24 B:12
0.26.77.139.22.142      A:24 B:12
240.125.104.113.31.137  A:24 B:12
240.125.104.118.197.101 A:24 B:12
0.22.157.70.254.26      A:24 B:12
0.15.144.34.58.26       A:24 B:12

0.30.88.163.47.205      A:0  B:10
100.22.141.250.141.73   A:1  B:10
100.22.141.250.141.72   A:5/2B:10
0.33.145.59.81.9        A:3  B:10
28.189.185.65.158.228   A:4  B:10
0.37.17.14.125.221      A:4  B:10
108.98.109.62.49.80     A:4  B:10
108.98.109.205.95.201   A:4  B:10
0.25.203.147.42.191     A:4  B:10
84.230.252.217.226.231  A:4  B:10
0.15.254.177.13.30      A:6  B:10
0.26.75.128.39.144      A:6  B:10
0.29.96.4.172.188       A:6  B:10
0.30.88.134.93.15       A:6  B:10
0.36.1.173.52.22        A:6  B:10
0.36.140.76.139.208     A:6  B:10
0.36.214.8.105.62       A:6  B:10
28.175.247.55.204.51    A:6  B:10
28.175.247.68.51.57     A:6  B:10
28.189.185.181.97.96    A:6  B:10
92.217.152.102.122.187  A:6  B:10
224.203.78.62.127.192   A:6  B:10

0.28.240.209.132.65     A:24 B:10
0.30.88.163.27.71       A:24 B:10
0.38.90.189.11.8        A:24 B:10
0.224.216.16.124.12     A:24 B:10
28.175.247.2.207.253    A:24 B:10
28.175.247.41.5.216     A:24 B:10
0.21.98.202.226.207     A:24 B:10
0.30.88.166.174.215     A:24 B:10

———————>
common macs on shared link: 8

0.28.240.209.132.65
0.30.88.163.27.71
0.38.90.189.11.8
0.224.216.16.124.12
28.175.247.2.207.253
28.175.247.41.5.216
0.21.98.202.226.207
0.30.88.166.174.215


 */
//   the topology is shown here...
//   (10.100.2.6:000ffeb10e26) --> <port 6:dlink1:port 24> ---<cloud>----<port 10:dlink2>
//                                                               |
//                                                           10.100.1.7:001e58a6aed7:101

public class Nms4930EnIT extends EnLinkdBuilderITCase {

	Nms4930NetworkBuilder builder = new Nms4930NetworkBuilder();
    String[] macsonbbport = { 
            "001e58a6aed7", 
            "00265abd0b08", 
            "1caff72905d8", 
            "1caff702cffd", 
            "00e0d8107c0c", 
            "001562cae2cf", 
            "001cf0d18441", 
            "001e58a31b47"
    };

    String[] macofdlink1onbbport = {
            "001cf0c7b1a7",
            "1caff702cffd",
            "2c27d7e7cc9d",
            "00265aac671e",
            "00169d46fe1a",
            "00192f0fce19",
            "00270e0a788a",
            "001e58a6aed7",
            "1c6f65b3599a",
            "1caff702d055",
            "1caff72905d8",
            "1caff763d385",
            "001de5a4604a",
            "00804834ccff",
            "001b11ea9133",
            "001cf0d18441",
            "001cf0d1843f",
            "001562cae2cf",
            "000c29dcc076",
            "f07d68711f89",
            "f07d6876c565",
            "001a81001978",
            "00265abd0b08",
            "001349fb1db4",
            "001e58a31b47",
            "001a4d8b168e",
            "0013774b4ab2",
            "00e0d8107c0c",
            "000f90223a1a",
            "00c0266a1d1d"      
    };
    @Before
    public void setUpNetwork4930() throws Exception {
    	builder.setNodeDao(m_nodeDao);
        builder.setIpNetToMediaDao(m_ipNetToMediaDao);
        builder.buildNetwork4930();
        // Adding a "node" with mac address 001e58a6aed7 found both on dlink1 port 24 and dlink2 port 10 
        builder.addMacNodeWithSnmpInterface("001e58a6aed7","10.100.1.7",101 );
        // Adding a "node" with mac address 000ffeb10e26 found on dlink1 port 6
        builder.addMacNode("000ffeb10e26","10.100.2.6" );
        assertEquals(4, m_nodeDao.countAll());
        assertEquals(2, m_ipNetToMediaDao.countAll());
        IpNetToMedia at0 = m_ipNetToMediaDao.findByPhysAddress("001e58a6aed7").get(0);
        assertNotNull(at0);
        assertEquals("10.100.1.7", at0.getNetAddress().getHostAddress());
        IpNetToMedia at1 = m_ipNetToMediaDao.findByPhysAddress("000ffeb10e26").get(0);
        assertNotNull(at1);
        assertEquals("10.100.2.6", at1.getNetAddress().getHostAddress());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE)
    })
    public void testNms4930Bft() throws Exception {
        final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));

        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        List<BridgeMacLink> links  = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(dlink1.getId());
        
        assertEquals(59,links.size());
        for (BridgeMacLink link: links) {
            System.err.println(link.printTopology());
            if (BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF ==  link.getBridgeDot1qTpFdbStatus())
                continue;
            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED,link.getBridgeDot1qTpFdbStatus());
            link.setBridgeMacLinkLastPollTime(link.getBridgeMacLinkCreateTime());
            m_bridgeMacLinkDao.save(link);
        }

        assertEquals(58,m_bridgeMacLinkDao.countAll());

        for (BridgeMacLink maclink: m_bridgeMacLinkDao.findAll()) {
                assertEquals(null,maclink.getBridgeDot1qTpFdbStatus());
                assertNotNull(maclink.getBridgePortIfIndex());
                assertNotNull(maclink.getBridgePort());
                assertNotNull(maclink.getNode());
                assertNotNull(maclink.getMacAddress());
                System.err.println(maclink.printTopology());
        }

        
    }
    /*
     * The main fact is that this devices have only the Bridge MIb walk
     * dlink_DES has STP disabled
     * dlink_DGS has STP enabled but root is itself
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930Network() throws Exception {
        
    	final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);
        final OnmsNode nodebetweendlink1dlink2 = m_nodeDao.findByForeignId("linkd", "10.100.1.7");
        final OnmsNode nodeonlink1dport6 = m_nodeDao.findByForeignId("linkd", "10.100.2.6");
        
        assertNotNull(nodebetweendlink1dlink2);
        assertNotNull(nodeonlink1dport6);
        
        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(dlink2.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(dlink1.getId()));
        checkTopologyDlink1(dlink1);

        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(dlink2.getId()));
        checkTopology(dlink1, dlink2, nodeonlink1dport6, nodebetweendlink1dlink2);
    }
    
    private void checkTopologyDlink1(OnmsNode dlink1) {
//      port: 1—>1
//      port: 2—>2
//      port: 3—>1
//      port: 4—>8
//      port: 5—>2
//      port: 6—>14
//      port: 24—>30
      assertEquals(0,m_bridgeBridgeLinkDao.countAll());
      
      assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 1).size());
      assertEquals(2, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 2).size());
      assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 3).size());
      assertEquals(8, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 4).size());
      assertEquals(2, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 5).size());
      assertEquals(14, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 6).size());
      assertEquals(30, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 24).size());

      assertEquals(58,m_bridgeMacLinkDao.countAll());
      assertEquals(2,m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes().size());
      assertEquals(0,m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes().size());

      for (BridgeMacLink maclink: m_bridgeMacLinkDao.findAll()) {
              assertEquals(null,maclink.getBridgeDot1qTpFdbStatus());
              assertNotNull(maclink.getBridgePortIfIndex());
              assertNotNull(maclink.getBridgePort());
              assertNotNull(maclink.getNode());
              assertNotNull(maclink.getMacAddress());
      }

    }
    
    private void checkTopology(OnmsNode dlink1, OnmsNode dlink2, OnmsNode nodeonlink1dport6,
    		OnmsNode nodebetweendlink1dlink2) {
        assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 1).size());
        assertEquals(2, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 2).size());
        assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 3).size());
        assertEquals(8, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 4).size());
        assertEquals(2, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 5).size());
        assertEquals(14, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 6).size());

        assertEquals(35, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 1).size());
        assertEquals(71, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 2).size());
        assertEquals(29, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 3).size());
        assertEquals(142, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 5).size());
        assertEquals(47, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 6).size());
        assertEquals(5, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 7).size());
        assertEquals(123, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 8).size());
        assertEquals(163, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 12).size());

        assertEquals(8, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 24).size());
        assertEquals(8, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 10).size());

        assertEquals(1,m_bridgeBridgeLinkDao.countAll());        
        assertEquals(659,m_bridgeMacLinkDao.countAll());
        // we have 3 that links "real mac nodes" to bridge.
        // we have 8 macs on bridge cloud between dlink1 and dlink2
        assertEquals(3,m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes().size());
        assertEquals(8,m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes().size());

        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            assertNotNull(link.getNode());
            assertNotNull(link.getBridgePort());
            assertNotNull(link.getBridgePortIfIndex());
            assertNotNull(link.getMacAddress());
            assertEquals(null, link.getBridgeDot1qTpFdbStatus());
        }

        for (BridgeMacTopologyLink link: m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()) {
            assertNotNull(link.getSrcNodeId());
            assertNotNull(link.getBridgePort());
            assertNotNull(link.getBridgePortIfIndex());
            assertNotNull(link.getTargetNodeId());
            assertNotNull(link.getMacAddr());
            assertNotNull(link.getTargetPortIfName());
            if (link.getSrcNodeId().intValue() == dlink1.getId().intValue()) {
                if (link.getBridgePort().intValue() == 6) {
                    assertEquals(link.getBridgePortIfIndex().intValue(), 6);
                    assertEquals(link.getTargetNodeId().intValue(), nodeonlink1dport6.getId().intValue());
                    assertEquals(link.getMacAddr(), "000ffeb10e26");
                    assertEquals(link.getTargetPortIfName(), "10.100.2.6");
                    assertEquals(link.getTargetIfIndex(), null);
                } else if (link.getBridgePort().intValue() == 24) {
                    assertEquals(link.getBridgePortIfIndex().intValue(), 24);
                    assertEquals(link.getTargetNodeId().intValue(), nodebetweendlink1dlink2.getId().intValue());
                    assertEquals(link.getMacAddr(), "001e58a6aed7");
                    assertEquals(link.getTargetPortIfName(), "10.100.1.7");
                    assertEquals(link.getTargetIfIndex().intValue(), 101);
                } else {
                    assertTrue(false);
                }
            } else if (link.getSrcNodeId().intValue() == dlink2.getId().intValue()) {
                assertEquals(link.getBridgePortIfIndex().intValue(), 10);
                assertEquals(link.getTargetNodeId().intValue(), nodebetweendlink1dlink2.getId().intValue());
                assertEquals(link.getMacAddr(), "001e58a6aed7");
                assertEquals(link.getTargetPortIfName(), "10.100.1.7");
                assertEquals(link.getTargetIfIndex().intValue(), 101);
            } else {
                assertTrue(false);
            }
        }
        
        for (BridgeMacTopologyLink link: m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()) {
            assertNotNull(link.getSrcNodeId());
            assertNotNull(link.getBridgePort());
            assertNotNull(link.getBridgePortIfIndex());
            assertNotNull(link.getTargetNodeId());
            assertNotNull(link.getMacAddr());
            assertNotNull(link.getTargetBridgePort());
            assertNotNull(link.getTargetIfIndex());
            assertNotNull(link.getTargetId());
            assertEquals(dlink1.getId().intValue(), link.getSrcNodeId().intValue());
            assertEquals(dlink2.getId().intValue(), link.getTargetNodeId().intValue());
//            assertEquals(24, link.getBridgePort().intValue());
//            assertEquals(10, link.getTargetBridgePort().intValue());
        }
        

        // Matt here you find that the macs on backbone port have all the same
        // switch port
        // the array "macsonbbport" is the intersection between 
        // the mac address forwarding table of dlink1 port 24
        // and the mac address forwarding table f dlink2 port 10
        // The following code will print the links as they are discovered
        for (String mac: macsonbbport) {
        	List<BridgeMacLink> maclinks = m_bridgeMacLinkDao.findByMacAddress(mac);
        	assertEquals(2,maclinks.size());
    		printBackboneBridgeMacLink(maclinks.get(0),maclinks.get(1));
        }

    }
    
    private void checkTopologyDlink2(OnmsNode dlink2) {
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
//      INTEGER: 1     35
//    INTEGER: 2      71
//    INTEGER: 3      29
//    INTEGER: 5     142
//    INTEGER: 6      47
//    INTEGER: 7       5
//    INTEGER: 8     123
//    INTEGER: 10     362
//    INTEGER: 12     163
      assertEquals(35, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 1).size());
      assertEquals(71, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 2).size());
      assertEquals(29, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 3).size());
      assertEquals(142, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 5).size());
      assertEquals(47, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 6).size());
      assertEquals(5, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 7).size());
      assertEquals(123, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 8).size());
      assertEquals(362, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 10).size());
      assertEquals(163, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 12).size());
//      total number of entry in bridgemaclink: 977
      assertEquals(977,m_bridgeMacLinkDao.countAll());
      assertEquals(1,m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes().size());
      assertEquals(0,m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes().size());

      for (BridgeMacLink maclink: m_bridgeMacLinkDao.findAll()) {
              assertEquals(null, maclink.getBridgeDot1qTpFdbStatus());
              assertNotNull(maclink.getBridgePortIfIndex());
              assertNotNull(maclink.getBridgePort());
              assertNotNull(maclink.getNode());
              assertNotNull(maclink.getMacAddress());
      }

    }
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930NetworkReverse() throws Exception {
  
    	final OnmsNode dlink1 = m_nodeDao.findByForeignId("linkd", DLINK1_NAME);
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);
        final OnmsNode nodebetweendlink1dlink2 = m_nodeDao.findByForeignId("linkd", "10.100.1.7");
        final OnmsNode nodeonlink1dport6 = m_nodeDao.findByForeignId("linkd", "10.100.2.6");
        
        assertNotNull(nodebetweendlink1dlink2);
        assertNotNull(nodeonlink1dport6);

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(true);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(dlink2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(dlink1.getId()));
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(dlink2.getId()));
        checkTopologyDlink2(dlink2);

        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        assertTrue(m_linkd.runTopologyDiscovery(dlink1.getId()));
        checkTopology(dlink1, dlink2, nodeonlink1dport6, nodebetweendlink1dlink2);
    }

}
