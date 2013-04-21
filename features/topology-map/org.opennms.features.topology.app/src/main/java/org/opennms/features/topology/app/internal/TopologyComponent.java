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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.GraphVisitor;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.MapViewManagerListener;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.GraphContainer.ChangeListener;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;


@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent implements ChangeListener, ValueChangeListener, MapViewManagerListener {
    
    public interface VertexUpdateListener{
        public void onVertexUpdate();
    }
    
    private static final long serialVersionUID = 1L;
    
	private final GraphContainer m_graphContainer;
    private Graph m_graph;
    private final List<MenuItemUpdateListener> m_menuItemStateListener = new ArrayList<MenuItemUpdateListener>();
    private final ContextMenuHandler m_contextMenuHandler;
    private final IconRepositoryManager m_iconRepoManager;
    private String m_activeTool = "pan";

    private Set<VertexUpdateListener> m_vertexUpdateListeners = new CopyOnWriteArraySet<VertexUpdateListener>();

    public TopologyComponent(GraphContainer dataSource, IconRepositoryManager iconRepositoryManager, ContextMenuHandler contextMenuHandler) {
	    m_graphContainer = dataSource;
	    m_iconRepoManager = iconRepositoryManager;
	    m_contextMenuHandler = contextMenuHandler;

	    setGraph(m_graphContainer.getGraph());
		
		m_graphContainer.getSelectionManager().addSelectionListener(new SelectionListener() {
			
			@Override
			public void selectionChanged(SelectionContext selectionContext) {
			    computeBoundsForSelected(selectionContext);
			}
			
		});
		
		m_graphContainer.getMapViewManager().addListener(this);
		m_graphContainer.addChangeListener(this);
		
		setScaleDataSource(m_graphContainer.getScaleProperty());
	}
	
	private void setScaleDataSource(Property scale) {
        // Listens the new data source if possible
        if (scale != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(scale.getClass())) {
            ((Property.ValueChangeNotifier) scale).addListener(this);
        }
    }
	
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("activeTool", m_activeTool);
        
        BoundingBox boundingBox = getBoundingBox();
        //System.out.println(m_viewManager);
        target.addAttribute("boundX", boundingBox.getX());
        target.addAttribute("boundY", boundingBox.getY());
        target.addAttribute("boundWidth", boundingBox.getWidth());
        target.addAttribute("boundHeight", boundingBox.getHeight());
        
		Graph graph = getGraph();
		//Set Status provider from the graph container because I may move it later
		GraphVisitor painter = new GraphPainter(m_graphContainer, graph.getLayout(), m_iconRepoManager, target, m_graphContainer.getStatusProvider());

		try {
			graph.visit(painter);
		} catch(PaintException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
        
        
    }

    private BoundingBox getBoundingBox() {
        
        return getViewManager().getCurrentBoundingBox();
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
            m_graphContainer.getSelectionManager().deselectAll();
        }
        
        if(variables.containsKey("clickedVertex")) {
            String vertexKey = (String) variables.get("clickedVertex");
            if((variables.containsKey("shiftKeyPressed") && (Boolean) variables.get("shiftKeyPressed") == true) 
                    || variables.containsKey("metaKeyPressed") && (Boolean) variables.get("metaKeyPressed") == true
                    || (variables.containsKey("ctrlKeyPressed") && (Boolean) variables.get("ctrlKeyPressed") == true  && !(((String)variables.get("platform")).indexOf("Mac") > 0)  )) {
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
        
        if(variables.containsKey("updateVertices")) {
            String[] vertices = (String[]) variables.get("updateVertices");
            for(String vUpdate : vertices) {
                updateVertex(vUpdate);
            }
            
            fireVertexUpdated();
            if(vertices.length > 0) {
                requestRepaint();
            }
            
        }
        
        if(variables.containsKey("scrollWheel")) {
            Map<String, Object> props = (Map<String, Object>) variables.get("scrollWheel");
            int x = (Integer) props.get("x");
            int y = (Integer) props.get("y");
            double scrollVal = (Double) props.get("scrollVal");
            getViewManager().zoomToPoint(getViewManager().getScale() + scrollVal, new Point(x, y));
        }
        
        if(variables.containsKey("clientCenterPoint")) {
            Map<String, Object> props = (Map<String, Object>) variables.get("clientCenterPoint");
            int x = (Integer) props.get("x");
            int y = (Integer) props.get("y"); 
            getViewManager().setCenter(new Point(x, y));
            
        }
        
        if(variables.containsKey("contextMenu")) {
            Map<String, Object> props = (Map<String, Object>) variables.get("contextMenu");
            
            
            int x = (Integer) props.get("x");
            int y = (Integer) props.get("y");
            
            String type = (String) props.get("type");

            Object target = null;
            if (type.toLowerCase().equals("vertex")) {
            	String targetKey = (String)props.get("target");
            	target = getGraph().getVertexByKey(targetKey);
            } else if (type.toLowerCase().equals("edge")) {
            	String targetKey = (String)props.get("target");
            	target = getGraph().getEdgeByKey(targetKey);
            }

            m_contextMenuHandler.show(target, x, y);
        }
        
        if(variables.containsKey("mapPhysicalBounds")) {
            Map<String, Object> bounds = (Map<String, Object>) variables.get("mapPhysicalBounds");
            Integer width = (Integer)bounds.get("width");
            Integer height = (Integer)bounds.get("height");
            
            getViewManager().setViewPort(width, height);
            
        }
        
        if(variables.containsKey("doubleClick")) {
            Map<String, Object> props = (Map<String, Object>) variables.get("doubleClick");
            int x = (Integer) props.get("x");
            int y = (Integer) props.get("y");
            
            double scale = getViewManager().getScale() + 0.25;
            getViewManager().zoomToPoint(scale, new Point(x, y));
        }
        
        updateMenuItems();
    }

	private void selectVertices(String... vertexKeys) {
		List<VertexRef> vertexRefs = new ArrayList<VertexRef>(vertexKeys.length);
		
		for(String vertexKey : vertexKeys) {
			vertexRefs.add(getGraph().getVertexByKey(vertexKey));
		}

		Collection<VertexRef> vertexTrees = m_graphContainer.getVertexRefForest(vertexRefs);
	    m_graphContainer.getSelectionManager().setSelectedVertexRefs(vertexTrees);
	}

	private void addVerticesToSelection(String... vertexKeys) {
		List<VertexRef> vertexRefs = new ArrayList<VertexRef>(vertexKeys.length);
		
		for(String vertexKey : vertexKeys) {
			vertexRefs.add(getGraph().getVertexByKey(vertexKey));
		}

		Collection<VertexRef> vertexTrees = m_graphContainer.getVertexRefForest(vertexRefs);
		
		m_graphContainer.getSelectionManager().selectVertexRefs(vertexTrees);
    }
    
	private void selectEdge(String edgeKey) {
		Edge edge = getGraph().getEdgeByKey(edgeKey);
		
		m_graphContainer.getSelectionManager().setSelectedEdgeRefs(Collections.singleton(edge));

	}

    private void updateVertex(String vertexUpdate) {
        String[] vertexProps = vertexUpdate.split("\\|");
        
        String id = vertexProps[0].split(",")[1];
        int x = (int) Double.parseDouble(vertexProps[1].split(",")[1]);
        int y = (int) Double.parseDouble(vertexProps[2].split(",")[1]);
        boolean selected = vertexProps[3].split(",")[1].equals("true");
        
        Vertex vertex = getGraph().getVertexByKey(id);
        
        getGraph().getLayout().setLocation(vertex, x, y);

        if (selected) {
        	m_graphContainer.getSelectionManager().selectVertexRefs(Collections.singleton(vertex));
        } else {
        	m_graphContainer.getSelectionManager().deselectVertexRefs(Collections.singleton(vertex));
        }
    }
    
	protected void setScale(double scale){
	    m_graphContainer.setScale(scale);
    }
    
    protected Graph getGraph() {
		return m_graph;
	}

	private void setGraph(Graph graph) {
		m_graph = graph;
		getViewManager().setMapBounds(graph.getLayout().getBounds());
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

	public void graphChanged(GraphContainer container) {
		Graph graph = container.getGraph();
        setGraph(graph);
		
		getViewManager().setMapBounds(graph.getLayout().getBounds());
		computeBoundsForSelected(m_graphContainer.getSelectionManager());
	}
	
	/**
	 * ValueChange listener for the scale property
	 */
    public void valueChange(ValueChangeEvent event) {
        
        double scale = (Double) event.getProperty().getValue();
        
        getViewManager().setScale(scale);
        
    }

    public void setActiveTool(String toolname) {
        if(!m_activeTool.equals(toolname)) {
            m_activeTool = toolname;
            requestRepaint();
        }
    }

    private void computeBoundsForSelected(SelectionContext selectionContext) {
        if(selectionContext.getSelectedVertexRefs().size() > 0) {
            Collection<? extends Vertex> visible = m_graphContainer.getGraph().getDisplayVertices();
            Collection<VertexRef> selected = selectionContext.getSelectedVertexRefs();
            Collection<VertexRef> vRefs = new ArrayList<VertexRef>();
            for(VertexRef vRef : selected) {
                if(visible.contains(vRef)) {
                    vRefs.add(vRef);
                }
            }
            
            getViewManager().setBoundingBox(m_graphContainer.getGraph().getLayout().computeBoundingBox(vRefs));
        	
        }else {
            getViewManager().setBoundingBox(m_graphContainer.getGraph().getLayout().getBounds());
        }
    }

    @Override
    public void boundingBoxChanged(MapViewManager viewManager) {
        setScale(viewManager.getScale());
        requestRepaint();
    }
    
    public MapViewManager getViewManager() {
        return m_graphContainer.getMapViewManager();
    }

    public void addVertexUpdateListener(VertexUpdateListener listener) {
        m_vertexUpdateListeners.add(listener);
    }
    
    private void fireVertexUpdated() {
        for(VertexUpdateListener listener : m_vertexUpdateListeners) {
            listener.onVertexUpdate();
        }
    }

}
