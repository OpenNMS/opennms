package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.dom.client.Element;

public class SVGGElement extends Element {

    protected SVGGElement() {}

    public final native SVGMatrix getCTM() /*-{
        return this.getCTM();
    }-*/;

    public final native SVGRect getBBox() /*-{
        return this.getBBox();
    }-*/;

    public final native ClientRect getBoundingClientRect() /*-{
        return this.getBoundingClientRect();
    }-*/;


}
