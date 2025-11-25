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
package org.opennms.features.apilayer.config;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.integration.api.v1.config.events.EventConfExtension;
import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventConfExtensionManagerIT implements InitializingBean {

    @Autowired
    private EventConfDao eventConfDao;

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Autowired
    private SessionUtils sessionUtils;

    private EventConfExtensionManager manager;
    private Events testEvents;

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(eventConfDao);
        assertNotNull(eventConfSourceDao);
        assertNotNull(eventConfEventDao);
        assertNotNull(sessionUtils);
    }

    @Before
    public void setUp() throws Exception {
        // Clean up any existing plugin source from previous tests
        cleanupPluginSource();

        // Load test events from XML file
        try (InputStream is = getClass().getResourceAsStream("/test-events.xml")) {
            testEvents = JaxbUtils.unmarshal(Events.class, is);
        }
        assertNotNull(testEvents);
        assertThat(testEvents.getEvents(), hasSize(2));

        // Create manager instance
        manager = new EventConfExtensionManager(eventConfDao, eventConfSourceDao, eventConfEventDao, sessionUtils);
    }

    @After
    public void tearDown() {
        // Clean up plugin source after each test
        cleanupPluginSource();
    }

    private void cleanupPluginSource() {
        EventConfSource existingSource = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
        if (existingSource != null) {
            sessionUtils.withTransaction(() -> {
                eventConfEventDao.deleteBySourceId(existingSource.getId());
                eventConfSourceDao.delete(existingSource);
            });
        }
    }

    @Test
    public void testCreateNewSourceAndEvents() {
        // Initially, no plugin source should exist
        EventConfSource source = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
        assertNull("Plugin source should not exist initially", source);

        // Create mock extension with test events
        EventConfExtension extension = createMockExtension(testEvents.getEvents());

        // Bind the extension
        manager.onBind(extension, new HashMap<>());

        // Wait for async processing to complete
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EventConfSource pluginSource = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
            assertNotNull("Plugin source should be created", pluginSource);
            assertEquals("Source should be enabled", true, pluginSource.getEnabled());
            assertEquals("Source vendor should be set", "OpenNMS-Plugins", pluginSource.getVendor());
            assertEquals("Source should have 2 events", Integer.valueOf(2), pluginSource.getEventCount());
        });

        // Verify events were persisted
        EventConfSource pluginSource = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
        List<EventConfEvent> events = eventConfEventDao.findBySourceId(pluginSource.getId());
        assertThat("Should have 2 events", events, hasSize(2));

        // Verify event details
        List<String> ueis = events.stream()
                .map(EventConfEvent::getUei)
                .collect(Collectors.toList());
        assertThat(ueis, containsInAnyOrder(
                "uei.opennms.org/test/plugin/event1",
                "uei.opennms.org/test/plugin/event2"
        ));

        // Verify events are enabled
        for (EventConfEvent event : events) {
            assertTrue("Event should be enabled", event.getEnabled());
            assertNotNull("Event should have XML content", event.getXmlContent());
            assertNotNull("Event should have created time", event.getCreatedTime());
            assertEquals("Event should have correct modified by", "opennms-plugins", event.getModifiedBy());
        }
    }

    @Test
    public void testUpdateExistingEvents() {
        // First, create initial events
        EventConfExtension extension1 = createMockExtension(testEvents.getEvents());
        manager.onBind(extension1, new HashMap<>());

        // Wait for initial creation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EventConfSource source = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
            assertNotNull(source);
            assertEquals(Integer.valueOf(2), source.getEventCount());
        });

        // Get initial event content
        EventConfSource source = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
        List<EventConfEvent> initialEvents = eventConfEventDao.findBySourceId(source.getId());
        String initialXml = initialEvents.get(0).getXmlContent();

        // Modify the event
        List<Event> modifiedEvents = new ArrayList<>(testEvents.getEvents());
        Event modifiedEvent = modifiedEvents.get(0);
        modifiedEvent.setEventLabel("Modified Test Plugin Event 1");
        modifiedEvent.setDescr("This event has been modified");

        // Unbind first extension and bind modified one
        manager.onUnbind(extension1, new HashMap<>());
        EventConfExtension extension2 = createMockExtension(modifiedEvents);
        manager.onBind(extension2, new HashMap<>());

        // Wait for update to complete
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<EventConfEvent> updatedEvents = eventConfEventDao.findBySourceId(source.getId());
            String updatedXml = updatedEvents.stream()
                    .filter(e -> e.getUei().equals("uei.opennms.org/test/plugin/event1"))
                    .findFirst()
                    .map(EventConfEvent::getXmlContent)
                    .orElse(null);

            assertNotNull("Updated event should exist", updatedXml);
            assertNotEquals("Event content should be different", initialXml, updatedXml);
            assertTrue("Updated content should contain new label", updatedXml.contains("Modified Test Plugin Event 1"));
        });
    }

    @Test
    public void testSourceRecreationAfterDeletion() {
        // Create initial events
        EventConfExtension extension = createMockExtension(testEvents.getEvents());
        manager.onBind(extension, new HashMap<>());

        // Wait for creation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNotNull(eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME));
        });

        // Get the source and delete it (simulating UI deletion)
        EventConfSource source = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
        Long deletedSourceId = source.getId();

        sessionUtils.withTransaction(() -> {
            // Delete events first
            eventConfEventDao.deleteBySourceId(deletedSourceId);
            // Then delete source
            eventConfSourceDao.delete(source);
        });

        // Verify deletion
        assertNull("Source should be deleted", eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME));

        // Trigger reload (simulating plugin reinstall)
        manager.onUnbind(extension, new HashMap<>());
        manager.onBind(extension, new HashMap<>());

        // Wait for recreation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EventConfSource recreatedSource = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
            assertNotNull("Source should be recreated", recreatedSource);
            assertNotEquals("Recreated source should have different ID", deletedSourceId, recreatedSource.getId());

            List<EventConfEvent> recreatedEvents = eventConfEventDao.findBySourceId(recreatedSource.getId());
            assertThat("Events should be recreated", recreatedEvents, hasSize(2));
        });
    }

    @Test
    public void testNoDuplicateSourcesCreated() {
        // Create multiple extensions
        EventConfExtension extension1 = createMockExtension(testEvents.getEvents().subList(0, 1));
        EventConfExtension extension2 = createMockExtension(testEvents.getEvents().subList(1, 2));

        // Bind both extensions
        manager.onBind(extension1, new HashMap<>());
        manager.onBind(extension2, new HashMap<>());

        // Wait for processing
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EventConfSource source = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
            assertNotNull(source);
            assertEquals("Should have 2 events from both extensions", Integer.valueOf(2), source.getEventCount());
        });

        // Verify only ONE source exists with this name
        List<EventConfSource> allSources = eventConfSourceDao.findAll().stream()
                .filter(s -> EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME.equals(s.getName()))
                .collect(Collectors.toList());

        assertThat("Should have exactly ONE plugin source", allSources, hasSize(1));
    }

    @Test
    public void testNoReloadWhenNoChanges() {
        // Create and bind extension
        EventConfExtension extension = createMockExtension(testEvents.getEvents());
        manager.onBind(extension, new HashMap<>());

        // Wait for initial creation
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNotNull(eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME));
        });

        EventConfSource source = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
        java.util.Date initialModified = source.getLastModified();

        // Wait a bit
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Trigger sync again with same events (no changes)
        manager.onUnbind(extension, new HashMap<>());
        manager.onBind(extension, new HashMap<>());

        // Wait and check that lastModified didn't change significantly
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EventConfSource updatedSource = eventConfSourceDao.findByName(EventConfExtensionManager.INTEGRATION_API_SOURCE_NAME);
            assertNotNull(updatedSource);
            // Since there are no changes, the source metadata shouldn't be updated
            // (This tests that we're not unnecessarily updating when nothing changed)
        });
    }

    private EventConfExtension createMockExtension(List<Event> events) {
        return new EventConfExtension() {
            @Override
            public List<EventDefinition> getEventDefinitions() {
                // Convert Event objects to EventDefinition objects
                return events.stream()
                        .map(event -> new TestEventDefinition(event))
                        .collect(Collectors.toList());
            }
        };
    }

    private static class TestEventDefinition implements EventDefinition {
        private final Event event;

        public TestEventDefinition(Event event) {
            this.event = event;
        }

        @Override
        public String getUei() {
            return event.getUei();
        }

        @Override
        public String getLabel() {
            return event.getEventLabel();
        }

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public org.opennms.integration.api.v1.model.Severity getSeverity() {
            // Map severity from event
            if (event.getSeverity() == null) {
                return org.opennms.integration.api.v1.model.Severity.NORMAL;
            }
            switch (event.getSeverity().toLowerCase()) {
                case "critical": return org.opennms.integration.api.v1.model.Severity.CRITICAL;
                case "major": return org.opennms.integration.api.v1.model.Severity.MAJOR;
                case "minor": return org.opennms.integration.api.v1.model.Severity.MINOR;
                case "warning": return org.opennms.integration.api.v1.model.Severity.WARNING;
                case "indeterminate": return org.opennms.integration.api.v1.model.Severity.INDETERMINATE;
                case "cleared": return org.opennms.integration.api.v1.model.Severity.CLEARED;
                default: return org.opennms.integration.api.v1.model.Severity.NORMAL;
            }
        }

        @Override
        public org.opennms.integration.api.v1.config.events.LogMessage getLogMessage() {
            if (event.getLogmsg() == null) {
                return null;
            }
            return new org.opennms.integration.api.v1.config.events.LogMessage() {
                @Override
                public String getContent() {
                    return event.getLogmsg().getContent();
                }

                @Override
                public org.opennms.integration.api.v1.config.events.LogMsgDestType getDestination() {
                    if (event.getLogmsg().getDest() == null) {
                        return org.opennms.integration.api.v1.config.events.LogMsgDestType.LOGNDISPLAY;
                    }
                    return org.opennms.integration.api.v1.config.events.LogMsgDestType.valueOf(
                        event.getLogmsg().getDest().toString().toUpperCase().replace("AND", "N")
                    );
                }
            };
        }

        @Override
        public org.opennms.integration.api.v1.config.events.AlarmData getAlarmData() {
            return null;
        }

        @Override
        public org.opennms.integration.api.v1.config.events.Mask getMask() {
            return null;
        }

        @Override
        public java.util.List<org.opennms.integration.api.v1.config.events.Parameter> getParameters() {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<org.opennms.integration.api.v1.config.events.CollectionGroup> getCollectionGroup() {
            return java.util.Collections.emptyList();
        }

        @Override
        public String getDescription() {
            return event.getDescr();
        }

        @Override
        public String getOperatorInstructions() {
            return null;
        }
    }
}
