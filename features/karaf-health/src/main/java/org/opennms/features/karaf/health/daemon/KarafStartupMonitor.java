/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

    public static final String LOG_PREFIX = "karafStartupMonitor";

    @SuppressWarnings("unchecked")
    private static final ServiceLookup<Class<?>, String> SERVICE_LOOKUP = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
            .blocking()
            .build();

    @Override
    public synchronized void start() {
        LOG.info("{} is starting.", NAME);

        if (!isKarafOk()) {
            String message = String.format(NAME + ": It seems Kafka can't be started properly. This is bad, will fail startup.%n" +
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
            final KarafHealthService service = SERVICE_LOOKUP.lookup(serviceClass, null);
            return service != null;
        } catch (Exception e) {
            LOG.warn("Could not lookup {}, will fail.", serviceClass.getName(), e);
            return false;
        }
    }

    /*
    private boolean isKarafOkAlternative() {
        try {
            // TODO: if we use this method we wil hae to move the health service to the end of the list in org.apache.features.cfg
            String healthEndpoint = "http://admin:admin@localhost:8980/opennms/rest/health"; // TODO: patrick Is this URL always correct?
            String base64Creds = Base64.getEncoder().encodeToString("admin:admin".getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + base64Creds);
            HttpEntity request = new HttpEntity(headers);
            ResponseEntity<String> response = new RestTemplate().exchange(healthEndpoint, HttpMethod.GET, request, String.class);
            // TODO: patrick: do we need to check the content?
            return true;
        } catch (Exception e) {
            // something went wrong => the health endpoint has a problem
            return false;
        }
    }
    */

    @Override
    public synchronized void destroy() {
        LOG.info("{} is stopped.", NAME);
    }

    @Override
    public void afterPropertiesSet() {
        // pass
    }

}