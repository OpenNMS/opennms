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

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.WalkResponse;

public class SNMPWalkWithTrackerBuilder extends AbstractSNMPRequestBuilder<CollectionTracker> {

    private final CollectionTracker m_tracker;

    public SNMPWalkWithTrackerBuilder(LocationAwareSnmpClientRpcImpl client, SnmpAgentConfig agent, CollectionTracker tracker) {
        super(client, agent, Collections.emptyList(), buildWalkRequests(tracker));
        m_tracker = tracker;
    }

    private static List<SnmpWalkRequestDTO> buildWalkRequests(CollectionTracker tracker) {
        return tracker.getWalkRequests().stream()
                .map(req -> {
                    SnmpWalkRequestDTO walkRequest = new SnmpWalkRequestDTO();
                    walkRequest.setCorrelationId(req.getCorrelationId());
                    walkRequest.setOids(Collections.singletonList(req.getBaseOid()));
                    walkRequest.setMaxRepetitions(req.getMaxRepetitions());
                    walkRequest.setInstance(req.getInstance());
                    return walkRequest;
                }).collect(Collectors.toList());
    }


    @Override
    protected CollectionTracker processResponse(SnmpMultiResponseDTO response) {
        final List<WalkResponse> responses = response.getResponses().stream()
            .map(res -> new WalkResponse(res.getResults(), res.getCorrelationId()))
            .collect(Collectors.toList());
        m_tracker.handleWalkResponses(responses);
        return m_tracker;
    }
}
