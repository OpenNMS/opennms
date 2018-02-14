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

package org.opennms.features.topology.app.internal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.TopologyService;
import org.opennms.features.topology.api.support.ServiceLocator;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.jung.D3TopoLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.LayoutOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author mvrueden
 */
public class DefaultTopologyService implements TopologyService {

    /**
     * Key class for the GraphProviderCache.
     */
    private static class GraphProviderKey {
        private final String metaTopologyId;
        private final String namespace;

        public GraphProviderKey(String metaTopologyId, String namespace) {
            this.metaTopologyId = metaTopologyId;
            this.namespace = namespace;
        }

        public String getMetaTopologyId() {
            return metaTopologyId;
        }

        public String getNamespace() {
            return namespace;
        }

        @Override
        public int hashCode() {
            return Objects.hash(metaTopologyId, namespace);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof GraphProviderKey)) {
                return false;
            }
            final GraphProviderKey key = (GraphProviderKey) o;
            return Objects.equals(metaTopologyId, key.metaTopologyId)
                    && Objects.equals(namespace, key.namespace);
        }
    }

    private static final LayoutAlgorithm DEFAULT_LAYOUT_ALGORITHM = new D3TopoLayoutAlgorithm();

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTopologyService.class);

    private final LoadingCache<GraphProviderKey, GraphProvider> graphProviderCache;

    private ServiceLocator serviceLocator;

    public DefaultTopologyService() {
        this(30);
    }

    /**
     * @param cacheTimeout The cache timeout in seconds.
     *                     0 disables caching.
     */
    public DefaultTopologyService(long cacheTimeout) {
        graphProviderCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(Math.max(0, cacheTimeout), TimeUnit.SECONDS)
                // Due to graphml we may end up having a ton of graph providers.
                // Caching is limited to 50, which is still plenty
                .maximumSize(50)
                .build(new CacheLoader<GraphProviderKey, GraphProvider>() {
                    @Override
                    public GraphProvider load(GraphProviderKey key) throws Exception {
                        final MetaTopologyProvider metaTopologyProvider = getMetaTopologyProvider(key.getMetaTopologyId());
                        final GraphProvider graphProvider = Optional.ofNullable(metaTopologyProvider.getGraphProviderBy(key.getNamespace()))
                                                                .orElseThrow(() -> new NoSuchGraphProviderException(key.getMetaTopologyId(), key.getNamespace()));
                        graphProvider.refresh();
                        return graphProvider;
                    }
                });
    }

    @Override
    public Graph getGraph(String metaTopologyId, String namespace, Criteria[] criteria, int semanticZoomLevel) {
        Objects.requireNonNull(metaTopologyId);
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(metaTopologyId);
        Objects.requireNonNull(criteria);
        if (semanticZoomLevel < 0) {
            LOG.warn("Semantic Zoom Level was {}. Only values >= 0 are allowed, forcing it to be 1", semanticZoomLevel);
            semanticZoomLevel = 0;
        }
        final GraphProvider graphProvider = getGraphProvider(metaTopologyId, namespace);

        // Determine visible vertices and edges
        final List<Vertex> displayVertices = new ArrayList<>();
        for(Vertex v : graphProvider.getVertices(criteria)) {
            int vzl = graphProvider.getSemanticZoomLevel(v);
            if (vzl == semanticZoomLevel || (vzl < semanticZoomLevel && !graphProvider.hasChildren(v))) {
                displayVertices.add(v);
            }
        }
        final Collection<Edge> displayEdges = graphProvider.getEdges(criteria);

        // Create graph object
        final DefaultGraph graph = new DefaultGraph(displayVertices, displayEdges);

        // Calculate status
        final StatusProvider vertexStatusProvider = serviceLocator != null ? findVertexStatusProvider(graphProvider) : null;
        final EdgeStatusProvider edgeStatusProvider = serviceLocator != null ? findEdgeStatusProvider(graphProvider) : null;
        if (vertexStatusProvider != null) {
            graph.setVertexStatus(vertexStatusProvider.getStatusForVertices(graphProvider, new ArrayList<>(displayVertices), criteria));
        }
        if(edgeStatusProvider != null) {
            graph.setEdgeStatus(edgeStatusProvider.getStatusForEdges(graphProvider, new ArrayList<>(graph.getDisplayEdges()), criteria));
        }
        return graph;
    }

    @Override
    public GraphProvider getGraphProvider(String metaTopologyId, String namespace) {
        Objects.requireNonNull(metaTopologyId);
        Objects.requireNonNull(namespace);
        try {
            return graphProviderCache.get(new GraphProviderKey(metaTopologyId, namespace));
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    // Determines default layout
    @Override
    public LayoutAlgorithm getPreferredLayoutAlgorithm(String metaTopologyId, String namespace) {
        Objects.requireNonNull(metaTopologyId);
        Objects.requireNonNull(namespace);

        final GraphProvider graphProvider = getGraphProvider(metaTopologyId, namespace);
        final String preferredLayout = graphProvider.getDefaults().getPreferredLayout();
        final LayoutAlgorithm preferredLayoutAlgorithm = serviceLocator != null ? findLayoutAlgorithm(preferredLayout) : DEFAULT_LAYOUT_ALGORITHM;

        return preferredLayoutAlgorithm;
    }

    @Override
    public MetaTopologyProvider getMetaTopologyProvider(String metaTopologyId) throws NoSuchProviderException {
        Optional<MetaTopologyProvider> metaTopologyProviderOptional = serviceLocator.findServices(MetaTopologyProvider.class, null)
                .stream()
                .filter(metaTopologyProvider -> metaTopologyId.equals(metaTopologyProvider.getId()))
                .findFirst();
        MetaTopologyProvider metaTopologyProvider = metaTopologyProviderOptional.orElseThrow(() -> new NoSuchMetaTopologyProvider(metaTopologyId));
        return metaTopologyProvider;
    }

    @Override
    public boolean isCategoryAware(String namespace) {
        final List<MetaTopologyProvider> providers = serviceLocator.findServices(MetaTopologyProvider.class, null);
        final Optional<GraphProvider> provider = providers.stream().flatMap(meta -> meta.getGraphProviders().stream()).filter(p -> p.getNamespace().equals(namespace)).findFirst();
        if (provider.isPresent()) {
            return provider.get().getTopologyProviderInfo().isSupportsCategorySearch();
        }
        return false;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
    }

    public void invalidate(String namespace) {
        // Tt the moment the namespace of each topology must be unique overall meta topology providers, even if they
        // are encapsulated by the meta topology provider. It should be addressed by <metaId>:<namespace>.
        // This is at the moment not the case, therefore we iterate over all meta topology providers and invalidate
        // The cache if the meta topology provider has a graph with the given namespace. In the future this should
        // Be handled differently.
        serviceLocator.findServices(MetaTopologyProvider.class, null).stream()
            .filter(metaTopologyProvider -> metaTopologyProvider.getGraphProviderBy(namespace) != null)
            .forEach(metaTopologyProvider ->  {
                graphProviderCache.invalidate(new GraphProviderKey(metaTopologyProvider.getId(), namespace));
            });
    }

    public void invalidateAll() {
        graphProviderCache.invalidateAll();
    }

    private LayoutAlgorithm findLayoutAlgorithm(String preferredLayout) {
        if (preferredLayout != null) {
            // LayoutOperations are exposed as CheckedOperations
            CheckedOperation operation = serviceLocator.findSingleService(CheckedOperation.class, null, String.format("(operation.label=%s*)", preferredLayout));
            if (operation instanceof LayoutOperation) { // Cast it to LayoutOperation if possible
                return ((LayoutOperation) operation).getLayoutAlgorithm();
            }
        }
        LOG.warn("No preferredLayout defined. Fallback to {}", DEFAULT_LAYOUT_ALGORITHM.getClass().getSimpleName());
        return DEFAULT_LAYOUT_ALGORITHM; // no preferredLayout defined
    }

    private StatusProvider findVertexStatusProvider(GraphProvider graphProvider) {
        StatusProvider vertexStatusProvider = serviceLocator.findSingleService(
                StatusProvider.class,
                statusProvider -> statusProvider.contributesTo(graphProvider.getNamespace()),
                null);
        return vertexStatusProvider;
    }

    private EdgeStatusProvider findEdgeStatusProvider(GraphProvider graphProvider) {
        EdgeStatusProvider edgeStatusProvider = serviceLocator.findSingleService(
                EdgeStatusProvider.class,
                statusProvider -> statusProvider.contributesTo(graphProvider.getNamespace()),
                null);
        return edgeStatusProvider;
    }
}
