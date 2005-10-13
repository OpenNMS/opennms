//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.virtual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.PassiveStatusConfigFactory;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.xml.event.Event;

public class PassiveStatusKeeper extends ServiceDaemon implements EventListener {
    
    private static Map m_statusTable = null;
    private static boolean m_initialized = false;
    private static PassiveStatusKeeper m_instance = new PassiveStatusKeeper();

    public PassiveStatusKeeper() {
        createMessageSelectorAndSubscribe();
    }
    public static PassiveStatusKeeper getInstance() {
        if (!m_initialized) {
            throw new IllegalStateException("PassiveStatusKeeper has not been initialized.");
        }
        return m_instance;
    }

    private EventIpcManager m_eventMgr;
    
    public void init() {
        if (!m_initialized) {
            m_statusTable = new HashMap();
            m_initialized = true;
            setStatus(START_PENDING);
        }
    }

    public void start() {
        setStatus(RUNNING);
    }

    public void stop() {
        setStatus(STOP_PENDING);
        m_statusTable = null;
        setStatus(STOPPED);
    }

    public String getName() {
        return "OpenNMS.PassiveStatusKeeper";    }

    public void pause() {
        setStatus(PAUSED);
    }

    public void resume() {
        setStatus(RESUME_PENDING);
    }

    public void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        m_statusTable.put(nodeLabel+":"+ipAddr+":"+svcName, pollStatus);
    }

    public Object getStatus(String nodeLabel, String ipAddr, String svcName) {
        //FIXME: Throw a log or exception here if this method is called and the this class hasn't been initialized
        PollStatus status = (PollStatus) (m_statusTable == null ? PollStatus.STATUS_UNKNOWN : m_statusTable.get(nodeLabel+":"+ipAddr+":"+svcName));
        return (status == null ? PollStatus.STATUS_UNKNOWN : status);
    }

    private void createMessageSelectorAndSubscribe() {
        List ueis = PassiveStatusConfigFactory.getInstance().getConfig().getPassiveStatusUeiCollection();
        // Subscribe to eventd
        getEventManager().addEventListener(this, ueis);
    }

    public void onEvent(Event e) {
        // TODO Auto-generated method stub
    }

    public EventIpcManager getEventManager() {
        if (m_eventMgr == null) {
            throw new IllegalStateException("getEventManager: EventIpcManager not set in PassiveStatusKeeper.");
        }
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public EventIpcManager getEventMgr() {
        return m_eventMgr;
    }

    public void setEventMgr(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

}
