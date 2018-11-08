/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.registry.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.netmgt.telemetry.protocols.registry.api.TelemetryServiceRegistry;
import org.opennms.netmgt.telemetry.api.TelemetryBeanFactory;
import org.opennms.netmgt.telemetry.config.api.TelemetryBeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                return (T) m_serviceFactoryByClassName.get(criteria);
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
