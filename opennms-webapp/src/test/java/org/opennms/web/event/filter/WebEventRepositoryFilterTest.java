/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.event.filter;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.filter.AlarmIdFilter;
import org.opennms.web.event.Event;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                  "classpath*:/META-INF/opennms/component-dao.xml",
                                  "classpath*:/META-INF/opennms/component-service.xml",
                                  "classpath:/daoWebEventRepositoryTestContext.xml",
                                  "classpath:/jdbcWebEventRepositoryTestContext.xml",
                                  "classpath:/NetworkElementFactoryContext.xml"})
@JUnitTemporaryDatabase()
public class WebEventRepositoryFilterTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    @Qualifier("dao")
    WebEventRepository m_daoEventRepo;
    
    @Autowired
    @Qualifier("jdbc")
    WebEventRepository m_jdbcEventRepo;
    
    @Autowired
    ApplicationContext m_appContext;
    
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
        
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getDistPoller("localhost", "127.0.0.1"));
        event.setAlarm(m_dbPopulator.getAlarmDao().get(1));
        event.setNode(m_dbPopulator.getNode1());
        event.setEventUei("uei.opennms.org/test2");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(OnmsSeverity.CLEARED.getId());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setIfIndex(11);
        event.setIpAddr("192.168.1.1");
        event.setEventLogMsg("This is a test log message");
        event.setEventDescr("This is a test event");
        event.setServiceType(m_dbPopulator.getServiceTypeDao().get(1));
        m_dbPopulator.getEventDao().save(event);
        m_dbPopulator.getEventDao().flush();
    }
    
    private OnmsDistPoller getDistPoller(String localhost, String localhostIp) {
        OnmsDistPoller distPoller = m_dbPopulator.getDistPollerDao().get(localhost);
        if (distPoller == null) {
            distPoller = new OnmsDistPoller(localhost, localhostIp);
            m_dbPopulator.getDistPollerDao().save(distPoller);
            m_dbPopulator.getDistPollerDao().flush();
        }
        return distPoller;
    }
    
    @Test
    @Transactional
    public void testEventIdFilter(){
        EventIdFilter filter = new EventIdFilter(1);
        assert1Result(filter);
    }
    
    @Test
    @Transactional
    public void testEventIdListFilter(){
        int[] ids = {1};
        EventIdListFilter filter = new EventIdListFilter(ids);
        assert1Result(filter);
        
    }
    
    @Test
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
        
        events = m_jdbcEventRepo.getMatchingEvents(criteria);
        assertEquals(0, events.length);
        
        m_daoEventRepo.acknowledgeAll("TestUser", new Date());
        events = m_jdbcEventRepo.getMatchingEvents(criteria);
        assertEquals(2, events.length);
        
    }
    
    @Test
    public void testAfterDateFilter(){
        AfterDateFilter filter = new AfterDateFilter(yesterday());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    public void testAlarmIdFilter(){
        AlarmIdFilter filter = new AlarmIdFilter(1);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    public void testBeforeDateFilter(){
        BeforeDateFilter filter = new BeforeDateFilter(new Date());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    public void testDescriptionSubstringFilterTest(){
        DescriptionSubstringFilter filter = new DescriptionSubstringFilter("test event");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        assertEquals("This is a test event", events[0].getDescription());
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        assertEquals("This is a test event", events[0].getDescription());
    }
    
    @Test
    public void testExactUEIFilter(){
        ExactUEIFilter filter = new ExactUEIFilter("uei.opennms.org/test2");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        assertEquals("uei.opennms.org/test2", events[0].getUei());
        
        events = getMatchingJdbcEvents(new ExactUEIFilter("uei.opennms.org/test"));
        assertEquals(1, events.length);
        assertEquals("uei.opennms.org/test", events[0].getUei());
    }
    
    @Test
    public void testIfIndexFilter(){
        IfIndexFilter filter = new IfIndexFilter(11);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingDaoEvents(new IfIndexFilter(1));
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(new IfIndexFilter(1));
        assertEquals(0, events.length);
    }
    
    @Test
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        assertEquals("192.168.1.1", events[0].getIpAddress());
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        assertEquals("192.168.1.1", events[0].getIpAddress());
        
    }
    
    @Test
    public void testIpAddrLikeFilter(){
        IPAddrLikeFilter filter = new IPAddrLikeFilter("192.168.*.*");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new IPAddrLikeFilter("193.168");
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    public void testLogMessageMatchesAny(){
        LogMessageMatchesAnyFilter filter = new LogMessageMatchesAnyFilter("This is a");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = null;
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    public void testLogMessageSubstringFilter(){
        LogMessageSubstringFilter filter = new LogMessageSubstringFilter("is a test");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    public void testNegativeAcknowledgedByFilter(){
        NegativeAcknowledgedByFilter filter = new NegativeAcknowledgedByFilter("TestUser");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = null;
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
        
        m_daoEventRepo.acknowledgeAll("TestUser", new Date());
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    public void testNegativeExactUeiFilter(){
        NegativeExactUEIFilter filter = new NegativeExactUEIFilter("uei.opennms.org/test2");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NegativeExactUEIFilter("uei.opennms.org/nontest");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    public void testNegativeInterfaceFilter(){
        NegativeInterfaceFilter filter = new NegativeInterfaceFilter("192.168.1.1");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NegativeInterfaceFilter("27.0.0.1");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    public void testNegativeNodeFilter(){
        NegativeNodeFilter filter = new NegativeNodeFilter(2, m_appContext);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
        
        filter = new NegativeNodeFilter(1, m_appContext);
        
        events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        assertEquals("node is not node1", filter.getTextDescription());
    }
    
    @Test
    public void testNegativePartialUeiFilter(){
        NegativePartialUEIFilter filter = new NegativePartialUEIFilter("uei.opennms.org");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
        
        filter = new NegativePartialUEIFilter("puei.org.opennms");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    public void testNegativeServiceFilter(){
        NegativeServiceFilter filter = new NegativeServiceFilter(1);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NegativeServiceFilter(2);
        
        events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
    }
    
    @Test
    public void testNegativeSeverityFilter(){
        NegativeSeverityFilter filter = new NegativeSeverityFilter(OnmsSeverity.CRITICAL.getId());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
        
        filter = new NegativeSeverityFilter(OnmsSeverity.CLEARED.getId());
        
        events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
    }
    
    
    @Test
    public void testNodeFilter(){
        NodeFilter filter = new NodeFilter(1, m_appContext);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NodeFilter(2, m_appContext);
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
        
        assertEquals("node=node2", filter.getTextDescription());
    }
    
    @Test
    public void testNodeNameLikeFilter(){
        NodeNameLikeFilter filter = new NodeNameLikeFilter("node1");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new NodeNameLikeFilter("testNode");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
        
    }
    
    @Test
    public void testPartialUeiFilter(){
        PartialUEIFilter filter = new PartialUEIFilter("uei.opennms.org/t");
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(2, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(2, events.length);
        
        filter = new PartialUEIFilter("unknown");
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
    }
    
    @Test
    public void testServiceFilter(){
        ServiceFilter filter = new ServiceFilter(2);
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
        
        filter = new ServiceFilter(1);
        
        events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
    }
    
    @Test
    public void testSeverityFilter(){
        SeverityFilter filter = new SeverityFilter(OnmsSeverity.CLEARED.getId());
        
        Event[] events = getMatchingDaoEvents(filter);
        assertEquals(1, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(1, events.length);
        
        filter = new SeverityFilter(OnmsSeverity.MAJOR.getId());
        
        events = getMatchingDaoEvents(filter);
        assertEquals(0, events.length);
        
        events = getMatchingJdbcEvents(filter);
        assertEquals(0, events.length);
    }
    
    private EventCriteria getCriteria(Filter... filters){
        return new EventCriteria(filters);
    }
    
    private Event[] getMatchingDaoEvents(Filter...filters) {
        return m_daoEventRepo.getMatchingEvents(getCriteria(filters));
    }
    
    private Event[] getMatchingJdbcEvents(Filter...filters){
        return m_jdbcEventRepo.getMatchingEvents(getCriteria(filters));
    }

    private void assert1Result(Filter filter){
        EventCriteria criteria = new EventCriteria(filter);
        
        Event[] events = m_jdbcEventRepo.getMatchingEvents(criteria);
        assertEquals(1, events.length);
        
        events = m_daoEventRepo.getMatchingEvents(criteria);
        assertEquals(1, events.length);
    }
    
    private Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DATE, -1 );
        return cal.getTime();
    }
}
