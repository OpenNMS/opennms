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

import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.opennms.netmgt.poller.monitors.PassiveServiceMonitor;
import org.opennms.netmgt.poller.support.DefaultServiceMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers PassiveServiceMonitor in the ServiceMonitorRegistry.
 * In OSGi, ServiceLoader can't discover monitors across bundle boundaries,
 * so we register explicitly after the Poller daemon initializes PollerConfig.
 */
public class PassiveServiceMonitorRegistrar {

    private static final Logger LOG = LoggerFactory.getLogger(PassiveServiceMonitorRegistrar.class);

    public void init() {
        try {
            ServiceMonitorRegistry registry = PollerConfigFactory.getInstance().getServiceMonitorRegistry();
            if (registry instanceof DefaultServiceMonitorRegistry) {
                ((DefaultServiceMonitorRegistry) registry).register(
                        PassiveServiceMonitor.class.getCanonicalName(),
                        new PassiveServiceMonitor());
                LOG.info("PassiveServiceMonitor registered in ServiceMonitorRegistry");
            } else {
                LOG.warn("Registry is {}, cannot register PassiveServiceMonitor", registry.getClass().getName());
            }
        } catch (Exception e) {
            LOG.error("Failed to register PassiveServiceMonitor in ServiceMonitorRegistry", e);
        }
    }
}
