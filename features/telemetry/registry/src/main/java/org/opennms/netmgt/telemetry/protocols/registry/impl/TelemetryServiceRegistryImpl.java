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
package org.opennms.netmgt.telemetry.protocols.registry.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.utils.properties.DurationPropertyEditor;
import org.opennms.netmgt.telemetry.protocols.registry.api.TelemetryServiceRegistry;
import org.opennms.netmgt.telemetry.api.TelemetryBeanFactory;
import org.opennms.netmgt.telemetry.config.api.TelemetryBeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Maintains the list of available telemtryd services, aggregating
 * those expose the the service loader and via the OSGi registry.
 *
 * @author mvrueden
 */
public class TelemetryServiceRegistryImpl<F extends TelemetryBeanFactory, BD extends TelemetryBeanDefinition, T> implements TelemetryServiceRegistry<BD, T>, ServiceLookup<String, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryServiceRegistryImpl.class);

    private final Map<String, ServiceFactoryRegistration> m_serviceFactoryByClassName = new HashMap<>();
    private final ServiceLookup<String, Void> delegate;

    @Autowired
    private ApplicationContext applicationContext;

    public TelemetryServiceRegistryImpl(Supplier<ServiceLoader<F>> staticServiceSupplier) {
        this(staticServiceSupplier, ServiceLookupBuilder.GRACE_PERIOD_MS, ServiceLookupBuilder.WAIT_PERIOD_MS, ServiceLookupBuilder.LOOKUP_DELAY_MS);
    }

    public TelemetryServiceRegistryImpl(Supplier<ServiceLoader<F>> staticServiceSupplier, long gracePeriodMs, long waitPeriodMs, long lookupDelayMs) {
        this.delegate = new ServiceLookupBuilder(new ServiceLookup<String, Void>() {
            @Override
            public <T> T lookup(String criteria, Void filter) {
                synchronized(TelemetryServiceRegistryImpl.this) {
                    return (T) m_serviceFactoryByClassName.get(criteria);
                }
            }
        }).blocking(gracePeriodMs, lookupDelayMs, waitPeriodMs).build();

        if (staticServiceSupplier != null) {
            // Register all of the factories exposed via the service loader
            for (F serviceFactory : staticServiceSupplier.get()) {
                final String className = serviceFactory.getBeanClass().getCanonicalName();
                m_serviceFactoryByClassName.put(className, new ServiceFactoryRegistration(serviceFactory, true));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onBind(F serviceFactory, Map properties) {
        LOG.debug("Bind called with {}: {}", serviceFactory, properties);
        if (serviceFactory != null) {
            final Class<?> clazz = serviceFactory.getBeanClass();
            if (clazz == null) {
                LOG.warn("Unable to determine the class for ServiceFactory: {}, with properties: {}. The service will not be registered.",
                        serviceFactory, properties);
                return;
            }
            final String className = clazz.getCanonicalName();
            m_serviceFactoryByClassName.put(className, new ServiceFactoryRegistration(serviceFactory, false));
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized void onUnbind(F serviceFactory, Map properties) {
        LOG.debug("Unbind called with {}: {}", serviceFactory, properties);
        if (serviceFactory != null) {
            final Class<?> clazz = serviceFactory.getBeanClass();
            if (clazz == null) {
                LOG.warn("Unable to determine the class name for ServiceFactory: {}, with properties: {}. The service will not be unregistered.",
                        serviceFactory, properties);
                return;
            }
            final String className = clazz.getCanonicalName();
            m_serviceFactoryByClassName.remove(className);
        }
    }

    @Override
    public T getService(BD beanDefinition) {
        final ServiceFactoryRegistration<F> registration = delegate.lookup(beanDefinition.getClassName(), null);
        if (registration != null) {
            final T service = (T) registration.getServiceFactory().createBean(beanDefinition);

            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(service);
            wrapper.registerCustomEditor(Duration.class, new DurationPropertyEditor());
            wrapper.setPropertyValues(beanDefinition.getParameterMap());

            if (registration.shouldAutowire()) {
                // Autowire!
                final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBean(service);
                beanFactory.initializeBean(service, "service");
            }
            return service;
        }
        return null;
    }

    @Override
    public <T> T lookup(String criteria, Void filter) {
        return delegate.lookup(criteria, filter);
    }

    private static class ServiceFactoryRegistration<T extends TelemetryBeanFactory> {
        private final T factory;
        private final boolean autowire;

        public ServiceFactoryRegistration(T factory, boolean autowire) {
            this.factory = Objects.requireNonNull(factory);
            this.autowire = autowire;
        }

        public T getServiceFactory() {
            return factory;
        }

        public boolean shouldAutowire() {
            return autowire;
        }
    }
}
