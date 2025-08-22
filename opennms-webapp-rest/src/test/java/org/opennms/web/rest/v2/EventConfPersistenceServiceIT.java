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
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
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
        assertTrue(persistedEvent.getEnabled());
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

}
