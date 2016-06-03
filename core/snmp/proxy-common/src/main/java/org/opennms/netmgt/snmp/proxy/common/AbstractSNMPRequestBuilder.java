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

    public static final Long DEFAULT_TIME_TO_LIVE = 60*1000l;

    private final DelegatingLocationAwareSnmpClientImpl client;
    private final SnmpAgentConfig agent;
    private List<SnmpGetRequestDTO> gets;
    private List<SnmpWalkRequestDTO> walks;
    private String location;
    private String description;
    private Long timeToLiveInMilliseconds = DEFAULT_TIME_TO_LIVE;

    public AbstractSNMPRequestBuilder(DelegatingLocationAwareSnmpClientImpl client,
            SnmpAgentConfig agent, List<SnmpGetRequestDTO> gets, List<SnmpWalkRequestDTO> walks) {
        this.client = Objects.requireNonNull(client);
        this.agent = Objects.requireNonNull(agent);
        this.gets = Objects.requireNonNull(gets);
        this.walks = Objects.requireNonNull(walks);
    }

    @Override
    public SNMPRequestBuilder<T> atLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public SNMPRequestBuilder<T> withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public SNMPRequestBuilder<T> withTimeToLive(long duration, TimeUnit unit) {
        timeToLiveInMilliseconds  = unit.toMillis(duration);
        return this;
    }

    @Override
    public CompletableFuture<T> execute() {
        final SnmpRequestDTO snmpRequestDTO = new SnmpRequestDTO();
        snmpRequestDTO.setLocation(location);
        snmpRequestDTO.setAgent(agent);
        snmpRequestDTO.setDescription(description);
        snmpRequestDTO.setGetRequests(gets);
        snmpRequestDTO.setWalkRequests(walks);
        snmpRequestDTO.setTimeToLive(timeToLiveInMilliseconds);
        return client.getSnmpRequestExecutor(location)
                .execute(snmpRequestDTO)
                // Different types of requests can process the responses differently
                .thenApply(res -> processResponse(res));
    }

    protected abstract T processResponse(SnmpMultiResponseDTO response);
}
