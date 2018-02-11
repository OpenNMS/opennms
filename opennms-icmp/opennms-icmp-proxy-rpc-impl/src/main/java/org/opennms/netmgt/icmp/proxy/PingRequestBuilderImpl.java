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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.proxy.strategy.MultiplePingExecutionStrategy;
import org.opennms.netmgt.icmp.proxy.strategy.SinglePingExecutionStrategy;

import com.google.common.base.Preconditions;

public class PingRequestBuilderImpl implements PingRequestBuilder {

    private final RpcClient<PingRequestDTO, PingResponseDTO> client;
    private long timeout = PingConstants.DEFAULT_TIMEOUT;
    private int packetSize = PingConstants.DEFAULT_PACKET_SIZE;
    private int retries = PingConstants.DEFAULT_RETRIES;
    private int numberOfRequests = 1;
    private InetAddress inetAddress;
    private String location;
    private String systemId;
    private Callback callback;

    public PingRequestBuilderImpl(RpcClient<PingRequestDTO, PingResponseDTO> client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public PingRequestBuilder withTimeout(long timeout, TimeUnit unit) {
        Preconditions.checkArgument(timeout > 0, "timeout must be > 0");
        Objects.requireNonNull(unit);
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        return this;
    }

    @Override
    public PingRequestBuilder withPacketSize(int packetSize) {
        Preconditions.checkArgument(packetSize > 0, "packetSize must be > 0");
        this.packetSize = packetSize;
        return this;
    }

    @Override
    public PingRequestBuilder withRetries(int retries) {
        Preconditions.checkArgument(retries >= 0, "retries must be >= 0");
        this.retries = retries;
        return this;
    }

    @Override
    public PingRequestBuilder withInetAddress(InetAddress inetAddress) {
        this.inetAddress = Objects.requireNonNull(inetAddress);
        return this;
    }

    @Override
    public PingRequestBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public PingRequestBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public PingRequestBuilder withNumberOfRequests(int numberOfRequests) {
        Preconditions.checkArgument(numberOfRequests >= 1, "number of requests must be >= 1");
        this.numberOfRequests = numberOfRequests;
        return this;
    }

    @Override
    public PingRequestBuilder withProgressCallback(Callback callback) {
        this.callback = Objects.requireNonNull(callback);
        return this;
    }

    @Override
    public CompletableFuture<PingSummary> execute() {
        final PingRequestDTO requestDTO = new PingRequestDTO();
        requestDTO.setInetAddress(inetAddress);
        requestDTO.setPacketSize(packetSize);
        requestDTO.setTimeout(timeout);
        requestDTO.setRetries(retries);
        requestDTO.setLocation(location);
        requestDTO.setSystemId(systemId);

        if (numberOfRequests > 1) {
            return new MultiplePingExecutionStrategy(client, numberOfRequests, callback).execute(requestDTO);
        }
        return new SinglePingExecutionStrategy(client).execute(requestDTO);
    }

}
