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
package org.opennms.web.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsEvent;
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
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class DaoWebEventRepositoryIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebEventRepository m_daoEventRepo;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
        
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_dbPopulator.getDistPollerDao().whoami());
        event.setEventUei("uei.opennms.org/test2");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(OnmsSeverity.CLEARED.getId());
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        m_dbPopulator.getEventDao().save(event);
        m_dbPopulator.getEventDao().flush();
        
        OnmsEvent event2 = new OnmsEvent();
        event2.setDistPoller(m_dbPopulator.getDistPollerDao().whoami());
        event2.setEventUei("uei.opennms.org/test3");
        event2.setEventTime(new Date());
        event2.setEventSource("test");
        event2.setEventCreateTime(new Date());
        event2.setEventSeverity(OnmsSeverity.CLEARED.getId());
        event2.setEventLog("Y");
        event2.setEventDisplay("N");
        m_dbPopulator.getEventDao().save(event2);
        m_dbPopulator.getEventDao().flush();
    }

    @Test
    @Transactional
    public void testCountMatchingEvents(){ 
        EventCriteria criteria = new EventCriteria();
        int event = m_daoEventRepo.countMatchingEvents(criteria);
        
        assertEquals(2, event);
    }
    
    @Test
    @Transactional
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
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetEvent(){

        Event event = m_daoEventRepo.getEvent(1);
        assertNotNull(event);
        
        assertEquals("uei.opennms.org/test", event.uei);
        assertNotNull(event.getEventDisplay());
        assertTrue(event.getEventDisplay());
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
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
