package org.opennms.features.vaadin.topology.gwt.client;

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


}
