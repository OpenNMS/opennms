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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.touch.client.Point;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.shared.MouseEventDetails;
import org.opennms.features.topology.app.internal.gwt.client.d3.*;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;
import org.opennms.features.topology.app.internal.gwt.client.handler.DragHandlerManager;
import org.opennms.features.topology.app.internal.gwt.client.handler.DragObject;
import org.opennms.features.topology.app.internal.gwt.client.handler.MarqueeSelectHandler;
import org.opennms.features.topology.app.internal.gwt.client.handler.PanHandler;
import org.opennms.features.topology.app.internal.gwt.client.map.SVGTopologyMap;
import org.opennms.features.topology.app.internal.gwt.client.service.ServiceRegistry;
import org.opennms.features.topology.app.internal.gwt.client.service.support.DefaultServiceRegistry;
import org.opennms.features.topology.app.internal.gwt.client.svg.BoundingRect;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opennms.features.topology.app.internal.gwt.client.d3.TransitionBuilder.fadeIn;
import static org.opennms.features.topology.app.internal.gwt.client.d3.TransitionBuilder.fadeOut;

public class VTopologyComponent extends Composite implements SVGTopologyMap, TopologyView.Presenter<VTopologyComponent.TopologyViewRenderer> {

    private HandlerRegistration m_windowResizeRegistration;

    public interface TopologyViewRenderer{
        void draw(GWTGraph graph, TopologyView<TopologyViewRenderer> topologyView, GWTBoundingBox oldBBox);
    }
    
    public interface GraphUpdateListener{
        void onGraphUpdated(GWTGraph graph, GWTBoundingBox oldBBox);
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
		private Handler<GWTVertex> m_dblClickHandler;
        Handler<GWTEdge> m_edgeClickHandler;
        Handler<GWTVertex> m_contextMenuHandler;
		private Handler<GWTVertex> m_vertexTooltipHandler;
		private Handler<GWTEdge> m_edgeContextHandler;
		private Handler<GWTEdge> m_edgeToolTipHandler;
        


		@SuppressWarnings("unchecked")
        public SVGGraphDrawer(D3Behavior dragBehavior, ServiceRegistry serviceRegistry) {
			m_dragBehavior = dragBehavior;
			
			m_clickHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexClick)");
			m_dblClickHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexDblClick)");
			m_edgeClickHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=edgeClick)");
			
			m_contextMenuHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexContextMenu)");
			m_vertexTooltipHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=vertexTooltip)");
			
			m_edgeContextHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=edgeContextMenu)");
			m_edgeToolTipHandler = serviceRegistry.findProvider(Handler.class, "(handlerType=edgeTooltip)");
			
		}

		public Handler<GWTVertex> getClickHandler() {
			return m_clickHandler;
		}
		
		public Handler<GWTVertex> getDblClickHandler() {
            return m_dblClickHandler;
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

                @Override
		public void draw(GWTGraph graph, final TopologyView<TopologyViewRenderer> topologyView, GWTBoundingBox oldBBox) {
			D3 edgeSelection = getEdgeSelection(graph, topologyView);

			D3 vertexSelection = getVertexSelection(graph, topologyView);
			
			vertexSelection.enter().create(GWTVertex.create()).call(setupEventHandlers())
			.attr("transform", new Func<String, GWTVertex>() {

                                @Override
				public String call(GWTVertex vertex, int index) {
					return "translate(" + vertex.getInitialX() + "," +  vertex.getInitialY() + ")";
				}

			}).attr("opacity", 1);

			
			//Exits
			edgeSelection.exit().remove();
			vertexSelection.exit().with(new D3Behavior() {

				@Override
				public D3 run(D3 selection) {
					return selection.transition().delay(0).duration(500);
				}
			}).attr("transform", new Func<String, GWTVertex>(){

                                @Override
				public String call(GWTVertex vertex, int index) {
					return "translate(" + vertex.getInitialX() + "," +  vertex.getInitialY() + ")";
				}

			}).attr("opacity", 0).remove();


			//Updates
			edgeSelection.call(GWTEdge.draw()).attr("opacity", 1);
			
			vertexSelection.with(updateTransition()).call(GWTVertex.draw()).attr("opacity", 1);

			//Enters
			edgeSelection.enter().create(GWTEdge.create()).call(setupEdgeEventHandlers());
			
            //Scaling and Fit to Zoom transitions
			SVGMatrix transform = topologyView.calculateNewTransform(graph.getBoundingBox());
            
			int width = topologyView.getPhysicalWidth();
			int height = topologyView.getPhysicalHeight();
			D3 selection = D3.d3().select(topologyView.getSVGViewPort());
			D3Transform tform = D3.getTransform(selection.attr("transform"));
			
            JsArrayInteger p0 = (JsArrayInteger) JsArrayInteger.createArray();
            int x = tform.getX();
            int oldCenterX = (int) Math.round(((width/2 - x) / tform.getScaleX()));
            int y = tform.getY();
            int oldCenterY = (int) Math.round(((height/2 - y) / tform.getScaleY()));
            p0.push(oldCenterX);
            p0.push( oldCenterY );
            p0.push((int) (width / tform.getScaleX()));
            p0.push((int) (height / tform.getScaleY()));
            
            JsArrayInteger p1 = (JsArrayInteger) JsArrayInteger.createArray();
            int newCenterX = graph.getBoundingBox().getX() + graph.getBoundingBox().getWidth()/2;
            int newCenterY = graph.getBoundingBox().getY() + graph.getBoundingBox().getHeight()/2;
            p1.push(newCenterX);
            p1.push(newCenterY);
            p1.push(graph.getBoundingBox().getWidth());
            p1.push(graph.getBoundingBox().getHeight());
            
			D3.d3().zoomTransition(selection, width, height, p0, p1);
            
            D3.d3().selectAll(GWTEdge.SVG_EDGE_ELEMENT).style("stroke-width", GWTEdge.EDGE_WIDTH/transform.getA() + "px").transition().delay(750).duration(500).attr("opacity", "1").transition();
            
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
							.on(D3Events.DOUBLE_CLICK.event(), getDblClickHandler())
							.call(getDragBehavior());
				}
			};
		}

		private Handler<GWTVertex> getVertexTooltipHandler(){
			return m_vertexTooltipHandler;
		}

		private D3 getVertexSelection(GWTGraph graph, TopologyView<TopologyViewRenderer> topologyView) {
			D3 vertexGroup = D3.d3().select( topologyView.getVertexGroup() );
            Func<String, GWTVertex> vertexIdentifierFunction = new Func<String, GWTVertex>() {

                                @Override
				public String call(GWTVertex param, int index) {
					return "" + param.getId();
				}

			};
			return vertexGroup.selectAll(GWTVertex.VERTEX_CLASS_NAME)
					.data(graph.getVertices(), vertexIdentifierFunction);
		}

		private D3 getEdgeSelection(GWTGraph graph, TopologyView<TopologyViewRenderer> topologyView) {
			D3 edgeGroup = D3.d3().select(topologyView.getEdgeGroup());
            Func<String, GWTEdge> edgeIdentifierFunction = new Func<String, GWTEdge>() {

                                @Override
				public String call(GWTEdge edge, int index) {
                    /*
                    TODO Figure out how to do this in the new GWT API
				    if(m_client.getTooltipTitleInfo(VTopologyComponent.this, edge) == null) {
				        m_client.registerTooltip(VTopologyComponent.this, edge, new TooltipInfo(edge.getTooltipText()));
				    }
					 */
                    String edgeId = edge.getId();
                    return edgeId;
                }

			};
			return edgeGroup.selectAll(GWTEdge.SVG_EDGE_ELEMENT).data(graph.getEdges(), edgeIdentifierFunction);
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

	private GWTGraph m_graph;
	private DragObject m_dragObject;
	
	@UiField
	FlowPanel m_componentHolder;

	private D3Drag m_d3PanDrag;
	private SVGGraphDrawer m_graphDrawer;
	private SVGGraphDrawerNoTransition m_graphDrawerNoTransition;
	private List<Element> m_selectedElements = new ArrayList<Element>();
	private DragHandlerManager m_svgDragHandlerManager;
    private ServiceRegistry m_serviceRegistry;
    private TopologyViewRenderer m_currentViewRender;
    
    private TopologyView<TopologyViewRenderer> m_topologyView;
    private List<GraphUpdateListener> m_graphListenerList = new ArrayList<GraphUpdateListener>();
    private TopologyComponentServerRpc m_serverRpc;


	public VTopologyComponent() {
		initWidget(uiBinder.createAndBindUi(this));
		m_graph = GWTGraph.create();
	}

    @SuppressWarnings("serial")
    @Override
	protected void onLoad() {
		super.onLoad();
		
		m_serviceRegistry = new DefaultServiceRegistry();
		m_serviceRegistry.register(vertexClickHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexClick"); }}, Handler.class);
		m_serviceRegistry.register(vertexDblClickHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexDblClick"); }}, Handler.class);
		m_serviceRegistry.register(vertexContextMenuHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexContextMenu"); }}, Handler.class);
		m_serviceRegistry.register(vertexTooltipHandler(), new HashMap<String, String>(){{ put("handlerType", "vertexTooltip"); }}, Handler.class);
		
		m_serviceRegistry.register(edgeContextHandler(), new HashMap<String, String>(){{ put("handlerType", "edgeContextMenu"); }}, Handler.class);
		m_serviceRegistry.register(edgeTooltipHandler(), new HashMap<String, String>(){{ put("handlerType", "edgeTooltip"); }}, Handler.class);
		m_serviceRegistry.register(edgeClickHandler(), new HashMap<String, String>(){{ put("handlerType", "edgeClick"); }}, Handler.class);
		
		
		m_topologyView = new TopologyViewImpl();
		m_topologyView.setPresenter(this);
		m_componentHolder.setSize("100%", "100%");
		m_componentHolder.add(m_topologyView.asWidget());
		
		m_svgDragHandlerManager = new DragHandlerManager();
		m_svgDragHandlerManager.addDragBehaviorHandler(PanHandler.DRAG_BEHAVIOR_KEY, new PanHandler(this, m_serviceRegistry));
		m_svgDragHandlerManager.addDragBehaviorHandler(MarqueeSelectHandler.DRAG_BEHAVIOR_KEY, new MarqueeSelectHandler(this, m_topologyView));
		m_svgDragHandlerManager.setCurrentDragHandler(PanHandler.DRAG_BEHAVIOR_KEY);
		D3 svgElement = D3.d3().select(m_topologyView.getSVGElement());
        setupDragBehavior(m_topologyView.getSVGElement(), m_svgDragHandlerManager);
//        svgElement.on("dblclick", new Handler<Void>() {
//
//            @Override
//            public void call(Void t, int index) {
//                JsArrayInteger pos = D3.getMouse(m_topologyView.getSVGElement());
//                onBackgroundDoubleClick(m_topologyView.getPoint(pos.get(0), pos.get(1)));
//            }
//        
//		})
		svgElement.on("mousewheel", new Handler<Void>() {

            @Override
            public void call(Void t, int index) {
                double scrollVal = (double)D3.getEvent().getMouseWheelVelocityY()/ 30.0;
                SVGPoint centerPos = m_topologyView.getCenterPos(m_graph.getBoundingBox());
                onMouseWheel(scrollVal, (int)centerPos.getX(), (int)centerPos.getY());
            }
            
		});
		
		
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

        m_windowResizeRegistration = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                sendPhysicalDimensions();
            }
        });


    }

    
	public TopologyView<TopologyViewRenderer> getTopologyView() {
        return m_topologyView;
    }

    private void setupDragBehavior(final Element panElem, final DragHandlerManager handlerManager) {
	    
		D3Drag d3Pan = D3.getDragBehavior();
		d3Pan.on(D3Events.DRAG_START.event(), new Handler<Element>() {

                        @Override
			public void call(Element elem, int index) {
			    handlerManager.onDragStart(elem);
			}
		});

		d3Pan.on(D3Events.DRAG.event(), new Handler<Element>() {

                        @Override
			public void call(Element elem, int index) {
			    handlerManager.onDrag(elem);
			}
		});

		d3Pan.on(D3Events.DRAG_END.event(), new Handler<Element>() {

                        @Override
			public void call(Element elem, int index) {
			    handlerManager.onDragEnd(elem);
			}
		});

		D3 select = D3.d3().select(panElem);
		select.call(d3Pan);
	}

	private void deselectAllItems() {
	    m_serverRpc.deselectAllItems();
    }

    private Handler<GWTVertex> vertexContextMenuHandler() {
		return new D3Events.Handler<GWTVertex>() {

                        @Override
			public void call(final GWTVertex vertex, int index) {
				showContextMenu(vertex.getId(), D3.getEvent().getClientX(), D3.getEvent().getClientY(), "vertex");
				D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
			}
		};
	}

	private Handler<GWTEdge> edgeContextHandler(){
		return new D3Events.Handler<GWTEdge>() {

                        @Override
			public void call(final GWTEdge edge, int index) {

				showContextMenu(edge.getId(), D3.getEvent().getClientX(), D3.getEvent().getClientY(), "edge");
				D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();

			}
		};
	}

	private Handler<GWTVertex> vertexTooltipHandler() {
		return new Handler<GWTVertex>() {

                        @Override
			public void call(GWTVertex t, int index) {
				if(m_client != null) {
					Event event = (Event) D3.getEvent();
					// TODO: Figure out how to do this in the new GWT
					//m_client.handleTooltipEvent(event, VTopologyComponent.this, t);
                    m_client.getVTooltip().show();
					event.stopPropagation();
					event.preventDefault();
				}
			}
		};
	}

	private Handler<GWTEdge> edgeTooltipHandler(){
		return new Handler<GWTEdge>() {

                        @Override
			public void call(GWTEdge edge, int index) {
				if(m_client != null) {
					Event event = D3.getEvent().cast();
					// TODO: Figure out how to do this in the new GWT
					//m_client.handleTooltipEvent(event, VTopologyComponent.this, edge);
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
                m_serverRpc.edgeClicked(edge.getId());
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }
	        
	    };
	}

	private Handler<GWTVertex> vertexClickHandler() {
		return new D3Events.Handler<GWTVertex>(){

                        @Override
			public void call(GWTVertex vertex, int index) {
				NativeEvent event = D3.getEvent();
				SVGGElement vertexElement = event.getCurrentEventTarget().cast();
				vertexElement.getParentElement().appendChild(vertexElement);
				
				event.preventDefault();
				event.stopPropagation();
				
				final MouseEventDetails mouseDetails = MouseEventDetailsBuilder.buildMouseEventDetails(event, getElement());
				m_serverRpc.vertexClicked(vertex.getId(), mouseDetails, Navigator.getPlatform());
				
				
			}
		};
	}
	
	private Handler<GWTVertex> vertexDblClickHandler(){
	    return new D3Events.Handler<GWTVertex>() {

            @Override
            public void call(GWTVertex vert, int index) {
                
                
            }
        };
	}

	private Handler<GWTVertex> vertexDragEndHandler() {
		return new Handler<GWTVertex>() {

                        @Override
			public void call(GWTVertex vertex, int index) {
			    if(D3.getEvent().getButton() != NativeEvent.BUTTON_RIGHT) {
			    
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
    			    
//    			    m_client.updateVariable(getPaintableId(), "updateVertices", values.toArray(new String[] {}), false);
//    			    m_client.sendPendingVariableChanges();
    			    m_serverRpc.updateVertices(values);
    			    
    				D3.getEvent().preventDefault();
    				D3.getEvent().stopPropagation();
			    }
			}

		};
	}

	private Handler<GWTVertex> vertexDragStartHandler() {
		return new Handler<GWTVertex>() {

                        @Override
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

                        @Override
			public void call(GWTVertex vertex, int index) {

				m_dragObject.move();
				
				if(getViewRenderer() == m_graphDrawer) {
				    m_currentViewRender = m_graphDrawerNoTransition;
				}
				
				for( GraphUpdateListener listener : m_graphListenerList) {
				    listener.onGraphUpdated(m_graph, m_graph.getBoundingBox());
				}
				
				D3.getEvent().preventDefault();
				D3.getEvent().stopPropagation();
			}
		};
	}
	
	 public void updateGraph(ApplicationConnection applicationConnection, TopologyComponentState componentState) {
	    
		GWTGraph graph = GWTGraph.create();
		
		m_client = applicationConnection;
		setActiveTool(componentState.getActiveTool());
        
		GWTVertex.setBackgroundImage(applicationConnection.translateVaadinUri("theme://images/vertex_circle_selector.png"));
		for(SharedVertex sharedVertex : componentState.getVertices()) {
		    
		    GWTVertex vertex = GWTVertex.create(sharedVertex.getKey(), sharedVertex.getX(), sharedVertex.getY());
            
            vertex.setInitialX(sharedVertex.getInitialX());
            vertex.setInitialY(sharedVertex.getInitialY());
            
            boolean selected = sharedVertex.getSelected();
            vertex.setSelected(selected);

            vertex.setIconUrl(applicationConnection.translateVaadinUri(sharedVertex.getIconUrl()));

            vertex.setLabel(sharedVertex.getLabel());
            
            vertex.setStatus(sharedVertex.getStatus());

            vertex.setStatusCount(sharedVertex.getStatusCount());

            graph.addVertex(vertex);
		}

		for(SharedEdge sharedEdge : componentState.getEdges()) {
			String edgeKey = sharedEdge.getKey();
            String sourceKey = sharedEdge.getSourceKey();
            String targetKey = sharedEdge.getTargetKey();
            
            GWTVertex source = graph.findVertexById(sourceKey);
            GWTVertex target = graph.findVertexById( targetKey );
            GWTEdge edge = GWTEdge.create(edgeKey, source, target);
            boolean selected = sharedEdge.getSelected();
            String cssClass = sharedEdge.getCssClass();
            edge.setSelected(selected);
            edge.setCssClass(cssClass);
            String ttText = sharedEdge.getTooltipText();
            edge.setTooltipText(ttText);
            graph.addEdge(edge);
		}
		
		JsArray<GWTEdge> edges = graph.getEdges();
		sortEdges(edges);
		
		
        for( int i = 0; i < edges.length(); i++) {
            if(i != 0) {
		        GWTEdge edge1 = edges.get(i-1);
		        GWTEdge edge2 = edges.get(i);
		        
		        String edge1Source = minEndPoint(edge1);
		        String edge2Source = minEndPoint(edge2);
		        String edge1Target = maxEndPoint(edge1);
		        String edge2Target = maxEndPoint(edge2);
		        
		        if((edge1Source.equals(edge2Source) && edge1Target.equals(edge2Target))) {
		            edge2.setLinkNum(edge1.getLinkNum() + 1);
		        }else {
		            edge2.setLinkNum(1);
		        }
		    }
		}
        
        int x = componentState.getBoundX();
        int y = componentState.getBoundY();
        int width = componentState.getBoundWidth();
        int height = componentState.getBoundHeight();
        
        GWTBoundingBox oldBBox = m_graph.getBoundingBox();
        graph.setBoundingBox(GWTBoundingBox.create(x, y, width, height));
		setGraph(graph, oldBBox);

         sendPhysicalDimensions();
	}
	
	private void sendPhysicalDimensions() {
	    int width = m_topologyView.getPhysicalWidth();
	    int height = m_topologyView.getPhysicalHeight();
	    m_serverRpc.mapPhysicalBounds(width, height);
	    
	}

    private String minEndPoint(GWTEdge edge1) {
        String edge1Source = edge1.getSource().getId().compareTo(edge1.getTarget().getId()) < 0 ? edge1.getSource().getId() : edge1.getTarget().getId();
        return edge1Source;
    }
	
    private String maxEndPoint(GWTEdge edge1) {
        String edge1Source = edge1.getSource().getId().compareTo(edge1.getTarget().getId()) < 0 ? edge1.getTarget().getId() : edge1.getSource().getId();
        return edge1Source;
    }
    
	private native void sortEdges(JsArray<GWTEdge> list)/*-{
	
	    list.sort(function(a,b){
	        var sourceA = a.source.id < a.target.id ? a.source.id : a.target.id;
            var targetA = a.source.id < a.target.id ? a.target.id : a.source.id;
	        var sourceB = b.source.id < b.target.id ? b.source.id : b.target.id;
	        var targetB = b.source.id < b.target.id ? b.target.id : b.source.id;
	        if(sourceA > sourceB){ 
	            return 1; 
	        } else if(sourceA < sourceB){
	            return -1;
	        }else{
	            if(targetA > targetB){
	                return 1;
	            }
	            if(targetA < targetB){
	                return -1;
	            } else {return 0;}
	        }
	    });
	    
	}-*/;
	
    private void setActiveTool(String toolname) {
	    if(toolname.equals("pan")) {
	        m_svgDragHandlerManager.setCurrentDragHandler(PanHandler.DRAG_BEHAVIOR_KEY);
	        m_topologyView.getSVGElement().getStyle().setCursor(Cursor.MOVE);
	    }else if(toolname.equals("select")) {
	        m_svgDragHandlerManager.setCurrentDragHandler(MarqueeSelectHandler.DRAG_BEHAVIOR_KEY);
	        m_topologyView.getSVGElement().getStyle().setCursor(Cursor.CROSSHAIR);
	    }
    }

    /**
	 * Sets the graph, updates the ViewRenderer if need be and 
	 * updates all graphUpdateListeners
	 * @param graph
     * @param oldBBox 
	 */
	private void setGraph(GWTGraph graph, GWTBoundingBox oldBBox) {
		m_graph = graph;
        
		//Set the ViewRenderer to the Animated one if it isn't already
		if(getViewRenderer() != m_graphDrawer) {
		    setTopologyViewRenderer(m_graphDrawer);
		}
        
        updateGraphUpdateListeners(oldBBox);
	}

    public ApplicationConnection getClient() {
		return m_client;
	}

	public void showContextMenu(String target, int x, int y, String type) {
		m_serverRpc.contextMenu(target, type, x, y);
	}
	
    @Override
    public void setVertexSelection(List<String> vertIds) {
        final MouseEventDetails mouseDetails  = MouseEventDetailsBuilder.buildMouseEventDetails(D3.getEvent(), getElement());
        m_serverRpc.marqueeSelection(vertIds.toArray(new String[] {}), mouseDetails);
    }

    /**
     * Returns the D3 selection for all Vertex svg elements
     */
    @Override
    public D3 selectAllVertexElements() {
        return D3.d3().selectAll(GWTVertex.VERTEX_CLASS_NAME);
    }
    
    private BoundingRect createBoundingRect(JsArray<GWTVertex> vertices, boolean fitToView) {
        final BoundingRect rect = new BoundingRect();

        for(int i = 0; i < vertices.length(); i++) {
            GWTVertex vertex = vertices.get(i);
            
            if(fitToView || vertex.isSelected()) {
                double vertexX = vertex.getX();
                double vertexY = vertex.getY();
                rect.addPoint(new Point(vertexX, vertexY));
            }
        }
        return rect;
    }
    
    public void updateMapPosition() {
        SVGPoint pos = m_topologyView.getCenterPos(m_graph.getBoundingBox());
        Map<String, Object> point = new HashMap<String, Object>();
        point.put("x", (int)Math.round(pos.getX()));
        point.put("y", (int)Math.round(pos.getY()));
//        m_client.updateVariable(getPaintableId(), "clientCenterPoint", point, true);
        m_serverRpc.clientCenterPoint((int)Math.round(pos.getX()) , (int)Math.round(pos.getY()));
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
        //m_client.updateVariable(m_paintableId, "clickedBackground", true, true);
        m_serverRpc.backgroundClicked();
    }

    @Override
    public void onContextMenu(Object target, int x, int y, String type) {
        showContextMenu((String)target, x, y, type);
    }

    @Override
    public void addGraphUpdateListener(GraphUpdateListener listener) {
        m_graphListenerList.add(listener);
    }
    
    private void updateGraphUpdateListeners(GWTBoundingBox oldBBox) {
        for(GraphUpdateListener listener : m_graphListenerList) {
            listener.onGraphUpdated( m_graph, oldBBox);
        }
    }

    @Override
    public void onMouseWheel(double scrollVal, int x, int y) {
        m_serverRpc.scrollWheel(scrollVal, x, y);
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

    @Override
    public void onBackgroundDoubleClick(SVGPoint center) {
        m_serverRpc.backgroundDoubleClick(center.getX(), center.getY());
    }

    public void setComponentServerRpc(TopologyComponentServerRpc rpc) {
        m_serverRpc = rpc;
    }

    @Override
    protected void onDetach() {
        m_windowResizeRegistration.removeHandler();
        super.onDetach();    //To change body of overridden methods use File | Settings | File Templates.
    }

}
