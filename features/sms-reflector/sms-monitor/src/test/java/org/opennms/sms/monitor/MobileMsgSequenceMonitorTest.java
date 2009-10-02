package org.opennms.sms.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath*:/META-INF/spring/bundle-context.xml",
        "classpath*:/META-INF/opennms/bundle-context-opennms.xml",
        "classpath:/testContext.xml"
})
public class MobileMsgSequenceMonitorTest {

	MonitoredService m_service;
	
	@Before
	public void setUp() {
		m_service = new MonitoredService() {
			public InetAddress getAddress() {
				try {
					return InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return null;
				}
			}

			public String getIpAddr() {
				return "127.0.0.1";
			}

			public NetworkInterface getNetInterface() {
				return new IPv4NetworkInterface(getAddress());
			}

			public int getNodeId() {
				return 1;
			}

			public String getNodeLabel() {
				return "localhost";
			}

			public String getSvcName() {
				return "SMS";
			}
		};
	}
	
	@Test
	@DirtiesContext
	public void testBrokenConfiguration() throws Exception {
		MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", "<mobile-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/mobile-sequence\"><octagon sides=\"8\" /></mobile-sequence>");

		PollStatus s = m.poll(m_service, parameters);
		assertEquals("monitor should fail", PollStatus.SERVICE_UNAVAILABLE, s.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testParseConfiguration() throws Exception {

		MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", "<mobile-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/mobile-sequence\" />");

		PollStatus s = m.poll(m_service, parameters);
		assertEquals("ping should pass", PollStatus.SERVICE_UNAVAILABLE, s.getStatusCode());
		assertEquals("No transactions were configured for host 127.0.0.1", s.getReason());
	}

	@Test
	@DirtiesContext
	public void testSimpleSequence() throws Exception {

		MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", getXmlBuffer("sms-ping-sequence.xml"));

		PollStatus s = m.poll(m_service, parameters);

		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
		assertTrue(s.getProperty("sms-ping").longValue() > 10);
	}

	@Test
	@DirtiesContext
	public void testUssdSequence() throws Exception {
		MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", getXmlBuffer("tmobile-balance-sequence.xml"));
		
		PollStatus s = m.poll(m_service, parameters);

		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
	}
	
	@Test
	@DirtiesContext
	public void testInlineSequence() throws Exception {
		MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<mobile-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/mobile-sequence\">\n" +
				"<transaction label=\"sms-ping\">\n" +
				"<sms-request recipient=\"${recipient}\" text=\"You suck!\"/>\n" +
				"<sms-response>\n" +
				"<from-recipient/>\n" +
				"<matches>^[Nn]o$</matches>\n" +
				"</sms-response>\n" +
				"</transaction>\n" +
				"</mobile-sequence>");
		
		PollStatus s = m.poll(m_service, parameters);

		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
	}
	
    private String getXmlBuffer(String fileName) throws IOException {
        StringBuffer xmlBuffer = new StringBuffer();
        File xmlFile = new File(ClassLoader.getSystemResource(fileName).getFile());
        assertTrue("xml file is readable", xmlFile.canRead());

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
        return xmlBuffer.toString();
    }

}
