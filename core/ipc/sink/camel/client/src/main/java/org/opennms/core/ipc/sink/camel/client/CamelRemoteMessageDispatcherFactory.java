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
package org.opennms.core.ipc.sink.camel.client;

import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_PRODUCER_DOMAIN;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.camel.CamelSinkConstants;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.distributed.core.api.Identity;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.MetricRegistry;

import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;

/**
 * Message dispatcher that sends messages via JMS.
 *
 * @author jwhite
 */
public class CamelRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<Map<String, Object>> {

    @EndpointInject(uri = "direct:sendMessage", context = "sinkClient")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:sendMessage", context = "sinkClient")
    private Endpoint endpoint;

    private BundleContext bundleContext;

    @Autowired
    private TracerRegistry tracerRegistry;

    private Identity identity;

    private MetricRegistry metrics;

    public <S extends Message, T extends Message> Map<String, Object> getModuleMetadata(SinkModule<S, T> module) {
        // Pre-compute the JMS headers instead of recomputing them every dispatch
        final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(
                CamelSinkConstants.JMS_QUEUE_PREFIX, module.getId());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CamelSinkConstants.JMS_QUEUE_NAME_HEADER, queueNameFactory.getName());
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Map<String, Object> headers, T message) {
        final Map<String, Object> messageHeaders = new HashMap<>(headers);
        module.getRoutingKey(message).ifPresent(id -> messageHeaders.put(CamelSinkConstants.JMS_XGROUP_ID, id));

        byte[] sinkMessageBytes = module.marshal(message);
        // Add tracing info to jms headers
        final Tracer tracer = tracerRegistry.getTracer();
        if (tracer.activeSpan() != null) {
            TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
            tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
            tracer.activeSpan().setTag(TracerConstants.TAG_LOCATION, identity.getLocation());
            tracer.activeSpan().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            if (messageHeaders.get(CamelSinkConstants.JMS_QUEUE_NAME_HEADER) instanceof String) {
                tracer.activeSpan().setTag(TracerConstants.TAG_TOPIC, (String) messageHeaders.get(CamelSinkConstants.JMS_QUEUE_NAME_HEADER));
            }
            tracer.activeSpan().setTag(TracerConstants.TAG_MESSAGE_SIZE, sinkMessageBytes.length);
            String tracingInfo = TracingInfoCarrier.marshalTracingInfo(tracingInfoCarrier.getTracingInfoMap());
            if (tracingInfo != null) {
                messageHeaders.put(CamelSinkConstants.JMS_SINK_TRACING_INFO, tracingInfo);
            }
        }
        template.sendBodyAndHeaders(endpoint, sinkMessageBytes, messageHeaders);
    }

    @Override
    public String getMetricDomain() {
        return SINK_METRIC_PRODUCER_DOMAIN;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void init() {
        if (tracerRegistry != null && identity != null) {
            tracerRegistry.init(identity.getLocation() + "@" + identity.getId());
        }
        onInit();
    }

    public void destroy() {
        onDestroy();
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    @Override
    public Tracer getTracer() {
        if (getTracerRegistry() != null) {
            return getTracerRegistry().getTracer();
        }
        return GlobalTracer.get();
    }

    @Override
    public MetricRegistry getMetrics() {
        if(metrics == null) {
            metrics = new MetricRegistry();
        }
        return metrics;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }
}
