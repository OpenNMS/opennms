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

import java.util.concurrent.CompletableFuture;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.Synchronization;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;

public class CamelRpcClientFactory implements RpcClientFactory {

    private String location;

    @EndpointInject(uri = "direct:executeRpc", context = "rpcClient")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:executeRpc", context = "rpcClient")
    private Endpoint endpoint;

    @Override
    public <S extends RpcRequest, T extends RpcResponse> RpcClient<S,T> getClient(RpcModule<S,T> module) {
        return new RpcClient<S,T>() {
            @Override
            public CompletableFuture<T> execute(S request) {
                if (request.getLocation() == null || request.getLocation().equals(location)) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }

                // Wrap the request in a CamelRpcRequest and forward it to the Camel route
                final CompletableFuture<T> future = new CompletableFuture<>();
                template.asyncCallbackSendBody(endpoint, new CamelRpcRequest<>(module, request), new Synchronization() {
                    @Override
                    public void onComplete(Exchange exchange) {
                        try {
                            future.complete(module.unmarshalResponse(exchange.getOut().getBody(String.class)));
                        } catch (Throwable ex) {
                            future.completeExceptionally(ex);
                        }
                    }
                    @Override
                    public void onFailure(Exchange exchange) {
                        future.completeExceptionally(exchange.getException());
                    }
                });
                return future;
            }
        };
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
