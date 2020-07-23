/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.graphml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class GraphmlToGraphConverter {

    private static final Logger LOG = LoggerFactory.getLogger(GraphmlToGraphConverter.class);
    private static final String FOCUS_STRATEGY = "focus-strategy";
    private static final String FOCUS_IDS = "focus-ids";
    private HashMap<String, GraphMLGraph> vertexIdToGraphMapping = new HashMap<>();

    public GenericGraphContainer convert(final GraphML graphML) {
        // Index vertex id to graph mapping
        graphML.getGraphs().stream().forEach(
                g -> g.getNodes().stream().forEach(n -> {
                    if (vertexIdToGraphMapping.containsKey(n.getId())) {
                        throw new IllegalStateException("GraphML graph contains vertices with same id. Bailing");
                    }
                    vertexIdToGraphMapping.put(n.getId(), g);
                })
        );

        // Convert graph
        final String graphContainerId = determineGraphContainerId(graphML);
        final GenericGraphContainer.GenericGraphContainerBuilder graphContainerBuilder = GenericGraphContainer.builder()
                .id(graphContainerId)
                .label(graphML.getProperty(GenericProperties.LABEL))
                .description(graphML.getProperty(GenericProperties.DESCRIPTION));
        for (GraphMLGraph eachGraph : graphML.getGraphs()) {
            final GenericGraph convertedGraph = convert(eachGraph);
            graphContainerBuilder.addGraph(convertedGraph);
        }
        this.vertexIdToGraphMapping.clear(); // clear data as it was only needed while building the container
        final GenericGraphContainer graphContainer = graphContainerBuilder.build();
        return graphContainer;
    }

    private final GenericGraph convert(GraphMLGraph graphMLGraph) {
        final GenericGraph.GenericGraphBuilder graphBuilder = GenericGraph.builder()
                .property(GenericProperties.Enrichment.RESOLVE_NODES, true) // Enable Node Enrichment first so it can be overridden
                .property(GenericProperties.Enrichment.DEFAULT_STATUS, true) // Enable default Status calculation
                .properties(graphMLGraph.getProperties());
        final List<GenericVertex> vertices = graphMLGraph.getNodes()
                .stream().map(n -> {
                    // In case of GraphML each vertex does not have a namespace, but it is inherited from the graph
                    // Therefore here we have to manually set it
                    return GenericVertex.builder()
                            .namespace(graphBuilder.getNamespace())
                            .id(n.getId())
                            .properties(n.getProperties()).build();
                })
                .collect(Collectors.toList());
        graphBuilder.addVertices(vertices);

        final List<GenericEdge> edges = graphMLGraph.getEdges().stream().map(e -> {
            final String sourceNamespace = vertexIdToGraphMapping.get(e.getSource().getId()).getProperty(GenericProperties.NAMESPACE);
            final String targetNamespace = vertexIdToGraphMapping.get(e.getTarget().getId()).getProperty(GenericProperties.NAMESPACE);
            final GenericVertex source = GenericVertex.builder().namespace(sourceNamespace).id(e.getSource().getId()).build();
            final GenericVertex target = GenericVertex.builder().namespace(targetNamespace).id(e.getTarget().getId()).build();
            // In case of GraphML each edge does not have a namespace, but it is inherited from the graph
            // Therefore here we have to manually set it
            final GenericEdge edge = GenericEdge.builder()
                    .namespace(graphBuilder.getNamespace())
                    .source(source.getVertexRef())
                    .target(target.getVertexRef())
                    .properties(e.getProperties()).build();
            return edge;
        }).collect(Collectors.toList());
        graphBuilder.addEdges(edges);

        applyFocus(graphMLGraph, graphBuilder);
        return graphBuilder.build();
    }

    private static void applyFocus(final GraphMLGraph graphMLGraph, final GenericGraph.GenericGraphBuilder graphBuilder) {
        final String strategy = graphMLGraph.getProperty(FOCUS_STRATEGY);
        if (strategy == null || "empty".equalsIgnoreCase(strategy)) {
            graphBuilder.focus().empty().apply();
        } else if ("all".equalsIgnoreCase(strategy)) {
            graphBuilder.focus().all().apply();
        } else if ("first".equalsIgnoreCase(strategy)) {
            graphBuilder.focus().first().apply();
        } else if ("specific".equalsIgnoreCase(strategy) || "selection".equalsIgnoreCase(strategy)) {
            final List<String> focusIds = getFocusIds(graphMLGraph);
            graphBuilder.focus().selection(graphBuilder.getNamespace(), focusIds).apply();
        } else {
            final String[] supportedStrategies = new String[]{"empty", "all", "first", "specific"};
            throw new IllegalStateException("Provided focus strategy '" + strategy + "' is not supported. Supported values are: " + Arrays.toString(supportedStrategies));
        }
    }

    private static List<String> getFocusIds(GraphMLGraph inputGraph) {
        final String property = inputGraph.getProperty(FOCUS_IDS);
        if (property != null) {
            String[] split = property.split(",");
            return Lists.newArrayList(split);
        }
        return Lists.newArrayList();
    }

    // The graphML specification does not allow for an id on the graphML object itself
    // As we always need a unique Id we check if a property called `containerId` is provided.
    // If so we use that, otherwise we concatenate the ids of the graphs
    protected static String determineGraphContainerId(GraphML graphML) {
        if (graphML.getProperty("containerId") != null) {
            return graphML.getProperty("containerId");
        }
        LOG.warn("No property 'containerId' was provided. Calculating the container id using the graph's ids");
        final String calculatedId = graphML.getGraphs().stream().map(g -> g.getId()).collect(Collectors.joining("."));
        return calculatedId;
    }
}
