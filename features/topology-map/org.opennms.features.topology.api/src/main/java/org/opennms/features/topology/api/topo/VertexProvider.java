package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.List;

public interface VertexProvider {
	
	/**
	 * A string used to identify references belonging to this provider
	 * 
	 * May only container characters that make for a reasonable java identifier
	 * such as letters digits and underscore (no colons, periods, commans etc.)
	 * 
	 */
	public String getNamespace();
	
	public Vertex getVertex(String id);
	
	public Vertex getVertex(VertexRef reference);
	
	public int getSemanticZoomLevel(VertexRef vertex);
	
	/**
	 * Return an immutable list of vertices that match the criteria.
	 */
	public List<? extends Vertex> getVertices(Criteria criteria);

	public List<? extends Vertex> getVertices();
	
	public List<? extends Vertex> getVertices(Collection<? extends VertexRef> references);
	
	public List<? extends Vertex> getRootGroup();
	
	public boolean hasChildren(VertexRef group);
	
	public Vertex getParent(VertexRef vertex);
	
    public List<? extends Vertex> getChildren(VertexRef group);
	
	public void addVertexListener(VertexListener vertexListener);
	
	public void removeVertexListener(VertexListener vertexListener);

}
