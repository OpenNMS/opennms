package org.opennms.features.vaadin.topology.gwt.client;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Behavior;
import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public final class GWTVertex extends JavaScriptObject {

    protected GWTVertex() {};
    
    public final native String getId()/*-{
        return this.id;
    }-*/;
    
    public final native int getX()/*-{
        return this.x;
    }-*/;
    
    public final native int getY()/*-{
        return this.y;
    }-*/;
    
    public final native void setSelected(boolean selected) /*-{
        this.selected = selected;
    }-*/;
    
    public final native boolean isSelected() /*-{
        return this.selected;
    }-*/;
    
    public static final native GWTVertex create(String id, int x, int y) /*-{
        return {"id":id, "x":x, "y":y, "selected":false, "actions":[]};
    }-*/;

    public final native void setX(int newX) /*-{
        this.x = newX;
    }-*/;

    public final native void setY(int newY) /*-{
        this.y = newY;
    }-*/;
    
    private final native JsArrayString actionKeys() /*-{
    	return this.actions;
    }-*/;
    
    private final native JsArrayString actionKeys(JsArrayString keys) /*-{
    	this.actions = keys;
    	return this.actions;
    }-*/;
    
    public String getTooltipText() {
        return "id: " + getId();
    }
    
    
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

    static Func<String, GWTVertex> selectedFill() {
    	return new Func<String, GWTVertex>(){
    
    		public String call(GWTVertex vertex, int index) {
    			return vertex.isSelected() ? "blue" : "black";
    		}
    	};
    }

    static Func<String, GWTVertex> getTranslation() {
    	return new Func<String, GWTVertex>() {
    
    		public String call(GWTVertex datum, int index) {
    			return "translate( " + datum.getX() + "," + datum.getY() + ")";
    		}
    		
    	};
    }
    
    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                return selection.attr("transform", GWTVertex.getTranslation()).select("circle").style("fill", GWTVertex.selectedFill());
            }
        };
    }
    
    public static D3Behavior create() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                D3 vertex = selection.append("g").attr("class", "little");
                vertex.attr("opacity",1e-6);
                vertex.style("cursor", "pointer");
                vertex.append("circle").attr("r", 9);
                vertex.call(draw());
                
                vertex.append("text").attr("dy", ".35em")
                    .attr("text-anchor", "middle").style("fill", "white")
                    .text(D3.property("id"));
                
                return vertex;
            }
        };
    }
    

}
