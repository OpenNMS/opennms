/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.camel;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically creates routes to consume from the JMS queues.
 *
 * @author jwhite
 */
public class CamelMessageConsumerManager extends AbstractMessageConsumerManager {

    private static final Logger LOG = LoggerFactory.getLogger(CamelMessageConsumerManager.class);

    private final CamelContext context;

    private final Map<SinkModule<?, Message>, String> routeIdsByModule = new ConcurrentHashMap<>();

    public CamelMessageConsumerManager(CamelContext context) throws Exception {
        this.context = Objects.requireNonNull(context);
        context.start();
    }

    @Override
    protected synchronized void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (!routeIdsByModule.containsKey(module)) {
            LOG.info("Creating route for module: {}", module);
            final DynamicIpcRouteBuilder routeBuilder = new DynamicIpcRouteBuilder(context, this, module);
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

    private static final class DynamicIpcRouteBuilder extends RouteBuilder {
        private final CamelMessageConsumerManager consumerManager;
        private final SinkModule<?, Message> module;

        private DynamicIpcRouteBuilder(CamelContext context, CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module) {
            super(context);
            this.consumerManager = consumerManager;
            this.module = module;
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
                .process(new CamelSinkServerProcessor(consumerManager, module))
                .routeId(getRouteId());
        }
    }
}
