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
package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.simple.SimpleGraphBuilder;
import org.opennms.features.topology.api.topo.simple.SimpleGraphProvider;
import org.opennms.features.topology.app.internal.DefaultTopologyServiceClient;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.service.SimpleServiceLocator;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BreadcrumbPathCalculatorTest {

    private TopologyServiceClient topologyServiceClient;

    @Before
    public void setUp() {
        final BackendGraph layer1 = new SimpleGraphBuilder("layer1")
                .vertex("A1")
                .vertex("A2")
                .get();
        final BackendGraph layer2 = new SimpleGraphBuilder("layer2")
                .vertex("B1")
                .vertex("B2")
                .vertex("B3")
                .vertex("B4")
                .get();
        final BackendGraph layer3 = new SimpleGraphBuilder("layer3")
                .vertex("C1")
                .vertex("C2")
                .vertex("C3")
                .vertex("C4")
                .vertex("C5")
                .vertex("C6")
                .edge("e1", "C1", "C4")
                .edge("e2", "C1", "C5")
                .edge("e3", "C4", "C5")
                .get();
        final Map<VertexRef, List<VertexRef>> oppositesMap = Maps.newHashMap();
        oppositesMap.put(new DefaultVertexRef("layer1", "A1"), Lists.newArrayList(new DefaultVertexRef("layer2", "B3"), new DefaultVertexRef("layer2", "B4")));
        oppositesMap.put(new DefaultVertexRef("layer1", "A2"), Lists.newArrayList(new DefaultVertexRef("layer2", "B1"), new DefaultVertexRef("layer2", "B2")));
        oppositesMap.put(new DefaultVertexRef("layer2", "B1"), Lists.newArrayList(new DefaultVertexRef("layer3", "C2")));
        oppositesMap.put(new DefaultVertexRef("layer2", "B2"), Lists.newArrayList(new DefaultVertexRef("layer3", "C1")));
        oppositesMap.put(new DefaultVertexRef("layer2", "B3"), Lists.newArrayList(new DefaultVertexRef("layer3", "C3")));

        MetaTopologyProvider metaTopologyProvider = new MetaTopologyProvider() {

            @Override
            public GraphProvider getDefaultGraphProvider() {
                return new ArrayList<>(getGraphProviders()).get(0);
            }

            @Override
            public Collection<GraphProvider> getGraphProviders() {
                return Lists.newArrayList(new SimpleGraphProvider(layer1), new SimpleGraphProvider(layer2), new SimpleGraphProvider(layer3));
            }

            @Override
            public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
                return Optional.ofNullable(oppositesMap.get(vertexRef)).orElse(Lists.newArrayList());
            }

            @Override
            public GraphProvider getGraphProviderBy(String namespace) {
                return getGraphProviders()
                        .stream()
                        .filter(p -> p.getNamespace().equals(namespace))
                        .findFirst().orElse(null);
            }

            @Override
            public BreadcrumbStrategy getBreadcrumbStrategy() {
                return BreadcrumbStrategy.NONE;
            }

            @Override
            public String getId() {
                return getGraphProviders().stream().map(g -> g.getNamespace()).collect(Collectors.joining(":"));
            }
        };
        DefaultTopologyService topologyService = new DefaultTopologyService();
        topologyService.setServiceLocator(new SimpleServiceLocator(metaTopologyProvider));
        topologyService.setTopologyEntityCache(mock(TopologyEntityCache.class));
        DefaultTopologyServiceClient client = new DefaultTopologyServiceClient(topologyService);
        client.setMetaTopologyId(metaTopologyProvider.getId());
        client.setNamespace(metaTopologyProvider.getDefaultGraphProvider().getNamespace());
        topologyServiceClient = client;
    }

    @Test
    public void testFindPathByVertex() throws IOException {
        // Verify elements, which are not available
        Assert.assertEquals(Lists.newArrayList(), findPath(topologyServiceClient, new DefaultVertexRef("nope", "nope", "I do not exist")));
        Assert.assertEquals(Lists.newArrayList(), findPath(topologyServiceClient, new DefaultVertexRef("layer3", "C6")));

        // Verify the path to A2 (very simple)
        Assert.assertEquals(
                Lists.newArrayList(new DefaultVertexRef("layer1", "A2")),
                findPath(topologyServiceClient, new DefaultVertexRef("layer1", "A2")));
        // Verify the path to B3
        Assert.assertEquals(
                Lists.newArrayList(new DefaultVertexRef("layer1", "A1"), new DefaultVertexRef("layer2", "B3")),
                findPath(topologyServiceClient, new DefaultVertexRef("layer2", "B3")));
        // Verify the path to C5 (should not include C5, as we merge vertices on the same namespace)
        Assert.assertEquals(
                Lists.newArrayList(new DefaultVertexRef("layer1", "A2"), new DefaultVertexRef("layer2", "B2"), new DefaultVertexRef("layer3", "C1")),
                findPath(topologyServiceClient, new DefaultVertexRef("layer3", "C5")));
    }

    private static List<VertexRef> findPath(TopologyServiceClient topologyServiceClient, VertexRef vertexToFind) {
        return BreadcrumbPathCalculator.findPath(BreadcrumbPathCalculator.getIncomingEdgeMap(topologyServiceClient), vertexToFind);
    }
}