package org.opennms.features.vaadin.topology.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

public final class GWTEdge extends JavaScriptObject {
    
    protected GWTEdge() {};
    
    public final native GWTVertex getSource() /*-{
        return this.source;
    }-*/;
    
    public final native GWTVertex getTarget() /*-{
        return this.target;
    }-*/;
    
    public static final native GWTEdge create(GWTVertex source, GWTVertex target) /*-{
        return {"source":source, "target":target};
    }-*/;

    public String getId() {
        return getSource().getId() + "::" + getTarget().getId();
    }
}
