/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.status.api.node.strategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AlarmNodeStatusCalculatorIT {

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private AlarmNodeStatusCalculator statusCalculator;

    @Before
    public void before() {
        databasePopulator.populateDatabase();
        alarmDao.findAll().forEach(alarm -> alarmDao.delete(alarm));
        alarmDao.flush();
    }

    @After
    public void after() {
        databasePopulator.resetDatabase();
    }

    @Test
    @Transactional
    public void verifyCalculateStatus() {
        final OnmsNode node = databasePopulator.getNode1();
        final NodeStatusCalculatorConfig query = new NodeStatusCalculatorConfig();
        query.setNodeIds(Sets.newHashSet(node.getId()));

        // No alarm exists, status should be normal
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));

        // Create an alarm and verify status
        OnmsAlarm alarm = createAlarm(node, OnmsSeverity.WARNING);
        alarmDao.save(alarm);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.WARNING), statusCalculator.calculateStatus(query));

        // Create an alarm for same node and verify
        OnmsAlarm alarm2 = createAlarm(node, OnmsSeverity.MINOR);
        alarmDao.save(alarm2);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));

        // Create an alarm for another node and verify
        alarmDao.save(createAlarm(databasePopulator.getNode2(), OnmsSeverity.CRITICAL));
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));

        // Acknowledge alarms
        alarm2.setAlarmAckTime(new Date());
        alarm2.setAlarmAckUser("ulf");
        alarmDao.saveOrUpdate(alarm2);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.WARNING), statusCalculator.calculateStatus(query));
        alarm.setAlarmAckTime(new Date());
        alarm.setAlarmAckUser("ulf");
        alarmDao.saveOrUpdate(alarm);
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));

        // Include acknowledged alarms
        query.setIncludeAcknowledgedAlarms(true);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));

        // Apply severity filter
        query.setSeverity(OnmsSeverity.WARNING);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));
        query.setSeverity(OnmsSeverity.MINOR);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));
        query.setSeverity(OnmsSeverity.MAJOR);
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));

        // reset severity filter and apply location filter
        query.setSeverity(null);
        query.setLocation(distPollerDao.whoami().getLocation());
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));
        query.setLocation("XXX");
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));
    }

    private OnmsAlarm createAlarm(OnmsNode node, OnmsSeverity severity) {
        return TestUtils.createAlarm(node, severity, distPollerDao.whoami());
    }

    static void verifyStatus(int statusCount, Map<Integer, OnmsSeverity> expectedStatusMap, Status verifyMe) {
        Assert.assertEquals("Could not verify status, as status count do not match expected status map.", verifyMe.size(), expectedStatusMap.size());
        Assert.assertEquals(statusCount, verifyMe.size());
        for (Map.Entry<Integer, OnmsSeverity> entry : expectedStatusMap.entrySet()) {
            Assert.assertEquals(entry.getValue(), verifyMe.getSeverity(entry.getKey()));
        }
    }
}