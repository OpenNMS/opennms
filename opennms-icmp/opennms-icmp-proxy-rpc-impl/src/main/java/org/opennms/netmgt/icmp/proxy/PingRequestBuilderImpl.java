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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.utils.InetAddressUtils;
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
        requestDTO.addTracingInfo(RpcRequest.TAG_IP_ADDRESS, InetAddressUtils.toIpAddrString(inetAddress));


        if (numberOfRequests > 1) {
            return new MultiplePingExecutionStrategy(client, numberOfRequests, callback).execute(requestDTO);
        }
        return new SinglePingExecutionStrategy(client).execute(requestDTO);
    }

}
