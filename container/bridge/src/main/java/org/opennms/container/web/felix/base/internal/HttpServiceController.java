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

import java.util.Hashtable;

import javax.servlet.ServletContext;

import org.apache.felix.http.api.ExtHttpService;
import org.opennms.container.web.felix.base.internal.dispatch.Dispatcher;
import org.opennms.container.web.felix.base.internal.handler.HandlerRegistry;
import org.opennms.container.web.felix.base.internal.listener.HttpSessionAttributeListenerManager;
import org.opennms.container.web.felix.base.internal.listener.HttpSessionListenerManager;
import org.opennms.container.web.felix.base.internal.listener.ServletContextAttributeListenerManager;
import org.opennms.container.web.felix.base.internal.listener.ServletRequestAttributeListenerManager;
import org.opennms.container.web.felix.base.internal.listener.ServletRequestListenerManager;
import org.opennms.container.web.felix.base.internal.service.HttpServiceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

public final class HttpServiceController
{
    /**
     * Name of the Framework property indicating whether the servlet context
     * attributes of the ServletContext objects created for each HttpContext
     * used to register servlets and resources share their attributes or not.
     * By default (if this property is not specified or it's value is not
     * <code>true</code> (case-insensitive)) servlet context attributes are not
     * shared. To have servlet context attributes shared amongst servlet context
     * and also with the ServletContext provided by the servlet container ensure
     * setting the property as follows:
     * <pre>
     * org.apache.felix.http.shared_servlet_context_attributes = true
     * </pre>
     * <p>
     * <b>WARNING:</b> Only set this property if absolutely needed (for example
     * you implement an HttpSessionListener and want to access servlet context
     * attributes of the ServletContext to which the HttpSession is linked).
     * Otherwise leave this property unset.
     */
    private static final String FELIX_HTTP_SHARED_SERVLET_CONTEXT_ATTRIBUTES = "org.apache.felix.http.shared_servlet_context_attributes";

    private final BundleContext bundleContext;
    private final HandlerRegistry registry;
    private final Dispatcher dispatcher;
    private final Hashtable<String, Object> serviceProps;
    private final ServletContextAttributeListenerManager contextAttributeListener;
    private final ServletRequestListenerManager requestListener;
    private final ServletRequestAttributeListenerManager requestAttributeListener;
    private final HttpSessionListenerManager sessionListener;
    private final HttpSessionAttributeListenerManager sessionAttributeListener;
    private final boolean sharedContextAttributes;
    private ServiceRegistration<?> serviceReg;

    public HttpServiceController(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        this.registry = new HandlerRegistry();
        this.dispatcher = new Dispatcher(this.registry);
        this.serviceProps = new Hashtable<String, Object>();
        this.contextAttributeListener = new ServletContextAttributeListenerManager(bundleContext);
        this.requestListener = new ServletRequestListenerManager(bundleContext);
        this.requestAttributeListener = new ServletRequestAttributeListenerManager(bundleContext);
        this.sessionListener = new HttpSessionListenerManager(bundleContext);
        this.sessionAttributeListener = new HttpSessionAttributeListenerManager(bundleContext);
        this.sharedContextAttributes = getBoolean(FELIX_HTTP_SHARED_SERVLET_CONTEXT_ATTRIBUTES);
    }

    public Dispatcher getDispatcher()
    {
        return this.dispatcher;
    }

    public ServletContextAttributeListenerManager getContextAttributeListener()
    {
        return contextAttributeListener;
    }

    public ServletRequestListenerManager getRequestListener()
    {
        return requestListener;
    }

    public ServletRequestAttributeListenerManager getRequestAttributeListener()
    {
        return requestAttributeListener;
    }

    public HttpSessionListenerManager getSessionListener()
    {
        return sessionListener;
    }

    public HttpSessionAttributeListenerManager getSessionAttributeListener()
    {
        return sessionAttributeListener;
    }

    public void setProperties(Hashtable<String, Object> props)
    {
        this.serviceProps.clear();
        this.serviceProps.putAll(props);

        if (this.serviceReg != null) {
            this.serviceReg.setProperties(this.serviceProps);
        }
    }

    public void register(ServletContext servletContext)
    {
        this.contextAttributeListener.open();
        this.requestListener.open();
        this.requestAttributeListener.open();
        this.sessionListener.open();
        this.sessionAttributeListener.open();

        HttpServiceFactory factory = new HttpServiceFactory(servletContext, this.registry,
            this.contextAttributeListener, this.sharedContextAttributes);
        String[] ifaces = new String[] { HttpService.class.getName(), ExtHttpService.class.getName() };
        this.serviceReg = this.bundleContext.registerService(ifaces, factory, this.serviceProps);
    }

    public void unregister()
    {
        if (this.serviceReg == null) {
            return;
        }

        this.sessionAttributeListener.close();
        this.sessionListener.close();
        this.contextAttributeListener.close();
        this.requestListener.close();
        this.requestAttributeListener.close();

        try {
            this.serviceReg.unregister();
            this.registry.removeAll();
        } finally {
            this.serviceReg = null;
        }
    }

    private boolean getBoolean(final String property)
    {
        String prop = this.bundleContext.getProperty(property);
        return (prop != null) ? Boolean.valueOf(prop).booleanValue() : false;
    }
}
