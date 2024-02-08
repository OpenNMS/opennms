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
                if (childStatus != null && childSeverity.isPresent() && maxSeverity.isLessThan(childSeverity.get())) {
                    final OnmsSeverity newSeverity = createSeverity(childStatus.computeStatus());
                    if (newSeverity != null) {
                        maxSeverity = newSeverity;
                    }
                } else if(!childSeverity.isPresent() || OnmsSeverity.NORMAL.equals(childSeverity.get())) {
                    allChildrenHaveActiveAlarms = false; // at least one child has no active alarm
                }

                if(childStatus != null) {
                    count = count + Integer.parseInt(childStatus.getStatusProperties().get("statusCount"));
                }
            }

            if (allChildrenHaveActiveAlarms && (maxSeverity == null || maxSeverity.isLessThan(OnmsSeverity.MAJOR))) {
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
        if (severity == null) return null;
        return new DefaultStatus(severity.getLabel(), count);
    }
}
