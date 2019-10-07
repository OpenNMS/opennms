/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
        BeanUtils.assertAutowiring(this);
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
