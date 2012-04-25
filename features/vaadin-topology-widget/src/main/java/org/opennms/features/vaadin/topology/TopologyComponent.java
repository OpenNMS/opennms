package org.opennms.features.vaadin.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.features.vaadin.topology.gwt.client.VTopologyComponent;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.KeyMapper;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;

@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent implements Action.Container {
	
    public class MapManager {

        private double m_scale = 1;
        private double m_minScale = 0;
        private double m_maxScale = 1;
        private int m_clientX = 0;
        private int m_clientY = 0;
        
        public void setScale(double newScale) {
            if(m_scale != newScale) {
                if(newScale > getMaxScale()) {
                    m_scale = getMaxScale();
                }else if(newScale < getMinScale()) {
                    m_scale = getMinScale();
                }else {
                    m_scale = newScale;
                }
            }
        }

        public double getScale() {
            return m_scale;
        }

        public void setMinScale(double min) {
            m_minScale = min;
        }

        private double getMinScale() {
            return m_minScale;
        }

        private double getMaxScale() {
            return m_maxScale;
        }

        private void setMaxScale(double maxScale) {
            m_maxScale = maxScale;
        }

        public void setClientX(int clientX) {
            m_clientX = clientX;
        }

        public void setClientY(int clientY) {
            m_clientY = clientY;
        }

        public int getClientX() {
            return m_clientX;
        }

        public int getClientY() {
            return m_clientY;
        }
        
        
    }
    
	private KeyMapper m_actionMapper;
	private Slider m_scaleSlider;

    @Override
    public void attach() {
        super.attach();
        setDescription("This is a description");
    }

    private Graph m_graph;
	private List<Action.Handler> m_actionHandlers = new CopyOnWriteArrayList<Action.Handler>();
	private MapManager m_mapManager = new MapManager();

	public TopologyComponent() {
		m_graph = new Graph();
		
	}
	
	public void setScaleSlider(Slider slider) {
	    m_scaleSlider = slider;
	    m_mapManager.setMaxScale(m_scaleSlider.getMax());
	    m_mapManager.setMinScale(m_scaleSlider.getMin());
	}
	
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("scale", m_mapManager.getScale());
        target.addAttribute("clientX", m_mapManager.getClientX());
        target.addAttribute("clientY", m_mapManager.getClientY());
        
        Set<Action> actions = new HashSet<Action>();
		m_actionMapper = new KeyMapper();

		List<String> bgActionList = new ArrayList<String>();
		for(Action.Handler handler : m_actionHandlers) {
			Action[] bgActions = handler.getActions(null, null);
			for(Action action : bgActions) {
				bgActionList.add(m_actionMapper.key(action));
				actions.add(action);
			}
		}

		target.addAttribute("backgroundActions", bgActionList.toArray());
		
		
        target.startTag("graph");
        for(Vertex vert : m_graph.getVertices()) {
        	target.startTag("vertex");
        	target.addAttribute("id", vert.getId());
        	target.addAttribute("x", vert.getX());
        	target.addAttribute("y", vert.getY());
        	target.addAttribute("selected", vert.isSelected());
        	target.addAttribute("iconUrl", vert.getIconUrl());
        	
    		List<String> vertActionList = new ArrayList<String>();
    		for(Action.Handler handler : m_actionHandlers) {
    			Action[] vertActions = handler.getActions(vert.getItem(), null);
    			for(Action action : vertActions) {
    				vertActionList.add(m_actionMapper.key(action));
    				actions.add(action);
    			}
    		}

    		target.addAttribute("actionKeys", vertActionList.toArray());
        	target.endTag("vertex");
        }
        
        for(Edge edge : m_graph.getEdges()) {
        	target.startTag("edge");
        	target.addAttribute("source", edge.getSource().getId());
        	target.addAttribute("target", edge.getTarget().getId());

    		List<String> edgeActionList = new ArrayList<String>();
    		for(Action.Handler handler : m_actionHandlers) {
    			Action[] vertActions = handler.getActions(edge.getItem(), null);
    			for(Action action : vertActions) {
    				edgeActionList.add(m_actionMapper.key(action));
    				actions.add(action);
    			}
    		}


        	target.addAttribute("actionKeys", edgeActionList.toArray());
        	target.endTag("edge");
        }
        
        target.endTag("graph");
        
        
        
		target.startTag("actions");

		// send available actions
		for(Action action : actions) {
			target.startTag("action");
			target.addAttribute("key", m_actionMapper.key(action));
			if (action.getCaption() != null) {
				target.addAttribute("caption", action.getCaption());
			}
			if (action.getIcon() != null) {
				target.addAttribute("icon", action.getIcon());
			}
			target.endTag("action");
		}

		
		target.endTag("actions");

        
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
        	String value = (String) variables.get("action");
        	String[] data = value.split(",");
        	String targetId = data[0];
        	String actionKey = data[1];
        	
        	Vertex vertex = m_graph.getVertexById(targetId);
        	Action action = (Action) m_actionMapper.get(actionKey);
        	
        	for(Handler handler : m_actionHandlers) {
        		handler.handleAction(action, this, vertex);
        	}
        	
        }
        
        if(variables.containsKey("updatedVertex")) {
            String vertexUpdate = (String) variables.get("updatedVertex");
            String[] vertexProps = vertexUpdate.split("\\|");
            
            String id = vertexProps[0].split(",")[1];
            int x = (int) Double.parseDouble(vertexProps[1].split(",")[1]);
            int y = (int) Double.parseDouble(vertexProps[2].split(",")[1]);
            boolean selected = vertexProps[3].split(",")[1] == "true" ;
            
            Vertex vertex = m_graph.getVertexById(id);
            vertex.setX(x);
            vertex.setY(y);
            vertex.setSelected(selected);
            
            requestRepaint();
        }
        
        if(variables.containsKey("mapScale")) {
            double newScale = (Double)variables.get("mapScale");
            setScale(newScale);
        }
        
        if(variables.containsKey("clientX")) {
            int clientX = (Integer) variables.get("clientX");
            m_mapManager.setClientX(clientX);
        }
        
        if(variables.containsKey("clientY")) {
            int clientY = (Integer) variables.get("clientY");
            m_mapManager.setClientY(clientY);
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
    	m_mapManager.setScale(scale);
    	try {
            m_scaleSlider.setValue(scale);
        } catch (ValueOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	requestRepaint();
    }
    
    public Graph getGraph() {
		return m_graph;
	}

	public void addRandomNode() {
		Graph graph = getGraph();

		String id = graph.getNextId();
		Vertex vertex = new Vertex(id);
		graph.addVertex(vertex);
		graph.addEdge(new Edge(graph.getVertices().get(0), vertex));
		graph.updateLayout();
		
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

    public void redoLayout() {
        m_graph.updateLayout();
        requestRepaint();
    }

    public void addVertexTo(Vertex target) {
        m_graph.addVertexTo(target);
        requestRepaint();
    }

    public void removeVertex(Vertex target) {
        m_graph.removeVertex(target);
        requestRepaint();
    }

	public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
		m_graph.setLayoutAlgorithm(layoutAlgorithm);
		requestRepaint();
	}

    public void addSwitchVertexTo(Vertex source) {
        m_graph.addSwitchVertex(source);
        requestRepaint();
    }

    public Double getScale() {
        return m_mapManager.getScale();
    }
   

}
