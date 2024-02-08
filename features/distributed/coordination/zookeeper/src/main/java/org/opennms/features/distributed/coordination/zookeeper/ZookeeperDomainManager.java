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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.common.ConnectionBasedDomainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DomainManager} that uses Apache ZooKeeper for leadership elections.
 */
public final class ZookeeperDomainManager extends ConnectionBasedDomainManager {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperDomainManager.class);

    /**
     * Notifies us when we become leader.
     */
    private LeaderSelector leaderSelector;

    /**
     * The client builder. We take a builder because clients can only be started/stopped once. To be able to stop our
     * client but still be able to connect again in the future we need to rebuild the client each time we want to
     * connect.
     */
    private final CuratorFrameworkFactory.Builder clientBuilder;

    /**
     * The Curator client.
     */
    private CuratorFramework client;

    /**
     * Receives a notification of when we become leader.
     */
    private final LeaderSelectorListener leaderSelectorListener = new LeaderSelectorListenerAdapter() {
        @Override
        public void takeLeadership(CuratorFramework client) {
            LOG.trace("calling becomeActive()");
            becomeActive();

            // This blocks the thread to prevent relinquishing leadership
            synchronized (this) {
                while(true) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        LOG.debug("Leadership thread was interrupted");
                        break;
                    }
                }
            }
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            if (newState == ConnectionState.LOST || newState == ConnectionState.SUSPENDED) {
                LOG.trace("calling becomeStandby()");
                becomeStandby();
            }

            // this used to be done for us in older versions of the upstream framework
            if (client == null) {
                throw new CancelLeadershipException();
            }

            super.stateChanged(client, newState);
        }
    };

    /**
     * Private constructor.
     *
     * @param domain        the domain to manage
     * @param clientBuilder the curator framework client to use
     */
    private ZookeeperDomainManager(String domain, CuratorFrameworkFactory.Builder clientBuilder) {
        super(domain);
        this.clientBuilder = clientBuilder;
    }

    /**
     * Private constructor to be used for unit testing only.
     *
     * @param domain             the domain
     * @param mockClientBuilder  a mocked client
     * @param mockLeaderSelector a mocked leader selector
     */
    private ZookeeperDomainManager(String domain, CuratorFrameworkFactory.Builder mockClientBuilder,
                                   LeaderSelector mockLeaderSelector) {
        super(domain);
        clientBuilder = mockClientBuilder;
        leaderSelector = mockLeaderSelector;
    }

    /**
     * Default static factory method.
     *
     * @param domain        the domain to manage
     * @param clientBuilder the client to use
     * @return the manager instance
     */
    static ZookeeperDomainManager of(String domain, CuratorFrameworkFactory.Builder clientBuilder) {
        return new ZookeeperDomainManager(domain, clientBuilder);
    }

    /**
     * Static factory method for use in unit tests using mocks.
     *
     * @param domain             the domain to manage
     * @param mockClientBuilder  the mock client
     * @param mockLeaderSelector the mock leader selector
     */
    static ZookeeperDomainManager withMocks(String domain, CuratorFrameworkFactory.Builder mockClientBuilder,
                                            LeaderSelector mockLeaderSelector) {
        return new ZookeeperDomainManager(domain, mockClientBuilder, mockLeaderSelector);
    }

    /**
     * Closes the connection.
     */
    private void closeConnection() {
        if (leaderSelector != null) {
            leaderSelector.close();
            leaderSelector = null;
        }

        client.close();
        client = null;
    }

    /**
     * A method for unit testing to obtain the {@link LeaderSelectorListener} being used.
     *
     * @return the LeaderSelectorListener
     */
    LeaderSelectorListener getLeaderSelectorListener() {
        return leaderSelectorListener;
    }

    @Override
    protected void failedToConnect(Throwable exception) {
        super.failedToConnect(exception);
        closeConnection();
    }

    @Override
    protected void connect() {
        if (isConnected()) {
            return;
        }

        if (client == null) {
            client = clientBuilder.build();
        }

        client.start();

        boolean blocked = true;
        while (blocked) {
            try {
                LOG.info("Connecting to ZooKeeper with client {}", client);
                client.blockUntilConnected();
                blocked = false;
                LOG.info("Connected to ZooKeeper");
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while connecting to ZooKeeper", e);
            }
        }

        if (leaderSelector == null) {
            leaderSelector = new LeaderSelector(client, "/" + getDomain(),
                    leaderSelectorListener);
        }

        leaderSelector.autoRequeue();
        leaderSelector.start();
    }

    @Override
    protected void disconnect() {
        if (!isConnected()) {
            return;
        }

        LOG.info("Disconnecting from ", client);
        closeConnection();
    }

    @Override
    public String toString() {
        return "ZookeeperDomainManager{" +
                "leaderSelector=" + leaderSelector +
                ", leaderSelectorListener=" + leaderSelectorListener +
                "} " + super.toString();
    }
}
