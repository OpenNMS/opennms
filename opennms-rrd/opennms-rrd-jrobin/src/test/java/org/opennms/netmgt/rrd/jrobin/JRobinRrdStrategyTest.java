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
 * Modifications:
 *
 * 2008 Jul 29: Fix up a test string. - dj@opennms.org
 * 2008 Jun 17: Add tests for bug #2223. - jeffg@opennms.org
 * 2008 Feb 15: Add tests for bug #2272. - dj@opennms.org
 * 2007 Mar 19: Adjust for changes with exceptions and add test
 *              for a graph with only PRINT commands through the
 *              RrdStrategy interface with createGraphReturnDetails. - dj@opennms.org
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.Sample;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraphInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.util.StringUtils;

/**
 * Unit tests for the JrobinRrdStrategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class JRobinRrdStrategyTest {
    
    private RrdStrategy m_strategy;
    private FileAnticipator m_fileAnticipator;
    
    @Before
    public void setUp() throws Exception {
        
        MockLogAppender.setupLogging();
        
        m_strategy = new JRobinRrdStrategy();
        m_strategy.initialize();

        // Don't initialize by default since not all tests need it.
        m_fileAnticipator = new FileAnticipator(false);
    }

    @After
    public void tearDown() throws Exception {
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
        m_fileAnticipator.tearDown();
    }

    @Test
    public void testInitialize() {
       // Don't do anything... just check that setUp works 
    }
    
    @Test
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
            
            // We don't care about the exact message, just a few details
            String problemText = "no graph was produced";
            assertTrue("cause message should contain '" + problemText + "'", t.getMessage().contains(problemText));
            
            String suggestionText = "Does the command have any drawing commands";
            assertTrue("cause message should contain '" + suggestionText + "'", t.getMessage().contains(suggestionText));
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testCreate() throws Exception {
        File rrdFile = createRrdFile();
        
        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        //m_strategy.updateFile(openedFile, "huh?", "N:1,234234");
        
        Sample sample = ((RrdDb) openedFile).createSample();
        sample.set("N:1.234 something not that politically incorrect");
        System.err.println(sample.dump());

        m_strategy.closeFile(openedFile);
    }

    @Test
    public void testUpdate() throws Exception {
        File rrdFile = createRrdFile();
        
        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        m_strategy.updateFile(openedFile, "huh?", "N:1.234234");
        m_strategy.closeFile(openedFile);
    }

    @Test
    public void testSampleSetFloatingPointValueGood() throws Exception {
        File rrdFile = createRrdFile();
        
        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        Sample sample = ((RrdDb) openedFile).createSample();
        sample.set("N:1.234");
        m_strategy.closeFile(openedFile);
        
        double[] values = sample.getValues();
        assertEquals("values list size", 1, values.length);
        assertEquals("values item 0", 1.234, values[0], 0.0);
    }

    /**
     * This test fails because of
     * <a href="http://bugzilla.opennms.org/show_bug.cgi?id=2272">bug #2272</a>
     * in org.jrobin.core.Sample.
     */
    @Test
    @Ignore("fails due to bug 2272")
    public void testSampleSetFloatingPointValueWithComma() throws Exception {
        File rrdFile = createRrdFile();
        
        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        Sample sample = ((RrdDb) openedFile).createSample();
        sample.set("N:1,234");
        m_strategy.closeFile(openedFile);
        
        double[] values = sample.getValues();
        assertEquals("values list size", 1, values.length);
        assertEquals("values item 0", 1.234, values[0], 0.0);
    }

    /**
     * This test fails because of
     * <a href="http://bugzilla.opennms.org/show_bug.cgi?id=2272">bug #2272</a>
     * in org.jrobin.core.Sample.
     */
    @Test
    @Ignore("fails due to bug 2272")
    public void testSampleSetFloatingPointValueWithExtraJunk() throws Exception {
        File rrdFile = createRrdFile();
        
        Object openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        Sample sample = ((RrdDb) openedFile).createSample();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new Exception("Some exception that complains about bogus data"));
        try {
            sample.set("N:1.234 extra junk");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        } finally {
            m_strategy.closeFile(openedFile);
        }
        ta.verifyAnticipated();
    }
    
    @Test
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

        RrdGraphDef graphDef = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command);
        RrdGraph graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        int firstHeight = graph.getRrdGraphInfo().getHeight();

        RrdGraphDef graphDef2 = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command2);
        RrdGraph graph2 = new RrdGraph(graphDef2);
        assertNotNull("second graph object", graph2);
        
        int secondHeight = graph2.getRrdGraphInfo().getHeight();

        assertFalse("first graph height " + firstHeight + " and second graph height " + secondHeight + " should not be equal... there should be another newline in the second one making it taller", firstHeight == secondHeight);
    }
    
    @Test
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

        RrdGraphDef graphDef = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command);
        RrdGraph graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        int firstHeight = graph.getRrdGraphInfo().getHeight();

        RrdGraphDef graphDef2 = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command2);
        RrdGraph graph2 = new RrdGraph(graphDef2);
        assertNotNull("second graph object", graph2);
        
        int secondHeight = graph2.getRrdGraphInfo().getHeight();

        assertFalse("first graph height " + firstHeight + " and second graph height " + secondHeight + " should not be equal... there should be another line with a newline in the second one making it taller", firstHeight == secondHeight);
    }
    
    @Test
    public void testPrint() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "--start=" + start,
                "--end=" + end,
                "CDEF:something=1",
                "PRINT:something:AVERAGE:\"%le\""
        };

        RrdGraphDef graphDef = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command);
        RrdGraph graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        RrdGraphInfo info = graph.getRrdGraphInfo();
        assertNotNull("graph info object", info);
        
        String[] printLines = info.getPrintLines();
        assertNotNull("graph printLines", printLines);
        assertEquals("graph printLines size", 1, printLines.length);
        assertEquals("graph printLines item 0", "1.000000e+00", printLines[0]);
        double d = Double.parseDouble(printLines[0]);
        assertEquals("graph printLines item 0 as a double", 1.0, d, 0.0);
    }
    

    @Test
    public void testPrintThroughInterface() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "--start=" + start,
                "--end=" + end,
                "CDEF:something=1",
                "PRINT:something:AVERAGE:\"%le\""
        };

        RrdGraphDetails graphDetails = m_strategy.createGraphReturnDetails(StringUtils.arrayToDelimitedString(command, " "), new File(""));
        assertNotNull("graph details object", graphDetails);
        
        String[] printLines = graphDetails.getPrintLines();
        assertNotNull("graph printLines", printLines);
        assertEquals("graph printLines size", 1, printLines.length);
        assertEquals("graph printLines item 0", "1.000000e+00", printLines[0]);
        double d = Double.parseDouble(printLines[0]);
        assertEquals("graph printLines item 0 as a double", 1.0, d, 0.0);
    }
    
    @Test
    public void testTWQnENoQNoE() throws Exception {
    	String input = "This string has no quoting and no escapes";
    	String[] expected = new String[] { "This", "string", "has", "no", "quoting", "and", "no", "escapes" }; 
    	String[] actual = JRobinRrdStrategy.tokenizeWithQuotingAndEscapes(input, " ", false, "");
    	assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }
    
    @Test
    public void testTWQnENoQNoEWithDEFUnix() throws Exception {
    	if (File.separatorChar != '/') {
    		MockUtil.println("-------- Skipping testTWQnENoQNoEWithDEFUnix since File.separator is not / ------------");
    		MockUtil.println("-------- Be sure to run the tests on Unix too! ---------");
    		return;
    	}
    	String input = "No quote, no escapes, but DEF:test=snmp/42/test.jrb:test:AVERAGE";
    	String[] expected = new String[] { "No", "quote,", "no", "escapes,", "but", "DEF:test=snmp/42/test.jrb:test:AVERAGE" }; 
    	String[] actual = JRobinRrdStrategy.tokenizeWithQuotingAndEscapes(input, " ", false, "");
    	assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }
    
    @Test
    public void testTWQnENoQNoEWithDEFWindows() throws Exception {
    	// This test case inspired by bug #2223
    	if (File.separatorChar != '\\') {
    		MockUtil.println("-------- Skipping testTWQnENoQNoEWithDEFWindows since File.separator is not \\ ------------");
    		MockUtil.println("-------- Be sure to run the tests on Windows too! ---------");
    		return;
    	}
    	String input = "No quote, no escapes, but DEF:test=snmp\\42\\test.jrb:test:AVERAGE";
    	String[] expected = new String[] { "No", "quote,", "no", "escapes,", "but", "DEF:test=snmp\\42\\test.jrb:test:AVERAGE" }; 
    	String[] actual = JRobinRrdStrategy.tokenizeWithQuotingAndEscapes(input, " ", false, "");
    	assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }

    public File createRrdFile() throws Exception {
        String rrdFileBase = "foo";
        String rrdExtension = ".jrb";

        m_fileAnticipator.initialize();
        
        // This is so the RrdUtils.getExtension() call in the strategy works
        Properties properties = new Properties();
        properties.setProperty("org.opennms.rrd.fileExtension", rrdExtension);
        RrdConfig.setProperties(properties);
        
        List<RrdDataSource> dataSources = new ArrayList<RrdDataSource>();
        dataSources.add(new RrdDataSource("bar", "GAUGE", 3000, "U", "U"));
        List<String> rraList = new ArrayList<String>();
        rraList.add("RRA:AVERAGE:0.5:1:2016");
        Object def = m_strategy.createDefinition("hello!", m_fileAnticipator.getTempDir().getAbsolutePath(), rrdFileBase, 300, dataSources, rraList);
        m_strategy.createFile(def);
        
        return m_fileAnticipator.expecting(rrdFileBase + rrdExtension);
    }
}
