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
package org.opennms.netmgt.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.test.MockLogAppender;

public class LegacySchedulerTest {

    private LegacyScheduler m_scheduler;

    @Before
    public void setUp() {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.scheduler", "DEBUG");
        MockLogAppender.setupLogging(p);
        m_scheduler = new LegacyScheduler("Test", 2);
    }

    @After
    public void tearDown() {
        if (m_scheduler.getStatus() == PausableFiber.START_PENDING) {
            // stop() refuses a fiber that was never started; release the pool directly
            m_scheduler.getRunner().shutdownNow();
        } else {
            m_scheduler.stop();
        }
    }

    @Test
    public void setThreadsGrowsAndShrinksThePool() {
        final ThreadPoolExecutor pool = (ThreadPoolExecutor) m_scheduler.getRunner();
        assertEquals(2, m_scheduler.getThreads());

        m_scheduler.setThreads(4);
        assertEquals(4, m_scheduler.getThreads());
        assertEquals(4, pool.getMaximumPoolSize());

        m_scheduler.setThreads(1);
        assertEquals(1, m_scheduler.getThreads());
        assertEquals(1, pool.getMaximumPoolSize());

        m_scheduler.setThreads(0);
        assertEquals(1, m_scheduler.getThreads());
    }

    @Test
    public void workersStartedAfterGrowingCarryTheNewPoolSizeInTheirName() throws InterruptedException {
        m_scheduler.setThreads(4);
        m_scheduler.start();

        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch running = new CountDownLatch(4);
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 4; i++) {
            m_scheduler.schedule(0, blockingTask(running, release, threadNames));
        }
        assertTrue("four tasks should run concurrently on the enlarged pool", running.await(10, TimeUnit.SECONDS));
        release.countDown();

        assertEquals(4, threadNames.size());
        assertTrue(threadNames.toString(), threadNames.stream().allMatch(n -> n.matches("Test-Thread-\\d+-of-4")));
    }

    @Test
    public void workersPrestartedWhileGrowingWithABacklogCarryTheNewPoolSize() throws InterruptedException {
        m_scheduler.start();

        // Occupy both original workers and queue two more tasks behind them
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch running = new CountDownLatch(4);
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 4; i++) {
            m_scheduler.schedule(0, blockingTask(running, release, threadNames));
        }
        final ThreadPoolExecutor pool = (ThreadPoolExecutor) m_scheduler.getRunner();
        for (int i = 0; i < 100 && pool.getQueue().size() < 2; i++) {
            Thread.sleep(50);
        }
        assertEquals("two tasks should be waiting behind the two busy workers", 2, pool.getQueue().size());

        // setCorePoolSize starts workers for the backlog at once; they must be named with the new size
        m_scheduler.setThreads(4);

        assertTrue("the backlog should now run on the prestarted workers", running.await(10, TimeUnit.SECONDS));
        release.countDown();

        assertEquals(4, threadNames.size());
        assertTrue(threadNames.toString(), threadNames.stream().filter(n -> n.endsWith("-of-4")).count() == 2);
        assertTrue(threadNames.toString(), threadNames.stream().noneMatch(n -> n.matches("Test-Thread-[34]-of-2")));
    }

    @Test
    public void queuedWorkStillRunsAfterShrinking() throws InterruptedException {
        m_scheduler.start();

        final CountDownLatch done = new CountDownLatch(6);
        for (int i = 0; i < 6; i++) {
            m_scheduler.schedule(0, countingTask(done));
        }
        m_scheduler.setThreads(1);

        assertTrue("all queued tasks should complete on the shrunk pool", done.await(10, TimeUnit.SECONDS));
        assertEquals(1, m_scheduler.getThreads());
    }

    private static ReadyRunnable countingTask(final CountDownLatch done) {
        return new ReadyRunnable() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void run() {
                done.countDown();
            }
        };
    }

    private static ReadyRunnable blockingTask(final CountDownLatch running, final CountDownLatch release, final Set<String> threadNames) {
        return new ReadyRunnable() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void run() {
                threadNames.add(Thread.currentThread().getName());
                running.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }
}
