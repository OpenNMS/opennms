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
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

public class DnsLookupClientRpcModule extends AbstractXmlRpcModule<DnsLookupRequestDTO, DnsLookupResponseDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(DnsLookupClientRpcModule.class);

    public static final String RPC_MODULE_ID = "DNS";

    public DnsLookupClientRpcModule() {
        super(DnsLookupRequestDTO.class, DnsLookupResponseDTO.class);
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
        final CompletableFuture<DnsLookupResponseDTO> future = new CompletableFuture<>();
        try {
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
            future.complete(dto);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

}
