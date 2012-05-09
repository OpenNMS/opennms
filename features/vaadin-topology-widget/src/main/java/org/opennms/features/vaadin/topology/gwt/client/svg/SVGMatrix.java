package org.opennms.features.vaadin.topology.gwt.client.svg;

import com.google.gwt.core.client.JavaScriptObject;

public class SVGMatrix extends JavaScriptObject {
    
    protected SVGMatrix() {};
    
    public final native SVGMatrix translate(double x, double y)/*-{
        return this.translate(x, y);
    }-*/;

    public final native SVGMatrix scale(double newScale) /*-{
        return this.scale(newScale);
    }-*/;

    public final native SVGMatrix multiply(SVGMatrix m) /*-{
        return this.multiply(m);
    }-*/;

    public final native double getA() /*-{
        return this.a;
    }-*/;
    
    public final native double getB() /*-{
        return this.b;
    }-*/;
    
    public final native double getC() /*-{
        return this.c;
    }-*/;
    
    public final native double getD() /*-{
        return this.d;
    }-*/;
    
    public final native double getE() /*-{
        return this.e;
    }-*/;
    
    public final native double getF() /*-{
        return this.f;
    }-*/;

    public final native SVGMatrix inverse() /*-{
        return this.inverse();
    }-*/;

    public final native void setX(int clientX) /*-{
        this.x = clientX;
    }-*/;

    public final native void setY(int clientY) /*-{
        this.y = clientY;
    }-*/;

    public final native SVGMatrix matrixTransform(SVGMatrix matrix) /*-{
        return this.matrixTransform(matrix);
    }-*/;

    public final native int getX() /*-{
        return this.x;
    }-*/;

    public final native int getY() /*-{
        return this.y;
    }-*/;

}
