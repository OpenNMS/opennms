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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.opennms.netmgt.alarmd.AlarmMatchers.hasCounter;
import static org.opennms.netmgt.alarmd.AlarmMatchers.hasSeverity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
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
@JUnitConfigurationEnvironment(systemProperties={
        // Reduce the default snapshot interval so that the tests can finish in a reasonable time
        AlarmLifecycleListenerManager.ALARM_SNAPSHOT_INTERVAL_MS_SYS_PROP+"=5000"
})
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
public class AlarmLifecycleListenerManagerIT implements TemporaryDatabaseAware<MockDatabase>, AlarmLifecycleListener {

    @Autowired
    private Alarmd m_alarmd;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AlarmDao m_alarmDao;
    
    @Autowired
    private AlarmPersisterImpl m_alarmPersisterImpl;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AlarmLifecycleListenerManager m_alarmLifecycleListenerManager;

    private MockDatabase m_database;

    private List<List<OnmsAlarm>> m_snapshots = new ArrayList<>();
    private List<OnmsAlarm> m_newOrUpdatedAlarms = new ArrayList<>();
    private List<String> m_deletedAlarms = new ArrayList<>();
    private Map<String, OnmsAlarm> m_alarmsByReductionKey = new HashMap<>();

    @BeforeClass
    public static void setUpClass() {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Before
    public void setUp() {
        // Async.
        m_eventMgr.setSynchronous(false);

        // Events need database IDs to make alarmd happy
        m_eventMgr.setEventWriter(m_database);

        // Events need to real nodes too
        final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);

        // Register!
        m_alarmLifecycleListenerManager.onListenerRegistered(this, Collections.emptyMap());
        
        m_alarmPersisterImpl.setLegacyAlarmState(true);

        // Fire it up
        m_alarmd.start();
    }

    @After
    public void tearDown() {
        m_alarmd.destroy();
    }

    @Test
    public void canIssueCreateAndUpdateCallbacks() throws Exception {
        // Send a nodeDown
        sendNodeDownEvent(1);
        await().until(getNodeDownAlarmFor(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getNodeDownAlarmFor(1).call(), hasCounter(1));

        // Send another nodeDown
        sendNodeDownEvent(1);
        await().until(getNodeDownAlarmFor(1), hasCounter(2));
        assertThat(getNodeDownAlarmFor(1).call(), hasSeverity(OnmsSeverity.MAJOR));

        // Send a nodeUp
        sendNodeUpEvent(1);
        await().until(getNodeDownAlarmFor(1), hasSeverity(OnmsSeverity.CLEARED));
        await().until(getNodeUpAlarmFor(1), hasSeverity(OnmsSeverity.NORMAL));

        // Send another nodeDown
        sendNodeDownEvent(1);
        await().until(getNodeDownAlarmFor(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getNodeDownAlarmFor(1).call(), hasCounter(3));
    }

    @Test
    public void canIssueDeleteCallbacks() {
        // Send a nodeDown
        sendNodeDownEvent(1);
        await().until(getNodeDownAlarmFor(1), hasSeverity(OnmsSeverity.MAJOR));

        // Send a nodeUp
        sendNodeUpEvent(1);
        await().until(getNodeDownAlarmFor(1), hasSeverity(OnmsSeverity.CLEARED));

        // We need to wait for the cleanUp automation which triggers when:
        //  'lastautomationtime' and 'lasteventtime' < "5 minutes ago"
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

        await().until(getNodeDownAlarmFor(1), nullValue());
    }

    @Test
    public void canIssueAlarmSnapshots() {
        // Wait for a snapshot
        await().until(() -> m_snapshots, hasSize(greaterThanOrEqualTo(1)));
    }

    private void sendNodeUpEvent(long nodeId) {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test");
        Date currentTime = new Date();
        builder.setTime(currentTime);
        builder.setNodeid(nodeId);
        builder.setSeverity(OnmsSeverity.NORMAL.getLabel());

        AlarmData data = new AlarmData();
        data.setAlarmType(2);
        data.setReductionKey(reductionKeyForNodeUp(nodeId));
        data.setClearKey(reductionKeyForNodeDown(nodeId));
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
        builder.setSeverity(OnmsSeverity.MAJOR.getLabel());

        AlarmData data = new AlarmData();
        data.setAlarmType(1);
        data.setReductionKey(reductionKeyForNodeDown(nodeId));
        builder.setAlarmData(data);

        builder.setLogDest("logndisplay");
        builder.setLogMessage("testing");

        m_eventMgr.sendNow(builder.getEvent());
    }

    private String reductionKeyForNodeDown(long nodeId) {
        return String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId);
    }

    private String reductionKeyForNodeUp(long nodeId) {
        return String.format("%s:%d", EventConstants.NODE_UP_EVENT_UEI, nodeId);
    }

    private Callable<OnmsAlarm> getNodeDownAlarmFor(long nodeId) {
        return () -> m_alarmsByReductionKey.get(reductionKeyForNodeDown(nodeId));
    }

    private Callable<OnmsAlarm> getNodeUpAlarmFor(long nodeId) {
        return () -> m_alarmsByReductionKey.get(reductionKeyForNodeUp(nodeId));
    }
    @Override
    public synchronized void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
        m_snapshots.add(alarms);
        /* Don't actually update the map since we want to make sure that the
           values are updated through the other callbacks.
        alarms.forEach(a -> m_alarmsByReductionKey.put(a.getReductionKey(), a));
        // Remove entries for alarms that are in the map, but not in the given list
        Sets.newHashSet(Sets.difference(m_alarmsByReductionKey.keySet(),
                alarms.stream().map(OnmsAlarm::getReductionKey).collect(Collectors.toSet())))
                .forEach(r -> m_alarmsByReductionKey.remove(r));
       */
    }

    @Override
    public synchronized void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
        m_newOrUpdatedAlarms.add(alarm);
        m_alarmsByReductionKey.put(alarm.getReductionKey(), alarm);
    }

    @Override
    public synchronized void handleDeletedAlarm(int alarmId, String reductionKey) {
        m_deletedAlarms.add(reductionKey);
        m_alarmsByReductionKey.remove(reductionKey);
    }

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }
}
