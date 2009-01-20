package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JAXBTest {
    private ForeignSourceWrapper fsw;
    private MockForeignSourceRepository fsr;
    private Marshaller m;
//    private Unmarshaller u;
    private JAXBContext c;
    private OnmsForeignSource fs;
    
    File schemaFile = new File("/tmp/foreign-sources.xsd");
    
    private class TestOutputResolver extends SchemaOutputResolver {
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(schemaFile);
        }
    }

    @Before
    public void setUp() throws JAXBException {
        fsr = new MockForeignSourceRepository();
        fsr.save(new OnmsForeignSource("cheese"));

        fs = fsr.get("cheese");
//        fs.setScanInterval(scanInterval)

        ArrayList<PluginConfig> detectors = new ArrayList<PluginConfig>();
        final PluginConfig detector = new PluginConfig("food", "org.opennms.netmgt.provision.persist.detectors.FoodDetector");
        detector.addParameter("type", "cheese");
        detector.addParameter("density", "soft");
        detector.addParameter("sharpness", "mild");
        detectors.add(detector);
        fs.setDetectors(detectors);

        ArrayList<PluginConfig> policies = new ArrayList<PluginConfig>();
        PluginConfig policy = new PluginConfig("lower-case-node", "org.opennms.netmgt.provision.persist.policies.NodeCategoryPolicy");
        policy.addParameter("label", "~^[a-z]$");
        policy.addParameter("category", "Lower-Case-Nodes");
        policies.add(policy);
        policy = new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy");
        policies.add(policy);
        policy = new PluginConfig("10-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.MatchingInterfacePolicy");
        policy.addParameter("ipaddress", "~^10\\..*$");
        policies.add(policy);
        policy = new PluginConfig("cisco-snmp-interfaces", "org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy");
        policy.addParameter("ifdescr", "~^(?i:LEC).*$");
        policies.add(policy);
        fs.setPolicies(policies);

        fsw = new ForeignSourceWrapper(fsr.getAll());
        c = JAXBContext.newInstance(ForeignSourceWrapper.class, OnmsForeignSource.class);

        m = c.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
    }

    @Test
    public void generateSchema() throws Exception {
        c.generateSchema(new TestOutputResolver());
        assertTrue("schema file exists", schemaFile.exists());
    }
    
    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        m.marshal(fsw, objectXML);

        // Read the example XML from src/test/resources
        StringBuffer exampleXML = new StringBuffer();
        File foreignSources = new File(ClassLoader.getSystemResource("foreign-sources.xml").getFile());
        assertTrue("foreign-sources.xml is readable", foreignSources.canRead());
        BufferedReader reader = new BufferedReader(new FileReader(foreignSources));
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
