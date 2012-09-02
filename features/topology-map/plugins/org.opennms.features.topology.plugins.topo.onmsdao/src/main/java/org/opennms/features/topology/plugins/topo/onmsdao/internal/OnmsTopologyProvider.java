package org.opennms.features.topology.plugins.topo.onmsdao.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class OnmsTopologyProvider implements TopologyProvider{
    

    private SimpleVertexContainer m_vertexContainer;
    private BeanContainer<String, SimpleEdge> m_edgeContainer;
    private int m_counter = 0;
    private int m_edgeCounter = 0;
    private int m_groupCounter = 0;
    
    private OnmsMapDao m_onmsMapDao;
    private OnmsMapElementDao m_onmsMapElementDao;
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    
    public OnmsMapDao getOnmsMapDao() {
        return m_onmsMapDao;
    }

    public void setOnmsMapDao(OnmsMapDao onmsMapDao) {
        m_onmsMapDao = onmsMapDao;
    }

    public OnmsMapElementDao getOnmsMapElementDao() {
        return m_onmsMapElementDao;
    }

    public void setOnmsMapElementDao(OnmsMapElementDao onmsMapElementDao) {
        m_onmsMapElementDao = onmsMapElementDao;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    public OnmsTopologyProvider() {
        m_vertexContainer = new SimpleVertexContainer();
        m_edgeContainer = new BeanContainer<String, SimpleEdge>(SimpleEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
    }

    public SimpleVertexContainer getVertexContainer() {
        return m_vertexContainer;
    }

    public BeanContainer<String, SimpleEdge> getEdgeContainer() {
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
    
    public Collection<?> getEndPointIdsForEdge(Object edgeId) {
        
        SimpleEdge edge = getRequiredEdge(edgeId);

        List<Object> endPoints = new ArrayList<Object>(2);
        
        endPoints.add(edge.getSource().getId());
        endPoints.add(edge.getTarget().getId());

        return endPoints;
    }

    public Collection<?> getEdgeIdsForVertex(Object vertexId) {
        
        SimpleVertex vertex = getRequiredVertex(vertexId);
        
        List<Object> edges = new ArrayList<Object>(vertex.getEdges().size());
        
        for(SimpleEdge e : vertex.getEdges()) {
            
            Object edgeId = e.getId();
            
            edges.add(edgeId);

        }
        
        return edges;

    }
    
    private Item addVertex(String id, int x, int y, String icon) {
        if (m_vertexContainer.containsId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        System.err.println("Adding a vertex: " + id);
        SimpleVertex vertex = new SimpleLeafVertex(-1,id, x, y);
        vertex.setIcon(icon);
        return m_vertexContainer.addBean(vertex);
    }
    
    private Item addGroup(String groupId, String icon) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        System.err.println("Adding a group: " + groupId);
        SimpleVertex vertex = new SimpleGroup(-1,groupId);
        vertex.setIcon(icon);
        return m_vertexContainer.addBean(vertex);
        
    }
    private void connectVertices(String id, Object sourceVertextId, Object targetVertextId) {
        SimpleVertex source = getRequiredVertex(sourceVertextId);
        SimpleVertex target = getRequiredVertex(targetVertextId);
        
        SimpleEdge edge = new SimpleEdge(id, source, target);
        
        m_edgeContainer.addBean(edge);
        
    }
    
    public void removeVertex(Object vertexId) {
        
        SimpleVertex vertex = getVertex(vertexId, false);
        if (vertex == null) return;
        
        m_vertexContainer.removeItem(vertexId);
        
        for(SimpleEdge e : vertex.getEdges()) {
            m_edgeContainer.removeItem(e.getId());
        }
                
        
    }

    private SimpleVertex getRequiredVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    private SimpleVertex getVertex(Object vertexId, boolean required) {
        BeanItem<SimpleVertex> item = m_vertexContainer.getItem(vertexId);
        if (required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    private SimpleEdge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private SimpleEdge getEdge(Object edgeId, boolean required) {
        BeanItem<SimpleEdge> item = m_edgeContainer.getItem(edgeId);
        if (required && item == null) {
            throw new IllegalArgumentException("required edge " + edgeId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }
    
    private OnmsMap getMap(int mapId) {
        return getOnmsMapDao().findMapById(mapId);
    }
    
    public void save(String filename) {
        
        List<SimpleVertex> vertices = getBeans(m_vertexContainer);
        int rootMapid = Integer.parseInt(filename);
        OnmsMap rootMap = getMap(rootMapid);
        getOnmsMapElementDao().deleteElementsByMapId(rootMap);
        
        for (SimpleVertex vertex : vertices) {
            if (!vertex.isLeaf()) {
                Integer mapid = ((SimpleGroup)vertex).getMapid();
                getOnmsMapElementDao().deleteElementsByMapId(getMap(mapid));
            }
        }
        
        for (SimpleVertex vertex : vertices) {
            Integer mapid;
            Integer id;
            String type;
            if (vertex.isLeaf()) {
                id = ((SimpleLeafVertex)vertex).getNodeid();
                type=OnmsMapElement.NODE_TYPE;
            } else {
                id = ((SimpleGroup)vertex).getMapid();
                type=OnmsMapElement.MAP_TYPE;
            }
            SimpleGroup parent = vertex.getParent();
            if (parent == null) {
                mapid = rootMapid;
            } else {
                mapid = parent.getMapid();
            }
            OnmsMap map = getMap(mapid);
            getOnmsMapElementDao().save(new OnmsMapElement(map, id, type, "Here is the label", vertex.getIcon(), vertex.getX(), vertex.getY()));
        }
    }
    
    public void load(String filename) {

        OnmsMap map = getMap(Integer.parseInt(filename));
        List<SimpleVertex> vertices = getVertex(map.getId(),null);
        List<SimpleEdge> edges = getEdges(vertices);
        
        m_vertexContainer.removeAllItems();
        m_vertexContainer.addAll(vertices);
        
        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(edges);
    }
    

    private List<SimpleVertex> getVertex(int mapId, SimpleGroup parent) {
        List<SimpleVertex> vertexes = new ArrayList<SimpleVertex>();
        for (OnmsMapElement element: getOnmsMapElementDao().findNodeElementsOnMap(mapId)) {
            SimpleLeafVertex vertex = new SimpleLeafVertex(element.getElementId(),Integer.toString(element.getId()),element.getX(),element.getY());
            vertex.setLocked(false);
            vertex.setSelected(false);
            vertex.setIcon(element.getIconName());
            vertex.setParent(parent);
            vertexes.add(vertex);
        }
        
        for (OnmsMapElement element: getOnmsMapElementDao().findMapElementsOnMap(mapId)) {
            SimpleGroup vertex = new SimpleGroup(element.getElementId(),Integer.toString(element.getId()));
            vertex.setLocked(false);
            vertex.setSelected(false);
            vertex.setIcon(element.getIconName());
            vertex.setParent(parent);
            vertexes.add(vertex);
            vertexes.addAll(getVertex(element.getElementId(),vertex));
        }
        
        return vertexes;
    }

    private List<SimpleEdge> getEdges(List<SimpleVertex> vertexes) {
        List<SimpleEdge> edges = new ArrayList<SimpleEdge>();
        List<SimpleLeafVertex> leafs = new ArrayList<SimpleLeafVertex>();
        for (SimpleVertex vertex: leafs ) {
            if (vertex.isLeaf())
                leafs.add((SimpleLeafVertex) vertex);
        }
        
        for (SimpleLeafVertex target: leafs){
            for (DataLinkInterface link: getDataLinkInterfaceDao().findByNodeParentId(target.getNodeid())) {
                for (SimpleLeafVertex source: leafs) {
                   if ( link.getNode().getId() == source.getNodeid() ) {
                       SimpleEdge edge = new SimpleEdge(link.getId().toString(), source, target);
                       edges.add(edge);
                   }
                }
            }
        }
        return edges;
    }

    private <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());
        
        for(Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }
        
        return beans;
    }

    public String getNextVertexId() {
        return "v" + m_counter++;
    }

    public String getNextEdgeId() {
        return "e" + m_edgeCounter ++;
    }
    
    public String getNextGroupId() {
        return "g" + m_groupCounter++;
    }

    public void resetContainer() {
        getVertexContainer().removeAllItems();
        getEdgeContainer().removeAllItems();
        
        m_counter = 0;
        m_edgeCounter = 0;
    }

    public Collection<?> getPropertyIds() {
        return Collections.EMPTY_LIST;
    }

    public Property getProperty(String propertyId) {
        return null;
    }
    
    
    public Object addVertex(int nodeid, int x, int y, String icon) {
        System.err.println("Adding vertex in SimpleTopologyProvider with icon: " + icon);
        String nextVertexId = getNextVertexId();
        addVertex(nextVertexId, x, y, icon);
        return nextVertexId;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }

    public Object connectVertices(Object sourceVertextId, Object targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
        return nextEdgeId;
    }

    @Override
    public Object addGroup(String groupIcon) {
        String nextGroupId = getNextGroupId();
        addGroup(nextGroupId, groupIcon);
        return nextGroupId;
    }

    @Override
    public boolean containsVertexId(Object vertexId) {
        return m_vertexContainer.containsId(vertexId);
    }
    
}