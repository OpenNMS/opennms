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

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationAwareDnsLookupClientRpcImpl implements LocationAwareDnsLookupClient, InitializingBean {

    @Autowired
    private RpcClientFactory rpcClientFactory;

    @Autowired
    private DnsLookupClientRpcModule dnsLookupClientRpcModule;

    private RpcClient<DnsLookupRequestDTO, DnsLookupResponseDTO> delegate;

    @Override
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(dnsLookupClientRpcModule);
    }

    @Override
    public CompletableFuture<String> lookup(String hostName, String location) {
        return lookup(hostName, location, null);
    }

    @Override
    public CompletableFuture<String> lookup(String hostName, String location, String systemId) {
        return lookupExecute(hostName, location, systemId, QueryType.LOOKUP);
    }

    @Override
    public CompletableFuture<String> reverseLookup(InetAddress ipAddress, String location) {
        return reverseLookup(ipAddress, location, null);
    }

    @Override
    public CompletableFuture<String> reverseLookup(InetAddress ipAddress, String location, String systemId) {
        return lookupExecute(InetAddressUtils.toIpAddrString(ipAddress), location, systemId, QueryType.REVERSE_LOOKUP);
    }

    public RpcClient<DnsLookupRequestDTO, DnsLookupResponseDTO> getDelegate() {
        return delegate;
    }

    private CompletableFuture<String> lookupExecute(String request, String location, String systemId, QueryType queryType) {
        final DnsLookupRequestDTO dto = new DnsLookupRequestDTO();
        dto.setHostRequest(request);
        dto.setLocation(location);
        dto.setSystemId(systemId);
        dto.setQueryType(queryType);
        CompletableFuture<DnsLookupResponseDTO> future = getDelegate().execute(dto);
        return future.thenApply(DnsLookupResponseDTO::getHostResponse);
    }

}
