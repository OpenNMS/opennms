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
