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

package org.opennms.web;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.web.ServiceRegistryHttpInvokerServiceExporter;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Test the Spring context for the http-remoting project.
 * 
 * @author Seth
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",

		// Contexts within this project
		"file:src/main/webapp/WEB-INF/applicationContext-common.xml",
		"file:src/main/webapp/WEB-INF/applicationContext-serviceRegistryRemoting.xml",
		"file:src/main/webapp/WEB-INF/applicationContext-spring-security.xml",
		"file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml "
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HttpRemotingWebAppTest extends TestCase {

	@Autowired
	private ServletContext servletContext;

	@Override
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();
	}

	@Test
	public void test() throws Exception {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		assertTrue(context.containsBean("serviceRegistry"));
		assertTrue(context.containsBean("serviceRegistryExporter"));

		ServiceRegistry registry = context.getBean("serviceRegistry", ServiceRegistry.class);
		ServiceRegistryHttpInvokerServiceExporter exporter = context.getBean("serviceRegistryExporter", ServiceRegistryHttpInvokerServiceExporter.class);

		assertEquals(exporter.getServiceRegistry(), registry);
		assertTrue(exporter.getServiceRegistry() == registry);
	}
}
