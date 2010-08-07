package org.opennms.netmgt.dao.jaxb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class JaxbUtilsTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @SuppressWarnings("deprecation")
    public void testUnmarshalReader() throws JAXBException, FileNotFoundException, IOException {
        JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, ConfigurationTestUtils.getReaderForConfigFile("/jdbc-datacollection-config.xml"));
    }

    public void testUnmarshalResource() throws JAXBException, FileNotFoundException, IOException {
        JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("/jdbc-datacollection-config.xml")));
    }
   
    public void testExceptionContainsFileNameUnmarshalResourceWithBadResource() throws JAXBException, FileNotFoundException, IOException {
        /*
         * We are going to attempt to unmarshal groups.xml with the wrong
         * class so we get a MarshalException and we can then test to see if the
         * file name is embedded in the exception.
         */
        boolean gotException = false;
        File file = ConfigurationTestUtils.getFileForConfigFile("groups.xml");
        try {
            JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, new FileSystemResource(file));
        } catch (JAXBException e) {
            String matchString = "unexpected element (uri:\"http://xmlns.opennms.org/xsd/groups\"";
            if (e.toString().contains(matchString)) {
                gotException = true;
            } else {
                AssertionFailedError ae = new AssertionFailedError("Got an exception, but not one containing the message we were expecting ('" + matchString + "'): " + e);
                ae.initCause(e);
                throw ae;
            }
        }
        
        if (!gotException) {
            fail("Did not get a JAXBException, but we were expecting one.");
        }
    }
}
