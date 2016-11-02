

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.TopologyService;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.opennms.features.topology.app.internal.jung.D3TopoLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.LayoutOperation;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class DefaultTopologyService implements TopologyService {

    private static final LayoutAlgorithm DEFAULT_LAYOUT_ALGORITHM = new D3TopoLayoutAlgorithm();

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTopologyService.class);

    private final BundleContext bundleContext;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final List<MetaTopologyProvider> providers = Lists.newArrayList();

    public DefaultTopologyService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newFixedThreadPool(Math.min(10, providers.size()));
                List<Future> futures = Lists.newArrayList();
                providers.forEach(eachProvider -> {
                    futures.add(executorService.submit(() -> {
                        eachProvider.reload();
                    }));
                });
                futures.forEach(eachFuture -> {
                    try {
                        eachFuture.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 5, 30, TimeUnit.SECONDS);
    }

    @Override
    public Graph getGraph(String metaTopologyId, String namespace, Criteria[] criteria, int semanticZoomLevel) {
        final GraphProvider graphProvider = findBy(metaTopologyId, namespace);

        // Determine visible vertices and edges
    	final List<Vertex> displayVertices = new ArrayList<>();
    	for(Vertex v : graphProvider.getVertices(criteria)) {
    		int vzl = graphProvider.getSemanticZoomLevel(v);
    		if (vzl == semanticZoomLevel || (vzl < semanticZoomLevel && !graphProvider.hasChildren(v))) {
    			displayVertices.add(v);
			}
    	}
    	final Collection<Edge> displayEdges = graphProvider.getEdges(criteria);

        // Determine default layout
        final String preferredLayout = graphProvider.getDefaults().getPreferredLayout();
        final LayoutAlgorithm layoutAlgorithm = findLayoutAlgorithm(preferredLayout);

        // create graph object
        final DefaultGraph graph = new DefaultGraph(displayVertices, displayEdges);
        final Layout layout = new DefaultLayout(graph);
        graph.setLayout(layout);
        graph.setLayoutAlgorithm(layoutAlgorithm);

        // Calculate status
        final StatusProvider vertexStatusProvider = findVertexStatusProvider(graphProvider);
        final EdgeStatusProvider edgeStatusProvider = findEdgeStatusProvider(graphProvider);
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
        return findBy(metaTopologyId, namespace);
    }

    @Override
    public MetaTopologyProvider getMetaTopologyProvider(String metaTopologyId) {
        Optional<MetaTopologyProvider> metaTopologyProviderOptional = providers.stream().filter(metaTopologyProvider -> metaTopologyId.equals(metaTopologyProvider.getId())).findFirst();
        MetaTopologyProvider metaTopologyProvider = metaTopologyProviderOptional.orElseThrow(() -> new NoSuchElementException("No MetaTopologyProvider with id '" + metaTopologyId + "' found."));
        return metaTopologyProvider;
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

    private GraphProvider findBy(String metaTopologyId, String namespace) {
        MetaTopologyProvider metaTopologyProvider = getMetaTopologyProvider(metaTopologyId);
        Optional<GraphProvider> graphProviderOptional = metaTopologyProvider.getGraphProviders().stream().filter(graphProvider -> graphProvider.getNamespace().equals(namespace)).findFirst();
        return graphProviderOptional.orElseThrow(() -> new NoSuchElementException("No GraphProvider with namespace '" + namespace + "' found."));
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
