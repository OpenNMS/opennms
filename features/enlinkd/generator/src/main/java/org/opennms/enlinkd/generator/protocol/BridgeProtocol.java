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

        context.currentProgress("Version 7"); // TODO: delete later just for testing purpose

        // Call with: enlinkd:generate-topology --protocol bridgeBridge --nodes 10 --snmpinterfaces 0 --ipinterfaces 0
        //      here is complete example of bridge topology
       // 4 nodes are bridges: nodeBridgeA, nodeBridgeB, nodeBridgeC, nodeBridgeD
       //            bridge2
       //           --------  
       //           |      |
       //        bridge1   bridge3
       //         -----
       //           |
       //        bridge4
       // 6 nodes are hosts: host5, host6, host7, host8, host9, host10 
       // generate 4 ipnettomedia without a corresponding node  
       // consider also that on port 1 of C is connected an HUB with a group of hosts connected
       // the hub has no snmp agent and therefore we are not to explore his mab forwarding table 
       // we also follow the convention to start the port countin from the if of the node
       // following an integer: so port11 -> is the first generated port of node with id 1
       //                          port12 -> is the second generated port of node with id 1 
       //                          port21 -> is the first generated port of node with id 2 
       //                          port53 -> is the third generated port of node with id 5 
        
        // this is the vlan id for all the bridgebridgelinks and bridgemaclinks...
        int vlanid = 1;
        OnmsNode bridge1 =  nodes.get(0);
        OnmsNode bridge2 =  nodes.get(1);
        OnmsNode bridge3 =  nodes.get(2);
        OnmsNode bridge4 =  nodes.get(3);

        // save bridge element
        this.context.getTopologyPersister().persist(
                    createBridgeElement(bridge1, vlanid ,"default"),
                    createBridgeElement(bridge2, vlanid ,"default"),
                    createBridgeElement(bridge3, vlanid ,"default"),
                    createBridgeElement(bridge4, vlanid ,"default")                
                );
        
        //First persist bridge topology
        // bridge1:port11 connected to bridge2:port21
        // generate and save snmpinterface and bridgebridgelink
        int bridge1portCounter = 11;
        int bridge2portCounter = 21;
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge1portCounter, bridge1), 
                   createSnmpInterface(bridge2portCounter, bridge2)
               );
        context.getTopologyPersister().persist(
               createBridgeBridgeLink(bridge1, bridge2, bridge1portCounter, bridge1portCounter,vlanid));
        bridge1portCounter++;
        bridge2portCounter++;

        int bridge3portCounter=31;
        // bridge3:port31 connected to bridge2:port22
        // save snmpinterface and bridgebridgelink
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge1portCounter, bridge2), 
                   createSnmpInterface(bridge2portCounter, bridge3)
               );
        context.getTopologyPersister().persist(
                   createBridgeBridgeLink(bridge3, bridge2, bridge2portCounter, bridge1portCounter,vlanid)
               );
        bridge2portCounter++;
        bridge1portCounter++;

        int bridge4portCounter=41;
        // bridge4:port41 connected to bridge1:port12
        // save snmpinterface and bridgebridgelink
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge4portCounter, bridge4), 
                   createSnmpInterface(bridge1portCounter, bridge1)
               );
        context.getTopologyPersister().persist(
                   createBridgeBridgeLink(bridge4, bridge1, bridge4portCounter, bridge1portCounter,vlanid)
               );
        bridge4portCounter++;
        bridge1portCounter++;
        
        //generate host topology
        // here ip addresses are needed

        // host5:port 51 connected to bridge1:port13
        OnmsNode host5 =  nodes.get(4);
        int host5portCounter = 51;
        String mac5=macGenerator.next();
        InetAddress ip5= inetGenerator.next();
        OnmsSnmpInterface snmpInterface51 = createSnmpInterface(host5portCounter, host5);
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge1portCounter, bridge1),
                   snmpInterface51
               );
        OnmsIpInterface ipInterface51 = createIpInterface(snmpInterface51, ip5);
        context.getTopologyPersister().persist(ipInterface51);
        context.getTopologyPersister().persist(
                   createIpNetToMedia(host5, host5portCounter, mac5, ip5, bridge1)
                );
        context.getTopologyPersister().persist(
                   createBridgeMacLink(bridge1, bridge1portCounter, vlanid, mac5)
               );
        bridge1portCounter++;
        host5portCounter++;
        
        // host6:port61 connected ton bridge1:port14
        OnmsNode host6 =  nodes.get(5);
        int host6portCounter = 61;
        String mac6=macGenerator.next();
        InetAddress ip6= inetGenerator.next();
        OnmsSnmpInterface snmpInterface61 = createSnmpInterface(host6portCounter, host6);
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge1portCounter, bridge1),
                   snmpInterface61
               );
        OnmsIpInterface ipInterface61 = createIpInterface(snmpInterface61, ip6);
        context.getTopologyPersister().persist(ipInterface61);
        context.getTopologyPersister().persist(
                   createIpNetToMedia(host6, host6portCounter,mac6, ip6, bridge1)
                );
        context.getTopologyPersister().persist(
                   createBridgeMacLink(bridge1, bridge1portCounter, vlanid, mac6)
               );
        bridge1portCounter++;
        host6portCounter++;

        // host7:port71 connected bridge3:port32
        OnmsNode host7 =  nodes.get(6);
        int host7portCounter = 71;
        String mac7=macGenerator.next();
        InetAddress ip7= inetGenerator.next();
        OnmsSnmpInterface snmpInterface71 = createSnmpInterface(host7portCounter, host7);
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge3portCounter, bridge3),
                   snmpInterface71
               );
        OnmsIpInterface ipInterface7 = createIpInterface(snmpInterface71, ip7);
        context.getTopologyPersister().persist(ipInterface7);
        context.getTopologyPersister().persist(
                   createIpNetToMedia(host7, host7portCounter,mac7, ip7, bridge1)
                );
        context.getTopologyPersister().persist(
                   createBridgeMacLink(bridge3, bridge3portCounter, vlanid, mac7)
               );
        bridge3portCounter++;
        host7portCounter++;

        // host8:port81 connected bridge4:port42
        OnmsNode host8 =  nodes.get(7);
        int host8portCounter = 81;
        String mac8=macGenerator.next();
        InetAddress ip8= inetGenerator.next();
        OnmsSnmpInterface snmpInterface81 = createSnmpInterface(host8portCounter, host8);
        context.getTopologyPersister().persist(
                   createSnmpInterface(bridge4portCounter, bridge4),
                   snmpInterface81
               );
        OnmsIpInterface ipInterface8 = createIpInterface(snmpInterface81, ip8);
        context.getTopologyPersister().persist(ipInterface8);
        context.getTopologyPersister().persist(
                   createIpNetToMedia(host8, host8portCounter,mac8, ip8, bridge1)
                );
        context.getTopologyPersister().persist(
                   createBridgeMacLink(bridge4, bridge4portCounter, vlanid, mac8)
               );
        bridge4portCounter++;
        host8portCounter++;
 
        // host9:with-no-snmp connected bridge4:port43
        OnmsNode host9 =  nodes.get(8);
        String mac9=macGenerator.next();
        InetAddress ip9= inetGenerator.next();
        context.getTopologyPersister().persist(createSnmpInterface(bridge4portCounter, bridge4));
        OnmsIpInterface ipInterface9 = createIpInterface(null, ip9);
        ipInterface9.setNode(host9);
        context.getTopologyPersister().persist(ipInterface9);
        context.getTopologyPersister().persist(
                   createIpNetToMedia(host9, -1,mac9, ip9, bridge1)
                );
        context.getTopologyPersister().persist(
                   createBridgeMacLink(bridge4, bridge4portCounter, vlanid, mac9)
               );
        bridge4portCounter++;

        // host9:with-no-snmp connected bridge4:port43
        OnmsNode host10 =  nodes.get(9);
        String mac10=macGenerator.next();
        InetAddress ip10= inetGenerator.next();
        context.getTopologyPersister().persist(createSnmpInterface(bridge4portCounter, bridge4));
        OnmsIpInterface ipInterface10 = createIpInterface(null,ip10);
        ipInterface10.setNode(host10);
        context.getTopologyPersister().persist(ipInterface10);
        context.getTopologyPersister().persist(
                   createIpNetToMedia(host10, -1, mac10, ip10, bridge1)
                );
        context.getTopologyPersister().persist(createBridgeMacLink(bridge4, bridge4portCounter, vlanid, mac10));
        bridge4portCounter++;
        
        
        // adding 2 mac addresses and ip on a cloud bridge3:port31 connected to bridge2:port22
        for (int i=0; i<2;i++) {
            String nextMac = macGenerator.next();
            context.getTopologyPersister().persist(
                   createIpNetToMedia(null, null,nextMac, inetGenerator.next(), bridge1)
                    );
            context.getTopologyPersister().persist(
                       createBridgeMacLink(bridge2, 22, vlanid, nextMac));
        }
        
        // adding 2 mac addresses without an ip address associated on a cloud bridge3:port31 connected to bridge2:port22
        for (int i=0; i<2;i++) {
            context.getTopologyPersister().persist(createBridgeMacLink(bridge2, 22, vlanid, macGenerator.next()));
        }

        //persisting bridge3 port 33
        context.getTopologyPersister().persist(
               createSnmpInterface(bridge3portCounter, bridge3)
           );        
        // adding 2 mac addresses and ip on a cloud bridge3:port33 
        for (int i=0; i<2;i++) {
            String nextMac = macGenerator.next();
            context.getTopologyPersister().persist(
                   createIpNetToMedia(null, null,nextMac, inetGenerator.next(), bridge1)
                    );
            context.getTopologyPersister().persist(
                       createBridgeMacLink(bridge3, bridge3portCounter, vlanid, nextMac));
        }
        
        // adding 3 mac addresses without an ip address associated on a cloud bridge3:port33 
        for (int i=0; i<3;i++) {
            context.getTopologyPersister().persist(createBridgeMacLink(bridge3, bridge3portCounter, vlanid, macGenerator.next()));
        }
        bridge3portCounter++;        
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
        bridge.setBaseBridgeAddress(macGenerator.next().replace(":", ""));
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

    private IpNetToMedia createIpNetToMedia(OnmsNode node, Integer ifindex, String mac, InetAddress inetAddress, OnmsNode sourceNode){
        IpNetToMedia ipNetToMedia = new IpNetToMedia();
        ipNetToMedia.setCreateTime(new Date());
        if (node != null && ifindex == null ) {
            ipNetToMedia.setIfIndex(-1);
        } else {
            ipNetToMedia.setIfIndex(ifindex);
        }
        ipNetToMedia.setLastPollTime(new Date());
        ipNetToMedia.setIpNetToMediaType(IpNetToMedia.IpNetToMediaType.IPNETTOMEDIA_TYPE_DYNAMIC);
        ipNetToMedia.setNetAddress(inetAddress);
        ipNetToMedia.setPhysAddress(mac);
        ipNetToMedia.setNode(node);
        ipNetToMedia.setSourceIfIndex(0);
        ipNetToMedia.setSourceNode(sourceNode);
        return ipNetToMedia;
    }
}
