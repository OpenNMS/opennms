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
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.*;

public class BridgeLinkStatusProvider extends AbstractLinkStatusProvider {

    private BridgeMacLinkDao m_bridgeMackLinkDao;
    private Multimap<String, BridgeMacTopologyLink> m_multimapLinks;

    @Override
    public String getNameSpace() {
        return EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE;
    }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        //Bridge Links are different, don't need the linkIds

        Multimap<String, EdgeAlarmStatusSummary> summaryMap = HashMultimap.create();
        for(BridgeMacTopologyLink link : m_multimapLinks.values()) {
            String key = link.getNodeId() + ":" + link.getBridgePortIfIndex();

            if (!summaryMap.containsKey(key)){
                summaryMap.put(key, new EdgeAlarmStatusSummary(link.getNodeId(), link.getBridgePort(), null));
            }

            if(link.getTargetNodeId() != null && link.getSourceIfIndex() != null){
                summaryMap.put(link.getTargetNodeId() + ":" + link.getSourceIfIndex(), new EdgeAlarmStatusSummary(link.getNodeId(), link.getTargetNodeId(), null));
            }
        }

        List<OnmsAlarm> alarms = getLinkDownAlarms();
        for(OnmsAlarm alarm : alarms){
            String key = alarm.getNodeId() + ":" + alarm.getIfIndex();
            String cloudKey = alarm.getNodeId() + "|" + alarm.getIfIndex();
            if (summaryMap.containsKey(key)) {
                Collection<EdgeAlarmStatusSummary> summaries = summaryMap.get(key);

                if(m_multimapLinks.containsKey(cloudKey)){
                    for(BridgeMacTopologyLink link : m_multimapLinks.get(cloudKey)) {
                        String indexKey = link.getTargetNodeId() + ":" + link.getSourceIfIndex();
                        summaries.addAll(summaryMap.get(indexKey));
                    }
                }

                for (EdgeAlarmStatusSummary summary : summaries) {
                    summary.setEventUEI(alarm.getUei());
                }
            }
        }


        List<EdgeAlarmStatusSummary> ret_val = new ArrayList<EdgeAlarmStatusSummary>();
        for (String summaryMapKey : summaryMap.keySet()) {
            ret_val.addAll(summaryMap.get(summaryMapKey));
        }

        return ret_val;
    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
        List<BridgeMacTopologyLink> bridgeMacLinks = m_bridgeMackLinkDao.getAllBridgeLinksToIpAddrToNodes();

        Multimap<String, BridgeMacTopologyLink> multimap = HashMultimap.create();
        for (BridgeMacTopologyLink macLink : bridgeMacLinks) {
            String idKey = String.valueOf(macLink.getNodeId()) + "|" + String.valueOf(macLink.getBridgePort());
            if (mappedRefs.containsKey(idKey) && macLink.getTargetNodeId() != null && macLink.getSourceIfIndex() != null) {
                multimap.put(idKey, macLink);
            }

        }

        if(m_multimapLinks == null){
            m_multimapLinks = HashMultimap.create();
        }
        m_multimapLinks.clear();
        m_multimapLinks.putAll(multimap);

        Set<Integer> ret_val = new HashSet<Integer>();
        for (String key : multimap.keySet()) {
            Collection<BridgeMacTopologyLink> links = multimap.get(key);
            for (BridgeMacTopologyLink link : links) {
                    ret_val.add(link.getId());
            }

        }

        return ret_val;
    }

    public void setBridgeMacLinkDao(BridgeMacLinkDao bridgeMacLinkDao) {
        m_bridgeMackLinkDao = bridgeMacLinkDao;
    }
}
