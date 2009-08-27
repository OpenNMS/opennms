package org.opennms.sms.reflector.smsservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

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
	
	@Resource(name="smsServiceList")
	List<SmsService> m_serviceList;

	@Test
	public void testInitialization() {
		assertNotNull(m_serviceList);
		assertEquals("must have one service", 1, m_serviceList.size());
		assertEquals("must have one gateway", 1, m_serviceList.get(0).getGateways().size());
		assertEquals("gateway ID must be 'monkeys!'", "monkeys!", m_serviceList.get(0).getGateways().iterator().next().getGatewayId());
	}
}