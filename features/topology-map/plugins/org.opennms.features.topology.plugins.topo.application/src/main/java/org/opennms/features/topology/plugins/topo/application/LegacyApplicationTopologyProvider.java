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
package org.opennms.features.topology.plugins.topo.application;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.provider.application.ApplicationGraph;
import org.opennms.netmgt.graph.provider.application.ApplicationVertex;
import org.opennms.netmgt.graph.provider.application.ApplicationVertexType;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LegacyApplicationTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "application";

    private final GraphService graphService;

    public LegacyApplicationTopologyProvider(GraphService graphService) {
        super(TOPOLOGY_NAMESPACE);
        this.graphService = Objects.requireNonNull(graphService);
    }

    private void load() {
        graph.resetContainer();

        final GenericGraph genericGraph = graphService.getGraph(ApplicationGraph.NAMESPACE);
        final ApplicationGraph applicationGraph = new ApplicationGraph(genericGraph);
        for (ApplicationVertex eachApplicationVertex : applicationGraph.getVertices()) {
            LegacyApplicationVertex applicationVertex = new LegacyApplicationVertex(eachApplicationVertex);
            graph.addVertices(applicationVertex);
        }
        for (SimpleDomainEdge edge : applicationGraph.getEdges()) {
            final VertexRef sourceRef = new DefaultVertexRef(edge.getSource().getNamespace(), edge.getSource().getId());
            final VertexRef targetRef = new DefaultVertexRef(edge.getTarget().getNamespace(), edge.getTarget().getId());
            final String id = String.format("connection:%s:%s", edge.getSource().getId(), edge.getTarget().getId());
            final AbstractEdge convertedEdge = new AbstractEdge(edge.getNamespace(), id, sourceRef, targetRef);
            graph.addEdges(convertedEdge);
        }

        // recreate children relationship.
        // Assumption: each application can have many services, each service can have 1 application
        for (ApplicationVertex serviceVertex : applicationGraph.getVertices()) {
            if(serviceVertex.getVertexType() == ApplicationVertexType.Service) {
                // this should return exactly one edge:
                // the one connecting the service to its application
                final SimpleDomainEdge edge = applicationGraph.getConnectingEdges(serviceVertex).stream().findFirst()
                        .orElseThrow(()-> new IllegalStateException("corrupt graph, each service should be connected to an application"));
                // find the other side of the edge:
                org.opennms.netmgt.graph.api.VertexRef parentRef = Stream.of(edge.getSource(), edge.getTarget())
                        .filter(ref -> !ref.equals(serviceVertex.getVertexRef())).findFirst().get();
                LegacyApplicationVertex parent = (LegacyApplicationVertex) graph.getVertex(new DefaultVertexRef(parentRef.getNamespace(), parentRef.getId()));
                LegacyApplicationVertex child = (LegacyApplicationVertex) graph.getVertex(new DefaultVertexRef(serviceVertex.getNamespace(), serviceVertex.getId()));
                if (parent == null) {
                    throw new IllegalStateException("Parent vertex [namespace='"+ parentRef.getNamespace() +"', id='" + parentRef.getId() +"'] was not found in graph.");
                }
                if (child == null) {
                    throw new IllegalStateException("Child vertex [namespace='"+ serviceVertex.getNamespace() +"', id='" + serviceVertex.getId() +"'] was not found in graph.");
                }
                parent.addChildren(child);
            }
        }
    }

    @Override
    public void refresh() {
       load();
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withPreferredLayout("Hierarchy Layout")
                .withCriteria(() -> {
                    // Only show the first application by default
                	GenericGraph genericGraph = graphService.getGraph(ApplicationGraph.NAMESPACE);
                    ApplicationGraph applicationGraph = new ApplicationGraph(genericGraph.asGenericGraph());
                    Optional<ApplicationVertex> firstVertex = applicationGraph.getVertices().stream()
                            .filter( v -> v.getVertexType() == ApplicationVertexType.Application)
                            .findFirst();
                    if (firstVertex.isPresent()) {
                        return Lists.newArrayList(new DefaultVertexHopCriteria(new LegacyApplicationVertex(firstVertex.get())));
                    }
                    return null;
                });
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType contentType) {
        Set<LegacyApplicationVertex> filteredVertices = selectedVertices.stream()
                .filter(v -> TOPOLOGY_NAMESPACE.equals(v.getNamespace()))
                .map(v -> (LegacyApplicationVertex) v)
                .collect(Collectors.toSet());
        Set<Integer> nodeIds = extractNodeIds(filteredVertices);
        switch (contentType) {
            case Alarm:
                return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
            case Node:
                return new SelectionChangedListener.IdSelection<>(nodeIds);
            case Application:
                final Set<Integer> applicationIds = filteredVertices.stream()
                        .map(LegacyApplicationVertex::getId)
                        .map(s -> s.startsWith("Application:") ? s.substring("Application:".length()) : s)
                        .map(s -> s.startsWith("Service:") ? s.substring("Service:".length()) : s)
                        .map(Integer::valueOf)
                        .collect(Collectors.toSet());
                return new SelectionChangedListener.IdSelection<>(applicationIds);
        }
        throw new IllegalArgumentException(getClass().getSimpleName() + " does not support filtering vertices for contentType " + contentType);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return Sets.newHashSet(
                ContentType.Application,
                ContentType.Alarm,
                ContentType.Node).contains(type);
    }

    private Set<Integer> extractNodeIds(Set<LegacyApplicationVertex> applicationVertices) {
        return applicationVertices.stream()
                .filter(eachVertex -> TOPOLOGY_NAMESPACE.equals(eachVertex.getNamespace()) && eachVertex.getNodeID() != null)
                .map(LegacyApplicationVertex::getNodeID)
                .collect(Collectors.toSet());
    }
}
