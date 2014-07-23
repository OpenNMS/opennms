package org.opennms.netmgt.config.internal.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;

public class ConvertOldDataCollectionToNewDataCollectionTest {

    @Test
    public void testImportEmpty() {
        final String old = "<datacollection-config rrdRepository=\"${install.share.dir}/rrd/snmp/\">\n" + 
                "  <snmp-collection name=\"default\" snmpStorageFlag=\"select\">\n" + 
                "    <rrd step=\"300\">\n" + 
                "      <rra>RRA:AVERAGE:0.5:1:2016</rra>\n" + 
                "      <rra>RRA:AVERAGE:0.5:12:1488</rra>\n" + 
                "      <rra>RRA:AVERAGE:0.5:288:366</rra>\n" + 
                "      <rra>RRA:MAX:0.5:288:366</rra>\n" + 
                "      <rra>RRA:MIN:0.5:288:366</rra>\n" + 
                "    </rrd>\n" + 
                "  </snmp-collection>\n" +
                "</datacollection-config>";
        
        final String expected = "<datacollection-config>\n" + 
                "    <snmp-collection name=\"default\">\n" + 
                "        <datacollection-group name=\"default-all\">\n" +
                "            <resourceType name=\"ifIndex\" label=\"Interfaces (MIB-2 ifTable)\">\n" + 
                "                <resourceName><template>${ifDescr}-${ifPhysAddr}</template></resourceName>\n" + 
                "                <resourceLabel><template>${ifDescr}-${ifPhysAddr}</template></resourceLabel>\n" + 
                "                <resourceKind><template>${ifType}</template></resourceKind>\n" + 
                "                <column oid=\".1.3.6.1.2.1.2.2.1.2\"    alias=\"ifDescr\"    type=\"string\" />\n" + 
                "                <column oid=\".1.3.6.1.2.1.2.2.1.6\"    alias=\"ifPhysAddr\" type=\"string\"  display-hint=\"1x:\"/>\n" + 
                "                <column oid=\".1.3.6.1.2.1.2.2.1.3\"    alias=\"ifType\"     type=\"string\" /> \n" + 
                "                <column oid=\".1.3.6.1.2.1.31.1.1.1.1\" alias=\"ifName\"     type=\"string\" />\n" + 
                "            </resourceType>\n" + 
                "        </datacollection-group>\n"+
                "    </snmp-collection>\n" +
                "</datacollection-config>";

        final DatacollectionConfig oldConfig = JaxbUtils.unmarshal(DatacollectionConfig.class, old);
        final DataCollectionConfigImpl expectedNewConfig = JaxbUtils.unmarshal(DataCollectionConfigImpl.class, expected);

        final DataCollectionConfigConverter generator = new DataCollectionConfigConverter();
        oldConfig.visit(generator);
        final DataCollectionConfigImpl actualNewConfig = generator.getDataCollectionConfig();

        XmlTest.assertDepthEquals(expectedNewConfig, actualNewConfig);
    }

    @Test
    public void testOldOnefileDatacollectionConfig() throws Exception {
        final String oldXml = IOUtils.toString(getClass().getResource("old-datacollection-config-mib2.xml"));
        final DatacollectionConfig oldConfig = JaxbUtils.unmarshal(DatacollectionConfig.class, oldXml);
        assertNotNull(oldConfig);

        final String expectedXml = IOUtils.toString(getClass().getResource("new-datacollection-config-mib2.xml"));
        final DataCollectionConfigImpl expectedNewConfig = JaxbUtils.unmarshal(DataCollectionConfigImpl.class, expectedXml);

        final DataCollectionConfigConverter generator = new DataCollectionConfigConverter();
        oldConfig.visit(generator);
        final DataCollectionConfigImpl actualNewConfig = generator.getDataCollectionConfig();

        //final String newXml = JaxbUtils.marshal(actualNewConfig);
        //XmlTest.assertXmlEquals(expectedXml, newXml);

        XmlTest.assertDepthEquals(expectedNewConfig, actualNewConfig);
    }

    @Test
    public void testGetParameters() throws Exception {
        Collection<String> results = DataCollectionConfigConverter.getParameters(new ExpressionImpl("${index}"));
        assertEquals(1, results.size());
        assertEquals("index", results.iterator().next());
        
        results = DataCollectionConfigConverter.getParameters(new ExpressionImpl("${hrStorageDescr} (index ${index})"));
        String[] expected = new String[] { "hrStorageDescr", "index" };
        assertTrue(Arrays.equals(expected, results.toArray()));
    }
}
