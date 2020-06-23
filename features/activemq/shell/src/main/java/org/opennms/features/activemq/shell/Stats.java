/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.activemq.shell;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.StringUtils;
import org.opennms.features.activemq.broker.api.ManagedDestination;
import org.opennms.features.activemq.broker.api.ManagedBroker;

@Command(scope = "opennms-activemq", name = "stats", description = "Show statistics for the embedded ActiveMQ broker.")
@Service
public class Stats implements Action {

    @Reference(optional = true)
    private ManagedBroker broker;

    @Option(name="-n", aliases="--top-n", description="Only show the Top N destinations (set to 0 to show all)")
    private int topNDests = 5;

    @Option(name="-a", aliases="--show-advisory", description="Show advisory topics.")
    private boolean showAdvisoryTopics = false;

    @Override
    public Object execute() {
        if (broker == null) {
            System.out.println("(No broker available.)");
            return null;
        }

        System.out.println("Broker statistics:");
        System.out.printf("\tConnections: %d\n", broker.getCurrentConnections());
        System.out.printf("\tMemory percent usage: %d%%\n", broker.getMemoryPercentUsage());
        System.out.printf("\tMemory usage: %s\n", StringUtils.getHumanReadableByteCount(broker.getMemoryUsage(), false));
        System.out.printf("\tMemory limit: %s\n", StringUtils.getHumanReadableByteCount(broker.getMemoryLimit(), false));

        System.out.print("Destination statistics ");
        Comparator<ManagedDestination> comparator;
        if (topNDests > 0) {
            System.out.printf("(top %d):\n", topNDests);
            comparator = Comparator.comparing(ManagedDestination::getMessageCount)
                    .thenComparing(d -> d.getEnqueueCount() + d.getDequeueCount())
                    .reversed()
                    .thenComparing(ManagedDestination::getName);
        } else {
            System.out.println("(All):");
            // Only sort by name
            comparator = Comparator.comparing(ManagedDestination::getName);
        }

        final List<ManagedDestination> dests = broker.getDestinations().stream()
                .filter(d -> !d.getName().startsWith("ActiveMQ.Advisory") || showAdvisoryTopics)
                .sorted(comparator)
                .limit(topNDests > 0 ? topNDests : Integer.MAX_VALUE)
                .collect(Collectors.toList());
        for (ManagedDestination dest : dests) {
            System.out.printf("\t%s (%s)\n", dest.getName(), getTypeName(dest));
            System.out.printf("\t\tMessage count: %d\n", dest.getMessageCount());
            System.out.printf("\t\tEnqueue count: %s\n", dest.getEnqueueCount());
            System.out.printf("\t\tDequeue count: %s\n", dest.getDequeueCount());
            System.out.printf("\t\tCursor full: %s\n", dest.isCursorFull());
        }
        return null;
    }

    private static String getTypeName(ManagedDestination dest) {
        final StringBuilder sb = new StringBuilder();
        if (dest.isQueue()) {
            sb.append("Queue");
        } else if (dest.isTopic()) {
            sb.append("Topic");
        } else {
            sb.append("Unknown");
        }
        if (dest.isTemporary()) {
            sb.append("(temp)");
        }
        return sb.toString();
    }


}
