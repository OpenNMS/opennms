//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 12: Remove command.input.dir. - dj@opennms.org
// 2007 Apr 08: Use Resources instead of Files. - dj@opennms.org
// 2007 Apr 07: Refactor to use setters and an afterPropertiesSet method for
//              configuration and initialization instead of configuration
//              passed to the constructor. - dj@opennms.org 
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class PropertiesGraphDaoTest extends TestCase {
    private static final Map<String, Resource> s_emptyMap = new HashMap<String, Resource>();
    
    final static String s_prefab =
            "command.prefix=foo\n"
            + "output.mime=foo\n"
            + "\n"
            + "reports=mib2.HCbits, mib2.bits, mib2.discards\n"
            + "\n"
            + "report.mib2.HCbits.name=Bits In/Out\n"
            + "report.mib2.HCbits.columns=ifHCInOctets,ifHCOutOctets\n"
            + "report.mib2.HCbits.type=interface\n"
            + "report.mib2.HCbits.externalValues=ifSpeed\n"
            + "report.mib2.HCbits.suppress=mib2.bits\n"
            + "report.mib2.HCbits.command=--title=\"Bits In/Out (High Speed)\" \\\n"
            + " DEF:octIn={rrd1}:ifHCInOctets:AVERAGE \\\n"
            + " DEF:octOut={rrd2}:ifHCOutOctets:AVERAGE \\\n"
            + " CDEF:bitsIn=octIn,8,* \\\n"
            + " CDEF:bitsOut=octOut,8,* \\\n"
            + " CDEF:totBits=octIn,octOut,+,8,* \\\n"
            + " AREA:totBits#00ff00:\"Total\" \\\n"
            + " GPRINT:totBits:AVERAGE:\" Avg  \\\\: %8.2lf %s\\\\n\" \\\n"
            + " LINE2:bitsIn#0000ff:\"Bits In\" \\\n"
            + " GPRINT:bitsIn:AVERAGE:\" Avg  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsIn:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsIn:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" \\\n"
            + " LINE2:bitsOut#ff0000:\"Bits Out\" \\\n"
            + " GPRINT:bitsOut:AVERAGE:\"Avg  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsOut:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsOut:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"\n"
            + "\n"
            + "report.mib2.bits.name=Bits In/Out\n"
            + "report.mib2.bits.columns=ifInOctets,ifOutOctets\n"
            + "report.mib2.bits.type=interface\n"
            + "report.mib2.bits.externalValues=ifSpeed\n"
            + "report.mib2.bits.command=--title=\"Bits In/Out\" \\\n"
            + " DEF:octIn={rrd1}:ifInOctets:AVERAGE \\\n"
            + " DEF:octOut={rrd2}:ifOutOctets:AVERAGE \\\n"
            + " CDEF:bitsIn=octIn,8,* \\\n"
            + " CDEF:bitsOut=octOut,8,* \\\n"
            + " CDEF:totBits=octIn,octOut,+,8,* \\\n"
            + " AREA:totBits#00ff00:\"Total\" \\\n"
            + " GPRINT:totBits:AVERAGE:\" Avg  \\\\: %8.2lf %s\\\\n\" \\\n"
            + " LINE2:bitsIn#0000ff:\"Bits In\" \\\n"
            + " GPRINT:bitsIn:AVERAGE:\" Avg  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsIn:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsIn:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" \\\n"
            + " LINE2:bitsOut#ff0000:\"Bits Out\" \\\n"
            + " GPRINT:bitsOut:AVERAGE:\"Avg  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsOut:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:bitsOut:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"\n"
            + "\n"
            + "report.mib2.discards.name=Discards In/Out\n"
            + "report.mib2.discards.columns=ifInDiscards,ifOutDiscards\n"
            + "report.mib2.discards.type=interface\n"
            + "report.mib2.discards.propertiesValues=ifSpeed\n"
            + "report.mib2.discards.command=--title=\"Discards In/Out\" \\\n"
            + " DEF:octIn={rrd1}:ifInDiscards:AVERAGE \\\n"
            + " DEF:octOut={rrd2}:ifOutDiscards:AVERAGE \\\n"
            + " LINE2:octIn#0000ff:\"Discards In\" \\\n"
            + " GPRINT:octIn:AVERAGE:\" Avg  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:octIn:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:octIn:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" \\\n"
            + " LINE2:octOut#ff0000:\"Discards Out\" \\\n"
            + " GPRINT:octOut:AVERAGE:\"Avg  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:octOut:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
            + " GPRINT:octOut:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"\n";
    
    private static final String s_adhoc =
        "command.prefix=${install.rrdtool.bin} graph - --imgformat PNG --start {1} --end {2}\n"
        + "output.mime=image/png\n"
        + "adhoc.command.title=--title=\"{3}\"\n"
        + "adhoc.command.ds=DEF:{4}={0}:{5}:{6}\n"
        + "adhoc.command.graphline={7}:{4}#{8}:\"{9}\"\n";
    
    private static final String s_responsePrefab =
        "command.prefix=foo\n"
        + "output.mime=foo\n"
        + "\n"
        + "reports=icmp\n"
        + "\n"
        + "report.icmp.name=ICMP\n"
        + "report.icmp.columns=icmp\n"
        + "report.icmp.type=responseTime\n"
        + "report.icmp.command=--title=\"ICMP Response Time\" \\\n"
        + "  --vertical-label=\"Seconds\" \\\n"
        + "  DEF:rtMicro={rrd1}:icmp:AVERAGE \\\n"
        + "  CDEF:rt=rtMicro,1000000,/ \\\n"
        + "  LINE1:rt#0000ff:\"Response Time\" \\\n"
        + "  GPRINT:rt:AVERAGE:\" Avg  \\: %8.2lf %s\" \\\n"
        + "  GPRINT:rt:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
        + "  GPRINT:rt:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"";

    private Map<String, PrefabGraph> m_graphs;

    private PropertiesGraphDao m_dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
        
        m_dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        ByteArrayInputStream in = new ByteArrayInputStream(s_prefab.getBytes());
        m_dao.loadProperties("performance", in);
        
        PrefabGraphType type = m_dao.findByName("performance");
        assertNotNull("could not get performance prefab graph type", type);

        m_graphs = type.getReportMap();
        assertNotNull("report map shouldn't be null", m_graphs);
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    public void testCompareToLessThan() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        PrefabGraph discards = m_graphs.get("mib2.discards");

        assertEquals("compareTo", -1, bits.compareTo(discards));
    }
    public void testCompareToGreaterThan() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        PrefabGraph discards = m_graphs.get("mib2.discards");

        assertEquals("compareTo", 1, discards.compareTo(bits));
    }
    public void testCompareToEquals() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        PrefabGraph bits2 = m_graphs.get("mib2.bits");

        assertEquals("compareTo", 0, bits.compareTo(bits2));
    }

    public void testGetName() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertEquals("getName", "mib2.bits", bits.getName());
    }

    public void testGetTitle() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertEquals("getTitle", "Bits In/Out", bits.getTitle());
    }

    public void testGetOrder() {
        PrefabGraph bits = m_graphs.get("mib2.HCbits");
        assertEquals("getOrder", 0, bits.getOrder());
    }

    public void testGetColumns() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        String[] columns = bits.getColumns();
        assertEquals("getColumns().length", 2, columns.length);
        assertEquals("getColumns()[0]", "ifInOctets", columns[0]);
        assertEquals("getColumns()[1]", "ifOutOctets", columns[1]);
    }

    public void testGetCommand() {
        String expectedCommand = "--title=\"Bits In/Out\" "
                + "DEF:octIn={rrd1}:ifInOctets:AVERAGE "
                + "DEF:octOut={rrd2}:ifOutOctets:AVERAGE "
                + "CDEF:bitsIn=octIn,8,* " + "CDEF:bitsOut=octOut,8,* "
                + "CDEF:totBits=octIn,octOut,+,8,* "
                + "AREA:totBits#00ff00:\"Total\" "
                + "GPRINT:totBits:AVERAGE:\" Avg  \\: %8.2lf %s\\n\" "
                + "LINE2:bitsIn#0000ff:\"Bits In\" "
                + "GPRINT:bitsIn:AVERAGE:\" Avg  \\: %8.2lf %s\" "
                + "GPRINT:bitsIn:MIN:\"Min  \\: %8.2lf %s\" "
                + "GPRINT:bitsIn:MAX:\"Max  \\: %8.2lf %s\\n\" "
                + "LINE2:bitsOut#ff0000:\"Bits Out\" "
                + "GPRINT:bitsOut:AVERAGE:\"Avg  \\: %8.2lf %s\" "
                + "GPRINT:bitsOut:MIN:\"Min  \\: %8.2lf %s\" "
                + "GPRINT:bitsOut:MAX:\"Max  \\: %8.2lf %s\\n\"";

        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertEquals("getCommand", expectedCommand, bits.getCommand());
    }

    public void testGetExternalValues() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        String[] values = bits.getExternalValues();
        assertEquals("getExternalValues().length", 1, values.length);
        assertEquals("getExternalValues()[0]", "ifSpeed", values[0]);
    }

    public void testGetExternalValuesEmpty() {
        PrefabGraph discards = m_graphs.get("mib2.discards");
        assertEquals("getExternalValues().length", 0,
                     discards.getExternalValues().length);
    }

    public void testGetPropertiesValues() {
        PrefabGraph discards = m_graphs.get("mib2.discards");
        String[] values = discards.getPropertiesValues();
        assertEquals("getPropertiesValues().length", 1, values.length);
        assertEquals("getPropertiesValues()[0]", "ifSpeed", values[0]);
    }

    public void testGetPropertiesValuesEmpty() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertEquals("getPropertiesValues().length", 0,
                     bits.getPropertiesValues().length);
    }

    public void testGetTypes() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertNotNull("getTypes", bits.getTypes());
        assertEquals("getTypes count", 1, bits.getTypes().length);
        assertEquals("getTypes 1", "interface", bits.getTypes()[0]);
    }

    public void testGetDescription() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertEquals("getDescription", null, bits.getDescription());
    }

    public void testLoadSnmpGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("snmp-graph.properties"));
    }

    public void testLoadSnmpAdhocGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadAdhocProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("snmp-adhoc-graph.properties"));
    }

    public void testLoadResponseTimeGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("response-graph.properties"));
    }

    public void testLoadResponseTimeAdhocGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadAdhocProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("response-adhoc-graph.properties"));
    }
    
    public void testPrefabPropertiesReload() throws Exception {
        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();
        
        try {
            File f = fa.tempFile("snmp-graph.properties");
            
            Writer writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            // Don't include mib2.discards in the reports line
            String noDiscards = s_prefab.replace(", mib2.discards", "");
            writer.write(noDiscards);
            writer.close();
            
            HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
            perfConfig.put("performance", new FileSystemResource(f));
            PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
            PrefabGraphType type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type", type);
            
            assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
            assertNull("could get mib2.discards report, but shouldn't have been able to", type.getQuery("mib2.discards"));

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             *  
             *  This also happens to be long enough for 
             *  FileReloadContainer.DEFAULT_RELOAD_CHECK_INTERVAL
             *  to pass by.
             */
            Thread.sleep(1100);

            writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            writer.write(s_prefab);
            writer.close();
            
            type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type after rewriting config file", type);
            assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
            assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
    }
    
    public void testPrefabPropertiesReloadBad() throws Exception {
        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();
        
        MockLogAppender.setupLogging(false, "DEBUG");
        
        try {
            File f = fa.tempFile("snmp-graph.properties");

            Writer writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            writer.write(s_prefab);
            writer.close();

            HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
            perfConfig.put("performance", new FileSystemResource(f));
            PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
            PrefabGraphType type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type", type);
            
            assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
            assertNotNull("could not get mib2.discards report", type.getQuery("mib2.discards"));

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             *  
             *  This also happens to be long enough for 
             *  FileReloadContainer.DEFAULT_RELOAD_CHECK_INTERVAL
             *  to pass by.
             */
            Thread.sleep(1100);

            writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            // Don't include the reports line at all so we get an error
            String noReports = s_prefab.replace("reports=mib2.HCbits, mib2.bits, mib2.discards", "");
            writer.write(noReports);
            writer.close();
            
            type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type after rewriting config file", type);
            assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
            assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
        
        LoggingEvent[] events = MockLogAppender.getEvents();
        assertNotNull("logged event list was null", events);
        assertEquals("should only have received two logged events", 2, events.length);
        assertEquals("should have received an ERROR event" + events[0], Level.ERROR, events[0].getLevel());
        assertEquals("should have received an INFO event" + events[1], Level.INFO, events[1].getLevel());
        
        MockLogAppender.resetEvents();
    }

    
    public void testAdhocPropertiesReload() throws Exception {
        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();
        
        try {
            File f = fa.tempFile("snmp-adhoc-graph.properties");
            
            Writer writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            // Set the image type to image/cheeesy
            String cheesy = s_adhoc.replace("image/png", "image/cheesy");
            writer.write(cheesy);
            writer.close();
            
            HashMap<String, Resource> adhocConfig = new HashMap<String, Resource>();
            adhocConfig.put("performance", new FileSystemResource(f));
            PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, adhocConfig);
            AdhocGraphType type = dao.findAdhocByName("performance");
            assertNotNull("could not get performance adhoc graph type", type);
            assertEquals("image type isn't correct", "image/cheesy", type.getOutputMimeType());

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             *  
             *  This also happens to be long enough for 
             *  FileReloadContainer.DEFAULT_RELOAD_CHECK_INTERVAL
             *  to pass by.
             */
            Thread.sleep(1100);

            writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            writer.write(s_adhoc);
            writer.close();
            
            type = dao.findAdhocByName("performance");
            assertNotNull("could not get performance adhoc graph type", type);
            assertEquals("image type isn't correct", "image/png", type.getOutputMimeType());
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
    }
    
    public void testNoType() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        String ourConfig = s_responsePrefab.replaceAll("report.icmp.type=responseTime", "");
        ByteArrayInputStream in = new ByteArrayInputStream(ourConfig.getBytes());
        dao.loadProperties("response", in);
        
        PrefabGraphType type = dao.findByName("response");
        assertNotNull("could not get response prefab graph type", type);

        PrefabGraph graph = type.getQuery("icmp");
        assertNotNull("could not get icmp response prefab graph type", graph);
        
        assertNotNull("graph type list should not be null", graph.getTypes());
        assertEquals("graph type was not specified the list should be empty", 0, graph.getTypes().length);
        
        assertFalse("should not have responseTime type", graph.hasMatchingType("responseTime"));
    }
    
    public void testOneType() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        ByteArrayInputStream in = new ByteArrayInputStream(s_responsePrefab.getBytes());
        dao.loadProperties("response", in);
        
        PrefabGraphType type = dao.findByName("response");
        assertNotNull("could not get response prefab graph type", type);

        PrefabGraph graph = type.getQuery("icmp");
        assertNotNull("could not get icmp response prefab graph type", graph);
        
        assertNotNull("graph type list should not be null", graph.getTypes());
        assertEquals("graph type was not specified the list should be empty", 1, graph.getTypes().length);
        assertEquals("graph type 1", "responseTime", graph.getTypes()[0]);

        assertTrue("should have responseTime type", graph.hasMatchingType("responseTime"));
        assertFalse("should not have distributedStatus type", graph.hasMatchingType("distributedStatus"));

        assertTrue("should have responseTime or distributedStatus type", graph.hasMatchingType("responseTime", "distributedStatus"));
    }

    public void testTwoTypes() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        String ourConfig = s_responsePrefab.replaceAll("report.icmp.type=responseTime", "report.icmp.type=responseTime, distributedStatus");
        ByteArrayInputStream in = new ByteArrayInputStream(ourConfig.getBytes());
        dao.loadProperties("response", in);
        
        PrefabGraphType type = dao.findByName("response");
        assertNotNull("could not get response prefab graph type", type);

        PrefabGraph graph = type.getQuery("icmp");
        assertNotNull("could not get icmp response prefab graph type", graph);
        
        assertNotNull("graph type should not be null", graph.getTypes());
        assertEquals("graph type count", 2, graph.getTypes().length);
        assertEquals("graph type 1", "responseTime", graph.getTypes()[0]);
        assertEquals("graph type 2", "distributedStatus", graph.getTypes()[1]);
        
        assertTrue("should have responseTime type", graph.hasMatchingType("responseTime"));
        assertTrue("should have distributedStatus type", graph.hasMatchingType("distributedStatus"));
    }
    
    public void testGetPrefabGraphsForResource() {
        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interface");
        HashSet<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(0);
        attributes.add(new RrdGraphAttribute("ifInOctets", "", ""));
        attributes.add(new RrdGraphAttribute("ifOutOctets", "", ""));
        attributes.add(new ExternalValueAttribute("ifSpeed", ""));
        OnmsResource resource = new OnmsResource("node", "1", resourceType, attributes);
        PrefabGraph[] graphs = m_dao.getPrefabGraphsForResource(resource);
        assertEquals("prefab graph array size", 1, graphs.length);
        assertEquals("prefab graph[0] name", "mib2.bits", graphs[0].getName());
    }
    
    public void testGetPrefabGraphsForResourceWithSuppress() {
        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interface");
        HashSet<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(0);
        attributes.add(new RrdGraphAttribute("ifInOctets", "", ""));
        attributes.add(new RrdGraphAttribute("ifOutOctets", "", ""));
        attributes.add(new RrdGraphAttribute("ifHCInOctets", "", ""));
        attributes.add(new RrdGraphAttribute("ifHCOutOctets", "", ""));
        attributes.add(new ExternalValueAttribute("ifSpeed", ""));
        OnmsResource resource = new OnmsResource("node", "1", resourceType, attributes);
        PrefabGraph[] graphs = m_dao.getPrefabGraphsForResource(resource);
        assertEquals("prefab graph array size", 1, graphs.length);
        assertEquals("prefab graph[0] name", "mib2.HCbits", graphs[0].getName());
    }
    
    public void testGetPrefabGraphsForResourceWithSuppressUnused() {
        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interface");
        HashSet<OnmsAttribute> attributes = new HashSet<OnmsAttribute>(0);
        attributes.add(new RrdGraphAttribute("ifHCInOctets", "", ""));
        attributes.add(new RrdGraphAttribute("ifHCOutOctets", "", ""));
        attributes.add(new ExternalValueAttribute("ifSpeed", ""));
        OnmsResource resource = new OnmsResource("node", "1", resourceType, attributes);
        PrefabGraph[] graphs = m_dao.getPrefabGraphsForResource(resource);
        assertEquals("prefab graph array size", 1, graphs.length);
        assertEquals("prefab graph[0] name", "mib2.HCbits", graphs[0].getName());
    }

    public PropertiesGraphDao createPropertiesGraphDao(Map<String, Resource> prefabConfigs, Map<String, Resource> adhocConfigs) throws IOException {
        PropertiesGraphDao dao = new PropertiesGraphDao();
        
        dao.setPrefabConfigs(prefabConfigs);
        dao.setAdhocConfigs(adhocConfigs);
        dao.afterPropertiesSet();
        
        return dao;
    }

}
