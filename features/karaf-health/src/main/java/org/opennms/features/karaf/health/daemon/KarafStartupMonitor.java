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
package org.opennms.features.karaf.health.daemon;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.features.karaf.health.service.KarafHealthService;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in startup script to detect if Karaf started properly.
 */
public class KarafStartupMonitor implements SpringServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(KarafStartupMonitor.class);

    public static final String NAME = "KarafStartupMonitor";

    @SuppressWarnings("unchecked")
    private static final ServiceLookup<Class<?>, String> SERVICE_LOOKUP = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
            .blocking()
            .build();

    @Override
    public synchronized void start() {
        LOG.info("{} is starting.", NAME);

        if (!isKarafOk()) {
            String message = String.format(NAME + ": It seems Karaf can't be started properly. This is bad, will fail startup.%n" +
                    "What can you do about this?%n" +
                    "1.) check in logs/karaf.log for problems%n" +
                    "2.) clear the 'data' folder - it contains Karaf's cache%n" +
                    "3.) run the script bin/fix-karaf-setup.sh");
            throw new IllegalStateException(message);
        }
        LOG.info("{} is started.", NAME);
    }

    private boolean isKarafOk() {
        Class serviceClass = KarafHealthService.class;
        try {
            LOG.info("Waiting for loading of {}, will block startup until service is available.", serviceClass.getName());
            final KarafHealthService service = SERVICE_LOOKUP.lookup(serviceClass, null);
            return service != null;
        } catch (Exception e) {
            LOG.error("Could not lookup {}, will fail.", serviceClass.getName(), e);
            return false;
        }
    }

    @Override
    public synchronized void destroy() {
        LOG.info("{} is stopped.", NAME);
    }

    @Override
    public void afterPropertiesSet() {
        // pass
    }

}
