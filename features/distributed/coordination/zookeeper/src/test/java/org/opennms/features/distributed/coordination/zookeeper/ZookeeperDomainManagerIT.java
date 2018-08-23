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

import static org.junit.Assert.assertEquals;

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
import org.opennms.features.distributed.coordination.api.DomainManager;

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
    private final AlwaysZookeeperDomainManagerFactory managerFactory = new AlwaysZookeeperDomainManagerFactory();
    private TestingServer testServer;
    private DomainManager manager;

    @Before
    public void setup() throws Exception {
        int freePort;

        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            freePort = socket.getLocalPort();
        }

        testServer = new TestingServer(freePort, tempFolder.getRoot());
        managerFactory.buildWithConnectString(testServer.getConnectString());
        managerFactory.buildWithNamespace("test.namespace");
        manager = managerFactory.getManagerForDomain(domain);
    }

    @After
    public void cleanup() throws Exception {
        testServer.stop();
    }

    /**
     * Test registering after ZooKeeper is already up.
     *
     * @throws Exception
     */
    @Test
    public void testRegisterAfterZookeeper() throws Exception {
        testServer.start();

        manager.register(id, activeFuture::complete, standbyFuture::complete);
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
        manager.register(id, activeFuture::complete, standbyFuture::complete);

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

        manager.register(id, domain -> {}, domain -> {});
        manager.deregister(id);

        manager.register(id, activeFuture::complete, standbyFuture::complete);
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

        manager.register(id, domain -> activeFutures.get(futureIndex.get()).complete(domain),
                domain -> standbyFutures.get(futureIndex.get()).complete(domain));

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
}
