package org.opennms.sms.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

public class MobileMsgSequenceMonitorTest {

	private MonitoredService m_service;
	private MobileMsgSequenceMonitor m_monitor;
	
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

			public NetworkInterface<InetAddress> getNetInterface() {
				return new InetNetworkInterface(getAddress());
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

			public String getSvcUrl() {
			    return null;
			}
		};
		
		m_monitor = createAndInitializeMonitor();
	}
	
	@Test
	public void testBrokenConfiguration() throws Exception {
		
		assertUnavailable("<mobile-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/mobile-sequence\">" +
				"   <octagon sides=\"8\" />" +
				"</mobile-sequence>");

	}

	@Test
	public void testInlineSequence() throws Exception {
		
		assertAvailable("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<mobile-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/mobile-sequence\">\n" +
				"<transaction label=\"sms-ping\">\n" +
				"<sms-request recipient=\"${recipient}\" text=\"You suck!\"/>\n" +
				"<sms-response>\n" +
				"<from-recipient/>\n" +
				"<matches>^[Nn]o$</matches>\n" +
				"</sms-response>\n" +
				"</transaction>\n" +
				"</mobile-sequence>");

	}
	
	@Test
	public void handleNullToInit() {
	    MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
	    m.initialize((Map<String, Object>)null);
	}

	@Test
	public void testParseConfiguration() throws Exception {

		PollStatus s = assertUnavailable("<mobile-sequence xmlns=\"http://xmlns.opennms.org/xsd/config/mobile-sequence\" />");
		assertEquals("No transactions were configured for host 127.0.0.1", s.getReason());

	}

	@Test
	public void testSimpleSequence() throws Exception {
		
		PollStatus s = assertAvailable(getXmlBuffer("sms-ping-sequence.xml"));
		assertTrue(s.getProperty("sms-ping").longValue() > 10);

	}

	@Test
	public void testUssdSequence() throws Exception {
	    assertAvailable(getXmlBuffer("tmobile-balance-sequence.xml"));
	}

    private PollStatus assertUnavailable(String config) {
        return assertPollStatus(config, PollStatus.SERVICE_UNAVAILABLE);
    }

    private PollStatus assertAvailable(String config) {
        return assertPollStatus(config, PollStatus.SERVICE_AVAILABLE);
    }

    private PollStatus assertPollStatus(String config, int expectedStatus) {
        Map<String, Object> parameters = createConfigParameters(config);
        PollStatus s = m_monitor.poll(m_service, parameters);
        assertEquals("unaccepted poll status", expectedStatus, s.getStatusCode());
        return s;
    }

	private MobileMsgSequenceMonitor createAndInitializeMonitor() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(MobileMsgSequenceMonitor.CONTEXT_KEY, "testMobileMessagePollerContext");
		
		MobileMsgSequenceMonitor m = new MobileMsgSequenceMonitor();
		m.initialize(params);
		return m;
	}
	
	private Map<String, Object> createConfigParameters(String mobileConfig) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "3000");
		parameters.put("sequence", mobileConfig);
		return parameters;
	}
	
    private String getXmlBuffer(String fileName) throws IOException {
        File xmlFile = new File(ClassLoader.getSystemResource(fileName).getFile());
        assertTrue("xml file is readable", xmlFile.canRead());
        return FileUtils.readFileToString(xmlFile);
    }

}
