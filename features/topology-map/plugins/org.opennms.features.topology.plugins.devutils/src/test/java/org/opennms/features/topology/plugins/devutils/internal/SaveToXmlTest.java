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

package org.opennms.features.topology.plugins.devutils.internal;

import java.util.Collection;

import org.junit.Test;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.simple.internal.SimpleTopologyProvider;

import com.vaadin.ui.Window;

public class SaveToXmlTest {

	
	@Test
	public void testSave() {
		
		final SimpleTopologyProvider simpleTopo = new SimpleTopologyProvider();
		simpleTopo.load("test-graph.xml");
		
		SaveToXmlOperation saver = new SaveToXmlOperation();
		
		saver.execute(null, new OperationContext() {
			
			@Override
			public boolean isChecked() {
				throw new UnsupportedOperationException(
						"Type1351878585294.isChecked is not yet implemented.");
			}
			
			@Override
			public Window getMainWindow() {
				throw new UnsupportedOperationException(
						"Type1351878585294.getMainWindow is not yet implemented.");
			}
			
			@Override
			public GraphContainer getGraphContainer() {
				return new GraphContainer() {

					@Override
					public int getSemanticZoomLevel() {
						throw new UnsupportedOperationException("DisplayState.getSemanticZoomLevel is not yet implemented.");
					}

					@Override
					public void setSemanticZoomLevel(int level) {
						throw new UnsupportedOperationException("DisplayState.setSemanticZoomLevel is not yet implemented.");
					}

					@Override
					public void setLayoutAlgorithm(
							LayoutAlgorithm layoutAlgorithm) {
						throw new UnsupportedOperationException("DisplayState.setLayoutAlgorithm is not yet implemented.");
					}

					@Override
					public LayoutAlgorithm getLayoutAlgorithm() {
						throw new UnsupportedOperationException("DisplayState.getLayoutAlgorithm is not yet implemented.");
					}

					@Override
					public void redoLayout() {
						throw new UnsupportedOperationException("DisplayState.redoLayout is not yet implemented.");
					}

					@Override
					public TopologyProvider getDataSource() {
						return simpleTopo;
					}

					@Override
					public void setDataSource(TopologyProvider topologyProvider) {
						throw new UnsupportedOperationException("GraphContainer.setDataSource is not yet implemented.");
					}

					@Override
					public GraphProvider getBaseTopology() {
						throw new UnsupportedOperationException("GraphContainer.getBaseTopology is not yet implemented.");
					}

					@Override
					public void setBaseTopology(GraphProvider graphProvider) {
						throw new UnsupportedOperationException("GraphContainer.setBaseTopology is not yet implemented.");
					}

					@Override
					public SelectionManager getSelectionManager() {
						throw new UnsupportedOperationException("GraphContainer.getSelectionManager is not yet implemented.");
					}

					@Override
					public Graph getGraph() {
						throw new UnsupportedOperationException("GraphContainer.getGraph is not yet implemented.");
					}

					@Override
					public Vertex getVertex(VertexRef ref) {
						throw new UnsupportedOperationException("GraphContainer.getVertex is not yet implemented.");
					}

					@Override
					public Edge getEdge(EdgeRef ref) {
						throw new UnsupportedOperationException("GraphContainer.getEdge is not yet implemented.");
					}

					@Override
					public Criteria getCriteria(String namespace) {
						throw new UnsupportedOperationException("GraphContainer.getCriteria is not yet implemented.");
					}

					@Override
					public void setCriteria(Criteria critiera) {
						throw new UnsupportedOperationException("GraphContainer.setCriteria is not yet implemented.");
					}

					@Override
					public double getScale() {
						throw new UnsupportedOperationException("DisplayState.getScale is not yet implemented.");
					}

					@Override
					public void setScale(double scale) {
						throw new UnsupportedOperationException("DisplayState.setScale is not yet implemented.");
					}

					@Override
					public Vertex getParent(VertexRef child) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void addChangeListener(ChangeListener listener) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void removeChangeListener(ChangeListener listener) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public Collection<? extends Vertex> getVertices() {
						throw new UnsupportedOperationException("GraphContainer.getVertices is not yet implemented.");
					}

					@Override
					public Collection<? extends Vertex> getChildren(
							VertexRef vRef) {
						throw new UnsupportedOperationException("GraphContainer.getChildren is not yet implemented.");
					}

					@Override
					public Collection<? extends Vertex> getRootGroup() {
						throw new UnsupportedOperationException("GraphContainer.getRootGroup is not yet implemented.");
					}

					@Override
					public boolean hasChildren(VertexRef vRef) {
						throw new UnsupportedOperationException("GraphContainer.hasChildren is not yet implemented.");
					}

					@Override
					public Collection<VertexRef> getVertexRefForest(
							Collection<? extends VertexRef> vertexRefs) {
						throw new UnsupportedOperationException("GraphContainer.getVertexRefForest is not yet implemented.");
					}

					
				};
			}
		});
		
		
		
	}
}
