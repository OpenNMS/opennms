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
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class VertexHopGraphProviderScaleTest {

	private VertexHopGraphProvider m_provider;
	private int m_vertexCount;
	private int m_edgeCount;

	
	@Before
	public void setUp() {

		SFreeTopologyProvider baseProvider = new SFreeTopologyProvider();
		baseProvider.setNodeCount(10000);
		baseProvider.setConnectedness(1.5);
		baseProvider.load(SFreeTopologyProvider.ERDOS_RENIS);
		
		m_vertexCount = baseProvider.getVertices().size();
		m_edgeCount = baseProvider.getEdges().size();
		System.err.println(m_vertexCount);
		System.err.println(m_edgeCount);
		
		m_provider = new VertexHopGraphProvider(baseProvider);
		
		//System.err.printf("SZL: %d\n", m_provider.
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
		FocusNodeHopCriteria criteria = new FocusNodeHopCriteria(randomVertex.getId(), randomVertex.getLabel());
		int focusNodeCount = 1;
		for(int i = 0; i < focusNodeCount; i++) {
            criteria.add(randomVertex);
		}
		
		System.err.printf("Focus Nodes: %s\n", criteria.getVertices());
		
		long start = System.nanoTime();
		List<Vertex> vertices2 = m_provider.getVertices(criteria);
		long end = System.nanoTime();
		
		double time = (end-start)/(1000000.0);

		System.err.printf("ElapsedTime = %f ms\n", time);
		System.err.printf("%d, %d, %f, %d\n", m_vertexCount, m_edgeCount, time, vertices2.size());
		
		//assertEquals(vertices1.size(), vertices2.size());
//		Collections.sort(vertices1);
//		Collections.sort(vertices2);
//		System.err.println(vertices1);
//		System.err.println(vertices2);
//		vertices1.removeAll(vertices2);
//		
//		System.err.println(vertices1);
	}

}
