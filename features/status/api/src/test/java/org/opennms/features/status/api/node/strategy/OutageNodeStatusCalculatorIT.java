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
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.stats.AlarmStatisticsService;
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
public class OutageNodeStatusCalculatorIT {
    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private OutageNodeStatusCalculator statusCalculator;

    @Before
    public void before() {
        databasePopulator.populateDatabase();
        eventDao.findAll().forEach(e -> eventDao.delete(e));
        outageDao.findAll().forEach(o -> outageDao.delete(o));
        outageDao.flush();
    }

    @After
    public void after() {
        databasePopulator.resetDatabase();
    }

    @Test
    @Transactional
    public void verifyCalculateStatus() {
        final OnmsNode node = databasePopulator.getNode1();
        final OnmsMonitoredService icmpService = node.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServiceByServiceType("ICMP");
        final OnmsMonitoredService snmpService = node.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServiceByServiceType("SNMP");
        final Set<Integer> nodeIds = Sets.newHashSet(node.getId());
        final NodeStatusCalculatorConfig query = new NodeStatusCalculatorConfig();

        // No nodeIds
        AlarmNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));

        // No outage exist, status should be normal
        query.setNodeIds(nodeIds);
        AlarmNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));

        // Create an alarm and verify status
        final OnmsOutage outage = createOutage(icmpService, createEvent(node, OnmsSeverity.WARNING));
        saveOrUpdate(outage);
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.WARNING), statusCalculator.calculateStatus(query));

        // Create another outage on same interface and verify
        final OnmsOutage outage2 = createOutage(snmpService, createEvent(node, OnmsSeverity.MINOR));
        saveOrUpdate(outage2);
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MINOR), statusCalculator.calculateStatus(query));

        // Create another outage on another interface and verify
        final OnmsMonitoredService httpService = node.getIpInterfaceByIpAddress("192.168.1.2").getMonitoredServiceByServiceType("HTTP");
        saveOrUpdate(createOutage(httpService, createEvent(node, OnmsSeverity.MAJOR)));
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));

        // Create another outage on another node and verify
        saveOrUpdate(createOutage(databasePopulator.getNode2().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP"),
                createEvent(databasePopulator.getNode2(), OnmsSeverity.CRITICAL)));
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));

        // calculate status for both
        query.setNodeIds(Sets.newHashSet(node.getId(), databasePopulator.getNode2().getId()));
        AlarmNodeStatusCalculatorIT.verifyStatus(
                2,
                ImmutableMap.of(
                    node.getId(), OnmsSeverity.MAJOR,
                    databasePopulator.getNode2().getId(), OnmsSeverity.CRITICAL),
                statusCalculator.calculateStatus(query));

        // Resolve the Warning Outage
        query.setNodeIds(nodeIds);
        outage.setServiceRegainedEvent(createEvent(node, OnmsSeverity.WARNING));
        outage.setIfRegainedService(new Date());
        saveOrUpdate(outage);
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));
        
        // Apply severity filter
        query.setSeverity(OnmsSeverity.WARNING);
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));
        query.setSeverity(OnmsSeverity.MINOR);
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));
        query.setSeverity(OnmsSeverity.MAJOR);
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));
        query.setSeverity(OnmsSeverity.CRITICAL);
        AlarmNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));

        // reset severity filter and apply location filter
        query.setSeverity(null);
        query.setLocation(distPollerDao.whoami().getLocation());
        AlarmNodeStatusCalculatorIT.verifyStatus(1, ImmutableMap.of(node.getId(), OnmsSeverity.MAJOR), statusCalculator.calculateStatus(query));
        query.setLocation("XXX");
        AlarmNodeStatusCalculatorIT.verifyStatus(0, new HashMap<>(), statusCalculator.calculateStatus(query));
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