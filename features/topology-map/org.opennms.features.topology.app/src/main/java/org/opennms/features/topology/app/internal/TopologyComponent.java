package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.KeyMapper;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;


@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent implements Action.Container, ItemSetChangeListener, PropertySetChangeListener, ValueChangeListener {
	
    public class MapManager {

        private int m_clientX = 0;
        private int m_clientY = 0;
        
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
	private GraphContainer m_graphContainer;
	private Property m_scale;

    @Override
    public void attach() {
        super.attach();
        setDescription("This is a description");
    }

    private Graph m_graph;
	private List<Action.Handler> m_actionHandlers = new ArrayList<Action.Handler>();
	private MapManager m_mapManager = new MapManager();
    private List<MenuItemUpdateListener> m_menuItemStateListener = new ArrayList<MenuItemUpdateListener>();

	public TopologyComponent(GraphContainer dataSource) {
		setGraph(new Graph(dataSource));
		m_graphContainer = dataSource;
		m_graphContainer.getVertexContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getVertexContainer().addListener((PropertySetChangeListener) this);
		
		m_graphContainer.getEdgeContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getEdgeContainer().addListener((PropertySetChangeListener) this);
		
		Property scale = m_graphContainer.getProperty(DisplayState.SCALE);
		setScaleDataSource(scale);
		
	}
	
	private void setScaleDataSource(Property scale) {
	    // Stops listening the old data source changes
        if (m_scale != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(m_scale.getClass())) {
            ((Property.ValueChangeNotifier) m_scale).removeListener(this);
        }

        // Sets the new data source
        m_scale = scale;

        // Listens the new data source if possible
        if (m_scale != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(m_scale.getClass())) {
            ((Property.ValueChangeNotifier) m_scale).addListener(this);
        }
    }
	
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("scale", (Double)m_scale.getValue());
        target.addAttribute("clientX", m_mapManager.getClientX());
        target.addAttribute("clientY", m_mapManager.getClientY());
        target.addAttribute("semanticZoomLevel", m_graphContainer.getSemanticZoomLevel());
        
        Set<Action> actions = new HashSet<Action>();
		m_actionMapper = new KeyMapper();

		List<String> bgActionList = new ArrayList<String>();
		Object t = null;
		Object s = null;
		List<Handler> actionHandlers = m_actionHandlers;
		List<Action> bgSortingList = sortActionHandlers(m_actionHandlers, t, s);
		for(Action action : bgSortingList) {
		    bgActionList.add(m_actionMapper.key(action));
		    actions.add(action);
		}
		
		
		target.addAttribute("backgroundActions", bgActionList.toArray());
		
		
        target.startTag("graph");
        for (Vertex group : getGraph().getVertices()) {
        	if (!group.isLeaf()) {
        		target.startTag("group");
        		target.addAttribute("key", group.getKey());
        		target.addAttribute("x", group.getX());
        		target.addAttribute("y", group.getY());
        		target.addAttribute("selected", group.isSelected());
        		target.addAttribute("iconUrl", group.getIconUrl());
        		target.addAttribute("semanticZoomLevel", group.getSemanticZoomLevel());
        		target.addAttribute("label", group.getLabel());

        		List<String> groupActionList = new ArrayList<String>();
        		List<Action> groupSortedList = sortActionHandlers(m_actionHandlers, group.getGroupId(), null); 
        		for(Action action : groupSortedList) {
        		    groupActionList.add(m_actionMapper.key(action));
        		    actions.add(action);
        		}
        		
    		    
        		target.addAttribute("actionKeys", groupActionList.toArray());
        		target.endTag("group");

        	}
        }
        
        
        for(Vertex vert : getGraph().getVertices()) {
        	if (vert.isLeaf()) {
        		target.startTag("vertex");
        		target.addAttribute("key", vert.getKey());
        		target.addAttribute("x", vert.getX());
        		target.addAttribute("y", vert.getY());
        		target.addAttribute("selected", vert.isSelected());
        		target.addAttribute("iconUrl", vert.getIconUrl());
        		target.addAttribute("semanticZoomLevel", vert.getSemanticZoomLevel());
        		if (vert.getGroupId() != null) {
        			target.addAttribute("groupKey", vert.getGroupKey());
        		}
        		target.addAttribute("label", vert.getLabel());

        		List<String> vertActionList = new ArrayList<String>();
        		List<Action> vertActionSortedList = sortActionHandlers(m_actionHandlers, vert.getItemId(), null);
        		
        		for(Action action : vertActionSortedList) {
        		    vertActionList.add(m_actionMapper.key(action));
        		    actions.add(action);
        		}
        		
        		target.addAttribute("actionKeys", vertActionList.toArray());
        		target.endTag("vertex");
        	}
        }
        
        for(Edge edge : getGraph().getEdges()) {
        	target.startTag("edge");
        	target.addAttribute("key", edge.getKey());
        	target.addAttribute("source", edge.getSource().getKey());
        	target.addAttribute("target", edge.getTarget().getKey());

    		List<String> edgeActionList = new ArrayList<String>();
    		List<Action> edgeSortedActionList = sortActionHandlers(m_actionHandlers, edge.getItemId(), null);
    		for(Action action : edgeSortedActionList) {
    		    edgeActionList.add(m_actionMapper.key(action));
    		    actions.add(action);
    		}
    		
        	target.addAttribute("actionKeys", edgeActionList.toArray());
        	target.endTag("edge");
        }
        
        for (Vertex group : getGraph().getVertices()) {
        	if (!group.isLeaf()) {
        		if (group.getGroupId() != null) {
        			target.startTag("groupParent");
        			target.addAttribute("key", group.getKey());
        			target.addAttribute("parentKey", group.getGroupKey());
        			
        			target.endTag("groupParent");
        		}
        	}
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

    private List<Action> sortActionHandlers(List<Handler> actionHandlers, Object target, Object sender) {
        List<Action> sortingList = new ArrayList<Action>();
        for(Action.Handler handler : actionHandlers) {
			Action[] bgActions = handler.getActions(target, sender);
			for(Action action : bgActions) {
			    sortingList.add(action);
			}
		}
		sortActions(sortingList);
        return sortingList;
    }

    private void sortActions(List<Action> bgActions) {
        Collections.sort(bgActions, new Comparator<Action>() {

            @Override
            public int compare(Action o1, Action o2) {
                return o1.getCaption().compareTo(o2.getCaption());
            }
        });
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
        	
        	Vertex vertex = getGraph().getVertexByKey(targetId);
        	Action action = (Action) m_actionMapper.get(actionKey);
        	
        	for(Handler handler : m_actionHandlers) {
        		handler.handleAction(action, this, vertex == null ? null : vertex.getItemId());
        	}
        	
        }
        
        if(variables.containsKey("updatedVertex")) {
            String vertexUpdate = (String) variables.get("updatedVertex");
            String[] vertexProps = vertexUpdate.split("\\|");
            
            String id = vertexProps[0].split(",")[1];
            int x = (int) Double.parseDouble(vertexProps[1].split(",")[1]);
            int y = (int) Double.parseDouble(vertexProps[2].split(",")[1]);
            boolean selected = vertexProps[3].split(",")[1] == "true" ;
            
            Vertex vertex = getGraph().getVertexByKey(id);
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
        
        updateMenuItems();
    }
    
    private void singleSelectVertex(String vertexId) {
        for(Vertex vertex : getGraph().getVertices()) {
            vertex.setSelected(false);
        }
        
        toggleSelectedVertex(vertexId);
    }

    private void multiSelectVertex(String vertexId) {
        toggleSelectedVertex(vertexId);
    }

    private void toggleSelectedVertex(String vertexId) {
		Vertex vertex = getGraph().getVertexByKey(vertexId);
		vertex.setSelected(!vertex.isSelected());
		
		requestRepaint();
	}

	public void setScale(double scale){
	    m_scale.setValue(scale);
    }
    
    protected Graph getGraph() {
		return m_graph;
	}

	public void addActionHandler(Handler actionHandler) {
		m_actionHandlers.add(actionHandler);
	}
	
	public void removeActionHandler(Handler actionHandler) {
		m_actionHandlers.remove(actionHandler);
		
	}
	
	public void addMenuItemStateListener(MenuItemUpdateListener listener) {
        m_menuItemStateListener .add(listener);
    }
	
	public void removeMenuItemStateListener(MenuItemUpdateListener listener) {
	    m_menuItemStateListener.remove(listener);
	}
	
	private void updateMenuItems() {
	    for(MenuItemUpdateListener listener : m_menuItemStateListener) {
	        listener.updateMenuItems();
	    }
	}

	private void setGraph(Graph graph) {
		m_graph = graph;
	}
	
	public void setContainerDataSource(GraphContainer graphContainer) {
		m_graph.setDataSource(graphContainer);
		m_graphContainer = graphContainer;
		m_graphContainer.getVertexContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getVertexContainer().addListener((PropertySetChangeListener) this);
		
		m_graphContainer.getEdgeContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getEdgeContainer().addListener((PropertySetChangeListener) this);
	}

	public void containerItemSetChange(ItemSetChangeEvent event) {
		m_graph.update();
		requestRepaint();
	}

	public void containerPropertySetChange(PropertySetChangeEvent event) {
		m_graph.update();
		requestRepaint();
	}

    public void valueChange(ValueChangeEvent event) {
        
        //Request repaint when a value changes, currently we are only listening to the scale property
        requestRepaint();
    }
   

}
