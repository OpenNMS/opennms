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
package org.opennms.core.ipc.grpc;

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
