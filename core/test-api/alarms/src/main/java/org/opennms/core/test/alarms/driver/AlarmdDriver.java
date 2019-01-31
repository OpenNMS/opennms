/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.test.alarms.driver;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.time.PseudoClock;
import org.opennms.netmgt.alarmd.AlarmPersisterImpl;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.alarmd.drools.AlarmService;
import org.opennms.netmgt.alarmd.drools.DroolsAlarmContext;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This class is expected to be ran by the {@link JUnitScenarioDriver}
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
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml",
        "classpath*:/META-INF/opennms/applicationContext-alarm-driver-ext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
public class AlarmdDriver implements TemporaryDatabaseAware<MockDatabase>, ActionVisitor, ScenarioHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmdDriver.class);

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
    private TransactionTemplate m_transactionTemplate;

    private MockDatabase m_database;

    @Autowired
    private DroolsAlarmContext m_droolsAlarmContext;
    
    @Autowired
    private AlarmPersisterImpl m_alarmPersister;

    @Autowired
    private AlarmService m_alarmService;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }

    private Scenario scenario;

    private final long tickLength = 1;

    private final ScenarioResults results = new ScenarioResults();

    @Before
    public void setUp() {
        // Async.
        m_eventMgr.setSynchronous(false);

        // Events need database IDs to make alarmd happy
        m_eventMgr.setEventWriter(m_database);

        // Events need to real nodes too
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);

        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node2");
        node.setId(2);
        m_nodeDao.save(node);

        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node3");
        node.setId(3);
        m_nodeDao.save(node);

        // Use a pseudo-clock
        m_droolsAlarmContext.setUsePseudoClock(true);
        // Drive the ticks ourselves
        m_droolsAlarmContext.setUseManualTick(true);

        // Set the behavior        
        if (scenario.getLegacyAlarmBehavior()) {
            m_alarmPersister.setLegacyAlarmState(true);
        }
        
        // Start alarmd
        m_alarmd.start();
    }

    @After
    public void tearDown() {
        m_alarmd.destroy();
    }

    @Test
    public void canDriveScenario() {
        if (scenario.getActions().size() == 0) {
            return;
        }

        try {
            final Map<Long,List<Action>> actionsByTick = scenario.getActions().stream()
                    .collect(Collectors.groupingBy(a -> roundToTick(a.getTime())));

            final long start = Math.max(scenario.getActions().stream()
                    .min(Comparator.comparing(Action::getTime))
                    .map(e -> roundToTick(e.getTime()))
                    .get() - tickLength, 0);
            final long end = scenario.getActions().stream()
                    .max(Comparator.comparing(Action::getTime))
                    .map(e -> roundToTick(e.getTime()))
                    .get() + tickLength;

            if (start > 0) {
                // Tick
                PseudoClock.getInstance().advanceTime(tickLength, TimeUnit.MILLISECONDS);
                m_droolsAlarmContext.getClock().advanceTime(tickLength, TimeUnit.MILLISECONDS);
                m_droolsAlarmContext.tick();
            }

            for (long now = start; now <= end; now += tickLength) {
                // Perform the actions
                final List<Action> actions = actionsByTick.get(now);
                if (actions != null) {
                    for (Action  a : actions) {
                        a.visit(this);
                    }
                }

                // Tick
                PseudoClock.getInstance().advanceTime(tickLength, TimeUnit.MILLISECONDS);
                m_droolsAlarmContext.getClock().advanceTime(tickLength, TimeUnit.MILLISECONDS);
                m_droolsAlarmContext.tick();
                results.addAlarms(now, m_transactionTemplate.execute((t) -> {
                            final List<OnmsAlarm> alarms = m_alarmDao.findAll();
                    alarms.forEach(a -> {
                       Hibernate.initialize(a.getAssociatedAlarms());

                    });
                            return alarms;
                        }));
            }

            // Tick every 5 minutes for the next 24 hours
            tickAtRateUntil(TimeUnit.MINUTES.toMillis(5),
                    end,
                    end + TimeUnit.DAYS.toMillis(1));

            // Tick every hour for the next week
            tickAtRateUntil(TimeUnit.HOURS.toMillis(1),
                    end + TimeUnit.DAYS.toMillis(1),
                    end + TimeUnit.DAYS.toMillis(8));
        } catch (Exception e) {
            LOG.error("Error occurred: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void tickAtRateUntil(long tickLength, long start, long end) {
        // Now keep tick'ing at an accelerated rate for another week
        for (long now = start; now <= end; now += tickLength) {
            // Tick
            m_droolsAlarmContext.getClock().advanceTime(tickLength, TimeUnit.MILLISECONDS);
            m_droolsAlarmContext.tick();
            results.addAlarms(now + tickLength, m_transactionTemplate.execute((t) -> {
                final List<OnmsAlarm> alarms = m_alarmDao.findAll();
                alarms.forEach(a -> {
                    Hibernate.initialize(a.getAssociatedAlarms());
                });
                return alarms;
            }));
        }
    }

    private long roundToTick(Date date) {
        return Math.floorDiv(date.getTime(),tickLength) * tickLength;
    }

    @Override
    public void sendEvent(Event e) {
        m_eventMgr.sendNow(e, true);
    }

    @Override
    public void acknowledgeAlarm(String ackUser, Date ackTime, Function<OnmsAlarm, Boolean> filter) {
        m_transactionTemplate.execute((t) -> {
            final List<OnmsAlarm> alarms = m_alarmDao.findAll().stream()
                    .filter(filter::apply)
                    .collect(Collectors.toList());
            alarms.forEach(a -> {
                m_alarmService.acknowledgeAlarm(a, ackTime);
            });
            return null;
        });
    }

    @Override
    public void unacknowledgeAlarm(String ackUser, Date ackTime, Function<OnmsAlarm, Boolean> filter) {
        m_transactionTemplate.execute((t) -> {
            final List<OnmsAlarm> alarms = m_alarmDao.findAll().stream()
                    .filter(filter::apply)
                    .collect(Collectors.toList());
            alarms.forEach(a -> {
                m_alarmService.unacknowledgeAlarm(a, ackTime);
            });
            return null;
        });
    }

    @Override
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public ScenarioResults getResults() {
        return results;
    }
}
