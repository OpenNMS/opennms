/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
