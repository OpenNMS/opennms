//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.*;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;

import org.opennms.netmgt.eventd.EventIpcManager;

import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.linkd.QueryManager;

import org.opennms.netmgt.xml.event.Event;

public class Linkd extends AbstractServiceDaemon {

	/**
	 * The log4j category used to log messages.
	 */
	private static final String LOG4J_CATEGORY = "OpenNMS.Linkd";

	/**
	 * Singleton instance of the Linkd class
	 */
	private static final Linkd m_singleton = new Linkd();

	/**
	 * Rescan scheduler thread
	 */
	private Scheduler m_scheduler;

	/**
	 * Event receiver.
	 */
	private LinkdEventProcessor m_receiver;

	/**
	 * List that contains Linkable Nodes.
	 */

	private List<LinkableNode> nodes;

	/**
	 * HashMap that contains SnmpCollections by package.
	 */

	private List<String> activepackages;
	

	/**
	 * Event Manager To Send Events
	 */
	private EventIpcManager m_eventMgr;

	/**
	 * Tha data source to use
	 */
    private DataSource m_dbConnectionFactory;

    /**
     * The Db connection read and write handler
     */
    private QueryManager m_queryMgr;
    
    /**
    * Linkd Configuration Initialization
    */
    
    private LinkdConfig m_linkdConfig;
	
	/**
	 * the list of ipaddress for which new suspect event is sent
	 */

	private List<String> newSuspenctEventsIpAddr= null;
	
	public Linkd() {
		super("OpenNMS.Linkd");
	}

	public static Linkd getInstance() {
		return m_singleton;
	}

	public Category log() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		return ThreadCategory.getInstance();
	}
	
	public synchronized void onInit() {


		if (log().isInfoEnabled())
			log()
					.info("init: Category Level Set to "
							+ log().getLevel().toString());

		nodes = new ArrayList<LinkableNode>();

		activepackages = new ArrayList<String>();
		
		try {
			nodes = m_queryMgr.getSnmpNodeList();
			m_queryMgr.updateDeletedNodes();
		} catch (SQLException e) {
	    	log().fatal("SQL exception executing on database", e);
	        throw new UndeclaredThrowableException(e);
		}
		
		// Create a scheduler
		//
		try {
			if (log().isDebugEnabled())
				log().debug("init: Creating link scheduler");

			m_scheduler = new Scheduler("Linkd", m_linkdConfig.getThreads());
		} catch (RuntimeException e) {
			log().fatal("init: Failed to create linkd scheduler", e);
			throw e;
		} catch (Throwable t) {
			log()
					.error("init: Failed to load threads from linkd configuration file "
							+ t);
		}

		// Load and schedule snmpcollection
		// Schedule the snmp data collection on nodes
		// and construct package2snmpcollection that contains nodes with snmpcollections
		
		Iterator<LinkableNode> ite = nodes.iterator();

		while (ite.hasNext()) {
			//Schedule snmp collection for node and also 
			//schedule discovery link on package where is not active 
			scheduleCollectionForNode(ite.next());
		}

		if (log().isDebugEnabled())
			log().debug("init: Scheduled Ready Runnable for active packages: " + activepackages.toString());

		// initialize the ipaddrsentevents
		newSuspenctEventsIpAddr = new ArrayList<String>();
		newSuspenctEventsIpAddr.add("127.0.0.1");

		// Create an event receiver.
		//
		try {
			if (log().isDebugEnabled()) {
				log().debug("init: Creating event broadcast event receiver");
			}

			m_receiver = new LinkdEventProcessor(this);
		} catch (Throwable t) {
			log().error(
					"init: Failed to initialized the broadcast event receiver",
					t);
			throw new UndeclaredThrowableException(t);
		}
		
		if (log().isInfoEnabled())
			log().info("init: LINKD CONFIGURATION INITIALIZED");

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
			if (activepackages.contains(snmpcoll.getPackageName())) {
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
	    		activepackages.add(snmpcoll.getPackageName());

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
	
	public synchronized void onStart() {

		// start the scheduler
		//
		try {
			if (log().isDebugEnabled())
				log().debug("start: Starting linkd scheduler");

			m_scheduler.start();
		} catch (RuntimeException e) {
				log().fatal("start: Failed to start scheduler", e);
			throw e;
		}

		// Set the status of the service as running.
		//

	}

	public synchronized void onStop() {

		// Stop the scheduler
		m_scheduler.stop();
		// Stop the broadcast event receiver
		//
		m_receiver.close();

		m_scheduler = null;

	}

	public synchronized void onPause() {
		m_scheduler.pause();
	}

	public synchronized void onResume() {
		m_scheduler.resume();
	}

	public Collection<LinkableNode> getLinkableNodes() {
		synchronized (nodes) {
			return nodes;
		}
	}

	public Collection<LinkableNode> getLinkableNodesOnPackage(String pkg) {
		Collection<LinkableNode> nodesOnPkg = new ArrayList<LinkableNode>();
		synchronized (nodes) {
			Iterator<LinkableNode> ite = nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode node = ite.next();
				if (m_linkdConfig.interfaceInPackage(node.getSnmpPrimaryIpAddr(), m_linkdConfig.getPackage(pkg)))
					nodesOnPkg.add(node);
			}
			return nodesOnPkg;
		}
	}
	
	public boolean isInterfaceInPackage(String ipaddr, String pkg) {
		if (m_linkdConfig.interfaceInPackage(ipaddr, m_linkdConfig.getPackage(pkg))) return true;
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
		nodes.add(node);
		
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
		nodes.add(node);

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
	void sendNewSuspectEvent(String ipInterface,String ipowner, String pkgName) {
		if (newSuspenctEventsIpAddr.contains(ipInterface)) {
			return;
		}

		org.opennms.netmgt.config.linkd.Package pkg = m_linkdConfig.getPackage(pkgName);

		boolean autodiscovery = false;
		if (pkg.hasAutoDiscovery() && m_linkdConfig.interfaceInPackage(ipInterface, pkg)) autodiscovery = pkg.getAutoDiscovery(); 
		else autodiscovery = m_linkdConfig.autoDiscovery();
		
		if ( autodiscovery ) {
				
			Event event = new Event();
			event.setSource("linkd");
			event.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
			event.setHost(ipowner);
			event.setInterface(ipInterface);
			event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

			// send the event to eventd
			m_eventMgr.sendNow(event);
			
			newSuspenctEventsIpAddr.add(ipInterface);
			
		}
	}


	public DataSource getDbConnectionFactory() {
		return m_dbConnectionFactory;
	}

	public void setDbConnectionFactory(DataSource connectionFactory) {
		m_dbConnectionFactory = connectionFactory;
	}

	public EventIpcManager getEventMgr() {
		return m_eventMgr;
	}

	public void setEventMgr(EventIpcManager mgr) {
		m_eventMgr = mgr;
	}

	public LinkdConfig getLinkdConfig() {
		return m_linkdConfig;
	}

	public void setLinkdConfig(LinkdConfig config) {
		m_linkdConfig = config;
	}
	
	LinkableNode getNode(int nodeid) {
		synchronized (nodes) {
			Iterator<LinkableNode> ite = nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode node = ite.next();
				if (node.getNodeId() == nodeid) return node;
			}
		}
		return null;
	}

	LinkableNode getNode(String ipaddr) {
		
		synchronized (nodes) {
			Iterator<LinkableNode> ite = nodes.iterator();
			while (ite.hasNext()) {
				LinkableNode node = ite.next();
				if (node.getSnmpPrimaryIpAddr().equals(ipaddr)) return node;
			}
		}
		return null;
	}
	

	private LinkableNode removeNode(int nodeid) {
		List<LinkableNode> nodeses = new ArrayList<LinkableNode>();
		LinkableNode node = null;
		synchronized (nodes) {
			Iterator<LinkableNode> ite = nodes.iterator();
			while (ite.hasNext()) {
				 LinkableNode curNode = ite.next();
				if (curNode.getNodeId() == nodeid) node=curNode;
				else nodeses.add(curNode);
			}
			nodes = nodeses;
		}
		return node;
	}

	private LinkableNode removeNode(String ipaddr) {
		List<LinkableNode> nodeses = new ArrayList<LinkableNode>();
		LinkableNode node = null;
		synchronized (nodes) {
			Iterator<LinkableNode> ite = nodes.iterator();
			while (ite.hasNext()) {
				 LinkableNode curNode = ite.next();
				if (curNode.getSnmpPrimaryIpAddr().equals(ipaddr)) node=curNode;
				else nodeses.add(curNode);
			}
			nodes = nodeses;
		}
		return node;
	}
	
	private ReadyRunnable getReadyRunnable(ReadyRunnable runnable) {
		if (log().isDebugEnabled()) {
			log().debug("getReadyRunnable: get ReadyRunnable from scheduler: " + runnable.getInfo());
		}
		
		return m_scheduler.getReadyRunnable(runnable);
		
	}

    public void setQueryManager(QueryManager queryMgr) {
        m_queryMgr = queryMgr;
    }

}