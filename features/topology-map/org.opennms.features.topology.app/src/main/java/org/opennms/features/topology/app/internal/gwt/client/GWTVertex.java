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

import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior;
import org.opennms.features.topology.app.internal.gwt.client.d3.Func;

import com.google.gwt.core.client.JavaScriptObject;

public class GWTVertex extends JavaScriptObject {
    
    /**
     * CSS Class name for a vertex
     */
    public static final String VERTEX_CLASS_NAME = ".vertex";
    public static final String SELECTED_VERTEX_CLASS_NAME = ".vertex.selected";
    private static String s_bgImagePath;
    
    protected GWTVertex() {};
    
    public static native GWTVertex create(String id, int x, int y) /*-{
    	return {"id":id, "x":x, "y":y, "initialX":0, "initialY":0, "selected":false, "iconUrl":"", "semanticZoomLevel":0, "group":null, "status":""};
	}-*/;

    public final native String getId()/*-{
        return this.id;
    }-*/;
    
    public final native void setSelected(boolean selected) /*-{
        this.selected = selected;
    }-*/;
    
    public final native boolean isSelected() /*-{
        return this.selected;
    }-*/;
    
    public final native void setLabel(String label) /*-{
    	this.label = label;
    }-*/;
    
    public final native String getLabel() /*-{
    	return this.label;
    }-*/;
    
    public final native void setStatus(String status) /*-{
        this.status = status;
    }-*/;
    
    public final native String getStatus()/*-{
        return this.status;
    }-*/;
    
    public final native void setIpAddr(String ipAddr) /*-{
        this.ipAddr = ipAddr;
    }-*/;
    
    public final native void getIpAddr() /*-{
        return this.ipAddr;
    }-*/;
    
    public final native void setNodeID(int nodeID) /*-{
    	this.nodeID = nodeID;
	}-*/;

    public final native void getNodeID() /*-{
    	return this.nodeID;
	}-*/;
    
    public final native int getX()/*-{
    	return this.x;
	}-*/;

    public final native int getY()/*-{
    	return this.y;
	}-*/;

    public final native void setX(int newX) /*-{
        this.x = newX;
    }-*/;

    public final native void setY(int newY) /*-{
        this.y = newY;
    }-*/;
    
    public final native int getInitialX()/*-{
    	return this.initialX;
	}-*/;

    public final native int getInitialY()/*-{
    	return this.initialY;
	}-*/;

    public final native void setInitialX(int initialX) /*-{
    	this.initialX = initialX;
	}-*/;

    public final native void setInitialY(int initialY) /*-{
    	this.initialY = initialY;
	}-*/;

    public final String getTooltipText() {
        return getLabel();
    }
    
    
    public final native String getIconUrl() /*-{
        return this.iconUrl;
    }-*/;
    
    public final native void setIconUrl(String iconUrl) /*-{
        this.iconUrl = iconUrl;
    }-*/;

    static Func<String, GWTVertex> selectedFill() {
    	return new Func<String, GWTVertex>(){
    
    		public String call(GWTVertex vertex, int index) {
    			return vertex.isSelected() ? "blue" : "black";
    		}
    	};
    }
    
    protected static Func<String, GWTVertex> selectionFilter() {
        return new Func<String, GWTVertex>(){

            public String call(GWTVertex vertex, int index) {
                return vertex.isSelected() ? "1" : "0";
            }
            
        };
    }
    
    protected static Func<String, GWTVertex> getCircleId(){
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex vertex, int index) {
                return "circle-" + vertex.getId();
            }
            
        };
    }
    
    protected static Func<String, GWTVertex> getStatusClass(){
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex vertex, int index) {
                if(vertex.getStatus().equals("")) {
                    return "status";
                }
                return "status " + vertex.getStatus();
            }
            
        };
    }
    
    protected static Func<String, GWTVertex> getClassName() {
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex datum, int index) {
                return datum.isSelected() ? "vertex selected" : "vertex";
            }};
    }
    
    static Func<String, GWTVertex> getTranslation() {
    	return new Func<String, GWTVertex>() {
    
    		public String call(GWTVertex vertex, int index) {
    			return "translate( " + vertex.getX() + "," + vertex.getY() + ")";
    		}
    		
    	};
    }
    
    static Func<String, GWTVertex> label() {
    	return new Func<String, GWTVertex>() {

			@Override
			public String call(GWTVertex datum, int index) {
				return datum.getLabel() == null ? "no label provided" : datum.getLabel();
			}
    		
    	};
    }
    
    static Func<String, GWTVertex> iconUrl() {
    	return new Func<String, GWTVertex>() {

			@Override
			public String call(GWTVertex datum, int index) {
				return datum.getIconUrl();
			}
    		
    	};
    }
    
    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                selection.select(".status").attr("class", getStatusClass());
                return selection.attr("class", GWTVertex.getClassName()).attr("transform", GWTVertex.getTranslation()).select("text").text(label());
            }
        };
    }
    
    public static void setBackgroundImage(String bgImagePath) {
        s_bgImagePath = bgImagePath;
    }
    
    public static String getBackgroundImage() {
        return s_bgImagePath;
    }
    
    public static D3Behavior create() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                int width = 80;
                int height = 80;
                D3 vertex = selection.append("g").attr("class", "vertex");
                vertex.attr("opacity",1e-6);
                vertex.style("cursor", "pointer");
                
                D3 circleSelection = vertex.append("circle");
                D3 statusIndicator = vertex.append("circle");
                D3 bgImage = vertex.append("svg:image");
                bgImage.attr("xlink:href", getBackgroundImage());
                D3 imageSelection = vertex.append("svg:image");
                D3 textSelection = vertex.append("text");
                
                
                bgImage.attr("width", width +"px")
                    .attr("height", height + "px")
                    .attr("x", "-" + Math.round(width/2))
                    .attr("y", "-" + Math.round(height/2));
                
                imageSelection.attr("xlink:href", iconUrl())
                	.attr("x", "-24px")
                	.attr("y", "-24px")
                	.attr("width", "48px")
                	.attr("height", "48px");
                
                int circleRadius = 38;
                circleSelection.attr("class", "highlight")
                    .attr("cx", -0.5)
                    .attr("cy", -0.55)
                    .attr("r", circleRadius + 1.5 + "px" )
                    .attr("stroke-width", "2px")
                    .attr("fill-opacity", 0);
                
                statusIndicator.attr("class", "status")
                    .attr("cx", 0)
                    .attr("cy", 0)
                    .attr("r", circleRadius + "px")
                    .attr("opacity", "0");
                    
                
                textSelection.text(label())
                    .attr("class", "vertex-label")
                    .attr("x", "0px")
                    .attr("y",  "" + (height/2) + "px")
                    .attr("text-anchor", "middle")
                    .attr("alignment-baseline", "text-before-edge");

                vertex.call(draw());
                
                return vertex;
            }
        };
    }

    public static final native void logDocument(Object doc)/*-{
        $wnd.console.log(doc)
    }-*/;

}
