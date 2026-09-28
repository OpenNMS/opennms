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
package org.opennms.web.rest.v2.plugins;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.opennms.web.rest.v2.plugins.KarafBridge.KarafOperationException;

/**
 * Runs container operations one at a time with a deadline. Karaf's FeaturesService
 * serialises provisioning on a single thread, and a plugin whose bundle blocks in
 * its blueprint lifecycle blocks that thread for good; without a deadline every
 * later request would queue behind it forever. Once an operation has overrun, later
 * ones are refused until the stuck one finishes or the server restarts.
 */
class ContainerGate {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContainerGate.class);

    interface Operation {
        void run() throws Exception;
    }

    static final class Pending {
        final String label;
        final Instant since;
        final Future<?> future;

        Pending(final String label, final Instant since, final Future<?> future) {
            this.label = label;
            this.since = since;
            this.future = future;
        }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "plugin-management-container");
        t.setDaemon(true);
        return t;
    });
    private final ReentrantLock lock = new ReentrantLock();
    private final Clock clock;
    private volatile Pending stuck;

    ContainerGate(final Clock clock) {
        this.clock = clock;
    }

    /** Label and start time of an operation that overran its deadline and has not finished, or null. */
    String busy() {
        final Pending p = stuck;
        if (p == null) {
            return null;
        }
        if (p.future.isDone()) {
            stuck = null;
            return null;
        }
        return p.label + " (running since " + p.since + ")";
    }

    void run(final String label, final Duration timeout, final Operation operation) throws KarafOperationException {
        if (!lock.tryLock()) {
            throw new KarafOperationException("Another container operation is in progress; try again when it has finished.");
        }
        try {
            doRun(label, timeout, operation);
        } finally {
            lock.unlock();
        }
    }

    private void doRun(final String label, final Duration timeout, final Operation operation) throws KarafOperationException {
        final String busy = busy();
        if (busy != null) {
            throw new KarafOperationException("The container is still busy with " + busy + "; that operation did not finish within its time limit, so the container is not accepting further changes. Restart OpenNMS to recover.");
        }
        final Instant started = clock.instant();
        final Future<?> future = executor.submit(() -> {
            try {
                operation.run();
                if (stuckIs(label, started)) {
                    LOG.info("The {} that overran its {} second limit has finished", label, timeout.toSeconds());
                }
            } catch (final Exception e) {
                if (stuckIs(label, started)) {
                    LOG.warn("The {} that overran its {} second limit has failed: {}", label, timeout.toSeconds(), e.toString());
                }
                throw e;
            }
            return null;
        });
        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            stuck = new Pending(label, started, future);
            throw new KarafOperationException(label + " did not finish within " + timeout.toSeconds() + " seconds; the container's feature service is not responding. Check karaf.log and restart OpenNMS.");
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof KarafOperationException) {
                throw (KarafOperationException) cause;
            }
            throw new KarafOperationException(cause.getMessage() == null || cause.getMessage().isBlank() ? cause.toString() : cause.getMessage(), cause);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            if (!future.isDone()) {
                stuck = new Pending(label, started, future);
            }
            throw new KarafOperationException(label + " was interrupted");
        }
    }

    private boolean stuckIs(final String label, final Instant started) {
        final Pending p = stuck;
        return p != null && p.label.equals(label) && p.since.equals(started);
    }

    void shutdown() {
        executor.shutdownNow();
    }
}
