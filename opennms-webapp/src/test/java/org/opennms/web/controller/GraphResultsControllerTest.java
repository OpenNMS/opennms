/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.support.PropertiesGraphDao;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.web.svclayer.api.GraphResultsService;
import org.springframework.core.io.FileSystemResource;

/**
 * The Test Class for GraphResultsController.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class GraphResultsControllerTest {

	/** The controller. */
	private GraphResultsController m_controller;

	/** The service. */
	private GraphResultsService m_service;

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		m_service = mock(GraphResultsService.class);

		PropertiesGraphDao graphDao = new PropertiesGraphDao();
		graphDao.loadProperties("performance", new FileSystemResource(new File("src/test/resources/etc/snmp-graph.properties")));

		graphDao.afterPropertiesSet();
		List<PrefabGraph> prefabs = graphDao.getAllPrefabGraphs();
		Assert.assertNotNull(prefabs);
		Assert.assertFalse(prefabs.isEmpty());
		when(m_service.getAllPrefabGraphs(ResourceId.get("node", "1").resolve("nodeSnmp", ""))).thenReturn(prefabs.toArray(new PrefabGraph[prefabs.size()]));

		m_controller = new GraphResultsController();
		m_controller.setGraphResultsService(m_service);
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {
		verifyNoMoreInteractions(m_service);
	}

	/**
	 * Test matching.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testMatching() throws Exception {
		// Test an expression
		String[] reports = m_controller.getSuggestedReports(ResourceId.get("node", "1").resolve("nodeSnmp", ""), "memAvailReal / memTotalReal * 100.0");
		System.out.println(StringUtils.join(reports, ", "));
		Assert.assertEquals(2, reports.length);
		Assert.assertEquals("netsnmp.memStats", reports[0]);

		// Test a single value
		reports = m_controller.getSuggestedReports(ResourceId.get("node", "1").resolve("nodeSnmp", ""), "memAvailReal");
		System.out.println(StringUtils.join(reports, ", "));
		Assert.assertEquals(2, reports.length);
		Assert.assertEquals("netsnmp.memStats", reports[0]);

		// Test an unexisting metric
		reports = m_controller.getSuggestedReports(ResourceId.get("node", "1").resolve("nodeSnmp", ""), "blahblah");
		System.out.println(StringUtils.join(reports, ", "));
		Assert.assertEquals(1, reports.length);
		Assert.assertEquals("all", reports[0]);

		verify(m_service, atLeastOnce()).getAllPrefabGraphs(any(ResourceId.class));
	}

}
