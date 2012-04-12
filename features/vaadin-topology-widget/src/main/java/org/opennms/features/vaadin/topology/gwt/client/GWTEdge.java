package org.opennms.features.vaadin.topology.gwt.client;

import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

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
    
    public static final native GWTEdge create(GWTVertex source, GWTVertex target) /*-{
        return {"source":source, "target":target, "actions":[]};
    }-*/;

    public String getId() {
        return getSource().getId() + "::" + getTarget().getId();
    }
    
    private final native JsArrayString actionKeys() /*-{
		return this.actions;
	}-*/;

    private final native JsArrayString actionKeys(JsArrayString keys) /*-{
		this.actions = keys;
		return this.actions;
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


}
