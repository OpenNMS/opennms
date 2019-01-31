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

package org.opennms.features.graph.providers.bsm;

import java.util.List;
import java.util.Objects;

import org.opennms.features.graph.api.GraphContainer;
import org.opennms.features.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.features.graph.api.info.DefaultGraphInfo;
import org.opennms.features.graph.api.info.GraphContainerInfo;
import org.opennms.features.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennsm.features.graphs.simple.SimpleEdge;
import org.opennsm.features.graphs.simple.SimpleGraph;
import org.opennsm.features.graphs.simple.SimpleGraphContainer;
import org.opennsm.features.graphs.simple.SimpleVertex;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

// TODO MVR this looks like a copy of BusinessServiceTopologyProvider
public class BusinessServiceGraphContainerProvider implements GraphContainerProvider, EventListener {

    protected static final String NAMESPACE = "bsm";

    // The UEIs this listener is interested in
    private static final List<String> UEI_LIST = Lists.newArrayList(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI);

    private final EventIpcManager eventIpcManager;

    private final BusinessServiceManager businessServiceManager;

    private boolean initialized = false;

    private GraphContainer graphContainer;

    public BusinessServiceGraphContainerProvider(BusinessServiceManager businessServiceManager, EventIpcManager eventIpcManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
    }

    // TODO MVR We may need some kind of caching strategyg implemenetation allowing each provider to deal with reloads individually if they so choose
    @Override
    public GraphContainer loadGraphContainer() {
        // TODO MVR this is not thread safe
        if (!initialized) {
            graphContainer = createContainer();
            initialized = true;
        }
        return graphContainer;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(NAMESPACE);
        containerInfo.setLabel("BSM Topology Provider"); // Business Services
        containerInfo.setDescription("This Topology Provider displays the hierarchy of the defined Business Services and their computed operational states.");

        final DefaultGraphInfo defaultGraphInfo = new DefaultGraphInfo(containerInfo.getId(), SimpleVertex.class);
        defaultGraphInfo.setLabel(containerInfo.getLabel());
        defaultGraphInfo.setDescription(containerInfo.getDescription());

        containerInfo.getGraphInfos().add(defaultGraphInfo);
        return containerInfo;
    }

    private GraphContainer createContainer() {
        final SimpleGraphContainer simpleGraphContainer = new SimpleGraphContainer(getContainerInfo());
        final SimpleGraph targetGraph = new SimpleGraph(NAMESPACE);
        targetGraph.setLabel(simpleGraphContainer.getLabel());
        targetGraph.setDescription(simpleGraphContainer.getDescription());
        final BusinessServiceGraph sourceGraph = businessServiceManager.getGraph();
        for (GraphVertex topLevelBusinessService : sourceGraph.getVerticesByLevel(0)) {
            addVertex(targetGraph, sourceGraph, topLevelBusinessService, null);
        }
        simpleGraphContainer.addGraph(targetGraph);
        return simpleGraphContainer;
    }

    private void addVertex(SimpleGraph targetGraph, BusinessServiceGraph sourceGraph, GraphVertex graphVertex, AbstractBusinessServiceVertex topologyVertex) {
        if (topologyVertex == null) {
            // Create a topology vertex for the current vertex
            topologyVertex = createTopologyVertex(graphVertex);
            targetGraph.addVertex(topologyVertex);
        }

        for (GraphEdge graphEdge : sourceGraph.getOutEdges(graphVertex)) {
            GraphVertex childVertex = sourceGraph.getOpposite(graphVertex, graphEdge);

            // Create a topology vertex for the child vertex
            AbstractBusinessServiceVertex childTopologyVertex = createTopologyVertex(childVertex);
            sourceGraph.getInEdges(childVertex).stream()
                    .map(GraphEdge::getFriendlyName)
                    .filter(s -> !Strings.isNullOrEmpty(s))
                    .findFirst()
                    .ifPresent(childTopologyVertex::setLabel);

            targetGraph.addVertex(childTopologyVertex);

            // Connect the two
            final SimpleEdge edge = new BusinessServiceEdge(graphEdge, topologyVertex, childTopologyVertex);
            targetGraph.addEdge(edge);

            // Recurse
            addVertex(targetGraph, sourceGraph, childVertex, childTopologyVertex);
        }
    }

    private AbstractBusinessServiceVertex createTopologyVertex(GraphVertex graphVertex) {
        return GraphVertexToTopologyVertexConverter.createTopologyVertex(graphVertex);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onEvent(Event e) {
        // BSM has been reloaded, force reload
        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)) {
            String daemonName = EventUtils.getParm(e, EventConstants.PARM_DAEMON_NAME);
            if (daemonName != null && "bsmd".equalsIgnoreCase(daemonName)) {
                graphContainer = createContainer();
            }
        }
    }

    public void init() {
        eventIpcManager.addEventListener(this, UEI_LIST);
    }

    public void destroy() {
        eventIpcManager.removeEventListener(this, UEI_LIST);
    }
}
