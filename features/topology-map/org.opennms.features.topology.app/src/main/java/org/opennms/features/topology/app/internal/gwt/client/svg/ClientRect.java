package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.core.client.JavaScriptObject;

public class ClientRect extends JavaScriptObject {

    protected ClientRect() {};
    
    public final native int getBottom() /*-{
        return this.bottom;
    }-*/;
    
    public final native int getHeight() /*-{
        return this.height;
    }-*/;
    
    public final native int getRight() /*-{
        return this.right;
    }-*/;
    
    public final native int getLeft() /*-{
        return this.left;
    }-*/;
    
    public final native int getTop() /*-{
        return this.top;
    }-*/;

    public final native int getWidth() /*-{
        return this.width;
    }-*/;
}
