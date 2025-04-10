/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.ackd;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.ackd.AckReader.AckReaderState;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.api.UserNotificationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Acknowledgment Daemon tests
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-ackd.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
@Transactional
public class AckdIT implements InitializingBean {

    @Autowired
    private AlarmDao m_alarmDao;
    
    @Autowired
    private EventDao m_eventDao;
    
    @Autowired
    private Ackd m_daemon;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private NotificationDao m_notificationDao;
    
    @Autowired
    private UserNotificationDao m_userNotificationDao;
    
    private static boolean m_populated = false;
    
    @BeforeTransaction
    public void populateDatabase() {
        try {
            if (!m_populated) {
                m_populator.populateDatabase();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
    }

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        MockLogAppender.setupLogging(props);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        Assert.assertSame("dao from populator should refer to same dao from local properties", m_populator.getAcknowledgmentDao(), m_ackDao);
    }
    
    @Test
    public void testRestartReaders() throws Exception {
        AckReader reader = m_daemon.getAckReaders().get(0);
        Reader readerConfig = m_daemon.getConfigDao().getReader("JavaMailReader");
        readerConfig.setEnabled(true);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STOPPED.equals(reader.getState()));
        
        m_daemon.restartReaders(false);
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STARTED.equals(reader.getState()));
        
        m_daemon.pauseReaders();
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.PAUSED.equals(reader.getState()));
        
        m_daemon.resumeReaders();
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.RESUMED.equals(reader.getState()));
        
        readerConfig.setEnabled(false);
        m_daemon.restartReaders(true);
        Thread.sleep(300);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STOPPED.equals(reader.getState()));
        
        m_daemon.resumeReaders();
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STOPPED.equals(reader.getState()));
        
        readerConfig.setEnabled(true);
        m_daemon.startReaders();
        Thread.sleep(300);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STARTED.equals(reader.getState()));
                
        m_daemon.destroy();
    }
    

    /**
     * Make sure the DB is not empty
     */
    @Test
    public void testDbState() {
        Assert.assertFalse(m_nodeDao.findAll().isEmpty());
    }

    
    /**
     * This tests the acknowledgment of an alarm and any related notifications.
     */
    @Test
    public void testAcknowledgeAlarm() {
        
        VerificationObject vo = createAckStructure();
        Assert.assertTrue(vo.m_nodeId > 0);
        Assert.assertTrue(vo.m_alarmId > 0);
        Assert.assertTrue(vo.m_eventID > 0);
        Assert.assertTrue(vo.m_userNotifId > 0);
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);

        OnmsAcknowledgment ack = new OnmsAcknowledgment(m_alarmDao.get(vo.m_alarmId));

        m_ackDao.save(ack);
        m_ackDao.flush();
        
        m_ackDao.processAck(ack);
        
        alarm = m_alarmDao.get(ack.getRefId());
        Assert.assertNotNull(alarm.getAlarmAckUser());
        Assert.assertEquals("admin", alarm.getAlarmAckUser());
        
        OnmsNotification notif = m_notificationDao.get(vo.m_notifId);
        Assert.assertNotNull(notif);
        Assert.assertEquals("admin", notif.getAnsweredBy());
        
        Assert.assertTrue(alarm.getAlarmAckTime().before(notif.getRespondTime()));
        
    }
    
    
    /**
     * This tests acknowledging a notification and a related alarm.  If events are being deduplicated
     * they should all have the same alarm ID.
     * @throws InterruptedException 
     */
    @Test
    public void testAcknowledgeNotification() throws InterruptedException {
        
        VerificationObject vo = createAckStructure();
        Assert.assertTrue(vo.m_nodeId > 0);
        Assert.assertTrue(vo.m_alarmId > 0);
        Assert.assertTrue(vo.m_eventID > 0);
        Assert.assertTrue(vo.m_userNotifId > 0);
        
        OnmsAcknowledgment ack = new OnmsAcknowledgment(m_notificationDao.get(vo.m_notifId));

        m_ackDao.save(ack);
        m_ackDao.flush();
        
        Thread.sleep(1);
        m_ackDao.processAck(ack);
        
        OnmsNotification notif = m_notificationDao.get(ack.getRefId());
        Assert.assertNotNull(notif.getAnsweredBy());
        Assert.assertEquals("admin", notif.getAnsweredBy());
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);
        Assert.assertNotNull(alarm);
        Assert.assertEquals("admin", alarm.getAlarmAckUser());
        
        long ackTime = ack.getAckTime().getTime();
        long respondTime = notif.getRespondTime().getTime();
        
        //the DAOs now set the acknowledgment time for each Acknowledgable and should
        //be later (by a few millis in this test) than the time the acknowledgment was created
        //this will give us an idea about the processing time of an acknowledgment
        Assert.assertTrue(ackTime < respondTime);
        
    }
    
    /*
     * Send Ackd Notification Event for the ALARM
     * ackAction is the default Acknowledge
     * this will led to ack both the notification and the alarm
     *  
     */
    @Test
    public void testHandleEvent() throws InterruptedException {
        
        VerificationObject vo = createAckStructure();
        EventBuilder bldr = new EventBuilder(EventConstants.ACKNOWLEDGE_EVENT_UEI, "AckdTest");
        bldr.addParam("ackType", String.valueOf(AckType.ALARM));
        bldr.addParam("refId", vo.m_alarmId);
        final String user = "ackd-test-user";
        bldr.addParam("ackUser", user);

        m_daemon.handleAckEvent(ImmutableMapper.fromMutableEvent(bldr.getEvent()));
        
        OnmsNotification notif = m_notificationDao.get(vo.m_notifId);
        Assert.assertEquals(notif.getAckUser(), user);
//        Assert.assertEquals(notif.getAckTime(), bldr.getEvent().getTime());
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);
        Assert.assertEquals(alarm.getAckUser(), user);
//        Assert.assertEquals(alarm.getAckTime(), bldr.getEvent().getTime());
    }

    /*
     * Send Ackd Notification Event for the NOTIFICATION
     * ackAction is the default Acknowledge
     * this will led to ack both the notification and the alarm
     *  
     */
    @Test
    public void testHandleEventNotificationAck() throws InterruptedException {
        
        VerificationObject vo = createAckStructure();
        EventBuilder bldr = new EventBuilder(EventConstants.ACKNOWLEDGE_EVENT_UEI, "AckdTest");
        bldr.addParam("ackType", String.valueOf(AckType.NOTIFICATION));
        bldr.addParam("refId", vo.m_alarmId);
        final String user = "ackd-test-user";
        bldr.addParam("ackUser", user);

        m_daemon.handleAckEvent(ImmutableMapper.fromMutableEvent(bldr.getEvent()));
        
        OnmsNotification notif = m_notificationDao.get(vo.m_notifId);
        Assert.assertEquals(notif.getAckUser(), user);
//        Assert.assertEquals(notif.getAckTime(), bldr.getEvent().getTime());
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);
        Assert.assertEquals(alarm.getAckUser(), user);
//        Assert.assertEquals(alarm.getAckTime(), bldr.getEvent().getTime());
    }

    /*
     * Send Ackd Notification Event for the ALARM
     * AckAction is ESCALATE
     * this will led only to escalate the alarm
     *  
     */
    @Test
    public void testHandleEventEscalate() throws InterruptedException {
        
        VerificationObject vo = createAckStructure();
        assertEquals(OnmsSeverity.MAJOR.getId(), vo.m_alarmSeverity);
        EventBuilder bldr = new EventBuilder(EventConstants.ACKNOWLEDGE_EVENT_UEI, "AckdTest");
        bldr.addParam("ackType", String.valueOf(AckType.ALARM));
        bldr.addParam("ackAction", String.valueOf(AckAction.ESCALATE));
        bldr.addParam("refId", vo.m_alarmId);
        final String user = "ackd-test-user";
        bldr.addParam("ackUser", user);

        m_daemon.handleAckEvent(ImmutableMapper.fromMutableEvent(bldr.getEvent()));
        
        OnmsNotification notif = m_notificationDao.get(vo.m_notifId);
        Assert.assertEquals(notif.getAckUser(), null);
//        Assert.assertEquals(notif.getAckTime(), bldr.getEvent().getTime());
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);
        Assert.assertEquals(alarm.getAckUser(), null);
        Assert.assertEquals(OnmsSeverity.CRITICAL.getId(), alarm.getSeverityId().intValue());
//        Assert.assertEquals(alarm.getAckTime(), bldr.getEvent().getTime());
    }

    /*
     * Send Ackd Notification Event for the ALARM
     * AckAction is CLEAR
     * this will led clear the alarm
     * and ack the notif 
     */
    @Test
    public void testHandleEventClear() throws InterruptedException {
        
        VerificationObject vo = createAckStructure();
        assertEquals(OnmsSeverity.MAJOR.getId(), vo.m_alarmSeverity);
        EventBuilder bldr = new EventBuilder(EventConstants.ACKNOWLEDGE_EVENT_UEI, "AckdTest");
        bldr.addParam("ackType", String.valueOf(AckType.ALARM));
        bldr.addParam("ackAction", String.valueOf(AckAction.CLEAR));
        bldr.addParam("refId", vo.m_alarmId);
        final String user = "ackd-test-user";
        bldr.addParam("ackUser", user);

        m_daemon.handleAckEvent(ImmutableMapper.fromMutableEvent(bldr.getEvent()));
        
        OnmsNotification notif = m_notificationDao.get(vo.m_notifId);
        Assert.assertEquals(notif.getAckUser(), user);
//        Assert.assertEquals(notif.getAckTime(), bldr.getEvent().getTime());
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);
        Assert.assertEquals(alarm.getAckUser(), null);
        Assert.assertEquals(OnmsSeverity.CLEARED.getId(), alarm.getSeverityId().intValue());
//        Assert.assertEquals(alarm.getAckTime(), bldr.getEvent().getTime());
    }

    class VerificationObject {
        long m_eventID;
        int m_alarmId;
        int m_nodeId;
        int m_notifId;
        int m_userNotifId;
        int m_alarmSeverity;
    }
    
    private VerificationObject createAckStructure() {
        
        final Date time = new Date();
        VerificationObject vo = new VerificationObject();
        vo.m_alarmSeverity = OnmsSeverity.MAJOR.getId();
        List<OnmsNode> nodes = m_nodeDao.findAll();
        Assert.assertTrue("List of nodes should not be empty", nodes.size() > 0);
        OnmsNode node = m_nodeDao.get(nodes.get(0).getId());
        vo.m_nodeId = node.getId();
                
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_populator.getDistPollerDao().whoami());
        event.setNode(node);
        
        event.setEventCreateTime(time);
        event.setEventDescr("Test node down event.");
        event.setEventSeverity(vo.m_alarmSeverity);
        event.setEventSource("AckdTest");
        event.setEventTime(time);
        event.setEventUei(EventConstants.NODE_DOWN_EVENT_UEI);
        event.setIpAddr(node.getPrimaryInterface().getIpAddress());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogMsg("Testing node down event from AckdTest.");
        m_eventDao.save(event);
        m_eventDao.flush();
        vo.m_eventID = event.getId();
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
        alarm.setClearKey(EventConstants.NODE_UP_EVENT_UEI + ":localhost:1");
        alarm.setCounter(1);
        alarm.setDescription(event.getEventDescr());
        alarm.setDistPoller(event.getDistPoller());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setIpAddr(event.getIpAddr());
        alarm.setLastEvent(event);
        alarm.setLastEventTime(event.getEventTime());
        alarm.setLogMsg("Some Log Message");
        alarm.setNode(event.getNode());
        alarm.setReductionKey("xyz");
        alarm.setServiceType(event.getServiceType());
        alarm.setSeverity(OnmsSeverity.get(event.getEventSeverity()));
        alarm.setUei(event.getEventUei());
        m_alarmDao.save(alarm);
        m_alarmDao.flush();
        vo.m_alarmId = alarm.getId();
        event.setAlarm(alarm);
        
        OnmsNotification notif = new OnmsNotification();
        notif.setEvent(event);
        notif.setEventUei(event.getEventUei());
        notif.setIpAddress(event.getIpAddr());
        notif.setNode(event.getNode());
        notif.setNotifConfigName("abc");
        notif.setNumericMsg(event.getEventLogMsg());
        notif.setPageTime(event.getEventTime());
        notif.setServiceType(event.getServiceType());
        notif.setSubject("notifyid: 1, node down");
        notif.setTextMsg(event.getEventLogMsg());
        m_notificationDao.save(notif);
        vo.m_notifId = notif.getNotifyId();
        
        OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setAutoNotify("Y");
        userNotif.setContactInfo("me@yourock.com");
        userNotif.setMedia("page");
        userNotif.setNotification(notif);
        userNotif.setNotifyTime(event.getEventTime());
        userNotif.setUserId("me");
        
        Set<OnmsUserNotification> usersnotifieds = new HashSet<>();
        usersnotifieds.add(userNotif);
        m_userNotificationDao.save(userNotif);
        vo.m_userNotifId = userNotif.getId();
        
        notif.setUsersNotified(usersnotifieds);
        m_notificationDao.update(notif);
        
        m_eventDao.update(event);
        m_eventDao.flush();
        
        return vo;
    }
    
    
}
