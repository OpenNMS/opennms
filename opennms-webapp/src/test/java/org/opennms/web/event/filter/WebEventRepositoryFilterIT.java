/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.event.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.event.Event;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class WebEventRepositoryFilterIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    MonitoringLocationDao m_monitoringLocationDao;

    @Autowired
    @Qualifier("dao")
    WebEventRepository m_daoEventRepo;
    
    @Autowired
    ApplicationContext m_appContext;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();

        final OnmsNode node2 = m_dbPopulator.getNode2();
        final OnmsMonitoringLocation location = m_monitoringLocationDao.get("RDU");
        node2.setLocation(location);
        m_nodeDao.saveOrUpdate(node2);
        m_nodeDao.flush();

        final OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_dbPopulator.getDistPollerDao().whoami());
        event.setAlarm(m_dbPopulator.getAlarmDao().get(1));
        event.setNode(node2);
        event.setEventUei("uei.opennms.org/test2");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(OnmsSeverity.CLEARED.getId());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setIfIndex(11);
        event.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        event.setEventLogMsg("This is a test log message");
        event.setEventDescr("This is a test event");
        event.setServiceType(m_dbPopulator.getServiceTypeDao().get(1));
        m_dbPopulator.getEventDao().save(event);
        m_dbPopulator.getEventDao().flush();
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testEventIdFilter(){
        EventIdFilter filter = new EventIdFilter(1);
        assert1Result(filter);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testEventIdListFilter(){
        int[] ids = {1};
        EventIdListFilter filter = new EventIdListFilter(ids);
        assert1Result(filter);
        
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testAcknowledgeByFilter(){
        AcknowledgedByFilter filter = new AcknowledgedByFilter("TestUser");
        EventCriteria criteria = new EventCriteria(filter);
        
        Event[] events = m_daoEventRepo.getMatchingEvents(criteria);
        assertEquals(0, events.length);
        
        m_daoEventRepo.acknowledgeMatchingEvents("TestUser", new Date(), new EventCriteria(new EventIdFilter(1)));
        
        events = m_daoEventRepo.getMatchingEvents(criteria);
        assertEquals(1, events.length);
        assertEquals("TestUser", events[0].getAcknowledgeUser());
        
        m_daoEventRepo.unacknowledgeAll();
    }
    
    @Test
    @Transactional
    public void testAfterDateFilter(){
        AfterDateFilter filter = new AfterDateFilter(yesterday());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testAlarmIdFilter(){
        AlarmIdFilter filter = new AlarmIdFilter(1);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    @Transactional
    public void testBeforeDateFilter(){
        BeforeDateFilter filter = new BeforeDateFilter(new Date());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    @Transactional
    public void testDescriptionSubstringFilterTest(){
        DescriptionSubstringFilter filter = new DescriptionSubstringFilter("test event");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        assertTrue(
            "This is a test event".equals(events[0].getDescription()) ||
            "This is the description of a test event.".equals(events[0].getDescription())
        );
        assertTrue(
            "This is a test event".equals(events[1].getDescription()) ||
            "This is the description of a test event.".equals(events[1].getDescription())
        );
    }
    
    @Test
    @Transactional
    public void testExactUEIFilter(){
        ExactUEIFilter filter = new ExactUEIFilter("uei.opennms.org/test2");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        assertEquals("uei.opennms.org/test2", events[0].getUei());
    }
    
    @Test
    @Transactional
    public void testIfIndexFilter(){
        IfIndexFilter filter = new IfIndexFilter(11);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingDaoEvents(new IfIndexFilter(1));
        assertEquals(0, events.length);
    }

    @Test
    @Transactional
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        assertEquals("192.168.1.1", events[0].getIpAddress());
        assertEquals("192.168.1.1", events[1].getIpAddress());
    }
    
    @Test
    @Transactional
    public void testIpAddrLikeFilter(){
        IPAddrLikeFilter filter = new IPAddrLikeFilter("192.168.*.*");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        filter = new IPAddrLikeFilter("193.168");
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    @Transactional
    public void testLogMessageMatchesAny(){
        LogMessageMatchesAnyFilter filter = new LogMessageMatchesAnyFilter("This is a");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    @Transactional
    public void testLogMessageSubstringFilter(){
        LogMessageSubstringFilter filter = new LogMessageSubstringFilter("is a test");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    @Transactional
    public void testNegativeAcknowledgedByFilter(){
        NegativeAcknowledgedByFilter filter = new NegativeAcknowledgedByFilter("TestUser");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = null;
        
        m_daoEventRepo.acknowledgeAll("TestUser", new Date());
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    @Transactional
    public void testNegativeExactUeiFilter(){
        NegativeExactUEIFilter filter = new NegativeExactUEIFilter("uei.opennms.org/test2");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NegativeExactUEIFilter("uei.opennms.org/nontest");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    @Transactional
    public void testNegativeInterfaceFilter(){
        NegativeInterfaceFilter filter = new NegativeInterfaceFilter("192.168.1.1");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        filter = new NegativeInterfaceFilter("27.0.0.1");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    @Transactional
    public void testNegativeNodeFilter(){
        // should match the "RDU" event
        NegativeNodeFilter filter = new NegativeNodeFilter(m_dbPopulator.getNode2().getId(), m_appContext);
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);

        // should match the "Default" event
        filter = new NegativeNodeFilter(m_dbPopulator.getNode1().getId(), m_appContext);
        events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);

        assertEquals("node is not node1", filter.getTextDescription());
    }
    
    @Test
    @Transactional
    public void testNegativePartialUeiFilter(){
        NegativePartialUEIFilter filter = new NegativePartialUEIFilter("uei.opennms.org");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        filter = new NegativePartialUEIFilter("puei.org.opennms");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNegativeServiceFilter(){
        NegativeServiceFilter filter = new NegativeServiceFilter(1, m_appContext);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        filter = new NegativeServiceFilter(2, m_appContext);
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    @Transactional
    public void testNegativeSeverityFilter(){
        NegativeSeverityFilter filter = new NegativeSeverityFilter(OnmsSeverity.CRITICAL.getId());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        filter = new NegativeSeverityFilter(OnmsSeverity.CLEARED.getId());
        
        events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
    }
    
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNodeFilter() {
        // should match the "Default" event
        NodeFilter filter = new NodeFilter(1, m_appContext);
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);

        // should match the "RDU event
        filter = new NodeFilter(2, m_appContext);
        events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        assertEquals("node=node2", filter.getTextDescription());
    }
    
    @Test
    @Transactional
    public void testNodeNameLikeFilter(){
        NodeNameLikeFilter filter = new NodeNameLikeFilter("node1");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NodeNameLikeFilter("testNode");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    @Transactional
    public void testPartialUeiFilter(){
        PartialUEIFilter filter = new PartialUEIFilter("uei.opennms.org/t");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        filter = new PartialUEIFilter("unknown");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testServiceFilter(){
        ServiceFilter filter = new ServiceFilter(2, m_appContext);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        filter = new ServiceFilter(1, m_appContext);
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    @Transactional
    public void testSeverityFilter(){
        SeverityFilter filter = new SeverityFilter(OnmsSeverity.CLEARED.getId());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        filter = new SeverityFilter(OnmsSeverity.MAJOR.getId());
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
    }
    
    private static EventCriteria getCriteria(Filter... filters){
        return new EventCriteria(filters);
    }
    
    private Event[] getMatchingDaoEvents(Filter...filters) {
        return m_daoEventRepo.getMatchingEvents(getCriteria(filters));
    }
    
    private void assert1Result(Filter filter){
        EventCriteria criteria = new EventCriteria(filter);
        
        Event[] events = m_daoEventRepo.getMatchingEvents(criteria);
        assertEquals(1, events.length);
    }
    
    private static Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DATE, -1 );
        return cal.getTime();
    }

    @Test
    public void testLocationFilter(){
        final LocationFilter filter1 = new LocationFilter("Default");
        final Event[] events1 = getMatchingDaoEvents(filter1);
        assertEquals(2, events1.length);

        final LocationFilter filter2 = new LocationFilter("Non-Default");
        final Event[] events2 = getMatchingDaoEvents(filter2);
        assertEquals(0, events2.length);

        final LocationFilter filter3 = new LocationFilter("RDU");
        final Event[] events3 = getMatchingDaoEvents(filter3);
        assertEquals(0, events3.length);

    }

    @Test
    public void testNodeLocationFilter(){
        final NodeLocationFilter filter1 = new NodeLocationFilter("Default");
        final Event[] events1 = getMatchingDaoEvents(filter1);
        assertEquals(1, events1.length);

        final NodeLocationFilter filter2 = new NodeLocationFilter("Non-Default");
        final Event[] events2 = getMatchingDaoEvents(filter2);
        assertEquals(0, events2.length);

        final NodeLocationFilter filter3 = new NodeLocationFilter("RDU");
        final Event[] events3 = getMatchingDaoEvents(filter3);
        assertEquals(1, events3.length);

    }

    @Test
    public void testSystemIdFilter(){
        SystemIdFilter filter1 = new SystemIdFilter(m_dbPopulator.getDistPollerDao().whoami().getId());

        Event[] events1 = getMatchingDaoEvents(filter1);
        assertEquals(2, events1.length);

        SystemIdFilter filter2 = new SystemIdFilter("99999999-9999-9999-9999-999999999999");

        Event[] events2 = getMatchingDaoEvents(filter2);
        assertEquals(0, events2.length);
    }

    @Test
    public void testNegativeLocationFilter(){
        NegativeLocationFilter filter1 = new NegativeLocationFilter("Default");

        Event[] events1 = getMatchingDaoEvents(filter1);
        assertEquals(0, events1.length);

        NegativeLocationFilter filter2 = new NegativeLocationFilter("Non-Default");

        Event[] events2 = getMatchingDaoEvents(filter2);
        assertEquals(2, events2.length);
    }

    @Test
    public void testNegativeNodeLocationFilter(){
        // should match the "RDU" event
        final NegativeNodeLocationFilter filter1 = new NegativeNodeLocationFilter("Default");
        final Event[] events1 = getMatchingDaoEvents(filter1);
        assertEquals(1, events1.length);

        // should match all events
        final NegativeNodeLocationFilter filter2 = new NegativeNodeLocationFilter("Non-Default");
        final Event[] events2 = getMatchingDaoEvents(filter2);
        assertEquals(2, events2.length);

        // should match the "Default" event
        final NegativeNodeLocationFilter filter3 = new NegativeNodeLocationFilter("RDU");
        final Event[] events3 = getMatchingDaoEvents(filter3);
        assertEquals(1, events3.length);
    }

    @Test
    public void testNegativeSystemIdFilter(){
        NegativeSystemIdFilter filter1 = new NegativeSystemIdFilter(m_dbPopulator.getDistPollerDao().whoami().getId());

        Event[] events1 = getMatchingDaoEvents(filter1);
        assertEquals(0, events1.length);

        NegativeLocationFilter filter2 = new NegativeLocationFilter("99999999-9999-9999-9999-999999999999");

        Event[] events2 = getMatchingDaoEvents(filter2);
        assertEquals(2, events2.length);
    }
}
