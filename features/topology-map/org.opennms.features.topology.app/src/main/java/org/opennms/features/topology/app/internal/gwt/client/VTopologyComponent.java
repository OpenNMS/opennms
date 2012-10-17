/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.gwt.client;

import static org.opennms.features.topology.app.internal.gwt.client.d3.TransitionBuilder.fadeIn;
import static org.opennms.features.topology.app.internal.gwt.client.d3.TransitionBuilder.fadeOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.app.internal.gwt.client.d3.AnonymousFunc;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Drag;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.d3.Func;
import org.opennms.features.topology.app.internal.gwt.client.handler.DragHandlerManager;
import org.opennms.features.topology.app.internal.gwt.client.handler.DragObject;
import org.opennms.features.topology.app.internal.gwt.client.handler.MarqueeSelectHandler;
import org.opennms.features.topology.app.internal.gwt.client.handler.PanHandler;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.svg.BoundingRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.touch.client.Point;
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

public class VTopologyComponent extends Composite implements Paintable, ActionOwner, SVGTopologyMap {
    
	public class GraphDrawerNoTransition extends GraphDrawer{

		public GraphDrawerNoTransition(GWTGraph graph, Element vertexGroup,Element edgeGroup, D3Behavior dragBehavior, Handler<GWTVertex> clickHandler, Handler<GWTVertex> contextMenuHandler, Handler<GWTVertex> tooltipHandler, Handler<GWTEdge> edgeContextHandler, Handler<GWTEdge> edgeToolTipHandler, Handler<GWTEdge> edgeClickHandler) {
			super(graph, vertexGroup, edgeGroup, dragBehavior, clickHandler,contextMenuHandler, tooltipHandler, edgeContextHandler, edgeToolTipHandler, edgeClickHandler);
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
		Handler<GWTEdge> m_edgeClickHandler;
        Handler<GWTVertex> m_contextMenuHandler;
		private Handler<GWTVertex> m_vertexMouseOverHandler;
		private Handler<GWTEdge> m_edgeContextHandler;
		private Handler<GWTEdge> m_edgeToolTipHandler;


		public GraphDrawer(GWTGraph graph, Element vertexGroup, Element edgeGroup, D3Behavior dragBehavior, Handler<GWTVertex> clickHandler, Handler<GWTVertex> contextMenuHandler, Handler<GWTVertex> tooltipHandler, Handler<GWTEdge> edgeContextHandler, Handler<GWTEdge> edgeToolTipHandler, Handler<GWTEdge> edgeClickHandler) {
			m_graph = graph;
			m_vertexGroup = vertexGroup;
			m_edgeGroup = edgeGroup;
			m_dragBehavior = dragBehavior;
			setClickHandler(clickHandler);
			m_edgeClickHandler = edgeClickHandler;
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
		
		public Handler<GWTEdge> getEdgeClickHandler() {
            return m_edgeClickHandler;
        }

        public void setEdgeClickHandler(Handler<GWTEdge> edgeClickHandler) {
            m_edgeClickHandler = edgeClickHandler;
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
					return selection.on(D3Events.CLICK.event(), getEdgeClickHandler())
					        .on(D3Events.CONTEXT_MENU.event(), getEdgeContextHandler())
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
			return getVertexGroup().selectAll(GWTVertex.VERTEX_CLASS_NAME)
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

	private static VTopologyComponentUiBinder uiBinder = GWT
			.create(VTopologyComponentUiBinder.class);
	
	interface VTopologyComponentUiBinder extends
	UiBinder<Widget, VTopologyComponent> {
	}

	protected static final String VERTEX_CSS_CLASS = ".vertex";
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

//	@UiField
//	Element m_scaledMap;

	@UiField
	Element m_referenceMap;

	@UiField
	Element m_referenceMapViewport;

	@UiField
	Element m_referenceMapBorder;
	
	@UiField
	Element m_marquee;
	
	/**
	 * This map contains captions and icon urls for actions like: * "33_c" ->
	 * "Edit" * "33_i" -> "http://dom.com/edit.png"
	 */
	private final HashMap<String, String> m_actionMap = new HashMap<String, String>();


	private String[] m_actionKeys;

	private D3Drag m_d3PanDrag;
	private GraphDrawer m_graphDrawer;
	private GraphDrawerNoTransition m_graphDrawerNoTransition;
	private List<Element> m_selectedElements = new ArrayList<Element>();
	private int m_semanticZoomLevel;
	private int m_oldSemanticZoomLevel;
	private DragHandlerManager m_svgDragHandlerManager;
    private boolean m_panToSelection = false;
    private boolean m_fitToView = false;

	public VTopologyComponent() {
		initWidget(uiBinder.createAndBindUi(this));

		m_graph = GWTGraph.create();
	}

    @Override
	protected void onLoad() {
		super.onLoad();
		
		sinkEvents(Event.ONCONTEXTMENU | VTooltip.TOOLTIP_EVENTS | Event.ONMOUSEWHEEL);
		
		m_svgDragHandlerManager = new DragHandlerManager();
		m_svgDragHandlerManager.addDragBehaviorHandler(PanHandler.DRAG_BEHAVIOR_KEY, new PanHandler(this));
		m_svgDragHandlerManager.addDragBehaviorHandler(MarqueeSelectHandler.DRAG_BEHAVIOR_KEY, new MarqueeSelectHandler(this));
		m_svgDragHandlerManager.setCurrentDragHandler(PanHandler.DRAG_BEHAVIOR_KEY);
		setupDragBehavior(m_svg, m_svgDragHandlerManager);
		
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

		m_graphDrawer = new GraphDrawer(m_graph, m_vertexGroup, m_edgeGroup, dragBehavior, vertexClickHandler(), vertexContextMenuHandler(), vertexTooltipHandler(), edgeContextHandler(), edgeToolTipHandler(), edgeClickHandler());
		m_graphDrawerNoTransition = new GraphDrawerNoTransition(m_graph, m_vertexGroup, m_edgeGroup, dragBehavior, vertexClickHandler(), vertexContextMenuHandler(), vertexTooltipHandler(), edgeContextHandler(), edgeToolTipHandler(), edgeClickHandler());
	}


	private void setupDragBehavior(final Element panElem, final DragHandlerManager handlerManager) {
	    
		D3Drag d3Pan = D3.getDragBehavior();
		d3Pan.on(D3Events.DRAG_START.event(), new Handler<Element>() {

			public void call(Element elem, int index) {
			    handlerManager.onDragStart(elem);
			}
		});

		d3Pan.on(D3Events.DRAG.event(), new Handler<Element>() {

			public void call(Element elem, int index) {
			    handlerManager.onDrag(elem);
			}
		});

		d3Pan.on(D3Events.DRAG_END.event(), new Handler<Element>() {

			public void call(Element elem, int index) {
			    handlerManager.onDragEnd(elem);
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
		
		final D3 selectedVertices = D3.d3().selectAll(GWTVertex.SELECTED_VERTEX_CLASS_NAME);
		selectedVertices.each(new Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex gwtVertex, int index) {
                SVGGElement vertex = D3.getElement(selectedVertices, index).cast();
                vertex.getParentElement().appendChild(vertex);
            }
        });
		
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);

		switch(DOM.eventGetType(event)) {
    		case Event.ONCONTEXTMENU:
    			EventTarget target = event.getEventTarget();
    
    			Element svg = this.getElement().getElementsByTagName("svg").getItem(0);
    
    			if (target.equals(svg)) {
    				showContextMenu(null, event.getClientX(), event.getClientY(), "map");
    				//m_client.getContextMenu().showAt(this, event.getClientX(), event.getClientY());
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
    			
    		case Event.ONCLICK:
    		    if(event.getEventTarget().equals(m_svg)) {
    		        deselectVertices(true);
    		    }
    		    break;
		}


	}

	private void deselectVertices(boolean immediate) {
	    m_client.updateVariable(m_paintableId, "clickedVertex", "", false);
        m_client.updateVariable(m_paintableId, "shiftKeyPressed", false, false);
	    if(immediate) {
	        m_client.sendPendingVariableChanges();
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

				showContextMenu(vertex.getId(), D3.getEvent().getClientX(), D3.getEvent().getClientY(), "vertex");
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

				showContextMenu(edge.getId(), D3.getEvent().getClientX(), D3.getEvent().getClientY(), "edge");
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
	
	private Handler<GWTEdge> edgeClickHandler(){
	    return new Handler<GWTEdge>() {

            @Override
            public void call(GWTEdge edge, int index) {
                m_client.updateVariable(m_paintableId, "clickedEdge", edge.getId(), true);
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
			    
			    final List<String> values = new ArrayList<String>();
			    final String[] vertexIds = m_dragObject.getDraggedVertices();
			    D3.d3().selectAll(GWTVertex.VERTEX_CLASS_NAME).each(new Handler<GWTVertex>() {

                    @Override
                    public void call(GWTVertex vertex, int index) {
                        for(String id : vertexIds) {
                            if(vertex.getId().equals(id)) {
                                values.add("id," + vertex.getId() + "|x," + vertex.getX() + "|y," + vertex.getY() + "|selected,"+ vertex.isSelected());
                            }
                        }
                    }
                });
			    
			    if(m_dragObject.getDraggableElement().getAttribute("class").equals("vertex")) {
			        if(!D3.getEvent().getShiftKey()) {
			            deselectVertices(false);
			        }
			    }
			    
			    m_client.updateVariable(getPaintableId(), "updateVertices", values.toArray(new String[] {}), false);
			    m_client.sendPendingVariableChanges();
			    
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
				D3 selection = null;
				
				boolean isSelected = draggableElement.getAttribute("class").equals("vertex selected");
				
				if(isSelected) {
				    selection = D3.d3().selectAll(GWTVertex.SELECTED_VERTEX_CLASS_NAME);
				}else {
				    selection = D3.d3().select(Element.as(event.getEventTarget()).getParentElement());
				}
				
				m_dragObject = new DragObject(VTopologyComponent.this, draggableElement, getSVGViewPort(), selection);
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

	private static final native void consoleLog(Object message) /*-{
        $wnd.console.log(message);
    }-*/;

	private Handler<GWTVertex> vertexDragHandler() {
		return new Handler<GWTVertex>() {

			public void call(GWTVertex vertex, int index) {

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
		setPanToSelection(uidl.getBooleanAttribute("panToSelection"));
		setFitToView(uidl.getBooleanAttribute("fitToView"));
		setActiveTool(uidl.getStringAttribute("activeTool"));

		UIDL graph = uidl.getChildByTagName("graph");
		Iterator<?> children = graph.getChildIterator();

		GWTGraph graphConverted = GWTGraph.create();
		GWTVertex.setBackgroundImage(client.translateVaadinUri("theme://images/vertex_circle_selector.png"));
		while(children.hasNext()) {
			UIDL child = (UIDL) children.next();

			if(child.getTag().equals("group")) {
				GWTGroup group = GWTGroup.create(child.getStringAttribute("key"), child.getIntAttribute("x"), child.getIntAttribute("y"));
				boolean booleanAttribute = child.getBooleanAttribute("selected");
				String[] actionKeys = child.getStringArrayAttribute("actionKeys");

				group.setActionKeys(actionKeys);

				group.setSelected(booleanAttribute);
				group.setIcon(client.translateVaadinUri(child.getStringAttribute("iconUrl")));
				group.setSemanticZoomLevel(child.getIntAttribute("semanticZoomLevel"));

				if (child.hasAttribute("label")) {
					group.setLabel(child.getStringAttribute("label"));
				}
				graphConverted.addGroup(group);

				if(m_client != null) {
					TooltipInfo ttInfo = new TooltipInfo(child.getStringAttribute("tooltipText"));
					m_client.registerTooltip(this, group, ttInfo);
				}

			}else if(child.getTag().equals("vertex")) {

				GWTVertex vertex = GWTVertex.create(child.getStringAttribute("key"), child.getIntAttribute("x"), child.getIntAttribute("y"));
				boolean selected = child.getBooleanAttribute("selected");
				String[] actionKeys = child.getStringArrayAttribute("actionKeys");
				vertex.setSemanticZoomLevel(child.getIntAttribute("semanticZoomLevel"));

				vertex.setActionKeys(actionKeys);

				if(child.hasAttribute("groupKey")) {
					String groupKey = child.getStringAttribute("groupKey");
					GWTGroup group = graphConverted.getGroup(groupKey);
					vertex.setParent(group);
				}

				vertex.setSelected(selected);
				vertex.setIcon(client.translateVaadinUri(child.getStringAttribute("iconUrl")));

				if (child.hasAttribute("label")) {
					vertex.setLabel(child.getStringAttribute("label"));
				}

				graphConverted.addVertex(vertex);

				if(m_client != null) {
					TooltipInfo ttInfo = new TooltipInfo(child.getStringAttribute("tooltipText"));
					m_client.registerTooltip(this, vertex, ttInfo);
				}
				
			}else if(child.getTag().equals("edge")) {
				GWTVertex source = graphConverted.findVertexById(child.getStringAttribute("source"));
				GWTEdge edge = GWTEdge.create(child.getStringAttribute("key"), source, graphConverted.findVertexById( child.getStringAttribute("target") ));
				boolean selected = child.getBooleanAttribute("selected");
				String cssClass = child.getStringAttribute("cssClass");
				String[] actionKeys = child.getStringArrayAttribute("actionKeys");
				edge.setActionKeys(actionKeys);
				edge.setSelected(selected);
				edge.setCssClass(cssClass);
				graphConverted.addEdge(edge);

				if(m_client != null) {
					TooltipInfo edgeInfo = new TooltipInfo(child.getStringAttribute("tooltipText"));
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

	private void setActiveTool(String toolname) {
	    if(toolname.equals("pan")) {
	        m_svgDragHandlerManager.setCurrentDragHandler(PanHandler.DRAG_BEHAVIOR_KEY);
	        getSVGElement().getStyle().setCursor(Cursor.MOVE);
	    }else if(toolname.equals("select")) {
	        m_svgDragHandlerManager.setCurrentDragHandler(MarqueeSelectHandler.DRAG_BEHAVIOR_KEY);
	        getSVGElement().getStyle().setCursor(Cursor.CROSSHAIR);
	    }
    }

    private void setFitToView(boolean fitToView) {
	    m_fitToView  = fitToView;
    }
	
	private boolean isFitToView() {
	    return m_fitToView;
	}

    private void setPanToSelection(boolean bool) {
        m_panToSelection = bool;
    }
	
	private boolean isPanToSelection() {
	    return m_panToSelection;
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
		
		if(isPanToSelection()) {
		    repaintGraph();
		    centerSelection(m_graph.getVertices(m_semanticZoomLevel));
		} else if(isFitToView()) {
		    repaintGraph();
		    fitMapToView(m_graph.getVertices(m_semanticZoomLevel));
		}else {
		    repaintGraph();
		}
		
	}

    private void repaintGraph() {
		drawGraph(m_graph, false);
	}

	public void repaintGraphNow() {
		drawGraph(m_graph, true);
	}
	
	@Override
	public void repaintNow() {
	    repaintGraphNow();
	}

	private void updateScale(double oldScale, double newScale, SVGElement svg,int cx, int cy) {

		double zoomFactor = newScale/oldScale;
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
		p = p.matrixTransform(g.getCTM().inverse());
		SVGMatrix m = svg.createSVGMatrix()
				.translate(p.getX(),p.getY())
				 .scale(zoomFactor)
				.translate(-p.getX(), -p.getY());
		SVGMatrix ctm = g.getCTM().multiply(m);
		D3.d3().select(m_svgViewPort).transition().duration(1000).attr("transform", matrixTransform(ctm));

	}

	private SVGPoint getPoint(int x, int y) {
		SVGPoint p = getSVGElement().createSVGPoint();
		p.setX(x);
		p.setY(y);
		return p;
	}

	@Override
	public SVGElement getSVGElement() {
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

	public void showContextMenu(Object target, int x, int y, String type) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("target", target);
		map.put("x", x);
		map.put("y", y);
		map.put("type", type);

		m_client.updateVariable(getPaintableId(), "contextMenu", map, true);
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
    public SVGGElement getSVGViewPort() {
        return m_svgViewPort.cast();
    }

    @Override
    public void setVertexSelection(List<String> vertIds) {
        m_client.updateVariable(getPaintableId(), "marqueeSelection", vertIds.toArray(new String[]{}), false);
        m_client.updateVariable(m_paintableId, "shiftKeyPressed", D3.getEvent().getShiftKey(), false);
        
        m_client.sendPendingVariableChanges();
    }

    @Override
    public Element getMarqueeElement() {
        return m_marquee;
    }

    /**
     * Returns the D3 selection for all Vertex svg elements
     */
    @Override
    public D3 selectAllVertexElements() {
        return D3.d3().selectAll(GWTVertex.VERTEX_CLASS_NAME);
    }
    
    /**
     * Centers the view on a selection
     * @param jsArray 
     */
    public void centerSelection(JsArray<GWTVertex> vertexArray) {
        centerD3Selection(vertexArray, false);
    }
    
    /**
     * Centers the view for the entire map
     * @param vertexArray
     */
    private void fitMapToView(JsArray<GWTVertex> vertexArray) {
        centerD3Selection(vertexArray, true);
    }
    
    private void zoomToFit(final BoundingRect rect) {
        
        SVGElement svg = getSVGElement();
        final int svgWidth = svg.getParentElement().getOffsetWidth(); 
        final int svgHeight = svg.getParentElement().getOffsetHeight();
        
        double svgCenterX = svgWidth/2;
        double svgCenterY = svgHeight/2;
        
        double translateX = (svgCenterX - rect.getCenterX());
        double translateY = (svgCenterY - rect.getCenterY());
        
      //transform="translate( -centerX*(factor-1), -centerY*(factor-1) ) scale(factor)
        final double scale = Math.min(svgWidth/(double)rect.getWidth(), svgHeight/(double)rect.getHeight());
        SVGMatrix transform = svg.createSVGMatrix()
            .translate(translateX, translateY)
            .translate(-rect.getCenterX()*(scale-1), -rect.getCenterY()*(scale-1)) 
            .scale(scale)
            ;
                   
        String transformVal = matrixTransform(transform);
        
        final double strokeWidth = 5 * (1/scale);
        //D3.d3().selectAll("line").style("opacity", "1").transition().duration(2000).style("stroke-width", "" + strokeWidth);
        
        D3.d3().select(getSVGViewPort()).transition().duration(2000).attr("transform", transformVal).each("end", new AnonymousFunc() {
            
            @Override
            public void call() {
                setMapScaleNow(scale);
                D3.d3().selectAll("line").style("opacity", "1").transition().duration(2000).style("stroke-width", "" + strokeWidth);
            }
        });
        
    }
    
    private void centerD3Selection(JsArray<GWTVertex> vertices, boolean fitToView) {
        
        final BoundingRect rect = new BoundingRect();

        for(int i = 0; i < vertices.length(); i++) {
            GWTVertex vertex = vertices.get(i);
            
            if(fitToView || vertex.isSelected()) {
                double vertexX = vertex.getX();
                double vertexY = vertex.getY();
                rect.addPoint(new Point(vertexX, vertexY));
            }
        }
        
        zoomToFit(rect);
    }
    
    private void setMapScaleNow(double scale) {
        setMapScale(scale, true);
    }
    
    private void setMapScale(double scale, boolean immediate) {
        m_scale = scale;
        m_client.updateVariable(m_paintableId, "mapScale", scale, immediate);
    }

}
