/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.topology.CdpTopologyLink;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class CdpLinkStatusProvider extends AbstractLinkStatusProvider{

    private CdpLinkDao m_cdpLinkDao;

    @Override
    public String getNameSpace() { return EnhancedLinkdTopologyProvider.CDP_EDGE_NAMESPACE; }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {

        List<CdpTopologyLink> cdpLinks = getCdpLinkDao().findLinksForTopologyByIds(linkIds.toArray(new Integer[0]));
        Multimap<String, EdgeAlarmStatusSummary> summaryMap = HashMultimap.create();
        for (CdpTopologyLink link : cdpLinks) {
            summaryMap.put(link.getSrcNodeId() + ":" + link.getSrcIfIndex(),
                    new EdgeAlarmStatusSummary(link.getSourceId(), link.getTargetId(), null));
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

        return new ArrayList<>(summaryMap.values());
    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
        Set<Integer> linkIds = new HashSet<Integer>();
        for (String edgeRefId : mappedRefs.keySet()) {
            if (edgeRefId.contains("|")) {
                int charIndex = edgeRefId.indexOf('|');
                int sourceId = Integer.parseInt(edgeRefId.substring(0, charIndex));
                int targetId = Integer.parseInt(edgeRefId.substring(charIndex + 1, edgeRefId.length()));
                linkIds.add(sourceId);
                linkIds.add(targetId);
            }
        }

        return linkIds;
    }

    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
    }
}
