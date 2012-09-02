/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.util.Assert;

/**
 * <p>BroadcastEventProcessor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BroadcastEventProcessor implements EventListener {
    private final EventIpcManager m_eventIpcManager;
    private final EventConfDao m_eventConfDao;
    
    /**
     * <p>Constructor for BroadcastEventProcessor.</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     * @param eventConfDao a {@link org.opennms.netmgt.config.EventConfDao} object.
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
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
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
    public void onEvent(Event event) {
        
        log().debug("onEvent: received event, UEI = " + event.getUei());
        EventBuilder ebldr = null;
        
        if (isReloadConfigEvent(event)) {
            try {
                m_eventConfDao.reload();
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Eventd");
                
            } catch (Throwable e) {
                log().error("onEvent: Could not reload events config: " + e, e);
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
            List<Parm> parmCollection = event.getParmCollection();
            
            for (Parm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Eventd".equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }
        
        // Deprecating this one...
        } else if (EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI.equals(event.getUei())) {
            isTarget = true;
        }
        
        return isTarget;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}

