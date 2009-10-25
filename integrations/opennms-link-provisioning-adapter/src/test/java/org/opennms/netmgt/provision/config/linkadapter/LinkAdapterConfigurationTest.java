package org.opennms.netmgt.provision.config.linkadapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.provision.config.DefaultNamespacePrefixMapper;
import org.opennms.netmgt.provision.config.dao.DefaultLinkAdapterConfigurationDao;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/linkTestContext.xml"
})
@JUnitTemporaryDatabase()
public class LinkAdapterConfigurationTest {

    static private class TestOutputResolver extends SchemaOutputResolver {
        private final File m_schemaFile;
        
        public TestOutputResolver(File schemaFile) {
            m_schemaFile = schemaFile;
        }
        
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(m_schemaFile);
        }
    }

    @Autowired
    private DefaultLinkAdapterConfigurationDao m_linkConfigDao;
    
    private FileAnticipator m_fileAnticipator;

    private JAXBContext m_context;

    private Marshaller m_marshaller;

    private Unmarshaller m_unmarshaller;

    @Before
    public void setUp() throws Exception {
        m_fileAnticipator = new FileAnticipator();

        m_context = JAXBContext.newInstance(LinkAdapterConfiguration.class, LinkPattern.class);

        m_marshaller = m_context.createMarshaller();
        m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m_marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new DefaultNamespacePrefixMapper("http://xmlns.opennms.org/xsd/config/map-link-adapter"));
        
        m_unmarshaller = m_context.createUnmarshaller();
        m_unmarshaller.setSchema(null);

        Properties props = new Properties();
        props.setProperty("log4j.logger.org.springframework", "WARN");
        props.setProperty("log4j.logger.org.hibernate", "WARN");
        props.setProperty("log4j.logger.org.opennms", "DEBUG");
        props.setProperty("log4j.logger.org.opennms.netmgt.dao.castor", "WARN");
        MockLogAppender.setupLogging(props);
        
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

    }
    
    private void printFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
        }
        System.err.println(sb.toString());
    }

    @Test
    public void generateSchema() throws Exception {
        File schemaFile = m_fileAnticipator.expecting("map-link-adapter.xsd");
        m_context.generateSchema(new TestOutputResolver(schemaFile));
        printFile(schemaFile);
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }

    @Test
    public void generateXML() throws JAXBException {
        StringWriter objectXML = new StringWriter();
        LinkAdapterConfiguration config = new LinkAdapterConfiguration();
        config.setPatterns(m_linkConfigDao.getPatterns());
        m_marshaller.marshal(config, objectXML);
        System.err.println(objectXML.toString());
    }
    
    @Test(expected=Exception.class)
    @Ignore("I can't find a way to get JAXB to set minOccurs=1 with annotations...")
    public void testRequireLinkTag() throws Exception {
        ValidationEventHandler handler = new DefaultValidationEventHandler();
        m_unmarshaller.setEventHandler(handler);

        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        		"<link-adapter-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/map-link-adapter\">\n" + 
        		"    <for match=\"foo-(.*?)-baz\">\n" + 
        		"    </for>\n" + 
        		"    <for match=\"before-(.*?)-after\">\n" + 
        		"        <link>middle-was-$1</link>\n" + 
        		"    </for>\n" + 
        		"</link-adapter-configuration>";

        StringReader xmlReader = new StringReader(testXml);
        LinkAdapterConfiguration lac = (LinkAdapterConfiguration)m_unmarshaller.unmarshal(xmlReader);
        System.err.println("sequence = " + lac);

    }
}
