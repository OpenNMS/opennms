/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator.protocol;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.topology.PairGenerator;
import org.opennms.enlinkd.generator.util.InetAddressGenerator;
import org.opennms.enlinkd.generator.util.MacAddressGenerator;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;


// BroadcastDomain:
// BridgeTopologyServiceImpl.findAll()
// + BridgeTopologyServiceImpl.getAllPersisted()
// +- BridgeBridgeLinkDao.findAll()
// +- BridgePort bridgeport = BridgePort.getFromBridgeBridgeLink(link);
// +- BridgePort getFromDesignatedBridgeBridgeLink(link);
// +- SharedSegment.create(link)
// +- m_bridgeMacLinkDao.findAll()
//
// It seems that you need also to generate ipnettomedia too.
// In fact a bridgebridgelink is a connection between two bridges and a bridge mac link is a connection with a node.
// but you also need to let the node binded to the mac address and then you need an ip address for the mac.
public class BridgeProtocol extends Protocol<BridgeBridgeLink> {

    private final MacAddressGenerator macGenerator = new MacAddressGenerator();
    private final InetAddressGenerator inetGenerator = new InetAddressGenerator();

    public BridgeProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    protected TopologyGenerator.Protocol getProtocol() {
        return TopologyGenerator.Protocol.bridgeBridge;
    }

    @Override
    protected void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {

        // Call with: enlinkd:generate-topology --protocol bridgeBridge --nodes 3 --snmpinterfaces 0 -- ipinterfaces 0

//        NodeA is connected to port 24 of NodeB
//        NodeC is connected to port 23 of NodeB
//        you have tio generate three nodes nodeA(id=1) nodeB(id=2) nodeC(id=3)

        OnmsNode nodeA =  nodes.get(0);
        OnmsNode nodeB =  nodes.get(1);
        OnmsNode nodeC =  nodes.get(2);

        // snmpinterfaces -> nodeB ----port 1,24 ---specified ifindex name ands so on
        OnmsSnmpInterface snmpInterfaceA = createSnmpInterface(1, nodeA);
        OnmsSnmpInterface snmpInterfaceB = createSnmpInterface(2, nodeB);
        OnmsSnmpInterface snmpInterfaceC = createSnmpInterface(3, nodeC);
        context.getTopologyPersister().persist(snmpInterfaceA, snmpInterfaceB, snmpInterfaceC);

        // ipinterface -> nodeA - 192.168.0.1
        // ipinterface -> nodeB - 192.168.0.2
        // ipinterface -> nodeC - 192.168.0.3
        OnmsIpInterface ipInterfaceA = createIpInterface(snmpInterfaceA, inetGenerator.next());
        OnmsIpInterface ipInterfaceB = createIpInterface(snmpInterfaceB, inetGenerator.next());
        OnmsIpInterface ipInterfaceC = createIpInterface(snmpInterfaceC, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceA, ipInterfaceB, ipInterfaceC);

        // ipnettomedia -> nodeid=1 0123456789a 192.168.0.1
        // ipnettomedia -> nodeid=1 0123456789b 192.168.0.2
        // ipnettomedia -> nodeid=3 0123456789c 192.168.0.3
        IpNetToMedia ipNetToMediaA = createIpNetToMedia(nodeA, "0123456789a", ipInterfaceA.getIpAddress(), nodeB);
        IpNetToMedia ipNetToMediaB = createIpNetToMedia(nodeA, "0123456789b", ipInterfaceB.getIpAddress(), nodeB);
        IpNetToMedia ipNetToMediaC = createIpNetToMedia(nodeC, "0123456789c", ipInterfaceC.getIpAddress(), nodeB);
        context.getTopologyPersister().persist(ipNetToMediaA, ipNetToMediaB, ipNetToMediaC);

        List<BridgeBridgeLink> bridgeBridgeLinks = createBridgeBridgeLinks(nodes);
        this.context.getTopologyPersister().persist(bridgeBridgeLinks);
    }

    private List<BridgeBridgeLink> createBridgeBridgeLinks(List<OnmsNode> nodes) {
        PairGenerator<OnmsNode> pairs = createPairGenerator(nodes);
        List<BridgeBridgeLink> links = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks()/2; i++) {

            // We create 2 links that reference each other, see also BridgeTopologyServiceImpl.getAllPersisted()
            Pair<OnmsNode, OnmsNode> pair = pairs.next();
            OnmsNode sourceNode = pair.getLeft();
            OnmsNode targetNode = pair.getRight();

            BridgeBridgeLink sourceLink = createBridgeBridgeLink(sourceNode, targetNode, 1 , 2);
            links.add(sourceLink);

            BridgeBridgeLink targetLink = createBridgeBridgeLink(targetNode, sourceNode, 2, 1);
            links.add(targetLink);
            context.currentProgress(String.format("Linked node %s with node %s", sourceNode.getLabel(), targetNode.getLabel()));
        }
        return links;
    }


    private BridgeBridgeLink createBridgeBridgeLink(OnmsNode node, OnmsNode designatedNode, int port, int designatedPort) {
        BridgeBridgeLink link = new BridgeBridgeLink();
        link.setBridgeBridgeLinkLastPollTime(new Date());
        link.setBridgePortIfIndex(3);
        link.setBridgePort(port);
        link.setVlan(23);
        link.setBridgePortIfName("xx");
        link.setNode(node);
        link.setBridgeBridgeLinkCreateTime(new Date());
        link.setDesignatedNode(designatedNode);
        link.setDesignatedPort(designatedPort);
        link.setDesignatedPortIfIndex(3);
        link.setDesignatedVlan(23);
        link.setDesignatedPortIfName("xx");
        return link;
    }

    private BridgeMacLink createBridgeMacLink(){
        BridgeMacLink bridgeMacLink = new BridgeMacLink();
        bridgeMacLink.setBridgeMacLinkCreateTime(new Date());
        bridgeMacLink.setBridgeMacLinkLastPollTime(new Date());
        bridgeMacLink.setBridgePort(1);
        bridgeMacLink.setBridgePortIfIndex(2);
        bridgeMacLink.setBridgePortIfName("bridgePortIfName");
        bridgeMacLink.setId(6);
        bridgeMacLink.setLinkType(BridgeMacLink.BridgeMacLinkType.BRIDGE_LINK);
        bridgeMacLink.setMacAddress(macGenerator.next());
        return bridgeMacLink;
    }

    private IpNetToMedia createIpNetToMedia(OnmsNode node, String port, InetAddress inetAddress, OnmsNode sourceNode){
        IpNetToMedia ipNetToMedia = new IpNetToMedia();
        ipNetToMedia.setCreateTime(new Date());
        ipNetToMedia.setIfIndex(0);
        ipNetToMedia.setLastPollTime(new Date());
        ipNetToMedia.setIpNetToMediaType(IpNetToMedia.IpNetToMediaType.IPNETTOMEDIA_TYPE_STATIC);
        ipNetToMedia.setNetAddress(inetAddress);
        ipNetToMedia.setPhysAddress("physAddress");
        ipNetToMedia.setNode(node);
        ipNetToMedia.setPort(port);
        ipNetToMedia.setSourceIfIndex(0);
        ipNetToMedia.setSourceNode(sourceNode);
        return ipNetToMedia;
    }
}
