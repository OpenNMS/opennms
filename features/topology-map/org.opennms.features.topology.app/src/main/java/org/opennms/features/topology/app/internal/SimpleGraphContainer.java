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

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.adapter.TPGraphProvider;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

@Deprecated
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

    public class GVertex {
        
		private String m_key;
        private Object m_itemId;
        private Item m_item;
        private String m_groupKey;
        private Object m_groupId;
        private int m_x;
        private int m_y;
        private boolean m_selected = false;
        
        public GVertex(String key, Object itemId, Item item, String groupKey, Object groupId) {
            setKey(key);
            m_itemId = itemId;
            setItem(item);
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

        public String getKey() {
            return m_key;
        }

        public void setKey(String key) {
            m_key = key;
        }

        public Item getItem() {
            return m_item;
        }

        public void setItem(Item item) {
            m_item = item;
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
        
        public void setIconKey(String iconKey) {
            m_item.getItemProperty(ICON_KEY).setValue(iconKey);
        }
        
        public String getIconKey() {
            return (String) m_item.getItemProperty(ICON_KEY).getValue();
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

        private GVertex getParent() {
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
    
    public static class GEdge {

		private String m_key;
        private Object m_itemId;
        private Item m_item;
        private GVertex m_source;
        private GVertex m_target;
        private boolean m_selected = false;

        public GEdge(String key, Object itemId, Item item, GVertex source, GVertex target) {
            m_key = key;
            m_itemId = itemId;
            m_item = item;
            m_source = source;
            m_target = target;
        }

        public String getKey() {
            return m_key;
        }

        public void setKey(String key) {
            m_key = key;
        }

        public Object getItemId() {
            return m_itemId;
        }

        public GVertex getSource() {
            return m_source;
        }

        public GVertex getTarget() {
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
    
    @SuppressWarnings("serial")
	private class GEdgeContainer extends BeanContainer<String, GEdge> implements ItemSetChangeListener, PropertySetChangeListener{
    	
    	TopologyProvider topologyProvider;

        public GEdgeContainer() {
            super(GEdge.class);
            setBeanIdProperty("key");
            addAll(m_edgeHolder.getElements());
        }
        
		public void setTopologyProvider(TopologyProvider provider) {
			if (topologyProvider != null) {
				topologyProvider.getEdgeContainer().removeListener((ItemSetChangeListener)this);
	            topologyProvider.getEdgeContainer().removeListener((PropertySetChangeListener)this);
			}
			
			topologyProvider = provider;
			
			if (topologyProvider != null) {
				topologyProvider.getEdgeContainer().addListener((ItemSetChangeListener)this);
				topologyProvider.getEdgeContainer().addListener((PropertySetChangeListener)this);
			}
			
			removeAllItems();
			addAll(m_edgeHolder.getElements());

			
			containerItemSetChange(null);
		}



        @Override
        public void containerPropertySetChange(PropertySetChangeEvent event) {
            m_edgeHolder.update();
            removeAllItems();
            addAll(m_edgeHolder.getElements());
            if (m_graph != null) {
            	m_graph.update();
            }
            fireContainerPropertySetChange();
        }

        @Override
        public void containerItemSetChange(ItemSetChangeEvent event) {
            LoggerFactory.getLogger(getClass()).debug("containerItemSetChange()");
            m_edgeHolder.update();
            removeAllItems();
            addAll(m_edgeHolder.getElements());
            if (m_graph != null) {
            	m_graph.update();
            }
            fireItemSetChange();
        }
        
        
    }
     
    private class GVertexContainer extends VertexContainer<Object, GVertex> implements ItemSetChangeListener, PropertySetChangeListener{
    	
		
		private static final long serialVersionUID = -5363822401177550580L;

		TopologyProvider topologyProvider;

		public GVertexContainer() {
            super(GVertex.class);
            setBeanIdProperty("key");
            addAll(m_vertexHolder.getElements());
        }

		public void setTopologyProvider(TopologyProvider provider) {
			if (topologyProvider != null) {
				topologyProvider.getVertexContainer().removeListener((ItemSetChangeListener)this);
	            topologyProvider.getVertexContainer().removeListener((PropertySetChangeListener)this);
			}
			
			
			
			topologyProvider = provider;
			
			
			if (topologyProvider != null) {
				topologyProvider.getVertexContainer().addListener((ItemSetChangeListener)this);
				topologyProvider.getVertexContainer().addListener((PropertySetChangeListener)this);
			}
			
			removeAllItems();
			addAll(m_vertexHolder.getElements());

			containerItemSetChange(null);
		}

        @Override
        public Collection<String> getChildren(Object gItemId) {
            GVertex v = m_vertexHolder.getElementByKey(gItemId.toString());
            Collection<?> children = topologyProvider.getVertexContainer().getChildren(v.getItemId());
            
            return m_vertexHolder.getKeysByItemId(children);
        }
        
        @Override
        public Object getParent(Object gItemId) {
            GVertex vertex = m_vertexHolder.getElementByKey(gItemId.toString());
            return vertex == null ? null : vertex.getGroupKey();
        }

        @Override
        public Collection<String> rootItemIds() {
            return m_vertexHolder.getKeysByItemId(topologyProvider.getVertexContainer().rootItemIds());
        }

        @Override
        public boolean setParent(Object gKey, Object gNewParentKey) throws UnsupportedOperationException {
           if(!containsId(gKey)) return false;
           
           GVertex vertex = m_vertexHolder.getElementByKey(gKey.toString());
           GVertex parentVertex = m_vertexHolder.getElementByKey(gNewParentKey.toString());
           
           if(topologyProvider.getVertexContainer().setParent(vertex.getItemId(), parentVertex.getItemId())) {
               vertex.setGroupId(parentVertex.getItemId());
               vertex.setGroupKey(parentVertex.getKey());
               return true;
           }
           
           return false;
           
        }

        @Override
        public boolean areChildrenAllowed(Object key) {
            if(key == null) return false;
            
            GVertex vertex = m_vertexHolder.getElementByKey(key.toString());
            return vertex != null && !vertex.isLeaf();
        }

        @Override
        public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("this operation is not allowed");
        }

        @Override
        public boolean isRoot(Object key) {
            GVertex vertex = m_vertexHolder.getElementByKey(key.toString());
            return topologyProvider.getVertexContainer().isRoot(vertex.getItemId());
        }

        @Override
        public boolean hasChildren(Object key) {
            GVertex vertex = m_vertexHolder.getElementByKey(key.toString());
            return topologyProvider.getVertexContainer().hasChildren(vertex.getItemId());
        }

        @Override
        public void containerItemSetChange(ItemSetChangeEvent event) {
            LoggerFactory.getLogger(getClass()).debug("containerItemSetChange()");
            m_vertexHolder.update();
            
//            removeAllItems();
//            addAll(m_vertexHolder.getElements());
            
            List<GVertex> oldVertices = getAllGVertices();
            List<GVertex> newVertices = m_vertexHolder.getElements();
            
            Set<GVertex> newContainerVertices = new LinkedHashSet<GVertex>(newVertices);
            newContainerVertices.removeAll(oldVertices);
            
            Set<GVertex> removedContainerVertices = new LinkedHashSet<GVertex>(oldVertices);
            removedContainerVertices.removeAll(newVertices);
            
            for(GVertex v : removedContainerVertices) {
                removeItem(v.getKey());
            }
            updateAll(newVertices);
            addAll(newContainerVertices);

            if (m_graph != null) {
            	m_graph.update();
            }

            fireItemSetChange();
        }

        private void updateAll(List<GVertex> vertices) {
            for(GVertex v : vertices) {
                Object key = v.getKey();
                if(containsId(key)) {
                    BeanItem<GVertex> item = getItem(key);
                    item.getItemProperty("groupId").setValue(v.getGroupId());
                    item.getItemProperty("groupKey").setValue(v.getGroupKey());
                    item.getItemProperty("icon").setValue(v.getIcon());
                    item.getItemProperty("item").setValue(v.getItem());
                    item.getItemProperty("itemId").setValue(v.getItemId());
                    item.getItemProperty("key").setValue(v.getKey());
                    item.getItemProperty("selected").setValue(v.isSelected());
                    item.getItemProperty("x").setValue(v.getX());
                    item.getItemProperty("y").setValue(v.getY());
                }
            }
        }

        private List<GVertex> getAllGVertices() {
            List<GVertex> allVertices = new ArrayList<GVertex>();
            for(Object itemId : getAllItemIds()) {
                allVertices.add(getItem(itemId).getBean());
            }
            return allVertices;
        }

        @Override
        public void containerPropertySetChange(PropertySetChangeEvent event) {
            LoggerFactory.getLogger(getClass()).debug("containerPropertySetChange()");
            m_vertexHolder.update();
            removeAllItems();
            addAll(m_vertexHolder.getElements());
            if (m_graph != null) {
            	m_graph.update();
            }
            fireContainerPropertySetChange();
        }
        
    }
    
    private final SelectionManager m_selectionManager;
	private TopoGraph m_graph;

	private LayoutAlgorithm m_layoutAlgorithm;
	private double m_scale = 1;
	private int m_semanticZoomLevel;

    private GVertexContainer m_vertexContainer;
    
    private ElementHolder<GVertex> m_vertexHolder;
    private ElementHolder<GEdge> m_edgeHolder;
    private TopologyProvider m_topologyProvider;
    private GraphProvider m_graphProvider;
    private GEdgeContainer m_edgeContainer;
    
    private Set<ChangeListener> m_listeners = new CopyOnWriteArraySet<ChangeListener>();
    
	public SimpleGraphContainer(TopologyProvider topologyProvider) {
		m_selectionManager = new DefaultSelectionManager();
		
		m_vertexHolder = new ElementHolder<GVertex>("gcV") {
			
            @Override
            protected void remove(GVertex element) {
                
            }

            @Override
            protected GVertex update(GVertex element) {
                Object groupId = m_topologyProvider.getVertexContainer().getParent(element.getItemId());
                String groupKey = groupId == null ? null : getKeyForItemId(groupId);
                
                element.setGroupId(groupId);
                element.setGroupKey(groupKey);
                
                return element;
            }

            @Override
            protected GVertex make(String key, Object itemId, Item item) {
                Object groupId = m_topologyProvider.getVertexContainer().getParent(itemId);
                String groupKey = groupId == null ? null : getKeyForItemId(groupId);
                //System.err.printf("GVertex Make Call :: Parent of itemId: %s with key %s groupId: %s groupKey %s\n" + key, itemId, key, groupId, groupKey);
                GVertex gVertex = new GVertex(key, itemId, item, groupKey, groupId);
                return gVertex;
            }
		};
		
        m_edgeHolder = new ElementHolder<GEdge>("gcE") {

            @Override
            protected GEdge make(String key, Object itemId, Item item) {

                Iterator<?> endPoints = m_topologyProvider.getEndPointIdsForEdge((String)itemId).iterator();

                Object sourceId = endPoints.next();
                Object targetId = endPoints.next();
                
                GVertex source = m_vertexHolder.getElementByItemId(sourceId);
                GVertex target = m_vertexHolder.getElementByItemId(targetId);

                return new GEdge(key, itemId, item, source, target);
            }

        };


		m_vertexContainer = new GVertexContainer();
		m_edgeContainer = new GEdgeContainer();
		
		setDataSource(topologyProvider);
	}
	
	@Override
	public SelectionManager getSelectionManager() {
		return m_selectionManager;
	}
	
	@Override
	public TopologyProvider getDataSource() {
		return m_topologyProvider;
	}
	
	@Override
	public void setDataSource(TopologyProvider topologyProvider) {
	    if (m_topologyProvider == topologyProvider) return;
	    
	    m_graphProvider = new TPGraphProvider(topologyProvider);
	    
	    m_topologyProvider = topologyProvider;
	    
	    m_vertexHolder.setContainer(m_topologyProvider.getVertexContainer());
	    
	    m_edgeHolder.setContainer(m_topologyProvider.getEdgeContainer());
	    
	    m_vertexContainer.setTopologyProvider(topologyProvider);
	    m_edgeContainer.setTopologyProvider(topologyProvider);
	    
	    m_graph = new TopoGraph(this);
	    
	    redoLayout();
	    
	    m_vertexContainer.addListener(new ItemSetChangeListener() {
			
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				SimpleGraphContainer.this.fireChange();
			}
		});
	    
	    m_edgeContainer.addListener(new ItemSetChangeListener() {
			
			@Override
			public void containerItemSetChange(ItemSetChangeEvent event) {
				SimpleGraphContainer.this.fireChange();
			}
		});

	}
	
	

	@Override
	public GraphProvider getBaseTopology() {
		return m_graphProvider;
	}

	@Override
	public void setBaseTopology(GraphProvider graphProvider) {
		throw new UnsupportedOperationException("SimpleGraphContainer.setBaseTopology is not yet implemented.");
	}

	public VertexContainer<Object, GVertex> getVertexContainer() {
		return m_vertexContainer;
	}

	public BeanContainer<String,GEdge> getEdgeContainer() {
		return m_edgeContainer;
	}

	public Collection<Object> getVertexIds() {
		return m_vertexContainer.getItemIds();
	}
	
	public Collection<String> getEdgeIds() {
		return m_edgeContainer.getItemIds();
	}

	public Collection<String> getEndPointIdsForEdge(Object key) {
	        if (key == null) return Collections.emptyList();
		GEdge edge = m_edgeHolder.getElementByKey(key.toString());
		if (edge == null) return Collections.emptyList();
		return Arrays.asList(edge.getSource().getKey(), edge.getTarget().getKey());
	}

	public Collection<String> getEdgeIdsForVertex(Object vertexKey) {
	    GVertex vertex = m_vertexHolder.getElementByKey(vertexKey.toString());
	    return m_edgeHolder.getKeysByItemId(m_topologyProvider.getEdgeIdsForVertex(vertex.getItemId()));
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

    public double getScale() {
        return m_scale;
    }
    
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

	public void fireChange() {
		for(ChangeListener listener : m_listeners) {
			listener.graphChanged(this);
		}
	}


    public Object getVertexItemIdForVertexKey(Object key) {
        Item vertexItem = getVertexContainer().getItem(key);
        return vertexItem == null ? null : vertexItem.getItemProperty("itemId").getValue();
    }
    
    public int getX(Object itemId) {
		BeanItem<GVertex> vertexItem = getVertexContainer().getItem(itemId);
		if (vertexItem == null) throw new NullPointerException("vertexItem "+ itemId +" is null");
		Property itemProperty = vertexItem.getItemProperty(X_PROPERTY);
		if (itemProperty == null) throw new NullPointerException("X property is null");
		return (Integer) itemProperty.getValue();
	}

    public int getY(Object itemId) {
		return (Integer) getVertexContainer().getItem(itemId).getItemProperty(Y_PROPERTY).getValue();
	}

    public void setX(Object itemId, int x) {
		getVertexContainer().getItem(itemId).getItemProperty(X_PROPERTY).setValue(x);
	}

    public void setY(Object itemId, int y) {
		getVertexContainer().getItem(itemId).getItemProperty(Y_PROPERTY).setValue(y);
	}

    public int getSemanticZoomLevel(Object itemId) {
		return getVertexItemProperty(itemId, SEMANTIC_ZOOM_LEVEL, Integer.valueOf(0));
	}
    
    public void setVertexItemProperty(Object itemId, String propertyName, Object value) {
    	getVertexContainer().getItem(itemId).getItemProperty(propertyName).setValue(value);
    }

	public <T> T getVertexItemProperty(Object itemId, String propertyName, T defaultValue) {
		Item vertexItem = getVertexContainer().getItem(itemId);
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
    
    public Object getGroupId(Object itemId) {
    	return getVertexContainer().getItem(itemId).getBean().getGroupKey();
    }

	boolean isLeaf(Object itemId) {
		Object value = getVertexContainer().getItem(itemId).getItemProperty(LEAF).getValue();
	    return (Boolean) value;
	}

	String getLabel(Object itemId) {
		Property labelProperty = getVertexContainer().getItem(itemId).getItemProperty(LABEL);
		String label = labelProperty == null ? "no such label" : (String)labelProperty.getValue();
		return label;
	}

	String getIconKey(Object itemId) {
		return (String) getVertexContainer().getItem(itemId).getItemProperty(ICON_KEY).getValue();
	}

	String getVertexTooltipText(Object itemId) {
		if(getVertexContainer().getItem(itemId).getItemProperty("tooltipText") != null && getVertexContainer().getItem(itemId).getItemProperty("tooltipText").getValue() != null) {
	        return (String) getVertexContainer().getItem(itemId).getItemProperty("tooltipText").getValue();
	    }else {
	        return getLabel(itemId);
	    }
	}
	
	public Object getDisplayVertexId(Object vertexId, int semanticZoomLevel) {

		int szl = getSemanticZoomLevel(vertexId);

		if(getGroupId(vertexId) == null || szl <= semanticZoomLevel) {
			return vertexId;
		}else {
			return getDisplayVertexId(getGroupId(vertexId), semanticZoomLevel);
		}
	}


	public Object getParentId(Object itemId) {
		return getVertexContainer().getParent(itemId);
	}


	public Collection<?> getChildren(Object itemId) {
		return getVertexContainer().getChildren(itemId);
	}


	public boolean hasChildren(Object itemId) {
		return getVertexContainer().hasChildren(itemId);
	}

	public boolean containsVertexId(Object vertexId) {
		return getVertexContainer().containsId(vertexId);
	}


	public boolean containsEdgeId(Object edgeId) {
		return getEdgeContainer().containsId(edgeId);
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

	public TopoGraph getCompleteGraph() {
		return m_graph;
	}

	@Override
	public Graph getGraph() {
		return m_graph;
	}
	
	private Object getItemId(VertexRef v) {
		return getVertex(v).getItemId();
	}

	public int getVertexX(VertexRef v) {
		Object itemId = getItemId(v);
		return itemId == null ? 0 : getX(itemId);
		
	}

	public int getVertexY(VertexRef v) {
		Object itemId = getItemId(v);
		return itemId == null ? 100 : getY(itemId);
		
	}

	public void setVertexX(VertexRef v, int x) {
		Object itemId = getItemId(v);
		if (itemId != null) {
			setX(itemId, x);
		}
	}
	
	public void setVertexY(VertexRef v, int y) {
		Object itemId = getItemId(v);
		if (itemId != null) {
			setY(itemId, y);
		}
	}

	public Vertex getParent(VertexRef childRef) {
		Object childId = getItemId(childRef);
		Object parentId = getParentId(childId);
		return parentId == null ? null : findVertexByItemId(parentId);
	}

	@Override
	public TopoVertex getVertex(VertexRef ref) {
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
	public Collection<? extends Vertex> getVertices() {
		return m_graph.getVertices();
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
