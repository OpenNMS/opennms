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
package org.opennms.netmgt.icmp.proxy.strategy;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.netmgt.icmp.proxy.PingRequestDTO;
import org.opennms.netmgt.icmp.proxy.PingResponse;
import org.opennms.netmgt.icmp.proxy.PingResponseDTO;
import org.opennms.netmgt.icmp.proxy.PingSummary;

/**
 * Is used to perform one ping.
 *
 * @author mvrueden
 */
public class SinglePingExecutionStrategy implements ExecutionStrategy {
    private final RpcClient<PingRequestDTO, PingResponseDTO> client;

    public SinglePingExecutionStrategy(RpcClient<PingRequestDTO, PingResponseDTO> client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public CompletableFuture<PingSummary> execute(PingRequestDTO requestDTO) {
        return client.execute(requestDTO).thenApply(responseDTO -> {
            final PingResponse pingResponse = new PingResponse();
            pingResponse.setRtt(responseDTO.getRtt());

            PingSummary summary = new PingSummary(requestDTO.toPingRequest(), 1 /* we always only have 1 */);
            summary.addSequence(1, pingResponse);
            return summary;
        });
    }
}
