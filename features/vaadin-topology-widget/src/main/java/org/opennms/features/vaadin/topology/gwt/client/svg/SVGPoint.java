package org.opennms.features.vaadin.topology.gwt.client.svg;

import com.google.gwt.core.client.JavaScriptObject;

public class SVGPoint extends JavaScriptObject {
    
    protected SVGPoint() {}

    public final native void setX(int x) /*-{
        this.x = x
    }-*/;

    public final native void setY(int y) /*-{
        this.y = y;
    }-*/;

    public final native SVGPoint matrixTransform(SVGMatrix m) /*-{
        return this.matrixTransform(m);
    }-*/;

    public final native double getX() /*-{
        return this.x;
    }-*/;
    
    public final native double getY() /*-{
        return this.y;
    }-*/;
    
}
