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
package org.opennms.netmgt.shared.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Unit tests for {@link SpringContextTracker}'s bundle-lifecycle bookkeeping, in particular the
 * generation-token guard that prevents a context-creation task &mdash; left in flight while a bundle is
 * stopped and restarted &mdash; from overwriting the newer context and leaking the orphaned one.
 */
public class SpringContextTrackerTest {

    /** Executor that queues tasks and runs them only when the test asks, making the race deterministic. */
    private static final class ManualExecutor extends AbstractExecutorService {
        private final Deque<Runnable> queue = new ArrayDeque<>();
        @Override public void execute(Runnable command) { queue.add(command); }
        boolean runNext() {
            final Runnable r = queue.poll();
            if (r != null) { r.run(); return true; }
            return false;
        }
        @Override public void shutdown() { }
        @Override public List<Runnable> shutdownNow() { final List<Runnable> r = new ArrayList<>(queue); queue.clear(); return r; }
        @Override public boolean isShutdown() { return false; }
        @Override public boolean isTerminated() { return false; }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return true; }
    }

    /** Tracker that returns pre-built mock contexts from {@link #buildContext} instead of refreshing a real one. */
    private static final class TestableTracker extends SpringContextTracker {
        private final Deque<ConfigurableApplicationContext> toReturn;
        int buildCount = 0;
        // Makes the first N buildContext calls throw, simulating e.g. the database service not appearing
        // within the osgi:reference wait during boot.
        int failFirstN = 0;
        // Retries captured instead of scheduled, so tests control when (and whether) a retry runs.
        final Deque<Runnable> retries = new ArrayDeque<>();
        // One-shot hook run mid-build to deterministically simulate the bundle changing state while a task is
        // blocked in refresh() (e.g. stop+restart), exercising the install-time generation guard.
        Runnable onBuild;
        TestableTracker(BundleContext ctx, ManualExecutor exec, Deque<ConfigurableApplicationContext> toReturn) {
            super(ctx, mock(NamespaceProviderRegistry.class), exec);
            this.toReturn = toReturn;
        }
        @Override
        ConfigurableApplicationContext buildContext(Bundle bundle, List<String> configLocations) {
            buildCount++;
            if (buildCount <= failFirstN) {
                throw new IllegalStateException("simulated context-creation failure " + buildCount);
            }
            final ConfigurableApplicationContext ctx = toReturn.poll();
            if (onBuild != null) {
                final Runnable hook = onBuild;
                onBuild = null;
                hook.run();
            }
            return ctx;
        }
        @Override
        void scheduleRetry(Runnable retry, long delayMillis) {
            retries.add(retry);
        }
    }

    private static Bundle mockBundle(long id) {
        final Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(id);
        when(bundle.getSymbolicName()).thenReturn("test.bundle");
        final Hashtable<String, String> headers = new Hashtable<>();
        headers.put("Spring-Context", "ctx.xml");
        when(bundle.getHeaders()).thenReturn(headers);
        return bundle;
    }

    private static ConfigurableApplicationContext mockContext() {
        final ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
        when(ctx.getDisplayName()).thenReturn("mock-ctx");
        return ctx;
    }

    @Test
    public void stopThenRestartWhileBuildingDoesNotOverwriteNewerContext() {
        final ConfigurableApplicationContext ctx1 = mockContext();
        final ConfigurableApplicationContext ctx2 = mockContext();
        final ManualExecutor exec = new ManualExecutor();
        final Deque<ConfigurableApplicationContext> queue = new ArrayDeque<>();
        Collections.addAll(queue, ctx1, ctx2);

        final TestableTracker tracker = new TestableTracker(mock(BundleContext.class), exec, queue);
        final Bundle bundle = mockBundle(1L);

        // While task1 is "building" ctx1 (mid-refresh), the bundle stops and restarts: this invalidates
        // generation 1 and queues task2 (generation 2). task1 then tries to install its now-stale ctx1.
        tracker.onBuild = () -> {
            tracker.removedBundle(bundle, null, bundle);    // bundle stops -> generation 1 invalidated
            tracker.addingBundle(bundle, null);             // bundle restarts -> task2 queued (generation 2)
        };

        tracker.addingBundle(bundle, null);                 // task1 queued (generation 1)
        exec.runNext();   // task1 builds ctx1; mid-build the bundle stop/restarts; ctx1 is now stale -> close, don't install
        exec.runNext();   // task2 builds ctx2 and installs it

        verify(ctx1).close();            // the stale, orphaned context was torn down
        verify(ctx2, never()).close();   // the current context is installed and left running

        tracker.removedBundle(bundle, null, bundle);        // final stop closes the INSTALLED context...
        verify(ctx2).close();            // ...proving ctx2 (not ctx1) was the one installed
    }

    @Test
    public void stopBeforeTaskRunsSkipsContextCreationEntirely() {
        final ManualExecutor exec = new ManualExecutor();
        final TestableTracker tracker = new TestableTracker(mock(BundleContext.class), exec, new ArrayDeque<>());
        final Bundle bundle = mockBundle(1L);

        tracker.addingBundle(bundle, null);            // task queued (generation 1)
        tracker.removedBundle(bundle, null, bundle);   // bundle stops before the task runs
        exec.runNext();                                // task observes it is stale and returns early

        assertTrue("buildContext must not run for a task invalidated before it started", tracker.buildCount == 0);
    }

    @Test
    public void failedContextCreationIsRetriedUntilItSucceeds() {
        final ConfigurableApplicationContext ctx = mockContext();
        final Deque<ConfigurableApplicationContext> queue = new ArrayDeque<>();
        queue.add(ctx);
        final ManualExecutor exec = new ManualExecutor();
        final TestableTracker tracker = new TestableTracker(mock(BundleContext.class), exec, queue);
        tracker.failFirstN = 2;
        final Bundle bundle = mockBundle(1L);

        tracker.addingBundle(bundle, null);
        exec.runNext();                          // attempt 1 fails -> retry scheduled
        assertTrue("a retry must be scheduled after a failed creation", tracker.retries.size() == 1);
        tracker.retries.poll().run();            // attempt 2 fails again -> another retry
        assertTrue(tracker.retries.size() == 1);
        tracker.retries.poll().run();            // attempt 3 succeeds and installs

        assertTrue(tracker.retries.isEmpty());
        verify(ctx, never()).close();
        tracker.removedBundle(bundle, null, bundle);
        verify(ctx).close();                     // proves the retried context was the one installed
    }

    @Test
    public void retryIsAbandonedOnceTheBundleStops() {
        final ManualExecutor exec = new ManualExecutor();
        final TestableTracker tracker = new TestableTracker(mock(BundleContext.class), exec, new ArrayDeque<>());
        tracker.failFirstN = Integer.MAX_VALUE;
        final Bundle bundle = mockBundle(1L);

        tracker.addingBundle(bundle, null);
        exec.runNext();                                // attempt 1 fails -> retry scheduled
        tracker.removedBundle(bundle, null, bundle);   // bundle stops -> generation invalidated
        tracker.retries.poll().run();                  // stale retry observes the token and gives up

        assertTrue("no further retries after the bundle stopped", tracker.retries.isEmpty());
        assertTrue("stale retry must not attempt another build", tracker.buildCount == 1);
    }

    @Test
    public void happyPathInstallsAndThenClosesOnRemoval() {
        final ConfigurableApplicationContext ctx = mockContext();
        final Deque<ConfigurableApplicationContext> queue = new ArrayDeque<>();
        queue.add(ctx);
        final ManualExecutor exec = new ManualExecutor();
        final TestableTracker tracker = new TestableTracker(mock(BundleContext.class), exec, queue);
        final Bundle bundle = mockBundle(1L);

        tracker.addingBundle(bundle, null);
        exec.runNext();                                // builds and installs ctx
        verify(ctx, never()).close();

        tracker.removedBundle(bundle, null, bundle);   // installed context is closed on stop
        verify(ctx, times(1)).close();
    }
}
