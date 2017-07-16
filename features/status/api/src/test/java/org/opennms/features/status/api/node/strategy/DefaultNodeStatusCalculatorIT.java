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
import java.util.Set;

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
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
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
public class DefaultNodeStatusCalculatorIT {

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private DefaultNodeStatusCalculator statusCalculator;

    @Before
    public void before() {
        databasePopulator.populateDatabase();

        alarmDao.findAll().forEach(alarm -> alarmDao.delete(alarm));
        alarmDao.flush();

        eventDao.findAll().forEach(e -> eventDao.delete(e));
        eventDao.flush();

        outageDao.findAll().forEach(o -> outageDao.delete(o));
        outageDao.flush();
    }

    @After
    public void after() {
        databasePopulator.resetDatabase();
    }

    @Test
    @Transactional
    public void verifyAlarmStatusCalculation() {
        final OnmsNode node = databasePopulator.getNode1();
        final NodeStatusCalculatorConfig config = new NodeStatusCalculatorConfig();
        config.setCalculationStrategy(NodeStatusCalculationStrategy.Outages);

        // No nodeIds
        verifyStatus(6, new HashMap<>(), statusCalculator.calculateStatus(config));

        // No alarm exists, status should be normal
        config.setNodeIds(Sets.newHashSet(node.getId()));
        verifyStatus(1, new HashMap<>(), statusCalculator.calculateStatus(config));

        // Create an alarm and verify status
        OnmsAlarm alarm = createAlarm(node, OnmsSeverity.WARNING);
        alarmDao.save(alarm);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.WARNING), statusCalculator.calculateStatus(config));

        // Create an alarm for same node and verify
        OnmsAlarm alarm2 = createAlarm(node, OnmsSeverity.MINOR);
        alarmDao.save(alarm2);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));

        // Create an alarm for another node and verify
        alarmDao.save(createAlarm(databasePopulator.getNode2(), OnmsSeverity.CRITICAL));
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));

        // Acknowledge alarms
        alarm2.setAlarmAckTime(new Date());
        alarm2.setAlarmAckUser("ulf");
        alarmDao.saveOrUpdate(alarm2);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.WARNING), statusCalculator.calculateStatus(config));
        alarm.setAlarmAckTime(new Date());
        alarm.setAlarmAckUser("ulf");
        alarmDao.saveOrUpdate(alarm);
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));

        // Include acknowledged alarms
        config.setIncludeAcknowledgedAlarms(true);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));

        // Apply severity filter
        config.setSeverity(OnmsSeverity.WARNING);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));
        config.setSeverity(OnmsSeverity.MINOR);
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));
        config.setSeverity(OnmsSeverity.MAJOR);
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));

        // reset severity filter and apply location filter
        config.setSeverity(null);
        config.setLocation(distPollerDao.whoami().getLocation());
        verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));
        config.setLocation("XXX");
        verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));
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

    @Test
    @Transactional
    public void verifyOutageStatusCalculation() {
        final OnmsNode node = databasePopulator.getNode1();
        final OnmsMonitoredService icmpService = node.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServiceByServiceType("ICMP");
        final OnmsMonitoredService snmpService = node.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServiceByServiceType("SNMP");
        final Set<Integer> nodeIds = Sets.newHashSet(node.getId());
        final NodeStatusCalculatorConfig config = new NodeStatusCalculatorConfig();
        config.setCalculationStrategy(NodeStatusCalculationStrategy.Outages);

        // No nodeIds
        DefaultNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));

        // No outage exist, status should be normal
        config.setNodeIds(nodeIds);
        DefaultNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));

        // Create an alarm and verify status
        final OnmsOutage outage = createOutage(icmpService, createEvent(node, OnmsSeverity.WARNING));
        saveOrUpdate(outage);
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.WARNING), statusCalculator.calculateStatus(config));

        // Create another outage on same interface and verify
        final OnmsOutage outage2 = createOutage(snmpService, createEvent(node, OnmsSeverity.MINOR));
        saveOrUpdate(outage2);
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(config));

        // Create another outage on another interface and verify
        final OnmsMonitoredService httpService = node.getIpInterfaceByIpAddress("192.168.1.2").getMonitoredServiceByServiceType("HTTP");
        saveOrUpdate(createOutage(httpService, createEvent(node, OnmsSeverity.MAJOR)));
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));

        // Create another outage on another node and verify
        saveOrUpdate(createOutage(databasePopulator.getNode2().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP"),
                createEvent(databasePopulator.getNode2(), OnmsSeverity.CRITICAL)));
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));

        // calculate status for both
        config.setNodeIds(Sets.newHashSet(node.getId(), databasePopulator.getNode2().getId()));
        DefaultNodeStatusCalculatorIT.verifyStatus(
                2,
                ImmutableMap.of(
                        node.getId(), OnmsSeverity.MAJOR,
                        databasePopulator.getNode2().getId(), OnmsSeverity.CRITICAL),
                statusCalculator.calculateStatus(config));

        // Resolve the Warning Outage
        config.setNodeIds(nodeIds);
        outage.setServiceRegainedEvent(createEvent(node, OnmsSeverity.WARNING));
        outage.setIfRegainedService(new Date());
        saveOrUpdate(outage);
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));

        // Apply severity filter
        config.setSeverity(OnmsSeverity.WARNING);
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));
        config.setSeverity(OnmsSeverity.MINOR);
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));
        config.setSeverity(OnmsSeverity.MAJOR);
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));
        config.setSeverity(OnmsSeverity.CRITICAL);
        DefaultNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));

        // reset severity filter and apply location filter
        config.setSeverity(null);
        config.setLocation(distPollerDao.whoami().getLocation());
        DefaultNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(config));
        config.setLocation("XXX");
        DefaultNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(config));
    }

    private void saveOrUpdate(OnmsOutage outage) {
        if (outage.getServiceLostEvent() != null) {
            eventDao.saveOrUpdate(outage.getServiceLostEvent());
        }
        if (outage.getServiceRegainedEvent() != null) {
            eventDao.saveOrUpdate(outage.getServiceRegainedEvent());
        }
        outageDao.save(outage);
        outageDao.flush();
    }

    private OnmsOutage createOutage(OnmsMonitoredService service, OnmsEvent svcLostEvent) {
        return TestUtils.createOutage(service, svcLostEvent);
    }

    private OnmsEvent createEvent(OnmsNode node, OnmsSeverity severity) {
        return TestUtils.createEvent(node, severity, distPollerDao.whoami());
    }
}