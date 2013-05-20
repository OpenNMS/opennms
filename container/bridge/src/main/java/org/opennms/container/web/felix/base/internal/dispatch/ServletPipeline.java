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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import org.opennms.container.web.felix.base.internal.handler.ServletHandler;

public final class ServletPipeline
{
    private final ServletHandler[] handlers;

    public ServletPipeline(ServletHandler[] handlers)
    {
        this.handlers = handlers;
    }

    public boolean handle(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        for (ServletHandler handler : this.handlers) {
            if (handler.handle(req, res)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasServletsMapped()
    {
        return this.handlers.length > 0;
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        for (ServletHandler handler : this.handlers) {
            if (handler.matches(path)) {
                return new Dispatcher(path, handler);
            }
        }
        
        return null;
    }

    private final class Dispatcher
        implements RequestDispatcher
    {
        private final String path;
        private final ServletHandler handler;

        public Dispatcher(String path, ServletHandler handler)
        {
            this.path = path;
            this.handler = handler;
        }

        @Override
        public void forward(ServletRequest req, ServletResponse res)
            throws ServletException, IOException
        {
            if (res.isCommitted()) {
                throw new ServletException("Response has been committed");
            }

            this.handler.handle(new RequestWrapper((HttpServletRequest)req, this.path), (HttpServletResponse)res);
        }

        @Override
        public void include(ServletRequest req, ServletResponse res)
            throws ServletException, IOException
        {
            this.handler.handle((HttpServletRequest)req, (HttpServletResponse)res);
        }
    }

    private final class RequestWrapper
        extends HttpServletRequestWrapper
    {
        private final String requestUri;
        
        public RequestWrapper(HttpServletRequest req, String requestUri)
        {
            super(req);
            this.requestUri = requestUri;
        }

        @Override
        public String getRequestURI()
        {
            return this.requestUri;
        }
    }
}
