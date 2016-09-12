/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

/**
 * This class is the main interface to the OpenNMS discovery service. The service 
 * delays the reading of configuration information until the service is started.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
@EventListener(name="OpenNMS.Discovery", logPrefix="discovery")
public class Discovery extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Discovery.class);
    
    /**
     * The callback that sends newSuspect events upon successful ping response.
     */
    private static final DiscoveryPingResponseCallback cb = new DiscoveryPingResponseCallback();

    private static final String LOG4J_CATEGORY = "discovery";

    private DiscoveryConfigFactory m_discoveryFactory;

    private EventForwarder m_eventForwarder;

    @Autowired
    @Qualifier ("discoveryContext")
    private CamelContext m_camelContext;

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventForwarder} object.
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
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    protected void onInit() throws IllegalStateException {

        Assert.state(m_eventForwarder != null, "must set the eventForwarder property");
        
        //Wiring doesn't seem to be working.
        Assert.state(m_discoveryFactory != null, "must set the Discovery Factory property");
        cb.setDiscoveryFactory(m_discoveryFactory);
        
        try {
        	LOG.debug("Initializing configuration...");
        	m_discoveryFactory.reload();
        	LOG.debug("Configuration initialized.  Init the factory...");
            EventIpcManagerFactory.init();
        	LOG.debug("Factory init'd.");
        } catch (Throwable e) {
            LOG.debug("onInit: initialization failed", e);
            throw new IllegalStateException("Could not initialize discovery configuration.", e);
        }

        // Reduce the shutdown timeout for the Camel context
        m_camelContext.getShutdownStrategy().setTimeout(5);
        m_camelContext.getShutdownStrategy().setTimeUnit(TimeUnit.SECONDS);
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        try {
            m_camelContext.start();
        } catch (Exception e) {
            LOG.error("Discovery startup failed: " + e.getMessage(), e);
        }
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        try {
            m_camelContext.stop();
        } catch (Exception e) {
            LOG.error("Discovery shutdown failed: " + e.getMessage(), e);
        }
    }

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        try {
            m_camelContext.stop();
        } catch (Exception e) {
            LOG.error("Discovery pause failed: " + e.getMessage(), e);
        }
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        try {
            m_camelContext.start();
        } catch (Exception e) {
            LOG.error("Discovery resume failed: " + e.getMessage(), e);
        }
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
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            this.stop();
            this.start();
        } catch (MarshalException e) {
            LOG.error("Unable to initialize the discovery configuration factory", e);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        } catch (ValidationException e) {
            LOG.error("Unable to initialize the discovery configuration factory", e);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Discovery");
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        } catch (IOException e) {
            LOG.error("Unable to initialize the discovery configuration factory", e);
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
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Discovery".equalsIgnoreCase(parm.getValue().getContent())) {
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

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }
}
