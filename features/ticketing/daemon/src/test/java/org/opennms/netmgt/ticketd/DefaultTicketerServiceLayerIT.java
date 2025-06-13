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
package org.opennms.netmgt.ticketd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.RelatedAlarmSummary;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-test-troubleTicketer.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/org/opennms/netmgt/ticketd/applicationContext-configOverride.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DefaultTicketerServiceLayerIT implements InitializingBean {
    @Autowired
    private TicketerServiceLayer m_ticketerServiceLayer;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
    }

    @After
    public void tearDown() {
        m_databasePopulator.resetDatabase();
    }

    @Test
    public void testWire() {
        assertNotNull(m_ticketerServiceLayer);
        
        OnmsAlarm alarm = m_alarmDao.findAll().get(0);
        assertNull(alarm.getTTicketState());
        assertNull(alarm.getTTicketId());

        final int alarmId = alarm.getId();
        m_ticketerServiceLayer.setTicketerPlugin(new TestTicketerPlugin());
        m_ticketerServiceLayer.createTicketForAlarm(alarmId, new HashMap<String, String>());

        m_alarmDao.flush();

        alarm = m_alarmDao.get(alarmId);
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
        assertNotNull(alarm.getTTicketId());
        assertEquals("testId", alarm.getTTicketId());

        m_ticketerServiceLayer.updateTicketForAlarm(alarm.getId(), alarm.getTTicketId());

        m_alarmDao.flush();

        alarm = m_alarmDao.get(alarmId);
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());

        alarm.setSeverity(OnmsSeverity.CLEARED);
        m_alarmDao.update(alarm);

        m_ticketerServiceLayer.closeTicketForAlarm(alarmId,
                alarm.getTTicketId());

        m_alarmDao.flush();

        alarm = m_alarmDao.get(alarmId);
        assertEquals(TroubleTicketState.CLOSED, alarm.getTTicketState());

    }

    @Test
    @Transactional
    public void testTicketsForSituations() throws PluginException {

        OnmsNode testNode = new OnmsNode();
        testNode.setLabel("TEST NODE");
        testNode.setCreateTime(new Date());
        testNode.setLocation(new OnmsMonitoringLocation("Default", "Default"));
        m_nodeDao.saveOrUpdate(testNode);

        // Create first alarm
        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setDistPoller(m_distPollerDao.whoami());
        alarm1.setCounter(1);
        alarm1.setUei("linkDown");
        alarm1.setNode(testNode);

        // Create second alarm
        OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setDistPoller(m_distPollerDao.whoami());
        alarm2.setCounter(1);
        alarm2.setUei("linkDown");
        alarm2.setNode(testNode);

        m_alarmDao.save(alarm2);
        m_alarmDao.save(alarm1);

        // create a situation relating multiple alarms
        OnmsAlarm situation = new OnmsAlarm();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(alarm1, alarm2)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);
        OnmsAlarm retrieved = m_alarmDao.findByReductionKey("situation/reduction/key");
        TestTicketerPlugin ticketerPlugin = new TestTicketerPlugin();
        m_ticketerServiceLayer.setTicketerPlugin(ticketerPlugin);
        m_ticketerServiceLayer.createTicketForAlarm(retrieved.getId(), new HashMap<String, String>());

        OnmsAlarm alarm = m_alarmDao.get(retrieved.getId());
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
        assertNotNull(alarm.getTTicketId());
        assertEquals("testId", alarm.getTTicketId());
        List<RelatedAlarmSummary> relatedAlarms = ticketerPlugin.get("testId").getRelatedAlarms();
        assertFalse(relatedAlarms.isEmpty());
        assertEquals(relatedAlarms.get(0).getNodeId(), testNode.getId());
    }

}
