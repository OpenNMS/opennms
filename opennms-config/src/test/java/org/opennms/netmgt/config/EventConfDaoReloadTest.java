/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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
        eventConfDao.setConfigResource(getResourceForRelativePath("reloaded/eventconf.xml"));
        eventConfDao.afterPropertiesSet();
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Reload
        eventConfDao.reload();
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
        eventConfDao.setConfigResource(new FileSystemResource(eventconfXml));
        eventConfDao.afterPropertiesSet();
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Reload
        eventConfDao.reload();
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
        eventConfDao.setConfigResource(new FileSystemResource(eventconfXml));
        eventConfDao.afterPropertiesSet();
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Replace the eventconf.xml with one that doesn't reference any files
        Thread.sleep(1000);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/eventconf-nofiles.xml").getInputStream(),
                eventconfXml);

        // Reload
        eventConfDao.reload();
        assertEquals(1, eventConfDao.getAllEvents().size());

        // Put the original eventconf.xml back
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/eventconf.xml").getInputStream(),
                eventconfXml);

        // Reload
        eventConfDao.reload();
        assertEquals(3, eventConfDao.getAllEvents().size());

        // Replace the BGP4.events.xml with another that has a few more events
        Thread.sleep(1000);
        FileUtils.copyInputStreamToFile(getResourceForRelativePath("reloaded/BGP4.more.events.xml").getInputStream(),
                bgp4eventsXml);

        // Reload
        eventConfDao.reload();
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
        eventConfDao.setConfigResource(new FileSystemResource(eventconfXml));
        eventConfDao.afterPropertiesSet();
        assertEquals(0, eventConfDao.getAllEvents().size());

        EventBuilder eb = new EventBuilder("uei.opennms.org/test/order", "JUnit");
        Event event = eventConfDao.findByEvent(eb.getEvent());
        assertNull("no event should match", event);

        // Replace the eventconf.xml with one that references 1
        Thread.sleep(1000);
        copyEventConfig("order/eventconf.1.xml", "eventconf.xml");
        copyEventConfig("order/1.events.xml", "1.events.xml");

        // Reload
        eventConfDao.reload();
        assertEquals(1, eventConfDao.getAllEvents().size());

        event = eventConfDao.findByEvent(eb.getEvent());
        assertEquals("Critical", event.getSeverity());

        // Replace the eventconf.xml with one that references 2 and then 1
        Thread.sleep(1000);
        copyEventConfig("order/eventconf.21.xml", "eventconf.xml");
        copyEventConfig("order/2.events.xml", "2.events.xml");

        // Reload
        eventConfDao.reload();
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
}
