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
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ApplicationTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "application";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationTopologyProvider.class);

    private ApplicationDao applicationDao;

    public ApplicationTopologyProvider(ApplicationDao applicationDao) {
        super(new SimpleVertexProvider(TOPOLOGY_NAMESPACE), new SimpleEdgeProvider(TOPOLOGY_NAMESPACE));
        this.applicationDao = applicationDao;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    private void load() {
        resetContainer();
        for (OnmsApplication application : applicationDao.findAll()) {
            ApplicationVertex applicationVertex = new ApplicationVertex(application);
            addVertices(applicationVertex);

            for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                final ApplicationVertex serviceVertex = new ApplicationVertex(eachMonitoredService);
                applicationVertex.addChildren(serviceVertex);
                addVertices(serviceVertex);

                // connect with application
                String id = String.format("connection:%s:%s", applicationVertex.getId(), serviceVertex.getId());
                Edge edge = new AbstractEdge(getNamespace(), id, applicationVertex, serviceVertex);
                addEdges(edge);
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
                        return Lists.newArrayList(new VertexHopGraphProvider.DefaultVertexHopCriteria(new ApplicationVertex(applications.get(0))));
                    }
                    return null;
                });
    }

    @Override
    public void resetContainer() {
        super.resetContainer();
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
