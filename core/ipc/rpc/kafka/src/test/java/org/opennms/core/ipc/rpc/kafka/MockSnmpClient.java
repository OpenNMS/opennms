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
package org.opennms.core.ipc.rpc.kafka;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.netmgt.snmp.proxy.common.SnmpMultiResponseDTO;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestDTO;

public class MockSnmpClient implements RpcClient<SnmpRequestDTO, SnmpMultiResponseDTO> {

    private final RpcClientFactory rpcProxy;

    private final RpcModule<SnmpRequestDTO, SnmpMultiResponseDTO> rpcModule;

    public MockSnmpClient(RpcClientFactory rpcProxy) {
        this(rpcProxy, new MockSnmpModule());
    }

    public MockSnmpClient(RpcClientFactory rpcProxy, RpcModule<SnmpRequestDTO, SnmpMultiResponseDTO> rpcModule) {
        this.rpcProxy = rpcProxy;
        this.rpcModule = rpcModule;
    }

    @Override
    public CompletableFuture<SnmpMultiResponseDTO> execute(SnmpRequestDTO request) {
        return rpcProxy.getClient(rpcModule).execute(request);
    }
}
