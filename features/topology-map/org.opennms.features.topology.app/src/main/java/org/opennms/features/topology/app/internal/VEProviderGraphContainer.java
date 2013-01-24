package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.GraphVisitor;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.adapter.TPGraphProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class VEProviderGraphContainer implements GraphContainer, VertexListener, EdgeListener {
    
    public class PseudoEdge extends AbstractEdge {

        private String m_styleName;
        private Vertex m_source;
        private Vertex m_target;
        
        public PseudoEdge(String namespace, String id, String styleName, Vertex source, Vertex target) {
        	super(namespace, id);
        	m_styleName = styleName;
            m_source = source;
            m_target = target;
        }

        @Override
        public String getStyleName() {
            return m_styleName;
        }
        
        @Override
        public String getKey() {
            return getNamespace()+":" + getId();
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

    }

    public class VEGraph implements Graph {
    	
    	private final Collection<? extends Vertex> m_displayVertices;
    	private final Collection<? extends Edge> m_displayEdges;
    	private final Layout m_layout;
    	
        public VEGraph(Layout layout, Collection<? extends Vertex> displayVertices,
				Collection<? extends Edge> displayEdges) {
			m_displayVertices = displayVertices;
			m_displayEdges = displayEdges;
			m_layout = layout;
		}

		@Override
        public Layout getLayout() {
			return m_layout;
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
    private LayoutAlgorithm m_layoutAlgorithm;
    private SelectionManager m_selectionManager = new DefaultSelectionManager(); 
    
    private MergingGraphProvider m_mergedGraphProvider;
    private TopologyProvider m_dataSource;

    private final Layout m_layout;
    private VEGraph m_graph;
    
    public VEProviderGraphContainer(TopologyProvider dataSource, ProviderManager providerManager) {
    	m_dataSource = dataSource;
    	m_mergedGraphProvider = new MergingGraphProvider(new TPGraphProvider(dataSource), providerManager);
		m_layout = new DefaultLayout(this);
		rebuildGraph();
	}
    
    public VEProviderGraphContainer(GraphProvider graphProvider, ProviderManager providerManager) {
    	m_mergedGraphProvider = new MergingGraphProvider(graphProvider, providerManager);
    	m_layout = new DefaultLayout(this);
    	rebuildGraph();
    }
    
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
        // Rebuild the graph vertices and edges
        rebuildGraph();
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(this);
            fireGraphChanged();
        }
    }

    @Override
    public GraphProvider getBaseTopology() {
        return m_mergedGraphProvider;
    }

    @Override
    public void setBaseTopology(GraphProvider graphProvider) {
        m_mergedGraphProvider.setBaseGraphProvider(graphProvider);
        rebuildGraph();
    }

    
    public void addVertexProvider(VertexProvider vertexProvider) {
        m_mergedGraphProvider.addVertexProvider(vertexProvider);
        rebuildGraph();
    }

    public void removeVertexProvider(VertexProvider vertexProvider) {
        m_mergedGraphProvider.removeVertexProvider(vertexProvider);
        rebuildGraph();
    }

    public void addEdgeProvider(EdgeProvider edgeProvider) {
        m_mergedGraphProvider.addEdgeProvider(edgeProvider);
        rebuildGraph();
    }

    public void removeEdgeProvider(EdgeProvider edgeProvider) {
    	m_mergedGraphProvider.removeEdgeProvider(edgeProvider);
        rebuildGraph();
    }

    private void rebuildGraph() {
    	
    	List<Vertex> displayVertices = new ArrayList<Vertex>();
    	
    	for(Vertex v : m_mergedGraphProvider.getVertices()) {
    		int vzl = m_mergedGraphProvider.getSemanticZoomLevel(v);
    		if (vzl == getSemanticZoomLevel() || (vzl < getSemanticZoomLevel() && !m_mergedGraphProvider.hasChildren(v))) {
    			displayVertices.add(v);
			}
    	}
    	
    	Set<Edge> displayEdges = new HashSet<Edge>(); 
    	
    	for(Edge e : m_mergedGraphProvider.getEdges()) {
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
				PseudoEdge pEdge = new PseudoEdge("pseudo-"+e.getNamespace(), pseudoId, e.getStyleName(), displaySource, displayTarget);
				displayEdges.add(pEdge);
			}
    	}
    	
    	m_graph = new VEGraph(m_layout, displayVertices, displayEdges);
    	
    	fireGraphChanged();
    	
    }

	private String pseudoId(VertexRef displaySource, VertexRef displayTarget) {
		String sourceId = displaySource.getNamespace()+":"+displaySource.getId();
		String targetId = displayTarget.getNamespace() + ":" + displayTarget.getId();
		
		String a = sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
		String b = sourceId.compareTo(targetId) < 0 ? targetId : sourceId;

		return "<" + a + ">-<" + b + ">";
	}
    
    private boolean refEquals(VertexRef a, VertexRef b) {
    	return a.getNamespace().equals(b.getNamespace()) && a.getId().equals(b.getId());
    }
    
    private Vertex getDisplayVertex(VertexRef vertexRef) {
    	int szl = getSemanticZoomLevel();
    	int vzl = m_mergedGraphProvider.getSemanticZoomLevel(vertexRef);
    	if (vzl == szl || (vzl < szl && !m_mergedGraphProvider.hasChildren(vertexRef))) {
    		return m_mergedGraphProvider.getVertex(vertexRef);
    	} else {
    		Vertex parent = m_mergedGraphProvider.getParent(vertexRef);
    		return getDisplayVertex(parent);
    	}
    }
    

    @Override
    public Vertex getVertex(VertexRef ref) {
    	return m_mergedGraphProvider.getVertex(ref);
    }

    @Override
    public Edge getEdge(EdgeRef ref) {
    	return m_mergedGraphProvider.getEdge(ref);
    }

    @Override
    public TopologyProvider getDataSource() {
    	return m_dataSource;
    }

    @Override
    public void setDataSource(TopologyProvider topologyProvider) {
    	m_dataSource = topologyProvider;
    	TPGraphProvider graphProvider = new TPGraphProvider(topologyProvider);
    	m_mergedGraphProvider.setBaseGraphProvider(graphProvider);
    }

    @Override
    public Graph getGraph() {
        return m_graph;
    }

    @Override
    public SelectionManager getSelectionManager() {
    	return m_selectionManager;
    }

    @Override
    public Criteria getCriteria(String namespace) {
    	return m_mergedGraphProvider.getCriteria(namespace);
    }

    @Override
    public void setCriteria(Criteria criteria) {
    	m_mergedGraphProvider.setCriteria(criteria);
        rebuildGraph();
    }

	@Override
	public Vertex getParent(VertexRef child) {
		return m_mergedGraphProvider.getParent(child);
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
		return m_mergedGraphProvider.getVertices();
	}

	@Override
	public Collection<? extends Vertex> getChildren(VertexRef vRef) {
		return m_mergedGraphProvider.getChildren(vRef);
	}

	@Override
	public Collection<? extends Vertex> getRootGroup() {
		return m_mergedGraphProvider.getRootGroup();
	}

	@Override
	public boolean hasChildren(VertexRef vRef) {
		return m_mergedGraphProvider.hasChildren(vRef);
	}

	@Override
	public Collection<VertexRef> getVertexRefForest(Collection<? extends VertexRef> vertexRefs) {
		Set<VertexRef> processed = new LinkedHashSet<VertexRef>();
		for(VertexRef vertexRef : vertexRefs) {
			addRefTreeToSet(vertexRef, processed);
		}
		return processed;
	}
	
	public void addRefTreeToSet(VertexRef vertexId, Set<VertexRef> processed) {
		processed.add(vertexId);

		for(VertexRef childId : getChildren(vertexId)) {
			if (!processed.contains(childId)) {
				addRefTreeToSet(childId, processed);
			}
		}
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider) {
		rebuildGraph();
	}

	@Override
	public void edgeSetChanged(EdgeProvider provider,
			List<? extends Edge> added, List<? extends Edge> updated,
			List<String> removedEdgeIds) {
		rebuildGraph();
	}

	@Override
	public void vertexSetChanged(VertexProvider provider) {
		rebuildGraph();
	}

	@Override
	public void vertexSetChanged(VertexProvider provider,
			List<? extends Vertex> added, List<? extends Vertex> update,
			List<String> removedVertexIds) {
		rebuildGraph();
	}



}
