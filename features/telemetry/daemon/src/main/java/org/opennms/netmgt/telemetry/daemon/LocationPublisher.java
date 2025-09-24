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
        lock.lock();
        try {
            configs.put(cfg.getNodeConnectorKey(), cfg);
            publishCurrentConfigs();
        } finally {
            lock.unlock();
        }
    }

    public void removeConfigAndPublish(String connectionKey) throws IOException {
        lock.lock();
        try {
            if (configs.remove(connectionKey) != null) {
                publishCurrentConfigs();
                if (configs.isEmpty()) {
                    closeSession();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void publishCurrentConfigs() throws IOException {
        session.publish(new ConnectorTwinConfig(new ArrayList<>(configs.values())));
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
}