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
    public static final int EDGE_WIDTH = 3;
    
    protected GWTEdge() {};
    
    public static final native GWTEdge create(String id, GWTVertex source, GWTVertex target) /*-{
    	return {"id":id, "source":source, "target":target, "cssClass": "path", "linkNum":1, "tooltipText": "", status:""};
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

    public static final native void consoleLog(Object obj)/*-{
        $wnd.console.log(obj);
    }-*/;

    public final native void setStatus(String status) /*-{
        this.status = status;
    }-*/;

    public final native String getStatus() /*-{
        return this.status;
    }-*/;

    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                return selection.attr("class", GWTEdge.getCssStyleClass()).attr("d", GWTEdge.createPath());
            }
        };
    }

    protected static Func<String, GWTEdge> createPath(){
        return new Func<String, GWTEdge>(){

            @Override
            public String call(GWTEdge edge, int index) {
                GWTVertex source = edge.getSource();
				GWTVertex target = edge.getTarget();
				int dx = Math.abs(target.getX() - source.getX());
                int dy = Math.abs(target.getY() - source.getY());
                int dr = edge.getLinkNum() > 1 ? (Math.max(dx, dy) * 10) / edge.getLinkNum() : 0;
                int direction = edge.getLinkNum() % 2 == 0  ? 0 : 1;

                return "M" + source.getX() + "," + source.getY() +
                       " A" + dr + "," + dr + " 0 0, " + direction + " " + target.getX() + "," + target.getY();
            }

        };
    }

    protected static Func<String, GWTEdge> getCssStyleClass(){
        return new Func<String, GWTEdge>(){

            @Override
            public String call(GWTEdge datum, int index) {
                int i = index;
                String status = datum.getStatus();
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
                        .style("stroke-width", EDGE_WIDTH + "px")
                        .style("fill", "none")
                        .style("cursor", "pointer")
                        .call(draw());
            }
        };
    }
}
