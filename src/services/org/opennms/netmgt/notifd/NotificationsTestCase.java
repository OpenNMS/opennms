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
import java.util.Iterator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
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
    private static final String NOTIFICATION_MANAGER = "<?xml version=\"1.0\"?>\n" + 
                "<notifications xmlns=\"http://xmlns.opennms.org/xsd/notifications\">\n" + 
                "    <header>\n" + 
                "        <rev>1.2</rev>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>localhost</mstation>\n" + 
                "    </header>\n" + 
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
                "        <text-message>All services are down on interface %interface%.</text-message>\n" + 
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
                "</notification-commands>";
    protected MockDatabase m_db;
    protected MockNetwork m_network;
    protected NotificationAnticipator m_anticipator;

    protected void setUp() throws Exception {
        super.setUp();
    
        MockUtil.println("################# Running Test "+getName()+" ################");
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
        
        Date downDate = new Date();
        anticipateNotificationsForGroup("node 2 down.", "InitialGroup", downDate, 0);
    
        //bring node down now
        m_eventMgr.sendEventToListeners(m_network.getNode(2).createDownEvent(downDate));
    
        m_anticipator.waitForAnticipated(2000);
        
        m_anticipator.reset();
    
        MockUtil.println("################ Finish Setup for "+getName()+" ################");

    
    }

    protected void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_notifd.stop();

        m_db.drop();
        MockNotificationStrategy.setAnticpator(null);
        assertTrue("Unexpected Warnings in Log", MockUtil.noWarningsOrHigherLogged());
        super.tearDown();
    }

    protected long anticipateNotificationsForGroup(String subject, String groupName, Date startTime, long interval) throws Exception {
        return anticipateNotificationsForGroup(subject, groupName, startTime.getTime(), interval);
    }

    protected long anticipateNotificationsForGroup(String subject, String groupName, long startTime, long interval) throws Exception {
        Group group = m_groupManager.getGroup(groupName);
        String[] users = group.getUser();
        long expectedTime = startTime;
        for (int i = 0; i < users.length; i++) {
            User user = m_userManager.getUser(users[i]);
            Contact[] contacts = user.getContact();
            for (int j = 0; j < contacts.length; j++) {
                Contact contact = contacts[j];
                if ("email".equals(contact.getType())) {
                    m_anticipator.anticipateNotification(createMockNotification(expectedTime, subject, contact.getInfo()));
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

    private void verifyAnticipated(int waitTime) {
        verifyAnticipated(0, waitTime);
    }

    protected void verifyAnticipated(long lastNotifyTime, long waitTime) {
        verifyAnticipated(lastNotifyTime, waitTime, 1000);
    }

    protected void verifyAnticipated(long lastNotifyTime, long waitTime, long sleepTime) {
        long totalWaitTime = Math.max(0, lastNotifyTime + waitTime - System.currentTimeMillis());
        
        Collection missingNotifications = m_anticipator.waitForAnticipated(totalWaitTime);
        printNotifications("Missing notifications", missingNotifications);
        assertEquals("Expected notifications not forthcoming.", 0, missingNotifications.size());
        // make sure that we didn't start before we should have
        long now = System.currentTimeMillis();
        MockUtil.println("Expected notifications no sooner than "+lastNotifyTime+", currentTime is "+now);
        assertTrue("Anticipated notifications received before expected start time", now > lastNotifyTime);
        sleep(sleepTime);
        printNotifications("Unexpected notifications", m_anticipator.getUnanticipated());
        assertEquals("Unexpected notifications forthcoming.", 0, m_anticipator.getUnanticipated().size());
    }

    /**
     * @param missingNotifications
     */
    protected void printNotifications(String prefix, Collection missingNotifications) {
        for (Iterator it = missingNotifications.iterator(); it.hasNext();) {
            MockNotification notification = (MockNotification) it.next();
            MockUtil.println(prefix+": "+notification);
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    protected  MockNotification createMockNotification(long expectedTime, String subject, String email) {
        MockNotification notification;
        notification = new MockNotification();
        notification.setExpectedTime(expectedTime);
        notification.setSubject(subject);
        notification.setEmail(email);
        return notification;
    }

    protected long computeInterval() throws IOException, MarshalException, ValidationException {
        String interval = m_destinationPathManager.getPath("Intervals").getTarget(0).getInterval();
        return TimeConverter.convertToMillis(interval);
    }

}
