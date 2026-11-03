/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.surveillance.views;

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
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDataService.SurveillanceAlarm;
import org.opennms.netmgt.surveillance.views.impl.DefaultSurveillanceViewDataService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

/**
 * Ports the alarm-drill-down coverage of the Vaadin service's
 * {@code NMS15448_IT} (only unacknowledged alarms appear) onto
 * {@link DefaultSurveillanceViewDataService}, and adds coverage for the
 * cell-status computation the grid is built from.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/META-INF/opennms/applicationContext-config-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
@Transactional
public class DefaultSurveillanceViewDataServiceAlarmIT implements InitializingBean {

    @Autowired
    DatabasePopulator databasePopulator;

    @Autowired
    TransactionTemplate transactionOperations;

    @Autowired
    NodeDao nodeDao;

    @Autowired
    CategoryDao categoryDao;

    private OnmsDistPoller distPoller;

    private DefaultSurveillanceViewDataService service;

    private static final Date EVENT_DATE = new Date();

    @Override
    public void afterPropertiesSet() {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        databasePopulator.populateDatabase();
        distPoller = databasePopulator.getDistPollerDao().whoami();

        service = new DefaultSurveillanceViewDataService();
        service.setTransactionOperations(transactionOperations);
        service.setNodeDao(nodeDao);
        service.setCategoryDao(categoryDao);
        service.setAlarmDao(databasePopulator.getAlarmDao());
        service.setMonitoredServiceDao(databasePopulator.getMonitoredServiceDao());
        service.setOutageDao(databasePopulator.getOutageDao());
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

    private void saveAlarm(final boolean acknowledged) {
        final OnmsEvent event = databasePopulator.buildEvent(distPoller);
        event.setEventCreateTime(EVENT_DATE);
        event.setEventTime(EVENT_DATE);
        databasePopulator.getEventDao().save(event);
        databasePopulator.getEventDao().flush();

        databasePopulator.getAlarmDao().save(buildAlarm(event, acknowledged));
        databasePopulator.getAlarmDao().flush();
    }

    @Test
    public void testThatOnlyUnacknowledgedAlarmsAppear() {
        // one unacknowledged alarm already exists after database creation
        final List<SurveillanceAlarm> alarmsBefore = service.getAlarmsForCategories(Sets.newHashSet("Routers"), Sets.newHashSet("DEV_AC"));

        Assert.assertEquals(1, alarmsBefore.size());
        Assert.assertNotNull(alarmsBefore.get(0).getNodeLabel());
        Assert.assertNotNull(alarmsBefore.get(0).getSeverity());

        // two unacknowledged and one acknowledged alarms on top: three unacknowledged in total
        saveAlarm(false);
        saveAlarm(true);
        saveAlarm(false);

        final List<SurveillanceAlarm> alarmsAfter = service.getAlarmsForCategories(Sets.newHashSet("Routers"), Sets.newHashSet("DEV_AC"));
        Assert.assertEquals("Only three unacknowledged alarms should appear", 3, alarmsAfter.size());
    }

    @Test
    public void testCellStatusMatchesTheViewGrid() {
        final SurveillanceView view = new SurveillanceView();
        view.setName("test");
        view.getRows().add(new SurveillanceViewDef("Routers", "Routers"));
        view.getRows().add(new SurveillanceViewDef("Servers", "Servers"));
        view.getColumns().add(new SurveillanceViewDef("DEV_AC", "DEV_AC"));

        final SurveillanceStatus[][] cellStatus = service.calculateCellStatus(view);

        Assert.assertEquals(2, cellStatus.length);
        Assert.assertEquals(1, cellStatus[0].length);
        for (final SurveillanceStatus[] row : cellStatus) {
            for (final SurveillanceStatus status : row) {
                Assert.assertNotNull(status);
                Assert.assertNotNull(status.getStatus());
                Assert.assertTrue(status.getDownEntityCount() <= status.getTotalEntityCount());
            }
        }
        // node1 is in both Routers and DEV_AC, so that cell is non-empty
        Assert.assertTrue(cellStatus[0][0].getTotalEntityCount() >= 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCellStatusRejectsUnknownCategories() {
        final SurveillanceView view = new SurveillanceView();
        view.setName("broken");
        view.getRows().add(new SurveillanceViewDef("Nope", "NoSuchCategory"));
        view.getColumns().add(new SurveillanceViewDef("DEV_AC", "DEV_AC"));

        service.calculateCellStatus(view);
    }
}
