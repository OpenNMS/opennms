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

import com.vaadin.annotations.JavaScript;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.AbstractComponent;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.GraphContainer.ChangeListener;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.gwt.client.TopologyComponentServerRpc;
import org.opennms.features.topology.app.internal.gwt.client.TopologyComponentState;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@JavaScript({"gwt/public/topologywidget/js/d3.v3.js", "gwt/public/topologywidget/js/d3.interpolate-zoom.v0.js"})
public class TopologyComponent extends AbstractComponent implements ChangeListener, ValueChangeListener, MapViewManagerListener {
    
    TopologyComponentServerRpc m_rpc = new TopologyComponentServerRpc(){

        private static final long serialVersionUID = 6945103738578953304L;

        @Override
        public void doubleClicked(MouseEventDetails eventDetails) {
            double scale = getViewManager().getScale() + 0.25;
            getViewManager().zoomToPoint(scale, new Point(eventDetails.getClientX(), eventDetails.getClientY()));
        }

        @Override
        public void deselectAllItems() {
            m_graphContainer.getSelectionManager().deselectAll();
        }

        @Override
        public void edgeClicked(String edgeId) {
            selectEdge(edgeId);
        }

        @Override
        public void backgroundClicked() {
            m_graphContainer.getSelectionManager().deselectAll();
        }

        @Override
        public void scrollWheel(double scrollVal, int x, int y) {
            getViewManager().zoomToPoint(getViewManager().getScale() - scrollVal, new Point(x, y));
        }

        @Override
        public void mapPhysicalBounds(int width, int height) {
            getViewManager().setViewPort(width, height);
        }

        @Override
        public void marqueeSelection(String[] vertexKeys, MouseEventDetails eventDetails) {
            selectVertices(eventDetails.isShiftKey(), eventDetails.isCtrlKey(), vertexKeys);
        }

        @Override
        public void contextMenu(String target, String type, int x, int y) {

          Object menuTarget = null;
          if (type.toLowerCase().equals("vertex")) {
            String targetKey = target;
              menuTarget = getGraph().getVertexByKey(targetKey);
          } else if (type.toLowerCase().equals("edge")) {
            String targetKey = (String)target;
              menuTarget = getGraph().getEdgeByKey(targetKey);
          }

          m_contextMenuHandler.show(menuTarget, x, y);
            
        }

        @Override
        public void clientCenterPoint(int x, int y) {
          getViewManager().setCenter(new Point(x, y));
        }

        @Override
        public void vertexClicked(String vertexId, MouseEventDetails eventDetails, String platform) {
            selectVertices(eventDetails.isShiftKey(), eventDetails.isCtrlKey(), vertexId);
        }

        @Override
        public void updateVertices(List<String> vertices) {
            for(String vUpdate : vertices) {
                updateVertex(vUpdate);
            }
            
            fireVertexUpdated();
            if(vertices.size() > 0) {
                updateGraph();
            }
        }

        @Override
        public void backgroundDoubleClick(double x, double y) {
            //TODO: set the center point and zoom in by 25%
        }

    };
    
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
    private boolean m_blockSelectionEvents = false;

    private Set<VertexUpdateListener> m_vertexUpdateListeners = new CopyOnWriteArraySet<VertexUpdateListener>();

    public TopologyComponent(GraphContainer dataSource, IconRepositoryManager iconRepositoryManager, ContextMenuHandler contextMenuHandler) {
	    m_graphContainer = dataSource;
	    m_iconRepoManager = iconRepositoryManager;
	    m_contextMenuHandler = contextMenuHandler;
	    
	    registerRpc(m_rpc);
	    
	    setGraph(m_graphContainer.getGraph());
		
		m_graphContainer.getSelectionManager().addSelectionListener(new SelectionListener() {

			@Override
			public void selectionChanged(SelectionContext selectionContext) {
                if (!m_blockSelectionEvents) {
                    computeBoundsForSelected(selectionContext);
                }
                updateGraph();
              }
		});
		
		m_graphContainer.getMapViewManager().addListener(this);
		m_graphContainer.addChangeListener(this);
		
		setScaleDataSource(m_graphContainer.getScaleProperty());
		
		updateGraph();
	}
	
	private void setScaleDataSource(Property<Double> scale) {
        // Listens the new data source if possible
        if (scale != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(scale.getClass())) {
            ((Property.ValueChangeNotifier) scale).addValueChangeListener(this);
        }
    }
	
	@Override
	protected TopologyComponentState getState() {
	    return (TopologyComponentState) super.getState();
	}
	
	public void updateGraph() {
	    BoundingBox boundingBox = getBoundingBox();
	    getState().setBoundX(boundingBox.getX());
	    getState().setBoundY(boundingBox.getY());
	    getState().setBoundWidth(boundingBox.getWidth());
	    getState().setBoundHeight(boundingBox.getHeight());
	    getState().setActiveTool(m_activeTool);
	    
	    Graph graph = getGraph();
		//Set Status provider from the graph container because I may move it later
	    GraphVisitor painter = new GraphPainter(m_graphContainer, graph.getLayout(), m_iconRepoManager, m_graphContainer.getStatusProvider(), getState());
	    try {
            graph.visit(painter);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
    private BoundingBox getBoundingBox() {
        
        return getViewManager().getCurrentBoundingBox();
    }

    private void selectVertices(boolean shiftModifierPressed, boolean ctrlModifierPressed, String... vertexKeys) {
        m_blockSelectionEvents = true;
        List<VertexRef> vertexRefsToSelect = new ArrayList<VertexRef>(vertexKeys.length);
        List<VertexRef> vertexRefsToDeselect = new ArrayList<VertexRef>();
        boolean add = shiftModifierPressed || ctrlModifierPressed;
        for (String eachVertexKey : vertexKeys) {
            if (ctrlModifierPressed
                    && m_graphContainer.getSelectionManager().isVertexRefSelected(m_graph.getVertexByKey(eachVertexKey))) {
                vertexRefsToDeselect.add(getGraph().getVertexByKey(eachVertexKey)); //we want it to be unselected
            } else {
                vertexRefsToSelect.add(getGraph().getVertexByKey(eachVertexKey));
            }
        }
        if (add) { // we want to add, so add the already selected ones (except the explicit removed ones)
            vertexRefsToSelect.addAll(m_graphContainer.getSelectionManager().getSelectedVertexRefs());
            vertexRefsToSelect.removeAll(vertexRefsToDeselect);
        }
        m_graphContainer.getSelectionManager().deselectAll();
        m_graphContainer.getSelectionManager().selectVertexRefs( m_graphContainer.getVertexRefForest(vertexRefsToSelect) );
        m_blockSelectionEvents = false;
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

    @Override
	public void graphChanged(GraphContainer container) {
		Graph graph = container.getGraph();
        setGraph(graph);
		
		getViewManager().setMapBounds(graph.getLayout().getBounds());
		computeBoundsForSelected(m_graphContainer.getSelectionManager());
	}
	
	/**
	 * ValueChange listener for the scale property
	 */
    @Override
    public void valueChange(ValueChangeEvent event) {
        
        double scale = (Double) event.getProperty().getValue();
        
        getViewManager().setScale(scale);
        
    }

    public void setActiveTool(String toolname) {
        if(!m_activeTool.equals(toolname)) {
            m_activeTool = toolname;
            getState().setActiveTool(toolname);
            updateGraph();
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
        updateGraph();
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
