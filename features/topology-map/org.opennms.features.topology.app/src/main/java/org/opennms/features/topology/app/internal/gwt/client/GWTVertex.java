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
import org.opennms.features.topology.app.internal.gwt.client.tracker.LoadTracker;
import org.opennms.features.topology.app.internal.gwt.client.tracker.LoadTracker.LoadTrackerHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Image;

public class GWTVertex extends JavaScriptObject {
    
    /**
     * CSS Class name for a vertex
     */
    public static final String VERTEX_CLASS_NAME = ".vertex";
    public static final String SELECTED_VERTEX_CLASS_NAME = ".vertex.selected";
    private static String s_bgImagePath;
    
    protected GWTVertex() {};
    
    public static native GWTVertex create(String id, int x, int y) /*-{
    	return {"id":id, "x":x, "y":y, "initialX":0, "initialY":0, "selected":false, "iconUrl":"", "semanticZoomLevel":0, "group":null};
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
    
    protected static Func<String, GWTVertex> getClassName() {
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex datum, int index) {
                return datum.isSelected() ? "vertex selected" : "vertex";
            }};
    }
    
    protected static Func<String, GWTVertex> strokeFilter(){
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex datum, int index) {
                return datum.isSelected() ? "blue" : "none";
            }
            
        };
    }

    static Func<String, GWTVertex> getTranslation() {
    	return new Func<String, GWTVertex>() {
    
    		public String call(GWTVertex vertex, int index) {
    			return "translate( " + vertex.getX() + "," + vertex.getY() + ")";
    		}
    		
    	};
    }
    
    static Func<String, GWTVertex> loadIconAndSize(final D3 bgImage, final D3 imageSelection, final D3 circleSelection, final D3 textSelection){
        return new Func<String, GWTVertex>(){

            public String call(GWTVertex datum, final int index) {
                LoadTracker tracker = LoadTracker.get();
                tracker.trackImageLoad(datum.getIconUrl(), new LoadTrackerHandler() {

                    @Override
                    public void onImageLoad(Image img) {
                        double widthRatio = 48.0/img.getWidth();
                        double heightRatio = 48.0/img.getHeight();
                        double scaleFactor = Math.min(widthRatio, heightRatio);
                        int width = (int) (img.getWidth() * scaleFactor);
                        int height = (int) (img.getHeight() * scaleFactor);
                        
                        String strWidth = width + "px";
                        String strHeight = height + "px";
                        String x = "-" + width/2 + "px";
                        String y = "-" + height/2 + "px";
                        
                        Element imgElem = D3.getElement(imageSelection, index);
                        imgElem.setAttribute("width", strWidth);
                        imgElem.setAttribute("height", strHeight);
                        imgElem.setAttribute("x", x);
                        imgElem.setAttribute("y", y);
                        
                        Element bgImgElem = D3.getElement(bgImage, index);
                        int length = (Math.max(width, height) + 10);
                        bgImgElem.setAttribute("width", length +"px");
                        bgImgElem.setAttribute("height", length + "px");
                        bgImgElem.setAttribute("x", "-" + Math.round(length/2));
                        bgImgElem.setAttribute("y", "-" + Math.round(length/2));
                        
                        Element rectElem = D3.getElement(circleSelection, index);
                        rectElem.setAttribute("class", "highlight");
                        rectElem.setAttribute("fill", "yellow");
                        rectElem.setAttribute("x", -(width/2 + 2) + "px");
                        rectElem.setAttribute("y", -(height/2 + 2) + "px");
                        rectElem.setAttribute("r", ((Math.max(width, height) + 9)/2) + "px" );
                        rectElem.setAttribute("opacity", "0");
                        
                        textSelection.text(label());
                        Element textElem = D3.getElement(textSelection, index);
                        textElem.setAttribute("class", "vertex-label");
                        textElem.setAttribute("x", "0px");
                        textElem.setAttribute("y",  "" + (height/2 + 5) + "px");
                        textElem.setAttribute("text-anchor", "middle");
                        textElem.setAttribute("alignment-baseline", "text-before-edge");
                    }
                    
                });
                
                
                return datum.getIconUrl();
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
                return selection.attr("class", GWTVertex.getClassName()).attr("transform", GWTVertex.getTranslation()).select(".highlight").attr("opacity", GWTVertex.selectionFilter());
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
                D3 vertex = selection.append("g").attr("class", "vertex");
                vertex.attr("opacity",1e-6);
                vertex.style("cursor", "pointer");
                
                //ImageElement img = DOM.createImg().cast();
                
                D3 circleSelection = vertex.append("circle");
                D3 bgImage = vertex.append("svg:image");
                bgImage.attr("xlink:href", getBackgroundImage());
                D3 imageSelection = vertex.append("svg:image");
                D3 textSelection = vertex.append("text");
                
                imageSelection.attr("xlink:href", loadIconAndSize(bgImage, imageSelection, circleSelection, textSelection));
                
//                imageSelection.attr("xlink:href", iconUrl())
//                	.attr("x", "-24px")
//                	.attr("y", "-24px")
//                	.attr("width", "48px")
//                	.attr("height", "48px");

                vertex.call(draw());
                
                return vertex;
            }
        };
    }

    public static final native void logDocument(Object doc)/*-{
        $wnd.console.log(doc)
    }-*/;
    
}
