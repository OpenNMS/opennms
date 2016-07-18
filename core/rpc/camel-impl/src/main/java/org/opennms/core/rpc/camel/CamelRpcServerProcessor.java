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

/**
 * Executes the {@link RpcRequest}, and asynchronously returns the {@link RpcResponse}.
 *
 * @author jwhite
 */
public class CamelRpcServerProcessor implements AsyncProcessor {

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
                if (ex != null) {
                    exchange.setException(ex);
                    exchange.getOut().setFault(true);
                } else {
                    try {
                        exchange.getOut().setBody(module.marshalResponse(res), String.class);
                    }  catch (Throwable t) {
                        exchange.setException(t);
                        exchange.getOut().setFault(true);
                    }
                }
            } finally {
                callback.done(false);
            }
        });
        return false;
    }
}
