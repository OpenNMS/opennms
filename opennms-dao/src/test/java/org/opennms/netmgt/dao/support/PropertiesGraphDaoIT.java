/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.dao.support.PropertiesGraphDao.PrefabGraphTypeDao;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.ObjectRetrievalFailureException;

public class PropertiesGraphDaoIT extends PropertiesGraphDaoITCase {
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
        for (PrefabGraph graph : dao.getAllPrefabGraphs()) {
            if (!graph.getCommand().trim().equals(graph.getCommand())) {
                fail("Prefab graph contains extra whitespace: " + graph.getCommand());
            }
        }
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
        OnmsResource resource = new OnmsResource("node", "1", resourceType, attributes, ResourcePath.get("foo"));
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
        OnmsResource resource = new OnmsResource("node", "1", resourceType, attributes, ResourcePath.get("foo"));
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
        OnmsResource resource = new OnmsResource("node", "1", resourceType, attributes, ResourcePath.get("foo"));
        PrefabGraph[] graphs = m_dao.getPrefabGraphsForResource(resource);
        assertEquals("prefab graph array size", 1, graphs.length);
        assertEquals("prefab graph[0] name", "mib2.HCbits", graphs[0].getName());
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
		m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(graphHCbits);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
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
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(multiFile1);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_includedMultiGraph1);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(multiFile2);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
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
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_separateBitsGraph);
        m_writer.close();
        m_outputStream.close();
        
        m_outputStream = new FileOutputStream(graphHCbits);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_separateHCBitsGraph);
        m_writer.close();
        m_outputStream.close();
                    
        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(multiFile);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
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
        m_testSpecificLoggingTest = true;
        
        PropertiesGraphDao dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
        dao.loadProperties("foo", new ByteArrayInputStream(s_partlyBorkedPrefab.getBytes(StandardCharsets.UTF_8)));
        
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

    @Test
    public void testIncludeDirectoryIncludeMissingReportId() throws Exception {
        //We're expecting an ERROR log, and will be most disappointed if
        // we don't get it.  Turn off the default check in runTest
        m_testSpecificLoggingTest = true;

        File rootFile = m_fileAnticipator.tempFile("snmp-graph.properties");
        File graphDirectory = m_fileAnticipator.tempDir("snmp-graph.properties.d");
        File graphBits = m_fileAnticipator.tempFile(graphDirectory, "mib2.bits.properties");

        m_outputStream = new FileOutputStream(rootFile);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
        m_writer.write(s_baseIncludePrefab);
        m_writer.close();
        m_outputStream.close();

        graphDirectory.mkdir();
        m_outputStream = new FileOutputStream(graphBits);
        m_writer = new OutputStreamWriter(m_outputStream, StandardCharsets.UTF_8);
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
}
