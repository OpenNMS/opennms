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
package org.opennms.container.web.felix.base.internal.handler;

import org.osgi.service.http.NamespaceException;
import javax.servlet.ServletException;
import javax.servlet.Servlet;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public final class HandlerRegistry
{
    private final Map<Servlet, ServletHandler> servletMap;
    private final Map<Filter, FilterHandler> filterMap;
    private final Map<String, Servlet> aliasMap;
    private ServletHandler[] servlets;
    private FilterHandler[] filters;

    public HandlerRegistry()
    {
        this.servletMap = new HashMap<Servlet, ServletHandler>();
        this.filterMap = new HashMap<Filter, FilterHandler>();
        this.aliasMap = new HashMap<String, Servlet>();
        this.servlets = new ServletHandler[0];
        this.filters = new FilterHandler[0];
    }

    public ServletHandler[] getServlets()
    {
        return this.servlets;
    }

    public FilterHandler[] getFilters()
    {
        return this.filters;
    }

    public synchronized void addServlet(ServletHandler handler)
        throws ServletException, NamespaceException
    {
        if (this.servletMap.containsKey(handler.getServlet())) {
            throw new ServletException("Servlet instance already registered");
        }

        if (this.aliasMap.containsKey(handler.getAlias())) {
            throw new NamespaceException("Servlet with alias already registered");
        }

        handler.init();
        this.servletMap.put(handler.getServlet(), handler);
        this.aliasMap.put(handler.getAlias(), handler.getServlet());
        updateServletArray();
    }

    public synchronized void addFilter(FilterHandler handler)
        throws ServletException
    {
        if (this.filterMap.containsKey(handler.getFilter())) {
            throw new ServletException("Filter instance already registered");
        }

        handler.init();
        this.filterMap.put(handler.getFilter(), handler);
        updateFilterArray();
    }

    public synchronized void removeServlet(Servlet servlet, final boolean destroy)
    {
        ServletHandler handler = this.servletMap.remove(servlet);
        if (handler != null) {
            updateServletArray();
            this.aliasMap.remove(handler.getAlias());
            if (destroy)
            {
                handler.destroy();
            }
        }
    }

    public synchronized void removeFilter(Filter filter, final boolean destroy)
    {
        FilterHandler handler = this.filterMap.remove(filter);
        if (handler != null) {
            updateFilterArray();
            if (destroy)
            {
                handler.destroy();
            }
        }
    }

    public synchronized Servlet getServletByAlias(String alias)
    {
        return this.aliasMap.get(alias);
    }

    public synchronized void removeAll()
    {
        for (ServletHandler handler : this.servletMap.values()) {
            handler.destroy();
        }

        for (FilterHandler handler : this.filterMap.values()) {
            handler.destroy();
        }

        this.servletMap.clear();
        this.filterMap.clear();
        this.aliasMap.clear();

        updateServletArray();
        updateFilterArray();
    }

    private void updateServletArray()
    {
        ServletHandler[] tmp = this.servletMap.values().toArray(new ServletHandler[this.servletMap.size()]);
        Arrays.sort(tmp);
        this.servlets = tmp;
    }

    private void updateFilterArray()
    {
        FilterHandler[] tmp = this.filterMap.values().toArray(new FilterHandler[this.filterMap.size()]);
        Arrays.sort(tmp);
        this.filters = tmp;
    }
}
