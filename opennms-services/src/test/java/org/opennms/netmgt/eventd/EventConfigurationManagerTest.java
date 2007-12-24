package org.opennms.netmgt.eventd;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.util.StringUtils;

public class EventConfigurationManagerTest extends TestCase {
    public void testLoadConfigurationSingleConfig() throws Exception {
        EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/singleConfig/eventconf.xml"));
    }

    public void testLoadConfigurationTwoDeepConfig() throws Exception {
        EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/twoDeepConfig/eventconf.xml"));
    }

    public void testLoadConfigurationThreeDeepConfig() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/threeDeepConfig/eventconf.xml"));
        } catch (ValidationException e) {
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
    
    public void testLoadConfigurationTwoDeepConfigWithGlobal() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/twoDeepConfigWithGlobal/eventconf.xml"));
        } catch (ValidationException e) {
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
     * This is disabled because the last check fails.
     */
    public void DISABLEDtestIncludedEventFilesExistAndNoExtras() throws Exception {
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
            eventFilesIncluded.add(new File(eventConfFile.getParentFile(), eventFile.replaceAll("@install.etc.dir@/", "")));
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
}
