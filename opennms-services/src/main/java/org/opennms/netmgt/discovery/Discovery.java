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

package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.util.Assert;

/**
 * This class is the main interface to the OpenNMS discovery service. The class
 * implements the <em>singleton</em> design pattern, in that there is only one
 * instance in any given virtual machine. The service delays the reading of
 * configuration information until the service is started.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
@EventListener(name="OpenNMS.Discovery")
public class Discovery extends AbstractServiceDaemon {

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
    
    private volatile EventForwarder m_eventForwarder;

    private Pinger m_pinger;

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>setPinger</p>
     *
     * @param pinger a {@link JniPinger} object.
     */
    public void setPinger(Pinger pinger) {
        m_pinger = pinger;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    
    /**
     * <p>setDiscoveryFactory</p>
     *
     * @param discoveryFactory a {@link org.opennms.netmgt.config.DiscoveryConfigFactory} object.
     */
    public void setDiscoveryFactory(DiscoveryConfigFactory discoveryFactory) {
        m_discoveryFactory = discoveryFactory;
    }

    /**
     * <p>getDiscoveryFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.DiscoveryConfigFactory} object.
     */
    public DiscoveryConfigFactory getDiscoveryFactory() {
        return m_discoveryFactory;
    }

    /**
     * Constructs a new discovery instance.
     */
    public Discovery() {
        super("OpenNMS.Discovery");
    }

    /**
     * <p>onInit</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    protected void onInit() throws IllegalStateException {

        Assert.state(m_eventForwarder != null, "must set the eventForwarder property");
        
        try {
            initializeConfiguration();
            EventIpcManagerFactory.init();
        } catch (Throwable e) {
            log().debug("onInit: initialization failed: "+e, e);
            throw new IllegalStateException("Could not initialize discovery configuration.", e);
        }
    }

    private void initializeConfiguration() throws MarshalException, ValidationException, IOException {
        DiscoveryConfigFactory.reload();
        setDiscoveryFactory(DiscoveryConfigFactory.getInstance());
    }

    private void doPings() {
        infof("starting ping sweep");
        
        try {
            initializeConfiguration();
        } catch (Throwable e) {
            log().error("doPings: could not re-init configuration, continuing with in memory configuration."+e, e);
        }


        m_xstatus = PING_RUNNING;

        getDiscoveryFactory().getReadLock().lock();
        try {
            for (IPPollAddress pollAddress : getDiscoveryFactory().getConfiguredAddresses()) {
                if (m_xstatus == PING_FINISHING || m_timer == null) {
                    m_xstatus = PING_IDLE;
                    return;
                }
                ping(pollAddress);
                try {
                    Thread.sleep(getDiscoveryFactory().getIntraPacketDelay());
                } catch (InterruptedException e) {
                    infof("interrupting discovery sweep");
                    break;
                }
            }
        } finally {
            getDiscoveryFactory().getReadLock().unlock();
        }

        infof("finished discovery sweep");
        m_xstatus = PING_IDLE;
    }

    private void ping(IPPollAddress pollAddress) {
        InetAddress address = pollAddress.getAddress();
        if (address != null) {
            if (!isAlreadyDiscovered(address)) {
                try {
                    m_pinger.ping(address, pollAddress.getTimeout(), pollAddress.getRetries(), (short) 1, cb);
                } catch (Throwable e) {
                    debugf(e, "error pinging %s", address.getAddress());
                }
            }
        }
    }

    private boolean isAlreadyDiscovered(InetAddress address) {
        if (m_alreadyDiscovered.contains(InetAddressUtils.str(address))) {
            return true;
        }
        return false;
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
        final Lock readLock = getDiscoveryFactory().getReadLock();
        readLock.lock();
        try {
            m_timer.scheduleAtFixedRate(task, getDiscoveryFactory().getInitialSleepTime(), getDiscoveryFactory().getRestartSleepTime());
        } finally {
            readLock.unlock();
        }
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

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
    	syncAlreadyDiscovered();
        startTimer();
    }

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
        stopTimer();
    }

    /**
     * <p>onPause</p>
     */
    protected void onPause() {
        stopTimer();
    }

    /**
     * <p>onResume</p>
     */
    protected void onResume() {
        startTimer();
    }
    
    /**
     * <p>syncAlreadyDiscovered</p>
     */
    protected void syncAlreadyDiscovered() {
    	/**
    	 * Make a new list with which we'll replace the existing one, that way
    	 * if something goes wrong with the DB we won't lose whatever was already
    	 * in there
    	 */
    	Set<String> newAlreadyDiscovered = Collections.synchronizedSet(new HashSet<String>());
    	Connection conn = null;
        final DBUtils d = new DBUtils(getClass());

    	try {
    		conn = DataSourceFactory.getInstance().getConnection();
    		d.watch(conn);
    		PreparedStatement stmt = conn.prepareStatement(ALL_IP_ADDRS_SQL);
    		d.watch(stmt);
    		ResultSet rs = stmt.executeQuery();
    		d.watch(rs);
    		if (rs != null) {
    			while (rs.next()) {
    				newAlreadyDiscovered.add(rs.getString(1));
    			}
    		} else {
    			log().warn("Got null ResultSet from query for all IP addresses");
    		}
    		m_alreadyDiscovered = newAlreadyDiscovered;
    	} catch (SQLException sqle) {
    		log().warn("Caught SQLException while trying to query for all IP addresses: " + sqle.getMessage());
    	} finally {
    	    d.cleanUp();
    	}
    	log().info("syncAlreadyDiscovered initialized list of managed IP addresses with " + m_alreadyDiscovered.size() + " members");
    }

    /**
     * <p>handleDiscoveryConfigurationChanged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI)
    public void handleDiscoveryConfigurationChanged(Event event) {
        log().info("handleDiscoveryConfigurationChanged: handling message that a change to configuration happened...");
        reloadAndReStart();
    }

    private void reloadAndReStart() {
        EventBuilder ebldr = null;
        try {
            initializeConfiguration();
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            this.stop();
            this.start();
        } catch (MarshalException e) {
            fatalf(e, "Unable to initialize the discovery configuration factory");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        } catch (ValidationException e) {
            fatalf(e, "Unable to initialize the discovery configuration factory");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        } catch (IOException e) {
            fatalf(e, "Unable to initialize the discovery configuration factory");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        }
        m_eventForwarder.sendNow(ebldr.getEvent());
    }
    
    /**
     * <p>reloadDaemonConfig</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void reloadDaemonConfig(Event e) {
        log().info("reloadDaemonConfig: processing reload daemon event...");
        if (isReloadConfigEventTarget(e)) {
            reloadAndReStart();
        }
        log().info("reloadDaemonConfig: reload daemon event processed.");
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        final List<Parm> parmCollection = event.getParmCollection();

        for (final Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Discovery".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: discovery was target of reload event: "+isTarget);
        return isTarget;
    }

    /**
     * <p>handleInterfaceDeleted</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.INTERFACE_DELETED_EVENT_UEI)
    public void handleInterfaceDeleted(final Event event) {
        if(event.getInterface() != null) {
            // remove from known nodes
            final String iface = event.getInterface();
			m_alreadyDiscovered.remove(iface);

            debugf("Removed %s from known node list", iface);
        }
    }

    /**
     * <p>handleDiscoveryResume</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DISC_RESUME_EVENT_UEI)
    public void handleDiscoveryResume(Event event) {
        try {
            resume();
        } catch (IllegalStateException ex) {
        }
    }

    /**
     * <p>handleDiscoveryPause</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DISC_PAUSE_EVENT_UEI)
    public void handleDiscoveryPause(Event event) {
        try {
            pause();
        } catch (IllegalStateException ex) {
        }
    }

    /**
     * <p>handleNodeGainedInterface</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)
    public void handleNodeGainedInterface(Event event) {
        // add to known nodes
        final String iface = event.getInterface();
		m_alreadyDiscovered.add(iface);

        debugf("Added %s as discovered", iface);
    }

}
