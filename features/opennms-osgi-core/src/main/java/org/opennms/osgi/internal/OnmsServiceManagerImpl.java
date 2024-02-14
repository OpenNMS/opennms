/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.osgi.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.opennms.osgi.EventRegistry;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextCreator;
import org.opennms.osgi.locator.EventRegistryLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnmsServiceManagerImpl implements OnmsServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(OnmsServiceManagerImpl.class);

    // key: Service
    private final Map<Object, ServiceRegistration<?>> serviceRegistrations = Collections.synchronizedMap(new HashMap<Object, ServiceRegistration<?>>());
    private final BundleContext bundleContext;

    public OnmsServiceManagerImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public <T> void registerAsService(Class<T> serviceClass, T serviceBean, VaadinApplicationContext applicationContext) {
        registerAsService(serviceClass, serviceBean, applicationContext, new Hashtable<String,Object>());
    }

    @Override
    public <T> void registerAsService(Class<T> serviceClass, T serviceBean, VaadinApplicationContext applicationContext, Dictionary<String,Object> properties) {
        if (serviceBean == null || serviceClass == null) return;
        ServiceRegistration<T> serviceRegistration = bundleContext.registerService(serviceClass, serviceBean, getProperties(applicationContext, properties));
        serviceRegistrations.put(serviceBean, serviceRegistration);
    }

    @Override
    public EventRegistry getEventRegistry() {
        return new EventRegistryLocator().lookup(bundleContext);
    }

    @Override
    public <T> T getService(Class<T> clazz, VaadinApplicationContext applicationContext) {
        List<T> services = getServices(clazz, applicationContext, new Hashtable<String,Object>());
        if (services.isEmpty()) return null;
        return services.get(0);
    }

    @Override
    public <T> List<T> getServices(Class<T> clazz, VaadinApplicationContext applicationContext, Hashtable<String,Object> additionalProperties) {
        if (additionalProperties == null) {
            additionalProperties = new Hashtable<String,Object>();
        }
        List<T> services = new ArrayList<>();
        try {
            Collection<ServiceReference<T>> serviceReferences = bundleContext.getServiceReferences(clazz, getFilter(applicationContext, additionalProperties));
            if (serviceReferences != null) {
                for (ServiceReference<T> eachServiceReference : serviceReferences) {
                    T service = bundleContext.getService(eachServiceReference);
                    if (service == null) continue;
                    services.add(service);
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Invalid Filter definition in getService()", e);
        }
        return services;
    }

    @Override
    public void sessionInitialized(String sessionId) {
        // we don't want to do anything
    }

    /**
     * Remove all services from OSGi-container for the destroyed session. 
     */
    @Override
    public void sessionDestroyed(String sessionId) {
        final String sessionIdFilter = "(sessionId=%s)";
        try {
            ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(null, String.format(sessionIdFilter, sessionId));
            if (allServiceReferences != null) {
                for (ServiceReference<?> eachReference : allServiceReferences) {
                    Object service = bundleContext.getService(eachReference);
                    if (service == null) continue;
                    if (serviceRegistrations.get(service) == null) continue; // wrong bundleContext/OnmsServiceManager
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
        registerAsService(VaadinApplicationContext.class, newContext, newContext);
        return newContext;
    }

    private String getFilter(VaadinApplicationContext applicationContext, Hashtable<String,Object> additionalProperties) {
        if (applicationContext == null && additionalProperties.isEmpty()) return null;
        String filter = "(&%s%s)";
        String sessionFilter = "(sessionId=%s)(uiId=%s)";
        String additionalPropertiesFilterString = getAdditionalPropertiesString(additionalProperties);
        String sessionFilterString = applicationContext == null ? "" : String.format(sessionFilter, applicationContext.getSessionId(), Integer.toString(applicationContext.getUiId()));
        return String.format(filter, sessionFilterString, additionalPropertiesFilterString);
    }

    private String getAdditionalPropertiesString(Hashtable<String,Object> additionalProperties) {
        String returnString = "";
        if (!additionalProperties.isEmpty()) {
            for (Map.Entry<String, Object> eachEntry : additionalProperties.entrySet()) {
                returnString += "("+ eachEntry.getKey() + "=" + eachEntry.getValue() +")";
            }
        }
        return returnString;
    }

    private Dictionary<String,Object> getProperties(VaadinApplicationContext applicationContext, Dictionary<String,Object> properties) {
        if (properties == null) properties = new Hashtable<String,Object>();
        String sessionId = applicationContext.getSessionId();
        if (sessionId != null && !sessionId.isEmpty()) properties.put("sessionId", applicationContext.getSessionId());
        if (applicationContext.getUiId() > -1) properties.put("uiId", applicationContext.getUiId());
        return properties;
    }
}
