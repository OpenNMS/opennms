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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the {@link RpcRequest}, and asynchronously returns the {@link RpcResponse}.
 *
 * @author jwhite
 */
public class CamelRpcServerProcessor implements AsyncProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcServerProcessor.class);

    private final RpcModule<RpcRequest,RpcResponse> module;

    public CamelRpcServerProcessor(RpcModule<RpcRequest,RpcResponse> module) {
        this.module = Objects.requireNonNull(module);
    }

    @Override
    public void process(Exchange exchange) {
        // Ensure that only async. calls are made.
        throw new UnsupportedOperationException("This processor must be invoked using the async interface.");
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        final RpcRequest request = module.unmarshalRequest(exchange.getIn().getBody(String.class));
        final CompletableFuture<RpcResponse> future = module.execute(request);
        future.whenComplete((res, ex) -> {
            try {
                final RpcResponse response;
                if (ex != null) {
                    // An exception occurred, store the exception in a new response
                    LOG.warn("An error occured while executing a call in {}.", module.getId(), ex);
                    response = module.createResponseWithException(ex);
                } else {
                    // No exception occurred, use the given response
                    response = res;
                }

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
}
