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
import org.junit.Ignore;
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
public class SMSSequenceMonitorTest {

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
		SMSSequenceMonitor m = new SMSSequenceMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", "<sms-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/sms-sequence\"><octagon sides=\"8\" /></sms-sequence>");

		PollStatus s = m.poll(m_service, parameters);
		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("monitor should fail", PollStatus.SERVICE_UNAVAILABLE, s.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testParseConfiguration() throws Exception {

		SMSSequenceMonitor m = new SMSSequenceMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", "<sms-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/sms-sequence\" />");

		PollStatus s = m.poll(m_service, parameters);
		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
	}

	@Test
	@DirtiesContext
	@Ignore
	public void testSimpleSequence() throws Exception {

		SMSSequenceMonitor m = new SMSSequenceMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		StringBuffer xmlBuffer = getXmlBuffer("test-sequence.xml");
		parameters.put("sequence", xmlBuffer.toString());

		PollStatus s = m.poll(m_service, parameters);
		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
	}

    private StringBuffer getXmlBuffer(String fileName) throws IOException {
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
        return xmlBuffer;
    }

}
