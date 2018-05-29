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
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms4930NetworkBuilder;

/*
 * port 24 of A is connected on port 10 of B 
dlink_DES-3026.properties
A)  00 1E 58 A3 2F CD —> 0.30.88.163.47.205 (001e58a32fcd)

self entry with bridgeport 0 and status SELF(4)
dlink_DES-3026.properties:.1.3.6.1.2.1.17.7.1.2.2.1.2.400.0.30.88.163.47.205 = INTEGER: 0
dlink_DES-3026.properties:.1.3.6.1.2.1.17.7.1.2.2.1.3.400.0.30.88.163.47.205 = INTEGER: 4

forwarding entry on B: learned(3) on port 10
dlink_DGS-3612G.properties:.1.3.6.1.2.1.17.7.1.2.2.1.2.400.0.30.88.163.47.205 = INTEGER: 10
dlink_DGS-3612G.properties:.1.3.6.1.2.1.17.7.1.2.2.1.3.400.0.30.88.163.47.205 = INTEGER: 3

BFT
   61 rows .1.3.6.1.2.1.17.7.1.2.2.1.2 {port}    —> 57 + 1 self + 2 duplicated + 1 without a valid status 
   60 rows .1.3.6.1.2.1.17.7.1.2.2.1.3 {learned} -> 57 + 1 self + 2 duplicated

   Port    ->BFT entries
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
8   ={mac learned on B&A on A:24}={mac learned on A&B on B:10} forwarders

333 ={mac: on B:10 && not on A} U  {mac: on B:10 && on A:0} forwarders

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

String[] forwardersdlink2on10bbport= {"001195256302","f07d68a13d67","001517028e04","00254591887c","1cbdb9419a98",
        "90e6ba8893ee","001731b83f34","001485dc7db9","00269994d3b9","485b39d72458","0064406c2980","0018f307910c",
        "001bd4b0be80","0080c84575f5","001c0e9c5c19","0055d0e72011","0040c7ea5fe9","0021913f50bd","0019d1b4e835",
        "00000000fe00","0015f260a0e3","60eb69ebacb5","001cf0e1c11f","001b0ceae240","0021918f4ab0","000fea5fe504",
        "001b119f90af","00024496a17e","5a003b0012f8","00270dc21568","0019d1397a9a","00016c90f20c","00e0424505c1",
        "0013463cd236","001349b4afc9","0021913fe8db","0016e63ffb0d","00304872e5b5","0014d1e14f92","000e082dfd5a",
        "1cbdb941a0d8","0040c7f6c3b9","001cb0b4a540","00e04ca2029e","1caff70801cb","00c12800d4fa","0040c7f6c3df",
        "0019d14497ec","0019666ed685","68efbd000be9","0002a403541c","0019d15fa04e","001930faf384","00e0f41aceb9",
        "001bfcff30db","bcaec511d055","0011115256a1","0019d1b4e80c","00192140817a","003048917410","001d602a19ca",
        "001fca590145","001d92d7f2da","001c10595252","0015174634bc","001e14a15486","0011f5d6ec7b","001e589f0f14",
        "00055d45975c","001349a9dde4","0019d10e5894","0018199e7989","001731b1c081","f07d6897870d","001346ddc2a4",
        "14d64d778af3","00055d3203d8","0040c7f640cd","000ab8614718","0018f375e902","f07d684dd3cd","00012345678a",
        "00e04c1aad77","0015623c3c81","0010f31197a1","0021914221f7","0021918bb9ed","0080484ddb79","00308488e5ae",
        "00138063ded1","0012d9ade6b7","001ef6331e9b","001731e58a9e","00195b12b1c0","0080484ddb74","3c5a37aa1795",
        "000d88b5b911","0016e09f8f20","0040c7f63992","0015e940ff75","0019db86ea8d","00c026a15735","0019d1841171",
        "00251129ca15","0019d1aa0366","001c57c387c3","001b4f38905f","8843e109d550","000f3d1bf298","00252231abe7",
        "000c293d638f","0019e258d000","f4ec38ac74c5","0015171778e8","1caff79e904d","90e6baacfb18","000ff77595a6",
        "00195b3819e4","f07d68825ef9","0040c7f6c304","0040c7f63882","001fc6b5deed","1caff79b1a75","0021918e8504",
        "1cbdb972002d","00c12802479e","1caff79798f3","000000000010","000021fa6bcb","0040c7ea28db","000c7650f682",
        "f4ec38bdc71b","001e58a32fcd","000af4aa34b0","f4ec38e915fd","68bdabb72100","0021919703aa","00904cc00000",
        "0021913f0927","001bfc200be1","588d091ba9f3","00265aac5b9b","001e58a32fc0","00265aac5a7d","001bfc9d93c2",
        "00260bb55cf1","4c0010131109","00134991f9ac","8418880d214a","00215ec7fd9c","00024456d458","001d7dca473d",
        "001a2fe8f182","00016cf6556a","0016b68e676d","e05fb9597072","7071bc71b4fb","00096b63547d","0040c7f639e9",
        "000c29e050ff","0040c7f639e7","1cbdb9ff6056","c471feb243f9","00173154a2f1","0019aac19fd0","0040c7f69000",
        "001346716177","0040c7f6380a","0018ba493d90","0002d110cfa4","0019dbd93f4a","0080485b607c","0016c70e9c05",
        "0015177c6310","00a0c944c29f","001346657f51","00046144b053","001e58f445ee","000fd9044246","00089bc57a61",
        "001bfc9d93dd","00195bfe5563","009027a7f87b","001731775384","00252273200b","0022b0e20455","000ff8ccd000",
        "0016e661c8ae","0040c7f6216c","0040c7f6c390","001e49761304","1caff79e9259","001d92d7f309","001517818b44",
        "f07d684f84bb","0016c70e9c10","0040c7f6261f","001f6cce0000","5cf3fc4c2458","0019d1987d52","001517e84a53",
        "14d64dcf4239","000cf1c8a902","0025456debe0","001517e84a54","0024be033412","000fd904421a","0021915a54a7",
        "005056b00169","001195dd2f89","001d607cdd31","002413f4f390","c0c1c050c88a","001bfcf65947","001d923db8bf",
        "0040c7f6bfd0","00112f0f1054","64168dfa8d48","001517e84b82","0016761846ee","000d28dd2a80","001cf0ab5a8d",
        "0016e63ffc96","001873f92003","0002442e35d2","00304866bbdd","00908f0fd9e2","0026ca9df81b","00211bf7dac4",
        "002522c0e539","340804144361","1caff702cfe9","0020d2286bdd","001cf0e1c18b","00908f0fd9d6","005056904c94",
        "00e051800166","001e589c2d44","0007e912739f","000d613ff745","00229064ffd1","0000855c70f5","0019d184546f",
        "0021918f1181","000a5e60098d","0013d3e3ac23","001e589ee128","001e589c2d3d","001f6cce336a","00145ea49072",
        "000bbea07216","00265abd0b0c","001f6cce336b","0010b5ad1577","000bbea07214","3ce5a681a5c3","00a0c993afbf",
        "001562cae2ce","1caff7825a8f","0002d10f5466","001125f29b67","0002d10f15dc","001cf0a569c3","1078d2155840",
        "0015e941a6e9","0015e9416070","0023544434d7","00e01833e66f","000ea662910d","00265abd0b09","0040c7f63ccc",
        "e0cb4ec5fdb9","0019d1840a08","00161712258b","0002a40264f4","0016c7f99e43","00265aac58bb","001bfc89b026",
        "006440893da1","0013c495d611","f07d689f027f","0002a4027786","00269994e318","1caff702d132","e0cb4eaa2626",
        "000fd9044177","00242124a1ee","1caff79b3883","002191546029","002191546028","0090fb23836e","00e052140240",
        "fcfbfb8ceac0","3037a6483ae0","001d922221dc","000c29b9824c","005056901ad2","0040c7f63ce6","000ea63553d2",
        "d4c1fc48acac","485b39264ce6","000fd90440bd","0020d22c2140","0021913b7f07","001e8cac2271","0019d1840b45",
        "0040c7f639d7","00248ce65505","00265abe2df4","0016e6616ba3","001b11055570","00265a154b0a","0015177c630f",
        "001b0db8881a","20cf309a1738","1caff7a5822e","00000000fe01","081ff3a89099","0040c7f639d5","001b0db8881b",
        "c84c75e4055b","00187d09df0c","000c292a1a11","00179a5504ae","00142af18edf","00304886ccbd","14d64d358e89",
        "0002d10f7852","5a003b011e1a","001e58af957b","0002d10f7855","0002d10f7853","0040c7ea5b78","005056b0661e"
        };

    String[] forwardersdlink1on24bbport = {
            "00270e0a788a",
            "00c0266a1d1d",
            "1c6f65b3599a",
            "0013774b4ab2"
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

        Set<BridgeForwardingTableEntry> links  = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(dlink1.getId());
        
        assertEquals(59,links.size());
        for (BridgeForwardingTableEntry link: links) {
            System.err.println(link.printTopology()); 
            if (BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF ==  link.getBridgeDot1qTpFdbStatus()) {
                continue;
            }
            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED,link.getBridgeDot1qTpFdbStatus());
            BridgeMacLink maclink = 
                    BridgeForwardingTableEntry.create(
                                      BridgePort.getFromBridgeForwardingTableEntry(link),
                                      link.getMacAddress(),
                                      BridgeMacLinkType.BRIDGE_LINK);
            maclink.setBridgeMacLinkLastPollTime(maclink.getBridgeMacLinkCreateTime());
            m_bridgeMacLinkDao.save(maclink);
        }

        assertEquals(58,m_bridgeMacLinkDao.countAll());

        for (BridgeMacLink maclink: m_bridgeMacLinkDao.findAll()) {
                assertNotNull(maclink.getBridgePortIfIndex());
                assertNotNull(maclink.getBridgePort());
                assertNotNull(maclink.getNode());
                assertNotNull(maclink.getMacAddress());
                System.err.println(maclink.printTopology());
        }

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK1_IP, port=161, resource=DLINK1_SNMP_RESOURCE)
    })
    public void testNms4930Dlink1() throws Exception {
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
        m_linkd.runTopologyDiscovery();
        checkTopologyDlink1(dlink1);
        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DLINK2_IP, port=161, resource=DLINK2_SNMP_RESOURCE)
    })
    public void testNms4930Dlink2() throws Exception {
        final OnmsNode dlink2 = m_nodeDao.findByForeignId("linkd", DLINK2_NAME);
        
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
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
        assertEquals(0,m_bridgeMacLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        m_linkd.runTopologyDiscovery();
        checkTopologyDlink2(dlink2);
        
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
        m_linkd.runTopologyDiscovery();
        checkTopologyDlink1(dlink1);

        assertTrue(m_linkd.runSingleSnmpCollection(dlink2.getId()));
        m_linkd.runTopologyDiscovery();
        checkTopology(dlink1, dlink2, nodeonlink1dport6, nodebetweendlink1dlink2,false);
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
        m_linkd.runTopologyDiscovery();
        checkTopologyDlink2(dlink2);

        assertTrue(m_linkd.runSingleSnmpCollection(dlink1.getId()));
        m_linkd.runTopologyDiscovery();
        checkTopology(dlink1, dlink2, nodeonlink1dport6, nodebetweendlink1dlink2,true);
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
      /*
       * deleteDuplicatedMac: mac:[64168dfa8d48], duplicated [64168dfa8d48, bridge:[1], bridgeport:2, ifindex:2, vlan:null, status:learned]
       * deleteDuplicatedMac: mac:[64168dfa8d48], duplicated [64168dfa8d48, bridge:[1], bridgeport:5, ifindex:5, vlan:null, status:learned]
       * deleteDuplicatedMac: mac:[64168dfa8d48] saved [64168dfa8d48, bridge:[1], bridgeport:2, ifindex:2, vlan:null
       * WHEN SUPPORTING DUPLICATED MACS...NOW REMOVE ON PORT 5 and 2 FROM 2 ONLY 1 IS FOUND
       */
      assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 1).size());
      assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 2).size()); //1 duplicated
      assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 3).size());
      assertEquals(8, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 4).size());
      assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 5).size()); //1 duplicated
      assertEquals(14, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 6).size());
      assertEquals(30, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 24).size());

      assertEquals(56,m_bridgeMacLinkDao.countAll()); 

      for (BridgeMacLink maclink: m_bridgeMacLinkDao.findAll()) {
              System.err.println(maclink.printTopology());
              assertNotNull(maclink.getBridgePortIfIndex());
              assertNotNull(maclink.getBridgePort());
              assertNotNull(maclink.getNode());
              assertNotNull(maclink.getMacAddress());
              assertEquals(BridgeMacLinkType.BRIDGE_LINK, maclink.getLinkType());
      }

    }
    
    private void checkTopology(OnmsNode dlink1, OnmsNode dlink2, OnmsNode nodeonlink1dport6,
    		OnmsNode nodebetweendlink1dlink2, boolean reverse) {
        
        /*
         *  
         * INTEGER: 1—>1
         * INTEGER: 2—>2 -1 duplicated
         * INTEGER: 3—>1
         * INTEGER: 4—>8
         * INTEGER: 5—>2 -1 duplicated
         * INTEGER: 6—>14
         * INTEGER: 24—>30 Backbone port
         */
        assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 1).size());
        assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 2).size());
        assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 3).size());
        assertEquals(8, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 4).size());
        assertEquals(1, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 5).size());
        assertEquals(14, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 6).size());

        /*
         *  
         *         INTEGER: 1     35
         *         INTEGER: 2     71 -1 duplicated
         *         INTEGER: 3     29
         *         INTEGER: 5    142
         *         INTEGER: 6     47
         *         INTEGER: 7      5
         *         INTEGER: 8    123 -1 duplicated
         *         INTEGER: 10   362 Backbone port
         *         INTEGER: 12   163
         *         */
        
/*
 *   
 *   deleteDuplicatedMac: mac:[002155321580], duplicated [002155321580, bridge:[2], bridgeport:8, ifindex:8, vlan:null, status:learned]
 *   deleteDuplicatedMac: mac:[002155321580], duplicated [002155321580, bridge:[2], bridgeport:2, ifindex:2, vlan:null, status:learned]
 *   deleteDuplicatedMac: mac:[002155321580] saved [002155321580, bridge:[2], bridgeport:8, ifindex:8, vlan:null, status:learned]
 */
        assertEquals(35, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 1).size());
        assertEquals(70, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 2).size()); //was 71
        assertEquals(29, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 3).size());
        assertEquals(142, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 5).size());
        assertEquals(47, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 6).size());
        assertEquals(5, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 7).size());
        assertEquals(122, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 8).size());
        assertEquals(163, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 12).size());

        if (reverse) {
            assertEquals(4, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 24).size());
            assertEquals(342, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 10).size());
        } else {
            assertEquals(12, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink1.getId(), 24).size());
            assertEquals(334, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 10).size());            
        } // 8 common 4 forwarders on dlink1 and 334 forwarders on dlink2
        assertEquals(985,m_bridgeMacLinkDao.countAll());

        assertEquals(1,m_bridgeBridgeLinkDao.countAll());        
        // we have 2 that links "real mac nodes" to bridge.
        // we have 8 macs on bridge cloud between dlink1 and dlink2

        for (BridgeMacLink link: m_bridgeMacLinkDao.findAll()) {
            assertNotNull(link.getNode());
            assertNotNull(link.getBridgePort());
            assertNotNull(link.getBridgePortIfIndex());
            assertNotNull(link.getMacAddress());
        }
        
        for (String mac: macsonbbport) {
        	List<BridgeMacLink> maclinks = m_bridgeMacLinkDao.findByMacAddress(mac);
        	assertEquals(1,maclinks.size());
        	for (BridgeMacLink link: maclinks) {
        	    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
        	    assertEquals(mac, link.getMacAddress());
            	if (reverse) {
                    assertEquals(10, link.getBridgePort().intValue());
                    assertEquals(10, link.getBridgePortIfIndex().intValue());
                    assertEquals(dlink2.getId().intValue(), link.getNode().getId().intValue());
            	    
            	} else {
                        assertEquals(24, link.getBridgePort().intValue());
                        assertEquals(24, link.getBridgePortIfIndex().intValue());
                        assertEquals(dlink1.getId().intValue(), link.getNode().getId().intValue());
            	}
                }
        }

        for (String mac: forwardersdlink1on24bbport) {
            List<BridgeMacLink> maclinks = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1,maclinks.size());
            for (BridgeMacLink link: maclinks) {
                assertEquals(BridgeMacLinkType.BRIDGE_FORWARDER, link.getLinkType());
                assertEquals(mac, link.getMacAddress());
                assertEquals(24, link.getBridgePort().intValue());
                assertEquals(24, link.getBridgePortIfIndex().intValue());
                assertEquals(dlink1.getId().intValue(), link.getNode().getId().intValue());
            }
        }

        for (String mac: forwardersdlink2on10bbport) {
            List<BridgeMacLink> maclinks = m_bridgeMacLinkDao.findByMacAddress(mac);
            assertEquals(1,maclinks.size());
            for (BridgeMacLink link: maclinks) {
                assertEquals(BridgeMacLinkType.BRIDGE_FORWARDER, link.getLinkType());
                assertEquals(mac, link.getMacAddress());
                assertEquals(10, link.getBridgePort().intValue());
                assertEquals(10, link.getBridgePortIfIndex().intValue());
                assertEquals(dlink2.getId().intValue(), link.getNode().getId().intValue());
            }
        }

    }
    
    private void checkTopologyDlink2(OnmsNode dlink2) {
        assertEquals(0,m_bridgeBridgeLinkDao.countAll());
//      INTEGER: 1     35
//    INTEGER: 2      71 1 duplicated
//    INTEGER: 3      29
//    INTEGER: 5     142
//    INTEGER: 6      47
//    INTEGER: 7       5
//    INTEGER: 8     123 1 duplicated
//    INTEGER: 10     362
//    INTEGER: 12     163
      assertEquals(35, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 1).size());
      assertEquals(70, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 2).size());
      assertEquals(29, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 3).size());
      assertEquals(142, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 5).size());
      assertEquals(47, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 6).size());
      assertEquals(5, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 7).size());
      assertEquals(122, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 8).size());
      assertEquals(362, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 10).size());
      assertEquals(163, m_bridgeMacLinkDao.findByNodeIdBridgePort(dlink2.getId(), 12).size());
//      total number of entry in bridgemaclink: 977 -1 duplicated
      assertEquals(975,m_bridgeMacLinkDao.countAll());

      for (BridgeMacLink maclink: m_bridgeMacLinkDao.findAll()) {
              assertNotNull(maclink.getBridgePortIfIndex());
              assertNotNull(maclink.getBridgePort());
              assertNotNull(maclink.getNode());
              assertNotNull(maclink.getMacAddress());
              assertEquals(BridgeMacLinkType.BRIDGE_LINK, maclink.getLinkType());
      }

    }
}
