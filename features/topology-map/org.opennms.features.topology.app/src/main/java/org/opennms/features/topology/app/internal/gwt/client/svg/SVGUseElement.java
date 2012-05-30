package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.dom.client.Element;

public class SVGUseElement extends Element {

    protected SVGUseElement() {}

    public final native SVGRect getBBox() /*-{
        return this.getBBox();
    }-*/;
}
