package org.opennms.features.vaadin.topology.gwt.client;

import java.util.HashMap;
import java.util.Iterator;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Drag;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler;
import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VTooltip;
import com.vaadin.terminal.gwt.client.ui.Action;
import com.vaadin.terminal.gwt.client.ui.ActionOwner;
import com.vaadin.terminal.gwt.client.ui.dd.VDragAndDropManager;
import com.vaadin.terminal.gwt.client.ui.dd.VDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VTransferable;

public class VTopologyComponent extends Composite implements Paintable, ActionOwner, VHasDropHandler {
	
    static class DragObject{
		private Element m_element;
		private int m_startX;
		private int m_startY;

        public DragObject(Element containerElement) {
        	m_element = containerElement;
            JsArrayInteger position = D3.getMouse(containerElement);
            m_startX = position.get(0);
            m_startY = position.get(1);
        }
        
    	public int getCurrentX() {
    		JsArrayInteger position = D3.getMouse(m_element);
    		return position.get(0);
    	}
    	
    	public int getCurrentY() {
    		JsArrayInteger position = D3.getMouse(m_element);
    		return position.get(1);
    	}

		public int getStartX() {
			return m_startX;
		}
		
		public int getStartY() {
			return m_startY;
		}


    }
    
    private static VTopologyComponentUiBinder uiBinder = GWT
            .create(VTopologyComponentUiBinder.class);

    interface VTopologyComponentUiBinder extends
            UiBinder<Widget, VTopologyComponent> {
    }
    
    private ApplicationConnection m_client;
    private String m_paintableId;
    private D3 m_svg;
    private int m_width;
    private int m_height;
    private GWTGraph m_graph;
	private D3 m_vertexGroup;
	private double m_scale;
    private boolean m_firstTime = true;
    private D3 m_edgeGroup;
    private DragObject m_dragObject;
    
    /**
     * This map contains captions and icon urls for actions like: * "33_c" ->
     * "Edit" * "33_i" -> "http://dom.com/edit.png"
     */
    private final HashMap<String, String> m_actionMap = new HashMap<String, String>();
    
    
	private String[] m_actionKeys;
    private D3Drag m_d3VertexDrag;
	private D3 m_svgViewPort;
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
        m_width = 300;
        m_height = 300;
        m_svg = d3.select("#chart-2").append("svg").attr("width", "100%").attr("height", "100%").style("background-color", "white").call(setupPanningBehavior(null));
        m_svgViewPort = m_svg.append("g").attr("id", "viewport").attr("transform", "translate(0,0)");
        m_edgeGroup = m_svgViewPort.append("g").attr("transform", "scale(1)");
        m_vertexGroup = m_svgViewPort.append("g").attr("transform", "scale(1)");
        
        setupPanningBehavior(m_svgViewPort);
        
        m_d3VertexDrag = D3.getDragBehavior();
        
        
    }

    private D3Drag setupPanningBehavior(D3 x) {
		D3Drag d3Pan = D3.getDragBehavior();
		d3Pan.on(D3Events.DRAG_START.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				//TODO: Pan viewport
				
				NativeEvent event = D3.getEvent();
				Element elem = Element.as(event.getEventTarget());
				
				m_dragObject = null;
				m_dragObject = new DragObject(elem);
				
			}
		});
		
		d3Pan.on(D3Events.DRAG.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				// TODO Auto-generated method stub
				int deltaX = m_dragObject.getCurrentX() - m_dragObject.getStartX();
				int deltaY = m_dragObject.getCurrentY() - m_dragObject.getStartY();
				String transform = D3.d3().select("#viewport").attr("transform");
				consoleLog("elemnt x: " + transform);
				//TODO: add the d3.transform stuff to the d3 object
				//D3.d3().select("#viewport").attr("transform", "translate(" + deltaX + "," + deltaY + ")");
			}
		});
		
		d3Pan.on(D3Events.DRAG_END.event(), new Handler<Object>() {

			public void call(Object t, int index) {
				m_dragObject = null;
			}
		});
		
    	return d3Pan;
		
	}

	private void drawGraph(GWTGraph graph) {
        D3 lines = m_edgeGroup.selectAll("line")
                .data(graph.getEdges(), new Func<String, GWTEdge>() {

					public String call(GWTEdge edge, int index) {
						String edgeId = edge.getId();
						return edgeId;
					}
                	
                });
        
        final D3 vertexGroup = m_vertexGroup.selectAll(".little")
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
                .attr("x1", getSourceX())
                .attr("x2", getTargetX())
                .attr("y1", getSourceY())
                .attr("y2", getTargetY())
                .attr("opacity", 1);
        
        vertexGroup.transition().delay(500).duration(500)
                .attr("transform", getTranslation())
                .attr("opacity", 1);
		
		D3 updateCircle = vertexGroup.select("circle");
		updateCircle.style("fill", selectedFill("Update"));
        
        //Enters
        lines.enter().append("line")
                .attr("opacity", 0)
                .attr("x1", getSourceX())
                .attr("x2", getTargetX())
                .attr("y1", getSourceY())
                .attr("y2", getTargetY())
                .style("stroke", "#ccc").transition().delay(1000).duration(500)
                .attr("opacity", 1);
        
      //Drag Handlers
        m_d3VertexDrag.on(D3Events.DRAG.event(), vertexDragHandler());
        
        m_d3VertexDrag.on(D3Events.DRAG_START.event(), vertexDragStartHandler());
        
        m_d3VertexDrag.on(D3Events.DRAG_END.event(), vertexDragEndHandler());
        //End Drag Handlers
        
        D3 vertex = vertexGroup.enter().append("g").attr("transform", getTranslation())
                .attr("opacity", 0)
                .attr("class", "little")
                .on(D3Events.CLICK.event(), vertexClickHandler())
                .on(D3Events.CONTEXT_MENU.event(), vertexContextMenuHandler()).call(m_d3VertexDrag);
                
        vertex.append("circle").attr("r", 9).style("fill", selectedFill("Enter"));
        
        vertex.append("text").attr("dy", ".35em")
            .attr("text-anchor", "middle").style("fill", "white")
            .text(D3.property("id"));
        
        vertex.transition().delay(1000).duration(500).attr("opacity", 1);
                
        
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
                Element elem = Element.as(event.getEventTarget()).getParentElement();
                
                m_dragObject = new DragObject(elem.getParentElement());
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }

            
        };
    }
    
    private static final native void consoleLog(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    private Handler<GWTVertex> vertexDragHandler() {
        return new Handler<GWTVertex>() {

            public void call(GWTVertex vertex, int index) {
                NativeEvent event = D3.getEvent();
                Element selectedElement = Element.as(D3.getEvent().getEventTarget());
                //get parent element because it somehow is never selected.
                //But since the circle and the text are both in a g, we shall select the g element
                Element parentElem = selectedElement.getParentElement();
                
                vertex.setX( m_dragObject.getCurrentX() );
                vertex.setY( m_dragObject.getCurrentY() );
                D3.d3().select(parentElem).attr("transform", "translate(" + vertex.getX() + "," + vertex.getY() + ")");
                //consoleLog("get cursorStartX: " + m_dragObject.getCursorStartX() + " x: " + x + " newVertexX: " + newVertexX);
                D3.getEvent().preventDefault();
                D3.getEvent().stopPropagation();
            }
        };
    }
   
    
    private Func<String, GWTVertex> selectedFill(final String caller) {
		return new Func<String, GWTVertex>(){

			public String call(GWTVertex vertex, int index) {
				return vertex.isSelected() ? "blue" : "black";
			}
		};
	}
	
	private static Func<String, GWTVertex> getTranslation() {
		return new Func<String, GWTVertex>() {

			public String call(GWTVertex datum, int index) {
				return "translate( " + datum.getX() + "," + datum.getY() + ")";
			}
			
		};
	}
    
    
    private Func<Integer, GWTEdge> getSourceX() {
		
		return new Func<Integer, GWTEdge>(){

            public Integer call(GWTEdge datum, int index) {
                return datum.getSource().getX();
            }
        };
	};
    
    private Func<Integer, GWTEdge> getTargetX() {
	
		return new Func<Integer, GWTEdge>(){

            public Integer call(GWTEdge datum, int index) {
                return datum.getTarget().getX();
            }
        };
	};
	
	private Func<Integer, GWTEdge> getSourceY() {
	    
        return new Func<Integer, GWTEdge>(){

            public Integer call(GWTEdge datum, int index) {
                return datum.getSource().getY();
            }
        };
    };
    
    private Func<Integer, GWTEdge> getTargetY() {
        
        return new Func<Integer, GWTEdge>(){

            public Integer call(GWTEdge datum, int index) {
                return datum.getTarget().getY();
            }
        };
    };
    

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
        drawGraph(m_graph);
	}

	private void updateScale(double scale) {
		m_vertexGroup.transition().duration(1000).attr("transform", "scale(" + scale + ")");
		m_edgeGroup.transition().duration(1000).attr("transform", "scale(" + scale + ")");
		
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
