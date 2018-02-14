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
