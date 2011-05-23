package org.opennms.netmgt.config.invd.wmi;

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

public class WmiInvScanConfigTest {
    private Marshaller m;
    private Unmarshaller um;
    private FileAnticipator fa;
    private JAXBContext c;
    
    private WmiInvScanConfig wmiInvConf;
    
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
        c = JAXBContext.newInstance(WmiInvScanConfig.class);
        m = c.createMarshaller();
        um = c.createUnmarshaller();
        
        wmiInvConf = new WmiInvScanConfig();
        
        WmiInventory inv = new WmiInventory("default");
        
        WmiCategory cat = new WmiCategory("Network Adapters");
        
        //public WmiAsset(String name, String wmiClass, String prop, Integer recheck) {
        WmiAsset asset1 = new WmiAsset("Network Adapter", "Win32_NetworkAdapter", "Name", 3600000);
        asset1.addWmiAssetProperty(new WmiAssetProperty("Device ID","DeviceID"));
        asset1.addWmiAssetProperty(new WmiAssetProperty("Connection Name","NetConnectionID"));
        asset1.addWmiAssetProperty(new WmiAssetProperty("Status","NetConnectionStatus"));
        asset1.addWmiAssetProperty(new WmiAssetProperty("Manufacturer","Manufacturer"));
        asset1.addWmiAssetProperty(new WmiAssetProperty("Type","AdapterType"));
        asset1.addWmiAssetProperty(new WmiAssetProperty("MAC Address","MACAddress"));        
        cat.addWmiAsset(asset1);
        
        WmiAsset asset2 = new WmiAsset("Network Adapter Config", "Win32_NetworkAdapterConfiguration", "Description", 3600000);
        asset2.addWmiAssetProperty(new WmiAssetProperty("Device ID","Index"));
        asset2.addWmiAssetProperty(new WmiAssetProperty("Description","Description"));
        asset2.addWmiAssetProperty(new WmiAssetProperty("DHCP Enabled?","DHCPEnabled"));  
        cat.addWmiAsset(asset2);
        
        inv.addWmiCategory(cat);
        wmiInvConf.addWmiInventory(inv);
        
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
    }
    
    @After
    public void tearDown() throws Exception {
        
    }
    
    @Test
    public void generateSchema() throws Exception {
        File schemaFile = fa.expecting("wmi-invscan-config.xsd");
        c.generateSchema(new TestOutputResolver(schemaFile));
        if (fa.isInitialized()) {
            fa.deleteExpected();
        }
    }
    
    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        m.marshal(wmiInvConf, objectXML);

        // Read the example XML from src/test/resources
        StringBuffer exampleXML = new StringBuffer();
        File invScanConf = new File(ClassLoader.getSystemResource("wmi-invscan-config.xml").getFile());
        assertTrue("wmi-invscan-config.xml is readable", invScanConf.canRead());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(invScanConf), "UTF-8"));
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
        File invScanConf = new File(ClassLoader.getSystemResource("wmi-invscan-config.xml").getFile());
        assertTrue("wmi-invscan-config.xml is readable", invScanConf.canRead());
        
        InputStream reader = new FileInputStream(invScanConf);
        
        um.setSchema(null);
        WmiInvScanConfig exampleInvd = (WmiInvScanConfig)um.unmarshal(reader);

        assertTrue("Compare WMI Inv Scan Config objects.", wmiInvConf.equals(exampleInvd));
        
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
