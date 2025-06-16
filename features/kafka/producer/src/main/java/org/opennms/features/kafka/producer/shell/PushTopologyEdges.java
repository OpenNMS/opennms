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

@Command(scope = "opennms", name = "kafka-push-topology-edges", description = "Pushes all of the related topology edges to the configured topic.")
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
