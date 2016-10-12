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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.netmgt.icmp.proxy.PingRequestBuilder;
import org.opennms.netmgt.icmp.proxy.PingRequestDTO;
import org.opennms.netmgt.icmp.proxy.PingResponseDTO;
import org.opennms.netmgt.icmp.proxy.PingSequence;
import org.opennms.netmgt.icmp.proxy.PingSummary;

public class MultiplePingExecutionStrategy implements ExecutionStrategy{

    private final PingRequestBuilder.Callback callback;
    private final RpcClient<PingRequestDTO, PingResponseDTO> client;
    private int numberOfRequests;

    public MultiplePingExecutionStrategy(RpcClient<PingRequestDTO, PingResponseDTO> client, int numberOfRequests, PingRequestBuilder.Callback callback) {
        this.client = Objects.requireNonNull(client);
        this.numberOfRequests = numberOfRequests;
        this.callback = Objects.requireNonNull(callback);
    }

    @Override
    public CompletableFuture<PingSummary> execute(PingRequestDTO requestDTO) {
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final PingSummary overallPingSummary = new PingSummary(requestDTO.toPingRequest(), numberOfRequests);

        final CompletableFuture<PingSummary> overallFuture = CompletableFuture.supplyAsync(() -> {
            for (int sequenceId = 1; sequenceId <= numberOfRequests; sequenceId++) {
                reportProgress(null, overallPingSummary);
                final CompletableFuture<PingSummary> singlePingResponse = new SinglePingExecutionStrategy(client).execute(requestDTO);
                try {
                    final PingSummary singleSummary = singlePingResponse.get(requestDTO.getTimeToLiveMs(), TimeUnit.MILLISECONDS);
                    final PingSequence newSequence = new PingSequence(sequenceId, singleSummary.getSequence(0).getResponse());
                    overallPingSummary.addSequence(newSequence);
                    reportProgress(newSequence, overallPingSummary);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    PingSequence newSequence = new PingSequence(sequenceId, e);
                    overallPingSummary.addSequence(newSequence);
                    reportProgress(newSequence, overallPingSummary);
                }

            }
            return overallPingSummary;
        }, executor);
        return overallFuture;
    }

    private void reportProgress(PingSequence newSequence, PingSummary summary) {
        if (callback != null) {
            callback.onUpdate(newSequence, summary);
        }
    }

}
