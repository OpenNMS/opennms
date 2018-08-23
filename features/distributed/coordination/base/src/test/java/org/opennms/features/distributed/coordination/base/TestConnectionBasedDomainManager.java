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

package org.opennms.features.distributed.coordination.base;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ConnectionBasedDomainManager}.
 */
public class TestConnectionBasedDomainManager {
    private static final String id = "test.id";
    private static final String domain = "test.domain";
    private final CompletableFuture<Thread> connectThreadFuture = new CompletableFuture<>();
    private final CompletableFuture<Thread> disconnectThreadFuture = new CompletableFuture<>();

    /**
     * Tests that connecting and disconnecting occurs in a separate thread.
     *
     * @throws Exception
     */
    @Test
    public void testNonBlockingConnectDisconnect() throws Exception {
        TestDomainManager manager = new TestDomainManager(domain);

        manager.register(id, domain -> {}, domain -> {});
        waitForFuture(connectThreadFuture);

        manager.deregister(id);
        waitForFuture(disconnectThreadFuture);
    }

    /**
     * Tests to make sure that an exception encountered while trying to connect prevents the connection from
     * succeeding.
     *
     * @throws Exception
     */
    @Test
    public void testExceptionPreventsConnection() throws Exception {
        ExceptionOnConnectDomainManager manager = new ExceptionOnConnectDomainManager(domain);

        manager.register(id, domain -> {}, domain -> {});
        waitForFuture(connectThreadFuture);
        Assert.assertFalse(manager.isConnected());
    }

    /**
     * Test that once we have deregistered we won't receive anymore notifications to our callbacks.
     */
    @Test
    public void testDeregisterThenDisconnect() {
        TestDomainManager manager = new TestDomainManager(domain);
        AtomicInteger active = new AtomicInteger(0);
        AtomicInteger standby = new AtomicInteger(0);

        manager.register(id, domain -> active.incrementAndGet(), domain -> standby.incrementAndGet());
        await().atMost(10, TimeUnit.SECONDS).until(() -> active.get() > 0);

        manager.deregister(id);
        await().atMost(10, TimeUnit.SECONDS).until(() -> !manager.isConnected());

        manager.register("dummyId", domain -> {}, domain -> {});
        await().atMost(10, TimeUnit.SECONDS).until(manager::isConnected);

        assertEquals(1, active.get());
        assertEquals(0, standby.get());
    }

    private static void waitForFuture(Future<Thread> threadFuture) throws Exception {
        Assert.assertNotSame(Thread.currentThread(), threadFuture.get(100, TimeUnit.MILLISECONDS));
    }

    private class TestDomainManager extends ConnectionBasedDomainManager {
        TestDomainManager(String domain) {
            super(domain);
        }

        @Override
        protected void connect() {
            becomeActive();
            connectThreadFuture.complete(Thread.currentThread());
        }

        @Override
        protected void disconnect() {
            disconnectThreadFuture.complete(Thread.currentThread());
        }
    }

    private class ExceptionOnConnectDomainManager extends ConnectionBasedDomainManager {
        private Thread connectThread;

        ExceptionOnConnectDomainManager(String domain) {
            super(domain);
        }

        @Override
        protected void connect() {
            connectThread = Thread.currentThread();

            throw new RuntimeException();
        }

        @Override
        protected void disconnect() {
        }

        @Override
        protected void failedToConnect(Throwable exception) {
            connectThreadFuture.complete(connectThread);
        }
    }
}
