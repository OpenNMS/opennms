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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.features.distributed.coordination.api.DomainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DomainManager} that must connect to an external source before being able to participate in a leadership
 * election.
 */
public abstract class ConnectionBasedDomainManager extends AbstractDomainManager {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionBasedDomainManager.class);

    /**
     * Whether or not we are currently connected.
     */
    private boolean connected = false;

    /**
     * This manager should never be attempting to connect multiple times so we will give it a single thread. This also
     * ensures we do not need to synchronize our connect/disconnect attempts since there can only be one running at a
     * given time.
     */
    private final ExecutorService connectionPool = Executors.newSingleThreadExecutor();

    /**
     * Constructor.
     *
     * @param domain the domain to manage
     */
    protected ConnectionBasedDomainManager(String domain) {
        super(domain);
    }

    /**
     * Checks if this domain manager is connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Handles the connect result.
     *
     * @param result    void
     * @param exception the exception if there was one otherwise null
     */
    private void handleConnectResult(Void result, Throwable exception) {
        // If we encountered any exception when trying to connect we will mark ourselves as not connected
        connected = exception == null;

        if (!isConnected()) {
            // TODO: In the event that there was an exception, this method ends up running in the main thread not the
            // async thread that was doing the connection attempt. Not sure if this is safe or not.
            failedToConnect(exception);
        }
    }

    /**
     * Handles the disconnect result.
     *
     * @param result    void
     * @param exception the exception if there was one otherwise null
     */
    private void handleDisconnectResult(Void result, Throwable exception) {
        connected = false;
    }

    /**
     * Implementations can optionally override this if they need special handling for a failure to connect.
     *
     * @param exception the exception that caused the failure the connect
     */
    protected void failedToConnect(Throwable exception) {
        LOG.warn("Failed to connect due to ", exception);
    }

    /**
     * Implementations must connect via this method.
     * <p>
     * This is executed in a separate thread so timeliness is not a factor. It is possible that multiple calls to
     * connect() block waiting for the first one to finish so implementations of this method must be written to guard
     * against this being a problem.
     */
    protected abstract void connect();

    /**
     * Implementations must disconnect via this method.
     * <p>
     * This is executed in a separate thread so timeliness is not a factor. It is possible that multiple calls to
     * disconnect() block waiting for the first one to finish so implementations of this method must be written to guard
     * against this being a problem.
     */
    protected abstract void disconnect();

    @Override
    protected final void onFirstRegister() {
        // To prevent the register() call from blocking we need to connect async
        CompletableFuture
                .runAsync(this::connect, connectionPool)
                .whenComplete(this::handleConnectResult);
    }

    @Override
    protected final void onLastDeregister() {
        // To prevent the deregister() call from blocking we need to disconnect async
        CompletableFuture
                .runAsync(this::disconnect, connectionPool)
                .whenComplete(this::handleDisconnectResult);
    }

    @Override
    public String toString() {
        return "org.opennms.features.distributed.coordination.base.ConnectionBasedDomainManager{" +
                "connected=" + connected +
                "} " + super.toString();
    }
}
