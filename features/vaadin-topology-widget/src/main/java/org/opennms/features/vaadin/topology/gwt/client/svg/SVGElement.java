package org.opennms.features.vaadin.topology.gwt.client.svg;

import com.google.gwt.dom.client.Element;

public class SVGElement extends Element{

    protected SVGElement() {}
    
    public final static native SVGElement wrapElement(Element svg) /*-{
        return elem;
    }-*/;

    public final native SVGMatrix createSVGMatrix() /*-{
        return this.createSVGMatrix();
    }-*/;

    public final native SVGElement getCTM() /*-{
        return this.getCTM();
    }-*/;

    public final native SVGPoint createSVGPoint() /*-{
        return this.createSVGPoint();
    }-*/;

}
