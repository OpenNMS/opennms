//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 15: Work with updated dependency injected and Resource-based DAO. - dj@opennms.org
// 2008 Jan 08: Don't keep references to EventconfFactory around (since it
//              returns EventConfDao, now)--use call EventconfFactory.getInstance()
//              every time we need it. - dj@opennms.org
// 2008 Jan 06: Duplicate all EventConfigurationManager tests. - dj@opennms.org
// 2008 Jan 05: Organize imports, format code a bit, make tests run with latest
//              EventconfFactory changes (lining up functionality with
//              EventConfigurationManager). - dj@opennms.org
// 2007 Aug 24: Eliminate warnings, use Java 5 generics and loops, and use
//              DaoTestConfigBean instead of calling
//              System.setProperty("opennms.home", ...). - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on Nov 11, 2004
 */
package org.opennms.netmgt.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;

/**
 * @author brozow
 * 
 */
public class EventconfFactoryTest {

    private static final String knownUEI1="uei.opennms.org/internal/capsd/snmpConflictsWithDb";
    private static final String knownLabel1="OpenNMS-defined capsd event: snmpConflictsWithDb";
    private static final String knownSubfileUEI1="uei.opennms.org/IETF/Bridge/traps/newRoot";
    private static final String knownSubfileLabel1="BRIDGE-MIB defined trap event: newRoot";
    private static final String unknownUEI1="uei.opennms.org/foo/thisShouldBeAnUnknownUEI";

    @Before
    public void setUp() throws Exception {
        
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        super.setUp();
        
        EventconfFactory.init();
    }

    @Test
    public void testIsSecureTagWhenExists() {
        assertTrue("isSecureTag(\"logmsg\") should be true", EventconfFactory.getInstance().isSecureTag("logmsg"));
    }

    @Test
    public void testIsSecureTagWhenDoesNotExist() {
        assertFalse("isSecureTag(\"foobarbaz\") should be false", EventconfFactory.getInstance().isSecureTag("foobarbaz"));
    }
    
    @Test
    public void testFindByUeiKnown() {
        Event eventConf = EventconfFactory.getInstance().findByUei(knownUEI1);
        assertNotNull("returned event configuration for known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("UEI", knownUEI1, eventConf.getUei());
        assertEquals("label", knownLabel1, eventConf.getEventLabel());
    }

    @Test
    public void testFindByUeiUnknown() {
        Event eventConf = EventconfFactory.getInstance().findByUei(unknownUEI1);
        assertNull("returned event configuration for unknown UEI '" + unknownUEI1 + "' should be null", eventConf);
    }
    
    @Test
    public void testFindByEventUeiKnown() {
        org.opennms.netmgt.xml.event.Event matchingEvent = new org.opennms.netmgt.xml.event.Event();
        matchingEvent.setUei(knownUEI1);

        Event eventConf = EventconfFactory.getInstance().findByEvent(matchingEvent);
        assertNotNull("returned event configuration for event with known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("UEI", matchingEvent.getUei(), eventConf.getUei());
    }

    @Test
    public void testFindByEventUnknown() {
        org.opennms.netmgt.xml.event.Event matchingEvent = new org.opennms.netmgt.xml.event.Event();
        matchingEvent.setUei(unknownUEI1);

        Event eventConf = EventconfFactory.getInstance().findByEvent(matchingEvent);
        assertNull("returned event configuration for event with unknown UEI '" + unknownUEI1 + "' should be null", eventConf);
    }

    @Test
    public void testGetEventsByLabel() {
        List<Event> events = getEventsByLabel();

        ArrayList<String> beforeSort = new ArrayList<String>(events.size());
        for (Event e : events) {
            String label = e.getEventLabel();
            beforeSort.add(label);
        }

        ArrayList<String> afterSort = new ArrayList<String>(beforeSort);
        Collections.sort(afterSort, String.CASE_INSENSITIVE_ORDER);

        assertEquals(beforeSort.size(), afterSort.size());
        for (int i = 0; i < beforeSort.size(); i++) {
            assertEquals("Lists unequals at index " + i, beforeSort.get(i), afterSort.get(i));
        }

    }
    
    private List<Event> getEventsByLabel() {
        return EventconfFactory.getInstance().getEventsByLabel();
    }
    
    @Test
    public void testGetEventByUEI() {
        List<Event> result=EventconfFactory.getInstance().getEvents(knownUEI1);
        assertEquals("Should only be one result", 1, result.size());
        Event firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownUEI1, knownUEI1, firstEvent.getUei());
        
        result=EventconfFactory.getInstance().getEvents("uei.opennms.org/internal/capsd/nonexistent");
        assertNull("Should be null list for non-existent URI", result);
        
        //Find an event that's in a sub-file
        result=EventconfFactory.getInstance().getEvents(knownSubfileUEI1);
        assertEquals("Should only be one result", 1, result.size());
        firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownSubfileUEI1,knownSubfileUEI1, firstEvent.getUei());
    }
    
    @Test
    public void testGetEventUEIS() {
        List<String> ueis=EventconfFactory.getInstance().getEventUEIs();
        //This test assumes the test eventconf files only have X events in them.  Adjust as you modify eventconf.xml and sub files
        assertEquals("Count must be correct", 4, ueis.size());
        assertTrue("Must contain known UEI", ueis.contains(knownUEI1));
        assertTrue("Must contain known UEI", ueis.contains(knownSubfileUEI1));
    }
    
    @Test
    public void testGetLabels() {
        Map<String,String> labels=EventconfFactory.getInstance().getEventLabels();
        //This test assumes the test eventconf files only have X events in them.  Adjust as you modify eventconf.xml and sub files
        assertEquals("Count must be correct", 4, labels.size());
        assertTrue("Must contain known UEI", labels.containsKey(knownUEI1));
        assertEquals("Must have known Label", labels.get(knownUEI1), knownLabel1);
        assertTrue("Must contain known UEI", labels.containsKey(knownSubfileUEI1));
        assertEquals("Must have known Label", labels.get(knownSubfileUEI1), knownSubfileLabel1);
    }
    
    @Test
    public void testGetLabel() {
        assertEquals("Must have correct label"+knownLabel1, knownLabel1, EventconfFactory.getInstance().getEventLabel(knownUEI1));
        assertEquals("Must have correct label"+knownSubfileLabel1, knownSubfileLabel1, EventconfFactory.getInstance().getEventLabel(knownSubfileUEI1));
    }
    
    @Test
    public void testGetAlarmType() {
        Event event = new Event();
        AlarmData data = new AlarmData();
        data.setAlarmType(2);
        data.setClearUei("uei.opennms.org.testUei");
        data.setReductionKey("reduceme");
        event.setAlarmData(data);
        
        int i = event.getAlarmData().getAlarmType();
        assertEquals(2, i);
        assertTrue("uei.opennms.org.testUei".equals(event.getAlarmData().getClearUei()));
        assertTrue("reduceme".equals(event.getAlarmData().getReductionKey()));
    }
    
    //Ensure reload does indeed reload fresh data
    @Test
    public void testReload() {
        String newUEI="uei.opennms.org/custom/newTestUEI";
        List<Event> events=EventconfFactory.getInstance().getEvents(knownUEI1);
        Event event=(Event)events.get(0);
        event.setUei(newUEI);
        
        //Check that the new UEI is there
        List<Event> events2=EventconfFactory.getInstance().getEvents(newUEI);
        Event event2=((Event)events2.get(0));
        assertNotNull("Must have some events", event2);
        assertEquals("Must be exactly 1 event", 1, events2.size());
        assertEquals("uei must be the new one", newUEI, event2.getUei());
        

        //Now reload without saving - should not find the new one, but should find the old one
        try {
            EventconfFactory.getInstance().reload();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not have had exception while reloading factory "+e.getMessage());
        }
        List<Event> events3=EventconfFactory.getInstance().getEvents(knownUEI1);
        assertNotNull("Must have some events", events3);
        assertEquals("Must be exactly 1 event", 1, events3.size());
        Event event3=(Event)events3.get(0);
        assertEquals("uei must be the new one", knownUEI1, event3.getUei());       
        
        //Check that the new UEI is *not* there this time
        List<Event> events4=EventconfFactory.getInstance().getEvents(newUEI);
        assertNull("Must be no events by that name", events4);
    }
    
    /**
     * Test an eventconf.xml with only &lt;event&gt; elements and no
     * &lt;event-file&gt; elements.
     */
    @Test
    public void testLoadConfigurationSingleConfig() throws Exception {
        loadConfiguration("singleConfig/eventconf.xml");
    }

    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain absolute paths.  The included &lt;event-file&gt;
     * has no errors.
     */
    @Test
    public void testLoadConfigurationTwoDeepConfigAbsolutePaths() throws Exception {
        loadConfiguration("twoDeepConfig/eventconf.xml");
    }

    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain absolute paths.  The included &lt;event-file&gt;
     * references additional &lt;event-file&gt;s which is an error.
     */
    @Test
    public void testLoadConfigurationThreeDeepConfig() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            loadConfiguration("threeDeepConfig/eventconf.xml");
        } catch (DataAccessException e) {
            if (e.getMessage().contains("cannot include other configuration files")) {
                caughtExceptionThatWeWanted = true;
            } else {
                throw e;
            }
        }
        
        if (!caughtExceptionThatWeWanted) {
            fail("Did not get the exception that we wanted");
        }
    }
    
    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain absolute paths.  The included &lt;event-file&gt;
     * has a &lt;global&gt; element which is an error.
     */
    @Test
    public void testLoadConfigurationTwoDeepConfigWithGlobal() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            loadConfiguration("twoDeepConfigWithGlobal/eventconf.xml");
        } catch (DataAccessException e) {
            if (e.getMessage().contains("cannot have a 'global' element")) {
                caughtExceptionThatWeWanted = true;
            } else {
                throw e;
            }
        }
        
        if (!caughtExceptionThatWeWanted) {
            fail("Did not get the exception that we wanted");
        }
    }

    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain relative paths.  The included &lt;event-file&gt;
     * has no errors.
     */
    @Test
    public void testLoadConfigurationRelativeTwoDeepConfig() throws Exception {
        loadConfiguration("relativeTwoDeepConfig/eventconf.xml");
    }
    
    /**
     * Test loading a configuration with relative included &lt;event-file&gt;
     * entries but without passing a File object to loadConfiguration, which
     * should fail because the relative path cannot be resolved.
     * 
     * @throws Exception
     */
    @Test
    public void testLoadConfigurationWithNoFileRelativePathFailure() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
       
        try {
            loadConfiguration("relativeTwoDeepConfig/eventconf.xml", false);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("so the relative path cannot be resolved")) {
                caughtExceptionThatWeWanted = true;
            } else {
                throw e;
            }
        }
        
        if (!caughtExceptionThatWeWanted) {
            fail("Did not get the exception that we wanted");
        }
    }

    /**
     * Test loading a configuration with relative included &lt;event-file&gt;
     * entries but without passing a File object to loadConfiguration, which
     * should fail because the relative path cannot be resolved.
     * 
     * @throws Exception
     */
    @Test
    public void testLoadConfigurationWithClassPathInclude() throws Exception {
        loadConfiguration("classpathTwoDeep/eventconf.xml", false);
    }

    /**
     * Test that every file included in eventconf.xml actually exists on disk
     * and that there are no files on disk that aren't included. 
     */
    @Test
    public void testIncludedEventFilesExistAndNoExtras() throws Exception {
        File eventConfFile = ConfigurationTestUtils.getFileForConfigFile("eventconf.xml");
        File eventsDirFile = new File(eventConfFile.getParentFile(), "events");
        assertTrue("events directory exists at " + eventsDirFile.getAbsolutePath(), eventsDirFile.exists());
        assertTrue("events directory is a directory at " + eventsDirFile.getAbsolutePath(), eventsDirFile.isDirectory());
        
        File[] eventFilesOnDiskArray = eventsDirFile.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            } });
        Set<File> eventFilesOnDisk = new HashSet<File>(Arrays.asList(eventFilesOnDiskArray));

        Events events = CastorUtils.unmarshal(Events.class, ConfigurationTestUtils.getReaderForConfigFile("eventconf.xml"));
        Set<File> eventFilesIncluded = new HashSet<File>(events.getEventFileCollection().size());
        for (String eventFile : events.getEventFileCollection()) {
            eventFilesIncluded.add(new File(eventConfFile.getParentFile(), eventFile));
        }
        
        Set<File> includedNotOnDisk = new HashSet<File>(eventFilesIncluded);
        includedNotOnDisk.removeAll(eventFilesOnDisk);
        if (!includedNotOnDisk.isEmpty()) {
            fail("Event configuration file " + eventConfFile.getAbsolutePath() + " references included files that could not be found:\n\t"
                    + StringUtils.collectionToDelimitedString(includedNotOnDisk, "\n\t"));
        }
        
        Set<File> onDiskNotIncluded = new HashSet<File>(eventFilesOnDisk);
        onDiskNotIncluded.removeAll(eventFilesIncluded);
        if (!onDiskNotIncluded.isEmpty()) {
            fail("Events directory " + eventsDirFile.getAbsolutePath() + " contains event files that are not referenced in event configuration file " + eventConfFile.getAbsolutePath() + ":\n\t"
                    + StringUtils.collectionToDelimitedString(onDiskNotIncluded, "\n\t"));
        }
    }

    /**
     * Test the standard eventconf.xml configuration file and its include files.
     */
    @Test
    public void testLoadStandardConfiguration() throws Exception {
        DefaultEventConfDao dao = new DefaultEventConfDao();
        dao.setConfigResource(new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("eventconf.xml")));
        dao.afterPropertiesSet();
    }

    private void loadConfiguration(String relativeResourcePath) throws DataAccessException, IOException {
        loadConfiguration(relativeResourcePath, true);
    }
    
    private void loadConfiguration(String relativeResourcePath, boolean passFile) throws DataAccessException, IOException {
        DefaultEventConfDao dao = new DefaultEventConfDao();
        
        if (passFile) {
            URL url = getUrlForRelativeResourcePath(relativeResourcePath);
            dao.setConfigResource(new MockFileSystemResourceWithInputStream(new File(url.getFile()), getFilteredInputStreamForConfig(relativeResourcePath)));
        } else {
            dao.setConfigResource(new InputStreamResource(getFilteredInputStreamForConfig(relativeResourcePath)));
        }
        
        dao.afterPropertiesSet();
    }

    private InputStream getFilteredInputStreamForConfig(String resourceSuffix) throws IOException {
        URL url = getUrlForRelativeResourcePath(resourceSuffix);
        
        return ConfigurationTestUtils.getInputStreamForResourceWithReplacements(this, getResourceForRelativePath(resourceSuffix),
                new String[] { "\\$\\{install.etc.dir\\}", new File(url.getFile()).getParent() });
    }

    private URL getUrlForRelativeResourcePath(String resourceSuffix) {
        URL url = getClass().getResource(getResourceForRelativePath(resourceSuffix));
        assertNotNull("URL for resource " + getResourceForRelativePath(resourceSuffix) + " must not be null", url);
        return url;
    }

    private String getResourceForRelativePath(String resourceSuffix) {
        return "/org/opennms/netmgt/config/eventd/" + resourceSuffix;
    }
    
    private class MockFileSystemResourceWithInputStream implements Resource {
        private Resource m_delegate;
        private InputStream m_inputStream;

        public MockFileSystemResourceWithInputStream(File file, InputStream inputStream) {
            m_delegate = new FileSystemResource(file);
            
            m_inputStream = inputStream;
        }
        
        public InputStream getInputStream() {
            return m_inputStream;
        }

        public Resource createRelative(String relative) throws IOException {
            return m_delegate.createRelative(relative);
        }

        public boolean exists() {
            return m_delegate.exists();
        }

        public String getDescription() {
            return m_delegate.getDescription();
        }

        public File getFile() throws IOException {
            return m_delegate.getFile();
        }

        public String getFilename() {
            return m_delegate.getFilename();
        }

        public URL getURL() throws IOException {
            return m_delegate.getURL();
        }

        public boolean isOpen() {
            return m_delegate.isOpen();
        }
    }
}
