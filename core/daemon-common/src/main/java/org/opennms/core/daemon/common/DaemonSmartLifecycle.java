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
package org.opennms.core.daemon.common;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * Adapts an {@link AbstractServiceDaemon} to Spring's {@link SmartLifecycle} interface,
 * replacing the Karaf-era DaemonLifecycleManager and SpringDaemonLifecycleManager.
 *
 * <p>When Spring calls {@link #start()}, this adapter invokes {@code init()} followed
 * by {@code start()} on the wrapped daemon. When Spring calls {@link #stop()}, the
 * daemon's {@code stop()} method is invoked.</p>
 *
 * <p>The phase is set to {@link Integer#MAX_VALUE} so daemons start last (after all
 * infrastructure beans) and stop first during shutdown.</p>
 */
public class DaemonSmartLifecycle implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(DaemonSmartLifecycle.class);

    private final AbstractServiceDaemon daemon;
    private volatile boolean running = false;

    public DaemonSmartLifecycle(AbstractServiceDaemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public void start() {
        LOG.info("Starting daemon: {}", daemon.getName());
        daemon.init();
        daemon.start();
        running = true;
        LOG.info("Daemon started: {}", daemon.getName());
    }

    @Override
    public void stop() {
        LOG.info("Stopping daemon: {}", daemon.getName());
        daemon.stop();
        running = false;
        LOG.info("Daemon stopped: {}", daemon.getName());
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
