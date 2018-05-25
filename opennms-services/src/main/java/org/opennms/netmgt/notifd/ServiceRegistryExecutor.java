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

import java.util.List;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistryExecutor implements ExecutorStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistryExecutor.class);

    private static final ServiceLookup SERVICE_LOOKUP = new ServiceLookupBuilder(DefaultServiceRegistry.INSTANCE)
            .blocking()
            .build();

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
        return SERVICE_LOOKUP.lookup(NotificationStrategy.class, filter);
    }
}
