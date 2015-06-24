/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class LldpLinkStatusProvider extends AbstractLinkStatusProvider {

    private LldpLinkDao m_lldpLinkDao;

    @Override
    public String getNameSpace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD + "::LLDP";
    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
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
        return lldpLinkIds;
    }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        List<LldpLink> links = m_lldpLinkDao.findLinksForIds(linkIds);

        Multimap<String, EdgeAlarmStatusSummary> summaryMap = HashMultimap.create();
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

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }
}
