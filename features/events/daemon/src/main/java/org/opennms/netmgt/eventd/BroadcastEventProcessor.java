/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>BroadcastEventProcessor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BroadcastEventProcessor implements EventListener {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);
    
    private final EventIpcManager m_eventIpcManager;
    private final EventConfDao m_eventConfDao;
    
    /**
     * <p>Constructor for BroadcastEventProcessor.</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     * @param eventConfDao a {@link org.opennms.netmgt.config.api.EventConfDao} object.
     */
    public BroadcastEventProcessor(EventIpcManager eventIpcManager, EventConfDao eventConfDao) {
        Assert.notNull(eventIpcManager, "argument eventIpcManager must not be null");
        Assert.notNull(eventConfDao, "argument eventConfDao must not be null");
        
        m_eventIpcManager = eventIpcManager;
        m_eventConfDao = eventConfDao;
        
        addEventListener();
    }

    /**
     * Create message selector to set to the subscription
     */
    private void addEventListener() {
        m_eventIpcManager.addEventListener(this, EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI);
        m_eventIpcManager.addEventListener(this, EventConstants.RELOAD_DAEMON_CONFIG_UEI);
    }

    /**
     * </p>
     * Closes the current connections to the event manager if they are
     * still active. This call may be invoked more than once safely and may be
     * invoked during object finalization.
     * </p>
     */
    public synchronized void close() {
        m_eventIpcManager.removeEventListener(this);
    }

    /**
     * This method may be invoked by the garbage thresholding. Once invoked it
     * ensures that the <code>close</code> method is called <em>at least</em>
     * once during the cycle of this object.
     *
     * @throws java.lang.Throwable if any.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Eventd:BroadcastEventProcessor";
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the event manager when a new event is
     * available for processing.  Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     */
    @Override
    public void onEvent(Event event) {
        
        LOG.debug("onEvent: received event, UEI = {}", event.getUei());
        EventBuilder ebldr = null;
        
        if (isReloadConfigEvent(event)) {
            LOG.info("onEvent: Reloading events configuration in response to event with UEI " + event.getUei());
            try {
                m_eventConfDao.reload();
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Eventd");
                
            } catch (Throwable e) {
                LOG.error("onEvent: Could not reload events config", e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Eventd");
                ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
            }
            
            if (ebldr != null) {
                m_eventIpcManager.sendNow(ebldr.getEvent());
            }
        }
    }

    private boolean isReloadConfigEvent(Event event) {
        boolean isTarget = false;
        
        if (EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {
            final Parm target = event.getParm(EventConstants.PARM_DAEMON_NAME);
            if (target != null && "Eventd".equalsIgnoreCase(target.getValue().getContent())) {
                isTarget = true;
            }
        // Deprecating this one...
        } else if (EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI.equals(event.getUei())) {
            isTarget = true;
        }
        
        return isTarget;
    }
}

