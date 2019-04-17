/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.application;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.provider.application.ApplicationGraphProvider;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ApplicationTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "application";

    private ApplicationDao applicationDao;
    private GraphService graphService;

    public ApplicationTopologyProvider(ApplicationDao applicationDao) {
        super(TOPOLOGY_NAMESPACE);
        this.applicationDao = Objects.requireNonNull(applicationDao);
    }

    private void load() {
        graph.resetContainer();
        // TODO: Patrick: discuss with mvr: why would we transfer one graph into another? I am missing something here?
        // my assumption was that we use the graph coming from the graph service directly...

        // TODO: Patrick: will be generic later
        Graph applicationGraph = graphService.getGraph(ApplicationGraphProvider.TOPOLOGY_NAMESPACE);

        for (OnmsApplication application : applicationDao.findAll()) {
            final ApplicationVertex applicationVertex = new ApplicationVertex(application);
            graph.addVertices(applicationVertex);

            for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                final ApplicationVertex serviceVertex = new ApplicationVertex(eachMonitoredService);
                applicationVertex.addChildren(serviceVertex);
                graph.addVertices(serviceVertex);

                // connect with application
                final String id = String.format("connection:%s:%s", applicationVertex.getId(), serviceVertex.getId());
                final Edge edge = new AbstractEdge(getNamespace(), id, applicationVertex, serviceVertex);
                graph.addEdges(edge);
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
                    List<OnmsApplication> applications = applicationDao.findAll();
                    if (!applications.isEmpty()) {
                        return Lists.newArrayList(new DefaultVertexHopCriteria(new ApplicationVertex(applications.get(0))));
                    }
                    return null;
                });
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType contentType) {
        Set<ApplicationVertex> filteredVertices = selectedVertices.stream()
                .filter(v -> TOPOLOGY_NAMESPACE.equals(v.getNamespace()))
                .map(v -> (ApplicationVertex) v)
                .collect(Collectors.toSet());
        Set<Integer> nodeIds = extractNodeIds(filteredVertices);
        switch (contentType) {
            case Alarm:
                return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
            case Node:
                return new SelectionChangedListener.IdSelection<>(nodeIds);
            case Application:
                final Set<Integer> applicationIds = filteredVertices.stream()
                        .filter(ApplicationVertex::isRoot)
                        .map(ApplicationVertex::getId)
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

    private Set<Integer> extractNodeIds(Set<ApplicationVertex> applicationVertices) {
        return applicationVertices.stream()
                .filter(eachVertex -> TOPOLOGY_NAMESPACE.equals(eachVertex.getNamespace()) && eachVertex.getNodeID() != null)
                .map(ApplicationVertex::getNodeID)
                .collect(Collectors.toSet());
    }
}
