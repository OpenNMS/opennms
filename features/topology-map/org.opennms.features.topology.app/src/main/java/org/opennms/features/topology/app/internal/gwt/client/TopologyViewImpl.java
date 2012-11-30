package org.opennms.features.topology.app.internal.gwt.client;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.AnonymousFunc;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Transform;
import org.opennms.features.topology.app.internal.gwt.client.d3.Tween;
import org.opennms.features.topology.app.internal.gwt.client.svg.BoundingRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.ClientRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.BrowserInfo;
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
    HTMLPanel m_widgetContainer;
    
    TopologyViewRenderer m_topologyViewRenderer;

    private boolean m_isRefresh;
    private SVGMatrix m_currentViewportCTM;
    

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
        m_presenter.getViewRenderer().draw(graph, this);
    }

    @Override
    public void onBrowserEvent(Event event) {
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
                
            case Event.ONMOUSEDOWN:
    
                break;
    
            case Event.ONMOUSEWHEEL:
                double delta = event.getMouseWheelVelocityY() / 30.0;
                double oldScale = getViewPortScale();
                final double newScale = oldScale + delta;
                final int clientX = event.getClientX();
                final int clientY = event.getClientY();
                consoleLog("delta: " + delta);
                
                //broken now need to fix it
                  Command cmd = new Command() {
                        
                        public void execute() {
                            
                            m_presenter.onMouseWheel(newScale, clientX, clientY);
                        }
                    };
                    
                    if(BrowserInfo.get().isWebkit()) {
                        Scheduler.get().scheduleDeferred(cmd);
                    }else {
                        cmd.execute();
                    }
    
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
    public void onGraphUpdated(GWTGraph graph) {
            m_presenter.getViewRenderer().draw(graph, this);
            
            if(m_isRefresh) {
                fitToScreen();
                m_isRefresh = false;
            }
            
    }
    
    void updateScale(double oldScale, double newScale, int cx, int cy) {
        if(oldScale > 0) {
            double zoomFactor = newScale/oldScale;
        	
            SVGElement svg = getSVGElement();
            SVGGElement g = getSVGViewPort().cast();
        
        	if(cx == 0 ) {
        		cx = (int) (Math.ceil(svg.getParentElement().getOffsetWidth() / 2.0) - 1);
        	}
        
        	if(cy == 0) {
        		cy = (int) (Math.ceil(svg.getParentElement().getOffsetHeight() / 2.0) -1);
        	}
        
        	SVGPoint p = svg.createSVGPoint();
        	p.setX(cx);
        	p.setY(cy);
        	String gCTM = matrixTransform(g.getCTM());
        	String gCTMInverse = matrixTransform(g.getCTM().inverse());
        	p = p.matrixTransform(g.getCTM().inverse());
        	double x2 = p.getX();
        	double y2 = p.getY();
        	SVGMatrix m = svg.createSVGMatrix()
        			.translate(x2,y2)
        			 .scale(zoomFactor)
        			.translate(-x2, -y2);
        	m_currentViewportCTM = g.getCTM().multiply(m);
        	
//        	D3.d3().select(getSVGViewPort())
//        	    .transition().duration(30000)
//        	    .attr("transform", matrixTransform(ctm) )
//        	    .selectAll(".edge").transition().styleTween("stroke-width", edgeStrokeWidthTween(5 / newScale) );
        } else {
           m_isRefresh = true;
        }
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
    public void zoomToFit(final BoundingRect rect) {
        if(!rect.isEmpty()) {
            SVGElement svg = getSVGElement().cast();
            final int svgWidth = svg.getParentElement().getOffsetWidth(); 
            final int svgHeight = svg.getParentElement().getOffsetHeight();
            
            final double scale = Math.min(svgWidth/(double)rect.getWidth(), svgHeight/(double)rect.getHeight());
            
            double svgCenterX = svgWidth/2;
            double svgCenterY = svgHeight/2;
            
            double translateX = (svgCenterX - rect.getCenterX());
            double translateY = (svgCenterY - rect.getCenterY());
            
            
            SVGMatrix transform = svg.createSVGMatrix()
                .translate(translateX, translateY)
                .translate(-rect.getCenterX()*(scale-1), -rect.getCenterY()*(scale-1)) 
                .scale(scale);
                       
            String transformVal = ((TopologyViewImpl)this).matrixTransform(transform);
            
            D3.d3().select(getSVGViewPort()).transition().duration(2000).attr("transform", transformVal).each("end", new AnonymousFunc() {
                
                @Override
                public void call() {
                    m_presenter.onScaleUpdate(scale);
                }
            });
        }
    }

    private void fitToScreen() {
        SVGElement svg = getSVGElement().cast();
        final int svgWidth = svg.getParentElement().getOffsetWidth(); 
        final int svgHeight = svg.getParentElement().getOffsetHeight();
        
        ClientRect clientRect = getSVGViewPort().getBoundingClientRect();
        
        final double scale = Math.min(svgWidth/((double)clientRect.getWidth() + 100), svgHeight/((double)clientRect.getHeight() + 100));
        double translateX = (svgWidth - (clientRect.getWidth() * scale )) / 2;
        double translateY = (svgHeight - (clientRect.getHeight() * scale)) / 2;
        
        SVGMatrix transform = svg.createSVGMatrix()
            .translate(translateX, translateY)
            .scale(scale);
                   
        String transformVal = ((TopologyViewImpl)this).matrixTransform(transform);
        
        D3.d3().select(getSVGViewPort()).attr("transform", transformVal);
        m_presenter.onScaleUpdate(scale);
        
    }

    @Override
    public SVGMatrix getViewportTransform() {
        return m_currentViewportCTM;
    }

}
