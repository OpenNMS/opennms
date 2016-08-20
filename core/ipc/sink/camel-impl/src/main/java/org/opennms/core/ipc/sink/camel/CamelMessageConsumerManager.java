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
import org.opennms.core.ipc.sink.api.SinkModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * Automatically creates routes to consume from the JMS queues.
 *
 * @author jwhite
 */
public class CamelMessageConsumerManager implements MessageConsumerManager {

    private static final Logger LOG = LoggerFactory.getLogger(CamelMessageConsumerManager.class);

    private final CamelContext context;

    private final Multimap<SinkModule<Message>, MessageConsumer<Message>> consumersByModule = LinkedListMultimap.create();
    private final Map<SinkModule<Message>, String> routeIdsByModule = new ConcurrentHashMap<>();

    public CamelMessageConsumerManager(CamelContext context) throws Exception {
        this.context = Objects.requireNonNull(context);
        context.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Message> void dispatch(SinkModule<T> module, T message) {
        consumersByModule.get((SinkModule<Message>)module)
            .forEach(c -> c.handleMessage(message));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Message> void registerConsumer(MessageConsumer<T> consumer)
            throws Exception {
        if (consumer == null) {
            return;
        }

        LOG.info("Registering consumer: {}", consumer);
        final SinkModule<Message> module = (SinkModule<Message>)consumer.getModule();
        consumersByModule.put(module, (MessageConsumer<Message>)consumer);
        if (!routeIdsByModule.containsKey(module)) {
            LOG.info("Creating route for module: {}", module);
            final DynamicIpcRouteBuilder routeBuilder = new DynamicIpcRouteBuilder(context, this, (SinkModule<Message>)consumer.getModule());
            context.addRoutes(routeBuilder);
            routeIdsByModule.put(module, routeBuilder.getRouteId());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Message> void unregisterConsumer(MessageConsumer<T> consumer)
            throws Exception {
        if (consumer == null) {
            return;
        }

        LOG.info("Unregistering consumer: {}", consumer);
        final SinkModule<Message> module = (SinkModule<Message>)consumer.getModule();
        consumersByModule.remove(module, (MessageConsumer<Message>)consumer);
        if (consumersByModule.get(module).size() < 1 && routeIdsByModule.containsKey(module)) {
            LOG.info("Destroying route for module: {}", module);
            final String routeId = routeIdsByModule.get(module);
            context.stopRoute(routeId);
            context.removeRoute(routeId);
        }
    }

    private static final class DynamicIpcRouteBuilder extends RouteBuilder {
        private final CamelMessageConsumerManager consumerManager;
        private final SinkModule<Message> module;

        private DynamicIpcRouteBuilder(CamelContext context, CamelMessageConsumerManager consumerManager, SinkModule<Message> module) {
            super(context);
            this.consumerManager = consumerManager;
            this.module = module;
        }

        public String getRouteId() {
            return "Sink.Server." + module.getId();
        }

        @Override
        public void configure() throws Exception {
            final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(
                    CamelSinkConstants.JMS_QUEUE_PREFIX, module.getId());
            from(String.format("queuingservice:%s", queueNameFactory.getName()))
                .setExchangePattern(ExchangePattern.InOnly)
                .process(new CamelSinkServerProcessor(consumerManager, module))
                .routeId(getRouteId());
        }
    }

}
