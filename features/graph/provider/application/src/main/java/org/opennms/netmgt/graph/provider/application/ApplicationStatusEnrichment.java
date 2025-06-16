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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private static final StatusInfo DEFAULT_STATUS = StatusInfo.defaultStatus().build();

    private final ApplicationDao applicationDao;

    public ApplicationStatusEnrichment(final ApplicationDao applicationDao) {
        this.applicationDao = Objects.requireNonNull(applicationDao);
    }

    @Override
    public boolean canEnrich(GenericGraph graph) {
        return graph.getNamespace().equals(ApplicationGraph.NAMESPACE);
    }

    @Override
    public void enrich(final EnrichmentGraphBuilder graphBuilder) {
        // Prepare calculation
        final List<GenericVertex> allVertices = graphBuilder.getView().getVertices();
        final List<GenericVertex> rootVertices = graphBuilder.getView().getVertices().stream().map(ApplicationVertex::from).filter(v -> v.getApplicationId() != null).map(AbstractDomainVertex::asGenericVertex).collect(Collectors.toList());
        List<GenericVertex> childVertices = new ArrayList<>(allVertices);
        childVertices.removeAll(rootVertices);

        // The status for all child services (OnmsMonitoredServices)
        final Map<String, StatusInfo> childStatusMap = new HashMap<>();

        // Mapping between each vertex to its status
        final Map<GenericVertex, StatusInfo> vertexStatusMap = new HashMap<>();

        // Load applications
        final List<OnmsApplication> applications = rootVertices.stream().map(v -> ApplicationVertex.from(v).getApplicationId()).map(id -> applicationDao.get(id)).collect(Collectors.toList());

        // Get maximum alarm severities for all application (alarm status)
        final List<MonitoredServiceStatusEntity> result = applicationDao.getAlarmStatus(applications);
        for (MonitoredServiceStatusEntity eachRow : result) {
            final StatusInfo statusInfo = StatusInfo.builder(eachRow.getSeverity()).count(eachRow.getCount()).build();
            childStatusMap.put(toId(eachRow.getNodeId(), eachRow.getIpAddress().toString(), eachRow.getServiceTypeId()), statusInfo);
        }

        // The statusMap until now contains only status for all child vertices which have alarms
        // The others are now filled with NORMAL status entries
        for (GenericVertex eachVertex : childVertices) {
            childStatusMap.putIfAbsent(toId(eachVertex), DEFAULT_STATUS);
            vertexStatusMap.put(eachVertex, childStatusMap.get(toId(eachVertex)));
        }

        // The status of each Application (root vertices) is the maximum of its children or if all children have an alarm
        // a minimum of "major"
        for (GenericVertex eachRoot : rootVertices) {
            vertexStatusMap.put(eachRoot, buildStatusForApplication(eachRoot, graphBuilder, childStatusMap));
        }

        // Update vertices
        vertexStatusMap.entrySet().forEach(entry -> graphBuilder.property(entry.getKey(), EnrichedProperties.STATUS, entry.getValue()));
    }

    StatusInfo buildStatusForApplication(final GenericVertex application, final EnrichmentGraphBuilder graphBuilder, final Map<String, StatusInfo> childStatusMap) {
        Collection<GenericEdge> services = graphBuilder.getView().getConnectingEdges(application);

        boolean allChildrenHaveActiveAlarms = services.size() > 0;

        final StatusInfo.StatusInfoBuilder rootStatusBuilder = StatusInfo.from(DEFAULT_STATUS);
        // Calculate max severity
        for (GenericEdge eachEdge : services) {
            final GenericVertex serviceVertex = graphBuilder.getView().resolveVertex(eachEdge.getTarget());
            final StatusInfo childStatus = Optional.ofNullable(childStatusMap.get(toId(serviceVertex))).orElse(StatusInfo.defaultStatus().build());
            final Severity childSeverity = childStatus.getSeverity();

            // check if all children have alarms
            if(Severity.Normal.isEqual(childSeverity) || Severity.Unknown.isEqual(childSeverity)){
                allChildrenHaveActiveAlarms = false;  // at least one child has no active alarm
            }

            // check for the highest severity
            if (rootStatusBuilder.getSeverity().isLessThan(childSeverity)) {
                rootStatusBuilder.severity(childSeverity);
            }

            // sum up all alarm counts from children
            rootStatusBuilder.count(rootStatusBuilder.getCount() + childStatus.getCount());
        }

        // if all children have active alarms, the application status must be at least major
        if (allChildrenHaveActiveAlarms && rootStatusBuilder.getSeverity().isLessThan(Severity.Major)) {
            rootStatusBuilder.severity(Severity.Major);
        }
        return rootStatusBuilder.build();
    }

    static String toId(final GenericVertex vertex) {
        ApplicationVertex service = new ApplicationVertex(vertex);
        return toId(service.getNodeRef().getNodeId(), service.getIpAddress(), service.getServiceTypeId());
    }

    static String toId(final int nodeId, final String ipAddress, final int serviceTypeId) {
        return nodeId + "-" + ipAddress + "-" +serviceTypeId;
    }
}
