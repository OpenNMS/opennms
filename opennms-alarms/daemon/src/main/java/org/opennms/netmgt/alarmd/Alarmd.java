/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.ThreadAwareEventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Alarm management Daemon
 *
 * TODO: Change this class to use AbstractServiceDaemon instead of SpringServiceDaemon
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@EventListener(name=Alarmd.NAME, logPrefix="alarmd")
public class Alarmd implements SpringServiceDaemon, ThreadAwareEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(Alarmd.class);

    /** Constant <code>NAME="Alarmd"</code> */
    public static final String NAME = "Alarmd";

    protected static final Integer THREADS = Integer.getInteger("org.opennms.alarmd.threads", 4);

    private List<Northbounder> m_northboundInterfaces = new ArrayList<>();

    private volatile boolean m_hasActiveAlarmNbis = false;

    private AlarmPersister m_persister;

    /** The event proxy. */
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /**
     * Listens for all events.
     *
     * This method is thread-safe.
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventHandler.ALL_UEIS)
    public void onEvent(Event e) {
    	if (e.getUei().equals("uei.opennms.org/internal/reloadDaemonConfig")) {
           handleReloadEvent(e);
           return;
    	}

    	// If there are 1+ NBIs we need to invoke, make sure the alarm
        // object that is returned is eagerly loaded (and avoid any
        // LazyInitializationExceptions). Otherwise, we can save resources
        // by not having to load these fields.
        final OnmsAlarm alarm = m_persister.persist(e, m_hasActiveAlarmNbis);

        if (alarm != null && m_hasActiveAlarmNbis) {
            forwardAlarmToNbis(alarm);
        }
    }

    /**
     * Forwards the alarms to the current set of NBIs.
     *
     * This method is synchronized since the event handler is multi-threaded
     * and the NBIs are not necessarily thread safe.
     *
     * @param alarm the alarm to forward
     */
    private synchronized void forwardAlarmToNbis(OnmsAlarm alarm) {
        NorthboundAlarm a = new NorthboundAlarm(alarm);
        for (Northbounder nbi : m_northboundInterfaces) {
            nbi.onAlarm(a);
        }
    }

    private synchronized void handleReloadEvent(Event e) {
        LOG.info("Received reload configuration event: {}", e);

        //Currently, Alarmd has no configuration... I'm sure this will change soon.

        List<Parm> parmCollection = e.getParmCollection();
        for (Parm parm : parmCollection) {

            String parmName = parm.getParmName();
            if ("daemonName".equals(parmName)) {
                if (parm.getValue() == null || parm.getValue().getContent() == null) {
                    LOG.warn("The daemonName parameter has no value, ignoring.");
                    return;
                }

                List<Northbounder> nbis = getNorthboundInterfaces();
                for (Northbounder nbi : nbis) {
                    if (parm.getValue().getContent().contains(nbi.getName())) {
                        LOG.debug("Handling reload event for NBI: {}", nbi.getName());
                        LOG.debug("Reloading NBI configuration for interface {} not yet implemented.", nbi.getName());
                        EventBuilder ebldr = null;
                        try {
                            nbi.reloadConfig();
                            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
                        } catch (NorthbounderException ex) {
                            LOG.error("Can't reload the northbound configuration for " + nbi.getName(), ex);
                            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
                            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
                            ebldr.addParam(EventConstants.PARM_REASON, ex.getMessage());
                        } finally {
                            if (ebldr != null)
                                try {
                                    m_eventProxy.send(ebldr.getEvent());
                                } catch (EventProxyException ep) {
                                    LOG.error("Can't send reload status event", ep);
                                }
                        }
                        return;
                    }
                }
            }
        }
    }

	/**
     * <p>setPersister</p>
     *
     * @param persister a {@link org.opennms.netmgt.alarmd.AlarmPersister} object.
     */
    public void setPersister(AlarmPersister persister) {
        this.m_persister = persister;
    }

    /**
     * <p>getPersister</p>
     *
     * @return a {@link org.opennms.netmgt.alarmd.AlarmPersister} object.
     */
    public AlarmPersister getPersister() {
        return m_persister;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return NAME;
    }

    @Override
    public void start() {
        // pass
    }

    @Override
    public void afterPropertiesSet()  {
        // pass
    }

    @Override
    public synchronized void destroy() {
        // On shutdown, stop all of the NBIs
        m_northboundInterfaces.forEach(nb -> {
            LOG.debug("destroy: stopping {}", nb.getName());
            nb.stop();
        });
    }

    public synchronized void onNorthbounderRegistered(final Northbounder northbounder, final Map<String,String> properties) {
        LOG.debug("onNorthbounderRegistered: starting {}", northbounder.getName());
        northbounder.start();
        m_northboundInterfaces.add(northbounder);
        onNorthboundersChanged();
    }

    public synchronized void onNorthbounderUnregistered(final Northbounder northbounder, final Map<String,String> properties) {
        LOG.debug("onNorthbounderUnregistered: stopping {}", northbounder.getName());
        northbounder.stop();
        m_northboundInterfaces.remove(northbounder);
        onNorthboundersChanged();
    }

    private void onNorthboundersChanged() {
        final long numNbisActive = m_northboundInterfaces.stream()
                .filter(Northbounder::isReady)
                .count();
        m_hasActiveAlarmNbis = numNbisActive > 0;
        LOG.debug("handleNorthboundersChanged: {} out of {} NBIs are currently active.",
                numNbisActive, m_northboundInterfaces.size());
    }

    public synchronized List<Northbounder> getNorthboundInterfaces() {
        return Collections.unmodifiableList(m_northboundInterfaces);
    }

    public synchronized void setNorthboundInterfaces(List<Northbounder> northboundInterfaces) {
        m_northboundInterfaces = northboundInterfaces;
        onNorthboundersChanged();
    }

    @Override
    public int getNumThreads() {
        return THREADS;
    }

}
