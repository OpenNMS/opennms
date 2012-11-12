package org.opennms.features.topology.app.internal.gwt.client.view;

import org.opennms.features.topology.app.internal.gwt.client.GWTGraph;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.svg.BoundingRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public interface TopologyView<T> {

    public interface Presenter<T>{
        void addGraphUpdateListener(GraphUpdateListener listener);
        T getViewRenderer();
        void onContextMenu(Object element, int x, int y, String type);
        void onMouseWheel();
        void onBackgroundClick();
        void onScaleUpdate(double scale);
    }
    
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
    SVGElement getSVGElement();
    SVGGElement getSVGViewPort();
    Element getEdgeGroup();
    Element getVertexGroup();
    Element getReferenceViewPort();
    Element getMarqueeElement();
    void repaintNow(GWTGraph graph);
    void zoomToFit(BoundingRect rect);
}
