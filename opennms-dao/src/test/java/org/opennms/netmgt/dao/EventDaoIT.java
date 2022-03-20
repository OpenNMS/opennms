/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventDaoIT implements InitializingBean {
    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private static boolean m_populated = false;

    @BeforeTransaction
    public void setUp() {
        if (!m_populated) {
            m_databasePopulator.populateDatabase();
            m_populated = true;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Test
	@Transactional
    public void testSave() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.whoami());
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        OnmsNode node = (OnmsNode) m_nodeDao.findAll().iterator().next();
        OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = (OnmsMonitoredService)iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        OnmsAlarm alarm = new OnmsAlarm();
	    event.setAlarm(alarm);
        event.setIpAddr(iface.getIpAddress());
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "label", "node", "string"),
                new OnmsEventParameter(event, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event, "value", "4.7", "string"),
                new OnmsEventParameter(event, "instance", "node", "string"),
                new OnmsEventParameter(event, "instanceLabel", "node", "string"),
                new OnmsEventParameter(event, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event, "threshold", "5.0", "string"),
                new OnmsEventParameter(event, "trigger", "2", "string"),
                new OnmsEventParameter(event, "rearm", "10.0", "string")));
        m_eventDao.save(event);
       
        OnmsEvent newEvent = m_eventDao.load(event.getId());
        assertEquals("uei://org/opennms/test/EventDaoTest", newEvent.getEventUei());
        assertNotNull(newEvent.getServiceType());
        assertEquals(service.getNodeId(), newEvent.getNode().getId());
        assertEquals(event.getIpAddr(), newEvent.getIpAddr());
        
        System.err.println(JaxbUtils.marshal(event));
    }

    @Test
    @Transactional
    public void testGetEventsAfterDate() {
        List<String> ueiList = new ArrayList<>();
        ueiList.add("uei/1"); // dummy
        ueiList.add("uei/2"); // dummy
        m_eventDao.getEventsAfterDate(ueiList, new Date()); // we just want to ensure that no exception is thrown :)
    }

    private void setEventsData(){
        m_eventDao.findAll().forEach(event -> m_eventDao.delete(event.getId()));
        OnmsEvent event1 = new OnmsEvent();
        event1.setDistPoller(m_distPollerDao.whoami());
        event1.setEventCreateTime(new Date());
        event1.setEventDescr("event dao test");
        event1.setEventHost("localhost");
        event1.setEventLog("Y");
        event1.setEventDisplay("Y");
        event1.setEventLogGroup("event dao test log group");
        event1.setEventLogMsg("event dao test log msg");
        event1.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event1.setEventSource("EventDaoTest1");
        event1.setEventTime(new Date());
        event1.setEventUei("uei://org/opennms/test/EventDaoTest1");
        OnmsNode node = (OnmsNode) m_nodeDao.findAll().iterator().next();
        OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = (OnmsMonitoredService)iface.getMonitoredServices().iterator().next();
        event1.setNode(node);
        event1.setServiceType(service.getServiceType());
        event1.setIpAddr(iface.getIpAddress());
        event1.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event1, "label", "node1", "string"),
                new OnmsEventParameter(event1, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event1, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event1, "value", "4.7", "string"),
                new OnmsEventParameter(event1, "instance", "node1", "string"),
                new OnmsEventParameter(event1, "instanceLabel", "node1", "string"),
                new OnmsEventParameter(event1, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event1, "threshold", "5.0", "string"),
                new OnmsEventParameter(event1, "trigger", "2", "string"),
                new OnmsEventParameter(event1, "rearm", "10.0", "string")));
        m_eventDao.save(event1);

        OnmsEvent event2 = new OnmsEvent();
        event2.setDistPoller(m_distPollerDao.whoami());
        event2.setEventCreateTime(new Date());
        event2.setEventDescr("event dao test");
        event2.setEventHost("localhost");
        event2.setEventLog("N");
        event2.setEventDisplay("Y");
        event2.setEventLogGroup("event dao test log group");
        event2.setEventLogMsg("event dao test log msg");
        event2.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event2.setEventSource("EventDaoTest2");
        event2.setEventTime(new Date());
        event2.setEventUei("uei://org/opennms/test/EventDaoTest2");
        event2.setNode(node);
        event2.setServiceType(service.getServiceType());
        event2.setIpAddr(iface.getIpAddress());
        event2.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event2, "label", "node2", "string"),
                new OnmsEventParameter(event2, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event2, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event2, "value", "4.8", "string"),
                new OnmsEventParameter(event2, "instance", "node2", "string"),
                new OnmsEventParameter(event2, "instanceLabel", "node2", "string"),
                new OnmsEventParameter(event2, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event2, "threshold", "6.0", "string"),
                new OnmsEventParameter(event2, "trigger", "3", "string"),
                new OnmsEventParameter(event2, "rearm", "11.0", "string")));
        m_eventDao.save(event2);

        OnmsEvent event3 = new OnmsEvent();
        event3.setDistPoller(m_distPollerDao.whoami());
        event3.setEventCreateTime(new Date());
        event3.setEventDescr("event dao test");
        event3.setEventHost("localhost");
        event3.setEventLog("Y");
        event3.setEventDisplay("Y");
        event3.setEventLogGroup("event dao test log group");
        event3.setEventLogMsg("event dao test log msg");
        event3.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event3.setEventSource("EventDaoTest3");
        event3.setEventTime(new Date());
        event3.setEventUei("uei://org/opennms/test/EventDaoTest3");
        event3.setNode(node);
        event3.setServiceType(service.getServiceType());
        event3.setIpAddr(iface.getIpAddress());
        event3.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event3, "label", "node2", "string"),
                new OnmsEventParameter(event3, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event3, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event3, "value", "4.9", "string"),
                new OnmsEventParameter(event3, "instance", "node2", "string"),
                new OnmsEventParameter(event3, "instanceLabel", "node2", "string"),
                new OnmsEventParameter(event3, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event3, "threshold", "7.0", "string"),
                new OnmsEventParameter(event3, "trigger", "3", "string"),
                new OnmsEventParameter(event3, "rearm", "12.0", "string")));
        m_eventDao.save(event3);

        OnmsEvent event4 = new OnmsEvent();
        event4.setDistPoller(m_distPollerDao.whoami());
        event4.setEventCreateTime(new Date());
        event4.setEventDescr("event dao test");
        event4.setEventHost("localhost");
        event4.setEventLog("N");
        event4.setEventDisplay("Y");
        event4.setEventLogGroup("event dao test log group");
        event4.setEventLogMsg("event dao test log msg");
        event4.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event4.setEventSource("EventDaoTest4");
        event4.setEventTime(new Date());
        event4.setEventUei("uei://org/opennms/test/EventDaoTest4");
        event4.setNode(node);
        event4.setServiceType(service.getServiceType());
        event4.setIpAddr(iface.getIpAddress());
        event4.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event4, "label", "node4", "string"),
                new OnmsEventParameter(event4, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event4, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event4, "value", "5.0", "string"),
                new OnmsEventParameter(event4, "instance", "node4", "string"),
                new OnmsEventParameter(event4, "instanceLabel", "node4", "string"),
                new OnmsEventParameter(event4, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event4, "threshold", "8.0", "string"),
                new OnmsEventParameter(event4, "trigger", "5", "string"),
                new OnmsEventParameter(event4, "rearm", "13.0", "string")));
        m_eventDao.save(event4);

        OnmsEvent event5 = new OnmsEvent();
        event5.setDistPoller(m_distPollerDao.whoami());
        event5.setEventCreateTime(new Date());
        event5.setEventDescr("event dao test");
        event5.setEventHost("localhost");
        event5.setEventLog("Y");
        event5.setEventDisplay("Y");
        event5.setEventLogGroup("event dao test log group");
        event5.setEventLogMsg("event dao test log msg");
        event5.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event5.setEventSource("EventDaoTest5");
        event5.setEventTime(new Date());
        event5.setEventUei("uei://org/opennms/test/EventDaoTest5");
        event5.setNode(node);
        event5.setServiceType(service.getServiceType());
        event5.setIpAddr(iface.getIpAddress());
        event5.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event5, "label", "node5", "string"),
                new OnmsEventParameter(event5, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event5, "description", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event5, "value", "5.1", "string"),
                new OnmsEventParameter(event5, "instance", "node5", "string"),
                new OnmsEventParameter(event5, "instanceLabel", "node5", "string"),
                new OnmsEventParameter(event5, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event5, "threshold", "9.0", "string"),
                new OnmsEventParameter(event5, "trigger", "6", "string"),
                new OnmsEventParameter(event5, "rearm", "14.0", "string")));
        m_eventDao.save(event5);

    }

    @Test
    @Transactional
    public void testGetEventsForEventParameters_Case1() {
        this.setEventsData();
        List<OnmsEvent> events = null;
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);

        cb.alias("eventParameters", "eventParameters")
            .multipleAnd(
                    Restrictions.and(
                            Restrictions.eq("eventParameters.name", "instance"),
                            Restrictions.eq("eventParameters.value", "node1")
                    ),
                    Restrictions.and(
                            Restrictions.eq("eventParameters.name", "trigger"),
                            Restrictions.eq("eventParameters.value", "2")
                    ),
                    Restrictions.and(
                            Restrictions.eq("eventParameters.name", "ds"),
                            Restrictions.like("eventParameters.value", "%(memAvailReal + memCached) / memTotalReal * 100.0%")
                    )
            );
        events = m_eventDao.findMatching(cb.toCriteria());
        assertEquals(1, events.size());
        assertEquals("uei://org/opennms/test/EventDaoTest1", events.get(0).getEventUei());
        assertEquals("EventDaoTest1", events.get(0).getEventSource());
    }

    @Test
    @Transactional
    public void testGetEventsForEventParameters_Case2() {
        this.setEventsData();
        List<OnmsEvent> events = null;
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);

        cb.alias("eventParameters", "eventParameters")
                .multipleAnd(
                        Restrictions.and(
                                Restrictions.eq("eventParameters.name", "instance"),
                                Restrictions.eq("eventParameters.value", "node2")
                        ),
                        Restrictions.and(
                                Restrictions.eq("eventParameters.name", "trigger"),
                                Restrictions.eq("eventParameters.value", "3")
                        )
                );
        events = m_eventDao.findMatching(cb.toCriteria());
        assertEquals(2, events.size());
        assertEquals("uei://org/opennms/test/EventDaoTest3", events.get(1).getEventUei());
        assertEquals("EventDaoTest3", events.get(1).getEventSource());
    }

    @Test
    @Transactional
    public void testGetEventsForEventParameters_Case3() {
        this.setEventsData();
        List<OnmsEvent> events = null;
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);

        cb.alias("eventParameters", "eventParameters")
                .and( Restrictions.and(
                        Restrictions.eq("eventSource", "EventDaoTest4"),
                        Restrictions.eq("eventLog", "N")
                ))
                .multipleAnd(
                        Restrictions.and(
                                Restrictions.eq("eventParameters.name", "instance"),
                                Restrictions.eq("eventParameters.value", "node4")
                        ),
                        Restrictions.and(
                                Restrictions.eq("eventParameters.name", "threshold"),
                                Restrictions.eq("eventParameters.value", "8.0")
                        )
                );
       events = m_eventDao.findMatching(cb.toCriteria());
       assertEquals(1, events.size());
       assertEquals("uei://org/opennms/test/EventDaoTest4", events.get(0).getEventUei());
       assertEquals("EventDaoTest4", events.get(0).getEventSource());
    }
}
