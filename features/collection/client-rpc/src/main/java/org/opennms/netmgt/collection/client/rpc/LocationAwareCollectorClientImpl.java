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
package org.opennms.netmgt.collection.client.rpc;

import java.util.Objects;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.rpc.utils.RpcTargetHelper;
import org.opennms.netmgt.collection.api.CollectorRequestBuilder;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareCollectorClientImpl implements LocationAwareCollectorClient, InitializingBean {

    @Autowired
    private CollectorClientRpcModule rpcModule;

    @Autowired
    private RpcClientFactory rpcClientFactory;

    @Autowired
    private RpcTargetHelper rpcTargetHelper;

    @Autowired
    private EntityScopeProvider entityScopeProvider;

    private RpcClient<CollectorRequestDTO, CollectorResponseDTO> delegate;

    public LocationAwareCollectorClientImpl() { }

    public LocationAwareCollectorClientImpl(RpcClientFactory rpcClientFactory) {
        this.rpcClientFactory = Objects.requireNonNull(rpcClientFactory);
        afterPropertiesSet();
    }

    @Override
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(rpcModule);
    }

    protected RpcClient<CollectorRequestDTO, CollectorResponseDTO> getDelegate() {
        return delegate;
    }

    @Override
    public CollectorRequestBuilder collect() {
        return new CollectorRequestBuilderImpl(this);
    }

    public void setRpcModule(CollectorClientRpcModule rpcModule) {
        this.rpcModule = rpcModule;
    }

    public ServiceCollectorRegistry getRegistry() {
        return rpcModule.getServiceCollectorRegistry();
    }

    public RpcTargetHelper getRpcTargetHelper() {
        return rpcTargetHelper;
    }

    public void setRpcTargetHelper(RpcTargetHelper rpcTargetHelper) {
        this.rpcTargetHelper = rpcTargetHelper;
    }

    public EntityScopeProvider getEntityScopeProvider() {
        return this.entityScopeProvider;
    }

    public void setEntityScopeProvider(final EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }
}
