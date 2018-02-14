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
package org.opennms.container.web.felix.base.internal.context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.container.web.felix.base.internal.logger.SystemLogger;
import org.opennms.container.web.felix.base.internal.util.MimeTypes;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public final class ServletContextImpl
    implements ExtServletContext
{
    private final Bundle bundle;
    private final ServletContext context;
    private final HttpContext httpContext;
    private final Map<String, Object> attributes;
    private final ServletContextAttributeListener attributeListener;

    public ServletContextImpl(Bundle bundle, ServletContext context, HttpContext httpContext,
        ServletContextAttributeListener attributeListener, boolean sharedAttributes)
    {
        this.bundle = bundle;
        this.context = context;
        this.httpContext = httpContext;
        this.attributeListener = attributeListener;
        this.attributes = sharedAttributes ? null : new ConcurrentHashMap<String, Object>();
    }

    @Override
    public String getContextPath()
    {
        return this.context.getContextPath();
    }

    @Override
    public ServletContext getContext(String uri)
    {
        return this.context.getContext(uri);
    }

    @Override
    public int getMajorVersion()
    {
        return this.context.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return this.context.getMinorVersion();
    }

    @Override
    public Set<String> getResourcePaths(String path)
    {
        Enumeration<?> paths = this.bundle.getEntryPaths(normalizePath(path));
        if ((paths == null) || !paths.hasMoreElements()) {
            return null;
        }

        Set<String> set = new HashSet<>();
        while (paths.hasMoreElements()) {
            set.add((String) paths.nextElement());
        }

        return set;
    }

    @Override
    public URL getResource(String path)
    {
        return this.httpContext.getResource(normalizePath(path));
    }

    @Override
    public InputStream getResourceAsStream(String path)
    {
        URL res = getResource(path);
        if (res != null) {
            try {
                return res.openStream();
            } catch (IOException e) {
                // Do nothing
            }
        }

        return null;
    }

    private static String normalizePath(String path)
    {
        if (path == null) {
            return null;
        }

        String normalizedPath = path.trim().replaceAll("/+", "/");
        if (normalizedPath.startsWith("/") && (normalizedPath.length() > 1)) {
            normalizedPath = normalizedPath.substring(1);
        }

        return normalizedPath;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String uri)
    {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name)
    {
        return null;
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.context.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return this.context.getInitParameterNames();
    }

    @Override
    public Object getAttribute(String name)
    {
        return (this.attributes != null) ? this.attributes.get(name) : this.context.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return (this.attributes != null) ? Collections.enumeration(this.attributes.keySet()) : this.context
            .getAttributeNames();
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
            Object oldValue;
            if (this.attributes != null)
            {
                oldValue = this.attributes.put(name, value);
            }
            else
            {
                oldValue = this.context.getAttribute(name);
                this.context.setAttribute(name, value);
            }

            if (oldValue == null)
            {
                attributeListener.attributeAdded(new ServletContextAttributeEvent(this, name, value));
            }
            else
            {
                attributeListener.attributeReplaced(new ServletContextAttributeEvent(this, name, oldValue));
            }
        }
    }

    @Override
    public void removeAttribute(String name)
    {
        Object oldValue;
        if (this.attributes != null)
        {
            oldValue = this.attributes.remove(name);
        }
        else
        {
            oldValue = this.context.getAttribute(name);
            this.context.removeAttribute(name);
        }

        if (oldValue != null)
        {
            attributeListener.attributeRemoved(new ServletContextAttributeEvent(this, name, oldValue));
        }
    }

    @Override
    public Servlet getServlet(String name)
        throws ServletException
    {
        return null;
    }

    @Override
    public Enumeration<Servlet> getServlets()
    {
        return Collections.enumeration(Collections.<Servlet>emptyList());
    }

    @Override
    public Enumeration<String> getServletNames()
    {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    @Override
    public void log(String message)
    {
        SystemLogger.info(message);
    }

    @Override
    public void log(Exception cause, String message)
    {
        SystemLogger.error(message, cause);
    }

    @Override
    public void log(String message, Throwable cause)
    {
        SystemLogger.error(message, cause);
    }

    @Override
    public String getServletContextName()
    {
        return this.context.getServletContextName();
    }

    @Override
    public String getRealPath(String name)
    {
        return null;
    }

    @Override
    public String getServerInfo()
    {
        return this.context.getServerInfo();
    }

    @Override
    public String getMimeType(String file)
    {
        String type = this.httpContext.getMimeType(file);
        if (type != null) {
            return type;
        }

        return MimeTypes.get().getByFile(file);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res)
        throws IOException
    {
        return this.httpContext.handleSecurity(req, res);
    }

	@Override
	public int getEffectiveMajorVersion() {
		return context.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return context.getEffectiveMinorVersion();
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return context.setInitParameter(name, value);
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		return context.addServlet(servletName, className);
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		return context.addServlet(servletName, servlet);
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		return context.addServlet(servletName, servletClass);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		return context.createServlet(clazz);
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return context.getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return context.getServletRegistrations();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		return context.addFilter(filterName, className);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		return context.addFilter(filterName, filter);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		return context.addFilter(filterName, filterClass);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		return context.createFilter(clazz);
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return context.getFilterRegistration(filterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return context.getFilterRegistrations();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return context.getSessionCookieConfig();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		context.setSessionTrackingModes(sessionTrackingModes);
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return context.getDefaultSessionTrackingModes();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return context.getEffectiveSessionTrackingModes();
	}

	@Override
	public void addListener(String className) {
		context.addListener(className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		context.addListener(t);
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		context.addListener(listenerClass);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		return context.createListener(clazz);
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return context.getJspConfigDescriptor();
	}

	@Override
	public ClassLoader getClassLoader() {
		return context.getClassLoader();
	}

	@Override
	public void declareRoles(String... roleNames) {
		context.declareRoles(roleNames);
	}

    @Override
    public String getVirtualServerName() {
        return context.getVirtualServerName();
    }
}
