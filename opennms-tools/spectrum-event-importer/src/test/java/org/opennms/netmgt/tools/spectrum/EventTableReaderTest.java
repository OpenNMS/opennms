/**
 * 
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

/**
 * @author jeffg
 *
 */
public class EventTableReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        EventTableReader reader = new EventTableReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/EventTables/ipUnityTrapSeverity"));
    }
    
    @Test
    public void readIpUnityTrapSeverityTable() throws IOException {
        EventTableReader reader = new EventTableReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/EventTables/ipUnityTrapSeverity"));
        EventTable et = reader.getEventTable();
        
        Assert.assertEquals("There should exist 6 event-map entries in this EventTable file", 6, et.size());
        
        Assert.assertEquals("clear(1)", "clear", et.get(1));
        Assert.assertEquals("informational(2)", "informational", et.get(2));
        Assert.assertEquals("warning(3)", "warning", et.get(3));
        Assert.assertEquals("minor(4)", "minor", et.get(4));
        Assert.assertEquals("major(5)", "major", et.get(5));
        Assert.assertEquals("critical(6)", "critical", et.get(6));
    }
}
