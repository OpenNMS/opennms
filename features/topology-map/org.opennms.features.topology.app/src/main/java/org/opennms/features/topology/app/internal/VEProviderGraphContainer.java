/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.features.topology.api.AutoRefreshSupport;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.IconManager;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyService;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.support.SemanticZoomLevelCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.jung.FRLayoutAlgorithm;
import org.opennms.features.topology.app.internal.service.DefaultGraph;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.osgi.VaadinApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.data.Property;

public class VEProviderGraphContainer implements GraphContainer, VertexListener, EdgeListener, ServiceListener {


    @SuppressWarnings("serial")
    public static class ScaleProperty implements Property<Double>, Property.ValueChangeNotifier{
        private Double m_scale;
        private Set<ValueChangeListener> m_listeners = new CopyOnWriteArraySet<>();
        
        public ScaleProperty(double scale) {
            m_scale = scale;
        }
        
        @Override
        public void addListener(ValueChangeListener listener) {
            m_listeners.add(listener);
        }

        @Override
        public void removeListener(ValueChangeListener listener) {
            m_listeners.remove(listener);
        }

        @Override
        public void addValueChangeListener(ValueChangeListener listener) {
            m_listeners.add(listener);
        }

        @Override
        public void removeValueChangeListener(ValueChangeListener listener) {
            m_listeners.remove(listener);
        }

        @Override
        public Double getValue() {
            return m_scale;
        }

        @Override
        public void setValue(Double newValue) {
            double oldScale = m_scale;
            m_scale = ((Number) newValue).doubleValue();
            if(oldScale != m_scale) {
                fireValueChange();
            }
        }

        private void fireValueChange() {
            ValueChangeEvent event = new ValueChangeEvent() {

                @Override
                public Property<Double> getProperty() {
                    return ScaleProperty.this;
                }
            };
            for(ValueChangeListener listener : m_listeners) {
                listener.valueChange(event);
            }
        }

        @Override
        public Class<Double> getType() {
            return Double.class;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setReadOnly(boolean newStatus) {
            
        }
        
    }

    private static final Logger s_log = LoggerFactory.getLogger(VEProviderGraphContainer.class);

    private int m_semanticZoomLevel = 1;
    private Property<Double> m_scaleProperty = new ScaleProperty(0.0);
    private LayoutAlgorithm m_layoutAlgorithm;
    private SelectionManager m_selectionManager;
    private IconManager m_iconManager;
    private MapViewManager m_viewManager = new DefaultMapViewManager();
    private VaadinApplicationContext applicationContext;
    private BundleContext m_bundleContext;
    private Set<ChangeListener> m_listeners = new CopyOnWriteArraySet<>();
    private AutoRefreshSupport m_autoRefreshSupport;
    private TopologyService m_topologyService;
    private Graph m_graph;
    private LayoutManager m_layoutManager;
    
    private AtomicBoolean m_containerDirty = new AtomicBoolean(Boolean.TRUE);
    private TopologyServiceClient m_topologyServiceClient;

    public VEProviderGraphContainer() {
        m_graph = new DefaultGraph(Lists.newArrayList(), Lists.newArrayList());
        m_layoutAlgorithm = new FRLayoutAlgorithm();
        setDirty(false);
        resetCriteriaDirty();
    }

    @Override
    public int getSemanticZoomLevel() {
        return m_semanticZoomLevel;
    }

    @Override
    public void setSemanticZoomLevel(int level) {
        int oldLevel = m_semanticZoomLevel;
        m_semanticZoomLevel = level;

        // Also set the SZL in a Criteria attached to the container so that we can
        // use the value to optimize some GraphProvider calls
        SemanticZoomLevelCriteria criteria = getSemanticZoomLevelCriteriaForContainer(this);
        criteria.setSemanticZoomLevel(level);

        if(oldLevel != m_semanticZoomLevel) {
            setDirty(true);
        }
    }

	public static SemanticZoomLevelCriteria getSemanticZoomLevelCriteriaForContainer(GraphContainer graphContainer) {
		Criteria[] criteria = graphContainer.getCriteria();
		if (criteria != null) {
			for (Criteria criterium : criteria) {
				try {
					return (SemanticZoomLevelCriteria)criterium;
				} catch (ClassCastException e) {}
			}
		}

		SemanticZoomLevelCriteria hopCriteria = new SemanticZoomLevelCriteria(graphContainer.getSemanticZoomLevel());
		graphContainer.addCriteria(hopCriteria);
		return hopCriteria;
	}

    @Override
    public double getScale() {
        return m_scaleProperty.getValue();
    }
    
    @Override
    public Property<Double> getScaleProperty() {
        return m_scaleProperty;
    }
    
    @Override
    public void setScale(double scale) {
        m_scaleProperty.setValue(scale);
    }

    @Override
    public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
        if(m_layoutAlgorithm != layoutAlgorithm) {
            m_layoutAlgorithm = layoutAlgorithm;
            setDirty(true);
        }
    }

    @Override
    public LayoutAlgorithm getLayoutAlgorithm() {
        return m_layoutAlgorithm;
    }

    @Override
    public void redoLayout() {
        s_log.debug("redoLayout()");
        getGraph();
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(m_graph);
            fireGraphChanged();
        }
    }

    @Override
    public void setMetaTopologyId(String metaTopologyId) {
        getTopologyServiceClient().setMetaTopologyId(metaTopologyId);
        setDirty(true);
    }

    @Override
    public String getMetaTopologyId() {
        return getTopologyServiceClient().getMetaTopologyId();
    }

    @Override
    public TopologyServiceClient getTopologyServiceClient() {
        if (m_topologyServiceClient == null) {
            m_topologyServiceClient = new DefaultTopologyServiceClient(m_topologyService);
        }
        return m_topologyServiceClient;
    }

    @Override
    public SelectionManager getSelectionManager() {
        return m_selectionManager;
    }

    @Override
    public void setSelectionManager(SelectionManager selectionManager) {
        m_selectionManager = selectionManager;
    }

    // Remove all vertices from focus which are not visible
    private void removeVerticesWhichAreNotVisible(final Collection<Vertex> displayVertices) {
        for(Criteria criteria : getCriteria()) {
            if (criteria instanceof VertexHopCriteria
                    // CollapsibleCriteria may contain not visible vertices (when collapsed)
                    // and multiple collapsible criteria may contain the same vertices.
                    // We do not remove them manually for now
                    && !(criteria instanceof CollapsibleCriteria)) {
                final VertexHopCriteria hopCriteria = (VertexHopCriteria) criteria;
                for(VertexRef vRef : hopCriteria.getVertices()){
                    if(!displayVertices.contains(vRef)){
                        removeCriteria(hopCriteria);
                    }
                }
            }
        }
    }

    // we have to find out if each selected vertex/edge is still displayable, if not we deselect it.
    private static void unselectElementsWhichAreNotVisibleAnymore(Graph graph, SelectionManager selectionManager) {
        if (selectionManager == null) return;
        List<VertexRef> selectedVertexRefs = new ArrayList<>(selectionManager.getSelectedVertexRefs());
        List<VertexRef> newSelectedVertexRefs = new ArrayList<>();
        for (VertexRef eachSelectedVertex : selectedVertexRefs) {
            for (Vertex eachDisplayableVertex : graph.getDisplayVertices()) {
                if (eachDisplayableVertex.getNamespace().equals(eachSelectedVertex.getNamespace())
                    && eachDisplayableVertex.getId().equals(eachSelectedVertex.getId())) {
                    newSelectedVertexRefs.add(eachSelectedVertex);
                    break;
                }
            }
        }

        List<EdgeRef> selectedEdgeRefs = new ArrayList<>(selectionManager.getSelectedEdgeRefs());
        List<EdgeRef> newSelectedEdgeRefs = new ArrayList<>();
        for (EdgeRef eachSelectedEdgeRef : selectedEdgeRefs) {
            for (Edge eachDisplayableEdge : graph.getDisplayEdges()) {
                if (eachDisplayableEdge.getNamespace().equals(eachSelectedEdgeRef.getNamespace())
                        && eachDisplayableEdge.getId().equals(eachSelectedEdgeRef.getId())) {
                    newSelectedEdgeRefs.add(eachSelectedEdgeRef);
                    break;
                }
            }
        }

        // if the selection changed, inform selectionManager
        if (!newSelectedVertexRefs.equals(selectedVertexRefs)) {
            selectionManager.setSelectedVertexRefs(newSelectedVertexRefs);
        }
        if (!newSelectedEdgeRefs.equals(selectedEdgeRefs)) {
            selectionManager.setSelectedEdgeRefs(newSelectedEdgeRefs);
        }
    }

    private void rebuildGraph() {
        Graph oldGraph = m_graph;
        m_graph = getTopologyServiceClient().getGraph(getCriteria(), getSemanticZoomLevel());
        unselectElementsWhichAreNotVisibleAnymore(m_graph, m_selectionManager);
        removeVerticesWhichAreNotVisible(m_graph.getDisplayVertices());

        // We must restore the old layout, otherwise the layout is in a weird state
        if (oldGraph != null) {
            m_graph.setLayout(oldGraph.getLayout());
            m_graph.getLayout().updateLocations(new ArrayList<>(m_graph.getDisplayVertices()));
        }
    }

    @Override
    public Graph getGraph() {
        synchronized(m_containerDirty) {
            if ((isDirty() || isCriteriaDirty()) && getTopologyServiceClient().getNamespace() != null) {
                rebuildGraph();
                setDirty(false);
                resetCriteriaDirty();
            }
        }
        return m_graph;
    }

    private final Set<Criteria> m_criteria = new LinkedHashSet<>();

    @Override
    public void clearCriteria() {
        m_criteria.clear();
        setDirty(true);
    }

	@Override
	public Criteria[] getCriteria() {
		return m_criteria.toArray(new Criteria[0]);
	}

	@Override
	public void addCriteria(Criteria criteria) {
        if (criteria != null) {
		    m_criteria.add(criteria);
            setDirty(true);
        }
	}

	@Override
	public void removeCriteria(Criteria criteria) {
		m_criteria.remove(criteria);
		setDirty(true);
	}

    public void setBundleContext(final BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    public void setTopologyService(final TopologyService topologyService) {
        m_topologyService = topologyService;
    }

    public void setLayoutManager(LayoutManager layoutManager) {
        m_layoutManager = layoutManager;
    }

    @Override
	public void fireGraphChanged() {
		for(ChangeListener listener : m_listeners) {
			listener.graphChanged(this);
		}
	}

    @Override
    public <T extends Criteria> Set<T> findCriteria(Class<T> criteriaType) {
        Objects.requireNonNull(criteriaType);
        final Set<T> criteriaSet = new HashSet<>();
        for (Criteria eachCriteria : getCriteria()) {
            if (criteriaType.isAssignableFrom(eachCriteria.getClass())) {
                criteriaSet.add((T) eachCriteria);
            }
        }
        return criteriaSet;
    }

    @Override
    public <T extends Criteria> T findSingleCriteria(Class<T> criteriaType) {
        if (criteriaType == null) {
            return null;
        }
        Set<T> criteriaSet = findCriteria(criteriaType);
        if (!criteriaSet.isEmpty()) {
            if (criteriaSet.size() > 1) {
                s_log.warn("Found more than one criteria of type {}. Returning first.", criteriaType);
            }
            return criteriaSet.iterator().next();
        }

        // not found
        return null;
    }

    @Override
    public IconManager getIconManager() {
        return m_iconManager;
    }

    @Override
    public void setIconManager(IconManager iconManager) {
        m_iconManager = iconManager;
    }

    @Override
    public void selectTopologyProvider(GraphProvider graphProvider, Callback... callbacks) {
        // Do not get the graph here, as it is received when invoking the "redoLayout" method.
        setSelectedNamespace(graphProvider.getNamespace());
        setLayoutAlgorithm(getTopologyServiceClient().getPreferredLayoutAlgorithm());
        if (callbacks != null) {
            for (Callback eachCallback : callbacks) {
                eachCallback.callback(this, graphProvider);
            }
        }
        redoLayout();
    }

    public void saveLayout() {
        m_layoutManager.persistLayout(this);
        fireGraphChanged();
    }

    @Override
	public void addChangeListener(ChangeListener listener) {
		m_listeners.add(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		m_listeners.remove(listener);
	}

	@Override
	public Collection<VertexRef> getVertexRefForest(Collection<VertexRef> vertexRefs) {
		Set<VertexRef> processed = new LinkedHashSet<>();
		for(VertexRef vertexRef : vertexRefs) {
			addRefTreeToSet(getTopologyServiceClient(), vertexRef, processed, getCriteria());
		}
		return processed;
	}

    @Override
    public void setSelectedNamespace(String namespace) {
        getTopologyServiceClient().setNamespace(namespace);
        setDirty(true);
    }

    private static void addRefTreeToSet(TopologyServiceClient topologyServiceClient, VertexRef vertexId, Set<VertexRef> processed, Criteria[] criteria) {
		processed.add(vertexId);

		for(VertexRef childId : topologyServiceClient.getChildren(vertexId, criteria)) {
			if (!processed.contains(childId)) {
				addRefTreeToSet(topologyServiceClient, childId, processed, criteria);
			}
		}
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider) {
		setDirty(true);
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider,
			Collection<? extends Edge> added, Collection<? extends Edge> updated,
			Collection<String> removedEdgeIds) {
		setDirty(true);
	}

	@Override
	public void vertexSetChanged(VertexProvider provider) {
		setDirty(true);
	}

	@Override
	public void vertexSetChanged(VertexProvider provider,
			Collection<? extends Vertex> added, Collection<? extends Vertex> update,
			Collection<String> removedVertexIds) {
		setDirty(true);
	}

    @Override
    public MapViewManager getMapViewManager() {
        return m_viewManager;
    }

    @Override
    public String getSessionId() {
        return applicationContext.getSessionId();
    }

    private void setSessionId(String sessionId) {
        try {
            m_bundleContext.removeServiceListener(this);
            m_bundleContext.addServiceListener(this, String.format("(&(objectClass=%s)(sessionId=%s))", "org.opennms.features.topology.api.topo.Criteria", sessionId));
        } catch (InvalidSyntaxException e) {
            LoggerFactory.getLogger(getClass()).error("registerServiceListener() failed", e);
        }
    }

    @Override
    public VaadinApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(VaadinApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        setSessionId(applicationContext.getSessionId());
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        ServiceReference<Criteria> serviceReference;
        Criteria criteria;
        switch(event.getType()) {
            case ServiceEvent.REGISTERED:
                serviceReference = (ServiceReference<Criteria>) event.getServiceReference();
                criteria = m_bundleContext.getService(serviceReference);
                addCriteria(criteria);
                break;

            case ServiceEvent.UNREGISTERING:
                serviceReference = (ServiceReference<Criteria>) event.getServiceReference();
                criteria = m_bundleContext.getService(serviceReference);
                removeCriteria(criteria);
                break;
        }
    }

    public AutoRefreshSupport getAutoRefreshSupport() {
        return m_autoRefreshSupport;
    }

    public boolean hasAutoRefreshSupport() {
        return m_autoRefreshSupport != null;
    }

    public void setAutoRefreshSupport(AutoRefreshSupport autoRefreshSupport) {
        m_autoRefreshSupport = autoRefreshSupport;
    }

    @Override
    public void setDirty(boolean isDirty) {
        m_containerDirty.set(isDirty);
    }

    private boolean isDirty() {
        return m_containerDirty.get();
    }

    private boolean isCriteriaDirty() {
        for (Criteria eachCriteria : m_criteria) {
            if (eachCriteria.isDirty()) {
                return true;
            }
        }
        return false;
    }

    private void resetCriteriaDirty() {
        for (Criteria eachCriteria : m_criteria) {
            eachCriteria.resetDirty();
        }
    }
}
