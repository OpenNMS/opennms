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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLEdge;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.asset.filter.Filter;
import org.opennms.features.topology.plugins.topo.asset.filter.FilterParser;
import org.opennms.features.topology.plugins.topo.asset.layers.IdGenerator;
import org.opennms.features.topology.plugins.topo.asset.layers.ItemProvider;
import org.opennms.features.topology.plugins.topo.asset.layers.Layer;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerBuilder;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinitionRepository;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetGraphGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphGenerator.class);

	private final NodeProvider nodeProvider;

	public AssetGraphGenerator(NodeProvider nodeProvider) {
		this.nodeProvider = Objects.requireNonNull(nodeProvider);
	}

	public GraphML generateGraphs(GeneratorConfig config) {
		final LayerDefinitionRepository layerDefinitionRepository = new LayerDefinitionRepository();
		final List<LayerDefinition> layerDefinitions = layerDefinitionRepository.getDefinitions(config.getLayerHierarchies());
		final List<OnmsNode> nodes = nodeProvider.getNodes(layerDefinitions);

		// Define Layers
		final List<Layer> layers = layerDefinitions.stream().map(LayerDefinition::getLayer).collect(Collectors.toList());

		// Add last Layer for Nodes
		layers.add(new LayerBuilder()
				.withId("nodes")
				.withNamespace("nodes")
				.withLabel("Nodes")
				.withDescription("The nodes in the hierarchy of the topology")
				.withItemProvider(node -> node)
				.withIdGenerator(IdGenerator.SIMPLE)
				.withSemanticZoomLevel(0)
				.withVertexStatusProvider(true)
				.build()
		);

		// Ensure that all elements in the nodes do have values set
		layers.forEach(layer -> {
			List<OnmsNode> nodeWithNullValues = nodes.stream().filter(n -> layer.getItemProvider().getItem(n) == null).collect(Collectors.toList());
			if (!nodeWithNullValues.isEmpty()) {
				LOG.debug("Found nodes with null value for layer (id: {}, label: {}). Removing nodes {}",
						layer.getId(), layer.getLabel(),
						nodeWithNullValues.stream().map(n -> String.format("(id: %s, label: %s)", n.getId(), n.getLabel())).collect(Collectors.toList()));
				nodes.removeAll(nodeWithNullValues);
			}
		});

		// Apply additional filters
		final Map<String, Filter> filterMap = new FilterParser().parse(config.getFilters());
		final List<LayerDefinition> layersToFilter = layerDefinitionRepository.getDefinitions(filterMap.keySet());
		applyFilters(nodes,filterMap,layerDefinitionRepository);
		
		// Start generating the hierarchy
		// Overall graphml object
		final GraphML graphML = new GraphML();
		graphML.setProperty(GraphMLProperties.LABEL, config.getLabel());
		graphML.setProperty(GraphMLProperties.BREADCRUMB_STRATEGY, config.getBreadcrumbStrategy());

		// Build each Graph
		int index = 0;
		for (Layer layer : layers) {
			GraphMLGraph layerGraph = new GraphMLGraph();
			layerGraph.setId(config.getProviderId() + ":" + layer.getId());
			layerGraph.setProperty(GraphMLProperties.NAMESPACE, config.getProviderId() + ":" + layer.getNamespace());
			layerGraph.setProperty(GraphMLProperties.PREFERRED_LAYOUT, config.getPreferredLayout());
			layerGraph.setProperty(GraphMLProperties.LABEL, layer.getLabel());
            layerGraph.setProperty(GraphMLProperties.DESCRIPTION, layer.getDescription());
			layerGraph.setProperty(GraphMLProperties.FOCUS_STRATEGY, layer.getFocusStrategy().name());
			layerGraph.setProperty(GraphMLProperties.SEMANTIC_ZOOM_LEVEL, layer.getSemanticZoomLevel());
			layerGraph.setProperty(GraphMLProperties.VERTEX_STATUS_PROVIDER, layer.hasVertexStatusProvider());

			// Build layer for nodes
			for (OnmsNode eachNode : nodes) {
				final Object eachItem = layer.getItemProvider().getItem(eachNode);
				if (eachItem != null) {
					List<Layer> processedLayers = layers.subList(0, index);
					String id = layer.getIdGenerator().generateId(processedLayers, eachNode, layer.getNodeDecorator().getId(eachItem));
					if (layerGraph.getNodeById(id) == null) {
						GraphMLNode node = new GraphMLNode();
						node.setId(id);
						layer.getNodeDecorator().decorate(node, eachItem);
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
				List<GraphMLNode> path = getPath(n, graphML.getGraphs(), layers);
				if (path.size() != graphML.getGraphs().size() ) {
					throw new IllegalStateException("");
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
		return graphML;
	}
	
	public static void applyFilters(List<OnmsNode> nodes, Map<String, Filter> filterMap,LayerDefinitionRepository layerDefinitionRepository) {
		final List<LayerDefinition> layersToFilter = layerDefinitionRepository.getDefinitions(filterMap.keySet());
		layersToFilter.stream()
				.filter(layerToFilter -> filterMap.get(layerToFilter.getKey()) != null)
				.forEach(
					layerToFilter -> {
						final List<OnmsNode> filteredNodes = nodes.stream().filter(n -> {
							ItemProvider itemProvider = layerToFilter.getLayer().getItemProvider();
							Filter filter = filterMap.get(layerToFilter.getKey());
							return filter.apply(itemProvider.getItem(n));
						}).collect(Collectors.toList());
						if (!filteredNodes.isEmpty()) {
							final Layer layer = layerToFilter.getLayer();
							LOG.debug("Found nodes to remove due to filter settings. Removing nodes {}",
									filteredNodes.stream().map(n -> String.format("(id: %s, label: %s)", n.getId(), n.getLabel())).collect(Collectors.toList()));
							nodes.removeAll(filteredNodes);
						}
					});
	}

	/**
	 * Calculates a path through all layers (from top to bottom) to the given <code>node</code>.
	 *
	 * Ensure that this path exist, otherwise an Exception is thrown.
	 *
	 * @param node The node to calculate the path to.
	 * @param graphs All generated graphs, containing all nodes created.
	 * @param layers All layers from which the graphs were created.
	 *
	 * @return A list of {@link GraphMLNode}s (the path) through all layers to the given <code>node</code>.
	 */
	private static List<GraphMLNode> getPath(OnmsNode node, List<GraphMLGraph> graphs, List<Layer> layers) {
		// The graphs were created based on the layers. If the count differs, the path cannot be calculated
		if (graphs.size() != layers.size()) {
			throw new IllegalArgumentException("Cannot calculate path. There are more layers than graphs, but the count must be identical");
		}
		final List<GraphMLNode> path = new ArrayList<>();
		for (int i=0; i<graphs.size(); i++) {
			final GraphMLGraph graph = graphs.get(i); // the layer graph
			final Layer layer = layers.get(i); // the layer definition

			// the the value for the specified node in that layer (it must exist)
			final Object item = layer.getItemProvider().getItem(node);
			final String itemId = layer.getNodeDecorator().getId(item);
			final String nodeId = layer.getIdGenerator().generateId(layers.subList(0, i), node, itemId);

			final GraphMLNode GraphMlNode = graph.getNodeById(nodeId);
			if (GraphMlNode != null) {
				path.add(GraphMlNode);
			} else {
				throw new IllegalStateException(String.format("Could not find a node with id {} in graph with id {}.", nodeId, graph.getId()));
			}
		}
		return path;
	}
}
