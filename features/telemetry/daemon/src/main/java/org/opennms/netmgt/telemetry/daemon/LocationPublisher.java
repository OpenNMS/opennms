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
package org.opennms.netmgt.telemetry.daemon;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LocationPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(LocationPublisher.class);
    private final String location;
    private final TwinPublisher twinPublisher;
    private final ReentrantLock lock = new ReentrantLock();
    private TwinPublisher.Session<ConnectorTwinConfig> session;
    private final Map<String, ConnectorTwinConfig.ConnectorConfig> configs = new HashMap<>();
    private String queueName;
    private boolean publishPending;

    public LocationPublisher(String location, TwinPublisher twinPublisher) {
        this.location = location;
        this.twinPublisher = twinPublisher;
        if (session == null) {
            try {
                session = twinPublisher.register(
                        ConnectorTwinConfig.CONNECTOR_KEY,
                        ConnectorTwinConfig.class,
                        location
                );
            } catch (IOException e) {
                LOG.error("Failed to create  session for {}: {}", location, e.getMessage(), e);
            }
        }
    }

    public void addConfigAndPublish(ConnectorTwinConfig.ConnectorConfig cfg) throws IOException {
        updateConfigsAndPublish(Collections.singletonList(cfg), Collections.emptyList());
    }

    public void removeConfigAndPublish(String connectionKey) throws IOException {
        updateConfigsAndPublish(Collections.emptyList(), Collections.singletonList(connectionKey));
    }

    /**
     * Applies all removals and additions, then publishes the resulting configuration once.
     * Nothing is published when the call changes nothing and no earlier publish is pending.
     * The changes are kept even when publishing fails, and are published with the next update.
     */
    public void updateConfigsAndPublish(Collection<ConnectorTwinConfig.ConnectorConfig> added,
                                        Collection<String> removedConnectionKeys) throws IOException {
        lock.lock();
        try {
            boolean changed = false;
            for (String connectionKey : removedConnectionKeys) {
                changed |= configs.remove(connectionKey) != null;
            }
            for (ConnectorTwinConfig.ConnectorConfig cfg : added) {
                configs.put(cfg.getConnectionKey(), cfg);
                changed = true;
            }
            if (changed || publishPending) {
                publish();
            }
        } finally {
            lock.unlock();
        }
    }

    private void publish() throws IOException {
        publishPending = true;
        publishCurrentConfigs();
        publishPending = false;
        if (configs.isEmpty()) {
            closeSession();
        }
    }

    private void publishCurrentConfigs() throws IOException {
        if (session == null) {
            session = twinPublisher.register(ConnectorTwinConfig.CONNECTOR_KEY, ConnectorTwinConfig.class, location);
        }
        ConnectorTwinConfig confReq = new ConnectorTwinConfig(
                queueName,
                new ArrayList<>(configs.values())
        );
        session.publish(confReq);
    }

    private void closeSession() throws IOException {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public void forceClose() {
        lock.lock();
        try {
            configs.clear();
            publishPending = false;
            closeSession();
        } catch (IOException e) {
            LOG.error("Failed to close session for {}: {}", location, e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasConfigs() {
        lock.lock();
        try {
            return !configs.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public boolean isPublishPending() {
        lock.lock();
        try {
            return publishPending;
        } finally {
            lock.unlock();
        }
    }

    public void setQueueName(String queueName) {
        lock.lock();
        try {
            if (queueName != null) {
                this.queueName = queueName;
            }
        } finally {
            lock.unlock();
        }
    }
}