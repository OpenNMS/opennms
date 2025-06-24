/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.service.api.Bridge;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTable;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgePortWithMacs;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.DiscoveryBridgeTopology;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.enlinkd.snmp.LldpSnmpUtils;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Sets;

public class BroadcastDomainTest extends EnLinkdTestHelper {

    public static String printTopology(Set<BridgeForwardingTableEntry> bft) {
        StringBuilder strbfr = new StringBuilder();
        boolean rn = false;
        for (BridgeForwardingTableEntry bftentry: bft) {
            if (rn) {
                strbfr.append("\n");
            } else {
                rn = true;
            }
            strbfr.append(bftentry.printTopology());
        }
        return strbfr.toString();
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.enlinkd.service.api", "DEBUG");
        MockLogAppender.setupLogging(p);
    }

    @Test
    public void testIsNumber() {
        assertFalse(LldpSnmpUtils.isNumber("766d7831"));
    }

    @Test
    public void testHumanReadable() {
        assertTrue(LldpSnmpUtils.humanReadable("766d7831"));
    }

    @Test
    public void testOneBridgeOnePortOneMac() throws Exception {
        OneBridgeOnePortOneMacTopology topology = new OneBridgeOnePortOneMacTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();
        
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());

    }

    @Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        OneBridgeMoreMacOnePortTopology topology = new OneBridgeMoreMacOnePortTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
        
        DiscoveryBridgeTopology ndbts= new DiscoveryBridgeTopology(domain);
        
        ndbts.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbts.calculate();
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbts.getDomain());

    }

    @Test
    public void testOneBridgeComplete() throws Exception {

        OneBridgeCompleteTopology topology = new OneBridgeCompleteTopology();        
        
        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testTwoConnectedBridge() throws Exception {

        TwoConnectedBridgeTopology topology = new TwoConnectedBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain(),false);
        
        domain.hierarchySetUp(domain.getBridge(topology.nodeBId));
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain(),true);
        
    }

    @Test
    public void testPrintTopologyFromLevel() {
        TwoMergeBridgeTopology topology = new TwoMergeBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT(topology.nodeAId,topology.bftA);
        ndbt.addUpdatedBFT(topology.nodeBId,topology.bftB);
        ndbt.calculate();

        // this resulted in a NPE because of the unknown Id 3333, see NMS-9852
        domain.printTopologyFromLevel(Sets.newHashSet( topology.nodeAId, topology.nodeBId, 3333), 5);
    }

    @Test
    public void testTwoSwitchWithOnlyOnePortcondition3() {
        Integer portAB = 16;
        Integer portBA = 24;

        String mac1 = "000daaaa0001"; // port AB ---port BA
        String mac2 = "000daaaa0002"; // port AB ---port BA
        String maca = "000daaaa000a"; // port AB ---port BA
        String macb = "000daaaa000b"; // port AB ---port BA
        Integer nodeAId  = 1100011;
        Integer nodeBId  = 2200022;
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<>();
        List<BridgeElement> elemlist = new ArrayList<>();

        nodeA.setId(nodeAId);
        elementA.setNode(nodeA);
        elementA.setBaseBridgeAddress(maca);
        elemlist.add(elementA);

        nodeB.setId(nodeBId);
        elementB.setNode(nodeB);
        elementB.setBaseBridgeAddress(macb);
        elemlist.add(elementB);

        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac1));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, maca));

        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macb));


        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,nodeAId);
        DiscoveryBridgeTopology.create(domain,nodeBId);
        setBridgeElements(domain,elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((nodeAId),bftA);
        ndbt.addUpdatedBFT((nodeBId),bftB);
        ndbt.calculate();
        
        assertEquals(1,domain.getSharedSegments().size());
        SharedSegment shared = domain.getSharedSegments().iterator().next();
        assertEquals(2,shared.getBridgePortsOnSegment().size());
        assertEquals(2,shared.getMacsOnSegment().size());
        assertTrue(shared.containsMac(mac1));
        assertTrue(shared.containsMac(mac2));
        assertEquals(0, domain.getForwarding().size());
        
    }

    @Test
    public void testTwoSwitchWithOnlyOnePortCondition2() {
        Integer portAB = 16;
        Integer portBA = 24;

        String mac1 = "000daaaa0001"; // port AB ---port BA
        String mac2 = "000daaaa0002"; // port AB ---port BA
        String maca = "000daaaa000a"; // port AB ---port BA
        String macb = "000daaaa000b"; // port AB ---port BA
        Integer nodeAId  = 1100011;
        Integer nodeBId  = 2200022;
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<>();
        List<BridgeElement> elemlist = new ArrayList<>();

        nodeA.setId(nodeAId);
        elementA.setNode(nodeA);
        elementA.setBaseBridgeAddress(maca);
        elemlist.add(elementA);

        nodeB.setId(nodeBId);
        elementB.setNode(nodeB);
        elementB.setBaseBridgeAddress(macb);
        elemlist.add(elementB);

        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac1));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, maca));

        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, maca));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macb));


        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,nodeAId);
        DiscoveryBridgeTopology.create(domain,nodeBId);
        setBridgeElements(domain,elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((nodeAId),bftA);
        ndbt.addUpdatedBFT((nodeBId),bftB);
        ndbt.calculate();
        
        assertEquals(1,domain.getSharedSegments().size());
        SharedSegment shared = domain.getSharedSegments().iterator().next();
        assertEquals(2,shared.getBridgePortsOnSegment().size());
        assertEquals(2,shared.getMacsOnSegment().size());
        assertTrue(shared.containsMac(mac1));
        assertTrue(shared.containsMac(mac2));
        assertEquals(1, domain.getForwarding().size());
        assertEquals(1, domain.getForwarders(nodeBId).size());
        BridgePortWithMacs forwarder = domain.getForwarders(nodeBId).iterator().next();
        assertEquals(1, forwarder.getMacs().size());
        assertEquals(maca, forwarder.getMacs().iterator().next());
        assertEquals(portBA, forwarder.getPort().getBridgePort());
        assertEquals(nodeBId, forwarder.getPort().getNodeId());
    }

    @Test
    public void testTwoSwitchWithOnlyOnePortCondition1() {
        Integer portAB = 16;
        Integer portBA = 24;

        String mac1 = "000daaaa0001"; // port AB ---port BA
        String mac2 = "000daaaa0002"; // port AB ---port BA
        String maca = "000daaaa000a"; // port AB ---port BA
        String macb = "000daaaa000b"; // port AB ---port BA
        Integer nodeAId  = 1100011;
        Integer nodeBId  = 2200022;
        OnmsNode nodeA= new OnmsNode();
        OnmsNode nodeB= new OnmsNode();
        BridgeElement elementA = new BridgeElement();
        BridgeElement elementB = new BridgeElement();
        Set<BridgeForwardingTableEntry> bftA = new HashSet<>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<>();
        List<BridgeElement> elemlist = new ArrayList<>();

        nodeA.setId(nodeAId);
        elementA.setNode(nodeA);
        elementA.setBaseBridgeAddress(maca);
        elemlist.add(elementA);

        nodeB.setId(nodeBId);
        elementB.setNode(nodeB);
        elementB.setBaseBridgeAddress(macb);
        elemlist.add(elementB);

        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac1));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, mac2));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, maca));
        bftA.add(addBridgeForwardingTableEntry(nodeA,portAB, macb));

        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac1));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, mac2));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, macb));
        bftB.add(addBridgeForwardingTableEntry(nodeB,portBA, maca));


        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,nodeAId);
        DiscoveryBridgeTopology.create(domain,nodeBId);
        setBridgeElements(domain,elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((nodeAId),bftA);
        ndbt.addUpdatedBFT((nodeBId),bftB);
        ndbt.calculate();
        
        assertEquals(1,domain.getSharedSegments().size());
        SharedSegment shared = domain.getSharedSegments().iterator().next();
        assertEquals(2,shared.getBridgePortsOnSegment().size());
        assertEquals(2,shared.getMacsOnSegment().size());
        assertTrue(shared.containsMac(mac1));
        assertTrue(shared.containsMac(mac2));
        
        assertEquals(2, domain.getForwarding().size());
        assertEquals(1, domain.getForwarders(nodeAId).size());
        assertEquals(1, domain.getForwarders(nodeBId).size());
        BridgePortWithMacs forwarderA = domain.getForwarders(nodeAId).iterator().next();
        BridgePortWithMacs forwarderB = domain.getForwarders(nodeBId).iterator().next();
        assertEquals(1, forwarderA.getMacs().size());
        assertEquals(macb, forwarderA.getMacs().iterator().next());
        assertEquals(portAB, forwarderA.getPort().getBridgePort());
        assertEquals(nodeAId, forwarderA.getPort().getNodeId());
        assertEquals(1, forwarderB.getMacs().size());
        assertEquals(maca, forwarderB.getMacs().iterator().next());
        assertEquals(portBA, forwarderB.getPort().getBridgePort());
        assertEquals(nodeBId, forwarderB.getPort().getNodeId());
        

    }

    @Test
    public void testTwoMergeBridge() throws Exception {
        TwoMergeBridgeTopology topology = new TwoMergeBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test 
    public void testTwoBridgeWithBackbonePorts() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCleanTopology() {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        domain.clearTopologyForBridge(topology.nodeBId);
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        assertEquals(2, domain.getSharedSegments().size());
        assertEquals(5, domain.getMacsOnSegments().size());
        for (SharedSegment segment: domain.getSharedSegments()) {
        	assertEquals(0, segment.getBridgeBridgeLinks().size());
        	assertEquals(1, segment.getBridgeIdsOnSegment().size());
        	assertEquals(topology.nodeAId.intValue(), segment.getBridgeIdsOnSegment().iterator().next().intValue());
        	
        	if (segment.containsMac(topology.macA11) && segment.containsMac(topology.macA12)) {
            	assertEquals(2, segment.getMacsOnSegment().size());
            	assertEquals(2, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portA1.intValue(), link.getBridgePort().intValue());
        		}
        	} else if (segment.containsMac(topology.macB21) 
        			&& segment.containsMac(topology.macB22) 
        			&& segment.containsMac(topology.macAB)){
            	assertEquals(3, segment.getMacsOnSegment().size());
            	assertEquals(3, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portAB.intValue(), link.getBridgePort().intValue());
        		}
        		
        	} else {
                fail();
        	}
        }
        
    }
    
    @Test 
    public void testCleanTopologyRoot() {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        domain.clearTopologyForBridge(topology.nodeAId);
        assertEquals(5, domain.getMacsOnSegments().size());
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getNodeId().intValue());
        assertEquals(2, domain.getSharedSegments().size());
        for (SharedSegment segment: domain.getSharedSegments()) {
        	assertEquals(0, segment.getBridgeBridgeLinks().size());
        	assertEquals(1, segment.getBridgeIdsOnSegment().size());
        	assertEquals(topology.nodeBId.intValue(), segment.getBridgeIdsOnSegment().iterator().next().intValue());
        	
        	if (segment.containsMac(topology.macA11) && segment.containsMac(topology.macA12) 
        		&& segment.containsMac(topology.macAB)) {
            	assertEquals(3, segment.getMacsOnSegment().size());
            	assertEquals(3, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portBA.intValue(), link.getBridgePort().intValue());
        		}
        	} else if (segment.containsMac(topology.macB21) 
        			&& segment.containsMac(topology.macB22) 
        			){
            	assertEquals(2, segment.getMacsOnSegment().size());
            	assertEquals(2, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portB2.intValue(), link.getBridgePort().intValue());
        		}
        	} else {
                fail();
        	}
        	
        }
    }

    @Test 
    public void testTwoBridgeWithBackbonePortsUsingBridgeAddressInBft() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopologyWithBridgeinBft topology = new TwoBridgeWithBackbonePortsTopologyWithBridgeinBft();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test 
    public void testTwoBridgeOneCalculation() {

        TwoNodeTopology topology = new TwoNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeBId, ndbt.getDomain().getRootBridge().getNodeId());
        topology.check2nodeTopology(ndbt.getDomain(),true);
    }
    

    @Test 
    public void testTwoBridgeTwoCalculation() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        System.err.println(printTopology(DiscoveryBridgeTopology.calculateBFT(domain,domain.getRootBridge())));

        List<SharedSegment> shsegs = ndbt.getDomain().getSharedSegments();
        assertEquals(3, shsegs.size());
        
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain(),true);
        assertEquals(topology.nodeBId, domain.getRootBridge().getNodeId());

        System.err.println(printTopology(DiscoveryBridgeTopology.calculateBFT(domain,domain.getBridge(topology.nodeAId))));
    }

    @Test 
    public void testTwoBridgeTwoCalculationReverse() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();
        
        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();
        
        List<SharedSegment> shsegs = ndbt.getDomain().getSharedSegments();
        assertEquals(3, shsegs.size());
        
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain(),false);
        assertEquals(topology.nodeAId, domain.getRootBridge().getNodeId());
        
        System.err.println(printTopology(DiscoveryBridgeTopology.calculateBFT(domain, domain.getRootBridge())));
        System.err.println(printTopology(DiscoveryBridgeTopology.calculateBFT(domain,domain.getBridge(topology.nodeBId))));

    }

    @Test
    public void testAB() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());

    }

    @Test
    public void testBA() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());
    }

    @Test
    public void testAC() {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());
    }

    @Test
    public void testCA() {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());
    }

    @Test
    public void testBC() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain());
    }

    @Test
    public void testCB() {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain());
    }

    @Test
    public void testElectRootBridge() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        Bridge elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assertNull(elected);

        //B root
        domain.getBridge(topology.nodeAId).setDesignated(topology.macB);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macB);
        elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assert elected != null;
        assertEquals(topology.nodeBId.intValue(), elected.getNodeId().intValue());

        //A Root
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(topology.macA);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macB);
        elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assert elected != null;
        assertEquals(topology.nodeAId.intValue(), elected.getNodeId().intValue());

        //C Root
        domain.getBridge(topology.nodeAId).setDesignated(topology.macB);
        domain.getBridge(topology.nodeBId).setDesignated(topology.macC);
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assert elected != null;
        assertEquals(topology.nodeCId.intValue(), elected.getNodeId().intValue());

        //C root
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(topology.macC);
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assert elected != null;
        assertEquals(topology.nodeCId.intValue(), elected.getNodeId().intValue());
        
       //D? root
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated("dddddddddddd");
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assert elected != null;
        assertEquals(topology.nodeBId.intValue(), elected.getNodeId().intValue());

        //A root B is bypassed
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(null);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macA);
        elected = DiscoveryBridgeTopology.electRootBridge(domain);
        assert elected != null;
        assertEquals(topology.nodeAId.intValue(), elected.getNodeId().intValue());

        //loop is bypassed
        domain.getBridge(topology.nodeAId).setDesignated(topology.macC);
        domain.getBridge(topology.nodeBId).setDesignated(null);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macA);
        try {
            DiscoveryBridgeTopology.electRootBridge(domain);
        } catch (BridgeTopologyException e) {
            assertEquals("getUpperBridge, too many iterations", e.getMessage());
        }

        //null root B is bypassed
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(null);
        domain.getBridge(topology.nodeCId).setDesignated(null);
        assertNull(DiscoveryBridgeTopology.electRootBridge(domain));

    }
    
    
    @Test
    public void testABC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testAThenBC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());
    }

    @Test
    public void testACThenB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());

        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testBAThenC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());

        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testBThenCA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();
        domain.hierarchySetUp(domain.getBridge(topology.nodeAId));
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCThenAB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        domain.hierarchySetUp(domain.getBridge(topology.nodeAId));

        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCBThenA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        topology.checkBC(ndbt.getDomain());

        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        domain.hierarchySetUp(domain.getBridge(topology.nodeAId));
        topology.check(ndbt.getDomain());

    }

    @Test
    public void testDE() {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDE(ndbt.getDomain());
    }

    @Test
    public void testDF() {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain());
    }

    @Test
    public void testEF() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkEF(ndbt.getDomain());
    }

    @Test
    public void testDG() {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeGId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDG(ndbt.getDomain());
    }

    @Test
    public void testDEF() {

        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain());

    }

    @Test
    public void testDFThenE() {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain());
        
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain());

    }

    @Test 
    public void testDEFG() {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        DiscoveryBridgeTopology.create(domain,topology.nodeGId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDEFG(ndbt.getDomain());

    }

    @Test 
    public void testDEFGThenI() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        DiscoveryBridgeTopology.create(domain,topology.nodeGId);
        DiscoveryBridgeTopology.create(domain,topology.nodeIId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDEFG(ndbt.getDomain());
        
        ndbt.addUpdatedBFT((topology.nodeIId),topology.bftI);
        ndbt.calculate();
        
        for (BridgeForwardingTableEntry bftentry: DiscoveryBridgeTopology.calculateBFT(domain, domain.getBridge(topology.nodeIId))) {
            assertEquals(topology.nodeIId.intValue(), bftentry.getNodeId().intValue());
            if (bftentry.getMacAddress().equals(topology.mac1)) {
                assertEquals(topology.portII.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac2)) {
                assertEquals(topology.portII.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac3)) {
                assertEquals(topology.portI3.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac4)) {
                assertEquals(topology.portI4.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac5)) {
                assertEquals(topology.portII.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac6)) {
                assertEquals(topology.portII.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac7)) {
                assertEquals(topology.portII.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac8)) {
                assertEquals(topology.portII.intValue(), bftentry.getBridgePort().intValue());
            } else {
                assertEquals(0, 1);
            }
        }

        for (BridgeForwardingTableEntry bftentry: DiscoveryBridgeTopology.calculateBFT(domain, domain.getBridge(topology.nodeGId))) {
            assertEquals(topology.nodeGId.intValue(), bftentry.getNodeId().intValue());
            if (bftentry.getMacAddress().equals(topology.mac1)) {
                assertEquals(topology.portGD.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac2)) {
                assertEquals(topology.portGD.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac3)) {
                assertEquals(topology.portGF.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac4)) {
                assertEquals(topology.portGF.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac5)) {
                assertEquals(topology.portGE.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac6)) {
                assertEquals(topology.portGE.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac7)) {
                assertEquals(topology.portG7.intValue(), bftentry.getBridgePort().intValue());
            } else if (bftentry.getMacAddress().equals(topology.mac8)) {
                assertEquals(topology.portG8.intValue(), bftentry.getBridgePort().intValue());
            } else {
                assertEquals(0, 1);
            }
        }


    }

    @Test 
    public void testDEFGHIL() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        DiscoveryBridgeTopology.create(domain,topology.nodeGId);
        DiscoveryBridgeTopology.create(domain,topology.nodeHId);
        DiscoveryBridgeTopology.create(domain,topology.nodeIId);
        DiscoveryBridgeTopology.create(domain,topology.nodeLId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT((topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT((topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT((topology.nodeLId),topology.bftL);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain());
    }


    @Test 
    public void testHierarchySetUp() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        DiscoveryBridgeTopology.create(domain,topology.nodeFId);
        DiscoveryBridgeTopology.create(domain,topology.nodeGId);
        DiscoveryBridgeTopology.create(domain,topology.nodeHId);
        DiscoveryBridgeTopology.create(domain,topology.nodeIId);
        DiscoveryBridgeTopology.create(domain,topology.nodeLId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT((topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT((topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT((topology.nodeLId),topology.bftL);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain());
        
        domain.hierarchySetUp(domain.getBridge(topology.nodeGId));
        assertEquals(topology.nodeGId, ndbt.getDomain().getRootBridge().getNodeId());
        assertTrue(ndbt.getDomain().getBridge(topology.nodeGId).isRootBridge());
        assertNull(ndbt.getDomain().getBridge(topology.nodeGId).getRootPort());
        assertFalse(ndbt.getDomain().getBridge(topology.nodeDId).isRootBridge());
        assertEquals(topology.portDD, ndbt.getDomain().getBridge(topology.nodeDId).getRootPort());
        assertFalse(ndbt.getDomain().getBridge(topology.nodeEId).isRootBridge());
        assertEquals(topology.portEE, ndbt.getDomain().getBridge(topology.nodeEId).getRootPort());
        assertFalse(ndbt.getDomain().getBridge(topology.nodeFId).isRootBridge());
        assertEquals(topology.portFF, ndbt.getDomain().getBridge(topology.nodeFId).getRootPort());
        assertFalse(ndbt.getDomain().getBridge(topology.nodeHId).isRootBridge());
        assertEquals(topology.portHH, ndbt.getDomain().getBridge(topology.nodeHId).getRootPort());
        assertFalse(ndbt.getDomain().getBridge(topology.nodeIId).isRootBridge());
        assertEquals(topology.portII, ndbt.getDomain().getBridge(topology.nodeIId).getRootPort());
        assertFalse(ndbt.getDomain().getBridge(topology.nodeLId).isRootBridge());
        assertEquals(topology.portLL, ndbt.getDomain().getBridge(topology.nodeLId).getRootPort());
    }

    @Test
    public void testFiveSwitchTopologyAD() {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        setBridgeElements(domain,topology.elemlist);
        DiscoveryBridgeTopology discoveryBridgeTopology= new DiscoveryBridgeTopology(domain);
        discoveryBridgeTopology.addUpdatedBFT(topology.nodeAId, topology.bftA);
        discoveryBridgeTopology.addUpdatedBFT(topology.nodeDId, topology.bftD);
        discoveryBridgeTopology.calculate();
    }

    @Test
    public void testFiveSwitchTopologyBCADE() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();        
        assertEquals(0, domain.getForwarders(topology.nodeAId).size());
        assertEquals(0, domain.getForwarders(topology.nodeBId).size());
        assertEquals(0, domain.getForwarders(topology.nodeCId).size());
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());
        
        
        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();
        topology.checkBC(domain);
        assertEquals(0, domain.getForwarders(topology.nodeAId).size());
        topology.checkBcalcBC(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBC(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.checkAcalcBCA(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCA(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCA(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());
        
        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.checkAcalcBCAD(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCAD(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCAD(domain.getForwarders(topology.nodeCId));
        topology.checkDcalcBCAD(domain.getForwarders(topology.nodeDId));
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());
        
        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBCAED() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        ndbtB.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtB.calculate();
        ndbtB.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtB.calculate();
        topology.checkAcalcBCA(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCA(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCA(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.checkAcalcBCAE(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCAE(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCAE(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        topology.checkEcalcBCAE(domain.getForwarders(topology.nodeEId));

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBCAThenED() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtB.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtB.calculate();
        topology.checkAcalcBCA(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCA(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCA(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtE.calculate();
        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        domain.hierarchySetUp(bridgeB);
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyEDCBA() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        
        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        
        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        domain.hierarchySetUp(bridgeB);
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBEDCA() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        topology.check(domain);

    }

    @Test
    public void testFiveSwitchTopologyEDCAB() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        domain.hierarchySetUp(bridgeB);
        topology.check(domain);

    }

    @Test
    public void testFiveSwitchTopologyBCADEBD() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        
        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        topology.check(domain);

        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        topology.check(domain);

        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        topology.check(domain);

    }
    
    @Test
    public void testFiveSwitchTopologyBEDCADBAEC() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        topology.check(domain);

        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.check(domain);

        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        topology.check(domain);

        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.check(domain);

        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.check(domain);

        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();
        topology.check(domain);
        
    }
    
    
    @Test
    public void testFiveSwitchTopologyBEDCABEDAC() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.nodeAId);
        DiscoveryBridgeTopology.create(domain,topology.nodeBId);
        DiscoveryBridgeTopology.create(domain,topology.nodeCId);
        DiscoveryBridgeTopology.create(domain,topology.nodeDId);
        DiscoveryBridgeTopology.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.check(domain);

        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        topology.check(domain);

        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.check(domain);

        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.check(domain);

        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.check(domain);

        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();
        topology.check(domain);
        
    }

    @Test
    public void testTwentySwitchTopology() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunalv01Id);
        DiscoveryBridgeTopology.create(domain,topology.villpizzasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.rsaspiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv01Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmuasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunalv02Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.daremunalv01Id),topology.bftdaremunalv01);
        ndbtB.addUpdatedBFT((topology.villpizzasw01Id),topology.bftvillpizzasw01);
        ndbtB.addUpdatedBFT((topology.rsaspiazzowl1Id),topology.bftrsaspiazzowl1);
        ndbtB.addUpdatedBFT((topology.vigrenmualv01Id),topology.bftvigrenmualv01);
        ndbtB.addUpdatedBFT((topology.vigrenmualv02Id),topology.bftvigrenmualv02);
        ndbtB.addUpdatedBFT((topology.vrendmunalv02Id),topology.bftvrendmunalv02);
        ndbtB.addUpdatedBFT((topology.daremunasw01Id),topology.bftdaremunasw01);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.vrendmunasw01Id),topology.bftvrendmunasw01);
        ndbtB.addUpdatedBFT((topology.vigrenmuasw01Id),topology.bftvigrenmuasw01);
        ndbtB.addUpdatedBFT((topology.comunevillarendenawl1Id),topology.bftcomunevillarendenawl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologySpiazzo() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        
        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologySpiazzoSVig() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.rsaspiazzowl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.rsaspiazzowl1Id),topology.bftrsaspiazzowl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    
    @Test
    public void testTwentySwitchTopologyVRendMun() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.vrendmunalv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.vrendmunalv02Id),topology.bftvrendmunalv02);
        ndbtB.addUpdatedBFT((topology.vrendmunasw01Id),topology.bftvrendmunasw01);
        ndbtB.addUpdatedBFT((topology.comunevillarendenawl1Id),topology.bftcomunevillarendenawl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }
    
    @Test
    public void testTwentySwitchTopologyVigReMun() {
        TwentyNodeTopology topology = new TwentyNodeTopology();
        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv01Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmuasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.vigrenmualv01Id),topology.bftvigrenmualv01);
        ndbtB.addUpdatedBFT((topology.vigrenmualv02Id),topology.bftvigrenmualv02);
        ndbtB.addUpdatedBFT((topology.vigrenmuasw01Id),topology.bftvigrenmuasw01);
        
        ndbtB.calculate();

    }

    @Test
    public void testTwentySwitchTopologyDareMun() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.daremunalv01Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.daremunalv01Id),topology.bftdaremunalv01);
        ndbtB.addUpdatedBFT((topology.daremunasw01Id),topology.bftdaremunasw01);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyAlvarion() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.vrendmunalv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunevillarendenawl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv01Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmuasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunalv01Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.vrendmunalv02Id),topology.bftvrendmunalv02);
        ndbtB.addUpdatedBFT((topology.vrendmunasw01Id),topology.bftvrendmunasw01);
        ndbtB.addUpdatedBFT((topology.comunevillarendenawl1Id),topology.bftcomunevillarendenawl1);
        ndbtB.addUpdatedBFT((topology.vigrenmualv01Id),topology.bftvigrenmualv01);
        ndbtB.addUpdatedBFT((topology.vigrenmualv02Id),topology.bftvigrenmualv02);
        ndbtB.addUpdatedBFT((topology.vigrenmuasw01Id),topology.bftvigrenmuasw01);
        ndbtB.addUpdatedBFT((topology.daremunalv01Id),topology.bftdaremunalv01);
        ndbtB.addUpdatedBFT((topology.daremunasw01Id),topology.bftdaremunasw01);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }


    @Test
    public void testTwentySwitchTopologyTwoSteps() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunalv01Id);
        DiscoveryBridgeTopology.create(domain,topology.villpizzasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.rsaspiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv01Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmuasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunalv02Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(domain);
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtA.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtA.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtA.addUpdatedBFT((topology.daremunalv01Id),topology.bftdaremunalv01);
        ndbtA.addUpdatedBFT((topology.villpizzasw01Id),topology.bftvillpizzasw01);
        ndbtA.addUpdatedBFT((topology.rsaspiazzowl1Id),topology.bftrsaspiazzowl1);
        ndbtA.addUpdatedBFT((topology.vigrenmualv01Id),topology.bftvigrenmualv01);
        ndbtA.addUpdatedBFT((topology.vigrenmualv02Id),topology.bftvigrenmualv02);

        ndbtA.calculate();
        assertEquals(1, ndbtA.getFailed().size());

        ndbtB.addUpdatedBFT((topology.vrendmunalv02Id),topology.bftvrendmunalv02);
        ndbtB.addUpdatedBFT((topology.daremunasw01Id),topology.bftdaremunasw01);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.vrendmunasw01Id),topology.bftvrendmunasw01);
        ndbtB.addUpdatedBFT((topology.vigrenmuasw01Id),topology.bftvigrenmuasw01);
        ndbtB.addUpdatedBFT((topology.comunevillarendenawl1Id),topology.bftcomunevillarendenawl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyVillpizzasw01B() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.villpizzasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.villpizzasw01Id),topology.bftvillpizzasw01);

        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());
    }

    @Test
    public void testTwentySwitchTopologyVillpizzasw01A() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.villpizzasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.villpizzasw01Id),topology.bftvillpizzasw01);

        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyLevel1() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);

        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyLevel2() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);

        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyLevel3() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);

        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());

    }

    //rsaspiazzowl1Id
    @Test
    public void testTwentySwitchTopologyLevel4() {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.rsaspiazzowl1Id);

        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.rsaspiazzowl1Id),topology.bftrsaspiazzowl1);

        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());

    }
    @Test
    public void testDuplicatedMac() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain,topology.spiazzofasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiasvigasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunalv01Id);
        DiscoveryBridgeTopology.create(domain,topology.villpizzasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.rsaspiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv01Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmualv02Id);
        DiscoveryBridgeTopology.create(domain,topology.vigrenmuasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunalv02Id);
        DiscoveryBridgeTopology.create(domain,topology.daremunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.spiazzomepe01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunespiazzowl1Id);
        DiscoveryBridgeTopology.create(domain,topology.vrendmunasw01Id);
        DiscoveryBridgeTopology.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);

        BridgeForwardingTable bridgeFtpe = 
                DiscoveryBridgeTopology.create(domain.getBridge(topology.spiazzomepe01Id), topology.bftspiazzomepe01);
        
        Map<String, Set<BridgePort>> duplicated = bridgeFtpe.getDuplicated();
        assertEquals(5, duplicated.size());

        assertTrue(duplicated.containsKey(topology.macdaremunasw01));  //port 5
        assertTrue(duplicated.containsKey(topology.mac001ebe70cec0));  //port 5
        assertTrue(duplicated.containsKey(topology.mac0022557fd68f));  //port 6
        assertTrue(duplicated.containsKey(topology.macvrendmunasw01)); //port 8
        assertTrue(duplicated.containsKey(topology.mac001906d5cf50));  //port 8
        assertEquals(2,duplicated.get(topology.macdaremunasw01).size());  //port 5
        assertEquals(2,duplicated.get(topology.mac001ebe70cec0).size());  //port 5
        assertEquals(2,duplicated.get(topology.mac0022557fd68f).size());  //port 8
        assertEquals(2,duplicated.get(topology.macvrendmunasw01).size()); //port 5
        assertEquals(2,duplicated.get(topology.mac001906d5cf50).size());  //port 5

        System.err.println(printTopology(bridgeFtpe.getEntries()));

        assertEquals(0, DiscoveryBridgeTopology.
                              create(domain.getBridge(topology.daremunasw01Id), topology.bftdaremunasw01).getDuplicated().
                              size());

        assertEquals(0, DiscoveryBridgeTopology.
                              create(domain.getBridge(topology.vigrenmuasw01Id), topology.bftvigrenmuasw01).getDuplicated().
                              size());

        assertEquals(0, DiscoveryBridgeTopology.
                            create(domain.getBridge(topology.vrendmunasw01Id), topology.bftvrendmunasw01).getDuplicated().
                            size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.spiazzofasw01Id), topology.bftspiazzofasw01).getDuplicated().
                          size());
                     
        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.spiasvigasw01Id), topology.bftspiasvigasw01).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.villpizzasw01Id), topology.bftvillpizzasw01).getDuplicated().
                          size());
        
        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.daremunalv01Id), topology.bftdaremunalv01).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.vigrenmualv01Id), topology.bftvigrenmualv01).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.vigrenmualv02Id), topology.bftvigrenmualv02).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.vrendmunalv02Id), topology.bftvrendmunalv02).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.rsaspiazzowl1Id), topology.bftrsaspiazzowl1).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.comunespiazzowl1Id), topology.bftcomunespiazzowl1).getDuplicated().
                          size());

        assertEquals(0, DiscoveryBridgeTopology.
                          create(domain.getBridge(topology.comunevillarendenawl1Id), topology.bftcomunevillarendenawl1).getDuplicated().
                          size());
        
    }
    
    @Test
    public void testUnresolvableTopology() {
        TwoUnresolvableTopology topology = new TwoUnresolvableTopology();
        BroadcastDomain domain = new BroadcastDomain();
        DiscoveryBridgeTopology.create(domain, topology.nodeIdA);
        DiscoveryBridgeTopology.create(domain, topology.nodeIdB);
        setBridgeElements(domain, topology.elemlist);
        
        DiscoveryBridgeTopology ndbt = new DiscoveryBridgeTopology(domain);
        
        ndbt.addUpdatedBFT((topology.nodeIdA), topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeIdB), topology.bftB);

        ndbt.calculate();
        assertEquals(0, ndbt.getFailed().size());
    }

}
