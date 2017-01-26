/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.AbstractEdgeRef;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphBuilder;

public class MergingGraphProviderTest {

	private GraphProvider m_graphProvider;
	private EdgeProvider m_edgeProvider;
	private MergingGraphProvider m_mergedProvider;

	
	@Before
	public void setUp() {

		MockLogAppender.setupLogging();

		m_graphProvider = new SimpleGraphBuilder("nodes")
			.vertex("g0").vLabel("group0").vIconKey("group").vTooltip("root group").vStyleName("vertex")
			.vertex("g1").parent("g0").vLabel("group1").vIconKey("group").vTooltip("group 1").vStyleName("vertex")
			.vertex("v1").parent("g1").vLabel("vertex1").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v2").parent("g1").vLabel("vertex2").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("g2").parent("g0").vLabel("group2").vIconKey("group").vTooltip("group 2").vStyleName("vertex")
			.vertex("v3").parent("g2").vLabel("vertex3").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.vertex("v4").parent("g2").vLabel("vertex4").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
			.edge("e1", "v1", "v2").eLabel("edge1").eStyleName("edge")
			.edge("e2", "v2", "v3").eLabel("edge2").eStyleName("edge")
			.edge("e3", "v3", "v4").eLabel("edge3").eStyleName("edge")
			.edge("e4", "v4", "v1").eLabel("edge4").eStyleName("edge")
			.get();

		m_edgeProvider = new SimpleEdgeBuilder("ncs", "nodes")
			.edge("ncs1", "nodes", "v1", "nodes", "v3").label("ncsedge1")
			.edge("ncs2", "nodes", "v2", "nodes", "v4").label("ncsedge2")
			.get();
		
		ProviderManager providerManager = new ProviderManager();
		providerManager.onEdgeProviderBind(m_edgeProvider);
		
		m_mergedProvider = new MergingGraphProvider(providerManager);
		m_mergedProvider.setBaseGraphProvider(m_graphProvider);
	}
	
	@Test
	public void testGraphProvider() {
		List<? extends Vertex> roots = m_graphProvider.getRootGroup();
		assertEquals(1, roots.size());
		Vertex root = roots.get(0);
		assertNotNull(root);
		
		assertEquals("nodes", root.getNamespace());
		assertEquals("g0", root.getId());
		
		List<? extends Vertex> children = m_graphProvider.getChildren(root);
		assertEquals(2, children.size());
		assertEquals(root, m_graphProvider.getParent(children.get(0)));
	}

	@Test
	public void testGetVertex() {
		assertEquals("vertex1", m_mergedProvider.getVertex("nodes", "v1").getLabel());
		assertEquals("vertex2", m_mergedProvider.getVertex(new DefaultVertexRef("nodes", "v2")).getLabel());
	}

	@Test
	public void testGetEdge() {
		assertEquals("edge1", m_mergedProvider.getEdge("nodes", "e1").getLabel());
		assertEquals("ncsedge2", m_mergedProvider.getEdge(new AbstractEdgeRef("ncs", "ncs2")).getLabel());
	}
	
	@Test
	public void testGetEdges() {
		// with no criteria set.. just base edges
		List<? extends Edge> edges = m_mergedProvider.getEdges();
		
		assertEquals(4, edges.size());
		assertEquals(m_graphProvider.getEdges(), edges);
		
		// set a criteria now and get some ncs edges
		edges = m_mergedProvider.getEdges(SimpleEdgeProvider.labelMatches("ncs", "ncsedge2"));

		assertEquals(5, edges.size());
		assertTrue(edges.contains(new AbstractEdgeRef("ncs", "ncs2")));
		
	}
}
