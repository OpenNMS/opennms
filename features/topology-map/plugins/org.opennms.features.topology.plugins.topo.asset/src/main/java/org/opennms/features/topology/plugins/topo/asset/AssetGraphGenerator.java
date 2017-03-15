/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLEdge;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.asset.layers.IdGenerator;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerMapping;
import org.opennms.features.topology.plugins.topo.asset.layers.definition.LayerDefinitionBuilder;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetGraphGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphGenerator.class);

	private final DataProvider dataProvider;

	public AssetGraphGenerator(DataProvider dataProvider) {
		this.dataProvider = Objects.requireNonNull(dataProvider);
	}

	public GraphML generateGraphs(GeneratorConfig config) {
		final List<LayerMapping.Mapping> layerMappings = new LayerMapping().getMapping(config.getLayerHierarchies());
		final List<OnmsNode> nodes = dataProvider.getNodes(layerMappings);

		// Define Layers
		final List<LayerDefinition> layerDefinitions = layerMappings.stream().map(mapping -> mapping.getLayerDefinition()).collect(Collectors.toList());

		// Add last Layer for Nodes
		layerDefinitions.add(new LayerDefinitionBuilder()
				.withId("nodes")
				.withNamespace("nodes")
				.withItemProvider(node -> node)
				.withIdGenerator(IdGenerator.SIMPLE)
				.build()
		);

		// Ensure that all elements in the nodes do have values set
		layerDefinitions.forEach(layerDefinition -> {
			List<OnmsNode> nodeWithNullValues = nodes.stream().filter(n -> layerDefinition.getItemProvider().getItem(n) == null).collect(Collectors.toList());
			if (!nodeWithNullValues.isEmpty()) {
				LOG.debug("Found nodes with null value for layer (id: {}, label: {}). Removing nodes {}",
						layerDefinition.getId(), layerDefinition.getLabel(),
						nodeWithNullValues.stream().map(n -> String.format("(id: %s, label: %s)", n.getId(), n.getLabel())).collect(Collectors.toList()));
				nodes.removeAll(nodeWithNullValues);
			}
		});

		// Start generating the hierarchy
		// Overall graphml object
		final GraphML graphML = new GraphML();
		graphML.setProperty(GraphMLProperties.LABEL, config.getLabel());
		graphML.setProperty(GraphMLProperties.BREADCRUMB_STRATEGY, config.getBreadcrumbStrategy());

		// Build each Graph
		int index = 0;
		for (LayerDefinition layerDefinition : layerDefinitions) {
			GraphMLGraph layerGraph = new GraphMLGraph();
			layerGraph.setId(config.getProviderId() + ":" + layerDefinition.getId());
			layerGraph.setProperty(GraphMLProperties.NAMESPACE, config.getProviderId() + ":" + layerDefinition.getNamespace());
			layerGraph.setProperty(GraphMLProperties.PREFERRED_LAYOUT, config.getPreferredLayout());
//            layerGraph.setProperty(GraphMLProperties.DESCRIPTION, layerDefinition.getDescription());
			layerGraph.setProperty(GraphMLProperties.DESCRIPTION, "");
			layerGraph.setProperty(GraphMLProperties.FOCUS_STRATEGY, "ALL");
			layerGraph.setProperty(GraphMLProperties.SEMANTIC_ZOOM_LEVEL, index);
			layerGraph.setProperty(GraphMLProperties.VERTEX_STATUS_PROVIDER, true);

			// Build layer for nodes
			for (OnmsNode eachNode : nodes) {
				final Object eachItem = layerDefinition.getItemProvider().getItem(eachNode);
				if (eachItem != null) {
					List<LayerDefinition> processedLayers = layerDefinitions.subList(0, index);
					String id = layerDefinition.getIdGenerator().generateId(processedLayers, eachNode, layerDefinition.getNodeDecorator().getId(eachItem));
					if (layerGraph.getNodeById(id) == null) {
						GraphMLNode node = new GraphMLNode();
						node.setId(id);
						layerDefinition.getNodeDecorator().decorate(node, eachItem);
						layerGraph.addNode(node);
					}
				}
			}
			graphML.addGraph(layerGraph);
			index++;
		}

		// Now link all nodes, but only if there are at least 2 layers
		if (graphML.getGraphs().size() > 1) {
			nodes.forEach(n -> {
				List<GraphMLNode> path = getPath(n, graphML.getGraphs(), layerDefinitions);
				if (path.size() != graphML.getGraphs().size() ) {
					throw new IllegalStateException("TODO MVR");
				}
				for (int i=0; i<path.size() - 1; i++) {
					GraphMLNode sourceNode = path.get(i);
					GraphMLNode targetNode = path.get(i+1);
					GraphMLGraph sourceGraph = graphML.getGraphs().get(i);
					String edgeId = String.format("%s_%s", sourceNode.getId(), targetNode.getId());
					if (sourceGraph.getEdgeById(edgeId) == null) {
						GraphMLEdge edge = new GraphMLEdge();
						edge.setId(edgeId);
						edge.setSource(sourceNode);
						edge.setTarget(targetNode);
						sourceGraph.addEdge(edge);
					}
				}
			});
		}

		// Add nodes for unallocated elements
		if (!config.getLayerHierarchies().isEmpty() && config.getGenerateUnallocated()) {
			GraphMLGraph layerGraph = new GraphMLGraph();
			layerGraph.setId(config.getProviderId() + ":unallocated_Nodes");
			layerGraph.setProperty(GraphMLProperties.NAMESPACE, layerGraph.getId());
			layerGraph.setProperty(GraphMLProperties.PREFERRED_LAYOUT, config.getPreferredLayout());
			layerGraph.setProperty(GraphMLProperties.DESCRIPTION, "A graph containing all nodes which cannot be placed in topology hierarchy");
			layerGraph.setProperty(GraphMLProperties.FOCUS_STRATEGY, "ALL");
			layerGraph.setProperty(GraphMLProperties.SEMANTIC_ZOOM_LEVEL, 0);
			layerGraph.setProperty(GraphMLProperties.VERTEX_STATUS_PROVIDER, true);
			graphML.addGraph(layerGraph);
		}

		return graphML;
	}

	private static List<GraphMLNode> getPath(OnmsNode node, List<GraphMLGraph> graphs, List<LayerDefinition> layerDefinitions) {
		if (graphs.size() != layerDefinitions.size()) {
			// TODO MVR not computable
		}

		final List<GraphMLNode> path = new ArrayList<>();
		for (int i=0; i<graphs.size(); i++) {
			final GraphMLGraph graph = graphs.get(i); // the layer graph
			final LayerDefinition layerDefinition = layerDefinitions.get(i); // the layer definition

			// the the value for the specified node in that layer (it must exist)
			final Object item = layerDefinition.getItemProvider().getItem(node);
			final String itemId = layerDefinition.getNodeDecorator().getId(item);
			final String nodeId = layerDefinition.getIdGenerator().generateId(layerDefinitions.subList(0, i), node, itemId);

			final GraphMLNode GraphMlNode = graph.getNodeById(nodeId);
			if (GraphMlNode != null) {
				path.add(GraphMlNode);
			} else {
				// TODO MVR should never occur?!
			}
		}
		return path;
	}
}
