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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.notifd;

import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;

import junit.framework.TestCase;
/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NotifdTest extends TestCase {

    private Notifd m_notifd;
    private MockEventIpcManager m_eventMgr;
    private MockNotifdConfigManager m_notifdConfig;
    private static final String m_configString = "<?xml version=\"1.0\"?>\n" + 
            "<notifd-configuration \n" + 
            "        status=\"off\"\n" + 
            "        pages-sent=\"SELECT * FROM notifications\"\n" + 
            "        next-notif-id=\"SELECT nextval(\'notifynxtid\')\"\n" + 
            "        next-group-id=\"SELECT nextval(\'notifygrpid\')\"\n" + 
            "        service-id-sql=\"SELECT serviceID from service where serviceName = ?\"\n" + 
            "        outstanding-notices-sql=\"SELECT notifyid FROM notifications where notifyId = ? AND respondTime is not null\"\n" + 
            "        acknowledge-id-sql=\"SELECT notifyid FROM notifications WHERE eventuei=? AND nodeid=? AND interfaceid=? AND serviceid=?\"\n" + 
            "        acknowledge-update-sql=\"UPDATE notifications SET answeredby=?, respondtime=? WHERE notifyId=?\"\n" + 
            "   match-all=\"false\">\n" + 
            "        \n" + 
            "   <auto-acknowledge uei=\"uei.opennms.org/nodes/serviceResponsive\" \n" + 
            "                          acknowledge=\"uei.opennms.org/nodes/serviceUnresponsive\">\n" + 
            "                          <match>nodeid</match>\n" + 
            "                          <match>interfaceid</match>\n" + 
            "                          <match>serviceid</match>\n" + 
            "        </auto-acknowledge>\n" + 
            "   \n" + 
            "        <auto-acknowledge uei=\"uei.opennms.org/nodes/nodeRegainedService\" \n" + 
            "                          acknowledge=\"uei.opennms.org/nodes/nodeLostService\">\n" + 
            "                          <match>nodeid</match>\n" + 
            "                          <match>interfaceid</match>\n" + 
            "                          <match>serviceid</match>\n" + 
            "        </auto-acknowledge>\n" + 
            "        \n" + 
            "        <auto-acknowledge uei=\"uei.opennms.org/nodes/interfaceUp\" \n" + 
            "                          acknowledge=\"uei.opennms.org/nodes/interfaceDown\">\n" + 
            "                          <match>nodeid</match>\n" + 
            "                          <match>interfaceid</match>\n" + 
            "        </auto-acknowledge>\n" + 
            "        \n" + 
            "        <auto-acknowledge uei=\"uei.opennms.org/nodes/nodeUp\" \n" + 
            "                          acknowledge=\"uei.opennms.org/nodes/nodeDown\">\n" + 
            "                          <match>nodeid</match>\n" + 
            "        </auto-acknowledge>\n" + 
            "        \n" + 
            "        <queue>\n" + 
            "                <queue-id>default</queue-id>\n" + 
            "                <interval>20s</interval>\n" + 
            "                <handler-class>\n" + 
            "                        <name>org.opennms.netmgt.notifd.DefaultQueueHandler</name>\n" + 
            "                </handler-class>\n" + 
            "        </queue>\n" + 
            "</notifd-configuration>";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.setupLogging();
        MockUtil.resetLogLevel();
        
        m_eventMgr = new MockEventIpcManager();
        
        m_notifd = new Notifd();
        m_notifdConfig = new MockNotifdConfigManager(m_configString);
        // FIXME: Needed to comment these out so the build worked
        m_notifd.setEventManager(m_eventMgr);
        m_notifd.setConfigManager(m_notifdConfig);
        //m_notifd.init();
        //m_notifd.start();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        // FIXME: commented this out so the build worked
        m_notifd.stop();
        assertTrue(MockUtil.noWarningsOrHigherLogged());
    }

    public void testNotifdBaseTest() {

    }
}
