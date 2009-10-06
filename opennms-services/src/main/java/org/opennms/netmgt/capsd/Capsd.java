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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.events.StoppableEventListener;
import org.springframework.util.Assert;

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
     * Event receiver.
     */
    private StoppableEventListener m_eventListener;

    /**
     * The pool of threads that are used to executed the SuspectEventProcessor
     * instances queued by the event processor (BroadcastEventProcessor).
     */
    private RunnableConsumerThreadPool m_suspectRunner;

    /**
     * The pool of threads that are used to executed RescanProcessor instances
     * queued by the rescan scheduler thread.
     */
    private RunnableConsumerThreadPool m_rescanRunner;

    /*
     * Injected properties, the should be asserted in onInit
     */
    private SuspectEventProcessorFactory m_suspectEventProcessorFactory;

    private CapsdDbSyncer m_capsdDbSyncer;
    
    private CapsdConfig m_capsdConfig;

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
        m_eventListener.stop();

        // Stop the Suspect Event Processor thread pool
        m_suspectRunner.stop();

        // Stop the Rescan Processor thread pool
        m_rescanRunner.stop();
	}

	protected void onInit() {
	    
        Assert.state(m_suspectEventProcessorFactory != null, "must set the suspectEventProcessorFactory property");
        Assert.state(m_capsdDbSyncer != null, "must set the capsdDbSyncer property");
        Assert.state(m_capsdConfig != null, "must set the capsdConfig property");
        Assert.state(m_suspectRunner != null, "must set the suspectRunner property");
        Assert.state(m_rescanRunner != null, "must set the rescanRunner property");
        Assert.state(m_scheduler != null, "must set the scheduler property");
        Assert.state(m_eventListener != null, "must set the eventListener property");

	    
	    /* 
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

	}

    protected void onStart() {
    	// Set the Set that SuspectEventProcessor will use to track
    	// suspect scans that are in progress
    	SuspectEventProcessor.setQueuedSuspectsTracker(new HashSet<String>());
    	
    	// Likewise, a separate Set for the RescanProcessor
    	RescanProcessor.setQueuedRescansTracker(new HashSet<Integer>());
    	
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
            SuspectEventProcessor proc = m_suspectEventProcessorFactory.createSuspectEventProcessor(addr.getHostAddress());
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

    private CapsdDbSyncer getCapsdDbSyncer() {
        return m_capsdDbSyncer;
    }

    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

    public void setSuspectEventProcessorFactory(SuspectEventProcessorFactory eventProcessorFactory) {
        m_suspectEventProcessorFactory = eventProcessorFactory;
    }
    
    public void setCapsdConfig(CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }

    public void setSuspectRunner(RunnableConsumerThreadPool suspectRunner) {
        m_suspectRunner = suspectRunner;
    }

    public void setRescanRunner(RunnableConsumerThreadPool rescanRunner) {
        m_rescanRunner = rescanRunner;
    }

    public void setEventListener(StoppableEventListener eventListener) {
        m_eventListener = eventListener;
    }

    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

} // end Capsd class

