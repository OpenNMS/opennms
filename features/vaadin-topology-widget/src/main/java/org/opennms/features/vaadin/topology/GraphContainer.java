package org.opennms.features.vaadin.topology;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

public interface GraphContainer {
	
	public <T extends Container,ItemSetChangedListener> T getVertexContainer();
	
	public <T extends Container,ItemSetChangedListener> T getEdgeContainer();
	
	public Collection<?> getVertexIds();
	
	public Collection<?> getEdgeIds();
	
	public Item getVertexItem(Object vertexId);
	
	public Item getEdgeItem(Object edgeId);
	
	public Collection<?> getEdgeEndPoints(Object edgeId);
	
	public Collection<?> getEdgesForVertex(Object vertexId);
	
}
