/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

	public final static String CISCO_ENTERPRISE_OID = ".1.3.6.1.4.1.9";

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

		Map<Integer,String> vlanmap = getVtpVlanMap();
		
		if (vlanmap.isEmpty())
			walkBridge(null,null);
		else {
			for (Entry<Integer, String> entry: vlanmap.entrySet()) {
				String community = getPeer().getReadCommunity();
				LOG.debug("run: cisco vlan collection setting peer community: {} with VLAN {}",
						community, entry.getKey());
				getPeer().setReadCommunity(community + "@" + entry.getKey());
				walkBridge(entry.getKey(), entry.getValue());
				getPeer().setReadCommunity(community);
			}
		}

		m_linkd.getQueryManager().reconcileBridge(getNodeId(), now);
		LOG.debug("run: collecting: {}", getPeer());
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

	protected void walkBridge(Integer vlan, String vlanname) {
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

		BridgeElement bridge = dot1dbase.getBridgeElement();
		bridge.setVlan(vlan);
		bridge.setVlanname(vlanname);
		if (bridge.getBaseBridgeAddress() == null) {
			LOG.info("bridge mib not supported on: {}",
					str(getPeer().getAddress()));
			return;
		}

		if (isValidBridgeAddress(bridge.getBaseBridgeAddress())) {
			LOG.info("bridge not supported, base address identifier {} is not valid on: {}",
					dot1dbase.getBridgeAddress(), str(getPeer().getAddress()));
			return;
		}

		if (bridge.getBaseNumPorts() == 0) {
			LOG.info("bridge {} has 0 port active, on: {}",
					dot1dbase.getBridgeAddress(), str(getPeer().getAddress()));
			return;
		}
		LOG.info("bridge {} has is if type {}, on: {}", dot1dbase
				.getBridgeAddress(), BridgeDot1dBaseType.getTypeString(dot1dbase.getBridgeType()));

		if (bridge.getBaseType() ==  BridgeDot1dBaseType.DOT1DBASETYPE_SOURCEROUTE_ONLY) {
			LOG.info("{}: source route only type bridge, on: {}",
					dot1dbase.getBridgeAddress(), str(getPeer().getAddress()));
			return;
		}
		m_linkd.getQueryManager().store(getNodeId(), bridge);
		
		if (!isValidStpBridgeId(bridge.getStpDesignatedRoot())) {
			LOG.info("spanning tree not supported on: {}",
					str(getPeer().getAddress()));
		} else if (bridge.getStpProtocolSpecification() != 3) {
			LOG.info("ieee8021d spanning tree not supported on bridge {}, on: {}",
					bridge.getStpDesignatedRoot(),
					str(getPeer().getAddress()));
		} else if (bridge.getBaseBridgeAddress().equals(getBridgeAddressFromStpBridgeId(bridge.getStpDesignatedRoot()))) {
			LOG.info("designated root of spanning tree is itself on bridge {}, on: {}",
					bridge.getStpDesignatedRoot(),
					str(getPeer().getAddress()));
		} else {
			walkSpanningTree(bridge.getBaseBridgeAddress(),vlan);
		}		
		walkDot1dTpFdp(vlan);
		walkDot1qTpFdp();
	}

	private void walkDot1dTpFdp(final Integer vlan) {
		String trackerName = "dot1dTbFdbPortTable";

		Dot1dTpFdbTableTracker stpPortTableTracker = new Dot1dTpFdbTableTracker() {

			@Override
			public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
				BridgeMacLink link = row.getLink();
				link.setVlan(vlan);
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

	private void walkDot1qTpFdp() {

		String trackerName = "dot1qTbFdbPortTable";

		Dot1qTpFdbTableTracker dot1qTpFdbTableTracker = new Dot1qTpFdbTableTracker() {

			@Override
			public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
				BridgeMacLink link = row.getLink();
				if (isValidBridgeAddress(link.getMacAddress())
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

	private void walkSpanningTree(final String baseBridgeAddress, final Integer vlan) {

		String trackerName = "dot1dStpPortTable";

		Dot1dStpPortTableTracker stpPortTableTracker = new Dot1dStpPortTableTracker() {
			
			@Override
			public void processDot1dStpPortRow(final Dot1dStpPortRow row) {
				BridgeStpLink link = row.getLink();
				link.setVlan(vlan);
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
