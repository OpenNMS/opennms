//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp. All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
//
//
package org.opennms.netmgt.inventory;

import java.lang.reflect.UndeclaredThrowableException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Priority;
import org.apache.log4j.Category;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.CapsdConfigFactory;

// castor classes generated from the inventory-configuration.xsd
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.inventory.*;
public final class Inventory implements PausableFiber {

	private final static String LOG4J_CATEGORY = "OpenNMS.Inventories";

	private final static String SQL_RETRIEVE_INTERFACES =
		"SELECT node.nodeid,ipaddr FROM ifServices, service, node WHERE ifServices.serviceid = service.serviceid AND service.servicename = ? AND ifServices.status<>'D' AND node.nodetype='A' AND node.nodeid = ifServices.nodeid";

	private final static String SQL_RETRIEVE_SERVICE_IDS =
		"SELECT serviceid,servicename  FROM service";

	private final static String SQL_RETRIEVE_SERVICE_STATUS =
		"SELECT ifregainedservice,iflostservice FROM outages WHERE nodeid = ? AND ipaddr = ? AND serviceid = ? AND iflostservice = (SELECT max(iflostservice) FROM outages WHERE nodeid = ? AND ipaddr = ? AND serviceid = ?)";

	/**
	 * Integer constant for passing in to PollableNode.getNodeLock() method
	 * in order to indicate that the method should block until node lock is 
	 * available.
	 */
	private static int WAIT_FOREVER = 0;

	private final static Inventory m_singleton = new Inventory();

	/** 
	 * Holds map of service names to service identifiers
	 */
	private final static Map m_serviceIds = new HashMap();

	/**
	 * List of all PollableGroup objects.
	 */
	private List m_pollableGroups;

	private Scheduler m_scheduler;

	private int m_status;

	private static boolean sendEachSuccessEvents = false;

	private BroadcastEventProcessor m_receiver;

	/**
	 * Map of 'PollableNode' objects keyed by nodeId
	*/
	private Map m_pollableNodes;

	/**  
	 * Map of all available 'groupMonitor' objects indexed by 
	 * group name
		*/
	private static Map m_groupMonitors;

	/**  
	 * Map of all available 'RequiredService' objects indexed by 
	 * group name
		*/
	private static Map m_svcreqMonitors;

	private Inventory() {
		m_scheduler = null;
		m_status = START_PENDING;
		m_groupMonitors = Collections.synchronizedMap(new TreeMap());
		m_svcreqMonitors = Collections.synchronizedMap(new TreeMap());
		m_pollableGroups = Collections.synchronizedList(new LinkedList());
		m_pollableNodes = Collections.synchronizedMap(new HashMap());
	}

	public synchronized void init() {
		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		// get the category logger 
		Category log = ThreadCategory.getInstance();

		log.debug("Inventory init");
		

		// Load up the configuration for the 
		// inventory. Use this to start everything
		//
		try {
			InventoryConfigFactory.reload();
		}
		catch (MarshalException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load inventory configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch (ValidationException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load inventory configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch (IOException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load inventory configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		// Initialize the Capsd configuration factory 
		// Necessary for testing if an interface is managed/unmanaged
		//

		try {
			CapsdConfigFactory.reload();
		}
		catch (MarshalException ex) {
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch (ValidationException ex) {
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch (IOException ex) {
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		// Initialize the Capsd configuration factory 
		// Necessary for testing if an interface is managed/unmanaged
		//
		try {
			CapsdConfigFactory.reload();
		}
		catch (MarshalException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch (ValidationException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch (IOException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		if (log.isDebugEnabled())
			log.debug("start: Testing database connection");

		// Make sure we can connect to the database and load
		// the services table so we can easily convert from
		// service name to service id
		//

		java.sql.Connection ctest = null;
		ResultSet rs = null;
		try {
			DatabaseConnectionFactory.init();
			ctest = DatabaseConnectionFactory.getInstance().getConnection();

			PreparedStatement loadStmt = ctest.prepareStatement(SQL_RETRIEVE_SERVICE_IDS);

			// go ahead and load the service table 
			//
			rs = loadStmt.executeQuery();
			while (rs.next()) {
				Integer id = new Integer(rs.getInt(1));
				String name = rs.getString(2);

				m_serviceIds.put(name, id);
			}
		}
		catch (IOException iE) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: IOException getting database connection", iE);
			throw new UndeclaredThrowableException(iE);
		}
		catch (MarshalException mE) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Marshall Exception getting database connection", mE);
			throw new UndeclaredThrowableException(mE);
		}
		catch (ValidationException vE) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Validation Exception getting database connection", vE);
			throw new UndeclaredThrowableException(vE);
		}
		catch (SQLException sqlE) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Error accessing database.", sqlE);
			throw new UndeclaredThrowableException(sqlE);
		}
		catch (ClassNotFoundException cnfE) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Error accessing database.", cnfE);
			throw new UndeclaredThrowableException(cnfE);
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (Exception e) {
					if (log.isInfoEnabled())
						log.info("start: an error occured closing the result set", e);
				}
			}
			if (ctest != null) {
				try {
					ctest.close();
				}
				catch (Exception e) {
					if (log.isInfoEnabled())
						log.info("start: an error occured closing the SQL connection", e);
				}
			}
		}


		
		// Load up an instance of each monitor from the config
		// so that the event processor will have them for
		// new incomming events to create pollable group objects.
		//
		if (log.isDebugEnabled())
			log.debug("start: Loading monitors");
		InventoryConfiguration iConfig = InventoryConfigFactory.getInstance().getConfiguration();

		sendEachSuccessEvents = iConfig.getSendEachSuccessEvent();

		Enumeration eiter = iConfig.enumerateMonitor();
		while (eiter.hasMoreElements()) {
			Monitor monitor = (Monitor)eiter.nextElement();
			try {
				if (log.isDebugEnabled()) {
					log.debug(
						"start: Loading monitor "
							+ monitor.getGroup()
							+ ", classname "
							+ monitor.getClassName());
				}
				Class mc = Class.forName(monitor.getClassName());
				InventoryMonitor im = (InventoryMonitor)mc.newInstance();
				im.setInventoryCategory(monitor.getInventoryType());
				// Attempt to initialize the group monitor
				//
				/*Map properties = null; // properties not currently used
				sm.initialize(properties);
				*/
				m_groupMonitors.put(monitor.getGroup(), im);
				m_svcreqMonitors.put(monitor.getGroup(), monitor.getRequiredService());
			}
			catch (Throwable t) {
				if (log.isEnabledFor(Priority.WARN)) {
					log.warn(
						"start: Failed to load monitor "
							+ monitor.getClassName()
							+ " for group "
							+ monitor.getGroup(),
						t);
				}
			}
		}

		// Create a scheduler
		//
		try {
			if (log.isDebugEnabled())
				log.debug("start: Creating inventory scheduler");

			m_scheduler = new Scheduler("Inventory", iConfig.getThreads());
		}
		catch (RuntimeException e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to create inventory scheduler", e);
			throw e;
		}

		if (log.isDebugEnabled())
			log.debug("start: Scheduling existing interfaces");

		// Schedule the interfaces currently in the database
		//
		try {
			scheduleExistingInterfaces();
		}
		catch (SQLException sqlE) {
			if (log.isEnabledFor(Priority.ERROR))
				log.error("start: Failed to schedule existing interfaces", sqlE);
		}

		// Create an event receiver. The receiver will
		// receive events, process them, creates network
		// interfaces, and schedulers them.
		//
		try {
			if (log.isDebugEnabled())
				log.debug("start: Creating event broadcast event processor");

			m_receiver = new BroadcastEventProcessor(m_pollableGroups);
		}
		catch (Throwable t) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to initialized the broadcast event receiver", t);

			throw new UndeclaredThrowableException(t);
		}
	}

	public synchronized void start() {
		m_status = STARTING;

		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		// get the category logger 
		Category log = ThreadCategory.getInstance();

		if (log.isDebugEnabled())
			log.debug("start: Initializing InventoryConfigFactory");

		// start the scheduler
		//
		try {
			if (log.isDebugEnabled())
				log.debug("start: Starting inventory scheduler");
			m_scheduler.start();
		}
		catch (RuntimeException e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to start scheduler", e);
			throw e;
		}

		// Set the status of the group as running.
		//
		m_status = RUNNING;

		if (log.isDebugEnabled())
			log.debug("start: Inventory running");

		refreshInventory ri = new refreshInventory();
		ri.start();
	}

	public synchronized void stop() {
		m_status = STOP_PENDING;
		m_scheduler.stop();
		m_receiver.close();

		m_scheduler = null;
		m_status = STOPPED;
		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("stop: Inventory stopped");
	}

	public synchronized void pause() {
		if (m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;
		m_scheduler.pause();
		m_status = PAUSED;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("pause: Inventory paused");
	}

	public synchronized void resume() {
		if (m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;
		m_scheduler.resume();
		m_status = RUNNING;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("resume: Inventory resumed");
	}

	public synchronized int getStatus() {
		return m_status;
	}

	public String getName() {
		return "OpenNMS.Inventory";
	}

	public static Inventory getInstance() {
		return m_singleton;
	}

	public Scheduler getScheduler() {
		return m_scheduler;
	}

	public InventoryMonitor getGroupMonitor(String grpName) {
		return (InventoryMonitor)m_groupMonitors.get(grpName);
	}

	public String getRequiredService(String grpName) {
		return (String)m_svcreqMonitors.get(grpName);
	}
	public List getPollableGroupList() {
		return m_pollableGroups;
	}

	public PollableNode getNode(int nodeId) {
		Integer key = new Integer(nodeId);
		boolean nodeInMap = m_pollableNodes.containsKey(key);

		if (nodeInMap) {
			return (PollableNode)m_pollableNodes.get(key);
		}
		else {
			return null;
		}
	}

	public void addNode(PollableNode pNode) {
		m_pollableNodes.put(new Integer(pNode.getNodeId()), pNode);
		Category log = ThreadCategory.getInstance();
		log.debug(
			"Inventory.addNode: adding pollable node with id: "
				+ pNode.getNodeId()
				+ " new size: "
				+ m_pollableNodes.size());
	}

	public void removeNode(int nodeId) {
		m_pollableNodes.remove(new Integer(nodeId));
	}

	private void scheduleExistingInterfaces() throws SQLException {
		// get the category logger 
		//
		Category log = ThreadCategory.getInstance();

		InventoryConfigFactory inventoryCfgFactory = InventoryConfigFactory.getInstance();

		// Database connection
		java.sql.Connection dbConn = null;

		PreparedStatement stmt = null;
		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			stmt = dbConn.prepareStatement(SQL_RETRIEVE_INTERFACES);

			// Loop through loaded monitors and schedule for each one present
			//
			Set grpNames = m_groupMonitors.keySet();
			Iterator i = grpNames.iterator();
			while (i.hasNext()) {
				String grpName = (String)i.next();
				InventoryMonitor monitor = (InventoryMonitor)m_groupMonitors.get(grpName);

				if (log.isDebugEnabled())
					log.debug(
						"scheduleExistingInterfaces: Scheduling existing interfaces for monitor: "
							+ grpName);

				// Retrieve list of interfaces from the database which 
				// support the group polled by this monitor
				//
				try {
					if (log.isDebugEnabled())
						log.debug(
							"scheduleExistingInterfaces: dbConn = "
								+ dbConn
								+ ", grpName = "
								+ grpName);

					stmt.setString(1, (String)m_svcreqMonitors.get(grpName));
					// group name
					ResultSet rs = stmt.executeQuery();

					// Iterate over result set and schedule each interface/group
					// pair which passes the criteria
					//
					while (rs.next()) {
						int nodeId = rs.getInt(1);
						String ipAddress = rs.getString(2);

						InventoryConfiguration pConfig = inventoryCfgFactory.getConfiguration();
						Enumeration epkgs = pConfig.enumeratePackage();

						// Compare interface/group pair against each inventory package
						// For each match, create new PollableGroup object and 
						// schedule it for inventory.
						//
						while (epkgs.hasMoreElements()) {
							org.opennms.netmgt.config.inventory.Package pkg =
								(org.opennms.netmgt.config.inventory.Package)epkgs.nextElement();

							// Make certain the the current group is in the package
							// and enabled!
							//
							if (!inventoryCfgFactory.groupInPackageAndEnabled(grpName, pkg)) {
								if (log.isDebugEnabled())
									log.debug(
										"scheduleExistingInterfaces: address/group: "
											+ ipAddress
											+ "/"
											+ grpName
											+ " not scheduled, group is not enabled or does not exist in package: "
											+ pkg.getName());
								continue;
							}

							// Is the interface in the package?
							//
							if (!inventoryCfgFactory.interfaceInPackage(ipAddress, pkg)) {
								if (log.isDebugEnabled())
									log.debug(
										"scheduleExistingInterfaces: address/group: "
											+ ipAddress
											+ "/"
											+ grpName
											+ " not scheduled, interface does not belong to package: "
											+ pkg.getName());
								continue;
							}

							//
							// getServiceLostDate() method will return the date a service 
							// was lost if the service was last known to be unavailable or
							// will return null if the service was last known to be 
							// available...based on outage information on the 'outages' table.
							Date grpLostDate =
								getServiceLostDate(dbConn, nodeId, ipAddress, grpName);
							int lastKnownStatus = -1;
							if (grpLostDate != null) {
								lastKnownStatus = InventoryMonitor.RETRIEVE_FAILURE;
								if (log.isDebugEnabled())
									log.debug(
										"scheduleExistingInterfaces: address= "
											+ ipAddress
											+ " svc= "
											+ grpName
											+ " lastKnownStatus= download failure"
											+ " grpLostDate= "
											+ grpLostDate);
							}
							else {
								lastKnownStatus = InventoryMonitor.RETRIEVE_SUCCESS;
								if (log.isDebugEnabled())
									log.debug(
										"scheduleExistingInterfaces: address= "
											+ ipAddress
											+ " svc= "
											+ grpName
											+ " lastKnownStatus= download success");
							}

							// Criteria checks have all been padded...update Node Outage 
							// Hierarchy and create new service for inventory
							//
							PollableNode pNode = null;
							PollableInterface pInterface = null;
							PollableGroup pGrp = null;
							boolean ownLock = false;
							boolean nodeCreated = false;
							boolean interfaceCreated = false;

							try {
								// Does the node already exist in the inventory's pollable node map?
								//
								pNode = this.getNode(nodeId);
								if (pNode == null) {
									// Nope...so we need to create it
									pNode = new PollableNode(nodeId);
									nodeCreated = true;
								}
								else {
									// Obtain node lock
									//

									if (log.isDebugEnabled())
										log.debug(
											"scheduleExistingInterfaces: ------------- requesting node lock for nodeid: "
												+ nodeId
												+ " -----------");

									if (!(ownLock = pNode.getNodeLock(WAIT_FOREVER))) {
										throw new LockUnavailableException(
											"scheduleExistingInterfaces: failed to obtain lock on nodeId "
												+ nodeId);

									}
								}

								// Does the interface exist in the pollable node?
								//
								pInterface = pNode.getInterface(ipAddress);
								if (pInterface == null) {
									// Create the PollableInterface
									pInterface =
										new PollableInterface(
											pNode,
											InetAddress.getByName(ipAddress));
									interfaceCreated = true;
								}
								// Create a new PollableGroup representing this node, interface,
								// group and package pairing
								//
								pGrp =
									new PollableGroup(
										pInterface,
										grpName,
										pkg,
										lastKnownStatus,
										grpLostDate);

								// Initialize the group monitor with the pollable group 
								//
								//monitor.initialize(pGrp);

								// Add new group to the pollable groups list.
								//
								m_pollableGroups.add(pGrp);

								// Add the group to the PollableInterface object
								//
								// WARNING:  The PollableInterface stores groups in a map
								//           keyed by group name, therefore, only the LAST
								//           PollableGroup aded to the interface for a 
								//           particular group will be represented in the
								//           map.  THIS IS BY DESIGN.
								//
								// NOTE:     addService() calls recalculateStatus() on the interface
								log.debug(
									"scheduleExistingInterfaces: adding pollable group to group list of interface: "
										+ ipAddress);
								pInterface.addGroup(pGrp);

								if (interfaceCreated) {
									// Add the interface to the node
									//
									// NOTE:  addInterface() calls recalculateStatus() on the node
									if (log.isDebugEnabled())
										log.debug(
											"scheduleExistingInterfaces: adding new pollable interface "
												+ ipAddress
												+ " to pollable node "
												+ nodeId);
									pNode.addInterface(pInterface);
								}
								else {
									// Recalculate node status
									//
									pNode.recalculateStatus();
								}

								if (nodeCreated) {
									// Add the node to the node map
									//
									if (log.isDebugEnabled())
										log.debug(
											"scheduleExistingInterfaces: adding new pollable node: "
												+ nodeId);
									this.addNode(pNode);
								}

								// Schedule the group
								//
								m_scheduler.schedule(pGrp, pGrp.recalculateInterval());
							}
							catch (UnknownHostException ex) {
								log.error(
									"scheduleExistingInterfaces: Failed to schedule interface "
										+ ipAddress
										+ " for group monitor "
										+ grpName
										+ ", illegal address",
									ex);
							}
							catch (InterruptedException ie) {
								log.error(
									"scheduleExistingInterfaces: Failed to schedule interface "
										+ ipAddress
										+ " for group monitor "
										+ grpName
										+ ", thread interrupted",
									ie);
							}
							catch (RuntimeException rE) {
								log.warn(
									"scheduleExistingInterfaces: Unable to schedule "
										+ ipAddress
										+ " for group monitor "
										+ grpName
										+ ", reason: "
										+ rE.getMessage());
							}
							catch (Throwable t) {
								log.error(
									"scheduleExistingInterfaces: Uncaught exception, failed to schedule interface "
										+ ipAddress
										+ " for group monitor "
										+ grpName,
									t);
							}
							finally {
								if (ownLock) {
									log.debug(
										"scheduleExistingInterfaces: ----------- releasing node lock for nodeid: "
											+ nodeId
											+ " ----------");
									try {
										pNode.releaseNodeLock();
										ownLock = false;
									}
									catch (InterruptedException iE) {
										log.error(
											"scheduleExistingInterfaces: Failed to release node lock on nodeid "
												+ pNode.getNodeId()
												+ ", thread interrupted.");
									}
								}

							}
						} // end while more packages exist
					} // end while more interfaces in result set

					rs.close();
				}
				catch (SQLException sqle) {
					log.warn(
						"scheduleExistingInterfaces: SQL exception while querying ipInterface table",
						sqle);
					throw sqle;
				}
			} // end while more group monitors exist

			// Debug dump pollable node map content
			//
			if (log.isDebugEnabled()) {
				log.debug("scheduleExistingInterfaces: dumping content of pollable node map: ");
				Iterator j = m_pollableNodes.values().iterator();
				while (j.hasNext()) {
					PollableNode pNode = (PollableNode)j.next();
					log.debug(
						"	nodeid="
							+ pNode.getNodeId()
							+ " status="
							+ Pollable.statusType[pNode.getStatus()]);
					Iterator k = pNode.getInterfaces().iterator();
					while (k.hasNext()) {
						PollableInterface pIf = (PollableInterface)k.next();
						log.debug(
							"		interface="
								+ pIf.getAddress().getHostAddress()
								+ " status="
								+ Pollable.statusType[pIf.getStatus()]);

						Iterator s = pIf.getGroups().iterator();
						while (s.hasNext()) {
							PollableGroup pGrp = (PollableGroup)s.next();
							log.debug(
								"			group="
									+ pGrp.getGroupName()
									+ " status="
									+ Pollable.statusType[pGrp.getStatus()]);
						}
					}
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (Exception e) {
					if (log.isDebugEnabled())
						log.debug(
							"scheduleExistingInterfaces: an exception occured closing the SQL statement",
							e);
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				}
				catch (Throwable t) {
					if (log.isDebugEnabled())
						log.debug(
							"scheduleExistingInterfaces: an exception occured closing the SQL connection",
							t);
				}
			}
		}
	}

	/**
	 * Determines the last known status of a ipaddr/(requiredService of group) pair based on outage information
	 * from the 'outages' table and if the last known status is DOWNLOAD FAILURE returns a 
	 * date object representing when the requiredService was lost.  If last known status is 
	 * DOWNLOAD SUCCESS null is returned.
	 *
	 * @param dbConn	Database connection
	 * @param nodeId	Node identifier
	 * @param ipAddr	IP address
	 * @param grpName	group name
	 * 
	 * @return Date object representing the time the requiredService was lost if the requiredService is 
	 * DOWNLOAD FAILURE or null if the requiredService is DOWNLOAD SUCCESS.
	 */
	public static final Date getServiceLostDate(
		Connection dbConn,
		int nodeId,
		String ipAddr,
		String grpName) {
		Category log = ThreadCategory.getInstance(Inventory.class);
		log.debug("getting last known status for address: " + ipAddr + " group: " + grpName);

		Date svcLostDate = null;
		// Convert service name to service identifier
		//
		Integer temp = (Integer)m_serviceIds.get(m_svcreqMonitors.get(grpName));
		int serviceId = -1;
		if (temp != null)
			serviceId = temp.intValue();
		else {
			log.warn(
				"Failed to retrieve service identifier for interface "
					+ ipAddr
					+ " and requiredService '"
					+ m_svcreqMonitors.get(grpName)
					+ "'");
			return svcLostDate;
		}

		ResultSet outagesResult = null;
		Timestamp regainedDate = null;
		Timestamp lostDate = null;

		try {
			//get the outage information for this service on this ip address
			PreparedStatement outagesQuery = dbConn.prepareStatement(SQL_RETRIEVE_SERVICE_STATUS);

			//add the values for the main query
			outagesQuery.setInt(1, nodeId);
			outagesQuery.setString(2, ipAddr);
			outagesQuery.setInt(3, serviceId);

			//add the values for the subquery
			outagesQuery.setInt(4, nodeId);
			outagesQuery.setString(5, ipAddr);
			outagesQuery.setInt(6, serviceId);

			outagesResult = outagesQuery.executeQuery();

			//if there was a result then the service has been down before,
			if (outagesResult.next()) {
				regainedDate = outagesResult.getTimestamp(1);
				lostDate = outagesResult.getTimestamp(2);
				log.debug("getServiceLastKnownStatus: lostDate: " + lostDate);
			}
			//the service has never been down, need to use current date for both
			else {
				Date currentDate = new Date(System.currentTimeMillis());
				regainedDate = new Timestamp(currentDate.getTime());
				lostDate = lostDate = new Timestamp(currentDate.getTime());
			}
		}
		catch (SQLException sqlE) {
			log.error(
				"SQL exception while retrieving last known service status for "
					+ ipAddr
					+ "/"
					+ grpName);
		}
		finally {
			if (outagesResult != null) {
				try {
					outagesResult.close();
				}
				catch (SQLException slqE) {
					// Do nothing
				}
			}
		}

		// Now use retrieved outage times to determine current status
		// of the service.  If there was an error and we were unable
		// to retrieve the outage times the default of DOWNLOAD SUCCESS will
		// be returned.
		//
		if (lostDate != null) {
			// If the service was never regained then simply 
			// assign the svc lost date.
			if (regainedDate == null) {
				svcLostDate = new Date(lostDate.getTime());
				log.debug("getServiceLastKnownStatus: svcLostDate: " + svcLostDate);
			}
		}

		return svcLostDate;
	}

	protected void refresh() throws SQLException, InterruptedException {
		// get the category logger 
		//
		Category log = ThreadCategory.getInstance();

		InventoryConfigFactory inventoryCfgFactory = InventoryConfigFactory.getInstance();

		// Database connection
		java.sql.Connection dbConn = null;

		PreparedStatement stmt = null;
		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			stmt = dbConn.prepareStatement(SQL_RETRIEVE_INTERFACES);

			// Loop through loaded monitors and schedule for each one present
			//
			Set grpNames = m_groupMonitors.keySet();
			Iterator i = grpNames.iterator();
			while (i.hasNext()) {
				String grpName = (String)i.next();
				InventoryMonitor monitor = (InventoryMonitor)m_groupMonitors.get(grpName);

				if (log.isDebugEnabled())
					log.debug("refresh: Scheduling existing interfaces for monitor: " + grpName);

				// Retrieve list of interfaces from the database which 
				// support the group polled by this monitor
				//
				try {
					if (log.isDebugEnabled())
						log.debug("refresh: dbConn = " + dbConn + ", grpName = " + grpName);

					stmt.setString(1, (String)m_svcreqMonitors.get(grpName));
					// Required service name
					ResultSet rs = stmt.executeQuery();

					// Iterate over result set and schedule each interface/reqService
					// pair which passes the criteria
					//
					while (rs.next()) {
						int nodeId = rs.getInt(1);
						String ipAddress = rs.getString(2);

						InventoryConfiguration pConfig = inventoryCfgFactory.getConfiguration();
						Enumeration epkgs = pConfig.enumeratePackage();

						// Compare interface/group pair against each inventory package
						// For each match, create new PollableGroup object and 
						// schedule it for inventory.
						//
						while (epkgs.hasMoreElements()) {
							org.opennms.netmgt.config.inventory.Package pkg =
								(org.opennms.netmgt.config.inventory.Package)epkgs.nextElement();

							// Make certain the the current group is in the package
							// and enabled!
							//
							if (!inventoryCfgFactory.groupInPackageAndEnabled(grpName, pkg)) {
								if (log.isDebugEnabled())
									log.debug(
										"refresh: address/group: "
											+ ipAddress
											+ "/"
											+ grpName
											+ " not scheduled, group is not enabled or does not exist in package: "
											+ pkg.getName());
								continue;
							}

							// Is the interface in the package?
							//
							if (!inventoryCfgFactory.interfaceInPackage(ipAddress, pkg)) {
								if (log.isDebugEnabled())
									log.debug(
										"refresh: address/group: "
											+ ipAddress
											+ "/"
											+ grpName
											+ " not scheduled, interface does not belong to package: "
											+ pkg.getName());
								continue;
							}

							//
							// getServiceLostDate() method will return the date a required service 
							// was lost if the service was last known to be unavailable or
							// will return null if the service was last known to be 
							// available...based on outage information on the 'outages' table.
							Date grpLostDate =
								getServiceLostDate(dbConn, nodeId, ipAddress, grpName);
							int lastKnownStatus = -1;
							if (grpLostDate != null) {
								lastKnownStatus = InventoryMonitor.RETRIEVE_FAILURE;
								if (log.isDebugEnabled())
									log.debug(
										"refresh: address= "
											+ ipAddress
											+ " grp= "
											+ grpName
											+ " lastKnownStatus= download failure"
											+ " grpLostDate= "
											+ grpLostDate);
							}
							else {
								lastKnownStatus = InventoryMonitor.RETRIEVE_SUCCESS;
								if (log.isDebugEnabled())
									log.debug(
										"refresh: address= "
											+ ipAddress
											+ " grp= "
											+ grpName
											+ " lastKnownStatus= download success");
							}

							// Criteria checks have all been padded...update Node Outage 
							// Hierarchy and create new group for inventory
							//
							PollableNode pNode = null;
							PollableInterface pInterface = null;
							PollableGroup pGrp = null;
							boolean ownLock = false;
							boolean nodeCreated = false;
							boolean interfaceCreated = false;

							try {
								// Does the node already exist in the inventory's pollable node map?
								//
								pNode = this.getNode(nodeId);
								if (pNode == null) {
									// Nope...so we need to create it
									pNode = new PollableNode(nodeId);
									nodeCreated = true;
								}
								else {
									// Obtain node lock
									//

									if (log.isDebugEnabled())
										log.debug(
											"refresh: ------------- requesting node lock for nodeid: "
												+ nodeId
												+ " -----------");
									if (!(ownLock = pNode.getNodeLock(60000))) {
										log.warn(
											"refresh: failed to obtain lock on nodeId " + nodeId);
									}
									//}
								}

								// Does the interface exist in the pollable node?
								//
								pInterface = pNode.getInterface(ipAddress);
								if (pInterface == null) {
									// Create the PollableInterface
									pInterface =
										new PollableInterface(
											pNode,
											InetAddress.getByName(ipAddress));
									interfaceCreated = true;
								}
								// Create a new PollableGroup representing this node, interface,
								// group and package pairing
								//
								pGrp =
									new PollableGroup(
										pInterface,
										grpName,
										pkg,
										lastKnownStatus,
										grpLostDate);

								// se il pollable group è già presente tra i groups pollabili, non lo aggiunge
								if (m_pollableGroups.contains(pGrp)) {
									log.debug(
										pGrp.getGroupName()
											+ " "
											+ pGrp.getAddress()
											+ "  found in  pollable-groups map.");

									int index = m_pollableGroups.indexOf(pGrp);
									PollableGroup pGrpTmp =
										(PollableGroup)m_pollableGroups.get(index);

									// se il group ha terminato la politica di downtime, viene rischedulato
									if (pGrpTmp.isDowntimeExceeded()) {
										pGrpTmp.setStatus(pGrp.getStatus());

										Date groupLostDate =
											getServiceLostDate(dbConn, nodeId, ipAddress, grpName);
										int lastKnownGroupStatus = -1;

										if (groupLostDate != null) {
											lastKnownGroupStatus = InventoryMonitor.RETRIEVE_FAILURE;
											if (log.isDebugEnabled())
												log.debug(
													"refresh: address= "
														+ ipAddress
														+ " grp= "
														+ grpName
														+ " lastKnownStatus= download failure"
														+ " groupLostDate= "
														+ groupLostDate
														+ ", will be rescheduled.");
										}
										else {
											lastKnownGroupStatus = InventoryMonitor.RETRIEVE_SUCCESS;
											if (log.isDebugEnabled())
												log.debug(
													"refresh: address= "
														+ ipAddress
														+ " grp= "
														+ grpName
														+ " lastKnownStatus= download success, will be rescheduled.");
										}

										pGrpTmp.setGrpLostDate(groupLostDate);
										pGrpTmp.reschedule(false, false);
									} //se non ha terminato la politica di downtime non lo rischedula
									if (ownLock) {
										log.debug(
											"refresh: ----------- releasing node lock for nodeid: "
												+ nodeId
												+ " ----------");
										try {
											pNode.releaseNodeLock();
											ownLock = false;
										}
										catch (InterruptedException iE) {
											log.error(
												"refresh: Failed to release node lock on nodeid "
													+ pNode.getNodeId()
													+ ", thread interrupted.");
										}
									}
								}
								else { // if node not appartains to pollable group map...
									if (ownLock) { // probably redundant
										log.debug(
											"refresh: ----------- releasing node lock for nodeid: "
												+ nodeId
												+ " ----------");
										try {
											pNode.releaseNodeLock();
											ownLock = false;
										}
										catch (InterruptedException iE) {
											log.error(
												"refresh: Failed to release node lock on nodeid "
													+ pNode.getNodeId()
													+ ", thread interrupted.");
										}
									}
									// if the group is not present into pollable group map, it is added.

									// Initialize the group monitor with the pollable group 
									//
									//monitor.initialize(pGrp);

									// Add new group to the pollable groups list.
									//
									m_pollableGroups.add(pGrp);

									// Add the group to the PollableInterface object
									//
									// WARNING:  The PollableInterface stores services in a map
									//           keyed by service name, therefore, only the LAST
									//           PollableGroup aded to the interface for a 
									//           particular service will be represented in the
									//           map.  THIS IS BY DESIGN.
									//
									// NOTE:     addService() calls recalculateStatus() on the interface
									log.debug(
										"refresh: adding pollable group to group list of interface: "
											+ ipAddress);
									pInterface.addGroup(pGrp);

									if (interfaceCreated) {
										// Add the interface to the node
										//
										// NOTE:  addInterface() calls recalculateStatus() on the node
										if (log.isDebugEnabled())
											log.debug(
												"refresh: adding new pollable interface "
													+ ipAddress
													+ " to pollable node "
													+ nodeId);
										pNode.addInterface(pInterface);
									}
									else {
										// Recalculate node status
										//
										pNode.recalculateStatus();
									}

									if (nodeCreated) {
										// Add the node to the node map
										//
										if (log.isDebugEnabled())
											log.debug(
												"refresh: adding new pollable node: " + nodeId);
										this.addNode(pNode);
									}

									// Schedule the group
									//
									m_scheduler.schedule(pGrp, pGrp.recalculateInterval());
									if (ownLock) {
										try {
											log.debug(
												"refresh: ----------- releasing node lock for nodeid: "
													+ nodeId
													+ " ----------");
											pNode.releaseNodeLock();
											ownLock = false;
										}
										catch (InterruptedException iE) {
											log.error(
												"refresh: Failed to release node lock on nodeid "
													+ pNode.getNodeId()
													+ ", thread interrupted.");
										}
									}

								}
							}
							catch (UnknownHostException ex) {
								log.error(
									"refresh: Failed to schedule interface "
										+ ipAddress
										+ " for group monitor "
										+ grpName
										+ ", illegal address",
									ex);
							}
							catch (InterruptedException ie) {
								log.error(
									"refresh: Failed to schedule interface "
										+ ipAddress
										+ " for group monitor "
										+ grpName
										+ ", thread interrupted",
									ie);
							}
							catch (Throwable t) {
								log.error(
									"refresh: Uncaught exception, failed to refresh list"
										+ ipAddress
										+ " for group monitor "
										+ grpName,
									t);
							}
							finally {
								if (ownLock) {
									log.debug(
										"refresh: ----------- releasing node lock for nodeid: "
											+ nodeId
											+ " ----------");
									try {
										pNode.releaseNodeLock();
										ownLock = false;
									}
									catch (InterruptedException iE) {
										log.error(
											"refresh: Failed to release node lock on nodeid "
												+ pNode.getNodeId()
												+ ", thread interrupted.");
									}
								}

							}
						} // end while more packages exist
					} // end while more interfaces in result set

					rs.close();
				}
				catch (SQLException sqle) {
					log.warn("refresh: SQL exception while querying ipInterface table", sqle);
					throw sqle;
				}
			} // end while more group monitors exist

			// Debug dump pollable node map content
			//
			if (log.isDebugEnabled()) {
				log.debug("refresh: dumping content of pollable node map: ");
				Iterator j = m_pollableNodes.values().iterator();
				while (j.hasNext()) {
					PollableNode pNode = (PollableNode)j.next();
					log.debug(
						"	nodeid="
							+ pNode.getNodeId()
							+ " status="
							+ Pollable.statusType[pNode.getStatus()]);
					Iterator k = pNode.getInterfaces().iterator();
					while (k.hasNext()) {
						PollableInterface pIf = (PollableInterface)k.next();
						log.debug(
							"		interface="
								+ pIf.getAddress().getHostAddress()
								+ " status="
								+ Pollable.statusType[pIf.getStatus()]);

						Iterator s = pIf.getGroups().iterator();
						while (s.hasNext()) {
							PollableGroup pGrp = (PollableGroup)s.next();
							log.debug(
								"			group="
									+ pGrp.getGroupName()
									+ " status="
									+ Pollable.statusType[pGrp.getStatus()]);
						}
					}
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (Exception e) {
					if (log.isDebugEnabled())
						log.debug("refresh: an exception occured closing the SQL statement", e);
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				}
				catch (Throwable t) {
					if (log.isDebugEnabled())
						log.debug("refresh: an exception occured closing the SQL connection", t);
				}
			}
		}
	}

	public static boolean sendEachSuccessEvent() {
		return sendEachSuccessEvents;
	}

	// This class invoke an Inventory refresh every refreshListTime (value in /etc/inventory-configuration.xml)

	private class refreshInventory extends Thread {
		public void run() {
			ThreadCategory.setPrefix(LOG4J_CATEGORY);
			Category log = ThreadCategory.getInstance(getClass());
			log.debug("refreshInventory starting...");
			InventoryConfigFactory iCfgFactory = InventoryConfigFactory.getInstance();
			InventoryConfiguration config = iCfgFactory.getConfiguration();

			int timeToSleep = config.getRefreshListTime();
			try {
				while (true) {
					sleep(timeToSleep);
					log.debug("refreshInventory: refreshing...");
					try {
						refresh();
					}
					catch (SQLException se) {
						log.error("Unable to refresh." + se);
					}
				}
			}
			catch (InterruptedException ie) {
				log.debug("refreshInventory interrupted." + ie);
			}
		} //end run()
	} // end class refreshInventory
} // end class Inventory
