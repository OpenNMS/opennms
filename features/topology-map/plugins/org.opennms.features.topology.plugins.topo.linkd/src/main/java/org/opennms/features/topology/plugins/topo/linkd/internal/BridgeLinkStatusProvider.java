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

import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BridgeLinkStatusProvider extends AbstractLinkStatusProvider {
    
    private static Logger LOG = LoggerFactory.getLogger(BridgeLinkStatusProvider.class);

    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;

    private Map<String, BridgeMacTopologyLink> m_macBridgeMacLinks;
    private Map<String, BridgeMacTopologyLink> m_bridgeBridgeMacLinks;
    private Map<String, BridgeBridgeLink> m_bridgeBridgeLinks;

    @Override
    public String getNameSpace() {
        return EnhancedLinkdTopologyProvider.BRIDGE_EDGE_NAMESPACE;
    }

    @Override
    protected List<EdgeAlarmStatusSummary> getEdgeAlarmSummaries(List<Integer> linkIds) {
        //Bridge Links are different, don't need the linkIds

        Map<String,EdgeAlarmStatusSummary> alarmMap = new HashMap<String, EdgeAlarmStatusSummary>();
        for (String key: m_bridgeBridgeLinks.keySet()) {
            BridgeBridgeLink link = m_bridgeBridgeLinks.get(key);
            alarmMap.put(key, new EdgeAlarmStatusSummary(key, link.getId(), link.getId(), null));
        }

        for (String key: m_bridgeBridgeMacLinks.keySet()) {
            BridgeMacTopologyLink link = m_bridgeBridgeMacLinks.get(key);
            alarmMap.put(key, new EdgeAlarmStatusSummary(key, link.getId(), link.getId(), null));
        }        

        for (String key: m_macBridgeMacLinks.keySet()) {
            BridgeMacTopologyLink link = m_macBridgeMacLinks.get(key);
            alarmMap.put(key, new EdgeAlarmStatusSummary(key, link.getId(), link.getId(), null));
        }
        
        for(OnmsAlarm alarm : getLinkDownAlarms()){
            LOG.debug("getEdgeAlarmSummaries: alarm: nodeid {} ifindex {} uei {}", alarm.getNodeId(), alarm.getIfIndex(),alarm.getUei());
            for (String key: m_bridgeBridgeLinks.keySet()) {
                BridgeBridgeLink link = m_bridgeBridgeLinks.get(key);
                LOG.debug("getEdgeAlarmSummaries: key {} bridgebridgelink: {} ", key, link.getId());
                if ( alarm.getNodeId() == link.getNode().getId()) {
                    if (link.getBridgePortIfIndex() != 0) {
                        if (alarm.getIfIndex() == link.getBridgePortIfIndex()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted source bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    } else {
                        if (alarm.getIfIndex() == link.getBridgePort()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted source bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    }
                } else if (alarm.getNodeId() == link.getDesignatedNode().getId()) {
                    if (link.getDesignatedPortIfIndex() != 0) {
                        if ( alarm.getIfIndex() == link.getDesignatedPortIfIndex()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted target bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    } else {
                        if (alarm.getIfIndex() == link.getDesignatedPort()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted target bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    }
                }    
            }
            
            for (String key: m_bridgeBridgeMacLinks.keySet()) {
                BridgeMacTopologyLink link = m_bridgeBridgeMacLinks.get(key);
                LOG.debug("getEdgeAlarmSummaries: key {} bridge bridgemaclink: {} ", key, link.getId());
                String sourceLink = EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId());
                String targetLink = EdgeAlarmStatusSummary.getDefaultEdgeId(link.getTargetId(), link.getTargetId());
                LOG.debug("getEdgeAlarmSummaries: sourcelinkid: {}", sourceLink);
                LOG.debug("getEdgeAlarmSummaries: targetLinkId: {}", targetLink);
                if (alarm.getNodeId() == link.getSrcNodeId() && sourceLink.equals(key) ) {
                    if (link.getBridgePortIfIndex() != 0) {
                        if (alarm.getIfIndex() == link.getBridgePortIfIndex()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted source bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    } else {
                        if (alarm.getIfIndex() == link.getBridgePort()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted source bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    }
                } else if (alarm.getNodeId() == link.getTargetNodeId() && targetLink.equals(key) ){
                    if (link.getTargetIfIndex() != 0) {
                        if ( alarm.getIfIndex() == link.getTargetIfIndex()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted target bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    } else {
                        if (alarm.getIfIndex() == link.getTargetBridgePort()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted target bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    }
                }
            }            
            
            for (String key: m_macBridgeMacLinks.keySet()) {
                BridgeMacTopologyLink link = m_macBridgeMacLinks.get(key);
                LOG.debug("getEdgeAlarmSummaries: key {} bridgemaclink: {} ", key, link.getId());
                String targetLink = String.valueOf(link.getId()+"|"+String.valueOf(link.getTargetId()));
                LOG.debug("getEdgeAlarmSummaries: targetLinkId: {}", targetLink);
                if (alarm.getNodeId() == link.getSrcNodeId()) {
                    if (link.getBridgePortIfIndex() != 0) {
                        if (alarm.getIfIndex() == link.getBridgePortIfIndex()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted source bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    } else {
                        if (alarm.getIfIndex() == link.getBridgePort()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted source bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    }
                } else if (alarm.getNodeId() == link.getTargetNodeId() && targetLink.equals(key) ){
                    if (link.getTargetIfIndex() == null ) {
                        alarmMap.get(key).setEventUEI(alarm.getUei());
                        LOG.debug("getEdgeAlarmSummaries: matchted target bridgebridgelink id {} key {}", link.getId(),key);
                   } else  {
                        if ( alarm.getIfIndex() == link.getTargetIfIndex()) {
                            alarmMap.get(key).setEventUEI(alarm.getUei());
                            LOG.debug("getEdgeAlarmSummaries: matchted target bridgebridgelink id {} key {}", link.getId(),key);
                        }
                    }
                }
            }
        }
        List<EdgeAlarmStatusSummary> ret_val = new ArrayList<EdgeAlarmStatusSummary>();
        ret_val.addAll(alarmMap.values());
        return ret_val;
    }

    @Override
    protected Set<Integer> getLinkIds(Map<String, EdgeRef> mappedRefs) {
        
        Map<String, BridgeBridgeLink> mapA = new HashMap<String, BridgeBridgeLink>();
        for (BridgeBridgeLink link: m_bridgeBridgeLinkDao.findAll()) {
            String idKey = EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId());
            LOG.debug("getLinkIds: parsing key {} bridgebridgelink: {} ", idKey, link.getId());
            if (mappedRefs.containsKey(idKey) ) {
                LOG.debug("getLinkIds: adding matched key {} bridgebridgelink: {} ", idKey, link.getId());
                mapA.put(idKey, link);
            }            
        }
        
        Map<String, BridgeMacTopologyLink> mapB =  new HashMap<String, BridgeMacTopologyLink>();
        for (BridgeMacTopologyLink link : m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()) {
            String idKey = EdgeAlarmStatusSummary.getDefaultEdgeId(link.getTargetId(), link.getTargetId());
            LOG.debug("getLinkIds: parsing key {} bridge bridgemaclink: {} ", idKey, link.getId());
            if (mappedRefs.containsKey(idKey) ) {
                LOG.debug("getLinkIds: adding matched key {} bridge bridgemaclink: {} ", idKey, link.getId());
                mapB.put(idKey, link);
            }
            idKey = EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId());
            LOG.debug("getLinkIds: parsing key {} bridge bridgemaclink: {} ", idKey, link.getId());
            if (mappedRefs.containsKey(idKey) ) {
                LOG.debug("getLinkIds: adding matched key {} bridge bridgemaclink: {} ", idKey, link.getId());
                mapB.put(idKey, link);
            }
        }
        
        Map<String, BridgeMacTopologyLink> mapC =  new HashMap<String, BridgeMacTopologyLink>();
        for (BridgeMacTopologyLink link : m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()) {
            String idKey = String.valueOf(link.getId())+ "|" + String.valueOf(link.getTargetId());
            LOG.debug("getLinkIds: parsing key {} mac bridgemaclink: {} ", idKey, link.getId());
            if (mappedRefs.containsKey(idKey) ) {
                LOG.debug("getLinkIds: adding matched key {} mac bridgemaclink: {} ", idKey, link.getId());
                mapC.put(idKey, link);
            }
            idKey = EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId());
            LOG.debug("getLinkIds: parsing key {} mac bridgemaclink: {} ", idKey, link.getId());
            if (mappedRefs.containsKey(idKey) ) {
                LOG.debug("getLinkIds: adding matched key {} mac bridgemaclink: {} ", idKey, link.getId());
                mapC.put(idKey, link);
            }
        }

        if(m_bridgeBridgeLinks == null){
            m_bridgeBridgeLinks = new HashMap<String, BridgeBridgeLink>();
        }
        m_bridgeBridgeLinks.clear();
        m_bridgeBridgeLinks.putAll(mapA);

        if(m_bridgeBridgeMacLinks == null){
            m_bridgeBridgeMacLinks = new HashMap<String, BridgeMacTopologyLink>();
        }
        m_bridgeBridgeMacLinks.clear();
        m_bridgeBridgeMacLinks.putAll(mapB);

        if(m_macBridgeMacLinks == null){
            m_macBridgeMacLinks = new HashMap<String, BridgeMacTopologyLink>();
        }
        m_macBridgeMacLinks.clear();
        m_macBridgeMacLinks.putAll(mapC);
        

        Set<Integer> ret_val = new HashSet<Integer>();
        for (BridgeBridgeLink link : mapA.values()) {
                    ret_val.add(link.getId());
        }

        for (BridgeMacTopologyLink link : mapB.values()) {
                ret_val.add(link.getId());
        }
        
        for (BridgeMacTopologyLink link : mapC.values()) {
                    ret_val.add(link.getId());
        }

        LOG.debug("getLinkIds {}", ret_val);
        return ret_val;
    }

    public BridgeMacLinkDao getBridgeMacLinkDao() {
        return m_bridgeMacLinkDao;
    }

    public void setBridgeMacLinkDao(BridgeMacLinkDao bridgeMacLinkDao) {
        m_bridgeMacLinkDao = bridgeMacLinkDao;
    }
    
    public BridgeBridgeLinkDao getBridgeBridgeLinkDao() {
        return m_bridgeBridgeLinkDao;
    }

    public void setBridgeBridgeLinkDao(BridgeBridgeLinkDao bridgeBridgeLinkDao) {
        m_bridgeBridgeLinkDao = bridgeBridgeLinkDao;
    }

}
