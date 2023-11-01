/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

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
                LOG.info("Taked: {}", poll);
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
}
