package org.opennms.features.vaadin.topology.gwt.client.d3;

import com.google.gwt.core.client.JavaScriptObject;

public class D3MouseEvent extends JavaScriptObject {

    protected D3MouseEvent() {}
    
    public final native boolean shiftKey() /*-{
        return this.shiftKey;
    }-*/;
    
    public final native boolean altKey()/*-{
        return this.altKey;
    }-*/;
    
    public final native boolean screenX() /*-{
        return this.screenX;
    }-*/;
    
    public final native boolean screenY() /*-{
        return this.screenY;
    }-*/;
    
    public final native boolean clientX() /*-{
        return this.clientX;
    }-*/;
    
    public final native boolean clientY() /*-{
        return this.clientY;
    }-*/;
    
    public final native boolean ctrlKey() /*-{
        return this.ctrlKey;
    }-*/;
}
