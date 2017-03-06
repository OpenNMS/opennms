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


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.OnmsNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.str;

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

    public static void printBridgeTopology(BroadcastDomain domain) {
            System.err.println(domain.printTopology());
    }
        
    public List<BridgeMacLink> addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, String mac, List<BridgeMacLink> bft) {
        BridgeMacLink link = new BridgeMacLink();
        link.setNode(node);
        link.setBridgePort(bridgeport);
        link.setMacAddress(mac);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        bft.add(link);
        return bft;
    }
    


    protected static void printBackboneBridgeMacLink(BridgeMacLink link1, BridgeMacLink link2) {

        if (link1.getMacAddress().equals(link2.getMacAddress())) {
            System.err.println("nodeid: " + link1.getNode().getId() + " port:"
                    + link1.getBridgePort() + "-->" +
                    link1.getMacAddress()
                    + "<-- port: " + link2.getBridgePort() + " nodeid: " + link2.getNode().getId());
        }
    }

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

        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftC = new ArrayList<BridgeMacLink>();


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
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);
    
            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);
    
            nodeC.setId(nodeCId);
            elementC.setNode(nodeC);
            elementC.setBaseBridgeAddress("cccccccccccc");
            elemlist.add(elementC);

            bftA =addBridgeForwardingTableEntry(nodeA,portA, mac1,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac2,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac3,bftA);

            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac1,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB, mac2,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBC, mac3,bftB);

            bftC =addBridgeForwardingTableEntry(nodeC,portCB, mac1,bftC);
            bftC =addBridgeForwardingTableEntry(nodeC,portCB, mac2,bftC);
            bftC =addBridgeForwardingTableEntry(nodeC,portC, mac3,bftC);
        }

        public void checkAC(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegms = domain.getTopology();        	
            assertEquals(3, shsegms.size());

            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(0, bblinks.size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, bblinks.size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, links.size());
                    assertTrue(!shared.noMacsOnSegment());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(mac2, link.getMacAddress());
                        boolean pass1 = true;
                        boolean pass2 = true;
                       if (pass1 && link.getNode().getId() == nodeAId) {
                           assertEquals(portAB,link.getBridgePort());
                            pass1 = false;
                       } else if (pass2 && link.getNode().getId() == nodeCId) {
                           assertEquals(portCB,link.getBridgePort());
                            pass2 = false;
                       } else
                            assertTrue(false);
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void checkAB(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegms = domain.getTopology(); 
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
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
                    assertEquals(portA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portBC,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkBC(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegms = domain.getTopology();
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
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
                    assertEquals(portBA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegms = domain.getTopology();
            assertEquals(5, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    if (bblink.getNode().getId() == nodeAId) {
                        assertEquals(nodeAId, bblink.getNode().getId());
                        assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                        assertEquals(portAB, bblink.getBridgePort());
                        assertEquals(portBA, bblink.getDesignatedPort());
                    } else if (bblink.getDesignatedNode().getId() == nodeCId) {
                        assertEquals(nodeBId, bblink.getNode().getId());
                        assertEquals(nodeCId, bblink.getDesignatedNode().getId());
                        assertEquals(portBC, bblink.getBridgePort());
                        assertEquals(portCB, bblink.getDesignatedPort());
                    }
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB,link.getBridgePort());
                    assertEquals(mac2, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
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

        List<BridgeMacLink> bftD = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftE = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftF = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftG = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftH = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftI = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftL = new ArrayList<BridgeMacLink>();

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
         *         |   "Bridge E"  |     |   "Bridge E"  |     |             |
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

            bftD =addBridgeForwardingTableEntry(nodeD,portD,  mac1,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portD,  mac2,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portDD, mac3,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portDD, mac4,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portDD, mac5,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portDD, mac6,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portDD, mac7,bftD);
            bftD =addBridgeForwardingTableEntry(nodeD,portDD, mac8,bftD);

            bftE =addBridgeForwardingTableEntry(nodeE,portEE, mac1,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portEE, mac2,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portEE, mac3,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portEE, mac4,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portE,  mac5,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portE,  mac6,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portEE, mac7,bftE);
            bftE =addBridgeForwardingTableEntry(nodeE,portEE, mac8,bftE);

            bftF =addBridgeForwardingTableEntry(nodeF,portFF, mac1,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portFF, mac2,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portF,  mac3,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portF,  mac4,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portFF, mac5,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portFF, mac6,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portFF, mac7,bftF);
            bftF =addBridgeForwardingTableEntry(nodeF,portFF, mac8,bftF);

            bftG =addBridgeForwardingTableEntry(nodeG,portGD, mac1,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portGD, mac2,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portGF, mac3,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portGF, mac4,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portGE, mac5,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portGE, mac6,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portG7, mac7,bftG);
            bftG =addBridgeForwardingTableEntry(nodeG,portG8, mac8,bftG);

            bftH =addBridgeForwardingTableEntry(nodeH,portH1, mac1,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portH2, mac2,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portHH, mac3,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portHH, mac4,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portHH, mac5,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portHH, mac6,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portHH, mac7,bftH);
            bftH =addBridgeForwardingTableEntry(nodeH,portHH, mac8,bftH);

            bftI =addBridgeForwardingTableEntry(nodeI,portII, mac1,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portII, mac2,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portI3, mac3,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portI4, mac4,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portII, mac5,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portII, mac6,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portII, mac7,bftI);
            bftI =addBridgeForwardingTableEntry(nodeI,portII, mac8,bftI);

            bftL =addBridgeForwardingTableEntry(nodeL,portLL, mac1,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portLL, mac2,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portLL, mac3,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portLL, mac4,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portL5, mac5,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portL6, mac6,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portLL, mac7,bftL);
            bftL =addBridgeForwardingTableEntry(nodeL,portLL, mac8,bftL);
          }
        
        public void checkDE(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                    }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                        assertEquals(2, shared.getBridgeMacLinks().size());
                        assertEquals(0, shared.getBridgeBridgeLinks().size());
                        assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portE.intValue(), shared.getDesignatedPort().intValue());
                        for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portE,link.getBridgePort());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac3));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(8, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        if (nodeDId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portDD,link.getBridgePort());
                        else if (nodeEId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portEE,link.getBridgePort());
                        else
                            assertEquals(false, true);
                    }
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDF(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, shared.getBridgeMacLinks().size());
                        assertEquals(0, shared.getBridgeBridgeLinks().size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().intValue());
                        for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac5));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(8, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        if (nodeDId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portDD,link.getBridgePort());
                        else if (nodeFId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portFF,link.getBridgePort());
                        else
                            assertEquals(false, true);
                    }
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDG(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(6, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    assertEquals(0, shared.getBridgeMacLinks().size());
                    BridgeBridgeLink link = shared.getBridgeBridgeLinks().iterator().next();
                    assertEquals(nodeDId, link.getDesignatedNode().getId());
                    assertEquals(nodeGId, link.getNode().getId());
                    assertEquals(portDD, link.getDesignatedPort());
                    assertEquals(portGD, link.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                    }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, shared.getBridgeMacLinks().size());
                        assertEquals(0, shared.getBridgeBridgeLinks().size());
                        assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portGF.intValue(), shared.getDesignatedPort().intValue());
                        for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portGF,link.getBridgePort());
                         }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portGE.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portGE,link.getBridgePort());
                     }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portG7.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(portG7,link.getBridgePort());
                            assertEquals(nodeGId, link.getNode().getId());
                    }
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(nodeGId.intValue(), shared.getDesignatedBridge().intValue());
                     assertEquals(portG8.intValue(), shared.getDesignatedPort().intValue());
                     for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                         assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                             assertEquals(portG8,link.getBridgePort());
                             assertEquals(nodeGId, link.getNode().getId());
                     }
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkEF(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(3, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portF.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portF,link.getBridgePort());
                    }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                        assertEquals(2, shared.getBridgeMacLinks().size());
                        assertEquals(0, shared.getBridgeBridgeLinks().size());
                        assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portE.intValue(), shared.getDesignatedPort().intValue());
                        for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portE,link.getBridgePort());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(4,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac7));
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(8, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        if (nodeFId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portFF,link.getBridgePort());
                        else if (nodeEId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portEE,link.getBridgePort());
                        else
                            assertEquals(false, true);
                    }
                } else {
                    assertEquals(false, true);
                }

            }
        }

        public void checkDEF(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(4, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac2));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                    }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portE.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portE,link.getBridgePort());
                     }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, shared.getBridgeMacLinks().size());
                        assertEquals(0, shared.getBridgeBridgeLinks().size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().intValue());
                        for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(3, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeDId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeEId));
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeFId));
                    assertEquals(2,shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac8));
                    assertEquals(6, shared.getBridgeMacLinks().size());
                    assertEquals(2, shared.getBridgeBridgeLinks().size());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        if (nodeDId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portDD,link.getBridgePort());
                        else if (nodeEId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portEE,link.getBridgePort());
                        else if (nodeFId.intValue() == link.getNode().getId().intValue())
                            assertEquals(portFF,link.getBridgePort());
                        else
                            assertEquals(false, true);
                    }
                } else {
                    assertEquals(false, true);
                }            }
        }

        public void checkDEFG(BroadcastDomain domain) {
        	printBridgeTopology(domain);
        	List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(8, shsegs.size());
            for (SharedSegment shared: shsegs) {
                System.err.println(shared.printTopology());
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    assertEquals(0, shared.getBridgeMacLinks().size());
                    BridgeBridgeLink link = shared.getBridgeBridgeLinks().iterator().next();
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
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeDId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portD.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                       assertEquals(nodeDId.intValue(), link.getNode().getId().intValue());
                       assertEquals(portD,link.getBridgePort());
                    }
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(true,shared.getMacsOnSegment().contains(mac6));
                    assertEquals(2, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeEId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portE.intValue(), shared.getDesignatedPort().intValue());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(nodeEId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portE,link.getBridgePort());
                     }
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                        assertEquals(1, shared.getBridgeIdsOnSegment().size());
                        assertEquals(2, shared.getMacsOnSegment().size());
                        assertEquals(true,shared.getMacsOnSegment().contains(mac4));
                        assertEquals(2, shared.getBridgeMacLinks().size());
                        assertEquals(0, shared.getBridgeBridgeLinks().size());
                        assertEquals(nodeFId.intValue(), shared.getDesignatedBridge().intValue());
                        assertEquals(portF.intValue(), shared.getDesignatedPort().intValue());
                        for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                            assertEquals(nodeFId.intValue(), link.getNode().getId().intValue());
                            assertEquals(portF,link.getBridgePort());
                         }
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portG7.intValue(), link.getBridgePort().intValue());
                        assertEquals(mac7, link.getMacAddress());
                    }
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(1, shared.getBridgeMacLinks().size());
                     assertEquals(0, shared.getBridgeBridgeLinks().size());
                     for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                         assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                         assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                         assertEquals(portG8.intValue(), link.getBridgePort().intValue());
                         assertEquals(mac8, link.getMacAddress());
                     }
                 } else {
                    assertEquals(false, true);
                }            
                }
            }
        }
        
        public void check(List<SharedSegment> shsegs) {
            //printBridgeTopology(shsegs);
            assertEquals(14, shsegs.size());
            for (SharedSegment shared: shsegs) {
                System.err.println(shared.printTopology());
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    assertEquals(0, shared.getBridgeMacLinks().size());
                    BridgeBridgeLink link = shared.getBridgeBridgeLinks().iterator().next();
                    if (shared.getBridgeIdsOnSegment().contains(nodeDId) && shared.getDesignatedPort().intValue() == portDD.intValue()) {
                        assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portDD,link.getDesignatedPort());
                        assertEquals(nodeGId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portGD,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeDId) && shared.getDesignatedPort().intValue() == portD.intValue()) {
                            assertEquals(nodeDId.intValue(),link.getDesignatedNode().getId().intValue());
                            assertEquals(portD,link.getDesignatedPort());
                            assertEquals(nodeHId.intValue(),link.getNode().getId().intValue());
                            assertEquals(portHH,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeGId) && shared.getDesignatedPort().intValue() == portGE.intValue()) {
                        assertEquals(nodeEId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portEE,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGE,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeEId) && shared.getDesignatedPort().intValue() == portE.intValue()) {
                        assertEquals(nodeEId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portE,link.getDesignatedPort());
                        assertEquals(nodeLId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portLL,link.getBridgePort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeGId) && shared.getDesignatedPort().intValue() == portGF.intValue()) {
                        assertEquals(nodeFId.intValue(),link.getNode().getId().intValue());
                        assertEquals(portFF,link.getBridgePort());
                        assertEquals(nodeGId.intValue(),link.getDesignatedNode().getId().intValue());
                        assertEquals(portGF,link.getDesignatedPort());
                    } else if (shared.getBridgeIdsOnSegment().contains(nodeFId) && shared.getDesignatedPort().intValue() == portF.intValue()) {
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
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeHId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portH1.intValue(), shared.getDesignatedPort().intValue());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeHId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portH1,link.getBridgePort());
                    assertEquals(mac1,link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeHId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeHId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portH2.intValue(), shared.getDesignatedPort().intValue());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeHId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portH2,link.getBridgePort());
                    assertEquals(mac2,link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac3)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeIId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeIId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portI3.intValue(), shared.getDesignatedPort().intValue());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeIId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portI3,link.getBridgePort());
                    assertEquals(mac3,link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac4)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeIId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeIId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portI4.intValue(), shared.getDesignatedPort().intValue());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeIId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portI4,link.getBridgePort());
                    assertEquals(mac4,link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac5)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeLId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeLId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portL5.intValue(), shared.getDesignatedPort().intValue());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeLId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portL5,link.getBridgePort());
                    assertEquals(mac5,link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac6)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeLId));
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(nodeLId.intValue(), shared.getDesignatedBridge().intValue());
                    assertEquals(portL6.intValue(), shared.getDesignatedPort().intValue());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(nodeLId.intValue(), link.getNode().getId().intValue());
                    assertEquals(portL6,link.getBridgePort());
                    assertEquals(mac6,link.getMacAddress());
                 } else if (shared.getMacsOnSegment().contains(mac7)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                    assertEquals(1,shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                        assertEquals(portG7.intValue(), link.getBridgePort().intValue());
                        assertEquals(mac7, link.getMacAddress());
                    }
                 } else if (shared.getMacsOnSegment().contains(mac8)) {
                     assertEquals(1, shared.getBridgeIdsOnSegment().size());
                     assertEquals(true,shared.getBridgeIdsOnSegment().contains(nodeGId));
                     assertEquals(1,shared.getMacsOnSegment().size());
                     assertEquals(1, shared.getBridgeMacLinks().size());
                     assertEquals(0, shared.getBridgeBridgeLinks().size());
                     for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                         assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                         assertEquals(nodeGId.intValue(), link.getNode().getId().intValue());
                         assertEquals(portG8.intValue(), link.getBridgePort().intValue());
                         assertEquals(mac8, link.getMacAddress());
                     }
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

        String macAB1  = "000daa000ab1"; // port AB ---port BA 
        String macAB2  = "000daa000ab2"; // port AB ---port BA 
        String macAB3  = "000daa000ab3"; // port AB ---port BA 
        String macAB4  = "000daa000ab4"; // port AB 
        String macAB5  = "000daa000ab5"; // port AB 
        String macAB6  = "000daa000ab6"; // port AB 

        String macBA1  = "000daa000ba1"; //          ---port BA 
        String macBA2  = "000daa000ba2"; //          ---port BA 
        String macBA3  = "000daa000ba3"; //          ---port BA 

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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();
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


            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA11,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA12,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA13,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA14,bftA);
            
            bftA =addBridgeForwardingTableEntry(nodeA,portA6, macA61,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA6, macA62,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA6, macA63,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA6, macA64,bftA);

            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB1,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB2,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB3,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB4,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB5,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB6,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macB21,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macB22,bftA);


            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA11,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA12,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA61,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA62,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA63,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macAB1,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macAB2,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macAB3,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macBA1,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macBA2,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macBA3,bftB);

            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB21,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB22,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB23,bftB);

            bftB =addBridgeForwardingTableEntry(nodeB,portB7, macB71,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB7, macB72,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB7, macB73,bftB);

        }
        
    public void check2nodeTopology(BroadcastDomain domain, boolean revertedbblink) {
    	printBridgeTopology(domain);
    	List<SharedSegment> shsegs = domain.getTopology();
        assertEquals(5, shsegs.size());
        for (SharedSegment shared: shsegs) {
            assertTrue(!shared.noMacsOnSegment());
            Set<Integer> nodeidsOnSegment = shared.getBridgeIdsOnSegment();
            List<BridgeMacLink> links = shared.getBridgeMacLinks();
            Set<String> macs = shared.getMacsOnSegment();
            if (
            		(shared.getDesignatedBridge().intValue() == nodeBId.intValue() && shared.getDesignatedPort() == portBA) 
            	|| 
                    (shared.getDesignatedBridge().intValue() == nodeAId.intValue() && shared.getDesignatedPort() == portAB)  
            		) {
                assertEquals(2, nodeidsOnSegment.size());
                assertEquals(3, macs.size());
                assertEquals(6, links.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertTrue(macs.contains(macAB1));
                assertTrue(macs.contains(macAB2));
                assertTrue(macs.contains(macAB3));
                for (BridgeMacLink link: links) {
                    if (link.getNode().getId() == nodeAId) {
                        assertEquals(portAB, link.getBridgePort());
                    } else  if (link.getNode().getId() == nodeBId) {
                        assertEquals(portBA, link.getBridgePort());
                    } else {
                        assertTrue(false);
                    }
                }
                assertEquals(1, shared.getBridgeBridgeLinks().size());
                BridgeBridgeLink dlink = shared.getBridgeBridgeLinks().iterator().next();
                if (revertedbblink) {
                	assertEquals(nodeBId, dlink.getDesignatedNode().getId());
                	assertEquals(portBA, dlink.getDesignatedPort());
                	assertEquals(nodeAId, dlink.getNode().getId());
                	assertEquals(portAB, dlink.getBridgePort());                   
                } else {
                	assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                	assertEquals(portAB, dlink.getDesignatedPort());
                	assertEquals(nodeBId, dlink.getNode().getId());
                	assertEquals(portBA, dlink.getBridgePort());                                   	
                }
            } else if (shared.getDesignatedBridge().intValue() == nodeBId.intValue() && shared.getDesignatedPort() == portB2) {
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertEquals(3, macs.size());
                assertEquals(3, links.size());
                assertTrue(macs.contains(macB21));
                assertTrue(macs.contains(macB22));
                assertTrue(macs.contains(macB23));
                for (BridgeMacLink link: links) {
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB2, link.getBridgePort());
                }
            } else if (shared.getDesignatedBridge().intValue() == nodeBId.intValue() && shared.getDesignatedPort() == portB7) {
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeBId));
                assertEquals(3, macs.size());
                assertEquals(3, links.size());
                assertTrue(macs.contains(macB71));
                assertTrue(macs.contains(macB72));
                assertTrue(macs.contains(macB73));
                for (BridgeMacLink link: links) {
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portB7, link.getBridgePort());
                }
            } else if (shared.getDesignatedBridge().intValue() == nodeAId.intValue() && shared.getDesignatedPort() == portA1) {
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertEquals(4, macs.size());
                assertEquals(4, links.size());
                assertTrue(macs.contains(macA11));
                assertTrue(macs.contains(macA12));
                assertTrue(macs.contains(macA13));
                assertTrue(macs.contains(macA14));
                for (BridgeMacLink link: links) {
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA1, link.getBridgePort());
                }
            } else if (shared.getDesignatedBridge().intValue() == nodeAId.intValue() && shared.getDesignatedPort() == portA6) {
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                assertEquals(1, nodeidsOnSegment.size());
                assertTrue(nodeidsOnSegment.contains(nodeAId));
                assertEquals(4, macs.size());
                assertEquals(4, links.size());
                assertTrue(macs.contains(macA61));
                assertTrue(macs.contains(macA62));
                assertTrue(macs.contains(macA63));
                assertTrue(macs.contains(macA64));
                for (BridgeMacLink link: links) {
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA6, link.getBridgePort());
                }
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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
        
        public OneBridgeOnePortOneMacTopology() {
            nodeA.setId(nodeAId);
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);
            bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac1, bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA2, mac2, bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA3, mac3, bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA4, mac4, bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA5, mac5, bftA);
            
        }
        
        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> links = domain.getTopology();
            assertEquals(5, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(1, shared.getMacsOnSegment().size());
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(link.getBridgePort(),shared.getDesignatedPort());
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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

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


            bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac1,bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac2,bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac3,bftA);
            bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac4,bftA);

        }
        
        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> links = domain.getTopology();

            assertEquals(1, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(portA1,shared.getDesignatedPort());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(4, shared.getMacsOnSegment().size());
                final Set<String> macs = shared.getMacsOnSegment();
                assertTrue(macs.contains(mac1));
                assertTrue(macs.contains(mac2));
                assertTrue(macs.contains(mac3));
                assertTrue(macs.contains(mac4));
                for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                    assertEquals(portA1, link.getBridgePort());
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

        public OneBridgeCompleteTopology() {
            nodeA.setId(nodeAId);
            element.setNode(nodeA);
            element.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(element);


            bftA =addBridgeForwardingTableEntry(nodeA,portA1, mac1,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA2, mac2,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA3, mac3,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA4, mac4,bftA);

            bftA =addBridgeForwardingTableEntry(nodeA,portA23, mac231,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA23, mac232,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA23, mac233,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA23, mac234,bftA);

            bftA =addBridgeForwardingTableEntry(nodeA,portA24, mac241,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA24, mac242,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA24, mac243,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA24, mac244,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA24, mac245,bftA);

            bftA =addBridgeForwardingTableEntry(nodeA,portA25, mac251,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA25, mac252,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA25, mac253,bftA);



        }
        
        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> links = domain.getTopology();
            assertEquals(7, links.size());
            for (SharedSegment shared: links) {
                assertTrue(!shared.noMacsOnSegment());
                assertEquals(nodeAId,shared.getDesignatedBridge());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(0, shared.getBridgeBridgeLinks().size());
                for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(link.getBridgePort(),shared.getDesignatedPort());
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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();

        public TwoConnectedBridgeTopology() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);

            bftA =addBridgeForwardingTableEntry(nodeA,portA1, mac1,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA2, mac2,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA3, mac3,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA4, mac4,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA5, mac5,bftA);

            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac6,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac7,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac8,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac9,bftA);


            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac1,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac2,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac3,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac4,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac5,bftB);

            bftB =addBridgeForwardingTableEntry(nodeB,portB6, mac6,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB7, mac7,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB8, mac8,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB9, mac9,bftB);

        }
        
        public void check(BroadcastDomain domain, boolean reverse) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();

            assertEquals(10, shsegs.size());
            for (SharedSegment shared: shsegs) {
                if (shared.noMacsOnSegment()) {
                    assertEquals(0, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    BridgeBridgeLink link=shared.getBridgeBridgeLinks().iterator().next();
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
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();

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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
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


            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac1,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac2,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac3,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac4,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac5,bftA);

            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac6,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac7,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA8, mac8,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, mac9,bftA);


            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac1,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac2,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac3,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac4,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac5,bftB);

            bftB =addBridgeForwardingTableEntry(nodeB,portB6, mac6,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac7,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac8,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, mac9,bftB);


        }
        
        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                assertEquals(false, shared.noMacsOnSegment());
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portAB,shared.getDesignatedPort());
                    assertEquals(14, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
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
                    for (BridgeMacLink link: shared.getBridgeMacLinks()) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertTrue(shared.getMacsOnSegment().contains(link.getMacAddress()));
                        if (link.getNode().getId() == nodeAId) {
                            assertEquals(portAB,link.getBridgePort());
                        } else if (link.getNode().getId() == nodeBId) {
                            assertEquals(portBA,link.getBridgePort());
                        } else {
                            assertTrue(false);
                        }
                    }
                    BridgeBridgeLink dlink = shared.getBridgeBridgeLinks().iterator().next();
                    assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                    assertEquals(portAB, dlink.getDesignatedPort());
                    assertEquals(nodeBId, dlink.getNode().getId());
                    assertEquals(portBA, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac6)) {
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB6,shared.getDesignatedPort());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                    assertEquals(mac6, link.getMacAddress());
                    assertEquals(nodeBId,link.getNode().getId());
                    assertEquals(portB6,link.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(mac8)) {
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(1, shared.getBridgeMacLinks().size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA8,shared.getDesignatedPort());
                    BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                    assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();

        public TwoBridgeWithBackbonePortsTopology() {

            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemlist.add(elementB);



            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA11,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA12,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macAB,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macB21,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macB22,bftA);


            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA11,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA12,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macAB,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB21,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB22,bftB);


        }
        
        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                assertEquals(false, shared.noMacsOnSegment());
                if (shared.getMacsOnSegment().contains(macAB)) {
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    assertEquals(1, shared.getMacsOnSegment().size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    List<BridgeMacLink> links = shared.getBridgeMacLinks();
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
                        assertEquals(macAB, link.getMacAddress());
                        if (link.getNode().getId() == nodeAId) 
                            assertEquals(portAB,link.getBridgePort());
                       else if (link.getNode().getId() == nodeBId) 
                           assertEquals(portBA,link.getBridgePort());
                       else 
                           assertTrue(false);
                    }
                    BridgeBridgeLink dlink = shared.getBridgeBridgeLinks().iterator().next();
                    assertEquals(nodeAId, dlink.getDesignatedNode().getId());
                    assertEquals(portAB, dlink.getDesignatedPort());
                    assertEquals(nodeBId, dlink.getNode().getId());
                    assertEquals(portBA, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(macA11)) {
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA1,shared.getDesignatedPort());
                    List<BridgeMacLink> links = shared.getBridgeMacLinks();
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB2,shared.getDesignatedPort());
                    List<BridgeMacLink> links = shared.getBridgeMacLinks();
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementB = new BridgeElement();
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();

        public TwoBridgeWithBackbonePortsTopologyWithBridgeinBft() {
            nodeA.setId(nodeAId);
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemlist.add(elementA);

            nodeB.setId(nodeBId);
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress(macB);
            elemlist.add(elementB);

            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA11,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portA1, macA12,bftA);
            bftA =addBridgeForwardingTableEntry(nodeA,portAB, macB,bftA);

            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA11,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macA12,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portBA, macAB,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB21,bftB);
            bftB =addBridgeForwardingTableEntry(nodeB,portB2, macB22,bftB);
            
        }
        
        public void check(BroadcastDomain domain) {
            printBridgeTopology(domain);
            List<SharedSegment> shsegs = domain.getTopology();
            assertEquals(3, shsegs.size());

            for (SharedSegment shared: shsegs) {
                if (shared.getMacsOnSegment().size() == 0) {
                    assertEquals(1, shared.getBridgeBridgeLinks().size());
                    assertEquals(0, shared.getMacsOnSegment().size());
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    List<BridgeMacLink> links = shared.getBridgeMacLinks();
                    assertEquals(0, links.size());
                    BridgeBridgeLink dlink = shared.getBridgeBridgeLinks().iterator().next();
                    assertEquals(nodeBId, dlink.getDesignatedNode().getId());
                    assertEquals(portBA, dlink.getDesignatedPort());
                    assertEquals(nodeAId, dlink.getNode().getId());
                    assertEquals(portAB, dlink.getBridgePort());
                } else if (shared.getMacsOnSegment().contains(macA11)) {
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA1,shared.getDesignatedPort());
                    List<BridgeMacLink> links = shared.getBridgeMacLinks();
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
                    assertEquals(0, shared.getBridgeBridgeLinks().size());
                    assertEquals(2, shared.getMacsOnSegment().size());
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portB2,shared.getDesignatedPort());
                    List<BridgeMacLink> links = shared.getBridgeMacLinks();
                    assertEquals(2, links.size());
                    for (BridgeMacLink link: links) {
                        assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
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
