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
package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.getBridgeAddressFromStpBridgeId;
import static org.opennms.core.utils.InetAddressUtils.isValidBridgeAddress;
import static org.opennms.core.utils.InetAddressUtils.isValidStpBridgeId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.enlinkd.model.BridgeStpLink;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.snmp.CiscoVtpTracker;
import org.opennms.netmgt.enlinkd.snmp.CiscoVtpVlanTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBasePortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBaseTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dStpPortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dTpFdbTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1qTpFdbTableTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class NodeDiscoveryBridge extends NodeCollector {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridge.class);


    private final BridgeTopologyService m_bridgeTopologyService;
    private final int m_maxSize;
    private final boolean m_disableBridgeVlanDiscovery;
    /**
     * Constructs a new SNMP collector for Bridge Node Discovery. The
     * collection does not occur until the <code>run</code> method is invoked.
     * 
     */
    public NodeDiscoveryBridge(
            final NodeCollectionGroupBridge group,
            final Node node,
            final int priority ) {
        super(group.getLocationAwareSnmpClient() , node, priority);
        m_bridgeTopologyService = group.getBridgeTopologyService();
        m_maxSize = group.getMaxBft();
        m_disableBridgeVlanDiscovery = group.isDisableBridgeVlanDiscovery();
    }

    public void collect() {
        final Date now = new Date();

        SnmpAgentConfig peer = getSnmpAgentConfig();
        String community = peer.getReadCommunity();
        Map<Integer, String> vlanmap = (m_disableBridgeVlanDiscovery) ? new HashMap<>() : getVtpVlanMap(peer);
        Map<Integer,SnmpAgentConfig> vlanSnmpAgentConfigMap = new HashMap<>();
        for (Integer vlanId: vlanmap.keySet()) {
            LOG.debug("run: node [{}], support cisco vtp: setting peer community for vlan: {}, vlanname: {}",
           		 getNodeId(),vlanId,vlanmap.get(vlanId));
            
            SnmpAgentConfig vlanpeer = getSnmpAgentConfig();
        	if (vlanpeer.isVersion3()) {
        		vlanpeer.setContextName("vlan-"+vlanId);
        	} else {
                vlanpeer.setReadCommunity(community + "@" + vlanId);
        	}
        	vlanSnmpAgentConfigMap.put(vlanId, vlanpeer);        	
        }
        
        if (vlanmap.isEmpty()) {
        	vlanSnmpAgentConfigMap.put(null, peer);
        	vlanmap.put(null, null);
        }
        
        List<BridgeForwardingTableEntry> bft = new ArrayList<>();
        Map<Integer, Integer> bridgeifindex = new HashMap<>();

        for (Entry<Integer, SnmpAgentConfig> entry : vlanSnmpAgentConfigMap.entrySet()) {
            Map<Integer,Integer> vlanbridgetoifindex = walkDot1dBasePortTable(entry.getValue());
            LOG.debug("run: node: [{}], vlan: {}, bridge ifindex map {}",
                      getNodeId(), vlanmap.get(entry.getKey()), vlanbridgetoifindex);
            bridgeifindex.putAll(vlanbridgetoifindex);
        }

        for (Entry<Integer, String> entry : vlanmap.entrySet()) {
            
            BridgeElement bridge = getDot1dBridgeBase(vlanSnmpAgentConfigMap.get(entry.getKey()));
            if (bridge != null) {
                bridge.setVlan(entry.getKey());
                bridge.setVlanname(vlanmap.get(entry.getKey()));
                m_bridgeTopologyService.store(getNodeId(), bridge);
            } else {
                LOG.debug("run: node: [{}], vlan {}. no dot1d bridge data found. skipping other operations",
                          getNodeId(), entry.getValue());
                continue;
            }

            if (!isValidStpBridgeId(bridge.getStpDesignatedRoot())) {
                LOG.debug("run: node: [{}], vlan {}. invalid designated root: spanning tree not supported.",
                         getNodeId(),entry.getValue());
            } else if (bridge.getBaseBridgeAddress().equals(getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()))) {
                LOG.debug("run: node [{}]: vlan {}. designated root {} is itself. Skipping store.",
                		 getNodeId(),
                		 entry.getValue(),
                         bridge.getStpDesignatedRoot());
            } else {
                for (BridgeStpLink stplink: walkSpanningTree(vlanSnmpAgentConfigMap.get(entry.getKey()),
                                 bridge.getBaseBridgeAddress())) {
                    stplink.setVlan(entry.getKey());
                    stplink.setStpPortIfIndex(bridgeifindex.get(stplink.getStpPort()));
                    m_bridgeTopologyService.store(getNodeId(), stplink);
                }
            }
            bft = walkDot1dTpFdp(entry.getValue(),entry.getKey(), bridgeifindex, bft, vlanSnmpAgentConfigMap.get(entry.getKey()));
        }
        LOG.debug("run: node [{}]: deleting older the time {}", getNodeId(), now);
        m_bridgeTopologyService.reconcile(getNodeId(), now);
		
        bft = walkDot1qTpFdb(peer,bridgeifindex, bft);
        LOG.debug("run: node [{}]: bft size:{}", getNodeId(), bft.size());

        if (bft.size() > 0) {
            LOG.debug("run: node [{}]: updating topology", getNodeId());
        	m_bridgeTopologyService.store(getNodeId(), bft);
        }
        m_bridgeTopologyService.collectedBft(getNodeId());
    }

    private BridgeElement getDot1dBridgeBase(SnmpAgentConfig peer) {
        final Dot1dBaseTracker dot1dbase = new Dot1dBaseTracker();

        try {
            getLocationAwareSnmpClient().walk(peer, dot1dbase).withDescription("dot1dbase").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.info("run: node [{}]: ExecutionException: BRIDGE_MIB not supported: {}", 
                     getNodeId(), e.getMessage());
            return null; 
        } catch (final InterruptedException e) {
            LOG.info("run: node [{}]: InterruptedException: BRIDGE_MIB not supported: {}", 
                     getNodeId(), e.getMessage());
            return null;
        }

        BridgeElement bridge = dot1dbase.getBridgeElement();
        if (bridge.getBaseBridgeAddress() == null) {
            LOG.info("run: node [{}]: base bridge address is null. BRIDGE_MIB not supported.",
                     getNodeId());
            return null;
        }

        if (!isValidBridgeAddress(bridge.getBaseBridgeAddress())) {
            LOG.info("run: node [{}]: base bridge address {} is not valid on. BRIDGE_MIB not supported",
            		getNodeId(),dot1dbase.getBridgeAddress());
            return null;
        }

        if (bridge.getBaseNumPorts() == null) {
            LOG.debug("run: node [{}]: base bridge address {}: has null number port active. Setting to -1.",
            		getNodeId(),dot1dbase.getBridgeAddress());
            bridge.setBaseNumPorts(-1);
        }
        LOG.debug("run: bridge {} has is if type {}, on: {}",
                 dot1dbase.getBridgeAddress(),
                 BridgeDot1dBaseType.getTypeString(dot1dbase.getBridgeType()),
                 getNodeId());

        if (bridge.getBaseType() == null) {
            LOG.debug("run: node [{}]: base bridge address {}: has null base type. Setting to unknown.",
                        getNodeId(),dot1dbase.getBridgeAddress());
            bridge.setBaseType(BridgeDot1dBaseType.DOT1DBASETYPE_UNKNOWN);
        }

        if (bridge.getBaseType() == BridgeDot1dBaseType.DOT1DBASETYPE_SOURCEROUTE_ONLY) {
            LOG.info("run: node [{}]: base bridge address {}: is source route bridge only. BRIDGE_MIB not supported",
            		getNodeId(),dot1dbase.getBridgeAddress());
            return null;
        }
        return bridge;
    }

    private Map<Integer, String> getVtpVlanMap(SnmpAgentConfig peer) {

        final Map<Integer, String> vlanmap = new HashMap<>();
        final CiscoVtpTracker vtpStatus = new CiscoVtpTracker();
        
        try {
            getLocationAwareSnmpClient().walk(peer, vtpStatus).
            withDescription("vtpVersion").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.debug("run: node [{}]: ExecutionException: vtpVersion: {}", 
                    getNodeId(), e.getMessage());
           return vlanmap;
       } catch (final InterruptedException e) {
           LOG.debug("run: node [{}]: InterruptedException: vtpVersion: {}", 
                    getNodeId(), e.getMessage());
           return vlanmap;
       }

        if (vtpStatus.getVtpVersion() == null) {
            LOG.debug("run: node [{}]: cisco vtp mib not supported.", getNodeId());
            return vlanmap;
        }

        LOG.debug("run: node [{}]: cisco vtp mib supported.", getNodeId());

        final CiscoVtpVlanTableTracker ciscoVtpVlanTableTracker = new CiscoVtpVlanTableTracker() {
            @Override
            public void processCiscoVtpVlanRow(final CiscoVtpVlanRow row) {
                if (row.isTypeEthernet() && row.isStatusOperational()) {
                    vlanmap.put(row.getVlanIndex(), row.getVlanName());
                }
            }
        };
        try {
            getLocationAwareSnmpClient().walk(peer, ciscoVtpVlanTableTracker).
            withDescription("ciscoVtpVlan").
            withLocation(getLocation()).
            execute().
            get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}", 
                     getNodeId(), e.getMessage());
        }
        return vlanmap;
    }

    private Map<Integer, Integer> walkDot1dBasePortTable(SnmpAgentConfig peer) {
        final Map<Integer, Integer> bridgetoifindex = new HashMap<>();
        Dot1dBasePortTableTracker dot1dBasePortTableTracker = new Dot1dBasePortTableTracker() {
                @Override
                public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
                    bridgetoifindex.put(row.getBaseBridgePort(),
                                        row.getBaseBridgePortIfindex());
                }
            };
    
        try {
            getLocationAwareSnmpClient().walk(peer, dot1dBasePortTableTracker).
            withDescription("dot1dBasePortTable").
            withLocation(getLocation()).
            execute().
            get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}", 
                     getNodeId(), e.getMessage());
        }
        return bridgetoifindex;
    }

    private List<BridgeForwardingTableEntry> walkDot1dTpFdp(final String vlan,final Integer vlanId,
            final Map<Integer, Integer> bridgeifindex,
            List<BridgeForwardingTableEntry> bft, SnmpAgentConfig peer) {

        Dot1dTpFdbTableTracker dot1dTpFdbTableTracker = new Dot1dTpFdbTableTracker() {

        	
            @Override
            public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
                BridgeForwardingTableEntry link = row.getLink();
                if (link.getBridgeDot1qTpFdbStatus() == null) {
                    LOG.debug("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {}. row has null status. ",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort());
                    return;
                }
                if (link.getBridgePort() == null) {
                    LOG.debug("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} status {}. row has null bridge port.  ",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (link.getMacAddress() == null
                        || !isValidBridgeAddress(link.getMacAddress())) {
                    LOG.debug("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} ifindex {} status {}. row has invalid mac.",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                link.setVlan(vlanId);

                if (!bridgeifindex.containsKey(link.getBridgePort())
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    LOG.debug("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} status {}. no ifindex found. ",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgeDot1qTpFdbStatus());
                    fixCiscoBridgeMibPort(link.getBridgePort(), bridgeifindex);
                }
                link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
                LOG.debug("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} ifindex {} status {}. row processed.",
                		getNodeId(),
                         link.getMacAddress(), link.getVlan(),
                         link.getBridgePort(), link.getBridgePortIfIndex(),
                         link.getBridgeDot1qTpFdbStatus());
                bft.add(link);
            }
        };
        
        try {
            getLocationAwareSnmpClient().walk(peer,
                                                      dot1dTpFdbTableTracker).withDescription("dot1dTbFdbPortTable").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}", 
                     getNodeId(), e.getMessage());
        }
        return bft;
    }

    private void fixCiscoBridgeMibPort(Integer bridgeport,Map<Integer,Integer> bridgeifindex) {
        List<Integer> sortedPort = new ArrayList<>(bridgeifindex.keySet());
        Collections.sort(sortedPort);
        Integer beforePort=null;
        Integer afterPort=null;
        for (Integer port: sortedPort) {
        	if (port < bridgeport)
        		beforePort = port;
        	if (port > bridgeport) {
        		afterPort = port;
        		break;
        	}
        }
        if (afterPort==null && beforePort == null) {
            bridgeifindex.put(bridgeport, bridgeport);        	
        } else if (afterPort == null && bridgeifindex.get(beforePort) == 0) {
            bridgeifindex.put(bridgeport, bridgeport);
        } else if (beforePort == null && bridgeifindex.get(afterPort) == 0) {
            bridgeifindex.put(bridgeport, bridgeport);
        } else if (afterPort == null) {
            bridgeifindex.put(bridgeport, bridgeport+bridgeifindex.get(beforePort)-beforePort);
        } else if (beforePort == null) {
            bridgeifindex.put(bridgeport, bridgeport+bridgeifindex.get(afterPort)-afterPort);
        } else {
            int diffbefore = 0;
            int diffafter = 0;
            if (bridgeifindex.get(beforePort) != null ) {
                 diffbefore = bridgeifindex.get(beforePort)-beforePort;
            } else {
                LOG.error("fixCiscoBridgeMibPort: node [{}]:  null ifindex found for before port {}.",
                          getNodeId(),
                          beforePort);
            }
            if (bridgeifindex.get(afterPort) != null ) {
                diffafter  = bridgeifindex.get(afterPort)-afterPort;
            } else {
                LOG.error("fixCiscoBridgeMibPort: node [{}]:  null ifindex found for after port {}.",
                          getNodeId(),
                          afterPort);
            }
            if (diffafter == diffbefore) {
                bridgeifindex.put(bridgeport, bridgeport+diffafter);
            } else if ((bridgeport-beforePort) > (afterPort-bridgeport) ) {
                bridgeifindex.put(bridgeport, diffafter+bridgeport);
            } else {
                bridgeifindex.put(bridgeport, diffbefore+bridgeport);
            }
        }
        LOG.debug("fixCiscoBridgeMibPort: node [{}]: port {} ifindex {}.",
                 getNodeId(),
    		bridgeport,
    		bridgeifindex.get(bridgeport));
    }
    
    private List<BridgeForwardingTableEntry> walkDot1qTpFdb(SnmpAgentConfig peer,
            final Map<Integer, Integer> bridgeifindex,
            final List<BridgeForwardingTableEntry> bft) {

        LOG.debug("walkDot1qTpFdb: node [{}]: bridge ifindex map {}",
                  getNodeId(), bridgeifindex);
        

        Dot1qTpFdbTableTracker dot1qTpFdbTableTracker = new Dot1qTpFdbTableTracker() {

            @Override
            public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
                BridgeForwardingTableEntry link = row.getLink();

                if (link.getBridgeDot1qTpFdbStatus() == null) {
                    LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: on port {}. row has null status.",
                    		getNodeId(),
                             link.getMacAddress(),
                             link.getBridgePort());
                    return;
                }
                if (link.getBridgePort() == null) {
                    LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: on port {} status {}. row has null bridge port.",
                    		getNodeId(),
                                link.getMacAddress(),
                                link.getBridgePort(),
                                link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (link.getMacAddress() == null
                        || !isValidBridgeAddress(link.getMacAddress())) {
                    LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: on port {} ifindex {} status {}. row has invalid mac.",
                    		getNodeId(),
                    	     link.getMacAddress(),
                             link.getBridgePort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (bridgeifindex.isEmpty() && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    link.setBridgePortIfIndex(link.getBridgePort());
                    LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: on port {} ifindex {} status {}. Empty map from bridgeport to ifindex. Assuming ifindex=bridgeport",
                              getNodeId(),
                           link.getMacAddress(),
                           link.getBridgePort(),
                           link.getBridgePortIfIndex(),
                           link.getBridgeDot1qTpFdbStatus());
                } else  if (!bridgeifindex.containsKey(link.getBridgePort()) && bridgeifindex.containsValue(link.getBridgePort())
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    for (Integer bridgeport: bridgeifindex.keySet()) {
                        if (link.getBridgePort().intValue() == bridgeifindex.get(bridgeport).intValue()) {
                            link.setBridgePort(bridgeport);
                            link.setBridgePortIfIndex(bridgeifindex.get(bridgeport));
                        }
                    }
                    LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: on port {} ifindex {} status {}. Assument bridgeport index is ifindex. Reverting bridgeport/ifindex",
                             getNodeId(),
                          link.getMacAddress(),
                          link.getBridgePort(),   
                          link.getBridgePortIfIndex(),
                          link.getBridgeDot1qTpFdbStatus());
                } else  if (!bridgeifindex.containsKey(link.getBridgePort()) 
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: on port {} ifindex {} status {}. Cnnot find suitable skipping entry",
                             getNodeId(),
                          link.getMacAddress(),
                          link.getBridgePort(),
                          link.getBridgePortIfIndex(),
                          link.getBridgeDot1qTpFdbStatus());
                    return;
                } else {
                    link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
                }
                LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: vlan {}: on port {} ifindex {} status {}. row processed.",
                		getNodeId(),
                         link.getMacAddress(), link.getVlan(),
                         link.getBridgePort(), link.getBridgePortIfIndex(),
                         link.getBridgeDot1qTpFdbStatus());
                bft.add(link);
            }

        };
        try {
            getLocationAwareSnmpClient().walk(peer,
                                                      dot1qTpFdbTableTracker).withDescription("dot1qTbFdbPortTable").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.debug("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
        } catch (final InterruptedException e) {
            LOG.debug("run: node [{}]: InterruptedException: {}", 
                     getNodeId(), e.getMessage());
        }
        return bft;
    }

    private List<BridgeStpLink> walkSpanningTree(SnmpAgentConfig peer,
            final String baseBridgeAddress) {

        final List<BridgeStpLink> stplinks = new ArrayList<>();
        Dot1dStpPortTableTracker stpPortTableTracker = new Dot1dStpPortTableTracker() {

            @Override
            public void processDot1dStpPortRow(final Dot1dStpPortRow row) {
                BridgeStpLink link = row.getLink();
                LOG.debug("processDot1dStpPortRow: node [{}]: stp: port:{}/{}, vlan:{}, designated root/bridge/port:{}/{}/{}.", getNodeId(),                		                               link.getStpPort(),
                   link.getStpPortState(),
                   link.getVlan(),
                   link.getDesignatedRoot(),
                   link.getDesignatedBridge(),
                   link.getDesignatedPort()
                   );
                if (isValidStpBridgeId(link.getDesignatedRoot())
                        && isValidStpBridgeId(link.getDesignatedBridge())
                        && !baseBridgeAddress.equals(link.getDesignatedBridgeAddress())) {
                    LOG.debug("processDot1dStpPortRow: node [{}]: stp: port:{}/{}, vlan:{}, designated root/bridge/port:{}/{}/{}. row added", getNodeId(),                		                               link.getStpPort(),
                            link.getStpPortState(),
                            link.getVlan(),
                            link.getDesignatedRoot(),
                            link.getDesignatedBridge(),
                            link.getDesignatedPort()
                            );
                    stplinks.add(link);
                }
            }
        };

        try {
            getLocationAwareSnmpClient().walk(peer,
                                                      stpPortTableTracker).withDescription("dot1dStpPortTable").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.info("run: node [{}]: ExecutionException: {}", 
                     getNodeId(), e.getMessage());
        } catch (final InterruptedException e) {
            LOG.info("run: node [{}]: InterruptedException: {}", 
                     getNodeId(), e.getMessage());
        }
        return stplinks;
    }

    @Override
    public String getName() {
        return "NodeDiscoveryBridge";
    }

    @Override
    public boolean isReady() {
        return m_bridgeTopologyService.collectBft(getNodeId(),m_maxSize);
    }
}
