package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.core.client.JavaScriptObject;

public class SVGAnimatedLength extends JavaScriptObject {

    protected SVGAnimatedLength() {
        
    }

    public final native SVGLength getBaseVal() /*-{
        return this.baseVal;
    }-*/;
}
