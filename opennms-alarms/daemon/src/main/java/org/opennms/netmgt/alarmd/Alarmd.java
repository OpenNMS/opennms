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

import org.opennms.netmgt.alarmd.drools.DroolsAlarmContext;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.ThreadAwareEventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alarm management Daemon
 *
 * @author jwhite
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@EventListener(name=Alarmd.NAME, logPrefix="alarmd")
public class Alarmd extends AbstractServiceDaemon implements ThreadAwareEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(Alarmd.class);

    /** Constant <code>NAME="alarmd"</code> */
    public static final String NAME = "alarmd";

    protected static final Integer THREADS = Integer.getInteger("org.opennms.alarmd.threads", 4);

    private AlarmPersister m_persister;

    @Autowired
    private AlarmLifecycleListenerManager m_alm;

    @Autowired
    private DroolsAlarmContext m_droolsAlarmContext;

    @Autowired
    private NorthbounderManager m_northbounderManager;

    public Alarmd() {
        super(NAME);
    }

    /**
     * Listens for all events.
     *
     * This method is thread-safe.
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventHandler.ALL_UEIS)
    public void onEvent(Event e) {
    	if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_UEI)) {
           handleReloadEvent(e);
           return;
    	}
    	m_persister.persist(e);
    }

    private synchronized void handleReloadEvent(Event e) {
        m_northbounderManager.handleReloadEvent(e);
        DaemonTools.handleReloadEvent(e, Alarmd.NAME, (event) -> onAlarmReload());
    }

    private void onAlarmReload() {
        m_droolsAlarmContext.reload();
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

    @Override
    protected synchronized void onInit() {
        // pass
    }

    @Override
    public synchronized void onStart() {
        // Start the Drools context
        m_droolsAlarmContext.start();
    }

    @Override
    public synchronized void onStop() {
        // Stop the northbound interfaces
        m_northbounderManager.stop();
        // Stop the Drools context
        m_droolsAlarmContext.stop();
    }

    @Override
    public int getNumThreads() {
        return THREADS;
    }

}
