package org.opennms.features.topology.app.internal.gwt.client.view;

import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public interface TopologyView<T> {

    public interface Presenter<T>{
        T getViewRenderer();
    }
    
    void setViewRenderer(T viewRenderer);
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
    SVGElement getSVGElement();
    SVGGElement getSVGViewPort();
    Element getEdgeGroup();
    Element getVertexGroup();
    Element getReferenceViewPort();
    Element getMarqueeElement();
}
