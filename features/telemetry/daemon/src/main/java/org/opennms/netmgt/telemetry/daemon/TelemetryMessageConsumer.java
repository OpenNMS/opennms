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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.ipc.TelemetryProtos;
import org.opennms.netmgt.telemetry.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class TelemetryMessageConsumer implements MessageConsumer<TelemetryMessage, TelemetryProtos.TelemetryMessageLog> {
    private final Logger LOG = LoggerFactory.getLogger(TelemetryMessageConsumer.class);

    @Autowired
    private TelemetryAdapterRegistry adapterRegistry;

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
            final Adapter adapter;
            try {
                adapter = adapterRegistry.getAdapter(adapterDef.getClassName(), protocolDef, adapterDef.getParameterMap());
            } catch (Exception e) {
                throw new Exception("Failed to create adapter from definition: " + adapterDef, e);
            }

            if (adapter == null) {
                throw new Exception("No adapter found for class: " + adapterDef.getClassName());
            }
            adapters.add(adapter);
        }
    }

    @Override
    public void handleMessage(TelemetryProtos.TelemetryMessageLog messageLog) {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Telemetryd.LOG_PREFIX)) {
            LOG.trace("Received message log: {}", messageLog);
            // Handle the message with all of the adapters
            for (Adapter adapter : adapters) {
                try {
                    adapter.handleMessageLog(messageLog);
                } catch (RuntimeException e) {
                    LOG.warn("Adapter: {} failed to handle message log: {}. Skipping.", adapter, messageLog, e);
                    continue;
                }
            }
        }
    }

    @PreDestroy
    public void destroy() {
        adapters.forEach((adapter) -> adapter.destroy());
    }

    @Override
    public SinkModule<TelemetryMessage, TelemetryProtos.TelemetryMessageLog> getModule() {
        return sinkModule;
    }

    public Protocol getProtocol() {
        return protocolDef;
    }
}
