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
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

public class DnsLookupClientRpcModule extends AbstractXmlRpcModule<DnsLookupRequestDTO, DnsLookupResponseDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(DnsLookupClientRpcModule.class);

    public static final String RPC_MODULE_ID = "DNS";

    private final ExecutorService executorService;

    public DnsLookupClientRpcModule(final int threadCount) {
        super(DnsLookupRequestDTO.class, DnsLookupResponseDTO.class);

        this.executorService = Executors.newFixedThreadPool(threadCount);

        LOG.debug("Configuring fixed-sized ThreadPool using threadCount={}", threadCount);
    }

    @Override
    public DnsLookupResponseDTO createResponseWithException(Throwable ex) {
        return new DnsLookupResponseDTO(ex);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public CompletableFuture<DnsLookupResponseDTO> execute(DnsLookupRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> {
                final InetAddress addr = InetAddressUtils.addr(request.getHostRequest());
                final DnsLookupResponseDTO dto = new DnsLookupResponseDTO();
                final QueryType queryType = request.getQueryType();
                if (queryType.equals(QueryType.LOOKUP)) {
                    dto.setHostResponse(addr.getHostAddress());
                } else if (queryType.equals(QueryType.REVERSE_LOOKUP)) {
                    // Attempt to retrieve the fully qualified domain name for this IP address
                    String hostName = addr.getCanonicalHostName();
                    if (InetAddressUtils.str(addr).equals(hostName)) {
                        // The given host name matches the textual representation of
                        // the IP address, which means that the reverse lookup failed
                        // NMS-9356: InetAddress#getCanonicalHostName requires PTR records
                        // to have a corresponding A record in order to succeed, so we
                        // try using dnsjava's implementation to work around this
                        try {
                            hostName = Address.getHostName(addr);
                        } catch (UnknownHostException e) {
                            LOG.warn("Failed to retrieve the fully qualified domain name for {}. "
                                    + "Using the textual representation of the IP address.", addr);
                        }
                    }
                    dto.setHostResponse(hostName);
                }
                return dto;
        }, this.executorService);
    }
}
