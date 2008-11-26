/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service.tasks;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;


/**
 * BaseTaskTest
 *
 * @author brozow
 */
public class BaseTaskTest {
    
    ExecutorService m_executor;
    DefaultTaskCoordinator m_coordinator;
    
    @Before
    public void setUp() {
        m_executor = Executors.newFixedThreadPool(50);
        m_coordinator = new DefaultTaskCoordinator(m_executor);
    }
    
    @Test
    public void testSimpleTask() throws Exception {
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        
        BaseTask task = new BaseTask(m_coordinator) {
            public void run() {
                sleep(100);
                hasRun.set(true);
            }
        };
        
        task.schedule();
        
        assertFalse(hasRun.get());
        
        task.waitFor();
        
        assertTrue(hasRun.get());
        
    }
    
    @Test
    public void testTaskWithSingleDependency() throws Exception {
        
        final List<String> sequence = new Vector<String>();
        
        BaseTask task1 = testTask(m_coordinator, "task1", sequence);
        BaseTask task2 = testTask(m_coordinator, "task2", sequence);
        BaseTask task3 = testTask(m_coordinator, "task3", sequence);

        task2.addDependency(task1);
        task3.addDependency(task2);

        task3.schedule();
        
        task2.schedule();
        
        task1.schedule();
        
        task3.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "task2", "task3" }, sequence.toArray(new String[0]));
        
    }
    
    @Test
    public void testLargeSequence() throws Exception {
        
        long count = 50000;
        
        AtomicLong result = new AtomicLong(0);
        
        SequenceTask task = new SequenceTask(m_coordinator);
        
        for(long i = 1; i <= count; i++) {
            task.add(add(result, i));
        }
        
        task.schedule();
        
        task.waitFor();
        
        assertEquals(count*(count+1)/2, result.get());
        
    }
    
    @Test
    public void testLargeBatch() throws Exception {
        
        long count = 50000;
        
        AtomicLong result = new AtomicLong(0);
        
        BatchTask task = new BatchTask(m_coordinator);
        
        for(long i = 1; i <= count; i++) {
            task.add(add(result, i));
        }
        
        task.schedule();
        
        task.waitFor();
        
        assertEquals(count*(count+1)/2, result.get());
        
    }
    
    public BaseTask add(final AtomicLong accum, final long n) {
        return new BaseTask(m_coordinator) {
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
          public String toString() {
              return String.format("add(%d)", n);
          }
          
        };
    }
    
    @Test
    public void testTaskWithCompletedDependencies() throws Exception {
        
        final List<String> sequence = new Vector<String>();
        
        BaseTask task1 = testTask(m_coordinator, "task1", sequence);
        BaseTask task2 = testTask(m_coordinator, "task2", sequence);
        BaseTask task3 = testTask(m_coordinator, "task3", sequence);

        task1.schedule();
        
        task1.waitFor();

        task2.addDependency(task1);
        
        task2.schedule();

        task2.waitFor();
        
        task3.addDependency(task2);

        task3.schedule();
        
        
        task3.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "task2", "task3" }, sequence.toArray(new String[0]));
        
    }
    
    @Test
    public void testBatchTask() throws Exception {
        
        AtomicInteger counter = new AtomicInteger(0);
        
        ContainerTask batch = new BatchTask(m_coordinator);

        batch.add(incr(m_coordinator, counter));
        batch.add(incr(m_coordinator, counter));
        batch.add(incr(m_coordinator, counter));

        batch.schedule();
        
        batch.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertEquals(3, counter.get());
        
    }
    
    @Test
    public void testSequenceTask() throws Exception {
        
        final List<String> sequence = new Vector<String>();
        
        SequenceTask seq = new SequenceTask(m_coordinator) {
          public void run() {
              System.out.println("Sequence Finished");
          }
          public String toString() {
              return "testSequenceTask Seq TAsk";
          }
        };

        seq.add(testTask(m_coordinator, "task1", sequence));
        seq.add(testTask(m_coordinator, "task2", sequence));
        seq.add(testTask(m_coordinator, "task3", sequence));

        seq.schedule();
        
        seq.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "task2", "task3" }, sequence.toArray(new String[0]));
        
    }
    
    @Test
    public void testSequenceWithDependencies() throws Exception {
        
        
        final List<String> sequence = new Vector<String>();
        
        BaseTask task1 = testTask(m_coordinator, "task1", sequence);
        BaseTask task2 = testTask(m_coordinator, "task2", sequence);

        SequenceTask seq = new SequenceTask(m_coordinator);

        seq.add(testTask(m_coordinator, "subtask1", sequence));
        seq.add(testTask(m_coordinator, "subtask2", sequence));
        seq.add(testTask(m_coordinator, "subtask3", sequence));
        
        seq.addDependency(task1);
        task2.addDependency(seq);

        seq.schedule();

        task1.schedule();
        
        task2.schedule();
        
        task2.waitFor(3500, TimeUnit.MILLISECONDS);
        
        assertArrayEquals(new String[] { "task1", "subtask1", "subtask2", "subtask3", "task2" }, sequence.toArray(new String[0]));
    }
    
    public BaseTask testTask(final DefaultTaskCoordinator coordinator, final String name, final List<String> sequence) {
        return new BaseTask(coordinator) {
            public void run() {
                sequence.add(name);
            }
            public String toString() { return name; };
        };
    }
    
    public BaseTask incr(DefaultTaskCoordinator coordinator, final AtomicInteger counter) {
        return new BaseTask(coordinator) {
            public void run() {
                counter.incrementAndGet();
            }
            public String toString() {
                return "increment the counter: "+counter;
            }
        };
    }
    
    // task with dependency
    
    // sequential task
    
    // batch task (parallelized subtasks)
    
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

}
