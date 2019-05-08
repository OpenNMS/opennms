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

package org.opennms.core.ipc.sink.camel.client;

import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_PRODUCER_DOMAIN;

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
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

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

    private MinionIdentity minionIdentity;

    public <S extends Message, T extends Message> Map<String, Object> getModuleMetadata(SinkModule<S, T> module) {
        // Pre-compute the JMS headers instead of recomputing them every dispatch
        final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(
                CamelSinkConstants.JMS_QUEUE_PREFIX, module.getId());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CamelSinkConstants.JMS_QUEUE_NAME_HEADER, queueNameFactory.getName());
        return headers;
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Map<String, Object> headers, T message) {
        // Add tracing info to jms headers
        final Tracer tracer = tracerRegistry.getTracer();
        if (tracer.activeSpan() != null) {
            TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
            tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
            tracer.activeSpan().setTag(TracerConstants.TAG_LOCATION, minionIdentity.getLocation());
            String tracingInfo = TracingInfoCarrier.marshalTracingInfo(tracingInfoCarrier.getTracingInfoMap());
            if (tracingInfo != null) {
                headers.put(CamelSinkConstants.JMS_SINK_TRACING_INFO, tracingInfo);
            }
        }
        template.sendBodyAndHeaders(endpoint, module.marshal((T)message), headers);
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
        if (tracerRegistry != null && minionIdentity != null) {
            tracerRegistry.init(minionIdentity.getLocation() + "@" + minionIdentity.getId());
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
        return getTracerRegistry().getTracer();
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public MinionIdentity getMinionIdentity() {
        return minionIdentity;
    }

    public void setMinionIdentity(MinionIdentity minionIdentity) {
        this.minionIdentity = minionIdentity;
    }
}
