/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.BridgeMacLink.BridgeMacLinkType;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public abstract class EnLinkdTestHelper {

    protected static void printLldpTopology(List<LldpLink> lldplinks) {
        for (LldpLink link : lldplinks)
            printLldpLink(link);
    }

    protected static void printLldpElements(List<LldpElement> lldpelements) {
        for (LldpElement element : lldpelements)
            printLldpElement(element);
    }

    protected static void printCdpElement(final CdpElement cdpElement) {
        System.err.println("----------cdp element --------");
        System.err.println("Nodeid: " + cdpElement.getNode().getId());
        System.err.println("Cdp Global Device Id: " + cdpElement.getCdpGlobalDeviceId());
        System.err.println("Cdp Global Run: " + TruthValue.getTypeString(cdpElement.getCdpGlobalRun().getValue()));
    }

    protected static void printCdpLink(CdpLink link) {
        System.err.println("----------cdp link --------");
        System.err.println("Create time: " + link.getCdpLinkCreateTime());
        System.err.println("Last Poll time: " + link.getCdpLinkLastPollTime());
        System.err.println("----------Source Node--------");
        System.err.println("Nodeid: " + link.getNode().getId());
        System.err.println("----------Source Port--------");
        System.err.println("cdpcacheifindex: " + link.getCdpCacheIfIndex());
        System.err.println("cdpcachedeviceindex: " + link.getCdpCacheDeviceIndex());
        System.err.println("cdpinterfacename: " + link.getCdpInterfaceName());
        System.err.println("----------Rem Node--------");
        System.err.println("cdpcacheaddresstype: " + CiscoNetworkProtocolType.getTypeString(link.getCdpCacheAddressType().getValue()));
        System.err.println("cdpcacheaddress: " + link.getCdpCacheAddress());
        System.err.println("cdpcacheversion: " + link.getCdpCacheVersion());
        System.err.println("cdpcachedeviceid: " + link.getCdpCacheDeviceId());
        System.err.println("cdpcachedeviceplatform: " + link.getCdpCacheDevicePlatform());
        System.err.println("----------Remote Port--------");
        System.err.println("cdpcachedeviceport: " + link.getCdpCacheDevicePort());
        System.err.println("");
    }

    protected static void printLldpElement(final LldpElement lldpElement) {
        System.err.println("----------lldp element --------");
        System.err.println("Nodeid: " + lldpElement.getNode().getId());
        System.err.println("lldp chassis type/id: " + LldpChassisIdSubType.getTypeString(lldpElement.getLldpChassisIdSubType().getValue()) + "/" + lldpElement.getLldpChassisId());
        System.err.println("lldp sysname: " + lldpElement.getLldpSysname());
    }

    protected static void printLldpLink(LldpLink link) {
        System.err.println("----------lldp link --------");
        System.err.println("Create time: " + link.getLldpLinkCreateTime());
        System.err.println("Last Poll time: " + link.getLldpLinkLastPollTime());
        System.err.println("----------Source Node--------");
        System.err.println("Nodeid: " + link.getNode().getId());
        System.err.println("----------Source Port--------");
        System.err.println("lldp port num: " + link.getLldpLocalPortNum());
        System.err.println("lldp port ifindex: " + link.getLldpPortIfindex());
        System.err.println("lldp port type/id: " + LldpPortIdSubType.getTypeString(link.getLldpPortIdSubType().getValue()) + "/" + link.getLldpPortId());
        System.err.println("lldp port descr: " + link.getLldpPortDescr());
        System.err.println("----------Rem Node--------");
        System.err.println("lldp rem chassis type/id: " + LldpChassisIdSubType.getTypeString(link.getLldpRemChassisIdSubType().getValue()) + "/" + link.getLldpRemChassisId());
        System.err.println("lldp rem sysname: " + link.getLldpRemSysname());
        System.err.println("----------Remote Port--------");
        System.err.println("lldp rem port type/id: " + LldpPortIdSubType.getTypeString(link.getLldpRemPortIdSubType().getValue()) + "/" + link.getLldpRemPortId());
        System.err.println("lldp rem port descr: " + link.getLldpRemPortDescr());
        System.err.println("");
    }

    protected static void printOspfTopology(List<OspfLink> ospflinks) {
        for (OspfLink link : ospflinks)
            printOspfLink(link);
    }

    protected static void printOspfElements(List<OspfElement> ospfelements) {
        for (OspfElement element : ospfelements)
            printOspfElement(element);
    }

    protected static void printOspfElement(final OspfElement element) {
        System.err.println("----------ospf element --------");
        System.err.println("Nodeid: " + element.getNode().getId());
        System.err.println("ospf router id/mask/ifindex: " + str(element.getOspfRouterId()) + "/" + str(element.getOspfRouterIdNetmask()) + "/" + element.getOspfRouterIdIfindex());
        System.err.println("ospf admin status: " + Status.getTypeString(element.getOspfAdminStat().getValue()));
        System.err.println("ospf version number: " + element.getOspfVersionNumber());
        System.err.println("ospf Border Router Status: " + TruthValue.getTypeString(element.getOspfBdrRtrStatus().getValue()));
        System.err.println("ospf AS Boder Router Status: " + TruthValue.getTypeString(element.getOspfASBdrRtrStatus().getValue()));
        System.err.println("");
    }

    protected static void printOspfLink(OspfLink link) {
        System.err.println("----------ospf link --------");
        System.err.println("Create time: " + link.getOspfLinkCreateTime());
        System.err.println("Last Poll time: " + link.getOspfLinkLastPollTime());
        System.err.println("----------Source Node--------");
        System.err.println("Nodeid: " + link.getNode().getId());
        System.err.println("----------Source Port--------");
        System.err.println("ospf router id/mask/ifindex/addressleifindex: " + str(link.getOspfIpAddr()) + "/" + str(link.getOspfIpMask()) + "/" + link.getOspfIfIndex() + "/" + link.getOspfAddressLessIndex());
        System.err.println("----------Rem Node--------");
        System.err.println("ospf rem router id: " + str(link.getOspfRemRouterId()));
        System.err.println("----------Remote Port--------");
        System.err.println("ospf rem router ip: " + str(link.getOspfRemIpAddr()));
        System.err.println("ospf rem router address less ifindex: " + link.getOspfRemAddressLessIndex());
        System.err.println("");
    }

    public void setBridgeElements(BroadcastDomain domain, List<BridgeElement> elements) {
        for (BridgeElement element: elements) {
            Bridge bridge = domain.getBridge(element.getNode().getId());
            if (bridge == null) {
                continue;
            }
            if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress())) {
                bridge.getIdentifiers().add(element.getBaseBridgeAddress());
            }
            if (InetAddressUtils.
                    isValidStpBridgeId(element.getStpDesignatedRoot()) 
                    && !element.getBaseBridgeAddress().
                    equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
                bridge.setDesignated(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()));
            }
        }        
    }
    
    public BridgeForwardingTableEntry addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, String mac) {
        BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
        link.setNodeId(node.getId());
        link.setBridgePort(bridgeport);
        link.setMacAddress(mac);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        return link;
    }
    
    public BridgeForwardingTableEntry addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, Integer ifindex,String mac) {
        BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
        link.setNodeId(node.getId());
        link.setBridgePort(bridgeport);
        link.setBridgePortIfIndex(ifindex);
        link.setMacAddress(mac);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        return link;
    }

    @SafeVarargs
    public static <T> boolean checkLinks(Iterable<T> iterable, Predicate<T>... matchers) {
        for (Predicate<T> matcher : matchers) {
            if (!Iterables.any(iterable, matcher)) {
                return false;
            }
        }
        return true;
    }

    public static Predicate<OspfLink> ospfLinkMatcher(OnmsNode node, OnmsNode parentNode, int ifIndex, int parentIfIndex) {
        return (link) -> {
           return node.equals(link.getNode()) &&
                   parentNode.getOspfElement().getOspfRouterId().equals(link.getOspfRemRouterId()) &&
                   ifIndex == link.getOspfIfIndex() &&
                   parentIfIndex == link.getOspfAddressLessIndex();
        };
    }
    
    /*
     *         /////////////////////////////////////////////////////////////////////////////
     *         //                               switch B                                   //
     *         //       1                              2                          3     4  //         
     *         //////////////////////////////////////////////////////////////////////////////
     *                  |                              |
     *         //////////////////////    /////////////////////////////////
     *         //      50          //    //      wireless net           //
     *         //     switch D     //    /////////////////////////////////
     *         //      49          //         |                      |
     *         //////////////////////         |                      |
     *                  |                     |                      |
     *         //////////////////////    ////////////////////    /////////////////////
     *         //      24          //    //   11           //    //  3              //
     *         //     switch A     //    //  switch C      //    //   switch E      //
     *         //  4   5   52      //    //  19      24    //    //       23        //
     *         //////////////////////    ////////////////////    /////////////////////
     */
    class FiveSwitchTopology {
        final int nodeAId = 1;
        final int nodeBId = 2;
        final int nodeCId = 3;
        final int nodeDId = 6;
        final int nodeEId = 8;
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        OnmsNode nodeC= new OnmsNode();
        OnmsNode nodeD= new OnmsNode();
        OnmsNode nodeE= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        BridgeElement elementC = new BridgeElement();
        BridgeElement elementD = new BridgeElement();
        BridgeElement elementE = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftC = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftD = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftE = new HashSet<BridgeForwardingTableEntry>();

        String[] macsonAport4= {
                "a04aee425191"
        };

        String[] macsonAport5= {
                "a05a99b52703",
                "a05a99b52749"
        };

        String[] macsonAport52= {
                "a52a01030104",
                "a52a84f84401",
                "a52a29f49b80",
                "a52aed30816a",
                "a52a6d0e9976"
        };

        String[] macsonBport3= {
                "b03b63010d4f"
        };

        String[] macsonBport4= {
                "b04b427bfee3",
                "b04b42f213af",
                "b04b6301050f"
        };
        
        String[] macsonCport19 = {
                "c19c822458d2"
        };

        String[] macsonCport24 = {
                "c24c83f6120a"
        };

        String[] macsonEport23 = {
                "e23e454ac907"
        };

        String[] macsOnWirelessSegment = {
                "000c429e3f3d",
                "000c42ef1df6",
                "000c42ef1e02",
                "000c42f5d30a",
                "0012cf68f80f",
                "001d454777dc",
                "001d71d5e4e7",
                "0021a4357254",
                "00641393f352",
                "00ca6d147c96",
                "00ca6d69c484",
                "00ca6d82ab08",
                "00ca6d88234f",
                "00ca6d954b3b",
                "00ca6da2d626",
                "00ca6ded84c8",
                "00ca6ded84d6",
                "00ca6dedd059",
                "00ca6df7f801",
                "008d8cf63372"
        };

        String[] forwardersABCD = {
                "fabcd57fd894"      
        };

        String[] forwardersABD = {
                "fabd0f68f800",
                "fabd03010792"
        };

        String[] forwardersBE = {
                "fbe0b1bd265e",
        };

        String[] forwardersD = {
                "fd00b1bd2f5e"
        };

        String[] forwardersE = {
                "fe00b1bd2f5f",
                "fe0042db4e11"
        };

        String macA="aaaa005b2980";
        String macB="bbbb00bd2f5c";
        String macC="cccc00bd2652";
        String macD="dddd005d2120";
        String macE="eeee03f4ee0";
        
        public FiveSwitchTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress(macA);
            elemlist.add(elementA);
    
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);
    
            nodeC.setId(nodeCId);
            elementC.setNode(nodeC);
            elementC.setBaseBridgeAddress(macC);
            elemlist.add(elementC);

            nodeD.setId(nodeDId);
            elementD.setNode(nodeD);
            elementD.setBaseBridgeAddress(macD);
            elemlist.add(elementD);

            nodeE.setId(nodeEId);
            elementE.setNode(nodeE);
            elementE.setBaseBridgeAddress(macE);
            elemlist.add(elementE);

            //BFT B port 1
            bftB.add(addBridgeForwardingTableEntry(nodeB, 1, 1001, macsonAport52[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 1, 1001, macsonAport52[2]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 1, 1001, macsonAport52[4]));

            //BFT B port 2 bridge address forwarding rules
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macE));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macC));
            //BFT B port 2 forwarders
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersABD[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersABD[1]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersABCD[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, forwardersBE[0]));            

            //BFT B port 2 wireless segment
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[1]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[2]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[3]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[4]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[5]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[6]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[7]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[8]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[9]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[10]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[11]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[12]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[13]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[14]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[15]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[16]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[17]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[18]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsOnWirelessSegment[19]));
            
            //BFT B port 2 forwarding C
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsonCport19[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsonCport24[0]));
            
            //BFT B port 2 forwarding E
            bftB.add(addBridgeForwardingTableEntry(nodeB, 2, 1002, macsonEport23[0]));

            //BFT B port 3 
            bftB.add(addBridgeForwardingTableEntry(nodeB, 3, 1003, macsonBport3[0]));
            
            //BFT B port 4 
            bftB.add(addBridgeForwardingTableEntry(nodeB, 4, 1004, macsonBport4[0]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 4, 1004, macsonBport4[1]));
            bftB.add(addBridgeForwardingTableEntry(nodeB, 4, 1004, macsonBport4[2]));

            //BFT C port 11 bridge address forwarding rules
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macB));
            //BFT C port 11 forwarder
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, forwardersABCD[0]));
            
            //BFT C port 11 forwarding E
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonEport23[0]));

            //BFT C port 11 forwarding A
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonAport52[0]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonAport52[2]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsonAport52[4]));
            
            //BFT C port 11 wireless
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[0]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[1]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[2]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[3]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[4]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[5]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[6]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[7]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[8]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[9]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[10]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[11]));            
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[12]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[13]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[14]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[15]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[16]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[17]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[18]));
            bftC.add(addBridgeForwardingTableEntry(nodeC, 11, 1011, macsOnWirelessSegment[19]));

            //BFT C port 19
            bftC.add(addBridgeForwardingTableEntry(nodeC, 19, 1019, macsonCport19[0]));
            
            //BFT C port 24
            bftC.add(addBridgeForwardingTableEntry(nodeC, 24, 1024, macsonCport24[0]));
            
            //BFT A port 4
            bftA.add(addBridgeForwardingTableEntry(nodeA,  4, 10104, macsonAport4[0]));
            //BFT A port 5
            bftA.add(addBridgeForwardingTableEntry(nodeA,  5, 10105, macsonAport5[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA,  5, 10105, macsonAport5[1]));

            //BFT A port 24 forwarding rules for base address
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macB));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macC));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macD));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macE));
            //BFT A port 24 forwarders
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, forwardersABD[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, forwardersABD[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, forwardersABCD[0]));

            //BFT A port 24 forwarding B
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport4[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport4[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport4[2]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonBport3[0]));
            //BFT A port 24 forwarding C
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonCport19[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonCport24[0]));

            //BFT A port 24 forwarding E
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsonEport23[0]));

            //BFT A port 24 wireless
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[2]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[3]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[5]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[6]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[7]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[8]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[9]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[10]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[11]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[12]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[13]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[14]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[15]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[16]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[17]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[18]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 24, 10124, macsOnWirelessSegment[19]));
            
            //BFT A port 52 
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[0]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[1]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[2]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[3]));
            bftA.add(addBridgeForwardingTableEntry(nodeA, 52,  5025, macsonAport52[4]));
            
            //BFT D port 49 forward A 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 49, 49, macsonAport52[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 49, 49, macsonAport52[2]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 49, 49, macsonAport52[4]));

            //BFT D port 50 base address 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macB));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macC));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macE));

            //BFT D port 50 forwarders 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersABD[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersABD[1]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersABCD[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, forwardersD[0]));
            
            //BFT D port 50 forward B 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport3[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport4[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport4[1]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonBport4[2]));

            //BFT D port 50 forward C 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonCport19[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonCport24[0]));

            //BFT D port 50 forward E 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsonEport23[0]));

            //BFT D port 50 wireless 
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[0]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[1]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[2]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[3]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[5]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[6]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[7]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[8]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[9]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[10]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[11]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[12]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[13]));
            bftD.add(addBridgeForwardingTableEntry(nodeD, 50, 50, macsOnWirelessSegment[14]));
            

            //BFT E port 3 base address
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macB));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macC));
            //BFT E port 3 forwarders
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, forwardersE[0]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, forwardersE[1]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, forwardersBE[0]));
            //BFT E port 3 forward A
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsonAport52[4]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsonAport52[0]));
            //BFT E port 3 forward C
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsonCport19[0]));
            
            //BFT E port 3 wireless 
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[0]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[1]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[2]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[3]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[4]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[5]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[6]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[7]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[8]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[9]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[10]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[11]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[12]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[13]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[14]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[15]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[16]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[17]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[18]));
            bftE.add(addBridgeForwardingTableEntry(nodeE,  3,  3, macsOnWirelessSegment[19]));
            
            //BFT E port 23 wireless 
            bftE.add(addBridgeForwardingTableEntry(nodeE, 23, 23, macsonEport23[0]));
        }
        public void checkBC(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(5, domain.getBridgeNodesOnDomain().size());
            assertEquals(nodeBId, domain.getRootBridge().getNodeId().intValue());
            assertEquals(6, domain.getSharedSegments().size());
            assertEquals(4, domain.getSharedSegments(nodeBId).size());
            assertEquals(3, domain.getSharedSegments(nodeCId).size());
            for (Bridge bridge: domain.getBridges()) {
                if (bridge.isRootBridge()) {
                    assertEquals(nodeBId, bridge.getNodeId().intValue());
                    assertNull(bridge.getRootPort());
                } else {
                    switch (bridge.getNodeId().intValue()) {
                    case nodeCId:
                        assertNotNull(bridge.getRootPort());
                        break;
                    case nodeAId:
                        assertNull(bridge.getRootPort());
                        break;
                    case nodeDId:
                        assertNull(bridge.getRootPort());
                        break;
                    case nodeEId:
                        assertNull(bridge.getRootPort());
                        break;
                    default:
                        assertTrue(false);
                        break;

                    }
                }
            }
            for (SharedSegment segment: domain.getSharedSegments()) {
                assertNotNull(segment.getDesignatedBridge());
                assertNotNull(segment.getDesignatedPort());
                switch (segment.getDesignatedBridge().intValue()) {
                case nodeBId:
                    checkBcalcBC(segment);
                    break;
                case nodeCId:
                    checkCcalcBC(segment);
                    break;

                default:
                    assertTrue(false);
                    break;
                }
                for (BridgeMacLink bridgeMacLink : SharedSegment.getBridgeMacLinks(segment)) {
                    assertNotNull(bridgeMacLink.getBridgePort());
                    assertNotNull(bridgeMacLink.getBridgePortIfIndex());
                    assertNotNull(bridgeMacLink.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, bridgeMacLink.getLinkType());
               }
            }
            checkBcalcBC(domain.getForwarders(nodeBId));
            checkCcalcBC(domain.getForwarders(nodeCId));

            
        }

        public void checkBcalcBCA(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(5, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeBId, forward.getNodeId().intValue());
                assertEquals(2, forward.getBridgePort().intValue());
                assertEquals(1002, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macC));
            assertTrue(macs.contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersBE) {
                assertTrue(macs.contains(mac));                
            }
        }     

        public void checkAcalcBCA(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(6, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeAId, forward.getNodeId().intValue());
                assertEquals(24, forward.getBridgePort().intValue());
                assertEquals(10124, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macB));
            assertTrue(macs.contains(macC));
            assertTrue(macs.contains(macD));
            assertTrue(macs.contains(macE));
            
            for (String mac: forwardersABD) {
                assertTrue(macs.contains(mac));                
            }
            
        }
        
        public void checkCcalcBC(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(1, forwarders.size());
            BridgeForwardingTableEntry forward = forwarders.iterator().next();
            assertEquals(nodeCId, forward.getNodeId().intValue());
            assertEquals(11, forward.getBridgePort().intValue());
            assertEquals(1011, forward.getBridgePortIfIndex().intValue());
            assertTrue(macB.equals(forward.getMacAddress()));
        }        

        public void checkBcalcBC(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(5, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeBId, forward.getNodeId().intValue());
                assertEquals(2, forward.getBridgePort().intValue());
                assertEquals(1002, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macC));
            assertTrue(macs.contains(macE));
            for (String mac: forwardersABD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersBE) {
                assertTrue(macs.contains(mac));                
            }
        }        

        
        public void checkCcalcBC(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeCId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 19:
                assertEquals(macsonCport19.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport19) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 24:
                assertEquals(macsonCport24.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport24) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }

        }

        public void checkBcalcBC(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeBId, segment.getDesignatedBridge().intValue());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 1:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(segment).size());
                assertEquals(3, segment.getMacsOnSegment().size());
                assertEquals(3, SharedSegment.getBridgeMacLinks(segment).size());
                break;
            case 2:
                assertEquals(2, segment.getBridgePortsOnSegment().size());
                assertEquals(2, segment.getBridgePort(nodeBId).getBridgePort().intValue());
                assertEquals(11, segment.getBridgePort(nodeCId).getBridgePort().intValue());
                assertEquals(macsOnWirelessSegment.length+forwardersABCD.length+macsonEport23.length, segment.getMacsOnSegment().size());
                for (String mac: macsOnWirelessSegment) {
                    assertTrue(segment.containsMac(mac));
                }
                for (String mac: forwardersABCD) {
                    assertTrue(segment.containsMac(mac));
                }
                for (String mac: macsonEport23) {
                    assertTrue(segment.containsMac(mac));
                }
                assertEquals(macsOnWirelessSegment.length+forwardersABCD.length+macsonEport23.length, SharedSegment.getBridgeMacLinks(segment).size());
                assertEquals(1, SharedSegment.getBridgeBridgeLinks(segment).size());
                BridgeBridgeLink blink = SharedSegment.getBridgeBridgeLinks(segment).iterator().next();  
                assertEquals(nodeBId, blink.getDesignatedNode().getId().intValue());
                assertEquals(2, blink.getDesignatedPort().intValue());
                assertEquals(11, blink.getBridgePort().intValue());
                break;
            case 3:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport3.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport3) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 4:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport4.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport4) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }
            
        }

        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(5, domain.getBridgeNodesOnDomain().size());
            assertEquals(nodeBId, domain.getRootBridge().getNodeId().intValue());
            assertEquals(11, domain.getSharedSegments().size());
            assertEquals(4, domain.getSharedSegments(nodeAId).size());
            assertEquals(4, domain.getSharedSegments(nodeBId).size());
            assertEquals(3, domain.getSharedSegments(nodeCId).size());
            assertEquals(2, domain.getSharedSegments(nodeDId).size());
            assertEquals(2, domain.getSharedSegments(nodeEId).size());
            for (Bridge bridge: domain.getBridges()) {
                if (bridge.isRootBridge())
                    assertNull(bridge.getRootPort());
                else
                    assertNotNull(bridge.getRootPort());
            }
            for (SharedSegment segment: domain.getSharedSegments()) {
                assertNotNull(segment.getDesignatedBridge());
                assertNotNull(segment.getDesignatedPort());
                switch (segment.getDesignatedBridge().intValue()) {
                case nodeAId:
                    checkA(segment);
                    break;
                case nodeBId:
                    checkB(segment);
                    break;
                case nodeCId:
                    checkC(segment);
                    break;
                case nodeDId:
                    checkD(segment);
                    break;
                case nodeEId:
                    checkE(segment);
                    break;

                default:
                    assertTrue(false);
                    break;
                }
                for (BridgeMacLink bridgeMacLink : SharedSegment.getBridgeMacLinks(segment)) {
                    assertNotNull(bridgeMacLink.getBridgePort());
                    assertNotNull(bridgeMacLink.getBridgePortIfIndex());
                    assertNotNull(bridgeMacLink.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, bridgeMacLink.getLinkType());
               }
            }
            checkA(domain.getForwarders(nodeAId));
            checkB(domain.getForwarders(nodeBId));
            checkC(domain.getForwarders(nodeCId));
            checkD(domain.getForwarders(nodeDId));
            checkE(domain.getForwarders(nodeEId));
            
        }
        public void checkE(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(5, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeEId, forward.getNodeId().intValue());
                assertEquals(3, forward.getBridgePort().intValue());
                assertEquals(3, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macB));
            assertTrue(macs.contains(macC));
            for (String mac: forwardersBE) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersE) {
                assertTrue(macs.contains(mac));                
            }
        }        

        public void checkD(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(7, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeDId, forward.getNodeId().intValue());
                assertEquals(50, forward.getBridgePort().intValue());
                assertEquals(50, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macB));
            assertTrue(macs.contains(macC));
            assertTrue(macs.contains(macE));
            for (String mac: forwardersABCD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersABD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersD) {
                assertTrue(macs.contains(mac));                
            }
        }        

        public void checkC(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(2, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeCId, forward.getNodeId().intValue());
                assertEquals(11, forward.getBridgePort().intValue());
                assertEquals(1011, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macB));
            for (String mac: forwardersABCD) {
                assertTrue(macs.contains(mac));                
            }
        }        

        public void checkB(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(6, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeBId, forward.getNodeId().intValue());
                assertEquals(2, forward.getBridgePort().intValue());
                assertEquals(1002, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macC));
            assertTrue(macs.contains(macE));
            for (String mac: forwardersABCD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersABD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersBE) {
                assertTrue(macs.contains(mac));                
            }
        }     
        
        public void checkA(Set<BridgeForwardingTableEntry> forwarders) {
            assertEquals(7, forwarders.size());
            Set<String> macs = new HashSet<String>();
            for (BridgeForwardingTableEntry forward: forwarders) {
                assertTrue(!macs.contains(forward.getMacAddress()));
                assertEquals(nodeAId, forward.getNodeId().intValue());
                assertEquals(24, forward.getBridgePort().intValue());
                assertEquals(10124, forward.getBridgePortIfIndex().intValue());
                macs.add(forward.getMacAddress());
            }
            assertTrue(macs.contains(macB));
            assertTrue(macs.contains(macC));
            assertTrue(macs.contains(macD));
            assertTrue(macs.contains(macE));
            for (String mac: forwardersABCD) {
                assertTrue(macs.contains(mac));                
            }
            for (String mac: forwardersABD) {
                assertTrue(macs.contains(mac));                
            }

        }
        public void checkA(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeAId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 4:
                assertEquals(macsonAport4.length, segment.getMacsOnSegment().size());
                for (String mac: macsonAport4) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 5:
                assertEquals(macsonAport5.length, segment.getMacsOnSegment().size());
                for (String mac: macsonAport5) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 52:
                assertEquals(macsonAport52.length, segment.getMacsOnSegment().size());
                for (String mac: macsonAport52) {
                    assertTrue(segment.containsMac(mac));
                }

             break;

            default:
                assertTrue(false);
                break;
            }
        }
        public void checkE(SharedSegment segment) throws BridgeTopologyException  {
            assertEquals(nodeEId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 23:
                assertEquals(macsonEport23.length, segment.getMacsOnSegment().size());
                for (String mac: macsonEport23) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }
            
        }
        
        public void checkC(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeCId, segment.getDesignatedBridge().intValue());
            assertEquals(1, segment.getBridgePortsOnSegment().size());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 19:
                assertEquals(macsonCport19.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport19) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 24:
                assertEquals(macsonCport24.length, segment.getMacsOnSegment().size());
                for (String mac: macsonCport24) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }

        }
        public void checkB(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeBId, segment.getDesignatedBridge().intValue());
            switch (segment.getDesignatedPort().getBridgePort().intValue()) {
            case 1:
                assertEquals(2, segment.getBridgePortsOnSegment().size());
                assertEquals(1, SharedSegment.getBridgeBridgeLinks(segment).size());
                assertEquals(0, segment.getMacsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeMacLinks(segment).size());
                BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(segment).iterator().next();
                assertEquals(nodeDId, link.getNode().getId().intValue());
                assertEquals(50, link.getBridgePort().intValue());
                assertEquals(nodeBId, link.getDesignatedNode().getId().intValue());
                assertEquals(1, link.getDesignatedPort().intValue());
                break;
            case 2:
                assertEquals(3, segment.getBridgePortsOnSegment().size());
                assertEquals(2, segment.getBridgePort(nodeBId).getBridgePort().intValue());
                assertEquals(3, segment.getBridgePort(nodeEId).getBridgePort().intValue());
                assertEquals(11, segment.getBridgePort(nodeCId).getBridgePort().intValue());
                assertEquals(macsOnWirelessSegment.length, segment.getMacsOnSegment().size());
                for (String mac: macsOnWirelessSegment) {
                    assertTrue(segment.containsMac(mac));
                }
                assertEquals(macsOnWirelessSegment.length, SharedSegment.getBridgeMacLinks(segment).size());
                assertEquals(2, SharedSegment.getBridgeBridgeLinks(segment).size());
                for (BridgeBridgeLink blink: SharedSegment.getBridgeBridgeLinks(segment))  {
                    assertEquals(nodeBId, blink.getDesignatedNode().getId().intValue());
                    assertEquals(2, blink.getDesignatedPort().intValue());
                    
                    switch (blink.getNode().getId().intValue()) {
                    case nodeEId:
                        assertEquals(3, blink.getBridgePort().intValue());
                        break;
                    case nodeCId:
                        assertEquals(11, blink.getBridgePort().intValue());
                        break;

                    default:
                        assertTrue(false);
                        break;
                    }
                }
                break;
            case 3:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport3.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport3) {
                    assertTrue(segment.containsMac(mac));
                }
                break;
            case 4:
                assertEquals(1, segment.getBridgePortsOnSegment().size());
                assertEquals(macsonBport4.length, segment.getMacsOnSegment().size());
                for (String mac: macsonBport4) {
                    assertTrue(segment.containsMac(mac));
                }
                break;

            default:
                assertTrue(false);
                break;
            }
            
        }

        public void checkD(SharedSegment segment) throws BridgeTopologyException {
            assertEquals(nodeDId, segment.getDesignatedBridge().intValue());
            assertEquals(2, segment.getBridgePortsOnSegment().size());
            assertEquals(1, SharedSegment.getBridgeBridgeLinks(segment).size());
            assertEquals(0, segment.getMacsOnSegment().size());
            assertEquals(0, SharedSegment.getBridgeMacLinks(segment).size());
            assertEquals(49, segment.getDesignatedPort().getBridgePort().intValue());
            BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(segment).iterator().next();
            assertEquals(nodeAId, link.getNode().getId().intValue());
            assertEquals(24, link.getBridgePort().intValue());
            assertEquals(nodeDId, link.getDesignatedNode().getId().intValue());
            assertEquals(49, link.getDesignatedPort().intValue());
        }

    }
    
    class ABCTopology {
        Integer nodeAId = 101;
        Integer nodeBId = 102;
        Integer nodeCId = 103;
        
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        OnmsNode nodeC= new OnmsNode();

        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        BridgeElement elementC = new BridgeElement();

        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftC = new HashSet<BridgeForwardingTableEntry>();


        Integer portA = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB  = 2;
        Integer portBC = 23;
        Integer portCB = 32;
        Integer portC  = 3;

        String mac1 = "000daaaa0101"; // port A  ---port BA ---port CB
        String mac2 = "000daaaa0202"; // port AB ---port B  ---port CB
        String mac3 = "000daaaa0303"; // port AB ---port BC ---port C
        String shar = "000daaaa1111"; // to test loadTopology

        String macA = "aaaaaaaaaaaa";
        String macB = "bbbbbbbbbbbb";
        String macC = "cccccccccccc";
        /*
         *              -----------------
         *     mac1 --  ||portA|        |
         *              |   "Bridge A"  |
         *              |   |portAB|    |
         *              -----------------
         *                      |
         *                      |
         *              -----------------
         *              |   |portBA|    |
         *              |   "Bridge B"  |
         *     mac2 --  ||portB|        |
         *              |   |portBC|    |
         *              -----------------
         *                      |
         *                      |
         *              -----------------
         *              |   |portCB|    |
         *              |   "Bridge C"  |
         *     mac3 --  ||portC|        |
         *              -----------------
         *               
         */  

        public ABCTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress(macA);
            elemlist.add(elementA);
    
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);
    
            nodeC.setId(nodeCId);
            elementC.setNode(nodeC);
            elementC.setBaseBridgeAddress(macC);
            elemlist.add(elementC);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac3));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB, mac2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBC, mac3));

            bftC.add(addBridgeForwardingTableEntry(nodeC,portCB, mac1));
            bftC.add(addBridgeForwardingTableEntry(nodeC,portCB, mac2));
            bftC.add(addBridgeForwardingTableEntry(nodeC,portC, mac3));
        }

        public void checkAC(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeAId).size());
            assertEquals(0, domain.getForwarders(nodeCId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments();        	
            assertEquals(3, shsegms.size());

            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(0, bblinks.size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, bblinks.size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(nodeAId,link.getNode().getId());
                    assertEquals(portAB,link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(0, bblinks.size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void checkAB(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeAId).size());
            assertEquals(0, domain.getForwarders(nodeBId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments(); 
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    assertEquals(nodeAId, bblink.getDesignatedNode().getId());
                    assertEquals(nodeBId, bblink.getNode().getId());
                    assertEquals(portAB, bblink.getDesignatedPort());
                    assertEquals(portBA, bblink.getBridgePort());
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portBC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkBC(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeCId).size());
            assertEquals(0, domain.getForwarders(nodeBId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments();
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                    assertEquals(nodeCId, bblink.getNode().getId());
                    assertEquals(portBC, bblink.getDesignatedPort());
                    assertEquals(portCB, bblink.getBridgePort());
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portBA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeCId).size());
            assertEquals(0, domain.getForwarders(nodeBId).size());
            assertEquals(0, domain.getForwarders(nodeAId).size());
            List<SharedSegment> shsegms = domain.getSharedSegments();
            assertEquals(5, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    if (bblink.getDesignatedNode().getId() == nodeAId) {
                        assertEquals(nodeBId, bblink.getNode().getId());
                        assertEquals(nodeAId, bblink.getDesignatedNode().getId());
                        assertEquals(portBA, bblink.getBridgePort());
                        assertEquals(portAB, bblink.getDesignatedPort());
                    } else if (bblink.getDesignatedNode().getId() == nodeBId) {
                        assertEquals(nodeCId, bblink.getNode().getId());
                        assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                        assertEquals(portCB, bblink.getBridgePort());
                        assertEquals(portBC, bblink.getDesignatedPort());
                    } else {
                        assertEquals(false, true);
                    }
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }
        
        public void checkwithshared(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeAId));
            assertEquals(0, domain.getForwarders(nodeCId));
            assertEquals(1, domain.getForwarders(nodeBId));
            BridgeForwardingTableEntry forwentry = domain.getForwarders(nodeBId).iterator().next();
            assertEquals(nodeBId.intValue(), forwentry.getNodeId().intValue());
            assertEquals(portBA.intValue(), forwentry.getBridgePort().intValue());
            assertEquals(macA, forwentry.getMacAddress());
            List<SharedSegment> shsegms = domain.getSharedSegments();
            assertEquals(5, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    if (bblink.getDesignatedNode().getId() == nodeAId) {
                        assertEquals(nodeAId, bblink.getDesignatedNode().getId());
                        assertEquals(nodeBId, bblink.getNode().getId());
                        assertEquals(portBA, bblink.getBridgePort());
                        assertEquals(portAB, bblink.getDesignatedPort());
                     } else {
                        assertEquals(false, true);
                     }
               } else if (shared.getMacsOnSegment().contains(shar)) {
                   assertEquals(2, shared.getBridgeIdsOnSegment().size());
                   assertEquals(1, links.size());
                   assertEquals(1, bblinks.size());
                   assertEquals(1, shared.getMacsOnSegment().size());
                   BridgeBridgeLink bblink = bblinks.iterator().next();
                   assertEquals(nodeCId, bblink.getNode().getId());
                   assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                   assertEquals(portCB, bblink.getBridgePort());
                   assertEquals(portBC, bblink.getDesignatedPort());
                   
                   BridgeMacLink link = links.iterator().next();
                   assertEquals(nodeBId, link.getNode().getId());
                   assertEquals(portB,link.getBridgePort());
                   assertEquals(shar, link.getMacAddress());
                   assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                   
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(0, bblinks.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(0, bblinks.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(0, bblinks.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort().getBridgePort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else {
                    assertEquals(false, true);
                }
            }
        }

    }
    
    class DEFGHILTopology {
        Integer nodeDId = 104;
        Integer nodeEId = 105;
        Integer nodeFId = 106;
        Integer nodeGId = 107;
        Integer nodeHId = 108;
        Integer nodeIId = 109;
        Integer nodeLId = 110;

        Integer portD  = 44;
        Integer portDD = 40;
        
        Integer portE  = 55;
        Integer portEE = 50;

        Integer portF  = 66;
        Integer portFF = 60;

        Integer portG7 = 7;
        Integer portG8 = 8;
        Integer portGD = 74;
        Integer portGE = 75;
        Integer portGF = 76;
        
        Integer portH1 = 1;
        Integer portH2 = 2;
        Integer portHH = 80;

        Integer portI3 = 3;
        Integer portI4 = 4;
        Integer portII = 90;

        Integer portL5 = 3;
        Integer portL6 = 4;
        Integer portLL = 20;

        String mac1 = "000daaaa0441"; // port D  ---port EE ---port FF ---portGD --portH1 --portII --portLL
        String mac2 = "000daaaa0442"; // port D  ---port EE ---port FF ---portGD --portH2 --portII --portLL
        String mac3 = "000daaaa0663"; // port DD ---port EE ---port F  ---portGF --portHH --portI3 --portLL
        String mac4 = "000daaaa0664"; // port DD ---port EE ---port F  ---portGF --portHH --portI4 --portLL
        String mac5 = "000daaaa0555"; // port DD ---port E ---port FF  ---portGE --portHH --portII --portL5
        String mac6 = "000daaaa0556"; // port DD ---port E ---port FF  ---portGE --portHH --portII --portL6
        String mac7 = "000daaaa0707"; // port DD ---port EE ---port FF ---portG7 --portHH --portII --portLL
        String mac8 = "000daaaa0808"; // port DD ---port EE ---port FF ---portG8 --portHH --portII --portLL

        OnmsNode nodeD= new OnmsNode();
        OnmsNode nodeE= new OnmsNode();
        OnmsNode nodeF= new OnmsNode();
        OnmsNode nodeG= new OnmsNode();
        OnmsNode nodeH= new OnmsNode();
        OnmsNode nodeI= new OnmsNode();
        OnmsNode nodeL= new OnmsNode();

        BridgeElement elementD = new BridgeElement();
        BridgeElement elementE = new BridgeElement();
        BridgeElement elementF = new BridgeElement();
        BridgeElement elementG = new BridgeElement();
        BridgeElement elementH = new BridgeElement();
        BridgeElement elementI = new BridgeElement();
        BridgeElement elementL = new BridgeElement();

        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

        Set<BridgeForwardingTableEntry> bftD = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftE = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftF = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftG = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftH = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftI = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftL = new HashSet<BridgeForwardingTableEntry>();

        /*
         *         -----------------     -----------------
         *  mac1---||portH1| portHH| --  ||portD|        |
         *         |   "Bridge H"  |     |   "Bridge D"  |     |-------------
         *  mac2---||portH2|       |     |       |portDD||-----||portGD|     |
         *         -----------------     -----------------     |             |
         *                                                     |             |
         *         -----------------     -----------------     |    |port G7||---mac7
         *  mac3---||portI3| portII| --  ||portF|        |     |             |
         *         |   "Bridge I"  |     |   "Bridge F"  |     |             |
         *  mac4---||portI4|       |     |       |portFF||-----||portGF|     |
         *         -----------------     -----------------     |             |
         *                                                     | "Bridge G"  |
         *         -----------------     -----------------     |             |
         *  mac5---||portL5| portLL| --  ||portE|        |     |    |port G8||---mac8
         *         |   "Bridge L"  |     |   "Bridge E"  |     |             |
         *  mac6---||portL6|       |     |       |portEE||-----||portGE|     |
         *         -----------------     -----------------     |-------------|
         */
    
        public DEFGHILTopology() {
            nodeD.setId(nodeDId);
            elementD.setNode(nodeD);
            elementD.setBaseBridgeAddress("dddddddddddd");
            elemlist.add(elementD);
    
            nodeE.setId(nodeEId);
            elementE.setNode(nodeE);
            elementE.setBaseBridgeAddress("ddddddddeddd");
            elemlist.add(elementE);
    
            nodeF.setId(nodeFId);
            elementF.setNode(nodeF);
            elementF.setBaseBridgeAddress("ddddddddfddd");
            elemlist.add(elementF);

            nodeG.setId(nodeGId);
            elementG.setNode(nodeG);
            elementG.setBaseBridgeAddress("ddddddd1dddd");
            elemlist.add(elementG);

            nodeI.setId(nodeIId);
            elementI.setNode(nodeI);
            elementI.setBaseBridgeAddress("ddddddd2dddd");
            elemlist.add(elementI);

            nodeH.setId(nodeHId);
            elementH.setNode(nodeH);
            elementH.setBaseBridgeAddress("ddddddd3dddd");
            elemlist.add(elementH);
            
            nodeL.setId(nodeLId);
            elementL.setNode(nodeL);
            elementL.setBaseBridgeAddress("ddddddd4dddd");
            elemlist.add(elementL);

            bftD.add(addBridgeForwardingTableEntry(nodeD,portD,  mac1));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portD,  mac2));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac3));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac4));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac5));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac6));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac7));
            bftD.add(addBridgeForwardingTableEntry(nodeD,portDD, mac8));

            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac1));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac2));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac3));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac4));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portE,  mac5));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portE,  mac6));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac7));
            bftE.add(addBridgeForwardingTableEntry(nodeE,portEE, mac8));

            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac1));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac2));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portF,  mac3));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portF,  mac4));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac5));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac6));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac7));
            bftF.add(addBridgeForwardingTableEntry(nodeF,portFF, mac8));

            bftG.add(addBridgeForwardingTableEntry(nodeG,portGD, mac1));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGD, mac2));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGF, mac3));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGF, mac4));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGE, mac5));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portGE, mac6));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portG7, mac7));
            bftG.add(addBridgeForwardingTableEntry(nodeG,portG8, mac8));

            bftH.add(addBridgeForwardingTableEntry(nodeH,portH1, mac1));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portH2, mac2));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac3));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac4));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac5));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac6));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac7));
            bftH.add(addBridgeForwardingTableEntry(nodeH,portHH, mac8));

            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac1));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac2));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portI3, mac3));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portI4, mac4));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac5));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac6));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac7));
            bftI.add(addBridgeForwardingTableEntry(nodeI,portII, mac8));

            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac1));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac2));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac3));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac4));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portL5, mac5));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portL6, mac6));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac7));
            bftL.add(addBridgeForwardingTableEntry(nodeL,portLL, mac8));
          }
        
        public void checkDE(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeEId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portE,link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac3));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(4, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeBridgeLink bblink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeDId.intValue(), bblink.getDesignatedNode().getId().intValue());
                    assertEquals(portDD.intValue(), bblink.getDesignatedPort().intValue());
                    assertEquals(nodeEId.intValue(), bblink.getNode().getId().intValue());
                    assertEquals(portEE.intValue(), bblink.getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeDId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portDD,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac3));
                    assertTrue(macs.contains(mac4));
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDF(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
             List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac5));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(4, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink bblink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeDId.intValue(), bblink.getDesignatedNode().getId().intValue());
                    assertEquals(portDD.intValue(), bblink.getDesignatedPort().intValue());
                    assertEquals(nodeFId.intValue(), bblink.getNode().getId().intValue());
                    assertEquals(portFF.intValue(), bblink.getBridgePort().intValue());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeDId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portDD,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac5));
                    assertTrue(macs.contains(mac6));
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDG(BroadcastDomain domain) throws BridgeTopologyException  {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeGId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(6, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeDId, link.getDesignatedNode().getId());
                    assertEquals(nodeGId, link.getNode().getId());
                    assertEquals(portDD, link.getDesignatedPort());
                    assertEquals(portGD, link.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portGF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portGF,link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                         }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portGE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portGE,link.getBridgePort());
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                     }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portG7.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(portG7,link.getBridgePort());
                            assertEquals(nodeGId, link.getNode().getId());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                     assertEquals(portG8.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                     for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                             assertEquals(portG8,link.getBridgePort());
                             assertEquals(nodeGId, link.getNode().getId());
                             assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                     }
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkEF(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeEId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portF,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac3));
                    assertTrue(macs.contains(mac4));
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        Set<String> macs = new HashSet<String>();
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portE,link.getBridgePort());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            macs.add(link.getMacAddress());
                         }
                        assertTrue(macs.contains(mac5));
                        assertTrue(macs.contains(mac6));
                 } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac7));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeBridgeLink bblink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeEId.intValue(), bblink.getDesignatedNode().getId().intValue());
                    assertEquals(portEE, bblink.getDesignatedPort());
                    assertEquals(nodeFId.intValue(), bblink.getNode().getId().intValue());
                    assertEquals(portFF, bblink.getBridgePort());
                    assertEquals(4, SharedSegment.getBridgeMacLinks(shared).size());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac1));
                    assertTrue(macs.contains(mac2));
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));

                } else {
                    assertEquals(false, true);
                }

            }
        }

        public void checkDEF(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeEId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(4, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertTrue(!macs.contains(link.getMacAddress()));
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                       macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac1));
                    assertTrue(macs.contains(mac2));
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portE,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                     }
                    assertTrue(macs.contains(mac5));
                    assertTrue(macs.contains(mac6));
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        Set<String> macs = new HashSet<String>();
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            macs.add(link.getMacAddress());
                         }
                        assertTrue(macs.contains(mac3));
                        assertTrue(macs.contains(mac4));
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(3, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(2,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(2, SharedSegment.getBridgeBridgeLinks(shared).size());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portDD,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac7));
                    assertTrue(macs.contains(mac8));
                } else {
                    assertEquals(false, true);
                }            }
        }

        public void checkDEFG(BroadcastDomain domain) throws BridgeTopologyException {
            assertEquals(0, domain.getForwarders(nodeDId).size());
            assertEquals(0, domain.getForwarders(nodeEId).size());
            assertEquals(0, domain.getForwarders(nodeFId).size());
            assertEquals(0, domain.getForwarders(nodeGId).size());
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(8, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    if (shared.getBridgeIdsOnSegment().contains(nodeDId)) {
                        assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portDD,link.getDesignatedPort());
                        assertEquals(nodeGId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portGD,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeEId)) {
                        assertEquals(nodeEId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGE,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeFId)) {
                        assertEquals(nodeFId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portFF,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGF,link.getDesignatedPort());
                    } else {
                        assertEquals(false, true);
                    }
                } else {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                       assertTrue(!macs.contains(link.getMacAddress()));
                       assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                       macs.add(link.getMacAddress());
                   }
                   assertTrue(macs.contains(mac1));
                   assertTrue(macs.contains(mac2));

                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portE.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    Set<String> macs = new HashSet<String>();
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portE,link.getBridgePort());
                        assertTrue(!macs.contains(link.getMacAddress()));
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        macs.add(link.getMacAddress());
                    }
                    assertTrue(macs.contains(mac5));
                    assertTrue(macs.contains(mac6));
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, SharedSegment.getBridgeMacLinks(shared).size());
                        assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                        Set<String> macs = new HashSet<String>();
                        for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            macs.add(link.getMacAddress());
                        }
                        assertTrue(macs.contains(mac3));
                        assertTrue(macs.contains(mac4));
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portG7.intValue(), link.getBridgePort().intValue());
                    assertEquals(mac7, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                     assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                     BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                     assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                     assertEquals(portG8.intValue(), link.getBridgePort().intValue());
                     assertEquals(mac8, link.getMacAddress());
                     assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else {
                    assertEquals(false, true);
                }            
                }
            }
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(14, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    BridgeBridgeLink link = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    if (shared.getBridgeIdsOnSegment().contains(nodeDId) && shared.getDesignatedPort().getBridgePort().intValue() == portDD.intValue()) {
                        assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portDD,link.getDesignatedPort());
                        assertEquals(nodeGId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portGD,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeDId) && shared.getDesignatedPort().getBridgePort().intValue() == portD.intValue()) {
                            assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                            assertEquals(portD,link.getDesignatedPort());
                            assertEquals(nodeHId.intValue(),link.getNode().getId().intValue());
                            assertEquals(portHH,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeGId) && shared.getDesignatedPort().getBridgePort().intValue() == portGE.intValue()) {
                        assertEquals(nodeEId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGE,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeEId) && shared.getDesignatedPort().getBridgePort().intValue() == portE.intValue()) {
                        assertEquals(nodeEId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portE,link.getDesignatedPort());
                        assertEquals(nodeLId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portLL,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeGId) && shared.getDesignatedPort().getBridgePort().intValue() == portGF.intValue()) {
                        assertEquals(nodeFId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portFF,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGF,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeFId) && shared.getDesignatedPort().getBridgePort().intValue() == portF.intValue()) {
                        assertEquals(nodeFId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portF,link.getDesignatedPort());
                        assertEquals(nodeIId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portII,link.getBridgePort());
                    } else {
                        assertEquals(false, true);
                    }
                } else {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeHId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeHId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portH1.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeHId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portH1,link.getBridgePort());
                    assertEquals(mac1,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeHId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeHId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portH2.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeHId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portH2,link.getBridgePort());
                    assertEquals(mac2,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeIId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeIId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portI3.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeIId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portI3,link.getBridgePort());
                    assertEquals(mac3,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac4)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeIId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeIId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portI4.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeIId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portI4,link.getBridgePort());
                    assertEquals(mac4,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeLId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeLId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portL5.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeLId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portL5,link.getBridgePort());
                    assertEquals(mac5,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                } else if (shared.getMacsOnSegment().contains(mac6)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeLId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(nodeLId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portL6.intValue(), shared.getDesignatedPort().getBridgePort().intValue());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeLId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portL6,link.getBridgePort());
                    assertEquals(mac6,link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portG7.intValue(), link.getBridgePort().intValue());
                    assertEquals(mac7, link.getMacAddress());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                     assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                     BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                     assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                     assertEquals(portG8.intValue(), link.getBridgePort().intValue());
                     assertEquals(mac8, link.getMacAddress());
                     assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                 } else {
                    assertEquals(false, true);
                }            
                }
            }
        }

    }
    
    class TwoNodeTopology {
        Integer nodeAId = 22101;
        Integer nodeBId = 22102;
        Integer portA1 = 1;
        Integer portA6 = 6;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;
        Integer portB7 = 7 ;

        
        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 
        String macA13 = "000daa000a13"; // port A1  
        String macA14 = "000daa000a14"; // port A1          

        String macA61 = "000daa000a61"; // port A6 ---port BA 
        String macA62 = "000daa000a62"; // port A6 ---port BA 
        String macA63 = "000daa000a63"; // port A6 ---port BA  
        String macA64 = "000daa000a64"; // port A6          

        String macshared1  = "000daa000ab1"; // port AB ---port BA 
        String macshared2  = "000daa000ab2"; // port AB ---port BA 
        String macshared3  = "000daa000ab3"; // port AB ---port BA 
        String macAforwd4  = "000daa000ab4"; // port AB 
        String macAforwd5  = "000daa000ab5"; // port AB 
        String macAforwd6  = "000daa000ab6"; // port AB 

        String macBforwd1  = "000daa000ba1"; //          ---port BA 
        String macBforwd2  = "000daa000ba2"; //          ---port BA 
        String macBforwd3  = "000daa000ba3"; //          ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2
        String macB23 = "000daa000b23"; //         ---port B2

        String macB71 = "000daa000b71"; //         ---port B7 
        String macB72 = "000daa000b72"; //         ---port B7
        String macB73 = "000daa000b73"; //         ---port B7

        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

        public TwoNodeTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);
        
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA11));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA12));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA13));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA14));
            
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA61));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA62));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA63));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA6, macA64));

            //3 Shared 
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macshared1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macshared2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macshared3));

            //3 forwarders
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAforwd4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAforwd5));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAforwd6));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB21));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB22));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA11));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA12));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA61));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA62));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA63));
 
            //3 shared
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macshared1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macshared2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macshared3));
            
            //3 forwarders
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macBforwd1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macBforwd2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macBforwd3));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB21));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB22));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB23));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, macB71));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, macB72));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, macB73));

        }
        
    public void check2nodeTopology(BroadcastDomain domain, boolean revertedbblink) throws BridgeTopologyException {
        Set<BridgeForwardingTableEntry> forwardersA = domain.getForwarders(nodeAId);
        Set<BridgeForwardingTableEntry> forwardersB = domain.getForwarders(nodeBId);
        assertEquals(3, forwardersA.size());
        assertEquals(3, forwardersB.size());
        Set<String> formacsA = new HashSet<String>();
        for (BridgeForwardingTableEntry forward: forwardersA) {
            assertEquals(nodeAId.intValue(), forward.getNodeId().intValue());
            assertEquals(portAB, forward.getBridgePort());
            assertTrue(!formacsA.contains(forward.getMacAddress()));
            formacsA.add(forward.getMacAddress());
        }
        Set<String> formacsB = new HashSet<String>();
        for (BridgeForwardingTableEntry forward: forwardersB) {
            assertEquals(nodeBId.intValue(), forward.getNodeId().intValue());
            assertEquals(portBA, forward.getBridgePort());
            assertTrue(!formacsA.contains(forward.getMacAddress()));
            formacsB.add(forward.getMacAddress());
        }
        assertTrue(formacsA.contains(macAforwd4));
        assertTrue(formacsA.contains(macAforwd5));
        assertTrue(formacsA.contains(macAforwd6));
        assertTrue(formacsB.contains(macBforwd1));
        assertTrue(formacsB.contains(macBforwd2));
        assertTrue(formacsB.contains(macBforwd3));

    	List<SharedSegment> shsegs = domain.getSharedSegments();
        assertEquals(5, shsegs.size());
        for (SharedSegment shared: shsegs) {
            assertTrue(!shared.noMacsOnSegment());
            Set<Integer> nodeidsOnSegment = shared.getBridgeIdsOnSegment();
            List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
            List<BridgeBridgeLink> bblinks = SharedSegment.getBridgeBridgeLinks(shared);
            if (shared.getMacsOnSegment().contains(macshared1)) {
                assertEquals(2, nodeidsOnSegment.size());
                assertEquals(3, shared.getMacsOnSegment().size());
                assertEquals(3, links.size());
                assertEquals(1, bblinks.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertTrue(shared.getMacsOnSegment().contains(macshared2));
                assertTrue(shared.getMacsOnSegment().contains(macshared3));
                assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                Set<String> macs = new HashSet<String>();
                if (revertedbblink) {
                	assertEquals(nodeBId, dlink.getDesignatedNode().getId());
                	assertEquals(portBA, dlink.getDesignatedPort());
                	assertEquals(nodeAId, dlink.getNode().getId());
                	assertEquals(portAB, dlink.getBridgePort());                   
                        for (BridgeMacLink link: links) {
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                            assertEquals(link.getNode().getId().intValue(), nodeBId.intValue());
                            assertEquals(portBA, link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            macs.add(link.getMacAddress());
                        }
                } else {
                	assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                	assertEquals(portAB, dlink.getDesignatedPort());
                	assertEquals(nodeBId, dlink.getNode().getId());
                	assertEquals(portBA, dlink.getBridgePort());                                   	
                        for (BridgeMacLink link: links) {
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                            assertEquals(link.getNode().getId().intValue(), nodeAId.intValue());
                            assertEquals(portAB, link.getBridgePort());
                            assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                            assertTrue(!macs.contains(link.getMacAddress()));
                            macs.add(link.getMacAddress());
                        }
                }
                assertTrue(macs.contains(macshared1));
                assertTrue(macs.contains(macshared2));
                assertTrue(macs.contains(macshared3));

            } else if (shared.getMacsOnSegment().contains(macB21)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertEquals(3, shared.getMacsOnSegment().size());
                assertEquals(3, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macB22));
                assertTrue(shared.getMacsOnSegment().contains(macB23));
                Set<String> macs = new HashSet<String>();
               for (BridgeMacLink link: links) {
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB2, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macB21));
               assertTrue(macs.contains(macB22));
               assertTrue(macs.contains(macB23));
            } else if (shared.getMacsOnSegment().contains(macB71)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertEquals(3, shared.getMacsOnSegment().size());
                assertEquals(3, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macB72));
                assertTrue(shared.getMacsOnSegment().contains(macB73));
                Set<String> macs = new HashSet<String>();
                for (BridgeMacLink link: links) {
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB7, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macB71));
               assertTrue(macs.contains(macB72));
               assertTrue(macs.contains(macB73));
            } else if (shared.getMacsOnSegment().contains(macA11)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertEquals(4, shared.getMacsOnSegment().size());
                assertEquals(4, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macA12));
                assertTrue(shared.getMacsOnSegment().contains(macA13));
                assertTrue(shared.getMacsOnSegment().contains(macA14));
                Set<String> macs = new HashSet<String>();
                for (BridgeMacLink link: links) {
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA1, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macA11));
               assertTrue(macs.contains(macA12));
               assertTrue(macs.contains(macA13));
               assertTrue(macs.contains(macA14));
              } else if (shared.getMacsOnSegment().contains(macA61)) {
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertEquals(4, shared.getMacsOnSegment().size());
                assertEquals(4, links.size());
                assertTrue(shared.getMacsOnSegment().contains(macA62));
                assertTrue(shared.getMacsOnSegment().contains(macA63));
                assertTrue(shared.getMacsOnSegment().contains(macA64));
                Set<String> macs = new HashSet<String>();
                for (BridgeMacLink link: links) {
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA6, link.getBridgePort());
                    assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    assertTrue(!macs.contains(link.getMacAddress()));
                    macs.add(link.getMacAddress());
                }
               assertTrue(macs.contains(macA61));
               assertTrue(macs.contains(macA62));
               assertTrue(macs.contains(macA63));
               assertTrue(macs.contains(macA64));
            } else {
                assertTrue(false);
            }
        }
    }
    
    }

    class OneBridgeOnePortOneMacTopology {
        Integer nodeAId  = 10;
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;
        Integer portA5 = 5;

        String mac1 = "000daaaa0001"; // learned on port A1
        String mac2 = "000daaaa0002"; // learned on port A2 
        String mac3 = "000daaaa0003"; // learned on port A3 
        String mac4 = "000daaaa0004"; // learned on port A4 
        String mac5 = "000daaaa0005"; // learned on port A5 

        OnmsNode nodeA= new OnmsNode();
        BridgeElement element = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        
        public OneBridgeOnePortOneMacTopology() {
            nodeA.setId(nodeAId);
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA2, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA3, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA4, mac4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA5, mac5));
            
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> links = domain.getSharedSegments();
            assertEquals(5, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(1, shared.getMacsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                    assertEquals(link.getBridgePort(),shared.getDesignatedPort().getBridgePort());
                if (link.getBridgePort() == portA1) {
                    assertEquals(mac1, link.getMacAddress());
                } else if (link.getBridgePort() == portA2) {
                    assertEquals(mac2, link.getMacAddress());
                } else if (link.getBridgePort() == portA3) {
                    assertEquals(mac3, link.getMacAddress());
                } else if (link.getBridgePort() == portA4) {
                    assertEquals(mac4, link.getMacAddress());
                } else if (link.getBridgePort() == portA5) {
                    assertEquals(mac5, link.getMacAddress());
                } else {
                    assertEquals(-1, 1);
                }
                }
            }

        }
    }
    
    class OneBridgeMoreMacOnePortTopology {
        Integer nodeAId  = 20;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement element = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();

        Integer portA1 = 1;

        String mac1 = "000daaaa0001"; // port A1 
        String mac2 = "000daaaa0002"; // port A1
        String mac3 = "000daaaa0003"; // port A1
        String mac4 = "000daaaa0004"; // port A1

        
        public OneBridgeMoreMacOnePortTopology() {
            nodeA.setId(nodeAId);
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);


            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac4));

        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> links = domain.getSharedSegments();

            assertEquals(1, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(portA1,shared.getDesignatedPort().getBridgePort());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(4, shared.getMacsOnSegment().size());
                final Set<String> macs = shared.getMacsOnSegment();
                assertTrue(macs.contains(mac1));
                assertTrue(macs.contains(mac2));
                assertTrue(macs.contains(mac3));
                assertTrue(macs.contains(mac4));
                for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                    assertEquals(portA1, link.getBridgePort());
                }
            }

        }
    }

    class OneBridgeCompleteTopology {
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;

        Integer portA23 = 23;
        Integer portA24 = 24;
        Integer portA25 = 25;

        String mac1 = "000daaaa0001"; // port A1
        String mac2 = "000daaaa0002"; // port A2
        String mac3 = "000daaaa0003"; // port A3
        String mac4 = "000daaaa0004"; // port A4

        String mac231 = "000daaaa0231"; // port A23
        String mac232 = "000daaaa0232"; // port A23
        String mac233 = "000daaaa0233"; // port A23
        String mac234 = "000daaaa0234"; // port A23

        String mac241 = "000daaaa0241"; // port A24
        String mac242 = "000daaaa0242"; // port A24
        String mac243 = "000daaaa0243"; // port A24
        String mac244 = "000daaaa0244"; // port A24
        String mac245 = "000daaaa0245"; // port A24

        String mac251 = "000daaaa0251"; // port A25
        String mac252 = "000daaaa0252"; // port A25
        String mac253 = "000daaaa0253"; // port A25

        Integer nodeAId = 30;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement element = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();

        public OneBridgeCompleteTopology() {
            nodeA.setId(nodeAId);
            nodeA.setLocation(new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID));
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);


            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA2, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA3, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA4, mac4));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac231));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac232));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac233));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA23, mac234));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac241));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac242));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac243));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac244));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA24, mac245));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA25, mac251));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA25, mac252));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA25, mac253));



        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> links = domain.getSharedSegments();
            assertEquals(7, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                    assertEquals(link.getBridgePort(),shared.getDesignatedPort().getBridgePort());
                    if (link.getBridgePort() == portA1) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac1, link.getMacAddress());
                    } else if (link.getBridgePort() == portA2) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac2, link.getMacAddress());
                    } else if (link.getBridgePort() == portA3) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac3, link.getMacAddress());
                    } else if (link.getBridgePort() == portA4) {
                        assertEquals(1, shared.getMacsOnSegment().size());
                        assertEquals(mac4, link.getMacAddress());
                    } else if (link.getBridgePort() == portA23) {
                        final Set<String> macs = shared.getMacsOnSegment();
                        assertEquals(4, macs.size());
                        assertTrue(macs.contains(mac231));
                        assertTrue(macs.contains(mac232));
                        assertTrue(macs.contains(mac233));
                        assertTrue(macs.contains(mac234));
                    } else if (link.getBridgePort() == portA24) {
                        final Set<String> macs = shared.getMacsOnSegment();
                        assertEquals(5, macs.size());
                        assertTrue(macs.contains(mac241));
                        assertTrue(macs.contains(mac242));
                        assertTrue(macs.contains(mac243));
                        assertTrue(macs.contains(mac244));
                        assertTrue(macs.contains(mac245));
                    } else if (link.getBridgePort() == portA25) {
                        final Set<String> macs = shared.getMacsOnSegment();
                        assertEquals(3, macs.size());
                        assertTrue(macs.contains(mac251));
                        assertTrue(macs.contains(mac252));
                        assertTrue(macs.contains(mac253));
                    } else {
                        assertEquals(-1, 1);
                    }
                }
            }

        }        
    }
    
    class TwoConnectedBridgeTopology {
        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;
        Integer portA5 = 5;
        Integer portAB = 16;
        Integer portBA = 24;
        Integer portB6 = 6;
        Integer portB7 = 7;
        Integer portB8 = 8;
        Integer portB9 = 9;

        String mac1 = "000daaaa0001"; // port A1 ---port BA
        String mac2 = "000daaaa0002"; // port A2 ---port BA
        String mac3 = "000daaaa0003"; // port A3 ---port BA
        String mac4 = "000daaaa0004"; // port A4 ---port BA
        String mac5 = "000daaaa0005"; // port A5 ---port BA
        String mac6 = "000daaaa0006"; // port AB ---port B6 
        String mac7 = "000daaaa0007"; // port AB ---port B7
        String mac8 = "000daaaa0008"; // port AB ---port B8
        String mac9 = "000daaaa0009"; // port AB ---port B9

        Integer nodeAId  = 1111;
        Integer nodeBId = 2222;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();

        public TwoConnectedBridgeTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA2, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA3, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA4, mac4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA5, mac5));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac6));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac7));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac8));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac9));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac3));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac4));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac5));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB6, mac6));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB7, mac7));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB8, mac8));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB9, mac9));

        }
        
        public void check(BroadcastDomain domain, boolean reverse) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();

            assertEquals(10, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(0, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    BridgeBridgeLink link=SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    if (reverse) {
                        assertEquals(nodeBId, link.getDesignatedNode().getId());
                        assertEquals(portBA,link.getDesignatedPort());
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portAB,link.getBridgePort());
                    	
                    } else {
                    	assertEquals(nodeAId, link.getDesignatedNode().getId());
                    	assertEquals(portAB,link.getDesignatedPort());
                    	assertEquals(nodeBId, link.getNode().getId());
                    	assertEquals(portBA,link.getBridgePort());
                    }
                } else {
                    assertEquals(1, shared.getMacsOnSegment().size());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    if (link.getMacAddress().equals(mac1)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA1,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac2)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA2,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac3)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA3,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac4)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA4,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac5)) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA5,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac6)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB6,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac7)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB7,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac8)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB8,link.getBridgePort());
                    } else if (link.getMacAddress().equals(mac9)) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB9,link.getBridgePort());
                    } else {
                        assertEquals(false, true);
                    }
                }
            }

        }
    }
    
    class TwoMergeBridgeTopology {
        Integer portA8 = 8;
        Integer portAB = 16;
        Integer portBA = 24;
        Integer portB6 = 6;

        String mac1 = "000daaaa0001"; // port AB ---port BA
        String mac2 = "000daaaa0002"; // port AB ---port BA
        String mac3 = "000daaaa0003"; // port AB ---port BA
        String mac4 = "000daaaa0004"; // port AB ---port BA
        String mac5 = "000daaaa0005"; // port AB ---port BA
        String mac6 = "000daaaa0006"; // port AB ---port B6
        String mac7 = "000daaaa0007"; // port AB ---port BA
        String mac8 = "000daaaa0008"; // port A8 ---port BA
        String mac9 = "000daaaa0009"; // port AB ---port BA
        Integer nodeAId  = 1111;
        Integer nodeBId  = 2222;
        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
               List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();

        public TwoMergeBridgeTopology() {

            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);


            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac1));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac3));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac4));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac5));

            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac6));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac7));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA8, mac8));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac9));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac3));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac4));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac5));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portB6, mac6));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac7));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac8));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac9));


        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                assertEquals(false, shared.noMacsOnSegment());
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portAB,shared.getDesignatedPort().getBridgePort());
                    assertEquals(7, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(7, shared.getMacsOnSegment().size());
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac2));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac3));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac4));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac5));
                    assertEquals(false, shared.getMacsOnSegment().contains(mac6));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac7));
                    assertEquals(false, shared.getMacsOnSegment().contains(mac8));
                    assertEquals(true,  shared.getMacsOnSegment().contains(mac9));
                    for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(shared)) {
                        assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                        assertEquals(link.getNode().getId().intValue(),nodeAId.intValue());
                        assertEquals(portAB,link.getBridgePort());
                    }
                    BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                    assertEquals(portAB, dlink.getDesignatedPort());
                    assertEquals(nodeBId, dlink.getNode().getId());
                    assertEquals(portBA, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac6)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB6,shared.getDesignatedPort().getBridgePort());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(mac6, link.getMacAddress());
                    assertEquals(nodeBId,link.getNode().getId());
                    assertEquals(portB6,link.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac8)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(1, SharedSegment.getBridgeMacLinks(shared).size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA8,shared.getDesignatedPort().getBridgePort());
                    BridgeMacLink link = SharedSegment.getBridgeMacLinks(shared).iterator().next();
                    assertEquals(mac8, link.getMacAddress());
                    assertEquals(nodeAId,link.getNode().getId());
                    assertEquals(portA8,link.getBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }
    }
    
    class TwoBridgeWithBackbonePortsTopology {
        Integer nodeAId = 1101;
        Integer nodeBId = 1102;
        Integer portA1 = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;

        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; // port AB ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2

        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();

        public TwoBridgeWithBackbonePortsTopology() {

            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);



            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA11));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA12));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macAB));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB21));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB22));


            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA11));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA12));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macAB));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB21));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB22));


        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());
            for (Integer nodeid: domain.getBridgeNodesOnDomain()) {
                assertEquals(0, domain.getForwarders(nodeid).size());
            }
            for (SharedSegment shared: shsegs) {
                assertEquals(false, shared.noMacsOnSegment());
                if (shared.getMacsOnSegment().contains(macAB)) {
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(1, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(macAB, link.getMacAddress());
                        assertEquals(nodeA.getId().intValue(),link.getNode().getId().intValue());
                        assertEquals(portAB,link.getBridgePort());
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                    }
                    BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                    assertEquals(portAB, dlink.getDesignatedPort());
                    assertEquals(nodeBId, dlink.getNode().getId());
                    assertEquals(portBA, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(macA11)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA1,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA1, link.getBridgePort());
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macA11)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macA12)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else if (shared.getMacsOnSegment().contains(macB21)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB2,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeMacLinkType.BRIDGE_LINK, link.getLinkType());
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB2, link.getBridgePort());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macB21)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macB22)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else {
                    assertEquals(false, true);
                }
            }


        }
    }
    
    class TwoBridgeWithBackbonePortsTopologyWithBridgeinBft {
        Integer nodeAId = 1101;
        Integer nodeBId = 1102;
        Integer portA1 = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;

        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; //         ---port BA 

        String macB21 = "000daa000b21"; //         ---port B2 
        String macB22 = "000daa000b22"; //         ---port B2
        String macB   = "bbbbbbbbbbbb"; // portAB
        OnmsNode nodeA= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();

        public TwoBridgeWithBackbonePortsTopologyWithBridgeinBft() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);

            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA11));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portA1, macA12));
            bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macB));

            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA11));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macA12));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macAB));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB21));
            bftB.add(addBridgeForwardingTableEntry(nodeB,portB2, macB22));
            
        }
        
        public void check(BroadcastDomain domain) throws BridgeTopologyException {
            List<SharedSegment> shsegs = domain.getSharedSegments();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().size() == 0) {
                    assertEquals(1, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(0, shared.getMacsOnSegment().size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(0, links.size());
                    BridgeBridgeLink dlink = SharedSegment.getBridgeBridgeLinks(shared).iterator().next();
                    assertEquals(nodeBId, dlink.getDesignatedNode().getId());
                    assertEquals(portBA, dlink.getDesignatedPort());
                    assertEquals(nodeAId, dlink.getNode().getId());
                    assertEquals(portAB, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(macA11)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA1,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(nodeAId, link.getNode().getId());
                        assertEquals(portA1, link.getBridgePort());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macA11)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macA12)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else if (shared.getMacsOnSegment().contains(macB21)) {
                    assertEquals(0, SharedSegment.getBridgeBridgeLinks(shared).size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB2,shared.getDesignatedPort().getBridgePort());
                    List<BridgeMacLink> links = SharedSegment.getBridgeMacLinks(shared);
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(nodeBId, link.getNode().getId());
                        assertEquals(portB2, link.getBridgePort());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getMacAddress().equals(macB21)) {
                            assertTrue(true);
                            pass1 = false;
                       } else if (pass2 && link.getMacAddress().equals(macB22)) {
                            assertTrue(true);
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else {
                    assertEquals(false, true);
                }
            }

        }
    }

}
