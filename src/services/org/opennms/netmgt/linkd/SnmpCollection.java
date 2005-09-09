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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.linkd.snmp.*;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.utils.BarrierSignaler;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;

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
final class SnmpCollection implements ReadyRunnable {

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpPeer m_peer;

	/**
	 * The IP address to used to collect the SNMP information
	 */
	private final InetAddress m_address;

	/** 
	 * The Vlan OID used to collect the Vlan IDs
	 */
// for debug only
//	private String m_vlanoid = ".1.3.6.1.4.1.9.9.46.1.3.1.1.4.1";
	private String m_vlanoid = null;
	/**
	 * The ipnettomedia table information
	 */
	public IpNetToMediaTable m_ipNetToMedia;

	/**
	 * The ipRoute table information
	 */
	public IpRouteTable m_ipRoute;

	/**
	 * The Vlan Table information
	 */
	public VlanTable m_vlanTable;

	/**
	 * The list of vlan snmp collection object 
	 */

	public java.util.List m_snmpvlaninfo = new ArrayList();
	
	/**
	 * The scheduler object
	 *
	 */
	
	private Scheduler m_scheduler;

	/**
	 * The interval
	 * default value 5 min
	 */
	
	private long interval = 300000;

	/**
	 * The initial sleep time 
	 * default value 5 min
	 */
	
	private long initial_sleep_time = 300000;

	private boolean suspendCollection = false;
	
	private boolean runned = false;

	/**
	 * The default constructor. Since this class requires an IP address to
	 * collect SNMP information the default constructor is declared private and
	 * will also throw an exception
	 * 
	 * @throws java.lang.UnsupportedOperationException
	 *             Always thrown.
	 */
	private SnmpCollection() {
		throw new UnsupportedOperationException(
				"default constructor not supported");
	}

	/**
	 * Constructs a new snmp collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param peer
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	SnmpCollection(SnmpPeer peer) {
		m_peer = peer;
		m_address = m_peer.getPeer();
		m_ipNetToMedia = null;
		m_ipRoute = null;
		m_vlanTable = null;
	}

	/**
	 * Constructs a new snmp collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param peer
	 *            The SnmpPeer object to collect from.
	 * @param vlanoid
	 *            The Vlan Oid to collect from.
	 *  
	 */
	SnmpCollection(SnmpPeer peer, String vlanoid) {
		m_peer = peer;
		m_vlanoid = vlanoid;
		m_address = peer.getPeer();
		m_ipNetToMedia = null;
		m_ipRoute = null;
		m_vlanTable = null;
	}

	public static void main(String[] a) throws UnknownHostException {
		//String localhost = "127.0.0.1";
		String localhost = "10.3.2.216";
		InetAddress ip = InetAddress.getByName(localhost);
		SnmpPeer peer = new SnmpPeer(ip);
		SnmpCollection snmpCollector = new SnmpCollection(peer);
		snmpCollector.run();
		java.util.Iterator itr = snmpCollector.getVlanTable().getEntries().iterator();
		System.out.println("number of vlan entities = "
				+ snmpCollector.m_vlanTable.getEntries().size());
		while (itr.hasNext()){
			java.util.TreeMap ent = (TreeMap)itr.next();
			System.out.println("Vlan found: Vlan Index = " + ent.get(VlanTable.VLAN_INDEX).toString()
					+ " Vlan name = " + ent.get(VlanTable.VLAN_NAME).toString());
		}
	}

	/**
	 * Returns true if any part of the collection failed.
	 */
	boolean failed() {
		return !hasIpNetToMediaTable() || !hasRouteTable() || !hasVlanTable();
	}

	/**
	 * Returns true if the ip net to media table was collected.
	 */
	boolean hasIpNetToMediaTable() {
		return (m_ipNetToMedia != null && !m_ipNetToMedia.failed());
	}

	/**
	 * Returns the collected ip net to media table.
	 */
	IpNetToMediaTable getIpNetToMediaTable() {
		return m_ipNetToMedia;
	}

	/**
	 * Returns true if the ip route table was collected.
	 */
	boolean hasRouteTable() {
		return (m_ipRoute != null && !m_ipRoute.failed());
	}

	/**
	 * Returns the collected ip route table.
	 */
	IpRouteTable getIpRouteTable() {
		return m_ipRoute;
	}

	/**
	 * Returns true if the VLAN table was collected.
	 */
	boolean hasVlanTable() {
		return (m_vlanTable != null && !m_vlanTable.failed());
	}

	/**
	 * Returns the collected VLAN table.
	 */
	VlanTable getVlanTable() {
		return m_vlanTable;
	}


	/**
	 * Returns the VLAN name from vlanindex.
	 */

	String getVlanName(String m_vlan) {
		if (this.hasVlanTable()){
			java.util.Iterator itr = m_vlanTable.getEntries().iterator();
			while (itr.hasNext()){
				java.util.TreeMap ent = (TreeMap)itr.next();
				if (ent.get(VlanTable.VLAN_INDEX).toString().equals(m_vlan)) {
					return ent.get(VlanTable.VLAN_NAME).toString();
				}
			}
		}
		return null;
	}

	
	/**
	 * Returns the VLAN vlanindex from name.
	 */

	String getVlanIndex(String m_vlanname) {
		if (this.hasVlanTable()){
			java.util.Iterator itr = m_vlanTable.getEntries().iterator();
			while (itr.hasNext()){
				java.util.TreeMap ent = (TreeMap)itr.next();
				if (ent.get(VlanTable.VLAN_NAME).toString().equals(m_vlanname)) {
					return ent.get(VlanTable.VLAN_INDEX).toString();
				}
			}
		}
		return null;
	}

	List getVlanSnmpList(){
		return m_snmpvlaninfo;
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
	 *  
	 */

	public void run() {
		Category log = ThreadCategory.getInstance(getClass());

		if (!suspendCollection) {

			SnmpSession session = null;
			try {
				log
						.debug("SnmpCollection.run: address: "
								+ m_address.getHostAddress()
								+ " Snmp version: "
								+ ((m_peer.getParameters().getVersion() == SnmpSMI.SNMPV1) ? "SNMPv1"
										: "SNMPv2"));
				session = new SnmpSession(m_peer);

				int numSignalers = 3;
				if (m_vlanoid == null) {
					log
					.debug("SnmpCollection.run: no vlan oid set for host: "
							+ m_address.getHostAddress()
							+ " Skipping VlanTable Download");
					numSignalers--;
					m_vlanTable = null;
				}
				BarrierSignaler signaler = new BarrierSignaler(numSignalers);
				synchronized (signaler) {
					m_ipNetToMedia = new IpNetToMediaTable(session, signaler,
							m_peer.getParameters().getVersion());
					m_ipRoute = new IpRouteTable(session, signaler, m_peer
							.getParameters().getVersion());
					if (m_vlanoid != null) m_vlanTable = new VlanTable(session, signaler, m_vlanoid);

					try {
						// wait a maximum of five minutes!
						//
						//signaler.wait(300000);
						signaler.wait(300000);
					} catch (InterruptedException e) {
						m_ipNetToMedia = null;
						m_ipRoute = null;
						m_vlanTable = null;

						log
								.warn(
										"RunSnmpCollection: collection interrupted, exiting",
										e);
						return;
					}
				}

				// Log any failures
				//
				if (!this.hasIpNetToMediaTable())
					log
							.info("RunSnmpCollection: failed to collect ipNetToMediaTable for "
									+ m_address.getHostAddress());
				if (!this.hasRouteTable())
					log
							.info("RunSnmpCollection: failed to collect ipRouteTable for "
									+ m_address.getHostAddress());
				if (!this.hasVlanTable())
					log.info("RunSnmpCollection: failed to collect Vlan for "
							+ m_address.getHostAddress());
			} catch (java.net.SocketException e) {
				log.error("Failed to create SNMP session to connect to host "
						+ m_address.getHostAddress(), e);
			} finally {
				if (session != null)
					session.close();
			}
			// Schedule snmp vlan collection
			// only on VLAN.
			// If it has not vlan collection no data download is done.
			if (this.hasVlanTable()) {
				java.util.Iterator itr = m_vlanTable.getEntries().iterator();
				while (itr.hasNext()) {
					java.util.TreeMap ent = (TreeMap) itr.next();
					String vlan = ent.get(VlanTable.VLAN_INDEX).toString();
					if (vlan.equals("1002") || vlan.equals("1003")
							|| vlan.equals("1004") || vlan.equals("1005"))
						continue;
					log.debug("SnmpCollection.run: community: "
							+ m_peer.getParameters().getReadCommunity() + "@"
							+ vlan);
					SnmpVlanCollection snmpvlancollection = new SnmpVlanCollection(
							m_peer, vlan);
					snmpvlancollection.run();
					if (!snmpvlancollection.failed())
						m_snmpvlaninfo.add(snmpvlancollection);
				}
			}
			runned = true;
		} else {
			runned = false;
			log.debug("SnmpCollection.run: suspended");

		}
		// update info in linkd used correctly by discoveryLink
		reschedule();
		Linkd.getInstance().updateNodeSnmpCollection(this);
	}

	public Scheduler getScheduler() {
		return m_scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}

	/**
	 * @return Returns the interval.
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @param interval The interval to set.
	 */
	public void setInterval(long interval) {
		this.interval = interval;
	}

	/**
	 * @return Returns the initial_sleep_time.
	 */
	public long getInitialSleepTime() {
		return initial_sleep_time;
	}

	/**
	 * @param initial_sleep_time The initial_sleep_timeto set.
	 */
	public void setInitialSleepTime(long initial_sleep_time) {
		this.initial_sleep_time = initial_sleep_time;
	}

    /**
     * 
     */
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException("Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(interval+initial_sleep_time,this);
    }

    /**
     * 
     */
    private void reschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException("Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(interval,this);
    }

	public boolean isReady() {
		return true;
	}
	
	/**
	 * @return Returns the suspendCollection.
	 */
	public boolean isSuspendCollection() {
		return suspendCollection;
	}
	/**
	 * @param suspendCollection The suspendCollection to set.
	 */
	public void setSuspendCollection(boolean suspendCollection) {
		this.suspendCollection = suspendCollection;
	}
	
	/**
	 * @return Returns true if at least one time was runned.
	 */
	public boolean isRunned() {
		return runned;
	}


}