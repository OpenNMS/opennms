/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

public final class GWTEdge extends JavaScriptObject {
    
    public static final String SVG_EDGE_ELEMENT = "path";

    protected GWTEdge() {};
    
    public static final native GWTEdge create(String id, GWTVertex source, GWTVertex target) /*-{
        return {"id":id, "source":source, "target":target, "cssClass": "path", "linkNum":0, "tooltipText": "", "status":"", "linkCount":1};
	}-*/;

    public static final native void consoleLog(Object obj)/*-{
        $wnd.console.log(obj);
    }-*/;

    public final native GWTVertex getSource() /*-{
        return this.source;
    }-*/;
    
    public final native GWTVertex getTarget() /*-{
        return this.target;
    }-*/;
    
    public final native String getId() /*-{
        return this.id;
    }-*/;
    
    private final native boolean isSelected() /*-{
        return this.selected === undefined ? false : this.selected;
    }-*/;
    
    public final native void setSelected(boolean selected) /*-{
        this.selected = selected;
    }-*/;
    
    public final native void setCssClass(String cssClass) /*-{
        this.cssClass = cssClass;
    }-*/;
    
    public final native String getCssClass() /*-{
        return this.cssClass;
    }-*/;

    public final native void setLinkNum(int num) /*-{
        this.linkNum = num;
    }-*/;
    
    public final native int getLinkNum() /*-{
        return this.linkNum;
    }-*/;

    public final native void setTooltipText(String tooltipText) /*-{
        this.tooltipText = tooltipText;
    }-*/;

    public final native String getTooltipText()/*-{
        return this.tooltipText;
    }-*/;

    public final native void setStatus(String status) /*-{
        this.status = status;
    }-*/;

    public final native String getStatus() /*-{
        return this.status;
    }-*/;

    public final native void setAdditionalStyling(JavaScriptObject additionalStyling) /*-{
        this.additionalStyling = additionalStyling;
    }-*/;

    public final native void setLinkCount(int linkCount) /*-{
        this.linkCount = linkCount;
    }-*/;

    public final native int getLinkCount() /*-{
        return this.linkCount;
    }-*/;

    /**
     * Applies the style defined in additionalStyling to the created SVG path element.
     * This is a hack as with pure GWT the "this" context did not match the correct DOM element.
     */
    public static final native JavaScriptObject createNativeFunctionToApplyStylings() /*-{
        return function(datum, index) {
            // only apply if defined
            if (datum.additionalStyling != undefined) {
                var currentSelection = $wnd.d3.select(this);

                currentSelection.style("stroke-width", "3px")
                                .style(datum.additionalStyling);
            }
        }
    }-*/;

    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                return selection.attr("class", GWTEdge.getCssStyleClass())
                        .attr("d", GWTEdge.createPath())
                        .each(createNativeFunctionToApplyStylings());
            }
        };
    }

    protected static double getPathSign(int linkIndex, int linkCount) {
        final int linkNum = linkIndex + 1;
        if (linkCount % 2 == 0) {
            // When there's an even number of links, alternate from
            // positive to negative
            return linkNum % 2 == 1 ? 1 : -1;
        } else if (linkNum == 1) {
            // When there's an odd number of links, the first link should
            // always be horizontal
            return 0;
        } else {
            // When there's an odd number of links, alternate from
            // positive to negative
            return linkNum % 2 == 0 ? 1 : -1;
        }
    }

    protected static double getPathMultiplier(int linkIndex, int linkCount) {
        final int linkNum = linkIndex + 1;
        // Increase the multiplier after every pair of links
        // If there's an odd number of links, use 0 for the first multiplier
        // since the link will be horizontal
        return Math.ceil((linkNum-(linkCount % 2)) / 2d);
    }

    protected static Func<String, GWTEdge> createPath() {
        return new Func<String, GWTEdge>(){

            @Override
            public String call(GWTEdge edge, int index) {
                final GWTVertex source = edge.getSource();
                final GWTVertex target = edge.getTarget();

                // Find the middle point of the line connecting the source and target vertices
                double sx = (source.getX() + target.getX()) / 2d;
                double sy = (source.getY() + target.getY()) / 2d;

                // Find the length of the line connecting p1 and p2
                double dy = (double)(target.getY() - source.getY());
                double dx = (double)(target.getX() - source.getX());
                double len = Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));

                // Calculate the sign (i.e. direction) and distance we'll use to project to the control point
                double pathSign = getPathSign(edge.getLinkNum(), edge.getLinkCount());
                double pathMultiplier = getPathMultiplier(edge.getLinkNum(), edge.getLinkCount());
                // Use the largest offset from the source and target vertices
                int pathOffset = Math.max(edge.getSource().getEdgePathOffset(), edge.getTarget().getEdgePathOffset());
                // Slowly increase the distance as the points get further apart (this is a logistics growth model)
                double lengthMultiplier = Math.max(3 / (1 + 250*Math.exp((-1/200d)*len)), 1);
                double effectiveDistance = pathMultiplier * lengthMultiplier * pathOffset;

                // Now calculate the coordinates (qx,qy) of our control point
                double qx, qy;
                if (dy == 0) {
                    // Both vertices are on the same horizontal line, project out vertically
                    qx = sx;
                    qy = sy + pathSign * effectiveDistance;
                } else if (dx == 0) {
                    // Both vertices are on the same vertical line, project out horizontally
                    qx = sx + pathSign * effectiveDistance;
                    qy = sy;
                } else {
                    // Calculate the slope and intercept of the line that is perpendicular to the
                    // line connecting both vertices and passes through the middle point (sx, sy)
                    double m = dy / dx;
                    double invm = -1/m; // The conjugate slope is perpendicular
                    double b = sy - invm * sx;
                    // Now calculate the coordinates of the point that is on the line
                    // we found above, and *distance* units away from (sx, sy)
                    qx = sx + pathSign * Math.sqrt(Math.pow(effectiveDistance, 2) / (1 + Math.pow(invm, 2)));
                    qy = invm * qx + b;
                }

                return "M" + source.getX() + " " + source.getY() + " Q " + qx + " " + qy +
                            " " + target.getX() + " " + target.getY();
            }

        };
    }

    protected static Func<String, GWTEdge> getCssStyleClass(){
        return new Func<String, GWTEdge>(){

            @Override
            public String call(GWTEdge datum, int index) {
                return datum.getCssClass();
            }
        };
    }

    public static D3Behavior create() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                return selection.append(SVG_EDGE_ELEMENT)
                        .attr("class", "path")
                        .attr("opacity", 0)
                        .style("fill", "none")
                        .style("cursor", "pointer")
                        .call(draw());
            }
        };
    }
}
