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
package org.opennms.web.rest.v2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"

})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventConfPersistenceServiceIT {

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testPersistNewEventConfSourceAndEvents() {
        String filename = "test-events.xml";
        String username = "integration_test_user";
        Date now = new Date();
        EventConfSourceMetadataDto metadata = new EventConfSourceMetadataDto.Builder().filename(filename).eventCount(1).fileOrder(1).username(username).now(now).vendor("test-vendor").description("integration test file").build();
        Event event = new Event();
        event.setUei("uei.opennms.org/test/it");
        event.setEventLabel("IT Event");
        event.setDescr("This is an integration test event.");
        event.setSeverity("Normal");
        Events events = new Events();
        events.getEvents().add(event);
        eventConfPersistenceService.persistEventConfFile(events, metadata);

        List<EventConfSource> sources = eventConfSourceDao.findAllByFileOrder();
        Assert.assertEquals(1, sources.size());
        EventConfSource source = sources.get(0);
        Assert.assertEquals(filename, source.getName());
        Assert.assertEquals("integration test file", source.getDescription());
        Assert.assertEquals("test-vendor", source.getVendor());
        Assert.assertEquals(username, source.getUploadedBy());

        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        Assert.assertEquals(1, dbEvents.size());
        EventConfEvent persistedEvent = dbEvents.get(0);
        Assert.assertEquals("uei.opennms.org/test/it", persistedEvent.getUei());
        Assert.assertEquals("IT Event", persistedEvent.getEventLabel());
        Assert.assertEquals("This is an integration test event.", persistedEvent.getDescription());
        Assert.assertTrue(persistedEvent.getEnabled());
        Assert.assertEquals(username, persistedEvent.getModifiedBy());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testPersistUpdatesExistingSource() {
        String filename = "existing-source.xml";
        String username = "test_user";
        Date now = new Date();
        EventConfSourceMetadataDto metadata = new EventConfSourceMetadataDto.Builder().filename(filename).eventCount(2).fileOrder(2).username(username).now(now).vendor("update-vendor").description("original entry").build();
        Event event = new Event();
        event.setUei("uei.opennms.org/test/update");
        event.setEventLabel("Initial Event");
        event.setDescr("Initial Description");
        event.setSeverity("Normal");
        Events events = new Events();
        events.getEvents().add(event);
        eventConfPersistenceService.persistEventConfFile(events, metadata);

        EventConfSourceMetadataDto updatedMetadata = new EventConfSourceMetadataDto.Builder().filename(filename).eventCount(3).fileOrder(3).username("updated_user").now(new Date()).vendor("updated-vendor").description("updated entry").build();
        Event updatedEvent = new Event();
        updatedEvent.setUei("uei.opennms.org/test/update2");
        updatedEvent.setEventLabel("Updated Event");
        updatedEvent.setDescr("Updated Description");
        updatedEvent.setSeverity("Normal");
        Events updatedEvents = new Events();
        updatedEvents.getEvents().add(updatedEvent);
        eventConfPersistenceService.persistEventConfFile(updatedEvents, updatedMetadata);
        List<EventConfSource> sources = eventConfSourceDao.findAllByFileOrder();

        Assert.assertEquals(1, sources.size());
        EventConfSource source = sources.get(0);
        Assert.assertEquals(filename, source.getName());
        Assert.assertEquals("updated entry", source.getDescription());
        Assert.assertEquals("updated-vendor", source.getVendor());
        Assert.assertEquals("updated_user", source.getUploadedBy());
        Assert.assertEquals(3, (int) source.getFileOrder());
        List<EventConfEvent> updatedDbEvents = eventConfEventDao.findEnabledEvents();
        Assert.assertEquals(1, updatedDbEvents.size());
        EventConfEvent finalEvent = updatedDbEvents.get(0);
        Assert.assertEquals("uei.opennms.org/test/update2", finalEvent.getUei());
        Assert.assertEquals("Updated Description", finalEvent.getDescription());
    }

    @Test
    @JUnitTemporaryDatabase  
    @Transactional
    public void testLoadEventConfFromDatabase() throws Exception {
        // Create multiple sources with events to test loading from database
        String username = "db_test_user";
        Date now = new Date();

        // Source with fileOrder 0 (highest priority)
        EventConfSourceMetadataDto metadata1 = new EventConfSourceMetadataDto.Builder()
                .filename("high-priority.xml")
                .eventCount(2)
                .fileOrder(0)
                .username(username)
                .now(now)
                .vendor("test-vendor")
                .description("high priority events")
                .build();
        
        Events events1 = new Events();
        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test/high1");
        event1.setEventLabel("High Priority Event 1");
        event1.setDescr("First high priority event");
        event1.setSeverity("Critical");
        events1.getEvents().add(event1);
        
        Event event2 = new Event();
        event2.setUei("uei.opennms.org/test/high2");
        event2.setEventLabel("High Priority Event 2");
        event2.setDescr("Second high priority event");
        event2.setSeverity("Major");
        events1.getEvents().add(event2);

        // Source with fileOrder 5 (lower priority)
        EventConfSourceMetadataDto metadata2 = new EventConfSourceMetadataDto.Builder()
                .filename("medium-priority.xml")
                .eventCount(1)
                .fileOrder(5)
                .username(username)
                .now(now)
                .vendor("test-vendor")
                .description("medium priority events")
                .build();
        
        Events events2 = new Events();
        Event event3 = new Event();
        event3.setUei("uei.opennms.org/test/medium");
        event3.setEventLabel("Medium Priority Event");
        event3.setDescr("Medium priority event");
        event3.setSeverity("Normal");
        events2.getEvents().add(event3);

        // Source with fileOrder 2 (between high and medium)
        EventConfSourceMetadataDto metadata3 = new EventConfSourceMetadataDto.Builder()
                .filename("low-medium-priority.xml")
                .eventCount(1)
                .fileOrder(2)
                .username(username)
                .now(now)
                .vendor("test-vendor")
                .description("low-medium priority events")
                .build();
        
        Events events3 = new Events();
        Event event4 = new Event();
        event4.setUei("uei.opennms.org/test/lowmedium");
        event4.setEventLabel("Low-Medium Priority Event");
        event4.setDescr("Low-medium priority event");
        event4.setSeverity("Minor");
        events3.getEvents().add(event4);

        // Source with fileOrder 10 (lowest priority)
        EventConfSourceMetadataDto metadata4 = new EventConfSourceMetadataDto.Builder()
                .filename("lowest-priority.xml")
                .eventCount(2)
                .fileOrder(10)
                .username(username)
                .now(now)
                .vendor("test-vendor")
                .description("lowest priority events")
                .build();
        
        Events events4 = new Events();
        Event event5 = new Event();
        event5.setUei("uei.opennms.org/test/lowest1");
        event5.setEventLabel("Lowest Priority Event 1");
        event5.setDescr("First lowest priority event");
        event5.setSeverity("Indeterminate");
        events4.getEvents().add(event5);
        
        Event event6 = new Event();
        event6.setUei("uei.opennms.org/test/lowest2");
        event6.setEventLabel("Lowest Priority Event 2");
        event6.setDescr("Second lowest priority event");
        event6.setSeverity("Cleared");
        events4.getEvents().add(event6);

        // Source with fileOrder 1 (second highest priority)
        EventConfSourceMetadataDto metadata5 = new EventConfSourceMetadataDto.Builder()
                .filename("second-high-priority.xml")
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("test-vendor")
                .description("second high priority events")
                .build();
        
        Events events5 = new Events();
        Event event7 = new Event();
        event7.setUei("uei.opennms.org/test/secondhigh");
        event7.setEventLabel("Second High Priority Event");
        event7.setDescr("Second high priority event");
        event7.setSeverity("Major");
        events5.getEvents().add(event7);

        // Persist test data
        eventConfPersistenceService.persistEventConfFile(events1, metadata1);
        eventConfPersistenceService.persistEventConfFile(events2, metadata2);
        eventConfPersistenceService.persistEventConfFile(events3, metadata3);
        eventConfPersistenceService.persistEventConfFile(events4, metadata4);
        eventConfPersistenceService.persistEventConfFile(events5, metadata5);

        // Verify data was persisted
        List<EventConfSource> sources = eventConfSourceDao.findAllByFileOrder();
        Assert.assertEquals("Should have 5 sources", 5, sources.size());
        
        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        Assert.assertEquals("Should have 7 events total", 7, dbEvents.size());

        // Now test the DefaultEventConfDao loadEventConfFromDB method
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        // Set a minimal config resource to satisfy loadConfig() requirement
        try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(
            "<?xml version=\"1.0\"?><events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\"></events>".getBytes())) {
            eventConfDao.setConfigResource(new org.springframework.core.io.InputStreamResource(bis));
            eventConfDao.afterPropertiesSet();
        }


        // Wait for async loading to complete
        await().atMost(10, TimeUnit.SECONDS)
               .until(() -> eventConfDao.getRootEvents() != null);

        // Verify events were loaded correctly
        Events rootEvents = eventConfDao.getRootEvents();
        Assert.assertNotNull("Root events should not be null", rootEvents);
        
        List<Event> allEvents = eventConfDao.getAllEvents();
        Assert.assertEquals("Should have loaded 7 events from database", 7, allEvents.size());

        // Test that events can be found by UEI
        Event foundEvent1 = eventConfDao.findByUei("uei.opennms.org/test/high1");
        Assert.assertNotNull("Should find high priority event 1", foundEvent1);
        Assert.assertEquals("High Priority Event 1", foundEvent1.getEventLabel());
        Assert.assertEquals("Critical", foundEvent1.getSeverity());

        Event foundEvent2 = eventConfDao.findByUei("uei.opennms.org/test/high2");
        Assert.assertNotNull("Should find high priority event 2", foundEvent2);
        Assert.assertEquals("High Priority Event 2", foundEvent2.getEventLabel());
        Assert.assertEquals("Major", foundEvent2.getSeverity());

        Event foundEvent3 = eventConfDao.findByUei("uei.opennms.org/test/medium");
        Assert.assertNotNull("Should find medium priority event", foundEvent3);
        Assert.assertEquals("Medium Priority Event", foundEvent3.getEventLabel());
        Assert.assertEquals("Normal", foundEvent3.getSeverity());

        Event foundEvent4 = eventConfDao.findByUei("uei.opennms.org/test/lowmedium");
        Assert.assertNotNull("Should find low-medium priority event", foundEvent4);
        Assert.assertEquals("Low-Medium Priority Event", foundEvent4.getEventLabel());
        Assert.assertEquals("Minor", foundEvent4.getSeverity());

        Event foundEvent5 = eventConfDao.findByUei("uei.opennms.org/test/lowest1");
        Assert.assertNotNull("Should find lowest priority event 1", foundEvent5);
        Assert.assertEquals("Lowest Priority Event 1", foundEvent5.getEventLabel());
        Assert.assertEquals("Indeterminate", foundEvent5.getSeverity());

        Event foundEvent6 = eventConfDao.findByUei("uei.opennms.org/test/lowest2");
        Assert.assertNotNull("Should find lowest priority event 2", foundEvent6);
        Assert.assertEquals("Lowest Priority Event 2", foundEvent6.getEventLabel());
        Assert.assertEquals("Cleared", foundEvent6.getSeverity());

        Event foundEvent7 = eventConfDao.findByUei("uei.opennms.org/test/secondhigh");
        Assert.assertNotNull("Should find second high priority event", foundEvent7);
        Assert.assertEquals("Second High Priority Event", foundEvent7.getEventLabel());
        Assert.assertEquals("Major", foundEvent7.getSeverity());


        // Test that non-existent event returns null
        Event notFoundEvent = eventConfDao.findByUei("uei.opennms.org/test/nonexistent");
        Assert.assertNull("Should not find non-existent event", notFoundEvent);

        // Test getEventLabel method
        String label = eventConfDao.getEventLabel("uei.opennms.org/test/high1");
        Assert.assertEquals("High Priority Event 1", label);

        String nullLabel = eventConfDao.getEventLabel("uei.opennms.org/test/nonexistent");
        Assert.assertNull("Should return null for non-existent UEI", nullLabel);

        // Test getEvents method (returns list of events for a UEI)
        List<Event> eventsForUei = eventConfDao.getEvents("uei.opennms.org/test/high1");
        Assert.assertNotNull("Should return events list", eventsForUei);
        Assert.assertEquals("Should find 1 event for this UEI", 1, eventsForUei.size());
        Assert.assertEquals("High Priority Event 1", eventsForUei.get(0).getEventLabel());

        // Test fileOrder - verify events are loaded in correct order by checking loaded event files
        Events events = rootEvents;
        Assert.assertNotNull("Events object should not be null", events);
        
        // The event files should be ordered by fileOrder: 0, 1, 2, 5, 10
        List<String> expectedOrder = List.of(
                "high-priority.xml",        // fileOrder 0
                "second-high-priority.xml", // fileOrder 1  
                "low-medium-priority.xml",  // fileOrder 2
                "medium-priority.xml",      // fileOrder 5
                "lowest-priority.xml"       // fileOrder 10
        );
        List<String> actualOrder = events.getEventFiles();
        
        Assert.assertEquals("Should have 5 event files loaded", 5, actualOrder.size());
        for (int i = 0; i < expectedOrder.size(); i++) {
            Assert.assertEquals("Event file order should match fileOrder priority at index " + i, 
                    expectedOrder.get(i), actualOrder.get(i));
        }

    }

}
