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
// 2007 May 06: Moved plugin management out of CapsdConfigManager. - dj@opennms.org
// 2003 Oct 08: Changed thread poll water marks.
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
// Tab Stop = 8
//

package org.opennms.netmgt.capsd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

/**
 * <P>
 * The Capability daemon - it is notified by the discovery process when a new
 * node is discovered - it then polls for all the capabilities for this node and
 * is responsible for loading the data collecte1d into the database.
 * </P>
 * 
 * <P>
 * Once a node is added to the database, its sends an indication back to the
 * discovery which then flags this node as 'known'.
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class Capsd extends AbstractServiceDaemon {
    /**
     * The log4j category used to log messages.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Capsd";

    /**
     * Database synchronization lock for synchronizing write access to the
     * database between the SuspectEventProcessor and RescanProcessor thread
     * pools
     */
    private static Object m_dbSyncLock = new Object();

    /**
     * <P>
     * Contains dotted-decimal representation of the IP address where Capsd is
     * running. Used when capsd sends events out
     * </P>
     */
    private static String m_address = null;

    /**
     * Rescan scheduler thread
     */
    private Scheduler m_scheduler;
    
    /**
     * Factory for creating event processors
     */
    private EventProcessorFactory m_eventProcessorFactory;

    /**
     * Event receiver.
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * The pool of threads that are used to executed the SuspectEventProcessor
     * instances queued by the event processor (BroadcastEventProcessor).
     */
    private RunnableConsumerThreadPool<SuspectEventProcessor> m_suspectRunner;

    /**
     * The pool of threads that are used to executed RescanProcessor instances
     * queued by the rescan scheduler thread.
     */
    private RunnableConsumerThreadPool<RescanProcessor> m_rescanRunner;

    private PluginManager m_pluginManager;
    
    private CapsdDbSyncer m_capsdDbSyncer;

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
            ThreadCategory.getInstance(LOG4J_CATEGORY).warn("Could not lookup the host name for the local host machine, address set to localhost", uhE);
        }
    } // end static class initialization

    /**
     * Constructs the Capsd objec
     */
    public Capsd() {
    	super("OpenNMS.Capsd");
        m_scheduler = null;
    }

    protected void onStop() {
		// Stop the broadcast event receiver
        m_receiver.close();

        // Stop the Suspect Event Processor thread pool
        m_suspectRunner.stop();

        // Stop the Rescan Processor thread pool
        m_rescanRunner.stop();
	}

	protected void onInit() {
        /*
         * Get connection to the database and use it to sync the
         * content of the database with the latest configuration
         * information.
         * 
         * First any new services are added to the services table
         * with a call to syncServices().
         *
         * Secondly the management state of interfaces and services
         * in the database is updated based on the latest configuration
         * information with a call to syncManagementState()
         *
         * Lastly the primary snmp interface state ('isSnmpPrimary')
         * of all interfaces which support SNMP is updated based on
         * the latest configuration information via a call to
         * syncSnmpPrimaryState()
         */

        log().debug("init: Loading services into database...");
        getCapsdDbSyncer().syncServices();
        
        log().debug("init: Syncing management state...");
        getCapsdDbSyncer().syncManagementState();
        
        log().debug("init: Syncing primary SNMP interface state...");
        getCapsdDbSyncer().syncSnmpPrimaryState();     

        // Create the suspect event and rescan thread pools
        m_suspectRunner = new RunnableConsumerThreadPool<SuspectEventProcessor>("Capsd Suspect Pool", 0.0f, 0.0f, CapsdConfigFactory.getInstance().getMaxSuspectThreadPoolSize());

        /*
         * Only stop thread if nothing in queue.
         * Always start thread if queue is not
         * empty and max threads has not been reached.
         */
        m_rescanRunner = new RunnableConsumerThreadPool<RescanProcessor>("Capsd Rescan Pool",
							0.0f, 0.0f,
                CapsdConfigFactory.getInstance().getMaxRescanThreadPoolSize());

        // Create the rescan scheduler
        if (log().isDebugEnabled()) {
            log().debug("init: Creating rescan scheduler");
        }
        try {
            /*
             * During instantiation, the scheduler will load the
             * list of known nodes from the database.
             */
            m_scheduler = new Scheduler(getCapsdDbSyncer(), m_pluginManager, m_rescanRunner.getRunQueue());
        } catch (SQLException sqlE) {
            log().error("Failed to initialize the rescan scheduler.", sqlE);
            throw new UndeclaredThrowableException(sqlE);
        } catch (Throwable t) {
            log().error("Failed to initialize the rescan scheduler.", t);
            throw new UndeclaredThrowableException(t);
        }

        // Create an event receiver.
        log().debug("init: Creating event broadcast event receiver");
        try {
            m_receiver = new BroadcastEventProcessor(m_suspectRunner.getRunQueue(), m_scheduler, m_eventProcessorFactory);
        } catch (Throwable t) {
            log().error("Failed to initialized the broadcast event receiver", t);
            throw new UndeclaredThrowableException(t);
        }
	}

    protected void onStart() {
		// Start the suspect event and rescan thread pools
        log().debug("start: Starting runnable thread pools...");

        m_suspectRunner.start();
        m_rescanRunner.start();

        // Start the rescan scheduler
        log().debug("start: Starting rescan scheduler");
        
        m_scheduler.start();
	}

    protected void onPause() {
        // XXX Pause all threads?
    }

    protected void onResume() {
        // XXX Resume all threads?
	}



    /**
     * Used to retrieve the local host name/address. The name/address of the
     * machine on which Capsd is running.
     */
    public static String getLocalHostAddress() {
        return m_address;
    }

    static Object getDbSyncLock() {
        return m_dbSyncLock;
    }

    /**
     * This method is used by other managed beans to forward an IP Address for
     * capability scanning. The If the interface converts properly then it is
     * scanned as a suspect interface for the discovery of all the services and
     * other interfaces that exists on the node.
     * 
     * @param ifAddr
     *            The address of the suspect interface.
     * 
     * @throws java.net.UnknownHostException
     *             Thrown if the address cannot be converted to aa proper
     *             internet address.
     */
    public void scanSuspectInterface(String ifAddr) throws UnknownHostException {
        String prefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(getName());
            InetAddress addr = InetAddress.getByName(ifAddr);
            SuspectEventProcessor proc = m_eventProcessorFactory.createSuspectEventProcessor(addr.getHostAddress());
            proc.run();
        } finally {
            ThreadCategory.setPrefix(prefix);
        }
    }

    /**
     * This method is used to force an existing node to be capability rescanned.
     * The main reason for its existence is as a hook for JMX managed beans to
     * invoke forced rescans allowing the main rescan logic to remain in the
     * capsd agent.
     * 
     * @param nodeId
     *            The node identifier from the database.
     */
    public void rescanInterfaceParent(Integer nodeId) {
        String prefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(getName());
            m_scheduler.forceRescan(nodeId.intValue());
        } finally {
            ThreadCategory.setPrefix(prefix);
        }
    }

    public void setPluginManager(PluginManager pluginManager) {
        m_pluginManager = pluginManager;
    }

    private CapsdDbSyncer getCapsdDbSyncer() {
        return m_capsdDbSyncer;
    }

    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

    public void setEventProcessorFactory(EventProcessorFactory eventProcessorFactory) {
        m_eventProcessorFactory = eventProcessorFactory;
    }

} // end Capsd class

