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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.vulnscand;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.VulnscandConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

/**
 * <P>
 * Vulnerability scanning daemon. This process is used to provide continual
 * scans of target interfaces that identify possible security holes. The
 * vulnerability scanner that this version uses to identify security flaws is
 * Nessus 1.1.X (www.nessus.org).
 * </P>
 * 
 * <P>
 * This code is adapted from the capsd code because its behavior is quite
 * similar; it continually scans the target ranges, enters the scan results into
 * a database table, and also supports rescans whose threads are pulled from a
 * separate thread pool so that they occur immediately.
 * </P>
 * 
 * @author <A HREF="mailto:seth@opennms.org">Seth Leger </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class Vulnscand extends AbstractServiceDaemon {
    /**
     * The log4j category used to log messages.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Vulnscand";

    /**
     * Singleton instance of the Vulnscand class
     */
    private static final Vulnscand m_singleton = new Vulnscand();

    /**
     * Database synchronization lock for synchronizing write access to the
     * database between the SpecificScanProcessor and RescanProcessor thread
     * pools
     */
    private static Object m_dbSyncLock = new Object();

    /**
     * <P>
     * Contains dotted-decimal representation of the IP address where Vulnscand
     * is running. Used when vulnscand sends events out
     * </P>
     */
    private static String m_address = null;

    /**
     * Rescan scheduler thread
     */
    private Scheduler m_scheduler;

    /**
     * Event receiver.
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * The pool of threads that are used to executed the SpecificScanProcessor
     * instances queued by the event processor (BroadcastEventProcessor).
     */
    private RunnableConsumerThreadPool m_specificScanRunner;

    /**
     * The pool of threads that are used to executed RescanProcessor instances
     * queued by the rescan scheduler thread.
     */
    private RunnableConsumerThreadPool m_scheduledScanRunner;

    /**
     * <P>
     * Static initialization
     * </P>
     */
    static {
        try {
            m_address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhE) {
            m_address = "localhost";
            ThreadCategory.getInstance(LOG4J_CATEGORY).warn("Could not lookup the host name for the local host machine, address set to \"localhost\"", uhE);
        }
    } // end static class initialization

    /**
     * Constructs the Vulnscand objec
     */
    public Vulnscand() {
    	super("OpenNMS.Vulnscand");
        m_scheduler = null;
    }

    protected void onStop() {
		// Stop the broadcast event receiver
        //
        m_receiver.close();

        // Stop the Suspect Event Processor thread pool
        //
        m_specificScanRunner.stop();

        // Stop the Rescan Processor thread pool
        //
        m_scheduledScanRunner.stop();
	}

    protected void onStart() {
		// Initialize the Vulnscand configuration factory.
        //
        try {
            VulnscandConfigFactory.reload();
        } catch (MarshalException ex) {
            log().error("Failed to load Vulnscand configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log().error("Failed to load Vulnscand configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log().error("Failed to load Vulnscand configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // Initialize the Database configuration factory
        //
        try {
            DataSourceFactory.init();
        } catch (IOException ie) {
            log().fatal("IOException loading database config", ie);
            throw new UndeclaredThrowableException(ie);
        } catch (MarshalException me) {
            log().fatal("Marshall Exception loading database config", me);
            throw new UndeclaredThrowableException(me);
        } catch (ValidationException ve) {
            log().fatal("Validation Exception loading database config", ve);
            throw new UndeclaredThrowableException(ve);
        } catch (ClassNotFoundException ce) {
            log().fatal("Class lookup failure loading database config", ce);
            throw new UndeclaredThrowableException(ce);
        } catch (PropertyVetoException e) {
            log().fatal("Property Veto Exception loading database config", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log().fatal("SQL Exception loading database config", e);
            throw new UndeclaredThrowableException(e);
        }
        

        // Create the specific and scheduled scan pools
        //
        m_specificScanRunner = new RunnableConsumerThreadPool("Vulnscand Scan Pool", 0.6f, 1.0f, VulnscandConfigFactory.getInstance().getMaxSuspectThreadPoolSize());

        m_scheduledScanRunner = new RunnableConsumerThreadPool("Vulnscand Rescan Pool", 0.6f, 1.0f, VulnscandConfigFactory.getInstance().getMaxRescanThreadPoolSize());

        // Start the suspect event and rescan thread pools
        //
        if (log().isDebugEnabled())
            log().debug("start: Starting runnable thread pools...");

        m_specificScanRunner.start();
        m_scheduledScanRunner.start();

        // Create and start the rescan scheduler
        //
        if (log().isDebugEnabled())
            log().debug("start: Creating rescan scheduler");
        try {
            // During instantiation, the scheduler will load the
            // list of known nodes from the database
            m_scheduler = new Scheduler(m_scheduledScanRunner.getRunQueue());
        } catch (SQLException sqlE) {
            log().error("Failed to initialize the rescan scheduler.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        } catch (Throwable t) {
            log().error("Failed to initialize the rescan scheduler.", t);
            throw new UndeclaredThrowableException(t);
        }
        m_scheduler.start();

        // Create an event receiver.
        //
        try {
            if (log().isDebugEnabled())
                log().debug("start: Creating event broadcast event receiver");

            m_receiver = new BroadcastEventProcessor(m_specificScanRunner.getRunQueue(), m_scheduler);
        } catch (Throwable t) {
            log().error("Failed to initialized the broadcast event receiver", t);
            throw new UndeclaredThrowableException(t);
        }
	}

    /**
     * Used to retrieve the local host name/address. The name/address of the
     * machine on which Vulnscand is running.
     */
    public static String getLocalHostAddress() {
        return m_address;
    }

    public static Vulnscand getInstance() {
        return m_singleton;
    }

    static Object getDbSyncLock() {
        return m_dbSyncLock;
    }

    protected void onInit() {
    }
} // end Vulnscand class
