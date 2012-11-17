package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.GraphVisitor;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.jung.FRLayoutAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class VEProviderGraphContainer implements GraphContainer {
    
    public class PseudoEdge implements Edge {

        private String m_namespace;
        private String m_id;
        private Vertex m_source;
        private Vertex m_target;
        
        public PseudoEdge(String namespace, String id, Vertex source, Vertex target) {
            m_source = source;
            m_target = target;
        }

        @Override
        public String getId() {
            return m_id;
        }

        @Override
        public String getNamespace() {
            return m_namespace;
        }

        @Override
        public String getKey() {
            return getNamespace()+":" + getId();
        }

        @Override
        public Object getItemId() {
            return getKey();
        }

        @Override
        public Item getItem() {
            return new BeanItem<PseudoEdge>(this);
        }

        @Override
        public Connector getSource() {
            return new Connector() {

                @Override
                public String getNamespace() {
                    return PseudoEdge.this.getNamespace();
                }

                @Override
                public String getId() {
                    return PseudoEdge.this.getId()+":source";
                }

                @Override
                public EdgeRef getEdge() {
                    return PseudoEdge.this;
                }

                @Override
                public VertexRef getVertex() {
                    return PseudoEdge.this.m_source;
                }

            };
        }

        @Override
        public Connector getTarget() {
            return new Connector() {

                @Override
                public String getNamespace() {
                    return PseudoEdge.this.getNamespace();
                }

                @Override
                public String getId() {
                    return PseudoEdge.this.getId()+":target";
                }

                @Override
                public EdgeRef getEdge() {
                    return PseudoEdge.this;
                }

                @Override
                public VertexRef getVertex() {
                    return PseudoEdge.this.m_target;
                }

            };
        }

        @Override
        public String getLabel() {
            return m_source.getLabel()+" :: " + m_target.getLabel();
        }

        @Override
        public String getTooltipText() {
            return getLabel();
        }

        @Override
        public String getStyleName() {
            return "edge";
        }
        
    }

    public class VEGraph implements Graph {
    	
    	private final Collection<? extends Vertex> m_displayVertices;
    	private final Collection<? extends Edge> m_displayEdges;
    	
        public VEGraph(Collection<? extends Vertex> displayVertices,
				Collection<? extends Edge> displayEdges) {
			m_displayVertices = displayVertices;
			m_displayEdges = displayEdges;
		}

		@Override
        public Layout getLayout() {
            throw new UnsupportedOperationException(
                    "VEGraph.getLayout is not yet implemented.");
        }

        @Override
        public Collection<? extends Vertex> getDisplayVertices() {
        	return Collections.unmodifiableCollection(m_displayVertices);
        }

        @Override
        public Collection<? extends Edge> getDisplayEdges() {
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

    }

    private static final Logger s_log = LoggerFactory.getLogger(VEProviderGraphContainer.class);

    private int m_semanticZoomLevel = 0;
    private double m_scale = 1.0;
    private LayoutAlgorithm m_layoutAlgorithm = new FRLayoutAlgorithm();

    private GraphProvider m_baseGraphProvider;
    private final Map<String, VertexProvider> m_vertexProviders = new HashMap<String, VertexProvider>();
    private final Map<String, EdgeProvider> m_edgeProviders = new HashMap<String, EdgeProvider>();
    private final Map<String, Criteria> m_criteria = new HashMap<String, Criteria>();

    private VEGraph m_graph;

	private Set<ChangeListener> m_listeners = new CopyOnWriteArraySet<ChangeListener>();

    @Override
    public int getSemanticZoomLevel() {
        return m_semanticZoomLevel;
    }

    @Override
    public void setSemanticZoomLevel(int level) {
        m_semanticZoomLevel = level;
        rebuildGraph();
    }

    @Override
    public double getScale() {
        return m_scale;
    }

    @Override
    public void setScale(double scale) {
        m_scale = scale;
    }
    @Override
    public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
        m_layoutAlgorithm = layoutAlgorithm;
        redoLayout();
    }

    @Override
    public LayoutAlgorithm getLayoutAlgorithm() {
        return m_layoutAlgorithm;
    }

    @Override
    public void redoLayout() {
        s_log.debug("redoLayout()");
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(this);
            fireChange();
        }
    }

    private void fireChange() {
        throw new UnsupportedOperationException("VEProviderGraphContainer.fireChange is not yet implemented.");
    }

    @Override
    public VertexContainer<?, ?> getVertexContainer() {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getVertexContainer is not yet implemented.");
    }

    @Override
    public Item getVertexItem(Object vertexId) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getVertexItem is not yet implemented.");
    }

    @Override
    public GraphProvider getBaseTopology() {
        return m_baseGraphProvider;
    }

    @Override
    public void setBaseTopology(GraphProvider graphProvider) {
        m_baseGraphProvider = graphProvider;
        rebuildGraph();
    }

    
    public void addVertexProvider(VertexProvider vertexProvider) {
        m_vertexProviders.put(vertexProvider.getNamespace(), vertexProvider);
        rebuildGraph();
    }

    public void removeVertexProvider(VertexProvider vertexProvider) {
        m_vertexProviders.remove(vertexProvider.getNamespace());
        rebuildGraph();
    }

    public void addEdgeProvider(EdgeProvider edgeProvider) {
        m_edgeProviders.put(edgeProvider.getNamespace(), edgeProvider);
        rebuildGraph();
    }

    public void removeEdgeProvider(EdgeProvider edgeProvider) {
        m_edgeProviders.remove(edgeProvider.getNamespace());
        rebuildGraph();
    }

    private void rebuildGraph() {
    	
    	List<Vertex> displayVertices = new ArrayList<Vertex>();
    	
    	for(Vertex v : m_baseGraphProvider.getVertices()) {
    		int vzl = m_baseGraphProvider.getSemanticZoomLevel(v);
    		if (vzl == getSemanticZoomLevel() || (vzl < getSemanticZoomLevel() && !m_baseGraphProvider.hasChildren(v))) {
    			displayVertices.add(v);
			}
    	}
    	
    	Set<Edge> displayEdges = new HashSet<Edge>(); 
    	
    	for(Edge e : m_baseGraphProvider.getEdges()) {
    		VertexRef source = e.getSource().getVertex();
    		VertexRef target = e.getTarget().getVertex();

    		Vertex displaySource = getDisplayVertex(source);
			Vertex displayTarget = getDisplayVertex(target);
			if (refEquals(displaySource, displayTarget)) {
				// skip this one
			}
			else if (refEquals(source, displaySource) && refEquals(target, displayTarget)) {
				displayEdges.add(e);
			} else {
				// we may need to create a pseudo edge to represent this edge
				String pseudoId = pseudoId(displaySource, displayTarget);
				PseudoEdge pEdge = new PseudoEdge("pseudo", pseudoId, displaySource, displayTarget);
				displayEdges.add(pEdge);
			}
    	}
    	
    	m_graph = new VEGraph(displayVertices, displayEdges);
    	
    	fireGraphChanged();
    	
    }

	private String pseudoId(VertexRef displaySource, VertexRef displayTarget) {
		return "<"+displaySource.getNamespace()+":"+displaySource.getId()+">-<"+displayTarget.getNamespace()+":"+displayTarget.getId()+">";
	}
    
    private boolean refEquals(VertexRef a, VertexRef b) {
    	return a.getNamespace().equals(b.getNamespace()) && a.getId().equals(b.getId());
    }
    
    private Vertex getDisplayVertex(VertexRef vertexRef) {
    	int szl = getSemanticZoomLevel();
    	int vzl = m_baseGraphProvider.getSemanticZoomLevel(vertexRef);
    	if (vzl == szl || (vzl < szl && !m_baseGraphProvider.hasChildren(vertexRef))) {
    		return m_baseGraphProvider.getVertex(vertexRef);
    	} else {
    		Vertex parent = m_baseGraphProvider.getParent(vertexRef);
    		return getDisplayVertex(parent);
    	}
    }
    

    @Override
    public Vertex getVertex(VertexRef ref) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getVertex is not yet implemented.");
    }

    @Override
    public Edge getEdge(EdgeRef ref) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getEdge is not yet implemented.");
    }

    @Override
    public TopologyProvider getDataSource() {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getDataSource is not yet implemented.");
    }

    @Override
    public void setDataSource(TopologyProvider topologyProvider) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.setDataSource is not yet implemented.");
    }

    @Override
    public Graph getGraph() {
        return m_graph;
    }

    @Override
    public boolean containsVertexId(Object vertexId) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.containsVertexId is not yet implemented.");
    }

    @Override
    public boolean containsEdgeId(Object edgeId) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.containsEdgeId is not yet implemented.");
    }

    @Override
    public SelectionManager getSelectionManager() {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getSelectionManager is not yet implemented.");
    }

    @Override
    public Collection<?> getVertexForest(Collection<?> vertexIds) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getVertexForest is not yet implemented.");
    }

    @Override
    public void setVertexItemProperty(Object itemId, String propertyName,
            Object value) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.setVertexItemProperty is not yet implemented.");
    }

    @Override
    public <T> T getVertexItemProperty(Object itemId, String propertyName,
            T defaultValue) {
        throw new UnsupportedOperationException(
                "VEProviderGraphContainer.getVertexItemProperty is not yet implemented.");
    }

    @Override
    public Criteria getCriteria(String namespace) {
        return m_criteria.get(namespace);
    }

    @Override
    public void setCriteria(Criteria criteria) {
        m_criteria.put(criteria.getNamespace(), criteria);
        rebuildGraph();
    }

	@Override
	public Vertex getParent(VertexRef child) {
		// TODO Auto-generated method stub
		return null;
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
	public Collection<? extends Vertex> getVertices() {
		throw new UnsupportedOperationException("GraphContainer.getVertices is not yet implemented.");
	}

	@Override
	public Collection<? extends Vertex> getChildren(VertexRef vRef) {
		throw new UnsupportedOperationException("GraphContainer.getChildren is not yet implemented.");
	}

	@Override
	public Collection<? extends Vertex> getRootGroup() {
		throw new UnsupportedOperationException("GraphContainer.getRootGroup is not yet implemented.");
	}

	@Override
	public boolean hasChildren(VertexRef vRef) {
		throw new UnsupportedOperationException("GraphContainer.hasChildren is not yet implemented.");
	}

	@Override
	public Collection<VertexRef> getVertexRefForest(
			Collection<? extends VertexRef> vertexRefs) {
		throw new UnsupportedOperationException("GraphContainer.getVertexRefForest is not yet implemented.");
	}


}
