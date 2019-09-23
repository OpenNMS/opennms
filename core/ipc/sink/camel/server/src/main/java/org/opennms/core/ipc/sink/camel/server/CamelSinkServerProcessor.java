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
import static org.opennms.core.ipc.sink.api.MessageConsumerManager.METRIC_MESSAGE_SIZE;
import static org.opennms.core.ipc.sink.camel.CamelSinkConstants.JMS_QUEUE_NAME_HEADER;
import static org.opennms.core.ipc.sink.camel.CamelSinkConstants.JMS_SINK_TRACING_INFO;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.util.GlobalTracer;

public class CamelSinkServerProcessor implements Processor {

    private final CamelMessageConsumerManager consumerManager;
    private final SinkModule<?, Message> module;
    private final TracerRegistry tracerRegistry;
    private final MetricRegistry metricRegistry;
    private JmxReporter jmxReporter = null;
    private Histogram messageSize;
    private Timer dispatchTime;

    public CamelSinkServerProcessor(CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module,
                                    TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        this.consumerManager = Objects.requireNonNull(consumerManager);
        this.module = Objects.requireNonNull(module);
        this.tracerRegistry = tracerRegistry;
        this.metricRegistry = metricRegistry;
        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(SINK_METRIC_CONSUMER_DOMAIN).build();
        jmxReporter.start();
        messageSize = metricRegistry.histogram(MetricRegistry.name(module.getId(), METRIC_MESSAGE_SIZE));
        dispatchTime = metricRegistry.timer(MetricRegistry.name(module.getId(), METRIC_DISPATCH_TIME));
    }

    @Override
    public void process(Exchange exchange) {
        final byte[] messageBytes = exchange.getIn().getBody(byte[].class);
        // build span from message headers and retrieve custom tags into tracing info.
        Map<String, String> tracingInfo = new HashMap<>();
        Tracer.SpanBuilder spanBuilder = buildSpanFromHeaders(exchange.getIn(), tracingInfo);
        // Update metrics.
        messageSize.update(messageBytes.length);
        try (Scope scope = spanBuilder.startActive(true);
             Timer.Context context = dispatchTime.time()) {
            // Set tags for this span.
            scope.span().setTag(TracerConstants.TAG_MESSAGE_SIZE, messageBytes.length);
            scope.span().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            if (exchange.getIn().getHeader(JMS_QUEUE_NAME_HEADER) instanceof String) {
                String topic = exchange.getIn().getHeader(JMS_QUEUE_NAME_HEADER, String.class);
                scope.span().setTag(TracerConstants.TAG_TOPIC, topic);
            }
            final Message message = module.unmarshal(messageBytes);
            consumerManager.dispatch(module, message);
        }
    }

    private Tracer.SpanBuilder buildSpanFromHeaders(org.apache.camel.Message message, Map<String, String> tracingInfo) {
        String tracingInfoObj = message.getHeader(JMS_SINK_TRACING_INFO, String.class);
        if (tracingInfoObj != null) {
            tracingInfo.putAll(TracingInfoCarrier.unmarshalTracinginfo(tracingInfoObj));
        }
        Tracer.SpanBuilder spanBuilder;
        Tracer tracer;
        if (tracerRegistry != null) {
            tracer = tracerRegistry.getTracer();
        } else {
            tracer = GlobalTracer.get();
        }
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingInfo));
        if (context != null) {
            spanBuilder = tracer.buildSpan(module.getId()).asChildOf(context);
        } else {
            spanBuilder = tracer.buildSpan(module.getId());
        }
        return spanBuilder;

    }
}
