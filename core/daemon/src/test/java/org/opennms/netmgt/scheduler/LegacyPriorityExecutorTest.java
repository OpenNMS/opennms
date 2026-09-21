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

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyPriorityExecutorTest {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyPriorityExecutorTest.class);

    @Before
    public void setUp() {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.scheduler", "DEBUG");
        MockLogAppender.setupLogging(p);
    }

    @Test
    public void testRun() {
        ExecutableTest discoveryTestA = new ExecutableTest("testA", 9);
        assertTrue(discoveryTestA.isReady());
        discoveryTestA.run();
    }

    @Test
    public void testPauseAndResume() throws InterruptedException {
        LegacyPriorityExecutor executor = new LegacyPriorityExecutor("CollectorGroupTest", 2, 5);
        executor.addPriorityReadyRunnable(new ExecutableTest("A",10) );
        executor.addPriorityReadyRunnable(new ExecutableTest("B",10) );
        executor.addPriorityReadyRunnable(new ExecutableTest("C",20) );
        executor.addPriorityReadyRunnable(new ExecutableTest("D",20) );
        executor.addPriorityReadyRunnable(new ExecutableTest("E",20) );
        executor.addPriorityReadyRunnable(new ExecutableTest("F",30) );
        executor.addPriorityReadyRunnable(new ExecutableTest("G",30) );
        executor.addPriorityReadyRunnable(new ExecutableTest("H",30) );
        executor.addPriorityReadyRunnable(new ExecutableTest("I",40) );
        executor.addPriorityReadyRunnable(new ExecutableTest("L",40) );

        executor.start();
        Thread.sleep(2);
        executor.pause();
        assertEquals(PausableFiber.PAUSE_PENDING, executor.getStatus());
        Thread.sleep(3000);
        assertEquals(PausableFiber.PAUSED, executor.getStatus());
        executor.resume();
        assertEquals(PausableFiber.RESUME_PENDING, executor.getStatus());
        Thread.sleep(200);
        assertEquals(PausableFiber.RUNNING, executor.getStatus());
        Thread.sleep(5000);

    }

    @Test
    public void testPriorityBlockingQueueOrder() {
        PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>();
        ArrayList<Integer> polledElements = new ArrayList<>();

        queue.add(1);
        queue.add(5);
        queue.add(2);
        queue.add(3);
        queue.add(4);

        queue.drainTo(polledElements);

        assertEquals(polledElements.get(0).intValue(),1);
        assertEquals(polledElements.get(1).intValue(),2);
        assertEquals(polledElements.get(2).intValue(),3);
        assertEquals(polledElements.get(3).intValue(),4);
        assertEquals(polledElements.get(4).intValue(),5);
    }

    @Test
    public void testPriorityBlockingQueueTake() throws InterruptedException {
        PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>();

        new Thread(() -> {
            LOG.info("Polling...");
            while(true) {
            try {
                LOG.info("Taking");
                Integer poll = queue.take();
                LOG.info("Taked: " + poll);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }            }
        }).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        LOG.info("Adding to queue");
        queue.add(1);
        queue.add(2);
        queue.add(3);
    }

    @Test
    public void testSchedulableExecutableGroup() {
        LegacyPriorityExecutor executor = new LegacyPriorityExecutor("CollectorGroupTest", 2, 5);
        SchedulableExecutableGroup group = new SchedulableExecutableGroup(60000,5000, executor, "testGroup");
        group.add(new ExecutableTest("A",30 ));
        group.add(new ExecutableTest("B",20 ));
        group.add(new ExecutableTest("C",10 ));
        Assert.assertEquals(3, group.getExecutables().size());

        group.getExecutables().forEach(System.err::println);

        ExecutableTest et = (ExecutableTest) group.getExecutables().iterator().next();
        LOG.info("Removing: {}", et);
        group.remove(et);
        Assert.assertEquals(2, group.getExecutables().size());

        ExecutableTest et1 = (ExecutableTest) group.getExecutables().iterator().next();
        LOG.info("Removing: {}", et1);
        group.remove(et1);
        Assert.assertEquals(1, group.getExecutables().size());

        Executable c = group.getExecutables().iterator().next();
        c.suspend();
        c.run();

        c.wakeUp();
        c.run();

    }

    @Test
    public void testDelayQueueWithZeroDelayExecutesImmediately() throws InterruptedException {
        DelayQueue<PriorityReadyRunnable> queue = new DelayQueue<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        ExecutableTest task = new ExecutableTest("immediate", 0);
        task.setDelayUntil(0);
        queue.add(task);
        
        long startTime = System.currentTimeMillis();
        PriorityReadyRunnable taken = queue.take();
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertTrue("Task with delay=0 should be taken immediately, but took " + elapsed + "ms", elapsed < 100);
        assertEquals("immediate", ((ExecutableTest) taken).getName());
    }

    @Test
    public void testDelayQueueBlocksUntilDelayExpires() throws InterruptedException {
        DelayQueue<PriorityReadyRunnable> queue = new DelayQueue<>();
        
        ExecutableTest task = new ExecutableTest("delayed", 0);
        task.setDelayUntil(System.currentTimeMillis() + 500);
        queue.add(task);
        
        long startTime = System.currentTimeMillis();
        PriorityReadyRunnable taken = queue.take();
        long elapsed = System.currentTimeMillis() - startTime;
        
        assertTrue("Task should wait ~500ms before being taken, but took " + elapsed + "ms", elapsed >= 450);
        assertTrue("Task should not wait much longer than 500ms, but took " + elapsed + "ms", elapsed < 700);
    }

    @Test
    public void testBackoffDelayIncreasesExponentially() {
        ExecutableTest task = new ExecutableTest("backoff", 0);
        long baseMs = 1000;
        long maxMs = 60000;
        
        task.setPriority(1);
        task.applyBackoffDelay(baseMs, maxMs);
        long delay1 = task.getDelayUntil() - System.currentTimeMillis();
        assertTrue("Priority 1 delay should be ~1000ms (got " + delay1 + ")", delay1 >= 800 && delay1 <= 1200);
        
        task.setPriority(2);
        task.applyBackoffDelay(baseMs, maxMs);
        long delay2 = task.getDelayUntil() - System.currentTimeMillis();
        assertTrue("Priority 2 delay should be ~2000ms (got " + delay2 + ")", delay2 >= 1600 && delay2 <= 2400);
        
        task.setPriority(3);
        task.applyBackoffDelay(baseMs, maxMs);
        long delay3 = task.getDelayUntil() - System.currentTimeMillis();
        assertTrue("Priority 3 delay should be ~4000ms (got " + delay3 + ")", delay3 >= 3200 && delay3 <= 4800);
        
        task.setPriority(10);
        task.applyBackoffDelay(baseMs, maxMs);
        long delay10 = task.getDelayUntil() - System.currentTimeMillis();
        assertTrue("Priority 10 delay should be capped at ~60000ms (got " + delay10 + ")", delay10 >= 48000 && delay10 <= 72000);
    }

    @Test
    public void testNotReadyTaskGetsDelayed() throws InterruptedException {
        AtomicInteger readyCallCount = new AtomicInteger(0);
        final int readyAfterCalls = 3;
        
        Executable rateLimitedTask = new Executable(0) {
            @Override
            public String getName() {
                return "rateLimited";
            }
            
            @Override
            public void runExecutable() {
                LOG.info("Executed rateLimited task");
            }
            
            @Override
            public boolean isReady() {
                return readyCallCount.incrementAndGet() >= readyAfterCalls;
            }
        };
        
        LegacyPriorityExecutor executor = new LegacyPriorityExecutor("DelayTest", 1, 5);
        CountDownLatch executed = new CountDownLatch(1);
        
        Executable wrappedTask = new Executable(0) {
            @Override
            public String getName() {
                return rateLimitedTask.getName();
            }
            
            @Override
            public void runExecutable() {
                rateLimitedTask.runExecutable();
                executed.countDown();
            }
            
            @Override
            public boolean isReady() {
                return rateLimitedTask.isReady();
            }
        };
        
        executor.addPriorityReadyRunnable(wrappedTask);
        executor.start();
        
        boolean completed = executed.await(10, TimeUnit.SECONDS);
        executor.stop();
        
        assertTrue("Task should have been executed after backoff delays", completed);
        assertTrue("Task should have been checked for readiness multiple times", readyCallCount.get() >= readyAfterCalls);
    }
}
