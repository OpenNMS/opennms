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

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;

public interface GraphContainer extends DisplayState {
	
	public interface ChangeListener {
		public void graphChanged(GraphContainer graphContainer);
	}

    public VertexContainer<?, ?> getVertexContainer();

    public BeanContainer<?, ?> getEdgeContainer();

    public GraphProvider getBaseTopology();
    
    public void setBaseTopology(GraphProvider graphProvider);
    
    public Vertex getParent(VertexRef child);
    
    public Vertex getVertex(VertexRef ref);
    
    public Edge getEdge(EdgeRef ref);
    
    public int getVertexX(VertexRef vertexId);
    
    public void setVertexX(VertexRef vertexId, int x);
    
    public int getVertexY(VertexRef vertexId);
    
    public void setVertexY(VertexRef vertexId, int y);
    
	public Criteria getCriteria(String namespace);
	
	public void setCriteria(Criteria critiera);
	
	public void addChangeListener(ChangeListener listener);
	
	public void removeChangeListener(ChangeListener listener);

	@Deprecated
    public TopologyProvider getDataSource();

    @Deprecated
    public void setDataSource(TopologyProvider topologyProvider);

	public Graph getCompleteGraph();
	
	public Graph getGraph();
	
	public boolean containsVertexId(Object vertexId);
	
	public boolean containsEdgeId(Object edgeId);

	public SelectionManager getSelectionManager();
	
	// returns a list containing all of the passed in vertices and their children grandchildren etc.
	public Collection<?> getVertexForest(Collection<?> vertexIds);
	
	
    public Item getVertexItem(Object vertexId);

    public void setVertexItemProperty(Object itemId, String propertyName, Object value);

	public <T> T getVertexItemProperty(Object itemId, String propertyName, T defaultValue);
	
}
