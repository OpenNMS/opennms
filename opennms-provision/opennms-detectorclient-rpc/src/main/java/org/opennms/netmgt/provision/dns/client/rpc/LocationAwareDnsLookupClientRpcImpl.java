/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.dns.client.rpc;


import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareDnsLookupClientRpcImpl implements LocationAwareDnsLookupClient, InitializingBean {

    @Autowired
    private RpcClientFactory rpcClientFactory;

    @Autowired
    private DnsLookupClientRpcModule dnsLookupClientRpcModule;
    
    @Autowired
    private DnsReverseLookupClientRpcModule dnsReverseLookupClientRpcModule;

    private RpcClient<DnsLookupRequestDTO, DnsLookupResponseDTO> delegate;
    
    private RpcClient<DnsLookupRequestDTO, DnsLookupResponseDTO> delegateR;

    @Override
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(dnsLookupClientRpcModule);
        delegateR = rpcClientFactory.getClient(dnsReverseLookupClientRpcModule);
    }

    @Override
    public CompletableFuture<String> lookup(String hostName, String location) {
        final DnsLookupRequestDTO dto = new DnsLookupRequestDTO();
        dto.setHostRequest(hostName);
        dto.setLocation(location);
        CompletableFuture<DnsLookupResponseDTO> future = getDelegate().execute(dto);
        return future.thenApply(response -> {
            return response.getHostResponse();
        });

    }

    @Override
    public CompletableFuture<String> reverseLookup(String ipAddress, String location) {
        final DnsLookupRequestDTO dto = new DnsLookupRequestDTO();
        dto.setHostRequest(ipAddress);
        dto.setLocation(location);
        CompletableFuture<DnsLookupResponseDTO> future = getDelegateR().execute(dto);
        return future.thenApply(response -> {
            return response.getHostResponse();
        });
    }

    public RpcClient<DnsLookupRequestDTO, DnsLookupResponseDTO> getDelegate() {
        return delegate;
    }
    

    public RpcClient<DnsLookupRequestDTO, DnsLookupResponseDTO> getDelegateR() {
        return delegateR;
    }


}
