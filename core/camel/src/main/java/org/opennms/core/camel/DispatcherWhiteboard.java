/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.camel;

import java.lang.reflect.Method;

import org.apache.camel.Consume;
import org.apache.camel.InOnly;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will redirect messages to the {@link DispatcherWhiteboard#m_endpointUri} URI
 * to any OSGi services that are registered at the interface that is defined by the
 * {@link #setServiceClass(String)} method call.
 */
@InOnly
public class DispatcherWhiteboard {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherWhiteboard.class);

    private BundleContext m_context;
    private Class<?> m_messageClass;
    private Class<?> m_serviceClass;
    private String m_methodName = "dispatch";
    private final String m_endpointUri;

    @SuppressWarnings("rawtypes")
    private ServiceTracker m_tracker = null;
    private Method m_method = null;

    public DispatcherWhiteboard(final String endpointUri) {
        m_endpointUri = endpointUri;
        LOG.info("DispatcherWhiteboard for endpoint {} initialized.", endpointUri);
    }

    public String getEndpointUri() {
        return m_endpointUri;
    }

    public void setContext(BundleContext context) {
        this.m_context = context;
    }

    public Class<?> getMessageClass() {
        return m_messageClass;
    }

    public void setMessageClass(Class<?> messageClass) {
        this.m_messageClass = messageClass;
    }

    public void setMessageClass(String messageClass) throws ClassNotFoundException {
        this.m_messageClass = Class.forName(messageClass);
    }

    public Class<?> getServiceClass() {
        return m_serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.m_serviceClass = serviceClass;
    }

    public void setServiceClass(String serviceClass) throws ClassNotFoundException {
        this.m_serviceClass = Class.forName(serviceClass);
    }

    public String getMethodName() {
        return m_methodName;
    }

    public void setMethodName(String methodName) {
        this.m_methodName = methodName;
    }

    public void destroy() {
        if (m_tracker != null) {
            m_tracker.close();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Consume(property="endpointUri")
    public void dispatch(final Object message) throws NoSuchMethodException, SecurityException {
        LOG.debug("dispatch: {}", message);
        if (m_tracker == null) {
            m_tracker = new ServiceTracker(m_context, m_serviceClass, null);
            m_tracker.open();
        }

        if (m_method == null) {
            m_method = m_serviceClass.getMethod(m_methodName, m_messageClass);
        }

        try {
            Object[] services = m_tracker.getServices();
            if (services != null && services.length > 0) {
                for (Object service : m_tracker.getServices()) {
                    m_method.invoke(service, message);
                }
            } else {
                // in case there is no dispatcher registered, let the user know.
                LOG.warn("No dispatcher for message found. ServiceClass: {}, ServiceMethod: {}", m_serviceClass, m_methodName);
            }
        } catch (Throwable e) {
            // If anything goes wrong, log an error message
            // TODO: Use a dead-letter channel?
            LOG.warn("Message dispatch failed: " + e.getMessage(), e);
        }
    }
}
