//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.outage;

import org.opennms.netmgt.config.OutageManagerConfig;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockUtil;

import junit.framework.TestCase;

public class OutageTest extends TestCase {

    private OutageManager m_outageMgr;
    private MockEventIpcManager m_eventMgr;
    
    private class MockOutageConfig implements OutageManagerConfig {
        
        public boolean deletePropagation() {
            return true;
        }
        public String getGetNextOutageID() {
            return "SELECT nextval('outageNxtId')";
        }
        public int getWriters() {
            return 1;
        }
    }
    
    protected void setUp() throws Exception {
        MockUtil.logToConsole();
        System.getProperties().put("opennms.home", "/sw/var/opennms/etc");
        
        m_eventMgr = new MockEventIpcManager();
        
        m_outageMgr = new OutageManager();
        m_outageMgr.setEventMgr(m_eventMgr);
        m_outageMgr.setOutageMgrConfig(new MockOutageConfig());
        m_outageMgr.init();
        m_outageMgr.start();
        
    }

    protected void tearDown() throws Exception {
        m_outageMgr.stop();
    }
    
    public void testCreate() {
        
    }

    // nodeLostService: EventConstants.NODE_LOST_SERVICE_EVENT_UEI

    // interfaceDown: EventConstants.INTERFACE_DOWN_EVENT_UEI

    // nodeDown: EventConstants.NODE_DOWN_EVENT_UEI

    // nodeUp: EventConstants.NODE_UP_EVENT_UEI

    // interfaceUp: EventConstants.INTERFACE_UP_EVENT_UEI

    // nodeRegainedService: EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI

    // interfaceReparented: EventConstants.INTERFACE_REPARENTED_EVENT_UEI


}
