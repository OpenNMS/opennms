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

package org.opennms.features.topology.plugins.topo.onmsdao.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.EditableGraphProvider;
import org.opennms.features.topology.api.SimpleEdge;
import org.opennms.features.topology.api.SimpleGroup;
import org.opennms.features.topology.api.SimpleVertex;
import org.opennms.features.topology.api.SimpleVertexContainer;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class OnmsTopologyProvider implements EditableGraphProvider {
    

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

    @Override
    private Item addVertex(String id, int x, int y, String icon) {
        if (m_vertexContainer.containsId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        LoggerFactory.getLogger(getClass()).debug("Adding a vertex: {}", id);
        SimpleVertex vertex = new SimpleLeafVertex(-1,id, x, y);
        vertex.setIcon(icon);
        return m_vertexContainer.addBean(vertex);
    }
    
    @Override
    public Item addGroup(String groupLabel, String icon) {
        if (m_vertexContainer.containsId(groupLabel)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupLabel + " already exists!");
        }
        LoggerFactory.getLogger(getClass()).debug("Adding a group: {}", groupLabel);
        SimpleVertex vertex = new SimpleGroup(-1,groupLabel);
        vertex.setIcon(icon);
        return m_vertexContainer.addBean(vertex);
        
    }

    private Edge connectVertices(String id, Vertex sourceVertextId, Vertex targetVertextId) {

        SimpleEdge edge = new SimpleEdge(id, sourceVertextId, targetVertextId);
        
        m_edgeContainer.addBean(edge);
        
        return edge;
    }
    
    @Override
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
    
    @Override
    public void save(String filename) {
        
        List<Vertex> vertices = getBeans(m_vertexContainer);
        int rootMapid = Integer.parseInt(filename);
        OnmsMap rootMap = getMap(rootMapid);
        getOnmsMapElementDao().deleteElementsByMapId(rootMap);
        
        for (Vertex vertex : vertices) {
            if (!vertex.isLeaf()) {
                Integer mapid = ((SimpleGroup)vertex).getMapid();
                getOnmsMapElementDao().deleteElementsByMapId(getMap(mapid));
            }
        }
        
        for (Vertex vertex : vertices) {
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
    
    @Override
    public void load(String filename) {

        OnmsMap map = getMap(Integer.parseInt(filename));
        List<Vertex> vertices = getVertex(map.getId(),null);
        List<SimpleEdge> edges = getEdges(vertices);
        
        m_vertexContainer.removeAllItems();
        m_vertexContainer.addAll(vertices);
        
        m_edgeContainer.removeAllItems();
        m_edgeContainer.addAll(edges);
    }
    

    private List<Vertex> getVertex(int mapId, SimpleGroup parent) {
        List<Vertex> vertexes = new ArrayList<Vertex>();
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

    private List<SimpleEdge> getEdges(List<Vertex> vertexes) {
        List<SimpleEdge> edges = new ArrayList<SimpleEdge>();
        List<SimpleLeafVertex> leafs = new ArrayList<SimpleLeafVertex>();
        for (Vertex vertex: leafs ) {
            if (vertex.isLeaf())
                leafs.add((SimpleLeafVertex) vertex);
        }
        
        for (SimpleLeafVertex target: leafs){
            for (DataLinkInterface link: getDataLinkInterfaceDao().findByNodeParentId(target.getNodeid())) {
                for (SimpleLeafVertex source: leafs) {
                   if ( link.getNode().getId() == source.getNodeid() ) {
                       SimpleEdge edge = new SimpleEdge("onmsdao", link.getId().toString(), source, target);
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

    private String getNextVertexId() {
        return "v" + m_counter++;
    }

    private String getNextEdgeId() {
        return "e" + m_edgeCounter ++;
    }
    
    @Override
    public void resetContainer() {
        m_vertexContainer.removeAllItems();
        m_edgeContainer.removeAllItems();
        
        m_counter = 0;
        m_edgeCounter = 0;
    }

    @Override
    public Vertex addVertex(int nodeid, int x, int y, String icon) {
        LoggerFactory.getLogger(getClass()).debug("Adding vertex in {} with icon: {}", getClass().getSimpleName(), icon);
        String nextVertexId = getNextVertexId();
        addVertex(nextVertexId, x, y, icon);
        return nextVertexId;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }

    @Override
    public Edge connectVertices(Vertex sourceVertextId, Vertex targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        return connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
    }

	@Override
	public String getNamespace() {
		return "onmsdao";
	}
}