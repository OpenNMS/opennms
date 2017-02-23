/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd.jrobin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.rrd.RrdAttributeType;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.MockUtil;
import org.springframework.util.StringUtils;

/**
 * Unit tests for the JrobinRrdStrategy.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class JRobinRrdStrategyTest {
    
    private RrdStrategy<RrdDef,RrdDb> m_strategy;
    private FileAnticipator m_fileAnticipator;
    
    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        // Make sure that AWT headless mode is enabled
        System.setProperty("java.awt.headless", "true");
        
        MockLogAppender.setupLogging(true, "DEBUG");
        
        m_strategy = new JRobinRrdStrategy();

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
    public void testDefWithEscapedCharacters() throws Exception {
        long end = System.currentTimeMillis() / 1000;
        long start = end - (24 * 60 * 60);
        final String[] command = new String[] {
                "--start=" + (start - 300),
                "--end=" + (end + 300),
                "DEF:baz=response/fe80\\:0000\\:0000\\:0000\\:0000\\:0000\\:0000\\:0000\\%5/dns.jrb:bar:AVERAGE",
                "VDEF:avg=baz,AVERAGE",
                "VDEF:min=baz,MIN",
                "VDEF:max=baz,MAX",
                "VDEF:tot=baz,TOTAL",
                "VDEF:nfp=baz,95,PERCENT",
                "PRINT:avg:AVERAGE:\"%le\"",
                "PRINT:min:AVERAGE:\"%le\"",
                "PRINT:max:AVERAGE:\"%le\"",
                "PRINT:tot:AVERAGE:\"%le\"",
                "PRINT:nfp:AVERAGE:\"%le\""
        };

        Throwable t = null;
        try {
        	((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command);
        } catch (final org.jrobin.core.RrdException e) {
        	t = e;
        }
        assertNotNull(t);
        
    	assertTrue("message was " + t.getMessage(), t.getMessage().contains("Could not open "));
    	assertTrue("message was " + t.getMessage(), t.getMessage().contains("fe80:0000:0000:0000:0000:0000:0000:0000%5"));
    }

    @Test
    public void testCreate() throws Exception {
        File rrdFile = createRrdFile();
        
        RrdDb openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        //m_strategy.updateFile(openedFile, "huh?", "N:1,234234");
        
        Sample sample = ((RrdDb) openedFile).createSample();
        sample.set("N:1.234 something not that politically incorrect");
        System.err.println(sample.dump());

        m_strategy.closeFile(openedFile);
    }

    @Test
    public void testUpdate() throws Exception {
        File rrdFile = createRrdFile();
        
        RrdDb openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        m_strategy.updateFile(openedFile, "huh?", "N:1.234234");
        m_strategy.closeFile(openedFile);
    }

    @Test
    public void testSampleSetFloatingPointValueGood() throws Exception {
        File rrdFile = createRrdFile();
        
        RrdDb openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        Sample sample = openedFile.createSample();
        sample.set("N:1.234");
        m_strategy.closeFile(openedFile);
        
        double[] values = sample.getValues();
        assertEquals("values list size", 1, values.length);
        assertEquals("values item 0", 1.234, values[0], 0.0);
    }
    
    @Test
    public void testSampleVDEFPercentile() throws Exception {
        Double[] vals = {
                39.0, 94.0, 95.0, 101.0, 155.0, 262.0, 274.0, 302.0, 319.0, 402.0, 466.0, 468.0, 494.0, 549.0, 550.0, 575.0, 600.0, 615.0, 625.0, 703.0, 729.0, 824.0, 976.0, 1018.0, 1036.0, 1138.0, 1195.0, 1265.0, 1287.0, 1323.0, 1410.0, 1443.0, 1516.0, 1538.0, 1664.0, 1686.0, 1801.0, 1912.0, 1921.0, 1929.0, 1936.0, 1941.0, 1985.0, 2003.0, 2010.0, 2013.0, 2082.0, 2106.0, 2213.0, 2358.0, 2394.0, 2572.0, 2616.0, 2627.0, 2676.0, 2694.0, 2736.0, 2740.0, 2966.0, 3005.0, 3037.0, 3041.0, 3146.0, 3194.0, 3228.0, 3235.0, 3243.0, 3339.0, 3365.0, 3414.0, 3440.0, 3454.0, 3567.0, 3570.0, 3615.0, 3619.0, 3802.0, 3831.0, 3864.0, 4061.0, 4084.0, 4106.0, 4233.0, 4328.0, 4362.0, 4372.0, 4376.0, 4388.0, 4413.0, 4527.0, 4612.0, 4643.0, 4684.0, 4750.0, 4799.0, 4810.0, 4824.0, 4825.0, 4871.0, 4932.0, 5028.0, 5112.0, 5118.0, 5163.0, 5198.0, 5256.0, 5296.0, 5413.0, 5471.0, 5568.0, 5628.0, 5645.0, 5733.0, 5790.0, 5851.0, 5886.0, 5927.0, 5937.0, 6018.0, 6027.0, 6046.0, 6145.0, 6147.0, 6289.0, 6371.0, 6384.0, 6393.0, 6431.0, 6469.0, 6543.0, 6649.0, 6772.0, 6864.0, 6943.0, 7009.0, 7014.0, 7037.0, 7258.0, 7356.0, 7364.0, 7386.0, 7387.0, 7399.0, 7450.0, 7519.0, 7527.0, 7578.0, 7632.0, 7709.0, 7849.0, 7896.0, 7952.0, 7980.0, 8050.0, 8126.0, 8152.0, 8165.0, 8332.0, 8347.0, 8520.0, 8522.0, 8542.0, 8587.0, 8621.0, 8678.0, 8721.0, 8739.0, 8765.0, 8889.0, 8951.0, 8962.0, 9082.0, 9149.0, 9199.0, 9278.0, 9334.0, 9339.0, 9345.0, 9365.0, 9383.0, 9402.0, 9471.0, 9483.0, 9492.0, 9496.0, 9532.0, 9553.0, 9563.0, 9571.0, 9574.0, 100000.0, 120000.0, 150000.0, 200000.0, 500000.0, 1000000.0, 2000000.0, 4000000.0, 8000000.0, 16000000.0
        };
        File rrdFile = createRrdFile();
        RrdDb openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        // This file's step size is 300
        int endTime = (int)(System.currentTimeMillis() / 1000);
        endTime -= (endTime % 300);
        int startTime = endTime - (200 * 300);
        
        // Got to throw away the first sample
        m_strategy.updateFile(openedFile, "huh?", (startTime - 300) + ":" + "0.0");

        int sampleTime = startTime;
        for (double val : vals) {
            m_strategy.updateFile(openedFile, "huh?", sampleTime + ":" + val);
            sampleTime += 300;
        }
        m_strategy.closeFile(openedFile);
        
        String[] command;
        RrdGraphDef graphDef;
        RrdGraph graph;
        RrdGraphInfo info;
        String[] printLines;
        
        command = new String[] {
                "--start=" + (startTime - 300),
                "--end=" + (endTime + 300),
                "DEF:baz=" + rrdFile.getAbsolutePath().replace("\\", "\\\\") + ":bar:AVERAGE",
                "VDEF:avg=baz,AVERAGE",
                "VDEF:min=baz,MIN",
                "VDEF:max=baz,MAX",
                "VDEF:tot=baz,TOTAL",
                "VDEF:nfp=baz,95,PERCENT",
                "PRINT:avg:AVERAGE:\"%le\"",
                "PRINT:min:AVERAGE:\"%le\"",
                "PRINT:max:AVERAGE:\"%le\"",
                "PRINT:tot:AVERAGE:\"%le\"",
                "PRINT:nfp:AVERAGE:\"%le\""
        };
        graphDef = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command);
        graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        info = graph.getRrdGraphInfo();
        assertNotNull("graph info object", info);
        
        printLines = info.getPrintLines();
        assertNotNull("graph printLines - DEF", printLines);
        assertEquals("graph printLines - DEF size", 5, printLines.length);
        assertEquals("graph printLines - DEF item 0", "1.649453e+05", printLines[0]);
        assertEquals("graph printLines - DEF item 1", "3.900000e+01", printLines[1]);
        assertEquals("graph printLines - DEF item 2", "1.600000e+07", printLines[2]);
        assertEquals("graph printLines - DEF item 3", "9.896721e+09", printLines[3]);
        assertEquals("graph printLines - DEF item 4", "9.574000e+03", printLines[4]);

        // Now do it with a CDEF
        command = new String[] {
                "--start=" + (startTime - 300),
                "--end=" + (endTime + 300),
                "DEF:baz=" + rrdFile.getAbsolutePath().replace("\\", "\\\\") + ":bar:AVERAGE",
                "CDEF:bazX1=baz,1,*",
                "CDEF:bazX1P0=bazX1,0,+",
                "VDEF:avg=bazX1,AVERAGE",
                "VDEF:min=bazX1,MIN",
                "VDEF:max=bazX1,MAX",
                "VDEF:tot=bazX1,TOTAL",
                "VDEF:nfp=bazX1,95,PERCENT",
                "VDEF:nfp2=bazX1P0,95,PERCENT",
                "PRINT:avg:AVERAGE:\"%le\"",
                "PRINT:min:AVERAGE:\"%le\"",
                "PRINT:max:AVERAGE:\"%le\"",
                "PRINT:tot:AVERAGE:\"%le\"",
                "PRINT:nfp:AVERAGE:\"%le\"",
                "PRINT:nfp2:AVERAGE:\"%le\""
        };
        graphDef = ((JRobinRrdStrategy)m_strategy).createGraphDef(new File(""), command);
        graph = new RrdGraph(graphDef);
        assertNotNull("graph object", graph);
        
        info = graph.getRrdGraphInfo();
        assertNotNull("graph info object", info);
        
        printLines = info.getPrintLines();
        assertNotNull("graph printLines - CDEF", printLines);
        assertEquals("graph printLines - CDEF size", 6, printLines.length);
        assertEquals("graph printLines - CDEF item 0", "1.649453e+05", printLines[0]);
        assertEquals("graph printLines - CDEF item 1", "3.900000e+01", printLines[1]);
        assertEquals("graph printLines - CDEF item 2", "1.600000e+07", printLines[2]);
        assertEquals("graph printLines - CDEF item 3", "9.896721e+09", printLines[3]);
        assertEquals("graph printLines - CDEF item 4", "9.574000e+03", printLines[4]);
        assertEquals("graph printLines - CDEF item 5", "9.574000e+03", printLines[5]);
        
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
        
        RrdDb openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        Sample sample = openedFile.createSample();
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
        
        RrdDb openedFile = m_strategy.openFile(rrdFile.getAbsolutePath());
        
        Sample sample = openedFile.createSample();
        
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

    @Test
    public void testFontArguments() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[] {
                "--start=" + start,
                "--end=" + end,
                "--font=DEFAULT:16",
                "--font", "TITLE:18:",
                "CDEF:something=1",
                "PRINT:something:AVERAGE:\"%le\""
        };

        JRobinRrdGraphDetails graphDetails = (JRobinRrdGraphDetails) m_strategy.createGraphReturnDetails(StringUtils.arrayToDelimitedString(command, " "), new File(""));
        assertNotNull("graph details object", graphDetails);
    }

    @Test
    public void testHRule() throws Exception {
        long end = System.currentTimeMillis();
        long start = end - (24 * 60 * 60 * 1000);
        String[] command = new String[]{
            "--start=" + start,
            "--end=" + end,
            "--font=DEFAULT:16",
            "--font", "TITLE:18:",
            "HRULE:2#ff0000"
        };

        JRobinRrdGraphDetails graphDetails = (JRobinRrdGraphDetails) m_strategy.createGraphReturnDetails(StringUtils.arrayToDelimitedString(command, " "), new File(""));
        assertNotNull("graph details object", graphDetails);
    }

    public File createRrdFile() throws Exception {
        String rrdFileBase = "foo";

        m_fileAnticipator.initialize();
        String rrdExtension = m_strategy.getDefaultFileExtension();
        
        List<RrdDataSource> dataSources = new ArrayList<RrdDataSource>();
        dataSources.add(new RrdDataSource("bar", RrdAttributeType.GAUGE, 3000, "U", "U"));
        List<String> rraList = new ArrayList<String>();
        rraList.add("RRA:AVERAGE:0.5:1:2016");
        RrdDef def = m_strategy.createDefinition("hello!", m_fileAnticipator.getTempDir().getAbsolutePath(), rrdFileBase, 300, dataSources, rraList);
        m_strategy.createFile(def, null);
        
        return m_fileAnticipator.expecting(rrdFileBase + rrdExtension);
    }
}
