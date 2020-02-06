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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.MonitoredServiceStatusEntity;
import org.opennms.netmgt.graph.api.enrichment.EnrichedProperties;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentGraphBuilder;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.Severity;
import org.opennms.netmgt.graph.api.info.StatusInfo;
import org.opennms.netmgt.graph.domain.AbstractDomainVertex;
import org.opennms.netmgt.model.OnmsApplication;

/**
 * Calculates the status of the application and its children.
 */
public class ApplicationStatusEnrichment implements EnrichmentProcessor {

    private static final StatusInfo DEFAULT_STATUS = StatusInfo.builder(Severity.Normal).build();

    private final ApplicationDao applicationDao;

    public ApplicationStatusEnrichment(final ApplicationDao applicationDao) {
        this.applicationDao = Objects.requireNonNull(applicationDao);
    }

    @Override
    public boolean canEnrich(GenericGraph graph) {
        return graph.getNamespace().equals(ApplicationGraph.NAMESPACE);
    }

    @Override
    public void enrich(EnrichmentGraphBuilder graphBuilder) {
        // Prepare calculation
        final List<GenericVertex> allVertices = graphBuilder.getView().getVertices();
        final List<GenericVertex> rootVertices = graphBuilder.getView().getVertices().stream().map(ApplicationVertex::from).filter(v -> v.getApplicationId() != null).map(AbstractDomainVertex::asGenericVertex).collect(Collectors.toList());
        List<GenericVertex> childVertices = new ArrayList<>(allVertices);
        childVertices.removeAll(rootVertices);

        // The status for all child services (OnmsMonitoredServices)
        final Map<Integer, StatusInfo> childStatusMap = new HashMap<>();

        // Mapping between each vertex to its status
        final Map<GenericVertex, StatusInfo> vertexStatusMap = new HashMap<>();

        // Load applications
        final List<OnmsApplication> applications = rootVertices.stream().map(v -> ApplicationVertex.from(v).getApplicationId()).map(id -> applicationDao.get(id)).collect(Collectors.toList());

        // Get maximum alarm severities for all application (alarm status)
        final List<MonitoredServiceStatusEntity> result = applicationDao.getAlarmStatus(applications);
        for (MonitoredServiceStatusEntity eachRow : result) {
            final StatusInfo statusInfo = StatusInfo.builder(eachRow.getSeverity()).count(eachRow.getCount()).build();
            childStatusMap.put(eachRow.getNodeId(), statusInfo);
        }

        // The statusMap until now contains only status for all child vertices which have alarms
        // The others are now filled with NORMAL status entries
        for (GenericVertex eachVertex : childVertices) {
            final int nodeId = eachVertex.getNodeRef().getNodeId();
            childStatusMap.putIfAbsent(nodeId, DEFAULT_STATUS);
            vertexStatusMap.put(eachVertex, childStatusMap.get(nodeId));
        }

        // The status of each Application (root vertices) is the maximum of its children
        // calculate status for root
        for (GenericVertex eachRoot : rootVertices) {
            final StatusInfo.StatusInfoBuilder rootStatusBuilder = StatusInfo.from(DEFAULT_STATUS);
            // Calculate max severity
            for (GenericEdge eachEdge : graphBuilder.getView().getConnectingEdges(eachRoot)) {
                final GenericVertex serviceVertex = graphBuilder.getView().resolveVertex(eachEdge.getTarget());
                final StatusInfo childStatus = childStatusMap.get(serviceVertex.getNodeRef().getNodeId());
                if (rootStatusBuilder.getSeverity().isLessThan(childStatus.getSeverity())) {
                    rootStatusBuilder.severity(childStatus.getSeverity()).count(childStatus.getCount());
                } else if (rootStatusBuilder.getSeverity().isEqual(childStatus.getSeverity())) {
                    rootStatusBuilder.count(rootStatusBuilder.getCount() + childStatus.getCount());
                }
            }
            vertexStatusMap.put(eachRoot, rootStatusBuilder.build());
        }

        // Update vertices
        vertexStatusMap.entrySet().forEach(entry -> graphBuilder.property(entry.getKey(), EnrichedProperties.STATUS, entry.getValue()));
    }
}
