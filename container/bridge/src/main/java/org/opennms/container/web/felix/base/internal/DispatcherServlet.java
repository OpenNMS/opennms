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
package org.opennms.container.web.felix.base.internal;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.opennms.container.web.felix.base.internal.dispatch.NotFoundFilterChain;
import org.opennms.container.web.felix.base.internal.listener.ServletRequestAttributeListenerManager;

public final class DispatcherServlet
    extends HttpServlet
{
    private final HttpServiceController controller;

    public DispatcherServlet(HttpServiceController controller)
    {
        this.controller = controller;
    }

    @Override
    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);
        this.controller.register(getServletContext());
    }

    @Override
    public void destroy()
    {
        this.controller.unregister();
        super.destroy();
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        final ServletRequestEvent sre = new ServletRequestEvent(getServletContext(), req);
        this.controller.getRequestListener().requestInitialized(sre);
        try
        {
            req = new AttributeEventRequest(getServletContext(), this.controller.getRequestAttributeListener(), req);
            this.controller.getDispatcher().dispatch(req, res, new NotFoundFilterChain());
        }
        finally
        {
            this.controller.getRequestListener().requestDestroyed(sre);
        }
    }

    private static class AttributeEventRequest extends HttpServletRequestWrapper
    {

        private final ServletContext servletContext;
        private final ServletRequestAttributeListenerManager requestAttributeListener;

        public AttributeEventRequest(ServletContext servletContext,
            ServletRequestAttributeListenerManager requestAttributeListener, HttpServletRequest request)
        {
            super(request);
            this.servletContext = servletContext;
            this.requestAttributeListener = requestAttributeListener;
        }

        @Override
        public void setAttribute(String name, Object value)
        {
            if (value == null)
            {
                this.removeAttribute(name);
            }
            else if (name != null)
            {
                Object oldValue = this.getAttribute(name);
                super.setAttribute(name, value);

                if (oldValue == null)
                {
                    requestAttributeListener.attributeAdded(new ServletRequestAttributeEvent(servletContext, this,
                        name, value));
                }
                else
                {
                    requestAttributeListener.attributeReplaced(new ServletRequestAttributeEvent(servletContext, this,
                        name, oldValue));
                }
            }
        }

        @Override
        public void removeAttribute(String name)
        {
            Object oldValue = this.getAttribute(name);
            super.removeAttribute(name);

            if (oldValue != null)
            {
                requestAttributeListener.attributeRemoved(new ServletRequestAttributeEvent(servletContext, this, name,
                    oldValue));
            }
        }
    }
}
