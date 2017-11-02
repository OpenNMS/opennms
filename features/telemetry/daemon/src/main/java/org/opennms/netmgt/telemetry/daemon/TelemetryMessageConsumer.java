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

package org.opennms.netmgt.telemetry.daemon;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.ipc.TelemetryProtos;
import org.opennms.netmgt.telemetry.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TelemetryMessageConsumer implements MessageConsumer<TelemetryMessage, TelemetryProtos.TelemetryMessageLog> {
    private final Logger LOG = LoggerFactory.getLogger(TelemetryMessageConsumer.class);

    private static final ServiceParameters EMPTY_SERVICE_PARAMETERS = new ServiceParameters(Collections.emptyMap());

    @Autowired
    private ApplicationContext applicationContext;

    private final Protocol protocolDef;
    private final TelemetrySinkModule sinkModule;
    private final List<Adapter> adapters;

    public TelemetryMessageConsumer(Protocol protocol, TelemetrySinkModule sinkModule) throws Exception {
        this.protocolDef = Objects.requireNonNull(protocol);
        this.sinkModule = Objects.requireNonNull(sinkModule);
        adapters = new ArrayList<>(protocol.getAdapters().size());
    }

    @PostConstruct
    public void init() throws Exception {
        // Pre-emptively instantiate the adapters
        for (org.opennms.netmgt.telemetry.config.model.Adapter adapterDef : protocolDef.getAdapters()) {
            try {
                adapters.add(buildAdapter(adapterDef));
            } catch (Exception e) {
                throw new Exception("Failed to create adapter from definition: " + adapterDef, e);
            }
        }
    }

    @Override
    public void handleMessage(TelemetryProtos.TelemetryMessageLog messageLog) {
        try(Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Telemetryd.LOG_PREFIX)) {
            LOG.trace("Received message log: {}", messageLog);
            // Handle the message with all of the adapters
            for (Adapter adapter : adapters) {
                try {
                    adapter.handleMessageLog(messageLog);
                } catch (RuntimeException e) {
                    LOG.warn("Adapter: {} failed to handle message log: {}. Skipping.", adapter, messageLog);
                    continue;
                }
            }
        }
    }

    private Adapter buildAdapter(org.opennms.netmgt.telemetry.config.model.Adapter adapterDef) throws Exception {
        // Instantiate the associated class
        final Object adapterInstance;
        try {
            final Class<?> clazz = Class.forName(adapterDef.getClassName());
            final Constructor<?> ctor = clazz.getConstructor();
            adapterInstance = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate adapter with class name '%s'.",
                    adapterDef.getClassName(), e));
        }

        // Cast
        if (!(adapterInstance instanceof Adapter)) {
            throw new IllegalArgumentException(String.format("%s must implement %s", adapterDef.getClassName(), Adapter.class.getCanonicalName()));
        }
        final Adapter adapter = (Adapter)adapterInstance;

        // Apply the parameters
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(adapter);
        wrapper.setPropertyValues(adapterDef.getParameterMap());

        // Autowire!
        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(adapter);
        beanFactory.initializeBean(adapter, "adapter");

        // Set the protocol reference
        adapter.setProtocol(protocolDef);

        return adapter;
    }

    @Override
    public SinkModule<TelemetryMessage, TelemetryProtos.TelemetryMessageLog> getModule() {
        return sinkModule;
    }

    public Protocol getProtocol() {
        return protocolDef;
    }
}
