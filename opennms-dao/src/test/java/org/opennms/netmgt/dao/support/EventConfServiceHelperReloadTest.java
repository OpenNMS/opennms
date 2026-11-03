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
package org.opennms.netmgt.dao.support;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfGlobalSecurityDao;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The async reload coalesces bursts: a reload that is queued but has not started reading yet
 * already covers every mutation committed so far, so those requests must not queue another one.
 */
public class EventConfServiceHelperReloadTest {

    /** Runs nothing by itself: tasks queue up and are executed by the test, like a paused executor. */
    private static final class ManualExecutor extends AbstractExecutorService {
        private final Deque<Runnable> queued = new ArrayDeque<>();

        @Override
        public void execute(Runnable command) {
            queued.add(command);
        }

        void runAll() {
            Runnable task;
            while ((task = queued.poll()) != null) {
                task.run();
            }
        }

        int queuedCount() {
            return queued.size();
        }

        @Override
        public void shutdown() {
        }

        @Override
        public java.util.List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }

    private EventConfEventDao eventConfEventDao;
    private EventConfDao eventConfDao;
    private EventConfGlobalSecurityDao eventConfGlobalSecurityDao;
    private ManualExecutor executor;

    @Before
    public void setUp() {
        eventConfEventDao = mock(EventConfEventDao.class);
        eventConfDao = mock(EventConfDao.class);
        eventConfGlobalSecurityDao = mock(EventConfGlobalSecurityDao.class);
        when(eventConfEventDao.findEnabledEvents()).thenReturn(Collections.emptyList());
        when(eventConfGlobalSecurityDao.findAll()).thenReturn(Collections.emptyList());
        executor = new ManualExecutor();
        drainCoalescingFlag();
    }

    /** The coalescing flag is static; leave it cleared whatever a previous test did. */
    private void drainCoalescingFlag() {
        ManualExecutor drain = new ManualExecutor();
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, drain);
        drain.runAll();
        org.mockito.Mockito.clearInvocations(eventConfDao, eventConfEventDao, eventConfGlobalSecurityDao);
    }

    @Test
    public void burstOfRequestsCoalescesIntoOneReload() {
        for (int i = 0; i < 10; i++) {
            EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, executor);
        }
        assertEquals("only the first request may queue a task", 1, executor.queuedCount());

        executor.runAll();
        verify(eventConfDao, times(1)).loadEventsFromDB(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void mutationAfterTheReloadStartedQueuesAFreshOne() {
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, executor);
        executor.runAll(); // the queued reload ran, clearing the flag before it read

        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, executor);
        assertEquals("a later mutation must get its own reload", 1, executor.queuedCount());

        executor.runAll();
        verify(eventConfDao, times(2)).loadEventsFromDB(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void rejectedExecutionDoesNotLeaveTheFlagStuck() {
        ExecutorService rejecting = mock(ExecutorService.class);
        org.mockito.Mockito.doThrow(new RejectedExecutionException("shutting down"))
                .when(rejecting).execute(org.mockito.ArgumentMatchers.any());

        try {
            EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, rejecting);
            fail("the rejection must propagate");
        } catch (RejectedExecutionException expected) {
            // expected
        }

        // a healthy executor must still be able to queue a reload afterwards
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, executor);
        assertEquals(1, executor.queuedCount());
        executor.runAll();
    }
}
