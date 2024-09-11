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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.SNMPRequestBuilder;

public abstract class AbstractSNMPRequestBuilder<T> implements SNMPRequestBuilder<T> {

    private final LocationAwareSnmpClientRpcImpl client;
    private final SnmpAgentConfig agent;
    private List<SnmpGetRequestDTO> gets;
    private List<SnmpWalkRequestDTO> walks;
    private List<SnmpSetRequestDTO> sets;
    private String location;
    private String systemId;
    private String description;
    private Long timeToLiveInMilliseconds = null;

    public AbstractSNMPRequestBuilder(LocationAwareSnmpClientRpcImpl client,
            SnmpAgentConfig agent, List<SnmpGetRequestDTO> gets, List<SnmpWalkRequestDTO> walks, List<SnmpSetRequestDTO> sets) {
        this.client = Objects.requireNonNull(client);
        this.agent = Objects.requireNonNull(agent);
        this.gets = Objects.requireNonNull(gets);
        this.walks = Objects.requireNonNull(walks);
        this.sets = Objects.requireNonNull(sets);
    }

    @Override
    public SNMPRequestBuilder<T> withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public SNMPRequestBuilder<T> withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public SNMPRequestBuilder<T> withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public SNMPRequestBuilder<T> withTimeToLive(Long ttlInMs) {
        timeToLiveInMilliseconds = ttlInMs;
        return this;
    }

    @Override
    public SNMPRequestBuilder<T> withTimeToLive(long duration, TimeUnit unit) {
        timeToLiveInMilliseconds = unit.toMillis(duration);
        return this;
    }

    @Override
    public CompletableFuture<T> execute() {
        final SnmpRequestDTO snmpRequestDTO = new SnmpRequestDTO();
        snmpRequestDTO.setLocation(location);
        snmpRequestDTO.setSystemId(systemId);
        snmpRequestDTO.setAgent(agent);
        snmpRequestDTO.setDescription(description);
        snmpRequestDTO.setGetRequests(gets);
        snmpRequestDTO.setWalkRequests(walks);
        snmpRequestDTO.setSetRequests(sets);
        // TTL specified in agent configuration overwrites any previous ttls specified.
        if (agent.getTTL() != null) {
            timeToLiveInMilliseconds = agent.getTTL();
        }
        snmpRequestDTO.setTimeToLive(timeToLiveInMilliseconds);
        snmpRequestDTO.addTracingInfo(RpcRequest.TAG_IP_ADDRESS, InetAddressUtils.toIpAddrString(agent.getAddress()));
        if (description != null) {
            snmpRequestDTO.addTracingInfo(RpcRequest.TAG_DESCRIPTION, description);
        }
        return client.execute(snmpRequestDTO)
            // Different types of requests can process the responses differently
            .thenApply(this::processResponse);
    }

    protected abstract T processResponse(SnmpMultiResponseDTO response);
}
