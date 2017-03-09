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

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;
import static org.opennms.core.utils.InetAddressUtils.isValidBridgeAddress;
import static org.opennms.core.utils.InetAddressUtils.isValidStpBridgeId;
import static org.opennms.core.utils.InetAddressUtils.getBridgeAddressFromStpBridgeId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.enlinkd.snmp.CiscoVtpTracker;
import org.opennms.netmgt.enlinkd.snmp.CiscoVtpVlanTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBasePortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dBaseTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dStpPortTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1dTpFdbTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1qTpFdbTableTracker;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.BridgeStpLink;
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
public final class NodeDiscoveryBridge extends NodeDiscovery {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridge.class);

    // public final static String CISCO_ENTERPRISE_OID = ".1.3.6.1.4.1.9";

    /**
     * Constructs a new SNMP collector for Bridge Node Discovery. The
     * collection does not occur until the <code>run</code> method is invoked.
     * 
     * @param EnhancedLinkd
     *            linkd
     * @param LinkableNode
     *            node
     */
    public NodeDiscoveryBridge(final EnhancedLinkd linkd, final Node node) {
        super(linkd, node);
    }

    protected void runCollection() {
        LOG.info("run: start: node discovery operations for bridge: '{}'",
                 getNodeId());
        final Date now = new Date();

        Map<Integer, String> vlanmap = getVtpVlanMap(getPeer());
        if (vlanmap.isEmpty())
            vlanmap.put(null, null);

        List<BridgeMacLink> bft = new ArrayList<BridgeMacLink>();
        Map<Integer, Integer> bridgeifindex = new HashMap<Integer, Integer>();

        String community = getPeer().getReadCommunity();
        for (Entry<Integer, String> entry : vlanmap.entrySet()) {
            LOG.debug("run: cisco vlan collection setting peer community: {} with VLAN {}",
                      community, entry.getKey());
            SnmpAgentConfig peer = getPeer();
            if (entry.getKey() != null)
                peer.setReadCommunity(community + "@" + entry.getKey());
            LOG.debug("run: Bridge Linkd node scan : ready to walk dot1d basedata on {}, vlan {}, vlanname {}.",
                      getNodeId(), entry.getKey(), entry.getValue());

            BridgeElement bridge = getDot1dBridgeBase(peer);
            if (bridge != null) {
                bridge.setVlan(entry.getKey());
                bridge.setVlanname(entry.getValue());
                m_linkd.getQueryManager().store(getNodeId(), bridge);
            } else {
                LOG.debug("run: Bridge Linkd node scan : no dot1d data found on {}, vlan {}, vlanname {}. skipping other operations",
                          getNodeId(), entry.getKey(), entry.getValue());
                continue;
            }

            Map<Integer,Integer> vlanbridgetoifindex = walkDot1dBasePortTable(peer);
            LOG.debug("run: found on node: '{}' vlan: '{}', bridge ifindex map {}",
                      getNodeId(), entry.getValue(), vlanbridgetoifindex);
            if (!isValidStpBridgeId(bridge.getStpDesignatedRoot())) {
                LOG.info("run: invalid Stp designated root: spanning tree not supported on: {}",
                         getNodeId());
            } else if (bridge.getBaseBridgeAddress().equals(getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()))) {
                LOG.info("run: designated root of spanning tree is itself on bridge {}, on: {}",
                         bridge.getStpDesignatedRoot(), getNodeId());
            } else {
                for (BridgeStpLink stplink: walkSpanningTree(peer,
                                 bridge.getBaseBridgeAddress())) {
                    stplink.setVlan(entry.getKey());
                    stplink.setStpPortIfIndex(vlanbridgetoifindex.get(stplink.getStpPort()));
                    m_linkd.getQueryManager().store(getNodeId(), stplink);

                }
            }

            bridgeifindex.putAll(vlanbridgetoifindex);
            bft = walkDot1dTpFdp(entry.getKey(), bridgeifindex, bft, peer);
        }
		
        LOG.debug("run: found on node: '{}' bridge ifindex map {}",
                  getNodeId(), bridgeifindex);
        bft = walkDot1qTpFdb(getPeer(),bridgeifindex, bft);
        LOG.debug("run: bridge: '{}' bft size {}", getNodeId(), bft.size());

        if (bft.size() > 0) {
            LOG.debug("run: updating topology bridge: '{}'", getNodeId());
        	m_linkd.getQueryManager().store(getNodeId(), bft);
        	m_linkd.scheduleBridgeTopologyDiscovery(getNodeId());
        }
        LOG.debug("run: reconciling bridge: '{}' time {}", getNodeId(), now);
        m_linkd.collectedBft(getNodeId());
        m_linkd.getQueryManager().reconcileBridge(getNodeId(), now);
        LOG.info("run: end: node discovery operations for bridge: '{}'",
                 getNodeId());
    }

    private BridgeElement getDot1dBridgeBase(SnmpAgentConfig peer) {
        final Dot1dBaseTracker dot1dbase = new Dot1dBaseTracker();

        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(), dot1dbase).withDescription("dot1dbase").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.info("run: Agent error while scanning the dot1dbase table", e);
            return null; 
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
            return null;
        }

        BridgeElement bridge = dot1dbase.getBridgeElement();
        if (bridge.getBaseBridgeAddress() == null) {
            LOG.info("run: base bridge address is null: bridge mib not supported on: {}",
                     getNodeId());
            return null;
        }

        if (!isValidBridgeAddress(bridge.getBaseBridgeAddress())) {
            LOG.info("run: bridge not supported, base address identifier {} is not valid on: {}",
                     dot1dbase.getBridgeAddress(), getNodeId());
            return null;
        }

        if (bridge.getBaseNumPorts() == null
                || bridge.getBaseNumPorts().intValue() == 0) {
            LOG.info("run: bridge {} has 0 port active, on: {}",
                     dot1dbase.getBridgeAddress(), getNodeId());
            return null;
        }
        LOG.info("run: bridge {} has is if type {}, on: {}",
                 dot1dbase.getBridgeAddress(),
                 BridgeDot1dBaseType.getTypeString(dot1dbase.getBridgeType()),
                 getNodeId());

        if (bridge.getBaseType() == BridgeDot1dBaseType.DOT1DBASETYPE_SOURCEROUTE_ONLY) {
            LOG.info("run: {}: source route only type bridge, on: {}",
                     dot1dbase.getBridgeAddress(), getNodeId());
            return null;
        }
        return bridge;
    }

    private Map<Integer, String> getVtpVlanMap(SnmpAgentConfig peer) {

        final Map<Integer, String> vlanmap = new HashMap<Integer, String>();
        final CiscoVtpTracker vtpStatus = new CiscoVtpTracker();
        
        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(), vtpStatus).
            withDescription("vtpVersion").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.info("run: Agent error while scanning the vtpVersion table", e);
           return vlanmap;
       } catch (final InterruptedException e) {
           LOG.info("run: Bridge Linkd node collection interrupted, exiting",
                     e);
           return vlanmap;
       }

        if (vtpStatus.getVtpVersion() == null) {
            LOG.info("run: cisco vtp mib not supported, on: {}", getNodeId());
            return vlanmap;
        }

        LOG.info("run: cisco vtp mib supported, on: {}", getNodeId());
        LOG.info("run: walking cisco vtp, on: {}", getNodeId());

        final CiscoVtpVlanTableTracker ciscoVtpVlanTableTracker = new CiscoVtpVlanTableTracker() {
            @Override
            public void processCiscoVtpVlanRow(final CiscoVtpVlanRow row) {
                if (row.isTypeEthernet() && row.isStatusOperational()) {
                    vlanmap.put(row.getVlanIndex(), row.getVlanName());
                }
            }
        };
        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(), ciscoVtpVlanTableTracker).
            withDescription("ciscoVtpVlan").
            withLocation(getLocation()).
            execute().
            get();
        } catch (ExecutionException e) {
            LOG.error("run: collection execution failed, exiting",e);
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return vlanmap;
    }

    private Map<Integer, Integer> walkDot1dBasePortTable(SnmpAgentConfig peer) {
        final Map<Integer, Integer> bridgetoifindex = new HashMap<Integer, Integer>();
        Dot1dBasePortTableTracker dot1dBasePortTableTracker = new Dot1dBasePortTableTracker() {
                @Override
                public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
                    bridgetoifindex.put(row.getBaseBridgePort(),
                                        row.getBaseBridgePortIfindex());
                }
            };
    
        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(), dot1dBasePortTableTracker).
            withDescription("dot1dBasePortTable").
            withLocation(getLocation()).
            execute().
            get();
        } catch (ExecutionException e) {
            LOG.error("run: collection execution failed, exiting",e);
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return bridgetoifindex;
    }

    private List<BridgeMacLink> walkDot1dTpFdp(final Integer vlan,
            final Map<Integer, Integer> bridgeifindex,
            List<BridgeMacLink> bft, SnmpAgentConfig peer) {

        Dot1dTpFdbTableTracker dot1dTpFdbTableTracker = new Dot1dTpFdbTableTracker() {

            @Override
            public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
                BridgeMacLink link = row.getLink();
                if (link.getBridgeDot1qTpFdbStatus() == null) {
                    LOG.warn("processDot1dTpFdbRow: row has null status. mac {}: vlan {}: on port {}",
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort());
                    return;
                }
                if (link.getBridgePort() == null) {
                    LOG.warn("processDot1dTpFdbRow: row has null bridge port.  mac {}: vlan {}: on port {} status {}",
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (link.getMacAddress() == null
                        || !isValidBridgeAddress(link.getMacAddress())) {
                    LOG.warn("processDot1dTpFdbRow: row has invalid mac. mac {}: vlan {}: on port {} ifindex {} status {}",
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                link.setVlan(vlan);

                if (!bridgeifindex.containsKey(link.getBridgePort())
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    LOG.warn("processDot1dTpFdbRow: row has invalid bridge port no ifindex found. mac {}: vlan {}: on port {} ifindex {} status {}",
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
                LOG.info("processDot1dTpFdbRow: row processed: mac {}: vlan {}: on port {} ifindex {} status {}",
                         link.getMacAddress(), link.getVlan(),
                         link.getBridgePort(), link.getBridgePortIfIndex(),
                         link.getBridgeDot1qTpFdbStatus());
                bft.add(link);
            }
        };
        
        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(),
                                                      dot1dTpFdbTableTracker).withDescription("dot1dTbFdbPortTable").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.error("run: collection execution failed, exiting", e);
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return bft;
    }

    private List<BridgeMacLink> walkDot1qTpFdb(SnmpAgentConfig peer,
            final Map<Integer, Integer> bridgeifindex,
            final List<BridgeMacLink> bft) {

        Dot1qTpFdbTableTracker dot1qTpFdbTableTracker = new Dot1qTpFdbTableTracker() {

            @Override
            public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
                BridgeMacLink link = row.getLink();
                if (link.getBridgeDot1qTpFdbStatus() == null) {
                    LOG.warn("processDot1qTpFdbRow: row has null status. mac {}: on port {}",
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort());
                    return;
                }
                if (link.getBridgePort() == null) {
                    LOG.warn("processDot1qTpFdbRow: row has null bridge port.  mac {}: on port {} status {}",
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (link.getMacAddress() == null
                        || !isValidBridgeAddress(link.getMacAddress())) {
                    LOG.warn("processDot1qTpFdbRow: row has invalid mac. mac {}: on port {} ifindex {} status {}",
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (!bridgeifindex.containsKey(link.getBridgePort())
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    LOG.warn("processDot1qTpFdbRow: row has invalid bridgeport no ifindex found. mac {}: on port {} ifindex {} status {}",
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }

                link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
                LOG.info("processDot1qTpFdbRow: row processed: mac {}: vlan {}: on port {} ifindex {} status {}",
                         link.getMacAddress(), link.getVlan(),
                         link.getBridgePort(), link.getBridgePortIfIndex(),
                         link.getBridgeDot1qTpFdbStatus());
                bft.add(link);
            }

        };
        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(),
                                                      dot1qTpFdbTableTracker).withDescription("dot1qTbFdbPortTable").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.error("run: collection execution failed, exiting", e);
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return bft;
    }

    private List<BridgeStpLink> walkSpanningTree(SnmpAgentConfig peer,
            final String baseBridgeAddress) {

        final List<BridgeStpLink> stplinks = new ArrayList<BridgeStpLink>();
        Dot1dStpPortTableTracker stpPortTableTracker = new Dot1dStpPortTableTracker() {

            @Override
            public void processDot1dStpPortRow(final Dot1dStpPortRow row) {
                BridgeStpLink link = row.getLink();
                if (isValidStpBridgeId(link.getDesignatedRoot())
                        && isValidStpBridgeId(link.getDesignatedBridge())
                        && !baseBridgeAddress.equals(link.getDesignatedBridgeAddress())) {
                    stplinks.add(link);
                }
            }
        };

        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(),
                                                      stpPortTableTracker).withDescription("dot1dStpPortTable").withLocation(getLocation()).execute().get();
        } catch (ExecutionException e) {
            LOG.error("run: collection execution failed, exiting", e);
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return stplinks;
    }

    @Override
    public String getInfo() {
        return "ReadyRunnable BridgeLinkNodeDiscovery" + " ip="
                + str(getTarget()) + " port=" + getPort() + " community="
                + getReadCommunity() + " package=" + getPackageName();
    }

    @Override
    public String getName() {
        return "BridgeLinkDiscovery";
    }

    @Override
    public boolean isReady() {
        return m_linkd.collectBft(getNodeId());
    }
}
