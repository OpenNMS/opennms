package org.opennms.sms.reflector.smsservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath*:/META-INF/spring/bundle-context.xml",
        "classpath*:/META-INF/opennms/bundle-context-opennms.xml",
        "classpath:/testGatewayContext.xml"
}
)
public class SmsServiceTest {
	
	@Autowired
	SmsService m_service;
	
	@Test
	public void testInitialization() {
		assertNotNull(m_service);
		assertEquals("must have one gateway", 1, m_service.getGateways().size());
		assertEquals("gateway ID must be 'ACM0'", "ACM0", m_service.getGateways().iterator().next().getGatewayId());
	}
}