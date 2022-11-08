/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.filters.shell;


import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

@Command(scope = "opennms", name = "track-service", description = "Track services that match a given filter")
@Service
public class TrackServiceCommand implements Action {

    @Reference
    private NodeDao nodeDao;

    @Reference
    private SessionUtils sessionUtils;

    @Reference
    private ServiceTracker serviceTracker;

    @Argument(index = 0, description = "Service name", required = true)
    private String serviceName;

    @Argument(index = 1, description = "Filter Rule", required = false)
    private String filterRule;

    @Override
    public Object execute() throws IOException {
        System.out.printf("Tracking service named %s%s. Press CTRL+C to stop.\n", serviceName,
                filterRule != null ? String.format(" with filter rule \"%s\"", filterRule) : "");

        // Callback may occur from other threads, but we can't use those to print the results
        // since output isn't redirected to the console. To work around this we push messages to a queue
        // and pull/print these from the command execution thread
        BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
        Closeable session = serviceTracker.trackServiceMatchingFilterRule(serviceName, filterRule, new ServiceTracker.ServiceListener() {
            @Override
            public void onServiceMatched(ServiceRef serviceRef) {
                messageQueue.add(String.format("FOUND: %s\n", getDescription(serviceRef)));
            }

            @Override
            public void onServiceStoppedMatching(ServiceRef serviceRef) {
                messageQueue.add(String.format("REMOVED: %s\n", getDescription(serviceRef)));
            }
        });

        while (true) {
            try {
                System.out.print(messageQueue.take());
            } catch (InterruptedException e) {
                break;
            }
        }

        session.close();
        return null;
    }

    private String getDescription(ServiceRef serviceRef) {
        return sessionUtils.withReadOnlyTransaction(() -> {
            String nodeLabel = "<unknown label>";
            String nodeLocation = "<unknown location>";
            OnmsNode node = nodeDao.get(serviceRef.getNodeId());
            if (node != null) {
                nodeLabel = node.getLabel();
                if (node.getLocation() != null) {
                    nodeLocation = node.getLocation().getLocationName();
                } else {
                    nodeLocation = LocationUtils.DEFAULT_LOCATION_NAME;
                }
            }
            return String.format("%s on interface %s on node %s (id=%d) at location %s.",
                    serviceRef.getServiceName(), InetAddressUtils.str(serviceRef.getIpAddress()),
                    nodeLabel, serviceRef.getNodeId(), nodeLocation);
        });
    }
}
