package org.opennms.features.topology.app.internal.gwt.client;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.AnonymousFunc;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.svg.BoundingRect;
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
    
    TopologyViewRenderer m_topologyViewRenderer;
    

    public TopologyViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
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
                    event.preventDefault();
                    event.stopPropagation();
                }
                break;
                
            case Event.ONMOUSEDOWN:
    
                break;
    
            case Event.ONMOUSEWHEEL:
                double delta = event.getMouseWheelVelocityY() / 30.0;
                double oldScale = 1; //m_scale;
                final double newScale = oldScale + delta;
                final int clientX = event.getClientX();
                final int clientY = event.getClientY();
                //broken now need to fix it
                //          Command cmd = new Command() {
                //                
                //                public void execute() {
                //                    m_client.updateVariable(m_paintableId, "mapScale", newScale, false);
                //                    m_client.updateVariable(m_paintableId, "clientX", clientX, false);
                //                    m_client.updateVariable(m_paintableId, "clientY", clientY, false);
                //                    
                //                    m_client.sendPendingVariableChanges();
                //                }
                //            };
                //            
                //            if(BrowserInfo.get().isWebkit()) {
                //                Scheduler.get().scheduleDeferred(cmd);
                //            }else {
                //                cmd.execute();
                //            }
    
                break;
                
            case Event.ONCLICK:
                if(event.getEventTarget().equals(getSVGElement())) {
                    m_presenter.onBackgroundClick();
                }
                break;
        }


    }

    @Override
    public void onGraphUpdated(GWTGraph graph) {
            m_presenter.getViewRenderer().draw(graph, this);
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
        	SVGMatrix ctm = g.getCTM().multiply(m);
        	String tempM = matrixTransform(ctm);
        	D3.d3().select(getSVGViewPort()).transition().duration(1000).attr("transform", tempM);
        }
    }
    
    String matrixTransform(SVGMatrix matrix) {
        return "matrix(" + matrix.getA() +
                ", " + matrix.getB() +
                ", " + matrix.getC() + 
                ", " + matrix.getD() +
                ", " + matrix.getE() + 
                ", " + matrix.getF() + ")";
    }

    @Override
    public void zoomToFit(final BoundingRect rect) {
        if(!rect.isEmpty()) {
            SVGElement svg = getSVGElement().cast();
            final int svgWidth = svg.getParentElement().getOffsetWidth(); 
            final int svgHeight = svg.getParentElement().getOffsetHeight();
            
            double svgCenterX = svgWidth/2;
            double svgCenterY = svgHeight/2;
            
            double translateX = (svgCenterX - rect.getCenterX());
            double translateY = (svgCenterY - rect.getCenterY());
            
            final double scale = Math.min(svgWidth/(double)rect.getWidth(), svgHeight/(double)rect.getHeight());
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

}
