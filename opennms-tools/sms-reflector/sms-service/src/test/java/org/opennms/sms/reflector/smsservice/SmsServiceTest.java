package org.opennms.sms.reflector.smsservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
	ApplicationContext m_context;

	SmsService[] m_serviceList;

	@Before
	public void setUp() {
		m_serviceList = (SmsService[])m_context.getBean("smsServiceList");
		for (String name : m_context.getBeanDefinitionNames()) {
			System.err.println(String.format("bean '%s' exists: %s", name, m_context.getBean(name)));
		}
	}

	@Test
	public void testInitialization() {
		assertNotNull(m_serviceList);
		assertEquals("must have one service", 1, m_serviceList.length);
		assertEquals("must have one gateway", 1, m_serviceList[0].getGateways().size());
		assertEquals("gateway ID must be 'monkeys!'", "monkeys!", m_serviceList[0].getGateways().iterator().next().getGatewayId());
	}
}