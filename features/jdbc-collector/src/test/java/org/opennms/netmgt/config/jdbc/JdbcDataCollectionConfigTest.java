package org.opennms.netmgt.config.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.test.FileAnticipator;
import org.xml.sax.SAXException;

public class JdbcDataCollectionConfigTest {
    private Marshaller m;
    private Unmarshaller um;
    private FileAnticipator fa;
    private JAXBContext c;
    
    private JdbcDataCollectionConfig jdcc;
    
    static private class TestOutputResolver extends SchemaOutputResolver {
        private final File m_schemaFile;
        
        public TestOutputResolver(File schemaFile) {
            m_schemaFile = schemaFile;
        }
        
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(m_schemaFile);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        fa = new FileAnticipator();
        c = JAXBContext.newInstance(JdbcDataCollectionConfig.class);
        m = c.createMarshaller();
        um = c.createUnmarshaller();
        
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
        c.generateSchema(new TestOutputResolver(schemaFile));
        if (fa.isInitialized()) {
            fa.deleteExpected();
        }
    }
    
    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        m.marshal(jdcc, objectXML);

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
        
        InputStream reader = new FileInputStream(jdbcCollectionConfig);
        
        um.setSchema(null);
        JdbcDataCollectionConfig exampleJdcc = (JdbcDataCollectionConfig)um.unmarshal(reader);

        assertTrue("Compare JDBC Data Collection Config objects.", jdcc.equals(exampleJdcc));
        
        reader.close();
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
