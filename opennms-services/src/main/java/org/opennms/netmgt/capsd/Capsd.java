/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.events.StoppableEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
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
 */
public class Capsd extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Capsd.class);
    
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
    @Autowired
    private Scheduler m_scheduler;
    
    /**
     * Event receiver.
     */
    private StoppableEventListener m_eventListener;

    /**
     * The pool of threads that are used to executed the SuspectEventProcessor
     * instances queued by the event processor (BroadcastEventProcessor).
     */
    private ExecutorService m_suspectRunner;

    /**
     * The pool of threads that are used to executed RescanProcessor instances
     * queued by the rescan scheduler thread.
     */
    private ExecutorService m_rescanRunner;

    /*
     * Injected properties, the should be asserted in onInit
     */
    @Autowired
    private SuspectEventProcessorFactory m_suspectEventProcessorFactory;

    @Autowired
    private CapsdDbSyncer m_capsdDbSyncer;

    /**
     * <P>
     * Static initialization
     * </P>
     */

    static {
    	m_address = InetAddressUtils.getLocalHostAddressAsString();
    } // end static class initialization

    /**
     * Constructs the Capsd objec
     */
    public Capsd() {
    	super("capsd");
        m_scheduler = null;
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        // System.err.println("Capsd onStop() dumping stack");
        // Thread.dumpStack();

        // Stop the broadcast event receiver
        m_eventListener.stop();

        // Stop the Suspect Event Processor thread pool
        m_suspectRunner.shutdown();

        // Stop the Rescan Processor thread pool
        m_rescanRunner.shutdown();

        if (m_scheduler != null) m_scheduler.stop();
	}

	/**
	 * <p>onInit</p>
	 */
    @Override
	protected void onInit() {
        BeanUtils.assertAutowiring(this);

        Assert.state(m_suspectRunner != null, "must set the suspectRunner property");
        Assert.state(m_rescanRunner != null, "must set the rescanRunner property");
        Assert.state(m_eventListener != null, "must set the eventListener property");

        if (System.getProperty("org.opennms.provisiond.enableDiscovery", "true").equalsIgnoreCase("true")) {
        	throw new IllegalStateException("Provisiond is configured to handle discovery events. " +
        			"Please disable Capsd in service-configuration.xml, or set " +
        			"org.opennms.provisiond.enableDiscovery=false in opennms.properties!");
        }

	    
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

        LOG.debug("init: Loading services into database...");
        m_capsdDbSyncer.syncServices();
        
        LOG.debug("init: Syncing management state...");
        m_capsdDbSyncer.syncManagementState();
        
        LOG.debug("init: Syncing primary SNMP interface state...");
        m_capsdDbSyncer.syncSnmpPrimaryState();

	}

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        // System.err.println("Capsd onStart() dumping stack");
        // Thread.dumpStack();

    	// Set the Set that SuspectEventProcessor will use to track
    	// suspect scans that are in progress
    	SuspectEventProcessor.setQueuedSuspectsTracker(new HashSet<String>());
    	
    	// Likewise, a separate Set for the RescanProcessor
    	RescanProcessor.setQueuedRescansTracker(new HashSet<Integer>());
    	
        // Start the rescan scheduler
        LOG.debug("start: Starting rescan scheduler");
        
        m_scheduler.start();
	}

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        // XXX Pause all threads?
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        // XXX Resume all threads?
	}



    /**
     * Used to retrieve the local host name/address. The name/address of the
     * machine on which Capsd is running.
     *
     * @return a {@link java.lang.String} object.
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
     * @throws java.net.UnknownHostException
     *             Thrown if the address cannot be converted to aa proper
     *             internet address.
     */
    public void scanSuspectInterface(final String ifAddr) throws UnknownHostException {
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                final InetAddress addr = InetAddressUtils.addr(ifAddr);
                final SuspectEventProcessor proc = m_suspectEventProcessorFactory.createSuspectEventProcessor(InetAddressUtils.str(addr));
                proc.run();
            }
            
        });
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
    public void rescanInterfaceParent(final Integer nodeId) {
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                m_scheduler.forceRescan(nodeId.intValue());
            }
            
        });
    }

    /**
     * <p>setSuspectRunner</p>
     *
     * @param suspectRunner a {@link java.util.concurrent.ExecutorService} object.
     */
    public void setSuspectRunner(ExecutorService suspectRunner) {
        m_suspectRunner = suspectRunner;
    }

    /**
     * <p>setRescanRunner</p>
     *
     * @param rescanRunner a {@link java.util.concurrent.ExecutorService} object.
     */
    public void setRescanRunner(ExecutorService rescanRunner) {
        m_rescanRunner = rescanRunner;
    }

    /**
     * <p>setEventListener</p>
     *
     * @param eventListener a {@link org.opennms.netmgt.model.events.StoppableEventListener} object.
     */
    public void setEventListener(StoppableEventListener eventListener) {
        m_eventListener = eventListener;
    }

} // end Capsd class

