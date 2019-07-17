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
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationGraphProvider implements GraphProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGraphProvider.class);
    private static final String GRAPH_LABEL = "Application Graph";
    private static final String GRAPH_DESCRIPTION = "This Topology Provider displays all defined Applications and their calculated states.";

    private final ApplicationDao applicationDao;
    private final SessionUtils sessionUtils;

    public ApplicationGraphProvider(SessionUtils sessionUtils, ApplicationDao applicationDao) {
        Objects.requireNonNull(applicationDao);
        Objects.requireNonNull(sessionUtils);
        this.applicationDao = applicationDao;
        this.sessionUtils = sessionUtils;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), ApplicationGraph.TOPOLOGY_NAMESPACE);
    }

    @Override
    public GraphInfo<?> getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(ApplicationGraph.TOPOLOGY_NAMESPACE, ApplicationVertex.class);
        graphInfo.setLabel(GRAPH_LABEL);
        graphInfo.setDescription(GRAPH_DESCRIPTION);
        return graphInfo;
    }

    @Override
    public Graph<ApplicationVertex, SimpleEdge> loadGraph() {
        return sessionUtils.withReadOnlyTransaction(() -> {
            final ApplicationGraph graph = ApplicationGraph.builder()
                    .label(GRAPH_LABEL)
                    .description(GRAPH_DESCRIPTION)
                    .build();

            for (OnmsApplication application : applicationDao.findAll()) {
                final ApplicationVertex applicationVertex = ApplicationVertex.builder()
                        .application(application)
                        .build();
                graph.addVertex(applicationVertex);

                for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                    final ApplicationVertex serviceVertex = ApplicationVertex.builder().service(eachMonitoredService).build();
                    graph.addVertex(serviceVertex);

                    // connect with application
                    final SimpleEdge edge = SimpleEdge.builder()
                            .namespace(ApplicationGraph.TOPOLOGY_NAMESPACE)
                            .source(applicationVertex.getVertexRef())
                            .target(serviceVertex.getVertexRef())
                            .build();
                    graph.addEdge(edge);
                }
            }
            return graph;
        });
    }
}
