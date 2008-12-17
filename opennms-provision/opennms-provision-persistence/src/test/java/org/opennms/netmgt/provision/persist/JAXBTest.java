package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;

public class JAXBTest {
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
        fsr.save(new OnmsForeignSource("test"));

        fs = fsr.get("test");

        List<PluginConfig> detectors = new ArrayList<PluginConfig>();
        final PluginConfig detector = new PluginConfig("food", "com.example.detectors.FoodDetectors");
        detector.addParameter("type", "cheese");
        detector.addParameter("density", "soft");
        detector.addParameter("sharpness", "mild");
        detectors.add(detector);
        fs.setDetectors(detectors);

        List<PluginConfig> policies = new ArrayList<PluginConfig>();
        PluginConfig policy = new PluginConfig("lower-case-node", "com.example.policies.NodeCategoryPolicy");
        policy.addParameter("nodelabel", "~^[a-z]$");
        policy.addParameter("category", "Lower-Case-Nodes");
        policies.add(policy);
        policy = new PluginConfig("all-ipinterfaces", "com.example.policies.InclusiveInterfacePolicy");
        policy.addParameter("cisco-snmp-interfaces", "comp.example.policies.IfDescrSnmpInterfacePolicy");
        policy.addParameter("ifdescr", "~(?i:cisco)");
        policies.add(policy);
        fs.setPolicies(policies);
        
        c = JAXBContext.newInstance(OnmsForeignSource.class);

        m = c.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    @Test
    public void generateSchema() throws IOException {
        c.generateSchema(new TestOutputResolver());
    }
    
    @Test
    public void generateXML() throws Exception {
        m.marshal(fs, System.out);
    }


}
