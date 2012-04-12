package org.opennms.features.vaadin.topology.gwt.client.d3;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;

public class D3Transform extends JavaScriptObject {

    protected D3Transform() {};
    
    public final native JsArrayInteger getTranslate() /*-{
        return this.translate;
    }-*/;
    
    public final native int getX() /*-{
        if(this.translate != "undefined"){
            return this.translate[0];
        }
        return -1;
    }-*/;
    
    public final native int getY() /*-{
        if(this.translate != "undefined"){
            return this.translate[1];
        }
        return -1;
    }-*/;
    
    public final native JsArrayNumber getScale() /*-{
        return this.scale;
    }-*/;
    
    public final native double getRotate() /*-{
        return this.rotate;
    }-*/;
    
    public final native double getSkew() /*-{
        return this.skew;
    }-*/;
}
