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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.config.EnhancedLinkdConfigManager;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeForwardingTable;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgePortWithMacs;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;

import com.google.common.collect.Sets;

public class BroadcastDomainTest extends EnLinkdTestHelper {

    EnhancedLinkd linkd;
            
    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.model.topology", "DEBUG");
        MockLogAppender.setupLogging(p);
        linkd = new EnhancedLinkd();
        EnhancedLinkdConfig config = new EnhancedLinkdConfigManager() {
            
            @Override
            public void save() throws IOException {
                
            }
            
            @Override
            public void reload() throws IOException {
                m_config = new EnlinkdConfiguration();
                m_config.setInitialSleepTime(1000L);
                m_config.setRescanInterval(10000L);
            }
            
            @Override
            protected void saveXml(String xml) throws IOException {
            }
        };
        config.reload();
        linkd.setLinkdConfig(config);
    }

    @Test
    public void testOneBridgeOnePortOneMac() throws Exception {
        OneBridgeOnePortOneMacTopology topology = new OneBridgeOnePortOneMacTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();
        
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());

    }

    @Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        OneBridgeMoreMacOnePortTopology topology = new OneBridgeMoreMacOnePortTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
        
        DiscoveryBridgeTopology ndbts= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbts.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbts.calculate();
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbts.getDomain());

    }

    @Test
    public void testOneBridgeComplete() throws Exception {

        OneBridgeCompleteTopology topology = new OneBridgeCompleteTopology();        
        
        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testTwoConnectedBridge() throws Exception {

        TwoConnectedBridgeTopology topology = new TwoConnectedBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain(),false);
        
        BroadcastDomain.hierarchySetUp(domain,domain.getBridge(topology.nodeBId));
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain(),true);
        
    }

    @Test
    public void testPrintTopologyFromLevel() throws Exception {
        TwoMergeBridgeTopology topology = new TwoMergeBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT(topology.nodeAId,topology.bftA);
        ndbt.addUpdatedBFT(topology.nodeBId,topology.bftB);
        ndbt.calculate();

        // this resulted in a NPE because of the unknown Id 3333, see NMS-9852
        domain.printTopologyFromLevel(Sets.newHashSet( topology.nodeAId, topology.nodeBId, 3333), 5);
    }

    @Test
    public void testTwoSwitchWithOnlyOnePortcondition3() throws Exception {
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
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

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
        Bridge.create(domain,nodeAId);
        Bridge.create(domain,nodeBId);
        setBridgeElements(domain,elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
    public void testTwoSwitchWithOnlyOnePortCondition2() throws Exception {
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
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

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
        Bridge.create(domain,nodeAId);
        Bridge.create(domain,nodeBId);
        setBridgeElements(domain,elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
    public void testTwoSwitchWithOnlyOnePortCondition1() throws Exception {
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
        Set<BridgeForwardingTableEntry> bftA = new HashSet<BridgeForwardingTableEntry>();
        Set<BridgeForwardingTableEntry> bftB = new HashSet<BridgeForwardingTableEntry>();
        List<BridgeElement> elemlist = new ArrayList<BridgeElement>();

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
        Bridge.create(domain,nodeAId);
        Bridge.create(domain,nodeBId);
        setBridgeElements(domain,elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCleanTopology() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        BroadcastDomain.clearTopologyForBridge(domain,topology.nodeBId);
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getNodeId().intValue());
        assertEquals(2, domain.getSharedSegments().size());
        assertEquals(5, domain.getMacsOnSegments().size());
        for (SharedSegment segment: domain.getSharedSegments()) {
        	assertEquals(0, SharedSegment.getBridgeBridgeLinks(segment).size());
        	assertEquals(1, segment.getBridgeIdsOnSegment().size());
        	assertEquals(topology.nodeAId.intValue(), segment.getBridgeIdsOnSegment().iterator().next().intValue());
        	
        	if (segment.containsMac(topology.macA11) && segment.containsMac(topology.macA12)) {
            	assertEquals(2, segment.getMacsOnSegment().size());
            	assertEquals(2, SharedSegment.getBridgeMacLinks(segment).size());
        		for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(segment)) {
        			assertEquals(topology.portA1.intValue(), link.getBridgePort().intValue());
        		}
        	} else if (segment.containsMac(topology.macB21) 
        			&& segment.containsMac(topology.macB22) 
        			&& segment.containsMac(topology.macAB)){
            	assertEquals(3, segment.getMacsOnSegment().size());
            	assertEquals(3, SharedSegment.getBridgeMacLinks(segment).size());
        		for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(segment)) {
        			assertEquals(topology.portAB.intValue(), link.getBridgePort().intValue());
        		}
        		
        	} else {
        		assertEquals(true, false);
        	}
        }
        
    }
    
    @Test 
    public void testCleanTopologyRoot() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
                
        BroadcastDomain.clearTopologyForBridge(domain,topology.nodeAId);
        assertEquals(5, domain.getMacsOnSegments().size());
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getNodeId().intValue());
        assertEquals(2, domain.getSharedSegments().size());
        for (SharedSegment segment: domain.getSharedSegments()) {
        	assertEquals(0, SharedSegment.getBridgeBridgeLinks(segment).size());
        	assertEquals(1, segment.getBridgeIdsOnSegment().size());
        	assertEquals(topology.nodeBId.intValue(), segment.getBridgeIdsOnSegment().iterator().next().intValue());
        	
        	if (segment.containsMac(topology.macA11) && segment.containsMac(topology.macA12) 
        		&& segment.containsMac(topology.macAB)) {
            	assertEquals(3, segment.getMacsOnSegment().size());
            	assertEquals(3, SharedSegment.getBridgeMacLinks(segment).size());
        		for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(segment)) {
        			assertEquals(topology.portBA.intValue(), link.getBridgePort().intValue());
        		}
        	} else if (segment.containsMac(topology.macB21) 
        			&& segment.containsMac(topology.macB22) 
        			){
            	assertEquals(2, segment.getMacsOnSegment().size());
            	assertEquals(2, SharedSegment.getBridgeMacLinks(segment).size());
        		for (BridgeMacLink link: SharedSegment.getBridgeMacLinks(segment)) {
        			assertEquals(topology.portB2.intValue(), link.getBridgePort().intValue());
        		}
        	} else {
        		assertEquals(true, false);
        	}
        	
        }
    }

    @Test 
    public void testTwoBridgeWithBackbonePortsUsingBridgeAddressInBft() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopologyWithBridgeinBft topology = new TwoBridgeWithBackbonePortsTopologyWithBridgeinBft();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getNodeId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test 
    public void testTwoBridgeOneCalculation() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        System.err.println(BridgeForwardingTableEntry.printTopology(BroadcastDomain.calculateBFT(domain,domain.getRootBridge())));

        List<SharedSegment> shsegs = ndbt.getDomain().getSharedSegments();
        assertEquals(3, shsegs.size());
        
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
;
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain(),true);
        assertEquals(topology.nodeBId, domain.getRootBridge().getNodeId());

        System.err.println(BridgeForwardingTableEntry.printTopology(BroadcastDomain.calculateBFT(domain,domain.getBridge(topology.nodeAId))));
    }

    @Test 
    public void testTwoBridgeTwoCalculationReverse() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();
        
        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();
        
        List<SharedSegment> shsegs = ndbt.getDomain().getSharedSegments();
        assertEquals(3, shsegs.size());
        
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain(),false);
        assertEquals(topology.nodeAId, domain.getRootBridge().getNodeId());
        
        System.err.println(BridgeForwardingTableEntry.printTopology(BroadcastDomain.calculateBFT(domain, domain.getRootBridge())));
        System.err.println(BridgeForwardingTableEntry.printTopology(BroadcastDomain.calculateBFT(domain,domain.getBridge(topology.nodeBId))));

    }

    @Test
    public void testAB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());

    }

    @Test
    public void testBA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());
    }

    @Test
    public void testAC() throws BridgeTopologyException {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());
    }

    @Test
    public void testCA() throws BridgeTopologyException {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());
    }

    @Test
    public void testBC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain());
    }

    @Test
    public void testCB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain());
    }

    @Test
    public void testElectRootBridge() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        Bridge elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(null, elected);

        //B root
        domain.getBridge(topology.nodeAId).setDesignated(topology.macB);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macB);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(topology.nodeBId.intValue(), elected.getNodeId().intValue());

        //A Root
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(topology.macA);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macB);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(topology.nodeAId.intValue(), elected.getNodeId().intValue());

        //C Root
        domain.getBridge(topology.nodeAId).setDesignated(topology.macB);
        domain.getBridge(topology.nodeBId).setDesignated(topology.macC);
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(topology.nodeCId.intValue(), elected.getNodeId().intValue());

        //C root
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(topology.macC);
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(topology.nodeCId.intValue(), elected.getNodeId().intValue());
        
       //D? root
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated("dddddddddddd");
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(topology.nodeBId.intValue(), elected.getNodeId().intValue());

        //A root B is bypassed
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(null);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macA);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(topology.nodeAId.intValue(), elected.getNodeId().intValue());

        //loop is bypassed
        domain.getBridge(topology.nodeAId).setDesignated(topology.macC);
        domain.getBridge(topology.nodeBId).setDesignated(null);
        domain.getBridge(topology.nodeCId).setDesignated(topology.macA);
        try {
            elected = BroadcastDomain.electRootBridge(domain);
        } catch (BridgeTopologyException e) {
            assertEquals("getUpperBridge, too many iterations", e.getMessage());
        }

        //null root B is bypassed
        domain.getBridge(topology.nodeAId).setDesignated(null);
        domain.getBridge(topology.nodeBId).setDesignated(null);
        domain.getBridge(topology.nodeCId).setDesignated(null);
        elected = BroadcastDomain.electRootBridge(domain);
        assertEquals(null, elected);

    }
    
    
    @Test
    public void testABC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
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
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());

        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testBAThenC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());

        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testBThenCA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();

        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();
        BroadcastDomain.hierarchySetUp(domain,domain.getBridge(topology.nodeAId));
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCThenAB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeCId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.calculate();

        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        BroadcastDomain.hierarchySetUp(domain,domain.getBridge(topology.nodeAId));

        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCBThenA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeBId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbt.calculate();
        topology.checkBC(ndbt.getDomain());

        Bridge.create(domain,topology.nodeAId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbt.calculate();

        BroadcastDomain.hierarchySetUp(domain,domain.getBridge(topology.nodeAId));
        topology.check(ndbt.getDomain());

    }

    @Test
    public void testDE() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDE(ndbt.getDomain());
    }

    @Test
    public void testDF() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain());
    }

    @Test
    public void testEF() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeEId);
        Bridge.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkEF(ndbt.getDomain());
    }

    @Test
    public void testDG() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeGId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDG(ndbt.getDomain());
    }

    @Test
    public void testDEF() throws BridgeTopologyException {

        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        Bridge.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain());

    }

    @Test
    public void testDFThenE() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeFId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain());
        
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);

        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain());

    }

    @Test 
    public void testDEFG() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        Bridge.create(domain,topology.nodeFId);
        Bridge.create(domain,topology.nodeGId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        Bridge.create(domain,topology.nodeFId);
        Bridge.create(domain,topology.nodeGId);
        Bridge.create(domain,topology.nodeIId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDEFG(ndbt.getDomain());
        
        ndbt.addUpdatedBFT((topology.nodeIId),topology.bftI);
        ndbt.calculate();
        
        for (BridgeForwardingTableEntry bftentry: BroadcastDomain.calculateBFT(domain, domain.getBridge(topology.nodeIId))) {
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

        for (BridgeForwardingTableEntry bftentry: BroadcastDomain.calculateBFT(domain, domain.getBridge(topology.nodeGId))) {
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
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        Bridge.create(domain,topology.nodeFId);
        Bridge.create(domain,topology.nodeGId);
        Bridge.create(domain,topology.nodeHId);
        Bridge.create(domain,topology.nodeIId);
        Bridge.create(domain,topology.nodeLId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        Bridge.create(domain,topology.nodeFId);
        Bridge.create(domain,topology.nodeGId);
        Bridge.create(domain,topology.nodeHId);
        Bridge.create(domain,topology.nodeIId);
        Bridge.create(domain,topology.nodeLId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbt= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbt.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT((topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT((topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT((topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT((topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT((topology.nodeLId),topology.bftL);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain());
        
        BroadcastDomain.hierarchySetUp(domain,domain.getBridge(topology.nodeGId));
        assertEquals(topology.nodeGId, ndbt.getDomain().getRootBridge().getNodeId());
        assertEquals(true, ndbt.getDomain().getBridge(topology.nodeGId).isRootBridge());
        assertEquals(null, ndbt.getDomain().getBridge(topology.nodeGId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeDId).isRootBridge());
        assertEquals(topology.portDD, ndbt.getDomain().getBridge(topology.nodeDId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeEId).isRootBridge());
        assertEquals(topology.portEE, ndbt.getDomain().getBridge(topology.nodeEId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeFId).isRootBridge());
        assertEquals(topology.portFF, ndbt.getDomain().getBridge(topology.nodeFId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeHId).isRootBridge());
        assertEquals(topology.portHH, ndbt.getDomain().getBridge(topology.nodeHId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeIId).isRootBridge());
        assertEquals(topology.portII, ndbt.getDomain().getBridge(topology.nodeIId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeLId).isRootBridge());
        assertEquals(topology.portLL, ndbt.getDomain().getBridge(topology.nodeLId).getRootPort());
    }

    @Test
    public void testFiveSwitchTopologyBCADE() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();        
        assertEquals(0, domain.getForwarders(topology.nodeAId).size());
        assertEquals(0, domain.getForwarders(topology.nodeBId).size());
        assertEquals(0, domain.getForwarders(topology.nodeCId).size());
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());
        
        
        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();
        topology.checkBC(domain);
        assertEquals(0, domain.getForwarders(topology.nodeAId).size());
        topology.checkBcalcBC(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBC(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.checkAcalcBCA(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCA(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCA(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());
        
        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.checkAcalcBCAD(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCAD(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCAD(domain.getForwarders(topology.nodeCId));
        topology.checkDcalcBCAD(domain.getForwarders(topology.nodeDId));
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());
        
        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBCAED() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
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

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.checkAcalcBCAE(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCAE(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCAE(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        topology.checkEcalcBCAE(domain.getForwarders(topology.nodeEId));

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBCAThenED() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtB.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtB.calculate();
        topology.checkAcalcBCA(domain.getForwarders(topology.nodeAId));
        topology.checkBcalcBCA(domain.getForwarders(topology.nodeBId));
        topology.checkCcalcBCA(domain.getForwarders(topology.nodeCId));
        assertEquals(0, domain.getForwarders(topology.nodeDId).size());
        assertEquals(0, domain.getForwarders(topology.nodeEId).size());

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtE.calculate();
        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        BroadcastDomain.hierarchySetUp(domain,bridgeB);
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyEDCBA() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        
        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        
        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        BroadcastDomain.hierarchySetUp(domain,bridgeB);
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBEDCA() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        topology.check(domain);

    }

    @Test
    public void testFiveSwitchTopologyEDCAB() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        BroadcastDomain.hierarchySetUp(domain,bridgeB);
        topology.check(domain);

    }

    @Test
    public void testFiveSwitchTopologyBCADEBD() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        
        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtA.addUpdatedBFT((topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        
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
        Bridge.create(domain,topology.nodeAId);
        Bridge.create(domain,topology.nodeBId);
        Bridge.create(domain,topology.nodeCId);
        Bridge.create(domain,topology.nodeDId);
        Bridge.create(domain,topology.nodeEId);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        DiscoveryBridgeTopology ndbtE= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtE.addUpdatedBFT((topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        DiscoveryBridgeTopology ndbtD= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtD.addUpdatedBFT((topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        DiscoveryBridgeTopology ndbtC= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtC.addUpdatedBFT((topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        
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
    public void testTwentySwitchTopology() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.spiasvigasw01Id);
        Bridge.create(domain,topology.daremunalv01Id);
        Bridge.create(domain,topology.villpizzasw01Id);
        Bridge.create(domain,topology.rsaspiazzowl1Id);
        Bridge.create(domain,topology.vigrenmualv01Id);
        Bridge.create(domain,topology.vigrenmualv02Id);
        Bridge.create(domain,topology.vigrenmuasw01Id);
        Bridge.create(domain,topology.vrendmunalv02Id);
        Bridge.create(domain,topology.daremunasw01Id);
        Bridge.create(domain,topology.spiazzomepe01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        Bridge.create(domain,topology.vrendmunasw01Id);
        Bridge.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
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
    public void testTwentySwitchTopologySpiazzo() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        Bridge.create(domain,topology.spiazzomepe01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologySpiazzoSVig() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiasvigasw01Id);
        Bridge.create(domain,topology.rsaspiazzowl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.rsaspiazzowl1Id),topology.bftrsaspiazzowl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    
    @Test
    public void testTwentySwitchTopologyVRendMun() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.vrendmunalv02Id);
        Bridge.create(domain,topology.vrendmunasw01Id);
        Bridge.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.vrendmunalv02Id),topology.bftvrendmunalv02);
        ndbtB.addUpdatedBFT((topology.vrendmunasw01Id),topology.bftvrendmunasw01);
        ndbtB.addUpdatedBFT((topology.comunevillarendenawl1Id),topology.bftcomunevillarendenawl1);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }
    
    @Test
    public void testTwentySwitchTopologyVigReMun() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();
        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.vigrenmualv01Id);
        Bridge.create(domain,topology.vigrenmualv02Id);
        Bridge.create(domain,topology.vigrenmuasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.vigrenmualv01Id),topology.bftvigrenmualv01);
        ndbtB.addUpdatedBFT((topology.vigrenmualv02Id),topology.bftvigrenmualv02);
        ndbtB.addUpdatedBFT((topology.vigrenmuasw01Id),topology.bftvigrenmuasw01);
        
        ndbtB.calculate();

    }

    @Test
    public void testTwentySwitchTopologyDareMun() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.daremunalv01Id);
        Bridge.create(domain,topology.daremunasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.daremunalv01Id),topology.bftdaremunalv01);
        ndbtB.addUpdatedBFT((topology.daremunasw01Id),topology.bftdaremunasw01);
        
        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyAlvarion() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.vrendmunalv02Id);
        Bridge.create(domain,topology.vrendmunasw01Id);
        Bridge.create(domain,topology.comunevillarendenawl1Id);
        Bridge.create(domain,topology.vigrenmualv01Id);
        Bridge.create(domain,topology.vigrenmualv02Id);
        Bridge.create(domain,topology.vigrenmuasw01Id);
        Bridge.create(domain,topology.daremunalv01Id);
        Bridge.create(domain,topology.daremunasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
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
    public void testTwentySwitchTopologyTwoSteps() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.spiasvigasw01Id);
        Bridge.create(domain,topology.daremunalv01Id);
        Bridge.create(domain,topology.villpizzasw01Id);
        Bridge.create(domain,topology.rsaspiazzowl1Id);
        Bridge.create(domain,topology.vigrenmualv01Id);
        Bridge.create(domain,topology.vigrenmualv02Id);
        Bridge.create(domain,topology.vigrenmuasw01Id);
        Bridge.create(domain,topology.vrendmunalv02Id);
        Bridge.create(domain,topology.daremunasw01Id);
        Bridge.create(domain,topology.spiazzomepe01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        Bridge.create(domain,topology.vrendmunasw01Id);
        Bridge.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtA= new DiscoveryBridgeTopology(linkd,domain);
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
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
    public void testTwentySwitchTopologyVillpizzasw01B() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiasvigasw01Id);
        Bridge.create(domain,topology.villpizzasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.villpizzasw01Id),topology.bftvillpizzasw01);

        ndbtB.calculate();
        
        assertEquals(1, ndbtB.getFailed().size());
    }

    @Test
    public void testTwentySwitchTopologyVillpizzasw01A() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.villpizzasw01Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.villpizzasw01Id),topology.bftvillpizzasw01);

        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyLevel1() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);

        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyLevel2() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzomepe01Id);
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);

        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    @Test
    public void testTwentySwitchTopologyLevel3() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzomepe01Id);
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        Bridge.create(domain,topology.spiasvigasw01Id);
        
        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);

        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }

    //rsaspiazzowl1Id
    @Test
    public void testTwentySwitchTopologyLevel4() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzomepe01Id);
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        Bridge.create(domain,topology.spiasvigasw01Id);
        Bridge.create(domain,topology.rsaspiazzowl1Id);

        setBridgeElements(domain,topology.elemlist);
        
        DiscoveryBridgeTopology ndbtB= new DiscoveryBridgeTopology(linkd,domain);
        
        ndbtB.addUpdatedBFT((topology.spiazzofasw01Id),topology.bftspiazzofasw01);
        ndbtB.addUpdatedBFT((topology.comunespiazzowl1Id),topology.bftcomunespiazzowl1);
        ndbtB.addUpdatedBFT((topology.spiazzomepe01Id),topology.bftspiazzomepe01);
        ndbtB.addUpdatedBFT((topology.spiasvigasw01Id),topology.bftspiasvigasw01);
        ndbtB.addUpdatedBFT((topology.rsaspiazzowl1Id),topology.bftrsaspiazzowl1);

        ndbtB.calculate();
        
        assertEquals(0, ndbtB.getFailed().size());

    }
    @Test
    public void testDuplicatedMac() throws BridgeTopologyException {
        TwentyNodeTopology topology = new TwentyNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        Bridge.create(domain,topology.spiazzofasw01Id);
        Bridge.create(domain,topology.spiasvigasw01Id);
        Bridge.create(domain,topology.daremunalv01Id);
        Bridge.create(domain,topology.villpizzasw01Id);
        Bridge.create(domain,topology.rsaspiazzowl1Id);
        Bridge.create(domain,topology.vigrenmualv01Id);
        Bridge.create(domain,topology.vigrenmualv02Id);
        Bridge.create(domain,topology.vigrenmuasw01Id);
        Bridge.create(domain,topology.vrendmunalv02Id);
        Bridge.create(domain,topology.daremunasw01Id);
        Bridge.create(domain,topology.spiazzomepe01Id);
        Bridge.create(domain,topology.comunespiazzowl1Id);
        Bridge.create(domain,topology.vrendmunasw01Id);
        Bridge.create(domain,topology.comunevillarendenawl1Id);
        setBridgeElements(domain,topology.elemlist);

        BridgeForwardingTable bridgeFtpe = 
                BridgeForwardingTable.create(domain.getBridge(topology.spiazzomepe01Id), topology.bftspiazzomepe01);
        
        Map<String, Set<BridgePort>> duplicated = bridgeFtpe.getDuplicated();
        assertEquals(5, duplicated.size());

        assertTrue(duplicated.keySet().contains(topology.macdaremunasw01));  //port 5
        assertTrue(duplicated.keySet().contains(topology.mac001ebe70cec0));  //port 5
        assertTrue(duplicated.keySet().contains(topology.mac0022557fd68f));  //port 6
        assertTrue(duplicated.keySet().contains(topology.macvrendmunasw01)); //port 8
        assertTrue(duplicated.keySet().contains(topology.mac001906d5cf50));  //port 8
        assertEquals(2,duplicated.get(topology.macdaremunasw01).size());  //port 5
        assertEquals(2,duplicated.get(topology.mac001ebe70cec0).size());  //port 5
        assertEquals(2,duplicated.get(topology.mac0022557fd68f).size());  //port 8
        assertEquals(2,duplicated.get(topology.macvrendmunasw01).size()); //port 5
        assertEquals(2,duplicated.get(topology.mac001906d5cf50).size());  //port 5

        System.err.println(BridgeForwardingTableEntry.printTopology(bridgeFtpe.getEntries()));

        assertEquals(0,BridgeForwardingTable.
                              create(domain.getBridge(topology.daremunasw01Id), topology.bftdaremunasw01).getDuplicated().
                              size());

        assertEquals(0,BridgeForwardingTable.
                              create(domain.getBridge(topology.vigrenmuasw01Id), topology.bftvigrenmuasw01).getDuplicated().
                              size());

        assertEquals(0,BridgeForwardingTable.
                            create(domain.getBridge(topology.vrendmunasw01Id), topology.bftvrendmunasw01).getDuplicated().
                            size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.spiazzofasw01Id), topology.bftspiazzofasw01).getDuplicated().
                          size());
                     
        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.spiasvigasw01Id), topology.bftspiasvigasw01).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.villpizzasw01Id), topology.bftvillpizzasw01).getDuplicated().
                          size());
        
        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.daremunalv01Id), topology.bftdaremunalv01).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.vigrenmualv01Id), topology.bftvigrenmualv01).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.vigrenmualv02Id), topology.bftvigrenmualv02).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.vrendmunalv02Id), topology.bftvrendmunalv02).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.rsaspiazzowl1Id), topology.bftrsaspiazzowl1).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.comunespiazzowl1Id), topology.bftcomunespiazzowl1).getDuplicated().
                          size());

        assertEquals(0,BridgeForwardingTable.
                          create(domain.getBridge(topology.comunevillarendenawl1Id), topology.bftcomunevillarendenawl1).getDuplicated().
                          size());
        
    }

}
