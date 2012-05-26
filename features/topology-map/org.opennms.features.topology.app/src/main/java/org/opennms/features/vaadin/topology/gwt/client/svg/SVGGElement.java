package org.opennms.features.vaadin.topology.gwt.client.svg;

import com.google.gwt.dom.client.Element;

public class SVGGElement extends Element {

    protected SVGGElement() {}

    public final native SVGMatrix getCTM() /*-{
        return this.getCTM();
    }-*/;

    public final native SVGRect getBBox() /*-{
        return this.getBBox();
    }-*/;

}
