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

public class IsIsProtocol extends Protocol {
    private final TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.isis;

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
