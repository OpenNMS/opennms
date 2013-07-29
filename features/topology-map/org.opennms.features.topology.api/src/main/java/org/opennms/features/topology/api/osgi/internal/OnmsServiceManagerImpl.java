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

package org.opennms.features.topology.api.osgi.internal;


import com.google.common.base.Strings;
import org.opennms.features.topology.api.osgi.EventRegistry;
import org.opennms.features.topology.api.osgi.OnmsServiceManager;
import org.opennms.features.topology.api.osgi.VaadinApplicationContext;
import org.opennms.features.topology.api.osgi.VaadinApplicationContextCreator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OnmsServiceManagerImpl implements OnmsServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(OnmsServiceManagerImpl.class);

    // key: Service
    private final Map<Object, ServiceRegistration> serviceRegistrations = Collections.synchronizedMap(new HashMap<Object, ServiceRegistration>());
    private final BundleContext bundleContext;

    public OnmsServiceManagerImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void registerAsService(Object object, VaadinApplicationContext applicationContext) {
        registerAsService(object, applicationContext, new Properties());
    }

    @Override
    public void registerAsService(Object object, VaadinApplicationContext applicationContext, Properties properties) {
        if (object == null) return;
        ServiceRegistration serviceRegistration = bundleContext
                .registerService(object.getClass().getName(), object, (Dictionary) getProperties(applicationContext, properties));
        serviceRegistrations.put(object, serviceRegistration);
    }

    @Override
    public EventRegistry getEventRegistry() {
        return getService(EventRegistry.class, null);
    }

    @Override
    public <T> T getService(Class<T> clazz, VaadinApplicationContext applicationContext) {
        List<T> services = getServices(clazz, applicationContext, new Properties());
        if (services.isEmpty()) return null;
        return services.get(0);
    }

    @Override
    public <T> List<T> getServices(Class<T> clazz, VaadinApplicationContext applicationContext, Properties additionalProperties) {
        List<T> services = new ArrayList<T>();
        try {
            ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences(clazz.getName(), getFilter(applicationContext, additionalProperties));
            if (serviceReferences != null) {
                for (ServiceReference eachServiceReference : serviceReferences) {
                    Object service = bundleContext.getService(eachServiceReference);
                    if (service == null) continue;
                    services.add((T)service);
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Invalid Filter definition in getService()", e);
        }
        return services;
    }

    @Override
    public void sessionInitialized(String sessionId) {
        ; // we don't want to do anything
    }

    /**
     * Remove all services from OSGi-container for the destroyed session. 
     */
    @Override
    public void sessionDestroyed(String sessionId) {
        final String sessionIdFilter = "(sessionId=%s)";
        try {
            ServiceReference[] allServiceReferences = bundleContext.getAllServiceReferences(null, String.format(sessionIdFilter, sessionId));
            if (allServiceReferences != null) {
                for (ServiceReference eachReference : allServiceReferences) {
                    Object service = bundleContext.getService(eachReference);
                    if (service == null) continue;
                    serviceRegistrations.get(service).unregister();
                    serviceRegistrations.remove(service);
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Error retrieving ServiceReferences", e);
        }
    }

    @Override
    public VaadinApplicationContext createApplicationContext(VaadinApplicationContextCreator creator) {
        VaadinApplicationContext newContext = creator.create(this);
        VaadinApplicationContext oldContext = getService(VaadinApplicationContext.class, newContext);
        if (oldContext != null) return oldContext;
        registerAsService(newContext, newContext);
        return newContext;
    }

    private String getFilter(VaadinApplicationContext applicationContext, Properties additionalProperties) {
        if (applicationContext == null && additionalProperties.isEmpty()) return null;
        String filter = "(&%s%s)";
        String sessionFilter = "(sessionId=%s)(uiId=%s)";
        String additionalPropertiesFilterString = getAdditionalPropertiesString(additionalProperties);
        String sessionFilterString = applicationContext == null ? "" : String.format(sessionFilter, applicationContext.getSessionId(), Integer.toString(applicationContext.getUiId()));
        return String.format(filter, sessionFilterString, additionalPropertiesFilterString);
    }

    private String getAdditionalPropertiesString(Properties additionalProperties) {
        String returnString = "";
        if (!additionalProperties.isEmpty()) {
            for (Map.Entry<Object, Object> eachEntry : additionalProperties.entrySet()) {
                returnString += "("+ eachEntry.getKey() + "=" + eachEntry.getValue() +")";
            }
        }
        return returnString;
    }

    private Properties getProperties(VaadinApplicationContext applicationContext, Properties properties) {
        if (properties == null) properties = new Properties();
        if (!Strings.isNullOrEmpty(applicationContext.getSessionId())) properties.put("sessionId", applicationContext.getSessionId());
        if (applicationContext.getUiId() > -1) properties.put("uiId", applicationContext.getUiId());
        return properties;
    }

    protected Object getService(ServiceReference serviceReference) {
        if (serviceReference == null) return null;
        return bundleContext.getService(serviceReference);
    }
}
