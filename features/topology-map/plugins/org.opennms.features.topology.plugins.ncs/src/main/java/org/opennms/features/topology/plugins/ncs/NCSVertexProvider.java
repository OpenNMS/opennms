package org.opennms.features.topology.plugins.ncs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;

public class NCSVertexProvider implements VertexProvider {

    Map<String, Vertex> m_vertices = new HashMap<String, Vertex>(); 
    @Override
    public String getVertexNamespace() {
        return "ncs";
    }

    @Override
    public boolean contributesTo(String namespace) {
        
        return namespace.equals("ncs");
    }

    @Override
    public boolean containsVertexId(String id) {
        return m_vertices.containsKey(id);
    }

    @Override
    public boolean containsVertexId(VertexRef id) {
        throw new UnsupportedOperationException("This is unsupported in NCSVertexProvider");
    }

    @Override
    public Vertex getVertex(String namespace, String id) {
        return null;
    }

    @Override
    public Vertex getVertex(VertexRef reference) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSemanticZoomLevel(VertexRef vertex) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Vertex> getVertices(Criteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Vertex> getVertices() {
        // TODO Auto-generated method stub
        return (List<Vertex>) m_vertices.values();
    }

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Vertex> getRootGroup() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasChildren(VertexRef group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Vertex getParent(VertexRef vertex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setParent(VertexRef child, VertexRef parent) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Vertex> getChildren(VertexRef group) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addVertexListener(VertexListener vertexListener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeVertexListener(VertexListener vertexListener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearVertices() {
        // TODO Auto-generated method stub
        
    }

}
