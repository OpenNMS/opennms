/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.FileReloadContainer;
import org.opennms.netmgt.dao.support.PropertiesGraphDao.PrefabGraphTypeDao;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.FileAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.ObjectRetrievalFailureException;

public class PropertiesGraphDaoTest {
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

    private static final String s_baseIncludePrefab = 
        "command.prefix=foo\n" +
        "output.mime=image/png\n" +
        "reports=\n" + //Empty for a simple base prefab, with only graphs included from the sub directory
        "include.directory=snmp-graph.properties.d\n" +
        "include.directory.rescan=1000\n"; //1 second rescan time, for efficient testing
    
    private static final String s_separateBitsGraph =
        "report.id=mib2.bits\n"
        + "report.name=Bits In/Out\n"
        + "report.columns=ifInOctets,ifOutOctets\n"
        + "report.type=interface\n"
        + "report.externalValues=ifSpeed\n"
        + "report.command=--title=\"Bits In/Out\"\n"; //Just a title is enough for testing
        
    private static final String s_separateHCBitsGraph =
        "report.id=mib2.HCbits\n"
        + "report.name=Bits In/Out\n"
        + "report.columns=ifHCInOctets,ifHCOutOctets\n"
        + "report.type=interface\n"
        + "report.externalValues=ifSpeed\n"
        + "report.suppress=mib2.bits\n"
        + "report.command=--title=\"Bits In/Out (High Speed)\"\n"; //Just a title is enough for testing

    private static final String s_separateErrorsGraph =
        "report.id=mib2.errors\n"
        +"report.name=Errors In/Out\n"
        + "report.columns=ifIfErrors,ifOutErrors\n"
        + "report.type=interface\n"
        + "report.propertiesValues=ifSpeed\n"
        + "report.command=--title=\"Erros In/Out\"\n";

    private static final String s_includedMultiGraph1 =
        "reports=mib2.discards,mib2.errors\n"
        +"report.mib2.discards.name=Discards In/Out\n"
        + "report.mib2.discards.columns=ifInDiscards,ifOutDiscards\n"
        + "report.mib2.discards.type=interface\n"
        + "report.mib2.discards.propertiesValues=ifSpeed\n"
        + "report.mib2.discards.command=--title=\"Discards In/Out\"\n"
        + "\n"
        + "report.mib2.errors.name=Errors In/Out\n"
        + "report.mib2.errors.columns=ifInErrors,ifOutErrors\n"
        + "report.mib2.errors.type=interface\n"
        + "report.mib2.errors.propertiesValues=ifSpeed\n"
        + "report.mib2.errors.command=--title=\"Discards In/Out\"\n";
    
    private static final String s_includedMultiGraph2 =
        "reports=mib2.bits,mib2.HCbits\n"
        + "report.mib2.bits.name=Bits In/Out\n"
        + "report.mib2.bits.columns=ifInOctets,ifOutOctets\n"
        + "report.mib2.bits.type=interface\n"
        + "report.mib2.bits.externalValues=ifSpeed\n"
        + "report.mib2.bits.command=--title=\"Bits In/Out\"\n"
        + "\n"
        + "report.mib2.HCbits.name=Bits In/Out\n"
        + "report.mib2.HCbits.columns=ifHCInOctets,ifHCOutOctets\n"
        + "report.mib2.HCbits.type=interface\n"
        + "report.mib2.HCbits.externalValues=ifSpeed\n"
        + "report.mib2.HCbits.suppress=mib2.bits\n"
        + "report.mib2.HCbits.command=--title=\"Bits In/Out (High Speed)\"\n"; //Just a title is enough for testing

    //A base file, with an include, that defines a single graph, with some unusual and incorrect details
    // that will be overridden by the included graph
    private static final String s_mib2bitsBasePrefab = 
        "command.prefix=foo\n"
        + "output.mime=image/png\n" 
        + "include.directory=snmp-graph.properties.d\n"
        + "reports=mib2.bits\n"
        + "report.mib2.bits.name=Wrong Name\n"
        + "report.mib2.bits.columns=wrongColumn1,wrongColumn2\n"
        + "report.mib2.bits.type=node\n"
        + "report.mib2.bits.externalValues=fooBar\n"
        + "report.mib2.bits.command=--title=\"Wrong Title\"\n";
    
    /**
     * A prefab graphs config with just one of the reports broken in a subtle way (mib2.bits, with it's "name" property spelled "nmae"
     * Used to test that the rest of the reports load as expected
     * (Actual report details trimmed for space
     */
    final static String s_partlyBorkedPrefab =
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
        + "report.mib2.HCbits.command=--title=\"Bits In/Out (High Speed)\" \n"
        + "\n"
        + "report.mib2.bits.nmae=Bits In/Out\n"
        + "report.mib2.bits.columns=ifInOctets,ifOutOctets\n"
        + "report.mib2.bits.type=interface\n"
        + "report.mib2.bits.externalValues=ifSpeed\n"
        + "report.mib2.bits.command=--title=\"Bits In/Out\" \n"
        + "\n"
        + "report.mib2.discards.name=Discards In/Out\n"
        + "report.mib2.discards.columns=ifInDiscards,ifOutDiscards\n"
        + "report.mib2.discards.type=interface\n"
        + "report.mib2.discards.propertiesValues=ifSpeed\n"
        + "report.mib2.discards.command=--title=\"Discards In/Out\" \n";

    private Map<String, FileReloadContainer<PrefabGraph>> m_graphs;

    private PropertiesGraphDao m_dao;
    
    private boolean testSpecificLoggingTest = false;

    private FileAnticipator m_fileAnticipator = null;
    private FileOutputStream m_outputStream = null;
    private Writer m_writer = null;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);
        
        m_dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        ByteArrayInputStream in = new ByteArrayInputStream(s_prefab.getBytes());
        m_dao.loadProperties("performance", in);
        
        PrefabGraphTypeDao type = m_dao.findPrefabGraphTypeDaoByName("performance");
        assertNotNull("could not get performance prefab graph type", type);

        m_graphs = type.getReportMap();
        assertNotNull("report map shouldn't be null", m_graphs);
        
        m_fileAnticipator = new FileAnticipator();
    }
    
    @After
    public void tearDown() throws Exception {
    	IOUtils.closeQuietly(m_writer);
    	IOUtils.closeQuietly(m_outputStream);

    	// For Windows, see
    	// http://stackoverflow.com/a/4213208/149820 for details
    	m_writer = null;
    	m_outputStream = null;
    	System.gc();

        //Allow an individual test to tell us to ignore the logging assertion
        // e.g. if they're testing with assertLogAtLevel
        if(!testSpecificLoggingTest) {
            MockLogAppender.assertNoWarningsOrGreater();
        }

    	m_fileAnticipator.deleteExpected();
    	m_fileAnticipator.tearDown();
        MockLogAppender.resetEvents();
    }

    @Test
    public void testCompareToLessThan() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();
        PrefabGraph discards = m_graphs.get("mib2.discards").getObject();;

        assertEquals("compareTo", -1, bits.compareTo(discards));
    }

    @Test
    public void testCompareToGreaterThan() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        PrefabGraph discards = m_graphs.get("mib2.discards").getObject();;

        assertEquals("compareTo", 1, discards.compareTo(bits));
    }

    @Test
    public void testCompareToEquals() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        PrefabGraph bits2 = m_graphs.get("mib2.bits").getObject();;

        assertEquals("compareTo", 0, bits.compareTo(bits2));
    }

    @Test
    public void testGetName() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        assertEquals("getName", "mib2.bits", bits.getName());
    }

    @Test
    public void testGetTitle() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        assertEquals("getTitle", "Bits In/Out", bits.getTitle());
    }

    @Test
    public void testGetOrder() {
        PrefabGraph bits = m_graphs.get("mib2.HCbits").getObject();;
        assertEquals("getOrder", 0, bits.getOrder());
    }

    @Test
    public void testGetColumns() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        String[] columns = bits.getColumns();
        assertEquals("getColumns().length", 2, columns.length);
        assertEquals("getColumns()[0]", "ifInOctets", columns[0]);
        assertEquals("getColumns()[1]", "ifOutOctets", columns[1]);
    }

    @Test
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

        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        assertEquals("getCommand", expectedCommand, bits.getCommand());
    }

    @Test
    public void testGetExternalValues() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        String[] values = bits.getExternalValues();
        assertEquals("getExternalValues().length", 1, values.length);
        assertEquals("getExternalValues()[0]", "ifSpeed", values[0]);
    }

    @Test
    public void testGetExternalValuesEmpty() {
        PrefabGraph discards = m_graphs.get("mib2.discards").getObject();;
        assertEquals("getExternalValues().length", 0,
                     discards.getExternalValues().length);
    }

    @Test
    public void testGetPropertiesValues() {
        PrefabGraph discards = m_graphs.get("mib2.discards").getObject();;
        String[] values = discards.getPropertiesValues();
        assertEquals("getPropertiesValues().length", 1, values.length);
        assertEquals("getPropertiesValues()[0]", "ifSpeed", values[0]);
    }

    @Test
    public void testGetPropertiesValuesEmpty() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        assertEquals("getPropertiesValues().length", 0,
                     bits.getPropertiesValues().length);
    }

    @Test
    public void testGetTypes() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        assertNotNull("getTypes", bits.getTypes());
        assertEquals("getTypes count", 1, bits.getTypes().length);
        assertEquals("getTypes 1", "interface", bits.getTypes()[0]);
    }

    @Test
    public void testGetDescription() {
        PrefabGraph bits = m_graphs.get("mib2.bits").getObject();;
        assertEquals("getDescription", null, bits.getDescription());
    }

    @Test
    public void testLoadSnmpGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadProperties("foo", new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("snmp-graph.properties")));
    }

    @Test
    public void testLoadSnmpAdhocGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadAdhocProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("snmp-adhoc-graph.properties"));
    }

    @Test
    public void testLoadResponseTimeGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("response-graph.properties"));
    }

    @Test
    public void testLoadResponseTimeAdhocGraphProperties() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadAdhocProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("response-adhoc-graph.properties"));
    }
    
    @Test
    public void testPrefabPropertiesReload() throws Exception {
        File f = m_fileAnticipator.tempFile("snmp-graph.properties");
        
        m_outputStream = new FileOutputStream(f);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        // Don't include mib2.discards in the reports line
        String noDiscards = s_prefab.replace(", mib2.discards", "");
        m_writer.write(noDiscards);
        m_writer.close();
        m_outputStream.close();
        
        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(f));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
        PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("performance");
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

        m_outputStream = new FileOutputStream(f);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_prefab);
        m_writer.close();
        m_outputStream.close();
        
        type = dao.findPrefabGraphTypeDaoByName("performance");
        assertNotNull("could not get performance prefab graph type after rewriting config file", type);
        assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
        assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
    }
    
    @Test
    public void testPrefabPropertiesReloadBad() throws Exception {
        MockLogAppender.setupLogging(false, "DEBUG");
        testSpecificLoggingTest = true;

        File f = m_fileAnticipator.tempFile("snmp-graph.properties");

        m_outputStream = new FileOutputStream(f);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_prefab);
        m_writer.close();
        m_outputStream.close();

        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(f));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
        PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("performance");
        assertNotNull("could not get performance prefab graph type", type);
        
        assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
        assertNotNull("could not get mib2.discards report", type.getQuery("mib2.discards"));

        Thread.sleep(1100);

        m_outputStream = new FileOutputStream(f);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        // Don't include the reports line at all so we get an error
        String noReports = s_prefab.replace("reports=mib2.HCbits, mib2.bits, mib2.discards", "");
        m_writer.write(noReports);
        m_writer.close();
        m_outputStream.close();
        
        type = dao.findPrefabGraphTypeDaoByName("performance");

        assertNotNull("could not get performance prefab graph type after rewriting config file", type);
        assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
        assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
        
        MockLogAppender.assertLogMatched(Level.ERROR, "Could not reload configuration");
    }

    @Test
    public void testAdhocPropertiesReload() throws Exception {
        File f = m_fileAnticipator.tempFile("snmp-adhoc-graph.properties");
        
        m_outputStream = new FileOutputStream(f);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        // Set the image type to image/cheeesy
        String cheesy = s_adhoc.replace("image/png", "image/cheesy");
        m_writer.write(cheesy);
        m_writer.close();
        m_outputStream.close();
        
        HashMap<String, Resource> adhocConfig = new HashMap<String, Resource>();
        adhocConfig.put("performance", new FileSystemResource(f));
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, adhocConfig);
        AdhocGraphType type = dao.findAdhocGraphTypeByName("performance");
        assertNotNull("could not get performance adhoc graph type", type);
        assertEquals("image type isn't correct", "image/cheesy", type.getOutputMimeType());

        Thread.sleep(1100);

        m_outputStream = new FileOutputStream(f);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_adhoc);
        m_writer.close();
        m_outputStream.close();
        
        type = dao.findAdhocGraphTypeByName("performance");
        assertNotNull("could not get performance adhoc graph type", type);
        assertEquals("image type isn't correct", "image/png", type.getOutputMimeType());
    }
    
    @Test
    public void testNoType() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        String ourConfig = s_responsePrefab.replaceAll("report.icmp.type=responseTime", "");
        ByteArrayInputStream in = new ByteArrayInputStream(ourConfig.getBytes());
        dao.loadProperties("response", in);
        
        PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("response");
        assertNotNull("could not get response prefab graph type", type);

        PrefabGraph graph = type.getQuery("icmp");
        assertNotNull("could not get icmp response prefab graph type", graph);
        
        assertNotNull("graph type list should not be null", graph.getTypes());
        assertEquals("graph type was not specified the list should be empty", 0, graph.getTypes().length);
        
        assertFalse("should not have responseTime type", graph.hasMatchingType("responseTime"));
    }
    
    @Test
    public void testOneType() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        ByteArrayInputStream in = new ByteArrayInputStream(s_responsePrefab.getBytes());
        dao.loadProperties("response", in);
        
        PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("response");
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

    @Test
    public void testTwoTypes() throws Exception {
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        String ourConfig = s_responsePrefab.replaceAll("report.icmp.type=responseTime", "report.icmp.type=responseTime, distributedStatus");
        ByteArrayInputStream in = new ByteArrayInputStream(ourConfig.getBytes());
        dao.loadProperties("response", in);
        
        PrefabGraphTypeDao type = dao.findPrefabGraphTypeDaoByName("response");
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
    
    @Test
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
    
    @Test
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
    
    @Test
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
    
    /**
     * Test that individual graph files in an include directory are loaded as expected
     */
    @Test
    public void testBasicPrefabConfigDirectorySingleReports() throws IOException {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");
        File graphHCbits = m_fileAnticipator.tempFile(graphDirectory, "mib2.HCbits.properties");
                    
        m_outputStream = new FileOutputStream(rootFile);
		m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(graphHCbits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateHCBitsGraph);
        m_writer.close();
        m_outputStream.close();
           
        HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
        prefabConfigs.put("performance", new FileSystemResource(rootFile));

        PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);
        
        PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
        assertNotNull(mib2Bits);
        assertEquals("mib2.bits", mib2Bits.getName());
        assertEquals("Bits In/Out", mib2Bits.getTitle());
        String columns1[] = {"ifInOctets","ifOutOctets"};
        Assert.assertArrayEquals(columns1, mib2Bits.getColumns());

        PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
        assertNotNull(mib2HCBits);
        assertEquals("mib2.HCbits", mib2HCBits.getName());
        assertEquals("Bits In/Out", mib2HCBits.getTitle());
        String columns2[] = {"ifHCInOctets","ifHCOutOctets"};
        Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());
    }
    
    /**
     * Test that properties files in an included directory with
     * multiple graphs defined in them are loaded correctly
     */
    @Test
    public void testPrefabConfigDirectoryMultiReports() throws IOException {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        
        File multiFile1 = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits1.properties");
        File multiFile2 = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits2.properties");
                    
        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(multiFile1);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_includedMultiGraph1);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(multiFile2);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_includedMultiGraph2);
        m_writer.close();
        m_outputStream.close();

        HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
        prefabConfigs.put("performance", new FileSystemResource(rootFile));

        PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);
        
        //Check the graphs, basically ensuring that a handful of unique but easily checkable 
        // bits are uniquely what they should be.
        
        //We check all 4 graphs
        PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
        assertNotNull(mib2Bits);
        assertEquals("mib2.bits", mib2Bits.getName());
        assertEquals("Bits In/Out", mib2Bits.getTitle());
        String columns1[] = {"ifInOctets","ifOutOctets"};
        Assert.assertArrayEquals(columns1, mib2Bits.getColumns());

        PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
        assertNotNull(mib2HCBits);
        assertEquals("mib2.HCbits", mib2HCBits.getName());
        assertEquals("Bits In/Out", mib2HCBits.getTitle());
        String columns2[] = {"ifHCInOctets","ifHCOutOctets"};
        Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());
        
        PrefabGraph mib2Discards = dao.getPrefabGraph("mib2.discards");
        assertNotNull(mib2Discards);
        assertEquals("mib2.discards", mib2Discards.getName());
        assertEquals("Discards In/Out", mib2Discards.getTitle());
        String columns3[] = {"ifInDiscards","ifOutDiscards"};
        Assert.assertArrayEquals(columns3, mib2Discards.getColumns());

        PrefabGraph mib2Errors = dao.getPrefabGraph("mib2.errors");
        assertNotNull(mib2Errors);
        assertEquals("mib2.errors", mib2Errors.getName());
        assertEquals("Errors In/Out", mib2Errors.getTitle());
        String columns4[] = {"ifInErrors","ifOutErrors"};
        Assert.assertArrayEquals(columns4, mib2Errors.getColumns());
    }
    
    /**
     * Test that properties files in an included directory with
     * multiple graphs defined in some, and single graphs in others, are loaded correctly
     */
    @Test
    public void testPrefabConfigDirectoryMixedSingleAndMultiReports() throws IOException {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        
        File multiFile = m_fileAnticipator.tempFile(graphDirectory, "mib2-1.properties");
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");
        File graphHCbits = m_fileAnticipator.tempFile(graphDirectory, "mib2.HCbits.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(graphHCbits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateHCBitsGraph);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(multiFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_includedMultiGraph1);
        m_writer.close();
        m_outputStream.close();

        HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
        prefabConfigs.put("performance", new FileSystemResource(rootFile));

        PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);
        
        //Check the graphs, basically ensuring that a handful of unique but easily checkable 
        // bits are uniquely what they should be.
        
        //We check all 4 graphs
        PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
        assertNotNull(mib2Bits);
        assertEquals("mib2.bits", mib2Bits.getName());
        assertEquals("Bits In/Out", mib2Bits.getTitle());
        String columns1[] = {"ifInOctets","ifOutOctets"};
        Assert.assertArrayEquals(columns1, mib2Bits.getColumns());

        PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
        assertNotNull(mib2HCBits);
        assertEquals("mib2.HCbits", mib2HCBits.getName());
        assertEquals("Bits In/Out", mib2HCBits.getTitle());
        String columns2[] = {"ifHCInOctets","ifHCOutOctets"};
        Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());
        
        PrefabGraph mib2Discards = dao.getPrefabGraph("mib2.discards");
        assertNotNull(mib2Discards);
        assertEquals("mib2.discards", mib2Discards.getName());
        assertEquals("Discards In/Out", mib2Discards.getTitle());
        String columns3[] = {"ifInDiscards","ifOutDiscards"};
        Assert.assertArrayEquals(columns3, mib2Discards.getColumns());

        PrefabGraph mib2Errors = dao.getPrefabGraph("mib2.errors");
        assertNotNull(mib2Errors);
        assertEquals("mib2.errors", mib2Errors.getName());
        assertEquals("Errors In/Out", mib2Errors.getTitle());
        String columns4[] = {"ifInErrors","ifOutErrors"};
        Assert.assertArrayEquals(columns4, mib2Errors.getColumns());
    }
    
    
    /**
     * Test that an included single report per file properties config can override
     * a report in the main properties file. 
     * @throws IOException
     */
    @Test
    public void testPrefabConfigDirectorySingleReportOverride() throws Exception {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");
        File graphHCbits = m_fileAnticipator.tempFile(graphDirectory, "mib2.HCbits.properties");
                    
        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_mib2bitsBasePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(graphHCbits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateHCBitsGraph);
        m_writer.close();
        m_outputStream.close();
       
        HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
        prefabConfigs.put("performance", new FileSystemResource(rootFile));

        PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);
        
        PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
        assertNotNull(mib2Bits);
        //The base properties file (s_mib2bitsBasePrefab) has the name=Wrong Name, and columns=wrongColumn1,wrongColumn2.
        // We check that the overridden graph has the correct details in it
        assertEquals("mib2.bits", mib2Bits.getName());
        assertEquals("Bits In/Out", mib2Bits.getTitle());
        String columns1[] = {"ifInOctets","ifOutOctets"};
        Assert.assertArrayEquals(columns1, mib2Bits.getColumns());

        PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
        assertNotNull(mib2HCBits);
        assertEquals("mib2.HCbits", mib2HCBits.getName());
        assertEquals("Bits In/Out", mib2HCBits.getTitle());
        String columns2[] = {"ifHCInOctets","ifHCOutOctets"};
        Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());
        
        //Now, having proven that the override works, rewrite the base file with the same data, thus updating the timestamp
        // and forcing a reload.  The mib2.bits graph should still be the correct overridden one.  

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_mib2bitsBasePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        //Wait long enough to make the FileReloadContainers do their thing reliably
        Thread.sleep(1100);

        //Ensure that the override still applies and hasn't been "underridden" by the rewrite of the base file.
        mib2Bits = dao.getPrefabGraph("mib2.bits");
        assertNotNull(mib2Bits);
        assertEquals("mib2.bits", mib2Bits.getName());
        assertEquals("Bits In/Out", mib2Bits.getTitle());
        String columns3[] = {"ifInOctets","ifOutOctets"};
        Assert.assertArrayEquals(columns3, mib2Bits.getColumns());
    }
    
    @Test
    public void testPrefabPropertiesIncludeDirectoryReloadSingleReports() throws Exception {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
        
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
        
        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(rootFile));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
                    
        PrefabGraph graph = dao.getPrefabGraph("mib2.bits");
        assertNotNull("could not get mib2.bits report", graph);
        assertEquals("ifSpeed", graph.getExternalValues()[0]);

        Thread.sleep(1100);

        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph.replace("ifSpeed", "anotherExternalValue"));
        m_writer.close();
        m_outputStream.close();
        
        graph = dao.getPrefabGraph("mib2.bits");
        assertNotNull("could not get mib2.bits report after rewriting config file", graph);
        assertEquals("anotherExternalValue", graph.getExternalValues()[0]);
    }

    /**
     * Test that reloading a badly formatted single report doens't overwrite a previously functioning
     * report.  
     * 
     * NB: It should still complain with an Error log.  Should there be an event as well?
     * @throws Exception
     */
    @Test
    public void testPrefabPropertiesIncludeDirectoryBadReloadSingleReport() throws Exception {
        //We're expecting an ERROR log, and will be most disappointed if
        // we don't get it.  Turn off the default check in runTest
        testSpecificLoggingTest = true;
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
        
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
           
        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(rootFile));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);
        
        
        PrefabGraph graph = dao.getPrefabGraph("mib2.bits");
        assertNotNull("could not get mib2.bits report", graph);
        assertEquals("ifSpeed", graph.getExternalValues()[0]);

        Thread.sleep(1100);

        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        //Two changes:
        // 1) Remove a required property; this should break the reading of the new file
        // 2) Change the externalvalues attribute name; we shouldn't see that new name after the reload
        m_writer.write(s_separateBitsGraph.replace("report.name", "report.fluggle").replace("ifSpeed", "anotherExternalValue"));
        m_writer.close();
        m_outputStream.close();
        
        graph = dao.getPrefabGraph("mib2.bits");
        assertNotNull("could not get mib2.bits report after rewriting config file", graph);
        assertEquals("ifSpeed", graph.getExternalValues()[0]);
        
    }
    
    /**
     * Test that we can load a partly borked config file (i.e. if one graph is incorrectly specified,
     * we load as many of the rest as we can).
     * The borked'ness we can tolerate does not include poor double quoting which confuses the underlying
     * Java properties parser, but misspelled property names should only affect the graph in question.
     * 
     * NB: It should still complain with an Error log.  Should there be an event as well?
     * @throws Exception
     */
    @Test
    public void testPrefabGraphPartlyBorkedConfig() throws Exception {
        //We're expecting an ERROR log, and will be most disappointed if
        // we don't get it.  Turn off the default check in runTest
        testSpecificLoggingTest = true;
        
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadProperties("foo", new ByteArrayInputStream(s_partlyBorkedPrefab.getBytes("UTF-8")));
        
        //We expect to be able to get a mib2.HCbits, and a mib2.discards, but no mib2.bits 
        try {
            PrefabGraph mib2bits = dao.getPrefabGraph("mib2.bits");
            fail("Should have thrown an ObjectRetrievalFailureException retrieving graph "+mib2bits);
        } catch (ObjectRetrievalFailureException e) {
            
        }
        PrefabGraph mib2HCbits = dao.getPrefabGraph("mib2.HCbits");
        assertNotNull(mib2HCbits);
        PrefabGraph mib2Discards = dao.getPrefabGraph("mib2.discards");
        assertNotNull(mib2Discards);
        

    }
    
    /**
     * Test that adding a "include.directory" property to the main graph config file
     * will cause included files to be read on reload of the main config file
     * (early code didn't do this right)
     * @throws Exception
     */
    @Test
    public void testAddingIncludeDirectory() throws Exception {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_prefab);
        m_writer.close();
        m_outputStream.close();
        
        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(rootFile));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);

        assertNotNull(dao.getPrefabGraph("mib2.bits"));
        try {
            PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
            fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
        } catch (ObjectRetrievalFailureException e) {
            
        }
        
        //Wait long enough to make the FileReloadContainers do their thing reliably
        Thread.sleep(1100);

        //Now create the new graph in a sub-directory, and rewrite the rootFile with an include.directory property 
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        File graphErrors = m_fileAnticipator.tempFile(graphDirectory, "mib2.errors.properties");
        
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphErrors);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateErrorsGraph);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_prefab.replace("output.mime", "include.directory=snmp-graph.properties.d\n" +
        		"output.mime"));
        m_writer.close();
        m_outputStream.close();
        
        assertNotNull(dao.getPrefabGraph("mib2.bits")); //Just checking the reload didn't lose existing graphs
        assertNotNull(dao.getPrefabGraph("mib2.errors")); //This is the core: this graph should have been picked up
    }
    
    /**
     * Test that adding a new properties file into an included directory
     * will be picked up.  Requires the include.directory.rescan to be set low 
     * @throws Exception
     */
    @Test
    public void testIncludeDirectoryNewFile() throws Exception {
        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
        
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        graphDirectory.mkdir();

        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(rootFile));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);

        try {
            PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
            fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
        } catch (ObjectRetrievalFailureException e) {
            
        }

        //Now create the new graph in a sub-directory; see if it gets read
        File graphErrors = m_fileAnticipator.tempFile(graphDirectory, "mib2.errors.properties");
        m_outputStream = new FileOutputStream(graphErrors);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateErrorsGraph);
        m_writer.close();
        m_outputStream.close();

        //Wait longer than the rescan timeout on the include directory
        Thread.sleep(1100);
        
        assertNotNull(dao.getPrefabGraph("mib2.errors")); //This is the core: this graph should have been picked up
    }

    @Test
    public void testIncludeDirectoryIncludeMissingReportId() throws Exception {
        //We're expecting an ERROR log, and will be most disappointed if
        // we don't get it.  Turn off the default check in runTest
        testSpecificLoggingTest = true;

        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();

        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateBitsGraph.replace("report.id", "report.noid"));
        m_writer.close();
        m_outputStream.close();

        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(rootFile));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig,s_emptyMap);

        try {
            PrefabGraph graph = dao.getPrefabGraph("mib2.bits");
            fail("Shouldn't have gotten here; expecting an exception fetching "+graph);
        } catch (ObjectRetrievalFailureException e) {
            //Expected; no such graph
        }

    }

    /**
     * It would be nice if having found a new file in the include directory that was malformed, that
     * when it is fixed, it is picked up immediately, rather than having to wait for the next rescan interval
     */
    @Test
    public void testIncludeNewFileMalformedContentThenFixed() throws Exception {
        //Don't do the normal checking of logging for worse than warning; we expect an error or two to be logged, and that's fine
        testSpecificLoggingTest = true;

        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
        
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        graphDirectory.mkdir();

        HashMap<String, Resource> perfConfig = new HashMap<String, Resource>();
        perfConfig.put("performance", new FileSystemResource(rootFile));
        PropertiesGraphDao dao = createPropertiesGraphDao(perfConfig, s_emptyMap);

        try {
            PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
            fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
        } catch (ObjectRetrievalFailureException e) {
            
        }

        //Now create the new graph in a sub-directory but make it malformed; make sure it isn't loaded
        File graphErrors = m_fileAnticipator.tempFile(graphDirectory, "mib2.errors.properties");
        m_outputStream = new FileOutputStream(graphErrors);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_separateErrorsGraph.replace("report.id", "report.noid"));
        m_writer.close();
        m_outputStream.close();

        //Wait longer than the rescan timeout on the include directory
        Thread.sleep(1100);
        
        //Confirm that the graph still hasn't been loaded (because it was munted)
        try {
            PrefabGraph mib2errors = dao.getPrefabGraph("mib2.errors");
            fail("Should have thrown an ObjectRetrievalFailureException retrieving graph " + mib2errors);
        } catch (ObjectRetrievalFailureException e) {
            
        }
        
        //Now set the include rescan interval to a large number, rewrite the graph correctly, and check
        // that the file is loaded (and we don't have to wait for the rescan interval)
       dao.findPrefabGraphTypeByName("performance").setIncludeDirectoryRescanInterval(300000); //5 minutes
       m_outputStream = new FileOutputStream(graphErrors);
       m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
       m_writer.write(s_separateErrorsGraph);
       m_writer.close();
       m_outputStream.close();
    
       //Just make sure any timestamps will be at least 1 second old, just to be sure
       Thread.sleep(1100);

       //And now the graph should have loaded
       try {
           assertNotNull(dao.getPrefabGraph("mib2.errors")); //This is the core: this graph should have been picked up
       } catch (Exception e) {
           //Catch exceptions and fail explicitly, because that's a failure, not an "error"
           fail("Should not have gotten an exception fetching the graph");
       }
    }
    
    /**
     * Test that when loading graphs from files in the include directory, that if one of
     * the graphs defined in one of the multi-graph files is borked, the rest load correctly
     * 
     * Then also check that on setting the reload interval high, that the borked graph is 
     * noticed immediately when we fix it
     * @throws IOException
     */
    @Test
    public void testPrefabConfigDirectoryPartlyBorkedMultiReports()
            throws Exception {
        //Don't do the normal checking of logging for worse than warning; we expect an error or two to be logged, and that's fine
        testSpecificLoggingTest = true;

        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");

        File multiFile1 = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits1.properties");
        File multiFile2 = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits2.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();

        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(multiFile1);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        //Make mib2.errors incorrectly specified
        m_writer.write(s_includedMultiGraph1.replace("report.mib2.errors.name", "report.mib2.errors.nmae"));
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(multiFile2);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        m_writer.write(s_includedMultiGraph2);
        m_writer.close();
        m_outputStream.close();
        
        HashMap<String, Resource> prefabConfigs = new HashMap<String, Resource>();
        prefabConfigs.put("performance", new FileSystemResource(rootFile));

        PropertiesGraphDao dao = createPropertiesGraphDao(prefabConfigs, s_emptyMap);

        //Check the graphs, basically ensuring that a handful of unique but easily checkable 
        // bits are uniquely what they should be.

        //We check all 4 graphs
        PrefabGraph mib2Bits = dao.getPrefabGraph("mib2.bits");
        assertNotNull(mib2Bits);
        assertEquals("mib2.bits", mib2Bits.getName());
        assertEquals("Bits In/Out", mib2Bits.getTitle());
        String columns1[] = { "ifInOctets", "ifOutOctets" };
        Assert.assertArrayEquals(columns1, mib2Bits.getColumns());

        PrefabGraph mib2HCBits = dao.getPrefabGraph("mib2.HCbits");
        assertNotNull(mib2HCBits);
        assertEquals("mib2.HCbits", mib2HCBits.getName());
        assertEquals("Bits In/Out", mib2HCBits.getTitle());
        String columns2[] = { "ifHCInOctets", "ifHCOutOctets" };
        Assert.assertArrayEquals(columns2, mib2HCBits.getColumns());

        PrefabGraph mib2Discards = dao.getPrefabGraph("mib2.discards");
        assertNotNull(mib2Discards);
        assertEquals("mib2.discards", mib2Discards.getName());
        assertEquals("Discards In/Out", mib2Discards.getTitle());
        String columns3[] = { "ifInDiscards", "ifOutDiscards" };
        Assert.assertArrayEquals(columns3, mib2Discards.getColumns());

        try {
            PrefabGraph mib2Errors = dao.getPrefabGraph("mib2.errors");
            fail("Should have thrown an ObjectRetrievalFailureException retrieving graph "
                    + mib2Errors);
        } catch (ObjectRetrievalFailureException e) {
            //This is ok, and what should have happened
        }

        //Now set the include rescan interval to a large number, rewrite the multigraph file correctly, and check
        // that the file is loaded (and we don't have to wait for the rescan interval)
        dao.findPrefabGraphTypeByName("performance").setIncludeDirectoryRescanInterval(300000); //5 minutes

        //Just make sure any timestamps will be at least 1 second old, just to be sure that the file timestamp
        // will be 1 second in the past
        Thread.sleep(1100);

        m_outputStream = new FileOutputStream(multiFile1);
        m_writer = new OutputStreamWriter(m_outputStream, "UTF-8");
        //Correctly specified graph file now (error corrected)
        m_writer.write(s_includedMultiGraph1);
        m_writer.close();
        m_outputStream.close();

        //And now the graph should have loaded correctly
        try {
            assertNotNull(dao.getPrefabGraph("mib2.errors")); 
        } catch (Exception e) {
            //Catch exceptions and fail explicitly, because that's a failure, not an "error"
            fail("Should not have gotten an exception fetching the graph");
        }
    }
}
