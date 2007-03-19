/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.rrd.jrobin;

import java.io.File;

import junit.framework.TestCase;

import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraphInfo;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;

/**
 * Unit tests for the JrobinRrdStrategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class JRobinRrdStrategyTest extends TestCase {
    
    private JRobinRrdStrategy m_strategy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
        
        m_strategy = new JRobinRrdStrategy();
        m_strategy.initialize();
    }
    
    public void testInitilize() {
       // Don't do anything... just check that setUp works 
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
    
    public void testPrint() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "--start=" + start,
                "--end=" + end,
                "CDEF:something=1",
                "PRINT:something:AVERAGE:\"%le\""
        };

        RrdGraphDef graphDef = m_strategy.createGraphDef(new File(""), command);
        RrdGraph graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        RrdGraphInfo info = graph.getRrdGraphInfo();
        assertNotNull("graph info object", info);
        
        String[] printLines = info.getPrintLines();
        assertNotNull("graph printLines", printLines);
        assertEquals("graph printLines size", 1, printLines.length);
        assertEquals("graph printLines item 0", "1.000000e+00", printLines[0]);
        double d = Double.parseDouble(printLines[0]);
        assertEquals("graph printLines item 0 as a double", 1.0, d);
    }
}
