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

package org.opennms.core.rpc.camel;

import static org.opennms.core.rpc.camel.CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT;
import static org.opennms.core.rpc.camel.CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY;
import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_RPC_FAILED;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;
import static org.opennms.core.tracing.api.TracerConstants.TAG_TIMEOUT;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.direct.DirectConsumerNotAvailableException;
import org.apache.camel.spi.Synchronization;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestRejectedException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.TimeLimiter;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public class CamelRpcClientFactory implements RpcClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcClientFactory.class);

    /**
     * Re-use the value of the default TTL as the default timeout for RPC request execution.
     * This value is bounded by the actual value of the TTL in the request when set.
     */
    private static final long rpcExecTimeoutMs = SystemProperties.getLong(CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY, CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT);

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("CamelRpcClientFactory-Pool-%d")
            .build();

    private String location;

    private ExecutorService executor;

    private TimeLimiter timeLimiter;

    @EndpointInject(uri = "direct:executeRpc", context = "rpcClient")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:executeRpc", context = "rpcClient")
    private Endpoint endpoint;

    @Autowired
    private TracerRegistry tracerRegistry;

    private Tracer tracer;

    private MetricRegistry metrics;

    private JmxReporter metricsReporter = null;

    @Override
    public <S extends RpcRequest, T extends RpcResponse> RpcClient<S,T> getClient(RpcModule<S,T> module) {
        return new RpcClient<S,T>() {
            @Override
            public CompletableFuture<T> execute(S request) {
                if (request.getLocation() == null || request.getLocation().equals(location)) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }
                // Save the context map and restore it on callback
                final Map<String, String> clientContextMap = Logging.getCopyOfContextMap();
                // Build span with module id and start it.
                Span span = tracer.buildSpan(module.getId()).start();
                span.setTag(TAG_LOCATION, request.getLocation());
                if(request.getSystemId() != null) {
                    span.setTag(TAG_SYSTEM_ID, request.getSystemId());
                }
                request.getTracingInfo().forEach(span::setTag);
                TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
                tracer.inject(span.context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
                //Add custom tags to tracing info.
                request.getTracingInfo().forEach(tracingInfoCarrier::put);
                // Build or retrieve rpc metrics.
                final Histogram rpcDuration = getMetrics().histogram(MetricRegistry.name(request.getLocation(), module.getId(), RPC_DURATION));
                final Histogram responseSize = getMetrics().histogram(MetricRegistry.name(request.getLocation(), module.getId(), RPC_RESPONSE_SIZE));
                final Meter failedMeter = getMetrics().meter(MetricRegistry.name(request.getLocation(), module.getId(), RPC_FAILED));
                long requestCreationTime = System.currentTimeMillis();
                // Wrap the request in a CamelRpcRequest and forward it to the Camel route
                final CompletableFuture<T> future = new CompletableFuture<>();
                try {
                    // Even though these calls are expected to be async, we've encountered cases where
                    // they do block. In order to prevent this, we wrap the calls with a timeout.

                    // Compute the amount of maximum amount of time we're willing to wait for the call to be dispatched
                    Long execTimeoutMs = request.getTimeToLiveMs();
                    if (execTimeoutMs != null) {
                        // If a TTL is set, the use the minimum value, of the TTL, or our exec timeout
                        execTimeoutMs = Math.min(execTimeoutMs, rpcExecTimeoutMs);
                    } else {
                        execTimeoutMs = rpcExecTimeoutMs;
                    }

                    timeLimiter.callWithTimeout(() -> {
                        template.asyncCallbackSendBody(endpoint, new CamelRpcRequest<>(module, request, tracingInfoCarrier.getTracingInfoMap()), new Synchronization() {
                            @Override
                            public void onComplete(Exchange exchange) {
                                try (MDCCloseable mdc = Logging.withContextMapCloseable(clientContextMap)) {
                                    String responseAsString = exchange.getOut().getBody(String.class);
                                    responseSize.update(responseAsString.getBytes().length);
                                    final T response = module.unmarshalResponse(responseAsString);
                                    if (response.getErrorMessage() != null) {
                                        future.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                                        span.setTag(TAG_RPC_FAILED, "true");
                                        span.log(response.getErrorMessage());
                                    } else {
                                        future.complete(response);
                                    }
                                } catch (Throwable ex) {
                                    LOG.error("Unmarshalling a response in RPC module {} failed.", module, ex);
                                    future.completeExceptionally(ex);
                                    span.setTag(TAG_RPC_FAILED, "true");
                                    span.log(ex.getMessage());
                                }
                                span.finish();
                                rpcDuration.update(System.currentTimeMillis() - requestCreationTime);
                                // Ensure that future log statements on this thread are routed properly
                                Logging.putPrefix(RpcClientFactory.LOG_PREFIX);
                            }

                            @Override
                            public void onFailure(Exchange exchange) {
                                try (MDCCloseable mdc = Logging.withContextMapCloseable(clientContextMap)) {
                                    final ExchangeTimedOutException timeoutException = exchange.getException(ExchangeTimedOutException.class);
                                    final DirectConsumerNotAvailableException directConsumerNotAvailableException = exchange.getException(DirectConsumerNotAvailableException.class);
                                    if (timeoutException != null) {
                                        // Wrap timeout exceptions within a RequestTimedOutException
                                        future.completeExceptionally(new RequestTimedOutException(exchange.getException()));
                                        span.setTag(TAG_TIMEOUT, "true");
                                    } else if (directConsumerNotAvailableException != null) {
                                        // Wrap consumer not available exceptions with a RequestRejectedException
                                        future.completeExceptionally(new RequestRejectedException(exchange.getException()));
                                    } else {
                                        future.completeExceptionally(exchange.getException());
                                    }
                                }
                                span.setTag(TAG_RPC_FAILED, "true");
                                span.log(exchange.getException().getMessage());
                                span.finish();
                                failedMeter.mark();
                                rpcDuration.update(System.currentTimeMillis() - requestCreationTime);
                                // Ensure that future log statements on this thread are routed properly
                                Logging.putPrefix(RpcClientFactory.LOG_PREFIX);
                            }
                        });
                        return null;
                    }, execTimeoutMs, TimeUnit.MILLISECONDS, true);
                } catch (Exception e) {
                    try (MDCCloseable mdc = Logging.withContextMapCloseable(clientContextMap)) {
                        // Wrap ProducerTemplate exceptions with a RequestRejectedException
                        future.completeExceptionally(new RequestRejectedException(e));
                        span.setTag(TAG_RPC_FAILED, "true");
                        span.log(e.getMessage());
                        rpcDuration.update(System.currentTimeMillis() - requestCreationTime);
                        span.finish();
                    }
                    // Ensure that future log statements on this thread are routed properly
                    Logging.putPrefix(RpcClientFactory.LOG_PREFIX);
                }
                final Meter requestSentMeter = getMetrics().meter(MetricRegistry.name(request.getLocation(), module.getId(), RPC_COUNT));
                requestSentMeter.mark();
                return future;
            }
        };
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public MetricRegistry getMetrics() {
        if(metrics == null) {
            metrics = new MetricRegistry();
        }
        return metrics;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void start() {
        executor = Executors.newCachedThreadPool(threadFactory);
        timeLimiter = new SimpleTimeLimiter(executor);

        tracerRegistry.init(SystemInfoUtils.getInstanceId());
        tracer = tracerRegistry.getTracer();
        // Initialize metrics reporter.
        metricsReporter = JmxReporter.forRegistry(getMetrics()).
                inDomain(JMX_DOMAIN_RPC).build();
        metricsReporter.start();
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
        if (metricsReporter != null) {
            metricsReporter.close();
        }
    }
}
