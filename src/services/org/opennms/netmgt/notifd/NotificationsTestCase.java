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
package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockNetwork;
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

import junit.framework.TestCase;

public class NotificationsTestCase extends TestCase {

    protected Notifd m_notifd;
    protected MockEventIpcManager m_eventMgr;
    protected MockNotifdConfigManager m_notifdConfig;
    protected MockGroupManager m_groupManager;
    protected MockUserManager m_userManager;
    protected NotificationManager m_notificationManager;
    protected NotificationCommandManager m_notificationCommandManger;
    protected MockDestinationPathManager m_destinationPathManager;
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
                "   <auto-acknowledge notify = \"true\" uei=\"uei.opennms.org/nodes/serviceResponsive\" \n" + 
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
                "                <interval>100ms</interval>\n" + 
                "                <handler-class>\n" + 
                "                        <name>org.opennms.netmgt.notifd.DefaultQueueHandler</name>\n" + 
                "                </handler-class>\n" + 
                "        </queue>\n" + 
                "</notifd-configuration>";
    private static final String NOTIFICATIONS = "<?xml version=\"1.0\"?>\n" + 
                "<notifications xmlns=\"http://xmlns.opennms.org/xsd/notifications\">\n" + 
                "    <header>\n" + 
                "        <rev>1.2</rev>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>localhost</mstation>\n" + 
                "    </header>\n" + 
                "    <notification name=\"snmpTrap\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/nodeDown</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>trapNotifier</destinationPath>\n" + 
                "        <text-message>All services are down on node %nodeid%.</text-message>\n" + 
                "        <subject>node %nodeid% down.</subject>\n" + 
                "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
                "        <parameter name=\"trapVersion\" value=\"v1\" />\n"+
                "        <parameter name=\"trapTransport\" value=\"UDP\" />\n"+
                "        <parameter name=\"trapHost\" value=\"localhost\" />\n"+
                "        <parameter name=\"trapPort\" value=\"161\" />\n"+
                "        <parameter name=\"trapCommunity\" value=\"public\" />\n"+
                "        <parameter name=\"trapEnterprise\" value=\".1.3.6.1.4.1.5813\" />\n"+
                "        <parameter name=\"trapGeneric\" value=\"6\" />\n"+
                "        <parameter name=\"trapSpecific\" value=\"1\" />\n"+
                "        <parameter name=\"trapVarbind\" value=\"%uei%\" />\n"+
                "    </notification>\n" + 
                "    <notification name=\"nodeDown\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/nodeDown</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>NoEscalate</destinationPath>\n" + 
                "        <text-message>All services are down on node %nodeid%.</text-message>\n" + 
                "        <subject>node %nodeid% down.</subject>\n" + 
                "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "    <notification name=\"nodeUp\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/nodeUp</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>UpPath</destinationPath>\n" + 
                "        <text-message>The node which was previously down is now up.</text-message>\n" + 
                "        <subject>node %nodeid% up.</subject>\n" + 
                "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "    <notification name=\"interfaceDown\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/interfaceDown</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>Escalate</destinationPath>\n" + 
                "        <text-message>All services are down on interface %interface%, %ifalias%.</text-message>\n" + 
                "        <subject>interface %interface% down.</subject>\n" + 
                "        <numeric-message>222-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "    <notification name=\"interfaceUp\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/interfaceUp</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>UpPath</destinationPath>\n" + 
                "        <text-message>The interface which was previously down is now up.</text-message>\n" + 
                "        <subject>interface %interface% up.</subject>\n" + 
                "        <numeric-message>222-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "    <notification name=\"nodeLostService\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/nodeLostService</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>Intervals</destinationPath>\n" + 
                "        <text-message>Service %service% is down on interface %interface%.</text-message>\n" + 
                "        <subject>service %service% on %interface% down.</subject>\n" + 
                "        <numeric-message>333-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "    <notification name=\"nodeRegainedService\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/nodeRegainedService</uei>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>UpPath</destinationPath>\n" + 
                "        <text-message>Service %service% on interface %interface% has come back up.</text-message>\n" + 
                "        <subject>service %service% on %interface% up.</subject>\n" + 
                "        <numeric-message>333-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "     <notification name=\"SNMP High disk Threshold Exceeded\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/threshold/highThresholdExceeded</uei>\n" + 
                "        <description>high disk threshold exceeded on snmp interface</description>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>NoEscalate</destinationPath>\n" + 
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
                "        <destinationPath>NoEscalate</destinationPath>\n" + 
                "        <text-message>High loadavg5 Threshold exceeded on %interface%, %parm[ds]% with %parm[value]%%%</text-message>\n" + 
                "        <subject>High loadavg5 Threshold exceeded</subject>\n" + 
                "        <varbind>\n" + 
                "            <vbname>ds</vbname>\n" + 
                "            <vbvalue>loadavg5</vbvalue>\n" + 
                "        </varbind>\n" + 
                "    </notification>" +
                "    <notification name=\"nodeTimeTest\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/tests/nodeTimeTest</uei>\n" + 
	        "        <description>test for properly formatted timestamp in notifications</description>\n" +
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>NoEscalate</destinationPath>\n" + 
                "        <text-message>Timestamp: %time%.</text-message>\n" + 
                "        <subject>time %time%.</subject>\n" + 
                "        <numeric-message>333-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "    <notification name=\"Roled Based Test Event\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/test/roleTestEvent</uei>\n" + 
                "        <description>Test for notification of roles</description>\n" + 
                "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
                "        <destinationPath>OnCall</destinationPath>\n" + 
                "        <text-message>Notification Test</text-message>\n" + 
                "        <subject>notification test</subject>\n" + 
                "    </notification>" +
                "</notifications>\n" + 
                "";
    public static final String GROUPS = "<?xml version=\"1.0\"?>\n" + 
                "<groupinfo>\n" + 
                "    <header>\n" + 
                "        <rev>1.3</rev>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>dhcp-219.internal.opennms.org</mstation>\n" + 
                "    </header>\n" + 
                "    <groups>\n" + 
                "        <group>\n" + 
                "            <name>All</name>\n" + 
                "            <comments>The group that contains all users</comments>\n" + 
                "            <user>admin</user>" + 
                "            <user>brozow</user>" + 
                "            <user>david</user>" + 
                "        </group>\n" + 
                "        <group>\n" + 
                "            <name>InitialGroup</name>\n" + 
                "            <comments>The group that gets notified first</comments>\n" + 
                "            <user>admin</user>" + 
                "            <user>brozow</user>" + 
                "        </group>\n" + 
                "        <group>\n" + 
                "            <name>EscalationGroup</name>\n" + 
                "            <comments>The group things escalate to</comments>\n" +
                "            <user>brozow</user>" + 
                "            <user>david</user>" + 
                "        </group>\n" + 
                "        <group>\n" + 
                "            <name>UpGroup</name>\n" + 
                "            <comments>The group things escalate to</comments>\n" +
                "            <user>upUser</user>" + 
                "        </group>\n" + 
                "        <group>\n" + 
                "            <name>DutyGroup</name>\n" + 
                "            <comments>The group things escalate to</comments>\n" +
                "            <user>brozow</user>" +
                "           <duty-schedule>MoTuWeThFrSaSu800-2300</duty-schedule>\n" + 
                "        </group>\n" + 
                "    </groups>\n" +
                "    <roles>\n" +
                "      <role name=\"oncall\" supervisor=\"admin\" description=\"oncall role\" membership-group=\"All\">" + 
                "           <schedule name=\"brozow\" type=\"weekly\">" +
                "               <time day=\"sunday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "               <time day=\"monday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "               <time day=\"wednesday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "               <time day=\"friday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "           </schedule>\n" +
                "           <schedule name=\"admin\" type=\"weekly\">" +
                "               <time day=\"sunday\" begins=\"00:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"tuesday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "               <time day=\"thursday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "               <time day=\"saturday\" begins=\"09:00:00\" ends=\"17:00:00\"/>\n" + 
                "           </schedule>" +
                "           <schedule name=\"david\" type=\"weekly\">" +
                "               <time day=\"sunday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"sunday\"    begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"monday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"monday\"    begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"tuesday\"   begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"tuesday\"   begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"wednesday\" begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"wednesday\" begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"thursday\"  begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"thursday\"  begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"friday\"    begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"friday\"    begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "               <time day=\"saturday\"  begins=\"00:00:00\" ends=\"09:00:00\"/>\n" + 
                "               <time day=\"saturday\"  begins=\"17:00:00\" ends=\"23:59:59\"/>\n" + 
                "           </schedule>" +
                "       </role>\n" +
                "       <role name=\"onDuty\" supervisor=\"admin\" description=\"onDuty role\" membership-group=\"All\">" +
                "           <schedule name=\"brozow\" type=\"weekly\">" +
                "               <time day=\"sunday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "               <time day=\"monday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "               <time day=\"tuesday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "               <time day=\"wednesday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "               <time day=\"thursday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "               <time day=\"friday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "               <time day=\"saturday\" begins=\"06:00:00\" ends=\"07:00:00\"/>\n" + 
                "           </schedule>\n" +
                "       </role>\n" +
                "   </roles>\n" +
                "</groupinfo>\n" + 
                "";
    public static final String USERS = "<?xml version=\"1.0\"?>\n" + 
                "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n" + 
                "   <header>\n" + 
                "       <rev>.9</rev>\n" + 
                "           <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "       <mstation>master.nmanage.com</mstation>\n" + 
                "   </header>\n" + 
                "   <users>\n" + 
                "       <user>\n" + 
                "           <user-id>trapd</user-id>\n" + 
                "           <full-name>SNMP Trapd</full-name>\n" + 
                "           <user-comments>User that receives trap notifications</user-comments>\n" +
                "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
                "           <contact type=\"snmpTrap\" info=\"Destination for SNMP Trap/Notifications\"/>\n" + 
                "       </user>\n" + 
                "       <user>\n" + 
                "           <user-id>brozow</user-id>\n" + 
                "           <full-name>Mathew Brozowski</full-name>\n" + 
                "           <user-comments>Test User</user-comments>\n" +
                "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
                "           <contact type=\"email\" info=\"brozow@opennms.org\"/>\n" + 
                "       </user>\n" + 
                "       <user>\n" + 
                "           <user-id>admin</user-id>\n" + 
                "           <full-name>Administrator</full-name>\n" + 
                "           <user-comments>Default administrator, do not delete</user-comments>\n" +
                "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
                "           <contact type=\"email\" info=\"admin@opennms.org\"/>\n" + 
                "       </user>\n" + 
                "       <user>\n" + 
                "           <user-id>upUser</user-id>\n" + 
                "           <full-name>User that receives up notifications</full-name>\n" + 
                "           <user-comments>Default administrator, do not delete</user-comments>\n" +
                "           <password>21232F297A57A5A743894A0E4A801FC3</password>\n" +
                "           <contact type=\"email\" info=\"up@opennms.org\"/>\n" + 
                "       </user>\n" + 
                "       <user>\n" + 
                "           <user-id>david</user-id>\n" + 
                "           <full-name>David Hustace</full-name>\n" +
                "           <user-comments>A cool dude!</user-comments>\n" + 
                "           <password>18126E7BD3F84B3F3E4DF094DEF5B7DE</password>\n" + 
                "           <contact type=\"email\" info=\"david@opennms.org\"/>\n" + 
                "           <contact type=\"numericPage\" info=\"6789\" serviceProvider=\"ATT\"/>\n" + 
                "           <contact type=\"textPage\" info=\"9876\" serviceProvider=\"Sprint\"/>\n" + 
//                "           <duty-schedule>MoTuWeThFrSaSu800-2300</duty-schedule>\n" + 
                "       </user>\n" + 
                "   </users>\n" + 
                "</userinfo>\n" + 
                "";
    private static final String DESTINATION_PATHS = "<?xml version=\"1.0\"?>\n" + 
                "<destinationPaths>\n" + 
                "    <header>\n" + 
                "        <rev>1.2</rev>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>localhost</mstation>\n" + 
                "    </header>\n" + 
                "    <path name=\"trapNotifier\" initial-delay=\"0s\">\n" + 
                "        <target>\n" + 
                "            <name>trapd</name>\n" + 
                "            <command>snmpTrap</command>\n" + 
                "        </target>\n" + 
                "    </path>\n" + 
                "    <path name=\"OnCall\" initial-delay=\"0s\">\n" + 
                "        <target>\n" + 
                "            <name>oncall</name>\n" + 
                "            <command>mockNotifier</command>\n" + 
                "        </target>\n" + 
                "    </path>\n" + 
                "    <path name=\"NoEscalate\" initial-delay=\"0s\">\n" + 
                "        <target>\n" + 
                "            <name>InitialGroup</name>\n" + 
                "            <command>mockNotifier</command>\n" + 
                "        </target>\n" + 
                "    </path>\n" + 
                "    <path name=\"Intervals\" initial-delay=\"0s\">\n" + 
                "        <target interval=\"3s\">\n" + 
                "            <name>InitialGroup</name>\n" + 
                "            <command>mockNotifier</command>\n" + 
                "        </target>\n" + 
                "    </path>\n" + 
                "    <path name=\"Escalate\">\n" + 
                "        <target>\n" + 
                "            <name>InitialGroup</name>\n" + 
                "            <command>mockNotifier</command>\n" + 
                "        </target>\n" + 
                "        <escalate delay=\"2500ms\">\n" + 
                "            <target>\n" + 
                "            <name>EscalationGroup</name>\n" + 
                "            <command>mockNotifier</command>\n" + 
                "            </target>\n" + 
                "        </escalate>\n" + 
                "    </path>\n" + 
                "    <path name=\"UpPath\" initial-delay=\"0s\">\n" + 
                "        <target>\n" + 
                "            <name>UpGroup</name>\n" + 
                "            <command>mockNotifier</command>\n" + 
                "        </target>\n" + 
                "    </path>\n" + 
                "</destinationPaths>\n" + 
                "";
    private static final String NOTIFICATION_COMMANDS = "<?xml version=\"1.0\"?>\n" + 
                "<notification-commands>\n" + 
                "    <header>\n" + 
                "        <ver>.9</ver>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>master.nmanage.com</mstation>\n" + 
                "    </header>\n" + 
                "    <command binary=\"false\">\n" + 
                "        <name>snmpTrap</name>\n" + 
                "        <execute>org.opennms.netmgt.notifd.SnmpTrapNotificationStrategy</execute>\n" + 
                "        <comment>Class for sending notifications as SNMP Traps</comment>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapVersion</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapTransport</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapHost</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapPort</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapCommunity</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapEnterprise</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapGeneric</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapSpecific</switch>\n" + 
                "        </argument>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>trapVarbind</switch>\n" + 
                "        </argument>\n" + 
                "    </command>\n" + 
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
                "</notification-commands>";
    protected MockDatabase m_db;
    protected MockNetwork m_network;
    protected NotificationAnticipator m_anticipator;

    protected void setUp() throws Exception {
        super.setUp();
    
        MockUtil.println("################# Running Test "+getName()+" ################");
        MockLogAppender.setupLogging();
        
        m_network = createMockNetwork();
        
        m_db = createDatabase(m_network);
    
        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        
        m_notifdConfig = new MockNotifdConfigManager(NOTIFD_CONFIG_MANAGER);
        m_notifdConfig.setNextNotifIdSql(m_db.getNextNotifIdSql());
        
        m_groupManager = createGroupManager();
        m_userManager = createUserManager(m_groupManager);
        
        m_destinationPathManager = new MockDestinationPathManager(DESTINATION_PATHS);        
        m_notificationCommandManger = new MockNotificationCommandManager(NOTIFICATION_COMMANDS);
        m_notificationManager = new MockNotificationManager(m_notifdConfig, m_db, NOTIFICATIONS);
        
        m_anticipator = new NotificationAnticipator();
        MockNotificationStrategy.setAnticpator(m_anticipator);
        
        m_notifd = new Notifd();
        m_notifd.setEventManager(m_eventMgr);
        m_notifd.setConfigManager(m_notifdConfig);
        m_notifd.setGroupManager(m_groupManager);
        m_notifd.setUserManager(m_userManager);
        m_notifd.setDestinationPathManager(m_destinationPathManager);
        m_notifd.setNotificationCommandManager(m_notificationCommandManger);
        m_notifd.setNotificationManager(m_notificationManager);
                
        m_notifd.init();
        m_notifd.start();
        
//        Date downDate = new Date();
//        anticipateNotificationsForGroup("node 2 down.", "All services are down on node 2.", "InitialGroup", downDate, 0);
//    
//        //bring node down now
//        m_eventMgr.sendEventToListeners(m_network.getNode(2).createDownEvent(downDate));
//    
//        m_anticipator.waitForAnticipated(2000);
//        
//        m_anticipator.reset();
    
        MockUtil.println("################ Finish Setup for "+getName()+" ################");

    
    }

    private MockDatabase createDatabase(MockNetwork network) {
        MockDatabase db = new MockDatabase();
        DatabaseConnectionFactory.setInstance(db);
        db.populate(network);
        return db;
    }

    private MockNetwork createMockNetwork() {
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "Router");
        network.addInterface("192.168.1.1");
        network.setIfAlias("dot1 interface alias");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addInterface("192.168.1.2");
        network.setIfAlias("dot2 interface alias");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addNode(2, "Server");
        network.addInterface("192.168.1.3");
        network.setIfAlias("dot3 interface alias");
        network.addService("ICMP");
        network.addService("HTTP");
        return network;
    }

    private MockUserManager createUserManager(MockGroupManager groupManager) throws MarshalException, ValidationException {
        return new MockUserManager(groupManager, USERS);
    }

    private MockGroupManager createGroupManager() throws MarshalException, ValidationException {
        return new MockGroupManager(GROUPS);
    }

    protected void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_notifd.stop();

        m_db.drop();
        MockNotificationStrategy.setAnticpator(null);
        MockLogAppender.assertNoWarningsOrGreater();
        super.tearDown();
    }
    
    public void testDoNothing() {
        // this is only here to ensure that we don't get an error when running AllTests
    }

    protected long anticipateNotificationsForGroup(String subject, String textMsg, String groupName, Date startTime, long interval) throws Exception {
        return anticipateNotificationsForGroup(subject, textMsg, groupName, startTime.getTime(), interval);
    }

    protected long anticipateNotificationsForGroup(String subject, String textMsg, String groupName, long startTime, long interval) throws Exception {
        Group group = m_groupManager.getGroup(groupName);
        String[] users = group.getUser();
        return anticipateNotificationsForUsers(users, subject, textMsg, startTime, interval);
    }
    
    protected long anticipateNotificationsForRole(String subject, String textMsg, String groupName, Date startTime, long interval) throws Exception {
        return anticipateNotificationsForRole(subject, textMsg, groupName, startTime.getTime(), interval);
    }

    protected long anticipateNotificationsForRole(String subject, String textMsg, String roleName, long startTime, long interval) throws MarshalException, ValidationException, IOException {
        String[] users = m_userManager.getUsersScheduledForRole(roleName, new Date(startTime));
        return anticipateNotificationsForUsers(users, subject, textMsg, startTime, interval);
    }

    protected long anticipateNotificationsForUsers(String[] users, String subject, String textMsg, long startTime, long interval) throws IOException, MarshalException, ValidationException {
        long expectedTime = startTime;
        for (int i = 0; i < users.length; i++) {
            User user = m_userManager.getUser(users[i]);
            Contact[] contacts = user.getContact();
            for (int j = 0; j < contacts.length; j++) {
                Contact contact = contacts[j];
                if ("email".equals(contact.getType())) {
                    m_anticipator.anticipateNotification(createMockNotification(expectedTime, subject, textMsg, contact.getInfo()));
                }
            }
            expectedTime += interval;
        }
        return expectedTime-interval;
    }

    protected Collection getUsersInGroup(String groupName) throws Exception {
        Group group = m_groupManager.getGroup(groupName);
        String[] users = group.getUser();
        return Arrays.asList(users);
        
    }
    
    protected void verifyAnticipated(long lastNotifyTime, long waitTime) {
        verifyAnticipated(lastNotifyTime, waitTime, 1000);
    }

    protected void verifyAnticipated(long lastNotifyTime, long waitTime, long sleepTime) {
	m_anticipator.verifyAnticipated(lastNotifyTime, waitTime, sleepTime);
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    protected  MockNotification createMockNotification(long expectedTime, String subject, String textMsg, String email) {
        MockNotification notification;
        notification = new MockNotification();
        notification.setExpectedTime(expectedTime);
        notification.setSubject(subject);
	notification.setTextMsg(textMsg);
        notification.setEmail(email);
        return notification;
    }

    protected long computeInterval() throws IOException, MarshalException, ValidationException {
        String interval = m_destinationPathManager.getPath("Intervals").getTarget(0).getInterval();
        return TimeConverter.convertToMillis(interval);
    }

}
