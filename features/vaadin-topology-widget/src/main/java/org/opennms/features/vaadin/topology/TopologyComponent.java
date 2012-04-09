package org.opennms.features.vaadin.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.features.vaadin.topology.gwt.client.Edge;
import org.opennms.features.vaadin.topology.gwt.client.Graph;
import org.opennms.features.vaadin.topology.gwt.client.VTopologyComponent;
import org.opennms.features.vaadin.topology.gwt.client.Vertex;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent implements Action.Container {
	
	

    @Override
    public void attach() {
        super.attach();
        setDescription("This is a description");
    }

    private Graph m_graph;
	private double m_scale = 1;
	private List<Action.Handler> m_actionHandlers = new CopyOnWriteArrayList<Action.Handler>();

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
        	target.addAttribute("selected", vert.isSelected());
        	target.endTag("vertex");
        }
        
        for(Edge edge : m_graph.getEdges()) {
        	target.startTag("edge");
        	target.addAttribute("source", edge.getSource().getId());
        	target.addAttribute("target", edge.getTarget().getId());
        	target.endTag("edge");
        }
        
        target.endTag("graph");
        
        target.addAttribute("actionList", getActionsList());
        
        
    }
    
    private String[] getActionsList() {
		List<String> actionList = new ArrayList<String>();
		for(Action.Handler handler : m_actionHandlers) {
			Action[] actions = handler.getActions(null, null);
			for(Action action : actions) {
				actionList.add(action.getCaption());
			}
		}
		return actionList.toArray(new String[0]);
	}

	@Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if(variables.containsKey("graph")) {
            String graph = (String) variables.get("graph");
            getApplication().getMainWindow().showNotification("" + graph);
            
        }
        
        if(variables.containsKey("clickedVertex")) {
        	String vertexId = (String) variables.get("clickedVertex");
            if(variables.containsKey("shiftKeyPressed") && (Boolean) variables.get("shiftKeyPressed") == true) {
        	    multiSelectVertex(vertexId);
        	}else {
        	    singleSelectVertex(vertexId);
        	}
        	
        }
        
        if(variables.containsKey("action")) {
        	System.err.println("I just received an action");
        	
        }
        
        if(variables.containsKey("updatedVertex")) {
            String vertexUpdate = (String) variables.get("updatedVertex");
            String[] vertexProps = vertexUpdate.split("\\|");
            
            String id = vertexProps[0].split(",")[1];
            int x = Integer.parseInt(vertexProps[1].split(",")[1]);
            int y = Integer.parseInt(vertexProps[2].split(",")[1]);
            boolean selected = vertexProps[3].split(",")[1] == "true" ;
            
            Vertex vertex = m_graph.getVertexById(id);
            vertex.setX(x);
            vertex.setY(y);
            vertex.setSelected(selected);
            
            requestRepaint();
        }
    }
    
    private void singleSelectVertex(String vertexId) {
        for(Vertex vertex : m_graph.getVertices()) {
            vertex.setSelected(false);
        }
        
        toggleSelectedVertex(vertexId);
    }

    private void multiSelectVertex(String vertexId) {
        toggleSelectedVertex(vertexId);
    }

    private void toggleSelectedVertex(String vertexId) {
		Vertex vertex = m_graph.getVertexById(vertexId);
		vertex.setSelected(!vertex.isSelected());
		
		requestRepaint();
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
		
		String id = graph.getNextId();
		Vertex vertex = new Vertex(id, 10, 10);
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

	public void addActionHandler(Handler actionHandler) {
		m_actionHandlers.add(actionHandler);
		
	}

	public void removeActionHandler(Handler actionHandler) {
		m_actionHandlers.remove(actionHandler);
		
	}
   

}
