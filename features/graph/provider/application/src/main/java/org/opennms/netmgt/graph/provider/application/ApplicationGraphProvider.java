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
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.graph.provider.application.ApplicationGraph.ApplicationGraphBuilder;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class ApplicationGraphProvider implements GraphProvider {

    private static final String GRAPH_LABEL = "Application Graph";
    private static final String GRAPH_DESCRIPTION = "Displays all defined applications and their calculated states.";

    private final ApplicationDao applicationDao;
    private final SessionUtils sessionUtils;

    public ApplicationGraphProvider(SessionUtils sessionUtils, ApplicationDao applicationDao) {
        Objects.requireNonNull(applicationDao);
        Objects.requireNonNull(sessionUtils);
        this.applicationDao = applicationDao;
        this.sessionUtils = sessionUtils;
    }

    @Override
    public GraphInfo getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(ApplicationGraph.NAMESPACE);
        graphInfo.setLabel(GRAPH_LABEL);
        graphInfo.setDescription(GRAPH_DESCRIPTION);
        return graphInfo;
    }

    @Override
    public ImmutableGraph<ApplicationVertex, SimpleDomainEdge> loadGraph() {
        return sessionUtils.withReadOnlyTransaction(() -> {
            final ApplicationGraphBuilder graphBuilder = ApplicationGraph.builder()
                    .label(GRAPH_LABEL)
                    .description(GRAPH_DESCRIPTION);

            for (OnmsApplication application : applicationDao.findAll()) {
                final ApplicationVertex applicationVertex = ApplicationVertex.builder()
                        .application(application)
                        .build();
                graphBuilder.addVertex(applicationVertex);

                for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                    final ApplicationVertex serviceVertex = ApplicationVertex.builder().service(eachMonitoredService).build();
                    graphBuilder.addVertex(serviceVertex);

                    // connect with application
                    final SimpleDomainEdge edge = SimpleDomainEdge.builder()
                            .namespace(ApplicationGraph.NAMESPACE)
                            .source(applicationVertex.getVertexRef())
                            .target(serviceVertex.getVertexRef())
                            .build();
                    graphBuilder.addEdge(edge);
                }
            }
            return graphBuilder.build();
        });
    }
}
