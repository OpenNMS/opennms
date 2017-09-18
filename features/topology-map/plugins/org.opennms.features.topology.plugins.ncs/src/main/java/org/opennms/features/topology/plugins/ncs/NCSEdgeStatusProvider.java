/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEventParameter;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

public class NCSEdgeStatusProvider implements EdgeStatusProvider{

    public class NCSLinkStatus implements Status {

        private final String m_status;

        public NCSLinkStatus(String status) {
            m_status = status.toLowerCase();
        }

        @Override
        public String computeStatus() {
            return m_status;
        }

        @Override
        public Map<String, String> getStatusProperties() {
            return null;
        }

        @Override
        public Map<String, String> getStyleProperties() {
            return Maps.newHashMap();
        }

    }

    private AlarmDao m_alarmDao;

    public NCSEdgeStatusProvider() {
    }

    @Override
    public String getNamespace() {
        return NCSPathEdgeProvider.PATH_NAMESPACE + "::NCS";
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        List<EdgeRef> ncsEdges = new ArrayList<EdgeRef>(Collections2.filter(edges, new Predicate<EdgeRef>() {
            @Override
            public boolean apply(EdgeRef edgeRef) {
                return edgeRef.getNamespace().equals("ncs");
            }
        }));

        Set<String> alarms = getNCSImpactedAlarms();

        Map<EdgeRef, Status> statusMap = new HashMap<EdgeRef, Status>();
        for (EdgeRef edge : ncsEdges) {
            NCSEdgeProvider.NCSEdge e = (NCSEdgeProvider.NCSEdge) edge;
            e.setStatus("up");
            statusMap.put(edge, new NCSLinkStatus("up"));
            if (alarms.contains(e.getSourceElementName()) || alarms.contains(e.getTargetElementName())) {
                statusMap.put(edge, new NCSLinkStatus("down"));
                e.setStatus("down");
            }
        }

        return statusMap;
    }

    private Set<String> getNCSImpactedAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("uei", "uei.opennms.org/internal/ncs/componentImpacted"));

        List<OnmsAlarm> alarms = getAlarmDao().findMatching(criteria);


        Set<String> alarmsSet = new HashSet<>();

        for (OnmsAlarm alarm : alarms) {
            final Optional<String> foreignSource = alarm.findEventParameter("foreignSource").map(OnmsEventParameter::getValue);
            final Optional<String> foreignId = alarm.findEventParameter("foreignId").map(OnmsEventParameter::getValue);

            if (foreignSource.isPresent() && foreignId.isPresent()) {
                alarmsSet.add(foreignSource.get() + "::" + foreignId.get());
            }

        }

        return alarmsSet;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals("nodes");
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
}
