package org.opennms.sms.monitor.internal.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
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

public class SequenceXmlTest {

	private FileAnticipator m_fileAnticipator;
	private MobileSequenceConfig m_smsSequence;
	private JAXBContext m_context;
	private Marshaller m_marshaller;
	private Unmarshaller m_unmarshaller;
	
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
    	m_fileAnticipator = new FileAnticipator();

    	m_smsSequence = new MobileSequenceConfig();

    	MobileSequenceTransaction reqBalanceTransfer = new MobileSequenceTransaction("ussd-transfer");
    	
    	UssdSequenceRequest request = new UssdSequenceRequest("req-balance-transfer", "*327*${session.target}*${session.amount}#");
    	reqBalanceTransfer.setRequest(request);
    	
    	UssdSequenceResponse response = new UssdSequenceResponse("balance-conf-resp");
    	response.addMatcher(new UssdSessionStatusMatcher("FURTHER_ACTION_REQUIRED"));
    	response.addMatcher(new TextResponseMatcher("^Transfiere L ${session.amount} al ${session.target}$"));
    	reqBalanceTransfer.addResponse(response);

    	m_smsSequence.addTransaction(reqBalanceTransfer);
    	
    	MobileSequenceTransaction reqConf = new MobileSequenceTransaction("req-conf");
    	
    	request = new UssdSequenceRequest("conf-transfer", "1");
    	reqConf.setRequest(request);
    	
    	response = new UssdSequenceResponse("processing");
    	response.addMatcher(new UssdSessionStatusMatcher("NO_FURTHER_ACTION_REQUIRED"));
    	response.addMatcher(new TextResponseMatcher("^.*Su transaccion se esta procesando.*$"));
    	reqConf.addResponse(response);

    	SmsSequenceResponse smsResponse = new SmsSequenceResponse("transferred");
    	smsResponse.addMatcher(new TextResponseMatcher("^.*le ha transferido L ${session.amount}.*$"));
    	smsResponse.addMatcher(new SmsSourceMatcher("+3746"));
    	reqConf.addResponse(smsResponse);

    	m_smsSequence.addTransaction(reqConf);

    	m_context = JAXBContext.newInstance(
    			MobileSequenceConfig.class,
    			SmsSequenceRequest.class,
    			UssdSequenceRequest.class,
    			SmsSequenceResponse.class,
    			UssdSequenceResponse.class,
    			SmsFromRecipientResponseMatcher.class,
    			SmsSourceMatcher.class,
    			TextResponseMatcher.class,
    			UssdSessionStatusMatcher.class
    			);

    	m_marshaller = m_context.createMarshaller();
    	m_marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m_marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new MobileSequenceNamespacePrefixMapper());

    	m_unmarshaller = m_context.createUnmarshaller();
    	m_unmarshaller.setSchema(null);

    	XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
    }

    @After
    public void tearDown() throws Exception {
    	m_fileAnticipator.tearDown();
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
        File schemaFile = m_fileAnticipator.expecting("mobile-sequence.xsd");
        m_context.generateSchema(new TestOutputResolver(schemaFile));
        printFile(schemaFile);
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }

    @Test
    public void generateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        m_marshaller.marshal(m_smsSequence, objectXML);
        System.err.println(objectXML.toString());
    }

    static private class TestValidationEventHandler implements ValidationEventHandler {
		public boolean handleEvent(ValidationEvent event) {
			System.err.println("Validation failure event: " + event);
			if (event != null && event.getLinkedException() != null) {
				event.getLinkedException().printStackTrace();
			}
			return false;
		}
    }

    @Test(expected=UnmarshalException.class)
    public void readInvalidXML() throws Exception {
    	File exampleFile = new File(ClassLoader.getSystemResource("invalid-sequence.xml").getFile());
    	ValidationEventHandler handler = new TestValidationEventHandler();
    	m_unmarshaller.setEventHandler(handler);
    	MobileSequenceConfig s = (MobileSequenceConfig)m_unmarshaller.unmarshal(exampleFile);
    	System.err.println("sequence = " + s);
    }
    
    @Test
    public void readXML() throws Exception {
    	File exampleFile = new File(ClassLoader.getSystemResource("ussd-balance-sequence.xml").getFile());
    	ValidationEventHandler handler = new TestValidationEventHandler();
    	m_unmarshaller.setEventHandler(handler);
    	MobileSequenceConfig s = (MobileSequenceConfig)m_unmarshaller.unmarshal(exampleFile);
    	System.err.println("sequence = " + s);
    }
    
    @Test
    public void validateXML() throws Exception {
        // Marshal the test object to an XML string
        StringWriter objectXML = new StringWriter();
        m_marshaller.marshal(m_smsSequence, objectXML);
 
        // Read the example XML from src/test/resources
        StringBuffer exampleXML = getXmlBuffer("ussd-balance-sequence.xml");
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
    public void tryFactory() throws Exception {
    	File exampleFile = new File(ClassLoader.getSystemResource("ussd-balance-sequence.xml").getFile());
    	MobileSequenceConfig sequence = SequenceConfigFactory.getInstance().getSequenceForFile(exampleFile);
    	assertEquals("ussd-transfer", sequence.getTransactions().iterator().next().getLabel());
    }
    
    @SuppressWarnings("unchecked")
	private DetailedDiff getDiff(StringWriter objectXML, StringBuffer exampleXML) throws SAXException, IOException {
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(exampleXML.toString(), objectXML.toString()));
        List<Difference> allDifferences = myDiff.getAllDifferences();
        if (allDifferences.size() > 0) {
            for (Difference d : allDifferences) {
                System.err.println(d);
            }
        }
        return myDiff;
    }

    private StringBuffer getXmlBuffer(String fileName) throws IOException {
        StringBuffer xmlBuffer = new StringBuffer();
        File xmlFile = new File(ClassLoader.getSystemResource("ussd-balance-sequence.xml").getFile());
        assertTrue("ussd-balance-sequence.xml is readable", xmlFile.canRead());

        BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) {
                reader.close();
                break;
            }
            xmlBuffer.append(line).append("\n");
        }
        return xmlBuffer;
    }
}
