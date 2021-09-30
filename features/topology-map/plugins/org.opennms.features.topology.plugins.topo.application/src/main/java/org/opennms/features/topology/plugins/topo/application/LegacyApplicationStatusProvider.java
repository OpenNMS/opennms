/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.MonitoredServiceStatusEntity;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Collections2;

public class LegacyApplicationStatusProvider implements StatusProvider {

    private final ApplicationDao applicationDao;

    public LegacyApplicationStatusProvider(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public Map<VertexRef, Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
        Map<VertexRef, Status> returnMap = new HashMap<>();
        Map<String, Status> statusMap = new HashMap<>();

        List<MonitoredServiceStatusEntity> statusEntities = applicationDao.getAlarmStatus();
        for (MonitoredServiceStatusEntity entity : statusEntities) {
            DefaultStatus status = createStatus(entity.getSeverity(), entity.getCount());
            statusMap.put(toId(entity.getNodeId(), entity.getIpAddress().toString(), entity.getServiceTypeId()), status);
        }

        // status for all known node ids
        Collection<VertexRef> vertexRefsForNamespace = getVertexRefsForNamespace(vertices);
        Collection<VertexRef> vertexRefsRoot = getRootElements(vertexRefsForNamespace);
        Collection<VertexRef> vertexRefs = new ArrayList<>(vertexRefsForNamespace);
        vertexRefs.removeAll(vertexRefsRoot);

        // calculate status for children
        for (VertexRef eachVertex : vertexRefs) {
            LegacyApplicationVertex applicationVertex = (LegacyApplicationVertex) eachVertex;
            Status alarmStatus = statusMap.get(toId(applicationVertex));
            if (alarmStatus == null) {
                alarmStatus = createStatus(OnmsSeverity.NORMAL, 0);
            }
            returnMap.put(eachVertex, alarmStatus);
        }

        // calculate status for root
        for (VertexRef eachRoot : vertexRefsRoot) {
            LegacyApplicationVertex eachRootApplication = (LegacyApplicationVertex) eachRoot;
            OnmsSeverity maxSeverity = OnmsSeverity.NORMAL;
            int count = 0;
            boolean allChildrenHaveActiveAlarms = eachRootApplication.getChildren().size() > 0 ;
            for (VertexRef eachChild : eachRootApplication.getChildren()) {
                LegacyApplicationVertex eachChildApplication = (LegacyApplicationVertex) eachChild;
                Status childStatus = statusMap.get(toId(eachChildApplication));
                Optional<OnmsSeverity> childSeverity = Optional.ofNullable(childStatus)
                        .map(Status::computeStatus)
                        .map(this::createSeverity);
                if (childSeverity.isPresent() && maxSeverity.isLessThan(childSeverity.get())) {
                    maxSeverity = createSeverity(childStatus.computeStatus());
                } else if(!childSeverity.isPresent() || OnmsSeverity.NORMAL.equals(childSeverity.get())) {
                    allChildrenHaveActiveAlarms = false; // at least one child has no active alarm
                }

                if(childStatus != null) {
                    count = count + Integer.parseInt(childStatus.getStatusProperties().get("statusCount"));
                }
            }

            if (allChildrenHaveActiveAlarms && maxSeverity.isLessThan(OnmsSeverity.MAJOR)) {
                maxSeverity = OnmsSeverity.MAJOR;
            }

            returnMap.put(eachRoot, createStatus(maxSeverity, count));
        }

        return returnMap;
    }

    private String toId(LegacyApplicationVertex vertex) {
        return toId(vertex.getNodeID(), vertex.getIpAddress(), vertex.getServiceTypeId());
    }

    private String toId(int nodeId, String ipAddress, int serviceTypeId) {
        return nodeId + "-" + ipAddress + "-" +serviceTypeId;
    }

    private OnmsSeverity createSeverity(String label) {
        for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
            if (label.equalsIgnoreCase(eachSeverity.name())) {
                return eachSeverity;
            }
        }
        return null;
    }

    @Override
    public String getNamespace() {
        return LegacyApplicationTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && getNamespace().equals(namespace);
    }

    private Collection<VertexRef> getRootElements(Collection<VertexRef> vertices) {
        return Collections2.filter(vertices, input -> ((LegacyApplicationVertex) input).isRoot());
    }

    private List<VertexRef> getVertexRefsForNamespace(Collection<VertexRef> vertices) {
        List<VertexRef> returnList = new ArrayList<>();
        for (VertexRef eachRef : vertices) {
            if (contributesTo(eachRef.getNamespace())) {
                if (!returnList.contains(eachRef)) {
                    returnList.add(eachRef);
                }
            }
        }
        return returnList;
    }

    private static DefaultStatus createStatus(OnmsSeverity severity, long count) {
        return new DefaultStatus(severity.getLabel(), count);
    }
}
