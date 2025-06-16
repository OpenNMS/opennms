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
package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;

import javax.annotation.PostConstruct;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "locationAwarePingClient")
public class LocationAwarePingClientImpl implements LocationAwarePingClient {

    @Autowired
    private RpcClientFactory rpcClientFactory;

    @Autowired
    private PingProxyRpcModule pingProxyRpcModule;

    @Autowired
    private PingSweepRpcModule pingSweepRpcModule;

    private RpcClient<PingRequestDTO, PingResponseDTO> pingProxyDelegate;

    private RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> pingSweepDelegate;

    @PostConstruct
    public void init() {
        pingProxyDelegate = rpcClientFactory.getClient(pingProxyRpcModule);
        pingSweepDelegate = rpcClientFactory.getClient(pingSweepRpcModule);
    }

    @Override
    public PingRequestBuilder ping(InetAddress inetAddress) {
        return new PingRequestBuilderImpl(pingProxyDelegate).withInetAddress(inetAddress);
    }

    @Override
    public PingSweepRequestBuilder sweep() {
        return new PingSweepRequestBuilderImpl(pingSweepDelegate);
    }
}
