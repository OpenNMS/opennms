package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

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
    private JAXBContext c;
    
    File baseDir = new File("/tmp");
    
    private class TestOutputResolver extends SchemaOutputResolver {
        public Result createOutput( String namespaceUri, String suggestedFileName ) throws IOException {
            return new StreamResult(new File(baseDir, suggestedFileName));
        }
    }

    @Before
    public void setUp() throws JAXBException {
        fsr = new MockForeignSourceRepository();
        fsr.save(new OnmsForeignSource("test"));
        c = JAXBContext.newInstance(OnmsForeignSource.class);
    }

    @Test
    public void generateXML() throws IOException {
        OnmsForeignSource fs = fsr.get("test");
        c.generateSchema(new TestOutputResolver());
        fail("not yet implemented");
    }


}
