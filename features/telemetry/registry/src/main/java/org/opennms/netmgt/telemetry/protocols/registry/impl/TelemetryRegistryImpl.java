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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.ConnectorDefinition;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.opennms.netmgt.telemetry.protocols.registry.api.TelemetryServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.codahale.metrics.MetricRegistry;

public class TelemetryRegistryImpl implements TelemetryRegistry {

    @Autowired
    @Qualifier("adapterRegistry")
    private TelemetryServiceRegistry<AdapterDefinition, Adapter> adapterRegistryDelegate;

    @Autowired
    @Qualifier("listenerRegistry")
    private TelemetryServiceRegistry<ListenerDefinition, Listener> listenerRegistryDelegate;

    @Autowired
    @Qualifier("connectorRegistry")
    private TelemetryServiceRegistry<ConnectorDefinition, Connector> connectorRegistryDelegate;

    @Autowired
    @Qualifier("parserRegistry")
    private TelemetryServiceRegistry<ParserDefinition, Parser> parserRegistryDelegate;

    private MetricRegistry metricRegistry;

    private final Map<String, AsyncDispatcher<TelemetryMessage>> dispatchers = new HashMap<>();

    @Override
    public Adapter getAdapter(AdapterDefinition adapterDefinition) {
        return adapterRegistryDelegate.getService(MutableAdapterDefinition.wrap(adapterDefinition));
    }

    @Override
    public Listener getListener(ListenerDefinition listenerDefinition) {
        return listenerRegistryDelegate.getService(MutableListenerDefinition.wrap(listenerDefinition));
    }

    @Override
    public Connector getConnector(ConnectorDefinition connectorDefinition) {
        return connectorRegistryDelegate.getService(MutableConnectorDefinition.wrap(connectorDefinition));
    }

    @Override
    public Parser getParser(ParserDefinition parserDefinition) {
        return parserRegistryDelegate.getService(MutableParserDefinition.wrap(parserDefinition));
    }

    @Override
    public void registerDispatcher(String queueName, AsyncDispatcher<TelemetryMessage> dispatcher) {
        if (dispatchers.containsKey(queueName)) {
            throw new IllegalArgumentException("A queue with name '" + queueName + "' is already registered");
        }
        this.dispatchers.put(queueName, dispatcher);
    }

    @Override
    public void clearDispatchers() {
        dispatchers.clear();
    }

    @Override
    public Collection<AsyncDispatcher<TelemetryMessage>> getDispatchers() {
        return dispatchers.values();
    }

    @Override
    public AsyncDispatcher<TelemetryMessage> getDispatcher(String queueName) {
        return dispatchers.get(queueName);
    }

    @Override
    public void removeDispatcher(String queueName) {
        dispatchers.remove(queueName);
    }

    public void setAdapterRegistryDelegate(TelemetryServiceRegistry<AdapterDefinition, Adapter> adapterRegistryDelegate) {
        this.adapterRegistryDelegate = adapterRegistryDelegate;
    }

    public void setListenerRegistryDelegate(TelemetryServiceRegistry<ListenerDefinition, Listener> listenerRegistryDelegate) {
        this.listenerRegistryDelegate = listenerRegistryDelegate;
    }

    public void setParserRegistryDelegate(TelemetryServiceRegistry<ParserDefinition, Parser> parserRegistryDelegate) {
        this.parserRegistryDelegate = parserRegistryDelegate;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    private static class MutableAdapterDefinition implements AdapterDefinition {
        private final AdapterDefinition definition;
        private final Map<String, String> parameters;

        private MutableAdapterDefinition(final AdapterDefinition definition) {
            this.definition = Objects.requireNonNull(definition);
            this.parameters = new HashMap<>(definition.getParameterMap());
        }

        @Override
        public String getName() {
            return this.definition.getName();
        }

        @Override
        public String getFullName() {
            return this.definition.getFullName();
        }

        @Override
        public String getClassName() {
            return this.definition.getClassName();
        }

        @Override
        public Map<String, String> getParameterMap() {
            return this.parameters;
        }

        @Override
        public List<? extends PackageDefinition> getPackages() {
            return this.definition.getPackages();
        }

        public static MutableAdapterDefinition wrap(final AdapterDefinition definition) {
            if (definition instanceof MutableAdapterDefinition) {
                return (MutableAdapterDefinition) definition;
            }

            return new MutableAdapterDefinition(definition);
        }
    }

    private static class MutableParserDefinition implements ParserDefinition {
        private final ParserDefinition definition;
        private final Map<String, String> parameters;

        private MutableParserDefinition(final ParserDefinition definition) {
            this.definition = Objects.requireNonNull(definition);
            this.parameters = new HashMap<>(definition.getParameterMap());
        }

        @Override
        public String getName() {
            return this.definition.getName();
        }

        @Override
        public String getFullName() {
            return this.definition.getFullName();
        }

        @Override
        public String getClassName() {
            return this.definition.getClassName();
        }

        @Override
        public Map<String, String> getParameterMap() {
            return this.parameters;
        }

        @Override
        public String getQueueName() {
            return this.definition.getQueueName();
        }

        public static MutableParserDefinition wrap(final ParserDefinition definition) {
            if (definition instanceof MutableParserDefinition) {
                return (MutableParserDefinition) definition;
            }

            return new MutableParserDefinition(definition);
        }
    }

    private static class MutableListenerDefinition implements ListenerDefinition {
        private final ListenerDefinition definition;
        private final Map<String, String> parameters;
        private final List<MutableParserDefinition> parsers;

        private MutableListenerDefinition(final ListenerDefinition definition) {
            this.definition = Objects.requireNonNull(definition);
            this.parameters = new HashMap<>(definition.getParameterMap());
            this.parsers = definition.getParsers().stream()
                    .map(MutableParserDefinition::new)
                    .collect(Collectors.toList());
        }

        @Override
        public String getName() {
            return this.definition.getName();
        }

        @Override
        public String getClassName() {
            return this.definition.getClassName();
        }

        @Override
        public Map<String, String> getParameterMap() {
            return this.parameters;
        }

        @Override
        public List<? extends ParserDefinition> getParsers() {
            return this.parsers;
        }

        public static MutableListenerDefinition wrap(final ListenerDefinition definition) {
            if (definition instanceof MutableListenerDefinition) {
                return (MutableListenerDefinition) definition;
            }

            return new MutableListenerDefinition(definition);
        }
    }

    private static class MutableConnectorDefinition implements ConnectorDefinition {
        private final ConnectorDefinition definition;
        private final Map<String, String> parameters;

        private MutableConnectorDefinition(final ConnectorDefinition definition) {
            this.definition = Objects.requireNonNull(definition);
            this.parameters = new HashMap<>(definition.getParameterMap());
        }

        @Override
        public String getName() {
            return this.definition.getName();
        }

        @Override
        public String getQueueName() {
            return this.definition.getQueueName();
        }

        @Override
        public String getServiceName() {
            return this.definition.getServiceName();
        }

        @Override
        public List<? extends PackageDefinition> getPackages() {
            return this.definition.getPackages();
        }

        @Override
        public String getClassName() {
            return this.definition.getClassName();
        }

        @Override
        public Map<String, String> getParameterMap() {
            return this.parameters;
        }

        public static MutableConnectorDefinition wrap(final ConnectorDefinition definition) {
            if (definition instanceof MutableConnectorDefinition) {
                return (MutableConnectorDefinition) definition;
            }

            return new MutableConnectorDefinition(definition);
        }
    }
}
