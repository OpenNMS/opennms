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
