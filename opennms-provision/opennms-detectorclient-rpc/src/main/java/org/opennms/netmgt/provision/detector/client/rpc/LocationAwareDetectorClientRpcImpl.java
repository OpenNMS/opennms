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
package org.opennms.netmgt.provision.detector.client.rpc;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.provision.DetectorRequestBuilder;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareDetectorClientRpcImpl implements LocationAwareDetectorClient, InitializingBean {

    @Autowired
    private ServiceDetectorRegistry registry;

    @Autowired
    private DetectorClientRpcModule detectorClientRpcModule;

    @Autowired
    private RpcClientFactory rpcClientFactory;

    @Autowired
    private EntityScopeProvider entityScopeProvider;

    private RpcClient<DetectorRequestDTO, DetectorResponseDTO> delegate;

    @Override
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(detectorClientRpcModule);
    }

    @Override
    public DetectorRequestBuilder detect() {
        return new DetectorRequestBuilderImpl(this);
    }

    protected RpcClient<DetectorRequestDTO, DetectorResponseDTO> getDelegate() {
        return delegate;
    }

    public void setRegistry(ServiceDetectorRegistry registry) {
        this.registry = registry;
    }

    public ServiceDetectorRegistry getRegistry() {
        return registry;
    }

    public EntityScopeProvider getEntityScopeProvider() {
        return this.entityScopeProvider;
    }

    public void setEntityScopeProvider(final EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }
}
