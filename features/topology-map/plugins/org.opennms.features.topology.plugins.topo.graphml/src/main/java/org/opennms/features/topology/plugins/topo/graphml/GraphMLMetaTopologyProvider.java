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
package org.opennms.features.topology.plugins.topo.graphml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLEdge;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphMLMetaTopologyProvider implements MetaTopologyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GraphMLTopologyProvider.class);

    private final GraphMLServiceAccessor m_serviceAccessor;

    private File graphMLFile;
    private final Map<String, GraphMLTopologyProvider> graphProvidersByNamespace = Maps.newLinkedHashMap();
    private final Map<VertexRef, List<VertexRef>> oppositeVertices = Maps.newLinkedHashMap();
    private BreadcrumbStrategy breadcrumbStrategy;

    public GraphMLMetaTopologyProvider(GraphMLServiceAccessor serviceAccessor) {
        m_serviceAccessor = Objects.requireNonNull(serviceAccessor);
    }

    private VertexRef getVertex(GraphMLNode node) {
        return graphProvidersByNamespace.values().stream()
                .map(provider -> provider.getCurrentGraph().getVertex(provider.getNamespace(), node.getId()))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public String getId() {
        return getGraphProviders().stream()
                .sorted(Comparator.comparing(GraphProvider::getNamespace))
                .map(GraphProvider::getNamespace)
                .collect(Collectors.joining(":"));
    }

    public void reload() throws IOException, InvalidGraphException {
        graphProvidersByNamespace.clear();
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
                final GraphMLTopologyProvider topoProvider = new GraphMLTopologyProvider(eachGraph, m_serviceAccessor);
                graphProvidersByNamespace.put(topoProvider.getNamespace(), topoProvider);
            }

            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                for (org.opennms.features.graphml.model.GraphMLEdge eachEdge : eachGraph.getEdges()) {
                    final VertexRef sourceVertex = getVertex(eachEdge.getSource());
                    final VertexRef targetVertex = getVertex(eachEdge.getTarget());
                    final String sourceNamespace = sourceVertex == null? null : sourceVertex.getNamespace();
                    final String targetNamespace = targetVertex == null? null : targetVertex.getNamespace();
                    if (!Objects.equals(sourceNamespace, targetNamespace)) {
                        List<VertexRef> opposites = oppositeVertices.get(sourceVertex);
                        if (opposites == null) {
                            opposites = Lists.newArrayList();
                            oppositeVertices.put(sourceVertex, opposites);
                        }
                        opposites.add(targetVertex);
                    }
                }
            }
            this.breadcrumbStrategy = getBreadcrumbStrategy(graphML);
        }
    }

    @Override
    public GraphProvider getDefaultGraphProvider() {
        return Iterables.getFirst(graphProvidersByNamespace.values(), null);
    }

    @Override
    public Collection<GraphProvider> getGraphProviders() {
        return Collections.unmodifiableCollection(graphProvidersByNamespace.values());
    }

    @Override
    public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
        return oppositeVertices.getOrDefault(vertexRef, Collections.emptyList());
    }

    @Override
    public GraphProvider getGraphProviderBy(String namespace) {
        return graphProvidersByNamespace.get(namespace);
    }

    @Override
    public BreadcrumbStrategy getBreadcrumbStrategy() {
        return breadcrumbStrategy == null ? BreadcrumbStrategy.NONE : breadcrumbStrategy;
    }

    public void setTopologyLocation(String filename) {
        this.graphMLFile = filename != null ? new File(filename) : null;
    }

    private void validate(GraphML graphML) throws InvalidGraphException {
        final Set<String> graphIds = new HashSet<>();
        final Map<String, Set<String>> nodeIdsByNamespace = new HashMap<>();
        final Map<String, Set<String>> edgeIdsByNamespace = new HashMap<>();

        for (GraphMLGraph eachGraph : graphML.getGraphs()) {
            final String ns = eachGraph.getProperty(GraphMLProperties.NAMESPACE);
            if (Strings.isNullOrEmpty(ns)) {
                throw new InvalidGraphException("No namespace defined on graph with id " + eachGraph.getId());
            }

            if (graphIds.contains(eachGraph.getId())) {
                throw new InvalidGraphException("There already exists a graph with id " + eachGraph.getId());
            }
            graphIds.add(eachGraph.getId());

            for (GraphMLNode eachNode : eachGraph.getNodes()) {
                Set<String> nodeIdsInNs = nodeIdsByNamespace.get(ns);
                if (nodeIdsInNs == null) {
                    nodeIdsInNs = new HashSet<>();
                    nodeIdsByNamespace.put(ns, nodeIdsInNs);
                }

                if (nodeIdsInNs.contains(eachNode.getId())) {
                    throw new InvalidGraphException("There already exists a node with id " + eachNode.getId()
                        + " in namespace " + ns);
                }
                nodeIdsInNs.add(eachNode.getId());
            }

            for (GraphMLEdge eachEdge : eachGraph.getEdges()) {
                Set<String> edgeIdsInNs = edgeIdsByNamespace.get(ns);
                if (edgeIdsInNs == null) {
                    edgeIdsInNs = new HashSet<>();
                    edgeIdsByNamespace.put(ns, edgeIdsInNs);
                }

                if (edgeIdsInNs.contains(eachEdge.getId())) {
                    throw new InvalidGraphException("There already exists an edge with id " + eachEdge.getId()
                        + " in namespace " + ns);
                }
                edgeIdsInNs.add(eachEdge.getId());
            }
        }
    }

    public GraphMLTopologyProvider getGraphProvider(String vertexNamespace) {
        return graphProvidersByNamespace.get(vertexNamespace);
    }

    private static BreadcrumbStrategy getBreadcrumbStrategy(GraphML graphML) {
        Objects.requireNonNull(graphML);
        return getBreadcrumbStrategy((String) graphML.getProperty("breadcrumb-strategy"));
    }

    static BreadcrumbStrategy getBreadcrumbStrategy(String property) {
        if (Strings.isNullOrEmpty(property)) {
            return null;
        }
        try {
            return BreadcrumbStrategy.valueOf(property.toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOG.warn("No breadcrumb-strategy found for value '{}'", property, ex);
            return null;
        }
    }
}
