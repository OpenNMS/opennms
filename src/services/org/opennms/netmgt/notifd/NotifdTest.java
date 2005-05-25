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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockService;
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
import org.opennms.netmgt.utils.RowProcessor;
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
            "        <text-message>All services are down on interface %interface%, %ifalias%.</text-message>\n" + 
//          "        <text-message>All services are down on interface %interface%.</text-message>\n" + 
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
            "        <rule>IPADDR IPLIKE *.*.*.*</rule>\n" + 
            "        <destinationPath>NoEscalate</destinationPath>\n" + 
            "        <text-message>Timestamp: %time%.</text-message>\n" + 
            "        <subject>time %time%.</subject>\n" + 
            "        <numeric-message>333-%noticeid%</numeric-message>\n" + 
            "    </notification>\n" + 
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
    private MockDatabase m_db;
    private MockNetwork m_network;
    private NotificationAnticipator m_anticipator;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.println("################# Running Test "+getName()+" ################");
        MockUtil.setupLogging();
        MockUtil.resetLogLevel();
        
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
		m_network.setIfAlias("dot1 interface alias");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
		m_network.setIfAlias("dot2 interface alias");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        
        m_db = new MockDatabase();
		DatabaseConnectionFactory.setInstance(m_db);
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
        
        Date downDate = new Date();
        anticipateNotificationsForGroup("node 2 down.", "All services are down on node 2.", "InitialGroup", downDate, 0);

        //bring node down now
        m_eventMgr.sendEventToListeners(m_network.getNode(2).createDownEvent(downDate));

        m_anticipator.waitForAnticipated(2000);
        
        m_anticipator.reset();
		
        MockUtil.println("################ Finish Setup for "+getName()+" ################");
        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        m_eventMgr.finishProcessingEvents();
        m_notifd.stop();
        m_db.drop();
        MockNotificationStrategy.setAnticpator(null);
        assertTrue("Unexpected Warnings in Log", MockUtil.noWarningsOrHigherLogged());
    }
    
    
    /**
     * see http://bugzilla.opennms.org/cgi-bin/bugzilla/show_bug.cgi?id=1022
     * @throws Exception
     */
    public void testWicktorBug_1022_1031() throws Exception {
        
        Date date = new Date();
        
        long finished = anticipateNotificationsForGroup("High loadavg5 Threshold exceeded", "High loadavg5 Threshold exceeded on 192.168.1.1, loadavg5 with ", "InitialGroup", date, 0);

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");
        Event e = MockUtil.createInterfaceEvent("test", "uei.opennms.org/threshold/highThresholdExceeded", iface);
        MockUtil.setEventTime(e, date);
        MockUtil.addEventParm(e, "ds", "loadavg5");
        m_eventMgr.sendEventToListeners(e);
        
        /*
         * This is the notification config that Wicktor sent when reporting this bug.
         * 
         * We need to create and Threshold Exceeded event for loadavg5 to match his
         * notification name = "SNMP High loadavg5 Threshold Exceeded" correctly parsing the varbind.
         * 
         * What happens (he sent us a patch for this) is that the code does a return instead of a continue
         * when going through the notification names.
         */
        
        verifyAnticipated(finished, 500);
        
        
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

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("node 1 down.", "All services are down on node 1.", "InitialGroup", downDate, 0);

        //bring node down now
        m_eventMgr.sendEventToListeners(node.createDownEvent(downDate));

        verifyAnticipated(finishedDowns, 500);
        
        m_anticipator.reset();
        
        Date upDate = new Date();
        anticipateNotificationsForGroup("RESOLVED: node 1 down.", "RESOLVED: All services are down on node 1.", "InitialGroup", upDate, 0);
        long finishedUps = anticipateNotificationsForGroup("node 1 up.", "The node which was previously down is now up.", "UpGroup", upDate, 0);

        //bring node back up now
        m_eventMgr.sendEventToListeners(node.createUpEvent(upDate));

        verifyAnticipated(finishedUps, 500);

    }
    
    public void testMockNotificationInitialDelay() throws Exception {

        m_destinationPathManager.getPath("NoEscalate").setInitialDelay("1800ms");
        
        MockNode node = m_network.getNode(1);

        Date downDate = new Date(new Date().getTime()+1800);
        long finished = anticipateNotificationsForGroup("node 1 down.", "All services are down on node 1.", "InitialGroup", downDate, 0);

        m_eventMgr.sendEventToListeners(node.createDownEvent(downDate));

        verifyAnticipated(finished, 500);

    }
    
    public void testInterval() throws Exception {
        
        MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        
        Date date = new Date();
        
        long interval = computeInterval();

        long endTime = anticipateNotificationsForGroup("service ICMP on 192.168.1.1 down.", "Service ICMP is down on interface 192.168.1.1.", "InitialGroup", date, interval);

        m_eventMgr.sendEventToListeners(svc.createDownEvent(date));
        
        verifyAnticipated(endTime, 500);
        
    }
    

    public void testEscalate() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date now = new Date();

//        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1.", "InitialGroup", now, 0);
//        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1.", "EscalationGroup", now.getTime()+2500, 0);
        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", now, 0);
        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "EscalationGroup", now.getTime()+2500, 0);

        m_eventMgr.sendEventToListeners(iface.createDownEvent(now));

        verifyAnticipated(endTime, 2000);
    }
    
    public void testManualAcknowledge1() throws Exception {

        m_destinationPathManager.getPath("NoEscalate").setInitialDelay("2000ms");
        
        MockNode node = m_network.getNode(1);
        
        Event e = node.createDownEvent();

        m_eventMgr.sendEventToListeners(e);

        m_db.acknowledgeNoticesForEvent(e);
  
        verifyAnticipated(0, 0, 5000);
    }

    public void testManualAcknowledge2() throws Exception {

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.",  "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        m_db.acknowledgeNoticesForEvent(event);
        sleep(5000);

        verifyAnticipated(finishedDowns, 500);
                
    }

    public void testAutoAcknowledge1() throws Exception {

        m_destinationPathManager.getPath("NoEscalate").setInitialDelay("2000ms");
        
        MockNode node = m_network.getNode(1);
        
        Event downEvent = node.createDownEvent();

        m_eventMgr.sendEventToListeners(downEvent);
        
        sleep(1000);
        Date date = new Date();
        Event upEvent = node.createUpEvent(date);
        long endTime = anticipateNotificationsForGroup("node 1 up.", "The node which was previously down is now up.", "UpGroup", date, 0);
        
        m_eventMgr.sendEventToListeners(upEvent);
                
        verifyAnticipated(endTime, 500, 5000);
    }

    public void testAutoAcknowledge2() throws Exception {

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        Date date = new Date();
        Event upEvent = iface.createUpEvent(date);
        anticipateNotificationsForGroup("RESOLVED: interface 192.168.1.1 down.", "RESOLVED: All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", date, 0);
        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 up.", "The interface which was previously down is now up.", "UpGroup", date, 0);
        
        m_eventMgr.sendEventToListeners(upEvent);
                
        verifyAnticipated(endTime, 500, 5000);
                
    }

    /**
     * see http://bugzilla.opennms.org/cgi-bin/bugzilla/show_bug.cgi?id=731
     * @throws Exception
     */
    public void testBug731() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        Date date = new Date();
        Event upEvent = iface.createUpEvent(date);
        anticipateNotificationsForGroup("RESOLVED: interface 192.168.1.1 down.", "RESOLVED: All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", date, 0);
        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 up.", "The interface which was previously down is now up.", "UpGroup", date, 0);
        m_eventMgr.sendEventToListeners(upEvent);
        verifyAnticipated(endTime, 500, 5000);
        
    }
    
    public void testBug1114() throws Exception {
		// XXX Needing to bump up this number is bogus
    		m_anticipator.setExpectedDifference(5000);
    		
        MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        
        long interval = computeInterval();

        Event event = MockUtil.createServiceEvent("Test", "uei.opennms.org/tests/nodeTimeTest", svc);
        
        Date date = EventConstants.parseToDate(event.getTime());
        String dateString = DateFormat.getDateTimeInstance(DateFormat.FULL,
        		DateFormat.FULL).format(date);
        long endTime = anticipateNotificationsForGroup("time " + dateString + ".", "Timestamp: " + dateString + ".", "InitialGroup", date, interval);
  
        m_eventMgr.sendEventToListeners(event);

		// XXX Needing to decrease the end time is bogus
        verifyAnticipated(endTime - 5000, 500);
    }
    
    public void testRebuildParameterMap() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        
        Collection notifIds = m_db.findNoticesForEvent(event);
        
        Notification[] notification = m_notificationManager.getNotifForEvent(event);
        
        int index = 0;
        for (Iterator it = notifIds.iterator(); it.hasNext(); index++) {
            Integer notifId = (Integer) it.next();
            
            Map originalMap = m_notifd.getBroadcastEventProcessor().buildParameterMap(notification[index], event, notifId.intValue());
            
            Map resolutionMap = new HashMap(originalMap);
            resolutionMap.put(NotificationManager.PARAM_SUBJECT, "RESOLVED: "+resolutionMap.get(NotificationManager.PARAM_SUBJECT));
            resolutionMap.put(NotificationManager.PARAM_TEXT_MSG, "RESOLVED: "+resolutionMap.get(NotificationManager.PARAM_TEXT_MSG));
           
            
            Map rebuiltMap = m_notifd.getBroadcastEventProcessor().rebuildParameterMap(notifId.intValue(), "RESOLVED: ");
            
            assertEquals(resolutionMap, rebuiltMap);
            
        }
    }
    
    public void testGetUsersNotified() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        
        Collection expectedResults = new LinkedList();
        Collection users = getUsersInGroup("InitialGroup");
        for (Iterator userIt = users.iterator(); userIt.hasNext();) {
            String userID = (String) userIt.next();
            List cmdList = new LinkedList();
            cmdList.add(userID);
            cmdList.add("mockNotifier");
            expectedResults.add(cmdList);
        }
        
        Collection notifIds = m_db.findNoticesForEvent(event);
        
        for (Iterator notifIt = notifIds.iterator(); notifIt.hasNext();) {
            Integer notifId = (Integer) notifIt.next();
            
            final Collection actualResults = new LinkedList();
            RowProcessor rp = new RowProcessor() {
                public void processRow(ResultSet rs) throws SQLException {
                    List cmdList = new LinkedList();
                    cmdList.add(rs.getString("userID"));
                    cmdList.add(rs.getString("media"));
                    actualResults.add(cmdList);
                }
            };
            m_notificationManager.forEachUserNotification(notifId.intValue(), rp);
	   
            assertEquals(expectedResults, actualResults);
        }
    }
    
    private long anticipateNotificationsForGroup(String subject, String textMsg, String groupName, Date startTime, long interval) throws Exception {
        return anticipateNotificationsForGroup(subject, textMsg, groupName, startTime.getTime(), interval);
    }        
    
    private long anticipateNotificationsForGroup(String subject, String textMsg, String groupName, long startTime, long interval) throws Exception {
        Group group = m_groupManager.getGroup(groupName);
        String[] users = group.getUser();
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
    
    private Collection getUsersInGroup(String groupName) throws Exception {
        Group group = m_groupManager.getGroup(groupName);
        String[] users = group.getUser();
        return Arrays.asList(users);
        
    }

    private void verifyAnticipated(int waitTime) {
        verifyAnticipated(0, waitTime);
    }

    private void verifyAnticipated(long lastNotifyTime, long waitTime) {
        verifyAnticipated(lastNotifyTime, waitTime, 1000);
    }

    private void verifyAnticipated(long lastNotifyTime, long waitTime, long sleepTime) {
        m_anticipator.verifyAnticipated(lastNotifyTime, waitTime, sleepTime);
    }
        
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private MockNotification createMockNotification(long expectedTime, String subject, String textMsg, String email) {
        MockNotification notification;
        notification = new MockNotification();
        notification.setExpectedTime(expectedTime);
        notification.setSubject(subject);
		notification.setTextMsg(textMsg);
        notification.setEmail(email);
        return notification;
    }

    private long computeInterval() throws IOException, MarshalException, ValidationException {
        String interval = m_destinationPathManager.getPath("Intervals").getTarget(0).getInterval();
        return TimeConverter.convertToMillis(interval);
    }

}
