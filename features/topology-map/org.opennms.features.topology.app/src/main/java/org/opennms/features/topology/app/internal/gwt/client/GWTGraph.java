/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        List<Integer> indexArray = new ArrayList<>();
        
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
