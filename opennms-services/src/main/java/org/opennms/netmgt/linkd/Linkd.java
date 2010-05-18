//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Oct 01: Add ability to update database when an interface is deleted. - ayres@opennms.org
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
package org.opennms.netmgt.linkd;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.*;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.*;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;

import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.QueryManager;

import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

public class Linkd extends AbstractServiceDaemon {

	/**
	 * The log4j category used to log messages.
	 */
	private static final String LOG4J_CATEGORY = "OpenNMS.Linkd";

	/**
	 * Singleton instance of the Linkd class
	 */
	private static Linkd m_singleton;

	/**
	 * Rescan scheduler thread
	 */
	private Scheduler m_scheduler;

	/**
	 * Event manager.
	 */
	private LinkdEventProcessor m_eventListener;

    /**
     * The Db connection read and write handler
     */
    private QueryManager m_queryMgr;
    
    /**
    * Linkd Configuration Initialization
    */
    
    private LinkdConfig m_linkdConfig;
	
	/**
	 * List that contains Linkable Nodes.
	 */

	private List<LinkableNode> m_nodes;

	/**
	 * HashMap that contains SnmpCollections by package.
	 */

	private List<String> m_activepackages;
	
	/**
	 * the list of ipaddress for which new suspect event is sent
	 */

	private List<String> m_newSuspenctEventsIpAddr = null;
	
	public Linkd() {
		super(LOG4J_CATEGORY);
	}

	public static Linkd getInstance() {
		return m_singleton;
	}

	protected void onInit() {

        Assert.state(m_queryMgr != null, "must set the queryManager property");
        Assert.state(m_linkdConfig != null, "must set the linkdConfig property");
        Assert.state(m_scheduler != null, "must set the scheduler property");
        Assert.state(m_eventListener != null,"must be set the event receiver");
	       
		if (log().isInfoEnabled())
			log()
					.info("init: Logging Level Set to "
							+ log().getLevel().toString());

		m_activepackages = new ArrayList<String>();
		
		// initialize the ipaddrsentevents
		m_newSuspenctEventsIpAddr = new ArrayList<String>();
		m_newSuspenctEventsIpAddr.add("127.0.0.1");
		m_newSuspenctEventsIpAddr.add("0.0.0.0");

		try {
			m_nodes = m_queryMgr.getSnmpNodeList();
			m_queryMgr.updateDeletedNodes();
		} catch (SQLException e) {
	    	log().fatal("SQL exception executing on database", e);
	        throw new UndeclaredThrowableException(e);
		}

		Assert.notNull(m_nodes);
		scheduleCollection();
		
		m_singleton = this;
		if (log().isInfoEnabled())
			log().info("init: LINKD CONFIGURATION INITIALIZED");

	}

	private void scheduleCollection() {
	    synchronized (m_nodes) {
	        Iterator<LinkableNode> ite = m_nodes.iterator();
	        while (ite.hasNext()) {
	            //Schedule snmp collection for node and also 
	            //schedule discovery link on package where is not active 
	            scheduleCollectionForNode(ite.next());
	        }
        }
	}

	/**
	 * This method schedule snmpcollection for node
	 * for each package
	 * Also schedule discovery link on package 
	 * when not still activated
	 * @param node
	 */
	private void scheduleCollectionForNode(LinkableNode node) {

		List<SnmpCollection> snmpcollOnNode = m_linkdConfig.getSnmpCollections(node.getSnmpPrimaryIpAddr(), node.getSysoid());
		Iterator<SnmpCollection>  coll_ite = snmpcollOnNode.iterator();
		while (coll_ite.hasNext()) {
			SnmpCollection snmpcoll = coll_ite.next();
			if (m_activepackages.contains(snmpcoll.getPackageName())) {
				if (log().isDebugEnabled())
					log().debug("ScheduleCollectionForNode: package active: " +snmpcoll.getPackageName());
			} else {
				// schedule discovery link
				if (log().isDebugEnabled())
					log().debug("ScheduleCollectionForNode: Schedulink Discovery Link for Active Package: " + snmpcoll.getPackageName());
				DiscoveryLink discovery = m_linkdConfig.getDiscoveryLink(snmpcoll.getPackageName());
	   			if (discovery.getScheduler() == null) {
	   				discovery.setScheduler(m_scheduler);
	    		}
	    		discovery.schedule();
	    		m_activepackages.add(snmpcoll.getPackageName());

			}
			if (snmpcoll.getScheduler() == null) {
					snmpcoll.setScheduler(m_scheduler);
			}
			if (log().isDebugEnabled())
				log().debug("ScheduleCollectionForNode: Schedulink Snmp Collection for Package/Nodeid: "
						+ snmpcoll.getPackageName() +"/"+node.getNodeId() + "/" +snmpcoll.getInfo());
			snmpcoll.schedule();
		}
	}
	
	protected synchronized void onStart() {

		// start the scheduler
		//
		if (log().isDebugEnabled())
			log().debug("start: Starting linkd scheduler");
		m_scheduler.start();

		// Set the status of the service as running.
		//

	}

	protected synchronized void onStop() {

		// Stop the scheduler
		m_scheduler.stop();
		
		m_eventListener.close();

		m_scheduler = null;

	}

	protected synchronized void onPause() {
		m_scheduler.pause();
	}

	protected synchronized void onResume() {
		m_scheduler.resume();
	}

	public Collection<LinkableNode> getLinkableNodes() {
		synchronized (m_nodes) {
			return m_nodes;
		}
	}

	public Collection<LinkableNode> getLinkableNodesOnPackage(String pkg) {
		Collection<LinkableNode> nodesOnPkg = new ArrayList<LinkableNode>();
		synchronized (m_nodes) {
			Iterator<LinkableNode> ite = m_nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode node = ite.next();
				if (isInterfaceInPackage(node.getSnmpPrimaryIpAddr(), pkg))
					nodesOnPkg.add(node);
			}
			return nodesOnPkg;
		}
	}
	
	public boolean isInterfaceInPackage(String ipaddr, String pkg) {
		if (m_linkdConfig.interfaceInPackage(ipaddr, m_linkdConfig.getPackage(pkg))) return true;
		return false;
	}

	public boolean isInterfaceInPackageRange(String ipaddr, String pkg) {
		if (m_linkdConfig.interfaceInPackageRange(ipaddr, m_linkdConfig.getPackage(pkg))) return true;
		return false;
	}

	void scheduleNodeCollection(int nodeid) {

		LinkableNode node = null;
		// database changed need reload packageiplist
		m_linkdConfig.createPackageIpListMap();
		

		// First of all get Linkable Node
		if (log().isDebugEnabled()) {
			log().debug("scheduleNodeCollection: Loading node " + nodeid
					+ " from database");
		}
		try {

			node = m_queryMgr.getSnmpNode(nodeid);
			if (node == null) {
				log().warn("scheduleNodeCollection: Failed to get Linkable node from DataBase. Exiting");
				return;
			}
		} catch (SQLException sqlE) {
			log().error("scheduleNodeCollection: " +
					"SQL Exception while syncing node object with database information.",sqlE);
			return;
		}
		synchronized (m_nodes) {
	        m_nodes.add(node);
        }
		
		scheduleCollectionForNode(node);

	}

	void wakeUpNodeCollection(int nodeid) {

		LinkableNode node = getNode(nodeid);

		
		if (node == null) {
			log().warn("wakeUpNodeCollection: schedulink a node not found: " + nodeid);
			scheduleNodeCollection(nodeid);
		} else {
			// get collections
			// get readyRunnuble
			// wakeup RR
			Iterator<SnmpCollection> ite = m_linkdConfig.getSnmpCollections(node.getSnmpPrimaryIpAddr(), node.getSysoid()).iterator();
			if (log().isDebugEnabled())
				log().debug("wakeUpNodeCollection: get Snmp Collection from Scratch! Iterating on found.");
			while (ite.hasNext()) {
				ReadyRunnable rr = getReadyRunnable(ite.next());
				if (rr == null) {
					log().warn("wakeUpNodeCollection: found null ReadyRunnable");
					return;
				} else {
					rr.wakeUp();
				}	
			}
		}

	}

	void deleteNode(int nodeid) {

		if (log().isDebugEnabled())
			log().debug("deleteNode: deleting LinkableNode for node "
					+ nodeid);

		try {
			m_queryMgr.update(nodeid, QueryManager.ACTION_DELETE);
		} catch (SQLException sqlE) {
			log().error("scheduleNodeCollection: " +
				"SQL Exception while syncing node object with database information.",sqlE);
		} 
		

		LinkableNode node = removeNode(nodeid);

		if (node == null) {
			log().warn("deleteNode: node not found: " + nodeid);
		} else {
			Iterator<SnmpCollection> ite = m_linkdConfig.getSnmpCollections(node.getSnmpPrimaryIpAddr(), node.getSysoid()).iterator();
			if (log().isDebugEnabled())
				log().debug("deleteNode: get Snmp Collection from Scratch! Iterating on found.");

			while (ite.hasNext()) {
				ReadyRunnable rr = getReadyRunnable(ite.next());
				
				if (rr == null) {
					log().warn("deleteNode: found null ReadyRunnable");
					return;
				} else {
					rr.unschedule();
				}	

			}
			
		}

		// database changed need reload packageiplist
		m_linkdConfig.createPackageIpListMap();

	}
	
	/**
	 * Update database when an interface is deleted
	 * 
	 * @param nodeid
	 *            the nodeid for the node
	 * @param ipAddr
	 *            the ip address of the interface
	 * @param ifIndex
	 *            the ifIndex of the interface
	 */
	void deleteInterface(int nodeid, String ipAddr, int ifIndex) {

		if (log().isDebugEnabled())
			log().debug("deleteInterface: marking table entries as deleted for node "
				+ nodeid + ", with ip address " + (ipAddr != null ? ipAddr : "null")
				+ ", and ifIndex "+ (ifIndex > -1 ? ifIndex : "N/A"));

		try {
			m_queryMgr.updateForInterface(nodeid, ipAddr, ifIndex, QueryManager.ACTION_DELETE);
		} catch (SQLException sqlE) {
			log().error("deleteInterface: " +
				"SQL Exception while updating database.",sqlE);
		} 

		// database changed need reload packageiplist
		m_linkdConfig.createPackageIpListMap();

	}

	void suspendNodeCollection(int nodeid) {
		
		if (log().isDebugEnabled())
			log().debug("suspendNodeCollection: suspend collection LinkableNode for node "
					+ nodeid);

		try {
			m_queryMgr.update(nodeid, QueryManager.ACTION_UPTODATE);
		} catch (SQLException sqlE) {
			log().error("suspendNodeCollection: " +
				"SQL Exception while syncing node object with database information.",sqlE);
		} 

		LinkableNode node = getNode(nodeid);

		if (node == null) {
			log().warn("suspendNodeCollection: found null ReadyRunnable");
		} else {
			// get collections
			// get readyRunnuble
			// suspend RR
			if (log().isDebugEnabled())
				log().debug("suspendNodeCollection: get Snmp Collection from Scratch! Iterating on found.");
			Iterator<SnmpCollection> ite = m_linkdConfig.getSnmpCollections(node.getSnmpPrimaryIpAddr(), node.getSysoid()).iterator();
			while (ite.hasNext()) {
				ReadyRunnable rr = getReadyRunnable(ite.next());
				if (rr == null) {
					log().warn("suspendNodeCollection: suspend: node not found: " + nodeid);
					return;
				} else {
					rr.suspend();
				}	
			}
		}

	}

	private ReadyRunnable getReadyRunnable(ReadyRunnable runnable) {
		if (log().isDebugEnabled()) {
			log().debug("getReadyRunnable: get ReadyRunnable from scheduler: " + runnable.getInfo());
		}
		
		return m_scheduler.getReadyRunnable(runnable);
		
	}


	/**
	 * Method that updates info in List nodes and also save info
	 * into database. This method is called by SnmpCollection after all stuff is
	 * done
	 * 
	 * @param snmpcoll
	 */

	void updateNodeSnmpCollection(SnmpCollection snmpcoll) {

		LinkableNode node = removeNode(snmpcoll.getTarget().getHostAddress());
		if (node == null) {
			log().error("No node found for snmp collection: " + snmpcoll.getInfo() + " unscheduling!");
			m_scheduler.unschedule(snmpcoll);
			return;
		}
		
		try {
			node = m_queryMgr.storeSnmpCollection(node, snmpcoll);
		} catch (SQLException e) {
			log().error("Failed to save on db snmpcollection/package: " + snmpcoll.getPackageName()+"/"+snmpcoll.getInfo() + " " + e);
			return;
		}
		synchronized (m_nodes) {
	        m_nodes.add(node);
        }

	}
	
	/**
	 * Method that uses info in hash snmpprimaryip2nodes and also save info
	 * into database. This method is called by DiscoveryLink after all stuff is
	 * done
	 * 
	 * @param discover
	 */

	void updateDiscoveryLinkCollection(DiscoveryLink discover) {

		try {
			m_queryMgr.storeDiscoveryLink(discover);
		} catch (SQLException e) {
			log().error("Failed to save discoverylink on database for package:" + discover.getPackageName());
		}
	}
	
	/**
	 * Send a newSuspect event for the interface
	 * construct event with 'linkd' as source
	 * 
	 * @param ipInterface
	 *            The interface for which the newSuspect event is to be
	 *            generated
	 * @param ipowner
	 * 			  The host that hold this ipInterface information           
	 * @pkgName
	 * 		      The package Name of the ready runnable involved
	 */
	
	void sendNewSuspectEvent(String ipaddress,String ipowner, String pkgName) {

		if (m_newSuspenctEventsIpAddr.contains(ipaddress) ) {
			log().info("sendNewSuspectEvent: nothing to send suspect event previously sent for ip address: "
							+ ipaddress);
			return;
		} else if (!isInterfaceInPackageRange(ipaddress, pkgName)) {
			log().info("sendNewSuspectEvent: nothing to send for ip address: "
					+ ipaddress + " not in package: " + pkgName);
			return;
		}

		org.opennms.netmgt.config.linkd.Package pkg = m_linkdConfig.getPackage(pkgName);

		boolean autodiscovery = false;
		if (pkg.hasAutoDiscovery()) autodiscovery = pkg.getAutoDiscovery(); 
		else autodiscovery = m_linkdConfig.autoDiscovery();
		
		if ( autodiscovery ) {
				
			Event event = new Event();
			event.setSource("linkd");
			event.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
			event.setHost(ipowner);
			event.setInterface(ipaddress);
			event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

			m_eventListener.getEventMgr().sendNow(event);
			
			m_newSuspenctEventsIpAddr.add(ipaddress);
			
		}
	}

	LinkableNode getNode(int nodeid) {
	    synchronized (m_nodes) {
    		for (LinkableNode node : m_nodes) {
    			if (node.getNodeId() == nodeid) return node;
    		}
            return null;
	    }
	}

	LinkableNode getNode(String ipaddr) {
        synchronized (m_nodes) {
    		for (LinkableNode node : m_nodes) {
    			if (node.getSnmpPrimaryIpAddr().equals(ipaddr)) return node;
    		}
    		return null;
        }
	}

	private LinkableNode removeNode(int nodeid) {
		synchronized (m_nodes) {
			Iterator<LinkableNode> ite = m_nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				if (curNode.getNodeId() == nodeid) {
					ite.remove();
					return curNode;
				}
			}
	        return null;
		}
	}

	private LinkableNode removeNode(String ipaddr) {
		synchronized (m_nodes) {
			Iterator<LinkableNode> ite = m_nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode curNode = ite.next();
				if (curNode.getSnmpPrimaryIpAddr().equals(ipaddr)) {
					ite.remove();
					return curNode;
				}
			}
		}
		return null;
	}
	
    public void setQueryManager(QueryManager queryMgr) {
        m_queryMgr = queryMgr;
    }

	public Scheduler getScheduler() {
		return m_scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}
	
	public LinkdConfig getLinkdConfig() {
		return m_linkdConfig;
	}

	public void setLinkdConfig(LinkdConfig config) {
		m_linkdConfig = config;
	}

	public LinkdEventProcessor getEventListener() {
		return m_eventListener;
	}

	public void setEventListener(LinkdEventProcessor eventListener) {
		m_eventListener = eventListener;
	}
	


}
