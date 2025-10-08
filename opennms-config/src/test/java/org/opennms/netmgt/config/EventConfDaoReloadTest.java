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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataRetrievalFailureException;

public class EventConfDaoReloadTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(false);
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Verify that the reload operation does not throw any errors
     * when reloading event files in the class-path.
     */
    @Test
    public void canReloadEventsInClasspath() throws IOException {
        // Load
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(3, eventConfDao.getAllEvents().size());
    }

    /**
     * Verify that the reload operation does not throw any errors
     * when reloading event files on disk.
     */
    @Test
    public void canReloadEventsOnDisk() throws IOException {
        // Copy the resources to the file system
        File eventconfXml = tempFolder.newFile("eventconf.xml");
        File bgp4eventsXml = tempFolder.newFile("BGP4.events.xml");

        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/eventconf.xml").getInputStream(),
                eventconfXml);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/BGP4.events.xml").getInputStream(),
                bgp4eventsXml);

        // Load
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(3, eventConfDao.getAllEvents().size());
    }

    /**
     * Verify that removed event files and modified event files
     * are handled properly on reload.
     *
     * A delay is added when replacing files in order to make
     * sure that their lastModifiedTime is different, which
     * is used to determine whether or not a file should be
     * reloaded.
     */
    @Test
    public void canReloadEvents() throws Exception {
        // Copy the resources to the file system
        File eventconfXml = tempFolder.newFile("eventconf.xml");
        File bgp4eventsXml = tempFolder.newFile("BGP4.events.xml");

        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/eventconf.xml").getInputStream(),
                eventconfXml);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/BGP4.events.xml").getInputStream(),
                bgp4eventsXml);

        // Load
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Replace the eventconf.xml with one that doesn't reference any files
        Thread.sleep(1000);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/eventconf-nofiles.xml").getInputStream(),
                eventconfXml);

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(1, eventConfDao.getAllEvents().size());

        // Put the original eventconf.xml back
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/eventconf.xml").getInputStream(),
                eventconfXml);

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Replace the BGP4.events.xml with another that has a few more events
        Thread.sleep(1000);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/BGP4.more.events.xml").getInputStream(),
                bgp4eventsXml);

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(5, eventConfDao.getAllEvents().size());
    }

    /**
     * Verify that the order of the includes is maintained
     * when new event configuration files are added, and reloaded.
     */
    @Test
    public void canMaintainOrderOnReload() throws Exception {
        // Copy the resources to the file system
        File eventconfXml = copyEventConfig("order/eventconf.empty.xml", "eventconf.xml");

        // Load
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(0, eventConfDao.getAllEvents().size());

        EventBuilder eb = new EventBuilder("uei.opennms.org/test/order", "JUnit");
        Event event = eventConfDao.findByEvent(eb.getEvent());
        assertNull("no event should match", event);

        // Replace the eventconf.xml with one that references 1
        Thread.sleep(1000);
        copyEventConfig("order/eventconf.1.xml", "eventconf.xml");
        copyEventConfig("order/1.events.xml", "1.events.xml");

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(1, eventConfDao.getAllEvents().size());

        event = eventConfDao.findByEvent(eb.getEvent());
        assertEquals("Critical", event.getSeverity());

        // Replace the eventconf.xml with one that references 2 and then 1
        Thread.sleep(1000);
        copyEventConfig("order/eventconf.21.xml", "eventconf.xml");
        copyEventConfig("order/2.events.xml", "2.events.xml");

        // Reload
        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(eventconfXml));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        assertEquals(2, eventConfDao.getAllEvents().size());

        event = eventConfDao.findByEvent(eb.getEvent());
        assertEquals("Major", event.getSeverity());
    }

    private File copyEventConfig(String from, String to) throws IOException {
        final File dest = new File(tempFolder.getRoot(), to);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath(from).getInputStream(), dest);
        return dest;
    }

    private Resource getResourceForRelativePath(String resourceSuffix) {
        return new ClassPathResource("/org/opennms/netmgt/config/eventd/" + resourceSuffix);
    }

    @Test
    public void NMS15289_working() throws IOException {
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        assertEquals(3, eventConfDao.getAllEvents().size());

        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("NMS-15289/working-eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        // reload should work
        assertEquals(1, eventConfDao.getAllEvents().size());
    }

    @Test(expected = DataRetrievalFailureException.class)
    public void NMS15289_notFound() throws IOException {
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        assertEquals(3, eventConfDao.getAllEvents().size());

        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("NMS-15289/broken0-eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        // reload should be skipped
        assertEquals(3, eventConfDao.getAllEvents().size());
    }

    @Test(expected = DataRetrievalFailureException.class)
    public void NMS15289_brokenRoot() throws IOException {
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        assertEquals(3, eventConfDao.getAllEvents().size());

        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("NMS-15289/broken1-eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        // reload should be skipped
        assertEquals(3, eventConfDao.getAllEvents().size());
    }

    @Test(expected = DataRetrievalFailureException.class)
    public void NMS15289_brokenChild() throws IOException {
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        assertEquals(3, eventConfDao.getAllEvents().size());

        eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(getResourceForRelativePath("NMS-15289/broken2-eventconf.xml"));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        // reload should be skipped
        assertEquals(3, eventConfDao.getAllEvents().size());
    }
}
