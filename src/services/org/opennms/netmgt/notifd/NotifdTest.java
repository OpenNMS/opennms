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

import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.notifd.mock.MockGroupManager;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.netmgt.notifd.mock.MockNotificationManager;
import org.opennms.netmgt.notifd.mock.MockUserManager;

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
    private MockGroupManager m_groupManager;
    private MockUserManager m_userManager;
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
    
    private static final String NOTIFICATION_MANAGER = "<?xml version=\"1.0\"?>\n" + 
            "<notifications xmlns=\"http://xmlns.opennms.org/xsd/notifications\">\n" + 
            "    <header>\n" + 
            "        <rev>1.2</rev>\n" + 
            "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
            "        <mstation>localhost</mstation>\n" + 
            "    </header>\n" + 
            "    <notification name=\"serviceUnresponsive\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/serviceUnresponsive</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>The %service% poll to interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% successfully \n" + 
            "completed a connection to the service listener on the \n" + 
            "remote machine. However, the synthetic transaction failed \n" + 
            "to complete within %parm[timeout]% milliseconds, over \n" + 
            "%parm[attempts]% attempts.  This event will NOT impact service \n" + 
            "level agreements, but may be an indicator of other problems on that node.  \n" + 
            "   </text-message>\n" + 
            "        <subject>Notice #%noticeid%: %service% service on %interfaceresolve% (%interface%) on node %nodelabel% is unresponsive.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"serviceResponsive\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/serviceResponsive</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>The %service% service on %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% has recovered from a previously \n" + 
            "UNRESPONSIVE state.  Synthetic transactions to this service \n" + 
            "are completing within the alotted timeout and retry period.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %service% service on %interfaceresolve% (%interface%) on node %nodelabel% has recovered.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"interfaceDown\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/interfaceDown</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>All services are down on interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel%.  New Outage records have been created \n" + 
            "and service level availability calculations will be impacted \n" + 
            "until this outage is resolved.  \n" + 
            "   </text-message>\n" + 
            "        <subject>Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel% down.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeDown\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeDown</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>All services are down on node %nodelabel%.  New Outage records have \n" + 
            "been created and service level availability calculations will \n" + 
            "be impacted until this outage is resolved.  \n" + 
            "   </text-message>\n" + 
            "        <subject>Notice #%noticeid%: node %nodelabel% down.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"interfaceUp\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/interfaceUp</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>The interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% which was previously down is now up.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: Interface %interfaceresolve% (%interface%) on node %nodelabel% has been cleared</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeUp\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeUp</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>The node %nodelabel% which was previously down is now up.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: Node %nodelabel% has been cleared.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeLostService\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeLostService</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>The %service% service poll on interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% failed at %time%. \n" + 
            "   </text-message>\n" + 
            "        <subject>Notice #%noticeid%: %service% down on %interfaceresolve% (%interface%) on node %nodelabel%.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeRegainedService\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeRegainedService</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>%service% service restored on interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel%.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel%&apos;s %service% service restored.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"coldStart\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>An SNMP coldStart trap has been received from\n" + 
            "interface %snmphost%.  This indicates that the box has been\n" + 
            "powered up.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %snmphost% powered up.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"warmStart\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Warm_Start</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>An SNMP warmStart trap has been received from\n" + 
            "interface %snmphost%.  This indicates that the box has been rebooted.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %snmphost% rebooted.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"authenticationFailure\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Authen_Failure</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>An Authentication Failure has been identified on\n" + 
            "network device %snmphost%.  This message is usually\n" + 
            "generated by an authentication failure during a user login\n" + 
            "attempt or an SNMP request failed due to incorrect community string.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: [OpenNMS] Authentication Failure on %snmphost%.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"serviceDeleted\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/serviceDeleted</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>Due to extended downtime, the %service% service on\n" + 
            "interface %interfaceresolve% (%interface%) on node %nodelabel% \n" + 
            "has been deleted from OpenNMS&apos;s polling database.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel%&apos;s %service% service deleted.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeAdded\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeAdded</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>OpenNMS has discovered a new node named\n" + 
            "%parm[nodelabel]%. Please be advised.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %parm[nodelabel]% discovered.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeInfoChanged\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeInfoChanged</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>Node information has changed for a device in your\n" + 
            "network.  The new information is included:    System Name:\n" + 
            "%parm[nodesysname]%  System Description:\n" + 
            "%parm[nodesysdescription]%  System Object Identifier:\n" + 
            "%parm[nodesysobjectid]%  System Location:\n" + 
            "%parm[nodesyslocation]%  System Contact:\n" + 
            "%parm[nodesyscontact]%  NetBIOS Name: %parm[nodenetbiosname]%</text-message>\n" + 
            "        <subject>Notice #%noticeid%: Node information changed.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"interfaceDeleted\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/interfaceDeleted</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Admin</destinationPath>\n" + 
            "        <text-message>Due to extended downtime, the interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% has been deleted from OpenNMS&apos;s polling database.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: [OpenNMS] %interfaceresolve% (%interface%) on node %nodelabel% deleted.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "</notifications>\n" + 
            "";
    
    public static final String GROUP_MANAGER = "";
    public static final String USER_MANAGER = "";
    
    private NotificationManager m_notificationManager;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.setupLogging();
        MockUtil.resetLogLevel();
        
        m_eventMgr = new MockEventIpcManager();
        m_groupManager = new MockGroupManager(GROUP_MANAGER);
        m_userManager = new MockUserManager(m_groupManager, USER_MANAGER);
        
        m_notifd = new Notifd();
        m_notifdConfig = new MockNotifdConfigManager(m_configString);
        m_notifd.setEventManager(m_eventMgr);
        m_notifd.setConfigManager(m_notifdConfig);
        m_notifd.setGroupManager(m_groupManager);
        m_notifd.setUserManager(m_userManager);
        
        m_notificationManager = new MockNotificationManager(m_notifdConfig, NOTIFICATION_MANAGER);
        m_notifd.setNotificationManager(m_notificationManager);
        
        // FIXME: Needed to comment these out so the build worked
        m_notifd.init();
        m_notifd.start();
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
