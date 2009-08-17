package org.opennms.sms.monitor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.sms.monitor.SMSPingMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
/*        "classpath*:/META-INF/spring/bundle-context.xml",
        "classpath*:/META-INF/opennms/bundle-context-opennms.xml",
        */
        "classpath:/testContext.xml"
})
public class SMSPingMonitorTest {

	private PingTestGateway m_gateway;

	@Before
	public void setUp() {
		m_gateway = new PingTestGateway("test");
	}

	@Test
	@Ignore
	public void testPing() {
		SMSPingMonitor p = new SMSPingMonitor();
	}
}