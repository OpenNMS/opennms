//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.linkd;

import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;
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
 * and colletion occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 *  
 */
final class SnmpVlanCollection implements ReadyRunnable {
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	/**
	 * The IP address to used to collect the SNMP information
	 */
	private final InetAddress m_address;

	/**
	 * The Dot1DBridge.dot1dbase map
	 */
	public Dot1dBaseGroup m_dot1dbase;

	/**
	 * The Dot1DBridge Ids List
	 */
	public Dot1dBasePortTable m_dot1dbaseTable;

	/**
	 * The Dot1dStp base map
	 */
	public Dot1dStpGroup m_dot1dstp;

	/**
	 * The Dot1dStp Port table List
	 */
	public Dot1dStpPortTable m_dot1dstptable;

	/**
	 * The Dot1dTpFdb table List
	 */
	public Dot1dTpFdbTable m_dot1dtpFdbtable;

	/**
	 * The QbridgeDot1dTpFdb table List
	 */
	public QBridgeDot1dTpFdbTable m_qdot1dtpFdbtable;


	/**
	 * if collect stp data
	 */
	
	public boolean collectStpNode=true;
	
	public boolean collectStpInterface= true;
	
	public boolean collectBridgeForwardingTable= true;

	/**
	 * Constructs a new snmp collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param agentConfig
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	SnmpVlanCollection(SnmpAgentConfig agentConfig) {
		
		m_agentConfig = agentConfig;
		m_address = m_agentConfig.getAddress();
		m_dot1dbase = null;
		m_dot1dbaseTable = null;
		m_dot1dstp = null;
		m_dot1dstptable = null;
		m_dot1dtpFdbtable = null;
	}
	// for debug only

	/**
	 * Constructs a new snmp collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param agentConfig
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	SnmpVlanCollection(SnmpAgentConfig agentConfig,boolean collectStpNode, boolean collectStpTable,boolean collectBridgeForwardingTable) {
		
		m_agentConfig = agentConfig;
		m_address = m_agentConfig.getAddress();
		m_dot1dbase = null;
		m_dot1dbaseTable = null;
		m_dot1dstp = null;
		m_dot1dstptable = null;
		m_dot1dtpFdbtable = null;
		this.collectStpNode = collectStpNode;
		this.collectStpInterface = collectStpTable;
		this.collectBridgeForwardingTable = collectBridgeForwardingTable;
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
		return (m_dot1dbase != null && !m_dot1dbase.failed());
	}

	/**
	 * Returns the collected dot1base.
	 */
	Dot1dBaseGroup getDot1dBase() {
		return m_dot1dbase;
	}

	/**
	 * Returns true if the dot1DBridge table was collected.
	 */
	boolean hasDot1dBasePortTable() {
		return (m_dot1dbaseTable != null && !m_dot1dbaseTable.failed());
	}

	/**
	 * Returns the collected dot1dbridge.
	 */
	Dot1dBasePortTable getDot1dBasePortTable() {
		return m_dot1dbaseTable;
	}

	/**
	 * Returns true if the dot1DStp info was collected.
	 */
	boolean hasDot1dStp() {
		return (m_dot1dstp != null && !m_dot1dstp.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dStpGroup getDot1dStp() {
		return m_dot1dstp;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasDot1dStpPortTable() {
		return (m_dot1dstptable != null && !m_dot1dstptable.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dStpPortTable getDot1dStpPortTable() {
		return m_dot1dstptable;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasDot1dTpFdbTable() {
		return (m_dot1dtpFdbtable != null && !m_dot1dtpFdbtable.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dTpFdbTable getDot1dFdbTable() {
		return m_dot1dtpFdbtable;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasQBridgeDot1dTpFdbTable() {
		return (m_qdot1dtpFdbtable!= null && !m_qdot1dtpFdbtable.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	QBridgeDot1dTpFdbTable getQBridgeDot1dFdbTable() {
		return m_qdot1dtpFdbtable;
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
	public void run() {
			m_dot1dbase = new Dot1dBaseGroup(m_address);
			m_dot1dbaseTable = new Dot1dBasePortTable(m_address);
			m_dot1dstp = new Dot1dStpGroup(m_address);
			m_dot1dstptable = new Dot1dStpPortTable(m_address);
			m_dot1dtpFdbtable = new Dot1dTpFdbTable(m_address);
			
			SnmpWalker walker = null;
			
			if (collectBridgeForwardingTable && collectStpInterface && collectStpNode) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBase/dot1dBaseTable/dot1dStp/dot1dStpTable/dot1dTpFdbTable ", 
		        			new CollectionTracker[] { m_dot1dbase, m_dot1dbaseTable, m_dot1dstp,m_dot1dstptable,m_dot1dtpFdbtable});
			} else if(collectBridgeForwardingTable && collectStpInterface ) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBase/dot1dBaseTable/dot1dStp/dot1dStpTable/dot1dTpFdbTable ", 
		        			new CollectionTracker[] { m_dot1dbase, m_dot1dbaseTable, m_dot1dstp,m_dot1dstptable,m_dot1dtpFdbtable});
			} else if (collectBridgeForwardingTable && collectStpInterface ) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBaseTable/dot1dStpTable/dot1dTpFdbTable ", 
		        			new CollectionTracker[] {m_dot1dbaseTable, m_dot1dstptable,m_dot1dtpFdbtable});
			} else if(collectBridgeForwardingTable && collectStpNode) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBase/dot1dStp/dot1dTpFdbTable ", 
		        			new CollectionTracker[] { m_dot1dbase, m_dot1dstp,m_dot1dtpFdbtable});
			} else if(collectBridgeForwardingTable) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dTpFdbTable ", 
		        			new CollectionTracker[] {m_dot1dtpFdbtable});
			} else if(collectStpNode) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBase/dot1dStp ", 
		        			new CollectionTracker[] { m_dot1dbase, m_dot1dstp});
			} else if(collectStpInterface) {
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "dot1dBaseTable/dot1dStpTable ", 
		        			new CollectionTracker[] { m_dot1dbaseTable, m_dot1dstptable});
			} else {
			    LogUtils.infof(this, "run: no info to collect return");
				return;
			}

			
	        walker.start();

	        try {
	            walker.waitFor();
	        } catch (InterruptedException e) {
				m_dot1dbase = null;
				m_dot1dbaseTable = null;
				m_dot1dstp = null;
				m_dot1dstptable = null;
				m_dot1dtpFdbtable = null;

				LogUtils.warnf(this, e, "SnmpVlanCollection.run: collection interrupted, exiting");
	            return;
	        }
	        


			// Log any failures
			//
			if (!this.hasDot1dBase())
			    LogUtils.infof(this, "run: failed to collect Dot1dBase for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
			if (!this.hasDot1dBasePortTable())
                LogUtils.infof(this, "run: failed to collect Dot1dBasePortTable for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
			if (!this.hasDot1dStp())
                LogUtils.infof(this, "run: failed to collect Dot1dStp for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
			if (!this.hasDot1dStpPortTable())
                LogUtils.infof(this, "run: failed to collect Dot1dStpPortTable for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
			if (!this.hasDot1dTpFdbTable())
                LogUtils.infof(this, "run: failed to collect Dot1dTpFdbTable for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
			
			//if not found macaddresses forwarding table find it in Qbridge
			//ExtremeNetwork works.....
			
			if (m_dot1dtpFdbtable.getEntries().isEmpty() && collectBridgeForwardingTable) {
			    LogUtils.infof(this, "run: Trying to collect QbridgeDot1dTpFdbTable for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
				m_qdot1dtpFdbtable = new QBridgeDot1dTpFdbTable(m_address);
		        walker = 
		        	SnmpUtils.createWalker(m_agentConfig, "qBridgedot1dTpFdbTable ", 
		        			new CollectionTracker[] { m_qdot1dtpFdbtable });

		        walker.start();

		        try {
		            walker.waitFor();
		        } catch (InterruptedException e) {
					m_qdot1dtpFdbtable = null;
					LogUtils.warnf(this, e, "SnmpVlanCollection.run: collection interrupted");
		            
		        }
			}
			if (!this.hasQBridgeDot1dTpFdbTable()) {
			    LogUtils.infof(this, "run: failed to collect QBridgeDot1dTpFdbTable for %s Community: %s", m_address.getHostAddress(), m_agentConfig.getReadCommunity());
			}
	}

	/**
	 * <p>isReady</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReady() {
		return true;
	}

}
