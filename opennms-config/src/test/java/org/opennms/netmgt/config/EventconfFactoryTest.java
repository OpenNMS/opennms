/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

/*
 * Created on Nov 11, 2004
 */
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URI;
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
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.snmp.SyntaxToEvent;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.ClassPathResource;
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
    private static final Logger LOG = LoggerFactory.getLogger(EventconfFactoryTest.class);

    private static final String knownUEI1="uei.opennms.org/internal/capsd/snmpConflictsWithDb";
    private static final String knownLabel1="OpenNMS-defined capsd event: snmpConflictsWithDb";
    private static final String knownSubfileUEI1="uei.opennms.org/IETF/Bridge/traps/newRoot";
    private static final String knownSubfileLabel1="BRIDGE-MIB defined trap event: newRoot";
    private static final String unknownUEI1="uei.opennms.org/foo/thisShouldBeAnUnknownUEI";
    
    DefaultEventConfDao m_eventConfDao;

    @Before
    public void setUp() throws Exception {
        m_eventConfDao = new DefaultEventConfDao();
        m_eventConfDao.setConfigResource(new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("eventconf.xml")));
        m_eventConfDao.afterPropertiesSet();
    }

    @Test
    public void testIsSecureTagWhenExists() {
        assertTrue("isSecureTag(\"logmsg\") should be true", m_eventConfDao.isSecureTag("logmsg"));
    }

    @Test
    public void testIsSecureTagWhenDoesNotExist() {
        assertFalse("isSecureTag(\"foobarbaz\") should be false", m_eventConfDao.isSecureTag("foobarbaz"));
    }
    
    @Test
    public void testFindByUeiKnown() {
        Event eventConf = m_eventConfDao.findByUei(knownUEI1);
        assertNotNull("returned event configuration for known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("UEI", knownUEI1, eventConf.getUei());
        assertEquals("label", knownLabel1, eventConf.getEventLabel());
    }

    @Test
    public void testFindByUeiUnknown() {
        Event eventConf = m_eventConfDao.findByUei(unknownUEI1);
        assertNull("returned event configuration for unknown UEI '" + unknownUEI1 + "' should be null", eventConf);
    }
    
    @Test
    public void testFindByEventUeiKnown() {
        EventBuilder bldr = new EventBuilder(knownUEI1, "testFindByEventUeiKnown");

        Event eventConf = m_eventConfDao.findByEvent(bldr.getEvent());
        assertNotNull("returned event configuration for event with known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("UEI", bldr.getEvent().getUei(), eventConf.getUei());
    }

    @Test
    public void testFindByEventUeiKnown1000Times() throws Exception {
    	
    	final int ATTEMPTS = 10000;
    	
        EventBuilder bldr = new EventBuilder(knownUEI1, "testFindByEventUeiKnown");

    	DefaultEventConfDao eventConfDao = loadConfiguration("eventconf-speedtest/eventconf.xml");

    	Event eventConf = null;
		org.opennms.netmgt.xml.event.Event event = bldr.getEvent();
        long start = System.currentTimeMillis();
        for(int i = 0; i < ATTEMPTS; i++) {
			eventConf = eventConfDao.findByEvent(event);
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        System.err.printf("%d Attempts: Elapsed: %d ms: events per second %f.%n", ATTEMPTS, elapsed, ATTEMPTS*1000.0/elapsed);

        
        assertNotNull("returned event configuration for event with known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("UEI", bldr.getEvent().getUei(), eventConf.getUei());
    }

    public class EventCreator  {
        
        private EventBuilder m_eventBuilder;

        
        public EventCreator() {
            setEventBuilder(new EventBuilder(null, "trapd"));
        }
        
        public void setCommunity(String community) {
            getEventBuilder().setCommunity(community);
        }

        public void setTimeStamp(long timeStamp) {
            getEventBuilder().setSnmpTimeStamp(timeStamp);
        }

        public void setVersion(String version) {
            getEventBuilder().setSnmpVersion(version);
        }

        private void setGeneric(int generic) {
            getEventBuilder().setGeneric(generic);
        }

        private void setSpecific(int specific) {
            getEventBuilder().setSpecific(specific);
        }

        private void setEnterpriseId(String enterpriseId) {
            getEventBuilder().setEnterpriseId(enterpriseId);
        }

        public void setAgentAddress(InetAddress agentAddress) {
            getEventBuilder().setHost(InetAddressUtils.toIpAddrString(agentAddress));
        }

        public void processVarBind(SnmpObjId name, SnmpValue value) {
            getEventBuilder().addParam(SyntaxToEvent.processSyntax(name.toString(), value));
            if (EventConstants.OID_SNMP_IFINDEX.isPrefixOf(name)) {
                getEventBuilder().setIfIndex(value.toInt());
            }
        }

        public void setTrapAddress(InetAddress trapAddress) {
            getEventBuilder().setSnmpHost(str(trapAddress));
            getEventBuilder().setInterface(trapAddress);
            long nodeId = 27;
            if (nodeId != -1) {
                getEventBuilder().setNodeid(nodeId);
            }
        }

        public void setTrapIdentity(TrapIdentity trapIdentity) {
            setGeneric(trapIdentity.getGeneric());
            setSpecific(trapIdentity.getSpecific());
            setEnterpriseId(trapIdentity.getEnterpriseId().toString());
        
            LOG.debug("setTrapIdentity: SNMP trap {}", trapIdentity);
        }

        public org.opennms.netmgt.xml.event.Event getEvent() {
            return getEventBuilder().getEvent();
        }
        
		private EventBuilder getEventBuilder() {
			return m_eventBuilder;
		}

		private void setEventBuilder(EventBuilder eventBuilder) {
			m_eventBuilder = eventBuilder;
		}
    }
    @Test
    public void testFindByTrap() throws Exception {
        String enterpriseId = ".1.3.6.1.4.1.5813.1";
		int generic = 6;
		int specific = 1;
        String ip = "127.0.0.1";

        EventBuilder bldr = new EventBuilder(null, "trapd");
		bldr.setSnmpVersion("v2");
        bldr.setCommunity("public");
		bldr.setHost(ip);
        bldr.setSnmpHost(ip);
		bldr.setInterface(InetAddress.getByName("127.0.0.1"));
    
        // time-stamp (units is hundreths of a second
		bldr.setSnmpTimeStamp(System.currentTimeMillis()/10);
    
        bldr.setGeneric(generic);
		bldr.setSpecific(specific);
		bldr.setEnterpriseId(enterpriseId);
		
		for(int i = 0; i < 19; i++) {
			bldr.addParam(".1.3.6."+(i+1), "parm" + (i+1) );
		}
		
		
    	DefaultEventConfDao eventConfDao = loadConfiguration("eventconf-speedtest/eventconf.xml");

        
		org.opennms.netmgt.xml.event.Event event = bldr.getEvent();
		Event eventConf = eventConfDao.findByEvent(event);

        
        assertNotNull("returned event configuration for event with known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("uei.opennms.org/traps/eventTrap", eventConf.getUei());
    }
    @Test
    public void testFindByTrapWithWildcard() throws Exception {
        String enterpriseId = ".1.3.6.1.4.1.253.1.2.3";
                int generic = 6;
                int specific = 1;
        String ip = "127.0.0.1";

        EventBuilder bldr = new EventBuilder(null, "trapd");
                bldr.setSnmpVersion("v2");
        bldr.setCommunity("public");
                bldr.setHost(ip);
        bldr.setSnmpHost(ip);
                bldr.setInterface(InetAddress.getByName("127.0.0.1"));
    
        // time-stamp (units is hundreths of a second
                bldr.setSnmpTimeStamp(System.currentTimeMillis()/10);
    
        bldr.setGeneric(generic);
                bldr.setSpecific(specific);
                bldr.setEnterpriseId(enterpriseId);
                
                for(int i = 0; i < 19; i++) {
                        bldr.addParam(".1.3.6."+(i+1), "parm" + (i+1) );
                }
                
                
        DefaultEventConfDao eventConfDao = loadConfiguration("eventconf-wildcard/eventconf.xml");

        
                org.opennms.netmgt.xml.event.Event event = bldr.getEvent();
                Event eventConf = eventConfDao.findByEvent(event);

        
        assertNotNull("returned event configuration for eclipsed trap '" + enterpriseId + "' should not be null", eventConf);
        assertEquals("uei.opennms.org/vendor/Xerox/traps/EnterpriseDefault", eventConf.getUei());
    }
    @Test
    public void testFindByTrap1000Times() throws Exception {
        String enterpriseId = ".1.3.6.1.4.1.5813.1";
		int generic = 6;
		int specific = 1;
        String ip = "127.0.0.1";

        EventBuilder bldr = new EventBuilder(null, "trapd");
		bldr.setSnmpVersion("v2");
        bldr.setCommunity("public");
		bldr.setHost(ip);
        bldr.setSnmpHost(ip);
		bldr.setInterface(InetAddress.getByName("127.0.0.1"));
    
        // time-stamp (units is hundreths of a second
		bldr.setSnmpTimeStamp(System.currentTimeMillis()/10);
    
        bldr.setGeneric(generic);
		bldr.setSpecific(specific);
		bldr.setEnterpriseId(enterpriseId);
		
		for(int i = 0; i < 19; i++) {
			bldr.addParam(".1.3.6."+(i+1), "parm" + (i+1) );
		}
		
		
    	DefaultEventConfDao eventConfDao = loadConfiguration("eventconf-speedtest/eventconf.xml");

    	final int ATTEMPTS = 10000;
    	
        Event eventConf = null;
        
		org.opennms.netmgt.xml.event.Event event = bldr.getEvent();
        long start = System.currentTimeMillis();
        for(int i = 0; i < ATTEMPTS; i++) {
			eventConf = eventConfDao.findByEvent(event);
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        System.err.printf("%d Attempts: Elapsed: %d ms: events per second %f.%n", ATTEMPTS, elapsed, ATTEMPTS*1000.0/elapsed);

        
        assertNotNull("returned event configuration for event with known UEI '" + knownUEI1 + "' should not be null", eventConf);
        assertEquals("uei.opennms.org/traps/eventTrap", eventConf.getUei());
    }

    @Test
    public void testFindByEventUnknown() {
        EventBuilder bldr = new EventBuilder(unknownUEI1, "testFindByEventUnknown");
        Event eventConf = m_eventConfDao.findByEvent(bldr.getEvent());
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
        return m_eventConfDao.getEventsByLabel();
    }
    
    @Test
    public void testGetEventByUEI() {
        List<Event> result=m_eventConfDao.getEvents(knownUEI1);
        assertEquals("Should only be one result", 1, result.size());
        Event firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownUEI1, knownUEI1, firstEvent.getUei());
        
        result=m_eventConfDao.getEvents("uei.opennms.org/internal/capsd/nonexistent");
        assertNull("Should be null list for non-existent URI", result);
        
        //Find an event that's in a sub-file
        result=m_eventConfDao.getEvents(knownSubfileUEI1);
        assertEquals("Should only be one result", 1, result.size());
        firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownSubfileUEI1,knownSubfileUEI1, firstEvent.getUei());
    }
    
    @Test
    public void testGetEventUEIS() {
        List<String> ueis=m_eventConfDao.getEventUEIs();
        assertTrue("Must contain known UEI", ueis.contains(knownUEI1));
        assertTrue("Must contain known UEI", ueis.contains(knownSubfileUEI1));
    }
    
    @Test
    public void testGetLabels() {
        Map<String,String> labels=m_eventConfDao.getEventLabels();
        assertTrue("Must contain known UEI", labels.containsKey(knownUEI1));
        assertEquals("Must have known Label", labels.get(knownUEI1), knownLabel1);
        assertTrue("Must contain known UEI", labels.containsKey(knownSubfileUEI1));
        assertEquals("Must have known Label", labels.get(knownSubfileUEI1), knownSubfileLabel1);
    }
    
    @Test
    public void testGetLabel() {
        assertEquals("Must have correct label"+knownLabel1, knownLabel1, m_eventConfDao.getEventLabel(knownUEI1));
        assertEquals("Must have correct label"+knownSubfileLabel1, knownSubfileLabel1, m_eventConfDao.getEventLabel(knownSubfileUEI1));
    }
    
    @Test
    public void testGetAlarmType() {
        Event event = new Event();
        AlarmData data = new AlarmData();
        data.setAlarmType(2);
        data.setClearKey("uei.opennms.org.testUei:localhost:1");
        data.setReductionKey("reduceme");
        event.setAlarmData(data);
        
        int i = event.getAlarmData().getAlarmType();
        assertEquals(2, i);
        assertTrue("uei.opennms.org.testUei:localhost:1".equals(event.getAlarmData().getClearKey()));
        assertTrue("reduceme".equals(event.getAlarmData().getReductionKey()));
    }
    
    //Ensure reload does indeed reload fresh data
    @Test
    public void testReload() throws Exception {
		m_eventConfDao.setConfigResource(new ClassPathResource(getResourceForRelativePath("eventconf-speedtest/eventconf.xml")));

        String newUEI="uei.opennms.org/custom/newTestUEI";
        List<Event> events=m_eventConfDao.getEvents(knownUEI1);
        Event event=(Event)events.get(0);
        event.setUei(newUEI);
        
        //Check that the new UEI is there
        List<Event> events2=m_eventConfDao.getEvents(newUEI);
        Event event2=((Event)events2.get(0));
        assertNotNull("Must have some events", event2);
        assertEquals("Must be exactly 1 event", 1, events2.size());
        assertEquals("uei must be the new one", newUEI, event2.getUei());
        

        //Now reload without saving - should not find the new one, but should find the old one
        try {
            m_eventConfDao.reload();
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Should not have had exception while reloading factory "+e.getMessage());
        }
        List<Event> events3=m_eventConfDao.getEvents(knownUEI1);
        assertNotNull("Must have some events", events3);
        assertEquals("Must be exactly 1 event", 1, events3.size());
        Event event3=(Event)events3.get(0);
        assertEquals("uei must be the new one", knownUEI1, event3.getUei());       
        
        //Check that the new UEI is *not* there this time
        List<Event> events4=m_eventConfDao.getEvents(newUEI);
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
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            } });
        Set<File> eventFilesOnDisk = new HashSet<File>(Arrays.asList(eventFilesOnDiskArray));

        Reader r = new FileReader(eventConfFile);
        Events events = JaxbUtils.unmarshal(Events.class, r);
        r.close();
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

    private DefaultEventConfDao loadConfiguration(String relativeResourcePath) throws DataAccessException, IOException {
        return loadConfiguration(relativeResourcePath, true);
    }
    
    private DefaultEventConfDao loadConfiguration(String relativeResourcePath, boolean passFile) throws DataAccessException, IOException {
        DefaultEventConfDao dao = new DefaultEventConfDao();
        
        if (passFile) {
            URL url = getUrlForRelativeResourcePath(relativeResourcePath);
            dao.setConfigResource(new MockFileSystemResourceWithInputStream(new File(url.getFile()), getFilteredInputStreamForConfig(relativeResourcePath)));
        } else {
            dao.setConfigResource(new InputStreamResource(getFilteredInputStreamForConfig(relativeResourcePath)));
        }
        
        dao.afterPropertiesSet();
        return dao;
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
        
        @Override
        public InputStream getInputStream() {
            return m_inputStream;
        }

        @Override
        public Resource createRelative(String relative) throws IOException {
            return m_delegate.createRelative(relative);
        }

        @Override
        public boolean exists() {
            return m_delegate.exists();
        }

        @Override
        public String getDescription() {
            return m_delegate.getDescription();
        }

        @Override
        public File getFile() throws IOException {
            return m_delegate.getFile();
        }

        @Override
        public String getFilename() {
            return m_delegate.getFilename();
        }

        @Override
        public URL getURL() throws IOException {
            return m_delegate.getURL();
        }

        @Override
        public boolean isOpen() {
            return m_delegate.isOpen();
        }

        @Override
        public URI getURI() throws IOException {
            return m_delegate.getURI();
        }

        @Override
        public boolean isReadable() {
            return m_delegate.isReadable();
        }

        @Override
        public long lastModified() throws IOException {
            return m_delegate.lastModified();
        }

        @Override
        public long contentLength() throws IOException {
            return m_delegate.contentLength();
        }
    }
}
