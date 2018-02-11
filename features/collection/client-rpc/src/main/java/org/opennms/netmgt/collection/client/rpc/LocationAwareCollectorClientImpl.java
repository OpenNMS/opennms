/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.client.rpc;

import java.util.Objects;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
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
}
