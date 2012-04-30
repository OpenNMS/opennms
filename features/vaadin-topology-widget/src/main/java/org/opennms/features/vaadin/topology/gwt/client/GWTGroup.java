package org.opennms.features.vaadin.topology.gwt.client;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Behavior;
import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public final class GWTGroup extends JavaScriptObject {

    protected GWTGroup() {};
    
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
    
    public static final native GWTGroup create(String id, int x, int y) /*-{
        return {"id":id, "x":x, "y":y, "selected":false, "actions":[], "iconUrl":"", "semanticZoomLevel":0};
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
        return "id: " + getId() + " SZL: " + getSemanticZoomLevel();
    }
    
    
    public final native int getSemanticZoomLevel() /*-{
		return this.semanticZoomLevel;
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
    
    public final native String getIconUrl() /*-{
        return this.iconUrl;
    }-*/;
    
    public final native void setIcon(String iconUrl) /*-{
        this.iconUrl = iconUrl;
    }-*/;

    static Func<String, GWTGroup> selectedFill() {
    	return new Func<String, GWTGroup>(){
    
    		public String call(GWTGroup vertex, int index) {
    			return vertex.isSelected() ? "blue" : "black";
    		}
    	};
    }
    
    protected static Func<String, GWTGroup> selectionFilter() {
        return new Func<String, GWTGroup>(){

            public String call(GWTGroup vertex, int index) {
                return vertex.isSelected() ? "1" : "0";
            }
            
        };
    }

    static Func<String, GWTGroup> getTranslation() {
    	return new Func<String, GWTGroup>() {
    
    		public String call(GWTGroup datum, int index) {
    			return "translate( " + datum.getX() + "," + datum.getY() + ")";
    		}
    		
    	};
    }
    
    static Func<String, GWTGroup> getIconPath(){
        return new Func<String, GWTGroup>(){

            public String call(GWTGroup datum, int index) {
                if(datum.getIconUrl().equals("")) {
                    return GWT.getModuleBaseURL() + "topologywidget/images/server.png";
                }else {
                    return datum.getIconUrl();
                }
                
            }
        };
    }
    
    public static D3Behavior draw() {
        return new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                return selection.attr("transform", GWTGroup.getTranslation()).select(".highlight").attr("opacity", GWTGroup.selectionFilter());
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
                
                vertex.append("rect").attr("class", "highlight").attr("fill", "yellow").attr("x", "-26px").attr("y", "-26px").attr("width", "52px").attr("height", "52px").attr("opacity", 0);
                
                vertex.append("svg:image").attr("xlink:href", getIconPath())
                      .attr("x", "-24px")
                      .attr("y", "-24px")
                      .attr("width", "48px")
                      .attr("height", "48px");
                
                vertex.call(draw());
                
                return vertex;
            }
        };
    }

	public final native void setParent(GWTGroup parentGroup) /*-{
		this.parent = parentGroup;		
	}-*/;
	
	public final native GWTGroup getParent()/*-{
		return this.parent;
	}-*/;

	public final native void setSemanticZoomLevel(int semanticZoomLevel) /*-{
		this.semanticZoomLevel = semanticZoomLevel;
	}-*/;

}
