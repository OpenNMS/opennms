/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

public class UserDefinedProtocol extends Protocol {
    public static final String OWNER = TopologyGenerator.class.getCanonicalName();
    private final TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.userdefined;

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
