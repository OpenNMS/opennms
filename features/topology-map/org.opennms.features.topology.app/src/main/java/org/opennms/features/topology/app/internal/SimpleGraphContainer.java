/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.EditableGraphProvider;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Connector;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.util.BeanContainer;

public class SimpleGraphContainer implements GraphContainer {

    private static final String LEAF = "leaf";
	private static final String ICON = "icon";
	private static final String ICON_KEY = "iconKey";
	private static final String LABEL = "label";
    private static final String IP_ADDR = "ipAddr";
	private static final String NODE_ID = "nodeID";
    private static final String TOOLTIP_TEXT = "tooltipText";
	private static final String X_PROPERTY = "x";
	private static final String Y_PROPERTY = "y";
	private static final String SEMANTIC_ZOOM_LEVEL = "semanticZoomLevel";

    public static class GVertex extends AbstractVertex {
        
        private Object m_itemId;
        private String m_groupKey;
        private Object m_groupId;
        private int m_x;
        private int m_y;
        private boolean m_selected = false;
        
        public GVertex(String key, Object itemId, Item item, String groupKey, Object groupId) {
            super("simple", key);
            m_itemId = itemId;
            m_item = item;
            m_groupKey = groupKey;
            m_groupId = groupId;
        }

        public Object getItemId() {
            return m_itemId;
        }

        public void setGroupId(Object groupId) {
            m_groupId = groupId;
        }

        public void setGroupKey(String groupKey) {
            m_groupKey = groupKey;
        }

        public String getGroupKey() {
            return m_groupKey;
        }
        
        public Object getGroupId() {
            return m_groupId;
        }

        public void setItemId(Object itemId) {
            m_itemId = itemId;
        }

        public boolean isLeaf() {
            return (Boolean) getItem().getItemProperty(LEAF).getValue();
        }

        public int getX() {
            return m_x;
        }

        public void setX(int x) {
            m_x = x;
        }

        public int getY() {
            return m_y;
        }

        public void setY(int y) {
            m_y = y;
        }

        public boolean isSelected() {
            return m_selected;
        }

        public void setSelected(boolean selected) {
            m_selected = selected;
        }

        public String getIcon() {
            return (String) m_item.getItemProperty(ICON).getValue();
        }

        public void setIcon(String icon) {
            m_item.getItemProperty(ICON).setValue(icon);
        }
        
        public String getLabel() {
            Property labelProperty = m_item.getItemProperty(LABEL);
			String label = labelProperty == null ? "labels unsupported " : (String) labelProperty.getValue();
			return label;
        }

        public void setLabel(String label) {
            m_item.getItemProperty(LABEL).setValue(label);
        }
        
        public String getIpAddr() {
            Property ipAddrProperty = m_item.getItemProperty(IP_ADDR);
            String ipAddr = ipAddrProperty == null ? null : (String) ipAddrProperty.getValue();
            return ipAddr;
        }
        
        public void setIpAddr(String ipAddr) {
            m_item.getItemProperty(IP_ADDR).setValue(ipAddr);
        }
        
        public int getNodeID() {
            Property nodeIDProperty = m_item.getItemProperty(NODE_ID);
            int nodeID = (nodeIDProperty == null) ? 0 : (Integer) nodeIDProperty.getValue();
            return nodeID;
        }
        
        public void setNodeID(int nodeID) {
            m_item.getItemProperty(NODE_ID).setValue(new Integer(nodeID));
        }

        public GVertex getParent() {
            if (m_groupKey == null) return null;
            
            return m_vertexHolder.getElementByKey(m_groupKey);
        }
        
        public int getSemanticZoomLevel() {
            GVertex parent = getParent();
            return parent == null ? 0 : parent.getSemanticZoomLevel() + 1;
        }
        
        public String getTooltipText() {
            if(m_item.getItemProperty(TOOLTIP_TEXT) != null && m_item.getItemProperty(TOOLTIP_TEXT).getValue() != null) {
                return (String) m_item.getItemProperty(TOOLTIP_TEXT).getValue();
            }else {
                return null;
            }
        }

    }
    
    public static class GEdge extends AbstractEdge {

        private Object m_itemId;
        private Item m_item;
        private GVertex m_source;
        private GVertex m_target;
        private boolean m_selected = false;

        public GEdge(String key, Object itemId, Item item, GVertex source, GVertex target) {
            super("simple", key);
            m_itemId = itemId;
            m_item = item;
            m_source = source;
            m_target = target;
        }

        public Object getItemId() {
            return m_itemId;
        }

        public Connector getSource() {
            return m_source;
        }

        public Connector getTarget() {
            return m_target;
        }

        public boolean isSelected() {
            return m_selected;
        }

        public void setSelected(boolean selected) {
            m_selected  = selected;
        }
        
        public String getTooltipText() {
            if(m_item.getItemProperty(TOOLTIP_TEXT) != null && m_item.getItemProperty(TOOLTIP_TEXT).getValue() != null) {
                return (String) m_item.getItemProperty(TOOLTIP_TEXT).getValue();
            }else {
                return null;
            }
        }
    }

    private final SelectionManager m_selectionManager;
	private TopoGraph m_graph;

	private LayoutAlgorithm m_layoutAlgorithm;
	private double m_scale = 1;
	private int m_semanticZoomLevel;

    private ElementHolder<GVertex> m_vertexHolder;
    private ElementHolder<GEdge> m_edgeHolder;
    private EditableGraphProvider m_graphProvider;

    private Set<ChangeListener> m_listeners = new CopyOnWriteArraySet<ChangeListener>();
    
	public SimpleGraphContainer(GraphProvider topologyProvider) {
		m_selectionManager = new DefaultSelectionManager();
		
		m_vertexHolder = new ElementHolder<GVertex>("gcV") {
			
            @Override
            protected void remove(GVertex element) {
                
            }

            @Override
            protected GVertex update(GVertex element) {
                Vertex groupId = m_graphProvider.getParent(element.getItemId());
                String groupKey = groupId == null ? null : getKeyForItemId(groupId);
                
                element.setGroupId(groupId);
                element.setGroupKey(groupKey);
                
                return element;
            }

            @Override
            protected GVertex make(String key, Object itemId, Item item) {
                Vertex groupId = m_graphProvider.getParent(itemId);
                String groupKey = groupId == null ? null : getKeyForItemId(groupId);
                //System.err.printf("GVertex Make Call :: Parent of itemId: %s with key %s groupId: %s groupKey %s\n" + key, itemId, key, groupId, groupKey);
                GVertex gVertex = new GVertex(key, itemId, item, groupKey, groupId);
                return gVertex;
            }
		};
		
        m_edgeHolder = new ElementHolder<GEdge>("gcE") {

            @Override
            protected GEdge make(String key, Object itemId, Item item) {

                Iterator<VertexRef> endPoints = m_graphProvider.getEndPointIdsForEdge((String)itemId).iterator();

                Object sourceId = endPoints.next();
                Object targetId = endPoints.next();
                
                GVertex source = m_vertexHolder.getElementByItemId(sourceId);
                GVertex target = m_vertexHolder.getElementByItemId(targetId);

                return new GEdge(key, itemId, item, source, target);
            }

        };
	}
	
	@Override
	public SelectionManager getSelectionManager() {
		return m_selectionManager;
	}

	@Override
	public void setDataSource(EditableGraphProvider topologyProvider) {
	    m_graphProvider = topologyProvider;
	    
	    m_vertexHolder.setContainer(m_graphProvider);
	    
	    m_edgeHolder.setContainer(m_graphProvider);
	    
	    m_graph = new TopoGraph(this);
	    
	    redoLayout();
	}

	@Override
	public EditableGraphProvider getBaseTopology() {
		return m_graphProvider;
	}

	@Override
	public void setBaseTopology(GraphProvider graphProvider) {
		throw new UnsupportedOperationException("SimpleGraphContainer.setBaseTopology is not yet implemented.");
	}

    @Override
	public Collection<VertexRef> getEndPointIdsForEdge(EdgeRef key) {
	        if (key == null) return Collections.emptyList();
		GEdge edge = m_edgeHolder.getElementByKey(key.toString());
		if (edge == null) return Collections.emptyList();
		return Arrays.asList(edge.getSource().getKey(), edge.getTarget().getKey());
	}

    @Override
	public Collection<EdgeRef> getEdgeIdsForVertex(VertexRef vertexKey) {
	    GVertex vertex = m_vertexHolder.getElementByKey(vertexKey.toString());
	    return m_edgeHolder.getKeysByItemId(m_graphProvider.getEdgeIdsForVertex(vertex.getItemId()));
	}
	
	public static Collection<String> getPropertyIds() {
	    return Arrays.asList(new String[] {"semanticZoomLevel", "scale"});
	}

    @Override
    public int getSemanticZoomLevel() {
        return m_semanticZoomLevel;
    }


    @Override
    public void setSemanticZoomLevel(int level) {
        m_semanticZoomLevel = level;
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
    public double getScale() {
        return m_scale;
    }
    
    @Override
    public void setScale(double scale) {
        m_scale = scale;
    }
    
    @Override
    public void redoLayout() {
        LoggerFactory.getLogger(getClass()).debug("redoLayout()");
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(this);
            fireChange();
        }
    }

    @Override
	public void fireChange() {
		for(ChangeListener listener : m_listeners) {
			listener.graphChanged(this);
		}
	}


    public int getX(VertexRef itemId) {
		Vertex vertexItem = getVertex(itemId);
		if (vertexItem == null) throw new NullPointerException("vertexItem "+ itemId +" is null");
		Property itemProperty = vertexItem.getItemProperty(X_PROPERTY);
		if (itemProperty == null) throw new NullPointerException("X property is null");
		return (Integer) itemProperty.getValue();
	}

    @Override
    public int getY(VertexRef itemId) {
		return (Integer) getBaseTopology().getVertex(itemId).getItem().getItemProperty(Y_PROPERTY).getValue();
	}

    @Override
    public void setX(VertexRef itemId, int x) {
		getBaseTopology().getVertex(itemId).getItem().getItemProperty(X_PROPERTY).setValue(x);
	}

    @Override
    public void setY(VertexRef itemId, int y) {
		getBaseTopology().getVertex(itemId).getItem().getItemProperty(Y_PROPERTY).setValue(y);
	}

    @Override
    public int getSemanticZoomLevel(Object itemId) {
		return getVertexItemProperty(itemId, SEMANTIC_ZOOM_LEVEL, Integer.valueOf(0));
	}
    
    @Override
    public void setVertexItemProperty(VertexRef itemId, String propertyName, Object value) {
    	getBaseTopology().getVertex(itemId).getItem().getItemProperty(propertyName).setValue(value);
    }

    @Override
	public <T> T getVertexItemProperty(VertexRef itemId, String propertyName, T defaultValue) {
		Item vertexItem = getBaseTopology().getVertex(itemId).getItem();
		if (vertexItem == null) {
			return defaultValue;
		} else {
			Property itemProperty = vertexItem.getItemProperty(propertyName);
    		return itemProperty == null 
    				? defaultValue
    				: itemProperty.getValue() == null
    				? defaultValue
    				: (T)itemProperty.getValue();
		}
	}
    
    @Override
    public Object getGroupId(VertexRef itemId) {
    	return getBaseTopology().getVertex(itemId).getGroupKey();
    }

    @Override
	boolean isLeaf(VertexRef itemId) {
		Object value = getBaseTopology().getVertex(itemId).isLeaf();
	    return (Boolean) value;
	}

    @Override
	String getLabel(VertexRef itemId) {
		return getBaseTopology().getVertex(itemId).getLabel();
	}

    @Override
	String getIconKey(VertexRef itemId) {
		return (String) getBaseTopology().getVertex(itemId).getIconKey();
	}

    @Override
    String getVertexTooltipText(VertexRef itemId) {
    	String tooltipText = getBaseTopology().getVertex(itemId).getTooltipText();
    	if(tooltipText == null || "".equals(tooltipText.trim())) {
    		return getLabel(itemId);
    	}else {
    		return tooltipText;
    	}
    }

    @Override
	public Object getDisplayVertexId(VertexRef vertexId, int semanticZoomLevel) {

		int szl = getSemanticZoomLevel(vertexId);

		if(getGroupId(vertexId) == null || szl <= semanticZoomLevel) {
			return vertexId;
		}else {
			return getDisplayVertexId(getGroupId(vertexId), semanticZoomLevel);
		}
	}

	@Override
	public Collection<VertexRef> getVertexRefForest(Collection<? extends VertexRef> vertexRefs) {
		Set<VertexRef> processed = new LinkedHashSet<VertexRef>();
		for(VertexRef vertexRef : vertexRefs) {
			addRefTreeToSet(vertexRef, processed);
		}
		return processed;
	}
	
    @Override
	public void addRefTreeToSet(VertexRef vertexId, Set<VertexRef> processed) {
		processed.add(vertexId);

		for(VertexRef childId : getChildren(vertexId)) {
			if (!processed.contains(childId)) {
				addRefTreeToSet(childId, processed);
			}
		}
	}

    @Override
	public TopoGraph getCompleteGraph() {
		return m_graph;
	}

	@Override
	public Graph getGraph() {
		return m_graph;
	}
	
	@Override
	public Vertex getVertex(VertexRef ref) {
		if (ref instanceof TopoVertex) {
			return (TopoVertex)ref;
		} else {
			return findVertex(ref);
		}
	}
	
	private boolean refEquals(Ref a, Ref b) {
		return a.getNamespace().equals(b.getNamespace()) && a.getId().equals(b.getId());
	}
	
	private TopoVertex findVertexByItemId(Object itemId) {
		for(TopoVertex vertex : m_graph.getVertices()) {
			if (itemId.equals(vertex.getItemId())) {
				return vertex;
			}
		}
		return null;
	}

	private TopoVertex findVertex(VertexRef ref) {
		for(TopoVertex vertex : m_graph.getVertices()) {
			if (refEquals(ref, vertex)) {
				return vertex;
			}
		}
		return null;
	}

	private Edge findEdge(EdgeRef ref) {
		for(Edge edge : m_graph.getEdges()) {
			if (refEquals(ref, edge)) {
				return edge;
			}
		}
		return null;
	}


	@Override
	public Edge getEdge(EdgeRef ref) {
		if (ref instanceof Edge) {
			return (Edge)ref;
		} else {
			return findEdge(ref);
		}
	}

	@Override
	public Criteria getCriteria(String namespace) {
		throw new UnsupportedOperationException("GraphContainer.getCriteria is not yet implemented.");
	}

	@Override
	public void setCriteria(Criteria critiera) {
		throw new UnsupportedOperationException("GraphContainer.setCriteria is not yet implemented.");
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
	public Collection<? extends Vertex> getChildren(VertexRef vRef) {
		TopoVertex v = m_graph.getVertex(vRef);
		Collection<?> childIds = getChildren(v.getItemId());
		
		List<TopoVertex> children = new ArrayList<TopoVertex>(childIds.size());
		for(Object childId : childIds) {
			TopoVertex child = m_graph.getVertexByItemId(childId);
			if (child != null) {
				children.add(child);
			}
		}
		return children;
	}

	@Override
	public Collection<? extends Vertex> getRootGroup() {
		List<TopoVertex> rootGroup = new ArrayList<TopoVertex>();
		
		for(TopoVertex v : m_graph.getVertices()) {
			if (getParent(v) == null) {
				rootGroup.add(v);
			}
		}
		
		return rootGroup;
	}

	@Override
	public boolean hasChildren(VertexRef vRef) {
		return !getChildren(vRef).isEmpty();
	}
}
