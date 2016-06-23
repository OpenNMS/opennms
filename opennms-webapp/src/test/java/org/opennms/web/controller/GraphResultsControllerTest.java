/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.support.PropertiesGraphDao;
import org.opennms.netmgt.model.PrefabGraph;
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
		m_service = EasyMock.createMock(GraphResultsService.class);

		PropertiesGraphDao graphDao = new PropertiesGraphDao();
		graphDao.loadProperties("performance", new FileSystemResource(new File("src/test/resources/etc/snmp-graph.properties")));

		List<PrefabGraph> prefabs = graphDao.getAllPrefabGraphs();
		Assert.assertNotNull(prefabs);
		Assert.assertFalse(prefabs.isEmpty());
		EasyMock.expect(m_service.getAllPrefabGraphs("node[1].nodeSnmp[]")).andReturn(prefabs.toArray(new PrefabGraph[prefabs.size()])).anyTimes();

		m_controller = new GraphResultsController();
		m_controller.setGraphResultsService(m_service);

		EasyMock.replay(m_service);
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {
		EasyMock.verify(m_service);
	}

	/**
	 * Test matching.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testMatching() throws Exception {
		// Test an expression
		String[] reports = m_controller.getSuggestedReports("node[1].nodeSnmp[]", "memAvailReal / memTotalReal * 100.0");
		System.out.println(StringUtils.join(reports, ", "));
		Assert.assertEquals(2, reports.length);
		Assert.assertEquals("netsnmp.memStats", reports[0]);

		// Test a single value
		reports = m_controller.getSuggestedReports("node[1].nodeSnmp[]", "memAvailReal");
		System.out.println(StringUtils.join(reports, ", "));
		Assert.assertEquals(2, reports.length);
		Assert.assertEquals("netsnmp.memStats", reports[0]);

		// Test an unexisting metric
		reports = m_controller.getSuggestedReports("node[1].nodeSnmp[]", "blahblah");
		System.out.println(StringUtils.join(reports, ", "));
		Assert.assertEquals(1, reports.length);
		Assert.assertEquals("all", reports[0]);
	}

}
