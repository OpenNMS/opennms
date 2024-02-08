/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.distributed.coordination.zookeeper;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
import org.opennms.features.distributed.coordination.api.Role;

/**
 * Tests for {@link ZookeeperDomainManager}.
 */
public class ZookeeperDomainManagerTest {
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
        // Wait some extra time to avoid a race condition where a thread was connecting right after we became connected
        Thread.sleep(1000);
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
            assertThat(e, instanceOf(CancelLeadershipException.class));
        }

        // Every registrant should have went standby
        CompletableFuture.allOf(standbyFutures.toArray(new CompletableFuture[numToRegister])).get(10,
                TimeUnit.SECONDS);

        manager.getRoleChangeHandlers().keySet().parallelStream().forEach(manager::deregister);
        await().atMost(10, TimeUnit.SECONDS).until(() -> !manager.isConnected());
        // Wait some extra time to avoid a race condition where a thread was disconnecting right after we became 
        // disconnected
        Thread.sleep(1000);
        assertEquals(1, disconnectionAttempts.get());
        assertThat(manager.isAnythingRegistered(), is(equalTo(false)));
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

        manager.register("test.id." + id, (role, domain) -> {
            if (role == Role.ACTIVE) {
                activeFuture.complete(null);
            } else if (role == Role.STANDBY) {
                standbyFuture.complete(null);
            }
        });
    }
}
