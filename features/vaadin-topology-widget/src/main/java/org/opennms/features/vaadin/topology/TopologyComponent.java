package org.opennms.features.vaadin.topology;

import java.util.Map;

import org.opennms.features.vaadin.topology.gwt.client.Edge;
import org.opennms.features.vaadin.topology.gwt.client.Graph;
import org.opennms.features.vaadin.topology.gwt.client.VTopologyComponent;
import org.opennms.features.vaadin.topology.gwt.client.Vertex;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent {
	
	

    private Graph m_graph;
	private double m_scale = 1;

	public TopologyComponent() {
		m_graph = new Graph();
	}
	
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("scale", getScale());
        
        target.startTag("graph");
        for(Vertex vert : m_graph.getVertices()) {
        	target.startTag("vertex");
        	target.addAttribute("id", vert.getId());
        	target.addAttribute("x", vert.getX());
        	target.addAttribute("y", vert.getY());
        	target.endTag("vertex");
        }
        
        for(Edge edge : m_graph.getEdges()) {
        	target.startTag("edge");
        	target.addAttribute("source", edge.getSource().getId());
        	target.addAttribute("target", edge.getTarget().getId());
        	target.endTag("edge");
        }
        
        target.endTag("graph");
    }
    
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if(variables.containsKey("graph")) {
            String graph = (String) variables.get("graph");
            getApplication().getMainWindow().showNotification("I got a new graph: now how do I get the data? Do I have vert data? " + graph);
            
            
        }
    }
    
    public void setScale(double scale){
    	m_scale = scale;
    	
    	requestRepaint();
    }
    
    public double getScale(){
    	return m_scale;
    }

	public Graph getGraph() {
		
		return m_graph;
	}

	void addRandomNode() {
		Graph graph = getGraph();
		int x = (int) (Math.random() * 100);
		int y = (int) (Math.random() * 100);
		
		int id = graph.getVertices().size();
		Vertex vertex = new Vertex(id, id*10, id*10);
		graph.addVertex(vertex);
		graph.addEdge(new Edge(graph.getVertices().get(0), vertex));
		
		requestRepaint();
	}

    public void resetGraph() {
        m_graph = new Graph();
        requestRepaint();
    }

    public void removeVertex() {
        m_graph.removeRandomVertext();
        requestRepaint();
    }
   

}
