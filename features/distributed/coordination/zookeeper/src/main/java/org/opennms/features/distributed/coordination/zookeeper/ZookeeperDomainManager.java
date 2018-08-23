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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.base.ConnectionBasedDomainManager;
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
        public void takeLeadership(CuratorFramework client) throws InterruptedException {
            LOG.trace("calling becomeActive()");
            becomeActive();

            // This blocks the thread to prevent relinquishing leadership
            synchronized (this) {
                this.wait();
            }
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            if (newState == ConnectionState.LOST || newState == ConnectionState.SUSPENDED) {
                LOG.trace("calling becomeStandby()");
                becomeStandby();
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

        try {
            LOG.info("Connecting to ZooKeeper with client: ", client);
            client.blockUntilConnected();
            LOG.info("Connected to ZooKeeper");
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while connecting to ZooKeeper", e);
        }

        if (leaderSelector == null) {
            // TODO: Not sure if the leading slash is necessary
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
