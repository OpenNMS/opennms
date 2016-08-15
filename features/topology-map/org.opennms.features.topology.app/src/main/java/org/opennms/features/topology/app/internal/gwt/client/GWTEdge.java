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

    protected static Func<String, GWTEdge> createPath() {
        return new Func<String, GWTEdge>(){

            @Override
            public String call(GWTEdge edge, int index) {
                final GWTVertex source = edge.getSource();
				final GWTVertex target = edge.getTarget();
				final int dx = Math.abs(target.getX() - source.getX());
                final int dy = Math.abs(target.getY() - source.getY());
                // The distance of two points is a^2 + b^2 = c^2 -> c = SQRT(dx^2 + dy^2)
                final double distance =  Math.sqrt(dx * dx + dy * dy);
                // The minimal radius therefore is distance / 2
                final double minRadius = distance / 2;
                final double step = Math.max((int) distance, 300); // Minimal Step size is 300
                // A guessed maxRadius
                final double maxRadius = distance * 4;
                int direction = edge.getLinkNum() % 2 == 0  ? 0 : 1;

                // By default we draw an arc
                int rx = (int) Math.max(maxRadius - (edge.getLinkNum() / 2 * step), minRadius);
                if (edge.getLinkCount() % 2 == 1 && edge.getLinkNum() == edge.getLinkCount() -1 ) {
                    rx = 0; // if uneven link count, the last edge is always straight
                }
                consoleLog(edge.getId() + " Distance: " + distance);
                consoleLog(edge.getId() + " MaxRadius: " + maxRadius);
                consoleLog(edge.getId() + " MinRadius: " + minRadius);
                consoleLog(edge.getId() + " Step: " + step);
                consoleLog(edge.getId() + " rx: " + rx);
                consoleLog("-----");

                return "M" + source.getX() + "," + source.getY() +
                       " A" + rx + "," + rx + " 0 0, " + direction + " " + target.getX() + "," + target.getY();
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
