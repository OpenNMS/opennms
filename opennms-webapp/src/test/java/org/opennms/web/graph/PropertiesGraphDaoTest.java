package org.opennms.web.graph;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.opennms.web.graph.PrefabGraph;

public class PropertiesGraphDaoTest extends TestCase {

    final static String s_propertiesString =
            "command.input.dir=foo\n"
            + "command.prefix=foo\n"
            + "output.mime=foo\n"
            + "\n"
            + "reports=mib2.bits, mib2.discards\n"
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

    Properties m_properties;

    Map<String, PrefabGraph> m_graphs;

    public void setUp() throws Exception {
        super.setUp();
        /*
        m_properties = new Properties();
        m_properties.load(new ByteArrayInputStream(s_propertiesString.getBytes()));
        m_graphs = PropertiesGraphDao.getPrefabGraphDefinitions(m_properties);
        */
        
        PropertiesGraphDao dao = new PropertiesGraphDao("", "");
        ByteArrayInputStream in = new ByteArrayInputStream(s_propertiesString.getBytes());
        dao.loadProperties("performance", in);
        
        PrefabGraphType type = dao.findByName("performance");
        assertNotNull("could not get performance prefab graph type", type);

        m_graphs = type.getReportMap();
        assertNotNull("report map shouldn't be null", m_graphs);
    }
    
    public void testCompareTo() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        PrefabGraph discards = m_graphs.get("mib2.discards");

        assertEquals("compareTo", -1, bits.compareTo(discards));
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
        PrefabGraph bits = m_graphs.get("mib2.bits");
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

    public void testGetType() {
        PrefabGraph bits = m_graphs.get("mib2.bits");
        assertEquals("getGetType", "interface", bits.getType());
    }

    public void testGetDescription() {
        PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
        assertEquals("getDescription", null, bits.getDescription());
    }

    public void testLoadSnmpGraphProperties() throws FileNotFoundException, IOException {
        PropertiesGraphDao dao = new PropertiesGraphDao("", "");
        dao.loadProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("snmp-graph.properties"));
    }

    public void testLoadSnmpAdhocGraphProperties() throws FileNotFoundException, IOException {
        PropertiesGraphDao dao = new PropertiesGraphDao("", "");
        dao.loadAdhocProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("snmp-adhoc-graph.properties"));
    }

    public void testLoadResponseTimeGraphProperties() throws FileNotFoundException, IOException {
        PropertiesGraphDao dao = new PropertiesGraphDao("", "");
        dao.loadProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("response-graph.properties"));
    }

    public void testLoadResponseTimeAdhocGraphProperties() throws FileNotFoundException, IOException {
        PropertiesGraphDao dao = new PropertiesGraphDao("", "");
        dao.loadAdhocProperties("foo", ConfigurationTestUtils.getInputStreamForConfigFile("response-adhoc-graph.properties"));
    }
    
    public void testPrefabPropertiesReload() throws IOException, InterruptedException {
        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();
        
        try {
            File f = fa.tempFile("snmp-graph.properties");
            
            FileWriter writer = new FileWriter(f);
            // Don't include mib2.discards in the reports line
            String noDiscards = s_propertiesString.replace(", mib2.discards", "");
            writer.write(noDiscards);
            writer.close();
            
            PropertiesGraphDao dao = new PropertiesGraphDao("performance=" + f.getAbsolutePath(), "");
            PrefabGraphType type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type", type);
            
            assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
            assertNull("could get mib2.discards report, but shouldn't have been able to", type.getQuery("mib2.discards"));

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writer = new FileWriter(f);
            writer.write(s_propertiesString);
            writer.close();
            
            type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type after rewriting config file", type);
            assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
            assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
        } finally {
            fa.deleteExpected();
        }
    }
    
    public void testPrefabPropertiesReloadBad() throws IOException, InterruptedException {
        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();
        
        MockLogAppender.setupLogging(false);
        
        try {
            File f = fa.tempFile("snmp-graph.properties");

            FileWriter writer = new FileWriter(f);
            writer.write(s_propertiesString);
            writer.close();

            PropertiesGraphDao dao = new PropertiesGraphDao("performance=" + f.getAbsolutePath(), "");
            PrefabGraphType type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type", type);
            
            assertNotNull("could not get mib2.bits report", type.getQuery("mib2.bits"));
            assertNotNull("could not get mib2.discards report", type.getQuery("mib2.discards"));

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writer = new FileWriter(f);
            // Don't include the reports line at all so we get an error
            String noReports = s_propertiesString.replace("reports=mib2.bits, mib2.discards", "");
            writer.write(noReports);
            writer.close();
            
            type = dao.findByName("performance");
            assertNotNull("could not get performance prefab graph type after rewriting config file", type);
            assertNotNull("could not get mib2.bits report after rewriting config file", type.getQuery("mib2.bits"));
            assertNotNull("could not get mib2.discards report after rewriting config file", type.getQuery("mib2.discards"));
        } finally {
            fa.deleteExpected();
        }
        
        LoggingEvent[] events = MockLogAppender.getEvents();
        assertNotNull("logged event list was null", events);
        assertEquals("should only have received one logged event", 1, events.length);
        assertEquals("should have received an ERROR event" + events[0], Level.ERROR, events[0].getLevel());
    }

}
