package org.opennms.features.topology.app.internal;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.GraphContainer;

import com.vaadin.data.Item;


public class Graph{
	
	public static final String PROP_X = "x";
	public static final String PROP_Y = "y";
	public static final String PROP_ICON = "icon";
	
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
		
		m_vertexHolder = new ElementHolder<Vertex>(m_dataSource.getVertexContainer(), "tcV") {

            @Override
            List<Vertex> getElements() {
                return super.getElements();
            }

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
				System.out.println("Graph Make Call :: Parent of itemId: " + itemId + " groupId: " + groupId);
				return new Vertex(key, itemId, item, groupKey, groupId);
			}

		};
		
		m_edgeHolder = new ElementHolder<Edge>(m_dataSource.getEdgeContainer(), "tcE") {

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

	public DisplayState getDataSource() {
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
		int szl = vertex.getSemanticZoomLevel();
        if(vertex.getGroupId() == null || szl <= semanticZoomLevel) {
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