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

package org.opennms.netmgt.linkd;

import java.net.InetAddress;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dBasePortTable;
import org.opennms.netmgt.linkd.snmp.Dot1dStpGroup;
import org.opennms.netmgt.linkd.snmp.Dot1dStpPortTable;
import org.opennms.netmgt.linkd.snmp.Dot1dTpFdbTable;
import org.opennms.netmgt.linkd.snmp.QBridgeDot1dTpFdbTable;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and collection occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 *  
 */
public final class SnmpVlanCollection implements ReadyRunnable {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpVlanCollection.class);
	private String m_packageName;
	
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private final SnmpAgentConfig m_agentConfig;

	/**
	 * The IP address to used to collect the SNMP information
	 */
	private final InetAddress m_address;

	/**
	 * The Dot1DBridge.dot1dbase map
	 */
	private Dot1dBaseGroup m_dot1dBase;

	/**
	 * The Dot1DBridge Ids List
	 */
	private Dot1dBasePortTable m_dot1dBaseTable;

	/**
	 * The Dot1dStp base map
	 */
	private Dot1dStpGroup m_dot1dStp;

	/**
	 * The Dot1dStp Port table List
	 */
	private Dot1dStpPortTable m_dot1dStpTable;

	/**
	 * The Dot1dTpFdb table List
	 */
	private Dot1dTpFdbTable m_dot1dTpFdbTable;

	/**
	 * The QbridgeDot1dTpFdb table List
	 */
	private QBridgeDot1dTpFdbTable m_dot1qTpFdbTable;


	private boolean m_collectStp = true;
		
	private boolean m_collectBridge = true;

	/**
	 * Constructs a new SNMP collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param agentConfig
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	SnmpVlanCollection(final SnmpAgentConfig agentConfig) {
		m_agentConfig = agentConfig;
		m_address = m_agentConfig.getEffectiveAddress();
		m_dot1dBase = null;
		m_dot1dBaseTable = null;
		m_dot1dStp = null;
		m_dot1dStpTable = null;
		m_dot1dTpFdbTable = null;
	}
	// for debug only

	/**
	 * Constructs a new SNMP collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param agentConfig
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	SnmpVlanCollection(final SnmpAgentConfig agentConfig, final boolean collectStp, final boolean collectBridge) {
		m_agentConfig = agentConfig;
		m_address = m_agentConfig.getEffectiveAddress();
		m_dot1dBase = null;
		m_dot1dBaseTable = null;
		m_dot1dStp = null;
		m_dot1dStpTable = null;
		m_dot1dTpFdbTable = null;
		m_collectStp = collectStp;
		m_collectBridge = collectBridge;
	}

	/**
	 * Returns true if any part of the collection failed.
	 */
	boolean failed() {
		return !hasDot1dBase() && !hasDot1dBasePortTable() && !hasDot1dStp() && !hasDot1dStpPortTable() && !hasDot1dTpFdbTable();
	}

	/**
	 * Returns true if any part of the collection failed.
	 */
	boolean someCollectionFailed() {
		return !hasDot1dBase() || !hasDot1dBasePortTable() || !hasDot1dStp() || !hasDot1dStpPortTable() || !hasDot1dTpFdbTable();
	}

	/**
	 * Returns true if the dot1DBridge table was collected.
	 */
	boolean hasDot1dBase() {
		return (m_dot1dBase != null && !m_dot1dBase.failed() && m_dot1dBase.getBridgeAddress() != null);
	}

	/**
	 * Returns the collected dot1base.
	 */
	Dot1dBaseGroup getDot1dBase() {
		return m_dot1dBase;
	}

	/**
	 * Returns true if the dot1DBridge table was collected.
	 */
	boolean hasDot1dBasePortTable() {
		return (m_dot1dBaseTable != null && !m_dot1dBaseTable.failed() && !m_dot1dBaseTable.isEmpty());
	}

	/**
	 * Returns the collected dot1dbridge.
	 */
	Dot1dBasePortTable getDot1dBasePortTable() {
		return m_dot1dBaseTable;
	}

	/**
	 * Returns true if the dot1DStp info was collected.
	 */
	boolean hasDot1dStp() {
		return (m_dot1dStp != null && !m_dot1dStp.failed() && m_dot1dStp.getStpDesignatedRoot() != null);
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dStpGroup getDot1dStp() {
		return m_dot1dStp;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasDot1dStpPortTable() {
		return (m_dot1dStpTable != null && !m_dot1dStpTable.failed() && !m_dot1dStpTable.isEmpty());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dStpPortTable getDot1dStpPortTable() {
		return m_dot1dStpTable;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasDot1dTpFdbTable() {
		return (m_dot1dTpFdbTable != null && !m_dot1dTpFdbTable.failed() && !m_dot1dTpFdbTable.isEmpty());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dTpFdbTable getDot1dFdbTable() {
		return m_dot1dTpFdbTable;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasQBridgeDot1dTpFdbTable() {
		return (m_dot1qTpFdbTable!= null && !m_dot1qTpFdbTable.failed() && !m_dot1qTpFdbTable.isEmpty());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	QBridgeDot1dTpFdbTable getQBridgeDot1dFdbTable() {
		return m_dot1qTpFdbTable;
	}


	/**
	 * Returns the target address that the collection occured for.
	 */

	InetAddress getTarget() {
		return m_address;
	}

	/**
	 * <p>
	 * Performs the collection for the targeted internet address. The success or
	 * failure of the collection should be tested via the <code>failed</code>
	 * method.
	 * </p>
	 *
	 * <p>
	 * No synchronization is performed, so if this is used in a separate thread
	 * context synchornization must be added.
	 * </p>
	 */
        @Override
	public void run() {
			m_dot1dBase = new Dot1dBaseGroup(m_address);
			m_dot1dBaseTable = new Dot1dBasePortTable(m_address);
			m_dot1dStp = new Dot1dStpGroup(m_address);
			m_dot1dStpTable = new Dot1dStpPortTable(m_address);
			m_dot1dTpFdbTable = new Dot1dTpFdbTable(m_address);
			
			SnmpWalker walker = null;
			
			if (m_collectBridge && m_collectStp) {
		        walker = SnmpUtils.createWalker(m_agentConfig, "dot1dBase/dot1dBaseTable/dot1dStp/dot1dStpTable/dot1dTpFdbTable ", 
		        			new CollectionTracker[] { m_dot1dBase, m_dot1dBaseTable, m_dot1dStp, m_dot1dStpTable, m_dot1dTpFdbTable});
			} else if(m_collectBridge) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dTpFdbTable ", 
		        			new CollectionTracker[] {m_dot1dTpFdbTable});
			} else if(m_collectStp) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBase/dot1dStp/dot1dBaseTable/dot1dStpTable ", 
		        			new CollectionTracker[] { m_dot1dBase, m_dot1dStp, m_dot1dBaseTable, m_dot1dStpTable});
			} else {
			    LOG.info("run: no info to collect return");
				return;
			}

			
	        walker.start();

	        try {
	            walker.waitFor();
	        } catch (final InterruptedException e) {
				m_dot1dBase = null;
				m_dot1dBaseTable = null;
				m_dot1dStp = null;
				m_dot1dStpTable = null;
				m_dot1dTpFdbTable = null;

				LOG.warn("SnmpVlanCollection.run: collection interrupted, exiting", e);
	            return;
	        }
	        


			// Log any failures
			//
			final String hostAddress = InetAddressUtils.str(m_address);
			if (!hasDot1dBase())
			    LOG.info("run: failed to collect Dot1dBase for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
			if (!hasDot1dBasePortTable())
                LOG.info("run: failed to collect Dot1dBasePortTable for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
			if (!hasDot1dStp())
                LOG.info("run: failed to collect Dot1dStp for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
			if (!hasDot1dStpPortTable())
                LOG.info("run: failed to collect Dot1dStpPortTable for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
			if (!hasDot1dTpFdbTable())
                LOG.info("run: failed to collect Dot1dTpFdbTable for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
			
			//if not found macaddresses forwarding table find it in Qbridge
			//ExtremeNetwork works.....
			
			if (m_dot1dTpFdbTable.isEmpty() && m_collectBridge) {
			    LOG.info("run: Trying to collect QbridgeDot1dTpFdbTable for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
				m_dot1qTpFdbTable = new QBridgeDot1dTpFdbTable(m_address);
		        walker =  SnmpUtils.createWalker(m_agentConfig, "qBridgedot1dTpFdbTable ", new CollectionTracker[] { m_dot1qTpFdbTable });
		        walker.start();

		        try {
		            walker.waitFor();
		        } catch (final InterruptedException e) {
					m_dot1qTpFdbTable = null;
					LOG.warn("SnmpVlanCollection.run: collection interrupted", e);
		            
		        }

		        if (!hasQBridgeDot1dTpFdbTable()) {
		            LOG.info("run: failed to collect QBridgeDot1dTpFdbTable for {} Community: {}", hostAddress, m_agentConfig.getReadCommunity());
		        }
			}
	}

	/**
	 * <p>isReady</p>
	 *
	 * @return a boolean.
	 */
        @Override
	public boolean isReady() {
		return true;
	}

	@Override
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("agentConfig", m_agentConfig)
	        .append("address", m_address)
	        .append("dot1dBase", m_dot1dBase)
	        .append("dot1dBaseTable", m_dot1dBaseTable)
	        .append("dot1dStp", m_dot1dStp)
	        .append("dot1dStpTable", m_dot1dStpTable)
	        .append("dot1dTpFdbTable", m_dot1dTpFdbTable)
	        .append("dot1qTpFdbTable", m_dot1qTpFdbTable)
	        .toString();
	}

	public void setPackageName(String packageName) {
		m_packageName = packageName;
	}
	
	public String getPackageName() {
		return m_packageName;
	}
}
