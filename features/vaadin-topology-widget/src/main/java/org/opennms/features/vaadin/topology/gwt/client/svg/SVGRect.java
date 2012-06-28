package org.opennms.features.vaadin.topology.gwt.client.svg;

import com.google.gwt.dom.client.Element;

public class SVGRect extends Element {

    protected SVGRect() {};
    
    public final native int getHeight() /*-{
        return this.height;
    }-*/;
    
    public final native int getWidth() /*-{
        return this.width;
    }-*/;
    
    public final native int getX() /*-{
        return this.x;
    }-*/;
    
    public final native int getY() /*-{
        return this.y;
    }-*/;
}
