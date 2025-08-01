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
package org.opennms.netmgt.provision.persist.rpc;

import javax.annotation.PostConstruct;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.netmgt.provision.persist.LocationAwareRequisitionClient;
import org.opennms.netmgt.provision.persist.RequisitionProviderRegistry;
import org.opennms.netmgt.provision.persist.RequisitionRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareRequisitionClientImpl implements LocationAwareRequisitionClient {

    @Autowired
    private RequisitionProviderRegistry registry;

    @Autowired
    private RequisitionRpcModule requisitionRpcModule;

    @Autowired
    private RpcClientFactory rpcClientFactory;

    private RpcClient<RequisitionRequestDTO, RequisitionResponseDTO> delegate;

    @PostConstruct
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(requisitionRpcModule);
    }

    @Override
    public RequisitionRequestBuilder requisition() {
        return new RequisitionRequestBuilderImpl(this);
    }

    protected RpcClient<RequisitionRequestDTO, RequisitionResponseDTO> getDelegate() {
        return delegate;
    }

    public void setRegistry(RequisitionProviderRegistry registry) {
        this.registry = registry;
    }

    public RequisitionProviderRegistry getRegistry() {
        return registry;
    }
}
