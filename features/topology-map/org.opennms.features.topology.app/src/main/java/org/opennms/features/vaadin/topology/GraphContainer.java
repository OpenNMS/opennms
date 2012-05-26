package org.opennms.features.vaadin.topology;

import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.terminal.Resource;

public interface GraphContainer {
	
	public VertexContainer getVertexContainer();
	
	public BeanContainer<?, ?> getEdgeContainer();
	
	public Collection<?> getVertexIds();
	
	public Collection<?> getEdgeIds();
	
	public Item getVertexItem(Object vertexId);
	
	public Item getEdgeItem(Object edgeId);
	
	public Collection<?> getEndPointIdsForEdge(Object edgeId);
	
	public Collection<?> getEdgeIdsForVertex(Object vertexId);

	public Integer getSemanticZoomLevel();
	
	public Property getProperty(String propertyId);
 	
}
