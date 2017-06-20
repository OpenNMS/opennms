/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.pathoutage;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.features.topology.api.support.FocusStrategy;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
public class PathOutageProviderTest {

	@Autowired
	private MockNodeDao nodeDao;

	private PathOutageProvider pathOutageProvider;

	private PathOutageStatusProvider pathOutageStatusProvider;

	private Map<Integer, Integer> nodesMap;

	@Before
	public void setUp() {
		this.nodesMap = new HashMap<>();
		Map<OnmsNode, Integer> nodes = TestNodeGenerator.generateNodes(null);
		for (OnmsNode node : nodes.keySet()) {
			this.nodesMap.put(node.getId(), nodes.get(node));
			this.nodeDao.save(node);
		}
		this.pathOutageStatusProvider = Mockito.mock(PathOutageStatusProvider.class);
		this.setBehaviour(this.pathOutageStatusProvider);
	}

	@Test
	/**
	 * This method tests the following functionality of the {@link PathOutageProvider}:
	 * <p>
	 *     Correct initialization of node topology and hierarchy building
	 * </p>
	 * <p>
	 *     Correct updating of node topology and the vertex hierarchy
	 * </p>
	 */
	public void testHierarchy_GraphModification() {
		this.pathOutageProvider = new PathOutageProvider(this.nodeDao, this.pathOutageStatusProvider);
		this.pathOutageProvider.refresh();
		List<VertexHopGraphProvider.VertexHopCriteria> criteria_original = FocusStrategy.ALL.getFocusCriteria(this.pathOutageProvider);
		this.checkProvider(criteria_original);
		this.updateNodes();
		this.pathOutageProvider.refresh();
		List<VertexHopGraphProvider.VertexHopCriteria> criteria_new = FocusStrategy.ALL.getFocusCriteria(this.pathOutageProvider);
		assertThat(criteria_original, not(criteria_new));
		this.checkProvider(criteria_new);
	}

	private void setBehaviour(PathOutageStatusProvider statusProvider) {
		Mockito.when(statusProvider.getStatusForVertices(
					Matchers.any(PathOutageProvider.class),
					Matchers.anyCollection(),
					Matchers.any()))
				.thenReturn(new HashMap());
	}

	/**
	 * This method updates nodes hierarchy
	 */
	private void updateNodes() {
		List<Integer> nodesToUpdate = Lists.newArrayList(5, 7, 8, 9, 10);
		OnmsNode nodeParent = this.nodeDao.get(nodesToUpdate.get(0));
		OnmsNode nodeChild = this.nodeDao.get(nodesToUpdate.get(1));
		nodeChild.setParent(nodeParent);
		this.nodesMap.put(nodesToUpdate.get(1), this.nodesMap.get(nodesToUpdate.get(0)) + 1);
		this.nodesMap.put(nodesToUpdate.get(2), this.nodesMap.get(nodesToUpdate.get(0)) + 2);
		this.nodesMap.put(nodesToUpdate.get(3), this.nodesMap.get(nodesToUpdate.get(0)) + 3);
		OnmsNode nodeNew = new OnmsNode();
		nodeNew.setId(nodesToUpdate.get(4));
		nodeNew.setLabel("New node");
		this.nodeDao.save(nodeNew);
		this.nodesMap.put(nodesToUpdate.get(4), 0);
	}

	/**
	 * In this method the vertices from the {@link PathOutageProvider} are compared to the local vertices data
	 * @param criteria
	 */
	private void checkProvider(List<VertexHopGraphProvider.VertexHopCriteria> criteria) {
		List<Vertex> vertices = this.pathOutageProvider.getVertices(Lists.newArrayList(criteria).toArray(new Criteria[criteria.size()]));
		assertEquals(vertices.size(), this.nodesMap.size());
		for (Vertex vertex : vertices) {
			Integer id = Integer.parseInt(vertex.getId());
			int levelNode = this.nodesMap.get(id);
			assertTrue(vertex instanceof PathOutageVertex);
			int levelVertex = ((PathOutageVertex)vertex).getLevel();
			assertEquals(levelNode, levelVertex);
		}
	}
}
