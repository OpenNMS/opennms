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
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsIsProtocol extends Protocol<IsIsElement> {
    private final static Logger LOG = LoggerFactory.getLogger(IsIsProtocol.class);
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.isis;

    public IsIsProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
        List<IsIsElement> elements = createElements(nodes);
        context.getTopologyPersister().persist(elements);
        List<IsIsLink> links = createLinks(elements);
        context.getTopologyPersister().persist(links);
    }

    private List<IsIsElement> createElements(List<OnmsNode> nodes) {
        ArrayList<IsIsElement> elements = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountElements(); i++) {
            OnmsNode node = nodes.get(i);
            elements.add(createElement(node));
        }
        return elements;
    }

    private IsIsElement createElement(OnmsNode node) {
        IsIsElement element = new IsIsElement();
        element.setNode(node);
        element.setIsisSysID("IsIsElementForNode" + node.getId());
        element.setIsisSysAdminState(IsIsElement.IsisAdminState.on);
        element.setIsisNodeLastPollTime(new Date());
        return element;
    }

    private List<IsIsLink> createLinks(List<IsIsElement> elements) {
        PairGenerator<IsIsElement> pairs = createPairGenerator(elements);
        List<IsIsLink> links = new ArrayList<>();
        Integer isisISAdjIndex = 0;

        for (int i = 0; i < topologySettings.getAmountLinks() / 2; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.match...Links()
            Pair<IsIsElement, IsIsElement> pair = pairs.next();
            IsIsElement sourceElement = pair.getLeft();
            IsIsElement targetElement = pair.getRight();
            isisISAdjIndex++;

            int sourceIfIndex = nodeIfIndexes.get(sourceElement.getNode().getId());
            int targetIfIndex = nodeIfIndexes.get(targetElement.getNode().getId());

            IsIsLink sourceLink = createLink(sourceElement.getNode(), isisISAdjIndex, targetElement.getIsisSysID(),
                    sourceIfIndex);
            links.add(sourceLink);

            IsIsLink targetLink = createLink(targetElement.getNode(), isisISAdjIndex, sourceElement.getIsisSysID(),
                    targetIfIndex);
            links.add(targetLink);

            context.currentProgress(String.format("Linked node %s with node %s", sourceElement.getNode().getLabel(),
                    targetElement.getNode().getLabel()));
        }
        return links;
    }

    private IsIsLink createLink(OnmsNode node, Integer isisISAdjIndex, String isisISAdjNeighSysID, int ifIndex) {
        IsIsLink link = new IsIsLink();
        link.setIsisISAdjIndex(isisISAdjIndex);
        link.setIsisISAdjNeighSysID(isisISAdjNeighSysID);
        link.setNode(node);
        // static data:
        link.setIsisLinkLastPollTime(new Date());
        link.setIsisCircIndex(3);
        link.setIsisISAdjState(IsIsLink.IsisISAdjState.up);
        link.setIsisISAdjNeighSNPAAddress("isisISAdjNeighSNPAAddress");
        link.setIsisISAdjNeighSysType(IsIsLink.IsisISAdjNeighSysType.l1_IntermediateSystem);
        link.setIsisISAdjNbrExtendedCircID(3);
        link.setIsisCircIfIndex(ifIndex);
        link.setIsisCircAdminState(IsIsElement.IsisAdminState.on);
        return link;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return this.protocol;
    }
}
