package org.opennms.features.vaadin.topology;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public List<Edge> getEdges(){
		return m_edgeHolder.getElements();
	}
	
	public Vertex getVertexByKey(String key) {
		return m_vertexHolder.getElementByKey(key);
	}
	
	public List<Edge> getEdgesForVertex(Vertex vertex){
		return m_edgeHolder.getElementsByItemIds(m_dataSource.getEdgeIdsForVertex(vertex.getItemId()));
	}
	
}