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
package org.opennms.core.daemon.loader;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Manages the lifecycle of a ServiceDaemon in a Karaf-only container.
 *
 * <p>In the full Horizon runtime, daemons are started by the Manager via
 * {@code AbstractSpringContextJmxServiceDaemon}. In a Karaf-only assembly,
 * there is no Manager — this class provides the same lifecycle management
 * by calling {@code init()} and {@code start()} when the Spring context
 * initializes, and {@code stop()} when it closes.</p>
 */
public class DaemonLifecycleManager implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(DaemonLifecycleManager.class);

    private final AbstractServiceDaemon daemon;

    public DaemonLifecycleManager(AbstractServiceDaemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public void afterPropertiesSet() {
        LOG.info("Starting daemon: {}", daemon.getName());
        daemon.init();
        daemon.start();
        LOG.info("Daemon started: {}", daemon.getName());
    }

    @Override
    public void destroy() {
        LOG.info("Stopping daemon: {}", daemon.getName());
        daemon.stop();
        LOG.info("Daemon stopped: {}", daemon.getName());
    }
}
