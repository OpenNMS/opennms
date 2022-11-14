/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.graph;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.graph.Graph;
import org.opennms.integration.api.v1.graph.GraphContainer;
import org.opennms.integration.api.v1.graph.VertexRef;
import org.opennms.integration.api.v1.graph.configuration.GraphConfiguration;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.focus.FocusStrategy;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public class GraphMapper {

    public GenericGraph map(final Graph extensionGraph, final GraphConfiguration graphConfiguration) {
        Objects.requireNonNull(extensionGraph, "extensionGraph must not be null");
        Objects.requireNonNull(graphConfiguration, "graphConfiguration must not be null");
        final List<GenericVertex> vertices = extensionGraph.getVertices().stream()
                .map(v -> GenericVertex.builder().properties(v.getProperties()).build())
                .collect(Collectors.toList());
        final List<GenericEdge> edges = extensionGraph.getEdges().stream()
                .map(e -> GenericEdge.builder()
                        .properties(e.getProperties())
                        .source(e.getSource().getNamespace(), e.getSource().getId())
                        .target(e.getTarget().getNamespace(), e.getTarget().getId())
                        .build())
                .collect(Collectors.toList());
        final GenericGraph.GenericGraphBuilder graphBuilder = GenericGraph.builder()
                .properties(extensionGraph.getProperties())
                .property(GenericProperties.Enrichment.RESOLVE_NODES, graphConfiguration.shouldEnrichNodeInfo())
                .property(GenericProperties.Enrichment.DEFAULT_STATUS, graphConfiguration.getGraphStatusStrategy() == GraphConfiguration.GraphStatusStrategy.Default)
                .addVertices(vertices)
                .addEdges(edges);
        final List<VertexRef> defaultFocus = extensionGraph.getDefaultFocus();
        if (defaultFocus != null) {
            final List<org.opennms.netmgt.graph.api.VertexRef> convertedDefaultFocus = defaultFocus.stream().map(vertexRef -> new org.opennms.netmgt.graph.api.VertexRef(vertexRef.getNamespace(), vertexRef.getId())).collect(Collectors.toList());
            graphBuilder.focus(new Focus(FocusStrategy.SELECTION, convertedDefaultFocus));
        }
        final GenericGraph convertedGraph = graphBuilder.build();
        return convertedGraph;
    }

    public GenericGraphContainer map(final GraphContainer extensionGraphContainer, final GraphConfiguration graphConfiguration) {
        Objects.requireNonNull(extensionGraphContainer, "extensionGraphContainer must not be null");
        Objects.requireNonNull(graphConfiguration, "graphConfiguration must not be null");
        final GenericGraphContainer.GenericGraphContainerBuilder containerBuilder = GenericGraphContainer.builder()
                .properties(extensionGraphContainer.getProperties());
        extensionGraphContainer.getGraphs().stream()
                .map(extensionGraph -> map(extensionGraph, graphConfiguration))
                .forEach(containerBuilder::addGraph);
        final GenericGraphContainer convertedGraphContainer = containerBuilder.build();
        return convertedGraphContainer;
    }

    public GraphInfo map(final org.opennms.integration.api.v1.graph.GraphInfo extensionGraphInfo) {
        Objects.requireNonNull(extensionGraphInfo, "extensionGraphInfo must not be null");
        return new org.opennms.netmgt.graph.api.info.GraphInfo() {

            @Override
            public String getNamespace() {
                return extensionGraphInfo.getNamespace();
            }

            @Override
            public String getDescription() {
                return extensionGraphInfo.getDescription();
            }

            @Override
            public String getLabel() {
                return extensionGraphInfo.getLabel();
            }
        };
    }

    public GraphContainerInfo map(final org.opennms.integration.api.v1.graph.GraphContainerInfo extensionGraphContainerInfo) {
        Objects.requireNonNull(extensionGraphContainerInfo, "extensionGraphContainerInfo must not be null");
        return new GraphContainerInfo() {

            @Override
            public String getId() {
                return extensionGraphContainerInfo.getContainerId();
            }

            @Override
            public List<String> getNamespaces() {
                return extensionGraphContainerInfo.getGraphInfos().stream().map(gi -> gi.getNamespace()).collect(Collectors.toList());
            }

            @Override
            public String getDescription() {
                return extensionGraphContainerInfo.getDescription();
            }

            @Override
            public String getLabel() {
                return extensionGraphContainerInfo.getLabel();
            }

            @Override
            public GraphInfo getGraphInfo(String namespace) {
                final org.opennms.integration.api.v1.graph.GraphInfo extensionGraphInfo = extensionGraphContainerInfo.getGraphInfos().stream()
                        .filter(gi -> gi.getNamespace().equals(namespace)).findAny()
                        .orElseThrow(() -> new NoSuchElementException("GraphInfo with namespace '" + namespace + "' does not exist"));
                return map(extensionGraphInfo);
            }

            @Override
            public GraphInfo getPrimaryGraphInfo() {
                final org.opennms.integration.api.v1.graph.GraphInfo defaultGraphInfo = extensionGraphContainerInfo.getDefaultGraphInfo();
                return map(defaultGraphInfo);
            }

            @Override
            public List<GraphInfo> getGraphInfos() {
                return extensionGraphContainerInfo.getGraphInfos().stream()
                        .map(gi -> map(gi))
                        .collect(Collectors.toList());
            }
        };
    }
}
