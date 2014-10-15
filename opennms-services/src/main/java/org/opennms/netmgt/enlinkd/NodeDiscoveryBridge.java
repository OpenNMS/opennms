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

import java.util.Date;
import java.util.HashMap;
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
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.LinkableSnmpNode;

import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and collection occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 */
public final class NodeDiscoveryBridge extends NodeDiscovery {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridge.class);

	//public final static String CISCO_ENTERPRISE_OID = ".1.3.6.1.4.1.9";

	/**
	 * Constructs a new SNMP collector for Bridge Node Discovery. The collection
	 * does not occur until the <code>run</code> method is invoked.
	 * 
	 * @param EnhancedLinkd
	 *            linkd
	 * @param LinkableNode
	 *            node
	 */
	public NodeDiscoveryBridge(final EnhancedLinkd linkd,
			final LinkableSnmpNode node) {
		super(linkd, node);
	}
	
	protected void runCollection() {

		final Date now = new Date();

		LOG.debug("run: collecting: {}", getPeer());
		Map<Integer,String> vlanmap = getVtpVlanMap();
		Map<Integer,Integer> bridgeifindex = new HashMap<Integer, Integer>();
		
		if (vlanmap.isEmpty())
			bridgeifindex.putAll(walkDot1d(null,null));
		else {
			String community = getPeer().getReadCommunity();
			for (Entry<Integer, String> entry: vlanmap.entrySet()) {
				LOG.debug("run: cisco vlan collection setting peer community: {} with VLAN {}",
						community, entry.getKey());
				getPeer().setReadCommunity(community + "@" + entry.getKey());
				bridgeifindex.putAll(walkDot1d(entry.getKey(), entry.getValue()));
			}
			getPeer().setReadCommunity(community);
		}
		walkDot1qTpFdp(bridgeifindex);
		m_linkd.getQueryManager().reconcileBridge(getNodeId(), now);
	}
	
	private Map<Integer,String> getVtpVlanMap() {
		
		final Map<Integer,String> vlanmap = new HashMap<Integer, String>();
		String trackerName = "vtpVersion";
		final CiscoVtpTracker vtpStatus = new CiscoVtpTracker();
		SnmpWalker walker = SnmpUtils.createWalker(getPeer(), trackerName,
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
			LOG.error("run: Bridge Linkd node collection interrupted, exiting", e);
			return vlanmap;
		}

		if (vtpStatus.getVtpVersion() == null) {
			LOG.info("run: cisco vtp mib not supported, on: {}",
					str(getPeer().getAddress()));
			return vlanmap;
		}

		LOG.info("run: cisco vtp mib supported, on: {}", str(getPeer()
				.getAddress()));
		LOG.info("run: walking cisco vtp, on: {}", str(getPeer()
				.getAddress()));

		trackerName = "ciscoVtpVlan";
		final CiscoVtpVlanTableTracker ciscoVtpVlanTableTracker = new CiscoVtpVlanTableTracker() {
			@Override
			public void processCiscoVtpVlanRow(final CiscoVtpVlanRow row) {
				if (row.isTypeEthernet() && row.isStatusOperational()) {
					vlanmap.put(row.getVlanIndex(), row.getVlanName());
				}
			}
		};
		walker = SnmpUtils.createWalker(getPeer(), trackerName,
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
			LOG.error("run: Bridge Linkd node collection interrupted, exiting",e);
		}
		return vlanmap;
	}

	protected Map<Integer,Integer> walkDot1d(Integer vlan, String vlanname) {
		LOG.debug("run: Bridge Linkd node scan : ready to walk dot1d data on {}, vlan {}, vlanname {}.",
				str(getPeer().getAddress()),vlan,vlanname);
		String trackerName = "dot1dbase";
		final Dot1dBaseTracker dot1dbase = new Dot1dBaseTracker();
		SnmpWalker walker = SnmpUtils.createWalker(getPeer(), trackerName,
				dot1dbase);
		walker.start();

		try {
			walker.waitFor();
			if (walker.timedOut()) {
				LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
						trackerName);
				return new HashMap<Integer, Integer>();
			} else if (walker.failed()) {
				LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
						trackerName, walker.getErrorMessage());
				return new HashMap<Integer, Integer>();
			}
		} catch (final InterruptedException e) {
			LOG.error("run: Bridge Linkd node collection interrupted, exiting",e);
			return new HashMap<Integer, Integer>();
		}

		BridgeElement bridge = dot1dbase.getBridgeElement();
		bridge.setVlan(vlan);
		bridge.setVlanname(vlanname);
		if (bridge.getBaseBridgeAddress() == null) {
			LOG.info("run: base bridge address is null: bridge mib not supported on: {}",
					str(getPeer().getAddress()));
			return new HashMap<Integer, Integer>();
		}

		if (!isValidBridgeAddress(bridge.getBaseBridgeAddress())) {
			LOG.info("run: bridge not supported, base address identifier {} is not valid on: {}",
					dot1dbase.getBridgeAddress(), str(getPeer().getAddress()));
			return new HashMap<Integer, Integer>();
		}

		if (bridge.getBaseNumPorts() == 0) {
			LOG.info("run: bridge {} has 0 port active, on: {}",
					dot1dbase.getBridgeAddress(), str(getPeer().getAddress()));
			return new HashMap<Integer, Integer>();
		}
		LOG.info("run: bridge {} has is if type {}, on: {}", dot1dbase
				.getBridgeAddress(), BridgeDot1dBaseType.getTypeString(dot1dbase.getBridgeType()),str(getPeer().getAddress()));

		if (bridge.getBaseType() ==  BridgeDot1dBaseType.DOT1DBASETYPE_SOURCEROUTE_ONLY) {
			LOG.info("run: {}: source route only type bridge, on: {}",
					dot1dbase.getBridgeAddress(), str(getPeer().getAddress()));
			return new HashMap<Integer, Integer>();
		}
		m_linkd.getQueryManager().store(getNodeId(), bridge);
		
		Map<Integer,Integer> bridgetoifindex = walkDot1dBasePortTable();

		if (!isValidStpBridgeId(bridge.getStpDesignatedRoot())) {
			LOG.info("run: invalid Stp designated root: spanning tree not supported on: {}",
					str(getPeer().getAddress()));
		} else if (bridge.getBaseBridgeAddress().equals(getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()))) {
			LOG.info("designated root of spanning tree is itself on bridge {}, on: {}",
					bridge.getStpDesignatedRoot(),
					str(getPeer().getAddress()));
		} else {
			walkSpanningTree(bridge.getBaseBridgeAddress(),vlan, bridgetoifindex);
		}		
		walkDot1dTpFdp(vlan,bridgetoifindex);
		return bridgetoifindex;
	}

	private Map<Integer,Integer> walkDot1dBasePortTable() {
		final Map<Integer,Integer> bridgetoifindex = new HashMap<Integer, Integer>();
		String trackerName = "dot1dBasePortTable";
		Dot1dBasePortTableTracker dot1dBasePortTableTracker = new Dot1dBasePortTableTracker() {
			@Override
			public void processDot1dBasePortRow(final Dot1dBasePortRow row) {
				bridgetoifindex.put(row.getBaseBridgePort(), row.getBaseBridgePortIfindex());
			}
		};
		
		SnmpWalker walker = SnmpUtils.createWalker(getPeer(), trackerName,
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
			LOG.error("run: Bridge Linkd node collection interrupted, exiting",e);
		}
		return bridgetoifindex;
	}
	
	private void walkDot1dTpFdp(final Integer vlan, final Map<Integer,Integer> bridgeifindex) {
		String trackerName = "dot1dTbFdbPortTable";

		Dot1dTpFdbTableTracker stpPortTableTracker = new Dot1dTpFdbTableTracker() {

			@Override
			public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
				BridgeMacLink link = row.getLink();
				link.setVlan(vlan);
				link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
				if (isValidBridgeAddress(link.getMacAddress())
						&& link.getBridgeDot1qTpFdbStatus() == BridgeMacLink.BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED)
					m_linkd.getQueryManager().store(getNodeId(), link);
			}
		};
		SnmpWalker walker = SnmpUtils.createWalker(getPeer(), trackerName,
				stpPortTableTracker);
		walker.start();

		try {
			walker.waitFor();
			if (walker.timedOut()) {
				LOG.info("run:Aborting Bridge Linkd node scan : Agent timed out while scanning the {} table",
						trackerName);
				return;
			} else if (walker.failed()) {
				LOG.info("run:Aborting Bridge Linkd node scan : Agent failed while scanning the {} table: {}",
						trackerName, walker.getErrorMessage());
				return;
			}
		} catch (final InterruptedException e) {
			LOG.error("run: Bridge Linkd node collection interrupted, exiting",e);
			return;
		}
	}

	private void walkDot1qTpFdp(final Map<Integer,Integer> bridgeifindex) {

		String trackerName = "dot1qTbFdbPortTable";

		Dot1qTpFdbTableTracker dot1qTpFdbTableTracker = new Dot1qTpFdbTableTracker() {

			@Override
			public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
				BridgeMacLink link = row.getLink();
				link.setBridgePortIfIndex(bridgeifindex.get(link.getBridgePort()));
				if (isValidBridgeAddress(link.getMacAddress()) && link.getBridgePort() != null
						&& link.getBridgeDot1qTpFdbStatus() == BridgeMacLink.BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED)
					m_linkd.getQueryManager().store(getNodeId(), link);
			}

		};
		SnmpWalker walker = SnmpUtils.createWalker(getPeer(), trackerName,
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
			LOG.error("run: Bridge Linkd node collection interrupted, exiting",e);
		}
	}

	private void walkSpanningTree(final String baseBridgeAddress, final Integer vlan, final Map<Integer,Integer> bridgeifindex) {

		String trackerName = "dot1dStpPortTable";

		Dot1dStpPortTableTracker stpPortTableTracker = new Dot1dStpPortTableTracker() {
			
			@Override
			public void processDot1dStpPortRow(final Dot1dStpPortRow row) {
				BridgeStpLink link = row.getLink();
				link.setVlan(vlan);
				link.setStpPortIfIndex(bridgeifindex.get(link.getStpPort()));
				if (!baseBridgeAddress.equals(link.getDesignatedBridgeAddress())) {
					m_linkd.getQueryManager().store(getNodeId(),link);
				}
			}
		};

		SnmpWalker walker = SnmpUtils.createWalker(getPeer(), trackerName,
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
			LOG.error("run: Bridge Linkd node collection interrupted, exiting",e);
		}
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

	
}
