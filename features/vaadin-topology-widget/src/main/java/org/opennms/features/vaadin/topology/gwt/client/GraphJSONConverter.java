/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.topology.gwt.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class GraphJSONConverter {
    
    public static String convertGraphToJSON(GWTGraph graph) {
        JSONArray obj = new JSONArray();
        obj.set(0, new JSONString("graph"));
        
        obj.set(1, getVertices(graph.getVertices()));
        
        obj.set(2, getEdges(graph.getEdges()));
        
        return obj.toString();
    }
    
    private static JSONValue getVertices(JsArray<GWTVertex> vertices) {
        JSONArray vertexArray = new JSONArray();
        for(int i = 0; i < vertices.length(); i++) {
            GWTVertex vertex = vertices.get(i);
            vertexArray.set(i, getVertex(vertex));
        }
        return vertexArray;
    }
    
    private static JSONValue getVertex(GWTVertex vertex) {
    	JSONArray array = new JSONArray();
    	array.set(0, new JSONString("vertex"));
    	
        JSONObject obj = new JSONObject();
        obj.put("id", new JSONString(vertex.getId()));
        obj.put("x", new JSONNumber(vertex.getX()));
        obj.put("y", new JSONNumber(vertex.getY()));
        
        array.set(1, obj);
        return array;
    }
    
    private static JSONValue getEdges(JsArray<GWTEdge> edges) {
        JSONArray edgeArray = new JSONArray();
        for(int j = 0; j < edges.length(); j++) {
            GWTEdge edge = edges.get(j);
            edgeArray.set(j, getEdge(edge));
        }
        return edgeArray;
    }
    
    private static JSONValue getEdge(GWTEdge edge) {
    	JSONArray array = new JSONArray();
    	array.set(0, new JSONString("edge"));
    	
        JSONObject obj = new JSONObject();
        obj.put("source", getVertex(edge.getSource()));
        obj.put("target", getVertex(edge.getTarget()));
        
        array.set(1, obj);
        return array;
    }
    
}
