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
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_SYSOID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_SYSOID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_SYSOID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.opennms.netmgt.nb.Nms10205bNetworkBuilder;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;

public class EnLinkdIT extends EnLinkdBuilderITCase {

	Nms10205bNetworkBuilder builder10205a = new Nms10205bNetworkBuilder();
	Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();    

    @Test
    public void testGetSnmpNodeList() throws Exception {
        m_nodeDao.save(builder10205a.getMumbai());
        m_nodeDao.save(builder10205a.getDelhi());
        m_nodeDao.save(builder.getSwitch1());

        m_nodeDao.flush();

        final int mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME).getId().intValue();
        final int delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME).getId().intValue();
        final int switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME).getId().intValue();
        

        List<Node> linkablenodes = m_linkd.getQueryManager().getSnmpNodeList();
        assertNotNull(linkablenodes);
        assertEquals(3, linkablenodes.size());
        
        for (Node linkablenode: linkablenodes) {
        	if (linkablenode.getNodeId() == mumbai) {
        		assertEquals(InetAddressUtils.addr(MUMBAI_IP), linkablenode.getSnmpPrimaryIpAddr());
        		assertEquals(MUMBAI_SYSOID,linkablenode.getSysoid());
        	} else if (linkablenode.getNodeId() == delhi) {
        		assertEquals(InetAddressUtils.addr(DELHI_IP), linkablenode.getSnmpPrimaryIpAddr());
        		assertEquals(DELHI_SYSOID,linkablenode.getSysoid());
        	} else if (linkablenode.getNodeId() == switch1) {
        		assertEquals(InetAddressUtils.addr(SWITCH1_IP), linkablenode.getSnmpPrimaryIpAddr());
        		assertEquals(SWITCH1_SYSOID,linkablenode.getSysoid());
        	} else {
        		assertTrue(false);
        	}
        }

        Node delhilinkablenode = m_linkd.getQueryManager().getSnmpNode(delhi);
        assertNotNull(delhilinkablenode);
		assertEquals(delhi, delhilinkablenode.getNodeId());
		assertEquals(InetAddressUtils.addr(DELHI_IP), delhilinkablenode.getSnmpPrimaryIpAddr());
		assertEquals(DELHI_SYSOID,delhilinkablenode.getSysoid());
        
    }
    
    @Test
    public void testStoreBft() {
        OneBridgeCompleteTopology topology = new OneBridgeCompleteTopology();
        m_nodeDao.save(topology.nodeA);
        
        m_linkd.getQueryManager().store(topology.nodeAId, topology.bftA);
        
        List<BridgeMacLink> links = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(topology.nodeAId);
        assertEquals(topology.bftA.size(), links.size());
        for (BridgeMacLink link: links) {
            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
            System.err.println(link.printTopology());
        }
    }
    
    @Test 
    public void testLoadTopology() {
        ABCTopology topology = new ABCTopology();
        OnmsNode lnodeA = topology.nodeA;
        lnodeA.setForeignSource("linkd");
        lnodeA.setForeignId("nodeA");
        lnodeA.setLabel("nodeA");

        OnmsNode lnodeB = topology.nodeB;
        lnodeB.setForeignSource("linkd");
        lnodeB.setForeignId("nodeB");
        lnodeB.setLabel("nodeB");

        OnmsNode lnodeC = topology.nodeC;
        lnodeC.setForeignSource("linkd");
        lnodeC.setForeignId("nodeC");
        lnodeC.setLabel("nodeC");


        m_nodeDao.save(lnodeA);
        m_nodeDao.save(lnodeB);
        m_nodeDao.save(lnodeC);

        OnmsNode nodeA = m_nodeDao.findByForeignId("linkd", "nodeA");
        OnmsNode nodeB = m_nodeDao.findByForeignId("linkd", "nodeB");
        OnmsNode nodeC = m_nodeDao.findByForeignId("linkd", "nodeC");

        topology.nodeAId = nodeA.getId();
        topology.nodeBId = nodeB.getId();
        topology.nodeCId = nodeC.getId();

        topology.nodeA = nodeA;
        topology.nodeB = nodeB;
        topology.nodeC = nodeC;

        BridgeBridgeLink ablink = new BridgeBridgeLink();
        ablink.setNode(nodeA);
        ablink.setBridgePort(topology.portAB);
        ablink.setDesignatedNode(nodeB);
        ablink.setDesignatedPort(topology.portBA);
        ablink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(ablink);

        BridgeBridgeLink bclink = new BridgeBridgeLink();
        bclink.setNode(nodeC);
        bclink.setBridgePort(topology.portCB);
        bclink.setDesignatedNode(nodeB);
        bclink.setDesignatedPort(topology.portBC);
        bclink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(bclink);

        BridgeMacLink mac1 = new BridgeMacLink();
        mac1.setNode(nodeA);
        mac1.setBridgePort(topology.portA);
        mac1.setMacAddress(topology.mac1);
        mac1.setBridgeMacLinkLastPollTime(mac1.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac1);

        BridgeMacLink mac2 = new BridgeMacLink();
        mac2.setNode(nodeB);
        mac2.setBridgePort(topology.portB);
        mac2.setMacAddress(topology.mac2);
        mac2.setBridgeMacLinkLastPollTime(mac2.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac2);

        BridgeMacLink mac3 = new BridgeMacLink();
        mac3.setNode(nodeC);
        mac3.setBridgePort(topology.portC);
        mac3.setMacAddress(topology.mac3);
        mac3.setBridgeMacLinkLastPollTime(mac3.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac3);

        m_bridgeMacLinkDao.flush();
        m_bridgeBridgeLinkDao.flush();
        assertEquals(3, m_bridgeMacLinkDao.countAll());
        assertEquals(2, m_bridgeBridgeLinkDao.countAll());
        
        assertNotNull(m_bridgeTopologyDao);
        m_linkd.getQueryManager().loadBridgeTopology();

        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertEquals(nodeAbd, nodeBbd);
        assertEquals(nodeAbd, nodeCbd);
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        topology.check(nodeAbd);
        
        List<SharedSegment> nodeASegments = m_bridgeTopologyDao.getBridgeNodeSharedSegments(m_bridgeBridgeLinkDao, m_bridgeMacLinkDao, nodeA.getId());
        assertEquals(2, nodeASegments.size());
        for (SharedSegment segment: nodeASegments) {
        	System.err.println(segment.printTopology());
        }
    }    
    
    @Test
    public void testDeleteBridgeC() {
        ABCTopology topology = new ABCTopology();
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("nodeA").setForeignSource("linkd").setForeignId("nodeA").setSysObjectId("0.0").setSysName("nodeA").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.1").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        
        nb.addNode("nodeB").setForeignSource("linkd").setForeignId("nodeB").setSysObjectId("0.0").setSysName("nodeB").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.2").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        
        nb.addNode("nodeC").setForeignSource("linkd").setForeignId("nodeC").setSysObjectId("0.0").setSysName("nodeC").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.3").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        OnmsNode nodeA = m_nodeDao.findByForeignId("linkd", "nodeA");
        OnmsNode nodeB = m_nodeDao.findByForeignId("linkd", "nodeB");
        OnmsNode nodeC = m_nodeDao.findByForeignId("linkd", "nodeC");

        topology.nodeAId = nodeA.getId();
        topology.nodeBId = nodeB.getId();
        topology.nodeCId = nodeC.getId();

        topology.nodeA = nodeA;
        topology.nodeB = nodeB;
        topology.nodeC = nodeC;

        BridgeBridgeLink ablink = new BridgeBridgeLink();
        ablink.setNode(nodeA);
        ablink.setBridgePort(topology.portAB);
        ablink.setDesignatedNode(nodeB);
        ablink.setDesignatedPort(topology.portBA);
        ablink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(ablink);

        BridgeBridgeLink bclink = new BridgeBridgeLink();
        bclink.setNode(nodeC);
        bclink.setBridgePort(topology.portCB);
        bclink.setDesignatedNode(nodeB);
        bclink.setDesignatedPort(topology.portBC);
        bclink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(bclink);

        BridgeMacLink mac1 = new BridgeMacLink();
        mac1.setNode(nodeA);
        mac1.setBridgePort(topology.portA);
        mac1.setMacAddress(topology.mac1);
        mac1.setBridgeMacLinkLastPollTime(mac1.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac1);

        BridgeMacLink mac2 = new BridgeMacLink();
        mac2.setNode(nodeB);
        mac2.setBridgePort(topology.portB);
        mac2.setMacAddress(topology.mac2);
        mac2.setBridgeMacLinkLastPollTime(mac2.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac2);

        BridgeMacLink mac3 = new BridgeMacLink();
        mac3.setNode(nodeC);
        mac3.setBridgePort(topology.portC);
        mac3.setMacAddress(topology.mac3);
        mac3.setBridgeMacLinkLastPollTime(mac3.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac3);

        m_bridgeMacLinkDao.flush();
        m_bridgeBridgeLinkDao.flush();
        assertEquals(3, m_bridgeMacLinkDao.countAll());
        assertEquals(2, m_bridgeBridgeLinkDao.countAll());
        
        assertNotNull(m_bridgeTopologyDao);
        m_linkd.getQueryManager().loadBridgeTopology();

        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        assertNotNull(nodeBbd);
        assertEquals(nodeAbd, nodeBbd);
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertNotNull(nodeCbd);
        assertEquals(nodeAbd, nodeCbd);
        assertNull(nodeAbd.getRootBridgeId());
        assertTrue(nodeAbd.containBridgeId(nodeA.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeB.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeC.getId()));
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        assertNotNull(nodeAbd.getRootBridgeId());
        topology.check(nodeAbd);
        
        m_linkd.deleteNode(nodeC.getId());
        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        
        BroadcastDomain domain = m_bridgeTopologyDao.getAll().iterator().next();
        topology.checkAB(domain);
        
    }
    
    @Test
    public void testDeleteBridgeB() {
        ABCTopology topology = new ABCTopology();
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("nodeA").setForeignSource("linkd").setForeignId("nodeA").setSysObjectId("0.0").setSysName("nodeA").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.1").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        
        nb.addNode("nodeB").setForeignSource("linkd").setForeignId("nodeB").setSysObjectId("0.0").setSysName("nodeB").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.2").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        
        nb.addNode("nodeC").setForeignSource("linkd").setForeignId("nodeC").setSysObjectId("0.0").setSysName("nodeC").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.3").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        OnmsNode nodeA = m_nodeDao.findByForeignId("linkd", "nodeA");
        OnmsNode nodeB = m_nodeDao.findByForeignId("linkd", "nodeB");
        OnmsNode nodeC = m_nodeDao.findByForeignId("linkd", "nodeC");

        topology.nodeAId = nodeA.getId();
        topology.nodeBId = nodeB.getId();
        topology.nodeCId = nodeC.getId();

        topology.nodeA = nodeA;
        topology.nodeB = nodeB;
        topology.nodeC = nodeC;

        BridgeBridgeLink ablink = new BridgeBridgeLink();
        ablink.setNode(nodeB);
        ablink.setBridgePort(topology.portBA);
        ablink.setDesignatedNode(nodeA);
        ablink.setDesignatedPort(topology.portAB);
        ablink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(ablink);

        BridgeBridgeLink bclink = new BridgeBridgeLink();
        bclink.setNode(nodeC);
        bclink.setBridgePort(topology.portCB);
        bclink.setDesignatedNode(nodeB);
        bclink.setDesignatedPort(topology.portBC);
        bclink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(bclink);

        BridgeMacLink mac1 = new BridgeMacLink();
        mac1.setNode(nodeA);
        mac1.setBridgePort(topology.portA);
        mac1.setMacAddress(topology.mac1);
        mac1.setBridgeMacLinkLastPollTime(mac1.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac1);

        BridgeMacLink mac2 = new BridgeMacLink();
        mac2.setNode(nodeB);
        mac2.setBridgePort(topology.portB);
        mac2.setMacAddress(topology.mac2);
        mac2.setBridgeMacLinkLastPollTime(mac2.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac2);

        BridgeMacLink mac3 = new BridgeMacLink();
        mac3.setNode(nodeC);
        mac3.setBridgePort(topology.portC);
        mac3.setMacAddress(topology.mac3);
        mac3.setBridgeMacLinkLastPollTime(mac3.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac3);

        m_bridgeMacLinkDao.flush();
        m_bridgeBridgeLinkDao.flush();
        assertEquals(3, m_bridgeMacLinkDao.countAll());
        assertEquals(2, m_bridgeBridgeLinkDao.countAll());
        
        assertNotNull(m_bridgeTopologyDao);
        m_linkd.getQueryManager().loadBridgeTopology();

        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertEquals(nodeAbd, nodeBbd);
        assertEquals(nodeAbd, nodeCbd);
        assertNull(nodeAbd.getRootBridgeId());
        assertTrue(nodeAbd.containBridgeId(nodeA.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeB.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeC.getId()));
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        assertNotNull(nodeAbd.getRootBridgeId());
        topology.check(nodeAbd);

        assertTrue(m_linkd.scheduleNodeCollection(nodeB.getId()));
        
        m_linkd.deleteNode(nodeB.getId());
        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        
        BroadcastDomain domain = m_bridgeTopologyDao.getAll().iterator().next();
        topology.checkAC(domain);
        
    }
    
    @Test
    public void testDeleteBridgeA() {
        ABCTopology topology = new ABCTopology();
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("nodeA").setForeignSource("linkd").setForeignId("nodeA").setSysObjectId("0.0").setSysName("nodeA").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.1").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());
        
        nb.addNode("nodeB").setForeignSource("linkd").setForeignId("nodeB").setSysObjectId("0.0").setSysName("nodeB").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.2").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        
        nb.addNode("nodeC").setForeignSource("linkd").setForeignId("nodeC").setSysObjectId("0.0").setSysName("nodeC").setType(NodeType.ACTIVE);
        nb.addInterface("10.0.1.3").setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        OnmsNode nodeA = m_nodeDao.findByForeignId("linkd", "nodeA");
        OnmsNode nodeB = m_nodeDao.findByForeignId("linkd", "nodeB");
        OnmsNode nodeC = m_nodeDao.findByForeignId("linkd", "nodeC");

        topology.nodeAId = nodeA.getId();
        topology.nodeBId = nodeB.getId();
        topology.nodeCId = nodeC.getId();

        topology.nodeA = nodeA;
        topology.nodeB = nodeB;
        topology.nodeC = nodeC;

        BridgeBridgeLink ablink = new BridgeBridgeLink();
        ablink.setNode(nodeB);
        ablink.setBridgePort(topology.portBA);
        ablink.setDesignatedNode(nodeA);
        ablink.setDesignatedPort(topology.portAB);
        ablink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(ablink);

        BridgeBridgeLink bclink = new BridgeBridgeLink();
        bclink.setNode(nodeC);
        bclink.setBridgePort(topology.portCB);
        bclink.setDesignatedNode(nodeB);
        bclink.setDesignatedPort(topology.portBC);
        bclink.setBridgeBridgeLinkLastPollTime(ablink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(bclink);

        BridgeMacLink mac1 = new BridgeMacLink();
        mac1.setNode(nodeA);
        mac1.setBridgePort(topology.portA);
        mac1.setMacAddress(topology.mac1);
        mac1.setBridgeMacLinkLastPollTime(mac1.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac1);

        BridgeMacLink mac2 = new BridgeMacLink();
        mac2.setNode(nodeB);
        mac2.setBridgePort(topology.portB);
        mac2.setMacAddress(topology.mac2);
        mac2.setBridgeMacLinkLastPollTime(mac2.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac2);

        BridgeMacLink mac3 = new BridgeMacLink();
        mac3.setNode(nodeC);
        mac3.setBridgePort(topology.portC);
        mac3.setMacAddress(topology.mac3);
        mac3.setBridgeMacLinkLastPollTime(mac3.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(mac3);

        m_bridgeMacLinkDao.flush();
        m_bridgeBridgeLinkDao.flush();
        assertEquals(3, m_bridgeMacLinkDao.countAll());
        assertEquals(2, m_bridgeBridgeLinkDao.countAll());
        
        assertNotNull(m_bridgeTopologyDao);
        m_linkd.getQueryManager().loadBridgeTopology();

        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertEquals(nodeAbd, nodeBbd);
        assertEquals(nodeAbd, nodeCbd);
        assertNull(nodeAbd.getRootBridgeId());
        assertTrue(nodeAbd.containBridgeId(nodeA.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeB.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeC.getId()));
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        assertNotNull(nodeAbd.getRootBridgeId());
        topology.check(nodeAbd);

        assertTrue(m_linkd.scheduleNodeCollection(nodeA.getId()));
        m_linkd.scheduleBridgeTopologyDiscovery(nodeA.getId());
        
        m_linkd.deleteNode(nodeA.getId());
        assertEquals(1, m_bridgeTopologyDao.getAll().size());
        
        BroadcastDomain domain = m_bridgeTopologyDao.getAll().iterator().next();
        topology.checkBC(domain);
        
    }


}
