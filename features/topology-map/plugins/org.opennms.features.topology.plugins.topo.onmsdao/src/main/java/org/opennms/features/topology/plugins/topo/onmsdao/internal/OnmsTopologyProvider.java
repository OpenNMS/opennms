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
import java.util.List;

import org.opennms.features.topology.api.SimpleEdge;
import org.opennms.features.topology.api.SimpleGroup;
import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.SimpleVertex;
import org.opennms.features.topology.api.topo.DelegatingVertexEdgeProvider;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.slf4j.LoggerFactory;

public class OnmsTopologyProvider extends DelegatingVertexEdgeProvider implements GraphProvider {

    private int m_counter = 0;
    private int m_edgeCounter = 0;

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
        super("onmsdao");
    }

    private Vertex addVertex(String id, int x, int y, String icon) {
        if (containsVertexId(id)) {
            throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
        }
        LoggerFactory.getLogger(getClass()).debug("Adding a vertex: {}", id);
        SimpleVertex vertex = new SimpleLeafVertex(id, x, y);
        vertex.setNodeID(-1);
        vertex.setIconKey(icon);
        addVertices(vertex);
        return vertex;
    }
    
    @Override
    public Vertex addGroup(String groupLabel, String icon) {
        if (containsVertexId(groupLabel)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupLabel + " already exists!");
        }
        LoggerFactory.getLogger(getClass()).debug("Adding a group: {}", groupLabel);
        SimpleVertex vertex = new SimpleGroup(groupLabel, -1);
        vertex.setIconKey(icon);
        addVertices(vertex);
        return vertex;
    }

    private Edge connectVertices(String id, VertexRef sourceVertextId, VertexRef targetVertextId) {

        SimpleEdge edge = new SimpleEdge(id, sourceVertextId, targetVertextId);
        
        addEdges(edge);
        
        return edge;
    }
    
    @Override
    public void removeVertex(VertexRef... vertexId) {
        for (VertexRef vertex : vertexId) {
            if (vertex == null) return;
            
            removeVertex(vertexId);
            
            for(EdgeRef e : getEdgeIdsForVertex(vertex)) {
                removeEdges(e);
            }
        }
    }

    private Vertex getVertex(VertexRef vertexId, boolean required) {
        Vertex item = getVertex(vertexId);
        if (required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }
        
        return item;
    }

    private OnmsMap getMap(int mapId) {
        return getOnmsMapDao().findMapById(mapId);
    }
    
    @Override
    public void save(String filename) {
        
        List<Vertex> vertices = super.getVertices();
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
            VertexRef parent = vertex.getParent();
            if (parent == null) {
                mapid = rootMapid;
            } else {
                mapid = parent.getMapid();
            }
            OnmsMap map = getMap(mapid);
            getOnmsMapElementDao().save(new OnmsMapElement(map, id, type, "Here is the label", vertex.getIconKey(), vertex.getX(), vertex.getY()));
        }
    }
    
    @Override
    public void load(String filename) {

        OnmsMap map = getMap(Integer.parseInt(filename));
        List<Vertex> vertices = getVertex(map.getId(),null);
        List<Edge> edges = getEdges(vertices);
        
        clearVertices();
        addVertices(vertices.toArray(new Vertex[] {}));
        
        clearEdges();
        addEdges(edges.toArray(new Edge[] {}));
    }
    

    private List<Vertex> getVertex(int mapId, SimpleGroup parent) {
        List<Vertex> vertexes = new ArrayList<Vertex>();
        for (OnmsMapElement element: getOnmsMapElementDao().findNodeElementsOnMap(mapId)) {
            SimpleLeafVertex vertex = new SimpleLeafVertex(element.getElementId(),Integer.toString(element.getId()),element.getX(),element.getY());
            vertex.setLocked(false);
            vertex.setSelected(false);
            vertex.setIconKey(element.getIconName());
            vertex.setParent(parent);
            vertexes.add(vertex);
        }
        
        for (OnmsMapElement element: getOnmsMapElementDao().findMapElementsOnMap(mapId)) {
            SimpleGroup vertex = new SimpleGroup(Integer.toString(element.getId()), element.getElementId());
            vertex.setLocked(false);
            vertex.setSelected(false);
            vertex.setIconKey(element.getIconName());
            vertex.setParent(parent);
            vertexes.add(vertex);
            vertexes.addAll(getVertex(element.getElementId(),vertex));
        }
        
        return vertexes;
    }

    private List<Edge> getEdges(List<Vertex> vertexes) {
        List<Edge> edges = new ArrayList<Edge>();
        List<Vertex> leafs = new ArrayList<Vertex>();
        for (Vertex vertex: leafs ) {
            if (vertex.isLeaf())
                leafs.add((SimpleLeafVertex) vertex);
        }
        
        for (Vertex target: leafs){
            for (DataLinkInterface link: getDataLinkInterfaceDao().findByNodeParentId(target.getNodeid())) {
                for (Vertex source: leafs) {
                   if ( link.getNode().getId() == source.getNodeid() ) {
                       SimpleEdge edge = new SimpleEdge("onmsdao", link.getId().toString(), source, target);
                       edges.add(edge);
                   }
                }
            }
        }
        return edges;
    }

    private String getNextVertexId() {
        return "v" + m_counter++;
    }

    private String getNextEdgeId() {
        return "e" + m_edgeCounter ++;
    }
    
    @Override
    public void resetContainer() {
        clearVertices();
        clearEdges();
        
        m_counter = 0;
        m_edgeCounter = 0;
    }

    @Override
    public Vertex addVertex(int nodeid, int x, int y, String icon) {
        LoggerFactory.getLogger(getClass()).debug("Adding vertex in {} with icon: {}", getClass().getSimpleName(), icon);
        String nextVertexId = getNextVertexId();
        return addVertex(nextVertexId, x, y, icon);
    }

    @Override
    public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        return connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
    }
}