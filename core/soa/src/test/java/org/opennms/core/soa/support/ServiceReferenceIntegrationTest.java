package org.opennms.core.soa.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ServiceReferenceIntegrationTest {
	
    @Autowired
    @Qualifier("reference")
	Hello m_hello;
	
	@Autowired
    @Qualifier("reference")
	Goodbye m_goodbye;
	
	@Autowired
	ServiceRegistry m_serviceRegistry;
	
	@Autowired
	MyProvider m_myProvider;
	
	
	@Test
	@DirtiesContext
	public void testWiring() throws IOException{
		
		assertNotNull(m_serviceRegistry);
        assertNotNull(m_hello);
        assertNotNull(m_goodbye);
		
		assertNotNull(m_myProvider);
		
		assertEquals(0, m_myProvider.helloSaid());

		m_hello.sayHello();
		
		int helloSaid = m_myProvider.helloSaid();

		assertEquals(1, helloSaid);
		
		assertEquals(0, m_myProvider.goodbyeSaid());

		m_goodbye.sayGoodbye();

		assertEquals(1, m_myProvider.goodbyeSaid());
		
        
	}
	
}
