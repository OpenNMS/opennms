/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class ReductionKeyHelper {

    // TODO: The distributed poller name (now monitoring system name?) should be part of the edge details
    public static final String DEFAULT_DISTRIBUTED_POLLER_NAME = "";

    public static Set<String> getReductionKeys(final OnmsMonitoredService monitoredService) {
        Objects.requireNonNull(monitoredService);
        Set<String> reductionKeys = new HashSet<>();
        reductionKeys.add(getNodeLostServiceReductionKey(monitoredService));
        // When node processing is enabled, we may get node down instead of node lost service events
        reductionKeys.add(getNodeDownReductionKey(monitoredService));
        // We may get interface down instead of node lost service events
        reductionKeys.add(getInterfaceDownReductionKey(monitoredService));
        return reductionKeys;
    }

    public static String getNodeDownReductionKey(final OnmsMonitoredService monitoredService) {
        return String.format("%s:%s:%d",
                EventConstants.NODE_DOWN_EVENT_UEI,
                DEFAULT_DISTRIBUTED_POLLER_NAME,
                monitoredService.getNodeId());
    }

    public static String getNodeLostServiceReductionKey(final OnmsMonitoredService monitoredService) {
        return String.format("%s:%s:%d:%s:%s",
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI,
                DEFAULT_DISTRIBUTED_POLLER_NAME,
                monitoredService.getNodeId(),
                InetAddressUtils.toIpAddrString(monitoredService.getIpAddress()),
                monitoredService.getServiceName());
    }

    public static String getInterfaceDownReductionKey(final OnmsMonitoredService monitoredService) {
        return String.format("%s:%s:%s:%s",
                EventConstants.INTERFACE_DOWN_EVENT_UEI,
                DEFAULT_DISTRIBUTED_POLLER_NAME,
                monitoredService.getNodeId(),
                InetAddressUtils.toIpAddrString(monitoredService.getIpAddress()));
    }
}
