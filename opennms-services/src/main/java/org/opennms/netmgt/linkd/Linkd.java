package org.opennms.netmgt.linkd;

import java.beans.PropertyVetoException;
import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;

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

	private static boolean m_auto_discovery = true;

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
		SnmpCollection[] snmpcolls = null;
		try {
			dbConn = DataSourceFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log
						.debug("init: Loading Snmp nodes");
			}
			snmpprimaryip2nodes = LinkdConfigFactory.getInstance()
						.getLinkableNodes(dbConn);
			snmpcolls = (SnmpCollection[]) LinkdConfigFactory.getInstance().getSnmpColls(dbConn).values().toArray(new SnmpCollection[0]);
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

		if (snmpcolls.length != 0) {
			for (int i = 0; i < snmpcolls.length; i++) {
				snmpCollector = snmpcolls[i];
				snmpCollector.setAutoDiscovery(m_auto_discovery);
				log.debug("init: scheduling Snmp Collection for ip "
						+ snmpCollector.getSnmpIpPrimary().getHostAddress());
				synchronized (snmpCollector) {
					if (snmpCollector.getScheduler() == null) {
						snmpCollector.setScheduler(m_scheduler);
					}
					snmpCollector.setPollInterval(m_snmp_poll_interval);
					snmpCollector.setInitialSleepTime(m_initial_sleep_time);
					m_initial_sleep_time = m_initial_sleep_time	+ 5000;
				}
				snmpCollector.schedule();
			}
			// Schedule the discovery link on nodes
			//
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

	void scheduleNode(int nid) throws UnknownHostException, Throwable {

		Category log = ThreadCategory.getInstance();
		java.sql.Connection dbConn = null;
		
		SnmpCollection coll = null;
		
		try {
			dbConn = DataSourceFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("scheduleNode: Loading node " + nid
						+ " from database");
			}
			try {
				coll = LinkdConfigFactory.getInstance().getSnmpCollection(
						dbConn, nid);
			} catch (UnknownHostException h) {
					log
							.warn("scheduleNode: Failed to get Linkable node from LinkdConfigFactory"
									+ h);
			}
		} catch (SQLException sqlE) {
			log
					.fatal(
							"scheduleNode: SQL Exception while syncing node object with database information.",
							sqlE);
			throw new UndeclaredThrowableException(sqlE);
		} catch (Throwable t) {
			log
					.fatal(
							"scheduleNode: Unknown error while syncing node object with database information.",
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
								"scheduleNode: SQL Exception while syncing node object with database information.",
								e);
			}
		}

		InetAddress ipAddr = coll.getSnmpIpPrimary();
		String ip = ipAddr.getHostAddress();
		LinkableNode node = new LinkableNode(nid,ip);

		// work on snmpprimary
		synchronized (snmpprimaryip2nodes) {
			if (snmpprimaryip2nodes.containsKey(ip)) {
				if (log.isDebugEnabled()) log.debug("Node collection exists: skipping schedule, performing DB cleaning");
				LinkableNode oldNode = (LinkableNode) snmpprimaryip2nodes
						.get(ip);

				// first of all set status to D on linkd tables for old node if
				// different from new
				if (node.getNodeId() != oldNode.getNodeId()) {
					DbEventWriter dbwriter = new DbEventWriter(oldNode.getNodeId(),
							DbEventWriter.ACTION_DELETE);
					dbwriter.run();
				}
			} else {
				if (log.isDebugEnabled()) log.debug("Node collection does not exists: scheduling");
				synchronized (coll) {
					if (coll.getScheduler() == null) {
						coll.setScheduler(m_scheduler);
					}
					coll.setPollInterval(m_snmp_poll_interval);
					coll.setInitialSleepTime(0);
					coll.setAutoDiscovery(m_auto_discovery);
					coll.schedule();
				}
			}	
			snmpprimaryip2nodes.put(ip, node);
		}

		if (!scheduledDiscoveryLink) {
			DiscoveryLink discoveryLink = new DiscoveryLink();

			if (log.isDebugEnabled())
				log
						.debug("scheduleNode: scheduling Discovery Link");
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

	void wakeUpNode(int nid) throws UnknownHostException, Throwable {

		Category log = ThreadCategory.getInstance();
		java.sql.Connection dbConn = null;
		InetAddress ipAddr = null;
		
		try {
			dbConn = DataSourceFactory.getInstance().getConnection();
			if (log.isDebugEnabled()) {
				log.debug("wakeUpNode: Loading node " + nid
						+ " from database");
			}
			try {
				ipAddr = LinkdConfigFactory.getInstance().getSnmpPrimaryIp(
						dbConn, nid);
			} catch (UnknownHostException h) {
					log
							.warn("wakeUpNode: Failed to get Linkable node from LinkdConfigFactory"
									+ h);
			}
		} catch (SQLException sqlE) {
			log
					.fatal(
							"wakeUpNode: SQL Exception while syncing node object with database information.",
							sqlE);
			throw new UndeclaredThrowableException(sqlE);
		} catch (Throwable t) {
			log
					.fatal(
							"wakeUpNode: Unknown error while syncing node object with database information.",
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
								"wakeUpNode: SQL Exception while syncing node object with database information.",
								e);
			}
		}

		String ip = ipAddr.getHostAddress();
		LinkableNode node = new LinkableNode(nid,ip);

		// work on snmpprimary
		synchronized (snmpprimaryip2nodes) {
			if (snmpprimaryip2nodes.containsKey(ip)) {
				LinkableNode oldNode = (LinkableNode) snmpprimaryip2nodes
						.get(ip);

				// first of all set status to D on linkd tables for old node if
				// different from new
				if (node.getNodeId() != oldNode.getNodeId()) {
					DbEventWriter dbwriter = new DbEventWriter(oldNode.getNodeId(),
							DbEventWriter.ACTION_DELETE);
					dbwriter.run();
				}
			}
			snmpprimaryip2nodes.put(ip, node);
		}

		// work on snmpcollection
		ReadyRunnable rr = m_scheduler.getReadyRunnable(ipAddr);

		if (rr == null) {
			scheduleNode(nid);
		} else {
			rr.wakeUp();
		}
	}

	void unscheduleNode(int nid) throws UnknownHostException, Throwable {

		Category log = ThreadCategory.getInstance();
		boolean passed = false;
		if (log.isDebugEnabled())
			log.debug("unscheduleNode: deleting snmp collection for node "
					+ nid);

		InetAddress ipAddr = null;
		synchronized (snmpprimaryip2nodes) {
			Iterator it = snmpprimaryip2nodes.values().iterator();
			while (it.hasNext()) {
				LinkableNode node = (LinkableNode) it.next();
				if (node.getNodeId() == nid) {
					snmpprimaryip2nodes.remove(node.getSnmpPrimaryIpAddr());
					if (log.isDebugEnabled())
						log
								.debug("unscheduleNode: removed linkable node from snmp collection for node "
										+ nid);
					ipAddr = InetAddress.getByName(node.getSnmpPrimaryIpAddr());
					passed = true;
					break;
				}
			}
		}

		if (!passed) {
			if (log.isDebugEnabled())
				log
						.debug("unscheduleNode: linkable node not found on snmp collection for node "
								+ nid);
			java.sql.Connection dbConn = null;

			try {
				dbConn = DataSourceFactory.getInstance()
						.getConnection();
				if (log.isDebugEnabled()) {
					log.debug("unscheduleNode: Loading node " + nid
							+ " from database");
				}
				try {
					ipAddr = LinkdConfigFactory.getInstance().getSnmpPrimaryIp(
							dbConn, nid);
				} catch (UnknownHostException h) {
						log
								.warn("unscheduleNode: Failed to get Linkable node from LinkdConfigFactory"
										+ h);
				}
			} catch (SQLException sqlE) {
				log
						.fatal(
								"unscheduleNode: SQL Exception while syncing node object with database information.",
								sqlE);
				throw new UndeclaredThrowableException(sqlE);
			} catch (Throwable t) {
				log
						.fatal(
								"unscheduleNode: Unknown error while syncing node object with database information.",
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
									"unscheduleNode: SQL Exception while syncing node object with database information.",
									e);
				}
			}
		}

		// test if ipaddress is null
		// get readyrunnuble and operate on it
		if (ipAddr != null) {
			ReadyRunnable rr = m_scheduler.getReadyRunnable(ipAddr);
			if (rr != null) {
				rr.unschedule();
			} else {
				if (log.isInfoEnabled())
					log
							.info("unscheduleNode: Failed to get Ready Runnable for ipaddress " + ipAddr.getHostAddress() 
									+ " with nodeid "
									+ nid);
				
			}
		} else {
				log
						.warn("unscheduleNode: Failed to get ipaddress for node "
								+ nid);
		}

		DbEventWriter dbwriter = new DbEventWriter(nid, DbEventWriter.ACTION_DELETE);
		dbwriter.run();
	}

	void suspendNode(int nid) throws UnknownHostException, Throwable {
		
		Category log = ThreadCategory.getInstance();
		boolean passed = false;

		if (log.isDebugEnabled())
			log.debug("suspendNode: suspend snmp collection for node " + nid);
		
		InetAddress ipAddr = null;
		synchronized (snmpprimaryip2nodes) {
			Iterator it = snmpprimaryip2nodes.values().iterator();
			while (it.hasNext()) {
				LinkableNode node = (LinkableNode) it.next();
				if (node.getNodeId() == nid) {
					snmpprimaryip2nodes.remove(node.getSnmpPrimaryIpAddr());
					if (log.isDebugEnabled())
						log.debug("suspendNode: removed linkable node from snmp collection for node " + nid);
					ipAddr = InetAddress.getByName(node.getSnmpPrimaryIpAddr());
					passed = true;
					break;
				}
			}
		}
		
		if (!passed) {
			if (log.isDebugEnabled())
				log.debug("suspendNode: linkable node not found on snmp collection for node " + nid);
			java.sql.Connection dbConn = null;
			
			try {
				dbConn = DataSourceFactory.getInstance().getConnection();
				if (log.isDebugEnabled()) {
					log.debug("suspendNode: Loading node " + nid
							+ " from database");
				}
				try {
					ipAddr = LinkdConfigFactory.getInstance().getSnmpPrimaryIp(
							dbConn, nid);
				} catch (UnknownHostException h) {
						log
								.warn("suspendNode: Failed to get Linkable node from LinkdConfigFactory"
										+ h);
				}
			} catch (SQLException sqlE) {
				log
						.fatal(
								"suspendNode: SQL Exception while syncing node object with database information.",
								sqlE);
				throw new UndeclaredThrowableException(sqlE);
			} catch (Throwable t) {
				log
						.fatal(
								"suspendNode: Unknown error while syncing node object with database information.",
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
									"suspendNode: SQL Exception while syncing node object with database information.",
									e);
				}
			}
		}

		// test if ipaddress is null
		// get readyrunnuble and operate on it
		if (ipAddr != null) {
			ReadyRunnable rr = m_scheduler.getReadyRunnable(ipAddr);
			if (rr != null) {
				rr.suspend();
			} else {
				if (log.isInfoEnabled())
					log
							.info("suspendNode: Failed to get Ready Runnable for ipaddress " + ipAddr.getHostAddress() 
									+ " with nodeid "
									+ nid);
			}
		} else {
				log
						.warn("suspendNode: Failed to get ipaddress for node "
								+ nid);
		}
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
		// construct event with 'trapd' as source
		Event event = new Event();
		event.setSource("linkd");
		event.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
		event.setHost(ipowner);
		event.setInterface(ipInterface);
		event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

		// send the event to eventd
		m_eventMgr.sendNow(event);
	}

	EventIpcManager getIpcManager() {
		return m_eventMgr;
	}

}