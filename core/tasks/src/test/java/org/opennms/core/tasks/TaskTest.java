/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.tasks;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BaseTaskTest
 *
 * @author brozow
 */
public class TaskTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskTest.class);
    
    ExecutorService m_executor;
    DefaultTaskCoordinator m_coordinator;
    
    @Before
    public void setUp() {
        m_executor = Executors.newFixedThreadPool(50,
            new LogPreservingThreadFactory(getClass().getSimpleName(), 50, false)
        );
        m_coordinator = new DefaultTaskCoordinator("TaskTest", m_executor);
    }
    
    @Test
    public void testSimpleTask() throws Exception {
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                sleep(100);
                hasRun.set(true);
            }
        };
        
        Task task = createTask(r);
        
        task.schedule();
        
        assertFalse(hasRun.get());
        
        task.waitFor();
        
        assertTrue(hasRun.get());
        
    }
    
    @Test
    public void testTaskWithSingleDependency() throws Exception {
        
        final List<String> sequence = new Vector<String>();
        
        Task task1 = createTask(appender(sequence, "task1"));
        Task task2 = createTask(appender(sequence, "task2"));
        Task task3 = createTask(appender(sequence, "task3"));

        task2.addPrerequisite(task1);
        task3.addPrerequisite(task2);

        task3.schedule();
        
        task2.schedule();
        
        task1.schedule();
        
        task3.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "task2", "task3" }, sequence.toArray(new String[0]));
        
    }

    private Task createTask(final Runnable runnable) {
        return m_coordinator.createTask(null, runnable);
    }
    
    @Test
    public void testTaskWithCompletedDependencies() throws Exception {
        
        
        final List<String> sequence = new Vector<String>();
        
        Task task1 = createTask(appender(sequence, "task1"));
        Task task2 = createTask(appender(sequence, "task2"));
        Task task3 = createTask(appender(sequence, "task3"));

        task1.schedule();
        
        task1.waitFor(10000, TimeUnit.MILLISECONDS);

        task2.addPrerequisite(task1);
        
        task2.schedule();

        task2.waitFor(10000, TimeUnit.MILLISECONDS);
        
        task3.addPrerequisite(task2);

        task3.schedule();
        
        
        task3.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "task2", "task3" }, sequence.toArray(new String[0]));
        
    }
    
    @Test(timeout=1000)
    public void testTaskThatThrowsException() throws Exception {
        
        AtomicInteger count = new AtomicInteger(0);
    
        Runnable thrower = new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("Intentionally failed for test purposes");

            }
        };
        
        Task throwerTask = m_coordinator.createTask(null, thrower);
        Task incrTask = m_coordinator.createTask(null, incr(count));
        
        incrTask.addPrerequisite(throwerTask);
        
        
        incrTask.schedule();
        throwerTask.schedule();
        
        
        incrTask.waitFor(1500, TimeUnit.MILLISECONDS);

        assertEquals(1, count.get());
    }
    
    @Test(timeout=1000)
    public void testAsyncThatThrowsException() throws Exception {
        
        AtomicInteger count = new AtomicInteger(0);
    
        Async<Integer> thrower = new Async<Integer>() {

            @Override
            public void submit(Callback<Integer> cb) {
                throw new RuntimeException("Intentionally failed for test purposes");
            }
            
        };
           
        
        Task throwerTask = m_coordinator.createTask(null, thrower, setter(count));
        Task incrTask = m_coordinator.createTask(null, incr(count));
        
        incrTask.addPrerequisite(throwerTask);
        
        
        incrTask.schedule();
        throwerTask.schedule();
        
        
        incrTask.waitFor(1500, TimeUnit.MILLISECONDS);

        assertEquals(1, count.get());
    }
    
    @Test(timeout=1000)
    public void testAsync() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        
        Task async = m_coordinator.createTask(null, timer(500, 17), setter(count));
        
        async.schedule();
        
        async.waitFor(15000, TimeUnit.MILLISECONDS);
        
        assertEquals(17, count.get());
    }
    
    @Test
    public void testBatchTask() throws Exception {
        
        AtomicInteger counter = new AtomicInteger(0);
        
        BatchTask batch = new BatchTask(m_coordinator, null);

        batch.add(incr(counter));
        batch.add(incr(counter));
        batch.add(incr(counter));

        batch.schedule();
        
        batch.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertEquals(3, counter.get());
        
    }
    
    @Test
    public void testSequenceTask() throws Exception {
        
        final List<String> sequence = new Vector<String>();
        
        SequenceTask seq = createSequence();

        seq.add(appender(sequence, "task1"));
        seq.add(appender(sequence, "task2"));
        seq.add(appender(sequence, "task3"));

        seq.schedule();
        
        seq.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "task2", "task3" }, sequence.toArray(new String[0]));
        
    }
    
    @Test
    public void testSequenceWithDependencies() throws Exception {
        
        
        List<String> sequence = new Vector<String>();
        
        Task task1 = createTask(appender(sequence, "task1"));
        Task task2 = createTask(appender(sequence, "task2"));

        SequenceTask seq = createSequence();

        seq.add(appender(sequence, "subtask1"));
        seq.add(appender(sequence, "subtask2"));
        seq.add(appender(sequence, "subtask3"));
        
        seq.addPrerequisite(task1);
        task2.addPrerequisite(seq);

        seq.schedule();

        task1.schedule();
        
        task2.schedule();
        
        task2.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "subtask1", "subtask2", "subtask3", "task2" }, sequence.toArray(new String[0]));
    }
    
    @Test
    public void testEnsureTaskIsSubmittedIfPreReqsCompleteWhileDependencyQueued() throws Exception {
    
        m_coordinator.setLoopDelay(1000);
        
        /**
         * This is a test case that tests a very specific race condition.  The loopDelay is used to
         * make the race condition work
         */

        // use latches so the finishing can be managed
        CountDownLatch aBlocker = new CountDownLatch(1);
        CountDownLatch bBlocker = new CountDownLatch(1);
        CountDownLatch cBlocker = new CountDownLatch(0); // we don't care when c finishes


        // create the tasks and a simple prerequisite and schedule
        Task a = createTask(waiter("A", aBlocker));
        Task b = createTask(waiter("B", bBlocker));
        Task c = createTask(waiter("C", cBlocker));

        c.addPrerequisite(a);
        
        b.schedule();
        a.schedule();
        c.schedule();

        // wait for the coordinator thread to process all of the above
        Thread.sleep(3500);
        
        /* we are not trying to set up the following situation
         * c has 1 'pendingPrereq' 
         * the coordinator threads Q has 'a.complete, b.complete, c.addPrereq(b)'
         *
         * In this situation then the completing tasks will not be able to submit
         * 'c' because it has a pending prerequisite.
         * 
         * By the time the prerequisite is added they are all complete. 
         * 
         * In this case we need to ensure the c is submitted
         */


        // Because of the loopDelay.. this following will all sit on the queue

        // call countDown will allow these to complete
        bBlocker.countDown();
        aBlocker.countDown();
        
        // we wait just to a litlte to make sure the two completes get added
        Thread.sleep(100);

        // not we add the prerequisite
        c.addPrerequisite(b);
        
        
        c.waitFor(10000, TimeUnit.MILLISECONDS);
        
        assertTrue("Task C never completed", c.isFinished());
        

        /*
         * If the queue call look this AFTER the call to c.addPrerequisite(b) increment pendingPrereqs
         * Q: a.complete, (pendingPrereq non zero) b.complete (pendingPrereq non zero) c.prereq(b) (decrementPrereqs)  ..... 
         */
        
    }
    
    @Test
    public void testLargeSequence() throws Exception {
        
        long count = 500;
        
        AtomicLong result = new AtomicLong(0);
        
        SequenceTask task = createSequence();
        
        for(long i = 1; i <= count; i++) {
            task.add(addr(result, i));
        }
        
        task.schedule();
        
        task.waitFor();
        
        assertEquals(count*(count+1)/2, result.get());
        
    }

    private SequenceTask createSequence() {
        return m_coordinator.createSequence().get();
    }
    
    @Test
    public void testLargeSequenceInProgress() throws Exception {
 
        long count = 10;
        long loops = 1000;
        long total=count*loops;
        
        AtomicLong result = new AtomicLong(0);
        
        SequenceTask task = createSequence();
        
        task.add(scheduler(task, result, 1, count, loops-1));
        
        task.schedule();

        task.waitFor();
        
        assertEquals(total*(total+1)/2, result.get());
        
    }
    
    public Runnable scheduler(final ContainerTask<?> container, final AtomicLong result, final long startIndex, final long count, final long remaining) {
        return new Runnable() {
            @Override
            public void run() {
                for(long i = startIndex; i < startIndex+count; i++) {
                    container.add(addr(result, i));
                }
                if (remaining != 0) {
                    container.add(scheduler(container, result, startIndex+count, count, remaining-1));
                }
            }
            @Override
            public String toString() {
                long batchNo = (startIndex - 1)/count + 1;
                long totalBatches = batchNo + remaining;
                return String.format("scheduleBatch %d of %d (batchSize = %d)", batchNo, totalBatches, count);
            }
        };
    }
    
    @Test
    public void testLargeBatch() throws Exception {
        
        long count = 500;
        
        AtomicLong result = new AtomicLong(0);
        
        BatchTask task = new BatchTask(m_coordinator, null);
        
        for(long i = 1; i <= count; i++) {
            task.add(addr(result, i));
        }
        
        task.schedule();
        
        task.waitFor();
        
        assertEquals(count*(count+1)/2, result.get());
        
    }
    
    @Test
    public void testLargeBatchInProgress() throws Exception {
 
        long count = 10;
        long loops = 1000;
        long total=count*loops;
        
        AtomicLong result = new AtomicLong(0);
        
        BatchTask task = new BatchTask(m_coordinator, null);
        
        task.add(scheduler(task, result, 1, count, loops-1));
        
        task.schedule();

        task.waitFor();
        
        assertEquals(total*(total+1)/2, result.get());
        
    }
    
    private <T> Runnable appender(final List<T> list, final T value) {
        return new Runnable() {
            @Override
            public void run() {
                list.add(value);
            }
            @Override
            public String toString() {
                return String.format("append(%s)", value);
            }
        };
    }
    
    private Runnable incr(final AtomicInteger counter) {
        return new Runnable() {
            @Override
            public void run() {
                //System.err.println("Incrementing!");
                counter.incrementAndGet();
            }
            @Override
            public String toString() {
                return "increment the counter: "+counter;
            }
        };
        
    }
    
    private Runnable addr(final AtomicLong accum, final long n) {
        return new Runnable() {
          @Override
          public void run() {
              int attempt = 0;
              while (true) {
                  attempt++;
                  long origVal = accum.get();
                  long newVal = origVal + n;
                  if (accum.compareAndSet(origVal, newVal)) {
                      //System.out.printf("%d: success %d: %d = %d + %d\n", n, attempt, newVal, n, origVal);
                      return;
                  } else {
                      System.out.printf("%d: FAILED %d: %d = %d + %d\n", n, attempt, newVal, n, origVal);
                  }
              }

          }
          @Override
          public String toString() {
              return String.format("add(%d)", n);
          }
          
        };
    }
    
    
    private Runnable waiter(final String name, final CountDownLatch latch) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                	LOG.debug("interrupted waiting for task", e);
                }
            }
            @Override
            public String toString() {
                return name;
            }
        };
    }
    
    private <T> Async<T> timer(final long millis, final T value) {
        final Timer timer = new Timer(true);
        return new Async<T>() {
            @Override
            public void submit(final Callback<T> cb) {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            //System.err.println("Running");
                            cb.complete(value);
                        } catch (Throwable t) {
                            cb.handleException(t);
                        }
                    }
                };
                //System.err.println("Scheduling");
                timer.schedule(timerTask, millis);
            }
        };
    }
    
    private Callback<Integer> setter(final AtomicInteger keeper) {
        return new Callback<Integer>() {

            @Override
            public void complete(Integer t) {
                keeper.set(t);
            }

            @Override
            public void handleException(Throwable t) {

            }
            
        };
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

}
