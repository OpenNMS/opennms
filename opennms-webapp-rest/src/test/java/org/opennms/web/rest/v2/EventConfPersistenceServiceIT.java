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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    private EventConfDao eventConfDao;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    private int defaultEventConfSize;
    private int defaultEventConfEventSize;

    @Before
    @Transactional
    public void setUp() {
        defaultEventConfSize =  eventConfSourceDao.findAllByFileOrder().size();
        defaultEventConfEventSize = eventConfEventDao.findAll().size();
    }


    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testPersistNewEventConfSourceAndEvents() {
        String filename = "test-events.xml";
        String username = "integration_test_user";
        Date now = new Date();
        EventConfSourceMetadataDto metadata = new EventConfSourceMetadataDto.Builder().filename(filename).eventCount(1).fileOrder(0).username(username).now(now).vendor("test-vendor").description("integration test file").build();
        Event event = new Event();
        event.setUei("uei.opennms.org/test/it");
        event.setEventLabel("IT Event");
        event.setDescr("This is an integration test event.");
        event.setSeverity("Normal");
        Events events = new Events();
        events.getEvents().add(event);
        eventConfPersistenceService.persistEventConfFile(events, metadata);

        List<EventConfSource> sources = eventConfSourceDao.findAllByFileOrder();
        assertEquals(1, sources.size() - defaultEventConfSize);
        EventConfSource source = sources.get(0);
        assertEquals(filename, source.getName());
        assertEquals("integration test file", source.getDescription());
        assertEquals("test-vendor", source.getVendor());
        assertEquals(username, source.getUploadedBy());

        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        assertEquals(1, dbEvents.size() - defaultEventConfEventSize);
        EventConfEvent persistedEvent = dbEvents.get(0);
        assertEquals("uei.opennms.org/test/it", persistedEvent.getUei());
        assertEquals("IT Event", persistedEvent.getEventLabel());
        assertEquals("This is an integration test event.", persistedEvent.getDescription());
        Assert.assertTrue(persistedEvent.getEnabled());
        assertEquals(username, persistedEvent.getModifiedBy());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testPersistUpdatesExistingSource() {
        String filename = "existing-source";
        String username = "test_user";
        Date now = new Date();
        EventConfSourceMetadataDto metadata = new EventConfSourceMetadataDto.Builder().filename(filename).eventCount(2).fileOrder(0).username(username).now(now).vendor("update-vendor").description("original entry").build();
        Event event = new Event();
        event.setUei("uei.opennms.org/test/update");
        event.setEventLabel("Initial Event");
        event.setDescr("Initial Description");
        event.setSeverity("Normal");
        Events events = new Events();
        events.getEvents().add(event);
        eventConfPersistenceService.persistEventConfFile(events, metadata);

        EventConfSourceMetadataDto updatedMetadata = new EventConfSourceMetadataDto.Builder().filename(filename).eventCount(3).fileOrder(1).username("updated_user").now(new Date()).vendor("updated-vendor").description("updated entry").build();
        Event updatedEvent = new Event();
        updatedEvent.setUei("uei.opennms.org/test/update2");
        updatedEvent.setEventLabel("Updated Event");
        updatedEvent.setDescr("Updated Description");
        updatedEvent.setSeverity("Normal");
        Events updatedEvents = new Events();
        updatedEvents.getEvents().add(updatedEvent);
        eventConfPersistenceService.persistEventConfFile(updatedEvents, updatedMetadata);
        List<EventConfSource> sources = eventConfSourceDao.findAllByFileOrder();

        assertEquals(1, sources.size() - defaultEventConfSize);
        EventConfSource source = sources.get(0);
        assertEquals(filename, source.getName());
        Assert.assertEquals("existing-source", source.getName());
        Assert.assertEquals("updated entry", source.getDescription());
        Assert.assertEquals("updated-vendor", source.getVendor());
        Assert.assertEquals("updated_user", source.getUploadedBy());
        Assert.assertEquals(0, (int) source.getFileOrder());
        List<EventConfEvent> updatedDbEvents = eventConfEventDao.findEnabledEvents();
        assertEquals(1, updatedDbEvents.size() - defaultEventConfEventSize);
        EventConfEvent finalEvent = updatedDbEvents.get(0);
        assertEquals("uei.opennms.org/test/update2", finalEvent.getUei());
        assertEquals("Updated Description", finalEvent.getDescription());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testFilterEventConf_ShouldReturnFilteredResults() {
        // Arrange: Persist multiple events with different vendors, UEIs, and source names
        String filename1 = "vendor-cisco.xml";
        String filename2 = "vendor-hp.xml";
        String username = "filter_test_user";
        Date now = new Date();

        // First metadata (Cisco vendor)
        EventConfSourceMetadataDto ciscoMetadata = new EventConfSourceMetadataDto.Builder()
                .filename(filename1)
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("Cisco")
                .description("Cisco events")
                .build();

        Event ciscoEvent = new Event();
        ciscoEvent.setUei("uei.opennms.org/vendor/cisco");
        ciscoEvent.setEventLabel("Cisco Event");
        ciscoEvent.setDescr("Cisco test event");
        ciscoEvent.setSeverity("Normal");

        Events ciscoEvents = new Events();
        ciscoEvents.getEvents().add(ciscoEvent);

        eventConfPersistenceService.persistEventConfFile(ciscoEvents, ciscoMetadata);

        // Second metadata (HP vendor)
        EventConfSourceMetadataDto hpMetadata = new EventConfSourceMetadataDto.Builder()
                .filename(filename2)
                .eventCount(1)
                .fileOrder(2)
                .username(username)
                .now(now)
                .vendor("HP")
                .description("HP events")
                .build();

        Event hpEvent = new Event();
        hpEvent.setUei("uei.opennms.org/vendor/hp");
        hpEvent.setEventLabel("HP Event");
        hpEvent.setDescr("HP test event");
        hpEvent.setSeverity("Normal");

        Events hpEvents = new Events();
        hpEvents.getEvents().add(hpEvent);

        eventConfPersistenceService.persistEventConfFile(hpEvents, hpMetadata);

        // Act: Filter only Cisco events
        List<EventConfEvent> filteredResults = eventConfPersistenceService.findEventConfByFilters(
                null, // uei
                "Cisco", // vendor
                null,  // sourceName
                0, //offset
                10 //limit
        );

        // Assert
        assertNotNull(filteredResults);
        Assert.assertFalse(filteredResults.isEmpty());
        Assert.assertTrue(filteredResults.stream()
                .allMatch(e -> "Cisco".equals(e.getSource().getVendor())));
        Assert.assertTrue(filteredResults.stream()
                .anyMatch(e -> e.getUei().contains("cisco")));
    }

    @Test
    public void filter_shouldReturnEmptyList_whenNoMatchesFound() {
        List<EventConfEvent> results = eventConfPersistenceService
                .findEventConfByFilters("nonexistent-uei", "nonexistent-vendor", "nonexistent-source", 0, 10);

        assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testUpdateSourceAndEventEnabled() {
        String username = "test_user";
        Date now = new Date();

        String filename1 = "source-file-1";
        EventConfSourceMetadataDto metadata1 = new EventConfSourceMetadataDto.Builder()
                .filename(filename1)
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("vendor-1")
                .description("first entry")
                .build();

        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test/update/1");
        event1.setEventLabel("Event One");
        event1.setDescr("Description for Event One");
        event1.setSeverity("Normal");

        Events events1 = new Events();
        events1.getEvents().add(event1);

        eventConfPersistenceService.persistEventConfFile(events1, metadata1);

        String filename2 = "source-file-2";
        EventConfSourceMetadataDto metadata2 = new EventConfSourceMetadataDto.Builder()
                .filename(filename2)
                .eventCount(1)
                .fileOrder(2)
                .username(username)
                .now(now)
                .vendor("vendor-2")
                .description("second entry")
                .build();

        Event event2 = new Event();
        event2.setUei("uei.opennms.org/test/update/2");
        event2.setEventLabel("Event Two");
        event2.setDescr("Description for Event Two");
        event2.setSeverity("Warning");

        Events events2 = new Events();
        events2.getEvents().add(event2);

        eventConfPersistenceService.persistEventConfFile(events2, metadata2);

        List<Long> sourcesIds = eventConfSourceDao.findAll()
                .stream().map(EventConfSource::getId).toList();
        // Disable eventConfSources and eventConfEvents.
        EventConfSrcEnableDisablePayload eventConfSrcDisablePayload = new EventConfSrcEnableDisablePayload(false, true, sourcesIds);
        eventConfPersistenceService.updateSourceAndEventEnabled(eventConfSrcDisablePayload);
        List<EventConfSource> eventConfSources = eventConfSourceDao.findAll();
        assertTrue(eventConfSources.stream().noneMatch(EventConfSource::getEnabled));
        List<EventConfEvent> eventConfEvents = eventConfEventDao.findAll();
        assertTrue(eventConfEvents.stream().noneMatch(EventConfEvent::getEnabled));

        // Enable eventConfSources and eventConfEvents.
        EventConfSrcEnableDisablePayload eventConfSrcEnablePayload = new EventConfSrcEnableDisablePayload(true, true, sourcesIds);
        eventConfPersistenceService.updateSourceAndEventEnabled(eventConfSrcEnablePayload);
        List<EventConfSource> enableEventConfSources = eventConfSourceDao.findAll();
        assertFalse(enableEventConfSources.stream().noneMatch(EventConfSource::getEnabled));
        List<EventConfEvent> enableEventConfEvents = eventConfEventDao.findAll();
        assertFalse(enableEventConfEvents.stream().noneMatch(EventConfEvent::getEnabled));
    }

    @Test
    @Transactional
    public void testFilterConfEventsBySourceId_ShouldReturnFilteredResults() {
        String filename1 = "vendor-cisco.xml";
        String filename2 = "vendor-hp.xml";
        String username = "filter_test_user";
        Date now = new Date();

        // First metadata (Cisco vendor)
        EventConfSourceMetadataDto ciscoMetadata = new EventConfSourceMetadataDto.Builder()
                .filename(filename1)
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("Cisco")
                .description("Cisco events")
                .build();

        Event ciscoEvent = new Event();
        ciscoEvent.setUei("uei.opennms.org/vendor/cisco");
        ciscoEvent.setEventLabel("Cisco Event");
        ciscoEvent.setDescr("Cisco test event");
        ciscoEvent.setSeverity("Normal");

        Events ciscoEvents = new Events();
        ciscoEvents.getEvents().add(ciscoEvent);

        eventConfPersistenceService.persistEventConfFile(ciscoEvents, ciscoMetadata);

        // Second metadata (HP vendor)
        EventConfSourceMetadataDto hpMetadata = new EventConfSourceMetadataDto.Builder()
                .filename(filename2)
                .eventCount(1)
                .fileOrder(2)
                .username(username)
                .now(now)
                .vendor("HP")
                .description("HP events")
                .build();

        Event hpEvent = new Event();
        hpEvent.setUei("uei.opennms.org/vendor/hp");
        hpEvent.setEventLabel("HP Event");
        hpEvent.setDescr("HP test event");
        hpEvent.setSeverity("Normal");

        Events hpEvents = new Events();
        hpEvents.getEvents().add(hpEvent);

        eventConfPersistenceService.persistEventConfFile(hpEvents, hpMetadata);

        EventConfSource eventConfSource = eventConfSourceDao.findByName("vendor-hp.xml");
        Assert.assertNotNull(eventConfSource);

        Map<String, Object> result = eventConfPersistenceService.filterConfEventsBySourceId(eventConfSource.getId(), 0, 0, 10);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("totalRecords"));
        Assert.assertEquals(1, result.get("totalRecords"));
        Assert.assertTrue(result.containsKey("eventConfEventList"));

        List<EventConfEvent> eventConfEventList = (List<EventConfEvent>) result.get("eventConfEventList");

        Assert.assertNotNull(eventConfEventList);
        Assert.assertFalse(eventConfEventList.isEmpty());
        Assert.assertTrue(eventConfEventList.stream().allMatch(e -> "HP Event".equals(e.getEventLabel())));



        eventConfSource = eventConfSourceDao.findByName("vendor-cisco.xml");
        Assert.assertNotNull(eventConfSource);

        result = eventConfPersistenceService.filterConfEventsBySourceId(eventConfSource.getId(), 0, 0, 10);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("totalRecords"));
        Assert.assertEquals(1, result.get("totalRecords"));
        Assert.assertTrue(result.containsKey("eventConfEventList"));

        eventConfEventList = (List<EventConfEvent>) result.get("eventConfEventList");


        Assert.assertNotNull(eventConfEventList);
        Assert.assertFalse(eventConfEventList.isEmpty());
        Assert.assertTrue(eventConfEventList.stream().allMatch(e -> "Cisco Event".equals(e.getEventLabel())));
    }

    @Test
    @Transactional
    public void testFilterConfEventsBySourceId_ShouldReturnEmptyResults() {
        Long sourceId = 8000L;
        Map<String, Object> result = eventConfPersistenceService.filterConfEventsBySourceId(sourceId, 0, 0, 10);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("totalRecords"));
        Assert.assertEquals(0, result.get("totalRecords"));
        Assert.assertTrue(result.containsKey("eventConfEventList"));

        List<EventConfEvent> eventConfEventList = (List<EventConfEvent>) result.get("eventConfEventList");
        Assert.assertNotNull(eventConfEventList);
        Assert.assertTrue(eventConfEventList.isEmpty());
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testLoadingOfEventsInMemory() {

        // Call loadEventsFromDB directly
        var dbEvents = eventConfEventDao.findEnabledEvents();
        eventConfDao.loadEventsFromDB(dbEvents);
        var event = eventConfDao.findByUei("uei.opennms.org/circuitBreaker/stateChange");
        assertNotNull(event);
        // This is not unique uei so getEventByUeiOptimistic will exclude this.
        var uniqueEvent = eventConfDao.getRootEvents().getEventByUeiOptimistic("uei.opennms.org/circuitBreaker/stateChange");
        assertNull(uniqueEvent);
        assertEquals(defaultEventConfEventSize, eventConfDao.getEventUEIs().size());

    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testDeleteEventConfSources() throws Exception {
        String username = "test_user";
        Date now = new Date();

        String filename1 = "source-file-1.xml";
        EventConfSourceMetadataDto metadata1 = new EventConfSourceMetadataDto.Builder()
                .filename(filename1)
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("vendor-1")
                .description("first entry")
                .build();

        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test/update/1");
        event1.setEventLabel("Event One");
        event1.setDescr("Description for Event One");
        event1.setSeverity("Normal");

        Events events1 = new Events();
        events1.getEvents().add(event1);

        eventConfPersistenceService.persistEventConfFile(events1, metadata1);

        String filename2 = "source-file-2.xml";
        EventConfSourceMetadataDto metadata2 = new EventConfSourceMetadataDto.Builder()
                .filename(filename2)
                .eventCount(1)
                .fileOrder(2)
                .username(username)
                .now(now)
                .vendor("vendor-2")
                .description("second entry")
                .build();

        Event event2 = new Event();
        event2.setUei("uei.opennms.org/test/update/2");
        event2.setEventLabel("Event Two");
        event2.setDescr("Description for Event Two");
        event2.setSeverity("Warning");

        Events events2 = new Events();
        events2.getEvents().add(event2);

        eventConfPersistenceService.persistEventConfFile(events2, metadata2);

        List<Long> sourcesIds = eventConfSourceDao.findAll()
                .stream().map(EventConfSource::getId).toList();
        // delete eventConfSources and its related events
        EventConfSourceDeletePayload eventConfSrcDisablePayload = new EventConfSourceDeletePayload();
        eventConfSrcDisablePayload.setSourceIds(sourcesIds);
        eventConfPersistenceService.deleteEventConfSources(eventConfSrcDisablePayload);
        List<EventConfSource> eventConfSources = eventConfSourceDao.findAll();
        assertTrue(eventConfSources.isEmpty());

    }

    @Test
    @JUnitTemporaryDatabase
    public void testEnableDisableEventConfSourcesEvents() {
        String username = "test_user";
        Date now = new Date();

        String filename1 = "source-file-1.xml";
        EventConfSourceMetadataDto metadata1 = new EventConfSourceMetadataDto.Builder()
                .filename(filename1)
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("vendor-1")
                .description("first entry")
                .build();

        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test/trigger/1");
        event1.setEventLabel("Event One");
        event1.setDescr("Description for Event One");
        event1.setSeverity("Normal");

        Events events1 = new Events();
        events1.getEvents().add(event1);

        eventConfPersistenceService.persistEventConfFile(events1, metadata1);

        String filename2 = "source-file-2.xml";
        EventConfSourceMetadataDto metadata2 = new EventConfSourceMetadataDto.Builder()
                .filename(filename2)
                .eventCount(1)
                .fileOrder(2)
                .username(username)
                .now(now)
                .vendor("vendor-2")
                .description("second entry")
                .build();

        Event event2 = new Event();
        event2.setUei("uei.opennms.org/test/clear/2");
        event2.setEventLabel("Event Two");
        event2.setDescr("Description for Event Two");
        event2.setSeverity("Warning");

        Events events2 = new Events();
        events2.getEvents().add(event2);

        eventConfPersistenceService.persistEventConfFile(events2, metadata2);

        // Retrieve initial events
        EventConfEvent triggerEvent = eventConfEventDao.findByUei("uei.opennms.org/test/trigger/1");

        long sourceId = triggerEvent.getSource().getId();

        // Disable events
        EnableDisableConfSourceEventsPayload disablePayload = new EnableDisableConfSourceEventsPayload();
        disablePayload.setEventsIds(List.of(triggerEvent.getId()));
        disablePayload.setEnable(false);

        eventConfPersistenceService.enableDisableConfSourcesEvents(sourceId, disablePayload);

        // Verify disabled state
        EventConfEvent disabledTriggerEvent = eventConfEventDao.findByUei("uei.opennms.org/test/trigger/1");

        assertFalse(disabledTriggerEvent.getEnabled());

        // Enable events
        EnableDisableConfSourceEventsPayload enablePayload = new EnableDisableConfSourceEventsPayload();
        enablePayload.setEventsIds(List.of(triggerEvent.getId()));
        enablePayload.setEnable(true);

        eventConfPersistenceService.enableDisableConfSourcesEvents(sourceId, enablePayload);

        // Verify enabled state
        EventConfEvent enabledTriggerEvent = eventConfEventDao.findByUei("uei.opennms.org/test/trigger/1");

        assertTrue(enabledTriggerEvent.getEnabled());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testaddEventConfSourceEvent() {
        String username = "test_user";
        Date now = new Date();
        String filename1 = "source-file-1.xml";
        EventConfSourceMetadataDto metadata1 = new EventConfSourceMetadataDto
                .Builder()
                .filename(filename1)
                .eventCount(1)
                .fileOrder(1)
                .username(username)
                .now(now)
                .vendor("vendor-1")
                .description("first entry")
                .build();

        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test/trigger/1");
        event1.setEventLabel("Event One");
        event1.setDescr("Description for Event One");
        event1.setSeverity("Normal");

        Events events1 = new Events();
        events1.getEvents().add(event1);

        eventConfPersistenceService.persistEventConfFile(events1, metadata1);

        EventConfSource eventConfSource = eventConfSourceDao.findByName("source-file-1.xml");
        assertNotNull("Event Source not found against name Cisco.airespace ", eventConfSource);

        String xmlEvent = """
                 <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
                   <uei>uei.opennms.org/vendor/test/test1</uei>
                   <event-label>Test1:  Adding new test  event</event-label>
                   <descr>Add new test event</descr>
                   <severity>Warning</severity>
                </event>
                """;
        Event event = JaxbUtils.unmarshal(Event.class, xmlEvent);
        eventConfPersistenceService.addEventConfSourceEvent(eventConfSource.getId(), username, event);

        EventConfEvent newlyAddedEvent = eventConfEventDao.findByUei("uei.opennms.org/vendor/test/test1");
        assertNotNull(newlyAddedEvent);
    }

    @Test
    @Transactional
    public void testUpdateEventConfEventWithXml() throws Exception {
        EventConfSource m_source = new EventConfSource();
        m_source.setName("testEventEnabledFlagTest");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(1);
        m_source.setDescription("Test event source");
        m_source.setVendor("TestVendor1");
        m_source.setUploadedBy("JUnitTest");
        m_source.setEventCount(2);
        m_source.setLastModified(new Date());

        eventConfSourceDao.saveOrUpdate(m_source);
        eventConfSourceDao.flush();

        insertEvent(m_source, "uei.opennms.org/internal/trigger", "Trigger configuration changed testing", "The Trigger configuration has been changed and should be reloaded", "Normal");

        insertEvent(m_source, "uei.opennms.org/internal/clear", "Clear discovery failed testing", "The Clear discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.", "Minor");

        EventConfSource source = eventConfSourceDao.findByName("testEventEnabledFlagTest");

        EventConfEvent triggerEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/trigger");
        EventConfEvent clearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");

        // xml payload
        EventConfEventEditRequest payload = new EventConfEventEditRequest();
        String xmlPayload = """
                <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
                   <uei>uei.opennms.org/internal/clear</uei>
                   <event-label>Clear label changed.</event-label>
                   <descr>Clear Description changed.</descr>
                   <severity>Major</severity>
                </event>
                """;
        Event event = JaxbUtils.unmarshal(Event.class, xmlPayload);
        payload.setEvent(event);
        payload.setEnabled(true);

        eventConfPersistenceService.updateEventConfEvent(source.getId(), clearEvent.getId(), payload);

        EventConfEvent updatedClearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");
        assertEquals("Clear label changed.", updatedClearEvent.getEventLabel());
        assertEquals("Clear Description changed.", updatedClearEvent.getDescription());

        // verify xml content updated or not.
        Event dbEvent = JaxbUtils.unmarshal(Event.class, updatedClearEvent.getXmlContent());
        assertEquals("Clear label changed.", dbEvent.getEventLabel());
        assertEquals("Clear Description changed.", dbEvent.getDescr());

    }

    @Test
    @Transactional
    public void testUpdateEventConfEventWithJson() throws Exception {
        EventConfSource m_source = new EventConfSource();
        m_source.setName("testEventJsonPayload");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(1);
        m_source.setDescription("Test event source");
        m_source.setVendor("TestVendor1");
        m_source.setUploadedBy("JUnitTest");
        m_source.setEventCount(2);
        m_source.setLastModified(new Date());

        eventConfSourceDao.saveOrUpdate(m_source);
        eventConfSourceDao.flush();

        insertEvent(m_source, "uei.opennms.org/internal/trigger", "Trigger configuration changed testing", "The Trigger configuration has been changed and should be reloaded", "Normal");

        insertEvent(m_source, "uei.opennms.org/internal/clear", "Clear discovery failed testing", "The Clear discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.", "Minor");

        EventConfSource source = eventConfSourceDao.findByName("testEventJsonPayload");

        EventConfEvent clearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");

        String jsonPayload = """
                {
                  "enabled": true,
                  "event": {
                    "uei": "uei.opennms.org/internal/clear",
                    "eventLabel": "Clear label changed.",
                    "descr": "Clear Description changed.",
                    "severity": "Major"
                  }
                }
                """;
        ObjectMapper mapper = new ObjectMapper();
        EventConfEventEditRequest payload = mapper.readValue(jsonPayload, EventConfEventEditRequest.class);

        eventConfPersistenceService.updateEventConfEvent(source.getId(),clearEvent.getId(), payload);

        EventConfEvent updatedClearEvent = eventConfEventDao.findByUei("uei.opennms.org/internal/clear");
        assertEquals("Clear label changed.", updatedClearEvent.getEventLabel());
        assertEquals("Clear Description changed.", updatedClearEvent.getDescription());

        // verify xml content updated correctly
        Event dbEvent = JaxbUtils.unmarshal(Event.class, updatedClearEvent.getXmlContent());
        assertEquals("Clear label changed.", dbEvent.getEventLabel());
        assertEquals("Clear Description changed.", dbEvent.getDescr());
    }

    private void insertEvent(EventConfSource m_source,String uei, String label, String description, String severity) {
        EventConfEvent event = new EventConfEvent();
        event.setUei(uei);
        event.setEventLabel(label);
        event.setDescription(description);
        event.setXmlContent("<event><uei>" + uei + "</uei></event>");
        event.setSource(m_source);
        event.setEnabled(true);
        event.setCreatedTime(new Date());
        event.setLastModified(new Date());
        event.setModifiedBy("JUnitTest");

        eventConfEventDao.saveOrUpdate(event);
    }
}

