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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.notifd;

import junit.framework.TestCase;

import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.notifd.mock.MockDestinationPathManager;
import org.opennms.netmgt.notifd.mock.MockGroupManager;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.netmgt.notifd.mock.MockNotification;
import org.opennms.netmgt.notifd.mock.MockNotificationCommandManager;
import org.opennms.netmgt.notifd.mock.MockNotificationManager;
import org.opennms.netmgt.notifd.mock.MockNotificationStrategy;
import org.opennms.netmgt.notifd.mock.MockUserManager;
import org.opennms.netmgt.notifd.mock.NotificationAnticipator;
import org.opennms.netmgt.xml.event.Event;
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
    private NotificationManager m_notificationManager;
    private NotificationCommandManager m_notificationCommandManger;
    private MockDestinationPathManager m_destinationPathManager;


    private static String NOTIFD_CONFIG_MANAGER = "<?xml version=\"1.0\"?>\n" + 
            "<notifd-configuration \n" + 
            "        status=\"on\"\n" + 
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
            "                <interval>2s</interval>\n" + 
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
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
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
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
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
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
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
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>All services are down on node %nodeid%.  New Outage records have \n" + 
            "been created and service level availability calculations will \n" + 
            "be impacted until this outage is resolved.  \n" + 
            "   </text-message>\n" + 
            "        <subject>node %nodeid% down.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"interfaceUp\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/interfaceUp</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>The interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% which was previously down is now up.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: Interface %interfaceresolve% (%interface%) on node %nodelabel% has been cleared</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeUp\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeUp</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>The node which was previously down is now up.</text-message>\n" + 
            "        <subject>node %nodeid% up.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeLostService\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeLostService</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>The %service% service poll on interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% failed at %time%. \n" + 
            "   </text-message>\n" + 
            "        <subject>Notice #%noticeid%: %service% down on %interfaceresolve% (%interface%) on node %nodelabel%.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeRegainedService\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeRegainedService</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>%service% service restored on interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel%.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel%&apos;s %service% service restored.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"coldStart\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>An SNMP coldStart trap has been received from\n" + 
            "interface %snmphost%.  This indicates that the box has been\n" + 
            "powered up.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %snmphost% powered up.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"warmStart\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Warm_Start</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>An SNMP warmStart trap has been received from\n" + 
            "interface %snmphost%.  This indicates that the box has been rebooted.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %snmphost% rebooted.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"authenticationFailure\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/generic/traps/SNMP_Authen_Failure</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
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
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>Due to extended downtime, the %service% service on\n" + 
            "interface %interfaceresolve% (%interface%) on node %nodelabel% \n" + 
            "has been deleted from OpenNMS&apos;s polling database.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel%&apos;s %service% service deleted.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeAdded\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeAdded</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>OpenNMS has discovered a new node named\n" + 
            "%parm[nodelabel]%. Please be advised.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: %parm[nodelabel]% discovered.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"nodeInfoChanged\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/nodes/nodeInfoChanged</uei>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
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
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>Due to extended downtime, the interface %interfaceresolve% (%interface%) \n" + 
            "on node %nodelabel% has been deleted from OpenNMS&apos;s polling database.</text-message>\n" + 
            "        <subject>Notice #%noticeid%: [OpenNMS] %interfaceresolve% (%interface%) on node %nodelabel% deleted.</subject>\n" + 
            "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
            "     <notification name=\"SNMP High disk Threshold Exceeded\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/threshold/highThresholdExceeded</uei>\n" + 
            "        <description>high disk threshold exceeded on snmp interface</description>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>High disk Threshold exceeded on %interface%, %parm[ds]% with %parm[value]%%%</text-message>\n" + 
            "        <subject>Notice #%noticeid%, High disk Threshold exceeded</subject>\n" + 
            "        <varbind>\n" + 
            "            <vbname>ds</vbname>\n" + 
            "            <vbvalue>dsk-usr-pcent</vbvalue>\n" + 
            "        </varbind>\n" + 
            "    </notification>\n" + 
            "    <notification name=\"SNMP High loadavg5 Threshold Exceeded\" status=\"on\">\n" + 
            "        <uei>uei.opennms.org/threshold/highThresholdExceeded</uei>\n" + 
            "        <description>high loadavg5 threshold exceeded on snmp interface</description>\n" + 
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>Email-Mock</destinationPath>\n" + 
            "        <text-message>High loadavg5 Threshold exceeded on %interface%, %parm[ds]% with %parm[value]%%%</text-message>\n" + 
            "        <subject>High loadavg5 Threshold exceeded</subject>\n" + 
            "        <varbind>\n" + 
            "            <vbname>ds</vbname>\n" + 
            "            <vbvalue>loadavg5</vbvalue>\n" + 
            "        </varbind>\n" + 
            "    </notification>" +
            "</notifications>\n" + 
            "";
    
    public static final String GROUP_MANAGER = "<?xml version=\"1.0\"?>\n" + 
            "<groupinfo>\n" + 
            "    <header>\n" + 
            "        <rev>1.3</rev>\n" + 
            "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
            "        <mstation>dhcp-219.internal.opennms.org</mstation>\n" + 
            "    </header>\n" + 
            "    <groups>\n" + 
            "        <group>\n" + 
            "            <name>Network/Systems</name>\n" + 
            "            <comments>The network and systems group</comments>\n" + 
            "        </group>\n" + 
            "        <group>\n" + 
            "            <name>Desktops</name>\n" + 
            "            <comments>The desktops group</comments>\n" + 
            "        </group>\n" + 
            "        <group>\n" + 
            "            <name>Security</name>\n" + 
            "            <comments>The security group</comments>\n" + 
            "        </group>\n" + 
            "        <group>\n" + 
            "            <name>Management</name>\n" + 
            "            <comments>The management group</comments>\n" +
            "            <user>admin</user>" + 
            "            <user>brozow</user>" + 
            "        </group>\n" + 
            "        <group>\n" + 
            "            <name>Reporting</name>\n" + 
            "            <comments>The reporting group</comments>\n" + 
            "        </group>\n" + 
            "        <group>\n" + 
            "           <name>Admin</name>\n" + 
            "           <comments>The administrators</comments>\n" + 
            "           <user>admin</user>\n" + 
            "           <user>brozow</user>\n" +
            "        </group>\n" + 
            "    </groups>\n" + 
            "</groupinfo>\n" + 
            "";
    public static final String USER_MANAGER = "<?xml version=\"1.0\"?>\n" + 
            "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n" + 
            "   <header>\n" + 
            "       <rev>.9</rev>\n" + 
            "           <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
            "       <mstation>master.nmanage.com</mstation>\n" + 
            "   </header>\n" + 
            "   <users>\n" + 
            "       <user>\n" + 
            "           <user-id>brozow</user-id>\n" + 
            "           <full-name>Mathew Brozowski</full-name>\n" + 
            "           <user-comments>Test User</user-comments>\n" +
            "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
            "           <contact type=\"email\" info=\"matt@opennms.org\"/>\n" + 
            "       </user>\n" + 
            "       <user>\n" + 
            "           <user-id>admin</user-id>\n" + 
            "           <full-name>Administrator</full-name>\n" + 
            "           <user-comments>Default administrator, do not delete</user-comments>\n" +
            "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
            "           <contact type=\"email\" info=\"dhustace@nc.rr.com\"/>\n" + 
            "       </user>\n" + 
            "       <user>\n" + 
            "           <user-id>tempuser</user-id>\n" + 
            "           <full-name>Temporary User</full-name>\n" + 
            "                        <user-comments></user-comments>\n" + 
            "           <password>18126E7BD3F84B3F3E4DF094DEF5B7DE</password>\n" + 
            "           <contact type=\"email\" info=\"temp.user@opennms.org\"/>\n" + 
            "           <contact type=\"numericPage\" info=\"6789\" serviceProvider=\"ATT\"/>\n" + 
            "           <contact type=\"textPage\" info=\"9876\" serviceProvider=\"Sprint\"/>\n" + 
            "           <duty-schedule>MoTuWeThFrSaSu800-2300</duty-schedule>\n" + 
            "       </user>\n" + 
            "   </users>\n" + 
            "</userinfo>\n" + 
            "";
    
    private static final String PATH_MANAGER = "<?xml version=\"1.0\"?>\n" + 
            "<destinationPaths>\n" + 
            "    <header>\n" + 
            "        <rev>1.2</rev>\n" + 
            "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
            "        <mstation>localhost</mstation>\n" + 
            "    </header>\n" + 
            "    <path name=\"Email-Mock\" initial-delay=\"0s\">\n" + 
            "        <target>\n" + 
            "            <name>Management</name>\n" + 
            "            <command>mockNotifier</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Reporting\">\n" + 
            "        <target>\n" + 
            "                <name>Reporting</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Management\">\n" + 
            "        <target>\n" + 
            "                <name>Management</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Network/Systems/Management\">\n" + 
            "   <target interval=\"15m\">\n" + 
            "                <name>Network/Systems</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <escalate delay=\"15m\">\n" + 
            "            <target>\n" + 
            "                <name>Management</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "            </target>\n" + 
            "        </escalate>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Network/Systems\">\n" + 
            "        <target>\n" + 
            "                <name>Network/Systems</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Management\">\n" + 
            "        <target>\n" + 
            "                <name>Management</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Desktops/Management\">\n" + 
            "        <target>\n" + 
            "                <name>Desktops</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <escalate delay=\"15m\">\n" + 
            "                <target>\n" + 
            "                        <name>Management</name>\n" + 
            "                        <command>textPage</command>\n" + 
            "                        <command>javaPagerEmail</command>\n" + 
            "                        <command>javaEmail</command>\n" + 
            "                </target>\n" + 
            "        </escalate>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Network/Systems/Management\">\n" + 
            "        <target>\n" + 
            "                <name>Network/Systems</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <escalate delay=\"15m\">\n" + 
            "                <target>\n" + 
            "                        <name>Management</name>\n" + 
            "                        <command>javaEmail</command>\n" + 
            "                </target>\n" + 
            "        </escalate>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Security/Management\">\n" + 
            "        <target>\n" + 
            "                <name>Security</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <escalate delay=\"15m\">\n" + 
            "                <target>\n" + 
            "                        <name>Management</name>\n" + 
            "                        <command>javaEmail</command>\n" + 
            "                </target>\n" + 
            "        </escalate>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Security/Management\">\n" + 
            "        <target>\n" + 
            "                <name>Security</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <escalate delay=\"15m\">\n" + 
            "                <target>\n" + 
            "                        <name>Management</name>\n" + 
            "                        <command>textPage</command>\n" + 
            "                        <command>javaPagerEmail</command>\n" + 
            "                        <command>javaEmail</command>\n" + 
            "                </target>\n" + 
            "        </escalate>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Desktops/Management\">\n" + 
            "        <target>\n" + 
            "                <name>Desktops</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <escalate delay=\"15m\">\n" + 
            "                <target>\n" + 
            "                        <name>Management</name>\n" + 
            "                        <command>javaEmail</command>\n" + 
            "                </target>\n" + 
            "        </escalate>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Desktops\">\n" + 
            "        <target>\n" + 
            "                <name>Desktops</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Security\">\n" + 
            "        <target>\n" + 
            "                <name>Security</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Network/Systems\">\n" + 
            "        <target>\n" + 
            "                <name>Network/Systems</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Desktops\">\n" + 
            "        <target>\n" + 
            "                <name>Desktops</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-Security\">\n" + 
            "        <target>\n" + 
            "                <name>Security</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Page-All\">\n" + 
            "        <target>\n" + 
            "                <name>Network/Systems</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        \n" + 
            "        <target interval=\"15m\">\n" + 
            "                <name>Security</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        \n" + 
            "        <target interval=\"1h\">\n" + 
            "                <name>Desktops</name>\n" + 
            "                <command>textPage</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        \n" + 
            "        <target interval=\"1d\">\n" + 
            "                <name>Management</name>\n" + 
            "                <command>page</command>\n" + 
            "                <command>javaPagerEmail</command>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-All\">\n" + 
            "        <target>\n" + 
            "                <name>Network/Systems</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "         </target>\n" + 
            "        <target>\n" + 
            "                <name>Security</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <target>\n" + 
            "                <name>Desktops</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "        <target>\n" + 
            "                <name>Management</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "    <path name=\"Email-Admin\">\n" + 
            "        <target>\n" + 
            "                <name>Admin</name>\n" + 
            "                <command>javaEmail</command>\n" + 
            "        </target>\n" + 
            "    </path>\n" + 
            "</destinationPaths>\n" + 
            "";
    private static final String CMD_MANAGER = "<?xml version=\"1.0\"?>\n" + 
            "<notification-commands>\n" + 
            "    <header>\n" + 
            "        <ver>.9</ver>\n" + 
            "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
            "        <mstation>master.nmanage.com</mstation>\n" + 
            "    </header>\n" + 
            "    <command binary=\"false\">\n" + 
            "        <name>mockNotifier</name>\n" + 
            "        <execute>org.opennms.netmgt.notifd.mock.MockNotificationStrategy</execute>\n" + 
            "        <comment>Mock Class for sending test notifications</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-subject</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-email</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-tm</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "    <command binary=\"false\">\n" + 
            "        <name>javaPagerEmail</name>\n" + 
            "        <execute>org.opennms.netmgt.notifd.JavaMailNotificationStrategy</execute>\n" + 
            "        <comment>class for sending pager email notifications</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-subject</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-pemail</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-tm</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "    <command binary=\"false\">\n" + 
            "        <name>javaEmail</name>\n" + 
            "        <execute>org.opennms.netmgt.notifd.JavaMailNotificationStrategy</execute>\n" + 
            "        <comment>class for sending email notifications</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-subject</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-email</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-tm</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "   <command binary=\"true\">\n" + 
            "       <name>syslog</name>\n" + 
            "       <execute>/usr/bin/logger</execute>\n" + 
            "       <comment>syslog to local0.warning</comment>\n" + 
            "       <argument streamed=\"false\">\n" + 
            "           <substitution>-p</substitution>\n" + 
            "       </argument>\n" + 
            "       <argument streamed=\"false\">\n" + 
            "           <substitution>local0.warning</substitution>\n" + 
            "       </argument>\n" + 
            "       <argument streamed=\"false\">\n" + 
            "           <substitution>-t</substitution>\n" + 
            "       </argument>\n" + 
            "       <argument streamed=\"false\">\n" + 
            "           <substitution>opennms</substitution>\n" + 
            "       </argument>\n" + 
            "       <argument streamed=\"true\">\n" + 
            "           <switch>-tm</switch>\n" + 
            "       </argument>\n" + 
            "   </command>\n" + 
            "    <command binary=\"true\">\n" + 
            "        <name>textPage</name>\n" + 
            "        <execute>/usr/bin/qpage</execute>\n" + 
            "        <comment>text paging program</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-p</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-t</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "    <command binary=\"true\">\n" + 
            "        <name>numericPage</name>\n" + 
            "        <execute>/usr/bin/qpage</execute>\n" + 
            "        <comment>numeric paging program</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <substitution>-p</substitution>\n" + 
            "            <switch>-d</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-nm</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "    <command binary=\"true\">\n" + 
            "        <name>email</name>\n" + 
            "        <execute>/bin/mail</execute>\n" + 
            "        <comment>for sending email notifications</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <substitution>-s</substitution>\n" + 
            "            <switch>-subject</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-email</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"true\">\n" + 
            "            <switch>-tm</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "    <command binary=\"true\">\n" + 
            "        <name>pagerEmail</name>\n" + 
            "        <execute>/bin/mail</execute>\n" + 
            "        <comment>for sending pager email notifications</comment>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <substitution>-s</substitution>\n" + 
            "            <switch>-subject</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"false\">\n" + 
            "            <switch>-pemail</switch>\n" + 
            "        </argument>\n" + 
            "        <argument streamed=\"true\">\n" + 
            "            <switch>-tm</switch>\n" + 
            "        </argument>\n" + 
            "    </command>\n" + 
            "</notification-commands>";
    private MockDatabase m_db;
    private MockNetwork m_network;
    private NotificationAnticipator m_anticipator;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.setupLogging();
        MockUtil.resetLogLevel();
        
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        
        m_db = new MockDatabase();
        m_db.populate(m_network);

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_notifdConfig = new MockNotifdConfigManager(NOTIFD_CONFIG_MANAGER);
        m_notifdConfig.setNextNotifIdSql(m_db.getNextNotifIdSql());
        m_groupManager = new MockGroupManager(GROUP_MANAGER);
        m_userManager = new MockUserManager(m_groupManager, USER_MANAGER);
        m_destinationPathManager = new MockDestinationPathManager(PATH_MANAGER);        
        m_notificationCommandManger = new MockNotificationCommandManager(CMD_MANAGER);
        m_notificationManager = new MockNotificationManager(m_notifdConfig, m_db, NOTIFICATION_MANAGER);
        
        m_notifd = new Notifd();
        m_notifd.setEventManager(m_eventMgr);
        m_notifd.setConfigManager(m_notifdConfig);
        m_notifd.setGroupManager(m_groupManager);
        m_notifd.setUserManager(m_userManager);
        m_notifd.setDestinationPathManager(m_destinationPathManager);
        m_notifd.setNotificationCommandManager(m_notificationCommandManger);
        m_notifd.setNotificationManager(m_notificationManager);
                
        m_anticipator = new NotificationAnticipator();
        MockNotificationStrategy.setAnticpator(m_anticipator);

        m_notifd.init();
        m_notifd.start();
        
        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        m_notifd.stop();
        m_db.drop();
        MockNotificationStrategy.setAnticpator(null);
        assertTrue(MockUtil.noWarningsOrHigherLogged());
    }
    
    /**
     * see http://bugzilla.opennms.org/cgi-bin/bugzilla/show_bug.cgi?id=731
     * @throws Exception
     */
    public void testBug731() throws Exception {
        assertEquals(0L,0L);
    }
    
    /**
     * see http://bugzilla.opennms.org/cgi-bin/bugzilla/show_bug.cgi?id=1022
     * @throws Exception
     */
    public void testWicktorBug_1022_1031() throws Exception {

        m_anticipator.anticipateNotification(createMockNotification("High loadavg5 Threshold exceeded", "dhustace@nc.rr.com"));
        m_anticipator.anticipateNotification(createMockNotification("High loadavg5 Threshold exceeded", "matt@opennms.org"));

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");
        Event e = MockUtil.createInterfaceEvent("test", "uei.opennms.org/threshold/highThresholdExceeded", iface);
        MockUtil.addEventParm(e, "ds", "loadavg5");
        m_eventMgr.sendNow(e);
        
        /*
         * This is the notification config that Wicktor sent when reporting this bug.
         * 
         * We need to create and Threshold Exceeded event for loadavg5 to match his
         * notification name = "SNMP High loadavg5 Threshold Exceeded" correctly parsing the varbind.
         * 
         * What happens (he sent us a patch for this) is that the code does a return instead of a continue
         * when going through the notification names.
         */
        
        verifyAnticipated(3000);
        
        
    }

    public void testNotifdStatus() throws Exception {
        
            //test for off status passed in config XML string
            assertEquals(m_notifdConfig.getNotificationStatus(), "on");
            
            //test for on status set here
            m_notifdConfig.turnNotifdOff();
            assertEquals(m_notifdConfig.getNotificationStatus(), "off");
            
            //test for off status set here
            m_notifdConfig.turnNotifdOn();
            assertEquals(m_notifdConfig.getNotificationStatus(), "on");

    }
    
    public void testMockNotificationBasic() throws Exception {

        MockNode node = m_network.getNode(1);

        String subject;
        String email;
        MockNotification notification;

        notification = createMockNotification("node 1 down.", "dhustace@nc.rr.com");
        m_anticipator.anticipateNotification(notification);

        notification = createMockNotification("node 1 down.", "matt@opennms.org");
        m_anticipator.anticipateNotification(notification);

        //bring node down now
        m_eventMgr.sendNow(node.createDownEvent());

        verifyAnticipated(3000);
        
        m_anticipator.reset();
        
        notification = createMockNotification("node 1 up.", "dhustace@nc.rr.com");
        m_anticipator.anticipateNotification(notification);

        notification = createMockNotification("node 1 up.", "matt@opennms.org");
        m_anticipator.anticipateNotification(notification);
        
        //bring node back up now
        m_eventMgr.sendNow(node.createUpEvent());

        verifyAnticipated(3000);

    }
    
    public void testMockNotificationInitialDelay() throws Exception {

        m_destinationPathManager.getPath("Email-Mock").setInitialDelay("10s");
        
        MockNode node = m_network.getNode(1);
        MockNotification notification = new MockNotification();
        
        notification = createMockNotification("node 1 down.", "dhustace@nc.rr.com");
        m_anticipator.anticipateNotification(notification);

        notification = createMockNotification("node 1 down.", "matt@opennms.org");
        m_anticipator.anticipateNotification(notification);

        m_eventMgr.sendNow(node.createDownEvent());

        assertEquals("Expected notifications not forthcoming.", 2, m_anticipator.waitForAnticipated(3000).size());
        sleep(1000);
        assertEquals("Unexpected notifications forthcoming.", 0, m_anticipator.getUnanticipated().size());

        verifyAnticipated(10000);

    }

    private void verifyAnticipated(int waitTime) {
        assertEquals("Expected notifications not forthcoming.", 0, m_anticipator.waitForAnticipated(waitTime).size());
        sleep(1000);
        assertEquals("Unexpected notifications forthcoming.", 0, m_anticipator.getUnanticipated().size());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private MockNotification createMockNotification(String subject, String email) {
        MockNotification notification;
        notification = new MockNotification();
        notification.setSubject(subject);
        notification.setEmail(email);
        return notification;
    }

}
