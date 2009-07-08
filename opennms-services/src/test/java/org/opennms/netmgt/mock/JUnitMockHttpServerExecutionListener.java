/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.lang.reflect.Method;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class JUnitMockHttpServerExecutionListener extends AbstractTestExecutionListener {
    private Server m_server;

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        JUnitMockHttpServer config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }

        m_server = new Server(config.port());
        ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase(config.directory());
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{rh, new DefaultHandler()});
        m_server.setHandler(handlers);
        
        m_server.start();
        Thread.sleep(100);
    }
    
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        JUnitMockHttpServer config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }
        
        if (m_server != null) {
            m_server.stop();
        }
    }
    
    private JUnitMockHttpServer findCollectorAnnotation(TestContext testContext) {
        Method testMethod = testContext.getTestMethod();
        JUnitMockHttpServer config = testMethod.getAnnotation(JUnitMockHttpServer.class);
        if (config != null) {
            return config;
        }

        Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitMockHttpServer.class);
    }

}
