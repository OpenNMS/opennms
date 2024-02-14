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
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.model.OnmsNode;

public class OspfProtocol extends Protocol {
    private final TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.ospf;
    private final InetAddressGenerator inetAddressCreator = new InetAddressGenerator();

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
