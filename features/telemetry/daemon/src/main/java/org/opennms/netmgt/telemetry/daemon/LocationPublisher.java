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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class LocationPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(LocationPublisher.class);

    private final String location;
    private final TwinPublisher twinPublisher;
    private final ReentrantLock lock = new ReentrantLock();
    private TwinPublisher.Session<ConnectorTwinConfig> session;
    private final AtomicInteger refCount = new AtomicInteger(0);

    public LocationPublisher(String location, TwinPublisher twinPublisher) {
        this.location = location;
        this.twinPublisher = twinPublisher;
    }


    public void acquireAndPublishStart(ConnectorTwinConfig cfg) throws IOException {
        lock.lock();
        try {
            if (session == null) {
                LOG.info("Initializing twin session for location: {}", location);
                session = twinPublisher.register(
                        ConnectorTwinConfig.CONNECTOR_KEY,
                        ConnectorTwinConfig.class,
                        location
                );
            }

            refCount.incrementAndGet();
            session.publish(cfg);
        } finally {
            lock.unlock();
        }
    }

    public void publishStopAndMaybeClose(ConnectorTwinConfig stopCfg) {
        lock.lock();
        try {
            if (session != null) {
                try {
                    session.publish(stopCfg);
                } catch (IOException e) {
                    LOG.error("Failed to publish stop for location {}: {}", location, e.getMessage(), e);
                }
            } else {
                LOG.debug("No session for location {} while publishing stop", location);
            }

            int remaining = refCount.decrementAndGet();

            if (remaining <= 0) {
                try {
                    if (session != null) {
                        session.close();
                        LOG.info("Twin session closed for location {}", location);
                    }
                } catch (IOException e) {
                    LOG.warn("Failed to close twin session for location {}: {}", location, e.getMessage(), e);
                } finally {
                    session = null;
                    refCount.set(0);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void forceClose() {
        lock.lock();
        try {
            if (session != null) {
                try {
                    session.close();
                    LOG.info("Closed twin session for location {} (force)", location);
                } catch (IOException e) {
                    LOG.warn("Failed to force-close twin session for location {}: {}", location, e.getMessage(), e);
                } finally {
                    session = null;
                    refCount.set(0);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int getRefCount() { return refCount.get(); }
}
