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
