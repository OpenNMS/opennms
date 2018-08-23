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

package org.opennms.features.distributed.coordination.zookeeper;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.junit.Test;

/**
 * Tests for {@link ZookeeperDomainManager}.
 */
public class TestZookeeperDomainManager {
    private final AtomicInteger connectionAttempts = new AtomicInteger(0);
    private final AtomicInteger disconnectionAttempts = new AtomicInteger(0);
    private final List<CompletableFuture<Void>> activeFutures = new CopyOnWriteArrayList<>();
    private final List<CompletableFuture<Void>> standbyFutures = new CopyOnWriteArrayList<>();
    private ZookeeperDomainManager manager;

    /**
     * Test that the registered callbacks are called when ZooKeeper informs the manager to take leadership. Also tests
     * that only one connection attempt to ZooKeeper was initiated even though multiple registrations/deregistrations
     * are occurring in parallel.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbacks() throws InterruptedException, TimeoutException, ExecutionException {
        CuratorFramework mockClient = mock(CuratorFramework.class);
        CuratorFrameworkFactory.Builder mockBuilder = mock(CuratorFrameworkFactory.Builder.class);
        LeaderSelector mockLeaderSelector = mock(LeaderSelector.class);

        when(mockBuilder.build()).thenReturn(mockClient);
        doAnswer((invocation) -> connectionAttempts.incrementAndGet()).when(mockClient).start();
        doAnswer((invocation) -> disconnectionAttempts.incrementAndGet()).when(mockClient).close();

        manager = ZookeeperDomainManager.withMocks("test.domain", mockBuilder, mockLeaderSelector);
        LeaderSelectorListener leaderSelectorListener = manager.getLeaderSelectorListener();

        int numToRegister = 100;
        IntStream.range(0, numToRegister).parallel().forEach(this::register);

        // We want to wait until the connection from above actually occurred
        await().atMost(10, TimeUnit.SECONDS).until(manager::isConnected);
        // TODO: This has potential to false positive since we could theoretically have another thread connecting
        assertEquals(1, connectionAttempts.get());

        // Simulate ZooKeeper telling our client it is leader
        CompletableFuture<Void> leadershipFuture = CompletableFuture.runAsync(() -> {
            try {
                leaderSelectorListener.takeLeadership(null);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        // Every registrant should have went active
        CompletableFuture.allOf(activeFutures.toArray(new CompletableFuture[numToRegister])).get(10,
                TimeUnit.SECONDS);

        if (leadershipFuture.isDone() || leadershipFuture.isCompletedExceptionally()) {
            fail("Leadership thread did not block");
        }

        try {
            leaderSelectorListener.stateChanged(null, ConnectionState.LOST);
            fail("No cancel leadership exception was caught");
        } catch (Exception e) {
            assertTrue(e instanceof CancelLeadershipException);
        }

        // Every registrant should have went standby
        CompletableFuture.allOf(standbyFutures.toArray(new CompletableFuture[numToRegister])).get(10,
                TimeUnit.SECONDS);

        manager.getRoleChangeHandlers().keySet().parallelStream().forEach(manager::deregister);
        await().atMost(10, TimeUnit.SECONDS).until(() -> !manager.isConnected());
        // TODO: This has potential to false positive since we could theoretically have another thread disconnecting
        assertEquals(1, disconnectionAttempts.get());
        assertFalse(manager.isAnythingRegistered());
    }

    /**
     * Register the a change handlers that will complete futures on active/standby.
     *
     * @param id the id to register
     */
    private void register(int id) {
        CompletableFuture<Void> activeFuture = new CompletableFuture<>();
        activeFutures.add(activeFuture);
        CompletableFuture<Void> standbyFuture = new CompletableFuture<>();
        standbyFutures.add(standbyFuture);

        manager.register("test.id." + id, domain -> activeFuture.complete(null),
                domain -> standbyFuture.complete(null));
    }
}
