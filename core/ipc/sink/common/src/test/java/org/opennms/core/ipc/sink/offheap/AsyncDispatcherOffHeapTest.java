/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.offheap;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.ThreadLockingDispatcherFactory;
import org.opennms.core.ipc.sink.common.ThreadLockingSyncDispatcher;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(MockitoJUnitRunner.class)
public class AsyncDispatcherOffHeapTest {

    private final static String OFFHEAP_SIZE = "offHeapSize";
    private final static String ENABLE_OFFHEAP = "enableOffHeap";
    public static final String OFFHEAP_CONFIG = "org.opennms.core.ipc.sink.offheap";
    private static final int QUEUE_SIZE = 100;
    private static final int NUM_THREADS = 16;
    private static final int OFFHEAP_MESSAGES = 200;
    private H2OffHeapStore offHeapStore;

    private SinkModule<MockMessage, MockMessage> module = new MockModule();

    private final ThreadLockingDispatcherFactory<MockMessage> threadLockingDispatcherFactory = new ThreadLockingDispatcherFactory<>();

    @Before
    public void setup() throws IOException {
        Hashtable<String, Object> configProperties = new Hashtable<>();
        configProperties.put(OFFHEAP_SIZE, "100000");
        configProperties.put(ENABLE_OFFHEAP, true);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(OFFHEAP_CONFIG).getProperties()).thenReturn(configProperties);
        offHeapStore = new H2OffHeapStore(configAdmin);
        offHeapStore.init();
        OffHeapServiceLoader.setOffHeapQueue(offHeapStore);
        OffHeapServiceLoader.setOffHeapEnabled(true);
    }
    @Test(timeout=4*60*1000)
    public void testOffHeapStore() throws Exception {

        final AsyncDispatcher<MockMessage> asyncDispatcher = threadLockingDispatcherFactory.createAsyncDispatcher(module);

        final AtomicBoolean allThreadsLocked = new AtomicBoolean(false);
        ThreadLockingSyncDispatcher<MockMessage> threadLockingSyncDispatcher = threadLockingDispatcherFactory.getThreadLockingSyncDispatcher();
        threadLockingSyncDispatcher.waitForThreads(NUM_THREADS).thenRun(() -> {
            allThreadsLocked.set(true);
        });

        final List<CompletableFuture<MockMessage>> futures = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS + QUEUE_SIZE; i++) {
            futures.add(asyncDispatcher.send(new MockMessage(Integer.toString(i))));
        }

        // All of the dispatcher thread should be locked, and
        await().atMost(1, MINUTES).until(() -> allThreadsLocked.get());
        // No additional thread should be waiting
        assertEquals(0, threadLockingSyncDispatcher.getNumExtraThreadsWaiting());
        // The queue should be full
        assertEquals(QUEUE_SIZE, asyncDispatcher.getQueueSize());

        for (int i = NUM_THREADS + QUEUE_SIZE; i < NUM_THREADS + QUEUE_SIZE + OFFHEAP_MESSAGES; i++) {
            CompletableFuture<MockMessage> future = asyncDispatcher.send(new MockMessage(Integer.toString(i)));
            futures.add(future);
        }
        // Since one message will be read immediately without waiting, numOfMessages would be one less than num of offheap messages.
        await().atMost(1, MINUTES).pollDelay(5, TimeUnit.SECONDS)
                .until(() -> offHeapStore.numOfMessages(module.getId()), equalTo(OFFHEAP_MESSAGES -1));
        // Release the threads!
        threadLockingSyncDispatcher.release();
        // Wait for the queue to be drained
        await().atMost(1, MINUTES).until(() -> asyncDispatcher.getQueueSize(), equalTo(0));
        await().atMost(1, MINUTES).until(() -> threadLockingDispatcherFactory.getNumMessageDispatched(),
                greaterThan(QUEUE_SIZE + OFFHEAP_MESSAGES));

        // All of our futures should be successfully resolved
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[]{}));

        asyncDispatcher.close();
    }

}
