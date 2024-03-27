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
package org.opennms.netmgt.poller;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.poller.pollables.PollableServiceConfig;
import org.opennms.netmgt.scheduler.PostponeNecessary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncPollingEngine {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncPollingEngine.class);

    private static final Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("Poller-AsyncPollingEngine-%d")
            .build());

    private final int maxConcurrentCalls;
    private final Bulkhead bulkhead;

    public AsyncPollingEngine(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
        final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(maxConcurrentCalls)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        bulkhead = Bulkhead.of("asyncPollingEngine", bulkheadConfig);
    }

    /**
     * The scheduler has triggered a poll on the given service
     * this could be done at the normal interval, or a modified interval based on the
     * downtime model
     *
     * @param svc service
     */
    public void triggerScheduledPollOnService(PollableService svc) {
        // Rate-limit the # of polls
        try {
            bulkhead.acquirePermission();
        } catch (BulkheadFullException e) {
            LOG.info("Postponing poll for {}. Too many concurrent polls in progress (max=%d).", maxConcurrentCalls);
            throw new PostponeNecessary("BulkheadFullException postpone poll");
        }

        // Trigger the poll, without affecting any of the existing state
        final CompletionStage<PollStatus> future;
        try {
            future = svc.getPollConfig().asyncPoll();
        } catch (Throwable t) {
            bulkhead.releasePermission();
            throw new RuntimeException(String.format("Failed to trigger poll asynchronously for svc=%s", svc), t);
        }

        // When we're done, we want to process the result
        future.whenCompleteAsync((res,ex) -> {
            try {
                if (ex == null) {
                    processPollResult(svc, res);
                } else {
                    processPollResult(svc, PollableServiceConfig.errorToPollStatus(svc, ex));
                }
            } finally {
                bulkhead.releasePermission();
            }
        }, executor);
    }

    private void processPollResult(PollableService svc, PollStatus status) {
        // Trigger the run, and feed the result back instantly
        svc.doRunWithPreemptivePollStatus(status);
    }

    public long getNumPollsInFlight() {
        return bulkhead.getMetrics().getMaxAllowedConcurrentCalls() - bulkhead.getMetrics().getAvailableConcurrentCalls();
    }
}
