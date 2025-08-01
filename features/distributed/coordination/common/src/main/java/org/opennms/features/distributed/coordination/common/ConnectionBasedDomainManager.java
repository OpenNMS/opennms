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
package org.opennms.features.distributed.coordination.common;

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
        LOG.warn("Failed to connect due to exception", exception);
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
