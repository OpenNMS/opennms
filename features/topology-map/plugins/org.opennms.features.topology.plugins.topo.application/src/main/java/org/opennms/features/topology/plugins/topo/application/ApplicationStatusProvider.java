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

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.ApplicationStatusEntity;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ApplicationStatusProvider implements StatusProvider {

    private final ApplicationDao applicationDao;

    public ApplicationStatusProvider(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public Map<VertexRef, Status> getStatusForVertices(VertexProvider vertexProvider, Collection<VertexRef> vertices, Criteria[] criteria) {
        Map<VertexRef, Status> returnMap = new HashMap<>();
        Map<ApplicationStatusEntity.Key, Status> statusMap = new HashMap<>();

        List<ApplicationStatusEntity> result = applicationDao.getAlarmStatus();
        for (ApplicationStatusEntity eachRow : result) {
            DefaultStatus status = createStatus(eachRow.getSeverity(), eachRow.getCount());
            statusMap.put(eachRow.getKey(), status);
        }

        // status for all known node ids
        Collection<VertexRef> vertexRefsForNamespace = getVertexRefsForNamespace(vertices);
        Collection<VertexRef> vertexRefsRoot = getRootElements(vertexRefsForNamespace);
        Collection<VertexRef> vertexRefs = new ArrayList<>(vertexRefsForNamespace);
        vertexRefs.removeAll(vertexRefsRoot);

        // calculate status for children
        for (VertexRef eachVertex : vertexRefs) {
            ApplicationVertex applicationVertex = (ApplicationVertex) eachVertex;
            Status alarmStatus = statusMap.get(createKey(applicationVertex));
            if (alarmStatus == null) {
                alarmStatus = createStatus(OnmsSeverity.NORMAL, 0);
            }
            returnMap.put(eachVertex, alarmStatus);
        }

        // calculate status for root
        for (VertexRef eachRoot : vertexRefsRoot) {
            ApplicationVertex eachRootApplication = (ApplicationVertex) eachRoot;
            OnmsSeverity maxSeverity = OnmsSeverity.NORMAL;
            int count = 0;
            for (VertexRef eachChild : eachRootApplication.getChildren()) {
                ApplicationVertex eachChildApplication = (ApplicationVertex) eachChild;
                ApplicationStatusEntity.Key childKey = createKey(eachChildApplication);
                Status childStatus = statusMap.get(childKey);
                if (childStatus != null && maxSeverity.isLessThan(createSeverity(childStatus.computeStatus()))) {
                    maxSeverity = createSeverity(childStatus.computeStatus());
                    count = Integer.parseInt(childStatus.getStatusProperties().get("statusCount"));
                }
            }
            returnMap.put(eachRoot, createStatus(maxSeverity, count));
        }

        return returnMap;
    }

    private OnmsSeverity createSeverity(String label) {
        for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
            if (label.equalsIgnoreCase(eachSeverity.name())) {
                return eachSeverity;
            }
        }
        return null;
    }

    private ApplicationStatusEntity.Key createKey(ApplicationVertex vertex) {
        return new ApplicationStatusEntity.Key(String.valueOf(vertex.getNodeID()), String.valueOf(vertex.getServiceType().getId()), vertex.getIpAddress());
    }

    @Override
    public String getNamespace() {
        return ApplicationTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && getNamespace().equals(namespace);
    }

    private Collection<VertexRef> getRootElements(Collection<VertexRef> vertices) {
        return Collections2.filter(vertices, new Predicate<VertexRef>() {
            @Override
            public boolean apply(VertexRef input) {
                return ((ApplicationVertex) input).isRoot();
            }
        });
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

    private static DefaultStatus createStatus(OnmsSeverity severity, int count) {
        return new DefaultStatus(severity.getLabel(), count);
    }
}
