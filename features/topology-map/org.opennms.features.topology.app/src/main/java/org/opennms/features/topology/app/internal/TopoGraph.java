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

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.topo.GraphVisitor;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Item;


public class TopoGraph implements Graph {
	
	public static final String PROP_X = "x";
	public static final String PROP_Y = "y";
	public static final String PROP_ICON = "icon";
	
	private SimpleGraphContainer m_dataSource;
	private ElementHolder<TopoVertex> m_vertexHolder;
	private ElementHolder<TopoEdge> m_edgeHolder;
	private Layout m_layout;

	
	public TopoGraph(SimpleGraphContainer dataSource){	
		
		if(dataSource == null) {
			throw new NullPointerException("dataSource may not be null");
		}
		setDataSource(dataSource);
		
	}
	
	private SimpleGraphContainer getGraphContainer() { return m_dataSource; }
	
	private int getSemanticZoomLevel() {
		return getGraphContainer().getSemanticZoomLevel();
	}
	
	private void setDataSource(SimpleGraphContainer dataSource) {
		if(dataSource == m_dataSource) {
			return;
		}
		
		m_dataSource = dataSource;
		
		m_vertexHolder = new ElementHolder<TopoVertex>(m_dataSource.getVertexContainer(), "tcV") {

            @Override
            List<TopoVertex> getElements() {
                return super.getElements();
            }

            @Override
			protected TopoVertex update(TopoVertex element) {
				return element;
			}

			@Override
			protected TopoVertex make(String key, Object itemId, Item item) {
				// System.out.println("Graph Make Call :: Parent of itemId: " + itemId + " groupId: " + groupId);
				return new TopoVertex(m_dataSource, key, itemId, item);
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
		
		m_layout = new DefaultLayout(dataSource);
		
		
	}
	public void update() {
		m_vertexHolder.update();
		m_edgeHolder.update();
	}

	public List<TopoVertex> getVertices(){
		return m_vertexHolder.getElements();
	}
	
	private boolean eq(VertexRef a, VertexRef b) {
		return a.getNamespace().equals(b.getNamespace()) && a.getId().equals(b.getId());
	}
	
	public TopoVertex getVertex(VertexRef ref) {
		for(TopoVertex v : getVertices()) {
			if (eq(v, ref)) {
				return v;
			}
		}
		return null;
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
	
	public List<TopoEdge> getEdges(int semanticZoomLevel) {
		List<TopoEdge> visible = new ArrayList<TopoEdge>();
		List<TopoEdge> edges = getEdges();
		
		for(TopoEdge edge : edges) {
			Object sourceId = edge.getSourceVertex().getItemId();
			Object targetId = edge.getTargetVertex().getItemId();
			Object displaySourceId = getGraphContainer().getDisplayVertexId(sourceId, semanticZoomLevel);
			Object displayTargetId = getGraphContainer().getDisplayVertexId(targetId, semanticZoomLevel);
			
			if(displaySourceId.equals(displayTargetId)) {
				//skip this one
			}else if(displaySourceId.equals(sourceId) && displayTargetId.equals(targetId)) {
				visible.add(edge);
			}else {
				TopoVertex displaySource = m_vertexHolder.getElementByItemId(displaySourceId);
				TopoVertex displayTarget = m_vertexHolder.getElementByItemId(displayTargetId);
				TopoEdge displayEdge = new TopoEdge(m_dataSource, "bogus:"+displaySourceId+":"+displayTargetId, null, displaySourceId, displaySource, displayTargetId, displayTarget);
				visible.add(displayEdge);
			}
		}
		
		return visible;
	}

	public TopoVertex getVertexByItemId(Object itemId) {
		return m_vertexHolder.getElementByItemId(itemId);
	}

    public TopoEdge getEdgeByKey(String edgeKey) {
        return m_edgeHolder.getElementByKey(edgeKey);
    }
    
    public void visit(GraphVisitor visitor) throws Exception {
    	
    	visitor.visitGraph(this);
    	
    	for(TopoVertex vertex : getDisplayVertices()) {
    		visitor.visitVertex(vertex);
    	}

    	for(TopoEdge edge : getDisplayEdges()) {
    		visitor.visitEdge(edge);
    	}
    	
    	visitor.completeGraph(this);
    	
    }

    @Override
	public List<TopoEdge> getDisplayEdges() {
		return getEdges(getSemanticZoomLevel());
	}

    @Override
	public List<TopoVertex> getDisplayVertices() {
		List <TopoVertex> displayVertices = new ArrayList<TopoVertex>();
    	for(TopoVertex vertex : getVertices()) {
    		if (vertex.getSemanticZoomLevel() == getSemanticZoomLevel() ||
    		    (vertex.getSemanticZoomLevel() < getSemanticZoomLevel() && vertex.isLeaf())
    			) {
    			displayVertices.add(vertex);
    		}
    	}
		return displayVertices;
	}

	public List<?> getVertexItemIdsForKeys(List<String> vertexKeys) {
		return m_vertexHolder.getItemsIdsForKeys(vertexKeys);
	}

	@Override
	public Layout getLayout() {
		return m_layout;
	}

	
}
