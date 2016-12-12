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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.mock.MockSinkModule;
import org.opennms.core.ipc.sink.test.ThreadLockingSyncDispatcher;

public class AsyncDispatcherTest {

    private SinkModule<MyMessage, MyMessage> module = new MockSinkModule<>();

    @Test
    public void testConcurrentAndQueuing() throws Exception {
        final int QUEUE_SIZE = 100;
        final int NUM_THREADS = 16;

        ThreadLockingDispatcherFactory threadLockingDispatcherFactory = new ThreadLockingDispatcherFactory();
        AsyncDispatcher<MyMessage> asyncDispatcher = threadLockingDispatcherFactory.createAsyncDispatcher(module, new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return QUEUE_SIZE;
            }

            @Override
            public int getNumThreads() {
                return NUM_THREADS;
            }
        });
        
        final AtomicBoolean allThreadsLocked = new AtomicBoolean(false);
        ThreadLockingSyncDispatcher<MyMessage> threadLockingSyncDispatcher = threadLockingDispatcherFactory.getThreadLockingSyncDispatcher();
        threadLockingSyncDispatcher.waitForThreads(NUM_THREADS).thenRun(() -> {
            allThreadsLocked.set(true);
        });

        for (int i = 0; i < NUM_THREADS; i++) {
            asyncDispatcher.send(new MyMessage());
        }

        // All of the dispatcher thread should be locked, and no additional thread should be waiting
        await().atMost(1, MINUTES).until(() -> allThreadsLocked.get());
        assertEquals(0, threadLockingSyncDispatcher.getNumExtraThreadsWaiting());

        // The queue should also be empty
        assertEquals(0, asyncDispatcher.getQueueSize());

        // Now fill up the queue
        for (int i = 0; i < 10 * QUEUE_SIZE; i++) {
            asyncDispatcher.send(new MyMessage());
        }
        assertEquals(QUEUE_SIZE, asyncDispatcher.getQueueSize());

        // No messages should have been dispatched yet
        assertEquals(0, threadLockingDispatcherFactory.getNumMessageDispatched());

        // Release the threads!
        threadLockingSyncDispatcher.release();
        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> asyncDispatcher.getQueueSize(), equalTo(0));
        await().atMost(1, MINUTES).until(() -> threadLockingDispatcherFactory.getNumMessageDispatched(),
                greaterThan(QUEUE_SIZE));
        asyncDispatcher.close();
    }

    private static class MyMessage implements Message {
        
    }

    public static class ThreadLockingDispatcherFactory extends AbstractMessageDispatcherFactory<Void> {
        private final AtomicInteger numMessageDispatched = new AtomicInteger(0);

        private final ThreadLockingSyncDispatcher<?> threadLockingSyncDispatcher = new ThreadLockingSyncDispatcher<MyMessage>() {
            @Override
            public void send(MyMessage message) {
                super.send(message);
                numMessageDispatched.incrementAndGet();
            }
        };

        @SuppressWarnings("unchecked")
        @Override
        public <S extends Message, T extends Message> SyncDispatcher<S> createSyncDispatcher(SinkModule<S, T> module) {
            return (ThreadLockingSyncDispatcher<S>)threadLockingSyncDispatcher;
        }

        @Override
        public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Void metadata, T message) {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public <S extends Message> ThreadLockingSyncDispatcher<S> getThreadLockingSyncDispatcher() {
            return (ThreadLockingSyncDispatcher<S>)threadLockingSyncDispatcher;
        }

        public int getNumMessageDispatched() {
            return numMessageDispatched.get();
        }
    }
}
