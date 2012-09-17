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
import com.google.gwt.core.client.JsArrayString;

public final class GWTEdge extends JavaScriptObject {
    
    protected GWTEdge() {};
    
    public final native GWTVertex getSource() /*-{
        return this.source;
    }-*/;
    
    public final native GWTVertex getTarget() /*-{
        return this.target;
    }-*/;
    
    public static final native GWTEdge create(String id, GWTVertex source, GWTVertex target) /*-{
        return {"id":id, "source":source, "target":target, "actions":[]};
    }-*/;

    public final native String getId() /*-{
        return this.id;
    }-*/;
    
    private final native JsArrayString actionKeys() /*-{
		return this.actions;
	}-*/;

    private final native JsArrayString actionKeys(JsArrayString keys) /*-{
		this.actions = keys;
		return this.actions;
	}-*/;
    
    private final native boolean isSelected() /*-{
        return this.selected;
    }-*/;
    
    public final native void setSelected(boolean selected) /*-{
        this.selected = selected;
    }-*/;


	public void setActionKeys(String[] keys) {
		JsArrayString actionKeys = actionKeys(newStringArray());
		for(String key : keys) {
			actionKeys.push(key);
		}
	}
	
	private JsArrayString newStringArray() {
		return JsArrayString.createArray().<JsArrayString>cast();
	}
	
	public String[] getActionKeys() {
		JsArrayString actionKeys = actionKeys();
		String[] keys = new String[actionKeys.length()];
		for(int i = 0; i < keys.length; i++) {
			keys[i] = actionKeys.get(i);
		}
		return keys;
	}

    static Func<Integer, GWTEdge> getTargetY() {
        
        return new Func<Integer, GWTEdge>(){
    
            public Integer call(GWTEdge datum, int index) {
                return datum.getTarget().getY();
            }
        };
    }

    static Func<Integer, GWTEdge> getSourceY() {
        
        return new Func<Integer, GWTEdge>(){
    
            public Integer call(GWTEdge datum, int index) {
                return datum.getSource().getY();
            }
        };
    }

    static Func<Integer, GWTEdge> getTargetX() {
    
    	return new Func<Integer, GWTEdge>(){
    
            public Integer call(GWTEdge datum, int index) {
                return datum.getTarget().getX();
            }
        };
    }

    static Func<Integer, GWTEdge> getSourceX() {
    	
    	return new Func<Integer, GWTEdge>(){
    
            public Integer call(GWTEdge datum, int index) {
                return datum.getSource().getX();
            }
        };
    }
    
    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                
                return selection.style("stroke", GWTEdge.selectionFilter())
                        .attr("x1", GWTEdge.getSourceX())
                        .attr("x2", GWTEdge.getTargetX())
                        .attr("y1", GWTEdge.getSourceY())
                        .attr("y2", GWTEdge.getTargetY());
            }
        };
    }
    
    protected static Func<String, GWTEdge> selectionFilter() {
        // TODO Auto-generated method stub
        return new Func<String, GWTEdge>(){

            public String call(GWTEdge edge, int index) {
                return edge.isSelected() ? "yellow" : "#ccc";
            }
            
        };
    }

    public static D3Behavior create() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                return selection.append("line").attr("opacity", 0).style("stroke", "#ccc").style("stroke-width", "2").style("cursor", "pointer")
                        .call(draw());
            }
        };
    }

}
