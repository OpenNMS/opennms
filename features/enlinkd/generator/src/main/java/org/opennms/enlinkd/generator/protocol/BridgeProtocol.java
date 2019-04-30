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

import java.util.List;

import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.protocol.bridge.BridgeBuilder;
import org.opennms.enlinkd.generator.protocol.bridge.BridgeBuilderContext;
import org.opennms.enlinkd.generator.util.InetAddressGenerator;
import org.opennms.enlinkd.generator.util.MacAddressGenerator;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Creates a topology with Bridges and Segments.
 * Call with: enlinkd:generate-topology --protocol bridge --nodes 10
 * will result in:
 *
 *                           bridge0
 *           ------------------------------------
 *           |      |          |        |       |
 *        bridge1  bridge3  bridge4  bridge5  Macs/Ip
 *           |          |               |     no node
 *        -------    --------     ...subtree...
 *        |          |      |
 *     Segment     host8    host9
 *   -----------
 *   |         |
 * Macs/Ip  bridge2
 * no node     |
 *          Segment
 *
 * If more than 10 nodes are requested then the tree repeats itself with bridge5 as the root node of the new subtree.
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
    protected void printProtocolSpecificMessage() {
        // the bridge topology is different as the other topologies since it consists of different node types -> we need
        // a different implementation of this method
        this.context.currentProgress("%nCreating %s topology with %s Nodes. Other settings are ignored since they not relevant for this topology",
                this.getProtocol(),
                topologySettings.getAmountNodes());
    }

    @Override
    protected TopologySettings adoptAndVerifySettings(TopologySettings topologySettings) {
        // make amount of nodes multiple of 10+1 since each (sub)tree needs 10 nodes:
        int amountNodes;
        if (topologySettings.getAmountNodes() < 11) {
            amountNodes = 11;
        } else {
            amountNodes = topologySettings.getAmountNodes();
            int modulo = (amountNodes -1) % 10;
            if(modulo != 0) {
                amountNodes += (10-modulo);
            }
        }

        return TopologySettings.builder()
                .amountNodes(amountNodes)
                .amountSnmpInterfaces(0)
                .amountIpInterfaces(0)
                .amountLinks(0)
                .amountElements(0)
                .build();
    }

    @Override
    protected TopologyGenerator.Protocol getProtocol() {
        return TopologyGenerator.Protocol.bridge;
    }

    @Override
    protected void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
        OnmsNode bridge0 = nodes.get(0);
        BridgeBuilderContext context = new BridgeBuilderContext(this.context.getTopologyPersister(), this.macGenerator, this.inetGenerator);
        BridgeBuilder bridge0B = new BridgeBuilder(bridge0, 0, context);
        createAndPersistProtocolSpecificEntities(nodes, bridge0B, bridge0, 0);

    }

    private void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes, BridgeBuilder root, OnmsNode gateway, int iteration) {

        int offset = iteration * 10;
        OnmsNode bridge1 = nodes.get(1 + offset);
        OnmsNode bridge2 = nodes.get(2 + offset);
        OnmsNode bridge3 = nodes.get(3 + offset);
        OnmsNode host4 = nodes.get(4 + offset);
        OnmsNode bridge5 = nodes.get(5 + offset);
        OnmsNode host6 = nodes.get(6 + offset);
        OnmsNode host7 = nodes.get(7 + offset);
        OnmsNode host8 = nodes.get(8 + offset);
        OnmsNode host9 = nodes.get(9 + offset);
        OnmsNode bridge10 = nodes.get(10 + offset);
        //bridge0
        //bridge0:port1 connected to up bridge1:port11
        BridgeBuilder bridge1B = root.connectToNewBridge(bridge1, 11);

        //bridge3:port31 connected to up bridge0:port2
        BridgeBuilder bridge3B = root.connectToNewBridge(bridge3, 31);

        //bridge0:port3 connected to cloud
        root.increasePortCounter();
        root.createAndPersistCloud(2, 2);

        // bridge0:port4 connected to host4:port41
        root.increasePortCounter();
        root.createAndPersistBridgeMacLink(host4, 41, gateway);

        // bridge5 will be the new root bridge for the next interation
        BridgeBuilder bridge5B = root.connectToNewBridge(bridge5, 51);

        //bridge1
        //bridge2:port21 connected to bridge1:port12 with clouds
        BridgeBuilder bridge2B = bridge1B.connectToNewBridge(bridge2, 21);
        bridge1B.createAndPersistCloud(2, 2);

        //bridge2
        // host6 and host 7 connected to port 22 : "cloud" symbol
        bridge2B.increasePortCounter();
        bridge2B.createAndPersistBridgeMacLink(true, host6, 61, gateway);
        bridge2B.createAndPersistBridgeMacLink(false, host7, 71, gateway);

        // bridge3
        // host8:with-no-snmp connected bridge3:port32
        bridge3B.increasePortCounter();
        bridge3B.createAndPersistBridgeMacLink(host8, null, gateway);
        bridge3B.increasePortCounter();
        bridge3B.createAndPersistBridgeMacLink(host9, null, gateway);

        //bridge3:port31 connected to up bridge0:port2
        root.connectToNewBridge(bridge10, 10);

        // create sub tree on bridge5:
        if (nodes.size() >= offset + 20) {
            createAndPersistProtocolSpecificEntities(nodes, bridge5B, gateway, iteration + 1);
        }
    }
}
