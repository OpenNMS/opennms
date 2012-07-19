/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.NegativeSeverityFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/jdbcWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcWebEventRepositoryTest implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebEventRepository m_eventRepo;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    @Transactional
    public void testCountMatchingEvents(){ 
        EventCriteria criteria = new EventCriteria();
        int event = m_eventRepo.countMatchingEvents(criteria);
        
        assertEquals(1, event);
    }
    
    @Test
    @Transactional
    public void testCountMatchingEventsBySeverity(){
        EventCriteria criteria = new EventCriteria();
        int[] matchingEvents = m_eventRepo.countMatchingEventsBySeverity(criteria);
        
        assertEquals(8, matchingEvents.length);
        
        assertEquals(0, matchingEvents[OnmsSeverity.CLEARED.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.CRITICAL.getId()]);
        assertEquals(1, matchingEvents[OnmsSeverity.INDETERMINATE.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.MAJOR.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.MINOR.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.NORMAL.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.WARNING.getId()]);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetEvent(){
        Event event = m_eventRepo.getEvent(1);
        assertNotNull(event);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testAcknowledgeUnacknowledgeMatchingAlarms(){
        m_eventRepo.acknowledgeMatchingEvents("TestUser", new Date(), new EventCriteria(new EventIdFilter(1)));
        
        int matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, matchingEventCount);
        
        m_eventRepo.unacknowledgeMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        
        matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingEventCount);
    }
    
    @Test
    @Transactional
    public void testAcknowledgeUnacknowledgeAllAlarms(){
        m_eventRepo.acknowledgeAll("TestUser", new Date());
        
        int matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, matchingEventCount);
        
        m_eventRepo.unacknowledgeAll();
        
        matchingEventCount = m_eventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingEventCount);
    }
    
    @Test
    @Transactional
    public void testCountMatchingBySeverity(){
        
        int[] matchingEventCount = m_eventRepo.countMatchingEventsBySeverity(new EventCriteria(new SeverityFilter(3)));
        assertEquals(8, matchingEventCount.length);
    }
    
    @Test
    @Transactional
    public void testFilterBySeverity() {
        NegativeSeverityFilter filter = new NegativeSeverityFilter(OnmsSeverity.NORMAL.getId());
        
        EventCriteria criteria = new EventCriteria(filter);
        Event[] events = m_eventRepo.getMatchingEvents(criteria);
        assertTrue(events.length > 0);
        
        EventCriteria sortedCriteria = new EventCriteria(new Filter[] { filter }, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        Event[] sortedEvents = m_eventRepo.getMatchingEvents(sortedCriteria);
        assertTrue(sortedEvents.length > 0);
         
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testDoubleFilterTest(){
        m_eventRepo.acknowledgeAll("TestUser", new Date());
        
        EventIdFilter filter1 = new EventIdFilter(1);
        AcknowledgedByFilter filter2 = new AcknowledgedByFilter("TestUser");
        EventCriteria criteria = new EventCriteria(filter1, filter2);
        
        Event[] events = m_eventRepo.getMatchingEvents(criteria);
        assertEquals(1, events.length);
        
    }

}
