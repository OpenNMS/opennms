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

package org.opennms.core.ipc.sink.common;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;

@RunWith(MockitoJUnitRunner.class)
public class AsyncDispatcherTest {

    private static final int QUEUE_SIZE = 100;
    private static final int NUM_THREADS = 16;

    @Mock
    private SinkModule<MyMessage, MyMessage> module;

    private static class MyMessage implements Message { }

    private final ThreadLockingDispatcherFactory<MyMessage> threadLockingDispatcherFactory = new ThreadLockingDispatcherFactory<>();

    @Test(timeout=3*60*1000)
    public void testConcurrencyAndQueuing() throws Exception {
        when(module.getAsyncPolicy()).thenReturn(new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return QUEUE_SIZE;
            }

            @Override
            public int getNumThreads() {
                return NUM_THREADS;
            }

            @Override
            public boolean isBlockWhenFull() {
                return true;
            }
        });
        final AsyncDispatcher<MyMessage> asyncDispatcher = threadLockingDispatcherFactory.createAsyncDispatcher(module);

        final AtomicBoolean allThreadsLocked = new AtomicBoolean(false);
        ThreadLockingSyncDispatcher<MyMessage> threadLockingSyncDispatcher = threadLockingDispatcherFactory.getThreadLockingSyncDispatcher();
        threadLockingSyncDispatcher.waitForThreads(NUM_THREADS).thenRun(() -> {
            allThreadsLocked.set(true);
        });

        final List<CompletableFuture<MyMessage>> futures = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            futures.add(asyncDispatcher.send(new MyMessage()));
        }

        // All of the dispatcher thread should be locked, and no additional thread should be waiting
        await().atMost(1, MINUTES).until(() -> allThreadsLocked.get());
        assertEquals(0, threadLockingSyncDispatcher.getNumExtraThreadsWaiting());

        // The queue should also be empty
        assertEquals(0, asyncDispatcher.getQueueSize());

        // Now fill up the queue
        for (int i = 0; i < QUEUE_SIZE; i++) {
            futures.add(asyncDispatcher.send(new MyMessage()));
        }
        assertEquals(QUEUE_SIZE, asyncDispatcher.getQueueSize());

        // No messages should have been dispatched yet
        assertEquals(0, threadLockingDispatcherFactory.getNumMessageDispatched());

        // The queue is full, additional calls should block
        AtomicReference<CompletableFuture<MyMessage>> futureRef = new AtomicReference<>();
        CountDownLatch willSend = new CountDownLatch(1);
        CountDownLatch didSend = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                willSend.countDown();
                futureRef.set(asyncDispatcher.send(new MyMessage()));
                didSend.countDown();
            }
        });
        t.start();

        // Wait for the thread to start
        willSend.await();
        assertEquals(0, willSend.getCount());
        // We know our thread is started, let's make sure we didn't send yet
        Thread.sleep(500);
        assertEquals(1, didSend.getCount());

        // Release the threads!
        threadLockingSyncDispatcher.release();
        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> asyncDispatcher.getQueueSize(), equalTo(0));
        await().atMost(1, MINUTES).until(() -> threadLockingDispatcherFactory.getNumMessageDispatched(),
                greaterThan(QUEUE_SIZE));

        // All of our futures should be successfully resolved
        futures.add(futureRef.get());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{}));

        asyncDispatcher.close();
    }

    @Test(timeout=3*60*1000)
    public void testRejectedWhenFull() throws Exception {
        when(module.getAsyncPolicy()).thenReturn(new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return QUEUE_SIZE;
            }

            @Override
            public int getNumThreads() {
                return NUM_THREADS;
            }

            @Override
            public boolean isBlockWhenFull() {
                return false;
            }
        });
        final AsyncDispatcher<MyMessage> asyncDispatcher = threadLockingDispatcherFactory.createAsyncDispatcher(module);

        final AtomicBoolean allThreadsLocked = new AtomicBoolean(false);
        ThreadLockingSyncDispatcher<MyMessage> threadLockingSyncDispatcher = threadLockingDispatcherFactory.getThreadLockingSyncDispatcher();
        threadLockingSyncDispatcher.waitForThreads(NUM_THREADS).thenRun(() -> {
            allThreadsLocked.set(true);
        });

        final List<CompletableFuture<MyMessage>> futures = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS + QUEUE_SIZE; i++) {
            futures.add(asyncDispatcher.send(new MyMessage()));
        }

        // All of the dispatcher thread should be locked, and
        await().atMost(1, MINUTES).until(() -> allThreadsLocked.get());
        // No additional thread should be waiting
        assertEquals(0, threadLockingSyncDispatcher.getNumExtraThreadsWaiting());
        // The queue should be full
        assertEquals(QUEUE_SIZE, asyncDispatcher.getQueueSize());

        // The next dispatch should return a failed future
        CompletableFuture<MyMessage> future = asyncDispatcher.send(new MyMessage());
        assertTrue("future should have failed!", future.isCompletedExceptionally());

        // Release the threads!
        threadLockingSyncDispatcher.release();
        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> asyncDispatcher.getQueueSize(), equalTo(0));
        await().atMost(1, MINUTES).until(() -> threadLockingDispatcherFactory.getNumMessageDispatched(),
                greaterThan(QUEUE_SIZE));

        // All of our futures should be successfully resolved
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{}));

        asyncDispatcher.close();
    }
}
