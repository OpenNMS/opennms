package org.opennms.features.vaadin.topology.gwt.client;

import java.util.HashMap;
import java.util.Iterator;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Drag;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler;
import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

import com.google.gwt.core.client.GWT;
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
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VTooltip;
import com.vaadin.terminal.gwt.client.ui.Action;
import com.vaadin.terminal.gwt.client.ui.ActionOwner;
import com.vaadin.terminal.gwt.client.ui.dd.VDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler;

public class VTopologyComponent extends Composite implements Paintable, ActionOwner, VHasDropHandler {
	
    static class GraphDrawer{
        GWTGraph m_graph;
        Element m_vertexGroup;
        Element m_edgeGroup;
        D3Drag m_dragBehavior;
        Handler<GWTVertex> m_clickHandler;
        Handler<GWTVertex> m_contextMenuHandler;
        

        public GraphDrawer(GWTGraph graph, Element vertexGroup, Element edgeGroup, D3Drag dragBehavior, Handler<GWTVertex> clickHandler, Handler<GWTVertex> contextMenuHandler) {
            m_graph = graph;
            m_vertexGroup = vertexGroup;
            m_edgeGroup = edgeGroup;
            m_dragBehavior = dragBehavior;
            setClickHandler(clickHandler);
            setContextMenuHandler(contextMenuHandler);
            
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

        public D3Drag getDragBehavior() {
            return m_dragBehavior;
        }

        void draw() {
            
            GWTGraph graph = getGraph();
            
            D3 edgeGroup = getEdgeGroup();
            D3 lines = edgeGroup.selectAll("line")
                    .data(graph.getEdges(), new Func<String, GWTEdge>() {
        
        				public String call(GWTEdge edge, int index) {
        					String edgeId = edge.getId();
        					return edgeId;
        				}
                    	
                    });
            
            D3 vGroup = getVertexGroup();
            final D3 vertexGroup = vGroup.selectAll(".little")
                    .data(graph.getVertices(), new Func<String, GWTVertex>() {
        
        				public String call(GWTVertex param, int index) {
        					return "" + param.getId();
        				}
                    	
                    });
            //Exits
            lines.exit().transition().duration(500).attr("opacity", 0).remove();
            vertexGroup.exit().transition().duration(500).attr("opacity", 0).remove();
            
            //Updates
            lines.transition().delay(500).duration(500)
                    .attr("x1", GWTEdge.getSourceX())
                    .attr("x2", GWTEdge.getTargetX())
                    .attr("y1", GWTEdge.getSourceY())
                    .attr("y2", GWTEdge.getTargetY())
                    .attr("opacity", 1);
            
            vertexGroup.transition().delay(500).duration(500)
                    .attr("transform", GWTVertex.getTranslation())
                    .attr("opacity", 1);
        	
        	D3 updateCircle = vertexGroup.select("circle");
        	updateCircle.style("fill", GWTVertex.selectedFill("Update"));
            
            //Enters
            lines.enter().append("line")
                    .attr("opacity", 0)
                    .attr("x1", GWTEdge.getSourceX())
                    .attr("x2", GWTEdge.getTargetX())
                    .attr("y1", GWTEdge.getSourceY())
                    .attr("y2", GWTEdge.getTargetY())
                    .style("stroke", "#ccc").transition().delay(1000).duration(500)
                    .attr("opacity", 1);
            
            D3Drag dragBehavior = getDragBehavior();
            
            D3 vertex = vertexGroup.enter().append("g").attr("transform", GWTVertex.getTranslation())
                    .attr("opacity", 0)
                    .attr("class", "little")
                    .on(D3Events.CLICK.event(), getClickHandler())
                    .on(D3Events.CONTEXT_MENU.event(), getContextMenuHandler()).call(dragBehavior);
                    
            vertex.append("circle").attr("r", 9).style("fill", GWTVertex.selectedFill("Enter"));
            
            vertex.append("text").attr("dy", ".35em")
                .attr("text-anchor", "middle").style("fill", "white")
                .text(D3.property("id"));
            
            vertex.transition().delay(1000).duration(500).attr("opacity", 1);
        }

        void drawNow() {
            GWTGraph graph = getGraph();
            
            D3 lines = getEdgeGroup().selectAll("line")
                    .data(graph.getEdges(), new Func<String, GWTEdge>() {
        
                        public String call(GWTEdge edge, int index) {
                            String edgeId = edge.getId();
                            return edgeId;
                        }
                        
                    });
            
            final D3 vertexGroup = getVertexGroup().selectAll(".little")
                    .data(graph.getVertices(), new Func<String, GWTVertex>() {
        
                        public String call(GWTVertex param, int index) {
                            return "" + param.getId();
                        }
                        
                    });
            //Exits
            lines.exit().remove();
            vertexGroup.exit().remove();
            
            //Updates
            lines.attr("x1", GWTEdge.getSourceX())
                    .attr("x2", GWTEdge.getTargetX())
                    .attr("y1", GWTEdge.getSourceY())
                    .attr("y2", GWTEdge.getTargetY())
                    .attr("opacity", 1);
            
            vertexGroup.attr("transform", GWTVertex.getTranslation())
                    .attr("opacity", 1);
            
            D3 updateCircle = vertexGroup.select("circle");
            updateCircle.style("fill", GWTVertex.selectedFill("Update"));
            
            //Enters
            lines.enter().append("line")
                    .attr("opacity", 1)
                    .attr("x1", GWTEdge.getSourceX())
                    .attr("x2", GWTEdge.getTargetX())
                    .attr("y1", GWTEdge.getSourceY())
                    .attr("y2", GWTEdge.getTargetY())
                    .style("stroke", "#ccc");
            
            D3 vertex = vertexGroup.enter().append("g").attr("transform", GWTVertex.getTranslation())
                    .attr("opacity", 1)
                    .attr("class", "little")
                    .on(D3Events.CLICK.event(), getClickHandler())
                    .on(D3Events.CONTEXT_MENU.event(), getContextMenuHandler()).call(getDragBehavior());
                    
            vertex.append("circle").attr("r", 9).style("fill", GWTVertex.selectedFill("Enter"));
            
            vertex.append("text").attr("dy", ".35em")
                .attr("text-anchor", "middle").style("fill", "white")
                .text(D3.property("id"));
        }
        
    }
    
    public class DragObject{
		private Element m_containerElement;
		private Element m_draggableElement;
		private int m_startX;
		private int m_startY;

        public DragObject(Element draggableElement, Element containerElement) {
            m_draggableElement = draggableElement;
            m_containerElement = containerElement;
        	
            JsArrayInteger position = D3.getMouse(containerElement);
            m_startX = position.get(0);
            m_startY = position.get(1);
        }
        
        public Element getContainerElement() {
            return m_containerElement;
        }
        
        public Element getDraggableElement() {
            return m_draggableElement;
        }
        
    	public int getCurrentX() {
    		JsArrayInteger position = D3.getMouse(m_containerElement);
    		return position.get(0);
    	}
    	
    	public int getCurrentY() {
    		JsArrayInteger position = D3.getMouse(m_containerElement);
    		return position.get(1);
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

    }
    
    private static VTopologyComponentUiBinder uiBinder = GWT
            .create(VTopologyComponentUiBinder.class);

    interface VTopologyComponentUiBinder extends
            UiBinder<Widget, VTopologyComponent> {
    }
    
    private ApplicationConnection m_client;
    private String m_paintableId;
    
    private GWTGraph m_graph;
	private double m_scale;
    private boolean m_firstTime = true;
    private DragObject m_dragObject;
    
    @UiField
    Element m_svg;
    
    @UiField
    Element m_svgViewPort;
    
    @UiField
    Element m_edgeGroup;
    
    @UiField
    Element m_vertexGroup;
    
    /**
     * This map contains captions and icon urls for actions like: * "33_c" ->
     * "Edit" * "33_i" -> "http://dom.com/edit.png"
     */
    private final HashMap<String, String> m_actionMap = new HashMap<String, String>();
    
    
	private String[] m_actionKeys;
    private D3Drag m_d3VertexDrag;
	
	private D3Drag m_d3PanDrag;
    
    public VTopologyComponent() {
        initWidget(uiBinder.createAndBindUi(this));
        
        m_graph = GWTGraph.create();
    }
    
    

    @Override
    protected void onLoad() {
        super.onLoad();
        
        sinkEvents(Event.ONCONTEXTMENU | VTooltip.TOOLTIP_EVENTS);
        
        D3 d3 = D3.d3();
        //setupPanningBehavior(m_svgViewPort);
        
        m_d3VertexDrag = D3.getDragBehavior();
        
        
    }

    private D3Drag setupPanningBehavior(D3 x) {
		D3Drag d3Pan = D3.getDragBehavior();
		d3Pan.on(D3Events.DRAG_START.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				//TODO: Pan viewport
				
				NativeEvent event = D3.getEvent();
				Element elem = Element.as(event.getEventTarget());
				
//				m_dragObject = null;
//				m_dragObject = new DragObject(null, elem);
				
			}
		});
		
		d3Pan.on(D3Events.DRAG.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				// TODO Auto-generated method stub
//				int deltaX = m_dragObject.getCurrentX() - m_dragObject.getStartX();
//				int deltaY = m_dragObject.getCurrentY() - m_dragObject.getStartY();
//				D3.d3().select("#viewport").attr("transform", "translate(" + deltaX + "," + deltaY + ")");
			}
		});
		
		d3Pan.on(D3Events.DRAG_END.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				m_dragObject = null;
			}
		});
		
    	return d3Pan;
		
	}

	private void drawGraph(GWTGraph g, boolean now) {
	    m_d3VertexDrag.on(D3Events.DRAG.event(), vertexDragHandler());
        
        m_d3VertexDrag.on(D3Events.DRAG_START.event(), vertexDragStartHandler());
        
        m_d3VertexDrag.on(D3Events.DRAG_END.event(), vertexDragEndHandler());
        
	    GraphDrawer drawer = new GraphDrawer(g, m_vertexGroup, m_edgeGroup, m_d3VertexDrag, vertexClickHandler(), vertexContextMenuHandler());
	    
	    if(now) {  
	        drawer.drawNow();
	    }else {
	        drawer.draw();
	    }
	    
                
        
	}

    @Override
    public void onBrowserEvent(Event event) {
    	super.onBrowserEvent(event);
    	//        if(m_client != null) {
    	//            m_client.handleTooltipEvent(event, this);
    	//        }

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
    
    public static final native void eval(Element elem) /*-{
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
                //consoleLog("get cursorStartX: " + m_dragObject.getCursorStartX() + " x: " + x + " newVertexX: " + newVertexX);
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
        
        setScale(uidl.getDoubleAttribute("scale"));
        setActionKeys(uidl.getStringArrayAttribute("backgroundActions"));
        
      //Register tooltip
//        String description = uidl.getStringAttribute("description");
//        if(description != null && m_client != null) {
//            TooltipInfo info = new TooltipInfo(description);
//            m_client.registerTooltip(this, getElement(), info);
//        }
        
        UIDL graph = uidl.getChildByTagName("graph");
        Iterator<?> children = graph.getChildIterator();
        
        GWTGraph graphConverted = GWTGraph.create();
        while(children.hasNext()) {
        	UIDL child = (UIDL) children.next();
        	
        	if(child.getTag().equals("vertex")) {
        		
        		GWTVertex vertex = GWTVertex.create(child.getStringAttribute("id"), child.getIntAttribute("x"), child.getIntAttribute("y"));
        		boolean booleanAttribute = child.getBooleanAttribute("selected");
        		String[] actionKeys = child.getStringArrayAttribute("actionKeys");
        		
        		vertex.setActionKeys(actionKeys);
        		
				vertex.setSelected(booleanAttribute);
				graphConverted.addVertex(vertex);
        		
        	}else if(child.getTag().equals("edge")) {
        		GWTEdge edge = GWTEdge.create(graphConverted.findVertexById(child.getStringAttribute("source")), graphConverted.findVertexById( child.getStringAttribute("target") ));
        		String[] actionKeys = child.getStringArrayAttribute("actionKeys");
        		edge.setActionKeys(actionKeys);
        		graphConverted.addEdge(edge);
        	}
        	
        }
        
        UIDL actions = uidl.getChildByTagName("actions");
        if (actions != null) {
        	updateActionMap(actions);
        }
        
        setGraph(graphConverted);
        
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

	private void setScale(double scale) {
		if(m_scale != scale) {
			m_scale = scale;
			repaintScale();
		}
		
	}

	private void repaintScale() {
		updateScale(m_scale);
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

	private void updateScale(double scale) {
		D3.d3().select(m_vertexGroup).transition().duration(1000).attr("transform", "scale(" + scale + ")");
		D3.d3().select(m_edgeGroup).transition().duration(1000).attr("transform", "scale(" + scale + ")");
		
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
