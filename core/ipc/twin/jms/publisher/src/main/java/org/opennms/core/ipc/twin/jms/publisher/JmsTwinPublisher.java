/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.jms.publisher;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsEndpoint;
import org.opennms.core.ipc.twin.api.TwinStrategy;
import org.opennms.core.ipc.twin.common.AbstractTwinPublisher;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequest;
import org.opennms.core.ipc.twin.common.TwinUpdate;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class JmsTwinPublisher extends AbstractTwinPublisher implements AsyncProcessor {

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("jms-twin-publisher-%d")
            .build();

    private static final String TWIN_QUEUE_NAME_FORMAT = "%s.%s";

    private static final Logger LOG = LoggerFactory.getLogger(JmsTwinPublisher.class);

    public static String JMS_QUEUE_NAME_HEADER = "JmsQueueName";

    private final CamelContext rpcCamelContext;

    private final ExecutorService executor = Executors.newFixedThreadPool(10, threadFactory);

    @EndpointInject(uri = "direct:sendTwinUpdate", context = "twinSinkClient")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:sendTwinUpdate", context = "twinSinkClient")
    private Endpoint endpoint;

    public JmsTwinPublisher(CamelContext camelContext, LocalTwinSubscriber twinSubscriber,
                            TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        super(twinSubscriber, tracerRegistry, metricRegistry);
        this.rpcCamelContext = camelContext;
    }

    @Override
    protected void handleSinkUpdate(TwinUpdate sinkUpdate) {
        try {
            TwinResponseProto twinResponseProto = mapTwinResponse(sinkUpdate);
            Map<String, Object> headers = new HashMap<>();
            String queueName = String.format(TWIN_QUEUE_NAME_FORMAT, SystemInfoUtils.getInstanceId(), "Twin.Sink");
            headers.put(JMS_QUEUE_NAME_HEADER, queueName);
            template.sendBodyAndHeaders(twinResponseProto.toByteArray(), headers);
        } catch (Exception e) {
            LOG.error("Exception while sending update for key {} at location {} ", sinkUpdate.getKey(), sinkUpdate.getLocation());
        }
    }

    public void init() throws Exception {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            rpcCamelContext.addRoutes(new RpcRouteBuilder(this, rpcCamelContext));
            LOG.info("JMS Twin publisher initialized");
        }
    }

    public void close() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            executor.shutdownNow();
            LOG.info("JMS Twin publisher stopped");
        }
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        byte[] requestBytes = exchange.getIn().getBody(byte[].class);
        CompletableFuture.runAsync(() -> {
            try {
                TwinRequest twinRequest = mapTwinRequestProto(requestBytes);
                String tracingOperationKey = generateTracingOperationKey(twinRequest.getLocation(), twinRequest.getKey());
                Tracer.SpanBuilder spanBuilder = TracingInfoCarrier.buildSpanFromTracingMetadata(getTracer(),
                        tracingOperationKey, twinRequest.getTracingInfo(), References.FOLLOWS_FROM);
                try(Scope scope = spanBuilder.startActive(true)) {
                    TwinUpdate twinUpdate = getTwin(twinRequest);
                    addTracingInfo(scope.span(), twinUpdate);
                    TwinResponseProto twinResponseProto = mapTwinResponse(twinUpdate);
                    exchange.getOut().setBody(twinResponseProto.toByteArray());
                    callback.done(false);
                }
            } catch (Exception e) {
                LOG.error("Exception while processing request", e);
            }
        }, executor);
        return false;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // Ensure that only async. calls are made.
        throw new UnsupportedOperationException("This processor must be invoked using the async interface.");
    }

    private class RpcRouteBuilder extends RouteBuilder {

        private final AsyncProcessor asyncProcessor;

        private RpcRouteBuilder(AsyncProcessor asyncProcessor, CamelContext camelContext) {
            super(camelContext);
            this.asyncProcessor = asyncProcessor;
        }

        @Override
        public void configure() throws Exception {
            String queueName = String.format(TWIN_QUEUE_NAME_FORMAT, SystemInfoUtils.getInstanceId(), "Twin.RPC");
            final JmsEndpoint endpoint = getContext().getEndpoint(String.format("queuingservice:%s?asyncConsumer=true",
                    queueName), JmsEndpoint.class);
            from(endpoint).setExchangePattern(ExchangePattern.InOut)
                    .process(asyncProcessor)
                    .routeId(SystemInfoUtils.getInstanceId() + ".Twin.RPC");
        }
    }
}
