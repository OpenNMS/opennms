package org.opennms.features.topology.app.internal.gwt.client;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Transform;
import org.opennms.features.topology.app.internal.gwt.client.d3.Tween;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VTooltip;

public class TopologyViewImpl extends Composite implements TopologyView<TopologyViewRenderer>, GraphUpdateListener {

    private static TopologyViewImplUiBinder uiBinder = GWT.create(TopologyViewImplUiBinder.class);

    interface TopologyViewImplUiBinder extends
            UiBinder<Widget, TopologyViewImpl> {
    }
    
    private Presenter<TopologyViewRenderer> m_presenter;
    
    @UiField
    Element m_svg;

    @UiField
    Element m_svgViewPort;

    @UiField
    Element m_edgeGroup;

    @UiField
    Element m_vertexGroup;

    @UiField
    Element m_referenceMap;

    @UiField
    Element m_referenceMapViewport;

    @UiField
    Element m_referenceMapBorder;
    
    @UiField
    Element m_marquee;
    
    @UiField
    Element m_marginContainer;
    
    @UiField
    HTMLPanel m_widgetContainer;
    
    TopologyViewRenderer m_topologyViewRenderer;

    private boolean m_isRefresh;


    public int getLeftMargin() {
        return LEFT_MARGIN;
    }

    public TopologyViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        m_widgetContainer.setSize("100%", "100%");
        sinkEvents(Event.ONCONTEXTMENU | VTooltip.TOOLTIP_EVENTS | Event.ONMOUSEWHEEL);
        m_topologyViewRenderer = m_presenter.getViewRenderer();
        
    }


    @Override
    public void setPresenter(Presenter<TopologyViewRenderer> presenter) {
        m_presenter = presenter;
        m_presenter.addGraphUpdateListener(this);
    }

    @Override
    public SVGElement getSVGElement() {
        return m_svg.cast();
    }
    
    private SVGGElement getMarginContainer() {
        return m_marginContainer.cast();
    }
    
    @Override
    public SVGGElement getSVGViewPort() {
        return m_svgViewPort.cast();
    }

    @Override
    public Element getEdgeGroup() {
        return m_edgeGroup;
    }

    @Override
    public Element getVertexGroup() {
        return m_vertexGroup;
    }

    @Override
    public Element getReferenceViewPort() {
        return m_referenceMapViewport;
    }

    @Override
    public Element getMarqueeElement() {
        return m_marquee;
    }

    @Override
    public void repaintNow(GWTGraph graph) {
        m_presenter.getViewRenderer().draw(graph, this, graph.getBoundingBox());
    }

    @Override
    public void onBrowserEvent(final Event event) {
        super.onBrowserEvent(event);
        switch(DOM.eventGetType(event)) {
            case Event.ONCONTEXTMENU:

                EventTarget target = event.getEventTarget();
                
                if (target.equals( getSVGElement() )) {
                    m_presenter.onContextMenu(null, event.getClientX(), event.getClientY(), "map");
                }
                event.preventDefault();
                event.stopPropagation();
                break;
                
    
            case Event.ONCLICK:
                if(event.getEventTarget().equals(getSVGElement())) {
                    m_presenter.onBackgroundClick();
                }
                event.preventDefault();
                event.stopPropagation();
                break;
                
        }


    }

    private double getViewPortScale() {
        D3Transform transform = D3.getTransform(D3.d3().select(getSVGViewPort()).attr("transform"));
        return transform.getScale().get(0);
    }

    private native void consoleLog(Object obj) /*-{
        $wnd.console.log(obj);
    }-*/;

    @Override
    public void onGraphUpdated(GWTGraph graph, GWTBoundingBox oldBBox) {
            m_presenter.getViewRenderer().draw(graph, this, oldBBox);
    }
    
    @Override
    public SVGMatrix calculateNewTransform(GWTBoundingBox bounds) {
        int iconMargin = 50;
        int iconLeftMargin = iconMargin + 50;
        int topMargin = iconMargin + 50;
        
        SVGElement svg = getSVGElement().cast();
        final int svgWidth = getPhysicalWidth(); 
        final int svgHeight = getPhysicalHeight();
        
        double scale = Math.min(svgWidth/((double)bounds.getWidth() + iconLeftMargin), svgHeight/((double)bounds.getHeight() + topMargin));
        scale = scale > 2 ? 2 : scale;
        double translateX =  -bounds.getX();
        double translateY =  -bounds.getY();
        
        double calcY = (svgHeight - (bounds.getHeight()* scale))/2;
        double calcX = (svgWidth - ((bounds.getWidth()) * scale))/2 + getLeftMargin();
        SVGMatrix transform = svg.createSVGMatrix()
                .translate(calcX, calcY)
                .scale(scale)
                .translate(translateX, translateY)
                    ;
        return transform;
    }
    
    private Tween<String, GWTEdge> edgeStrokeWidthTween(final double scale) {
        return new Tween<String, GWTEdge>() {

            @Override
            public String call(GWTEdge edge, int index, String a) {
                
                final double strokeWidth = 5/scale;
                consoleLog("scale: " + scale + " strokeWidth: " + strokeWidth);
                consoleLog("a: " + a);
                return scale + "px";
            }
            
        };
    }       
    
    String matrixTransform(SVGMatrix matrix) {
        String m = "matrix(" + matrix.getA() +
                ", " + matrix.getB() +
                ", " + matrix.getC() + 
                ", " + matrix.getD() +
                ", " + matrix.getE() + 
                ", " + matrix.getF() + ")";
        return D3.getTransform( m ).toString();
    }

    @Override
    public SVGPoint getCenterPos(GWTBoundingBox box) {
        SVGGElement g = getSVGViewPort().cast();
        SVGMatrix stateTF = g.getCTM().inverse();
        
        SVGPoint p = getSVGElement().createSVGPoint();
        p.setX(getPhysicalWidth()/2 + getLeftMargin());
        p.setY(getPhysicalHeight()/2);
        
        SVGPoint center = p.matrixTransform(stateTF);
        
        return center;
    }
    
    public SVGPoint getPoint(int clientX, int clientY) {
        SVGGElement g = getSVGViewPort().cast();
        SVGMatrix stateTF = g.getCTM().inverse();
        
        SVGPoint p = getSVGElement().createSVGPoint();
        
        p.setX(clientX + getLeftMargin());
        p.setY(clientY);
        
        SVGPoint center = p.matrixTransform(stateTF);
        
        return center;
    }

    @Override
    public int getPhysicalWidth() {
        return getSVGElement().getParentElement().getOffsetWidth() - getLeftMargin();
    }

    @Override
    public int getPhysicalHeight() {
        return getSVGElement().getParentElement().getOffsetHeight();
    }


}
