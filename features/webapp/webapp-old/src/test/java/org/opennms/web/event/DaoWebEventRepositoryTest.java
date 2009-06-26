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
package org.opennms.web.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

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
import org.opennms.web.event.filter.AcknowledgedByFilter;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.NegativeSeverityFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
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
                                  "classpath:/daoWebEventRepositoryTestContext.xml"})
@JUnitTemporaryDatabase()
public class DaoWebEventRepositoryTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebEventRepository m_daoEventRepo;
    
    @Before
    public void setUp(){
        assertNotNull(m_daoEventRepo);
        m_dbPopulator.populateDatabase();
        
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getDistPoller("localhost", "127.0.0.1"));
        event.setEventUei("uei.opennms.org/test2");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(OnmsSeverity.CLEARED.getId());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
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
    public void testCountMatchingEvents(){ 
        EventCriteria criteria = new EventCriteria();
        int event = m_daoEventRepo.countMatchingEvents(criteria);
        
        assertEquals(2, event);
    }
    
    @Test
    public void testCountMatchingEventsBySeverity(){
        EventCriteria criteria = new EventCriteria();
        int[] matchingEvents = m_daoEventRepo.countMatchingEventsBySeverity(criteria);
        
        assertNotNull(matchingEvents);
        assertEquals(8, matchingEvents.length);
        
        assertEquals(1, matchingEvents[OnmsSeverity.CLEARED.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.CRITICAL.getId()]);
        assertEquals(1, matchingEvents[OnmsSeverity.INDETERMINATE.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.MAJOR.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.MINOR.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.NORMAL.getId()]);
        assertEquals(0, matchingEvents[OnmsSeverity.WARNING.getId()]);
    }
    
    @Test
    public void testGetEvent(){

        Event event = m_daoEventRepo.getEvent(1);
        assertNotNull(event);
        
        assertEquals("uei.opennms.org/test", event.uei);
    }
    
    @Test
    @Transactional
    public void testAcknowledgeUnacknowledgeMatchingAlarms(){
        m_daoEventRepo.acknowledgeMatchingEvents("TestUser", new Date(), new EventCriteria(new EventIdFilter(1)));
        
        int matchingEventCount = m_daoEventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(1, matchingEventCount);
        
        m_daoEventRepo.unacknowledgeMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        
        matchingEventCount = m_daoEventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingEventCount);
    }
    
    @Test
    @Transactional
    public void testAcknowledgeUnacknowledgeAllAlarms(){
        m_daoEventRepo.acknowledgeAll("TestUser", new Date());
        
        int matchingEventCount = m_daoEventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(2, matchingEventCount);
        
        m_daoEventRepo.unacknowledgeAll();
        
        matchingEventCount = m_daoEventRepo.countMatchingEvents(new EventCriteria(new AcknowledgedByFilter("TestUser")));
        assertEquals(0, matchingEventCount);
    }
    
    @Test
    @Transactional
    public void testCountMatchingBySeverity(){
        
        int[] matchingEventCount = m_daoEventRepo.countMatchingEventsBySeverity(new EventCriteria(new SeverityFilter(3)));
        assertNotNull(matchingEventCount);
        assertEquals(8, matchingEventCount.length);
    }
    
    @Test
    @Transactional
    public void testFilterBySeverity() {
        NegativeSeverityFilter filter = new NegativeSeverityFilter(OnmsSeverity.NORMAL.getId());
        
        EventCriteria criteria = new EventCriteria(filter);
        Event[] events = m_daoEventRepo.getMatchingEvents(criteria);
        assertTrue(events.length > 0);
        
        EventCriteria sortedCriteria = new EventCriteria(new Filter[] { filter }, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, 100, 0);
        Event[] sortedEvents = m_daoEventRepo.getMatchingEvents(sortedCriteria);
        assertTrue(sortedEvents.length > 0);        
        
    }
    
}
