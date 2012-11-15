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

package org.opennms.features.topology.api;

import java.util.Collection;

import org.opennms.features.topology.api.topo.GraphProvider;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;

public interface GraphContainer extends DisplayState {

    public VertexContainer<?, ?> getVertexContainer();

    public BeanContainer<?, ?> getEdgeContainer();

    public Item getVertexItem(Object vertexId);

    public Item getEdgeItem(Object edgeId);

    public Collection<?> getEndPointIdsForEdge(Object edgeId);

    public Collection<?> getEdgeIdsForVertex(Object vertexId);

    public Object getVertexItemIdForVertexKey(Object key);
    
    public GraphProvider getBaseTopology();
    
    public void setBaseTopology(GraphProvider graphProvider);
    
    public Object getGroupId(Object vertexId);
    
    public int getX(Object vertexId);
    
    public void setX(Object vertexId, int x);
    
    public int getY(Object vertexId);
    
    public void setY(Object vertexId, int y);
    
    public int getSemanticZoomLevel(Object vertexId);
    
	public Object getDisplayVertexId(Object vertexId, int semanticZoomLevel);

	public Collection<Object> getDisplayVertexIds(int semanticZoomLevel);
	
	@Deprecated
    public TopologyProvider getDataSource();

    @Deprecated
    public void setDataSource(TopologyProvider topologyProvider);

	public Collection<?> getVertexIds();
	
	public Graph getGraph();

	public boolean hasChildren(Object itemId);

	public Collection<?> getChildren(Object itemId);

	Object getParentId(Object itemId);

	public boolean containsVertexId(Object vertexId);
	
	public boolean containsEdgeId(Object edgeId);

	public SelectionManager getSelectionManager();
	
	// returns a list containing all of the passed in vertices and their children grandchildren etc.
	public Collection<?> getVertexForest(Collection<?> vertexIds);
}
