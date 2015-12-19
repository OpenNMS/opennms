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

package org.opennms.netmgt.model.topology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.OnmsNode;
public class DefaultBridgeTopologyTest {

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.model.topology", "DEBUG");
        MockLogAppender.setupLogging(p);

    }

    private void printBridgeTopology(List<SharedSegment> shareds) {
        for (SharedSegment shared: shareds)
            printSharedSegment(shared);
    }
    
    private void printSharedSegment(SharedSegment shared) {
        System.err.println("");
        System.err.println("------shared Segment-----");
        System.err.println("designated bridge: " + shared.getDesignatedBridge());
        System.err.println("designated port: " + shared.getDesignatedPort());
        System.err.println("macs on segment: " + shared.getMacsOnSegment());
        System.err.println("bridge ids on segment: " + shared.getBridgeIdsOnSegment());
        if (shared.noMacsOnSegment()) {
            for (BridgeBridgeLink blink:  shared.getBridgeBridgeLinks())
                printBridgeBridgeLink(blink);
        } else {
        for (BridgeMacLink mlink: shared.getBridgeMacLinks()) 
            printBridgeMacLink(mlink);
        }
        System.err.println("------shared Segment-----");
    }

    private void printBridgeMacLink(BridgeMacLink mlink) {
        System.err.println("------BridgeMacLink-----");
        System.err.println("nodeid: " + mlink.getNode().getId());
        System.err.println("bridgeport: " + mlink.getBridgePort());
        System.err.println("mac: " + mlink.getMacAddress());
        System.err.println("status: " + BridgeDot1qTpFdbStatus.getTypeString(mlink.getBridgeDot1qTpFdbStatus().getValue()));
        System.err.println("------BridgeMacLink-----");
        
    }
    private void printBridgeBridgeLink(BridgeBridgeLink blink) {
        System.err.println("------BridgeBridgeLink-----");
        System.err.println("nodeid: " + blink.getNode().getId());
        System.err.println("bridgeport: " + blink.getBridgePort());
        System.err.println("designatednodeid: " + blink.getDesignatedNode().getId());
        System.err.println("designatedbridgeport: " + blink.getDesignatedPort());
        System.err.println("------BridgeBridgeLink-----");        
    }
    
    private List<BridgeMacLink> addBridgeForwardingTableEntry(OnmsNode node, Integer bridgeport, String mac, List<BridgeMacLink> bft) {
        BridgeMacLink link = new BridgeMacLink();
        link.setNode(node);
        link.setBridgePort(bridgeport);
        link.setMacAddress(mac);
        link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        bft.add(link);
        return bft;
    }

    @Test
    public void testOneBridgeOnePortOneMac() throws Exception {

        Integer nodeAId  = 10;
        OnmsNode nodeA= new OnmsNode();
        nodeA.setId(nodeAId);
        BridgeElement element = new BridgeElement();
        element.setNode(nodeA);
        element.setBaseBridgeAddress("aaaaaaaaaaaa");
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        elemlist.add(element);
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

        Integer portA1 = 1;
        Integer portA2 = 2;
        Integer portA3 = 3;
        Integer portA4 = 4;
        Integer portA5 = 5;

        String mac1 = "000daaaa0001"; // learned on port A1
        String mac2 = "000daaaa0002"; // learned on port A2 
        String mac3 = "000daaaa0003"; // learned on port A2 
        String mac4 = "000daaaa0004"; // learned on port A2 
        String mac5 = "000daaaa0005"; // learned on port A2 

        bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac1, bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA2, mac2, bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA3, mac3, bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA4, mac4, bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA5, mac5, bftA);

        BroadcastDomain bridgeTopology = new BroadcastDomain();

        bridgeTopology.loadBFT(nodeAId,bftA,null,elemlist);
        
        assertTrue(bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

        bridgeTopology.calculate();
        List<SharedSegment> links = bridgeTopology.getTopology();
        printBridgeTopology(links);
        assertEquals(5, links.size());
        for (SharedSegment shared: links) {
            assertTrue(!shared.noMacsOnSegment());
            assertEquals(nodeAId,shared.getDesignatedBridge());
            assertEquals(1, shared.getBridgeIdsOnSegment().size());
            assertEquals(1, shared.getMacsOnSegment().size());
            for (BridgeMacLink link: shared.getBridgeMacLinks()) {
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
        assertTrue(!bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

    }
    @Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        Integer nodeAId  = 20;
        OnmsNode nodeA= new OnmsNode();
        nodeA.setId(nodeAId);
        BridgeElement element = new BridgeElement();
        element.setNode(nodeA);
        element.setBaseBridgeAddress("aaaaaaaaaaaa");
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        elemlist.add(element);
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

        Integer portA1 = 1;

        String mac1 = "000daaaa0001"; // port A1 
        String mac2 = "000daaaa0002"; // port A1
        String mac3 = "000daaaa0003"; // port A1
        String mac4 = "000daaaa0004"; // port A1


        bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac1,bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac2,bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac3,bftA);
        bftA = addBridgeForwardingTableEntry(nodeA,portA1, mac4,bftA);

        BroadcastDomain bridgeTopology = new BroadcastDomain();

        bridgeTopology.loadBFT(nodeAId,bftA,null,elemlist);
        assertTrue(bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

        bridgeTopology.calculate();
        List<SharedSegment> links = bridgeTopology.getTopology();
        printBridgeTopology(links);

        assertEquals(1, links.size());
        for (SharedSegment shared: links) {
            assertTrue(!shared.noMacsOnSegment());
            assertEquals(nodeAId,shared.getDesignatedBridge());
            assertEquals(portA1,shared.getDesignatedPort());
            assertEquals(1, shared.getBridgeIdsOnSegment().size());
            assertEquals(4, shared.getMacsOnSegment().size());
            final Set<String> macs = shared.getMacsOnSegment();
            assertTrue(macs.contains(mac1));
            assertTrue(macs.contains(mac2));
            assertTrue(macs.contains(mac3));
            assertTrue(macs.contains(mac4));
            for (BridgeMacLink link: shared.getBridgeMacLinks())
                assertEquals(portA1, link.getBridgePort());
        }

    }

    @Test
    public void testOneBridgeComplete() throws Exception {

        Integer nodeAId = 30;
        OnmsNode nodeA= new OnmsNode();
        nodeA.setId(nodeAId);
        BridgeElement element = new BridgeElement();
        element.setNode(nodeA);
        element.setBaseBridgeAddress("aaaaaaaaaaaa");
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();
        elemlist.add(element);
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

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


        BroadcastDomain bridgeTopology = new BroadcastDomain();

        bridgeTopology.loadBFT(nodeAId,bftA,null,elemlist);
        assertTrue(bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

        bridgeTopology.calculate();
        List<SharedSegment> links = bridgeTopology.getTopology();
        printBridgeTopology(links);
        assertEquals(7, links.size());
        for (SharedSegment shared: links) {
            assertTrue(!shared.noMacsOnSegment());
            assertEquals(nodeAId,shared.getDesignatedBridge());
            assertEquals(1, shared.getBridgeIdsOnSegment().size());
            for (BridgeMacLink link: shared.getBridgeMacLinks()) {
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

    @Test
    public void testTwoConnectedBridgeTopology() throws Exception {

        Integer nodeAId  = 1111;
        Integer nodeBId = 2222;
        OnmsNode nodeA= new OnmsNode();
        nodeA.setId(nodeAId);
        BridgeElement elementA = new BridgeElement();
        elementA.setNode(nodeA);
        elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
        List<BridgeElement> elemAlist = new ArrayList<BridgeElement>();
        elemAlist.add(elementA);
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

        OnmsNode nodeB= new OnmsNode();
        nodeB.setId(nodeBId);
        BridgeElement elementB = new BridgeElement();
        elementB.setNode(nodeB);
        elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
        List<BridgeElement> elemBlist = new ArrayList<BridgeElement>();
        elemBlist.add(elementB);
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();

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

        BroadcastDomain bridgeTopology = new BroadcastDomain();

        bridgeTopology.loadBFT(nodeAId,bftA,null,elemAlist);
        bridgeTopology.loadBFT(nodeBId,bftB,null,elemBlist);
        assertTrue(bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

        bridgeTopology.calculate();
        List<SharedSegment> shsegs = bridgeTopology.getTopology();
        printBridgeTopology(shsegs);

        assertEquals(10, shsegs.size());
        for (SharedSegment shared: shsegs) {
            if (shared.noMacsOnSegment()) {
                assertEquals(0, shared.getBridgeMacLinks().size());
                assertEquals(1, shared.getBridgeBridgeLinks().size());
                BridgeBridgeLink link=shared.getBridgeBridgeLinks().iterator().next();
                assertEquals(nodeAId, link.getNode().getId());
                assertEquals(portAB,link.getBridgePort());
                assertEquals(nodeBId, link.getDesignatedNode().getId());
                assertEquals(portBA,link.getDesignatedPort());
            } else {
                assertEquals(1, shared.getMacsOnSegment().size());
                BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
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

    @Test
    public void testTwoMergeBridgeTopology() throws Exception {

        Integer nodeAId  = 1111;
        Integer nodeBId  = 2222;

        OnmsNode nodeA= new OnmsNode();
        nodeA.setId(nodeAId);
        BridgeElement elementA = new BridgeElement();
        elementA.setNode(nodeA);
        elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
        List<BridgeElement> elemAlist = new ArrayList<BridgeElement>();
        elemAlist.add(elementA);
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

        OnmsNode nodeB= new OnmsNode();
        nodeB.setId(nodeBId);
        BridgeElement elementB = new BridgeElement();
        elementB.setNode(nodeB);
        elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
        List<BridgeElement> elemBlist = new ArrayList<BridgeElement>();
        elemBlist.add(elementB);
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

        BroadcastDomain bridgeTopology = new BroadcastDomain();

        bridgeTopology.loadBFT(nodeAId,bftA,null,elemAlist);
        bridgeTopology.loadBFT(nodeBId,bftB,null,elemBlist);
        assertTrue(bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

        bridgeTopology.calculate();
        List<SharedSegment> shsegs = bridgeTopology.getTopology();
        printBridgeTopology(shsegs);
        assertEquals(3, shsegs.size());

        for (SharedSegment shared: shsegs) {
            assertEquals(false, shared.noMacsOnSegment());
            if (shared.getMacsOnSegment().contains(mac1)) {
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
                    if (link.getNode().getId() == nodeAId) {
                        assertEquals(portAB,link.getBridgePort());
                    } else if (link.getNode().getId() == nodeBId) {
                        assertEquals(portBA,link.getBridgePort());
                    } else {
                        assertTrue(false);
                    }
                }
            } else if (shared.getMacsOnSegment().contains(mac6)) {
                assertEquals(1, shared.getMacsOnSegment().size());
                assertEquals(nodeBId, shared.getDesignatedBridge());
                assertEquals(portB6,shared.getDesignatedPort());
                BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                assertEquals(mac6, link.getMacAddress());
                assertEquals(nodeBId,link.getNode().getId());
                assertEquals(portB6,link.getBridgePort());
            } else if (shared.getMacsOnSegment().contains(mac8)) {
                assertEquals(1, shared.getMacsOnSegment().size());
                assertEquals(nodeAId, shared.getDesignatedBridge());
                assertEquals(portA8,shared.getDesignatedPort());
                BridgeMacLink link = shared.getBridgeMacLinks().iterator().next();
                assertEquals(mac8, link.getMacAddress());
                assertEquals(nodeAId,link.getNode().getId());
                assertEquals(portA8,link.getBridgePort());
            } else {
                assertEquals(false, true);
            }
        }


    }

    @Test 
    public void testTwoBridgeWithBackbonePorts() {
        Integer nodeAId = 1101;
        Integer nodeBId = 1102;

        OnmsNode nodeA= new OnmsNode();
        nodeA.setId(nodeAId);
        BridgeElement elementA = new BridgeElement();
        elementA.setNode(nodeA);
        elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
        List<BridgeElement> elemAlist = new ArrayList<BridgeElement>();
        elemAlist.add(elementA);
        List<BridgeMacLink> bftA = new ArrayList<BridgeMacLink>();

        OnmsNode nodeB= new OnmsNode();
        nodeB.setId(nodeBId);
        BridgeElement elementB = new BridgeElement();
        elementB.setNode(nodeB);
        elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
        List<BridgeElement> elemBlist = new ArrayList<BridgeElement>();
        elemBlist.add(elementB);
        List<BridgeMacLink> bftB = new ArrayList<BridgeMacLink>();

        Integer portA1 = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;

        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; // port AB ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2


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

        BroadcastDomain bridgeTopology = new BroadcastDomain();

        bridgeTopology.loadBFT(nodeAId,bftA,null,elemAlist);
        bridgeTopology.loadBFT(nodeBId,bftB,null,elemBlist);
        assertTrue(bridgeTopology.isTopologyChanged());
        assertTrue(!bridgeTopology.isCalculating());

        bridgeTopology.calculate();
        List<SharedSegment> shsegs = bridgeTopology.getTopology();
        printBridgeTopology(shsegs);
        assertEquals(3, shsegs.size());

        for (SharedSegment shared: shsegs) {
            assertEquals(false, shared.noMacsOnSegment());
            if (shared.getMacsOnSegment().contains(macAB)) {
                assertEquals(1, shared.getMacsOnSegment().size());
                assertEquals(2, shared.getBridgeIdsOnSegment().size());
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                assertEquals(2, links.size());
                for (BridgeMacLink link: links) {
                    assertEquals(macAB, link.getMacAddress());
                    if (link.getNode().getId() == nodeAId) 
                        assertEquals(portAB,link.getBridgePort());
                   else if (link.getNode().getId() == nodeBId) 
                       assertEquals(portBA,link.getBridgePort());
                   else 
                       assertTrue(false);
                }
            } else if (shared.getMacsOnSegment().contains(macA11)) {
                assertEquals(2, shared.getMacsOnSegment().size());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(true, shared.getMacsOnSegment().contains(macA12));
                assertEquals(nodeAId, shared.getDesignatedBridge());
                assertEquals(portA1,shared.getDesignatedPort());
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
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
                assertEquals(2, shared.getMacsOnSegment().size());
                assertEquals(1, shared.getBridgeIdsOnSegment().size());
                assertEquals(true, shared.getMacsOnSegment().contains(macB22));
                assertEquals(nodeBId, shared.getDesignatedBridge());
                assertEquals(portB2,shared.getDesignatedPort());
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
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

    @Test
    public void testTwoConnectedBridgeTopologyAB() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);

        bridgeTopology.calculate();

        topology.checkAB(bridgeTopology.getTopology());

    }

    @Test
    public void testTwoConnectedBridgeTopologyBA() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);

        bridgeTopology.calculate();

        topology.checkAB(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyAC() {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);

        bridgeTopology.calculate();

        topology.checkAC(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyCA() {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);

        bridgeTopology.calculate();

        topology.checkAC(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyBC() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);

        bridgeTopology.calculate();

        topology.checkBC(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyCB() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);

        bridgeTopology.calculate();

        topology.checkBC(bridgeTopology.getTopology());
    }

    @Test
    @Ignore
    public void testThreeConnectedBridgeTopologyABC() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);

        bridgeTopology.calculate();

        topology.check(bridgeTopology.getTopology());

    }

    @Test
    @Ignore
    public void testThreeConnectedBridgeTopologyACB() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);

        bridgeTopology.calculate();

        topology.check(bridgeTopology.getTopology());

    }

    @Test
    @Ignore
    public void testThreeConnectedBridgeTopologyBAC() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);

        bridgeTopology.calculate();

        topology.check(bridgeTopology.getTopology());

    }

    @Test
    @Ignore
    public void testThreeConnectedBridgeTopologyBCA() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);

        bridgeTopology.calculate();

        topology.check(bridgeTopology.getTopology());
    }

    @Test
    @Ignore
    public void testThreeConnectedBridgeTopologyCAB() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);

        bridgeTopology.calculate();

        topology.check(bridgeTopology.getTopology());
    }

    @Test
    @Ignore
    public void testThreeConnectedBridgeTopologyCBA() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain bridgeTopology = new BroadcastDomain();
        bridgeTopology.loadBFT(topology.nodeCId,topology.bftC,null,topology.elemClist);
        bridgeTopology.loadBFT(topology.nodeBId,topology.bftB,null,topology.elemBlist);
        bridgeTopology.loadBFT(topology.nodeAId,topology.bftA,null,topology.elemAlist);

        bridgeTopology.calculate();

        topology.check(bridgeTopology.getTopology());

    }

    /*
    @Test
    public void testTwoConnectedBridgeTopologyDE() {
        DEFGTopology topology = new DEFGTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeD,topology.bftD);
        bridgeTopology.parseBFT(topology.nodeE,topology.bftE);

        topology.checkDE(bridgeTopology.getTopology());

    }

    @Test
    public void testTwoConnectedBridgeTopologyDF() {
        DEFGTopology topology = new DEFGTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeD,topology.bftD);
        bridgeTopology.parseBFT(topology.nodeF,topology.bftF);

        topology.checkDF(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyEF() {
        DEFGTopology topology = new DEFGTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeE,topology.bftE);
        bridgeTopology.parseBFT(topology.nodeF,topology.bftF);

        topology.checkEF(bridgeTopology.getTopology());
    }



    @Test
    public void testThreeConnectedBridgeTopologyDEF() {

        DEFGTopology topology = new DEFGTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeD,topology.bftD);
        bridgeTopology.parseBFT(topology.nodeE,topology.bftE);
        bridgeTopology.parseBFT(topology.nodeF,topology.bftF);

        topology.checkDEF(bridgeTopology.getTopology());

    }

    @Test
    public void testThreeConnectedBridgeTopologyDFE() {
        DEFGTopology topology = new DEFGTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeD,topology.bftD);
        bridgeTopology.parseBFT(topology.nodeF,topology.bftF);
        bridgeTopology.parseBFT(topology.nodeE,topology.bftE);

        topology.checkDEF(bridgeTopology.getTopology());
    }

    @Test 
    public void testFourConnectedBridgeTopologyDEFG() {
        DEFGTopology topology = new DEFGTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeD,topology.bftD);
        bridgeTopology.parseBFT(topology.nodeE,topology.bftE);
        bridgeTopology.parseBFT(topology.nodeF,topology.bftF);
        bridgeTopology.parseBFT(topology.nodeG,topology.bftG);

        topology.check(bridgeTopology.getTopology());

    }
*/
    private class ABCTopology {
        Integer nodeAId = 101;
        Integer nodeBId = 102;
        Integer nodeCId = 103;
        
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        OnmsNode nodeC= new OnmsNode();

        List<BridgeElement> elemAlist = new ArrayList<BridgeElement>();
        List<BridgeElement> elemBlist = new ArrayList<BridgeElement>();
        List<BridgeElement> elemClist = new ArrayList<BridgeElement>();

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

        String mac1 = "000daaaa0001"; // port A  ---port BA ---port CB
        String mac2 = "000daaaa0002"; // port AB ---port B  ---port CB
        String mac3 = "000daaaa0003"; // port AB ---port BC ---port C


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
            BridgeElement elementA = new BridgeElement();
            elementA.setNode(nodeA);
            elementA.setBaseBridgeAddress("aaaaaaaaaaaa");
            elemAlist.add(elementA);
    
            nodeB.setId(nodeBId);
            BridgeElement elementB = new BridgeElement();
            elementB.setNode(nodeB);
            elementB.setBaseBridgeAddress("bbbbbbbbbbbb");
            elemBlist.add(elementB);
    
            nodeC.setId(nodeCId);
            BridgeElement elementC = new BridgeElement();
            elementC.setNode(nodeC);
            elementC.setBaseBridgeAddress("cccccccccccc");
            elemClist.add(elementC);

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

        public void checkAC(List<SharedSegment> shsegms) {
            printBridgeTopology(shsegms);
            assertEquals(3, shsegms.size());

            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
                assertEquals(0, bblinks.size());
                if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeAId, link.getNode().getId());
                    assertEquals(portA,link.getBridgePort());
                    assertEquals(mac1, link.getMacAddress());
                } else if (shared.getMacsOnSegment().contains(mac2)) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(2, links.size());
                    assertTrue(!shared.noMacsOnSegment());
                    for (BridgeMacLink link: links) {
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
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void checkAB(List<SharedSegment> shsegms) {
            printBridgeTopology(shsegms);
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    assertEquals(nodeAId, bblink.getNode().getId());
                    assertEquals(nodeBId, bblink.getDesignatedNode().getId());
                    assertEquals(portAB, bblink.getBridgePort());
                    assertEquals(portBA, bblink.getDesignatedPort());
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeAId, shared.getDesignatedBridge());
                    assertEquals(portA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
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
                    assertEquals(nodeBId, link.getNode().getId());
                    assertEquals(portBC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkBC(List<SharedSegment> shsegms) {
            printBridgeTopology(shsegms);
            assertEquals(4, shsegms.size());
            for (SharedSegment shared: shsegms) {
                List<BridgeMacLink> links = shared.getBridgeMacLinks();
                List<BridgeBridgeLink> bblinks = shared.getBridgeBridgeLinks();
                if (shared.noMacsOnSegment()) {
                    assertEquals(2, shared.getBridgeIdsOnSegment().size());
                    assertEquals(0, links.size());
                    assertEquals(1, bblinks.size());
                    BridgeBridgeLink bblink = bblinks.iterator().next();
                    assertEquals(nodeBId, bblink.getNode().getId());
                    assertEquals(nodeCId, bblink.getDesignatedNode().getId());
                    assertEquals(portBC, bblink.getBridgePort());
                    assertEquals(portCB, bblink.getDesignatedPort());
               } else if (shared.getMacsOnSegment().contains(mac1)) {
                    assertEquals(1, shared.getBridgeIdsOnSegment().size());
                    assertEquals(1, links.size());
                    assertEquals(nodeBId, shared.getDesignatedBridge());
                    assertEquals(portBA,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
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
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void check(List<SharedSegment> shsegms) {
            printBridgeTopology(shsegms);
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
                    assertEquals(nodeCId, shared.getDesignatedBridge());
                    assertEquals(portC,shared.getDesignatedPort());
                    assertTrue(!shared.noMacsOnSegment());
                    BridgeMacLink link = links.iterator().next();
                    assertEquals(nodeCId, link.getNode().getId());
                    assertEquals(portC,link.getBridgePort());
                    assertEquals(mac3, link.getMacAddress());
                } else {
                    assertEquals(false, true);
                }
            }
        }
    }

/*
    private class DEFGTopology {
        Integer nodeD = 104;
        Integer nodeE = 105;
        Integer nodeF = 106;
        Integer nodeG = 107;

        Integer portD  = 4;
        Integer portDD = 40;
        Integer portE  = 5;
        Integer portEE = 50;
        Integer portF  = 6;
        Integer portFF = 60;

        Integer portG7 = 7;
        Integer portG8 = 8;
        Integer portGD = 74;
        Integer portGE = 75;
        Integer portGF = 76;

        String mac1 = "000daaaa0001"; // port D  ---port EE ---port FF ---portGD
        String mac2 = "000daaaa0002"; // port D  ---port EE ---port FF ---portGD
        String mac3 = "000daaaa0003"; // port DD ---port EE ---port F  ---portGF
        String mac4 = "000daaaa0004"; // port DD ---port EE ---port F  ---portGF
        String mac5 = "000daaaa0005"; // port DD ---port E ---port FF  ---portGE 
        String mac6 = "000daaaa0006"; // port DD ---port E ---port FF  ---portGE
        String mac7 = "000daaaa0007"; // port DD ---port EE ---port FF ---portG7
        String mac8 = "000daaaa0008"; // port DD ---port EE ---port FF ---portG8

        Map<Integer,Set<String>> bftD = new HashMap<Integer, Set<String>>();
        Map<Integer,Set<String>> bftE = new HashMap<Integer, Set<String>>();
        Map<Integer,Set<String>> bftF = new HashMap<Integer, Set<String>>();
        Map<Integer,Set<String>> bftG = new HashMap<Integer, Set<String>>();

        /*
         *              -----------------
         *mac1/mac2 --  ||portD|        |
         *              |   "Bridge D"  |     |-------------
         *              |       |portDD||-----||portGD|     |
         *              -----------------     |             |
         *                                    |             |
         *              -----------------     |    |port G7||---mac7
         *mac3/mac4 --  ||portF|        |     |             |
         *              |   "Bridge F"  |     |             |
         *              |       |portFF||-----||portGF|     |
         *              -----------------     |             |
         *                                    | "Bridge G"  |
         *              -----------------     |             |
         *mac5/mac6 --  ||portE|        |     |    |port G8||---mac8
         *              |   "Bridge E"  |     |             |
         *              |       |portEE||-----||portGE|     |
         *              -----------------     |-------------|
         */
    /*
        public DEFGTopology() {
            bftD =addBridgeForwardingTableEntry(portD,  mac1,bftD);
            bftD =addBridgeForwardingTableEntry(portD,  mac2,bftD);
            bftD =addBridgeForwardingTableEntry(portDD, mac3,bftD);
            bftD =addBridgeForwardingTableEntry(portDD, mac4,bftD);
            bftD =addBridgeForwardingTableEntry(portDD, mac5,bftD);
            bftD =addBridgeForwardingTableEntry(portDD, mac6,bftD);
            bftD =addBridgeForwardingTableEntry(portDD, mac7,bftD);
            bftD =addBridgeForwardingTableEntry(portDD, mac8,bftD);

            bftE =addBridgeForwardingTableEntry(portEE, mac1,bftE);
            bftE =addBridgeForwardingTableEntry(portEE, mac2,bftE);
            bftE =addBridgeForwardingTableEntry(portEE, mac3,bftE);
            bftE =addBridgeForwardingTableEntry(portEE, mac4,bftE);
            bftE =addBridgeForwardingTableEntry(portE,  mac5,bftE);
            bftE =addBridgeForwardingTableEntry(portE,  mac6,bftE);
            bftE =addBridgeForwardingTableEntry(portEE, mac7,bftE);
            bftE =addBridgeForwardingTableEntry(portEE, mac8,bftE);

            bftF =addBridgeForwardingTableEntry(portFF, mac1,bftF);
            bftF =addBridgeForwardingTableEntry(portFF, mac2,bftF);
            bftF =addBridgeForwardingTableEntry(portF,  mac3,bftF);
            bftF =addBridgeForwardingTableEntry(portF,  mac4,bftF);
            bftF =addBridgeForwardingTableEntry(portFF, mac5,bftF);
            bftF =addBridgeForwardingTableEntry(portFF, mac6,bftF);
            bftF =addBridgeForwardingTableEntry(portFF, mac7,bftF);
            bftF =addBridgeForwardingTableEntry(portFF, mac8,bftF);

            bftG =addBridgeForwardingTableEntry(portGD, mac1,bftG);
            bftG =addBridgeForwardingTableEntry(portGD, mac2,bftG);
            bftG =addBridgeForwardingTableEntry(portGF, mac3,bftG);
            bftG =addBridgeForwardingTableEntry(portGF, mac4,bftG);
            bftG =addBridgeForwardingTableEntry(portGE, mac5,bftG);
            bftG =addBridgeForwardingTableEntry(portGE, mac6,bftG);
            bftG =addBridgeForwardingTableEntry(portG7, mac7,bftG);
            bftG =addBridgeForwardingTableEntry(portG8, mac8,bftG);

        }

        public void checkDE(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(3, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().contains(mac1)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac2));
                    assertEquals(nodeD, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portD,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac5)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac6));
                    assertEquals(nodeE, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portE,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac7)) {
                    assertEquals(4, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac3));
                    assertEquals(true,link.getMacs().contains(mac4));
                    assertEquals(true,link.getMacs().contains(mac8));
                    assertEquals(nodeD, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portDD,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeE, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portEE,link.getDesignateBridgePort().getBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkDF(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(3, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().contains(mac1)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac2));
                    assertEquals(nodeD, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portD,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac3)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac4));
                    assertEquals(nodeF, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portF,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac7)) {
                    assertEquals(4, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac5));
                    assertEquals(true,link.getMacs().contains(mac6));
                    assertEquals(true,link.getMacs().contains(mac8));
                    assertEquals(nodeD, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portDD,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeF, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portFF,link.getDesignateBridgePort().getBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkEF(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(3, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().contains(mac3)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac4));
                    assertEquals(nodeF, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portF,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac5)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac6));
                    assertEquals(nodeE, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portE,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                }  else if (link.getMacs().contains(mac7)) {
                    assertEquals(4, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac1));
                    assertEquals(true,link.getMacs().contains(mac2));
                    assertEquals(true,link.getMacs().contains(mac8));
                    assertEquals(nodeE, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portEE,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeF, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portFF,link.getDesignateBridgePort().getBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void checkDEF(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(6, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().contains(mac1)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac2));
                    assertEquals(nodeD, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portD,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac3)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac4));
                    assertEquals(nodeF, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portF,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac5)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac6));
                    assertEquals(nodeE, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portE,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (nodeE == link.getBridgeTopologyPort().getNodeid() && link.getMacs().contains(mac7)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac8));
                    assertEquals(portEE,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (nodeF == link.getBridgeTopologyPort().getNodeid() && link.getMacs().contains(mac7)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac8));
                    assertEquals(portFF,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (nodeD == link.getBridgeTopologyPort().getNodeid() && link.getMacs().contains(mac7)) {
                    assertEquals(2, link.getMacs().size());
                    assertEquals(true,link.getMacs().contains(mac8));
                    assertEquals(portDD,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }

        public void check(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(8, links.size());
        }
    }

*/
}
