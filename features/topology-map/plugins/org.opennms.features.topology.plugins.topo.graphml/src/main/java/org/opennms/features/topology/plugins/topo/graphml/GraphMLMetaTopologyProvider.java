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

package org.opennms.features.topology.plugins.topo.graphml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.DefaultMetaInfo;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaInfo;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphML;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLGraph;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLReader;
import org.opennms.features.topology.plugins.topo.graphml.model.InvalidGraphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphMLMetaTopologyProvider implements MetaTopologyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GraphMLTopologyProvider.class);

    private MetaInfo metaInfo = new DefaultMetaInfo();
    private File graphMLFile;
    private final Map<String, GraphProvider> graphsByNamespace = Maps.newLinkedHashMap();
    private final Map<VertexRef, List<VertexRef>> oppositeVertices = Maps.newLinkedHashMap();

    private VertexRef getVertex(org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode node) {
        return graphsByNamespace.values().stream()
            .map(g -> g.getVertex(g.getVertexNamespace(), node.getId()))
            .filter(v -> v != null)
            .findFirst().orElse(null);
    }

    public void load() {
        graphsByNamespace.clear();
        oppositeVertices.clear();
        if (graphMLFile == null) {
            LOG.warn("No graph defined");
            return;
        }
        if (!graphMLFile.exists()) {
            LOG.warn("No graph found at location " + graphMLFile.toString());
            return;
        }
        try (InputStream input = new FileInputStream(graphMLFile)) {
            final GraphML graphML = GraphMLReader.read(input);
            validate(graphML);

            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                final GraphMLTopologyProvider topoProvider = new GraphMLTopologyProvider(eachGraph);
                topoProvider.setMetaInfo(metaInfo);
                final VertexHopGraphProvider vertexHopGraphProvider = new VertexHopGraphProvider(topoProvider);
                graphsByNamespace.put(topoProvider.getVertexNamespace(), vertexHopGraphProvider);
            }

            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                for (org.opennms.features.topology.plugins.topo.graphml.model.GraphMLEdge eachEdge : eachGraph.getEdges()) {
                    final VertexRef sourceVertex = getVertex(eachEdge.getSource());
                    final VertexRef targetVertex = getVertex(eachEdge.getTarget());
                    if (!sourceVertex.getNamespace().equals(targetVertex.getNamespace())) {
                        List<VertexRef> opposites = oppositeVertices.get(sourceVertex);
                        if (opposites == null) {
                            opposites = Lists.newArrayList();
                            oppositeVertices.put(sourceVertex, opposites);
                        }
                        opposites.add(targetVertex);
                    }
                }
            }
        } catch (InvalidGraphException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public GraphProvider getDefaultGraphProvider() {
        return Iterables.getFirst(graphsByNamespace.values(), null);
    }

    @Override
    public Collection<GraphProvider> getGraphProviders() {
        return graphsByNamespace.values();
    }

    @Override
    public String getPreferredLayout(GraphProvider graphProvider) {
        return null;
    }

    @Override
    public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
        return oppositeVertices.getOrDefault(vertexRef, Collections.emptyList());
    }

    public void setTopologyLocation(String filename) {
        this.graphMLFile = filename != null ? new File(filename) : null;
    }

    public void setMetaInfo(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    // TODO Validate namespace?
    private void validate(GraphML graphML) throws InvalidGraphException {
        final List<String> nodeIds = new ArrayList<>();
        final List<String> edgeIds = new ArrayList<>();
        final List<String> graphIds = new ArrayList<>();

        for (GraphMLGraph eachGraph : graphML.getGraphs()) {
            if (Strings.isNullOrEmpty(eachGraph.getProperty(GraphMLProperties.NAMESPACE))) {
                throw new InvalidGraphException("No namespace defined on graph with id " + eachGraph.getId());
            }

            if (graphIds.contains(eachGraph.getId())) {
                throw new InvalidGraphException("There already exists a graph with id " + eachGraph.getId());
            }
            graphIds.add(eachGraph.getId());

            for (GraphMLNode eachNode : eachGraph.getNodes()) {
                if (nodeIds.contains(eachNode.getId())) {
                    throw new InvalidGraphException("There already exists a node with id " + eachNode.getId());
                }
                nodeIds.add(eachNode.getId());
            }
            for (org.opennms.features.topology.plugins.topo.graphml.model.GraphMLEdge eachEdge : eachGraph.getEdges()) {
                if (edgeIds.contains(eachEdge.getId())) {
                    throw new InvalidGraphException("There already exists an edge with id " + eachEdge.getId());
                }
                edgeIds.add(eachEdge.getId());
            }
        }
    }
}
