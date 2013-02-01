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

package org.opennms.features.topology.app.internal.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public final class GWTGraph extends JavaScriptObject {
    
    protected GWTGraph() {}
    
    public static final native GWTGraph create() /*-{
    	return { "vertices":[], "edges":[], "idToVMap":{}, 
    	         "clientX":0, "clientY":0, 
    	         "viewTransformation":{}, "panToSelection":false,
    	         "fitToView":false, "bounds":{"x":0, "y":0, "width":100, "height":100}};
	}-*/;

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
    
	public GWTVertex getVertex(String vertexKey) {
		return get(vertexKey);
	}

    public native void setClientX(int clientX) /*-{
        this.clientX = clientX;
    }-*/;

    public native void setClientY(int clientY) /*-{
        this.clientY = clientY;
    }-*/;

    public native void setViewportTransform(SVGMatrix viewportTransform) /*-{
        this.viewTransform = viewportTransform;
    }-*/;

    public native void setOldScale(double oldScale) /*-{
        this.oldScale = oldScale;
    }-*/;

    public native void setBoundingBox(GWTBoundingBox bounds) /*-{
        this.bounds = bounds;
    }-*/;
    
    public native GWTBoundingBox getBoundingBox() /*-{
        return this.bounds;
    }-*/;

}
