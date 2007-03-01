package org.opennms.netmgt.rrd.jrobin;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;

public class JRobinRrdStrategyTest extends TestCase {
    
    private JRobinRrdStrategy m_strategy;
    private FileAnticipator m_fileAnticipator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        // Don't initialize by default since not all tests need it.
        m_fileAnticipator = new FileAnticipator(false);

        m_strategy = new JRobinRrdStrategy();
        m_strategy.initialize();
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        m_fileAnticipator.tearDown();
    }
    
    public void testCommandWithoutDrawing() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String command = "--start=" + start + " --end=" + end;

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new RrdException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_strategy.createGraph(command, new File(""));
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
    
    public void testCommentWithNewlines() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "--start=" + start,
                "--end=" + end,
                "COMMENT:foo\\n"
        };
        String[] command2 = new String[] {
                "--start=" + start,
                "--end=" + end,
                "COMMENT:foo\\n",
                "COMMENT:foo2\\n"
        };

        RrdGraphDef graphDef = m_strategy.createGraphDef(new File(""), command);
        RrdGraph graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        int firstHeight = graph.getRrdGraphInfo().getHeight();

        RrdGraphDef graphDef2 = m_strategy.createGraphDef(new File(""), command2);
        RrdGraph graph2 = new RrdGraph(graphDef2);
        assertNotNull("second graph object", graph2);
        
        int secondHeight = graph2.getRrdGraphInfo().getHeight();

        assertFalse("first graph height " + firstHeight + " and second graph height " + secondHeight + " should not be equal... there should be another newline in the second one making it taller", firstHeight == secondHeight);
    }
    
    public void testGprintWithNewlines() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "--start=" + start,
                "--end=" + end,
                "CDEF:a=1",
                "GPRINT:a:AVERAGE:\"%8.2lf\\n\""
        };
        String[] command2 = new String[] {
                "--start=" + start,
                "--end=" + end,
                "CDEF:a=1",
                "CDEF:b=1",
                "GPRINT:a:AVERAGE:\"%8.2lf\\n\"",
                "GPRINT:b:AVERAGE:\"%8.2lf\\n\""
        };

        RrdGraphDef graphDef = m_strategy.createGraphDef(new File(""), command);
        RrdGraph graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        int firstHeight = graph.getRrdGraphInfo().getHeight();

        RrdGraphDef graphDef2 = m_strategy.createGraphDef(new File(""), command2);
        RrdGraph graph2 = new RrdGraph(graphDef2);
        assertNotNull("second graph object", graph2);
        
        int secondHeight = graph2.getRrdGraphInfo().getHeight();

        assertFalse("first graph height " + firstHeight + " and second graph height " + secondHeight + " should not be equal... there should be another line with a newline in the second one making it taller", firstHeight == secondHeight);
    }
}
