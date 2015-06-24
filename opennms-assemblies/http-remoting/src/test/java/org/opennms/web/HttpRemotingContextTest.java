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

import javax.servlet.Filter;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Test the Spring context for the http-remoting project by loading it with a
 * {@link org.springframework.mock.web.MockServletContext}.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author Seth
 */
public class HttpRemotingContextTest extends TestCase {

	private String contextPath = "/opennms/http-remoting";

	private MockServletConfig servletConfig;
	private MockServletContext servletContext;
	private ContextLoaderListener contextListener;
	private Filter filter;
	
	@Override
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();
	}

	public void testLoadContext() throws Throwable {

		DaoTestConfigBean bean = new DaoTestConfigBean();
		bean.afterPropertiesSet();

		MockDatabase db = new MockDatabase(true);
		DataSourceFactory.setInstance(db);

		servletContext = new MockServletContext("file:src/main/webapp");

		servletContext.addInitParameter(
				"contextConfigLocation", 
				"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml " +
				"classpath:/META-INF/opennms/applicationContext-soa.xml " +
				"classpath:/META-INF/opennms/applicationContext-mockDao.xml " +
				"classpath*:/META-INF/opennms/component-service.xml " +
				"classpath*:/META-INF/opennms/component-dao.xml " +

				// Contexts within this project
				"/WEB-INF/applicationContext-common.xml " +
				"/WEB-INF/applicationContext-serviceRegistryRemoting.xml " +
				"/WEB-INF/applicationContext-spring-security.xml " + 
				"/WEB-INF/applicationContext-svclayer.xml "
		);

		servletContext.addInitParameter("parentContextKey", "daoContext");

		ServletContextEvent e = new ServletContextEvent(servletContext);
		contextListener = new ContextLoaderListener();
		contextListener.contextInitialized(e);

		servletContext.setContextPath(contextPath);
		servletConfig = new MockServletConfig(servletContext, "dispatcher");    
		servletConfig.addInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
		servletConfig.addInitParameter("com.sun.jersey.config.property.packages", "org.opennms.web.rest");

		try {

			MockFilterConfig filterConfig = new MockFilterConfig(servletContext, "openSessionInViewFilter");
			filter = new OpenSessionInViewFilter();
			filter.init(filterConfig);
		} catch (ServletException se) {
			throw se.getRootCause();
		}
	}
}
