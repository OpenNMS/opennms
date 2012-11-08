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

package org.opennms.features.topology.plugins.topo.simple.internal;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.plugins.devutils.internal.SaveToXmlOperation;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
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
					public Integer getSemanticZoomLevel() {
						throw new UnsupportedOperationException("DisplayState.getSemanticZoomLevel is not yet implemented.");
					}

					@Override
					public void setSemanticZoomLevel(Integer level) {
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
					public Property getProperty(String property) {
						throw new UnsupportedOperationException("DisplayState.getProperty is not yet implemented.");
					}

					@Override
					public VertexContainer<?, ?> getVertexContainer() {
						throw new UnsupportedOperationException("GraphContainer.getVertexContainer is not yet implemented.");
					}

					@Override
					public BeanContainer<?, ?> getEdgeContainer() {
						throw new UnsupportedOperationException("GraphContainer.getEdgeContainer is not yet implemented.");
					}

					@Override
					public List<Object> getSelectedVertices() {
						throw new UnsupportedOperationException("GraphContainer.getSelectedVertices is not yet implemented.");
					}

					@Override
					public Collection<?> getVertexIds() {
						throw new UnsupportedOperationException("GraphContainer.getVertexIds is not yet implemented.");
					}

					@Override
					public Collection<?> getEdgeIds() {
						throw new UnsupportedOperationException("GraphContainer.getEdgeIds is not yet implemented.");
					}

					@Override
					public Item getVertexItem(Object vertexId) {
						throw new UnsupportedOperationException("GraphContainer.getVertexItem is not yet implemented.");
					}

					@Override
					public Item getEdgeItem(Object edgeId) {
						throw new UnsupportedOperationException("GraphContainer.getEdgeItem is not yet implemented.");
					}

					@Override
					public Collection<?> getEndPointIdsForEdge(Object edgeId) {
						throw new UnsupportedOperationException("GraphContainer.getEndPointIdsForEdge is not yet implemented.");
					}

					@Override
					public Collection<?> getEdgeIdsForVertex(Object vertexId) {
						throw new UnsupportedOperationException("GraphContainer.getEdgeIdsForVertex is not yet implemented.");
					}

					@Override
					public Object getVertexItemIdForVertexKey(Object key) {
						throw new UnsupportedOperationException("GraphContainer.getVertexItemIdForVertexKey is not yet implemented.");
					}

					@Override
					public TopologyProvider getDataSource() {
						return simpleTopo;
					}

					@Override
					public void setDataSource(TopologyProvider topologyProvider) {
						throw new UnsupportedOperationException("GraphContainer.setDataSource is not yet implemented.");
					}
					
				};
			}
		});
		
		
		
	}
}
