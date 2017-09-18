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

package org.opennms.netmgt.notifd;

import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceRegistryExecutor implements ExecutorStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistryExecutor.class);

    private static final DefaultServiceRegistry s_registry = DefaultServiceRegistry.INSTANCE;

    private static final String GRACE_PERIOD_MS_SYS_PROP = "org.opennms.netmgt.notifd.notificationStrategyGracePeriodMs";
    private static final int GRACE_PERIOD_MS = Integer.getInteger(GRACE_PERIOD_MS_SYS_PROP, 3*60*1000);
    private static final int LOOKUP_DELAY_MS = 5*1000;

    @Override
    public int execute(String filter, List<Argument> arguments) {
        LOG.debug("Searching for notification strategy matching filter: {}", filter);
        final NotificationStrategy ns = getNotificationStrategy(filter);
        if (ns == null) {
            LOG.error("No notification strategy found matching filter: {}", filter);
            return 1;
        }
        LOG.debug("Found notification strategy: {}", ns);
        return ns.send(arguments);
    }

    private NotificationStrategy getNotificationStrategy(String filter) {
        // Lookup
        NotificationStrategy ns = s_registry.findProvider(NotificationStrategy.class, filter);
        if (ns != null) {
            return ns;
        }

        // A strategy matching the filter is not currently available.
        // Wait until the system has finished starting up (uptime >= grace period)
        // before aborting the search.
        while (ManagementFactory.getRuntimeMXBean().getUptime() < GRACE_PERIOD_MS) {
            try {
                Thread.sleep(LOOKUP_DELAY_MS);
            } catch (InterruptedException e) {
                LOG.error("Interrupted while waiting for notification strategy to become available in the service registry. Aborting.");
                return null;
            }
            ns = s_registry.findProvider(NotificationStrategy.class, filter);
            if (ns != null) {
                return ns;
            }
        }

        return null;
    }
}
