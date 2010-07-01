/**
 * 
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

/**
 * @author jeffg
 *
 */
public class EventDispositionReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        EventDispositionReader reader = new EventDispositionReader(new FileSystemResource("src/test/resources/sonus-traps/EventDisp"));
    }
    
    @Test
    public void readSonusEventDisposition() throws IOException {
        EventDispositionReader reader = new EventDispositionReader(new FileSystemResource("src/test/resources/sonus-traps/EventDisp"));
        List<EventDisposition> dispositions = reader.getEventDispositions();
        
        int alarmFreeDispositions = 0;
        int alarmCreateDispositions = 0;
        int alarmClearDispositions = 0;
        
        Assert.assertEquals("There should exist 757 event-dispositions in this EventDisp file", 757, dispositions.size());
        
        for (EventDisposition disposition : dispositions) {
            if (disposition.isCreateAlarm()) {
                alarmCreateDispositions++;
            } else if (disposition.isClearAlarm()) {
                alarmClearDispositions++;
            } else {
                alarmFreeDispositions++;
            }
        }

        Assert.assertEquals("321 event-dispositions should neither create nor clear an alarm", 321, alarmFreeDispositions);
        Assert.assertEquals("379 event-dispositions should create an alarm", 379, alarmCreateDispositions);
        Assert.assertEquals("57 event-dispositions should clear an alarm", 57, alarmClearDispositions);
    }
}
