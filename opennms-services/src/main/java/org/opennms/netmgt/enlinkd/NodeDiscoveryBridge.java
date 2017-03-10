/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero
 * General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/ For more information contact: OpenNMS(R)
 * Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.isValidBridgeAddress;
import static org.opennms.core.utils.InetAddressUtils.isValidStpBridgeId;
import static org.opennms.core.utils.InetAddressUtils.getBridgeAddressFromStpBridgeId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
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
        LOG.info("run: start discovery operations for node: [{}]",
                 getNodeId());
        final Date now = new Date();

        Map<Integer, String> vlanmap = getVtpVlanMap(getPeer());
        if (vlanmap.isEmpty())
            vlanmap.put(null, null);

        List<BridgeMacLink> bft = new ArrayList<BridgeMacLink>();
        Map<Integer, Integer> bridgeifindex = new HashMap<Integer, Integer>();

        String community = getPeer().getReadCommunity();
        for (Entry<Integer, String> entry : vlanmap.entrySet()) {
            LOG.debug("run: node [{}] cisco vtp: setting peer community for VLAN {}",
            		 getNodeId(),
                      entry.getValue());
            SnmpAgentConfig peer = getPeer();
            if (entry.getKey() != null)
                peer.setReadCommunity(community + "@" + entry.getKey());
            LOG.debug("run: walking dot1d basedata on node [{}}, vlan [{}], vlanname {}.",
                      getNodeId(), entry.getKey(), entry.getValue());

            BridgeElement bridge = getDot1dBridgeBase(peer);
            if (bridge != null) {
                bridge.setVlan(entry.getKey());
                bridge.setVlanname(entry.getValue());
                m_linkd.getQueryManager().store(getNodeId(), bridge);
            } else {
                LOG.debug("run: no dot1d data found on node [{}], vlan [{}], vlanname {}. skipping other operations",
                          getNodeId(), entry.getKey(), entry.getValue());
                continue;
            }

            Map<Integer,Integer> vlanbridgetoifindex = walkDot1dBasePortTable(peer);
            LOG.debug("run: found on node: [{}] vlan: [{}], bridge ifindex map {}",
                      getNodeId(), entry.getValue(), vlanbridgetoifindex);
            if (!isValidStpBridgeId(bridge.getStpDesignatedRoot())) {
                LOG.info("run: node [{}]: invalid designated root: spanning tree not supported.",
                         getNodeId());
            } else if (bridge.getBaseBridgeAddress().equals(getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()))) {
                LOG.info("run: node [{}]: designated root {} is itself. Skipping store.",
                		 getNodeId(),
                         bridge.getStpDesignatedRoot());
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
		
        LOG.debug("run: node [{}]: bridge ifindex map {}",
                  getNodeId(), bridgeifindex);
        bft = walkDot1qTpFdb(getPeer(),bridgeifindex, bft);
        LOG.debug("run: node [{}]: bft size:{}", getNodeId(), bft.size());

        if (bft.size() > 0) {
            LOG.debug("run: node [{}]: updating topology", getNodeId());
        	m_linkd.getQueryManager().store(getNodeId(), bft);
        	m_linkd.scheduleBridgeTopologyDiscovery(getNodeId());
        }
        LOG.debug("run: node [{}]: deleting older the time {}", getNodeId(), now);
        m_linkd.collectedBft(getNodeId());
        m_linkd.getQueryManager().reconcileBridge(getNodeId(), now);
        LOG.info("run: end: node discovery operations for bridge: '{}'",
                 getNodeId());
    }

    private BridgeElement getDot1dBridgeBase(SnmpAgentConfig peer) {
        String trackerName = "dot1dbase";
        final Dot1dBaseTracker dot1dbase = new Dot1dBaseTracker();
        SnmpWalker walker = SnmpUtils.createWalker(peer, trackerName,
                                                   dot1dbase);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
                return null;
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
                return null;
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
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

        if (bridge.getBaseNumPorts() == null
                || bridge.getBaseNumPorts().intValue() == 0) {
            LOG.info("run: node [{}]: base bridge address {}: has 0 port active. BRIDGE_MIB not supported",
            		getNodeId(),dot1dbase.getBridgeAddress());
            return null;
        }
        LOG.info("run: bridge {} has is if type {}, on: {}",
                 dot1dbase.getBridgeAddress(),
                 BridgeDot1dBaseType.getTypeString(dot1dbase.getBridgeType()),
                 getNodeId());

        if (bridge.getBaseType() == BridgeDot1dBaseType.DOT1DBASETYPE_SOURCEROUTE_ONLY) {
            LOG.info("run: node [{}]: base bridge address {}: is source route bridge only. BRIDGE_MIB not supported",
            		getNodeId(),dot1dbase.getBridgeAddress());
            return null;
        }
        return bridge;
    }

    private Map<Integer, String> getVtpVlanMap(SnmpAgentConfig peer) {

        final Map<Integer, String> vlanmap = new HashMap<Integer, String>();
        String trackerName = "vtpVersion";
        final CiscoVtpTracker vtpStatus = new CiscoVtpTracker();
        SnmpWalker walker = SnmpUtils.createWalker(peer, trackerName,
                                                   vtpStatus);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
                return vlanmap;
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
                return vlanmap;
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
            return vlanmap;
        }

        if (vtpStatus.getVtpVersion() == null) {
            LOG.info("run: node [{}]: cisco vtp mib not supported.", getNodeId());
            return vlanmap;
        }

        LOG.info("run: node [{}]: cisco vtp mib supported.", getNodeId());
        LOG.debug("run: node [{}]: walking cisco vtp.", getNodeId());

        trackerName = "ciscoVtpVlan";
        final CiscoVtpVlanTableTracker ciscoVtpVlanTableTracker = new CiscoVtpVlanTableTracker() {
            @Override
            public void processCiscoVtpVlanRow(final CiscoVtpVlanRow row) {
                if (row.isTypeEthernet() && row.isStatusOperational()) {
                    vlanmap.put(row.getVlanIndex(), row.getVlanName());
                }
            }
        };
        walker = SnmpUtils.createWalker(peer, trackerName,
                                        ciscoVtpVlanTableTracker);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return vlanmap;
    }

    private Map<Integer, Integer> walkDot1dBasePortTable(SnmpAgentConfig peer) {
        final Map<Integer, Integer> bridgetoifindex = new HashMap<Integer, Integer>();
        String trackerName = "dot1dBasePortTable";
        Dot1dBasePortTableTracker dot1dBasePortTableTracker = new Dot1dBasePortTableTracker() {
            @Override
            public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
                bridgetoifindex.put(row.getBaseBridgePort(),
                                    row.getBaseBridgePortIfindex());
            }
        };

        SnmpWalker walker = SnmpUtils.createWalker(peer, trackerName,
                                                   dot1dBasePortTableTracker);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return bridgetoifindex;
    }

    private List<BridgeMacLink> walkDot1dTpFdp(final Integer vlan,
            final Map<Integer, Integer> bridgeifindex,
            List<BridgeMacLink> bft, SnmpAgentConfig peer) {
        String trackerName = "dot1dTbFdbPortTable";

        Dot1dTpFdbTableTracker stpPortTableTracker = new Dot1dTpFdbTableTracker() {

        	
            @Override
            public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
                BridgeMacLink link = row.getLink();
                if (link.getBridgeDot1qTpFdbStatus() == null) {
                    LOG.warn("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {}. row has null status. ",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort());
                    return;
                }
                if (link.getBridgePort() == null) {
                    LOG.warn("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} status {}. row has null bridge port.  ",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (link.getMacAddress() == null
                        || !isValidBridgeAddress(link.getMacAddress())) {
                    LOG.warn("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} ifindex {} status {}. row has invalid mac.",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                link.setVlan(vlan);

                if (!bridgeifindex.containsKey(link.getBridgePort())
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    LOG.warn("processDot1dTpFdbRow: node [{}]: mac {}: vlan {}: on port {} ifindex {} status {}. row has invalid bridge port. no ifindex found. ",
                    		getNodeId(),
                             row.getDot1dTpFdbAddress(), vlan,
                             row.getDot1dTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
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
        SnmpWalker walker = SnmpUtils.createWalker(peer, trackerName,
                                                   stpPortTableTracker);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
                return bft;
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
                return bft;
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
            return bft;
        }
        return bft;
    }

    private List<BridgeMacLink> walkDot1qTpFdb(SnmpAgentConfig peer,
            final Map<Integer, Integer> bridgeifindex,
            final List<BridgeMacLink> bft) {

        String trackerName = "dot1qTbFdbPortTable";

        Dot1qTpFdbTableTracker dot1qTpFdbTableTracker = new Dot1qTpFdbTableTracker() {

            @Override
            public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
                BridgeMacLink link = row.getLink();

                if (link.getBridgeDot1qTpFdbStatus() == null) {
                    LOG.warn("processDot1qTpFdbRow: node [{}]: mac {}: on port {}. row has null status.",
                    		getNodeId(),
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort());
                    return;
                }
                if (link.getBridgePort() == null) {
                    LOG.warn("processDot1qTpFdbRow: node [{}]: mac {}: on port {} status {}. row has null bridge port.",
                    		getNodeId(),
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (link.getMacAddress() == null
                        || !isValidBridgeAddress(link.getMacAddress())) {
                    LOG.warn("processDot1qTpFdbRow: node [{}]: mac {}: on port {} ifindex {} status {}. row has invalid mac.",
                    		getNodeId(),
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }
                if (!bridgeifindex.containsKey(link.getBridgePort())
                        && link.getBridgeDot1qTpFdbStatus() != BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_SELF) {
                    LOG.warn("processDot1qTpFdbRow: node [{}]: mac {}: on port {} ifindex {} status {}. row has invalid bridgeport. No ifindex found.",
                    		getNodeId(),
                             row.getDot1qTpFdbAddress(),
                             row.getDot1qTpFdbPort(),
                             link.getBridgePortIfIndex(),
                             link.getBridgeDot1qTpFdbStatus());
                    return;
                }

                link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
                LOG.debug("processDot1qTpFdbRow: node [{}]: mac {}: vlan {}: on port {} ifindex {} status {}. row processed.",
                		getNodeId(),
                         link.getMacAddress(), link.getVlan(),
                         link.getBridgePort(), link.getBridgePortIfIndex(),
                         link.getBridgeDot1qTpFdbStatus());
                bft.add(link);
            }

        };
        SnmpWalker walker = SnmpUtils.createWalker(peer, trackerName,
                                                   dot1qTpFdbTableTracker);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
            return bft;
        }
        return bft;
    }

    private List<BridgeStpLink> walkSpanningTree(SnmpAgentConfig peer, final String baseBridgeAddress) {

        String trackerName = "dot1dStpPortTable";

        final List<BridgeStpLink> stplinks = new ArrayList<BridgeStpLink>();
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

        SnmpWalker walker = SnmpUtils.createWalker(peer, trackerName,
                                                   stpPortTableTracker);
        walker.start();

        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
                         trackerName);
            } else if (walker.failed()) {
                LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
                         trackerName, walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            LOG.error("run: Bridge Linkd node collection interrupted, exiting",
                      e);
        }
        return stplinks;
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
