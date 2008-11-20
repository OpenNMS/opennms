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
package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;


/**
 * TaskBuilderTest
 *
 * @author brozow
 */
public class TaskBuilderTest {
    
    // simple task building
    // create the actual 'tasks'
    // schedule and run the task
    // test for background processing
    
    ExecutorService m_executor;
    
    @Before
    public void setUp() {
        m_executor = null;
    }
    
    @Test
    public void testSimpleTask() throws Exception {

        AtomicBoolean hasRun = new AtomicBoolean(false);
        
        TaskBuilder<Boolean, Void> bldr = TaskBuilders.action(waitAndThen(1000, set(hasRun))); 
        
        Task<Boolean,Void> task = bldr.buildTask(m_executor);
        
        task.start(true);
        
        assertFalse(hasRun.get());

        task.waitFor();
        
        assertTrue(hasRun.get());
        
    }
    
    @Test
    public void testRunOnce() throws Exception {
        
        AtomicInteger runCount = new AtomicInteger(0);
        
        TaskBuilder<Void, Void> bldr = TaskBuilders.runnable(incr(runCount));
        
        Task<Void, Void> task = bldr.buildTask(m_executor);
        
        task.start();
        
        task.waitFor();
        
        assertEquals(1, runCount.get());
        
    }
    
    @Test
    public void testSequence() throws Exception {
        
        
        TaskBuilder<String, String> seq = TaskBuilders.seq(TaskBuilders.compute(toInt()))
          .seq(TaskBuilders.compute(add(3)))
          .seq(TaskBuilders.compute(add(5)))
          .seq(TaskBuilders.compute(toStr()))
          .builder();
        
        Task<String, String> task = seq.buildTask(m_executor);
        
        task.start("0");
        
        assertEquals("8", task.waitFor());
        
    }
    
    private Computation<String, Integer> toInt() {
        return new Computation<String, Integer>() {
            public Integer compute(String numStr) throws Exception {
                return Integer.decode(numStr);
            }
        };
    }

    private Computation<Integer, String> toStr() {
        return new Computation<Integer, String>() {
            public String compute(Integer a) throws Exception {
                return String.valueOf(a);
            }
        };
    }

    public <T> Action<T> waitAndThen(final int millis, final Action<T> action) {
        return new Action<T>() {
            public void action(T t) {
                sleep(millis);
                action.action(t);
            }
        };
    }
    
    public void sleep(long millis) {
        try { Thread.sleep(millis); } catch (Exception e) {}
    }
    
    public Action<Boolean> set(final AtomicBoolean b) {
        return new Action<Boolean>() {
            public void action(Boolean value) {
                b.set(value);
            }
        };
    }
    
    public Runnable incr(final AtomicInteger i) {
        return new Runnable() {
            public void run() {
                i.incrementAndGet();
            }
        };
    }
    
    public Computation<Integer, Integer> add(final int n) {
        return new Computation<Integer, Integer>() {
            public Integer compute(Integer a) throws Exception {
                return a + n;
            }
        };
    }

}
