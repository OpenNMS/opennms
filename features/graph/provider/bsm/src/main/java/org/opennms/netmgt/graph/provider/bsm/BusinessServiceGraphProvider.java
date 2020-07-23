/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.bsm;

import static org.opennms.netmgt.graph.provider.bsm.BusinessServiceVertex.BusinessServiceVertexBuilder;
import static org.opennms.netmgt.graph.provider.bsm.BusinessServiceVertex.builder;

import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerCache;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.graph.provider.bsm.BusinessServiceGraph.BusinessServiceGraphBuilder;
import org.opennms.netmgt.model.events.EventUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class BusinessServiceGraphProvider implements GraphProvider, EventListener {

    // The UEIs this listener is interested in
    private static final List<String> UEI_LIST = Lists.newArrayList(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI);

    private final EventIpcManager eventIpcManager;

    private final BusinessServiceManager businessServiceManager;

    private final GraphContainerCache graphContainerCache;

    public BusinessServiceGraphProvider(GraphContainerCache graphContainerCache, BusinessServiceManager businessServiceManager, EventIpcManager eventIpcManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
        this.graphContainerCache = Objects.requireNonNull(graphContainerCache);
    }

    @Override
    public ImmutableGraph<?, ?> loadGraph() {
        final BusinessServiceGraphBuilder bsmGraph = BusinessServiceGraph.builder().graphInfo(getGraphInfo());
        final org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph sourceGraph = businessServiceManager.getGraph();
        for (GraphVertex topLevelBusinessService : sourceGraph.getVerticesByLevel(0)) {
            addVertex(bsmGraph, sourceGraph, topLevelBusinessService, null);
        }
        // Apply Focus
        final VertexRef defaultFocusVertex = getDefaultFocusVertex(bsmGraph);
        if (defaultFocusVertex != null) {
            bsmGraph.focus().selection(defaultFocusVertex).apply();
        } else {
            bsmGraph.focus().selection(Lists.newArrayList()).apply();
        }
        return bsmGraph.build();
    }

    @Override
    public GraphInfo getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(BusinessServiceGraph.NAMESPACE);
        graphInfo.setLabel("Business Service Graph"); // Business Services
        graphInfo.setDescription("Displays the hierarchy of the defined Business Services and their computed operational states.");
        return graphInfo;
    }

    private VertexRef getDefaultFocusVertex(BusinessServiceGraphBuilder graphBuilder) {
        // Grab the business service with the smallest id
        final List<BusinessService> businessServices = businessServiceManager.findMatching(new CriteriaBuilder(BusinessService.class).orderBy("id", true).limit(1).toCriteria());

        // If one was found, use it for the default focus
        if (!businessServices.isEmpty()) {
            final BusinessService businessService = businessServices.iterator().next();
            final String vertexId = BusinessServiceVertex.Type.BusinessService + ":" + businessService.getId();
            final VertexRef vertexRef = graphBuilder.getVertexRef(vertexId);
            return vertexRef;
        }
        return null;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onEvent(IEvent e) {
        // BSM has been reloaded, force reload
        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)) {
            String daemonName = EventUtils.getParm(e, EventConstants.PARM_DAEMON_NAME);
            if (daemonName != null && "bsmd".equalsIgnoreCase(daemonName)) {
                graphContainerCache.invalidate(BusinessServiceGraph.NAMESPACE);
            }
        }
    }

    public void init() {
        eventIpcManager.addEventListener(this, UEI_LIST);
    }

    public void destroy() {
        eventIpcManager.removeEventListener(this, UEI_LIST);
    }

    private static void addVertex(BusinessServiceGraphBuilder targetGraphBuilder, org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph sourceGraph, GraphVertex graphVertex, BusinessServiceVertex topologyVertex) {
        if (topologyVertex == null) {
            // Create a topology vertex for the current vertex
            topologyVertex = builder().graphVertex(graphVertex).build();
            targetGraphBuilder.addVertex(topologyVertex);
        }

        for (GraphEdge graphEdge : sourceGraph.getOutEdges(graphVertex)) {
            final GraphVertex childVertex = sourceGraph.getOpposite(graphVertex, graphEdge);

            // Create a topology vertex for the child vertex
            final BusinessServiceVertexBuilder childTopologyVertexBuilder = builder().graphVertex(childVertex);
            sourceGraph.getInEdges(childVertex).stream()
                    .map(GraphEdge::getFriendlyName)
                    .filter(s -> !Strings.isNullOrEmpty(s))
                    .findFirst()
                    .ifPresent(childTopologyVertexBuilder::label);
            final BusinessServiceVertex childTopologyVertex = childTopologyVertexBuilder.build();
            targetGraphBuilder.addVertex(childTopologyVertex);

            // Connect the two
            final BusinessServiceEdge edge = BusinessServiceEdge.builder()
                    .graphEdge(graphEdge)
                    .source(topologyVertex.getVertexRef())
                    .target(childTopologyVertex.getVertexRef()).build();
            targetGraphBuilder.addEdge(edge);

            // Recurse
            addVertex(targetGraphBuilder, sourceGraph, childVertex, childTopologyVertex);
        }
    }
}
