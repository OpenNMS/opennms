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

package org.opennms.netmgt.snmp.proxy.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

public class SNMPMultiGetBuilder extends AbstractSNMPRequestBuilder<List<SnmpValue>> {

    public SNMPMultiGetBuilder(LocationAwareSnmpClientRpcImpl client, SnmpAgentConfig agent, List<SnmpObjId> oids) {
        super(client, agent, buildGetRequests(oids), Collections.emptyList());
    }

    private static List<SnmpGetRequestDTO> buildGetRequests(List<SnmpObjId> oids) {
        final SnmpGetRequestDTO getRequest = new SnmpGetRequestDTO();
        getRequest.setOids(oids);
        return Collections.singletonList(getRequest);
    }
    
    @Override
    protected List<SnmpValue> processResponse(SnmpMultiResponseDTO response) {
        return response.getResponses().stream()
                    .flatMap(res -> res.getResults().stream())
                    .map(SnmpResult::getValue)
                    .collect(Collectors.toList());
    }
}
