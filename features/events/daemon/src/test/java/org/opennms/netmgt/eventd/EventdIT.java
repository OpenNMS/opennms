/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Crank up a real eventd instance, send it some events, and verify that the records 
 * are created in the database correctly.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventdIT implements InitializingBean {
    private static final long SLEEP_TIME = 50;

    @Autowired
    private EventIpcManager m_eventdIpcMgr;

    @Autowired
    private Eventd m_eventd;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_databasePopulator.populateDatabase();
        m_eventd.onStart();
    }

    @After
    public void tearDown() {
        m_eventd.onStop();
        m_databasePopulator.resetDatabase();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test(timeout=30000)
    public void testPersistEvent() throws Exception {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);
        cb.eq("eventUei", EventConstants.NODE_DOWN_EVENT_UEI);
        assertEquals(0, m_eventDao.countMatching(cb.toCriteria()));

        OnmsNode node = m_databasePopulator.getNode1();
        assertNotNull(node);
        sendNodeDownEvent(null, node);

        while(m_eventDao.countMatching(cb.toCriteria()) < 1) {
            Thread.sleep(SLEEP_TIME);
        }

        final List<OnmsEvent> matching = m_eventDao.findMatching(cb.toCriteria());
        System.err.println("matching = " + matching);
        assertEquals(1, m_eventDao.countMatching(cb.toCriteria()));

        node = m_databasePopulator.getNode2();
        assertNotNull(node);
        Event generatedEvent = sendNodeDownEvent(null, node);

        while(m_eventDao.countMatching(cb.toCriteria()) < 2) {
            Thread.sleep(SLEEP_TIME);
        }

        assertEquals(2, m_eventDao.countMatching(cb.toCriteria()));

        assertNull(generatedEvent.getInterfaceAddress());
        cb.isNull("ipAddr");
        assertEquals("failed, found: " + m_eventDao.findMatching(cb.toCriteria()), 2, m_eventDao.countMatching(cb.toCriteria()));
    }

    /**
     * Test that eventd's service ID lookup works properly.
     */
    @Test(timeout=30000)
    public void testPersistEventWithService() throws Exception {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);
        cb.eq("eventUei", EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI);
        assertEquals(0, m_eventDao.countMatching(cb.toCriteria()));
        assertNotNull(m_serviceTypeDao.findByName("ICMP"));

        OnmsNode node = m_databasePopulator.getNode1();
        assertNotNull(node);
        OnmsIpInterface intf = node.getIpInterfaceByIpAddress("192.168.1.1");
        assertNotNull(intf);
        System.err.println("services = " + intf.getMonitoredServices());
        OnmsMonitoredService svc = intf.getMonitoredServiceByServiceType("ICMP");
        assertNotNull(svc);
        assertEquals("192.168.1.1", str(svc.getIpAddress()));
        final Integer serviceId = svc.getServiceId();
        sendServiceDownEvent(null, svc);

        while(m_eventDao.countMatching(cb.toCriteria()) != 1) {
            Thread.sleep(SLEEP_TIME);
        }
        assertEquals(1, m_eventDao.countMatching(cb.toCriteria()));
        assertEquals("service ID for event", serviceId, m_eventDao.findMatching(cb.toCriteria()).get(0).getServiceType().getId());
    }

    @Test(timeout=30000)
    public void testNMS8919() throws Exception {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);
        cb.eq("eventUei", EventConstants.FORCE_RESCAN_EVENT_UEI);
        assertEquals(0, m_eventDao.countMatching(cb.toCriteria()));

        OnmsNode node = m_databasePopulator.getNode1();
        assertNotNull(node);

        final EventBuilder e = new EventBuilder(EventConstants.FORCE_RESCAN_EVENT_UEI, "JUnit");
        e.addParam("_foreignSource", "imported:");
        e.addParam("_foreignId", "1");
        e.setLogDest("logndisplay");
        e.setLogMessage("forcing rescan");
        final Event event = e.getEvent();
        m_eventdIpcMgr.sendNow(event);

        while(m_eventDao.countMatching(cb.toCriteria()) < 1) {
            Thread.sleep(SLEEP_TIME);
        }

        final List<OnmsEvent> matching = m_eventDao.findMatching(cb.toCriteria());
        System.err.println("matching = " + matching);
        assertEquals(1, matching.size());
        assertEquals(new Integer(1), matching.get(0).getNodeId());
    }

    /**
     * @param reductionKey
     * @param node
     * @return 
     */
    private Event sendNodeDownEvent(String reductionKey, OnmsNode node) {
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

        final Event event = e.getEvent();
        m_eventdIpcMgr.sendNow(event);
        return event;
    }

    /**
     * @param reductionKey
     */
    private void sendServiceDownEvent(String reductionKey, OnmsMonitoredService svc) {
        final EventBuilder e = MockEventUtil.createEventBuilder("Test", EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, svc.getNodeId(), str(svc.getIpAddress()), svc.getServiceName(), "Not responding");

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
