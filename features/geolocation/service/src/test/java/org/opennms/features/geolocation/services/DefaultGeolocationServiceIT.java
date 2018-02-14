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

package org.opennms.features.geolocation.services;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geolocation.api.GeolocationInfo;
import org.opennms.features.geolocation.api.GeolocationQuery;
import org.opennms.features.geolocation.api.GeolocationQueryBuilder;
import org.opennms.features.geolocation.api.StatusCalculationStrategy;
import org.opennms.features.status.api.node.NodeStatusCalculator;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class DefaultGeolocationServiceIT {

    private static final Coordinates coordinates = new Coordinates(51.485278, -3.186667);

    @Autowired
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private NodeStatusCalculator statusCalculator;

    private DefaultGeolocationService geolocationService;

    @Before
    public void before() {
        geolocationService = new DefaultGeolocationService(genericPersistenceAccessor, statusCalculator);

        // Initialize Database and clean up alarms
        databasePopulator.populateDatabase();
        alarmDao.findAll().forEach(a -> alarmDao.delete(a));
        alarmDao.flush();
    }

    @After
    public void after() {
        databasePopulator.resetDatabase();
    }

    @Test
    @Transactional
    public void verifyMerging() {
        // Set coordinates for all
        nodeDao.findAll().forEach(n -> {
            n.getAssetRecord().getGeolocation().setLongitude(coordinates.getLongitude());
            n.getAssetRecord().getGeolocation().setLatitude(coordinates.getLatitude());
            nodeDao.saveOrUpdate(n);
        });
        // Query
        final GeolocationQuery query = new GeolocationQueryBuilder()
                .withStatusCalculationStrategy(StatusCalculationStrategy.Alarms)
                .build();

        // We do not have any alarms, therefore all nodes should be "NORMAL"
        List<GeolocationInfo> locations = geolocationService.getLocations(query);
        Assert.assertEquals(nodeDao.countAll(), locations.size());
        locations.forEach(l -> Assert.assertEquals("Normal", l.getSeverityInfo().getLabel()));

        // Add an alarm for one node and try again
        alarmDao.save(createAlarm(databasePopulator.getNode1(), OnmsSeverity.MAJOR, distPollerDao.whoami()));
        alarmDao.flush();
        locations = geolocationService.getLocations(query);
        Assert.assertEquals(nodeDao.countAll(), locations.size());
        locations.forEach(l -> {
            if (l.getNodeInfo().getNodeId() == databasePopulator.getNode1().getId()) {
                Assert.assertEquals("Major", l.getSeverityInfo().getLabel());
            } else {
                Assert.assertEquals("Normal", l.getSeverityInfo().getLabel());
            }
        });
    }

    private static OnmsAlarm createAlarm(OnmsNode node, OnmsSeverity severity, OnmsDistPoller distpoller) {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei(EventConstants.NODE_DOWN_EVENT_UEI);
        alarm.setDistPoller(distpoller);
        alarm.setCounter(1);
        alarm.setSeverity(severity);
        alarm.setNode(node);
        return alarm;
    }
}