/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/META-INF/opennms/applicationContext-config-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
@Transactional
public class NMS15448_IT implements InitializingBean {
    @Autowired
    DatabasePopulator databasePopulator;

    @Autowired
    TransactionTemplate transactionOperations;

    private OnmsDistPoller distPoller;

    @Override
    public void afterPropertiesSet() {
        BeanUtils.assertAutowiring(this);
    }

    private static final Date EVENT_DATE = new Date();

    @Before
    public void setUp() {
        databasePopulator.populateDatabase();
        distPoller = databasePopulator.getDistPollerDao().whoami();
    }

    private OnmsAlarm buildAlarm(final OnmsEvent event, boolean acknowledged) {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(distPoller);
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
        alarm.setNode(databasePopulator.getNode1());
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setServiceType(databasePopulator.getServiceTypeDao().findByName("ICMP"));
        if (acknowledged) {
            alarm.setAlarmAckUser("foobar");
            alarm.setAlarmAckTime(EVENT_DATE);
        }
        return alarm;
    }

    @Test
    public void testThatOnlyUnacknowledgedAlarmsAppear() {
        final DefaultSurveillanceViewService surveillanceViewService = new DefaultSurveillanceViewService();
        surveillanceViewService.setTransactionOperations(transactionOperations);
        surveillanceViewService.setMonitoredServiceDao(databasePopulator.getMonitoredServiceDao());
        surveillanceViewService.setOutageDao(databasePopulator.getOutageDao());
        surveillanceViewService.setAlarmDao(databasePopulator.getAlarmDao());
        final OnmsCategory cat1 = databasePopulator.getCategoryDao().findByName("Routers");
        final OnmsCategory cat2 = databasePopulator.getCategoryDao().findByName("DEV_AC");

        // query to assert, that one unacknowledged alarms already exist after database creation
        final List<OnmsAlarm> alarmsBefore = surveillanceViewService.getAlarmsForCategories(Sets.newHashSet(cat1), Sets.newHashSet(cat2));

        Assert.assertEquals(1, alarmsBefore.size());

        // we add two unacknowledged and one acknowledged alarms, so we have three unacknowledged alarms in total
        final OnmsEvent event1 = databasePopulator.buildEvent(distPoller);
        event1.setEventCreateTime(EVENT_DATE);
        event1.setEventTime(EVENT_DATE);
        databasePopulator.getEventDao().save(event1);
        databasePopulator.getEventDao().flush();

        final OnmsAlarm alarm1 = buildAlarm(event1, false);
        databasePopulator.getAlarmDao().save(alarm1);
        databasePopulator.getAlarmDao().flush();

        final OnmsEvent event2 = databasePopulator.buildEvent(distPoller);
        event2.setEventCreateTime(EVENT_DATE);
        event2.setEventTime(EVENT_DATE);
        databasePopulator.getEventDao().save(event2);
        databasePopulator.getEventDao().flush();

        final OnmsAlarm alarm2 = buildAlarm(event2, true);
        databasePopulator.getAlarmDao().save(alarm2);
        databasePopulator.getAlarmDao().flush();

        final OnmsEvent event3 = databasePopulator.buildEvent(distPoller);
        event3.setEventCreateTime(EVENT_DATE);
        event3.setEventTime(EVENT_DATE);
        databasePopulator.getEventDao().save(event3);
        databasePopulator.getEventDao().flush();

        final OnmsAlarm alarm3 = buildAlarm(event3, false);
        databasePopulator.getAlarmDao().save(alarm3);
        databasePopulator.getAlarmDao().flush();

        // check for three unacknowledged alarms
        final List<OnmsAlarm> alarmsAfter = surveillanceViewService.getAlarmsForCategories(Sets.newHashSet(cat1), Sets.newHashSet(cat2));
        Assert.assertEquals("Only three unacknowledged alarms should appear", 3, alarmsAfter.size());
    }
}
