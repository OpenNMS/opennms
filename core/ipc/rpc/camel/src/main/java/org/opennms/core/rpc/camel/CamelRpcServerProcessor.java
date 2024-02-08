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
package org.opennms.core.rpc.camel;

import static org.opennms.core.rpc.camel.CamelRpcConstants.JMS_TRACING_INFO;
import static org.opennms.core.tracing.api.TracerConstants.TAG_RPC_FAILED;
import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.tracing.api.TracerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;

/**
 * Executes the {@link RpcRequest}, and asynchronously returns the {@link RpcResponse}.
 *
 * @author jwhite
 */
public class CamelRpcServerProcessor implements AsyncProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcServerProcessor.class);

    private final RpcModule<RpcRequest,RpcResponse> module;

    private final TracerRegistry tracerRegistry;

    private Tracer tracer;

    public CamelRpcServerProcessor(RpcModule<RpcRequest,RpcResponse> module, TracerRegistry tracerRegistry) {
        this.module = Objects.requireNonNull(module);
        this.tracerRegistry = Objects.requireNonNull(tracerRegistry);
    }

    @Override
    public void process(Exchange exchange) {
        // Ensure that only async. calls are made.
        throw new UnsupportedOperationException("This processor must be invoked using the async interface.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean process(Exchange exchange, AsyncCallback callback) {
        // build span from message headers and retrieve custom tags into tracing info.
        Map<String, String> tracingInfo = new HashMap<>();
        Tracer.SpanBuilder spanBuilder = buildSpanFromHeaders(exchange.getIn(), tracingInfo);
        // Start minion span.
        Span minionSpan = spanBuilder.start();
        //Add custom tags to minion span.
        tracingInfo.forEach(minionSpan::setTag);
        final RpcRequest request = module.unmarshalRequest(exchange.getIn().getBody(String.class));
        minionSpan.setTag(TAG_LOCATION, request.getLocation());
        if(request.getSystemId() != null) {
            minionSpan.setTag(TAG_SYSTEM_ID, request.getSystemId());
        }
        final CompletableFuture<RpcResponse> future = module.execute(request);
        future.whenComplete((res, ex) -> {
            try {
                final RpcResponse response;
                if (ex != null) {
                    // An exception occurred, store the exception in a new response
                    LOG.warn("An error occured while executing a call in {}.", module.getId(), ex);
                    response = module.createResponseWithException(ex);
                    minionSpan.setTag(TAG_RPC_FAILED, "true");
                    minionSpan.log(ex.getMessage());
                } else {
                    // No exception occurred, use the given response
                    response = res;
                }
                // Received response, finish minion span.
                minionSpan.finish();
                try {
                    exchange.getOut().setBody(module.marshalResponse(response), String.class);
                    postProcess(exchange);
                }  catch (Throwable t) {
                    LOG.error("Marshalling a response in RPC module {} failed.", module, t);
                    exchange.setException(t);
                    exchange.getOut().setFault(true);
                }
            } finally {
                callback.done(false);
            }
        });
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[module=%s]", super.toString(), module.toString());
    }

    public void postProcess(Exchange exchange) {
        // pass
    }

    private Tracer.SpanBuilder buildSpanFromHeaders(Message message, Map<String, String> tracingInfo) {
        final Tracer tracer = tracerRegistry.getTracer();
        String tracingInfoObj = message.getHeader(JMS_TRACING_INFO, String.class);
        if(tracingInfoObj != null) {
            tracingInfo.putAll(TracingInfoCarrier.unmarshalTracinginfo(tracingInfoObj));
        }
        Tracer.SpanBuilder spanBuilder;
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(tracingInfo));
        if (context != null) {
            spanBuilder = tracer.buildSpan(module.getId()).asChildOf(context);
        } else {
            spanBuilder = tracer.buildSpan(module.getId());
        }
        return spanBuilder;
    }
}
