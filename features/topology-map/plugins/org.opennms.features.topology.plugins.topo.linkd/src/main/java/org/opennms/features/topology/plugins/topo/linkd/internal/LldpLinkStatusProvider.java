/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.criteria.*;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.core.criteria.restrictions.NotRestriction;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class LldpLinkStatusProvider implements EdgeStatusProvider {

    public class LldpLinkStatus implements Status{

        final String m_status;

        public LldpLinkStatus(String status) {
            m_status = status;
        }

        public LldpLinkStatus(EdgeAlarmStatusSummary summary) {
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
    private LldpLinkDao m_lldpLinkDao;

    @Override
    public String getNameSpace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD;
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        Map<String, EdgeRef> mappedRefs = mapRefs(edges);
        Map<EdgeRef, Status> returnMap = initializeMap(edges);//new HashMap<EdgeRef, Status>();


        Set<Integer> lldpLinkIds = new HashSet<Integer>();
        for (String edgeRefId : mappedRefs.keySet()) {
            if(edgeRefId.contains("|")) {
                int charIndex = edgeRefId.indexOf('|');
                int sourceId = Integer.parseInt(edgeRefId.substring(0, charIndex));
                int targetId = Integer.parseInt(edgeRefId.substring(charIndex + 1, edgeRefId.length()));
                lldpLinkIds.add(sourceId);
                lldpLinkIds.add(targetId);
            }
        }

        List<EdgeAlarmStatusSummary> lldpEdgeAlarmSummaries = getEdgeAlarmSummaries(new ArrayList<Integer>(lldpLinkIds));//m_alarmDao.getLldpEdgeAlarmSummaries(new ArrayList<Integer>(lldpLinkIds));

        for (EdgeAlarmStatusSummary eSum : lldpEdgeAlarmSummaries) {
            String linkId = eSum.getLldpLinkId();
            EdgeRef edge = mappedRefs.get(linkId);
            if(returnMap.size() > 0 && edge != null) returnMap.put(edge, new LldpLinkStatus(eSum));
        }


        //TODO: handle child edges
        return returnMap;
    }

    private List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        List<LldpLink> links = m_lldpLinkDao.findLinksForIds(linkIds);

        Map<String, EdgeAlarmStatusSummary> summaryMap = new HashMap<String, EdgeAlarmStatusSummary>();
        for (LldpLink sourceLink : links) {

            OnmsNode sourceNode = sourceLink.getNode();
            LldpElement sourceElement = sourceNode.getLldpElement();

            for (LldpLink targetLink : links) {
                OnmsNode targetNode = targetLink.getNode();
                LldpElement targetLldpElement = targetNode.getLldpElement();

                //Compare the remote data to the targetNode element data
                boolean bool1 = sourceLink.getLldpRemPortId().equals(targetLink.getLldpPortId()) && targetLink.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool2 = sourceLink.getLldpRemPortDescr().equals(targetLink.getLldpPortDescr()) && targetLink.getLldpRemPortDescr().equals(sourceLink.getLldpPortDescr());
                boolean bool3 = sourceLink.getLldpRemChassisId().equals(targetLldpElement.getLldpChassisId()) && targetLink.getLldpRemChassisId().equals(sourceElement.getLldpChassisId());
                boolean bool4 = sourceLink.getLldpRemSysname().equals(targetLldpElement.getLldpSysname()) && targetLink.getLldpRemSysname().equals(sourceElement.getLldpSysname());
                boolean bool5 = sourceLink.getLldpRemPortIdSubType() == targetLink.getLldpPortIdSubType() && targetLink.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool2 && bool3 && bool4 && bool5) {

                    summaryMap.put(sourceNode.getNodeId() + ":" + sourceLink.getLldpPortIfindex(),
                            new EdgeAlarmStatusSummary(sourceLink.getId(),
                                    targetLink.getId(), null)
                    );

                }
            }
        }

        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OnmsAlarm.class);
        criteria.addRestriction(new EqRestriction("uei", "uei.opennms.org/internal/topology/linkDown"));
        criteria.addRestriction(new NeRestriction("severity", OnmsSeverity.CLEARED));
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);

        for (OnmsAlarm alarm : alarms) {
            String key = alarm.getNodeId() + ":" + alarm.getIfIndex();
            if (summaryMap.containsKey(key)) {
                EdgeAlarmStatusSummary summary = summaryMap.get(key);
                summary.setEventUEI(alarm.getUei());
            }

        }
        return new ArrayList<EdgeAlarmStatusSummary>(summaryMap.values());
    }

    private Map<String, EdgeRef> mapRefs(Collection<EdgeRef> edges) {
        Map<String, EdgeRef> retVal = new HashMap<String, EdgeRef>();
        for (EdgeRef edge : edges) {
            if(edge.getNamespace().equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD)) retVal.put(edge.getId(), edge);
        }
        return retVal;
    }

    private Map<EdgeRef, Status> initializeMap(Collection<EdgeRef> edges) {
        Map<EdgeRef, Status> retVal = new HashMap<EdgeRef, Status>();
        for (EdgeRef edge : edges) {
            if(edge.getNamespace().equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD)) retVal.put(edge, new LldpLinkStatus("unknown"));
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return "lldpLinkStatusProvider";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EdgeStatusProvider){
            EdgeStatusProvider provider = (EdgeStatusProvider) obj;
            return provider.getClass().getSimpleName().equals(getClass().getSimpleName());
        } else {
            return false;
        }
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }
}
