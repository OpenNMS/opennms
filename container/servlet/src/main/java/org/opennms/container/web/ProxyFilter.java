/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.container.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.framework.BundleContext;

public final class ProxyFilter implements Filter {
    private DispatcherTracker m_dispatcherTracker;

    @Override
    public void destroy() {
        this.m_dispatcherTracker.close();
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        filterConfig.getServletContext().log("initializing filter config " + filterConfig);
        try {
            this.m_dispatcherTracker = new DispatcherTracker(getBundleContext(filterConfig.getServletContext()), null, filterConfig);
        } catch (final ServletException e) {
            throw e;
        } catch (final Throwable e) {
            throw new ServletException("Unable to create dispatcher m_dispatcherTracker.", e);
        }
        this.m_dispatcherTracker.open();
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final Filter dispatcher = this.m_dispatcherTracker.getDispatcher();
        if (dispatcher != null) {
            dispatcher.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static BundleContext getBundleContext(final ServletContext servletContext) throws ServletException {
        final Object context = servletContext.getAttribute(BundleContext.class.getName());
        if (context instanceof BundleContext) {
            return (BundleContext)context;
        }

        throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() + "] not set in servlet context");
    }
}
