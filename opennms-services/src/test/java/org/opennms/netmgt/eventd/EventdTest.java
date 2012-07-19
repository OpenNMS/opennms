/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.core.utils.InetAddressUtils.str;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Crank up a real eventd instance, send it some events, and verify that the records 
 * are created in the database correctly.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:META-INF/opennms/smallEventConfDao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventdTest implements TemporaryDatabaseAware<TemporaryDatabase>, InitializingBean {

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    @Qualifier(value="eventIpcManagerImpl")
    private EventIpcManager m_eventdIpcMgr;

    @Autowired
    private Eventd m_eventd;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private TemporaryDatabase m_database;

    @Override
    public void setTemporaryDatabase(TemporaryDatabase database) {
        m_database = database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_databasePopulator.populateDatabase();
        m_eventd.onStart();
    }

    @After
    public void tearDown() throws Exception {
        m_eventd.onStop();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    @JUnitTemporaryDatabase
    public void testPersistEvent() throws Exception {
        assertEquals(0, m_database.countRows(String.format("select * from events where eventuei = '%s'", EventConstants.NODE_DOWN_EVENT_UEI)));

        OnmsNode node = m_databasePopulator.getNode1();
        assertNotNull(node);
        sendNodeDownEvent(null, node);
        Thread.sleep(1000);

        assertEquals(1, m_database.countRows(String.format("select * from events where eventuei = '%s'", EventConstants.NODE_DOWN_EVENT_UEI)));

        node = m_databasePopulator.getNode2();
        assertNotNull(node);
        sendNodeDownEvent(null, node);
        Thread.sleep(1000);

        assertEquals(2, m_database.countRows(String.format("select * from events where eventuei = '%s'", EventConstants.NODE_DOWN_EVENT_UEI)));
    }

    /**
     * Test that eventd's service ID lookup works properly.
     */
    @Test
    @JUnitTemporaryDatabase
    public void testPersistEventWithService() throws Exception {

        assertEquals(0, m_database.countRows(String.format("select * from events where eventuei = '%s'", EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)));
        assertEquals("service ID for ICMP", 1, m_jdbcTemplate.queryForInt("select serviceid from service where servicename = 'ICMP'"));

        OnmsNode node = m_databasePopulator.getNode1();
        assertNotNull(node);
        OnmsIpInterface intf = node.getIpInterfaceByIpAddress("192.168.1.1");
        assertNotNull(intf);
        OnmsMonitoredService svc = intf.getMonitoredServiceByServiceType("ICMP");
        assertNotNull(svc);
        assertEquals(1, svc.getNodeId().intValue());
        assertEquals("192.168.1.1", str(svc.getIpAddress()));
        assertEquals(1, svc.getServiceId().intValue());
        sendServiceDownEvent(null, svc);

        Thread.sleep(1000);
        assertEquals(1, m_database.countRows(String.format("select * from events where eventuei = '%s'", EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)));
        assertEquals("service ID for event", 1, m_jdbcTemplate.queryForInt(String.format("select serviceID from events where eventuei = '%s'", EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)));
    }


    /**
     * @param reductionKey
     * @param node
     */
    private void sendNodeDownEvent(String reductionKey, OnmsNode node) {
        EventBuilder e = MockEventUtil.createNodeDownEventBuilder("Test", node);

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            e.setAlarmData(data);
        } else {
            e.setAlarmData(null);
        }

        e.setLogDest("logndisplay");
        e.setLogMessage("testing");

        m_eventdIpcMgr.sendNow(e.getEvent());
    }

    /**
     * @param reductionKey
     */
    private void sendServiceDownEvent(String reductionKey, OnmsMonitoredService svc) {
        EventBuilder e = MockEventUtil.createEventBuilder("Test", EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, svc.getNodeId(), str(svc.getIpAddress()), svc.getServiceName(), "Not responding");

        if (reductionKey != null) {
            AlarmData data = new AlarmData();
            data.setAlarmType(1);
            data.setReductionKey(reductionKey);
            e.setAlarmData(data);
        } else {
            e.setAlarmData(null);
        }

        e.setLogDest("logndisplay");
        e.setLogMessage("testing");

        m_eventdIpcMgr.sendNow(e.getEvent());
    }
}
