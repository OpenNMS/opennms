/**
 * 
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author jeffg
 *
 */
public class AlertMapReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void testOneArgConstructor() throws IOException {
        AlertMapReader reader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
    }
    
    @Test
    public void testReadSonusAlertMap() throws IOException {
        AlertMapReader reader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
        List<AlertMapping> mappings = reader.getAlertMaps();
        Assert.assertEquals(751, mappings.size());
    }
}
