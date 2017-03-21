/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.support.SimpleGraphBuilder;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.DefaultTopologyServiceClient;
import org.opennms.features.topology.app.internal.service.DefaultTopologyService;
import org.opennms.features.topology.app.internal.service.SimpleServiceLocator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class BreadcrumbPathCalculatorTest {

    private TopologyServiceClient topologyServiceClient;

    @Before
    public void setUp() {
        final GraphProvider layer1 = new SimpleGraphBuilder("layer1")
                .vertex("A1")
                .vertex("A2")
                .get();
        final GraphProvider layer2 = new SimpleGraphBuilder("layer2")
                .vertex("B1")
                .vertex("B2")
                .vertex("B3")
                .vertex("B4")
                .get();
        final GraphProvider layer3 = new SimpleGraphBuilder("layer3")
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
                return layer1;
            }

            @Override
            public Collection<GraphProvider> getGraphProviders() {
                return Lists.newArrayList(layer1, layer2, layer3);
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