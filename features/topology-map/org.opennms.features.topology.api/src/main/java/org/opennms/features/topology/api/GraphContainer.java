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

public interface GraphContainer extends DisplayState {
	
	public interface ChangeListener {
		public void graphChanged(GraphContainer graphContainer);
	}

	public Collection<? extends Vertex> getVertices();

	public Collection<? extends Vertex> getChildren(VertexRef vRef);

	public Collection<? extends Vertex> getRootGroup();

	public boolean hasChildren(VertexRef vRef);

	public GraphProvider getBaseTopology();
    
    public void setBaseTopology(GraphProvider graphProvider);
    
    public Vertex getParent(VertexRef child);
    
    public Vertex getVertex(VertexRef ref);
    
    public Edge getEdge(EdgeRef ref);
    
	public Criteria getCriteria(String namespace);
	
	public void setCriteria(Criteria critiera);
	
	public void addChangeListener(ChangeListener listener);
	
	public void removeChangeListener(ChangeListener listener);

	public Graph getGraph();
	
	public SelectionManager getSelectionManager();
	
	public Collection<VertexRef> getVertexRefForest(Collection<? extends VertexRef> vertexRefs);


	// These will work the GraphProvider in the future
	@Deprecated
    public TopologyProvider getDataSource();

    @Deprecated
    public void setDataSource(TopologyProvider topologyProvider);

}
