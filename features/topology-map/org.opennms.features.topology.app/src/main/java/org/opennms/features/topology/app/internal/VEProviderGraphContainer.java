package org.opennms.features.topology.app.internal;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VEProviderGraphContainer implements GraphContainer, VertexListener, EdgeListener, ServiceListener {

    @SuppressWarnings("serial")
    public class ScaleProperty implements Property<Double>, Property.ValueChangeNotifier{
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
    
    public class PseudoEdge extends AbstractEdge {

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
				if (m_displayVertices.contains(edge.getSource().getVertex())) {
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

    private int m_semanticZoomLevel = 0;
    private Property<Double> m_scaleProperty = new ScaleProperty(0.0);
    private LayoutAlgorithm m_layoutAlgorithm;
    private SelectionManager m_selectionManager;
    private StatusProvider m_statusProvider;
    private MergingGraphProvider m_mergedGraphProvider;
    private MapViewManager m_viewManager = new DefaultMapViewManager();
    private String m_userName;
    private String m_sessionId;
    private BundleContext m_bundleContext;
    private Set<ChangeListener> m_listeners = new CopyOnWriteArraySet<ChangeListener>();
    private AutoRefreshSupport m_autoRefreshSupport;
    
    private VEGraph m_graph;
    private AtomicBoolean m_graphDirty = new AtomicBoolean(Boolean.TRUE);

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
            m_graphDirty.set(Boolean.TRUE);
        }
    }

	public static SemanticZoomLevelCriteria getSemanticZoomLevelCriteriaForContainer(GraphContainer graphContainer) {
		Criteria[] criteria = graphContainer.getCriteria();
		if (criteria != null) {
			for (Criteria criterium : criteria) {
				try {
					SemanticZoomLevelCriteria hopCriteria = (SemanticZoomLevelCriteria)criterium;
					return hopCriteria;
				} catch (ClassCastException e) {}
			}
		}

		SemanticZoomLevelCriteria hopCriteria = new SemanticZoomLevelCriteria(graphContainer.getSemanticZoomLevel());
		graphContainer.setCriteria(hopCriteria);
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
        m_graphDirty.set(Boolean.TRUE);
    }
    
    @Override
    public void setStatusProvider(StatusProvider statusProvider) {
        m_statusProvider = statusProvider;
        m_graphDirty.set(Boolean.TRUE);
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
        m_graphDirty.set(Boolean.TRUE);
    }

    public void removeVertexProvider(VertexProvider vertexProvider) {
        m_mergedGraphProvider.removeVertexProvider(vertexProvider);
        m_graphDirty.set(Boolean.TRUE);
    }

    public void addEdgeProvider(EdgeProvider edgeProvider) {
        m_mergedGraphProvider.addEdgeProvider(edgeProvider);
        m_graphDirty.set(Boolean.TRUE);
    }

    public void removeEdgeProvider(EdgeProvider edgeProvider) {
    	m_mergedGraphProvider.removeEdgeProvider(edgeProvider);
        m_graphDirty.set(Boolean.TRUE);
    }

    private void rebuildGraph() {
    	
    	List<Vertex> displayVertices = new ArrayList<Vertex>();

    	for(Vertex v : m_mergedGraphProvider.getVertices(getCriteria())) {
    		int vzl = m_mergedGraphProvider.getSemanticZoomLevel(v);
    		if (vzl == getSemanticZoomLevel() || (vzl < getSemanticZoomLevel() && !m_mergedGraphProvider.hasChildren(v))) {
    			displayVertices.add(v);
			}
    	}

    	Set<Edge> displayEdges = new HashSet<Edge>();

        final List<Edge> edges = m_mergedGraphProvider.getEdges(getCriteria());
        for(Edge e : edges) {
    		VertexRef source = e.getSource().getVertex();
    		VertexRef target = e.getTarget().getVertex();

    		Vertex displaySource = getDisplayVertex(source);
			Vertex displayTarget = getDisplayVertex(target);
			if (displaySource == null || displayTarget == null) {
				// skip this one
			}
			else if (refEquals(displaySource, displayTarget)) {
				// skip this one
			}
			else if (refEquals(source, displaySource) && refEquals(target, displayTarget)) {
				displayEdges.add(e);
			} else {
				// we may need to create a pseudo edge to represent this edge
				String pseudoId = pseudoId(displaySource, displayTarget);
				PseudoEdge pEdge = new PseudoEdge("pseudo-"+e.getNamespace(), pseudoId, e.getStyleName(), displaySource, displayTarget);
                //This is a hack to get around the device A to device Z label in NCS Path when going through groups
                if(e.getStyleName().equals("ncs edge direct")){
                    pEdge.setTooltipText(e.getTooltipText());
                }
				displayEdges.add(pEdge);
			}
    	}

        if (m_graph == null) {
            m_graph = new VEGraph(displayVertices, displayEdges);
        } else {
            m_graph.updateLayout(displayVertices, displayEdges);
        }

        unselectVerticesWhichAreNotVisibleAnymore();
    }

    // we have to find out if each selected vertex is still displayable,
    // if not we deselect it.
    private void unselectVerticesWhichAreNotVisibleAnymore() {
        if (m_selectionManager == null) return;
        List<VertexRef> selectedVertexRefs = new ArrayList<VertexRef>(m_selectionManager.getSelectedVertexRefs());
        List<VertexRef> newSelectedVertexRefs = new ArrayList<VertexRef>();
        for (VertexRef eachSelectedVertex : selectedVertexRefs) {
            for (Vertex eachDisplayableVertex : getGraph().getDisplayVertices()) {
                if (eachDisplayableVertex.getNamespace().equals(eachSelectedVertex.getNamespace())
                    && eachDisplayableVertex.getId().equals(eachSelectedVertex.getId())) {
                    newSelectedVertexRefs.add(eachSelectedVertex);
                    break;
                }
            }
        }

        // if the selection changed, inform selectionManager
        if (!newSelectedVertexRefs.equals(selectedVertexRefs)) {
            m_selectionManager.setSelectedVertexRefs(newSelectedVertexRefs);
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
    
    private Vertex getDisplayVertex(VertexRef vertexRef) {
    	int szl = getSemanticZoomLevel();
    	int vzl = m_mergedGraphProvider.getSemanticZoomLevel(vertexRef);
    	if (vzl == szl || (vzl < szl && !m_mergedGraphProvider.hasChildren(vertexRef))) {
    		return m_mergedGraphProvider.getVertex(vertexRef);
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
        synchronized(m_graphDirty) {
            if (m_graphDirty.get() == Boolean.TRUE) {
                rebuildGraph();
                m_graphDirty.set(Boolean.FALSE);
            }
        }
        return m_graph;
    }

	private final Set<Criteria> m_criteria = new LinkedHashSet<Criteria>();

	@Override
	public Criteria[] getCriteria() {
		return m_criteria.toArray(new Criteria[0]);
	}

	@Override
	public void setCriteria(Criteria criteria) {
		m_criteria.add(criteria);
		/*
		Set<Criteria> criterias = m_criteria.get(criteria.getNamespace());
		if (criterias == null) {
			criterias = new HashSet<Criteria>();
			m_criteria.put(criteria.getNamespace(), new TreeSet<Criteria>());
		}
		criterias.add(criteria);
		 */
		m_graphDirty.set(Boolean.TRUE);
	}

	@Override
	public void removeCriteria(Criteria criteria) {
		m_criteria.remove(criteria);
		/*
		String namespace = criteria.getNamespace();
		m_criteria.remove(namespace);
		*/
		m_graphDirty.set(Boolean.TRUE);
	}

    public void setBundleContext(final BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

	private void fireGraphChanged() {
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
			addRefTreeToSet(vertexRef, processed);
		}
		return processed;
	}
	
	public void addRefTreeToSet(VertexRef vertexId, Set<VertexRef> processed) {
		processed.add(vertexId);

		for(VertexRef childId : getBaseTopology().getChildren(vertexId)) {
			if (!processed.contains(childId)) {
				addRefTreeToSet(childId, processed);
			}
		}
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider) {
		m_graphDirty.set(Boolean.TRUE);
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider,
			Collection<? extends Edge> added, Collection<? extends Edge> updated,
			Collection<String> removedEdgeIds) {
		m_graphDirty.set(Boolean.TRUE);
	}

	@Override
	public void vertexSetChanged(VertexProvider provider) {
		m_graphDirty.set(Boolean.TRUE);
	}

	@Override
	public void vertexSetChanged(VertexProvider provider,
			Collection<? extends Vertex> added, Collection<? extends Vertex> update,
			Collection<String> removedVertexIds) {
		m_graphDirty.set(Boolean.TRUE);
	}

    @Override
    public MapViewManager getMapViewManager() {
        return m_viewManager;
    }

    @Override
    public StatusProvider getStatusProvider() {
        return m_statusProvider;
    }

	@Override
	public String getUserName() {
		return m_userName;
	}

	@Override
	public void setUserName(String userName) {
		m_userName = userName;
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
                setCriteria(criteria);
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
}
