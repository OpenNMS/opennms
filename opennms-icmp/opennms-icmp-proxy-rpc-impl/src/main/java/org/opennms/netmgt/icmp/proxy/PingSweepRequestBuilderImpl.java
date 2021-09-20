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

package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.netmgt.icmp.PingConstants;

public class PingSweepRequestBuilderImpl implements PingSweepRequestBuilder {

    private final RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> client;
    private int packetSize = PingConstants.DEFAULT_PACKET_SIZE;
    private double packetsPerSecond = PingConstants.DEFAULT_PACKETS_PER_SECOND;
    private String location;
    private String systemId;
    private List<IPRangeDTO> ranges = new ArrayList<>();

    public PingSweepRequestBuilderImpl(RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public PingSweepRequestBuilder withPacketSize(int packetSize) {
        this.packetSize = (packetSize > 0 ? packetSize : this.packetSize);
        return this;
    }

    @Override
    public PingSweepRequestBuilder withPacketsPerSecond(double packetsPerSecond) {
        this.packetsPerSecond = packetsPerSecond;
        return this;
    }

    @Override
    public PingSweepRequestBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public PingSweepRequestBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public PingSweepRequestBuilder withRange(InetAddress begin, InetAddress end) {
        return withRange(begin, end, PingConstants.DEFAULT_RETRIES, PingConstants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public PingSweepRequestBuilder withRange(InetAddress begin, InetAddress end, int retries, long timeout, TimeUnit timeoutUnit) {
        this.ranges.add(new IPRangeDTO(begin, end, retries, TimeUnit.MILLISECONDS.convert(timeout, timeoutUnit)));
        return this;
    }

    @Override
    public CompletableFuture<PingSweepSummary> execute() {
        final PingSweepRequestDTO requestDTO = new PingSweepRequestDTO();
        requestDTO.setIpRanges(ranges);
        requestDTO.setLocation(location);
        requestDTO.setSystemId(systemId);
        requestDTO.setPacketSize(packetSize);
        requestDTO.setPacketsPerSecond(packetsPerSecond);

        return client.execute(requestDTO).thenApply(responseDTO -> {
            final PingSweepSummary summary = new PingSweepSummary();
            final Map<InetAddress, Double> responses = new LinkedHashMap<>();
            for (PingSweepResultDTO result : responseDTO.getPingSweepResult()) {
                responses.put(result.getAddress(), result.getRtt());
            }
            summary.setResponses(responses);
            return summary;
        });

    }

    public RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> getClient() {
        return client;
    }

}
