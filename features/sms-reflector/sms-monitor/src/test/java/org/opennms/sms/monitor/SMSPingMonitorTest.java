package org.opennms.sms.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath*:/META-INF/spring/bundle-context.xml",
        "classpath*:/META-INF/opennms/bundle-context-opennms.xml",
        "classpath:/testContext.xml"
})
public class SMSPingMonitorTest {
	@Autowired
	ApplicationContext m_context;
	
	@Resource(name="smsService")
	SmsService m_smsService;

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
	public void testPing() {
		assertNotNull(m_smsService);
		
		assertEquals("ACM0", m_smsService.getGateways().iterator().next().getGatewayId());
		
		SMSPingMonitor p = new SMSPingMonitor();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("retry", "0");
		parameters.put("timeout", "30000");
		PollStatus s = p.poll(m_service, parameters);
		System.err.println("reason = " + s.getReason());
		System.err.println("status name = " + s.getStatusName());
		assertEquals("ping should pass", PollStatus.SERVICE_AVAILABLE, s.getStatusCode());
	}
}