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

import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.vacuumd.Vacuumd;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
    private NodeDao m_nodeDao;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    private MockDatabase m_database;

    private EventAnticipator m_anticipator;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }

    @Before
    public void setUp() throws Exception {
        m_anticipator = new EventAnticipator();
        m_eventMgr.setEventAnticipator(m_anticipator);

        // Events need database IDs to make alarmd happy
        m_eventMgr.setEventWriter(m_database);

        // Events need to real nodes too
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("node1");
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
        m_anticipator.resetAnticipated();
        m_anticipator.anticipateEvent(new EventBuilder(EventConstants.ALARM_CREATED_UEI, "alarmd").getEvent());
        m_anticipator.setDiscardUnanticipated(true);

        // Send a nodeDown
        sendNodeDownEvent(1);

        // Wait until we've received the alarmCreated event
        await().until(allAnticipatedEventsWereReceived());

        // Expect an alarmUpdatedWithReducedEvent event
        m_anticipator.resetAnticipated();
        m_anticipator.anticipateEvent(new EventBuilder(EventConstants.ALARM_UPDATED_WITH_REDUCED_EVENT_UEI, "alarmd").getEvent());
        m_anticipator.setDiscardUnanticipated(true);

        // Send another nodeDown
        sendNodeDownEvent(1);
        
        // Wait until we've received the alarmUpdatedWithReducedEvent event
        await().until(allAnticipatedEventsWereReceived());

        // Expect an alarmCreated and a alarmCleared event
        m_anticipator.resetAnticipated();
        m_anticipator.anticipateEvent(new EventBuilder(EventConstants.ALARM_CREATED_UEI, "alarmd").getEvent());
        m_anticipator.anticipateEvent(new EventBuilder(EventConstants.ALARM_CLEARED_UEI, "alarmd").getEvent());
        m_anticipator.setDiscardUnanticipated(true);

        // Send a nodeUp
        sendNodeUpEvent(1);

        // Wait until we've received the alarmCreated and alarmCleared events
        // We need to wait for the cosmicClear automation, which currently runs every 30 seconds
        await().atMost(1, MINUTES).until(allAnticipatedEventsWereReceived());

        // Expect an alarmUncleared event
        m_anticipator.resetAnticipated();
        m_anticipator.anticipateEvent(new EventBuilder(EventConstants.ALARM_UNCLEARED_UEI, "alarmd").getEvent());
        m_anticipator.setDiscardUnanticipated(true);

        // Send another nodeDown
        sendNodeDownEvent(1);

        // Wait until we've received the alarmUncleared event
        // We need to wait for the unclear automation, which currently runs every 30 seconds
        await().atMost(1, MINUTES).until(allAnticipatedEventsWereReceived());
    }

    public Callable<Boolean> allAnticipatedEventsWereReceived() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return m_anticipator.getAnticipatedEvents().isEmpty();
            }
        };
    }

    private void sendNodeUpEvent(long nodeId) {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test");
        Date currentTime = new Date();
        builder.setCreationTime(currentTime);
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
        builder.setCreationTime(currentTime);
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
