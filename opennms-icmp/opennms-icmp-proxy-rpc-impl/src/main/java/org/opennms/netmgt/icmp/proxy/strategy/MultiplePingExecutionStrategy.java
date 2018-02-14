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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.netmgt.icmp.proxy.PingRequestBuilder;
import org.opennms.netmgt.icmp.proxy.PingRequestDTO;
import org.opennms.netmgt.icmp.proxy.PingResponseDTO;
import org.opennms.netmgt.icmp.proxy.PingSequence;
import org.opennms.netmgt.icmp.proxy.PingSummary;

/**
 * The {@link MultiplePingExecutionStrategy} allows the execution of more than 1 pings with the same configuration.
 * As multiple pings need to be cancelled, the {@link CompletableFuture} is encapsulated with a single Thread.
 */
public class MultiplePingExecutionStrategy implements ExecutionStrategy{

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    /**
     * This {@link CompletableFuture} is actual cancelable.
     * This is achieved by holding a reference to the underlying future and the executing thead.
     * In addition the SingleThreadPool to handle the execution of this {@link CompletableFuture} is shutdown afterwards.
     */
    private static class CancelableCompletableFuture extends CompletableFuture {
        private Future<?> future;
        private ExecutorService executor;

        @Override
        public boolean complete(Object value) {
            try {
                return super.complete(value);
            } finally {
                executor.shutdown();
            }
        }

        @Override
        public boolean completeExceptionally(Throwable ex) {
            try {
                return super.completeExceptionally(ex);
            } finally {
                executor.shutdown();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                return future.cancel(mayInterruptIfRunning);
            } finally {
                super.cancel(mayInterruptIfRunning);
            }
        }

        public void startCancelableExecution(Callable<?> task) {
            this.executor = Executors.newSingleThreadExecutor(runnable -> new Thread(null, runnable, "icmp-proxy-pool-" + poolNumber.getAndIncrement()));
            this.future = executor.submit(task);
        }
    }

    /**
     * Wraps multiple {@link SinglePingExecutionStrategy}s and stores results in a {@link PingSummary}.
     */
    private class PingTask implements Callable<PingSummary> {

        private final PingRequestDTO requestDTO;
        private final CompletableFuture<PingSummary> completableFuture;

        private PingTask(PingRequestDTO requestDTO, CompletableFuture<PingSummary> completableFuture) {
            this.requestDTO = Objects.requireNonNull(requestDTO);
            this.completableFuture = Objects.requireNonNull(completableFuture);
        }

        @Override
        public PingSummary call() throws Exception {
            final PingSummary pingSummary = new PingSummary(requestDTO.toPingRequest(), numberOfRequests);
            for (int sequenceId = 1; sequenceId <= numberOfRequests; sequenceId++) {
                if (completableFuture.isCancelled()) {
                    return pingSummary; // terminate early
                }
                reportProgress(null, pingSummary);
                final CompletableFuture<PingSummary> singlePingFuture = new SinglePingExecutionStrategy(client).execute(requestDTO);
                try {
                    final PingSummary singlePingSummary = singlePingFuture.get(requestDTO.getTimeToLiveMs(), TimeUnit.MILLISECONDS);
                    final PingSequence newSequence = new PingSequence(sequenceId, singlePingSummary.getSequence(0).getResponse());
                    pingSummary.addSequence(newSequence);
                    reportProgress(newSequence, pingSummary);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    PingSequence newSequence = new PingSequence(sequenceId, e);
                    pingSummary.addSequence(newSequence);
                    reportProgress(newSequence, pingSummary);
                }
            }
            completableFuture.complete(pingSummary);
            return pingSummary;
        }

        private void reportProgress(PingSequence newSequence, PingSummary summary) {
            if (callback != null) {
                callback.onUpdate(newSequence, summary);
            }
        }
    }

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
        final CancelableCompletableFuture completableFuture = new CancelableCompletableFuture();
        completableFuture.startCancelableExecution(new PingTask(requestDTO, completableFuture));
        return completableFuture;
    }
}
