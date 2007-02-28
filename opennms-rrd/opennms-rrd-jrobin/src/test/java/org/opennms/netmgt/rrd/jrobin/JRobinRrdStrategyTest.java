package org.opennms.netmgt.rrd.jrobin;

import java.io.File;

import org.opennms.netmgt.rrd.RrdException;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

public class JRobinRrdStrategyTest extends TestCase {
    public void testCommandWithoutDrawing() throws Exception {
        JRobinRrdStrategy strategy = new JRobinRrdStrategy();
        strategy.initialize();
        
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String command = "--start=" + start + " --end=" + end;

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new RrdException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            strategy.createGraph(command, new File(""));
        } catch (Throwable t) {
            ta.throwableReceived(t);
            
            // Do some additional checks that ThrowableAnticipator doesn't support.
            Throwable cause = t.getCause();
            assertNotNull("throwable should have a cause", cause);
            assertEquals("cause class", RrdException.class, cause.getClass());
            
            String problemText = "no graph was produced";
            assertTrue("cause message should contain '" + problemText + "'", cause.getMessage().contains(problemText));
            
            String suggestionText = "Does the command have any drawing commands";
            assertTrue("cause message should contain '" + suggestionText + "'", cause.getMessage().contains(suggestionText));
        }
        ta.verifyAnticipated();
        
        
    }
}
