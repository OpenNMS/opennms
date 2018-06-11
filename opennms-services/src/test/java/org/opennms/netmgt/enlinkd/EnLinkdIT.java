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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_SYSOID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_SYSOID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SWITCH1_SYSOID;

import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
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
        
        m_linkd.getQueryManager().updateBft(topology.nodeAId, topology.bftA);
        
        List<BridgeMacLink> links = m_linkd.getQueryManager().useBridgeTopologyUpdateBFT(topology.nodeAId);
        assertEquals(topology.bftA.size(), links.size());
        for (BridgeMacLink link: links) {
            assertEquals(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED, link.getBridgeDot1qTpFdbStatus());
            System.err.println(link.printTopology());
        }
    }

    /*
     * 1400=
     *      [1544, 1534]
     *             1534=
     *                   [1364, 405, 1310]}
     *                    1364=
     *                          [2965, 5022]
     */
    @Test 
    public void testLoadFourLevelTopology() {
        final OnmsMonitoringLocation location = new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
        OnmsNode lnode1400= new OnmsNode();
        lnode1400.setId(1400);
        lnode1400.setForeignSource("linkd");
        lnode1400.setForeignId("lnode1400");
        lnode1400.setLabel("lnode1400");
        lnode1400.setLocation(location);

        OnmsNode lnode1544= new OnmsNode();
        lnode1544.setId(1544);
        lnode1544.setForeignSource("linkd");
        lnode1544.setForeignId("lnode1544");
        lnode1544.setLabel("lnode1544");
        lnode1544.setLocation(location);
        
        OnmsNode lnode1534= new OnmsNode();
        lnode1534.setId(1534);
        lnode1534.setForeignSource("linkd");
        lnode1534.setForeignId("lnode1534");
        lnode1534.setLabel("lnode1534");
        lnode1534.setLocation(location);

        OnmsNode lnode1364= new OnmsNode();
        lnode1364.setId(1364);
        lnode1364.setForeignSource("linkd");
        lnode1364.setForeignId("lnode1364");
        lnode1364.setLabel("lnode1364");
        lnode1364.setLocation(location);

        OnmsNode lnode1310= new OnmsNode();
        lnode1310.setId(1310);
        lnode1310.setForeignSource("linkd");
        lnode1310.setForeignId("lnode1310");
        lnode1310.setLabel("lnode1310");
        lnode1310.setLocation(location);

        OnmsNode lnode405= new OnmsNode();
        lnode405.setId(405);
        lnode405.setForeignSource("linkd");
        lnode405.setForeignId("lnode405");
        lnode405.setLabel("lnode405");
        lnode405.setLocation(location);
        
        OnmsNode lnode2965= new OnmsNode();
        lnode2965.setId(2965);
        lnode2965.setForeignSource("linkd");
        lnode2965.setForeignId("lnode2965");
        lnode2965.setLabel("lnode2965");
        lnode2965.setLocation(location);

        OnmsNode lnode5022= new OnmsNode();
        lnode5022.setId(5022);
        lnode5022.setForeignSource("linkd");
        lnode5022.setForeignId("lnode5022");
        lnode5022.setLabel("lnode5022");
        lnode5022.setLocation(location);
        
        m_nodeDao.save(lnode1400);
        m_nodeDao.save(lnode1544);
        m_nodeDao.save(lnode1534);
        m_nodeDao.save(lnode1364);
        m_nodeDao.save(lnode1310);
        m_nodeDao.save(lnode405);
        m_nodeDao.save(lnode2965);
        m_nodeDao.save(lnode5022);

        OnmsNode node1400 = m_nodeDao.findByForeignId("linkd", "lnode1400");
        OnmsNode node1544 = m_nodeDao.findByForeignId("linkd", "lnode1544");
        OnmsNode node1534 = m_nodeDao.findByForeignId("linkd", "lnode1534");
        OnmsNode node1364 = m_nodeDao.findByForeignId("linkd", "lnode1364");
        OnmsNode node1310 = m_nodeDao.findByForeignId("linkd", "lnode1310");
        OnmsNode node405 = m_nodeDao.findByForeignId("linkd", "lnode405");
        OnmsNode node2965 = m_nodeDao.findByForeignId("linkd", "lnode2965");
        OnmsNode node5022 = m_nodeDao.findByForeignId("linkd", "lnode5022");
        

        BridgeBridgeLink link7 = new BridgeBridgeLink();
        link7.setNode(node5022);
        link7.setBridgePort(4);
        link7.setDesignatedNode(node2965);
        link7.setDesignatedPort(5);
        link7.setBridgeBridgeLinkLastPollTime(link7.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link7);
        m_bridgeBridgeLinkDao.flush();

        BridgeBridgeLink link1 = new BridgeBridgeLink();
        link1.setNode(node1544);
        link1.setBridgePort(15);
        link1.setDesignatedNode(node1400);
        link1.setDesignatedPort(1);
        link1.setBridgeBridgeLinkLastPollTime(link1.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link1);
        m_bridgeBridgeLinkDao.flush();

        BridgeBridgeLink link3 = new BridgeBridgeLink();
        link3.setNode(node1310);
        link3.setBridgePort(1);
        link3.setDesignatedNode(node1534);
        link3.setDesignatedPort(1);
        link3.setBridgeBridgeLinkLastPollTime(link3.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link3);
        m_bridgeBridgeLinkDao.flush();

        BridgeBridgeLink link4 = new BridgeBridgeLink();
        link4.setNode(node1364);
        link4.setBridgePort(24);
        link4.setDesignatedNode(node1534);
        link4.setDesignatedPort(4);
        link4.setBridgeBridgeLinkLastPollTime(link4.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link4);
        m_bridgeBridgeLinkDao.flush();

        BridgeBridgeLink link2 = new BridgeBridgeLink();
        link2.setNode(node1534);
        link2.setBridgePort(11);
        link2.setDesignatedNode(node1400);
        link2.setDesignatedPort(11);
        link2.setBridgeBridgeLinkLastPollTime(link2.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link2);
        m_bridgeBridgeLinkDao.flush();


        BridgeBridgeLink link6 = new BridgeBridgeLink();
        link6.setNode(node2965);
        link6.setBridgePort(2);
        link6.setDesignatedNode(node1364);
        link6.setDesignatedPort(5);
        link6.setBridgeBridgeLinkLastPollTime(link6.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link6);
        m_bridgeBridgeLinkDao.flush();

        BridgeBridgeLink link5 = new BridgeBridgeLink();
        link5.setNode(node405);
        link5.setBridgePort(1);
        link5.setDesignatedNode(node1534);
        link5.setDesignatedPort(1);
        link5.setBridgeBridgeLinkLastPollTime(link5.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(link5);
        m_bridgeBridgeLinkDao.flush();

        assertEquals(0, m_bridgeMacLinkDao.countAll());
        assertEquals(7, m_bridgeBridgeLinkDao.countAll());

        assertNotNull(m_bridgeTopologyDao);
        m_linkd.getQueryManager().loadBridgeTopology();

        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        
        BroadcastDomain fourlevelDomain = m_linkd.getQueryManager().getAllBroadcastDomains().iterator().next();
        assertEquals(8, fourlevelDomain.getBridgeNodesOnDomain().size());
        
        System.err.println(fourlevelDomain.printTopology());

    }
    
    @Test 
    public void testLoadTopology() {
        final OnmsMonitoringLocation location = new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
        ABCTopology topology = new ABCTopology();
        OnmsNode lnodeA = topology.nodeA;
        lnodeA.setForeignSource("linkd");
        lnodeA.setForeignId("nodeA");
        lnodeA.setLabel("nodeA");
        lnodeA.setLocation(location);

        OnmsNode lnodeB = topology.nodeB;
        lnodeB.setForeignSource("linkd");
        lnodeB.setForeignId("nodeB");
        lnodeB.setLabel("nodeB");
        lnodeB.setLocation(location);

        OnmsNode lnodeC = topology.nodeC;
        lnodeC.setForeignSource("linkd");
        lnodeC.setForeignId("nodeC");
        lnodeC.setLabel("nodeC");
        lnodeC.setLocation(location);

        BridgeElement elementD = new BridgeElement();
        BridgeElement elementE = new BridgeElement();
        BridgeElement elementK = new BridgeElement();

        OnmsNode lnodeD= new OnmsNode();
        lnodeD.setId(topology.nodeAId+1234);
        lnodeD.setForeignSource("linkd");
        lnodeD.setForeignId("nodeD");
        lnodeD.setLabel("nodeD");
        lnodeD.setLocation(location);
        elementD.setNode(lnodeD);
        elementD.setBaseBridgeAddress("dddddddddddd");

        OnmsNode lnodeE= new OnmsNode();
        lnodeE.setId(topology.nodeBId+1234);
        lnodeE.setForeignSource("linkd");
        lnodeE.setForeignId("nodeE");
        lnodeE.setLabel("nodeE");
        lnodeE.setLocation(location);
        elementE.setNode(lnodeE);
        elementE.setBaseBridgeAddress("eeeeeeeeeeee");

        OnmsNode lnodeK= new OnmsNode();
        lnodeK.setId(topology.nodeCId+12345);
        lnodeK.setForeignSource("linkd");
        lnodeK.setForeignId("nodeK");
        lnodeK.setLabel("nodeK");
        lnodeK.setLocation(location);
        elementK.setNode(lnodeK);
        elementK.setBaseBridgeAddress("aaaabbbbcccc");

        
        m_nodeDao.save(lnodeA);
        m_nodeDao.save(lnodeB);
        m_nodeDao.save(lnodeC);
        m_nodeDao.save(lnodeD);
        m_nodeDao.save(lnodeE);
        m_nodeDao.save(lnodeK);

        OnmsNode nodeA = m_nodeDao.findByForeignId("linkd", "nodeA");
        OnmsNode nodeB = m_nodeDao.findByForeignId("linkd", "nodeB");
        OnmsNode nodeC = m_nodeDao.findByForeignId("linkd", "nodeC");
        OnmsNode nodeD = m_nodeDao.findByForeignId("linkd", "nodeD");
        OnmsNode nodeE = m_nodeDao.findByForeignId("linkd", "nodeE");
        OnmsNode nodeK = m_nodeDao.findByForeignId("linkd", "nodeK");

        topology.nodeAId = nodeA.getId();
        topology.nodeBId = nodeB.getId();
        topology.nodeCId = nodeC.getId();

        topology.nodeA = nodeA;
        topology.nodeB = nodeB;
        topology.nodeC = nodeC;

        BridgeBridgeLink delink = new BridgeBridgeLink();
        delink.setNode(nodeD);
        delink.setBridgePort(45);
        delink.setDesignatedNode(nodeE);
        delink.setDesignatedPort(54);
        delink.setBridgeBridgeLinkLastPollTime(delink.getBridgeBridgeLinkCreateTime());
        m_bridgeBridgeLinkDao.save(delink);

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
        
        BridgeMacLink forward = new BridgeMacLink();
        forward.setNode(nodeB);
        forward.setBridgePort(topology.portBA);
        forward.setMacAddress(topology.macA);
        forward.setBridgeMacLinkLastPollTime(forward.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(forward);

        BridgeMacLink sharedB = new BridgeMacLink();
        sharedB.setNode(nodeB);
        sharedB.setBridgePort(topology.portBC);
        sharedB.setMacAddress(topology.shar);
        sharedB.setBridgeMacLinkLastPollTime(sharedB.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(sharedB);

        BridgeMacLink sharedC = new BridgeMacLink();
        sharedC.setNode(nodeC);
        sharedC.setBridgePort(topology.portCB);
        sharedC.setMacAddress(topology.shar);
        sharedC.setBridgeMacLinkLastPollTime(sharedC.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(sharedC);
        
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
        
        BridgeMacLink sharedD = new BridgeMacLink();
        sharedD.setNode(nodeD);
        sharedD.setBridgePort(45);
        sharedD.setMacAddress(topology.shar);
        sharedD.setBridgeMacLinkLastPollTime(sharedD.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(sharedD);

        BridgeMacLink sharedE = new BridgeMacLink();
        sharedE.setNode(nodeE);
        sharedE.setBridgePort(54);
        sharedE.setMacAddress(topology.shar);
        sharedE.setBridgeMacLinkLastPollTime(sharedE.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(sharedE);

        BridgeMacLink sharedK = new BridgeMacLink();
        sharedK.setNode(nodeK);
        sharedK.setBridgePort(1099);
        sharedK.setMacAddress(topology.shar);
        sharedK.setBridgeMacLinkLastPollTime(sharedK.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(sharedK);

        BridgeMacLink macK = new BridgeMacLink();
        macK.setNode(nodeK);
        macK.setBridgePort(1099);
        macK.setMacAddress("1234567800aa");
        macK.setBridgeMacLinkLastPollTime(macK.getBridgeMacLinkCreateTime());
        m_bridgeMacLinkDao.save(macK);

        m_bridgeMacLinkDao.flush();
        m_bridgeBridgeLinkDao.flush();
        assertEquals(10, m_bridgeMacLinkDao.countAll());
        assertEquals(3, m_bridgeBridgeLinkDao.countAll());
        
        assertNotNull(m_bridgeTopologyDao);
        m_linkd.getQueryManager().loadBridgeTopology();

        assertEquals(3, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertEquals(nodeAbd, nodeBbd);
        assertEquals(nodeAbd, nodeCbd);
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));

        topology.checkwithshared(nodeAbd);
        assertEquals(0, nodeAbd.getForwarders(topology.nodeAId).size());
        assertEquals(1, nodeAbd.getForwarders(topology.nodeBId).size());
        assertEquals(0, nodeAbd.getForwarders(topology.nodeCId).size());
        
        List<SharedSegment> nodeASegments = m_bridgeTopologyDao.getBridgeSharedSegments(nodeA.getId());
        assertEquals(2, nodeASegments.size());
        
        System.err.println(nodeAbd.printTopology());
        
        BroadcastDomain nodeDbd = m_linkd.getQueryManager().getBroadcastDomain(nodeD.getId().intValue());
        assertNotNull(nodeDbd);
        BroadcastDomain nodeEbd = m_linkd.getQueryManager().getBroadcastDomain(nodeE.getId().intValue());
        assertEquals(nodeDbd, nodeEbd);
        
        assertEquals(2, nodeDbd.getBridges().size());
        assertEquals(1, nodeDbd.getTopology().size());
        assertTrue(nodeDbd.containBridgeId(nodeD.getId()));
        assertTrue(nodeDbd.containBridgeId(nodeE.getId()));
        SharedSegment deSegment = nodeDbd.getTopology().iterator().next();
        assertEquals(2,deSegment.getBridgePortsOnSegment().size());
        assertEquals(1,deSegment.getMacsOnSegment().size());
        assertEquals(45,deSegment.getPortForBridge(nodeD.getId()).intValue());
        assertEquals(54,deSegment.getPortForBridge(nodeE.getId()).intValue());
        assertTrue(deSegment.containsMac(topology.shar));

        System.err.println(nodeDbd.printTopology());

        BroadcastDomain nodeKbd = m_linkd.getQueryManager().getBroadcastDomain(nodeK.getId().intValue());
        assertNotNull(nodeKbd);
        assertEquals(1, nodeKbd.getBridges().size());
        assertEquals(1, nodeKbd.getTopology().size());
        assertTrue(nodeKbd.containBridgeId(nodeK.getId()));
        SharedSegment kSegment = nodeKbd.getTopology().iterator().next();
        assertEquals(1,kSegment.getBridgePortsOnSegment().size());
        assertEquals(2,kSegment.getMacsOnSegment().size());
        assertEquals(1099,kSegment.getPortForBridge(nodeK.getId()).intValue());
        assertTrue(kSegment.containsMac(topology.shar));
        assertTrue(kSegment.containsMac("1234567800aa"));

        System.err.println(nodeKbd.printTopology());
        
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

        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        assertNotNull(nodeBbd);
        assertEquals(nodeAbd, nodeBbd);
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertNotNull(nodeCbd);
        assertEquals(nodeAbd, nodeCbd);
        assertTrue(nodeAbd.hasRootBridge());
        assertEquals(nodeAbd.getRootBridgeId().intValue(), nodeB.getId().intValue());
        assertTrue(nodeAbd.containBridgeId(nodeA.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeB.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeC.getId()));
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        assertNotNull(nodeAbd.getRootBridgeId());
        topology.check(nodeAbd);
        
        m_linkd.deleteNode(nodeC.getId());
        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        
        BroadcastDomain domain = m_linkd.getQueryManager().getAllBroadcastDomains().iterator().next();
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

        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertEquals(nodeAbd, nodeBbd);
        assertEquals(nodeAbd, nodeCbd);
        assertTrue(nodeAbd.hasRootBridge());
        assertEquals(nodeAbd.getRootBridge().getId().intValue(), nodeA.getId().intValue());
        assertTrue(nodeAbd.containBridgeId(nodeA.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeB.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeC.getId()));
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        assertNotNull(nodeAbd.getRootBridgeId());
        topology.check(nodeAbd);

        assertTrue(m_linkd.scheduleNodeCollection(nodeB.getId()));
        
        m_linkd.deleteNode(nodeB.getId());
        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        
        BroadcastDomain domain = m_linkd.getQueryManager().getAllBroadcastDomains().iterator().next();
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

        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        BroadcastDomain nodeAbd = m_linkd.getQueryManager().getBroadcastDomain(nodeA.getId().intValue());
        assertNotNull(nodeAbd);
        BroadcastDomain nodeBbd = m_linkd.getQueryManager().getBroadcastDomain(nodeB.getId().intValue());
        BroadcastDomain nodeCbd = m_linkd.getQueryManager().getBroadcastDomain(nodeC.getId().intValue());
        assertEquals(nodeAbd, nodeBbd);
        assertEquals(nodeAbd, nodeCbd);
        assertTrue(nodeAbd.hasRootBridge());
        assertEquals(nodeAbd.getRootBridge().getId().intValue(), nodeA.getId().intValue());
        assertTrue(nodeAbd.containBridgeId(nodeA.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeB.getId()));
        assertTrue(nodeAbd.containBridgeId(nodeC.getId()));
        nodeAbd.hierarchySetUp(nodeAbd.getBridge(nodeA.getId()));
        assertNotNull(nodeAbd.getRootBridgeId());
        topology.check(nodeAbd);

        assertTrue(m_linkd.scheduleNodeCollection(nodeA.getId()));
        m_linkd.scheduleBridgeTopologyDiscovery(nodeA.getId());
        
        m_linkd.deleteNode(nodeA.getId());
        assertEquals(1, m_linkd.getQueryManager().getAllBroadcastDomains().size());
        
        BroadcastDomain domain = m_linkd.getQueryManager().getAllBroadcastDomains().iterator().next();
        topology.checkBC(domain);
        
    }


}
