/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.application;

import java.util.Objects;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: patrick use in ApplicationTopologyProvider
public class ApplicationGraphProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "application";
    static final String GRAPH_LABEL = "Application Graph";
    static final String GRAPH_DESCRIPTION = "This Topology Provider displays all defined Applications and their calculated states.";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGraphProvider.class);

    private ApplicationDao applicationDao;

    public ApplicationGraphProvider(ApplicationDao applicationDao) {
        Objects.requireNonNull(applicationDao);
        this.applicationDao = applicationDao;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    @Override
    public GraphInfo<?> getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(TOPOLOGY_NAMESPACE, ApplicationVertex.class);
        graphInfo.setLabel(GRAPH_LABEL);
        graphInfo.setDescription(GRAPH_DESCRIPTION);
        return graphInfo;
    }

    @Override
    public Graph<?, ?> loadGraph() {
        final ApplicationGraph graph = new ApplicationGraph(TOPOLOGY_NAMESPACE);
        graph.setLabel(GRAPH_LABEL);
        graph.setDescription(GRAPH_DESCRIPTION);


        for (OnmsApplication application : applicationDao.findAll()) {
            ApplicationVertex applicationVertex = new ApplicationVertex(application);
            graph.addVertex(applicationVertex);

            for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                final ApplicationVertex serviceVertex = new ApplicationVertex(eachMonitoredService);
                applicationVertex.addChildren(serviceVertex);
                graph.addVertex(applicationVertex);

                // connect with application
                SimpleEdge edge = new SimpleEdge(applicationVertex, serviceVertex);
                graph.addEdge(edge);
            }
        }
        return graph;
    }
}
