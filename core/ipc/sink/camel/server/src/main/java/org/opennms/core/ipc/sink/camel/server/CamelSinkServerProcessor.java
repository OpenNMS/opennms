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

package org.opennms.core.ipc.sink.camel.server;

import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_CONSUMER_DOMAIN;
import static org.opennms.core.ipc.sink.api.MessageConsumerManager.METRIC_DISPATCH_TIME;
import static org.opennms.core.ipc.sink.api.MessageConsumerManager.METRIC_MESSAGES_RECEIVED;
import static org.opennms.core.ipc.sink.api.MessageConsumerManager.METRIC_MESSAGE_SIZE;

import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class CamelSinkServerProcessor implements Processor {

    private final CamelMessageConsumerManager consumerManager;
    private final SinkModule<?, Message> module;
    private MetricRegistry metricRegistry = new MetricRegistry();
    private JmxReporter jmxReporter = null;
    private Meter messagesReceived;
    private Histogram messageSize;
    private Timer dispatchTime;

    public CamelSinkServerProcessor(CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module) {
        this.consumerManager = Objects.requireNonNull(consumerManager);
        this.module = Objects.requireNonNull(module);
        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(SINK_METRIC_CONSUMER_DOMAIN).build();
        jmxReporter.start();
        messagesReceived = metricRegistry.meter(MetricRegistry.name(module.getId(), METRIC_MESSAGES_RECEIVED));
        messageSize = metricRegistry.histogram(MetricRegistry.name(module.getId(), METRIC_MESSAGE_SIZE));
        dispatchTime = metricRegistry.timer(MetricRegistry.name(module.getId(), METRIC_DISPATCH_TIME));
    }

    @Override
    public void process(Exchange exchange) {
        final byte[] messageBytes = exchange.getIn().getBody(byte[].class);
        final Message message = module.unmarshal(messageBytes);
        // Update metrics.
        messagesReceived.mark();
        messageSize.update(messageBytes.length);
        try(Timer.Context context = dispatchTime.time()) {
            // Dispatch messages to specific module.
            consumerManager.dispatch(module, message);
        }
    }
}
