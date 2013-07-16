/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.container.web.bridge.internal;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.opennms.container.web.felix.base.internal.HttpServiceController;
import org.opennms.container.web.felix.base.internal.listener.ServletRequestAttributeListenerManager;

public class DispatcherFilter implements Filter {

    private HttpServiceController m_controller;

    private FilterConfig m_filterConfig;

    public DispatcherFilter(final HttpServiceController controller) {
        m_controller = controller;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        m_filterConfig = filterConfig;
        m_controller.register(filterConfig.getServletContext());
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final ServletRequestEvent sre = new ServletRequestEvent(m_filterConfig.getServletContext(), request);
        m_controller.getRequestListener().requestInitialized(sre);
        try {
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                final HttpServletRequest req = new AttributeEventRequest(m_filterConfig.getServletContext(), m_controller.getRequestAttributeListener(), (HttpServletRequest) request);
                m_controller.getDispatcher().dispatch(req, (HttpServletResponse) response, chain);
            } else {
                chain.doFilter(request, response);
            }
        } catch (final Exception e) {
            m_filterConfig.getServletContext().log("Something went horribly awry.", e);
        } finally {
            m_controller.getRequestListener().requestDestroyed(sre);
        }
    }

    @Override
    public void destroy() {
        m_controller.unregister();
    }

    private static class AttributeEventRequest extends HttpServletRequestWrapper {

        private final ServletContext servletContext;

        private final ServletRequestAttributeListenerManager requestAttributeListener;

        public AttributeEventRequest(ServletContext servletContext, ServletRequestAttributeListenerManager requestAttributeListener, HttpServletRequest request) {
            super(request);
            this.servletContext = servletContext;
            this.requestAttributeListener = requestAttributeListener;
        }

        @Override
        public void setAttribute(String name, Object value) {
            if (value == null) {
                this.removeAttribute(name);
            } else if (name != null) {
                Object oldValue = this.getAttribute(name);
                super.setAttribute(name, value);

                if (oldValue == null) {
                    requestAttributeListener.attributeAdded(new ServletRequestAttributeEvent(servletContext, this, name, value));
                } else {
                    requestAttributeListener.attributeReplaced(new ServletRequestAttributeEvent(servletContext, this, name, oldValue));
                }
            }
        }

        @Override
        public void removeAttribute(String name) {
            Object oldValue = this.getAttribute(name);
            super.removeAttribute(name);

            if (oldValue != null) {
                requestAttributeListener.attributeRemoved(new ServletRequestAttributeEvent(servletContext, this, name, oldValue));
            }
        }
    }
}
