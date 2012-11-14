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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SelectionManager.SelectionListener;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;


@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent implements Action.Container, ItemSetChangeListener, PropertySetChangeListener, ValueChangeListener {

    private static final long serialVersionUID = 1L;
	
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
    
	private GraphContainer m_graphContainer;
	private Property m_scale;
    private TopoGraph m_graph;
	private MapManager m_mapManager = new MapManager();
    private List<MenuItemUpdateListener> m_menuItemStateListener = new ArrayList<MenuItemUpdateListener>();
    private ContextMenuHandler m_contextMenuHandler;
    private IconRepositoryManager m_iconRepoManager;
    private boolean m_panToSelection = false;
    private boolean m_fitToView = true;
    private boolean m_scaleUpdateFromUI = false;
    private String m_activeTool = "pan";

	public TopologyComponent(GraphContainer dataSource) {
		setGraph(new TopoGraph(dataSource));
		
		m_graphContainer = dataSource;

		m_graphContainer.getSelectionManager().addSelectionListener(new SelectionListener() {
			
			@Override
			public void selectionChanged(SelectionManager selectionManager) {
				requestRepaint();
			}
		});

		
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
        target.addAttribute("activeTool", m_activeTool);
        
        target.addAttribute("panToSelection", getPanToSelection());
        if (getPanToSelection()) {
            
        }
        setPanToSelection(false);
        
        target.addAttribute("fitToView", isFitToView());
        setFitToView(false);
		GraphVisitor painter = new GraphPainter(m_graphContainer, m_iconRepoManager, target);

		TopoGraph r = getGraph();
		try {
			r.visit(painter);
		} catch(Exception e) {
			throw new PaintException(e.getMessage());
		}
        
        
    }

	public boolean isFitToView() {
        return m_fitToView;
    }
    
    public void setFitToView(boolean fitToView) {
        m_fitToView  = fitToView;
    }

    private void setPanToSelection(boolean b) {
        m_panToSelection  = b;
    }

    private boolean getPanToSelection() {
        return m_panToSelection;
    }

    /**
     * Main vaadin method for receiving communication from the Front End
     * 
     */
	@SuppressWarnings("unchecked")
	@Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if(variables.containsKey("graph")) {
            String graph = (String) variables.get("graph");
            getApplication().getMainWindow().showNotification("" + graph);
            
        }
        
        if(variables.containsKey("clickedEdge")) {
            String edgeId = (String) variables.get("clickedEdge");
            selectEdge(edgeId);
        }
        
        if(variables.containsKey("clickedBackground")) {
            getSelectionManager().deselectAll();
        }
        
        if(variables.containsKey("clickedVertex")) {
        	String vertexKey = (String) variables.get("clickedVertex");
            if(variables.containsKey("shiftKeyPressed") && (Boolean) variables.get("shiftKeyPressed") == true) {
        	    addVerticesToSelection(vertexKey);
        	}else {
        	    selectVertices(vertexKey);
        	}
        	
        }
        
        if(variables.containsKey("marqueeSelection")) {
            String[] vertexKeys = (String[]) variables.get("marqueeSelection");
            if(variables.containsKey("shiftKeyPressed") && (Boolean) variables.get("shiftKeyPressed") == true) {
            	addVerticesToSelection(vertexKeys);
            } else {
            	selectVertices(vertexKeys);
            }
            
        }
        
        if(variables.containsKey("updatedVertex")) {
            String vertexUpdate = (String) variables.get("updatedVertex");
            updateVertex(vertexUpdate);
            
            requestRepaint();
        }
        
        if(variables.containsKey("updateVertices")) {
            String[] vertices = (String[]) variables.get("updateVertices");
            for(String vUpdate : vertices) {
                updateVertex(vUpdate);
            }
            
            if(vertices.length > 0) {
                requestRepaint();
            }
            
        }
        
        if(variables.containsKey("mapScale")) {
            double newScale = (Double)variables.get("mapScale");
            setScaleUpdateFromUI(true);
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
        
        if(variables.containsKey("contextMenu")) {
            Map<String, Object> props = (Map<String, Object>) variables.get("contextMenu");
            
            String type = (String) props.get("type");
            
            int x = (Integer) props.get("x");
            int y = (Integer) props.get("y");
            Object itemId = (Object)props.get("target");

            if (type.toLowerCase().equals("vertex")) {
	                TopoVertex vertex = getGraph().getVertexByKey((String)itemId);
	                itemId = vertex.getItemId();
            }

            getContextMenuHandler().show(itemId, x, y);
        }
        
        updateMenuItems();
    }

	private void selectVertices(String... vertexKeys) {
		List<?> itemIds = getGraph().getVertexItemIdsForKeys(Arrays.asList(vertexKeys));
	    Collection<?> vertexIds = m_graphContainer.getVertexForest(itemIds);

	    getSelectionManager().setSelectedVertices(vertexIds);
	}

	private SelectionManager getSelectionManager() {
		return m_graphContainer.getSelectionManager();
	}

	private void selectEdge(String edgeKey) {
		Object edgeId = getGraph().getEdgeByKey(edgeKey).getItemId();
		
		getSelectionManager().setSelectedEdges(Collections.singleton(edgeId));

	}

	private void deselectAll() {
		getSelectionManager().deselectAll();
	}

    private void setScaleUpdateFromUI(boolean scaleUpdateFromUI) {
        m_scaleUpdateFromUI  = scaleUpdateFromUI;
    }
    
    private boolean isScaleUpdateFromUI() {
        return m_scaleUpdateFromUI;
    }

    private void updateVertex(String vertexUpdate) {
        String[] vertexProps = vertexUpdate.split("\\|");
        
        String id = vertexProps[0].split(",")[1];
        int x = (int) Double.parseDouble(vertexProps[1].split(",")[1]);
        int y = (int) Double.parseDouble(vertexProps[2].split(",")[1]);
        boolean selected = vertexProps[3].split(",")[1].equals("true");
        
        TopoVertex vertex = getGraph().getVertexByKey(id);
        vertex.setX(x);
        vertex.setY(y);
        vertex.setSelected(selected);
    }
    
	public void selectVerticesByItemId(Collection<Object> itemIds) {
    	
        deselectAll();
        
        getSelectionManager().selectVertices(itemIds);

        if(itemIds.size() > 0) {
            setPanToSelection(true);
            requestRepaint();
        }
    }
    
	private void addVerticesToSelection(String... vertexKeys) {
		
		List<?> itemIds = getGraph().getVertexItemIdsForKeys(Arrays.asList(vertexKeys));
		
		Collection<?> vertexIds = m_graphContainer.getVertexForest(itemIds);
		getSelectionManager().selectVertices(vertexIds);
    }
    
	protected void setScale(double scale){
	    m_scale.setValue(scale);
    }
    
    protected TopoGraph getGraph() {
		return m_graph;
	}

	public void addActionHandler(Handler actionHandler) {
	}
	
	public void removeActionHandler(Handler actionHandler) {
	}
	
	public void addMenuItemStateListener(MenuItemUpdateListener listener) {
        m_menuItemStateListener.add(listener);
    }
	
	public void removeMenuItemStateListener(MenuItemUpdateListener listener) {
	    m_menuItemStateListener.remove(listener);
	}
	
	private void updateMenuItems() {
	    for(MenuItemUpdateListener listener : m_menuItemStateListener) {
	        listener.updateMenuItems();
	    }
	}

	private void setGraph(TopoGraph graph) {
		m_graph = graph;
	}
	
	public void setContainerDataSource(GraphContainer graphContainer) {
		getGraph().setDataSource(graphContainer);
		m_graphContainer = graphContainer;
		m_graphContainer.getVertexContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getVertexContainer().addListener((PropertySetChangeListener) this);
		
		m_graphContainer.getEdgeContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getEdgeContainer().addListener((PropertySetChangeListener) this);
	}

	public void containerItemSetChange(ItemSetChangeEvent event) {
		getGraph().update();
		setFitToView(true);
		requestRepaint();
	}

	public void containerPropertySetChange(PropertySetChangeEvent event) {
		getGraph().update();
		requestRepaint();
	}

    public void valueChange(ValueChangeEvent event) {
        double scale = (Double) m_scale.getValue();
        if(scale == 0) {
            m_scale.setValue(0.01);
        }
        
        if(!isScaleUpdateFromUI()) {
            requestRepaint();
            setScaleUpdateFromUI(false);
        }else {
            setScaleUpdateFromUI(false);
        }
    }

    public ContextMenuHandler getContextMenuHandler() {
        return m_contextMenuHandler;
    }

    public void setContextMenuHandler(ContextMenuHandler contextMenuHandler) {
        m_contextMenuHandler = contextMenuHandler;
    }

    public IconRepositoryManager getIconRepoManager() {
        return m_iconRepoManager;
    }

    public void setIconRepoManager(IconRepositoryManager iconRepoManager) {
        m_iconRepoManager = iconRepoManager;
    }

    public void setActiveTool(String toolname) {
        if(!m_activeTool.equals(toolname)) {
            m_activeTool = toolname;
            requestRepaint();
        }
    }

    public Collection<?> getItemIdsForSelectedVertices() {
        Collection<?> vItemIds = m_graphContainer.getVertexIds();
        List<Object> selectedIds = new ArrayList<Object>(); 
        
        for(Object itemId : vItemIds) {
            if(getGraph().getVertexByItemId(itemId).isSelected()) {
                selectedIds.add(itemId);
            }
        }
        
        return selectedIds;
    }

}
