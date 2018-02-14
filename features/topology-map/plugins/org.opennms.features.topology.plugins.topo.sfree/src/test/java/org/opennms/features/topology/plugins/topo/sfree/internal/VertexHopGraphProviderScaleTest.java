/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.sfree.internal;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertexHopGraphProviderScaleTest {

	private static final Logger LOG = LoggerFactory.getLogger(VertexHopGraphProviderScaleTest.class);

	private VertexHopGraphProvider m_provider;
	private int m_vertexCount;
	private int m_edgeCount;

	
	@Before
	public void setUp() {

		SFreeTopologyProvider baseProvider = new SFreeTopologyProvider();
		baseProvider.setNodeCount(10000);
		baseProvider.setConnectedness(1.5);
		baseProvider.refresh();
		
		m_vertexCount = baseProvider.getVertices().size();
		m_edgeCount = baseProvider.getEdges().size();
		LOG.info("Vertex Count: {}", m_vertexCount);
		LOG.info("Edge Count: {}", m_edgeCount);
		
		m_provider = new VertexHopGraphProvider(baseProvider);
	}
	
	public int randomInt(int max) {
		return (int) Math.round(Math.random()*max);
	}
	
	public VertexRef randomVertex() {
		String randomNumber = Integer.toString(randomInt(m_vertexCount));
		return new DefaultVertexRef("sfree", randomNumber, randomNumber);
	}
	
	@Test
	public void testGraphProvider() {
        VertexRef randomVertex = randomVertex();
		DefaultVertexHopCriteria criteria = new DefaultVertexHopCriteria(randomVertex);

		LOG.info("Focus Nodes: {}", criteria.getVertices());
		
		long start = System.nanoTime();
		List<Vertex> vertices2 = m_provider.getVertices(criteria);
		long end = System.nanoTime();
		
		double time = (end-start)/(1000000.0);

		LOG.info("ElapsedTime = {} ms", time);
		LOG.info("{}, {}, {}, {}", m_vertexCount, m_edgeCount, time, vertices2.size());
	}

}
