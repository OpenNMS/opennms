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
public class EventFormatReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        EventFormatReader reader = new EventFormatReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/Eventfff0034e"));
    }
    
    @Test
    public void readCovergenceSystemHaltEventFormat() throws IOException {
        EventFormatReader reader = new EventFormatReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/Eventfff0034e"));
        EventFormat ef = reader.getEventFormat();
        
        Assert.assertEquals("Check the contents against known good copy",
                            "{d \"%w- %d %m-, %Y - %T\"} - A \"systemHalt\" event has occurred, from {t} device, named {m}.\n" +
                            "\n" +
                            "\"system: report that a system halt has been initiated\"\n" +
                            "\n" +
                            "(event [{e}])\n", ef.getContents());
        
        Assert.assertEquals("Format should have four substitution tokens", 4, ef.getSubstTokens().size());
        Assert.assertEquals("Date stamp substitution token", "{d \"%w- %d %m-, %Y - %T\"}", ef.getSubstTokens().get(0));
        Assert.assertEquals("Model type substitution token", "{t}", ef.getSubstTokens().get(1));
        Assert.assertEquals("Model name substitution token", "{m}", ef.getSubstTokens().get(2));
        Assert.assertEquals("Event code substitution token", "{e}", ef.getSubstTokens().get(3));
    }
}
