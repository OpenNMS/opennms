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

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.features.topology.api.AutoRefreshSupport;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.support.SemanticZoomLevelCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

public class VEProviderGraphContainer implements GraphContainer, VertexListener, EdgeListener, ServiceListener {



    @SuppressWarnings("serial")
    public static class ScaleProperty implements Property<Double>, Property.ValueChangeNotifier{
        private Double m_scale;
        private Set<ValueChangeListener> m_listeners = new CopyOnWriteArraySet<Property.ValueChangeListener>();
        
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
    
    public static class PseudoEdge extends AbstractEdge {

        public PseudoEdge(String namespace, String id, String styleName, Vertex source, Vertex target) {
            super(namespace, id, source, target);
            setLabel(source.getLabel() + " :: " + target.getLabel());
            setStyleName(styleName);
        }

        @Override
        public Item getItem() {
            return new BeanItem<PseudoEdge>(this);
        }

    }

    public class VEGraph implements Graph {

    	private final Set<Vertex> m_displayVertices = new TreeSet<Vertex>(new RefComparator());
    	private final Set<Edge> m_displayEdges = new TreeSet<Edge>(new RefComparator());
    	private final Layout m_layout;
    	
        public VEGraph(Collection<Vertex> displayVertices, Collection<Edge> displayEdges) {
        	m_layout = new DefaultLayout(VEProviderGraphContainer.this);
        	updateLayout(displayVertices, displayEdges);
		}

		@Override
        public Layout getLayout() {
			return m_layout;
        }

        @Override
        public Collection<Vertex> getDisplayVertices() {
        	return Collections.unmodifiableCollection(m_displayVertices);
        }

        @Override
        public Collection<Edge> getDisplayEdges() {
        	return Collections.unmodifiableCollection(m_displayEdges);
        }

        @Override
        public Edge getEdgeByKey(String edgeKey) {
        	for(Edge e : m_displayEdges) {
        		if (edgeKey.equals(e.getKey())) {
        			return e;
        		}
        	}
        	return null;
        }

        @Override
        public Vertex getVertexByKey(String vertexKey) {
        	for(Vertex v : m_displayVertices) {
        		if (vertexKey.equals(v.getKey())) {
        			return v;
        		}
        	}
        	return null;
        }

        @Override
        public void visit(GraphVisitor visitor) throws Exception {
        	
        	visitor.visitGraph(this);
        	
        	for(Vertex v : m_displayVertices) {
        		visitor.visitVertex(v);
        	}
        	
        	for(Edge e : m_displayEdges) {
        		visitor.visitEdge(e);
        	}
        	
        	visitor.completeGraph(this);
        }

		public void updateLayout(Collection<Vertex> displayVertices, Collection<Edge> displayEdges) {
			m_displayVertices.clear();
			m_displayVertices.addAll(displayVertices);
			m_displayEdges.clear();
			m_displayEdges.addAll(displayEdges);
			for (Iterator<Edge> itr = m_displayEdges.iterator(); itr.hasNext();) {
				Edge edge = itr.next();
				if (new RefComparator().compare(edge.getSource().getVertex(), edge.getTarget().getVertex()) == 0) {
					s_log.debug("Discarding edge whose source and target are the same: {}", edge);
					itr.remove();
				} else if (m_displayVertices.contains(edge.getSource().getVertex())) {
					if (m_displayVertices.contains(edge.getTarget().getVertex())) {
						// This edge is OK, it is attached to two vertices that are in the graph
					} else {
						s_log.debug("Discarding edge that is not attached to 2 vertices in the graph: {}", edge);
						itr.remove();
					}
				} else {
					s_log.debug("Discarding edge that is not attached to 2 vertices in the graph: {}", edge);
					itr.remove();
				}
			}
			s_log.debug("Created a graph with {} vertices and {} edges", m_displayVertices.size(), m_displayEdges.size());
		}
    }

    private static final Logger s_log = LoggerFactory.getLogger(VEProviderGraphContainer.class);

    private int m_semanticZoomLevel = 1;
    private Property<Double> m_scaleProperty = new ScaleProperty(0.0);
    private LayoutAlgorithm m_layoutAlgorithm;
    private SelectionManager m_selectionManager;
    private StatusProvider m_statusProvider;
    private Set<EdgeStatusProvider> m_edgeStatusProviders;
    private MergingGraphProvider m_mergedGraphProvider;
    private MapViewManager m_viewManager = new DefaultMapViewManager();
    private String m_sessionId;
    private BundleContext m_bundleContext;
    private Set<ChangeListener> m_listeners = new CopyOnWriteArraySet<ChangeListener>();
    private AutoRefreshSupport m_autoRefreshSupport;
    
    private VEGraph m_graph;
    private AtomicBoolean m_containerDirty = new AtomicBoolean(Boolean.TRUE);

    public VEProviderGraphContainer(GraphProvider graphProvider, ProviderManager providerManager) {
        m_mergedGraphProvider = new MergingGraphProvider(graphProvider, providerManager);
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
            redoLayout();
        }
    }

    @Override
    public LayoutAlgorithm getLayoutAlgorithm() {
        return m_layoutAlgorithm;
    }

    @Override
    public void redoLayout() {
        s_log.debug("redoLayout()");
        // Rebuild the graph vertices and edges if necessary
        getGraph();
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(this);
            fireGraphChanged();
        }
    }

    @Override
    public GraphProvider getBaseTopology() {
        return m_mergedGraphProvider.getBaseGraphProvider();
    }

    @Override
    public void setBaseTopology(GraphProvider graphProvider) {
        m_mergedGraphProvider.setBaseGraphProvider(graphProvider);
        setDirty(true);
    }
    
    @Override
    public void setVertexStatusProvider(StatusProvider statusProvider) {
        m_statusProvider = statusProvider;
        setDirty(true);
    }

    @Override
    public SelectionManager getSelectionManager() {
        return m_selectionManager;
    }

    @Override
    public void setSelectionManager(SelectionManager selectionManager) {
        m_selectionManager = selectionManager;
    }

    public void addVertexProvider(VertexProvider vertexProvider) {
        m_mergedGraphProvider.addVertexProvider(vertexProvider);
        setDirty(true);
    }

    public void removeVertexProvider(VertexProvider vertexProvider) {
        m_mergedGraphProvider.removeVertexProvider(vertexProvider);
        setDirty(true);
    }

    public void addEdgeProvider(EdgeProvider edgeProvider) {
        m_mergedGraphProvider.addEdgeProvider(edgeProvider);
        setDirty(true);
    }

    public void removeEdgeProvider(EdgeProvider edgeProvider) {
    	m_mergedGraphProvider.removeEdgeProvider(edgeProvider);
        setDirty(true);
    }

    private void rebuildGraph() {
    	
    	List<Vertex> displayVertices = new ArrayList<Vertex>();

    	for(Vertex v : m_mergedGraphProvider.getVertices(getCriteria())) {
    		int vzl = m_mergedGraphProvider.getSemanticZoomLevel(v);
    		if (vzl == getSemanticZoomLevel() || (vzl < getSemanticZoomLevel() && !m_mergedGraphProvider.hasChildren(v))) {
    			displayVertices.add(v);
			}
    	}

    	Collection<Edge> displayEdges = new HashSet<Edge>();
    	// This legacy grouping code mimics the CollapsibleCriteria behavior
    	if (m_mergedGraphProvider.groupingSupported()) {
    		for(Edge e : m_mergedGraphProvider.getEdges(getCriteria())) {
    			VertexRef source = e.getSource().getVertex();
    			VertexRef target = e.getTarget().getVertex();

    			VertexRef displaySource = getDisplayVertex(source);
    			VertexRef displayTarget = getDisplayVertex(target);
    			if (displaySource == null) {
    				s_log.debug("Discarding edge with null source: {}", e);
    			} else if (displayTarget == null) {
    				s_log.debug("Discarding edge with null target: {}", e);
    			} else if (refEquals(displaySource, displayTarget)) {
    				s_log.debug("Discarding edge with identical source and target: {}", e);
    			} else if (refEquals(source, displaySource) && refEquals(target, displayTarget)) {
    				// If the grouping display source and target are the same as the actual
    				// source and target (ie. the vertex is not part of a group) then just
    				// display the edge
    				displayEdges.add(e);
    			} else {
    				// we may need to create a pseudo edge to represent this edge
    				String pseudoId = pseudoId(displaySource, displayTarget);
    				PseudoEdge pEdge = new PseudoEdge("pseudo-"+e.getNamespace(), pseudoId, e.getStyleName(), m_mergedGraphProvider.getVertex(displaySource), m_mergedGraphProvider.getVertex(displayTarget));
    				//This is a hack to get around the device A to device Z label in NCS Path when going through groups
    				if(e.getStyleName().equals("ncs edge direct")){
    					pEdge.setTooltipText(e.getTooltipText());
    				}
    				displayEdges.add(pEdge);
    			}
    		}
    	} else {
    		displayEdges = m_mergedGraphProvider.getEdges(getCriteria());
    	}

        if (m_graph == null) {
            m_graph = new VEGraph(displayVertices, displayEdges);
        } else {
            m_graph.updateLayout(displayVertices, displayEdges);
        }

        unselectVerticesWhichAreNotVisibleAnymore(m_graph, m_selectionManager);

        for(Criteria criteria : getCriteria()){
            if(criteria instanceof VertexHopGraphProvider.FocusNodeHopCriteria){
                VertexHopGraphProvider.FocusNodeHopCriteria focusCriteria = (VertexHopGraphProvider.FocusNodeHopCriteria) criteria;
                List<VertexRef> vertexRefs = new LinkedList<VertexRef>();
                for(VertexRef vRef : focusCriteria.getVertices()){
                    if(!displayVertices.contains(vRef)){
                        vertexRefs.add(vRef);
                    }
                }
                focusCriteria.removeAll(vertexRefs);
            }
        }
    }

    // we have to find out if each selected vertex is still displayable,
    // if not we deselect it.
    private static void unselectVerticesWhichAreNotVisibleAnymore(Graph graph, SelectionManager selectionManager) {
        if (selectionManager == null) return;
        List<VertexRef> selectedVertexRefs = new ArrayList<VertexRef>(selectionManager.getSelectedVertexRefs());
        List<VertexRef> newSelectedVertexRefs = new ArrayList<VertexRef>();
        for (VertexRef eachSelectedVertex : selectedVertexRefs) {
            for (Vertex eachDisplayableVertex : graph.getDisplayVertices()) {
                if (eachDisplayableVertex.getNamespace().equals(eachSelectedVertex.getNamespace())
                    && eachDisplayableVertex.getId().equals(eachSelectedVertex.getId())) {
                    newSelectedVertexRefs.add(eachSelectedVertex);
                    break;
                }
            }
        }

        // if the selection changed, inform selectionManager
        if (!newSelectedVertexRefs.equals(selectedVertexRefs)) {
            selectionManager.setSelectedVertexRefs(newSelectedVertexRefs);
        }
    }

    private String pseudoId(VertexRef displaySource, VertexRef displayTarget) {
		String sourceId = displaySource.getNamespace()+":"+displaySource.getId();
		String targetId = displayTarget.getNamespace() + ":" + displayTarget.getId();
		
		String a = sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
		String b = sourceId.compareTo(targetId) < 0 ? targetId : sourceId;

		return "<" + a + ">-<" + b + ">";
	}
    
    private boolean refEquals(VertexRef a, VertexRef b) {
        return new RefComparator().compare(a, b) == 0;
    }
    
    private VertexRef getDisplayVertex(VertexRef vertexRef) {
		int szl = getSemanticZoomLevel();
		int vzl = m_mergedGraphProvider.getSemanticZoomLevel(vertexRef);
		if (vzl == szl || (vzl < szl && !m_mergedGraphProvider.hasChildren(vertexRef))) {
			return vertexRef;
		} else {
			Vertex parent = m_mergedGraphProvider.getParent(vertexRef);
			if (parent != null) {
				return getDisplayVertex(parent);
			} else {
				return null;
			}
		}
    }

    @Override
    public Graph getGraph() {
        synchronized(m_containerDirty) {
            if (isDirty() || isCriteriaDirty()) {
                rebuildGraph();
                setDirty(false);
                resetCriteriaDirty();
            }
        }
        return m_graph;
    }

    private final Set<Criteria> m_criteria = new LinkedHashSet<Criteria>();

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

    @Override
	public void fireGraphChanged() {
		for(ChangeListener listener : m_listeners) {
			listener.graphChanged(this);
		}
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
		Set<VertexRef> processed = new LinkedHashSet<VertexRef>();
		for(VertexRef vertexRef : vertexRefs) {
			addRefTreeToSet(getBaseTopology(), vertexRef, processed, getCriteria());
		}
		return processed;
	}

	private static void addRefTreeToSet(GraphProvider graphProvider, VertexRef vertexId, Set<VertexRef> processed, Criteria[] criteria) {
		processed.add(vertexId);

		for(VertexRef childId : graphProvider.getChildren(vertexId, criteria)) {
			if (!processed.contains(childId)) {
				addRefTreeToSet(graphProvider, childId, processed, criteria);
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
    public StatusProvider getVertexStatusProvider() {
        return m_statusProvider;
    }

    @Override
    public Set<EdgeStatusProvider> getEdgeStatusProviders(){
        if(m_edgeStatusProviders == null) m_edgeStatusProviders = new HashSet<EdgeStatusProvider>();
        return m_edgeStatusProviders;
    }

    @Override
    public String getSessionId() {
        return m_sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        m_sessionId = sessionId;
        try {
            m_bundleContext.removeServiceListener(this);
            m_bundleContext.addServiceListener(this, String.format("(&(objectClass=%s)(sessionId=%s))", "org.opennms.features.topology.api.topo.Criteria", m_sessionId));
        } catch (InvalidSyntaxException e) {
            LoggerFactory.getLogger(getClass()).error("registerServiceListener() failed", e);
        }
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
