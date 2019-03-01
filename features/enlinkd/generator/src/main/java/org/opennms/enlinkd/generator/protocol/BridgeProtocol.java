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
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dBaseType;
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

        context.currentProgress("Version 1"); // TODO: delete later just for testing purpose

        // Call with: enlinkd:generate-topology --protocol bridgeBridge --nodes 10 --snmpinterfaces 0 -- ipinterfaces 1
        //      here is complete example of bridge topology
       // 4 nodes are bridges: nodeBridgeA, nodeBridgeB, nodeBridgeC, nodeBridgeD
       //              B
       //           --------  
       //           |      |
       //           A      C
       //         -----
       //           |
       //           D 
       // 6 nodes are hosts: nodeHostE, nodeHostF, nodeHostG, nodeHostH, nodeHostI, nodeHostL 
       // generate 4 ipnettomedia without a corresponding node  
       // consider also that on port 1 of C is connected an HUB with a group of hosts connected
       // the hub has no snmp agent and therefore we are not to explore his mab forwarding table 
        
        OnmsNode nodeBridgeA =  nodes.get(0);
        OnmsNode nodeBridgeB =  nodes.get(1);
        OnmsNode nodeBridgeC =  nodes.get(2);
        OnmsNode nodeBridgeD =  nodes.get(3);

        // create bridge element
        BridgeElement bridgeA = createBridgeElement(nodeBridgeA, 1 ,"default");
        BridgeElement bridgeB = createBridgeElement(nodeBridgeB, 1 ,"default");
        BridgeElement bridgeC = createBridgeElement(nodeBridgeC, 1 ,"default");
        BridgeElement bridgeD = createBridgeElement(nodeBridgeD, 1 ,"default");

        this.context.getTopologyPersister().persist(bridgeA,bridgeB,bridgeC,bridgeD);
        
        //First persist bridge topology
        // nodeBridgeB:port24 connected to nodeBridgeA port4
        // save snmpinterface and bridgebridgelink
        context.getTopologyPersister().persist(createSnmpInterface(4, nodeBridgeA), 
                                               createSnmpInterface(24, nodeBridgeB));
        context.getTopologyPersister().persist(createBridgeBridgeLink(nodeBridgeA, nodeBridgeB, 4, 24,1));

        // nodeBridgeB:port23 connected to nodeBridgeC port3
        // save snmpinterface and bridgebridgelink
        context.getTopologyPersister().persist(createSnmpInterface(23, nodeBridgeB), createSnmpInterface(3, nodeBridgeC));
        context.getTopologyPersister().persist(createBridgeBridgeLink(nodeBridgeC, nodeBridgeB, 3, 23,1));

        // nodeBridgeD:port1 connected to nodeBridgeA port11
        // save snmpinterface and bridgebridgelink
        context.getTopologyPersister().persist(createSnmpInterface(1, nodeBridgeD), createSnmpInterface(11, nodeBridgeA));
        context.getTopologyPersister().persist(createBridgeBridgeLink(nodeBridgeD, nodeBridgeA, 1, 11,1));
        
        //generate host topology
        // here ip addresses are needed

        // nodeHostE port 1 is connected on port 5 of nodeBridgeA
        // ipinterface -> nodeHostE - 192.168.0.11
        OnmsNode nodeHostE =  nodes.get(4);
        String macE=macGenerator.next();
        OnmsSnmpInterface snmpInterfaceE = createSnmpInterface(1, nodeHostE);
        context.getTopologyPersister().persist(createSnmpInterface(5, nodeBridgeA),snmpInterfaceE);
        OnmsIpInterface ipInterfaceE = createIpInterface(snmpInterfaceE, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceE);
        context.getTopologyPersister().persist(
           createIpNetToMedia(nodeHostE, snmpInterfaceE.getIfIndex(), snmpInterfaceE.getIfName(),macE, ipInterfaceE.getIpAddress(), nodeBridgeA)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeA, 5, 1, macE));
        
        // nodeHostF port 1 is connected on port 5 of nodeBridgeA
        // ipinterface -> nodeHostF - 192.168.0.12
        OnmsNode nodeHostF =  nodes.get(5);
        String macF=macGenerator.next();
        OnmsSnmpInterface snmpInterfaceF = createSnmpInterface(1, nodeHostF);
        context.getTopologyPersister().persist(createSnmpInterface(5, nodeBridgeA),snmpInterfaceF);
        OnmsIpInterface ipInterfaceF = createIpInterface(snmpInterfaceF, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceF);
        context.getTopologyPersister().persist(
           createIpNetToMedia(nodeHostF, snmpInterfaceF.getIfIndex(), snmpInterfaceF.getIfName(),macF, ipInterfaceF.getIpAddress(), nodeBridgeA)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeA, 5, 1, macF));
        

        // nodeHostG port 10 is connected on port 10 of nodeBridgeC
        // ipinterface -> nodeHostF - 192.168.0.13
        OnmsNode nodeHostG =  nodes.get(6);
        String macG=macGenerator.next();
        OnmsSnmpInterface snmpInterfaceG = createSnmpInterface(10, nodeHostG);
        context.getTopologyPersister().persist(createSnmpInterface(10, nodeBridgeC),snmpInterfaceG);
        OnmsIpInterface ipInterfaceG = createIpInterface(snmpInterfaceG, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceG);
        context.getTopologyPersister().persist(
           createIpNetToMedia(nodeHostG, snmpInterfaceG.getIfIndex(), snmpInterfaceG.getIfName(),macG, ipInterfaceG.getIpAddress(), nodeBridgeA)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeC, 10, 1, macG));

        // nodeHostH port 11 is connected on port 11 of nodeBridgeD
        // ipinterface -> nodeHostH - 192.168.0.14
        OnmsNode nodeHostH =  nodes.get(7);
        String macH=macGenerator.next();
        OnmsSnmpInterface snmpInterfaceH = createSnmpInterface(11, nodeHostH);
        context.getTopologyPersister().persist(createSnmpInterface(11, nodeBridgeD),snmpInterfaceH);
        OnmsIpInterface ipInterfaceH = createIpInterface(snmpInterfaceH, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceH);
        context.getTopologyPersister().persist(
           createIpNetToMedia(nodeHostH, snmpInterfaceH.getIfIndex(), snmpInterfaceH.getIfName(),macH, ipInterfaceH.getIpAddress(), nodeBridgeA)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeD, 11, 1, macH));
 
        // nodeHostI  is connected on port 12 of nodeBridgeD
        // ipinterface -> nodeHostI - 192.168.0.15
        OnmsNode nodeHostI =  nodes.get(8);
        String macI=macGenerator.next();
        context.getTopologyPersister().persist(createSnmpInterface(12, nodeBridgeD));
        OnmsIpInterface ipInterfaceI = createIpInterface(null, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceI);
        context.getTopologyPersister().persist(
           createIpNetToMedia(nodeHostI, null, null,macI, ipInterfaceI.getIpAddress(), nodeBridgeA)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeD, 12, 1, macI));

        // nodeHostL  is connected on port 13 of nodeBridgeD
        // ipinterface -> nodeHostI - 192.168.0.16
        OnmsNode nodeHostL =  nodes.get(8);
        String macL=macGenerator.next();
        context.getTopologyPersister().persist(createSnmpInterface(13, nodeBridgeD));
        OnmsIpInterface ipInterfaceL = createIpInterface(null, inetGenerator.next());
        context.getTopologyPersister().persist(ipInterfaceL);
        context.getTopologyPersister().persist(
           createIpNetToMedia(nodeHostL, null, null,macL, ipInterfaceL.getIpAddress(), nodeBridgeA)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeD, 13, 1, macL));

        // adding some mac addresses and ip on a cloud on port 23 bridge B ---
        for (int i=0; i<5;i++) {
            String nextMac = macGenerator.next();
            context.getTopologyPersister().persist(
                   createIpNetToMedia(null, null, null,nextMac, inetGenerator.next(), nodeBridgeA)
                    );
            context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeB, 23, 1, nextMac));
        }
        
        // you can also have mac addresses without an ip address associated
        for (int i=0; i<10;i++) {
            context.getTopologyPersister().persist(createBridgeMacLink(nodeBridgeB, 23, 1, macGenerator.next()));
        }
    }

    private List<BridgeElement> createBridgeElements(OnmsNode ... nodes) {
        List<BridgeElement> elements = new ArrayList<>();
        for (OnmsNode node: nodes) {
            elements.add(createBridgeElement(node, 1, "default"));
        }
        return elements;
    }
    
    private List<BridgeElement> createBridgeElements(List<OnmsNode> nodes){
        List<BridgeElement> elements = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks()/2; i++) {
            OnmsNode node = nodes.get(i);
            elements.add(createBridgeElement(node,1,"default"));
        }
        return elements;
    }
    
    private BridgeElement createBridgeElement(OnmsNode node, Integer vlanid, String vlanname) {
        BridgeElement bridge = new BridgeElement();
        bridge.setNode(node);
        bridge.setBaseBridgeAddress(macGenerator.next());
        bridge.setBaseType(BridgeDot1dBaseType.DOT1DBASETYPE_TRANSPARENT_ONLY);
        bridge.setBridgeNodeLastPollTime(new Date());
        bridge.setBaseNumPorts(topologySettings.getAmountLinks());
        bridge.setVlan(vlanid);
        bridge.setVlanname(vlanname);
        return bridge;
    }
    
    private List<BridgeBridgeLink> createBridgeBridgeLinks(List<OnmsNode> nodes, Integer vlanid) {
        // Here we shold use a different strategy because we need to generate a tree
        PairGenerator<OnmsNode> pairs = createPairGenerator(nodes);
        List<BridgeBridgeLink> links = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks()/2; i++) {

            // We create 2 links that reference each other, see also BridgeTopologyServiceImpl.getAllPersisted()
            Pair<OnmsNode, OnmsNode> pair = pairs.next();
            OnmsNode sourceNode = pair.getLeft();
            OnmsNode targetNode = pair.getRight();

            BridgeBridgeLink sourceLink = createBridgeBridgeLink(sourceNode, targetNode, 1 , 2,1);
            links.add(sourceLink);

//            BridgeBridgeLink targetLink = createBridgeBridgeLink(targetNode, sourceNode, 2, 1,1);
//           links.add(targetLink);
            context.currentProgress(String.format("Linked node %s with node %s", sourceNode.getLabel(), targetNode.getLabel()));
        }
        return links;
    }


    private BridgeBridgeLink createBridgeBridgeLink(OnmsNode node, OnmsNode designatedNode, int port, int designatedPort, Integer vlanid) {
        BridgeBridgeLink link = new BridgeBridgeLink();
        link.setBridgePortIfIndex(port);
        link.setBridgePort(port);
        link.setVlan(1);
        link.setNode(node);
        link.setDesignatedNode(designatedNode);
        link.setDesignatedPort(designatedPort);
        link.setDesignatedPortIfIndex(designatedPort);
        link.setDesignatedVlan(vlanid);
        link.setBridgeBridgeLinkLastPollTime(new Date());
        return link;
    }

    private BridgeMacLink createBridgeMacLink(OnmsNode bridge, Integer bridgePort, Integer vlan, String mac){
        BridgeMacLink bridgeMacLink = new BridgeMacLink();
        bridgeMacLink.setNode(bridge);
        bridgeMacLink.setBridgePort(bridgePort);
        bridgeMacLink.setBridgePortIfIndex(bridgePort);
        bridgeMacLink.setLinkType(BridgeMacLink.BridgeMacLinkType.BRIDGE_LINK);
        bridgeMacLink.setMacAddress(mac);
        bridgeMacLink.setVlan(vlan);
        bridgeMacLink.setBridgeMacLinkLastPollTime(new Date());
        return bridgeMacLink;
    }

    private IpNetToMedia createIpNetToMedia(OnmsNode node, Integer ifindex, String port, String mac, InetAddress inetAddress, OnmsNode sourceNode){
        IpNetToMedia ipNetToMedia = new IpNetToMedia();
        ipNetToMedia.setCreateTime(new Date());
        ipNetToMedia.setIfIndex(ifindex);
        ipNetToMedia.setLastPollTime(new Date());
        ipNetToMedia.setIpNetToMediaType(IpNetToMedia.IpNetToMediaType.IPNETTOMEDIA_TYPE_DYNAMIC);
        ipNetToMedia.setNetAddress(inetAddress);
        ipNetToMedia.setPhysAddress(mac);
        ipNetToMedia.setNode(node);
        ipNetToMedia.setPort(port);
        ipNetToMedia.setSourceIfIndex(0);
        ipNetToMedia.setSourceNode(sourceNode);
        return ipNetToMedia;
    }
}
