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
package org.opennms.core.ipc.sink.camel.server;

import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_CONSUMER_DOMAIN;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.camel.CamelSinkConstants;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * Automatically creates routes to consume from the JMS queues.
 *
 * @author jwhite
 */
public class CamelMessageConsumerManager extends AbstractMessageConsumerManager {

    private static final Logger LOG = LoggerFactory.getLogger(CamelMessageConsumerManager.class);

    private final CamelContext context;

    private final MetricRegistry metricRegistry;

    private final Map<SinkModule<?, Message>, String> routeIdsByModule = new ConcurrentHashMap<>();

    @Autowired
    private TracerRegistry tracerRegistry;

    @Autowired
    private Identity identity;

    private JmxReporter jmxReporter = null;

    public CamelMessageConsumerManager(CamelContext context, MetricRegistry metricRegistry) throws Exception {
        this.context = Objects.requireNonNull(context);
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
        context.start();
    }

    public CamelMessageConsumerManager(CamelContext context, Identity identity,
                                       TracerRegistry tracerRegistry, MetricRegistry metricRegistry) throws Exception {
        this.context = Objects.requireNonNull(context);
        this.identity = Objects.requireNonNull(identity);
        this.tracerRegistry = Objects.requireNonNull(tracerRegistry);
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
    }

    @Override
    protected synchronized void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (!routeIdsByModule.containsKey(module)) {
            LOG.info("Creating route for module: {}", module);
            final DynamicIpcRouteBuilder routeBuilder = new DynamicIpcRouteBuilder(context, this, module, tracerRegistry, metricRegistry);
            context.addRoutes(routeBuilder);
            routeIdsByModule.put(module, routeBuilder.getRouteId());
        }
    }

    @Override
    protected synchronized void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (routeIdsByModule.containsKey(module)) {
            LOG.info("Destroying route for module: {}", module);
            final String routeId = routeIdsByModule.remove(module);
            context.stopRoute(routeId);
            context.removeRoute(routeId);
        }
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public void start() {
        if (tracerRegistry != null && identity != null) {
            tracerRegistry.init(identity.getId());
        }

        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(SINK_METRIC_CONSUMER_DOMAIN).build();
        jmxReporter.start();
    }

    public void shutdown() {
        if(getStartupExecutor() != null) {
            getStartupExecutor().shutdown();
        }

        jmxReporter.stop();
    }

    private static final class DynamicIpcRouteBuilder extends RouteBuilder {
        private final CamelMessageConsumerManager consumerManager;
        private final SinkModule<?, Message> module;
        private final TracerRegistry tracerRegistry;
        private final MetricRegistry metricRegistry;

        private DynamicIpcRouteBuilder(CamelContext context, CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module,
                                       TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
            super(context);
            this.consumerManager = consumerManager;
            this.module = module;
            this.tracerRegistry = tracerRegistry;
            this.metricRegistry = metricRegistry;
        }

        public String getRouteId() {
            return "Sink.Server." + module.getId();
        }

        @Override
        public void configure() throws Exception {
            // Verify that the module returns a valid number. If number of threads is < 0,
            // creating the routes below does not work
            final int numberConsumerThrads = getNumConsumerThreads(module);
            final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(
                    CamelSinkConstants.JMS_QUEUE_PREFIX, module.getId());
            from(String.format("queuingservice:%s?concurrentConsumers=%d", queueNameFactory.getName(), numberConsumerThrads))
                .setExchangePattern(ExchangePattern.InOnly)
                .process(new CamelSinkServerProcessor(consumerManager, module, tracerRegistry, metricRegistry))
                .routeId(getRouteId());
        }
    }
}
