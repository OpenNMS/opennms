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
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.enlinkd.generator.topology.PairGenerator;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.model.OnmsNode;

public class UserDefinedProtocol extends Protocol<UserDefinedLink> {
    public static final String OWNER = TopologyGenerator.class.getCanonicalName();
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.userdefined;

    public UserDefinedProtocol(TopologySettings topologySettings, TopologyContext context) {
        super(topologySettings, context);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) {
        List<UserDefinedLink> links = createLinks(nodes);
        context.getTopologyPersister().persist(links);
    }

    private List<UserDefinedLink> createLinks(List<OnmsNode> nodes) {
        PairGenerator<OnmsNode> pairs = createPairGenerator(nodes);
        List<UserDefinedLink> links = new ArrayList<>();
        for (int i = 0; i < topologySettings.getAmountLinks() / 2; i++) {
            Pair<OnmsNode, OnmsNode> pair = pairs.next();
            OnmsNode sourceNode = pair.getLeft();
            OnmsNode targetNode = pair.getRight();

            UserDefinedLink udl = new UserDefinedLink();
            udl.setNodeIdA(sourceNode.getId());
            udl.setComponentLabelA(Integer.toString(nodeIfIndexes.get(sourceNode.getId())));
            udl.setNodeIdZ(targetNode.getId());
            udl.setComponentLabelZ(Integer.toString(nodeIfIndexes.get(sourceNode.getId())));
            udl.setOwner(OWNER);
            udl.setLinkId(Integer.toString(i));

            links.add(udl);
            context.currentProgress(String.format("Linked node %s with node %s", sourceNode.getLabel(),
                    targetNode.getLabel()));
        }
        return links;
    }

    public TopologyGenerator.Protocol getProtocol() {
        return this.protocol;
    }
}
