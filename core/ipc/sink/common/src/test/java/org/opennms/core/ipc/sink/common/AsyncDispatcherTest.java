/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.DispatchQueueFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.offheap.DispatchQueueServiceLoader;
import org.opennms.core.ipc.sink.offheap.QueueFileOffHeapDispatchQueueFactory;

import com.jayway.awaitility.core.ConditionTimeoutException;

@RunWith(MockitoJUnitRunner.class)
public class AsyncDispatcherTest {

    private static final int QUEUE_SIZE = 100;
    private static final int NUM_THREADS = 16;

    @Mock
    private SinkModule<MyMessage, MyMessage> module;

    private static class MyMessage implements Message {
        private final String value;

        public MyMessage(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyMessage myMessage = (MyMessage) o;
            return Objects.equals(value, myMessage.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "MyMessage{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    private final BlockableDispatcherFactory<MyMessage> blockableDispatcherFactory = new BlockableDispatcherFactory<>();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() {
        when(module.getId()).thenReturn("myModule");
        when(module.marshalSingleMessage(any(MyMessage.class))).thenAnswer((Answer<byte[]>) invocationOnMock ->
                ((MyMessage) invocationOnMock.getArguments()[0]).getValue().getBytes());
        when(module.unmarshalSingleMessage(any())).thenAnswer((Answer<MyMessage>) invocationOnMock ->
                new MyMessage(new String((byte[]) invocationOnMock.getArguments()[0])));
    }

    @Test(timeout=3*60*1000)
    public void testRejectedWhenFull() throws Exception {
        // Set up the dispatch queue
        DispatchQueueFactory dispatchQueueFactory = new QueueFileOffHeapDispatchQueueFactory(NUM_THREADS, NUM_THREADS, null,
                folder.newFolder().toPath().toString());
        DispatchQueueServiceLoader.setDispatchQueue(dispatchQueueFactory);
        
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

        final AsyncDispatcher<MyMessage> asyncDispatcher = blockableDispatcherFactory.createAsyncDispatcher(module);
        BlockableSyncDispatcher<MyMessage> blockableSyncDispatcher = blockableDispatcherFactory.getBlockableSyncDispatcher();
        final List<CompletableFuture<AsyncDispatcher.DispatchStatus>> futures = new ArrayList<>();
        
        // Get all the dispatch threads parked and fill up the queue
        // Since we might get some rejects if we send too many message too quickly, we will break the sending up into
        // two chunks separated by checking to make sure the queue has capacity again
        blockableSyncDispatcher.block();
        for (int i = 0; i < NUM_THREADS; i++) {
            futures.add(asyncDispatcher.send(new MyMessage(Integer.toString(i))));
        }

        // Wait for the initial messages to be accepted by the thread pool
        await().atMost(1, MINUTES).until(asyncDispatcher::getQueueSize, equalTo(0));
        
        for (int i = NUM_THREADS; i < NUM_THREADS * 2; i++) {
            futures.add(asyncDispatcher.send(new MyMessage(Integer.toString(i))));
        }
        
        // The queue should be full
        assertEquals(NUM_THREADS, asyncDispatcher.getQueueSize());
        // All the dispatch threads should be blocked
        await().atMost(1, MINUTES).until(blockableSyncDispatcher::getBlockedThreadCount, equalTo(NUM_THREADS));

        // The next dispatch should return a failed future
        CompletableFuture<AsyncDispatcher.DispatchStatus> future = asyncDispatcher.send(
                new MyMessage(Integer.toString(NUM_THREADS * 2)));
        assertTrue("future should have failed!", future.isCompletedExceptionally());

        // Release the threads!
        blockableSyncDispatcher.unblock();

        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> asyncDispatcher.getQueueSize(), equalTo(0));
        await().atMost(1, MINUTES).until(() -> blockableSyncDispatcher.getNumMessageDispatched(),
                equalTo(NUM_THREADS * 2));

        // All of our futures should be successfully resolved
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{}));
        
        // All but one of the messages (the rejected one) should have been dispatched
        List<MyMessage> actuallyDispatched = blockableSyncDispatcher.getDispatchedMessages();
        assertThat(actuallyDispatched, hasSize(NUM_THREADS * 2));
        for (int i = 0; i < NUM_THREADS * 2; i++) {
            assertThat(actuallyDispatched, hasItem(new MyMessage(Integer.toString(i))));
        }

        asyncDispatcher.close();
    }

    @Test(timeout=3*60*1000)
    public void testBlockedWhenFull() throws Exception {
        // Set up the dispatch queue
        DispatchQueueFactory dispatchQueueFactory = new QueueFileOffHeapDispatchQueueFactory(NUM_THREADS, NUM_THREADS, null,
                folder.newFolder().toPath().toString());
        DispatchQueueServiceLoader.setDispatchQueue(dispatchQueueFactory);

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

        final AsyncDispatcher<MyMessage> asyncDispatcher = blockableDispatcherFactory.createAsyncDispatcher(module);
        BlockableSyncDispatcher<MyMessage> blockableSyncDispatcher = blockableDispatcherFactory.getBlockableSyncDispatcher();
        final List<CompletableFuture<AsyncDispatcher.DispatchStatus>> futures = new ArrayList<>();

        // Get all the dispatch threads blocked and fill up the queue
        blockableSyncDispatcher.block();
        for (int i = 0; i < NUM_THREADS * 2; i++) {
            futures.add(asyncDispatcher.send(new MyMessage(Integer.toString(i))));
        }

        // The queue should be full
        assertEquals(NUM_THREADS, asyncDispatcher.getQueueSize());
        // All the dispatch threads should be blocked
        await().atMost(1, MINUTES).until(blockableSyncDispatcher::getBlockedThreadCount, equalTo(NUM_THREADS));

        // This send should block right now
        AtomicBoolean didSend = new AtomicBoolean(false);
        CompletableFuture.runAsync(() -> {
            futures.add(asyncDispatcher.send(new MyMessage(Integer.toString(NUM_THREADS * 2))));
            didSend.set(true);
        });

        try {
            await().pollDelay(10, MILLISECONDS)
                    .atMost(100, MILLISECONDS)
                    .until(didSend::get);
            fail("Should not have sent yet");
        } catch (ConditionTimeoutException expected) {
        }

        // Release the threads!
        blockableSyncDispatcher.unblock();

        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> asyncDispatcher.getQueueSize(), equalTo(0));
        // We should see all the originally queued messages plus the extra that was blocked
        await().atMost(1, MINUTES).until(() -> blockableSyncDispatcher.getNumMessageDispatched(),
                equalTo((NUM_THREADS * 2) + 1));

        // All of our futures should be successfully resolved
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{}));

        // All of the messages should have been dispatched
        List<MyMessage> actuallyDispatched = blockableSyncDispatcher.getDispatchedMessages();
        assertThat(actuallyDispatched, hasSize((NUM_THREADS * 2) + 1));
        for (int i = 0; i < (NUM_THREADS * 2) + 1; i++) {
            assertThat(actuallyDispatched, hasItem(new MyMessage(Integer.toString(i))));
        }
        
        asyncDispatcher.close();
    }

    @Test(timeout=3*60*1000)
    public void testOverflowOffHeap() throws Exception {
        int inMemorySize = NUM_THREADS;
        int batchSize = inMemorySize / 2;
        // Set up the dispatch queue
        DispatchQueueFactory dispatchQueueFactory = new QueueFileOffHeapDispatchQueueFactory(inMemorySize, batchSize,"100KB",
                folder.newFolder().toPath().toString());
        DispatchQueueServiceLoader.setDispatchQueue(dispatchQueueFactory);

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

        final AsyncDispatcher<MyMessage> asyncDispatcher = blockableDispatcherFactory.createAsyncDispatcher(module);
        BlockableSyncDispatcher<MyMessage> blockableSyncDispatcher =
                blockableDispatcherFactory.getBlockableSyncDispatcher();
        final List<CompletableFuture<AsyncDispatcher.DispatchStatus>> futures = new ArrayList<>();

        // Get all the dispatch threads blocked and fill up the queue and make sure some overflows onto disk
        int overflowNumber = (batchSize * 2) + 1;
        final int totalToSend = (inMemorySize + batchSize) + overflowNumber;
        blockableSyncDispatcher.block();
        for (int i = 0; i < totalToSend; i++) {
            futures.add(asyncDispatcher.send(new MyMessage(Integer.toString(i))));
        }

        // The queue should be full
        await().atMost(1, MINUTES).until(asyncDispatcher::getQueueSize, equalTo(totalToSend - NUM_THREADS));

        // All the dispatch threads should be blocked
        await().atMost(1, MINUTES).until(blockableSyncDispatcher::getBlockedThreadCount, equalTo(NUM_THREADS));
        
        // Release the threads!
        blockableSyncDispatcher.unblock();

        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> blockableSyncDispatcher.getBlockedThreadCount() == 0 &&
                asyncDispatcher.getQueueSize() == 0);

        // We should see all the messages got dispatched, including those that got stored off-heap
        assertThat(blockableSyncDispatcher.getNumMessageDispatched(), equalTo(totalToSend));

        // All of our futures should be successfully resolved
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{}));

        // All of the messages should have been dispatched
        List<MyMessage> actuallyDispatched = blockableSyncDispatcher.getDispatchedMessages();
        assertThat(actuallyDispatched, hasSize(totalToSend));
        for (int i = 0; i < totalToSend; i++) {
            assertThat(actuallyDispatched, hasItem(new MyMessage(Integer.toString(i))));
        }
        
        asyncDispatcher.close();
    }

    @Test
    public void doesNotRaceOnResultCompletingFuture() throws Exception {
        int inMemorySize = 20000;
        int batchSize = 100;
        // Set up the dispatch queue
        DispatchQueueFactory dispatchQueueFactory = new QueueFileOffHeapDispatchQueueFactory(inMemorySize, batchSize,
                "100KB",
                folder.newFolder().toPath().toString());
        DispatchQueueServiceLoader.setDispatchQueue(dispatchQueueFactory);

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

        final AsyncDispatcher<MyMessage> asyncDispatcher = blockableDispatcherFactory.createAsyncDispatcher(module);
        BlockableSyncDispatcher<MyMessage> blockableSyncDispatcher =
                blockableDispatcherFactory.getBlockableSyncDispatcher();

        // Release the threads!
        blockableSyncDispatcher.unblock();

        // Testing locally the race condition occured ~10% of send's so sending 10,000 times should virtually guarantee
        // it occurs.
        IntStream.range(0, 9999).parallel().forEach(i -> asyncDispatcher.send(new MyMessage(Integer.toString(i))));

        assertThat(((AsyncDispatcherImpl) asyncDispatcher).getMissedFutures(), equalTo(0L));

        asyncDispatcher.close();
    }
    
}
