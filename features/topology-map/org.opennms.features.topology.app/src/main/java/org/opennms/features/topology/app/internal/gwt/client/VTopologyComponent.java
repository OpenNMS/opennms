package org.opennms.features.topology.app.internal.gwt.client;

import static org.opennms.features.topology.app.internal.gwt.client.d3.TransitionBuilder.fadeIn;
import static org.opennms.features.topology.app.internal.gwt.client.d3.TransitionBuilder.fadeOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Drag;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Transform;
import org.opennms.features.topology.app.internal.gwt.client.d3.Func;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGRect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.TooltipInfo;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VTooltip;
import com.vaadin.terminal.gwt.client.ui.Action;
import com.vaadin.terminal.gwt.client.ui.ActionOwner;
import com.vaadin.terminal.gwt.client.ui.dd.VDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler;

public class VTopologyComponent extends Composite implements Paintable, ActionOwner, VHasDropHandler {
	
    public class GraphDrawerNoTransition extends GraphDrawer{
        
        public GraphDrawerNoTransition(GWTGraph graph, Element vertexGroup,Element edgeGroup, D3Behavior dragBehavior, Handler<GWTVertex> clickHandler, Handler<GWTVertex> contextMenuHandler, Handler<GWTVertex> tooltipHandler, Handler<GWTEdge> edgeContextHandler, Handler<GWTEdge> edgeToolTipHandler) {
            super(graph, vertexGroup, edgeGroup, dragBehavior, clickHandler,contextMenuHandler, tooltipHandler, edgeContextHandler, edgeToolTipHandler);
        }
        
        @Override
        protected D3Behavior enterTransition() {
            return new D3Behavior() {

                @Override
                public D3 run(D3 selection) {
                    return selection;
                }
                
            };
        }

        @Override
        protected D3Behavior exitTransition() {
            return new D3Behavior() {

                @Override
                public D3 run(D3 selection) {
                    return selection;
                }
                
            };
        }

        @Override
        protected D3Behavior updateTransition() {
            return new D3Behavior() {

                @Override
                public D3 run(D3 selection) {
                    return selection;
                }
                
            };
        }

    }
    
    public class GraphDrawer{
        GWTGraph m_graph;
        Element m_vertexGroup;
        Element m_edgeGroup;
        D3Behavior m_dragBehavior;
        Handler<GWTVertex> m_clickHandler;
        Handler<GWTVertex> m_contextMenuHandler;
        private Handler<GWTVertex> m_vertexMouseOverHandler;
        private Handler<GWTEdge> m_edgeContextHandler;
        private Handler<GWTEdge> m_edgeToolTipHandler;
        

        public GraphDrawer(GWTGraph graph, Element vertexGroup, Element edgeGroup, D3Behavior dragBehavior, Handler<GWTVertex> clickHandler, Handler<GWTVertex> contextMenuHandler, Handler<GWTVertex> tooltipHandler, Handler<GWTEdge> edgeContextHandler, Handler<GWTEdge> edgeToolTipHandler) {
            m_graph = graph;
            m_vertexGroup = vertexGroup;
            m_edgeGroup = edgeGroup;
            m_dragBehavior = dragBehavior;
            setClickHandler(clickHandler);
            setContextMenuHandler(contextMenuHandler);
            m_vertexMouseOverHandler = tooltipHandler;
            setEdgeContextHandler(edgeContextHandler);
            setEdgeToolTipHandler(edgeToolTipHandler);
        }
        
        public void updateGraph(GWTGraph graph) {
            m_graph = graph;
            draw();
        }
        
        public Handler<GWTVertex> getClickHandler() {
            return m_clickHandler;
        }

        public void setClickHandler(Handler<GWTVertex> clickHandler) {
            m_clickHandler = clickHandler;
        }

        public Handler<GWTVertex> getContextMenuHandler() {
            return m_contextMenuHandler;
        }

        public void setContextMenuHandler(Handler<GWTVertex> contextMenuHandler) {
            m_contextMenuHandler = contextMenuHandler;
        }

        public GWTGraph getGraph() {
            return m_graph;
        }

        public Element getEdgeGroupElement() {
            return m_edgeGroup;
        }

        public Element getVertexGroupElement() {
            return m_vertexGroup;
        }

        D3 getEdgeGroup() {
            return D3.d3().select(getEdgeGroupElement());
        }

        D3 getVertexGroup() {
            return D3.d3().select(getVertexGroupElement());
        }

        public D3Behavior getDragBehavior() {
            return m_dragBehavior;
        }

        void draw() {
            
            GWTGraph graph = getGraph();
            
            D3 edgeSelection = getEdgeSelection(graph);
            
            D3 vertexSelection = getVertexSelection(graph);
            
            vertexSelection.enter().create(GWTVertex.create()).call(setupEventHandlers())
            	.attr("transform", new Func<String, GWTVertex>() {

					public String call(GWTVertex vertex, int index) {
						GWTVertex displayVertex = vertex.getDisplayVertex(m_oldSemanticZoomLevel);
						
						return "translate(" + displayVertex.getX() + "," +  displayVertex.getY() + ")";
					}
            		
				}).attr("opacity", 1);
            
            //Exits
            edgeSelection.exit().with(exitTransition()).remove();
            vertexSelection.exit().with(new D3Behavior() {

				@Override
				public D3 run(D3 selection) {
					return selection.transition().delay(0).duration(500);
				}
			}).attr("transform", new Func<String, GWTVertex>(){

				public String call(GWTVertex vertex, int index) {
					GWTVertex displayVertex = vertex.getDisplayVertex(m_semanticZoomLevel);
					
					return "translate(" + displayVertex.getX() + "," +  displayVertex.getY() + ")";
				}
				
			}).attr("opacity", 0).remove();
            
            
            //Updates
            edgeSelection.with(updateTransition()).call(GWTEdge.draw()).attr("opacity", 1);
            
            vertexSelection.with(updateTransition()).call(GWTVertex.draw()).attr("opacity", 1);
        	
            
            //Enters
            edgeSelection.enter().create(GWTEdge.create()).call(setupEdgeEventHandlers()).with(enterTransition());
            
            //vertexSelection.enter().create(GWTVertex.create()).call(setupEventHandlers()).with(enterTransition());

        }

        protected D3Behavior enterTransition() {
            return fadeIn(500, 1000);
        }

        protected D3Behavior exitTransition() {
            return fadeOut(500, 0);
        }
        
        protected D3Behavior updateTransition() {
            return new D3Behavior() {

                @Override
                public D3 run(D3 selection) {
                    return selection.transition().delay(500).duration(500);
                }
            };
        }
        
        private D3Behavior setupEdgeEventHandlers() {
            return new D3Behavior() {

                @Override
                public D3 run(D3 selection) {
                    return selection.on(D3Events.CONTEXT_MENU.event(), getEdgeContextHandler())
                                    .on(D3Events.MOUSE_OVER.event(), getEdgeToolTipHandler())
                                    .on(D3Events.MOUSE_OUT.event(), getEdgeToolTipHandler());
                }
            };
        }
        
        private D3Behavior setupEventHandlers() {
            return new D3Behavior() {

                @Override
                public D3 run(D3 selection) {
                    return selection.on(D3Events.CLICK.event(), getClickHandler())
                            .on(D3Events.CONTEXT_MENU.event(), getContextMenuHandler())
                            .on(D3Events.MOUSE_OVER.event(), getVertexTooltipHandler())
                            .on(D3Events.MOUSE_OUT.event(), getVertexTooltipHandler())
                            .call(getDragBehavior());
                }
            };
        }
        
        private Handler<GWTVertex> getVertexTooltipHandler(){
            return m_vertexMouseOverHandler;
        }
        
        private D3 getVertexSelection(GWTGraph graph) {
            return getVertexGroup().selectAll(".little")
                    .data(graph.getVertices(m_semanticZoomLevel), new Func<String, GWTVertex>() {
        
        				public String call(GWTVertex param, int index) {
        					return "" + param.getId();
        				}
                    	
                    });
        }

        private D3 getEdgeSelection(GWTGraph graph) {
            return getEdgeGroup().selectAll("line")
                    .data(graph.getEdges(m_semanticZoomLevel), new Func<String, GWTEdge>() {
        
        				public String call(GWTEdge edge, int index) {
        					String edgeId = edge.getId();
        					return edgeId;
        				}
                    	
                    });
        }

        public Handler<GWTEdge> getEdgeContextHandler() {
            return m_edgeContextHandler;
        }

        public void setEdgeContextHandler(Handler<GWTEdge> edgeClickHandler) {
            m_edgeContextHandler = edgeClickHandler;
        }

        public Handler<GWTEdge> getEdgeToolTipHandler() {
            return m_edgeToolTipHandler;
        }

        public void setEdgeToolTipHandler(Handler<GWTEdge> edgeToolTipHandler) {
            m_edgeToolTipHandler = edgeToolTipHandler;
        }

    }
    
    public class PanObject extends DragObject{
        private SVGMatrix m_stateTf;
        private SVGPoint m_stateOrigin;
        
        public PanObject(Element draggableElement, Element containerElement) {
            super(draggableElement, containerElement);
            
            SVGGElement g = draggableElement.cast();
            m_stateTf = g.getCTM().inverse();
            
            m_stateOrigin = getEventPoint(D3.getEvent()).matrixTransform(m_stateTf); 
            
        }
        
        @Override
        public void move() {
            Event event = D3.getEvent().cast();
            SVGPoint p = getEventPoint(event).matrixTransform(m_stateTf);
            
            SVGElement svg = getContainerElement().cast();
            SVGGElement g = getDraggableElement().cast();
            SVGRect gBox = g.getBBox();
            
            SVGMatrix m = m_stateTf.inverse().translate(p.getX() - m_stateOrigin.getX(), p.getY() - m_stateOrigin.getY() );
            
            double mapWidth = gBox.getWidth() * m.getA();
            double mapHeight = gBox.getHeight() * m.getA();
            
            double boundaryX = calculateBoundsX(mapWidth, svg.getOffsetWidth(), m.getE());
            
            double boundaryY = calculateBoundsY(mapHeight, svg.getOffsetHeight(), m.getF());
            
            String matrixTransform = "matrix(" + m.getA() +
                    ", " + m.getB() +
                    ", " + m.getC() + 
                    ", " + m.getD() +
                    ", " + boundaryX + 
                    ", " + boundaryY + ")";
            
            getDraggableElement().setAttribute("transform", matrixTransform);
            
            //Updating the reference map
            //TODO: this needs to be reworked a little its off
            double viewPortWidth = (getContainerElement().getOffsetWidth() / m.getA()) * 0.4;
            double viewPortHeight = (getContainerElement().getOffsetHeight() / m.getA()) * 0.4;
            m_referenceMapViewport.setAttribute("width", "" + viewPortWidth);
            m_referenceMapViewport.setAttribute("height", "" + viewPortHeight);
            m_referenceMapViewport.setAttribute("x", "" + (-boundaryX * 0.4));
            m_referenceMapViewport.setAttribute("y", "" + (-boundaryY * 0.4));
        }

        private double calculateBoundsY(double mapHeight, int offsetHeight,
                double y) {
            double boundaryY;
            if(mapHeight > offsetHeight) {
                boundaryY = Math.min(0, Math.max(offsetHeight - mapHeight, y));
            }else {
                boundaryY = Math.max(0, Math.min(offsetHeight - mapHeight, y));
            }
            return boundaryY;
        }

        private double calculateBoundsX(double mapWidth, int offsetWidth, double x) {
            double boundaryX;
            if(mapWidth > offsetWidth) {
                return Math.min(0, Math.max(offsetWidth - mapWidth, x));
            }else {
                return Math.max(0, Math.min(offsetWidth - mapWidth, x));
            }
        }

    }
    
    public class DragObject{
		private Element m_containerElement;
		private Element m_draggableElement;
		private int m_startX;
		private int m_startY;
		private D3Transform m_transform;

        public DragObject(Element draggableElement, Element containerElement) {
            
            m_draggableElement = draggableElement;
            m_containerElement = containerElement;
        	
            //User m_vertexgroup because this is what we scale instead of every vertex element
            m_transform = D3.getTransform(D3.d3().select(m_vertexGroup).attr("transform"));
            
            JsArrayInteger position = D3.getMouse(containerElement);
            m_startX = (int) (position.get(0) / m_transform.getScale().get(0));
            m_startY = (int) (position.get(1) / m_transform.getScale().get(1));
        }
        
        public Element getContainerElement() {
            return m_containerElement;
        }
        
        public Element getDraggableElement() {
            return m_draggableElement;
        }
        
    	public int getCurrentX() {
    		JsArrayInteger position = D3.getMouse(m_containerElement);
    		return (int) (position.get(0) / m_transform.getScale().get(0));
    	}
    	
    	public int getCurrentY() {
    		JsArrayInteger position = D3.getMouse(m_containerElement);
    		return (int) (position.get(1) / m_transform.getScale().get(1));
    	}

		public int getStartX() {
			return m_startX;
		}
		
		public int getStartY() {
			return m_startY;
		}

        public void move() {
            VTopologyComponent.this.repaintGraphNow();
        }
        
        protected SVGPoint getEventPoint(NativeEvent event) {
            SVGElement svg = m_svg.cast();
            SVGPoint p = svg.createSVGPoint();
            p.setX(event.getClientX());
            p.setY(event.getClientY());
            return p;
        }

    }
    
    private static VTopologyComponentUiBinder uiBinder = GWT
            .create(VTopologyComponentUiBinder.class);

    interface VTopologyComponentUiBinder extends
            UiBinder<Widget, VTopologyComponent> {
    }
    
    private ApplicationConnection m_client;
    private String m_paintableId;
    
    private GWTGraph m_graph;
	private double m_scale = 1;
    private DragObject m_dragObject;
    
    @UiField
    Element m_svg;
    
    @UiField
    Element m_svgViewPort;
    
    @UiField
    Element m_edgeGroup;
    
    @UiField
    Element m_vertexGroup;
    
    @UiField
    Element m_scaledMap;
    
    @UiField
    Element m_referenceMap;
    
    @UiField
    Element m_referenceMapViewport;
    
    @UiField
    Element m_referenceMapBorder;
    
    /**
     * This map contains captions and icon urls for actions like: * "33_c" ->
     * "Edit" * "33_i" -> "http://dom.com/edit.png"
     */
    private final HashMap<String, String> m_actionMap = new HashMap<String, String>();
    
    
	private String[] m_actionKeys;
	
	private D3Drag m_d3PanDrag;
    private GraphDrawer m_graphDrawer;
    private GraphDrawerNoTransition m_graphDrawerNoTransition;
    protected PanObject m_panObject;
    private List<Element> m_selectedElements = new ArrayList<Element>();
	private int m_semanticZoomLevel;
	private int m_oldSemanticZoomLevel;
    
    public VTopologyComponent() {
        initWidget(uiBinder.createAndBindUi(this));
        
        m_graph = GWTGraph.create();
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
       
        sinkEvents(Event.ONCONTEXTMENU | VTooltip.TOOLTIP_EVENTS | Event.ONMOUSEWHEEL);
        
        setupPanningBehavior(m_svg);
        
        D3Behavior dragBehavior = new D3Behavior() {

            @Override
            public D3 run(D3 selection) {
                D3Drag drag = D3.getDragBehavior();
                drag.on(D3Events.DRAG_START.event(), vertexDragStartHandler());
                drag.on(D3Events.DRAG.event(), vertexDragHandler());
                drag.on(D3Events.DRAG_END.event(), vertexDragEndHandler());
                
                selection.call(drag);
                return selection;
            }
            
        };
        
        m_graphDrawer = new GraphDrawer(m_graph, m_vertexGroup, m_edgeGroup, dragBehavior, vertexClickHandler(), vertexContextMenuHandler(), vertexTooltipHandler(), edgeContextHandler(), edgeToolTipHandler());
        m_graphDrawerNoTransition = new GraphDrawerNoTransition(m_graph, m_vertexGroup, m_edgeGroup, dragBehavior, vertexClickHandler(), vertexContextMenuHandler(), vertexTooltipHandler(), edgeContextHandler(), edgeToolTipHandler());
    }


    private void setupPanningBehavior(final Element panElem) {
		D3Drag d3Pan = D3.getDragBehavior();
		d3Pan.on(D3Events.DRAG_START.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				m_panObject = new PanObject(m_svgViewPort, m_svg);
			}
		});
		
		d3Pan.on(D3Events.DRAG.event(), new Handler<Object>() {

			public void call(Object t, int index) {
			    m_panObject.move();
			}
		});
		
		d3Pan.on(D3Events.DRAG_END.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				m_panObject = null;
			}
		});
		
		D3 select = D3.d3().select(panElem);
        select.call(d3Pan);
	}

	private void drawGraph(GWTGraph g, boolean now) {
	    
	    if(now) {  
	        m_graphDrawerNoTransition.updateGraph(g);
	    }else {
	        m_graphDrawer.updateGraph(g);
	    }
	    
	    //TODO: working here
	    SVGRect bbox = getSVGElement().getBBox();
        SVGGElement map = m_svgViewPort.cast();
        SVGRect mapBbox = map.getBBox();
        double referenceScale = 0.4;
        int x = bbox.getX();
        int y = bbox.getY();
        int width = (int) (mapBbox.getWidth() * referenceScale);
        int height = (int) (mapBbox.getHeight() * referenceScale);
       
        int viewPortWidth = (int) (m_svg.getOffsetWidth() * referenceScale);
        int viewPortHeight = (int) (m_svg.getOffsetHeight() * referenceScale);
        
        m_referenceMapViewport.setAttribute("width", "" + viewPortWidth);
        m_referenceMapViewport.setAttribute("height", "" + viewPortHeight);
        
        m_referenceMap.setAttribute("transform", "translate(" + (m_svg.getOffsetWidth() - width) + " " + (m_svg.getOffsetHeight() - height) + ")");
        
	    
	    //TODO: Fix this calc
	    
        m_scaledMap.setAttribute("viewBox", x + " " + y + " " + mapBbox.getWidth() + " " + mapBbox.getHeight());
        
	    
	}

    @Override
    public void onBrowserEvent(Event event) {
    	super.onBrowserEvent(event);
    
    	switch(DOM.eventGetType(event)) {
    	case Event.ONCONTEXTMENU:
    		EventTarget target = event.getEventTarget();

    		Element svg = this.getElement().getElementsByTagName("svg").getItem(0);

    		if (target.equals(svg)) {
    			m_client.getContextMenu().showAt(this, event.getClientX(), event.getClientY());
    			event.preventDefault();
    			event.stopPropagation();
    		}
    		break;

    	case Event.ONMOUSEDOWN:
    		
    		break;
    	
    	case Event.ONMOUSEWHEEL:
    	    double delta = event.getMouseWheelVelocityY() / 30.0;
    	    double oldScale = m_scale;
    	    final double newScale = oldScale + delta;
    	    final int clientX = event.getClientX();
    	    final int clientY = event.getClientY();
    	    //broken now need to fix it
//    	    Command cmd = new Command() {
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
    	}


    }
    
    private Handler<GWTVertex> vertexContextMenuHandler() {
        return new D3Events.Handler<GWTVertex>() {

            public void call(final GWTVertex vertex, int index) {
            	
            	ActionOwner owner = new ActionOwner() {

        			public Action[] getActions() {
        				return VTopologyComponent.this.getActions(vertex.getId(), vertex.getActionKeys());
        			}

        			public ApplicationConnection getClient() {
        				return VTopologyComponent.this.getClient();
        			}

        			public String getPaintableId() {
        				return VTopologyComponent.this.getPaintableId();
        			}
            		
            	};
            	
            	m_client.getContextMenu().showAt(owner, D3.getEvent().getClientX(), D3.getEvent().getClientY());
                D3.eventPreventDefault();
            }
        };
    }
    
    private Handler<GWTEdge> edgeContextHandler(){
        return new D3Events.Handler<GWTEdge>() {

            public void call(final GWTEdge edge, int index) {
                
                ActionOwner owner = new ActionOwner() {

                    public Action[] getActions() {
                        return VTopologyComponent.this.getActions(edge.getId(), edge.getActionKeys());
                    }

                    public ApplicationConnection getClient() {
                        return VTopologyComponent.this.getClient();
                    }

                    public String getPaintableId() {
                        return VTopologyComponent.this.getPaintableId();
                    }
                    
                };
                
                m_client.getContextMenu().showAt(owner, D3.getEvent().getClientX(), D3.getEvent().getClientY());
                D3.eventPreventDefault();
                
            }
        };
    }
    
    private Handler<GWTVertex> vertexTooltipHandler() {
        return new Handler<GWTVertex>() {

            public void call(GWTVertex t, int index) {
                if(m_client != null) {
                    Event event = (Event) D3.getEvent();
                    m_client.handleTooltipEvent(event, VTopologyComponent.this, t);
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        };
    }
    
    private Handler<GWTEdge> edgeToolTipHandler(){
        return new Handler<GWTEdge>() {

            public void call(GWTEdge edge, int index) {
                if(m_client != null) {
                    Event event = D3.getEvent().cast();
                    m_client.handleTooltipEvent(event, VTopologyComponent.this, edge);
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
            
        };
    }
    
    
    private final ActionOwner getActionOwner() {
    	return this;
    }

    private Handler<GWTVertex> vertexClickHandler() {
        return new D3Events.Handler<GWTVertex>(){

        	public void call(GWTVertex vertex, int index) {
        		m_client.updateVariable(m_paintableId, "clickedVertex", vertex.getId(), false);
        		m_client.updateVariable(m_paintableId, "shiftKeyPressed", D3.getEvent().getShiftKey(), false);
        		
        		m_client.sendPendingVariableChanges();
        	}
        };
    }
    
    private Handler<GWTVertex> vertexDragEndHandler() {
        return new Handler<GWTVertex>() {

            public void call(GWTVertex vertex, int index) {
                m_client.updateVariable(m_paintableId, "updatedVertex", "id," + vertex.getId() + "|x," + vertex.getX() + "|y," + vertex.getY() + "|selected,"+ vertex.isSelected(), true);
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }
            
        };
    }

    private Handler<GWTVertex> vertexDragStartHandler() {
        return new Handler<GWTVertex>() {

            public void call(GWTVertex vertex, int index) {
                NativeEvent event = D3.getEvent();
                Element draggableElement = Element.as(event.getEventTarget()).getParentElement();
                
                
                
                m_dragObject = new DragObject(draggableElement, m_svgViewPort);
                
                
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }

            
        };
    }
    
    public static final native void eval(JavaScriptObject elem) /*-{
        $wnd.console.log($wnd.eval(elem));
    }-*/;
    
    public static final native void typeof(Element elem) /*-{
        $wnd.console.log("typeof: " + typeof(elem));
    }-*/;
    
    private static final native void consoleLog(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    private Handler<GWTVertex> vertexDragHandler() {
        return new Handler<GWTVertex>() {

            public void call(GWTVertex vertex, int index) {
                
                vertex.setX( m_dragObject.getCurrentX() );
                vertex.setY( m_dragObject.getCurrentY() );
                
                m_dragObject.move();
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }
        };
    }
   
    
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
    	
        if(client.updateComponent(this, uidl, true)) {
            return;
        }
        
        m_client = client;
        m_paintableId = uidl.getId();
        
        
        setScale(uidl.getDoubleAttribute("scale"), uidl.getIntAttribute("clientX"), uidl.getIntAttribute("clientY"));
        setSemanticZoomLevel(uidl.getIntAttribute("semanticZoomLevel"));
        setActionKeys(uidl.getStringArrayAttribute("backgroundActions"));
        
        UIDL graph = uidl.getChildByTagName("graph");
        Iterator<?> children = graph.getChildIterator();
        
        GWTGraph graphConverted = GWTGraph.create();
        while(children.hasNext()) {
        	UIDL child = (UIDL) children.next();
        	
        	if(child.getTag().equals("group")) {
        		GWTGroup group = GWTGroup.create(child.getStringAttribute("key"), child.getIntAttribute("x"), child.getIntAttribute("y"));
        		boolean booleanAttribute = child.getBooleanAttribute("selected");
        		String[] actionKeys = child.getStringArrayAttribute("actionKeys");
        		
        		group.setActionKeys(actionKeys);
        		
				group.setSelected(booleanAttribute);
				group.setIcon(child.getStringAttribute("iconUrl"));
				group.setSemanticZoomLevel(child.getIntAttribute("semanticZoomLevel"));
				
				if (child.hasAttribute("label")) {
					group.setLabel(child.getStringAttribute("label"));
				}
				graphConverted.addGroup(group);
				
				if(m_client != null) {
				    TooltipInfo ttInfo = new TooltipInfo(group.getTooltipText());
				    m_client.registerTooltip(this, group, ttInfo);
				}
        		
        	}else if(child.getTag().equals("vertex")) {
        		
        		GWTVertex vertex = GWTVertex.create(child.getStringAttribute("key"), child.getIntAttribute("x"), child.getIntAttribute("y"));
        		boolean booleanAttribute = child.getBooleanAttribute("selected");
        		String[] actionKeys = child.getStringArrayAttribute("actionKeys");
        		vertex.setSemanticZoomLevel(child.getIntAttribute("semanticZoomLevel"));
        		
        		vertex.setActionKeys(actionKeys);
        		
        		if(child.hasAttribute("groupKey")) {
        			String groupKey = child.getStringAttribute("groupKey");
        			GWTGroup group = graphConverted.getGroup(groupKey);
        			vertex.setParent(group);
        		}
        		
				vertex.setSelected(booleanAttribute);
				vertex.setIcon(child.getStringAttribute("iconUrl"));

				if (child.hasAttribute("label")) {
					vertex.setLabel(child.getStringAttribute("label"));
				}
				
				graphConverted.addVertex(vertex);
				
				if(m_client != null) {
				    TooltipInfo ttInfo = new TooltipInfo(vertex.getTooltipText());
				    m_client.registerTooltip(this, vertex, ttInfo);
				}
        		
        	}else if(child.getTag().equals("edge")) {
        		GWTVertex source = graphConverted.findVertexById(child.getStringAttribute("source"));
                GWTEdge edge = GWTEdge.create(child.getStringAttribute("key"), source, graphConverted.findVertexById( child.getStringAttribute("target") ));
        		String[] actionKeys = child.getStringArrayAttribute("actionKeys");
        		edge.setActionKeys(actionKeys);
        		graphConverted.addEdge(edge);
        		
        		if(m_client != null) {
        		    TooltipInfo edgeInfo = new TooltipInfo("Edge: " + edge.getId());
        		    m_client.registerTooltip(this, edge, edgeInfo);
        		}
        	}else if(child.getTag().equals("groupParent")) {
        		String groupKey = child.getStringAttribute("key");
        		String parentKey = child.getStringAttribute("parentKey");
        		GWTGroup group = graphConverted.getGroup(groupKey);
        		GWTGroup parentGroup = graphConverted.getGroup(parentKey);
        		
        		group.setParent(parentGroup);
        	}
        	
        }
        
        UIDL actions = uidl.getChildByTagName("actions");
        if (actions != null) {
        	updateActionMap(actions);
        }
        
        setGraph(graphConverted);
        
    }
    
    private void setSemanticZoomLevel(int level) {
    	m_oldSemanticZoomLevel = m_semanticZoomLevel;
		m_semanticZoomLevel = level;
	}

	private void updateActionMap(UIDL c) {
        final Iterator<?> it = c.getChildIterator();
        while (it.hasNext()) {
            final UIDL action = (UIDL) it.next();
            final String key = action.getStringAttribute("key");
            final String caption = action.getStringAttribute("caption");
            m_actionMap.put(key + "_c", caption);
            if (action.hasAttribute("icon")) {
                // TODO need some uri handling ??
                m_actionMap.put(key + "_i", m_client.translateVaadinUri(action
                        .getStringAttribute("icon")));
            } else {
                m_actionMap.remove(key + "_i");
            }
        }

    }

    
	private void setActionKeys(String[] actions) {
		m_actionKeys = actions;
		
	}

	private void setScale(double scale, int clientX, int clientY) {
		if(m_scale != scale) {
		    double oldScale = m_scale;
			m_scale = scale;
		    repaintScale(oldScale, clientX, clientY);
		}
		
	}
	
    private void repaintScale(double oldScale, int clientX, int clientY) {
		updateScale(oldScale, m_scale, getSVGElement(), clientX, clientY);
	}

	private void setGraph(GWTGraph graph) {
		m_graph = graph;
		repaintGraph();
	}

	private void repaintGraph() {
        drawGraph(m_graph, false);
	}
	
	public void repaintGraphNow() {
        drawGraph(m_graph, true);
    }

	private void updateScale(double oldScale, double newScale) {
	    SVGElement svg = getSVGElement();
	    int cx = svg.getClientWidth()/2;
        int cy = svg.getClientWidth()/2;
	    
	    updateScale(oldScale, newScale, svg, cx, cy);
	}

    private void updateScale(double oldScale, double newScale, SVGElement svg,int cx, int cy) {
        
        double zoomFactor = newScale/oldScale;
	    // (x in new coord system - x in old coord system)/x coordinate
	    SVGGElement g = m_svgViewPort.cast();
	    
	    if(cx == 0 ) {
	        cx = (int) (Math.ceil(svg.getOffsetWidth() / 2.0) - 1);
	    }
	    
	    if(cy == 0) {
	        cy = (int) (Math.ceil(svg.getOffsetHeight() / 2.0) -1);
	    }
	    
	    SVGPoint p = svg.createSVGPoint();
	    p.setX(cx);
	    p.setY(cy);
	    p = p.matrixTransform(g.getCTM().inverse());
	    
	    SVGMatrix m = svg.createSVGMatrix()
	        .translate(p.getX(),p.getY())
	        .scale(zoomFactor)
	        .translate(-p.getX(), -p.getY());
	    SVGMatrix ctm = g.getCTM().multiply(m);
	    
	    consoleLog("zoomFactor: " + zoomFactor + " oldScale: " + oldScale + " newScale:" + newScale);
		D3.d3().select(m_svgViewPort).transition().duration(1000).attr("transform", matrixTransform(ctm));

    }
	
	private SVGPoint getPoint(int x, int y) {
	    SVGPoint p = getSVGElement().createSVGPoint();
	    p.setX(x);
	    p.setY(y);
	    return p;
	}

    private SVGElement getSVGElement() {
        return m_svg.cast();
    }
	
	private void setCTM(SVGGElement elem, SVGMatrix matrix) {
        elem.setAttribute("transform", matrixTransform(matrix));
    }

    private String matrixTransform(SVGMatrix matrix) {
        return "matrix(" + matrix.getA() +
                ", " + matrix.getB() +
                ", " + matrix.getC() + 
                ", " + matrix.getD() +
                ", " + matrix.getE() + 
                ", " + matrix.getF() + ")";
    }

    public String[] getActionKeys() {
		return m_actionKeys;
	}
	
	public Action[] getActions() {
		return getActions(null, getActionKeys());
	}


	public Action[] getActions(String target, String[] actionKeys) {
		if(actionKeys == null) {
			return new Action[] {};
		}
		final Action[] actions = new Action[actionKeys.length];
		for(int i = 0; i < actions.length; i++) {
			String actionKey = actionKeys[i];

			GraphAction a = new GraphAction(this, target, actionKey);

			a.setCaption(m_actionMap.get(actionKey + "_c"));

			if (m_actionMap.containsKey(actionKey+"_i")) {
				a.setIconUrl(m_actionMap.get(actionKey+"_i"));
			}
			
			actions[i] = a;
			
		}
		
		return actions;
	}

	public ApplicationConnection getClient() {
		return m_client;
	}

	public String getPaintableId() {
		return m_paintableId;
	}

    public VDropHandler getDropHandler() {
        
        return null;
    }



}
