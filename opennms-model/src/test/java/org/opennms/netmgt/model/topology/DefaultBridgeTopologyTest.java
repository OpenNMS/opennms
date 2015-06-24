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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.topology.BridgeTopology.BridgeTopologyLink;
import org.opennms.netmgt.model.topology.BridgeTopology.BridgeTopologyPort;
public class DefaultBridgeTopologyTest {

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.model.topology", "DEBUG");
        MockLogAppender.setupLogging(p);

    }

    protected List<BridgeTopologyLink> printBridgeTopologyLinks(List<BridgeTopologyLink> paths) {
        for (final BridgeTopologyLink path: paths) {
            printBridgeTopologyLink(path);
        }
        return paths;
    }

    protected void printBridgeTopologyLink(BridgeTopologyLink path) {
        System.err.println("");
        System.err.println("------link-----");
        System.err.println("macs on link: " + path.getMacs());
        System.err.println("------bridge port-----");
        printBridgeTopologyPort(path.getBridgeTopologyPort());
        if (path.getDesignateBridgePort() != null ) {
            System.err.println("------designated port-----");
            printBridgeTopologyPort(path.getDesignateBridgePort());
        }
    }

    protected void printBridgeTopologyPort(BridgeTopologyPort port) {
        System.err.println("nodeid: " + port.getNodeid());
        System.err.println("bridgeport: " + port.getBridgePort());
        System.err.println("learned macs: " + port.getMacs());
    }

    private Map<Integer,Set<String>> addBridgeForwardingTableEntry(Integer bridgeport, String mac, Map<Integer,Set<String>> bft) {
        Set<String> macs = new HashSet<String>();
        if (bft.containsKey(bridgeport)) {
            macs = bft.get(bridgeport);
        }
        macs.add(mac);

        bft.put(bridgeport, macs);
        return bft;
    }

    @Test
    public void testOneBridgeOnePortOneMac() throws Exception {

        Integer nodeA  = 10;

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();

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

        bftA = addBridgeForwardingTableEntry(portA1, mac1, bftA);
        bftA = addBridgeForwardingTableEntry(portA2, mac2, bftA);
        bftA = addBridgeForwardingTableEntry(portA3, mac3, bftA);
        bftA = addBridgeForwardingTableEntry(portA4, mac4, bftA);
        bftA = addBridgeForwardingTableEntry(portA5, mac5, bftA);

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(nodeA,bftA);

        List<BridgeTopologyLink> links = bridgeTopology.getTopology();
        printBridgeTopologyLinks(links);
        assertEquals(5, links.size());
        for (BridgeTopologyLink link: links) {
            assertEquals(null,link.getDesignateBridgePort());
            BridgeTopologyPort port = link.getBridgeTopologyPort();
            assertEquals(nodeA, port.getNodeid());
            assertEquals(1, link.getMacs().size());
            assertEquals(1, port.getMacs().size());
            assertEquals(port.getMacs().iterator().next(),link.getMacs().iterator().next());
            if (link.getBridgeTopologyPort().getBridgePort() == portA1) {
                assertEquals(mac1, link.getMacs().iterator().next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA2) {
                assertEquals(mac2, link.getMacs().iterator().next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA3) {
                assertEquals(mac3, link.getMacs().iterator().next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA4) {
                assertEquals(mac4, link.getMacs().iterator().next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA5) {
                assertEquals(mac5, link.getMacs().iterator().next());
            } else {
                assertEquals(-1, 1);
            }
        }
    }

    @Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        Integer nodeA  = 20;

        Integer portA1 = 1;

        String mac1 = "000daaaa0001"; // port A1 
        String mac2 = "000daaaa0002"; // port A1
        String mac3 = "000daaaa0003"; // port A1
        String mac4 = "000daaaa0004"; // port A1

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();

        bftA = addBridgeForwardingTableEntry(portA1, mac1,bftA);
        bftA =addBridgeForwardingTableEntry(portA1, mac2,bftA);
        bftA =addBridgeForwardingTableEntry(portA1, mac3,bftA);
        bftA =addBridgeForwardingTableEntry(portA1, mac4,bftA);

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(nodeA,bftA);

        List<BridgeTopologyLink> links = bridgeTopology.getTopology();
        printBridgeTopologyLinks(links);
        assertEquals(1, links.size());
        for (BridgeTopologyLink link: links) {
            BridgeTopologyPort port = link.getBridgeTopologyPort();
            assertEquals(port.getMacs(),link.getMacs());
            assertEquals(null,link.getDesignateBridgePort());
            if (link.getBridgeTopologyPort().getBridgePort() == portA1) {
                final Set<String> macs = link.getMacs();
                assertEquals(4, macs.size());
                assertTrue(macs.contains(mac1));
                assertTrue(macs.contains(mac2));
                assertTrue(macs.contains(mac3));
                assertTrue(macs.contains(mac4));
            } else {
                assertEquals(-1, 1);
            }
        }

    }

    @Test
    public void testOneBridgeComplete() throws Exception {

        Integer nodeA = 30;

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

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();

        bftA =addBridgeForwardingTableEntry(portA1, mac1,bftA);
        bftA =addBridgeForwardingTableEntry(portA2, mac2,bftA);
        bftA =addBridgeForwardingTableEntry(portA3, mac3,bftA);
        bftA =addBridgeForwardingTableEntry(portA4, mac4,bftA);

        bftA =addBridgeForwardingTableEntry(portA23, mac231,bftA);
        bftA =addBridgeForwardingTableEntry(portA23, mac232,bftA);
        bftA =addBridgeForwardingTableEntry(portA23, mac233,bftA);
        bftA =addBridgeForwardingTableEntry(portA23, mac234,bftA);

        bftA =addBridgeForwardingTableEntry(portA24, mac241,bftA);
        bftA =addBridgeForwardingTableEntry(portA24, mac242,bftA);
        bftA =addBridgeForwardingTableEntry(portA24, mac243,bftA);
        bftA =addBridgeForwardingTableEntry(portA24, mac244,bftA);
        bftA =addBridgeForwardingTableEntry(portA24, mac245,bftA);

        bftA =addBridgeForwardingTableEntry(portA25, mac251,bftA);
        bftA =addBridgeForwardingTableEntry(portA25, mac252,bftA);
        bftA =addBridgeForwardingTableEntry(portA25, mac253,bftA);


        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(nodeA,bftA);

        List<BridgeTopologyLink> links = bridgeTopology.getTopology();
        printBridgeTopologyLinks(links);
        assertEquals(7, links.size());
        for (BridgeTopologyLink link: links) {
            BridgeTopologyPort port = link.getBridgeTopologyPort();
            assertEquals(port.getMacs(),link.getMacs());
            assertEquals(null,link.getDesignateBridgePort());
            if (link.getBridgeTopologyPort().getBridgePort() == portA1) {
                assertEquals(1, link.getMacs().size());
                Iterator<String> macs = link.getMacs().iterator();
                assertEquals(mac1, macs.next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA2) {
                assertEquals(1, link.getMacs().size());
                Iterator<String> macs = link.getMacs().iterator();
                assertEquals(mac2, macs.next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA3) {
                assertEquals(1, link.getMacs().size());
                Iterator<String> macs = link.getMacs().iterator();
                assertEquals(mac3, macs.next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA4) {
                assertEquals(1, link.getMacs().size());
                Iterator<String> macs = link.getMacs().iterator();
                assertEquals(mac4, macs.next());
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA23) {
                final Set<String> macs = link.getMacs();
                assertEquals(4, macs.size());
                assertTrue(macs.contains(mac231));
                assertTrue(macs.contains(mac232));
                assertTrue(macs.contains(mac233));
                assertTrue(macs.contains(mac234));
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA24) {
                final Set<String> macs = link.getMacs();
                assertEquals(5, macs.size());
                assertTrue(macs.contains(mac241));
                assertTrue(macs.contains(mac242));
                assertTrue(macs.contains(mac243));
                assertTrue(macs.contains(mac244));
                assertTrue(macs.contains(mac245));
            } else if (link.getBridgeTopologyPort().getBridgePort() == portA25) {
                final Set<String> macs = link.getMacs();
                assertEquals(3, macs.size());
                assertTrue(macs.contains(mac251));
                assertTrue(macs.contains(mac252));
                assertTrue(macs.contains(mac253));
            } else {
                assertEquals(-1, 1);
            }
        }

    }

    @Test
    public void testTwoConnectedBridgeTopology() throws Exception {

        Integer nodeA  = 1111;
        Integer nodeB  = 2222;

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

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();

        bftA =addBridgeForwardingTableEntry(portA1, mac1,bftA);
        bftA =addBridgeForwardingTableEntry(portA2, mac2,bftA);
        bftA =addBridgeForwardingTableEntry(portA3, mac3,bftA);
        bftA =addBridgeForwardingTableEntry(portA4, mac4,bftA);
        bftA =addBridgeForwardingTableEntry(portA5, mac5,bftA);

        bftA =addBridgeForwardingTableEntry(portAB, mac6,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac7,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac8,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac9,bftA);

        Map<Integer,Set<String>> bftB = new HashMap<Integer, Set<String>>();

        bftB =addBridgeForwardingTableEntry(portBA, mac1,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac2,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac3,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac4,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac5,bftB);

        bftB =addBridgeForwardingTableEntry(portB6, mac6,bftB);
        bftB =addBridgeForwardingTableEntry(portB7, mac7,bftB);
        bftB =addBridgeForwardingTableEntry(portB8, mac8,bftB);
        bftB =addBridgeForwardingTableEntry(portB9, mac9,bftB);

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(nodeA,bftA);
        bridgeTopology.parseBFT(nodeB,bftB);

        List<BridgeTopologyLink> links = bridgeTopology.getTopology();
        printBridgeTopologyLinks(links);
        assertEquals(10, links.size());
        for (BridgeTopologyLink link: links) {
            if (link.getMacs().isEmpty()) {
                assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portAB,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(nodeB, link.getDesignateBridgePort().getNodeid());
                assertEquals(portBA,link.getDesignateBridgePort().getBridgePort());
            } else {
                assertEquals(1, link.getMacs().size());
                String mac = link.getMacs().iterator().next();
                if (mac.equals(mac1)) {
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA1,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac2)) {
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA2,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac3)) {
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA3,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac4)) {
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA4,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac5)) {
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA5,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac6)) {
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB6,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac7)) {
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB7,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac8)) {
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB8,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (mac.equals(mac9)) {
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB9,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }

    }

    @Test
    public void testTwoMergeBridgeTopology() throws Exception {

        Integer nodeA  = 1111;
        Integer nodeB  = 2222;

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

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();

        bftA =addBridgeForwardingTableEntry(portAB, mac1,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac2,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac3,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac4,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac5,bftA);

        bftA =addBridgeForwardingTableEntry(portAB, mac6,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac7,bftA);
        bftA =addBridgeForwardingTableEntry(portA8, mac8,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, mac9,bftA);

        Map<Integer,Set<String>> bftB = new HashMap<Integer, Set<String>>();

        bftB =addBridgeForwardingTableEntry(portBA, mac1,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac2,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac3,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac4,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac5,bftB);

        bftB =addBridgeForwardingTableEntry(portB6, mac6,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac7,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac8,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, mac9,bftB);

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(nodeA,bftA);
        bridgeTopology.parseBFT(nodeB,bftB);

        List<BridgeTopologyLink> links = bridgeTopology.getTopology();
        printBridgeTopologyLinks(links);
        assertEquals(3, links.size());

        for (BridgeTopologyLink link: links) {
            assertEquals(false, link.getMacs().isEmpty());
            if (link.getMacs().contains(mac1)) {
                assertEquals(7, link.getMacs().size());
                assertEquals(true, link.getMacs().contains(mac2));
                assertEquals(true, link.getMacs().contains(mac3));
                assertEquals(true, link.getMacs().contains(mac4));
                assertEquals(true, link.getMacs().contains(mac5));
                assertEquals(false, link.getMacs().contains(mac6));
                assertEquals(true, link.getMacs().contains(mac7));
                assertEquals(false, link.getMacs().contains(mac8));
                assertEquals(true, link.getMacs().contains(mac9));
                assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portAB,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(nodeB, link.getDesignateBridgePort().getNodeid());
                assertEquals(portBA,link.getDesignateBridgePort().getBridgePort());
            } else if (link.getMacs().contains(mac6)) {
                assertEquals(1, link.getMacs().size());
                assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portB6,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(null, link.getDesignateBridgePort());
            } else if (link.getMacs().contains(mac8)) {
                assertEquals(1, link.getMacs().size());
                assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portA8,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(null, link.getDesignateBridgePort());
            } else {
                assertEquals(false, true);
            }
        }


    }

    @Test 
    public void testTwoBridgeWithBackbonePorts() {
        Integer nodeA = 1101;
        Integer nodeB = 1102;

        Integer portA1 = 1;
        Integer portAB = 12;
        Integer portBA = 21;
        Integer portB2 = 2 ;

        String macA11 = "000daa000a11"; // port A1 ---port BA 
        String macA12 = "000daa000a12"; // port A1 ---port BA 

        String macAB  = "000daa0000ab"; // port AB ---port BA 

        String macB21 = "000daa000b21"; // port AB ---port B2 
        String macB22 = "000daa000b22"; // port AB ---port B2

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();

        bftA =addBridgeForwardingTableEntry(portA1, macA11,bftA);
        bftA =addBridgeForwardingTableEntry(portA1, macA12,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, macAB,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, macB21,bftA);
        bftA =addBridgeForwardingTableEntry(portAB, macB22,bftA);

        Map<Integer,Set<String>> bftB = new HashMap<Integer, Set<String>>();

        bftB =addBridgeForwardingTableEntry(portBA, macA11,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, macA12,bftB);
        bftB =addBridgeForwardingTableEntry(portBA, macAB,bftB);
        bftB =addBridgeForwardingTableEntry(portB2, macB21,bftB);
        bftB =addBridgeForwardingTableEntry(portB2, macB22,bftB);

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(nodeA,bftA);
        bridgeTopology.parseBFT(nodeB,bftB);

        List<BridgeTopologyLink> links = bridgeTopology.getTopology();
        printBridgeTopologyLinks(links);
        assertEquals(3, links.size());

        for (BridgeTopologyLink link: links) {
            assertEquals(false, link.getMacs().isEmpty());
            if (link.getMacs().contains(macAB)) {
                assertEquals(1, link.getMacs().size());
                assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portAB,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(nodeB, link.getDesignateBridgePort().getNodeid());
                assertEquals(portBA,link.getDesignateBridgePort().getBridgePort());
            } else if (link.getMacs().contains(macA12)) {
                assertEquals(2, link.getMacs().size());
                assertEquals(true, link.getMacs().contains(macA12));
                assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portA1,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(null, link.getDesignateBridgePort());
            } else if (link.getMacs().contains(macB21)) {
                assertEquals(2, link.getMacs().size());
                assertEquals(true, link.getMacs().contains(macB22));
                assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                assertEquals(portB2,link.getBridgeTopologyPort().getBridgePort());
                assertEquals(null, link.getDesignateBridgePort());
            } else {
                assertEquals(false, true);
            }
        }

    }

    @Test
    public void testTwoConnectedBridgeTopologyAB() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);
        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);

        topology.checkAB(bridgeTopology.getTopology());

    }

    @Test
    public void testTwoConnectedBridgeTopologyBA() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);

        topology.checkAB(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyAC() {

        ABCTopology topology = new ABCTopology();
        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);
        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);

        topology.checkAC(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyCA() {

        ABCTopology topology = new ABCTopology();
        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);

        topology.checkAC(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyBC() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);
        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);

        topology.checkBC(bridgeTopology.getTopology());
    }

    @Test
    public void testTwoConnectedBridgeTopologyCB() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);
        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);

        topology.checkBC(bridgeTopology.getTopology());
    }

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
    public void testThreeConnectedBridgeTopologyABC() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);
        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);
        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);

        topology.check(bridgeTopology.getTopology());

    }

    @Test
    public void testThreeConnectedBridgeTopologyACB() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);
        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);
        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);

        topology.check(bridgeTopology.getTopology());

    }

    @Test
    public void testThreeConnectedBridgeTopologyBAC() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);
        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);

        topology.check(bridgeTopology.getTopology());

    }

    @Test
    public void testThreeConnectedBridgeTopologyBCA() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);
        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);

        topology.check(bridgeTopology.getTopology());
    }

    @Test
    public void testThreeConnectedBridgeTopologyCAB() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);
        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);

        topology.check(bridgeTopology.getTopology());
    }

    @Test
    public void testThreeConnectedBridgeTopologyCBA() {
        ABCTopology topology = new ABCTopology();

        BridgeTopology bridgeTopology = new BridgeTopology();

        bridgeTopology.parseBFT(topology.nodeC,topology.bftC);
        bridgeTopology.parseBFT(topology.nodeB,topology.bftB);
        bridgeTopology.parseBFT(topology.nodeA,topology.bftA);

        topology.check(bridgeTopology.getTopology());

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

    private class ABCTopology {
        Integer nodeA = 101;
        Integer nodeB = 102;
        Integer nodeC = 103;

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

        Map<Integer,Set<String>> bftA = new HashMap<Integer, Set<String>>();
        Map<Integer,Set<String>> bftB = new HashMap<Integer, Set<String>>();
        Map<Integer,Set<String>> bftC = new HashMap<Integer, Set<String>>();

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

            bftA =addBridgeForwardingTableEntry(portA, mac1,bftA);
            bftA =addBridgeForwardingTableEntry(portAB, mac2,bftA);
            bftA =addBridgeForwardingTableEntry(portAB, mac3,bftA);

            bftB =addBridgeForwardingTableEntry(portBA, mac1,bftB);
            bftB =addBridgeForwardingTableEntry(portB, mac2,bftB);
            bftB =addBridgeForwardingTableEntry(portBC, mac3,bftB);

            bftC =addBridgeForwardingTableEntry(portCB, mac1,bftC);
            bftC =addBridgeForwardingTableEntry(portCB, mac2,bftC);
            bftC =addBridgeForwardingTableEntry(portC, mac3,bftC);
        }

        public void checkAC(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(3, links.size());

            for (BridgeTopologyLink link: links) {
                if (link.getMacs().contains(mac1)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac2)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portAB,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeC, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portCB,link.getDesignateBridgePort().getBridgePort());
                } else if (link.getMacs().contains(mac3)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeC, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portC,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }
        public void checkAB(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(4, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().isEmpty()) {
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portAB,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeB, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portBA,link.getDesignateBridgePort().getBridgePort());
                } else if (link.getMacs().contains(mac1)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac2)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac3)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portBC,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void checkBC(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(4, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().isEmpty()) {
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portBC,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeC, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portCB,link.getDesignateBridgePort().getBridgePort());
                } else if (link.getMacs().contains(mac1)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portBA,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac2)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac3)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeC, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portC,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }

        }

        public void check(List<BridgeTopologyLink> links) {
            printBridgeTopologyLinks(links);
            assertEquals(5, links.size());
            for (BridgeTopologyLink link: links) {
                if (link.getMacs().isEmpty() && link.getBridgeTopologyPort().getNodeid() == nodeB) {
                    assertEquals(portBC,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeC, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portCB,link.getDesignateBridgePort().getBridgePort());
                } else if (link.getMacs().isEmpty() && link.getBridgeTopologyPort().getNodeid() == nodeA) {
                    assertEquals(portAB,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(nodeB, link.getDesignateBridgePort().getNodeid());
                    assertEquals(portBA,link.getDesignateBridgePort().getBridgePort());
                } else if (link.getMacs().contains(mac1)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeA, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portA,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac2)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeB, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portB,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else if (link.getMacs().contains(mac3)) {
                    assertEquals(1, link.getMacs().size());
                    assertEquals(nodeC, link.getBridgeTopologyPort().getNodeid());
                    assertEquals(portC,link.getBridgeTopologyPort().getBridgePort());
                    assertEquals(null, link.getDesignateBridgePort());
                } else {
                    assertEquals(false, true);
                }
            }
        }
    }


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


}
