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
import java.util.Date;
import java.util.List;

import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.protocol.bridge.BridgeBuilder;
import org.opennms.enlinkd.generator.protocol.bridge.BridgeBuilderContext;
import org.opennms.enlinkd.generator.util.IdGenerator;
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

/**
 * Creates a topology with Bridges and Segments.
 * Call with: enlinkd:generate-topology --protocol bridge --nodes 10 --snmpinterfaces 0 --ipinterfaces 0
 * Example:
 *
 *
 *                           bridge0
 *           ------------------------------------
 *           |      |          |        |       |
 *        bridge1  bridge3  bridge4  bridge5  Macs/Ip
 *           |          |                     no node
 *        -------    --------
 *        |          |      |
 *     Segment     host8    host9
 *   -----------
 *   |         |
 * Macs/Ip  bridge2
 * no node     |
 *          Segment
 *
 * 6 nodes are hosts: host4,host5, host6, host7, host8, host9
 * generate 4 ipnettomedia without a corresponding node
 * consider also that on port 1 of C is connected an HUB with a group of hosts connected
 * the hub has no snmp agent and therefore we are not to explore his mab forwarding table
 * we also follow the convention to start the port countin from the if of the node
 * following an integer: so port11 -> is the first generated port of node with id 1
 *                          port12 -> is the second generated port of node with id 1
 *                          port21 -> is the first generated port of node with id 2
 *                          port53 -> is the third generated port of node with id 5
 */
public class BridgeProtocol extends Protocol<BridgeBridgeLink> {

    /* this is the vlan id for all the bridgebridgelinks and bridgemaclinks */
    private final static int VLAN_ID = 1;

    private final MacAddressGenerator macGenerator = new MacAddressGenerator();
    private final InetAddressGenerator inetGenerator = new InetAddressGenerator();

    public BridgeProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    protected TopologyGenerator.Protocol getProtocol() {
        return TopologyGenerator.Protocol.bridge;
    }

    @Override
    protected void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {

        OnmsNode bridge0 =  nodes.get(0);
        OnmsNode bridge1 =  nodes.get(1);
        OnmsNode bridge2 =  nodes.get(2);
        OnmsNode bridge3 =  nodes.get(3);
        OnmsNode host4   =  nodes.get(4);
        OnmsNode host5   =  nodes.get(5);
        OnmsNode host6   =  nodes.get(6);
        OnmsNode host7   =  nodes.get(7);
        OnmsNode host8   =  nodes.get(8);
        OnmsNode host9   =  nodes.get(9);

        BridgeBuilderContext context = new BridgeBuilderContext(this.context.getTopologyPersister(), this.macGenerator, this.inetGenerator);
        BridgeBuilder bridge0B = new BridgeBuilder(bridge0, 0, context);
        
        //bridge0
        //bridge0:port1 connected to up bridge1:port11
        BridgeBuilder bridge1B = bridge0B.connectToNewBridge(bridge1, 11);

        //bridge3:port31 connected to up bridge0:port2
        BridgeBuilder bridge3B = bridge0B.connectToNewBridge(bridge3, 31);

        //bridge0:port3 connected to cloud
        bridge0B.increasePortCounter();
        bridge0B.createAndPersistCloud(2, 2);

        // bridge0:port4 connected to host4:port41
        bridge0B.increasePortCounter();
        bridge0B.createAndPersistBridgeMacLink(host4, 41, bridge0);

        bridge0B.increasePortCounter();
        bridge0B.createAndPersistBridgeMacLink(host5, 51, bridge0);

        //bridge1
        //bridge2:port21 connected to bridge1:port12 with clouds
        BridgeBuilder bridge2B = bridge1B.connectToNewBridge(bridge2, 21);
        bridge1B.createAndPersistCloud(2, 2);
        
        //bridge2
        // host6 and host 7 connected to port 22 : "cloud" symbol
        bridge2B.increasePortCounter();
        bridge2B.createAndPersistBridgeMacLink(true,  host6, 61, bridge0);
        bridge2B.createAndPersistBridgeMacLink(false, host7, 71, bridge0);
        
        // bridge3
        // host8:with-no-snmp connected bridge3:port32
        bridge3B.increasePortCounter();
        bridge3B.createAndPersistBridgeMacLink(host8, null, bridge0);
        bridge3B.increasePortCounter();
        bridge3B.createAndPersistBridgeMacLink(host9, null, bridge0);
    }

}
