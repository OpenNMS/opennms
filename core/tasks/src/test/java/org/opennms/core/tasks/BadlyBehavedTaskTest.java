/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tests the exception handling inside {@link Task} and
 * {@link DefaultTaskCoordinator} by trying to perform operations
 * on a task that throws various exceptions.
 * 
 * @author Seth
 */
public class BadlyBehavedTaskTest {

    private static final Logger LOG = LoggerFactory.getLogger(BadlyBehavedTask.class);

    private DefaultTaskCoordinator m_coordinator;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        m_coordinator = new DefaultTaskCoordinator("Provisiond");
        m_coordinator.setDefaultExecutor("scan");

        Map<String, Executor> executors = new HashMap<>();

        executors.put("import", Executors.newScheduledThreadPool(10));
        executors.put("scan", Executors.newScheduledThreadPool(10));
        executors.put("write", Executors.newScheduledThreadPool(10));

        m_coordinator.setExecutors(executors);
    }

    public enum Failure {
        COMPLETESUBMIT,
        DOSUBMIT,
        POSTSCHEDULE,
        PRESCHEDULE,
        SCHEDULE
    }

    public static class TaskLatch {
        public final CountDownLatch doSubmitLatch;
        public final CountDownLatch preScheduleLatch;
        public final CountDownLatch postScheduleLatch;

        public TaskLatch(int doSubmit, int preschedule, int postSchedule) {
            doSubmitLatch = new CountDownLatch(doSubmit);
            preScheduleLatch = new CountDownLatch(preschedule);
            postScheduleLatch = new CountDownLatch(postSchedule);
        }
        
        public void waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            assertTrue("doSubmit latch not satisfied: " + doSubmitLatch.getCount(), doSubmitLatch.await(timeout, unit));
            assertTrue("preSchedule latch not satisfied: " + preScheduleLatch.getCount(), preScheduleLatch.await(timeout, unit));
            assertTrue("postSchedule latch not satisfied: " + postScheduleLatch.getCount(), postScheduleLatch.await(timeout, unit));
        }
    }

    public static class BadlyBehavedTask extends AbstractTask {

        private final String m_name;
        private final List<Failure> m_failures;
        private TaskLatch m_taskLatch;

        public BadlyBehavedTask(DefaultTaskCoordinator coordinator, String name) {
            this(coordinator, name, Failure.COMPLETESUBMIT, Failure.DOSUBMIT, Failure.POSTSCHEDULE, Failure.PRESCHEDULE, Failure.SCHEDULE);
        }

        public BadlyBehavedTask(DefaultTaskCoordinator coordinator, String name, Failure... failures) {
            super(coordinator, null);
            m_name = name;
            m_failures = Arrays.asList(failures);
        }

        public void setTaskLatch(TaskLatch taskLatch) {
            m_taskLatch = taskLatch;
        }

        // Don't override this because it is only used to submit task
        // completions which call onComplete() and clearDependents()
        /*
        @Override
        public void completeSubmit() {
            LOG.warn(m_name + " behaving badly! completeSubmit()");
            throw new IllegalStateException();
        }
        */

        /**
         * Behave like {@link ContainerTask}.
         */
        @Override
        public void completeSubmit() {
            getCoordinator().markTaskAsCompleted(this);
        }

        @Override
        public void doSubmit() {
            if (m_taskLatch != null) m_taskLatch.doSubmitLatch.countDown();
            if (m_failures.contains(Failure.DOSUBMIT)) {
                LOG.warn(m_name + " behaving badly! doSubmit()");
                throw new IllegalStateException();
            } else {
                super.doSubmit();
            }
        }

        /*
        @Override
        public void onComplete() {
            LOG.warn(m_name + " behaving badly! onComplete()");
            throw new IllegalStateException();
        }
        */

        @Override
        public void preSchedule() {
            if (m_taskLatch != null) m_taskLatch.preScheduleLatch.countDown();
            if (m_failures.contains(Failure.PRESCHEDULE)) {
                LOG.warn(m_name + " behaving badly! preSchedule()");
                throw new IllegalStateException();
            } else {
                super.preSchedule();
            }
        }

        @Override
        public void postSchedule() {
            if (m_taskLatch != null) m_taskLatch.postScheduleLatch.countDown();
            if (m_failures.contains(Failure.POSTSCHEDULE)) {
                LOG.warn(m_name + " behaving badly! postSchedule()");
                throw new IllegalStateException();
            } else {
                super.postSchedule();
            }
        }

        /*
        @Override
        public void schedule() {
            if (m_failures.contains(Failure.SCHEDULE)) {
                LOG.warn(m_name + " behaving badly! schedule()");
                throw new IllegalStateException();
            } else {
                super.schedule();
            }
        }
        */

        /*
        @Override
        public void submitIfReady() {
            LOG.warn(m_name + " behaving badly! submitIfReady()");
            throw new IllegalStateException();
        }
        */
    }

    @Test
    public void testBadlyBehavedTask() throws Exception {

        BadlyBehavedTask bad1 = new BadlyBehavedTask(m_coordinator, "Task 1", Failure.COMPLETESUBMIT, Failure.DOSUBMIT, Failure.POSTSCHEDULE, Failure.PRESCHEDULE);
        BadlyBehavedTask bad2 = new BadlyBehavedTask(m_coordinator, "Task 2", Failure.COMPLETESUBMIT, Failure.DOSUBMIT, Failure.POSTSCHEDULE, Failure.PRESCHEDULE);

        TaskLatch latch1 = new TaskLatch(1, 1, 1);
        bad1.setTaskLatch(latch1);
        TaskLatch latch2 = new TaskLatch(1, 1, 1);
        bad2.setTaskLatch(latch2);

        // Add the second task as a dependent of the first
        bad1.addDependent(bad2);

        // Schedule the first task
        bad2.schedule();

        assertFalse("Task 1 completed unexpectedly", bad1.waitFor(200, TimeUnit.MILLISECONDS));
        assertFalse("Task 2 completed unexpectedly", bad2.waitFor(200, TimeUnit.MILLISECONDS));

        bad1.schedule();

        assertTrue("Task 1 did not finish", bad1.waitFor(5, TimeUnit.SECONDS));
        LOG.info("Task 1 finished!");
        assertTrue("Task 2 did not finish", bad2.waitFor(5, TimeUnit.SECONDS));
        LOG.info("Task 2 finished!");

        latch1.waitFor(1, TimeUnit.SECONDS);
        latch2.waitFor(1, TimeUnit.SECONDS);
    }

    @Test
    public void testBadlyBehavedTaskThatSubmits() throws Exception {

        BadlyBehavedTask bad1 = new BadlyBehavedTask(m_coordinator, "Task 1", Failure.COMPLETESUBMIT, Failure.POSTSCHEDULE, Failure.PRESCHEDULE);
        BadlyBehavedTask bad2 = new BadlyBehavedTask(m_coordinator, "Task 2", Failure.COMPLETESUBMIT, Failure.POSTSCHEDULE, Failure.PRESCHEDULE);

        TaskLatch latch1 = new TaskLatch(1, 1, 1);
        bad1.setTaskLatch(latch1);
        TaskLatch latch2 = new TaskLatch(1, 1, 1);
        bad2.setTaskLatch(latch2);

        // Add the second task as a dependent of the first
        bad1.addDependent(bad2);

        // Schedule the first task
        bad2.schedule();

        assertFalse("Task 1 completed unexpectedly", bad1.waitFor(200, TimeUnit.MILLISECONDS));
        assertFalse("Task 2 completed unexpectedly", bad2.waitFor(200, TimeUnit.MILLISECONDS));

        bad1.schedule();

        assertTrue("Task 1 did not finish", bad1.waitFor(5000, TimeUnit.MILLISECONDS));
        LOG.info("Task 1 finished!");
        assertTrue("Task 2 did not finish", bad2.waitFor(5000, TimeUnit.MILLISECONDS));
        LOG.info("Task 2 finished!");

        latch1.waitFor(1, TimeUnit.SECONDS);
        latch2.waitFor(1, TimeUnit.SECONDS);
    }
}
