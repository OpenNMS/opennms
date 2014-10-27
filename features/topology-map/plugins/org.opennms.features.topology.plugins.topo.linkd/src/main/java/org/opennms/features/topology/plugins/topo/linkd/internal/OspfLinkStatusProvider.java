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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.MultiMap;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class OspfLinkStatusProvider extends AbstractLinkStatusProvider {

    private OspfLinkDao m_ospfLinkDao;

    @Override
    public String getNameSpace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD + "::OSPF";
    }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        org.opennms.core.criteria.Criteria criteria = new org.opennms.core.criteria.Criteria(OspfLink.class);
        criteria.addRestriction(new InRestriction("id", linkIds));

        List<OspfLink> links = getOspfLinkDao().findMatching(criteria);
        Multimap<String, EdgeAlarmStatusSummary> summaryMap = HashMultimap.create();
        for (OspfLink sourceLink : links) {
            OnmsNode sourceNode = sourceLink.getNode();
            for (OspfLink targetLink : links) {
                boolean ipAddrCheck = sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr());
                if (ipAddrCheck) {
                    summaryMap.put(sourceNode.getNodeId() + ":" + sourceLink.getOspfIfIndex(),
                            new EdgeAlarmStatusSummary(sourceLink.getId(), targetLink.getId(), null));
                }
            }
        }

        List<OnmsAlarm> alarms = getLinkDownAlarms();

        for (OnmsAlarm alarm : alarms) {
            String key = alarm.getNodeId() + ":" + alarm.getIfIndex();
            if (summaryMap.containsKey(key)) {

                Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);
                for (EdgeAlarmStatusSummary summary : summaries) {
                    summary.setEventUEI(alarm.getUei());
                }

            }

        }

        return new ArrayList<EdgeAlarmStatusSummary>(summaryMap.values());

    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
        Set<Integer> linkIds = new HashSet<Integer>();
        for (String edgeRefId : mappedRefs.keySet()) {
            if(edgeRefId.contains("|")) {
                int charIndex = edgeRefId.indexOf('|');
                int sourceId = Integer.parseInt(edgeRefId.substring(0, charIndex));
                int targetId = Integer.parseInt(edgeRefId.substring(charIndex + 1, edgeRefId.length()));
                linkIds.add(sourceId);
                linkIds.add(targetId);
            }
        }
        return linkIds;
    }

    public OspfLinkDao getOspfLinkDao() {
        return m_ospfLinkDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        m_ospfLinkDao = ospfLinkDao;
    }
}
