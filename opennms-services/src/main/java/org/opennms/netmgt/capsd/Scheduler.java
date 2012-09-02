/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CapsdConfigFactory;

/**
 * This class implements a simple scheduler to ensure that Capsd rescans occurs
 * at the expected intervals.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
final class Scheduler implements Runnable, PausableFiber {
    /**
     * The prefix for the fiber name.
     */
    private static final String FIBER_NAME = "Capsd Scheduler";

    /**
     * SQL used to retrieve list of nodes from the node table.
     */
    private static final String SQL_RETRIEVE_NODES = "SELECT nodeid FROM node WHERE nodetype != 'D'";

    /**
     * SQL used to retrieve the last poll time for all the managed interfaces
     * belonging to a particular node.
     */
    private static final String SQL_GET_LAST_POLL_TIME = "SELECT iplastcapsdpoll FROM ipinterface WHERE nodeid=? AND (ismanaged = 'M' OR ismanaged = 'N')";

    /**
     * Special identifier used in place of a valid node id in order to schedule
     * an SMB reparenting using the rescan scheduler.
     */
    private static final int SMB_REPARENTING_IDENTIFIER = -1;

    /**
     * The status for this fiber.
     */
    private int m_status;

    /**
     * The worker thread that executes this instance.
     */
    private Thread m_worker;

    /**
     * List of NodeInfo objects representing each of the nodes in the database
     * capability of being scheduled.
     */
    private List<NodeInfo> m_knownNodes;

    /**
     * The configured interval (in milliseconds) between rescans
     */
    private long m_interval;

    /**
     * The configured initial sleep (in milliseconds) prior to scheduling
     * rescans
     */
    private long m_initialSleep;

    /**
     * The rescan queue where new RescanProcessor objects are enqueued for
     * execution.
     */
    private ExecutorService m_rescanQ;

    private RescanProcessorFactory m_rescanProcessorFactory;

    /**
     * This class encapsulates the information about a node necessary to
     * schedule the node for rescans.
     */
    final class NodeInfo implements Runnable {
        int m_nodeId;

        Timestamp m_lastScanned;

        long m_interval;

        boolean m_scheduled;

        NodeInfo(int nodeId, Timestamp lastScanned, long interval) {
            m_nodeId = nodeId;
            m_lastScanned = lastScanned;
            m_interval = interval;
            m_scheduled = false;
        }

        NodeInfo(int nodeId, Date lastScanned, long interval) {
            m_nodeId = nodeId;
            m_lastScanned = new Timestamp(lastScanned.getTime());
            m_interval = interval;
            m_scheduled = false;
        }

        boolean isScheduled() {
            return m_scheduled;
        }

        int getNodeId() {
            return m_nodeId;
        }

        Timestamp getLastScanned() {
            return m_lastScanned;
        }

        long getRescanInterval() {
            return m_interval;
        }

        void setScheduled(boolean scheduled) {
            m_scheduled = scheduled;
        }

        void setLastScanned(Date lastScanned) {
            m_lastScanned = new Timestamp(lastScanned.getTime());
        }

        void setLastScanned(Timestamp lastScanned) {
            m_lastScanned = lastScanned;
        }

        boolean timeForRescan() {
            if (System.currentTimeMillis() >= (m_lastScanned.getTime() + m_interval))
                return true;
            else
                return false;
        }

        public void run() {
            try {
                m_rescanProcessorFactory.createRescanProcessor(getNodeId()).run();
            } finally {
                setLastScanned(new Date());
                setScheduled(false);
            }
        }
    }

    /**
     * Constructs a new instance of the scheduler.
     * @param rescanProcessorFactory TODO
     * 
     */
    Scheduler(ExecutorService rescanQ, RescanProcessorFactory rescanProcessorFactory) throws SQLException {

        m_rescanQ = rescanQ;
        m_rescanProcessorFactory = rescanProcessorFactory;

        m_status = START_PENDING;
        m_worker = null;

        m_knownNodes = Collections.synchronizedList(new LinkedList<NodeInfo>());
        
        // Get rescan interval from configuration factory
        //
        m_interval = CapsdConfigFactory.getInstance().getRescanFrequency();
        if (log().isDebugEnabled())
            log().debug("Scheduler: rescan interval(millis): " + m_interval);

        // Get initial rescan sleep time from configuration factory
        //
        m_initialSleep = CapsdConfigFactory.getInstance().getInitialSleepTime();
        if (log().isDebugEnabled())
            log().debug("Scheduler: initial rescan sleep time(millis): " + m_initialSleep);

        // Schedule SMB Reparenting using special nodeId (-1)
        //
        // Schedule this node in such a way that it will be
        // scheduled immediately and SMB reparenting will take place
        Date lastSmbReparenting = new Date();
        lastSmbReparenting.setTime(System.currentTimeMillis() - m_interval);

        if (log().isDebugEnabled())
            log().debug("Scheduler: scheduling SMB reparenting...");
        NodeInfo smbInfo = new NodeInfo(SMB_REPARENTING_IDENTIFIER, lastSmbReparenting, m_interval);
        m_knownNodes.add(smbInfo);

        // Load actual known nodes from the database
        //
        loadKnownNodes();
        if (log().isDebugEnabled())
            log().debug("Scheduler: done loading known nodes, node count: " + m_knownNodes.size());
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * Builds a list of NodeInfo objects representing each of the nodes in the
     * database capable of being scheduled for rescan.
     * 
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    private void loadKnownNodes() throws SQLException {
        Connection db = null;

        PreparedStatement nodeStmt = null;
        PreparedStatement ifStmt = null;
        ResultSet rs = null;
        ResultSet rset = null;
        
        final DBUtils d = new DBUtils(getClass());
        try {
            db = DataSourceFactory.getInstance().getConnection();
            d.watch(db);
            // Prepare SQL statements in advance
            //
            nodeStmt = db.prepareStatement(SQL_RETRIEVE_NODES);
            d.watch(nodeStmt);
            ifStmt = db.prepareStatement(SQL_GET_LAST_POLL_TIME);
            d.watch(ifStmt);

            // Retrieve non-deleted nodes from the node table in the database
            //
            rs = nodeStmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                // Retrieve an interface from the ipInterface table in
                // the database for its last polled/scanned time

                int nodeId = rs.getInt(1);
                ifStmt.setInt(1, nodeId); // set nodeid
                if (log().isDebugEnabled())
                    log().debug("loadKnownNodes: retrieved nodeid " + nodeId + ", now getting last poll time.");

                rset = ifStmt.executeQuery();
                d.watch(rs);
                if (rset.next()) {
                    Timestamp lastPolled = rset.getTimestamp(1);
                    if (lastPolled != null && rset.wasNull() == false) {
                        if (log().isDebugEnabled())
                            log().debug("loadKnownNodes: adding node " + nodeId + " with last poll time " + lastPolled);
                        NodeInfo nodeInfo = new NodeInfo(nodeId, lastPolled, m_interval);
                        m_knownNodes.add(nodeInfo);
                    }
                } else {
                    if (log().isDebugEnabled())
                        log().debug("Node w/ nodeid " + nodeId + " has no managed interfaces from which to retrieve a last poll time...it will not be scheduled.");
                }
            }
        } finally {
            d.cleanUp();
        }

    }

    /**
     * Creates a NodeInfo object representing the specified node and adds it to
     * the known node list for scheduling.
     * 
     * @param nodeId
     *            Id of node to be scheduled
     * 
     * @throws SQLException
     *             if there is any problem accessing the database
     */
    void scheduleNode(int nodeId) throws SQLException {
        // Retrieve last poll time for the node from the ipInterface
        // table.
        Connection db = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            db = DataSourceFactory.getInstance().getConnection();
            d.watch(db);
            PreparedStatement ifStmt = db.prepareStatement(SQL_GET_LAST_POLL_TIME);
            d.watch(ifStmt);
            ifStmt.setInt(1, nodeId);
            ResultSet rset = ifStmt.executeQuery();
            d.watch(rset);
            if (rset.next()) {
                Timestamp lastPolled = rset.getTimestamp(1);
                if (lastPolled != null && rset.wasNull() == false) {
                    if (log().isDebugEnabled())
                        log().debug("scheduleNode: adding node " + nodeId + " with last poll time " + lastPolled);
                    m_knownNodes.add(new NodeInfo(nodeId, lastPolled, m_interval));
                }
            } else
                log().warn("scheduleNode: Failed to retrieve last polled time from database for nodeid " + nodeId);
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Removes the specified node from the known node list.
     * 
     * @param nodeId
     *            Id of node to be removed.
     */
    void unscheduleNode(int nodeId) {
        synchronized (m_knownNodes) {
            Iterator<NodeInfo> iter = m_knownNodes.iterator();
            while (iter.hasNext()) {
                NodeInfo nodeInfo = iter.next();
                if (nodeInfo.getNodeId() == nodeId) {
                    log().debug("unscheduleNode: removing node " + nodeId + " from the scheduler.");
                    m_knownNodes.remove(nodeInfo);
                    break;
                }
            }
        }
    }

    /**
     * Creates a NodeInfo object representing the specified node and adds it to
     * the rescan queue for immediate rescanning.
     * 
     * @param nodeId
     *            Id of node to be rescanned
     */
    void forceRescan(int nodeId) {
        try {
            m_rescanQ.execute(m_rescanProcessorFactory.createForcedRescanProcessor(nodeId));
        } catch (RejectedExecutionException e) {
            log().error("forceRescan: Failed to add node " + nodeId + " to the rescan queue.", e);
        }
    }

    /**
     * Starts the fiber.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is already running.
     */
    public synchronized void start() {
        if (m_worker != null) throw new IllegalStateException("The fiber has already run or is running");

        m_worker = new Thread(this, getName());
        m_worker.setDaemon(true);
        m_worker.start();
        m_status = STARTING;

        if (log().isDebugEnabled()) log().debug("Scheduler.start: scheduler started");
    }

    /**
     * Stops the fiber. If the fiber has never been run then an exception is
     * generated.
     *
     * @throws java.lang.IllegalStateException
     *             Throws if the fiber has never been started.
     */
    public synchronized void stop() {
        if (m_worker == null)
            throw new IllegalStateException("The fiber has never been started");

        m_status = STOP_PENDING;
        m_worker.interrupt();

        log().debug("Scheduler.stop: scheduler stopped");
    }

    /**
     * Pauses the scheduler if it is current running. If the fiber has not been
     * run or has already stopped then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Throws if the operation could not be completed due to the
     *             fiber's state.
     */
    public synchronized void pause() {
        if (m_worker == null)
            throw new IllegalStateException("The fiber has never been started");

        if (m_status == STOPPED || m_status == STOP_PENDING)
            throw new IllegalStateException("The fiber is not running or a stop is pending");

        if (m_status == PAUSED)
            return;

        m_status = PAUSE_PENDING;
        notifyAll();
    }

    /**
     * Resumes the scheduler if it has been paused. If the fiber has not been
     * run or has already stopped then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Throws if the operation could not be completed due to the
     *             fiber's state.
     */
    public synchronized void resume() {
        if (m_worker == null)
            throw new IllegalStateException("The fiber has never been started");

        if (m_status == STOPPED || m_status == STOP_PENDING)
            throw new IllegalStateException("The fiber is not running or a stop is pending");

        if (m_status == RUNNING)
            return;

        m_status = RESUME_PENDING;
        notifyAll();
    }

    /**
     * Returns the current of this fiber.
     *
     * @return The current status.
     */
    public synchronized int getStatus() {
        if (m_worker != null && m_worker.isAlive() == false)
            m_status = STOPPED;
        return m_status;
    }

    /**
     * Returns the name of this fiber.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return FIBER_NAME;
    }

    /**
     * The main method of the scheduler. This method is responsible for checking
     * the runnable queues for ready objects and then enqueuing them into the
     * thread pool for execution.
     */
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        if (log().isDebugEnabled())
            log().debug("Scheduler.run: scheduler running");

        // Loop until a fatal exception occurs or until
        // the thread is interrupted.
        //
        boolean firstPass = true;
        for (;;) {
            // Status check
            //
            synchronized (this) {
                if (m_status != RUNNING && m_status != PAUSED && m_status != PAUSE_PENDING && m_status != RESUME_PENDING) {
                    if (log().isDebugEnabled())
                        log().debug("Scheduler.run: status = " + m_status + ", time to exit");
                    break;
                }
            }

            // If this is the first pass we want to pause momentarily
            // This allows the rest of the background processes to come
            // up and stabilize before we start generating events from rescans.
            //
            if (firstPass) {
                firstPass = false;
                synchronized (this) {
                    try {
                        if (log().isDebugEnabled())
                            log().debug("Scheduler.run: initial sleep configured for " + m_initialSleep + "ms...sleeping...");
                        wait(m_initialSleep);
                    } catch (InterruptedException ex) {
                        if (log().isDebugEnabled())
                            log().debug("Scheduler.run: interrupted exception during initial sleep...exiting.");
                        break; // exit for loop
                    }
                }
            }

            // iterate over the known node list, add any
            // nodes ready for rescan to the rescan queue
            // for processing.
            //
            int added = 0;

            synchronized (m_knownNodes) {
                if (log().isDebugEnabled())
                    log().debug("Scheduler.run: iterating over known nodes list to schedule...");
                Iterator<NodeInfo> iter = m_knownNodes.iterator();
                while (iter.hasNext()) {
                    NodeInfo node = iter.next();

                    // Don't schedule if already scheduled
                    if (node.isScheduled())
                        continue;

                    // Don't schedule if its not time for rescan yet
                    if (!node.timeForRescan())
                        continue;

                    // Must be time for a rescan!
                    //
                    try {
                        node.setScheduled(true); // Mark node as scheduled

                        // Special Case...perform SMB reparenting if nodeid
                        // of the scheduled node is -1
                        //
                        if (node.getNodeId() == SMB_REPARENTING_IDENTIFIER) {
                            if (log().isDebugEnabled())
                                log().debug("Scheduler.run: time for reparenting via SMB...");

                            Connection db = null;
                            try {
                                db = DataSourceFactory.getInstance().getConnection();

                                ReparentViaSmb reparenter = new ReparentViaSmb(db);
                                try {
                                    reparenter.sync();
                                } catch (SQLException sqlE) {
                                    log().error("Unexpected database error during SMB reparenting", sqlE);
                                } catch (Throwable t) {
                                    log().error("Unexpected error during SMB reparenting", t);
                                }
                            } catch (SQLException sqlE) {
                                log().error("Unable to get database connection from the factory.", sqlE);
                            } finally {
                                if (db != null) {
                                    try {
                                        db.close();
                                    } catch (Throwable e) {
                                    }
                                }
                            }

                            // Update the schedule information for the SMB
                            // reparenting node
                            // 
                            node.setLastScanned(new Date());
                            node.setScheduled(false);

                            if (log().isDebugEnabled())
                                log().debug("Scheduler.run: SMB reparenting completed...");
                        }
                        // Otherwise just add the NodeInfo to the queue which will create
                        // a rescanProcessor and run it
                        //
                        else {
                            if (log().isDebugEnabled())
                                log().debug("Scheduler.run: adding node " + node.getNodeId() + " to the rescan queue.");
                            m_rescanQ.execute(node);
                            added++;
                        }
                    } catch (RejectedExecutionException e) {
                        log().info("Scheduler.schedule: failed to add new node to rescan queue", e);
                        throw new UndeclaredThrowableException(e);
                    }
                }
            }

            // Wait for 60 seconds if there were no nodes
            // added to the rescan queue during this loop,
            // otherwise just start over.
            //
            synchronized (this) {
                if (added == 0) {
                    try {
                        wait(60000);
                    } catch (InterruptedException ex) {
                        break; // exit for loop
                    }
                }
            }

        } // end for(;;)

        log().debug("Scheduler.run: scheduler exiting, state = STOPPED");
        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run

}
