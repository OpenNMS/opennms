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

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Behavior;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Drag;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.d3.Func;
import org.opennms.features.topology.app.internal.gwt.client.d3.Tween;
import org.opennms.features.topology.app.internal.gwt.client.handler.DragHandlerManager;
import org.opennms.features.topology.app.internal.gwt.client.handler.DragObject;
import org.opennms.features.topology.app.internal.gwt.client.handler.MarqueeSelectHandler;
import org.opennms.features.topology.app.internal.gwt.client.handler.PanHandler;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.service.ServiceRegistry;
import org.opennms.features.topology.app.internal.gwt.client.service.support.DefaultServiceRegistry;
import org.opennms.features.topology.app.internal.gwt.client.svg.BoundingRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.touch.client.Point;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.TooltipInfo;
import com.vaadin.terminal.gwt.client.UIDL;

public class VTopologyComponent extends Composite implements Paintable, SVGTopologyMap, TopologyView.Presenter<TopologyViewRenderer> {
    
    public interface TopologyViewRenderer{
        void updateGraph(GWTGraph graph);
        void draw(GWTGraph graph, TopologyView<TopologyViewRenderer> topologyView);
    }
    
    public interface GraphUpdateListener{
        void onGraphUpdated(GWTGraph graph);
    }
    
	public class SVGGraphDrawerNoTransition extends SVGGraphDrawer{

		public SVGGraphDrawerNoTransition(D3Behavior dragBehavior, ServiceRegistry serviceRegistry) {
			super(dragBehavior, serviceRegistry);
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

	public class SVGGraphDrawer implements TopologyViewRenderer{
		GWTGraph m_graph;
		Element m_edgeGroup;
		D3Behavior m_dragBehavior;
		Handler<GWTVertex> m_clickHandler;
		Handler<GWTEdge> m_edgeClickHandler;
        Handler<GWTVertex> m_contextMenuHandler;
		private Handler<GWTVertex> m_vertexTooltipHandler;
		private Handler<GWTEdge> m_edgeContextHandler;
		private Handler<GWTEdge> m_edgeToolTipHandler;


		public SVGGraphDrawer(D3Behavior dragBehavior, ServiceRegistry serviceRegistry) {
			m_dragBehavior = dragBehavior;
			
			m_clickHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexClick)");
			m_edgeClickHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=edgeClick)");
			
			m_contextMenuHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexContextMenu)");
			m_vertexTooltipHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexTooltip)");
			
			m_edgeContextHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=edgeContextMenu)");
			m_edgeToolTipHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=edgeTooltip)");
			
		}

		public void updateGraph(GWTGraph graph) {
			m_graph = graph;
			draw(null, null);
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

		public D3Behavior getDragBehavior() {
			return m_dragBehavior;
		}

		public void draw(GWTGraph graph, final TopologyView<TopologyViewRenderer> topologyView) {
			D3 edgeSelection = getEdgeSelection(graph, topologyView);

			D3 vertexSelection = getVertexSelection(graph, topologyView);

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
			edgeSelection.with(updateTransition()).call(GWTEdge.draw()).attr("opacity", 1)
			    .transition().styleTween("stroke-width", edgeStrokeWidthTween(topologyView));

			vertexSelection.with(updateTransition()).call(GWTVertex.draw()).attr("opacity", 1);


			//Enters
			edgeSelection.enter().create(GWTEdge.create()).call(setupEdgeEventHandlers()).with(enterTransition())
			    .transition().styleTween("stroke-width", edgeStrokeWidthTween(topologyView));

			//vertexSelection.enter().create(GWTVertex.create()).call(setupEventHandlers()).with(enterTransition());
			
		}

        private Tween<Double, GWTEdge> edgeStrokeWidthTween(final TopologyView<TopologyViewRenderer> topologyView) {
            return new Tween<Double, GWTEdge>() {

                @Override
                public Double call(GWTEdge edge, int index, String a) {
                    D3 viewPort = D3.d3().select(topologyView.getSVGViewPort());
                    double scale = D3.getTransform(viewPort.attr("transform")).getScale().get(0);
                    final double strokeWidth = 5 * (1/scale);
                    return strokeWidth;
                }
                
            };
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
			return m_vertexTooltipHandler;
		}

		private D3 getVertexSelection(GWTGraph graph, TopologyView<TopologyViewRenderer> topologyView) {
			D3 vertexGroup = D3.d3().select( topologyView.getVertexGroup() );
            return vertexGroup.selectAll(GWTVertex.VERTEX_CLASS_NAME)
					.data(graph.getVertices(m_semanticZoomLevel), new Func<String, GWTVertex>() {

						public String call(GWTVertex param, int index) {
							return "" + param.getId();
						}

					});
		}

		private D3 getEdgeSelection(GWTGraph graph, TopologyView<TopologyViewRenderer> topologyView) {
			D3 edgeGroup = D3.d3().select(topologyView.getEdgeGroup());
            return edgeGroup.selectAll(GWTEdge.SVG_EDGE_ELEMENT)
					.data(graph.getEdges(m_semanticZoomLevel), new Func<String, GWTEdge>() {

						public String call(GWTEdge edge, int index) {
						    if(m_client.getTooltipTitleInfo(VTopologyComponent.this, edge) == null) {
						        m_client.registerTooltip(VTopologyComponent.this, edge, new TooltipInfo(edge.getTooltipText()));
						    }
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
	
	private static VTopologyComponentUiBinder uiBinder = GWT.create(VTopologyComponentUiBinder.class);
	
	interface VTopologyComponentUiBinder extends UiBinder<Widget, VTopologyComponent> {}

	private ApplicationConnection m_client;
	private String m_paintableId;

	private GWTGraph m_graph;
	private double m_scale = 0.0;
	private DragObject m_dragObject;
	
	@UiField
	FlowPanel m_componentHolder;

	private D3Drag m_d3PanDrag;
	private SVGGraphDrawer m_graphDrawer;
	private SVGGraphDrawerNoTransition m_graphDrawerNoTransition;
	private List<Element> m_selectedElements = new ArrayList<Element>();
	private int m_semanticZoomLevel;
	private int m_oldSemanticZoomLevel;
	private DragHandlerManager m_svgDragHandlerManager;
    private boolean m_panToSelection = false;
    private ServiceRegistry m_serviceRegistry;
    private TopologyViewRenderer m_currentViewRender;
    
    private TopologyView<TopologyViewRenderer> m_topologyView;
    private List<GraphUpdateListener> m_graphListenerList = new ArrayList<GraphUpdateListener>();
    private boolean m_fitToView;

	public VTopologyComponent() {
		initWidget(uiBinder.createAndBindUi(this));
		m_graph = GWTGraph.create();
	}

    @Override
	protected void onLoad() {
		super.onLoad();
		
		m_serviceRegistry = new DefaultServiceRegistry();
		m_serviceRegistry.register(vertexClickHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexClick"); }}, Handler.class);
		m_serviceRegistry.register(vertexContextMenuHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexContextMenu"); }}, Handler.class);
		m_serviceRegistry.register(vertexTooltipHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexTooltip"); }}, Handler.class);
		
		m_serviceRegistry.register(edgeContextHandler(), new HashMap<String, String>(){{ put("handlerType", "edgeContextMenu"); }}, Handler.class);
		m_serviceRegistry.register(edgeTooltipHandler(), new HashMap<String, String>(){{ put("handlerType", "edgeTooltip"); }}, Handler.class);
		m_serviceRegistry.register(edgeClickHandler(), new HashMap<String, String>(){{ put("handlerType", "edgeClick"); }}, Handler.class);
		
		
		m_topologyView = new TopologyViewImpl();
		m_topologyView.setPresenter(this);
		m_componentHolder.add(m_topologyView.asWidget());
		
		m_svgDragHandlerManager = new DragHandlerManager();
		m_svgDragHandlerManager.addDragBehaviorHandler(PanHandler.DRAG_BEHAVIOR_KEY, new PanHandler(m_topologyView, m_serviceRegistry));
		m_svgDragHandlerManager.addDragBehaviorHandler(MarqueeSelectHandler.DRAG_BEHAVIOR_KEY, new MarqueeSelectHandler(this, m_topologyView));
		m_svgDragHandlerManager.setCurrentDragHandler(PanHandler.DRAG_BEHAVIOR_KEY);
		setupDragBehavior(m_topologyView.getSVGElement(), m_svgDragHandlerManager);
		
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

		m_graphDrawer = new SVGGraphDrawer(dragBehavior, m_serviceRegistry);
		m_graphDrawerNoTransition = new SVGGraphDrawerNoTransition(dragBehavior, m_serviceRegistry);
		
		setTopologyViewRenderer(m_graphDrawer);
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

	private void deselectAllItems(boolean immediate) {
	    m_client.updateVariable(m_paintableId, "deselectAllItems", true, immediate);
    }

    private Handler<GWTVertex> vertexContextMenuHandler() {
		return new D3Events.Handler<GWTVertex>() {

			public void call(final GWTVertex vertex, int index) {

				showContextMenu(vertex.getId(), D3.getEvent().getClientX(), D3.getEvent().getClientY(), "vertex");
				D3.eventPreventDefault();
			}
		};
	}

	private Handler<GWTEdge> edgeContextHandler(){
		return new D3Events.Handler<GWTEdge>() {

			public void call(final GWTEdge edge, int index) {

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

	private Handler<GWTEdge> edgeTooltipHandler(){
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
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }
	        
	    };
	}

	private Handler<GWTVertex> vertexClickHandler() {
		return new D3Events.Handler<GWTVertex>(){

			public void call(GWTVertex vertex, int index) {
				NativeEvent event = D3.getEvent();
				m_client.updateVariable(m_paintableId, "clickedVertex", vertex.getId(), false);
				m_client.updateVariable(m_paintableId, "shiftKeyPressed", event.getShiftKey(), false);
				
				event.preventDefault();
				event.stopPropagation();

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
			        //if(!D3.getEvent().getShiftKey()) {
			        //    deselectAllItems(false);
			        //}
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
				
				m_dragObject = new DragObject(VTopologyComponent.this.m_topologyView, draggableElement, m_topologyView.getSVGViewPort(), selection);
				D3.getEvent().preventDefault();
				D3.getEvent().stopPropagation();
			}

		};
	}

	

	private Handler<GWTVertex> vertexDragHandler() {
		return new Handler<GWTVertex>() {

			public void call(GWTVertex vertex, int index) {

				m_dragObject.move();
				
				//TODO: change the viewRenderer to no transition
				if(getViewRenderer() == m_graphDrawer) {
				    m_currentViewRender = m_graphDrawerNoTransition;
				}
				
				for( GraphUpdateListener listener : m_graphListenerList) {
				    listener.onGraphUpdated(m_graph);
				}
				
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
				vertex.setSemanticZoomLevel(child.getIntAttribute("semanticZoomLevel"));

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
				edge.setSelected(selected);
				edge.setCssClass(cssClass);
				String ttText = child.getStringAttribute("tooltipText");
				edge.setTooltipText(ttText);
				graphConverted.addEdge(edge);

			}else if(child.getTag().equals("groupParent")) {
				String groupKey = child.getStringAttribute("key");
				String parentKey = child.getStringAttribute("parentKey");
				GWTGroup group = graphConverted.getGroup(groupKey);
				GWTGroup parentGroup = graphConverted.getGroup(parentKey);

				group.setParent(parentGroup);

			}else if(child.getTag().equals("vertexParent")) {
				String vertexKey = child.getStringAttribute("key");
				String parentKey = child.getStringAttribute("parentKey");
				GWTVertex vertex = graphConverted.getVertex(vertexKey);
				GWTGroup parentGroup = graphConverted.getGroup(parentKey);

				vertex.setParent(parentGroup);
			}
		}

		setGraph(graphConverted);
		
	}

	private void setFitToView(boolean bool) {
	    m_fitToView = bool;
    }
	
	private boolean isFitToView() {
	    return m_fitToView;
	}

    private void setActiveTool(String toolname) {
	    if(toolname.equals("pan")) {
	        m_svgDragHandlerManager.setCurrentDragHandler(PanHandler.DRAG_BEHAVIOR_KEY);
	        m_topologyView.getSVGElement().getStyle().setCursor(Cursor.MOVE);
	    }else if(toolname.equals("select")) {
	        m_svgDragHandlerManager.setCurrentDragHandler(MarqueeSelectHandler.DRAG_BEHAVIOR_KEY);
	        m_topologyView.getSVGElement().getStyle().setCursor(Cursor.CROSSHAIR);
	    }
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

	private void setScale(double scale, int clientX, int clientY) {
	    if(m_scale != scale) {
			double oldScale = m_scale;
			m_scale = scale;
			updateScale(oldScale, m_scale, clientX, clientY);
		}

	}

	/**
	 * Sets the graph, updates the ViewRenderer if need be and 
	 * updates all graphUpdateListeners
	 * @param graph
	 */
	private void setGraph(GWTGraph graph) {
		m_graph = graph;
		updateGraphUpdateListeners();
		if(isPanToSelection()) {
		    centerSelection(m_graph.getVertices(m_semanticZoomLevel));
		} else { // if(isFitToView())
		    fitMapToView(m_graph.getVertices(m_semanticZoomLevel));
		}
        
		//Set the ViewRenderer to the Animated one if it isn't already
		if(getViewRenderer() != m_graphDrawer) {
		    setTopologyViewRenderer(m_graphDrawer);
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

    private void updateScale(double oldScale, double newScale, int cx,int cy) {
        ((TopologyViewImpl) m_topologyView).updateScale(oldScale, newScale, cx, cy);

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
    public void setVertexSelection(List<String> vertIds) {
        m_client.updateVariable(getPaintableId(), "marqueeSelection", vertIds.toArray(new String[]{}), false);
        m_client.updateVariable(m_paintableId, "shiftKeyPressed", D3.getEvent().getShiftKey(), false);
        
        m_client.sendPendingVariableChanges();
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
        
        m_topologyView.zoomToFit(rect);
    }
    
    private void setMapScaleNow(double scale) {
        setMapScale(scale, true);
    }
    
    private void setMapScale(double scale, boolean immediate) {
        m_scale = scale;
        m_client.updateVariable(m_paintableId, "mapScale", scale, immediate);
    }

    @Override
    public TopologyViewRenderer getViewRenderer() {
        return m_currentViewRender;
    }
    
    private void setTopologyViewRenderer(TopologyViewRenderer viewRenderer) {
        m_currentViewRender = viewRenderer;
    }

    @Override
    public void onBackgroundClick() {
        m_client.updateVariable(m_paintableId, "clickedBackground", true, true);
    }

    @Override
    public void onContextMenu(Object target, int x, int y, String type) {
        showContextMenu(target, x, y, type);
    }

    @Override
    public void addGraphUpdateListener(GraphUpdateListener listener) {
        m_graphListenerList.add(listener);
    }
    
    private void updateGraphUpdateListeners() {
        for(GraphUpdateListener listener : m_graphListenerList) {
            listener.onGraphUpdated( m_graph );
        }
    }

    @Override
    public void onScaleUpdate(double scale) {
        setMapScaleNow(scale);
    }
    
    @Override
    public void onMouseWheel() {
        // TODO Auto-generated method stub
        
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

}
