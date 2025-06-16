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
package org.opennms.netmgt.telemetry.daemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.common.ipc.TelemetryProtos;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.QueueDefinition;
import org.opennms.netmgt.telemetry.config.model.QueueConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;


public class TelemetryMessageConsumer implements MessageConsumer<TelemetryMessage, TelemetryProtos.TelemetryMessageLog> {
    private final Logger LOG = LoggerFactory.getLogger(TelemetryMessageConsumer.class);

    @Autowired
    private TelemetryRegistry telemetryRegistry;

    private final QueueDefinition queueDef;
    private final TelemetrySinkModule sinkModule;
    private final List<AdapterDefinition> adapterDefs;

    // Actual adapters implementing the logic
    private final Set<Adapter> adapters = Sets.newHashSet();

    public TelemetryMessageConsumer(QueueConfig queueConfig, TelemetrySinkModule sinkModule) throws Exception {
        this(queueConfig,
                queueConfig.getAdapters(),
                sinkModule);
    }

    public TelemetryMessageConsumer(QueueDefinition queueDef,
                                    Collection<? extends AdapterDefinition> adapterDefs,
                                    TelemetrySinkModule sinkModule) {
        this.queueDef = Objects.requireNonNull(queueDef);
        this.sinkModule = Objects.requireNonNull(sinkModule);
        this.adapterDefs = new ArrayList(adapterDefs);
    }

    @PostConstruct
    public void init() throws Exception {
        // Pre-emptively instantiate the adapters
        for (AdapterDefinition adapterDef : adapterDefs) {
            final Adapter adapter;
            try {
                adapter = telemetryRegistry.getAdapter(adapterDef);
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

    public QueueDefinition getQueue() {
        return queueDef;
    }

    public void setRegistry(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }

    public Set<Adapter> getAdapters() {
        return this.adapters;
    }
}
