package org.opennms.features.topology.app.internal.gwt.client.map;

import java.util.List;

import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;

import com.google.gwt.dom.client.Element;


public interface SVGTopologyMap {

    public static String EDGE_CSS_CLASS = ".edge";
    
    Element getVertexGroup();

    SVGElement getSVGElement();

    Element getReferenceViewPort();

    void repaintNow();

    Element getSVGViewPort();

    void setVertexSelection(List<String> vertIds);

    Element getMarqueeElement();

    D3 selectAllVertexElements();

}
