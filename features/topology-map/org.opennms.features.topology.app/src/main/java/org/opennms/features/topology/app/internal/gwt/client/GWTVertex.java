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

import com.google.gwt.dom.client.NativeEvent;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events;
import org.opennms.features.topology.app.internal.gwt.client.d3.Func;

import com.google.gwt.core.client.JavaScriptObject;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGRect;

public class GWTVertex extends JavaScriptObject {
    
    /**
     * CSS Class name for a vertex
     */
    public static final String VERTEX_CLASS_NAME = ".vertex";
    public static final String SELECTED_VERTEX_CLASS_NAME = ".vertex.selected";
    private static final int VERTEX_STATUS_CHAR_SIZE = 7;

    protected GWTVertex() {};

    public static native GWTVertex create(String id, int x, int y) /*-{
    	return {"id":id, "x":x, "y":y, "initialX":0, "initialY":0, "selected":false,
    	        "iconUrl":"", "svgIconId":"", "semanticZoomLevel":0, "group":null,
    	        "status":"", "statusCount":"", "iconHeight":48, "iconWidth":48};
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

    public final native void setStatusCount(String count) /*-{
        this.statusCount = count;
    }-*/;

    public final native String getStatusCount() /*-{
        return this.statusCount;
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

    public final native void setSVGIconId(String svgIconId) /*-{
        this.svgIconId = svgIconId;
    }-*/;

    public final native String getSVGIconId() /*-{
        return this.svgIconId;
    }-*/;

    public final native double getIconHeight()/*-{
        if($wnd.isNaN(this.iconHeight)){
            return 0;
        } else {
            return this.iconHeight;
        }
    }-*/;

    public final native void setIconHeight(double iconHeight)/*-{
        this.iconHeight = iconHeight;
    }-*/;

    public final native double getIconWidth() /*-{
        if($wnd.isNaN(this.iconWidth)){
            return 0;
        } else {
            return this.iconWidth;
        }
    }-*/;

    public final native void setIconWidth(double iconWidth)/*-{
        this.iconWidth = iconWidth
    }-*/;

    static Func<String, GWTVertex> selectedFill() {
    	return new Func<String, GWTVertex>(){

                    @Override
    		public String call(GWTVertex vertex, int index) {
    			return vertex.isSelected() ? "blue" : "black";
    		}
    	};
    }

    protected static Func<String, GWTVertex> selectionFilter() {
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex vertex, int index) {
                return vertex.isSelected() ? "1" : "0";
            }

        };
    }

    protected static Func<String, GWTVertex> getStatusClass(final boolean isCounterIndicator){
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex vertex, int index) {
                if(!isCounterIndicator){
                    if(vertex.isSelected()){
                        return "status selected";
                    }
                }
                return "status " + vertex.getStatus();
            }

        };
    }

    protected static Func<String, GWTVertex> getStatusCountText(){
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex vertex, int index) {
                return vertex.getStatusCount();
            }

        };
    }

    protected static Func<String, GWTVertex> showStatusCount(){
        return new Func<String, GWTVertex>(){

            @Override
            public String call(GWTVertex vertex, int index) {
                return !vertex.getStatusCount().equals("") && !vertex.getStatusCount().equals("0") ? "1" : "0";
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

                    @Override
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

    static Func<String, GWTVertex> svgIconId(final String suffix) {
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex datum, int index) {
                return "#" + datum.getSVGIconId() + suffix;
            }
        };
    }

    static Func<String, GWTVertex> textLabelPlacement() {
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                return (vertex.getIconHeight()/2 + 2) + "px";
            }
        };
    }

    static Func<String, GWTVertex> statusCounterPos(){
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                double iconHeight = vertex.getIconHeight();
                double iconWidth = vertex.getIconWidth();

                double xPos = 0;
                double yPos = 0;
                int statusCountLength = vertex.getStatusCount().length();
                xPos = (iconWidth/2) - (13 + (VERTEX_STATUS_CHAR_SIZE * statusCountLength));
                yPos = (iconHeight/2) + 25;
                return "translate(" + xPos + ", -" + yPos +  ")";
            }
        };
    }

    static Func<String, GWTVertex> normalizeSVGIcon() {
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                SVGRect iconRect = getHiddenIconElement(vertex.getSVGIconId());
                double primeLength = iconRect.getWidth() >= iconRect.getHeight() ? iconRect.getWidth() : iconRect.getHeight();
                double scaleFactor = 48 / primeLength;
                double newX = (scaleFactor * iconRect.getWidth()) / 2;
                double newY = (scaleFactor * iconRect.getHeight()) / 2;
                double iconHeight = scaleFactor * iconRect.getHeight();
                vertex.setIconHeight(iconHeight);
                vertex.setIconWidth(scaleFactor * iconRect.getWidth());
                return "translate(-" + newX +" , -" + newY +") scale(" + scaleFactor + ")";
            }
        };
    }

    static native SVGRect getHiddenIconElement(String iconId) /*-{
        var existingUseElem = $wnd.d3.select("#hiddenIconContainer #" + iconId + "-icon");
        if(existingUseElem[0][0] == null){
            var iconGroup = $wnd.d3.select("#hiddenIconContainer");
            var useTag = iconGroup.append("svg:use").attr("id", iconId + "-icon").attr("xlink:href", "#" + iconId)[0][0];
            return useTag.getBBox();
        } else {
            return existingUseElem[0][0].getBBox();
        }


    }-*/;


    static Func<String, GWTVertex> calculateOverlayWidth(){
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                return vertex.getIconWidth() + "px";
            }
        };
    }

    static Func<String, GWTVertex> calculateOverlayHeight(){
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                return vertex.getIconHeight() + "px";
            }
        };
    }

    static Func<String, GWTVertex> calculateOverlayXPos(){
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                return "-" + (vertex.getIconWidth() / 2) + "px";
            }
        };
    }

    static Func<String, GWTVertex> calculateOverlayYPos(){
        return new Func<String, GWTVertex>() {
            @Override
            public String call(GWTVertex vertex, int index) {
                return "-" + (vertex.getIconHeight() / 2) + "px";
            }
        };
    }

    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                final D3 iconContainer = selection.select(".icon-container");
                iconContainer.attr("transform", normalizeSVGIcon());
                iconContainer.select(".vertex .activeIcon").attr("opacity", selectionFilter());

                selection.select(".svgIconOverlay").attr("width", calculateOverlayWidth()).attr("height", calculateOverlayHeight())
                        .attr("x", calculateOverlayXPos()).attr("y", calculateOverlayYPos());

                selection.select(".status").attr("width", calculateOverlayWidth()).attr("height", calculateOverlayHeight())
                        .attr("x", calculateOverlayXPos()).attr("y", calculateOverlayYPos()).attr("class", getStatusClass(false));

                selection.select(".node-status-counter").attr("transform", statusCounterPos()).style("opacity", showStatusCount())
                    .select("rect").attr("class", getStatusClass(true));
                selection.select(".status-counter").text(getStatusCountText());

                return selection.attr("class", GWTVertex.getClassName()).attr("transform", GWTVertex.getTranslation()).select("text.vertex-label").text(label()).attr("y", textLabelPlacement());
            }
        };
    }

    public static D3Behavior create() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {

                D3 vertex = selection.append("g").attr("class", "vertex");
                vertex.attr("opacity",1e-6).style("cursor", "pointer");
                vertex.append("svg:rect").attr("class", "status").attr("fill", "none").attr("stroke-width", 5).attr("stroke-location", "outside").attr("stroke", "blue").attr("opacity", 0);

                D3 svgIconContainer         = vertex.append("g").attr("class", "icon-container");
                D3 svgIcon                  = svgIconContainer.append("use");
                D3 svgIconRollover          = svgIconContainer.append("use");
                D3 svgIconActive            = svgIconContainer.append("use");
                D3 statusCounter            = vertex.append("g");
                D3 textSelection            = vertex.append("text");

                vertex.append("svg:rect").attr("class", "svgIconOverlay").attr("width", 100).attr("height", 100).attr("opacity", 0).call(new D3Behavior() {
                    @Override
                    public D3 run(D3 selection) {
                        return selection.on("mouseover", new D3Events.Handler<Object>() {
                            @Override
                            public void call(Object o, int index) {
                                NativeEvent event = D3.getEvent();
                                SVGRect element = event.getCurrentEventTarget().cast();
                                SVGGElement parent = element.getParentElement().cast();

                                D3 selection = D3.d3().select(parent);
                                selection.select(".overIcon").attr("opacity", 1);
                            }
                        }).on("mouseout", new D3Events.Handler<Object>() {
                            @Override
                            public void call(Object o, int index) {
                                NativeEvent event = D3.getEvent();
                                SVGRect element = event.getCurrentEventTarget().cast();
                                SVGGElement parent = element.getParentElement().cast();

                                D3 selection = D3.d3().select(parent);
                                selection.select(".overIcon").attr("opacity", 0);
                            }
                        });
                    }
                });

                svgIcon.attr("xlink:href", svgIconId("")).attr("class", "upIcon");
                svgIconRollover.attr("xlink:href", svgIconId("_rollover")).attr("class", "overIcon").attr("opacity", 0);
                svgIconActive.attr("xlink:href", svgIconId("_active")).attr("class", "activeIcon").attr("opacity", 0);

                statusCounter.attr("class", "node-status-counter")
                        .append("svg:rect").attr("height", 20).attr("width", 20).attr("rx", 10).attr("ry", 10);

                statusCounter.append("text").attr("x", "7px").attr("y","3px").attr("alignment-baseline", "text-before-edge")
                        .attr("class", "status-counter").text("2");


                textSelection.text(label())
                    .attr("class", "vertex-label")
                    .attr("x", "0px")
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
