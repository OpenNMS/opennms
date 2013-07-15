/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.test.FileAnticipator;
import org.xml.sax.SAXException;

public class JdbcDataCollectionConfigTest {
    private FileAnticipator fa;
    
    private JdbcDataCollectionConfig jdcc;
    
    static private class TestOutputResolver extends SchemaOutputResolver {
        private final File m_schemaFile;
        
        public TestOutputResolver(File schemaFile) {
            m_schemaFile = schemaFile;
        }
        
        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(m_schemaFile);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        fa = new FileAnticipator();
        
        // Mock up a JdbcDataCollectionConfig class.      
        JdbcRrd jdbcRrd = new JdbcRrd();
        jdbcRrd.addRra("RRA:AVERAGE:0.5:1:2016");
        jdbcRrd.addRra("RRA:AVERAGE:0.5:12:1488");
        jdbcRrd.addRra("RRA:AVERAGE:0.5:288:366");
        jdbcRrd.addRra("RRA:MAX:0.5:288:366");
        jdbcRrd.addRra("RRA:MIN:0.5:288:366");
        jdbcRrd.setStep(300);
        
        JdbcStatement jdbcStatement = new JdbcStatement();
        jdbcStatement.setJdbcQuery(
        "SELECT COUNT(eventid) as EventCount\n"+
        "FROM events\n"+
        "WHERE eventtime\n"+ 
        "BETWEEN (CURRENT_TIMESTAMP - INTERVAL '1 day')\n"+ 
        "AND CURRENT_TIMESTAMP;");
        
        JdbcColumn column = new JdbcColumn();
        column.setColumnName("eventCount");
        column.setDataSourceName("EventCount");
        column.setDataType("GAUGE");
        column.setAlias("eventCount");
        
        JdbcQuery jdbcQuery = new JdbcQuery();
        jdbcQuery.setQueryName("opennmsQuery");
        jdbcQuery.setJdbcStatement(jdbcStatement);
        jdbcQuery.addJdbcColumn(column);
        jdbcQuery.setRecheckInterval(3600000);
        jdbcQuery.setIfType("all");
        jdbcQuery.setResourceType("node");
        
        JdbcDataCollection jdbcDataCollection = new JdbcDataCollection();
        jdbcDataCollection.setJdbcRrd(jdbcRrd);
        jdbcDataCollection.addQuery(jdbcQuery);
        jdbcDataCollection.setName("default");
        
        jdcc = new JdbcDataCollectionConfig();
        jdcc.addDataCollection(jdbcDataCollection);
        jdcc.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
    }
    
    @After
    public void tearDown() throws Exception {
        
    }
    
    @Test
    public void generateSchema() throws Exception {
        File schemaFile = fa.expecting("jdbc-datacollection-config.xsd");
        JAXBContext c = JAXBContext.newInstance(JdbcDataCollectionConfig.class);
        c.generateSchema(new TestOutputResolver(schemaFile));
        if (fa.isInitialized()) {
            fa.deleteExpected();
        }
    }
    
    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        JaxbUtils.marshal(jdcc, objectXML);

        // Read the example XML from src/test/resources
        StringBuffer exampleXML = new StringBuffer();
        File jdbcCollectionConfig = new File(ClassLoader.getSystemResource("jdbc-datacollection-config.xml").getFile());
        assertTrue("jdbc-datacollection-config.xml is readable", jdbcCollectionConfig.canRead());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jdbcCollectionConfig), "UTF-8"));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) {
                reader.close();
                break;
            }
            exampleXML.append(line).append("\n");
        }
        System.err.println("========================================================================");
        System.err.println("Object XML:");
        System.err.println("========================================================================");
        System.err.print(objectXML.toString());
        System.err.println("========================================================================");
        System.err.println("Example XML:");
        System.err.println("========================================================================");
        System.err.print(exampleXML.toString());
        DetailedDiff myDiff = getDiff(objectXML, exampleXML);
        assertEquals("number of XMLUnit differences between the example XML and the mock object XML is 0", 0, myDiff.getAllDifferences().size());
    }
    
    @Test
    public void readXML() throws Exception {
        // Retrieve the file we're parsing.
        File jdbcCollectionConfig = new File(ClassLoader.getSystemResource("jdbc-datacollection-config.xml").getFile());
        assertTrue("jdbc-datacollection-config.xml is readable", jdbcCollectionConfig.canRead());
        
        JdbcDataCollectionConfig exampleJdcc = JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, jdbcCollectionConfig);

        assertTrue("Compare JDBC Data Collection Config objects.", jdcc.equals(exampleJdcc));
    }
    
    @SuppressWarnings("unchecked")
    private DetailedDiff getDiff(StringWriter objectXML,
            StringBuffer exampleXML) throws SAXException, IOException {
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(exampleXML.toString(), objectXML.toString()));
        List<Difference> allDifferences = myDiff.getAllDifferences();
        if (allDifferences.size() > 0) {
            for (Difference d : allDifferences) {
                System.err.println(d);
            }
        }
        return myDiff;
    }
}
