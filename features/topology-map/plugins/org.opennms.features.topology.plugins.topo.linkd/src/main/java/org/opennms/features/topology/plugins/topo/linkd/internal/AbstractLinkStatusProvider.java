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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public abstract class AbstractLinkStatusProvider implements EdgeStatusProvider {

    public static class LinkStatus implements Status{

        private final String m_status;

        public LinkStatus(String status) {
            m_status = status;
        }

        public LinkStatus(EdgeAlarmStatusSummary summary) {
            m_status = summary.getEventUEI().equals(EventConstants.TOPOLOGY_LINK_DOWN_EVENT_UEI) ? "down" : "up";
        }

        @Override
        public String computeStatus() {
            return m_status.toLowerCase();
        }

        @Override
        public Map<String, String> getStatusProperties() {
            Map<String, String> statusMap = new HashMap<String, String>();
            statusMap.put("status", m_status);

            return statusMap;
        }

    }
    private AlarmDao m_alarmDao;

    @Override
    public String getNameSpace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
    }

    protected abstract List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds);



    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        Map<String, EdgeRef> mappedRefs = mapRefs(edges);
        Map<EdgeRef, Status> returnMap = initializeMap(edges);


        Set<Integer> linkIds = getLinkIds(mappedRefs);
        if(linkIds.size() > 0) {
            List<EdgeAlarmStatusSummary> edgeAlarmSummaries = getEdgeAlarmSummaries(new ArrayList<Integer>(linkIds));

            for (EdgeAlarmStatusSummary eSum : edgeAlarmSummaries) {
                String linkId = eSum.getId();
                EdgeRef edge = mappedRefs.get(linkId);
                if (returnMap.size() > 0 && edge != null) returnMap.put(edge, getLinkStatusForSummary(eSum));
            }
        }

        //TODO: handle child edges
        return returnMap;
    }

    protected LinkStatus getLinkStatusForSummary(EdgeAlarmStatusSummary summary) {
        return new LinkStatus(summary);
    };

    protected abstract Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs);

    protected Map<String, EdgeRef> mapRefs(Collection<EdgeRef> edges) {
        Map<String, EdgeRef> retVal = new HashMap<String, EdgeRef>();
        for (EdgeRef edge : edges) {
            String nameSpace = getNameSpace();
            if(edge.getNamespace().equals(nameSpace)) retVal.put(edge.getId(), edge);
        }
        return retVal;
    }

    protected Map<EdgeRef, Status> initializeMap(Collection<EdgeRef> edges) {
        Map<EdgeRef, Status> retVal = new HashMap<EdgeRef, Status>();
        for (EdgeRef edge : edges) {
            String nameSpace = getNameSpace();
            if(edge.getNamespace().equals(nameSpace)) retVal.put(edge, new LinkStatus("unknown"));
        }
        return retVal;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    protected List<OnmsAlarm> getLinkDownAlarms() {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("uei", "uei.opennms.org/internal/topology/linkDown"));
        criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));
        return getAlarmDao().findMatching(criteria);
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
}
