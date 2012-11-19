package org.opennms.features.topology.api;

import java.util.Collection;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphVisitor;
import org.opennms.features.topology.api.topo.Vertex;

public interface Graph {

	Layout getLayout();
	
	Collection<? extends Vertex> getDisplayVertices();
	
	Collection<? extends Edge> getDisplayEdges();

	Edge getEdgeByKey(String edgeKey);
	
	Vertex getVertexByKey(String vertexKey);

	void visit(GraphVisitor visitor) throws Exception;
	
	
}
