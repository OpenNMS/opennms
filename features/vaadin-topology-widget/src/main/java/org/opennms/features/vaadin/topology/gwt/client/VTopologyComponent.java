package org.opennms.features.vaadin.topology.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.topology.gwt.client.d3.D3;
import org.opennms.features.vaadin.topology.gwt.client.d3.D3Events;
import org.opennms.features.vaadin.topology.gwt.client.d3.Func;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

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
    private Graph m_graph;
	private D3 m_vertexGroup;
	private double m_scale;
    private boolean m_firstTime = true;
    private D3 m_edgeGroup;
    
    @UiField
    Button m_saveButton;
    
    public VTopologyComponent() {
        initWidget(uiBinder.createAndBindUi(this));
        
        m_graph = new Graph();
        
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        D3 d3 = getD3();
        m_width = 300;
        m_height = 300;
        m_svg = d3.select("#chart-2").append("svg").attr("width", 300).attr("height", 200);
        m_edgeGroup = m_svg.append("g").attr("transform", "scale(1)");
        m_vertexGroup = m_svg.append("g").attr("transform", "scale(1)");
        
    }
    
    @UiHandler("m_saveButton")
    public void onSaveButtonClick(ClickEvent e) {
        Command command = new Command() {

            public void execute() {
                m_client.updateVariable(m_paintableId, "graph", GraphJSONConverter.convertGraphToJSON(m_graph), true);
            }
        };
        
        if(BrowserInfo.get().isWebkit()) {
            Scheduler.get().scheduleDeferred(command);
        }else {
            command.execute();
        }
    };

    private void drawGraph(Graph graph) {
        D3 lines = m_edgeGroup.selectAll("line")
                .data(graph.getEdges().toArray(new Edge[0]), new Func<String, Edge>() {

					public String call(Edge edge, int index) {
						String edgeId = edge.getId();
						return edgeId;
					}
                	
                });
        
        D3 vertexGroup = m_vertexGroup.selectAll(".little")
                .data(graph.getVertices().toArray(new Vertex[0]), new Func<String, Vertex>() {

					public String call(Vertex param, int index) {
						return "" + param.getId();
					}
                	
                });
        //Exits
        lines.exit().transition().duration(500).attr("opacity", 0).remove();
        vertexGroup.exit().transition().duration(500).attr("opacity", 0).remove();
        
        //Updates
        lines.transition().delay(500).duration(500)
                .attr("x1", getX1())
                .attr("x2", getX2())
                .attr("y1", getY1())
                .attr("y2", getY2())
                .attr("opacity", 1);
        
        vertexGroup.transition().delay(500).duration(500)
                .attr("transform", getTranslation())
                .attr("opacity", 1);
		
		D3 updateCircle = vertexGroup.select("circle");
		//Window.alert("updateCircle: " + updateCircle);
		updateCircle.style("fill", selectedFill("Update"));
        
        //Enters
        lines.enter().append("line")
                .attr("opacity", 0)
                .attr("x1", getX1())
                .attr("x2", getX2())
                .attr("y1", getY1())
                .attr("y2", getY2())
                .style("stroke", "#ccc").transition().delay(1000).duration(500)
                .attr("opacity", 1);
        
        D3 vertex = vertexGroup.enter().append("g").attr("transform", getTranslation())
                .attr("opacity", 0)
                .attr("class", "little").on(D3Events.CLICK.event(), new D3Events.Handler<Vertex>(){

					public void call(Vertex vertex, int index) {
						m_client.updateVariable(m_paintableId, "clickedVertex", vertex.getId(), true);
						
					}
				});
                
        vertex.append("circle").attr("r", 9).style("fill", selectedFill("Enter"));
        
        vertex.append("text").attr("dy", ".35em")
            .attr("text-anchor", "middle").style("fill", "white")
            .text(getVertexId());
        
        vertex.transition().delay(1000).duration(500).attr("opacity", 1);
                
        
	}

	private Func<String, Vertex> selectedFill(final String caller) {
		return new Func<String, Vertex>(){

			public String call(Vertex vertex, int index) {
				
				//Window.alert("Caller " + caller + " :: Print vertex: " + vertex);
				
				return vertex.isSelected() ? "blue" : "black";
			}
		};
	}
	
	private static Func<String, Vertex> getTranslation() {
		return new Func<String, Vertex>() {

			public String call(Vertex datum, int index) {
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

    private static native JavaScriptObject getEdgeId() /*-{
        return function(d){
            var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Edge::getId()();
            return retVal;
        }
    }-*/;
    
    private static native JavaScriptObject getVertexId() /*-{
        return function(d){
            var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Vertex::getId()();
            return retVal;
        }
    }-*/;
    
    private static native JavaScriptObject getX1() /*-{
		
		return function(d, i){
			var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Edge::getX1()();
			return retVal;
		};
	}-*/;
    
    private static native JavaScriptObject getX2() /*-{
	
		return function(d, i){
			var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Edge::getX2()();
			return retVal;
		};
	}-*/;
    
    private static native JavaScriptObject getY1() /*-{
	
		return function(d, i){
			var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Edge::getY1()();
			return retVal;
		};
	}-*/;
    
    private static native JavaScriptObject getY2() /*-{
	
		return function(d, i){
			var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Edge::getY2()();
			return retVal;
		};
	}-*/;

	private static native JavaScriptObject getX() /*-{
    
    	return function(d, i){
    	     var retVal = d.@org.opennms.features.vaadin.topology.gwt.client.Vertex::getX()();
    	     return retVal;
    	};
    }-*/;
    
    private static native JavaScriptObject getY() /*-{
    	return function(d, i){
    		return d.@org.opennms.features.vaadin.topology.gwt.client.Vertex::getY()();
    	};
    }-*/;
    
    private static native JavaScriptObject getAttr(String attr) /*-{
    	return function(d, i) { return d[attr]; };
    }-*/;

	private void drawCircles(int[] data) {

    	//Window.alert("Received data " + data);


    	JavaScriptObject x = getD3().scale().ordinal().domain(data).rangePoints(rangeArray(m_width), 1);
    	final JavaScriptObject y = getD3().scale().ordinal().domain(data).rangePoints(rangeArray(m_height), 1);


    	final D3 circle = m_svg.selectAll(".little")
    			.data(data)
    			.enter()
    			.append("circle")
    			.attr("class", "little")
    			.attr("cx", x)
    			.attr("cy", y)
    			.attr("r", 12);

    	circle.style("fill", "#aaa").attr("r", 12).attr("cy", y);
    	circle.transition().duration(500).delay(0).style("fill", "steelBlue");
    	circle.transition().duration(500).delay(500).attr("cy", 90);
    	circle.transition().duration(500).delay(1000).attr("r", 30);
    }
    
    private final native void drawCirclesJS(int[] data, JavaScriptObject svg, int w, int h) /*-{
    
    
        var d3 =$wnd.d3;
    	var x = d3.scale.ordinal().domain(data).rangePoints([0, w], 1);
    	var y = d3.scale.ordinal().domain(data).rangePoints([0, h], 2);

    	var circle = svg.selectAll(".little").data(data);
    			
    	circle.enter()
    			.append("circle")
    			.attr("class", "little")
    			.attr("cx", x)
    			.attr("cy", y)
    			.attr("r", 0);

    	circle.style("fill", "#aaa")
    	circle.exit().transition().duration(1000).delay(0).attr("cx", x).attr("r", 0).remove();
    	circle.transition().duration(1000).delay(0).attr("r", 12).attr("cx", x).attr("cy", y);
    	circle.transition().duration(500).delay(1500).style("fill", "steelBlue");
    	circle.transition().duration(500).delay(2000).attr("cy", 90);
    	circle.transition().duration(500).delay(2500).attr("r", 30);
    }-*/;
    
    
    private static native JsArray getTestArray() /*-{
        return [32, 55, 112];
    }-*/;
    
    private static native JsArray rangeArray(int range) /*-{
        return [0, range];
    }-*/;
    
    private native D3 getD3() /*-{
        return $wnd.d3;
    }-*/;
    

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        
        if(client.updateComponent(this, uidl, true)) {
            return;
        }
        
        m_client = client;
        m_paintableId = uidl.getId();
        
        setScale(uidl.getDoubleAttribute("scale"));
        UIDL graph = uidl.getChildByTagName("graph");
        Iterator<?> children = graph.getChildIterator();
        
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Edge> edges = new ArrayList<Edge>();
        Map<Integer, Vertex> idMap = new HashMap<Integer, Vertex>();
        while(children.hasNext()) {
        	UIDL child = (UIDL) children.next();
        	
        	if(child.getTag().equals("vertex")) {
        		
        		Vertex vertex = new Vertex(child.getIntAttribute("id"), child.getIntAttribute("x"), child.getIntAttribute("y"));
        		boolean booleanAttribute = child.getBooleanAttribute("selected");
        		//Window.alert("selected: " + booleanAttribute);
        		//You were here.
        		
				vertex.setSelected(booleanAttribute);
				vertices.add(vertex);
        		idMap.put(vertex.getId(), vertex);
        		
        	}else if(child.getTag().equals("edge")) {
        		edges.add(new Edge( idMap.get(child.getIntAttribute("source")), idMap.get( child.getIntAttribute("target")) ));
        	}
        	
        }
        
        Graph graphConverted = new Graph(vertices, edges);
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

	private void setGraph(Graph graph) {
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
