package org.opennms.sms.reflector.smsservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath*:/META-INF/spring/bundle-context.xml",
        "classpath*:/META-INF/opennms/bundle-context-opennms.xml",
        "classpath:/testGatewayContext.xml"
})
public class SmsServiceTest {
	SmsService[] m_serviceList;
	
	@Test
	@Ignore
	public void testInitialization() {
		assertNotNull(m_serviceList);
		assertEquals("must have one service", 1, m_serviceList.length);
		assertEquals("must have one gateway", 1, m_serviceList[0].getGateways().size());
		assertEquals("gateway ID must be 'monkeys!'", "monkeys!", m_serviceList[0].getGateways().iterator().next().getGatewayId());
	}
}