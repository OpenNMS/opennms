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

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.config.DatabaseConnectionFactory;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.*;

// Castor generated
import org.opennms.netmgt.xml.event.Event;

import org.opennms.netmgt.scheduler.Scheduler;

// castor classes generated from the inventory-configuration.xsd
import org.opennms.netmgt.config.inventory.*;

import org.opennms.netmgt.ExtendedEventConstants;

/**
 * <P>The PollableGroup class ...</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class PollableGroup
	extends IPv4NetworkInterface
	implements Pollable, ReadyRunnable {
	/**
	 * interface that this group belongs to
	 */
	private PollableInterface m_pInterface;

	/**
	 * The group inforamtion for this interface.
	 */
	private final Group m_group;

	/**
	 * The package for this inventory interface.
	 */
	private final org.opennms.netmgt.config.inventory.Package m_package;

	/**
	 * Last known/current status. 
	 */
	private int m_status;

	/**
	 * Indicates if the group changed status as the
	 * result of most recent poll.
	 *
	 * Set by poll() method.
	 */
	private boolean m_statusChangedFlag;

	/**
	 * When the last status change occured.
	 *
	 * Set by the poll() method.
	 */
	private long m_statusChangeTime;

	/** 
	 * Deletion flag...set to indicate that the group/interface/node
	 * tuple represented by this PollableGroup object has been
	 * deleted and should no longer be polled.
	 */
	private boolean m_deletionFlag;

	/**
	 * The group monitor used to poll this 
	 * group/interface pair.
	 */
	private final InventoryMonitor m_monitor;

	/**
	 * The scheduler for the inventory
	 */
	private final Scheduler m_scheduler;

	/**  
	 * List of all scheduled PollableGroup objects
	 */
	private final List m_pollableGroups;

	/** 
	 * Set to true when group is first constructed which
	 * will cause the recalculateInterval() method to return
	 * 0 resulting in an immediate poll.  
	 */
	private boolean m_pollImmediate;

	private boolean downtimeExceeded = false;

	private boolean m_unresponsiveFlag;

	/** 
	 * The last time the group was polled...whether due
	 * to a scheduled poll or node outage processing.
	 */

	private long m_lastPoll;

	/** 
	 * The last time the group was scheduled for a poll.
	 */
	private long m_lastScheduledPoll;

	/**
	 * This was the interval to use when the node was
	 * last rescheduled. This must be used or it could
	 * block a queue! (i.e. its ready time gets longer
	 * while the elements behind it are ready to go!)
	 */
	private long m_lastInterval;

	/**
	 * The key used to lookup the group properties
	 * that are passed to the monitor.
	 */
	private final String m_grpPropKey;
	
	/**
	 * The map of group parameters. These parameters are
	 * mapped by the composite key <em>(package name, group name)</em>.
	 */
	private static Map GRP_PROP_MAP =
		Collections.synchronizedMap(new TreeMap());

		
	private static String COUNT_GROUP_WITH_STATUS_A =
		"SELECT count(*) FROM ifServices, service WHERE nodeid = ? AND ifServices.serviceid = service.serviceid AND service.servicename = ? AND ifServices.status<>'D'";
		
	private static final String RETRIEVE_NODEID_BY_INTERFACES =	"SELECT nodeId FROM ipInterface WHERE ipAddr = ?";

	private static final String SELECT_PATHTOFILE = "SELECT pathtofile from inventory where nodeid=? and name=? and status='A'";

	private static final String UPDATE_CONFIGURATION =	"UPDATE inventory SET  status=? WHERE nodeID =? AND name=?";

	private static final String IS_NODE_DELETED =	"select count(*) from node where  nodeType='D' and nodeID =?";

	/**
	 * Constructs a new instance of a pollable group object that is 
	 * polled using the passed monitor. The group is scheduled based
	 * upon the values in the packages.
	 *
	 * @param dbNodeId	The database identifier key for the interfaces' node.
	 * @param iface		The interface to poll
	 * @param grpName	The name of the group being polled.
	 * @param pkg		The package with the inventory information
	 *
	 */
	PollableGroup(
		PollableInterface pInterface,
		String grpName,
		org.opennms.netmgt.config.inventory.Package pkg,
		int status,
		Date grpLostDate)throws FileNotFoundException, ValidationException, MarshalException {
		m_pInterface = pInterface;
		m_address = pInterface.getAddress(); // IPv4NetworkInterface address
		m_package = pkg;
		m_status = status;
		m_deletionFlag = false;
		m_monitor = Inventory.getInstance().getGroupMonitor(grpName);
		m_scheduler = Inventory.getInstance().getScheduler();
		m_pollableGroups = Inventory.getInstance().getPollableGroupList();

		m_pollImmediate = true; // set for immediate poll
		m_lastScheduledPoll = 0L;
		m_lastPoll = 0L;
		m_lastInterval = 0L;

		// Set status change values.  
		m_statusChangeTime = 0L;

		m_statusChangedFlag = false;
		if (m_status == InventoryMonitor.RETRIEVE_FAILURE) {
			if (grpLostDate == null)
				throw new IllegalArgumentException("The grpLostDate parm cannot be null if status is DOWNLOAD_FAILURE!");

			m_statusChangeTime = grpLostDate.getTime();
		}

		downtimeExceeded = false;
		m_unresponsiveFlag = false;

		// find the group matching the name
		//
		Group grp = null;
		Enumeration egrp = m_package.enumerateGroup();
		while (egrp.hasMoreElements()) {
			Group g = (Group) egrp.nextElement();
			if (g.getName().equalsIgnoreCase(grpName)) {
				grp = g;
				break;
			}
		}
		if (grp == null)
			throw new RuntimeException("Group name not part of package!");

		// save reference to the group
		m_group = grp;

		// add property list for this group/package combination if
		// it doesn't already exist in the group property map
		//
		m_grpPropKey = m_package.getName() + "." + m_group.getName();
		synchronized (GRP_PROP_MAP) {
			if (!GRP_PROP_MAP.containsKey(m_grpPropKey)) {
				Map m = Collections.synchronizedMap(new TreeMap());
				Enumeration ep = m_group.enumerateParameter();
				while (ep.hasMoreElements()) {
					Parameter p = (Parameter) ep.nextElement();
					m.put(p.getKey(), p.getValue());
				}
				GRP_PROP_MAP.put(m_grpPropKey, m);
			}
		}
				
	}

	public PollableInterface getInterface() {
		return m_pInterface;
	}

	/**
	 * Returns the group name 
	 */
	public String getGroupName() {
		return m_group.getName();
	}

	/**
	 * Returns true if status of group changed as a result
	 * of the last poll.
	 *
	 * WARNING:  value of m_statusChangedFlag is only reliable immediately
	 *		following a call to poll()
	 */
	public boolean statusChanged() {
		return m_statusChangedFlag;
	}

	public void resetStatusChanged() {
		m_statusChangedFlag = false;
	}

	public int getStatus() {
		return m_status;
	}

	public void setStatus(int status) {
		if (m_status != status) {
			m_status = status;
			m_statusChangeTime = System.currentTimeMillis();
		}
	}

	public void markAsDeleted() {
		m_deletionFlag = true;
	}

	public boolean isDeleted() {
		return m_deletionFlag;
	}

	public long getLastPollTime() {
		return m_lastPoll;
	}

	public long getLastScheduleInterval() {
		return m_lastInterval;
	}

	/**
	 * Returns the time (in milliseconds) after which this is
	 * scheduled to run.
	 */
	public long getScheduledRuntime() {
		return (this.getLastPollTime() + this.getLastScheduleInterval());
	}

	public String getPackageName() {
		return m_package.getName();
	}

	/**
	 * Returns the package associated with this group. 
	 */
	public org.opennms.netmgt.config.inventory.Package getPackage() {
		return m_package;
	}

	/**
	 * This method is used to evaluate the status of this
	 * interface and group pair. If it is time to run the
	 * poll again then a value of true is returned. If the
	 * interface is not ready then a value of false is returned.
	 *
	 * @throws java.lang.RuntimeException Throws if the ready
	 * 	time cannot be computed due to invalid downtime model.
	 */
	public boolean isReady() {
		long when = m_lastInterval;
		boolean ready = false;

		if (when < 1) {
			ready = true;
		} else {
			ready =
				((when - (System.currentTimeMillis() - m_lastScheduledPoll))
					< 1);
		}

		return ready;
	}

	/**
	 * Reschedules the group at the specified interval
	 * (in milliseconds).
	 */
	void reschedule(long interval) {
		// Update m_lastInterval
		// 
		// NOTE: Never want to reschedule at less than 1 milliscond interval
		//
		if (interval <= 0)
			m_lastInterval = m_group.getInterval();
		else
			m_lastInterval = interval;

		// Reschedule the group
		m_scheduler.schedule(this, interval);
	}

	/**
	 * This method is called to reschedule the group for inventory. 
	 *
	 * NOTE:  Scheduler calls reschedule() with reUseInterval parm 
	 *	  set to true in the event that a scheduled outage
	 * 	  is in effect when a group is popped from the interval
	 * 	  queue for inventory.
	 *
	 * @param reUseInterval Flag which controls how the interval
	 * 			at which to reschedule the interface
	 * 			is determined.  If true, value of
	 * 			m_lastInterval is used.  Otherwise
	 * 			recalculateInterval() is called to
	 * 			recalculate the interval.
	 */
	void reschedule(boolean reUseInterval, boolean sendEvent) {
		Category log = ThreadCategory.getInstance(getClass());

		// Determine interval at which to reschedule the interface
		// 
		long interval = 0L;

		if (reUseInterval) {
			interval = m_lastInterval;
		} else {
			// Recalculate inventory interval
			// 
			// NOTE:  interval of -1 indicates interface/group
			//        pair has exceeded the downtime model 

			interval = recalculateInterval();

			if (interval < 0) {
				downtimeExceeded = true;
				log.info("Downtime exceeded. Will not reschedule it.");
				if (sendEvent) {
					if(m_group.getSendEvents()==true)
						sendEvent(ExtendedEventConstants.INVENTORY_FAILURE_EVENT_UEI, m_monitor.getInventoryCategory()+ " inventory failed.", null );
				}
				try {
					java.sql.Connection dbConn = DatabaseConnectionFactory.getInstance().getConnection();
					PreparedStatement stmt = dbConn.prepareStatement(RETRIEVE_NODEID_BY_INTERFACES);
					stmt.setString(1, m_address.getHostAddress());
					ResultSet rs = stmt.executeQuery();

					// retrieve nodeid the ip address is associated and update the file repository path
					int nodeId = 0;
					while (rs.next()) {
						nodeId = rs.getInt(1);
					}
					stmt = dbConn.prepareStatement(IS_NODE_DELETED);
					stmt.setInt(1, nodeId);
					rs = stmt.executeQuery();
					int countDeleted = 0;
					while (rs.next()) {
						countDeleted = rs.getInt(1);
					}
					String newStatus="N";
					if(countDeleted>0){
						newStatus="D";
					}
					stmt =	dbConn.prepareStatement(UPDATE_CONFIGURATION);
					stmt.setString(1,newStatus);
					stmt.setInt(2, nodeId);
					stmt.setString(3, m_monitor.getInventoryCategory());
					stmt.executeUpdate();
					dbConn.close();
				} catch (SQLException s) {
					log.error("Unable to update DB");
				}
				return; // Return without rescheduling
			} else {
				downtimeExceeded = false;
				log.info("Downtime not exceeded. Will reschedule it.");
			}
		}
		this.reschedule(interval);

	}

	/**
	 * This method is used to return the next interval for this
	 * interface. If the interval is zero then this group 
	 * has never run and should be scheduled immediantly. If the
	 * time is -1 then the node should be deleted. Otherwise the
	 * appropriate scheduled time is returned.
	 *
	 * @throws java.lang.RuntimeException Throws if the ready
	 * 	time cannot be computed due to invalid downtime model.
	 */
	long recalculateInterval() {
		Category log = ThreadCategory.getInstance(getClass());

		// If poll immediate flag is set the group hasn't
		// been polled yet.  Return 0 to cause an immediate 
		// poll of the interface.
		if (m_pollImmediate) {
			return 0;
		}

		long when = m_group.getInterval();
		long downSince = 0;
		if (m_status == InventoryMonitor.RETRIEVE_FAILURE)
			downSince = System.currentTimeMillis() - m_statusChangeTime;

		if (log.isDebugEnabled())
			log.debug(
				"recalculateInterval for "
					+ m_pInterface.getAddress().getHostAddress()
					+ "/"
					+ m_group.getName()
					+ " : "
					+ " status= "
					+ Pollable.statusType[m_status]
					+ " downSince= "
					+ downSince);

		switch (m_status) {
			case InventoryMonitor.RETRIEVE_SUCCESS :
				break;

			case InventoryMonitor.RETRIEVE_FAILURE :
				boolean matched = false;
				Enumeration edowntime = m_package.enumerateDowntime();
				while (edowntime.hasMoreElements()) {
					Downtime dt = (Downtime) edowntime.nextElement();
					if (dt.getBegin() <= downSince) {
						if (dt.getDelete() != null
							&& (dt.getDelete().equals("yes")
								|| dt.getDelete().equals("true"))) {
							when = -1;
							matched = true;
						} else if (
							dt.hasEnd() && dt.getEnd() > m_statusChangeTime) {
							// in this interval
							//
							when = dt.getInterval();
							matched = true;
						} else // no end
							{
							when = dt.getInterval();
							matched = true;
						}
					}
				}
				if (!matched) {
					log.warn(
						"recalculateInterval: Could not locate downtime model, throwing runtime exception");
					throw new RuntimeException(
						"Downtime model is invalid, cannot schedule interface "
							+ m_pInterface.getAddress().getHostAddress()
							+ ", group = "
							+ m_group.getName());
				}

				break;

			default :
				log.warn(
					"recalculateInterval: invalid status found, downtime model lookup failed. throwing runtime exception");
				throw new RuntimeException(
					"Invalid Inventory Status for interface "
						+ m_pInterface.getAddress().getHostAddress()
						+ ", group = "
						+ m_group.getName()
						+ ", status = "
						+ m_status);

		} // end switch()

		if (log.isDebugEnabled())
			log.debug(
				"recalculateInterval: new scheduling interval for "
					+ m_pInterface.getAddress().getHostAddress()
					+ "/"
					+ m_group.getName()
					+ " = "
					+ when);
		return when;
	}

	public void setGrpLostDate(Date grpLostDate) {
		if (m_status == InventoryMonitor.RETRIEVE_FAILURE && grpLostDate == null) {
			if (grpLostDate == null)
				throw new IllegalArgumentException("The grpLostDate parm cannot be null if status is DOWNLOAD_FAILURE!");
		}
		if (grpLostDate != null)
			m_statusChangeTime = grpLostDate.getTime();
	}

	/**
	 * Send an event to the event listener
	 * @param uei 
	 * @param properties
	 * @param eventDescr
	 */
	private void sendEvent(String uei, String eventDescr, String operatorInstruction) {
		Category log = ThreadCategory.getInstance(getClass());
		Event event = new Event();
		event.setUei(uei);
		event.setNodeid((long) m_pInterface.getNode().getNodeId());
		event.setInterface(m_pInterface.getAddress().getHostAddress());
		event.setService(
			Inventory.getInstance().getRequiredService(m_group.getName()));
		event.setSource("OpenNMS.Inventory");
		event.setTime(ExtendedEventConstants.formatToString(new java.util.Date()));
		if(operatorInstruction != null){
			event.setOperinstruct(operatorInstruction);
		}
		
		if (eventDescr != null) {
			event.setDescr(eventDescr);
		}

		try {
			event.setHost(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException ex) {
			event.setHost("unresolved.host");
		}

		// Send the event
		//
		try {
			EventIpcManagerFactory.getInstance().getManager().sendNow(event);
			if (log.isDebugEnabled()) {
				log.debug(
					"Sent event "
						+ uei
						+ " for "
						+ m_pInterface.getNode().getNodeId()
						+ "/"
						+ m_pInterface.getAddress().getHostAddress()
						+ "/"
						+ m_group.getName());
				log.debug("Event sent by Thread (Thread hash:" + Thread.currentThread().hashCode() + " ||| Thread object:" + Thread.currentThread() +")");
			}
		} catch (Throwable t) {
			log.error(
				"Failed to send the event "
					+ uei
					+ " for interface "
					+ m_pInterface.getAddress().getHostAddress(),
				t);
		}
	}

	/**
	 * Tests if two PollableGroup objects refer to the same 
	 * nodeid/interface/group tuple.  
	 *
	 * @param agroup  	the PollableGroup object to compare
	 * 
	 * @return TRUE if the two pollable group objects are equivalent,
	 * 		FALSE otherwise.
	 */
	public boolean equals(Object agroup) {
		boolean isEqual = false;

		if (agroup instanceof PollableGroup) {
			PollableGroup temp = (PollableGroup) agroup;

			if (this.m_pInterface.getNode().getNodeId()
				== temp.m_pInterface.getNode().getNodeId()
				&& this.m_address.equals(temp.m_address)
				&& this.m_group.getName().equals(temp.m_group.getName())) {
				isEqual = true;
			}
		}

		return isEqual;
	}

	/**
	 * This is the main method of the class. An instance is normally
	 * enqueued on the scheduler which checks its <code>isReady</code>
	 * method to determine execution. If the instance is ready for 
	 * execution then it is started with it's own thread context
	 * to execute the query. The last step in the method before
	 * it exits is to reschedule the interface.
	 *
	 */
	public void run() {
		Category log = ThreadCategory.getInstance(getClass());

		try {
			this.doRun(true);
		} catch (LockUnavailableException e) {
			// failed to acquire lock, just reschedule on 10 second queue
			if (log.isDebugEnabled())
				log.debug(
					"Lock unavailable, rescheduling on 10 sec queue, reason: "
						+ e.getMessage());
			this.reschedule(10000);
		} catch (InterruptedException e) {
			// The thread was interrupted; reschedule on 10 second queue
			if (log.isDebugEnabled())
				log.debug(e);
			this.reschedule(10000);
		}
	}

	/**
	 * This an alternative entry point into the class. This was originally
	 * created in order to support the PollableGroupProxy, which needed
	 * the option of handling its own scheduling and needed to keep the
	 * PollableGroup from rescheduling itself.
	 *
	 * In addition to allowing this, it also allows exceptions that require
	 * a rescheduling decision to pass back up the stack.  In all other ways,
	 * this method works the same as run().
	 *
	 * @param reschedule set this to true if you want the pollable group 
	 * to reschedule itself when done processing.
	 *
	 * @throws LockUnavailableException If it was unable to obtain a node
	 *                                  lock
	 * @throws ThreadInterruped If the thread was interrtuped while
	 *                          waiting for a node lock.
	 */
	public void run(boolean reschedule)
		throws LockUnavailableException, InterruptedException {
		this.doRun(reschedule);
	}

	/**
	 * This used to be the implementation for the run() method.  When
	 * we created run(boolean), however, we needed to move the
	 * implementation down a level lower so that we could overload the
	 * run() method.
	 *
	 * @param allowedToRescheduleMyself set this to true if you want the
	 *  pollable group to reschedule itself when done processing.
	 *
	 * @throws LockUnavailableException If it was unable to obtain a node
	 *                                  lock
	 * @throws ThreadInterruped If the thread was interrtuped while
	 *                          waiting for a node lock.
	 *
	 */
	private void doRun(boolean allowedToRescheduleMyself)
		throws LockUnavailableException, InterruptedException {
		Category log = ThreadCategory.getInstance(getClass());

		// Is the group marked for deletion?  If so simply return.
		//
		if (this.isDeleted())
			return;

		// NodeId
		int nodeId = m_pInterface.getNode().getNodeId();

		// Update last scheduled poll time if allowedToRescheduleMyself
		// flag is true
		if (allowedToRescheduleMyself)
			m_lastScheduledPoll = System.currentTimeMillis();

		PollableNode pNode = Inventory.getInstance().getNode(nodeId);

		/*
		 * Acquire lock to 'PollableNode'
		 */
		boolean ownLock = false;
		try {
			// Attempt to obtain node lock...wait no longer than 500ms
			// We don't want to tie up the thread for long periods of time
			// waiting for the lock on the PollableNode to be released.
			if (log.isDebugEnabled())
				log.debug(
					"run: ------------- requesting node lock for nodeid: "
						+ nodeId
						+ " -----------");
			if (!(ownLock = pNode.getNodeLock(500)))
				throw new LockUnavailableException(	"failed to obtain lock on nodeId " + nodeId);
		} catch (InterruptedException iE) {
			// failed to acquire lock
			throw new InterruptedException(
				"failed to obtain lock on nodeId "
					+ nodeId
					+ ": "
					+ iE.getMessage());
		}
		
		// Now we have a lock
		if (ownLock) // This is probably redundant, but better to be sure.
			{
			try {
				pNode.pollGroup(this);
			} finally {
				if (log.isDebugEnabled())
					log.debug(
						"run: ----------- releasing node lock for nodeid: "
							+ nodeId
							+ " ----------");
				try {
					pNode.releaseNodeLock();
				} catch (InterruptedException iE) {
					log.error(
						"run: thread interrupted...failed to release lock on nodeId "
							+ nodeId);
				}
			}
		}
		// reschedule the group for inventory
		if (allowedToRescheduleMyself)
			this.reschedule(false, true);
		return;
	}
	/**
	 * <P>Invokes an inventory of the group via the GroupMonitor.</P>
	 */
	public int inventory() throws IOException{
		Category log = ThreadCategory.getInstance(getClass());
  		String eventCompareDescr = null;
		String eventSaveDescr = null;
		m_lastPoll = System.currentTimeMillis();
		m_statusChangedFlag = false;
		InetAddress addr = (InetAddress) m_pInterface.getAddress();
		if (log.isDebugEnabled())
				log.debug("poll: starting new poll for "+ addr.getHostAddress()+ "/"+ m_group.getName()+ "/"+ m_package.getName());
		Map propertiesMap = (Map) GRP_PROP_MAP.get(m_grpPropKey);

		int retrieveResult = InventoryMonitor.RETRIEVE_FAILURE;
		int saveResult = -1;
		int compareResult=-1;
		boolean renameOldConfigutationFile=false;
		boolean unparsableOldConfiguration=false;
		try {
			synchronized (m_monitor) {
				java.sql.Connection dbConn = null;
				ResultSet rs = null;
				dbConn = DatabaseConnectionFactory.getInstance().getConnection();
				PreparedStatement stmt = dbConn.prepareStatement(COUNT_GROUP_WITH_STATUS_A);
				stmt.setInt(1, (int) m_pInterface.getNode().getNodeId());
				stmt.setString(2,Inventory.getInstance().getRequiredService(m_group.getName()));
				rs = stmt.executeQuery();
				int count = 0;
				while (rs.next()) {
					count = rs.getInt(1);
				}
				dbConn.close();
				stmt.close();
				rs.close();
				if (count > 0) { // *** BUSINESS CODE BEGINS ***
					log.debug("before retrieve() for "+addr.getHostAddress()+"/"+m_group.getName());
					retrieveResult = m_monitor.doRetrieve(this, propertiesMap);
					log.debug("after retrieve() for "+addr.getHostAddress()+"/"+m_group.getName()+"status = "+retrieveResult);
					if (retrieveResult != m_status)
						  m_statusChangedFlag = true;
					String newConfiguration = null;
					if (retrieveResult == InventoryMonitor.RETRIEVE_SUCCESS) {
						try{
							newConfiguration = m_monitor.getData();
							if(newConfiguration==null || newConfiguration.equals("")){
								retrieveResult = InventoryMonitor.RETRIEVE_FAILURE;
							}
							String oldInventoryPath = getOldInventoryPath(propertiesMap);
							if(m_group.getCompare()==true){
								log.debug("before compare() for "+addr.getHostAddress()+"/"+m_group.getName());
								Comparator cmp = new Comparator(oldInventoryPath,newConfiguration);
								compareResult = cmp.compare();
								eventCompareDescr = cmp.getCompareMessage();
								log.debug("Compare Descr: "+eventCompareDescr);
								log.debug("after compare() for "+addr.getHostAddress()+"/"+m_group.getName()+"compareResult = "+compareResult);
							}else{
								compareResult=InventoryMonitor.CONFIGURATION_CHANGED;
							}
						}catch(UnparsableConfigurationException uc){
							log.warn(uc);
							renameOldConfigutationFile=true;
							unparsableOldConfiguration=true;
						}
						catch(ValidationException ve){
							log.error(ve);
						}
						catch(MarshalException me){
							log.error(me);
						}
						catch(IllegalStateException ise){
							log.error(ise);
						}
					if(retrieveResult==InventoryMonitor.RETRIEVE_SUCCESS){
							Saver sv = new Saver(this,propertiesMap);
							log.debug("before save() for "+addr.getHostAddress()+"/"+m_group.getName());
							saveResult = sv.save(newConfiguration, m_monitor.getInventoryCategory() , compareResult, renameOldConfigutationFile);
							log.debug("after save() for "+addr.getHostAddress()+"/"+m_group.getName());
							eventSaveDescr = sv.getSaveMessage();
						}
					}
				} else {
					return InventoryMonitor.RETRIEVE_FAILURE;
					} // *** BUSINESS CODE ENDS ***
			}

		} catch (NetworkInterfaceNotSupportedException ex) {
				log.error("poll: Interface " + addr.getHostAddress() + " Not Supported!",ex);
				return retrieveResult;
			}
		catch (SQLException sqlex) {
				log.error(sqlex);
				return retrieveResult;
			}

		switch (retrieveResult) {
			case InventoryMonitor.RETRIEVE_FAILURE :
			    log.debug(m_group.getName()+"/"+m_address.getHostAddress()+"status = RETRIEVE_FAILURE");
				break;

			case InventoryMonitor.RETRIEVE_SUCCESS :
			    log.debug(m_group.getName()+"/"+m_address.getHostAddress()+"status = RETRIEVE_SUCCESS");
				if(saveResult == InventoryMonitor.CONFIGURATION_NOT_SAVED){
					if(m_group.getSendEvents()==true){
						String operatorInstruction=null;
						sendEvent(ExtendedEventConstants.INVENTORY_FAILURE_EVENT_UEI, m_monitor.getInventoryCategory()+" inventory failure.",operatorInstruction);
					}
					break;						
				}
				String operatorInstruction=null;
				if (Inventory.sendEachSuccessEvent()) 
					if(m_group.getSendEvents()==true){
							if(unparsableOldConfiguration){
											operatorInstruction="Old "+m_monitor.getInventoryCategory()+" 'active' inventory file corrupted, renamed with current time.";
											}
							if (saveResult == InventoryMonitor.FIRST_ACTIVE_CONFIGURATION_DOWNLOAD) {	
									sendEvent(ExtendedEventConstants.INVENTORY_SUCCESS_EVENT_UEI,  "Old "+m_monitor.getInventoryCategory()+" 'active' inventory not found.",operatorInstruction);
									}else 
										 sendEvent(ExtendedEventConstants.INVENTORY_SUCCESS_EVENT_UEI, m_monitor.getInventoryCategory()+" inventory success.",operatorInstruction);
					}

				// make sure is not the first configuration download and is changed the configuration
				if (m_group.getCompare()==true && compareResult == InventoryMonitor.CONFIGURATION_CHANGED	&& saveResult != InventoryMonitor.FIRST_ACTIVE_CONFIGURATION_DOWNLOAD) {
							log.debug(m_group.getName()+"/"+m_address.getHostAddress()+"status= CONFIGURATION_CHANGED");
							log.debug(eventCompareDescr);
							
							String prefix = "<b>"+m_monitor.getInventoryCategory()+" inventory changed.</b><br>";
							if(m_group.getSendEvents()==true)
								sendEvent(ExtendedEventConstants.CONFIGURATION_CHANGED_EVENT_UEI,prefix + eventCompareDescr,null);
						}
			}

		// Set the new status
		m_status = retrieveResult;

		// Reset poll immediate flag
		m_pollImmediate = false;

		log.debug("poll: exiting for "+ addr.getHostAddress()	+ "/"+ m_group.getName()+ "/"+ m_package.getName()+ "return status="+m_status);
		
		return m_status;
	}

	private String getOldInventoryPath(Map parameters){
			Category log = ThreadCategory.getInstance(getClass());
			String path = (String) parameters.get("path");
			int nodeId=0;
			
			java.sql.Connection dbConn = null;
			try {
				dbConn = DatabaseConnectionFactory.getInstance().getConnection();
			} catch (SQLException s) {
				log.error("Unable to connect to DB");
			} 

			ResultSet rs = null;
			String file_repository = "";
			try {
				PreparedStatement stmt =
					dbConn.prepareStatement(RETRIEVE_NODEID_BY_INTERFACES);
				stmt.setString(1, m_address.getHostAddress());
				rs = stmt.executeQuery();

				// retrieve nodeid the ip address is associated and update the file repository path

				while (rs.next()) {
					nodeId = rs.getInt(1);
				}
				stmt = dbConn.prepareStatement(SELECT_PATHTOFILE);
				stmt.setInt(1,nodeId);
				stmt.setString(2,m_monitor.getInventoryCategory());
				rs=stmt.executeQuery();
				while(rs.next()){
					file_repository = rs.getString(1);
				}
				dbConn.close();
			} catch (SQLException s) {
					log.error("Unable to read from DB");
				}
			return (file_repository!=null)?file_repository:"";
	}


	public boolean isDowntimeExceeded() {
		return downtimeExceeded;
	}

}
