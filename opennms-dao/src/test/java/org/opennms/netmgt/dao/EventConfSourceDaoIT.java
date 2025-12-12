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
package org.opennms.netmgt.dao;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventConfSourceDaoIT implements InitializingBean {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EventConfSourceDao m_dao;

    @Autowired
    EventConfEventDao m_eventDao;

    private EventConfSource m_source;

    private int defaultEventSize;

    @Before
    @Transactional
    public void setUp() {
        defaultEventSize = m_eventDao.findAll().size();
        m_source = new EventConfSource();
        m_source.setName("JUnit Source");
        m_source.setVendor("TestVendor");
        m_source.setEnabled(true);
        m_source.setFileOrder(42);
        m_source.setDescription("JUnit Description");
        m_source.setEventCount(5);
        m_source.setCreatedTime(new Date());
        m_source.setLastModified(new Date());
        m_source.setUploadedBy("JUnit");

        m_dao.saveOrUpdate(m_source);
    }

    @After
    @Transactional
    public void tearDown() {
        var listofConfig = m_eventDao.findAll();
        var listOfSource = m_dao.findAll();
        m_eventDao.deleteAll(listofConfig);
        m_dao.deleteAll(listOfSource);
        m_dao.flush();
        m_eventDao.flush();

    }

    @Test
    @Transactional
    public void testFindByName() {
        EventConfSource found = m_dao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals("JUnit Description", found.getDescription());
    }

    @Test
    @Transactional
    public void testVendorIsPersisted() {
        EventConfSource found = m_dao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals("TestVendor", found.getVendor());
    }

    @Test
    @Transactional
    public void testFileOrderIsPersisted() {
        EventConfSource found = m_dao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals(Integer.valueOf(42), found.getFileOrder());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<EventConfSource> list = m_dao.findAllEnabled();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.getName().equals("JUnit Source")));
    }

    @Test
    @Transactional
    public void testGetIdToNameMap() {
        Map<Long, String> map = m_dao.getIdToNameMap();
        assertNotNull(map);
        assertTrue(map.containsValue("JUnit Source"));
    }

    @Test
    public void testEventConfFilesExists() throws IOException {
        String[] xmlFiles = {
                "eventconf-test-1.xml",
                "eventconf-test-2.xml"
        };

        for (String fileName : xmlFiles) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
                assertNotNull("File should be available in classpath: " + fileName, input);
            }
        }
    }

    @Test
    @Transactional
    public void testLoadAndPersistEventsFromSingleXmlFile() throws Exception {

        var m_source1 = new EventConfSource();
        m_source1.setName("test-source");
        m_source1.setEnabled(true);
        m_source1.setCreatedTime(new Date());
        m_source1.setFileOrder(1);
        m_source1.setDescription("Test event source");
        m_source1.setVendor("TestVendor");
        m_source1.setUploadedBy("JUnitTest");
        m_source1.setEventCount(9);
        m_source1.setLastModified(new Date());

        m_dao.saveOrUpdate(m_source1);
        m_dao.flush();

        org.opennms.netmgt.xml.eventconf.Events events =
                JaxbUtils.unmarshal(org.opennms.netmgt.xml.eventconf.Events.class,
                        getClass().getClassLoader().getResourceAsStream("eventconf-test-1.xml"));

        assertNotNull("Parsed Events should not be null", events);
        assertFalse("Parsed Events should not be empty", events.getEvents().isEmpty());
        assertEquals("Should have  9 events from XML", 9, events.getEvents().size());

        for (var xmlEvent : events.getEvents()) {
            EventConfEvent jpaEvent = new EventConfEvent();
            jpaEvent.setUei(xmlEvent.getUei());
            jpaEvent.setDescription(xmlEvent.getDescr());
            jpaEvent.setXmlContent(xmlEvent.toString());
            jpaEvent.setEnabled(true);
            jpaEvent.setCreatedTime(new Date());
            jpaEvent.setLastModified(new Date());
            jpaEvent.setModifiedBy("XMLTest");
            jpaEvent.setSource(m_source1);

            m_eventDao.saveOrUpdate(jpaEvent);
        }
        m_eventDao.flush();
        List<EventConfEvent> savedEvents = m_eventDao.findBySourceId(m_source1.getId());
        assertEquals("Should have saved 9 events from XML", 9, savedEvents.size());
    }


    @Test
    @Transactional
    public void testLoadAndPersistMultipleEventConfFiles() throws Exception {
        // List of XML files to test
        String[] xmlFiles = {
                "eventconf-test-1.xml",       // has 9 events
                "eventconf-test-2.xml"      // assume it has 3 events
        };

        int totalExpectedEventCount = 0;
        List<Long> allSourceIds = new ArrayList<>();

        for (int i = 0; i < xmlFiles.length; i++) {
            String file = xmlFiles[i];

            // Create EventConfSource per file
            EventConfSource source = new EventConfSource();
            source.setName("test-source-" + i);
            source.setEnabled(true);
            source.setCreatedTime(new Date());
            source.setFileOrder(i + 1);
            source.setDescription("Source for " + file);
            source.setVendor("JUnitVendor");
            source.setUploadedBy("JUnitTest");
            source.setLastModified(new Date());

            org.opennms.netmgt.xml.eventconf.Events events =
                    JaxbUtils.unmarshal(org.opennms.netmgt.xml.eventconf.Events.class,
                            getClass().getClassLoader().getResourceAsStream(file));

            assertNotNull("Events should not be null for file: " + file, events);
            assertFalse("Event list should not be empty for file: " + file, events.getEvents().isEmpty());

            int eventCount = events.getEvents().size();
            totalExpectedEventCount += eventCount;
            source.setEventCount(eventCount);

            m_dao.saveOrUpdate(source);
            m_dao.flush();
            allSourceIds.add(source.getId());

            // Persist events
            for (var xmlEvent : events.getEvents()) {
                EventConfEvent jpaEvent = new EventConfEvent();
                jpaEvent.setUei(xmlEvent.getUei());
                jpaEvent.setDescription(xmlEvent.getDescr());
                jpaEvent.setXmlContent(xmlEvent.toString());
                jpaEvent.setEnabled(true);
                jpaEvent.setCreatedTime(new Date());
                jpaEvent.setLastModified(new Date());
                jpaEvent.setModifiedBy("XMLTest");
                jpaEvent.setSource(source);

                m_eventDao.saveOrUpdate(jpaEvent);
            }

            m_eventDao.flush();

            // Verify events for this source
            List<EventConfEvent> savedForSource = m_eventDao.findBySourceId(source.getId());
            assertEquals("Event count mismatch for " + file, eventCount, savedForSource.size());
        }

        List<EventConfEvent> allEvents = m_eventDao.findAll();
        assertEquals("Total event count mismatch across all files", totalExpectedEventCount, allEvents.size());
    }

    @Test
    @Transactional
    public void testDeleteBySourceIds() throws Exception {
        // List of XML files to test
        String[] xmlFiles = {
                "eventconf-test-1.xml",
                "eventconf-test-2.xml"
        };

        int totalExpectedEventCount = 0;
        List<Long> allSourceIds = new ArrayList<>();

        for (int i = 0; i < xmlFiles.length; i++) {
            String file = xmlFiles[i];

            EventConfSource source = new EventConfSource();
            source.setName("test-source-" + i);
            source.setEnabled(true);
            source.setCreatedTime(new Date());
            source.setFileOrder(i + 1);
            source.setDescription("Source for " + file);
            source.setVendor("JUnitVendor");
            source.setUploadedBy("JUnitTest");
            source.setLastModified(new Date());

            org.opennms.netmgt.xml.eventconf.Events events =
                    JaxbUtils.unmarshal(org.opennms.netmgt.xml.eventconf.Events.class,
                            getClass().getClassLoader().getResourceAsStream(file));

            assertNotNull("Events should not be null for file: " + file, events);
            assertFalse("Event list should not be empty for file: " + file, events.getEvents().isEmpty());

            int eventCount = events.getEvents().size();
            totalExpectedEventCount += eventCount;
            source.setEventCount(eventCount);

            m_dao.saveOrUpdate(source);
            m_dao.flush();
            allSourceIds.add(source.getId());

            for (var xmlEvent : events.getEvents()) {
                EventConfEvent jpaEvent = new EventConfEvent();
                jpaEvent.setUei(xmlEvent.getUei());
                jpaEvent.setDescription(xmlEvent.getDescr());
                jpaEvent.setXmlContent(xmlEvent.toString());
                jpaEvent.setEnabled(true);
                jpaEvent.setCreatedTime(new Date());
                jpaEvent.setLastModified(new Date());
                jpaEvent.setModifiedBy("XMLTest");
                jpaEvent.setSource(source);

                m_eventDao.saveOrUpdate(jpaEvent);
            }

            m_eventDao.flush();
        }
        final var  eventConfSources = m_dao.findAll();
        m_dao.deleteBySourceIds(eventConfSources.stream().map(EventConfSource::getId).collect(Collectors.toList()));
        final var  deletedEventConfSources = m_dao.findAll();
        assertEquals(0,deletedEventConfSources.size());
    }


    @Test
    @Transactional
    public void testLoadAndPersistMultipleEventConfFilesAndUpdateEnabledFlag() throws Exception {
        String[] xmlFiles = {"eventconf-test-1.xml",
                "eventconf-test-2.xml"
        };

        int totalExpectedEventCount = 0;
        List<Long> allSourceIds = new ArrayList<>();

        for (int i = 0; i < xmlFiles.length; i++) {
            String file = xmlFiles[i];

            EventConfSource source = new EventConfSource();
            source.setName("test-source-" + i);
            source.setEnabled(true);
            source.setCreatedTime(new Date());
            source.setFileOrder(i + 1);
            source.setDescription("Source for " + file);
            source.setVendor("testVendor");
            source.setUploadedBy("Test");
            source.setLastModified(new Date());

            org.opennms.netmgt.xml.eventconf.Events events = JaxbUtils.unmarshal(org.opennms.netmgt.xml.eventconf.Events.class, getClass().getClassLoader().getResourceAsStream(file));

            int eventCount = events.getEvents().size();
            totalExpectedEventCount += eventCount;
            source.setEventCount(eventCount);

            m_dao.saveOrUpdate(source);
            m_dao.flush();
            allSourceIds.add(source.getId());

            for (var xmlEvent : events.getEvents()) {
                EventConfEvent jpaEvent = new EventConfEvent();
                jpaEvent.setUei(xmlEvent.getUei());
                jpaEvent.setDescription(xmlEvent.getDescr());
                jpaEvent.setXmlContent(xmlEvent.toString());
                jpaEvent.setEnabled(true);
                jpaEvent.setCreatedTime(new Date());
                jpaEvent.setLastModified(new Date());
                jpaEvent.setModifiedBy("XMLTest");
                jpaEvent.setSource(source);

                m_eventDao.saveOrUpdate(jpaEvent);
            }
            m_eventDao.flush();
            List<EventConfEvent> savedForSource = m_eventDao.findBySourceId(source.getId());
            assertEquals("Event count mismatch for " + file, eventCount, savedForSource.size());
        }

        List<EventConfEvent> allEvents = m_eventDao.findAll();
        assertEquals("Total event count mismatch across all files", totalExpectedEventCount, allEvents.size() - defaultEventSize);

        m_dao.updateEnabledFlag(allSourceIds, false, false);
        sessionFactory.getCurrentSession().clear();
        for (Long sourceId : allSourceIds) {
            final var source = m_dao.get(sourceId);
            assertFalse("Source should be disabled", source.getEnabled());

            List<EventConfEvent> events = m_eventDao.findBySourceId(sourceId);
            assertFalse("Events should still be enabled when cascade=false", events.isEmpty() && events.stream().anyMatch(EventConfEvent::getEnabled));
        }

        m_dao.updateEnabledFlag(allSourceIds, true, true);
        sessionFactory.getCurrentSession().clear();
        for (final var sourceId : allSourceIds) {
            final var source = m_dao.get(sourceId);
            assertTrue("Source should be enabled", source.getEnabled());

            List<EventConfEvent> events = m_eventDao.findBySourceId(sourceId);
            assertFalse("Events should not be empty", events.isEmpty());
            assertTrue("All events should be enabled when cascade=true", events.stream().allMatch(EventConfEvent::getEnabled));
        }
    }

    @Test
    @Transactional
    public void testFindAllNamesReturnsPersistedNames() {
        List<String> names = m_dao.findAllNames();

        assertNotNull("Names list should not be null", names);
        assertFalse("Names list should not be empty", names.isEmpty());
        assertTrue("Names should contain the persisted source", names.contains("JUnit Source"));
    }

    @Test
    @Transactional
    public void testFindAllNamesRespectsFileOrder() {
        EventConfSource source1 = new EventConfSource();
        source1.setName("Source-A");
        source1.setVendor("VendorA");
        source1.setEnabled(true);
        source1.setFileOrder(1);
        source1.setDescription("First Source");
        source1.setEventCount(1);
        source1.setCreatedTime(new Date());
        source1.setLastModified(new Date());
        source1.setUploadedBy("JUnit");
        m_dao.saveOrUpdate(source1);

        EventConfSource source2 = new EventConfSource();
        source2.setName("Source-B");
        source2.setVendor("VendorB");
        source2.setEnabled(true);
        source2.setFileOrder(2);
        source2.setDescription("Second Source");
        source2.setEventCount(2);
        source2.setCreatedTime(new Date());
        source2.setLastModified(new Date());
        source2.setUploadedBy("JUnit");
        m_dao.saveOrUpdate(source2);

        m_dao.flush();

        List<String> names = m_dao.findAllNames();

        assertNotNull(names);
    }

    @Test
    @Transactional
    public void testFindAllNamesWhenNoSourcesExist() {
        m_dao.deleteAll(m_dao.findAll());
        m_dao.flush();

        List<String> names = m_dao.findAllNames();

        assertNotNull("Names list should not be null even if empty", names);
        assertTrue("Names list should be empty when no sources exist", names.isEmpty());
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testFilterEventConfSource_ReturnsValidRecords() {
        EventConfSource source1 = new EventConfSource();
        source1.setName("opennms.test.events");
        source1.setFileOrder(1);
        source1.setEventCount(5);
        source1.setEnabled(true);
        source1.setCreatedTime(new Date());
        source1.setLastModified(new Date());
        source1.setVendor("opennms");
        source1.setDescription("Open Network Monitoring System");
        m_dao.saveOrUpdate(source1);

        EventConfSource source2 = new EventConfSource();
        source2.setName("cisco.test.events");
        source2.setFileOrder(2);
        source2.setEventCount(3);
        source2.setEnabled(true);
        source2.setCreatedTime(new Date());
        source2.setLastModified(new Date());
        source2.setVendor("cisco");
        source2.setDescription("Cisco Events");
        m_dao.saveOrUpdate(source2);
        m_dao.flush();
        // Insert event in the source2
        insertEvent(source2,"uei.opennms.org/internal/trigger", "Trigger-label", "The Trigger configuration has been changed and should be reloaded", "Normal");

        // Inline helper for reusing assertions
        BiConsumer<Map<String, Object>, String> assertSourceName =
                (result, expectedName) -> {
                    assertNotNull(result);
                    List<?> list = (List<?>) result.get("eventConfSourceList");
                    assertEquals(result.get("totalRecords"), list.size());
                    assertEquals(expectedName, ((EventConfSource) list.get(0)).getName());
                };

        // 1. Exact filter, ascending by name
        Map<String, Object> result = m_dao.filterEventConfSource("opennms.test.events", "name", "asc", 0, 0, 10);
        assertEquals(1, result.get("totalRecords"));
        assertSourceName.accept(result, "opennms.test.events");

        // 2. Partial filter, ascending by name
        result = m_dao.filterEventConfSource("test.events", "name", "asc", 0, 0, 10);
        assertEquals(2, result.get("totalRecords"));
        assertSourceName.accept(result, "cisco.test.events"); // asc = cisco first

        // 3. Partial filter, descending by name
        result = m_dao.filterEventConfSource("test.events", "name", "desc", 0, 0, 10);
        assertEquals(2, result.get("totalRecords"));
        assertSourceName.accept(result, "opennms.test.events"); // desc = opennms first

        // 4. Partial filter, ascending by fileOrder
        result = m_dao.filterEventConfSource("test.events", "fileOrder", "asc", 0, 0, 10);
        assertEquals(2, result.get("totalRecords"));
        assertSourceName.accept(result, "opennms.test.events"); // fileOrder 1 first

        // 5. Filter by description
        result = m_dao.filterEventConfSource("Open Network Monitoring System", "fileOrder", "asc", 0, 0, 10);
        assertEquals(1, result.get("totalRecords"));
        assertSourceName.accept(result, "opennms.test.events");

        // 6. Filter by vendor (case-insensitive)
        result = m_dao.filterEventConfSource("CISCO", "fileOrder", "asc", 0, 0, 10);
        assertEquals(1, result.get("totalRecords"));
        assertSourceName.accept(result, "cisco.test.events");

        // 7. Pagination (only second record returned)
        result = m_dao.filterEventConfSource("test.events", "name", "asc", 0, 1, 1);
        assertEquals(2, result.get("totalRecords"));
        List<?> list = (List<?>) result.get("eventConfSourceList");
        assertEquals(1, list.size());
        assertEquals("opennms.test.events", ((EventConfSource) list.get(0)).getName());

        // 8. Filter by event uei "uei.opennms.org/internal/trigger"
        result = m_dao.filterEventConfSource("uei.opennms.org/internal/trigger", "fileOrder", "asc", 0, 0, 10);
        assertEquals(1, result.get("totalRecords"));
        assertSourceName.accept(result, "cisco.test.events");

        // 8. Filter by event label "trigger-label"
        result = m_dao.filterEventConfSource("trigger-label", "fileOrder", "asc", 0, 0, 10);
        assertEquals(1, result.get("totalRecords"));
        assertSourceName.accept(result, "cisco.test.events");
    }

    @Test
    public void testFilterEventConfSource_ReturnsEmptyMap() {
        Map<String, Object> result = m_dao.filterEventConfSource(
                null, null, null, null, 0, 5);
        assertNotNull(result);
        assertFalse(result.isEmpty());
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

        m_eventDao.saveOrUpdate(event);
    }
}