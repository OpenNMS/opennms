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
// 2008 Jan 06: Convert to use EventConfDao instead of
//              EventConfigurationManager. - dj@opennms.org
// 2007 Mar 21: Format code, remove outdated references to JMS, create log()
//      method, and keep around the EventIpcManager when we are instantiated
//      so we can use it when we close up shop. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.eventd;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.util.Assert;

public class BroadcastEventProcessor implements EventListener {
    private final EventIpcManager m_eventIpcManager;
    private final EventConfDao m_eventConfDao;
    
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
     * 
     */
    public synchronized void close() {
        m_eventIpcManager.removeEventListener(this);
    }

    /**
     * This method may be invoked by the garbage thresholding. Once invoked it
     * ensures that the <code>close</code> method is called <em>at least</em>
     * once during the cycle of this object.
     * 
     */
    protected void finalize() throws Throwable {
        close();
    }

    public String getName() {
        return "Eventd:BroadcastEventProcessor";
    }

    /**
     * This method is invoked by the event manager when a new event is
     * available for processing.  Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     * 
     * @param event
     *            The event message.
     * 
     */
    public void onEvent(Event event) {
        
        log().debug("onEvent: received event, UEI = " + event.getUei());
        EventBuilder ebldr = null;
        
        if (isReloadConfigEvent(event)) {
            try {
                m_eventConfDao.reload();
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Eventd");
                
            } catch (Exception e) {
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
            List<Parm> parmCollection = event.getParms().getParmCollection();
            
            for (Parm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Eventd".equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }
        
        //Depreciating this one...
        } else if (EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI.equals(event.getUei())) {
            isTarget = true;
        }
        
        return isTarget;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}

