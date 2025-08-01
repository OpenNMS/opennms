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
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.util.GlobalTracer;

public class CamelSinkServerProcessor implements Processor {

    private final CamelMessageConsumerManager consumerManager;
    private final SinkModule<?, Message> module;
    private final TracerRegistry tracerRegistry;
    private Histogram messageSize;
    private Timer dispatchTime;

    public CamelSinkServerProcessor(CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module,
                                    TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        this.consumerManager = Objects.requireNonNull(consumerManager);
        this.module = Objects.requireNonNull(module);
        this.tracerRegistry = tracerRegistry;

        this.messageSize = metricRegistry.histogram(MetricRegistry.name(module.getId(), METRIC_MESSAGE_SIZE));
        this.dispatchTime = metricRegistry.timer(MetricRegistry.name(module.getId(), METRIC_DISPATCH_TIME));
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
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(tracingInfo));
        if (context != null) {
            spanBuilder = tracer.buildSpan(module.getId()).asChildOf(context);
        } else {
            spanBuilder = tracer.buildSpan(module.getId());
        }
        return spanBuilder;

    }
}
