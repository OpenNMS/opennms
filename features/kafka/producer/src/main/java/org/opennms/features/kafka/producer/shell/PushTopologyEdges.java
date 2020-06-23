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

package org.opennms.features.kafka.producer.shell;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyConsumer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@Command(scope = "kafka-producer", name = "push-topology-edges", description = "Pushes all of the related topology edges to the configured topic.")
@Service
public class PushTopologyEdges implements Action {

    @Option(name = "-p", aliases = "--protocol", description = "Protocol", multiValued = true)
    private List<String> protocols;

    @Reference
    private OnmsTopologyDao onmsTopologyDao;

    @Reference
    private BundleContext bundleContext;

    @Override
    public Object execute() throws InvalidSyntaxException {
        // Grab a reference to the OnmsTopologyConsumer interface exposed by the OpennmsKafkaProducer
        final ServiceReference<OnmsTopologyConsumer> serviceRef = bundleContext.getServiceReferences(OnmsTopologyConsumer.class, "(type=kafkaProducer)").stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find reference to OnmsTopologyConsumer service exposed by the OpennmsKafkaProducer."));
        final OnmsTopologyConsumer consumer = bundleContext.getService(serviceRef);

        System.out.println("Retrieving topologies...");
        final Map<OnmsTopologyProtocol, OnmsTopology> topologies;
        if (protocols == null || protocols.isEmpty()) {
            // No protocols set, retrieve them all
            topologies =  onmsTopologyDao.getTopologies();
        } else {
            // One or more protocols set, retrieve them in and process them in the order specified
            topologies = new LinkedHashMap<>();
            for (String protocol : protocols) {
                topologies.put(OnmsTopologyProtocol.create(protocol), onmsTopologyDao.getTopology(protocol));
            }
        }

        System.out.printf("Retrieved %d topologies.\n", topologies.size());
        for (Map.Entry<OnmsTopologyProtocol, OnmsTopology> entry : topologies.entrySet()) {
            final OnmsTopologyProtocol protocol = entry.getKey();
            final String protocolName = protocol.getId().toUpperCase();
            final OnmsTopology topology = entry.getValue();

            final Set<OnmsTopologyVertex> topologyVertices= topology.getVertices();
            final Set<OnmsTopologyEdge> topologyEdges = topology.getEdges();
            System.out.printf("%s: Pushing %d vertices and %d edges.\n",
                    protocolName, topologyVertices.size(), topologyEdges.size());

            int numVerticesPushed = 0;
            for (OnmsTopologyVertex vertex : topology.getVertices()) {
                consumer.consume(OnmsTopologyMessage.update(vertex, protocol));
                // Progress tracking
                numVerticesPushed++;
                if (numVerticesPushed > 0 && numVerticesPushed % 100 == 0) {
                    System.out.printf("%s: Pushed %d vertices.\n", protocolName, numVerticesPushed);
                }
                if (Thread.interrupted()) {
                    System.out.println("Interrupted. Aborting.");
                }
            }

            int numEdgesPushed = 0;
            for (OnmsTopologyEdge edge : topology.getEdges()) {
                consumer.consume(OnmsTopologyMessage.update(edge, protocol));
                // Progress tracking
                numEdgesPushed++;
                if (numEdgesPushed > 0 && numEdgesPushed % 100 == 0) {
                    System.out.printf("%s: Pushed %d edges.\n", protocolName, numEdgesPushed);
                }
                if (Thread.interrupted()) {
                    System.out.println("Interrupted. Aborting.");
                }
            }
        }
        System.out.println("Done.");
        return null;
    }

}
