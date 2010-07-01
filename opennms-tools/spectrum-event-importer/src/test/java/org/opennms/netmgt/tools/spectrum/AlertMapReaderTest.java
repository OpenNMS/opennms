/**
 * 
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

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
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        AlertMapReader reader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
    }
    
    @Test
    public void readSonusAlertMap() throws IOException {
        AlertMapReader reader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
        List<AlertMapping> mappings = reader.getAlertMappings();
        
        int singleVarbind = 0;
        
        Assert.assertEquals("There should exist 751 alert-mappings in this AlertMap", 751, mappings.size());
        
        for (AlertMapping mapping : mappings) {
            if (mapping.getOidMappings().size() == 0) {
                String ec = mapping.getEventCode();
                Assert.assertTrue("Only ten specific alert-mappings should have no OID-mappings; " + ec + " is not one of them", mapping.getEventCode().matches("^0xfff00((17[23cde])|(18[03cde])|(34e))$"));
            }
            
            LogUtils.debugf(this, "Alert-mapping for alert code %s to event code %s has %d OID-mappings", mapping.getAlertCode(), mapping.getEventCode(), mapping.getOidMappings().size());
            if (mapping.getOidMappings().size() == 1) {
                singleVarbind++;
            }
        }

        Assert.assertEquals("86 alert-mapings should have exactly one OID-mapping", 86, singleVarbind);
    }
}
