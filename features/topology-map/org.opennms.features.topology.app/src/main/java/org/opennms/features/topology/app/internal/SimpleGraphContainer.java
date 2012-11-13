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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.plugins.topo.adapter.TPGraphProvider;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;

public class SimpleGraphContainer implements GraphContainer {

    public class GVertex {
        
        private static final String LEAF = "leaf";
		private static final String ICON = "icon";
		private static final String ICON_KEY = "iconKey";
		private static final String LABEL = "label";
		private static final String IP_ADDR = "ipAddr";
		private static final String NODE_ID = "nodeID";
		private static final String TOOLTIP_TEXT = "tooltipText";
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

        private void setItemId(Object itemId) {
            m_itemId = itemId;
        }

        private Item getItem() {
            return m_item;
        }

        private void setItem(Item item) {
            m_item = item;
        }

        public GVertex getSource() {
            return m_source;
        }

        private void setSource(GVertex source) {
            m_source = source;
        }

        public GVertex getTarget() {
            return m_target;
        }

        private void setTarget(GVertex target) {
            m_target = target;
        }
        
        public boolean isSelected() {
            return m_selected;
        }

        public void setSelected(boolean selected) {
            m_selected  = selected;
        }
        
        public String getTooltipText() {
            if(m_item.getItemProperty("tooltipText") != null && m_item.getItemProperty("tooltipText").getValue() != null) {
                return (String) m_item.getItemProperty("tooltipText").getValue();
            }else {
                return null;
            }
        }
    }
    
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
            fireContainerPropertySetChange();
        }

        @Override
        public void containerItemSetChange(ItemSetChangeEvent event) {
            LoggerFactory.getLogger(getClass()).debug("containerItemSetChange()");
            m_edgeHolder.update();
            removeAllItems();
            addAll(m_edgeHolder.getElements());
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
            fireContainerPropertySetChange();
        }
        
    }
    
    private final SelectionManager m_selectionManager;
	private LayoutAlgorithm m_layoutAlgorithm;
	private double m_scale = 1;
	private int m_semanticZoomLevel;
	private MethodProperty<Integer> m_zoomLevelProperty;
	private MethodProperty<Double> m_scaleProperty;
	
    private GVertexContainer m_vertexContainer;
    
    private ElementHolder<GVertex> m_vertexHolder;
    private ElementHolder<GEdge> m_edgeHolder;
    private TopologyProvider m_topologyProvider;
    private GraphProvider m_graphProvider;
    private GEdgeContainer m_edgeContainer;
    
	public SimpleGraphContainer() {
		m_selectionManager = new DefaultSelectionManager();
		m_zoomLevelProperty = new MethodProperty<Integer>(Integer.class, this, "getSemanticZoomLevel", "setSemanticZoomLevel");
		m_scaleProperty = new MethodProperty<Double>(Double.class, this, "getScale", "setScale");
		
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
                // System.out.println("GVertex Make Call :: Parent of itemId: " + itemId + " groupId: " + groupId);
                GVertex gVertex = new GVertex(key, itemId, item, groupKey, groupId);
                return gVertex;
            }
		};
		
        m_edgeHolder = new ElementHolder<GEdge>("gcE") {

            @Override
            protected GEdge make(String key, Object itemId, Item item) {

                List<Object> endPoints = new ArrayList<Object>(m_topologyProvider.getEndPointIdsForEdge(itemId));

                Object sourceId = endPoints.get(0);
                Object targetId = endPoints.get(1);
                
                GVertex source = m_vertexHolder.getElementByItemId(sourceId);
                GVertex target = m_vertexHolder.getElementByItemId(targetId);

                return new GEdge(key, itemId, item, source, target);
            }

        };


		m_vertexContainer = new GVertexContainer();
		m_edgeContainer = new GEdgeContainer();
		
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
	    
	    redoLayout();

	}
	
	

	@Override
	public GraphProvider getBaseTopology() {
		return m_graphProvider;
	}

	@Override
	public void setBaseTopology(GraphProvider graphProvider) {
		throw new UnsupportedOperationException("SimpleGraphContainer.setBaseTopology is not yet implemented.");
	}

	@Override
	public VertexContainer<Object, GVertex> getVertexContainer() {
		return m_vertexContainer;
	}

	@Override
	public BeanContainer<String,GEdge> getEdgeContainer() {
		return m_edgeContainer;
	}

	public Collection<Object> getVertexIds() {
		return m_vertexContainer.getItemIds();
	}
	
	public Collection<String> getEdgeIds() {
		return m_edgeContainer.getItemIds();
	}

	@Override
	public BeanItem<GVertex> getVertexItem(Object vertexId) {
		return m_vertexContainer.getItem(vertexId);
	}

	@Override
	public BeanItem<GEdge> getEdgeItem(Object edgeId) {
		return m_edgeContainer.getItem(edgeId); 
	}
	
	@Override
	public Collection<String> getEndPointIdsForEdge(Object key) {
	        if (key == null) return Collections.emptyList();
		GEdge edge = m_edgeHolder.getElementByKey(key.toString());
		if (edge == null) return Collections.emptyList();
		return Arrays.asList(edge.getSource().getKey(), edge.getTarget().getKey());
	}

	@Override
	public Collection<String> getEdgeIdsForVertex(Object vertexKey) {
	    GVertex vertex = m_vertexHolder.getElementByKey(vertexKey.toString());
	    return m_edgeHolder.getKeysByItemId(m_topologyProvider.getEdgeIdsForVertex(vertex.getItemId()));
	}
	
	public static Collection<String> getPropertyIds() {
	    return Arrays.asList(new String[] {"semanticZoomLevel", "scale"});
	}

	@Override
	public Property getProperty(String propertyId) {
	    if(propertyId.equals("semanticZoomLevel")) {
	        return m_zoomLevelProperty;
	    }else if(propertyId.equals("scale")) {
	        return m_scaleProperty;
	    }
		return null;
	}


    @Override
    public Integer getSemanticZoomLevel() {
        return m_semanticZoomLevel;
    }


    @Override
    public void setSemanticZoomLevel(Integer level) {
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

    public Double getScale() {
        return m_scale;
    }
    
    public void setScale(Double scale) {
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
		getVertexContainer().fireItemSetChange();
	}


    @Override
    public Object getVertexItemIdForVertexKey(Object key) {
        Item vertexItem = getVertexItem(key);
        return vertexItem == null ? null : vertexItem.getItemProperty("itemId").getValue();
    }
    
	@Override
    public int getX(Object itemId) {
		BeanItem<GVertex> vertexItem = getVertexItem(itemId);
		if (vertexItem == null) throw new NullPointerException("vertexItem "+ itemId +" is null");
		Property itemProperty = vertexItem.getItemProperty(TopoVertex.X_PROPERTY);
		if (itemProperty == null) throw new NullPointerException("X property is null");
		return (Integer) itemProperty.getValue();
	}

    @Override
    public int getY(Object itemId) {
		return (Integer) getVertexItem(itemId).getItemProperty(TopoVertex.Y_PROPERTY).getValue();
	}

    @Override
    public void setX(Object itemId, int x) {
		getVertexItem(itemId).getItemProperty(TopoVertex.X_PROPERTY).setValue(x);
	}

    @Override
    public void setY(Object itemId, int y) {
		getVertexItem(itemId).getItemProperty(TopoVertex.Y_PROPERTY).setValue(y);
	}

    @Override
    public int getSemanticZoomLevel(Object itemId) {
		BeanItem<GVertex> vertexItem = getVertexItem(itemId);
		Property itemProperty = vertexItem.getItemProperty(TopoVertex.SEMANTIC_ZOOM_LEVEL);
		Integer zoomLevel = (Integer) itemProperty.getValue();
	    return zoomLevel;
	}
    
    @Override
    public Object getGroupId(Object itemId) {
    	return getVertexItem(itemId).getBean().getGroupKey();
    }

	boolean isLeaf(Object itemId) {
		Object value = getVertexItem(itemId).getItemProperty(TopoVertex.LEAF_PROPERTY).getValue();
	    return (Boolean) value;
	}

	String getLabel(Object itemId) {
		Property labelProperty = getVertexItem(itemId).getItemProperty(TopoVertex.LABEL_PROPERTY);
		String label = labelProperty == null ? "no such label" : (String)labelProperty.getValue();
		return label;
	}

	String getIconKey(Object itemId) {
		return (String) getVertexItem(itemId).getItemProperty(TopoVertex.ICON_KEY).getValue();
	}

	String getTooltipText(Object itemId) {
		if(getVertexItem(itemId).getItemProperty("tooltipText") != null && getVertexItem(itemId).getItemProperty("tooltipText").getValue() != null) {
	        return (String) getVertexItem(itemId).getItemProperty("tooltipText").getValue();
	    }else {
	        return getLabel(itemId);
	    }
	}
	
	@Override
	public Object getDisplayVertexId(Object vertexId, int semanticZoomLevel) {

		int szl = getSemanticZoomLevel(vertexId);

		if(getGroupId(vertexId) == null || szl <= semanticZoomLevel) {
			return vertexId;
		}else {
			return getDisplayVertexId(getGroupId(vertexId), semanticZoomLevel);
		}
	}

	@Override
	public Collection<Object> getDisplayVertexIds(int semanticZoomLevel) {
		Set<Object> visibleVertexIds = new LinkedHashSet<Object>();
		for(Object itemId : getVertexIds()) {
			if (isLeaf(itemId)) {
				Object displayItemId = getDisplayVertexId(itemId, semanticZoomLevel);
				visibleVertexIds.add(displayItemId);
			}
		}
		return visibleVertexIds;
	}

	@Override
	public Object getParentId(Object itemId) {
		return getVertexContainer().getParent(itemId);
	}

	@Override
	public Collection<?> getChildren(Object itemId) {
		return getVertexContainer().getChildren(itemId);
	}

	@Override
	public boolean hasChildren(Object itemId) {
		return getVertexContainer().hasChildren(itemId);
	}

	@Override
	public boolean containsVertexId(Object vertexId) {
		return getVertexContainer().containsId(vertexId);
	}


	@Override
	public boolean containsEdgeId(Object edgeId) {
		return getEdgeContainer().containsId(edgeId);
	}
	
	@Override
	public Collection<?> getVertexForest(Collection<?> vertexIds) {
		Set<Object> processed = new LinkedHashSet<Object>();
		for(Object vertexId : vertexIds) {
			addTreeToSet(vertexId, processed);
		}
		return processed;
	}
	
	public void addTreeToSet(Object vertexId, Set<Object> processed) {
		processed.add(vertexId);

		for(Object childId : getChildren(vertexId)) {
			if (!processed.contains(childId)) {
				addTreeToSet(childId, processed);
			}
		}
	}

	@Deprecated
    public boolean isVertexSelected(Object itemId) {
		return (Boolean) getVertexItem(itemId).getItemProperty(TopoVertex.SELECTED_PROPERTY).getValue();
	}

	@Deprecated
    public void setVertexSelected(Object itemId, boolean selected) {
		getVertexItem(itemId).getItemProperty(TopoVertex.SELECTED_PROPERTY).setValue(selected);
	}

	@Deprecated
	public boolean isEdgeSelected(Object edgeId) {
		return (Boolean) getEdgeItem(edgeId).getItemProperty(TopoEdge.SELECTED_PROPERTY).getValue();
	}

	@Deprecated
	public void setEdgeSelected(Object edgeId, boolean selected) {
		getEdgeItem(edgeId).getItemProperty(TopoEdge.SELECTED_PROPERTY).setValue(selected);
	}
	
	


}
