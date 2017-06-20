/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.vacuumd.Vacuumd;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Used to verify that alarmd and vacuumd generate alarm
 * life-cycle events when creating or updating alarms.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
public class AlarmLifecycleEventsIT implements TemporaryDatabaseAware<MockDatabase> {

    @Autowired
    private Alarmd m_alarmd;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private MockDatabase m_database;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }

    @Before
    public void setUp() throws Exception {

        // Events need database IDs to make alarmd happy
        m_eventMgr.setEventWriter(m_database);

        // Events need to real nodes too
        final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);

        Vacuumd.destroySingleton();
        Vacuumd vacuumd = Vacuumd.getSingleton();
        vacuumd.setEventManager(m_eventMgr);
        vacuumd.init();
        vacuumd.start();
    }

    @After
    public void tearDown() throws Exception {
        m_alarmd.destroy();
    }

    @Test
    public void canGenerateAlarmLifecycleEvents() {
        // Expect an alarmCreated event
        m_eventMgr.getEventAnticipator().resetAnticipated();
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_CREATED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // Send a nodeDown
        sendNodeDownEvent(1);

        // Wait until we've received the alarmCreated event
        await().until(allAnticipatedEventsWereReceived());

        // Expect an alarmUpdatedWithReducedEvent event
        m_eventMgr.getEventAnticipator().resetAnticipated();
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_UPDATED_WITH_REDUCED_EVENT_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // Send another nodeDown
        sendNodeDownEvent(1);
        
        // Wait until we've received the alarmUpdatedWithReducedEvent event
        await().until(allAnticipatedEventsWereReceived());

        // Expect an alarmCreated and a alarmCleared event
        m_eventMgr.getEventAnticipator().resetAnticipated();
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_CREATED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_CLEARED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // Send a nodeUp
        sendNodeUpEvent(1);

        // Wait until we've received the alarmCreated and alarmCleared events
        // We need to wait for the cosmicClear automation, which currently runs every 30 seconds
        await().atMost(1, MINUTES).until(allAnticipatedEventsWereReceived());

        // Expect an alarmUncleared event
        m_eventMgr.getEventAnticipator().resetAnticipated();
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_UNCLEARED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // Send another nodeDown
        sendNodeDownEvent(1);

        // Wait until we've received the alarmUncleared event
        // We need to wait for the unclear automation, which currently runs every 30 seconds
        await().atMost(1, MINUTES).until(allAnticipatedEventsWereReceived());
    }

    @Test
    public void canGenerateAlarmDeletedLifecycleEvents() {
        // Expect an alarmCreated event
        m_eventMgr.getEventAnticipator().resetAnticipated();
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_CREATED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // Send a nodeDown
        sendNodeDownEvent(1);

        // Wait until we've received the alarmCreated event
        await().until(allAnticipatedEventsWereReceived());

        // Expect an alarmCreated and a alarmCleared event
        m_eventMgr.getEventAnticipator().resetAnticipated();
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_CREATED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_CLEARED_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // Send a nodeUp
        sendNodeUpEvent(1);

        // Wait until we've received the alarmCreated and alarmCleared events
        // We need to wait for the cosmicClear automation, which currently runs every 30 seconds
        await().atMost(1, MINUTES).until(allAnticipatedEventsWereReceived());

        // Expect an alarmDeleted event
        m_eventMgr.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.ALARM_DELETED_EVENT_UEI, "alarmd").getEvent());
        m_eventMgr.getEventAnticipator().setDiscardUnanticipated(true);

        // We need to wait for the cleanUp automation, which currently runs every 60 seconds
        // but it will only trigger then 'lastautomationtime' and 'lasteventtime' < "5 minutes ago"
        // so we cheat a little and update the timestamps ourselves instead of waiting
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Criteria criteria = new Criteria(OnmsAlarm.class);
                criteria.addRestriction(new EqRestriction("node.id", 1));
                criteria.addRestriction(new EqRestriction("uei", EventConstants.NODE_DOWN_EVENT_UEI));
                for (OnmsAlarm alarm : m_alarmDao.findMatching(criteria)) {
                    LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
                    Date then = Date.from(tenMinutesAgo.toInstant(ZoneOffset.UTC));
                    alarm.setLastAutomationTime(then);
                    alarm.setLastEventTime(then);
                    m_alarmDao.save(alarm);
                }
                m_alarmDao.flush();
            }
        });

        // Wait until we've received the alarmDeleted event
        await().atMost(2, MINUTES).until(allAnticipatedEventsWereReceived());
    }

    public Callable<Boolean> allAnticipatedEventsWereReceived() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return m_eventMgr.getEventAnticipator().getAnticipatedEvents().isEmpty();
            }
        };
    }

    private void sendNodeUpEvent(long nodeId) {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test");
        Date currentTime = new Date();
        builder.setTime(currentTime);
        builder.setNodeid(nodeId);
        builder.setSeverity("Normal");

        AlarmData data = new AlarmData();
        data.setAlarmType(2);
        data.setReductionKey(String.format("%s:%d", EventConstants.NODE_UP_EVENT_UEI, nodeId));
        data.setClearKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId));
        builder.setAlarmData(data);

        builder.setLogDest("logndisplay");
        builder.setLogMessage("testing");

        m_eventMgr.sendNow(builder.getEvent());
    }

    private void sendNodeDownEvent(long nodeId) {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, "test");
        Date currentTime = new Date();
        builder.setTime(currentTime);
        builder.setNodeid(nodeId);
        builder.setSeverity("Major");

        AlarmData data = new AlarmData();
        data.setAlarmType(1);
        data.setReductionKey(String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId));
        builder.setAlarmData(data);

        builder.setLogDest("logndisplay");
        builder.setLogMessage("testing");

        m_eventMgr.sendNow(builder.getEvent());
    }
}
