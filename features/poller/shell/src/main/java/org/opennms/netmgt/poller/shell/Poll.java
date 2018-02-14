/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.shell;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;

@Command(scope = "poller", name = "poll", description = "Used to invoke a monitor against a host at a specified location")
@Service
public class Poll implements Action {

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String systemId;

    @Option(name = "-t", aliases = "--ttl", description = "Time to live", required = false, multiValued = false)
    Long ttlInMs;

    @Argument(index = 0, name = "monitorClass", description = "Monitor class", required = true, multiValued = false)
    @Completion(MonitorClassNameCompleter.class)
    String className;

    @Argument(index = 1, name = "host", description = "Hostname or IP Address of the system to poll", required = true, multiValued = false)
    String host;

    @Argument(index = 2, name = "attributes", description = "Monitor specific attributes in key=value form", multiValued = true)
    List<String> attributes;

    @Reference
    public LocationAwarePollerClient locationAwarePollerClient;

    @Override
    public Object execute() throws Exception {
        final CompletableFuture<PollerResponse> future = locationAwarePollerClient.poll()
                .withService(new SimpleMonitoredService(InetAddress.getByName(host), "SVC", location))
                .withSystemId(systemId)
                .withMonitorClassName(className)
                .withTimeToLive(ttlInMs)
                .withAttributes(parse(attributes))
                .execute();

        while (true) {
            try {
                try {
                    PollStatus pollStatus = future.get(1, TimeUnit.SECONDS).getPollStatus();
                    if (pollStatus.getStatusCode() == PollStatus.SERVICE_AVAILABLE) {
                        System.out.printf("\nService is %s on %s using %s:\n", pollStatus.getStatusName(), host, className);
                        final Map<String, Number> properties = pollStatus.getProperties();
                        if (properties.size() > 0) {
                            properties.entrySet().stream().forEach(e -> {
                                System.out.printf("\t%s: %.4f\n", e.getKey(),
                                        e.getValue() != null ? e.getValue().doubleValue() : null);
                            });
                        } else {
                            System.out.printf("(No properties were returned by the monitor.\n");
                        }
                    } else {
                        System.out.printf("\nService is %s on %s using %s\n", pollStatus.getStatusName(), host, className);
                        System.out.printf("\tReason: %s\n", pollStatus.getReason());
                    }
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                } catch (ExecutionException e) {
                    System.out.printf("\nPoll failed with: %s\n", e);
                }
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
            System.out.flush();
        }
        return null;
    }

    protected static Map<String, Object> parse(List<String> attributeList) {
        final Map<String, Object> properties = new HashMap<>();
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property " + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1, keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }

}
