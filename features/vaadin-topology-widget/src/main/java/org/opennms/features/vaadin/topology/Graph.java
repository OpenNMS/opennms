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

package org.opennms.features.vaadin.topology;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.vaadin.topology.gwt.client.GWTEdge;
import org.opennms.features.vaadin.topology.gwt.client.GWTVertex;

import com.google.gwt.core.client.JsArray;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.terminal.KeyMapper;


public class Graph{
	
	public static final String PROP_X = "x";
	public static final String PROP_Y = "y";
	public static final String PROP_ICON = "icon";
	
	private abstract class ElementHolder<T> {
		Container m_itemContainer;
		List<T> m_graphElements = Collections.emptyList();
		List<Object> m_itemIds = Collections.emptyList();
		KeyMapper m_elementKey2ItemId = new KeyMapper();
		Map<String, T> m_keyToElementMap = new HashMap<String, T>();
		
		ElementHolder(Container container) {
			
			m_itemContainer = container;
			
			update();
		}

		public void update() {
			List<Object> oldItemIds = m_itemIds;
			List<Object> newItemIds = new ArrayList<Object>(m_itemContainer.getItemIds());
			m_itemIds = newItemIds;
			
			Set<Object> newContainerItems = new LinkedHashSet<Object>(newItemIds);
			newContainerItems.removeAll(oldItemIds);
			
			Set<Object> removedContainerItems = new LinkedHashSet<Object>(oldItemIds);
			removedContainerItems.removeAll(newItemIds);
			
			m_graphElements = new ArrayList<T>(newItemIds.size());

			
			for(Object itemId : removedContainerItems) {
				String key = m_elementKey2ItemId.key(itemId);
				m_elementKey2ItemId.remove(itemId);
				m_keyToElementMap.remove(key);
			}
			
			for(T element : m_keyToElementMap.values()) {
				m_graphElements.add(update(element));
			}
			
			
			for(Object itemId : newContainerItems) {
			    String key = m_elementKey2ItemId.key(itemId);
			    
			    T v = make(key, itemId, m_itemContainer.getItem(itemId));
			    System.err.println("make v: " + v);
			    m_graphElements.add(v);

			    m_keyToElementMap.put(key, v);
			}
		}
		
		List<T> getElements(){
			return m_graphElements;
		}

		protected abstract T make(String key, Object itemId, Item item);
		
		protected T update(T element) { return element; }

		public T getElementByKey(String key) {
			return m_keyToElementMap.get(key);
		}
		
		public String getKeyForItemId(Object itemId) {
			return m_elementKey2ItemId.key(itemId);
		}
		
		public T getElementByItemId(Object itemId) {
			return getElementByKey(m_elementKey2ItemId.key(itemId));
		}
		
		public List<T> getElementsByItemIds(Collection<?> itemIds) {
			List<T> elements = new ArrayList<T>(itemIds.size());
			
			for(Object itemId : itemIds) {
				elements.add(getElementByItemId(itemId));
			}
			
			return elements;
		}

		
		
	}

	
	private GraphContainer m_dataSource;
	private ElementHolder<Vertex> m_vertexHolder;
	private ElementHolder<Edge> m_edgeHolder;

	
	public Graph(GraphContainer dataSource){
		
		if(dataSource == null) {
			throw new NullPointerException("dataSource may not be null");
		}
		setDataSource(dataSource);
		
	}
	
	public int getSemanticZoomLevel() {
		return m_dataSource.getSemanticZoomLevel();
	}
	
	public void setDataSource(GraphContainer dataSource) {
		if(dataSource == m_dataSource) {
			return;
		}
		
		m_dataSource = dataSource;
		
		m_vertexHolder = new ElementHolder<Vertex>(m_dataSource.getVertexContainer()) {

			@Override
			protected Vertex update(Vertex element) {
				Object groupId = m_dataSource.getVertexContainer().getParent(element.getItemId());
				String groupKey = groupId == null ? null : getKeyForItemId(groupId);
				
				element.setGroupId(groupId);
				element.setGroupKey(groupKey);
				return element;
			}

			@Override
			protected Vertex make(String key, Object itemId, Item item) {
				Object groupId = m_dataSource.getVertexContainer().getParent(itemId);
				String groupKey = groupId == null ? null : getKeyForItemId(groupId);
				System.out.println("Parent of itemId: " + itemId + " groupId: " + groupId);
				return new Vertex(key, itemId, item, groupKey, groupId);
			}

		};
		
		m_edgeHolder = new ElementHolder<Edge>(m_dataSource.getEdgeContainer()) {

			@Override
			protected Edge make(String key, Object itemId, Item item) {

				List<Object> endPoints = new ArrayList<Object>(m_dataSource.getEndPointIdsForEdge(itemId));

				Object sourceId = endPoints.get(0);
				Object targetId = endPoints.get(1);
				
				Vertex source = m_vertexHolder.getElementByItemId(sourceId);
				Vertex target = m_vertexHolder.getElementByItemId(targetId);

				return new Edge(key, itemId, item, source, target);
			}

		};
		
		
	}
	public void update() {
		m_vertexHolder.update();
		m_edgeHolder.update();
	}

	public GraphContainer getDataSource() {
		return m_dataSource;
	}

	public List<Vertex> getVertices(){
		return m_vertexHolder.getElements();
	}
	
	public List<Vertex> getLeafVertices(){
		List<Vertex> elements = m_vertexHolder.getElements();
		List<Vertex> leaves = new ArrayList<Vertex>(elements.size());
		
		for(Vertex v : elements) {
			if (v.isLeaf()) {
				leaves.add(v);
			}
		}
		
		return leaves;
	}
	
	public List<Edge> getEdges(){
		return m_edgeHolder.getElements();
	}
	
	public Vertex getVertexByKey(String key) {
		return m_vertexHolder.getElementByKey(key);
	}
	
	public List<Edge> getEdgesForVertex(Vertex vertex){
		return m_edgeHolder.getElementsByItemIds(m_dataSource.getEdgeIdsForVertex(vertex.getItemId()));
	}

	public List<Edge> getEdgesForVertex(Vertex vertex, int semanticZoomLevel){
		Vertex displayVertex = getDisplayVertex(vertex, semanticZoomLevel);
		List<Edge> edges = getEdges(semanticZoomLevel);
		List<Edge> visible = new ArrayList<Edge>(edges.size());
		
		for(Edge e : edges) {
			
			if (e.getSource() == displayVertex) {
				visible.add(e);
			} else if (e.getTarget() == displayVertex) {
				visible.add(e);
			}
			
		}

		return visible;
	}

	public List<Vertex> getVertices(int semanticZoomLevel) {
		List<Vertex> vertices = getLeafVertices();
		Set<Vertex> visible = new LinkedHashSet<Vertex>();
		
		for(Vertex vertex : vertices) {
			visible.add(getDisplayVertex(vertex, semanticZoomLevel));
		}
		
		return new ArrayList<Vertex>(visible);
	}

	public Vertex getDisplayVertex(Vertex vertex, int semanticZoomLevel) {
		if(vertex.getGroupId() == null || vertex.getSemanticZoomLevel() <= semanticZoomLevel) {
			return vertex;
		}else {
			Vertex group = m_vertexHolder.getElementByKey(vertex.getGroupKey());
			return getDisplayVertex(group, semanticZoomLevel);
		}
	}

	public List<Edge> getEdges(int semanticZoomLevel) {
		List<Edge> visible = new ArrayList<Edge>();
		List<Edge> edges = getEdges();
		
		for(Edge edge : edges) {
			Vertex source = edge.getSource();
			Vertex target = edge.getTarget();
			Vertex displaySource = getDisplayVertex(source, semanticZoomLevel);
			Vertex displayTarget = getDisplayVertex(target, semanticZoomLevel);
			
			if(displaySource == displayTarget) {
				//skip this one
			}else if(displaySource == source && displayTarget == target) {
				visible.add(edge);
			}else {
				
				Edge displayEdge = new Edge("bogus", null, null, displaySource, displayTarget);
				visible.add(displayEdge);
			}
		}
		
		return visible;
	}

	public Vertex getVertexByItemId(Object itemId) {
		return m_vertexHolder.getElementByItemId(itemId);
	}
	
}