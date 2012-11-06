package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.List;

public interface VertexProvider {
	
	public String getNamespace();
	
	public Vertex getVertex(String id);
	
	public Vertex getVertex(VertexRef reference);
	
	public List<Vertex> getVertices();
	
	public List<Vertex> getVertices(Collection<? extends VertexRef> references);
	
	public List<Vertex> getRootGroup();
	
	public boolean hasChildren(VertexRef group);
	
	public Vertex getParent(VertexRef vertex);
	
	public List<Vertex> getChildren(VertexRef group);
	
	public void addVertexListener(VertexListener vertexListener);
	
	public void removeVertexListener(VertexListener vertexListener);

}
