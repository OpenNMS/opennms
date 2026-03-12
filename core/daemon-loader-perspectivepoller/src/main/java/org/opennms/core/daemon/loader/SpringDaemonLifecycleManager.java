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

import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Manages the lifecycle of a {@link SpringServiceDaemon} in a Karaf-only container.
 *
 * <p>{@code SpringServiceDaemon} extends {@code InitializingBean} and {@code DisposableBean},
 * so its lifecycle is: {@code afterPropertiesSet()} then {@code start()} to initialize,
 * and {@code destroy()} to shut down. This class drives that sequence when the
 * enclosing Spring context starts and stops.</p>
 *
 * <p>This is the {@code SpringServiceDaemon} counterpart to {@link DaemonLifecycleManager},
 * which handles {@code AbstractServiceDaemon} instances.</p>
 */
public class SpringDaemonLifecycleManager implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(SpringDaemonLifecycleManager.class);

    private final SpringServiceDaemon daemon;
    private final String daemonName;

    public SpringDaemonLifecycleManager(SpringServiceDaemon daemon, String daemonName) {
        this.daemon = daemon;
        this.daemonName = daemonName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("Initializing daemon: {}", daemonName);
        daemon.afterPropertiesSet();
        LOG.info("Starting daemon: {}", daemonName);
        daemon.start();
        LOG.info("Daemon started: {}", daemonName);
    }

    @Override
    public void destroy() throws Exception {
        LOG.info("Stopping daemon: {}", daemonName);
        daemon.destroy();
        LOG.info("Daemon stopped: {}", daemonName);
    }
}
