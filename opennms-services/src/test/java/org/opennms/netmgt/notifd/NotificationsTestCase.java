/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.mock.MockDestinationPathManager;
import org.opennms.netmgt.config.mock.MockGroupManager;
import org.opennms.netmgt.config.mock.MockNotifdConfigManager;
import org.opennms.netmgt.config.mock.MockNotificationCommandManager;
import org.opennms.netmgt.config.mock.MockNotificationManager;
import org.opennms.netmgt.config.mock.MockNotificationStrategy;
import org.opennms.netmgt.config.mock.MockUserManager;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNotification;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.NotificationAnticipator;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        // Notifd
        "classpath:/META-INF/opennms/applicationContext-notifdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
@Ignore
public class NotificationsTestCase implements TemporaryDatabaseAware<MockDatabase> {

    @Autowired
    protected Notifd m_notifd;

    @Autowired
    protected BroadcastEventProcessor m_eventProcessor;

    @Autowired
    protected EventUtil m_eventUtil;

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

        m_db.populate(m_network);
        DataSourceFactory.setInstance(m_db);

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

        m_notifd.setConfigManager(m_notifdConfig);

        m_eventProcessor.setEventManager(m_eventMgr);
        m_eventProcessor.setNotifdConfigManager(m_notifdConfig);
        m_eventProcessor.setGroupManager(m_groupManager);
        m_eventProcessor.setUserManager(m_userManager);
        m_eventProcessor.setDestinationPathManager(m_destinationPathManager);
        m_eventProcessor.setNotificationCommandManager(m_notificationCommandManger);
        m_eventProcessor.setNotificationManager(m_notificationManager);
        m_eventProcessor.setPollOutagesConfigManager(m_pollOutagesConfigManager);

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

    protected MockNetwork createMockNetwork() {
        MockNetwork network = new MockNetwork();
        network.createStandardNetwork();
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

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_db = database;
    }
}
