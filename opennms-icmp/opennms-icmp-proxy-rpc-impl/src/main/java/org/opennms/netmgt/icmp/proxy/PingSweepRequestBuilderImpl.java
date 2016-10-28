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

import static java.math.MathContext.DECIMAL64;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.collect.Maps;

public class PingSweepRequestBuilderImpl implements PingSweepRequestBuilder {

    protected final RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> client;
    protected long timeout = PingConstants.DEFAULT_TIMEOUT;
    protected int packetSize = PingConstants.DEFAULT_PACKET_SIZE;
    protected int retries = PingConstants.DEFAULT_RETRIES;
    protected String location;
    protected InetAddress begin;
    protected InetAddress end;
    protected String foreignSource;
    protected List<IPRangeDTO> ranges = new ArrayList<>();
    public static final BigDecimal FUDGE_FACTOR = BigDecimal.valueOf(1.5);

    public PingSweepRequestBuilderImpl(RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public PingSweepRequestBuilder withTimeout(long timeout, TimeUnit unit) {
        Objects.requireNonNull(unit);
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        return this;
    }

    @Override
    public PingSweepRequestBuilder withPacketSize(int packetSize) {
        this.packetSize = (packetSize > 0 ? packetSize : this.packetSize);
        return this;
    }

    @Override
    public PingSweepRequestBuilder withRetries(int retries) {
        this.retries = (retries > 0 ? retries : this.retries);
        return this;
    }

    @Override
    public PingSweepRequestBuilder withLocation(String location) {
        this.location = Objects.requireNonNull(location);
        return this;
    }

    @Override
    public CompletableFuture<PingSweepSummary> execute() {
        final PingSweepRequestDTO requestDTO = new PingSweepRequestDTO();
        requestDTO.setIpRanges(ranges);
        requestDTO.setLocation(location);
        requestDTO.setRetries(retries);
        requestDTO.setPacketSize(packetSize);
        requestDTO.setTimeout(timeout);
        requestDTO.setForeignSource(foreignSource);
        requestDTO.setTimeToLiveMs((long) calculateTaskTimeout());

        return client.execute(requestDTO).thenApply(responseDTO -> {
            PingSweepSummary summary = new PingSweepSummary();
            Map<InetAddress, Double> responses = Maps.newConcurrentMap();
            for (PingSweepResultDTO result : responseDTO.getPingSweepResult()) {
                responses.put(result.getAddress(), result.getRtt());
            }
            summary.setResponses(responses);
            return summary;
        });

    }

    @Override
    public PingSweepRequestBuilder withRange(InetAddress begin, InetAddress end) {
        this.ranges.add(new IPRangeDTO(begin, end));
        return this;
    }

    @Override
    public PingSweepRequestBuilder withIpPollRanges(List<IPPollRange> ranges) {
        ranges.forEach(range -> {
            try {
                InetAddress begin = InetAddress.getByAddress(range.getAddressRange().getBegin());
                InetAddress end = InetAddress.getByAddress(range.getAddressRange().getEnd());
                this.ranges.add(new IPRangeDTO(begin, end));
            } catch (Exception e) {
                throw new RuntimeException("Unknown ranges");
            }
        });
        return this;
    }

    public RpcClient<PingSweepRequestDTO, PingSweepResponseDTO> getClient() {
        return client;
    }

    @Override
    public PingSweepRequestBuilder withForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
        return this;
    }

    public int calculateTaskTimeout() {
        BigDecimal taskTimeOut = BigDecimal.ZERO;
        for (final IPRangeDTO range : ranges) {
            BigInteger size = InetAddressUtils.difference(InetAddressUtils.getInetAddress(range.getEnd().getAddress()),
                    InetAddressUtils.getInetAddress(range.getBegin().getAddress())).add(BigInteger.ONE);
            taskTimeOut = taskTimeOut.add(
                    // Take the number of retries
                    BigDecimal.valueOf(retries)
                            // Add 1 for the original request
                            .add(BigDecimal.ONE, DECIMAL64)
                            // Multiply by the number of addresses
                            .multiply(new BigDecimal(size), DECIMAL64)
                            // Multiply by the timeout per retry
                            .multiply(BigDecimal.valueOf(timeout), DECIMAL64)
                            // Multiply by the fudge factor
                            .multiply(FUDGE_FACTOR, DECIMAL64),
                    DECIMAL64);

            // Add a delay for the rate limiting done with the
            // m_packetsPerSecond field
            taskTimeOut = taskTimeOut.add(
                    // Take the number of addresses
                    new BigDecimal(size)
                            // Divide by the number of packets per second
                            .divide(BigDecimal.valueOf(packetSize), DECIMAL64)
                            // 1000 milliseconds
                            .multiply(BigDecimal.valueOf(1000), DECIMAL64),
                    DECIMAL64);
        }
        // If the timeout is greater than Integer.MAX_VALUE, just return
        // Integer.MAX_VALUE
        return taskTimeOut.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) >= 0 ? Integer.MAX_VALUE
                : taskTimeOut.intValue();
    }

}
