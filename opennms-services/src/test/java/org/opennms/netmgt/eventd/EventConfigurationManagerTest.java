package org.opennms.netmgt.eventd;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.util.StringUtils;

public class EventConfigurationManagerTest extends TestCase {
    /**
     * Test an eventconf.xml with only &lt;event&gt; elements and no
     * &lt;event-file&gt; elements.
     */
    public void testLoadConfigurationSingleConfig() throws Exception {
        loadConfiguration("singleConfig/eventconf.xml");
    }

    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain absolute paths.  The included &lt;event-file&gt;
     * has no errors.
     */
    public void testLoadConfigurationTwoDeepConfigAbsolutePaths() throws Exception {
        loadConfiguration("twoDeepConfig/eventconf.xml");
    }

    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain absolute paths.  The included &lt;event-file&gt;
     * references additional &lt;event-file&gt;s which is an error.
     */
    public void testLoadConfigurationThreeDeepConfig() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            loadConfiguration("threeDeepConfig/eventconf.xml");
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
    
    /**
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain absolute paths.  The included &lt;event-file&gt;
     * has a &lt;global&gt; element which is an error.
     */
    public void testLoadConfigurationTwoDeepConfigWithGlobal() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            loadConfiguration("twoDeepConfigWithGlobal/eventconf.xml");
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
     * Test an eventconf.xml with &lt;event&gt; elements and &lt;event-file&gt;
     * elements that contain relative paths.  The included &lt;event-file&gt;
     * has no errors.
     */
    public void testLoadConfigurationRelativeTwoDeepConfig() throws Exception {
        loadConfiguration("relativeTwoDeepConfig/eventconf.xml");
    }

    /**
     * Test the standard eventconf.xml configuration file and its include files.
     */
    public void testLoadStandardConfiguration() throws Exception {
        EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getFileForConfigFile("eventconf.xml"));
    }

    /**
     * This is disabled because the last check fails.
     */
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

    private void loadConfiguration(String relativeResourcePath) throws IOException, MarshalException, ValidationException {
        URL url = getUrlForRelativeResourcePath(relativeResourcePath);
        EventConfigurationManager.loadConfiguration(getFilteredReaderForConfig(relativeResourcePath), new File(url.getFile()));
    }

    private Reader getFilteredReaderForConfig(String resourceSuffix) throws IOException {
        URL url = getUrlForRelativeResourcePath(resourceSuffix);
        
        Reader reader = ConfigurationTestUtils.getReaderForResourceWithReplacements(this, getResourceForRelativePath(resourceSuffix),
                new String[] { "@install.etc.dir@", new File(url.getFile()).getParent() });
        return reader;
    }

    private URL getUrlForRelativeResourcePath(String resourceSuffix) {
        URL url = getClass().getResource(getResourceForRelativePath(resourceSuffix));
        assertNotNull("URL for resource " + getResourceForRelativePath(resourceSuffix) + " must not be null", url);
        return url;
    }

    private String getResourceForRelativePath(String resourceSuffix) {
        return "/org/opennms/netmgt/config/eventd/" + resourceSuffix;
    }
}
