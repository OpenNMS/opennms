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
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationGraphProvider implements GraphProvider, EventListener {

    static final String TOPOLOGY_NAMESPACE = "application";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGraphProvider.class);

    private Graph graph;

    private ApplicationDao applicationDao;

    private boolean initialized = false;

    public ApplicationGraphProvider(ApplicationDao applicationDao) {
        Objects.requireNonNull(applicationDao);
        this.applicationDao = applicationDao;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    // TODO MVR We may need some kind of caching strategy implementation allowing each provider to deal with reloads individually if they so choose
    @Override
    public Graph<?, ?> loadGraph() {
        // TODO MVR this is not thread safe
        if (!initialized) {
            graph = createGraph();
            initialized = true;
        }
        return graph;
    }

    @Override
    public GraphInfo<?> getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(TOPOLOGY_NAMESPACE, SimpleVertex.class);
        graphInfo.setLabel("Application Graph");
        graphInfo.setDescription("This Topology Provider displays all defined Applications and their calculated states.");
        return graphInfo;
    }

    Graph<?, ?> createGraph() {
        final SimpleGraph graph = new SimpleGraph(getGraphInfo());


        for (OnmsApplication application : applicationDao.findAll()) {
            ApplicationVertex applicationVertex = new ApplicationVertex(application);
            graph.addVertex(applicationVertex);

            for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                final ApplicationVertex serviceVertex = new ApplicationVertex(eachMonitoredService);
                applicationVertex.addChildren(serviceVertex);
                graph.addVertex(applicationVertex);

                // connect with application
                String id = String.format("connection:%s:%s", applicationVertex.getId(), serviceVertex.getId());
                SimpleEdge edge = new SimpleEdge(applicationVertex, serviceVertex);
                graph.addEdge(edge);
            }
        }
        return graph;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onEvent(Event e) {
        // BSM has been reloaded, force reload
        // TODO: patrick find out if we need to listen to an event and for which one?
//        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)) {
//            String daemonName = EventUtils.getParm(e, EventConstants.PARM_DAEMON_NAME);
//            if (daemonName != null && "bsmd".equalsIgnoreCase(daemonName)) {
//                graph = createGraph();
//            }
//        }
    }
}
