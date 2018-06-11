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

package org.opennms.netmgt.collection.commands;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.InvalidCollectionAgentException;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.InetAddrUtils;

@Command(scope = "collection", name = "collect", description="Invokes a collector against a host at a specified location.")
@Service
public class CollectCommand implements Action {

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String systemId;

    @Option(name = "-t", aliases = "--ttl", description = "Time to live in milliseconds", required = false, multiValued = false)
    Long ttlInMs;

    @Option(name = "-n", aliases = "--node", description = "Node ID or FS:FID", required = false, multiValued = false)
    String nodeCriteria;

    @Argument(index = 0, name = "collectorClass", description = "Collector class", required = true, multiValued = false)
    @Completion(CollectorClassNameCompleter.class)
    String className;

    @Argument(index = 1, name = "host", description = "Hostname or IP Address of the system to poll", required = true, multiValued = false)
    String host;

    @Argument(index = 2, name = "attributes", description = "Collector specific attributes in key=value form", multiValued = true)
    List<String> attributes;

    @Reference
    public ServiceCollectorRegistry serviceCollectorRegistry;

    @Reference
    public LocationAwareCollectorClient locationAwareCollectorClient;

    @Reference
    public CollectionAgentFactory collectionAgentFactory;

    @Override
    public Void execute() {
        final ServiceCollector collector = serviceCollectorRegistry.getCollectorByClassName(className);
        if (collector == null) {
            System.out.printf("No collector found with class name '%s'. Aborting.\n", className);
            return null;
        }

        try {
            // The collector may not have been initialized - initialize it
            collector.initialize();
        } catch (CollectionInitializationException e) {
            System.out.println("Failed to initialize the collector. Aborting.");
            e.printStackTrace();
            return null;
        }

        final CollectionAgent agent = getCollectionAgent();
        final CompletableFuture<CollectionSet> future = locationAwareCollectorClient.collect()
                .withAgent(agent)
                .withSystemId(systemId)
                .withCollector(collector)
                .withTimeToLive(ttlInMs)
                .withAttributes(parse(attributes))
                .execute();

        while (true) {
            try {
                try {
                    CollectionSet collectionSet = future.get(1, TimeUnit.SECONDS);
                    if (CollectionStatus.SUCCEEDED.equals(collectionSet.getStatus())) {
                        printCollectionSet(collectionSet);
                    } else {
                        System.out.printf("\nThe collector returned a collection set with status: %s\n", collectionSet.getStatus());
                    }
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                } catch (ExecutionException e) {
                    final Throwable cause = e.getCause();
                    if (cause != null && cause instanceof InvalidCollectionAgentException) {
                        System.out.printf("The collector requires a valid node and interface. Try specifying a valid node using the --node option.\n", e);
                        break;
                    }
                    System.out.printf("\nCollect failed with:", e);
                    e.printStackTrace();
                    System.out.println();
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

    private CollectionAgent getCollectionAgent() {
        final InetAddress hostAddr = InetAddrUtils.addr(host);
        if (nodeCriteria != null) {
            return collectionAgentFactory.createCollectionAgentAndOverrideLocation(nodeCriteria, hostAddr, location);
        } else {
            System.out.println("NOTE: Some collectors require a database node and IP interface.\n");
            final CollectionAgentDTO agent = new CollectionAgentDTO();
            agent.setLocationName(location);
            agent.setAddress(hostAddr);
            agent.setStorageResourcePath(ResourcePath.fromString(""));
            return agent;
        }
    }

    private static void printCollectionSet(CollectionSet collectionSet) {
        AtomicBoolean didPrintAttribute = new AtomicBoolean(false);
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitResource(CollectionResource resource) {
                System.out.printf("%s\n", resource);
            }

            @Override
            public void visitGroup(AttributeGroup group) {
                System.out.printf("\tGroup: %s\n", group.getName());
            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                System.out.printf("\t\t%s\n", attribute);
                didPrintAttribute.set(true);
            }
        });

        if (!didPrintAttribute.get()) {
            System.out.println("(Empty collection set)");
        }
    }

    private static Map<String, Object> parse(List<String> attributeList) {
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
