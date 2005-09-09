package org.opennms.netmgt.linkd;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.*;
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

	private HashMap snmpprimaryip2nodes;

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

		if (log.isEnabledFor(Priority.WARN))
			log
					.warn("init: Category Level Set to "
							+ log.getLevel().toString());

		// Initialize the Capsd configuration factory.
		//
		try {
			CapsdConfigFactory.reload();
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

		snmpprimaryip2nodes = new HashMap();

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
		try {
			DatabaseConnectionFactory.init();
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
		}

		java.sql.Connection dbConn = null;
		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("init: Loading nodes fron database");
			}
			try {
				snmpprimaryip2nodes = LinkdConfigFactory.getInstance()
						.getLinkableSnmpNodes(dbConn);
			} catch (UnknownHostException h) {
				if (log.isEnabledFor(Priority.WARN))
					log
							.warn("init: Failed to get Snmp Peer from snmppeerfactory"
									+ h);
			}
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
				log.debug("start: Creating link scheduler");

			m_scheduler = new Scheduler("Linkd", LinkdConfigFactory
					.getInstance().getThreads());
		} catch (RuntimeException e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to create linkd scheduler", e);
			throw e;
		} catch (Throwable t) {
			if (log.isEnabledFor(Priority.WARN)) {
				log
						.warn("init: Failed to load threads from linkd configuration file "
								+ t);
			}
		}
		if (log.isDebugEnabled())
			log.debug("start: Scheduling existing interfaces");

		// Schedule the snmp data collection on nodes
		//
		try {
			scheduleSnmpCollections();
		} catch (UnknownHostException sqlE) {
			if (log.isEnabledFor(Priority.ERROR))
				log
						.error("start: Failed to schedule existing interfaces",
								sqlE);
		}

		// Schedule the discovery link on nodes
		//
		try {
			scheduleDiscoveryLink();
		} catch (UnknownHostException sqlE) {
			if (log.isEnabledFor(Priority.ERROR))
				log
						.error("start: Failed to schedule existing interfaces",
								sqlE);
		}

		// Create an event receiver.
		//
		try {
			if (log.isDebugEnabled()) {
				log.debug("init: Creating event broadcast event receiver");
			}

			m_receiver = new LinkdEventProcessor(this);
		} catch (Throwable t) {
			log.error("Failed to initialized the broadcast event receiver", t);
			throw new UndeclaredThrowableException(t);
		}
		
		if (log.isEnabledFor(Priority.INFO))
			log.info("init: LINKD CONFIGURATION INITIALIZED");

}

	public synchronized void start() {
		m_status = STARTING;

		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		// get the category logger
		Category log = ThreadCategory.getInstance();

		if (log.isDebugEnabled())
			log.debug("start: Initializing Linkd");

		// start the scheduler
		//
		try {
			if (log.isDebugEnabled())
				log.debug("start: Starting linkd scheduler");

			m_scheduler.start();
		} catch (RuntimeException e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to start scheduler", e);
			throw e;
		}

		// Set the status of the service as running.
		//
		m_status = RUNNING;

		if (log.isDebugEnabled())
			log.debug("start: Linkd running");

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
		if (log.isDebugEnabled())
			log.debug("stop: Linkd stopped");

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
		if (log.isDebugEnabled())
			log.debug("pause: Linkd paused");
	}

	public synchronized void resume() {
		if (m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;
		m_scheduler.resume();
		m_status = RUNNING;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("resume: Linkd resumed");
	}

	public String getName() {
		return "OpenNMS.Linkd";
	}

	public int getStatus() {
		return m_status;
	}

	public LinkableSnmpNode[] getSnmpLinkableNodes() {
		synchronized (snmpprimaryip2nodes) {
			return	(LinkableSnmpNode[]) snmpprimaryip2nodes.values().toArray(new LinkableSnmpNode[0]);
		}
	}

	private void scheduleSnmpCollections() throws UnknownHostException {
		// get the category logger
		//
		Category log = ThreadCategory.getInstance();

		if(log.isDebugEnabled()) log.debug("scheduling " + snmpprimaryip2nodes.size() + " snmp primary ip nodes");

		Iterator it = snmpprimaryip2nodes.values().iterator();
		while (it.hasNext()) {
			LinkableSnmpNode curNode = (LinkableSnmpNode) it.next();
			SnmpCollection snmpCollector = null;
			if (!curNode.hasVlanOid()) {
				snmpCollector = new SnmpCollection(curNode
					.getSnmpPeer());
			} else {
				snmpCollector = new SnmpCollection(curNode
						.getSnmpPeer(),curNode.getVlanOid());
			}
	        synchronized(snmpCollector) {
	            if (snmpCollector.getScheduler() == null) {
	                snmpCollector.setScheduler(m_scheduler);
	            }
	            try {
					snmpCollector.setInterval(LinkdConfigFactory
							.getInstance().getSnmpPollInterval());
				} catch (Throwable t) {
					if (log.isEnabledFor(Priority.WARN)) {
						log
								.warn("init: Failed to load Snmp Poll Interval from linkd configuration file "
										+ t);
					}
				}
	            try {
					snmpCollector.setInitialSleepTime(LinkdConfigFactory
							.getInstance().getInitialSleepTime());
				} catch (Throwable t) {
					if (log.isEnabledFor(Priority.WARN)) {
						log
								.warn("init: Failed to load Initial Sleep Time from linkd configuration file "
										+ t);
					}
				}
	        } 
	        snmpCollector.schedule();
		}
	}

	private void scheduleSnmpCollection(SnmpCollection snmpCollector) throws UnknownHostException {
		// get the category logger
		//
		Category log = ThreadCategory.getInstance();

		if(log.isDebugEnabled()) log.debug("scheduling " + snmpCollector.getTarget().toString() + " snmp primary ip nodes");
		synchronized(snmpCollector) {
            if (snmpCollector.getScheduler() == null) {
                snmpCollector.setScheduler(m_scheduler);
            }
            try {
				snmpCollector.setInterval(LinkdConfigFactory
						.getInstance().getSnmpPollInterval());
			} catch (Throwable t) {
				if (log.isEnabledFor(Priority.WARN)) {
					log
							.warn("scheduleNode: Failed to load Snmp Poll Interval from linkd configuration file "
									+ t);
				}
			}
        }
        snmpCollector.schedule();
	}

	void scheduleNode(int nid) throws UnknownHostException {

		Category log = ThreadCategory.getInstance();
		java.sql.Connection dbConn = null;
		LinkableSnmpNode curNode = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("init: Loading node " + nid + " from database");
			}
			try {
				curNode = LinkdConfigFactory.getInstance().GetLinkableSnmpNode(
						dbConn, nid);
			} catch (UnknownHostException h) {
				if (log.isEnabledFor(Priority.WARN))
					log
							.warn("init: Failed to get Snmp Peer from snmppeerfactory"
									+ h);
			}
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

		SnmpCollection snmpCollector = null;
		synchronized (snmpprimaryip2nodes) {
			if (snmpprimaryip2nodes.containsKey(curNode.getSnmpPrimaryIpAddr())) {
				LinkableSnmpNode oldNode = (LinkableSnmpNode) snmpprimaryip2nodes.get(curNode.getSnmpPrimaryIpAddr());
				snmpCollector = oldNode.getSnmpCollection();
				// first of all set staus to D forold node if different from new 
				if (curNode.getNodeId() != oldNode.getNodeId()) {
					DbWriter dbwriter = new DbWriter(oldNode.getNodeId(), DbWriter.ACTION_DELETE);
					dbwriter.run();
				}
				// second work on snmpcollection if necessary

				if (snmpCollector == null) {
					if (!curNode.hasVlanOid()) {
						snmpCollector = new SnmpCollection(curNode
							.getSnmpPeer());
					} else {
						snmpCollector = new SnmpCollection(curNode
								.getSnmpPeer(),curNode.getVlanOid());
					}
					scheduleSnmpCollection(snmpCollector);
				} else {
					if ( snmpCollector.isSuspendCollection() && curNode.hasSameVlanOid(oldNode)) {
							m_scheduler.wakeUp(snmpCollector,snmpCollector.getInterval());
					} else if (!curNode.hasSameVlanOid(oldNode)) {
						m_scheduler.unschedule(snmpCollector,snmpCollector.getInterval());
						if (!curNode.hasVlanOid()) {
							snmpCollector = new SnmpCollection(curNode
								.getSnmpPeer());
						} else {
							snmpCollector = new SnmpCollection(curNode
									.getSnmpPeer(),curNode.getVlanOid());
						}
						scheduleSnmpCollection(snmpCollector);
					}
				}
			snmpprimaryip2nodes.put(curNode.getSnmpPrimaryIpAddr(), curNode);
			if (log.isEnabledFor(Priority.WARN))
			log.warn("scheduleNode: adding Linkable SNMP Node element in hash snmpprimaryip2nodes node/ip " + curNode.getNodeId() 
					+ "/" + curNode.getSnmpPrimaryIpAddr());
			}
		}
	}

	void unscheduleNode(int nid, char status) throws UnknownHostException {
		Category log = ThreadCategory.getInstance();
		LinkableSnmpNode curNode = null;
		synchronized (snmpprimaryip2nodes) {
			Iterator it = snmpprimaryip2nodes.values().iterator();
			while (it.hasNext()) {
				curNode = (LinkableSnmpNode)it.next();
				if (curNode.getNodeId() == nid) break;
			}
			if (curNode == null) {
				if (log.isEnabledFor(Priority.WARN))
					log.warn("removeNode: cannot find Linkable snmp node to remove for nodeid " + nid);
				DbWriter dbwriter = new DbWriter(nid, status);
				dbwriter.run();
				return;
			}
			
			SnmpCollection snmpcoll = curNode.getSnmpCollection();
			if (status == DbWriter.ACTION_UPDATE) {
				m_scheduler.suspend(snmpcoll, snmpcoll.getInterval());
				snmpcoll = new SnmpCollection(curNode.getSnmpPeer());
				curNode.setSnmpCollection(snmpcoll);
				snmpprimaryip2nodes
				.put(curNode.getSnmpPrimaryIpAddr(), curNode);
			} else if (status == DbWriter.ACTION_DELETE) {
				m_scheduler.unschedule(snmpcoll, snmpcoll.getInterval());
				snmpprimaryip2nodes.remove(curNode.getSnmpPrimaryIpAddr());
			}
			DbWriter dbwriter = new DbWriter(curNode, status);
			dbwriter.run();
		}
	}

	private void scheduleDiscoveryLink() throws UnknownHostException {

		Category log = ThreadCategory.getInstance();
		DiscoveryLink discoveryLink = new DiscoveryLink();

		synchronized(discoveryLink) {
            if (discoveryLink.getScheduler() == null) {
                discoveryLink.setScheduler(m_scheduler);
            }
            try {
    			discoveryLink.setInterval(LinkdConfigFactory
    					.getInstance().getDiscoveryLinkInterval());
    		} catch (Throwable t) {
    			if (log.isEnabledFor(Priority.WARN)) {
    				log
    						.warn("init: Failed to load Discovery Link Interval from linkd configuration file "
    								+ t);
    			}
    		}
            try {
				discoveryLink.setInitialSleepTime(LinkdConfigFactory
						.getInstance().getInitialSleepTime());
			} catch (Throwable t) {
				if (log.isEnabledFor(Priority.WARN)) {
					log
							.warn("init: Failed to load Initial Sleep Time from linkd configuration file "
									+ t);
				}
			}
        } 
        discoveryLink.schedule();

	}

	/**
	 * Method that updates info in hash snmpprimaryip2nodes and also save info
	 * into database.
	 * This method is called by SnmpCollection after all stuff is done
	 * 
	 * @param snmpcoll
	 */

	void updateNodeSnmpCollection(SnmpCollection snmpcoll) {
		LinkableSnmpNode node = null;
		Category log = ThreadCategory.getInstance();
		synchronized (snmpprimaryip2nodes) {
			if (snmpprimaryip2nodes.containsKey(snmpcoll.getTarget()
					.getHostAddress())) {
				node = (LinkableSnmpNode) snmpprimaryip2nodes
					.get(snmpcoll.getTarget().getHostAddress());
				node.setSnmpCollection(snmpcoll);
				DbWriter dbwriter = new DbWriter(node);
				dbwriter.run();
				snmpprimaryip2nodes
					.put(snmpcoll.getTarget().getHostAddress(), node);
			} else {
				if (log.isEnabledFor(Priority.WARN))
					log.warn("udateNodeSnmpCollection: cannot find Linkable SNMP Node element in hash snmpprimaryip2nodes");
			}
	    }
	}
}