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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.Computations.AsyncSequenceBuilder;
import org.opennms.test.mock.MockLogAppender;


/**
 * TaskBuilderTest
 *
 * @author brozow
 */
public class AsyncComputationTest {
    
    // simple task building
    // create the actual 'tasks'
    // schedule and run the task
    // test for background processing
    
    ExecutorService m_executor;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_executor = new ThreadPoolExecutor(2, 2,
                                          0L, TimeUnit.MILLISECONDS,
                                          new LinkedBlockingQueue<Runnable>(100000));

    }
    
    @Test
    public void testAsync() throws Exception {

        Computation<Integer, String> toString = toStr();
        
        Async<Integer, String> async = Computations.async(m_executor, toString);

        async.start(7);
        
        assertEquals("7", async.waitFor());

    }
    
    @Test
    public void testRunOnce() throws Exception {
        
        AtomicInteger runCount = new AtomicInteger(0);
        
        AsyncRunnable async = Computations.async(m_executor, incr(runCount));
        
        async.start();
        
        async.waitFor();
        
        assertEquals(1, runCount.get());
        
    }
    
    @Test
    public void testSyncSequence() throws Exception {
        
        Computation<String, String> seq = 
            Computations.seq(toInt())
                .add(add(3))
                .add(add(5))
                .add(this.<Integer>toStr())
                .computation();
        
        assertEquals("8", seq.compute("0"));
        
    }
    
    @Test
    public void testAsyncComposition() throws Exception {
        Async<String, Integer> toInt = Computations.async(m_executor, toInt());
        Async<Integer, String> toStr = Computations.async(m_executor, this.<Integer>toStr());
        
        Async<String, String> toIntAndBack = Computations.composition(toInt, toStr);
        
        toIntAndBack.start("3");
        
        assertEquals("3", toIntAndBack.waitFor());
    }
    
    @Test
    public void testAsyncSequenceSimple() throws Exception {
        
        
    }
    
    @Test
    public void testAsyncSequenceLarge() throws Exception {
        
        final long count = 50000;

        AsyncSequenceBuilder<String, Integer> summer = Computations.asyncSeq(m_executor, toInt());
        
        for(int i = 1; i <= count; i++) {
            summer = summer.add(add(i));
        }
    
        Async<String, String> seq = summer.add(this.<Integer>toStr()).async();
        
        seq.start("0");
    
        assertEquals(String.valueOf(count*(count+1)/2), seq.waitFor());

    }
    
    @Test
    public void testAsyncSequenceInProgress() throws Exception {
        
        final long count = 1000000;
        final long batchSize = 1000;
        final long batches = count / batchSize;


        Async<String, Long> first = Computations.async(m_executor, toLong());
        
        
        AsyncSequenceBuilder<String, Long> summer = Computations.asyncSeq(m_executor, first);
        
        Async<AsyncSequenceBuilder<String, Long>, AsyncSequenceBuilder<String, Long>> firstBatch = Computations.async(m_executor, schedule(1, batchSize));
        AsyncSequenceBuilder<AsyncSequenceBuilder<String, Long>, AsyncSequenceBuilder<String, Long>> scheduler = Computations.asyncSeq(m_executor, firstBatch);
        for(long i = 1; i < batches; i++) {
            scheduler = scheduler.add(Computations.async(m_executor, schedule((batchSize*i)+1, batchSize)));
        }
        
        scheduler.start(summer);
        
        firstBatch.waitFor();
        

        summer.start("0");
        
        // so it can be garbage collected
        first = null;
        firstBatch = null;
        
        // we need to make sure all the summer tasks are added before
        // we ask for the seq task
        summer = scheduler.async().waitFor();
        
        Async<String, String> seq = summer.add(this.<Long>toStr()).async();

        assertEquals(String.valueOf(count*(count+1)/2), seq.waitFor());

    }
    
    @Test
    public void testAsyncSequenceInProgressSimple() throws Exception {
        
        final long count = 5000000;
        final long batchSize = 1000;
        final long batches = count / batchSize;

        AsyncSequenceBuilder<String, Long> summer = Computations.asyncSeq(m_executor, Computations.async(m_executor, toLong()));

        summer = schedule(1, batchSize, summer);
        
        summer.start("0");

        
        for(int i = 1; i < batches; i++) {
            //sleep(10);
            summer = schedule((batchSize*i)+1, batchSize, summer);
        }
        
        
        Async<String, String> seq = summer.add(this.<Long>toStr()).async();

        assertEquals(String.valueOf(count*(count+1)/2), seq.waitFor());

    }
    
    public Computation<AsyncSequenceBuilder<String, Long>, AsyncSequenceBuilder<String, Long>>
    schedule(final long start, final long count) {
        return new Computation<AsyncSequenceBuilder<String, Long>, AsyncSequenceBuilder<String, Long>>() {
            public AsyncSequenceBuilder<String, Long> compute(AsyncSequenceBuilder<String, Long> summer) throws Exception {
                return schedule(start, count, summer);
            }
            
        };
    }
    
    public AsyncSequenceBuilder<String, Long>
    schedule(long start, long count, AsyncSequenceBuilder<String, Long> summer) {
        for(long i = start; i < start+count; i++) {
            summer = summer.add(add(i));
        }
        return summer;
    }
    
    private Computation<String, Integer> toInt() {
        return new Computation<String, Integer>() {
            public Integer compute(String numStr) throws Exception {
                return Integer.decode(numStr);
            }
        };
    }

    private Computation<String, Long> toLong() {
        return new Computation<String, Long>() {
            public Long compute(String numStr) throws Exception {
                return Long.decode(numStr);
            }
        };
    }

    private <T> Computation<T, String> toStr() {
        return new Computation<T, String>() {
            public String compute(T a) throws Exception {
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
        try { Thread.sleep(millis); } catch(InterruptedException e) {}
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
                debugf("%d + %d = %d", a, n, a+n);
                return a + n;
            }
        };
    }
    public Computation<Long, Long> add(final long n) {
        final boolean print = n % 100000 == 0;
        if (print) {
            debugf("Creating add(%d)", n);
        }
        return new Computation<Long, Long>() {
            public Long compute(Long a) throws Exception {
                if (print) {
                    debugf("%d = %d + %d", a+n, a, n);
                }
                return a + n;
            }

            @Override
            protected void finalize() throws Throwable {
//                if (n % 1000000 == 1) {
//                    debugf("GCing add(%d)", n);
//                }
                long m = n + 1;
            }
            
            

        };
    }
    
    public void debugf(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }
    
}
