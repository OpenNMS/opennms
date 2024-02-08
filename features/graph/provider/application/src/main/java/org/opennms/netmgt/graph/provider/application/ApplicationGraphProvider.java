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
