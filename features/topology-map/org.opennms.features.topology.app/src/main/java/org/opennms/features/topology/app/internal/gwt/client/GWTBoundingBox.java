package org.opennms.features.topology.app.internal.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;


public class GWTBoundingBox extends JavaScriptObject {

    protected GWTBoundingBox() {}
    
    public static final native GWTBoundingBox create(int x, int y, int width, int height) /*-{
        return {"x":x, "y":y, "width":width, "height":height};
    }-*/;

    public final native int getX() /*-{
        return this.x;
    }-*/;

    public final native int getY() /*-{
        return this.y;
    }-*/;

    public final native int getWidth() /*-{
        return this.width;
    }-*/;

    public final native int getHeight() /*-{
        return this.height;
    }-*/;
    
}
