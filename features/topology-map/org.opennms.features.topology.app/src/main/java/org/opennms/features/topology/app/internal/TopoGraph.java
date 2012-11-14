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
import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionManager;

import com.vaadin.data.Item;


public class TopoGraph{
	
	public static final String PROP_X = "x";
	public static final String PROP_Y = "y";
	public static final String PROP_ICON = "icon";
	
	private SimpleGraphContainer m_dataSource;
	private ElementHolder<TopoVertex> m_vertexHolder;
	private ElementHolder<TopoEdge> m_edgeHolder;

	
	public TopoGraph(GraphContainer dataSource){
		
		if(dataSource == null) {
			throw new NullPointerException("dataSource may not be null");
		}
		setDataSource(dataSource);
		
	}
	
	public SimpleGraphContainer getGraphContainer() { return m_dataSource; }
	
	public int getSemanticZoomLevel() {
		return getGraphContainer().getSemanticZoomLevel();
	}
	
	public void setDataSource(GraphContainer dataSource) {
		if(dataSource == m_dataSource) {
			return;
		}
		
		m_dataSource = (SimpleGraphContainer) dataSource;
		
		m_vertexHolder = new ElementHolder<TopoVertex>(m_dataSource.getVertexContainer(), "tcV") {

            @Override
            List<TopoVertex> getElements() {
                return super.getElements();
            }

            @Override
			protected TopoVertex update(TopoVertex element) {
				Object groupId = m_dataSource.getParentId(element.getItemId());
				String groupKey = groupId == null ? null : getKeyForItemId(groupId);
				
				element.setGroupId(groupId);
				element.setGroupKey(groupKey);
				return element;
			}

			@Override
			protected TopoVertex make(String key, Object itemId, Item item) {
				Object groupId = m_dataSource.getParentId(itemId);
				String groupKey = groupId == null ? null : getKeyForItemId(groupId);
				// System.out.println("Graph Make Call :: Parent of itemId: " + itemId + " groupId: " + groupId);
				return new TopoVertex(m_dataSource, key, itemId, groupKey, groupId);
			}

		};
		
		m_edgeHolder = new ElementHolder<TopoEdge>(m_dataSource.getEdgeContainer(), "tcE") {

			@Override
			protected TopoEdge make(String key, Object itemId, Item item) {

				List<Object> endPoints = new ArrayList<Object>(m_dataSource.getEndPointIdsForEdge(itemId));

				if (endPoints.size() < 2) {
				    return null;
				}

				Object sourceId = endPoints.get(0);
				Object targetId = endPoints.get(1);
				
				TopoVertex source = m_vertexHolder.getElementByItemId(sourceId);
				TopoVertex target = m_vertexHolder.getElementByItemId(targetId);

				return new TopoEdge(m_dataSource, key, itemId, sourceId, source, targetId, target);
			}

		};
		
		
	}
	public void update() {
		m_vertexHolder.update();
		m_edgeHolder.update();
	}

	public DisplayState getDataSource() {
		return m_dataSource;
	}

	public List<TopoVertex> getVertices(){
		return m_vertexHolder.getElements();
	}
	
	public List<TopoVertex> getLeafVertices(){
		List<TopoVertex> elements = m_vertexHolder.getElements();
		List<TopoVertex> leaves = new ArrayList<TopoVertex>(elements.size());
		
		for(TopoVertex v : elements) {
			if (v.isLeaf()) {
				leaves.add(v);
			}
		}
		
		return leaves;
	}
	
	public List<TopoEdge> getEdges(){
		return m_edgeHolder.getElements();
	}
	
	public TopoVertex getVertexByKey(String key) {
		return m_vertexHolder.getElementByKey(key);
	}
	
	public List<TopoEdge> getEdgesForVertex(TopoVertex vertex){
		return m_edgeHolder.getElementsByItemIds(m_dataSource.getEdgeIdsForVertex(vertex.getItemId()));
	}

	public List<TopoEdge> getEdgesForVertex(TopoVertex vertex, int semanticZoomLevel){
		TopoVertex displayVertex = getDisplayVertex(vertex, semanticZoomLevel);
		List<TopoEdge> edges = getEdges(semanticZoomLevel);
		List<TopoEdge> visible = new ArrayList<TopoEdge>(edges.size());
		
		for(TopoEdge e : edges) {
			
			if (e.getSource().equals(displayVertex)) {
				visible.add(e);
			} else if (e.getTarget().equals(displayVertex)) {
				visible.add(e);
			}
			
		}

		return visible;
	}

	public TopoVertex getDisplayVertex(TopoVertex vertex, int semanticZoomLevel) {
		Object vertexId = m_dataSource.getDisplayVertexId(vertex.getItemId(), semanticZoomLevel);
		return m_vertexHolder.getElementByItemId(vertexId);
	}
	

	public List<TopoEdge> getEdges(int semanticZoomLevel) {
		List<TopoEdge> visible = new ArrayList<TopoEdge>();
		List<TopoEdge> edges = getEdges();
		
		for(TopoEdge edge : edges) {
			Object sourceId = edge.getSource().getItemId();
			Object targetId = edge.getTarget().getItemId();
			Object displaySourceId = getGraphContainer().getDisplayVertexId(sourceId, semanticZoomLevel);
			Object displayTargetId = getGraphContainer().getDisplayVertexId(targetId, semanticZoomLevel);
			
			if(displaySourceId.equals(displayTargetId)) {
				//skip this one
			}else if(displaySourceId.equals(sourceId) && displayTargetId.equals(targetId)) {
				visible.add(edge);
			}else {
				TopoVertex displaySource = m_vertexHolder.getElementByItemId(displaySourceId);
				TopoVertex displayTarget = m_vertexHolder.getElementByItemId(displayTargetId);
				TopoEdge displayEdge = new TopoEdge(m_dataSource, "bogus", null, displaySourceId, displaySource, displayTargetId, displayTarget);
				visible.add(displayEdge);
			}
		}
		
		return visible;
	}

	public TopoVertex getVertexByItemId(Object itemId) {
		return m_vertexHolder.getElementByItemId(itemId);
	}

    public TopoEdge getEdgeByItemId(String edgeItemId) {
        return m_edgeHolder.getElementByItemId(edgeItemId);
    }

    public TopoEdge getEdgeByKey(String edgeKey) {
        return m_edgeHolder.getElementByKey(edgeKey);
    }
    
    public void visit(GraphVisitor visitor) throws Exception {
    	
    	visitor.visitGraph(this);
    	
    	for(TopoVertex vertex : getVertices()) {
    		vertex.visit(visitor);
    	}
    	
    	for(TopoEdge edge : getEdges()) {
    		edge.visit(visitor);
    	}
    	
    	visitor.completeGraph(this);
    	
    }

	public List<?> getVertexItemIdsForKeys(List<String> vertexKeys) {
		return m_vertexHolder.getItemsIdsForKeys(vertexKeys);
	}

	SelectionManager getSelectionManager() {
		return m_dataSource.getSelectionManager();
	}
	
}
