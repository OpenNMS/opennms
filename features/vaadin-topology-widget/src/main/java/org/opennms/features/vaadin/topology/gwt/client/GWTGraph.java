package org.opennms.features.vaadin.topology.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public final class GWTGraph extends JavaScriptObject {
    
    protected GWTGraph() {}
    
    public final native JsArray<GWTVertex> getVertices()/*-{
        return this.vertices;
    }-*/;
    
    public final native JsArray<GWTEdge> getEdges()/*-{
        return this.edges;
    }-*/;
    
    private final native void put(String id, GWTVertex vertex) /*-{
        this.idToVMap[id] = vertex;
    }-*/;
    
    private final native GWTVertex get(String id) /*-{
        return this.idToVMap[id];
    }-*/;
    
    public void addVertex(GWTVertex vertex) {
        getVertices().push(vertex);
        put(vertex.getId(), vertex);
    }
    
    public void removeVertex(GWTVertex vertex) {
        int index = findIndex(vertex);
        if(index >= 0) {
            splice(getVertices(), index);
        }
        
        Integer[] edgeIndices = findEdgeIndices(vertex);
        for(Integer edgeIndex : edgeIndices) {
            splice(getEdges(), edgeIndex);
        }
    }
    
    public GWTVertex findVertexById(String id) {
        for(int i = 0; i < getVertices().length(); i++) {
            GWTVertex v = getVertices().get(i);
            if(v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    private Integer[] findEdgeIndices(GWTVertex vertex) {
        List<Integer> indexArray = new ArrayList<Integer>();
        
        for(int i = 0; i < getEdges().length(); i++) {
            GWTEdge edge = getEdges().get(i);
            if(edge.getSource().getId().equals(vertex.getId()) || edge.getTarget().getId().equals(vertex.getId())) {
                indexArray.add(i);
            }
        }
        return indexArray.toArray(new Integer[0]);
    }
    

    private final native void splice(JsArray<?> vertices, int index) /*-{
        vertices.splice(index, 1);
    }-*/;

    private int findIndex(GWTVertex vertex) {
        for(int i = 0; i < getVertices().length(); i++) {
            GWTVertex v = getVertices().get(i);
            if(v.getId().equals(vertex.getId())) {
                return i;
            }
        }
        return -1;
    }
    
    public void addEdge(GWTEdge edge) {
        getEdges().push(edge);
    }
    
    public static final native GWTGraph create() /*-{
        return {"vertices":[], "edges":[], "idToVMap":{}};
    }-*/;

    public String json() {
        return "{}";
    }
}
