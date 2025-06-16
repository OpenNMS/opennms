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

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

public class SNMPMultiGetBuilder extends AbstractSNMPRequestBuilder<List<SnmpValue>> {

    public SNMPMultiGetBuilder(LocationAwareSnmpClientRpcImpl client, SnmpAgentConfig agent, List<SnmpObjId> oids) {
        super(client, agent, buildGetRequests(oids), Collections.emptyList(), Collections.emptyList());
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
