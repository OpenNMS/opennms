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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.Role;

import org.awaitility.core.ConditionTimeoutException;

/**
 * Integration tests for {@link ZookeeperDomainManager}.
 */
public class ZookeeperDomainManagerIT {
    private static final String domain = "test.domain";
    private static final String id = "test.id";
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();
    private final CompletableFuture<String> activeFuture = new CompletableFuture<>();
    private final CompletableFuture<String> standbyFuture = new CompletableFuture<>();
    private ZookeeperDomainManagerFactory managerFactory;
    private TestingServer testServer;
    private DomainManager manager;

    @Before
    public void setup() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        int freePort;

        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            freePort = socket.getLocalPort();
        }

        testServer = new TestingServer(freePort, tempFolder.getRoot(), false);
        managerFactory = new ZookeeperDomainManagerFactory(testServer.getConnectString(), "test.namespace");
        manager = managerFactory.getManagerForDomain(domain);
    }

    @After
    public void cleanup() throws Exception {
        testServer.stop();
    }

    /**
     * Tests to make sure we do not become active if Zookeeper is not available.
     */
    @Test(expected = ConditionTimeoutException.class)
    public void testWithZookeeperDown() {
        register();
        await().atMost(10, TimeUnit.SECONDS).until(activeFuture::isDone);
        fail("Became active when we shouldn't have");
    }

    /**
     * Test registering after ZooKeeper is already up.
     *
     * @throws Exception
     */
    @Test
    public void testRegisterAfterZookeeper() throws Exception {
        testServer.start();

        register();
        assertEquals(domain, activeFuture.get(10, TimeUnit.SECONDS));

        testServer.stop();
        assertEquals(domain, standbyFuture.get(10, TimeUnit.SECONDS));
    }

    /**
     * Test registering before ZooKeeper is up.
     *
     * @throws Exception
     */
    @Test
    public void testRegisterBeforeZookeeper() throws Exception {
        register();

        testServer.start();
        assertEquals(domain, activeFuture.get(10, TimeUnit.SECONDS));

        testServer.stop();
        assertEquals(domain, standbyFuture.get(10, TimeUnit.SECONDS));
    }

    /**
     * Test registering after deregistering to make sure we still get notified.
     *
     * @throws Exception
     */
    @Test
    public void testReregister() throws Exception {
        testServer.start();

        manager.register(id, (role, domain) -> {});
        manager.deregister(id);

        register();
        assertEquals(domain, activeFuture.get(10, TimeUnit.SECONDS));
    }

    /**
     * Test ZooKeeper flapping.
     * <p>
     * Note: This test will take several seconds to complete due to waiting for ZooKeeper to go up and down.
     *
     * @throws Exception
     */
    @Test
    public void testZookeeperFlap() throws Exception {
        List<CompletableFuture<String>> activeFutures = new ArrayList<>();
        List<CompletableFuture<String>> standbyFutures = new ArrayList<>();
        AtomicInteger futureIndex = new AtomicInteger(0);

        manager.register(id, (role, domain) -> {
            if (role == Role.ACTIVE) {
                activeFutures.get(futureIndex.get()).complete(domain);
            } else if (role == Role.STANDBY) {
                standbyFutures.get(futureIndex.get()).complete(domain);
            }
        });

        // There is no asserts here but failure will occur due to timeout exception if one of the active/standby calls
        // does not happen
        int numFlaps = 3;
        for (int i = 0; i < numFlaps; i++) {
            activeFutures.add(new CompletableFuture<>());
            standbyFutures.add(new CompletableFuture<>());
            testServer.restart();
            activeFutures.get(futureIndex.get()).get(10, TimeUnit.SECONDS);
            testServer.stop();
            standbyFutures.get(futureIndex.get()).get(10, TimeUnit.SECONDS);
            futureIndex.incrementAndGet();
        }
    }
    
    private void register() {
        manager.register(id, (role, domain) -> {
            if (role == Role.ACTIVE) {
                activeFuture.complete(domain);
            } else if (role == Role.STANDBY) {
                standbyFuture.complete(domain);
            }
        });
    }
}
