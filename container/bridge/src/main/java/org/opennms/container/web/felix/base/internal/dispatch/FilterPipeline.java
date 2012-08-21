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
package org.opennms.container.web.felix.base.internal.dispatch;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.opennms.container.web.felix.base.internal.handler.FilterHandler;

public final class FilterPipeline
{
    private final FilterHandler[] handlers;
    private final ServletPipeline servletPipeline;

    public FilterPipeline(FilterHandler[] handlers, ServletPipeline servletPipeline)
    {
        this.handlers = handlers;
        this.servletPipeline = servletPipeline;
    }

    public void dispatch(HttpServletRequest req, HttpServletResponse res, FilterChain proceedingChain)
        throws ServletException, IOException
    {
        FilterChain chain = new InvocationFilterChain(this.handlers, this.servletPipeline, proceedingChain);

        if (this.servletPipeline.hasServletsMapped()) {
            req = new RequestWrapper(req);
        }

        chain.doFilter(req, res);
    }

    private final class RequestWrapper
        extends HttpServletRequestWrapper
    {
        public RequestWrapper(HttpServletRequest req)
        {
            super(req);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path)
        {
            final RequestDispatcher dispatcher = servletPipeline.getRequestDispatcher(path);
            return (null != dispatcher) ? dispatcher : super.getRequestDispatcher(path);
        }        
    }
}
