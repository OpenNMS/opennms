/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ackd;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.ackd.AckReader.AckReaderState;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.JavaMailConfigurationDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.dao.UserNotificationDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.opennms.netmgt.model.acknowledgments.AckService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-ackd.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})

/**
 * Acknowledgment Daemon tests
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@JUnitTemporaryDatabase(populate=true)
@Transactional
public class AckdTest {

    @Autowired
    private AckService m_ackService;
    
    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
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
    
    @Autowired
    private JavaMailConfigurationDao m_jmConfigDao;


    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        
        m_populator.populateDatabase();
    }
    
    @Test
    public void testWiring() {
        Assert.assertNotNull(m_ackDao);
        Assert.assertNotNull(m_alarmDao);
        Assert.assertNotNull(m_eventDao);
        Assert.assertNotNull(m_nodeDao);
        Assert.assertNotNull(m_notificationDao);
        Assert.assertNotNull(m_userNotificationDao);
        Assert.assertNotNull(m_mockEventIpcManager);
        Assert.assertNotNull(m_ackService);
        Assert.assertNotNull(m_daemon);
        Assert.assertNotNull(m_populator);
        Assert.assertNotNull(m_jmConfigDao);
        
        Assert.assertSame("dao from populator should refer to same dao from local properties", m_populator.getAcknowledgmentDao(), m_ackDao);
    }
    
    @Test
    public void testRestartReaders() throws Exception {
        AckReader reader = m_daemon.getAckReaders().get(0);
        Reader readerConfig = m_daemon.getConfigDao().getReader("JavaMailReader");
        readerConfig.setEnabled(true);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STOPPED.equals(reader.getState()));
        
        m_daemon.restartReaders();
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.STARTED.equals(reader.getState()));
        
        m_daemon.pauseReaders();
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.PAUSED.equals(reader.getState()));
        
        m_daemon.resumeReaders();
        Thread.sleep(30);
        Assert.assertTrue("Unexpected reader state: "+reader.getState(), AckReaderState.RESUMED.equals(reader.getState()));
        
        readerConfig.setEnabled(false);
        m_daemon.restartReaders();
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
        
        m_ackService.processAck(ack);
        
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
        m_ackService.processAck(ack);
        
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
    
    @Test
    public void testHandelEvent() throws InterruptedException {
        
        VerificationObject vo = createAckStructure();
        EventBuilder bldr = new EventBuilder("uei.opennms.org/internal/ackd/Acknowledge", "AckdTest");
        bldr.addParam("ackType", String.valueOf(AckType.ALARM));
        bldr.addParam("refId", vo.m_alarmId);
        final String user = "ackd-test-user";
        bldr.addParam("user", user);

        m_daemon.handleAckEvent(bldr.getEvent());
        
        OnmsNotification notif = m_notificationDao.get(vo.m_notifId);
        Assert.assertEquals(notif.getAckUser(), user);
//        Assert.assertEquals(notif.getAckTime(), bldr.getEvent().getTime());
        
        OnmsAlarm alarm = m_alarmDao.get(vo.m_alarmId);
        Assert.assertEquals(alarm.getAckUser(), user);
//        Assert.assertEquals(alarm.getAckTime(), bldr.getEvent().getTime());
    }
    
    
    class VerificationObject {
        int m_eventID;
        int m_alarmId;
        int m_nodeId;
        int m_notifId;
        int m_userNotifId;
    }
    
    private VerificationObject createAckStructure() {
        
        final Date time = new Date();
        VerificationObject vo = new VerificationObject();
        
        List<OnmsNode> nodes = m_nodeDao.findAll();
        Assert.assertTrue("List of nodes should not be empty", nodes.size() > 0);
        OnmsNode node = m_nodeDao.get(nodes.get(0).getId());
        vo.m_nodeId = node.getId();
                
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(node.getDistPoller());
        event.setNode(node);
        
        event.setEventCreateTime(time);
        event.setEventDescr("Test node down event.");
        event.setEventSeverity(6);
        event.setEventSource("AckdTest");
        event.setEventTime(time);
        event.setEventUei(EventConstants.NODE_DOWN_EVENT_UEI);
        event.setIpAddr(node.getPrimaryInterface().getIpAddress());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogMsg("Testing node down event from AckdTest.");
        m_eventDao.save(event);
        vo.m_eventID = event.getId();
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setAlarmType(1);
        alarm.setClearKey("abc");
        alarm.setClearUei(EventConstants.NODE_UP_EVENT_UEI);
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
        
        Set<OnmsUserNotification> usersnotifieds = new HashSet<OnmsUserNotification>();
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
