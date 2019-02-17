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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.topology.PairGenerator;
import org.opennms.enlinkd.generator.util.MacAddressGenerator;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;


// BroadcastDomain:
// BridgeTopologyServiceImpl.findAll()
// + BridgeTopologyServiceImpl.getAllPersisted()
// +- BridgeBridgeLinkDao.findAll()
// +- BridgePort bridgeport = BridgePort.getFromBridgeBridgeLink(link);
// +- BridgePort getFromDesignatedBridgeBridgeLink(link);
// +- SharedSegment.create(link)
// +- m_bridgeMacLinkDao.findAll()
public class BridgeProtocol extends Protocol<BridgeBridgeLink> {

    private final MacAddressGenerator macGenerator = new MacAddressGenerator();

    public BridgeProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    protected TopologyGenerator.Protocol getProtocol() {
        return TopologyGenerator.Protocol.bridgeBridge;
    }

    @Override
    protected void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
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

            BridgeBridgeLink sourceLink = createBridgeBridgeLink(sourceNode, targetNode);
            links.add(sourceLink);

            BridgeBridgeLink targetLink = createBridgeBridgeLink(targetNode, sourceNode);
            links.add(targetLink);
            context.currentProgress(String.format("Linked node %s with node %s", sourceNode.getLabel(), targetNode.getLabel()));
        }
        return links;
    }


    private BridgeBridgeLink createBridgeBridgeLink(OnmsNode node, OnmsNode designatedNode) {
        BridgeBridgeLink link = new BridgeBridgeLink();
        link.setBridgeBridgeLinkLastPollTime(new Date());
        link.setBridgePortIfIndex(3);
        link.setBridgePort(4);
        link.setVlan(1);
        link.setBridgePortIfName("xx");
        link.setNode(node);
        link.setBridgeBridgeLinkCreateTime(new Date());
        link.setDesignatedNode(designatedNode);
        link.setDesignatedPort(5);
        link.setDesignatedPortIfIndex(6);
        link.setDesignatedVlan(23);
        link.setDesignatedPortIfName("yy");
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
}
