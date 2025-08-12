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
        super(client, agent, Collections.emptyList(), buildWalkRequests(tracker), Collections.emptyList());
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
