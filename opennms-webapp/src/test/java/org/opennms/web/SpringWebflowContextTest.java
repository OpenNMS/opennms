/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 30, 2008
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web;

import javax.servlet.Filter;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.web.context.ContextLoaderListener;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class SpringWebflowContextTest extends TestCase {

    private String contextPath = "/opennms/rest";

    private ServletContainer dispatcher;
    private MockServletConfig servletConfig;
    private MockServletContext servletContext;
    private ContextLoaderListener contextListener;
    private Filter filter;

    public void testLoadContext() throws Throwable {

        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        MockDatabase db = new MockDatabase(true);
        DataSourceFactory.setInstance(db);

        servletContext = new MockServletContext("file:src/main/webapp");

        servletContext.addInitParameter("contextConfigLocation", 
                                        "classpath:/org/opennms/web/rest/applicationContext-test.xml " +
                                        "classpath*:/META-INF/opennms/component-service.xml " +
                                        "classpath*:/META-INF/opennms/component-dao.xml " +
                                        "classpath:/META-INF/opennms/applicationContext-reportingCore.xml " +
                                        "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml " +
                                        "classpath:/META-INF/opennms/applicationContext-reporting.xml " +
                                        "/WEB-INF/applicationContext-spring-security.xml " +
                                        "/WEB-INF/applicationContext-spring-webflow.xml"
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

            dispatcher = new SpringServlet();
            dispatcher.init(servletConfig);

        } catch (ServletException se) {
            throw se.getRootCause();
        }
    }
}
