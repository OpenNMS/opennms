package org.opennms.features.vaadin.topology;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.MethodProperty;

public class SimpleGraphContainer implements GraphContainer {


	private VertexContainer m_vertexContainer;
	private BeanContainer<String, SimpleEdge> m_edgeContainer;
	private int m_counter = 0;
	private int m_edgeCounter = 0;
	private int m_groupCounter = 0;
	private LayoutAlgorithm m_layoutAlgorithm;
	private double m_scale = 1;
	private int m_semanticZoomLevel;
	private MethodProperty<Integer> m_zoomLevelProperty;
	private MethodProperty<Double> m_scaleProperty;
	
	public SimpleGraphContainer() {
		m_vertexContainer = new VertexContainer();
		m_edgeContainer = new BeanContainer<String, SimpleEdge>(SimpleEdge.class);
		m_edgeContainer.setBeanIdProperty("id");
		m_zoomLevelProperty = new MethodProperty<Integer>(Integer.class, this, "getSemanticZoomLevel", "setSemanticZoomLevel");
		m_scaleProperty = new MethodProperty<Double>(Double.class, this, "getScale", "setScale");
	}

	public VertexContainer getVertexContainer() {
		return m_vertexContainer;
	}

	@SuppressWarnings("unchecked")
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
	
	public Item addVertex(String id, int x, int y, String icon) {
		if (m_vertexContainer.containsId(id)) {
			throw new IllegalArgumentException("A vertex or group with id " + id + " already exists!");
		}
		System.err.println("Adding a vertex: " + id);
		SimpleVertex vertex = new SimpleLeafVertex(id, x, y);
		vertex.setIcon(icon);
		return m_vertexContainer.addBean(vertex);
	}
	
	public Item addGroup(String groupId, String icon) {
		if (m_vertexContainer.containsId(groupId)) {
			throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
		}
		System.err.println("Adding a group: " + groupId);
		SimpleVertex vertex = new SimpleGroup(groupId);
		vertex.setIcon(icon);
		return m_vertexContainer.addBean(vertex);
		
 	}
	public void connectVertices(String id, String sourceVertextId, String targetVertextId) {
		SimpleVertex source = getRequiredVertex(sourceVertextId);
		SimpleVertex target = getRequiredVertex(targetVertextId);
		
		SimpleEdge edge = new SimpleEdge(id, source, target);
		
		m_edgeContainer.addBean(edge);
		
	}
	
	public void removeVertex(String vertexId) {
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
	

	@XmlRootElement(name="graph")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SimpleGraph {
		
		@XmlElements({
				@XmlElement(name="vertex", type=SimpleLeafVertex.class),
				@XmlElement(name="group", type=SimpleGroup.class)
		})
		List<SimpleVertex> m_vertices = new ArrayList<SimpleVertex>();
		
		@XmlElement(name="edge")
		List<SimpleEdge> m_edges = new ArrayList<SimpleEdge>();
		
		public SimpleGraph() {}

		public SimpleGraph(List<SimpleVertex> vertices, List<SimpleEdge> edges) {
			m_vertices = vertices;
			m_edges = edges;
		}

	}
	
	public void save(String filename) {
		List<SimpleVertex> vertices = getBeans(m_vertexContainer);
		List<SimpleEdge> edges = getBeans(m_edgeContainer);

		SimpleGraph graph = new SimpleGraph(vertices, edges);
		
		JAXB.marshal(graph, new File(filename));
	}
	
	public void load(String filename) {
		SimpleGraph graph = JAXB.unmarshal(new File(filename), SimpleGraph.class);
		
		m_vertexContainer.removeAllItems();
		m_vertexContainer.addAll(graph.m_vertices);
		
		m_edgeContainer.removeAllItems();
		m_edgeContainer.addAll(graph.m_edges);
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

	public void setScale(Double scale) {
		m_scale = scale;
	}
	
	public Double getScale() {
	    return m_scale;
	}
	
	public void setSemanticZoomLevel(Integer level) {
		m_semanticZoomLevel = level;
	}
	
	public Integer getSemanticZoomLevel() {
		return m_semanticZoomLevel;
	}

	public void redoLayout() {
		m_layoutAlgorithm.updateLayout(this);
		m_vertexContainer.fireLayoutChange();
	}

	public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
		m_layoutAlgorithm = layoutAlgorithm;
		redoLayout();
	}
	
	public LayoutAlgorithm getLayoutAlgorithm() {
		return m_layoutAlgorithm;
	}
	
	public Collection<?> getSelectedVertexIds() {
		List<Object> selectedVertexIds = new LinkedList<Object>();
		
	    for(Object vertexId : getVertexIds()) {
	    	SimpleVertex vertex = m_vertexContainer.getItem(vertexId).getBean();
	    	if (vertex.isSelected()) {
	    		selectedVertexIds.add(vertexId);
	    	}
	    }
	    
	    return selectedVertexIds;
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

}
