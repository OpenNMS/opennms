package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
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

    private Resource getResourceForRelativePath(String resourceSuffix) {
        return new ClassPathResource("/org/opennms/netmgt/config/eventd/" + resourceSuffix);
    }
}
