/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ackd;

import java.util.Calendar;
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
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class DefaultAckServiceTest implements InitializingBean {

    @Autowired AcknowledgmentDao m_ackDao;

    @Autowired NotificationDao m_notifDao;
    
    @Autowired AlarmDao m_alarmDao;
    
    @Autowired EventDao m_eventDao;
    
    @Autowired NodeDao m_nodeDao;

    @Autowired DatabasePopulator m_populator;
    
    @Before public void createDb() {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        MockLogAppender.setupLogging(props);

        m_populator.populateDatabase();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test(expected=IllegalStateException.class)
    public void notificationWithMissingAlarm() {
        
        OnmsNode dbNode = m_nodeDao.get(m_populator.getNode1().getId());
        OnmsEvent event = getEvent(dbNode);
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
        alarm.setDescription(event.getEventDescr());
        alarm.setDistPoller(event.getDistPoller());
        alarm.setEventParms(event.getEventParms());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setIfIndex(event.getIfIndex());
        alarm.setIpAddr(event.getIpAddr());
        alarm.setLastEvent(event);
        alarm.setLastEventTime(event.getEventTime());
        alarm.setLogMsg(event.getEventLogMsg());
        alarm.setMouseOverText(event.getEventMouseOverText());
        alarm.setNode(dbNode);
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setUei(event.getEventUei());
        alarm.setCounter(1);
        m_alarmDao.save(alarm);
        m_alarmDao.flush();

        getNotification(event);

        OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setRefId(alarm.getAckId());
        ack.setAckType(alarm.getType());

        m_alarmDao.delete(alarm);
        m_alarmDao.flush();

        m_ackDao.processAck(ack);
    }
 
    @Test 
    public void processAck() {
        
        OnmsNode dbNode = m_nodeDao.get(m_populator.getNode1().getId());
        OnmsEvent event = getEvent(dbNode);
        OnmsNotification notif = getNotification(event);
        // OnmsUserNotification un = getUserNotification(notif);
        
        Assert.assertTrue(m_notifDao.countAll() > 0);
        
        List<OnmsNotification> notifs = m_notifDao.findAll();
        Assert.assertTrue((notifs.contains(notif)));
        
        OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setRefId(notif.getNotifyId());
        ack.setAckType(AckType.NOTIFICATION);
        m_ackDao.processAck(ack);
        
        List<Acknowledgeable> ackables = m_ackDao.findAcknowledgables(ack);
        Assert.assertEquals(1, ackables.size());
        Acknowledgeable ackable = ackables.get(0);
        Assert.assertEquals("admin", ackable.getAckUser());
        
    }

    @SuppressWarnings("unused")
    private OnmsUserNotification getUserNotification(OnmsNotification notif) {
        OnmsUserNotification un = new OnmsUserNotification();
        un.setUserId("admin");
        un.setNotification(notif);
        Set<OnmsUserNotification> usersNotified = new HashSet<OnmsUserNotification>();
        usersNotified.add(un);
        notif.setUsersNotified(usersNotified);
        m_notifDao.save(notif);
        m_notifDao.flush();
        
        return un;
    }

    private OnmsNotification getNotification(OnmsEvent event) {
        OnmsNotification notif = new OnmsNotification();
        notif.setEvent(event);
        notif.setNode(event.getNode());
        notif.setNumericMsg("123456");
        notif.setPageTime(Calendar.getInstance().getTime());
        notif.setSubject("ackd notif test");
        notif.setTextMsg("ackd notif test");
        m_notifDao.save(notif);
        m_notifDao.flush();
        
        return notif;
    }

    private OnmsEvent getEvent(OnmsNode node) {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(node.getDistPoller());
        event.setEventUei(EventConstants.NODE_DOWN_EVENT_UEI);
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setNode(node);
        m_eventDao.save(event);
        m_eventDao.flush();
        return event;
    }

}
