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
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.model.OnmsNode;

public class OspfProtocol extends Protocol<OspfElement> {
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.ospf;
    private InetAddressGenerator inetAddressCreator = new InetAddressGenerator();

    public OspfProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
        List<OspfLink> links = createLinks(nodes);
        context.getTopologyPersister().persist(links);
    }

    private List<OspfLink> createLinks(List<OnmsNode> nodes) {
        PairGenerator<OnmsNode> pairs = createPairGenerator(nodes);
        List<OspfLink> links = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks() / 2; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.matchCdpLinks()
            Pair<OnmsNode, OnmsNode> pair = pairs.next();
            OnmsNode sourceNode = pair.getLeft();
            OnmsNode targetNode = pair.getRight();
            InetAddress ospfIpAddr = inetAddressCreator.next();
            InetAddress ospfRemIpAddr = inetAddressCreator.next();

            OspfLink sourceLink = createLink(
                    sourceNode,
                    ospfIpAddr,
                    ospfRemIpAddr,
                    nodeIfIndexes.get(sourceNode.getId())
            );
            links.add(sourceLink);

            OspfLink targetLink = createLink(
                    targetNode,
                    ospfRemIpAddr,
                    ospfIpAddr,
                    nodeIfIndexes.get(targetNode.getId())
            );
            links.add(targetLink);
            context.currentProgress(String.format("Linked node %s with node %s", sourceNode.getLabel(),
                    targetNode.getLabel()));
        }
        return links;
    }

    private OspfLink createLink(OnmsNode node, InetAddress ipAddress, InetAddress remoteAddress, int ifIndex) {
        OspfLink link = new OspfLink();
        link.setNode(node);
        link.setOspfIpAddr(ipAddress);
        link.setOspfRemIpAddr(remoteAddress);

        link.setOspfIpMask(this.inetAddressCreator.next());
        link.setOspfAddressLessIndex(3);
        link.setOspfIfIndex(ifIndex);
        link.setOspfRemRouterId(this.inetAddressCreator.next());
        link.setOspfRemAddressLessIndex(3);
        link.setOspfLinkLastPollTime(new Date());

        return link;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return this.protocol;
    }
}
