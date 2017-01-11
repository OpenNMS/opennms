

package org.opennms.features.topology.app.internal.service;

import static org.opennms.features.topology.app.internal.service.BundleContextUtils.findSingleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.TopologyService;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.jung.D3TopoLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.LayoutOperation;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

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

    private final List<MetaTopologyProvider> providers = Lists.newArrayList();

    private final LoadingCache<GraphProviderKey, GraphProvider> graphProviderCache;

    private BundleContext bundleContext;

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
                        metaTopologyProvider.reload(key.getNamespace());
                        return Optional.ofNullable(metaTopologyProvider.getGraphProviderBy(key.getNamespace()))
                                .orElseThrow(() -> new NoSuchElementException("No GraphProvider with namespace '" + key.getNamespace() + "' found."));
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
        final StatusProvider vertexStatusProvider = bundleContext != null ? findVertexStatusProvider(graphProvider) : null;
        final EdgeStatusProvider edgeStatusProvider = bundleContext != null ? findEdgeStatusProvider(graphProvider) : null;
        if (vertexStatusProvider != null && vertexStatusProvider.contributesTo(graphProvider.getNamespace())) {
            graph.setVertexStatus(vertexStatusProvider.getStatusForVertices(graphProvider, new ArrayList<>(displayVertices), criteria));
        }
        if(edgeStatusProvider != null && edgeStatusProvider.contributesTo(graphProvider.getNamespace())) {
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
        final LayoutAlgorithm preferredLayoutAlgorithm = bundleContext != null ? findLayoutAlgorithm(preferredLayout) : DEFAULT_LAYOUT_ALGORITHM;

        return preferredLayoutAlgorithm;
    }

    @Override
    public MetaTopologyProvider getMetaTopologyProvider(String metaTopologyId) {
        Optional<MetaTopologyProvider> metaTopologyProviderOptional = providers.stream().filter(metaTopologyProvider -> metaTopologyId.equals(metaTopologyProvider.getId())).findFirst();
        MetaTopologyProvider metaTopologyProvider = metaTopologyProviderOptional.orElseThrow(() -> new NoSuchElementException("No MetaTopologyProvider with id '" + metaTopologyId + "' found."));
        return metaTopologyProvider;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public synchronized void onBind(MetaTopologyProvider provider, Map<?, ?> metaData) {
        try {
            LOG.debug("Adding meta topology provider: " + provider);
            providers.add(provider);
        } catch (Throwable e) {
            LOG.warn("Exception during onGraphProviderBind()", e);
        }
    }

    public synchronized void onUnbind(MetaTopologyProvider provider, Map<?, ?> metaData) {
        try {
            LOG.debug("Adding meta topology provider: " + provider);
            providers.remove(provider);
        } catch (Throwable e) {
            LOG.warn("Exception during onGraphProviderBind()", e);
        }
    }

    private LayoutAlgorithm findLayoutAlgorithm(String preferredLayout) {
        if (preferredLayout != null) {
            // LayoutOperations are exposed as CheckedOperations
            CheckedOperation operation = findSingleService(bundleContext, CheckedOperation.class, null, String.format("(operation.label=%s*)", preferredLayout));
            if (operation instanceof LayoutOperation) { // Cast it to LayoutOperation if possible
                return ((LayoutOperation) operation).getLayoutAlgorithm();
            }
        }
        LOG.warn("No preferredLayout defined. Fallback to {}", DEFAULT_LAYOUT_ALGORITHM.getClass().getSimpleName());
        return DEFAULT_LAYOUT_ALGORITHM; // no preferredLayout defined
    }

    private StatusProvider findVertexStatusProvider(GraphProvider graphProvider) {
        StatusProvider vertexStatusProvider = findSingleService(
                bundleContext,
                StatusProvider.class,
                statusProvider -> statusProvider.contributesTo(graphProvider.getNamespace()),
                null);
        return vertexStatusProvider;
    }

    private EdgeStatusProvider findEdgeStatusProvider(GraphProvider graphProvider) {
        EdgeStatusProvider edgeStatusProvider = findSingleService(
                bundleContext,
                EdgeStatusProvider.class,
                statusProvider -> statusProvider.contributesTo(graphProvider.getNamespace()),
                null);
        return edgeStatusProvider;
    }
}
