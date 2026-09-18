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
package org.opennms.netmgt.wsman.eventlog.rpc;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareWsManEventLogClientRpcImpl implements LocationAwareWsManEventLogClient, InitializingBean {

    @Autowired
    private RpcClientFactory rpcClientFactory;

    @Autowired
    private WsManEventLogRpcModule module;

    private RpcClient<EventLogRequestDTO, EventLogResponseDTO> delegate;

    public LocationAwareWsManEventLogClientRpcImpl() {
    }

    public LocationAwareWsManEventLogClientRpcImpl(RpcClientFactory rpcClientFactory, WsManEventLogRpcModule module) {
        this.rpcClientFactory = rpcClientFactory;
        this.module = module;
        afterPropertiesSet();
    }

    @Override
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(module);
    }

    @Override
    public CompletableFuture<EventLogResponseDTO> read(EventLogRequestDTO request) {
        return delegate.execute(request);
    }
}
