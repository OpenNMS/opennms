package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;

public class JAXBTest {
    private MockForeignSourceRepository fsr;
    private Marshaller m;
    private Unmarshaller u;
    private JAXBContext c;
    
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
        OnmsForeignSource fs = fsr.get("test");
        m.marshal(fs, System.out);
    }


}
