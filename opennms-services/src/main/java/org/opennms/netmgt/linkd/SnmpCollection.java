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
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.snmp.CdpCacheTable;
import org.opennms.netmgt.linkd.snmp.IpNetToMediaTable;
import org.opennms.netmgt.linkd.snmp.IpRouteTable;
import org.opennms.netmgt.linkd.snmp.VlanCollectorEntry;
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
 */
public final class SnmpCollection implements ReadyRunnable {

	/**
	 * The vlan string to define vlan name when collection is made for all vlan
	 */

	public final static String TRUNK_VLAN_NAME = "AllVlans";

	/**
	 * The vlan string to define vlan index when collection is made for all vlan
	 */

	public final static int TRUNK_VLAN_INDEX = 0;

	/**
	 * The vlan string to define default vlan name
	 */

	public final static String DEFAULT_VLAN_NAME = "default";

	/**
	 * The vlan string to define default vlan index
	 */

	public final static int DEFAULT_VLAN_INDEX = 1;

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
     * The Class used to collect the ipRoute IDs
     */
    private String m_ipRouteClass = null;
	
    /**
	 * A boolean used to decide if you can collect Vlan Table and Bridge Data
	 */
	private boolean m_collectVlanTable = false;

	/**
	 * A boolean used to decide if you can collect Route Table
	 */
	private boolean m_collectIpRouteTable = false;

	/**
	 * A boolean used to decide if you can collect Stp Base Info
	 */
	private boolean m_collectStpNode = false;

	/**
	 * A boolean used to decide if save StpNode Table
	 */
	private boolean m_saveStpNodeTable = false;

	/**
	 * A boolean used to decide if save IpRouteTable
	 */
	private boolean m_saveIpRouteTable = false;

	/**
	 * A boolean used to decide if you save StpInterfaceTable
	 */
	private boolean m_saveStpInterfaceTable = false;

	/**
	 * A boolean used to decide if you can collect Stp Table
	 */
	private boolean m_collectStpTable = false;


	/**
	 * A boolean used to decide if you can collect Bridge Forwarding Table
	 */
	private boolean m_collectBridgeForwardingTable = false;
	
	/**
	 * A boolean used to decide if you can collect Cdp Table
	 */
	private boolean m_collectCdpTable = false;

	/**
	 * The ipnettomedia table information
	 */
	public IpNetToMediaTable m_ipNetToMedia;

	/**
	 * The ipRoute table information
	 */
	public SnmpTable<SnmpTableEntry> m_ipRoute;

	/**
	 * The CdpCache table information
	 */
	public CdpCacheTable m_CdpCache;

	/** * */
	/**
	 * The Vlan Table information
	 */
	public SnmpTable<SnmpTableEntry> m_vlanTable;

	/**
	 * The list of vlan snmp collection object
	 */

	public java.util.Map<Vlan,SnmpVlanCollection> m_snmpVlanCollection; 

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

	private String packageName;
	/**
	 * The default constructor. Since this class requires an IP address to
	 * collect SNMP information the default constructor is declared private and
	 * will also throw an exception
	 * 
	 * @throws java.lang.UnsupportedOperationException
	 *             Always thrown.
	 */
	
	SnmpCollection() {
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
	 */
	public SnmpCollection(SnmpAgentConfig config) {
		m_agentConfig = config;
		m_address = m_agentConfig.getAddress();
		m_ipNetToMedia = null;
		m_ipRoute = null;
		m_vlanTable = null;
		m_CdpCache = null;
		m_snmpVlanCollection = new HashMap<Vlan,SnmpVlanCollection>();
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
	SnmpTable<SnmpTableEntry> getIpRouteTable() {
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
	SnmpTable<SnmpTableEntry> getVlanTable() {
		return m_vlanTable;
	}

	/**
	 * Returns the VLAN name from vlanindex.
	 *
	 * @param m_vlan a int.
	 * @return a {@link java.lang.String} object.
	 */
	public String getVlanName(int m_vlan) {
		if (this.hasVlanTable()) {
		    for (final SnmpTableEntry ent : this.getVlanTable().getEntries()) {
				int vlanIndex = ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
				if (vlanIndex == m_vlan) {
					return ent.getDisplayString(VlanCollectorEntry.VLAN_NAME);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the VLAN vlanindex from name.
	 *
	 * @param m_vlanname a {@link java.lang.String} object.
	 * @return a int.
	 */
	public int getVlanIndex(String m_vlanname) {
		if (this.hasVlanTable()) {
		    for (final SnmpTableEntry ent : this.getVlanTable().getEntries()) {
				String vlanName = ent
						.getDisplayString(VlanCollectorEntry.VLAN_NAME);
				if (vlanName.equals(m_vlanname)) {
					return ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
				}
			}
		}
		return -1;
	}

	Map<Vlan, SnmpVlanCollection> getSnmpVlanCollections() {
		return m_snmpVlanCollection;
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
	@SuppressWarnings("unchecked")
    public void run() {
		

		if (suspendCollection) {
		    LogUtils.debugf(this, "SnmpCollection.run: address: %s Suspended!", m_address.getHostAddress());
		} else {

			m_ipNetToMedia = new IpNetToMediaTable(m_address);

			m_CdpCache = new CdpCacheTable(m_address);

			LogUtils.debugf(this, "run: collecting : %s", m_agentConfig);

			SnmpWalker walker = null;

            if (m_collectIpRouteTable) {
                Class<?> ipRouteGetter = null;
                try {
                        ipRouteGetter = Class.forName(m_ipRouteClass);
                } catch (ClassNotFoundException e) {
                        log().error("SnmpCollection.run: " + m_ipRouteClass + " class not found " + e);
                }

                Class<?>[] classes = { InetAddress.class };
                Constructor<?> constr = null;
                try {
                        constr = ipRouteGetter.getConstructor(classes);
                } catch (NoSuchMethodException e) {
                        log().error("SnmpCollection.run: " + m_ipRouteClass + " class has not such method " + e);
                } catch (SecurityException s) {
                        log().error("SnmpCollection.run: " + m_ipRouteClass + " class security violation " + s);
                }
                Object[] argum = { m_address };
                try {
                        m_ipRoute = (SnmpTable) constr.newInstance(argum);
                } catch (InvocationTargetException t) {
                        log().error("SnmpCollection.run: " + m_ipRouteClass + " class Invocation Exception " + t);
                } catch (InstantiationException i) {
                        log().error("SnmpCollection.run: " + m_ipRouteClass + " class Instantiation Exception " + i);
                } catch (IllegalAccessException s) {
                        log().error("SnmpCollection.run: " + m_ipRouteClass + " class Illegal Access Exception " + s);
                }
            }
			    			
			if (m_collectVlanTable) {
				Class<?> vlanGetter = null;
				try {
					vlanGetter = Class.forName(m_vlanClass);
				} catch (ClassNotFoundException e) {
				    LogUtils.warnf(this, e, "SnmpCollection.run: %s class not found", m_vlanClass);
				}

				Class<?>[] classes = { InetAddress.class };
				Constructor<?> constr = null;
				try {
					constr = vlanGetter.getConstructor(classes);
				} catch (NoSuchMethodException e) {
				    LogUtils.warnf(this, e, "SnmpCollection.run: %s class has no such method", m_vlanClass);
				} catch (SecurityException s) {
                    LogUtils.warnf(this, s, "SnmpCollection.run: %s class security violation", m_vlanClass);
				}
				Object[] argum = { m_address };
				try {
					m_vlanTable = (SnmpTable) constr.newInstance(argum);
				} catch (Exception e) {
				    LogUtils.warnf(this, e, "SnmpCollection.run: unable to instantiate class %s", m_vlanClass);
				}
			}
			
			if (m_collectVlanTable && m_collectIpRouteTable && m_collectCdpTable) {
				walker = SnmpUtils.createWalker(m_agentConfig,
								"ipNetToMediaTable/ipRouteTable/cdpCacheTable/vlanTable",
								new CollectionTracker[] { m_ipNetToMedia, m_ipRoute, m_CdpCache, m_vlanTable });
			} else if (m_collectCdpTable && m_collectIpRouteTable){
				walker = SnmpUtils.createWalker(m_agentConfig,
						"ipNetToMediaTable/ipRouteTable/cdpCacheTable",
						new CollectionTracker[] { m_ipNetToMedia, m_ipRoute, m_CdpCache });
			} else if (m_collectVlanTable && m_collectIpRouteTable) {
				walker = SnmpUtils.createWalker(m_agentConfig,
						"ipNetToMediaTable/ipRouteTable/vlanTable",
						new CollectionTracker[] { m_ipNetToMedia, m_ipRoute, m_vlanTable });
			} else if (m_collectVlanTable && m_collectCdpTable) {
				walker = SnmpUtils.createWalker(m_agentConfig,
					"ipNetToMediaTable/vlanTable/cdpCacheTable",
					new CollectionTracker[] { m_ipNetToMedia, m_vlanTable, m_CdpCache });
			} else if (m_collectIpRouteTable){
				walker = SnmpUtils.createWalker(m_agentConfig,
						"ipNetToMediaTable/ipRouteTable",
						new CollectionTracker[] { m_ipNetToMedia, m_ipRoute});
			} else if (m_collectVlanTable ) {
				walker = SnmpUtils.createWalker(m_agentConfig,
						"ipNetToMediaTable/vlanTable",
						new CollectionTracker[] { m_ipNetToMedia, m_vlanTable });
			} else if (m_collectCdpTable) {
				walker = SnmpUtils.createWalker(m_agentConfig,
					"ipNetToMediaTable/cdpCacheTable",
					new CollectionTracker[] { m_ipNetToMedia, m_CdpCache });
			}else {
				walker = SnmpUtils.createWalker(m_agentConfig,
						"ipNetToMediaTable",
						new CollectionTracker[] { m_ipNetToMedia});
			}

			walker.start();

			try {
				walker.waitFor();
			} catch (InterruptedException e) {
				m_ipNetToMedia = null;
				m_ipRoute = null;
				m_CdpCache = null;
				m_vlanTable = null;

				LogUtils.errorf(this, e, "SnmpCollection.run: collection interrupted, exiting");
				return;
			}

			// Log any failures
			//
			if (!this.hasIpNetToMediaTable())
			    LogUtils.infof(this, "SnmpCollection.run: failed to collect ipNetToMediaTable for %s", m_address.getHostAddress());
			if (!this.hasRouteTable())
                LogUtils.infof(this, "SnmpCollection.run: failed to collect ipRouteTable for %s", m_address.getHostAddress());
			if (!this.hasCdpCacheTable())
                LogUtils.infof(this, "SnmpCollection.run: failed to collect dpCacheTable for %s", m_address.getHostAddress());
			if (m_collectVlanTable && !this.hasVlanTable())
                LogUtils.infof(this, "SnmpCollection.run: failed to collect Vlan for %s", m_address.getHostAddress());
			// Schedule snmp vlan collection only on VLAN.
			// If it has not vlan collection no data download is done.
			
			Vlan vlan = null;

			if (this.hasVlanTable()) {
				if (!m_vlanClass.equals("org.opennms.netmgt.linkd.snmp.CiscoVlanTable")
						&& !m_vlanClass.equals("org.opennms.netmgt.linkd.snmp.IntelVlanTable")) {

					runAndSaveSnmpVlanCollection(new Vlan(TRUNK_VLAN_INDEX,TRUNK_VLAN_NAME,VlanCollectorEntry.VLAN_STATUS_OPERATIONAL));
				} else {
				    LogUtils.debugf(this, "SnmpCollection.run: start collection for %d VLAN entries", getVlanTable().getEntries().size());

					for (final SnmpTableEntry ent : m_vlanTable.getEntries()) {
		 				int vlanindex = ent.getInt32(VlanCollectorEntry.VLAN_INDEX);
						if (vlanindex == -1) {
						    LogUtils.debugf(this, "SnmpCollection.run: found null value for vlan.");
							continue;
						}
						String vlanname = ent.getDisplayString(VlanCollectorEntry.VLAN_NAME);
						if (vlanname == null) vlanname = DEFAULT_VLAN_NAME; 
						Integer status = ent.getInt32(VlanCollectorEntry.VLAN_STATUS);

						if (status == null || status != VlanCollectorEntry.VLAN_STATUS_OPERATIONAL) {
						    LogUtils.infof(this, "SnmpCollection.run: skipping VLAN %s: NOT ACTIVE or null", vlan);
							continue;
						}

						String community = m_agentConfig.getReadCommunity();
						LogUtils.debugf(this, "SnmpCollection.run: peer community: %s with VLAN %s", community, vlan);

						Integer type = ent.getInt32(VlanCollectorEntry.VLAN_TYPE);
						if (type == null || type != VlanCollectorEntry.VLAN_TYPE_ETHERNET) {
						    LogUtils.infof(this, "SnmpCollection.run: skipping VLAN %s NOT ETHERNET TYPE", vlan);
							continue;
						}
						m_agentConfig.setReadCommunity(community + "@" + vlanindex);

						runAndSaveSnmpVlanCollection(new Vlan(vlanindex,vlanname,status));
						m_agentConfig.setReadCommunity(community);
					}  
				}

			} else {
				runAndSaveSnmpVlanCollection(new Vlan(DEFAULT_VLAN_INDEX,DEFAULT_VLAN_NAME,VlanCollectorEntry.VLAN_STATUS_OPERATIONAL));
			}
			// update info in linkd used correctly by discoveryLink
			LogUtils.debugf(this, "SnmpCollection.run: saving collection into database");

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
	
	private void runAndSaveSnmpVlanCollection(Vlan vlan) {
		SnmpVlanCollection snmpvlancollection = new SnmpVlanCollection(m_agentConfig,m_collectStpNode,m_collectStpTable,m_collectBridgeForwardingTable);
		snmpvlancollection.run();
		
		if (snmpvlancollection.failed()) {
		    LogUtils.debugf(this, "SnmpCollection.run: no bridge info found");
		} else {
		    LogUtils.debugf(this, "SnmpCollection.run: adding bridge info to snmpcollection");
			m_snmpVlanCollection.put(vlan,snmpvlancollection);
		}

	}

	/**
	 * <p>getScheduler</p>
	 *
	 * @return a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
	 */
	public Scheduler getScheduler() {
		return m_scheduler;
	}

	/**
	 * <p>setScheduler</p>
	 *
	 * @param scheduler a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
	 */
	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}

	/**
	 * <p>getInitialSleepTime</p>
	 *
	 * @return Returns the initial_sleep_time.
	 */
	public long getInitialSleepTime() {
		return initial_sleep_time;
	}

	/**
	 * <p>setInitialSleepTime</p>
	 *
	 * @param initial_sleep_time
	 *            The initial_sleep_timeto set.
	 */
	public void setInitialSleepTime(long initial_sleep_time) {
		this.initial_sleep_time = initial_sleep_time;
	}

	/**
	 * <p>getPollInterval</p>
	 *
	 * @return Returns the initial_sleep_time.
	 */
	public long getPollInterval() {
		return poll_interval;
	}

	/**
	 * <p>setPollInterval</p>
	 *
	 * @param interval a long.
	 */
	public void setPollInterval(long interval) {
		this.poll_interval = interval;
	}

	/**
	 * <p>schedule</p>
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

	/**
	 * <p>isReady</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReady() {
		return true;
	}

	/**
	 * <p>isSuspended</p>
	 *
	 * @return Returns the suspendCollection.
	 */
	public boolean isSuspended() {
		return suspendCollection;
	}

	/**
	 * <p>suspend</p>
	 */
	public void suspend() {
		this.suspendCollection = true;
	}

	/**
	 * <p>wakeUp</p>
	 */
	public void wakeUp() {
		this.suspendCollection = false;
	}

	/**
	 * <p>unschedule</p>
	 */
	public void unschedule() {
		if (m_scheduler == null)
			throw new IllegalStateException(
					"rescedule: Cannot schedule a service whose scheduler is set to null");
		if (runned) {
			m_scheduler.unschedule(this,poll_interval);
		} else {
			m_scheduler.unschedule(this, poll_interval
					+ initial_sleep_time);
		}
	}

    public String getIpRouteClass() {
               return m_ipRouteClass;
    }
	    
    public void setIpRouteClass(String className) {
           if (className == null || className.equals(""))
                   return;
           m_ipRouteClass = className;
           m_collectIpRouteTable = true;
    }

	/**
	 * <p>getVlanClass</p>
	 *
	 * @return Returns the m_vlanClass.
	 */
	public String getVlanClass() {
		return m_vlanClass;
	}

	/**
	 * <p>setVlanClass</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 */
	public void setVlanClass(String className) {
		if (className == null || className.equals(""))
			return;
		m_vlanClass = className;
		m_collectVlanTable = true;
	}

	/**
	 * Returns the target address that the collection occured for.
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getTarget() {
		return m_address;
	}

	/**
	 * <p>collectVlanTable</p>
	 *
	 * @return Returns the m_collectVlanTable.
	 */
	public boolean collectVlanTable() {
		return m_collectVlanTable;
	}
	
	/**
	 * <p>getReadCommunity</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getReadCommunity() {
		return m_agentConfig.getReadCommunity();
	}
	
	/**
	 * <p>getPeer</p>
	 *
	 * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
	 */
	public SnmpAgentConfig getPeer() {
		return m_agentConfig;
	}

	/**
	 * <p>getPort</p>
	 *
	 * @return a int.
	 */
	public int getPort() {
		return m_agentConfig.getPort();
	}
	
	/** {@inheritDoc} */
	public boolean equals(ReadyRunnable run) {
		if (run instanceof SnmpCollection && this.getPackageName().equals(run.getPackageName())) {
			SnmpCollection c = (SnmpCollection) run;
			if ( c.getTarget().equals(m_address)
					&& c.getPort() == getPort()
					&& c.getReadCommunity().equals(getReadCommunity())) return true;
		}
		return false;
	}
	
	/**
	 * <p>getInfo</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getInfo() {
		return "Ready Runnable(s) SnmpCollection "
		+ " ip=" + getTarget()
		+ " port=" + getPort()
		+ " community=" + getReadCommunity()
		+ " package=" + getPackageName()
		+ " collectBridgeForwardingTable=" + getCollectBridgeForwardingTable()
		+ " collectStpNode=" + getCollectStpNode()
		+ " collectStpTable=" + getCollectStpTable()
		+ " collectCdpTable=" + getCollectCdpTable()
		+ " collectIpRouteTable=" + getCollectIpRouteTable()
		+ " saveIpRouteTable=" + getSaveIpRouteTable()
		+ " saveStpInterfaceTable=" + getSaveStpInterfaceTable()
		+ " saveStpNodeTable=" + getSaveStpNodeTable();
		
	}

	/**
	 * <p>getCollectBridgeForwardingTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getCollectBridgeForwardingTable() {
		return m_collectBridgeForwardingTable;
	}

	/**
	 * <p>collectBridgeForwardingTable</p>
	 *
	 * @param bridgeForwardingTable a boolean.
	 */
	public void collectBridgeForwardingTable(boolean bridgeForwardingTable) {
		m_collectBridgeForwardingTable = bridgeForwardingTable;
	}

	/**
	 * <p>getCollectCdpTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getCollectCdpTable() {
		return m_collectCdpTable;
	}

	/**
	 * <p>collectCdpTable</p>
	 *
	 * @param cdpTable a boolean.
	 */
	public void collectCdpTable(boolean cdpTable) {
		m_collectCdpTable = cdpTable;
	}

	/**
	 * <p>getCollectIpRouteTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getCollectIpRouteTable() {
		return m_collectIpRouteTable;
	}

	/**
	 * <p>collectIpRouteTable</p>
	 *
	 * @param ipRouteTable a boolean.
	 */
	public void collectIpRouteTable(boolean ipRouteTable) {
		m_collectIpRouteTable = ipRouteTable;
	}

	/**
	 * <p>getCollectStpNode</p>
	 *
	 * @return a boolean.
	 */
	public boolean getCollectStpNode() {
		return m_collectStpNode;
	}

	/**
	 * <p>collectStpNode</p>
	 *
	 * @param stpNode a boolean.
	 */
	public void collectStpNode(boolean stpNode) {
		m_collectStpNode = stpNode;
	}

	/**
	 * <p>getCollectStpTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getCollectStpTable() {
		return m_collectStpTable;
	}

	/**
	 * <p>collectStpTable</p>
	 *
	 * @param stpTable a boolean.
	 */
	public void collectStpTable(boolean stpTable) {
		m_collectStpTable = stpTable;
	}

	/**
	 * <p>Getter for the field <code>packageName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * <p>getSaveStpNodeTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getSaveStpNodeTable() {
		return m_saveStpNodeTable;
	}

	/**
	 * <p>saveStpNodeTable</p>
	 *
	 * @param stpNodeTable a boolean.
	 */
	public void saveStpNodeTable(boolean stpNodeTable) {
		m_saveStpNodeTable = stpNodeTable;
	}

	/**
	 * <p>getSaveIpRouteTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getSaveIpRouteTable() {
		return m_saveIpRouteTable;
	}

	/**
	 * <p>SaveIpRouteTable</p>
	 *
	 * @param ipRouteTable a boolean.
	 */
	public void SaveIpRouteTable(boolean ipRouteTable) {
		m_saveIpRouteTable = ipRouteTable;
	}

	/**
	 * <p>getSaveStpInterfaceTable</p>
	 *
	 * @return a boolean.
	 */
	public boolean getSaveStpInterfaceTable() {
		return m_saveStpInterfaceTable;
	}

	/**
	 * <p>saveStpInterfaceTable</p>
	 *
	 * @param stpInterfaceTable a boolean.
	 */
	public void saveStpInterfaceTable(boolean stpInterfaceTable) {
		m_saveStpInterfaceTable = stpInterfaceTable;
	}

}
