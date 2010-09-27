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
// Modifications:
//
// 2007 Jul 03: Fix calls to ConfigurationTestUtils.getConfigForResourceWithReplacements. - dj@opennms.org
// 2007 Jul 03: Move config files to external resources and indent. - dj@opennms.org
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
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.notifd.mock.MockDestinationPathManager;
import org.opennms.netmgt.notifd.mock.MockGroupManager;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.netmgt.notifd.mock.MockNotification;
import org.opennms.netmgt.notifd.mock.MockNotificationCommandManager;
import org.opennms.netmgt.notifd.mock.MockNotificationManager;
import org.opennms.netmgt.notifd.mock.MockNotificationStrategy;
import org.opennms.netmgt.notifd.mock.MockUserManager;
import org.opennms.netmgt.notifd.mock.NotificationAnticipator;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

public class NotificationsTestCase {

    protected Notifd m_notifd;
    protected MockEventIpcManager m_eventMgr;
    protected MockNotifdConfigManager m_notifdConfig;
    protected MockGroupManager m_groupManager;
    protected MockUserManager m_userManager;
    protected NotificationManager m_notificationManager;
    protected NotificationCommandManager m_notificationCommandManger;
    protected MockDestinationPathManager m_destinationPathManager;
    protected MockDatabase m_db;
    protected MockNetwork m_network;
    protected NotificationAnticipator m_anticipator;
    private PollOutagesConfigManager m_pollOutagesConfigManager;

    protected void setUp() throws Exception {
        MockUtil.println("################# Running Test ################");

        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        MockLogAppender.setupLogging();
        
        m_network = createMockNetwork();
        
        m_db = createDatabase(m_network);
    
        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        
        m_notifdConfig = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));
        m_notifdConfig.setNextNotifIdSql(m_db.getNextNotifIdSql());
        m_notifdConfig.setNextUserNotifIdSql(m_db.getNextUserNotifIdSql());
        
        m_groupManager = createGroupManager();
        m_userManager = createUserManager(m_groupManager);
        
        m_destinationPathManager = new MockDestinationPathManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "destination-paths.xml"));        
        m_notificationCommandManger = new MockNotificationCommandManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notification-commands.xml"));
        m_notificationManager = new MockNotificationManager(m_notifdConfig, m_db, ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifications.xml"));
        m_pollOutagesConfigManager = new MockPollerConfig(m_network);
        
        m_anticipator = new NotificationAnticipator();
        MockNotificationStrategy.setAnticipator(m_anticipator);
        
        m_notifd = new Notifd();
        m_notifd.setEventManager(m_eventMgr);
        m_notifd.setConfigManager(m_notifdConfig);
        m_notifd.setGroupManager(m_groupManager);
        m_notifd.setUserManager(m_userManager);
        m_notifd.setDestinationPathManager(m_destinationPathManager);
        m_notifd.setNotificationCommandManager(m_notificationCommandManger);
        m_notifd.setNotificationManager(m_notificationManager);
        m_notifd.setPollOutagesConfigManager(m_pollOutagesConfigManager);
                
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
    
        MockUtil.println("################ Finish Setup ################");

    
    }

    protected MockDatabase createDatabase(MockNetwork network) throws Exception {
        MockDatabase db = new MockDatabase();
        DataSourceFactory.setInstance(db);
        db.populate(network);
        return db;
    }

    /**
     * TODO Use {@link MockNetwork#createStandardNetwork()} instead?
     * @return
     */
    protected MockNetwork createMockNetwork() {
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

    private MockUserManager createUserManager(MockGroupManager groupManager) throws MarshalException, ValidationException, IOException {
        return new MockUserManager(groupManager, ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "users.xml"));
    }

    private MockGroupManager createGroupManager() throws MarshalException, ValidationException, IOException {
        return new MockGroupManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "groups.xml"));
    }
    
    protected void tearDown() throws Exception {
        this.tearDown(false);
    }

    protected void tearDown(boolean allowAllLogMessages) throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_notifd.stop();

        // m_db.drop();
        MockNotificationStrategy.setAnticipator(null);
        if (!allowAllLogMessages) {
            MockLogAppender.assertNoWarningsOrGreater();
        }
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

    protected Collection<String> getUsersInGroup(String groupName) throws Exception {
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
