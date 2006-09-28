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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.snmp.*;
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
public final class SnmpCollection implements ReadyRunnable {
	
	/**
	 * The vlan string to define vlan name when collection is made for all vlan 
	 */

	private final static String ALL_VLAN_NAME="AllVlans";
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	/**
	 * The IP address to used to collect the SNMP information
	 */
	private final InetAddress m_address;

	/**
	 * The Class used to collect the Vlan IDs
	 */
	private String m_vlanClass = null;

	/**
	 * A boolean used to decide if you can collect Vlan Table and Bridge Data
	 */
	private boolean m_collectVlanTable = false;

	/**
	 * The ipnettomedia table information
	 */
	public IpNetToMediaTable m_ipNetToMedia;

	/**
	 * The ipRoute table information
	 */
	public IpRouteTable m_ipRoute;

	/**
	 * The CdpCache table information
	 */
	public CdpCacheTable m_CdpCache;

	/**
	 * The Vlan Table information
	 */
	public SnmpTable m_vlanTable;

	/**
	 * The list of vlan snmp collection object
	 */

	public java.util.List m_snmpVlanCollection = new ArrayList();

	/**
	 * The scheduler object
	 *  
	 */

	private Scheduler m_scheduler;

	/**
	 * The interval default value 5 min
	 */

	private long poll_interval = 1800000;

	/**
	 * The initial sleep time default value 5 min
	 */

	private long initial_sleep_time = 600000;

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
	 * @param config
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	
	public SnmpCollection(SnmpAgentConfig config) {
		m_agentConfig = config;
		m_address = m_agentConfig.getAddress();
		m_ipNetToMedia = null;
		m_ipRoute = null;
		m_vlanTable = null;
		m_CdpCache = null;
		m_snmpVlanCollection.clear();
	}

	public static void main(String[] a) throws UnknownHostException {
		//String localhost = "127.0.0.1";
		String localhost = "10.3.2.216";
		InetAddress ip = InetAddress.getByName(localhost);
		SnmpAgentConfig peer = new SnmpAgentConfig(ip);
		SnmpCollection snmpCollector = new SnmpCollection(peer);
		snmpCollector.run();
		java.util.Iterator itr = snmpCollector.getVlanTable().iterator();
		System.out.println("number of vlan entities = "
				+ snmpCollector.m_vlanTable.getEntries().size());
		while (itr.hasNext()) {
			SnmpTableEntry ent = (SnmpTableEntry) itr.next();
			System.out.println("Vlan found: Vlan Index = "
					+ ent.getInt32(VlanCollectorEntry.VLAN_INDEX)
					+ " Vlan name = "
					+ ent.getHexString(VlanCollectorEntry.VLAN_NAME));
		}
	}

	/**
	 * Returns true if any part of the collection failed.
	 */
	
	boolean failed() {
		return !hasIpNetToMediaTable() && !hasRouteTable()
				&& !hasCdpCacheTable() && !hasVlanTable();
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
	 * Returns true if the Cdp Cache table was collected.
	 */
	boolean hasCdpCacheTable() {
		return (m_CdpCache != null && !m_CdpCache.failed());
	}

	/**
	 * Returns the collected ip route table.
	 */
	CdpCacheTable getCdpCacheTable() {
		return m_CdpCache;
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
	List getVlanTable() {
		return m_vlanTable.getEntries();
	}

	/**
	 * Returns the VLAN name from vlanindex.
	 */

	private String getVlanName(String m_vlan) {
		if (this.hasVlanTable()) {
			java.util.Iterator itr = this.getVlanTable().iterator();
			while (itr.hasNext()) {
				SnmpTableEntry ent = (SnmpTableEntry) itr.next();
				int vlanIndex= ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
				if (vlanIndex == Integer.parseInt(m_vlan)) {
					return ent.getHexString(VlanCollectorEntry.VLAN_NAME);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the VLAN vlanindex from name.
	 */

	private String getVlanIndex(String m_vlanname) {
		if (this.hasVlanTable()) {
			java.util.Iterator itr = this.getVlanTable().iterator();
			while (itr.hasNext()) {
				SnmpTableEntry ent = (SnmpTableEntry) itr.next();
				String vlanName = ent.getHexString(VlanCollectorEntry.VLAN_NAME);
				if (vlanName.equals(m_vlanname)) {
					return ent.getInt32(VlanCollectorEntry.VLAN_INDEX).toString();
				}
			}
		}
		return null;
	}

	List getSnmpVlanCollections() {
		return m_snmpVlanCollection;
	}

	/**
	 * Returns the target address that the collection occured for.
	 */

	public InetAddress getTarget() {
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

		if (suspendCollection) {
			log
			.debug("SnmpCollection.run: address: "
					+ m_address.getHostAddress()
					+ " Suspended!");
		} else {

			m_ipNetToMedia = new IpNetToMediaTable(m_address);

			m_ipRoute = new IpRouteTable(m_address);

			m_CdpCache = new CdpCacheTable(m_address);

	        if (log.isDebugEnabled())
	            log.debug("run: collecting for: "+m_address+" with agentConfig: "+m_agentConfig);
	        
	        SnmpWalker walker = null;

			if (m_collectVlanTable) {
				Class vlanGetter = null;
				try {
					vlanGetter = Class.forName(m_vlanClass);
				} catch (ClassNotFoundException e) {
					log.error("SnmpCollection.run: " + m_vlanClass
							+ " class not found " + e);
				}

				Class[] classes = { InetAddress.class};
				Constructor constr = null;
				try {
					constr = vlanGetter.getConstructor(classes);
				} catch (NoSuchMethodException e) {
					log.error("SnmpCollection.run: " + m_vlanClass
							+ " class has not such method " + e);
				} catch (SecurityException s) {
					log.error("SnmpCollection.run: " + m_vlanClass
							+ " class security violation " + s);
				}
				Object[] argum = {m_address};
				try {
					m_vlanTable = (SnmpTable) constr
							.newInstance(argum);
				} catch (InvocationTargetException t) {
					log.error("SnmpCollection.run: " + m_vlanClass
							+ " class Invocation Exception " + t);
				} catch (InstantiationException i) {
					log.error("SnmpCollection.run: " + m_vlanClass
							+ " class Instantiation Exception " + i);
				} catch (IllegalAccessException s) {
					log.error("SnmpCollection.run: " + m_vlanClass
							+ " class Illegal Access Exception " + s);
				}
				walker = SnmpUtils.createWalker(m_agentConfig, "ipNetToMediaTable/ipRouteTable/cdpCacheTable/vlanTable", new CollectionTracker[] { m_ipNetToMedia, m_ipRoute, m_CdpCache,m_vlanTable});

			} else {
				walker = SnmpUtils.createWalker(m_agentConfig, "ipNetToMediaTable/ipRouteTable/cdpCacheTable", new CollectionTracker[] { m_ipNetToMedia, m_ipRoute, m_CdpCache});

			}

	        walker.start();

	        try {
	            // wait a maximum of five minutes!
	            //
	            // FIXME: Why do we do this. If we are successfully processing responses shouldn't we keep going?
	            walker.waitFor(300000);
	        } catch (InterruptedException e) {
	            m_ipNetToMedia = null;
	            m_ipRoute = null;
	            m_CdpCache = null;
	            m_vlanTable = null;

	            log.warn("SnmpCollection.run: collection interrupted, exiting", e);
	            return;
	        }
	        
	        // Log any failures
			//
			if (!this.hasIpNetToMediaTable())
				log
						.info("SnmpCollection.run: failed to collect ipNetToMediaTable for "
								+ m_address.getHostAddress());
			if (!this.hasRouteTable())
				log
						.info("SnmpCollection.run: failed to collect ipRouteTable for "
								+ m_address.getHostAddress());
			if (!this.hasCdpCacheTable())
				log
						.info("SnmpCollection.run: failed to collect dpCacheTable for "
								+ m_address.getHostAddress());
			if (m_collectVlanTable && !this.hasVlanTable())
				log.info("SnmpCollection.run: failed to collect Vlan for "
						+ m_address.getHostAddress());
			// Schedule snmp vlan collection
			// only on VLAN.
			// If it has not vlan collection no data download is done.
			if (this.hasVlanTable()) {
				if (log.isDebugEnabled())
					log.debug("SnmpCollection.run: found "
							+ getVlanTable().size() + " VLAN entries ");

				if (m_vlanClass
						.equals("org.opennms.netmgt.linkd.snmp.HpVlanPortTable") || m_vlanClass
						.equals("org.opennms.netmgt.linkd.snmp.RapidCityVlanPortTable")) {
					SnmpVlanCollection snmpvlancollection = new SnmpVlanCollection(
							m_agentConfig);
					snmpvlancollection.setVlan("0");
					snmpvlancollection.setVlanName(ALL_VLAN_NAME);
					snmpvlancollection.run();
					if (snmpvlancollection.failed()) {
						if (log.isDebugEnabled())
							log
									.debug("SnmpCollection.run: no bridge info found on HP ");
					} else {
						if (log.isDebugEnabled())
							log
									.debug("SnmpCollection.run: adding bridge info to snmpcollection for HP ");
						m_snmpVlanCollection.add(snmpvlancollection);
	}
				} else {
					java.util.Iterator itr = m_vlanTable.getEntries()
							.iterator();
					while (itr.hasNext()) {
						SnmpTableEntry ent = (SnmpTableEntry) itr.next();
						String vlan = ent.getInt32(VlanCollectorEntry.VLAN_INDEX)
								.toString();
						if (vlan == null) {
							if (log.isDebugEnabled())
								log
										.debug("SnmpCollection.run: found null value for vlan.");
							continue;
						}
						String community = m_agentConfig.getReadCommunity();
						if (log.isDebugEnabled())
							log.debug("SnmpCollection.run: peer community: "
									+ community + " with VLAN " + vlan);

						Integer status = ent.getInt32(VlanCollectorEntry.VLAN_STATUS);
						if (status == null || status != VlanCollectorEntry.VLAN_STATUS_OPERATIONAL) {
							if (log.isEnabledFor(Priority.INFO))
								log.info("SnmpCollection.run: skipping VLAN "
									+ vlan + " NOT ACTIVE or null ");
							continue;
						}

						Integer type = ent.getInt32(VlanCollectorEntry.VLAN_TYPE);
						if (type == null || type != VlanCollectorEntry.VLAN_TYPE_ETHERNET) {
							if (log.isEnabledFor(Priority.INFO))
								log
										.info("SnmpCollection.run: skipping VLAN "
												+ vlan
												+ " NOT ETHERNET TYPE");
							continue;
						}
						m_agentConfig.setReadCommunity(community + "@" + vlan);

						SnmpVlanCollection snmpvlancollection = new SnmpVlanCollection(
								m_agentConfig);
						snmpvlancollection.setVlan(vlan);
						snmpvlancollection.setVlanName(getVlanName(vlan));
						snmpvlancollection.run();
						if (snmpvlancollection.failed()) {
							if (log.isDebugEnabled())
								log
										.debug("SnmpCollection.run: no bridge info found for VLAN "
												+ vlan);
						} else {
							m_snmpVlanCollection.add(snmpvlancollection);
						}
						m_agentConfig.setReadCommunity(community);
					}
				}
			}
			// update info in linkd used correctly by discoveryLink
			if (log.isDebugEnabled())
				log.debug("SnmpCollection.run: saving collection into database ");
			
			Linkd.getInstance().updateNodeSnmpCollection(this);
			// clean memory
			// first make every think clean
			m_ipNetToMedia = null;
			m_ipRoute = null;
			m_CdpCache = null;
			m_vlanTable = null;
			m_snmpVlanCollection.clear();
		}

		// schedule it self
		reschedule();
		runned = true;
	}

	public Scheduler getScheduler() {
		return m_scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}

	/**
	 * @return Returns the initial_sleep_time.
	 */
	public long getInitialSleepTime() {
		return initial_sleep_time;
	}

	/**
	 * @param initial_sleep_time
	 *            The initial_sleep_timeto set.
	 */
	public void setInitialSleepTime(long initial_sleep_time) {
		this.initial_sleep_time = initial_sleep_time;
	}

	/**
	 * @return Returns the initial_sleep_time.
	 */
	public long getPollInterval() {
		return poll_interval;
	}

	/**
	 * @param initial_sleep_time
	 *            The initial_sleep_timeto set.
	 */
	public void setPollInterval(long interval) {
		this.poll_interval = interval;
	}


	/**
	 *  
	 */
	public void schedule() {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"Cannot schedule a service whose scheduler is set to null");
		m_scheduler.schedule(poll_interval + initial_sleep_time, this);
	}

	/**
	 *  
	 */
	private void reschedule() {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"Cannot schedule a service whose scheduler is set to null");
		m_scheduler.schedule(poll_interval, this);
	}

	public boolean isReady() {
		return true;
	}

	/**
	 * @return Returns the suspendCollection.
	 */
	public boolean isSuspended() {
		return suspendCollection;
	}

	/**
	 * @param suspendCollection
	 *            The suspendCollection to set.
	 */
	
	public void suspend() {
		this.suspendCollection = true;
	}

	/**
	 * @param suspendCollection
	 *            The suspendCollection to set.
	 */
	
	public void wakeUp() {
		this.suspendCollection = false;
	}

	public void unschedule() throws UnknownHostException, Throwable {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"rescedule: Cannot schedule a service whose scheduler is set to null");
		if (runned) {
			m_scheduler.unschedule(getTarget(),poll_interval);
		} else {
			m_scheduler.unschedule(getTarget(),poll_interval+initial_sleep_time);
		}
	}

	/**
	 * @return Returns the m_collectVlanTable.
	 */
	public boolean collectVlanTable() {
		return m_collectVlanTable;
	}
	
	/**
	 * @return Returns the m_vlanClass.
	 */
	public String getVlanClass() {
		return m_vlanClass;
	}
	
	public void setVlanClass(String className) {
		if (className == null || className.equals("")) return;
		m_vlanClass = className;
		m_collectVlanTable = true;
	}
	/**
	 * @return Returns the m_address.
	 */
	public InetAddress getSnmpIpPrimary() {
		return m_address;
	}
	
	public boolean isSnmpCollection(){
		return true;
	}

	public boolean isDiscoveryLink(){
		return false;
	}

}