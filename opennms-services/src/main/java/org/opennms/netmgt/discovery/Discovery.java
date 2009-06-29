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
// 2003 Oct 21: Fixed typo in variable name.
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
// Tab Size = 8
//

package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.events.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class is the main interface to the OpenNMS discovery service. The class
 * implements the <em>singleton</em> design pattern, in that there is only one
 * instance in any given virtual machine. The service delays the reading of
 * configuration information until the service is started.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * 
 */
@EventListener(name="OpenNMS.Discovery")
public class Discovery extends AbstractServiceDaemon {

    /**
     * The singular instance of the discovery service.
     */
    private static final Discovery m_singleton = new Discovery();

    /**
     * The callback that sends newSuspect events upon successful ping response.
     */
    private static final DiscoveryPingResponseCallback cb = new DiscoveryPingResponseCallback();


    private static final int PING_IDLE = 0;
    private static final int PING_RUNNING = 1;
    private static final int PING_FINISHING = 2;
    
    /**
     * The SQL query used to get the list of managed IP addresses from the database
     */
    private static final String ALL_IP_ADDRS_SQL = "SELECT DISTINCT ipAddr FROM ipInterface WHERE isManaged <> 'D'";
    
    /**
     * a set of devices to skip discovery on
     */
    private Set<String> m_alreadyDiscovered = Collections.synchronizedSet(new HashSet<String>());

    private DiscoveryConfigFactory m_discoveryFactory;

    private Timer m_timer;

    private int m_xstatus = PING_IDLE;

    /**
     * Constructs a new discovery instance.
     */
    private Discovery() {
        super("OpenNMS.Discovery");
    }

    protected void onInit() {

        initializeConfiguration();
        EventIpcManagerFactory.init();
        
        @SuppressWarnings("unused")
        // TODO: Is this doing some kind of wacky initialization?  Or is it ignored?
        AnnotationBasedEventListenerAdapter listener = new AnnotationBasedEventListenerAdapter(this, EventIpcManagerFactory.getIpcManager());

    }

    private void initializeConfiguration() {
        try {
            DiscoveryConfigFactory.reload();
            m_discoveryFactory = DiscoveryConfigFactory.getInstance();

        } catch (Exception e) {
            fatalf(e, "Unable to initialize the discovery configuration factory");
            throw new UndeclaredThrowableException(e);
        }
    }

    protected void doPings() {
        debugf("starting ping sweep");
        initializeConfiguration();


        m_xstatus = PING_RUNNING;

        for (IPPollAddress pollAddress : m_discoveryFactory.getConfiguredAddresses()) {
            if (m_xstatus == PING_FINISHING || m_timer == null) {
                m_xstatus = PING_IDLE;
                return;
            }
            ping(pollAddress);
            try {
                Thread.sleep(m_discoveryFactory.getIntraPacketDelay());
            } catch (InterruptedException e) {
                break;
            }
        }

        debugf("finished discovery sweep");
        m_xstatus = PING_IDLE;
    }

    private void ping(IPPollAddress pollAddress) {
        InetAddress address = pollAddress.getAddress();
        if (address != null) {
            if (!isAlreadyDiscovered(address)) {
                try {
                    Pinger.ping(address, pollAddress.getTimeout(), pollAddress.getRetries(), (short) 1, cb);
                } catch (IOException e) {
                    debugf(e, "error pinging %s", address.getAddress());
                }
            }
        }
    }

    private boolean isAlreadyDiscovered(InetAddress address) {
        if (m_alreadyDiscovered.contains(address.getHostAddress())) {
            return true;
        }
        return false;
    }

    /**
     * Returns the singular instance of the discovery process
     */
    public static Discovery getInstance() {
        return m_singleton;
    }

    private void startTimer() {
        if (m_timer != null) {
            debugf("startTimer() called, but a previous timer exists; making sure it's cleaned up");
            m_xstatus = PING_FINISHING;
            m_timer.cancel();
        }
        
        debugf("scheduling new discovery timer");
        m_timer = new Timer("Discovery.Pinger", true);

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                doPings();
            }

        };
        m_timer.scheduleAtFixedRate(task, m_discoveryFactory.getInitialSleepTime(), m_discoveryFactory.getRestartSleepTime());

    }

    private void stopTimer() {
        if (m_timer != null) {
            debugf("stopping existing timer");
            m_xstatus = PING_FINISHING;
            m_timer.cancel();
            m_timer = null;
        } else {
            debugf("stopTimer() called, but there is no existing timer");
        }
    }

    protected void onStart() {
    	syncAlreadyDiscovered();
        startTimer();
    }

    protected void onStop() {
        stopTimer();
    }

    protected void onPause() {
        stopTimer();
    }

    protected void onResume() {
        startTimer();
    }
    
    protected void syncAlreadyDiscovered() {
    	/**
    	 * Make a new list with which we'll replace the existing one, that way
    	 * if something goes wrong with the DB we won't lose whatever was already
    	 * in there
    	 */
    	Set<String> newAlreadyDiscovered = Collections.synchronizedSet(new HashSet<String>());
    	Connection conn = null;

    	try {
    		conn = DataSourceFactory.getInstance().getConnection();
    		PreparedStatement stmt = conn.prepareStatement(ALL_IP_ADDRS_SQL);
    		ResultSet rs = stmt.executeQuery();
    		if (rs != null) {
    			while (rs.next()) {
    				newAlreadyDiscovered.add(rs.getString(1));
    			}
    			rs.close();
    		} else {
    			log().warn("Got null ResultSet from query for all IP addresses");
    		}
    		stmt.close();
    		m_alreadyDiscovered = newAlreadyDiscovered;
    	} catch (SQLException sqle) {
    		log().warn("Caught SQLException while trying to query for all IP addresses: " + sqle.getMessage());
    	} finally {
    		if (conn != null) {
    			try {
    				conn.close();
    			} catch (SQLException sqle) {
    				log().warn("Caught SQLException while closing DB connection after querying for all IP addresses: " + sqle.getMessage());
    			}
    		}
    	}
    	log().info("syncAlreadyDiscovered initialized list of managed IP addresses with " + m_alreadyDiscovered.size() + " members");
    }

    @EventHandler(uei=EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI)
    public void handleDiscoveryConfigurationChanged(Event event) {
        initializeConfiguration();
        this.stop();
        this.start();
    }

    @EventHandler(uei=EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void handleInterfaceDeleted(Event event) {
        // remove from known nodes
        m_alreadyDiscovered.remove(event.getInterface());

        debugf("Removed %s from known node list", event.getInterface());
    }

    @EventHandler(uei=EventConstants.DISC_RESUME_EVENT_UEI)
    public void handleDiscoveryResume(Event event) {
        try {
            resume();
        } catch (IllegalStateException ex) {
        }
    }

    @EventHandler(uei=EventConstants.DISC_PAUSE_EVENT_UEI)
    public void handleDiscoveryPause(Event event) {
        try {
            pause();
        } catch (IllegalStateException ex) {
        }
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)
    public void handleNodeGainedInterface(Event event) {
        // add to known nodes
        m_alreadyDiscovered.add(event.getInterface());

        debugf("Added %s as discovered", event.getInterface());
    }

}
