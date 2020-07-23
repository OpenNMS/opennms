/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.rpc.kafka;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;

public class MockEchoClient implements RpcClient<EchoRequest, EchoResponse> {
    
    private final RpcClientFactory rpcProxy;

    private RpcModule<EchoRequest, EchoResponse> rpcModule;

    public MockEchoClient(RpcClientFactory rpcProxy) {
        this(rpcProxy, new EchoRpcModule());
    }

    public MockEchoClient(RpcClientFactory rpcProxy, RpcModule<EchoRequest, EchoResponse> rpcModule) {
        this.rpcProxy = rpcProxy;
        this.rpcModule = rpcModule;
    }

    @Override
    public CompletableFuture<EchoResponse> execute(EchoRequest request) {
        return getRpcProxy().getClient(rpcModule).execute(request);
    }

    public RpcClientFactory getRpcProxy() {
        return rpcProxy;
    }

}
