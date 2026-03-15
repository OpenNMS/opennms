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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.opennms.netmgt.poller.monitors.PassiveServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local ServiceMonitorRegistry for standalone daemon containers.
 * Discovers ServiceMonitor implementations via Java ServiceLoader
 * and explicitly registers monitors that can't be discovered in OSGi.
 */
public class LocalServiceMonitorRegistry implements ServiceMonitorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(LocalServiceMonitorRegistry.class);

    private final Map<String, ServiceMonitor> monitorsByClassName = new HashMap<>();

    public LocalServiceMonitorRegistry() {
        for (ServiceMonitor monitor : ServiceLoader.load(ServiceMonitor.class)) {
            final String className = monitor.getClass().getCanonicalName();
            LOG.info("Registered service monitor: {}", className);
            monitorsByClassName.put(className, monitor);
        }
        // In Karaf OSGi, ServiceLoader can't discover monitors across bundle boundaries.
        // Explicitly register monitors from poller-api that aren't in the monitors-core JAR.
        monitorsByClassName.putIfAbsent(PassiveServiceMonitor.class.getCanonicalName(), new PassiveServiceMonitor());
        LOG.info("Loaded {} service monitors (ServiceLoader + explicit)", monitorsByClassName.size());
    }

    @Override
    public ServiceMonitor getMonitorByClassName(String className) {
        return monitorsByClassName.get(className);
    }

    @Override
    public Set<String> getMonitorClassNames() {
        return Collections.unmodifiableSet(monitorsByClassName.keySet());
    }
}
