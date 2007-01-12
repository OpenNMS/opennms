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

import java.beans.PropertyVetoException;
import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.core.fiber.*;

public class Linkd implements PausableFiber {

	/**
	 * The log4j category used to log messages.
	 */
	private static final String LOG4J_CATEGORY = "OpenNMS.Linkd";

	/**
	 * Singleton instance of the Linkd class
	 */
	private static final Linkd m_singleton = new Linkd();

	/**
	 * Current status of this fiber
	 */
	private int m_status;

	/**
	 * Rescan scheduler thread
	 */
	private Scheduler m_scheduler;

	/**
	 * Event receiver.
	 */
	private LinkdEventProcessor m_receiver;

	/**
	 * HashMap that contains Linkable Snmp Nodes by primary ip address.
	 */

	private HashMap<String,LinkableNode> snmpprimaryip2nodes;

	/**
	 * HashMap that contains SnmpCollection by nodeid.
	 */

	private HashMap<Integer,SnmpCollection> nodeid2snmpcollection;
	

	/**
	 * Event Manager To Send Events
	 */
	private EventIpcManager m_eventMgr;

	private boolean scheduledDiscoveryLink = false;

	/**
	 * the snmp poll interval in ms
	 */

	private static long m_snmp_poll_interval = 1800000;

	/**
	 * the initial sleep time interval in ms
	 */

	private static long m_initial_sleep_time = 300000;

	/**
	 * the discovery link time interval in ms
	 */

	private static long m_discovery_link_interval = 600000;

	/**
	 * boolean operator that tells if to do auto discovery
	 */

	private static boolean m_auto_discovery = false;

	/**
	 * the list of ipaddress for which new suspect event is sent
	 */

	private List<String> newSuspenctEventsIpAddr= null;
	
	private Linkd() {
		m_scheduler = null;
		m_status = START_PENDING;
	}

	public static Linkd getInstance() {
		return m_singleton;
	}

	public synchronized void init() {

		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		if (log.isInfoEnabled())
			log
					.info("init: Category Level Set to "
							+ log.getLevel().toString());

		// Initialize the Capsd configuration factory.
		//
		try {
			CapsdConfigFactory.init();
		} catch (MarshalException ex) {
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (ValidationException ex) {
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (IOException ex) {
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		// Initialize the Linkd configuration factory.
		//
		try {
			LinkdConfigFactory.init();
		} catch (ClassNotFoundException ex) {
			log.error("init: Failed to load linkd configuration file ", ex);
			return;
		} catch (MarshalException ex) {
			log.error("init: Failed to load linkd configuration file ", ex);
			return;
		} catch (ValidationException ex) {
			log.error("init: Failed to load linkd configuration file ", ex);
			return;
		} catch (IOException ex) {
			log.error("init: Failed to load linkd configuration file ", ex);
			return;
		}

		snmpprimaryip2nodes = new HashMap<String,LinkableNode>();

		nodeid2snmpcollection = new HashMap<Integer,SnmpCollection>();
		
		// Initialize the SNMP Peer Factory
		//
		try {
			SnmpPeerFactory.reload();
		} catch (MarshalException ex) {
			log.error("Failed to load SNMP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (ValidationException ex) {
			log.error("Failed to load SNMP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (IOException ex) {
			log.error("Failed to load SNMP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		// Initialize the Database configuration factory
		//
        // Initialize the Database configuration factory
        try {
            DataSourceFactory.init();
        } catch (IOException ie) {
            log.fatal("IOException loading database config", ie);
            throw new UndeclaredThrowableException(ie);
        } catch (MarshalException me) {
            log.fatal("Marshall Exception loading database config", me);
            throw new UndeclaredThrowableException(me);
        } catch (ValidationException ve) {
            log.fatal("Validation Exception loading database config", ve);
            throw new UndeclaredThrowableException(ve);
        } catch (ClassNotFoundException ce) {
            log.fatal("Class lookup failure loading database config", ce);
            throw new UndeclaredThrowableException(ce);
        } catch (PropertyVetoException pve) {
            log.fatal("Property veto failure loading database config", pve);
            throw new UndeclaredThrowableException(pve);
        } catch (SQLException sqle) {
            log.fatal("SQL exception loading database config", sqle);
            throw new UndeclaredThrowableException(sqle);
        }


		try {
			m_initial_sleep_time = LinkdConfigFactory.getInstance()
					.getInitialSleepTime();
		} catch (Throwable t) {
			log
					.error("init: Failed to load Initial Sleep Time from linkd configuration file "
							+ t);
		}

		try {
			m_snmp_poll_interval = LinkdConfigFactory.getInstance()
					.getSnmpPollInterval();
		} catch (Throwable t) {
			log
					.error("init: Failed to load Snmp Poll Interval from linkd configuration file "
							+ t);
		}

		try {
			m_discovery_link_interval = LinkdConfigFactory.getInstance()
					.getDiscoveryLinkInterval();
		} catch (Throwable t) {
			log
					.error("init: Failed to load Discovery Link Interval from linkd configuration file "
							+ t);
		}

		try {
			m_auto_discovery = LinkdConfigFactory.getInstance()
					.autoDiscovery();
		} catch (Throwable t) {
			log
					.error("init: Failed to load Auto Discovery from linkd configuration file "
							+ t);
		}

		java.sql.Connection dbConn = null;

		try {
			dbConn = DataSourceFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log
						.debug("init: Loading Snmp nodes");
			}
			snmpprimaryip2nodes = LinkdConfigFactory.getInstance()
						.getLinkableNodes(dbConn);
			nodeid2snmpcollection = LinkdConfigFactory.getInstance().getSnmpColls(dbConn);
			LinkdConfigFactory.getInstance().updateDeletedNodes(dbConn);
		} catch (SQLException sqlE) {
			log
					.fatal(
							"SQL Exception while syncing node object with database information.",
							sqlE);
			throw new UndeclaredThrowableException(sqlE);
		} catch (Throwable t) {
			log
					.fatal(
							"Unknown error while syncing node object with database information.",
							t);
			throw new UndeclaredThrowableException(t);
		} finally {
			try {
				if (dbConn != null) {
					dbConn.close();
				}
			} catch (Exception e) {
		
			}
		}

		// Create a scheduler
		//
		try {
			if (log.isDebugEnabled())
				log.debug("init: Creating link scheduler");

			m_scheduler = new Scheduler("Linkd", LinkdConfigFactory
					.getInstance().getThreads());
		} catch (RuntimeException e) {
			log.fatal("init: Failed to create linkd scheduler", e);
			throw e;
		} catch (Throwable t) {
			log
					.error("init: Failed to load threads from linkd configuration file "
							+ t);
		}

		// Schedule the snmp data collection on nodes
		//

		if (log.isDebugEnabled())
			log.debug("init: scheduling "
					+ snmpprimaryip2nodes.size() + " Snmp Collections ");

		SnmpCollection snmpCollector = null;

		Iterator<SnmpCollection> ite = nodeid2snmpcollection.values().iterator();
		while (ite.hasNext()) {
			snmpCollector = ite.next();
			log.debug("init: scheduling Snmp Collection for ip "
					+ snmpCollector.getTarget().getHostAddress());
			synchronized (snmpCollector) {
				if (snmpCollector.getScheduler() == null) {
					snmpCollector.setScheduler(m_scheduler);
				}
				snmpCollector.setPollInterval(m_snmp_poll_interval);
				snmpCollector.setInitialSleepTime(m_initial_sleep_time);
				m_initial_sleep_time = m_initial_sleep_time	+ 30000;
			}
				snmpCollector.schedule();
		}
			// Schedule the discovery link on nodes
			//
		if (!nodeid2snmpcollection.isEmpty()) {
			DiscoveryLink discoveryLink = new DiscoveryLink();
			if (log.isDebugEnabled())
				log
						.debug("init: scheduling Discovery Link");
	
			synchronized (discoveryLink) {
				if (discoveryLink.getScheduler() == null) {
					discoveryLink.setScheduler(m_scheduler);
				}
				discoveryLink.setSnmpPollInterval(m_snmp_poll_interval);
				discoveryLink.setDiscoveryInterval(m_discovery_link_interval);
				discoveryLink.setInitialSleepTime(m_initial_sleep_time);
			}
			discoveryLink.schedule();
			scheduledDiscoveryLink = true;
		}

		// Create the IPCMANAGER
		
		EventIpcManagerFactory.init();
		m_eventMgr = EventIpcManagerFactory.getIpcManager();
		if (log.isDebugEnabled()) {
			log.debug("init: Creating event Manager");
		}

		// initialize the ipaddrsentevents
		newSuspenctEventsIpAddr = new ArrayList<String>();
		newSuspenctEventsIpAddr.add("127.0.0.1");

		// Create an event receiver.
		//
		try {
			if (log.isDebugEnabled()) {
				log.debug("init: Creating event broadcast event receiver");
			}

			m_receiver = new LinkdEventProcessor(this);
		} catch (Throwable t) {
			log.error(
					"init: Failed to initialized the broadcast event receiver",
					t);
			throw new UndeclaredThrowableException(t);
		}
		
		if (log.isInfoEnabled())
			log.info("init: LINKD CONFIGURATION INITIALIZED");

	}

	public synchronized void start() {
		m_status = STARTING;

		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		// get the category logger
		Category log = ThreadCategory.getInstance();

		if (log.isDebugEnabled())
			log.debug("start: Starting Linkd");

		// start the scheduler
		//
		try {
			if (log.isDebugEnabled())
				log.debug("start: Starting linkd scheduler");

			m_scheduler.start();
		} catch (RuntimeException e) {
				log.fatal("start: Failed to start scheduler", e);
			throw e;
		}

		// Set the status of the service as running.
		//
		m_status = RUNNING;

		if (log.isInfoEnabled())
			log.info("start: Linkd running");

	}

	public synchronized void stop() {

		m_status = STOP_PENDING;
		// Stop the scheduler
		m_scheduler.stop();
		// Stop the broadcast event receiver
		//
		m_receiver.close();

		m_scheduler = null;
		m_status = STOPPED;
		Category log = ThreadCategory.getInstance();
		if (log.isInfoEnabled())
			log.info("stop: Linkd stopped");

	}

	public synchronized void reload() throws IOException {

	}

	public synchronized void pause() {
		if (m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;
		m_scheduler.pause();
		m_status = PAUSED;

		Category log = ThreadCategory.getInstance();
		if (log.isInfoEnabled())
			log.info("pause: Linkd paused");
	}

	public synchronized void resume() {
		if (m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;
		m_scheduler.resume();
		m_status = RUNNING;

		Category log = ThreadCategory.getInstance();
		if (log.isInfoEnabled())
			log.info("resume: Linkd resumed");
	}

	public String getName() {
		return "OpenNMS.Linkd";
	}

	public int getStatus() {
		return m_status;
	}

	public LinkableNode[] getSnmpLinkableNodes() {
		synchronized (snmpprimaryip2nodes) {
			return (LinkableNode[]) snmpprimaryip2nodes.values().toArray(
					new LinkableNode[0]);
		}
	}

	void scheduleNodeCollection(int nid) {

		Category log = ThreadCategory.getInstance();
		java.sql.Connection dbConn = null;
		SnmpCollection coll = null;
		boolean scheduleSnmpCollection = true;
		
		// First of all get SnmpCollection
		try {
			dbConn = DataSourceFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("scheduleNodeCollection: Loading node " + nid
						+ " from database");
			}
			try {
				coll = LinkdConfigFactory.getInstance().getSnmpCollection(
						dbConn, nid);
				if (coll == null) {
					log.warn("scheduleNodeCollection: Failed to get Linkable node from LinkdConfigFactory. Exiting");
					return;
				}
			} catch (UnknownHostException h) {
					log.warn("scheduleNodeCollection: Failed to get Linkable node from LinkdConfigFactory"
									+ h);
			}
		} catch (SQLException sqlE) {
			log
					.fatal(
							"scheduleNodeCollection: SQL Exception while syncing node object with database information.",
							sqlE);
			throw new UndeclaredThrowableException(sqlE);
		} catch (Throwable t) {
			log
					.fatal(
							"scheduleNodeCollection: Unknown error while syncing node object with database information.",
							t);
			throw new UndeclaredThrowableException(t);
		} finally {
			try {
				if (dbConn != null) {
					dbConn.close();
				}
			} catch (SQLException e) {
				log
						.error(
								"scheduleNodeCollection: SQL Exception while syncing node object with database information.",
								e);
			}
		}

		// you have to test if an old collection for this node exists 
		if (nodeid2snmpcollection.containsKey(new Integer(nid))) {
			SnmpCollection oldColl = (SnmpCollection) nodeid2snmpcollection.get(new Integer(nid));
			if (oldColl.equals(coll)) {
				scheduleSnmpCollection = false;
				coll = oldColl;
			} else {
				// the new collection is change
				// first of all unschedule
				// second remove from hash
				oldColl.unschedule();
				synchronized (snmpprimaryip2nodes) {
					snmpprimaryip2nodes.remove(getLinkableNodeKey(nid));
				}
			}
		}
			
		// this means that same snmpcollection exists
		if (scheduleSnmpCollection && snmpprimaryip2nodes.containsKey(coll.getTarget().getHostAddress())) {
			// this means that collection is the same but nodes should be different
			scheduleSnmpCollection = false;
			
			LinkableNode oldNode = (LinkableNode) snmpprimaryip2nodes
					.get(coll.getTarget().getHostAddress());

			// first of all set status to D on linkd tables for old node if
			// different from new
			if (nid != oldNode.getNodeId()) {
				DbEventWriter dbwriter = new DbEventWriter(oldNode.getNodeId(),
						DbEventWriter.ACTION_DELETE);
				dbwriter.run();
				nodeid2snmpcollection.remove(new Integer(oldNode.getNodeId()));
			} else {
				coll = (SnmpCollection) nodeid2snmpcollection.get(new Integer(nid));
			}
		}
		
		LinkableNode node = new LinkableNode(nid,coll.getTarget().getHostAddress());
		
		synchronized (nodeid2snmpcollection) {
			snmpprimaryip2nodes.put(coll.getTarget().getHostAddress(), node);
		}
		
		synchronized (snmpprimaryip2nodes) {
			nodeid2snmpcollection.put(new Integer(nid), coll);
		}

		
		// schedule collection if
		if (scheduleSnmpCollection) {
			synchronized (coll) {
				if (coll.getScheduler() == null) {
					coll.setScheduler(m_scheduler);
				}
				coll.setPollInterval(m_snmp_poll_interval);
				coll.setInitialSleepTime(0);
				coll.schedule();
			}
			
			if (!scheduledDiscoveryLink) {
				DiscoveryLink discoveryLink = new DiscoveryLink();
	
				if (log.isDebugEnabled())
					log
							.debug("scheduleNodeCollection: scheduling Discovery Link");
				synchronized (discoveryLink) {
					if (discoveryLink.getScheduler() == null) {
						discoveryLink.setScheduler(m_scheduler);
					}
					discoveryLink.setSnmpPollInterval(m_snmp_poll_interval);
					discoveryLink.setDiscoveryInterval(m_discovery_link_interval);
					discoveryLink.setInitialSleepTime(0);
				}
				discoveryLink.schedule();
				scheduledDiscoveryLink = true;
			}
		}

	}

	void wakeUpNodeCollection(int nid) {

		SnmpCollection snmpcoll = null;
		synchronized (nodeid2snmpcollection) {
			if (nodeid2snmpcollection.containsKey(new Integer(nid))) {
				snmpcoll = (SnmpCollection) nodeid2snmpcollection.get(new Integer(nid));
			}
		}

		// work on snmpcollection
		ReadyRunnable rr = m_scheduler.getReadyRunnable(snmpcoll);

		if (rr == null) {
			scheduleNodeCollection(nid);
		} else {
			rr.wakeUp();
			nodeid2snmpcollection.put(new Integer(nid), (SnmpCollection)rr);
		}
	}

	void deleteNode(int nid) {

		Category log = ThreadCategory.getInstance();

		SnmpCollection collection = null;
		if (log.isDebugEnabled())
			log.debug("deleteNode: deleting LinkableNode for node "
					+ nid);
		
		if (nodeid2snmpcollection.containsKey(new Integer(nid))) {
			collection = (SnmpCollection) nodeid2snmpcollection.get(new Integer(nid));
		} else log.warn("deleteNode: no snmp collection found for node " + nid);
			
		String nodekey = getLinkableNodeKey(nid);

		//test if collectionkey is the same
		if (nodekey != null) {
			if (log.isInfoEnabled())
				log.info("deleteNode: removing linkable node for nodeid " + nid + " key " + nodekey);
			synchronized (snmpprimaryip2nodes) {
				snmpprimaryip2nodes.remove(nodekey);
			}
		} else {
			if (log.isInfoEnabled())
				log.info("deleteNode: no linkable node found for nodeid " + nid);
		}

		if (collection != null) {
			// a nodeid with the same collection exists so
			// verify that rr exists if not add
			// else do nothing
			ReadyRunnable rr = m_scheduler.getReadyRunnable(collection);
			if (rr == null) {
				log.warn("deleteNode: Failed to get " + collection.getInfo() 
										+ " with nodeid "
										+ nid);
			} else {
				rr.unschedule();
			}
		}

		DbEventWriter dbwriter = new DbEventWriter(nid, DbEventWriter.ACTION_DELETE);
		dbwriter.run();

	}

	void suspendNodeCollection(int nid) {
		
		SnmpCollection snmpcoll = null;
		synchronized (nodeid2snmpcollection) {
			if (nodeid2snmpcollection.containsKey(new Integer(nid))) {
				snmpcoll = (SnmpCollection) nodeid2snmpcollection.get(new Integer(nid));
			}
		}

		// work on snmpcollection
		ReadyRunnable rr = m_scheduler.getReadyRunnable(snmpcoll);

		if (rr == null) {
			scheduleNodeCollection(nid);
			synchronized (nodeid2snmpcollection) {
				if (nodeid2snmpcollection.containsKey(new Integer(nid))) {
					snmpcoll = (SnmpCollection) nodeid2snmpcollection.get(new Integer(nid));
				}
			}
			rr = m_scheduler.getReadyRunnable(snmpcoll);
		} 
		
		if (rr != null) rr.suspend();
		nodeid2snmpcollection.put(new Integer(nid), (SnmpCollection)rr);
		
		DbEventWriter dbwriter = new DbEventWriter(nid, DbEventWriter.ACTION_UPTODATE);
		dbwriter.run();
	}

	/**
	 * Method that updates info in hash snmpprimaryip2nodes and also save info
	 * into database. This method is called by SnmpCollection after all stuff is
	 * done
	 * 
	 * @param snmpcoll
	 */

	void updateNodeSnmpCollection(SnmpCollection snmpcoll) {

		Category log = ThreadCategory.getInstance();
		synchronized (snmpprimaryip2nodes) {
			if (snmpprimaryip2nodes.containsKey(snmpcoll.getTarget()
					.getHostAddress())) {
				LinkableNode node = (LinkableNode) snmpprimaryip2nodes.get(snmpcoll
						.getTarget().getHostAddress());
				DbEventWriter dbwriter = new DbEventWriter(node.getNodeId(), snmpcoll);
				dbwriter.setAutoDiscovery(m_auto_discovery);
				dbwriter.run();
				node = dbwriter.getLinkableNode();
				snmpprimaryip2nodes.put(snmpcoll.getTarget().getHostAddress(),
						node);
			} else {
					log
							.warn("updateNodeSnmpCollection: cannot find Linkable SNMP Node element in hash snmpprimaryip2nodes");
			}
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

		DbEventWriter dbwriter = new DbEventWriter(discover);
		dbwriter.run();
	}
	
	/**
	 * Send a newSuspect event for the interface
	 * 
	 * @param trapInterface
	 *            The interface for which the newSuspect event is to be
	 *            generated
	 */
	void sendNewSuspectEvent(String ipInterface,String ipowner) {
		// construct event with 'linkd' as source
		
		// first of all verify that ipaddress has been not sent
		if (!newSuspenctEventsIpAddr.contains(ipInterface)) {
			
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

	EventIpcManager getIpcManager() {
		return m_eventMgr;
	}

	private String getLinkableNodeKey(int nid) {
		synchronized (snmpprimaryip2nodes) {
			Iterator<Entry<String, LinkableNode>> it = snmpprimaryip2nodes.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, LinkableNode> entry = it.next();
				if ( entry.getValue().getNodeId() == nid ) return entry.getKey();
			}
		}
		return null;
	}


}