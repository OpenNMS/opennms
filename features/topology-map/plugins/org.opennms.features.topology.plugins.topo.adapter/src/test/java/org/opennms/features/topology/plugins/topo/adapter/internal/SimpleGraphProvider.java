package org.opennms.features.topology.plugins.topo.adapter.internal;

import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;

public class SimpleGraphProvider implements GraphProvider {
	
	SimpleVertexProvider m_vertices;
	SimpleEdgeProvider m_edges;
	
	public SimpleGraphProvider(String namespace) {
		m_vertices = new SimpleVertexProvider(namespace);
		m_edges = new SimpleEdgeProvider(namespace);
	}

	@Override
	public Vertex getVertex(String id) {
		return m_vertices.getVertex(id);
	}

	@Override
	public Vertex getVertex(VertexRef reference) {
		return m_vertices.getVertex(reference);
	}

	@Override
	public List<? extends Vertex> getVertices() {
		return m_vertices.getVertices();
	}

	@Override
	public List<? extends Vertex> getVertices(
			Collection<? extends VertexRef> references) {
		return m_vertices.getVertices(references);
	}

	@Override
	public boolean hasChildren(VertexRef group) {
		return m_vertices.hasChildren(group);
	}

	@Override
	public void addVertexListener(VertexListener vertexListener) {
		m_vertices.addVertexListener(vertexListener);
	}

	@Override
	public void removeVertexListener(VertexListener vertexListener) {
		m_vertices.removeVertexListener(vertexListener);
	}

	public void setVertices(List<SimpleVertex> vertices) {
		m_vertices.setVertices(vertices);
	}

	public void add(SimpleVertex... vertices) {
		m_vertices.add(vertices);
	}

	public void add(List<SimpleVertex> vertices) {
		m_vertices.add(vertices);
	}

	@Override
	public boolean equals(Object obj) {
		return m_vertices.equals(obj);
	}

	@Override
	public String getNamespace() {
		return m_vertices.getNamespace();
	}

	@Override
	public List<? extends Vertex> getRootGroup() {
		return m_vertices.getRootGroup();
	}

	@Override
	public Vertex getParent(VertexRef vertex) {
		return m_vertices.getParent(vertex);
	}

	@Override
	public List<? extends Vertex> getChildren(VertexRef group) {
		return m_vertices.getChildren(group);
	}

	public void remove(List<SimpleVertex> vertices) {
		m_vertices.remove(vertices);
	}

	public void remove(SimpleVertex... vertices) {
		m_vertices.remove(vertices);
	}

	@Override
	public Edge getEdge(String id) {
		return m_edges.getEdge(id);
	}

	@Override
	public Edge getEdge(EdgeRef reference) {
		return m_edges.getEdge(reference);
	}

	@Override
	public List<? extends Edge> getEdges() {
		return m_edges.getEdges();
	}

	@Override
	public List<? extends Edge> getEdges(
			Collection<? extends EdgeRef> references) {
		return m_edges.getEdges(references);
	}

	@Override
	public void addEdgeListener(EdgeListener edgeListener) {
		m_edges.addEdgeListener(edgeListener);
	}

	@Override
	public void removeEdgeListener(EdgeListener edgeListener) {
		m_edges.removeEdgeListener(edgeListener);
	}

	public void setEdges(List<SimpleEdge> edges) {
		m_edges.setEdges(edges);
	}

	public void add(SimpleEdge... edges) {
		m_edges.add(edges);
	}

	public void remove(SimpleEdge... edges) {
		m_edges.remove(edges);
	}


}
