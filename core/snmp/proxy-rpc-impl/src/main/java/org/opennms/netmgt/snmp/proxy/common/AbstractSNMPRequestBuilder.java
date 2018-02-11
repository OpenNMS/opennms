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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.SNMPRequestBuilder;

public abstract class AbstractSNMPRequestBuilder<T> implements SNMPRequestBuilder<T> {

    private final LocationAwareSnmpClientRpcImpl client;
    private final SnmpAgentConfig agent;
    private List<SnmpGetRequestDTO> gets;
    private List<SnmpWalkRequestDTO> walks;
    private String location;
    private String systemId;
    private String description;
    private Long timeToLiveInMilliseconds = null;

    public AbstractSNMPRequestBuilder(LocationAwareSnmpClientRpcImpl client,
            SnmpAgentConfig agent, List<SnmpGetRequestDTO> gets, List<SnmpWalkRequestDTO> walks) {
        this.client = Objects.requireNonNull(client);
        this.agent = Objects.requireNonNull(agent);
        this.gets = Objects.requireNonNull(gets);
        this.walks = Objects.requireNonNull(walks);
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
        snmpRequestDTO.setTimeToLive(timeToLiveInMilliseconds);
        return client.execute(snmpRequestDTO)
            // Different types of requests can process the responses differently
            .thenApply(this::processResponse);
    }

    protected abstract T processResponse(SnmpMultiResponseDTO response);
}
