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

import java.util.List;
import java.util.Objects;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

public class AssetGraphGenerator {

    private NodeDao nodeDao;

    public AssetGraphGenerator(NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    public GraphML generateGraphs(GeneratorConfig config) {
        final GraphML graphML = new GraphML();
        graphML.setProperty(GraphMLProperties.LABEL, config.getLabel());
        graphML.setProperty(GraphMLProperties.BREADCRUMB_STRATEGY, config.getBreadcrumbStrategy());

        // Simulate asset layer generation
        final List<String> layerHierarchies = config.getLayerHierarchies();
        for (String eachLayer : layerHierarchies) {
            GraphMLGraph layerGraph = new GraphMLGraph();
            layerGraph.setId(eachLayer);
            layerGraph.setProperty(GraphMLProperties.NAMESPACE, "asset:" + eachLayer);
            applyDefaults(layerGraph, "Layer " + eachLayer, config);

            // E.g. assetHelper.getAssetValues(eachLayer)
            for (int i=0; i<3; i++) {
                GraphMLNode assetNode = new GraphMLNode();
                assetNode.setId(eachLayer + "_" + i);
                assetNode.setProperty(GraphMLProperties.LABEL, "Simulated Asset value layer " + eachLayer + " item " + i);

                layerGraph.addNode(assetNode);
            }
            graphML.addGraph(layerGraph);
        }
        // TODO implement linking to next layer, e.g.:
//            for (String eachParent : assetHelper.getAssetValues(eachLayer)) {
//                for (String eachChild : assetHelper.getAssetValues(eachParent)) {
//
//                }
//            }


        // Finally simulating last layer
        GraphMLGraph nodeGraph = new GraphMLGraph();
        nodeGraph.setId("nodes");
        nodeGraph.setProperty(GraphMLProperties.NAMESPACE, "asset:nodes");
        applyDefaults(nodeGraph, "Nodes", config);
        for (OnmsNode node : nodeDao.findAllProvisionedNodes()) {
            GraphMLNode graphMLNode = new GraphMLNode();
            graphMLNode.setId("node: " + node.getNodeId());

            graphMLNode.setProperty(GraphMLProperties.LABEL, node.getLabel());
            graphMLNode.setProperty(GraphMLProperties.NODE_ID, node.getId());
            graphMLNode.setProperty(GraphMLProperties.FOREIGN_ID, node.getForeignId());
            graphMLNode.setProperty(GraphMLProperties.FOREIGN_SOURCE, node.getForeignSource());

            nodeGraph.addNode(graphMLNode);
        }
        graphML.addGraph(nodeGraph);

        // TODO implement linking to last layer.:
        return graphML;
    }

    private void applyDefaults(GraphMLGraph graph, String description, GeneratorConfig config) {
        graph.setProperty(GraphMLProperties.NAMESPACE, graph.getId());
        graph.setProperty(GraphMLProperties.FOCUS_STRATEGY, "ALL");
        if (config.getPreferredLayout() != null) {
            graph.setProperty(GraphMLProperties.PREFERRED_LAYOUT, config.getPreferredLayout());
        }
        if (description != null) {
            graph.setProperty(GraphMLProperties.DESCRIPTION, description);
        }
        graph.setProperty(GraphMLProperties.SEMANTIC_ZOOM_LEVEL, 0);
    }
}
