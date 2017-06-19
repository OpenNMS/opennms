/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This class is the main interface to the OpenNMS discovery service. The service 
 * delays the reading of configuration information until the service is started.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
@EventListener(name=Discovery.DAEMON_NAME, logPrefix=Discovery.LOG4J_CATEGORY)
public class Discovery extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Discovery.class);

    protected static final String DAEMON_NAME = "Discovery";

    protected static final String LOG4J_CATEGORY = "discovery";

    @Autowired
    private DiscoveryConfigFactory m_discoveryFactory;

    @Autowired
    private DiscoveryTaskExecutor m_discoveryTaskExecutor;

    @Autowired
    @Qualifier("eventIpcManager")
    private EventForwarder m_eventForwarder;

    private Timer discoveryTimer;

    /**
     * Constructs a new discovery instance.
     */
    public Discovery() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    protected void onInit() throws IllegalStateException {
        Objects.requireNonNull(m_eventForwarder, "must set the eventForwarder property");
        Objects.requireNonNull(m_discoveryTaskExecutor, "must set the discoveryTaskExecutor property");
        Objects.requireNonNull(m_discoveryFactory, "must set the discoveryFactory property");

        try {
        	LOG.debug("Initializing configuration...");
        	m_discoveryFactory.reload();
        } catch (Throwable e) {
            LOG.debug("onInit: initialization failed", e);
            throw new IllegalStateException("Could not initialize discovery configuration.", e);
        }
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected synchronized void onStart() {
        if (discoveryTimer != null) {
            LOG.warn("Discovery is already started.");
            return;
        }

        discoveryTimer = new Timer(DAEMON_NAME);
        discoveryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOG.info("Discovery triggered by timer.");
                try {
                    m_discoveryTaskExecutor.handleDiscoveryTask(m_discoveryFactory.getConfiguration()).whenComplete((result, ex) -> {
                        LOG.info("Discovery completed succesfully.");
                    }).join();
                } catch (Throwable t) {
                    LOG.error("Discovery failed. Will try again in {} ms", m_discoveryFactory.getRestartSleepTime(), t);
                }
            }
        }, m_discoveryFactory.getInitialSleepTime(), m_discoveryFactory.getRestartSleepTime());
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected synchronized void onStop() {
        if (discoveryTimer == null) {
            LOG.warn("Discovery is already stopped.");
            return;
        }

        discoveryTimer.cancel();
        discoveryTimer = null;
    }

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        onStop();
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        onStart();
    }

    /**
     * <p>handleDiscoveryConfigurationChanged</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI)
    public void handleDiscoveryConfigurationChanged(Event event) {
        LOG.info("handleDiscoveryConfigurationChanged: handling message that a change to configuration happened...");
        reloadAndReStart();
    }

    private void reloadAndReStart() {
        EventBuilder ebldr = null;
        try {
            m_discoveryFactory.reload();
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
            this.stop();
            this.start();
        } catch (IOException e) {
            LOG.error("Unable to initialize the discovery configuration factory", e);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
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
        LOG.info("reloadDaemonConfig: processing reload daemon event...");
        if (isReloadConfigEventTarget(e)) {
            reloadAndReStart();
        }
        LOG.info("reloadDaemonConfig: reload daemon event processed.");
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        final List<Parm> parmCollection = event.getParmCollection();

        for (final Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && DAEMON_NAME.equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        LOG.debug("isReloadConfigEventTarget: discovery was target of reload event: {}", isTarget);
        return isTarget;
    }

    /**
     * <p>handleDiscoveryResume</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DISC_RESUME_EVENT_UEI)
    public void handleDiscoveryResume(Event event) {
        resume();
    }

    /**
     * <p>handleDiscoveryPause</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.DISC_PAUSE_EVENT_UEI)
    public void handleDiscoveryPause(Event event) {
        pause();
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public void setDiscoveryFactory(DiscoveryConfigFactory discoveryFactory) {
        m_discoveryFactory = discoveryFactory;
    }

    public void setDiscoveryTaskExecutor(DiscoveryTaskExecutor discoveryTaskExecutor) {
        m_discoveryTaskExecutor = discoveryTaskExecutor;
    }
}
