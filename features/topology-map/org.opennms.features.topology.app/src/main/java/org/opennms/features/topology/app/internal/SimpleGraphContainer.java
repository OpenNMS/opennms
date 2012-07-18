package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;

import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;

public class SimpleGraphContainer implements GraphContainer {

    public class GVertex{
        
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
            return (Boolean) getItem().getItemProperty("leaf").getValue();
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
            return (String) m_item.getItemProperty("icon").getValue();
        }

        public void setIcon(String icon) {
            m_item.getItemProperty("icon").setValue(icon);
        }

        private GVertex getParent() {
            if (m_groupKey == null) return null;
            
            return m_vertexHolder.getElementByKey(m_groupKey);
        }
        
        public int getSemanticZoomLevel() {
            GVertex parent = getParent();
            return parent == null ? 0 : parent.getSemanticZoomLevel() + 1;
        }

    }
    
    public class GEdge{

        private String m_key;
        private Object m_itemId;
        private Item m_item;
        private GVertex m_source;
        private GVertex m_target;

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
        
    }
    
    private class GEdgeContainer extends BeanContainer<String, GEdge> implements ItemSetChangeListener, PropertySetChangeListener{

        public GEdgeContainer() {
            super(GEdge.class);
            setBeanIdProperty("key");
            addAll(m_edgeHolder.getElements());
            m_topologyProvider.getEdgeContainer().addListener((ItemSetChangeListener)this);
            m_topologyProvider.getEdgeContainer().addListener((PropertySetChangeListener)this);
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
            m_edgeHolder.update();
            removeAllItems();
            addAll(m_edgeHolder.getElements());
            fireItemSetChange();
        }
        
        
    }
     
    private class GVertexContainer extends VertexContainer<Object, GVertex> implements ItemSetChangeListener, PropertySetChangeListener{

        public GVertexContainer() {
            super(GVertex.class);
            setBeanIdProperty("key");
            addAll(m_vertexHolder.getElements());
            m_topologyProvider.getVertexContainer().addListener((ItemSetChangeListener)this);
            m_topologyProvider.getVertexContainer().addListener((PropertySetChangeListener)this);
        }

        @Override
        public Collection<?> getChildren(Object gItemId) {
            GVertex v = m_vertexHolder.getElementByKey(gItemId.toString());
            Collection<?> children = m_topologyProvider.getVertexContainer().getChildren(v.getItemId());
            
            return m_vertexHolder.getKeysByItemId(children);
        }
        
        @Override
        public Object getParent(Object gItemId) {
            GVertex vertex = m_vertexHolder.getElementByKey(gItemId.toString());
            return vertex.getGroupKey();
        }

        @Override
        public Collection<?> rootItemIds() {
            return m_vertexHolder.getKeysByItemId(m_topologyProvider.getVertexContainer().rootItemIds());
        }

        @Override
        public boolean setParent(Object gKey, Object gNewParentKey) throws UnsupportedOperationException {
           if(!containsId(gKey)) return false;
           
           GVertex vertex = m_vertexHolder.getElementByKey(gKey.toString());
           GVertex parentVertex = m_vertexHolder.getElementByKey(gNewParentKey.toString());
           
           if(m_topologyProvider.getVertexContainer().setParent(vertex.getItemId(), parentVertex.getItemId())) {
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
            return m_topologyProvider.getVertexContainer().isRoot(vertex.getItemId());
        }

        @Override
        public boolean hasChildren(Object key) {
            GVertex vertex = m_vertexHolder.getElementByKey(key.toString());
            return m_topologyProvider.getVertexContainer().hasChildren(vertex.getItemId());
        }

        @Override
        public void containerItemSetChange(ItemSetChangeEvent event) {
            System.err.println("containerItemSetChange called in GVertexContainer");
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
                removeItem(v.getItemId());
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
            System.err.println("containerPropertySetChange called in GVertexContainer");
            m_vertexHolder.update();
            removeAllItems();
            addAll(m_vertexHolder.getElements());
            fireContainerPropertySetChange();
        }
        
    }
    
	private LayoutAlgorithm m_layoutAlgorithm;
	private double m_scale = 1;
	private int m_semanticZoomLevel;
	private MethodProperty<Integer> m_zoomLevelProperty;
	private MethodProperty<Double> m_scaleProperty;
	
    private GVertexContainer m_vertexContainer;
    
    private ElementHolder<GVertex> m_vertexHolder;
    private ElementHolder<GEdge> m_edgeHolder;
    private TopologyProvider m_topologyProvider;
    private BeanContainer<?, ?> m_edgeContainer;

	
	public SimpleGraphContainer(TopologyProvider topologyProvider) {
		m_zoomLevelProperty = new MethodProperty<Integer>(Integer.class, this, "getSemanticZoomLevel", "setSemanticZoomLevel");
		m_scaleProperty = new MethodProperty<Double>(Double.class, this, "getScale", "setScale");
		
		setDataSource(topologyProvider);
		
		m_vertexContainer = new GVertexContainer();
		m_edgeContainer = new GEdgeContainer();
		
	}
	
	
	private void setDataSource(TopologyProvider topologyProvider) {
	    if (m_topologyProvider == topologyProvider) return;
	    
	    m_topologyProvider = topologyProvider;
	    
        m_vertexHolder = new ElementHolder<GVertex>(m_topologyProvider.getVertexContainer(), "gcV") {

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
                System.out.println("GVertex Make Call :: Parent of itemId: " + itemId + " groupId: " + groupId);
                GVertex gVertex = new GVertex(key, itemId, item, groupKey, groupId);
                return gVertex;
            }

        };
        
        m_edgeHolder = new ElementHolder<GEdge>(m_topologyProvider.getEdgeContainer(), "gcE") {

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
	    
	}

	public VertexContainer<?,?> getVertexContainer() {
		return m_vertexContainer;
	}

	public BeanContainer<?, ?> getEdgeContainer() {
		return m_edgeContainer;
	}

	public Collection<?> getVertexIds() {
		return m_vertexContainer.getItemIds();
	}

	public Collection<?> getEdgeIds() {
		return m_edgeContainer.getItemIds();
	}

	public Item getVertexItem(Object vertexId) {
		return m_vertexContainer.getItem(vertexId);
	}

	public Item getEdgeItem(Object edgeId) {
		return m_edgeContainer.getItem(edgeId); 
	}
	
	public Collection<?> getEndPointIdsForEdge(Object key) {
		GEdge edge = m_edgeHolder.getElementByKey(key.toString());
		return Arrays.asList(edge.getSource().getKey(), edge.getTarget().getKey());
	}

	public Collection<?> getEdgeIdsForVertex(Object vertexKey) {
	    GVertex vertex = m_vertexHolder.getElementByKey(vertexKey.toString());
	    return m_edgeHolder.getKeysByItemId(m_topologyProvider.getEdgeIdsForVertex(vertex.getItemId()));
	}
	
	public Collection<?> getPropertyIds() {
	    return Arrays.asList(new String[] {"semanticZoomLevel", "scale"});
	}

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
        if(m_layoutAlgorithm != null) {
            m_layoutAlgorithm.updateLayout(this);
            getVertexContainer().fireItemSetChange();
        }
    }


    @Override
    public Object getVertexItemIdForVertexKey(Object key) {
        Item vertexItem = getVertexItem(key);
        return vertexItem == null ? null : vertexItem.getItemProperty("itemId").getValue();
    }

}
