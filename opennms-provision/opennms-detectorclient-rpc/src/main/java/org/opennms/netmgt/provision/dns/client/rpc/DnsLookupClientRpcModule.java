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

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.core.utils.InetAddressUtils;

public class DnsLookupClientRpcModule extends AbstractXmlRpcModule<DnsLookupRequestDTO, DnsLookupResponseDTO> {

    public static final String RPC_MODULE_ID = "DNS";

    public DnsLookupClientRpcModule() {
        super(DnsLookupRequestDTO.class, DnsLookupResponseDTO.class);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public CompletableFuture<DnsLookupResponseDTO> execute(DnsLookupRequestDTO request) {
        String hostRequest = request.getHostRequest();
        QueryType queryType = request.getQueryType();
        DnsLookupResponseDTO dto = new DnsLookupResponseDTO();
        try {
            InetAddress addr = InetAddressUtils.addr(hostRequest);
            if (queryType.equals(QueryType.LOOKUP)) {
                dto.setHostResponse(addr.getHostAddress());
            } else if (queryType.equals(QueryType.REVERSE_LOOKUP)) {
                dto.setHostResponse(addr.getCanonicalHostName());
            }
        } catch (Exception e) {
            dto.setFailureMessage(e.getMessage());
        }
        CompletableFuture<DnsLookupResponseDTO> response = new CompletableFuture<DnsLookupResponseDTO>();
        response.complete(dto);
        return response;
    }

}
