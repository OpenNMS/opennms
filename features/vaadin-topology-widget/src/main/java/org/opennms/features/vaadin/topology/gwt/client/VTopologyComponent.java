package org.opennms.features.vaadin.topology.gwt.client;

import java.util.Iterator;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events.Handler;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3MouseEvent;
import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

public class VTopologyComponent extends Composite implements Paintable {
	
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
    
    @UiField
    Button m_saveButton;
    
    public VTopologyComponent() {
        initWidget(uiBinder.createAndBindUi(this));
        
        m_graph = GWTGraph.create();
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        D3 d3 = D3.d3();
        m_width = 300;
        m_height = 300;
        m_svg = d3.select("#chart-2").append("svg").attr("width", 300).attr("height", 200);
        m_edgeGroup = m_svg.append("g");//.attr("transform", "scale(1)");
        m_vertexGroup = m_svg.append("g");//.attr("transform", "scale(1)");
        
    }

    @UiHandler("m_saveButton")
    public void onSaveButtonClick(ClickEvent e) {
        Command command = new Command() {

            public void execute() {
                //Let's change this up in the future and do the conversion in the GWTGraph class
                m_client.updateVariable(m_paintableId, "graph", GraphJSONConverter.convertGraphToJSON(m_graph), true);
            }
        };
        
        if(BrowserInfo.get().isWebkit()) {
            Scheduler.get().scheduleDeferred(command);
        }else {
            command.execute();
        }
    };

    private void drawGraph(GWTGraph graph) {
        D3 lines = m_edgeGroup.selectAll("line")
                .data(graph.getEdges(), new Func<String, GWTEdge>() {

					public String call(GWTEdge edge, int index) {
						String edgeId = edge.getId();
						return edgeId;
					}
                	
                });
        
        D3 vertexGroup = m_vertexGroup.selectAll(".little")
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
        
        D3 vertex = vertexGroup.enter().append("g").attr("transform", getTranslation())
                .attr("opacity", 0)
                .attr("class", "little")
                .on(D3Events.CLICK.event(), vertexClickHandler())
                .on(D3Events.CONTEXT_MENU.event(), vertexContextMenuHandler());
                
        vertex.append("circle").attr("r", 9).style("fill", selectedFill("Enter"));
        
        vertex.append("text").attr("dy", ".35em")
            .attr("text-anchor", "middle").style("fill", "white")
            .text(D3.property("id"));
        
        vertex.transition().delay(1000).duration(500).attr("opacity", 1);
                
        
	}

    private Handler<GWTVertex> vertexContextMenuHandler() {
        return new D3Events.Handler<GWTVertex>() {

            public void call(GWTVertex vertex, int index) {
                VConsole.log("Context Menu on vertex: " + vertex.getId());
                m_client.updateVariable(m_paintableId, "vertexContextMenu", true, true);
                D3.eventPreventDefault();
            }
        };
    }

    private Handler<GWTVertex> vertexClickHandler() {
        return new D3Events.Handler<GWTVertex>(){

        	public void call(GWTVertex vertex, int index) {
        		m_client.updateVariable(m_paintableId, "clickedVertex", vertex.getId(), false);
        		m_client.updateVariable(m_paintableId, "shiftKeyPressed", getMouseEvent(D3.event()).shiftKey(), false);
        		
        		m_client.sendPendingVariableChanges();
        	}
        };
    }
    
	private final native D3MouseEvent getMouseEvent(JavaScriptObject event) /*-{
        return event;
    }-*/;

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
    
//    private static native JavaScriptObject getTranslation() /*-{
//        return function(d){
//            var x = d.@org.opennms.features.vaadin.topology.gwt.client.Vertex::getX()();
//            var y = d.@org.opennms.features.vaadin.topology.gwt.client.Vertex::getY()();
//            return "translate(" + x + "," + y + ")";
//        }
//    }-*/;

    
    
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
        UIDL graph = uidl.getChildByTagName("graph");
        Iterator<?> children = graph.getChildIterator();
        
        GWTGraph graphConverted = GWTGraph.create();
        while(children.hasNext()) {
        	UIDL child = (UIDL) children.next();
        	
        	if(child.getTag().equals("vertex")) {
        		
        		GWTVertex vertex = GWTVertex.create(child.getStringAttribute("id"), child.getIntAttribute("x"), child.getIntAttribute("y"));
        		boolean booleanAttribute = child.getBooleanAttribute("selected");
        		
				vertex.setSelected(booleanAttribute);
				graphConverted.addVertex(vertex);
        		
        	}else if(child.getTag().equals("edge")) {
        		GWTEdge edge = GWTEdge.create(graphConverted.findVertexById(child.getStringAttribute("source")), graphConverted.findVertexById( child.getStringAttribute("target") ));
        		graphConverted.addEdge(edge);
        	}
        	
        }
        
        setGraph(graphConverted);
        
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

}
