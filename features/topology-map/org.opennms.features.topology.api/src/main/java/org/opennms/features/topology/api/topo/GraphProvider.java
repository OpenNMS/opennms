package org.opennms.features.topology.api.topo;

public interface GraphProvider extends VertexProvider, EdgeProvider {

	void removeVertex(Vertex... vertexId);

	void save(String filename);

	void load(String filename);

	void resetContainer();

	Vertex addVertex(int x, int y);

	boolean setParent(VertexRef vertexId, VertexRef parentId);
	
	Vertex addGroup(String label, String iconKey);

	Edge connectVertices(Vertex sourceVertextId, Vertex targetVertextId);

}
