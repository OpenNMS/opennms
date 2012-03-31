package org.opennms.features.vaadin.topology.gwt.client;

import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class GraphJSONConverter {
    
    public static String convertGraphToJSON(Graph graph) {
        JSONArray obj = new JSONArray();
        obj.set(0, new JSONString("graph"));
        
        obj.set(1, getVertices(graph.getVertices()));
        
        obj.set(2, getEdges(graph.getEdges()));
        
        return obj.toString();
    }
    
    private static JSONValue getVertices(List<Vertex> vertices) {
        JSONArray vertexArray = new JSONArray();
        for(int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            vertexArray.set(i, getVertex(vertex));
        }
        return vertexArray;
    }
    
    private static JSONValue getVertex(Vertex vertex) {
    	JSONArray array = new JSONArray();
    	array.set(0, new JSONString("vertex"));
    	
        JSONObject obj = new JSONObject();
        obj.put("id", new JSONNumber(vertex.getId()));
        obj.put("x", new JSONNumber(vertex.getX()));
        obj.put("y", new JSONNumber(vertex.getY()));
        
        array.set(1, obj);
        return array;
    }
    
    private static JSONValue getEdges(List<Edge> edges) {
        JSONArray edgeArray = new JSONArray();
        for(int j = 0; j < edges.size(); j++) {
            Edge edge = edges.get(j);
            edgeArray.set(j, getEdge(edge));
        }
        return edgeArray;
    }
    
    private static JSONValue getEdge(Edge edge) {
    	JSONArray array = new JSONArray();
    	array.set(0, new JSONString("edge"));
    	
        JSONObject obj = new JSONObject();
        obj.put("source", getVertex(edge.getSource()));
        obj.put("target", getVertex(edge.getTarget()));
        
        array.set(1, obj);
        return array;
    }
    
}
