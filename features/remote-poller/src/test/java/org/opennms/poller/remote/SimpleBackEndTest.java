package org.opennms.poller.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.JUnitHttpServerExecutionListener;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.core.test.annotations.Webapp;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	DependencyInjectionTestExecutionListener.class,
    JUnitHttpServerExecutionListener.class
})
@ContextConfiguration(locations="classpath:/applicationContext-client.xml")
public class SimpleBackEndTest {
	@SuppressWarnings("restriction")
	@Resource(name="noAuthBean")
	private SimpleBackEnd m_noAuthBackEnd;
	
	@SuppressWarnings("restriction")
	@Resource(name="authBean")
	private SimpleBackEnd m_authBackEnd;
	
	@BeforeClass
	public static void setup() {
		MockLogAppender.setupLogging();
	}
	
	@Test
	@JUnitHttpServer(port=9162, webapps=@Webapp(context="/", path="src/test/resources/simple-test-webapp"))
	public void testBackend() throws Exception {
		assertNotNull(m_noAuthBackEnd);
		assertEquals("first get should be 0", 0, m_noAuthBackEnd.getCount());
		assertEquals("second should be 1", 1, m_noAuthBackEnd.getCount());
	}
	
	@Test
	@JUnitHttpServer(port=9162, basicAuth=true, webapps=@Webapp(context="/", path="src/test/resources/simple-test-webapp"))
	public void testBackendWithBasicAuth() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "testpassword"));
		assertNotNull(m_authBackEnd);
		assertEquals("first get should be 0", 0, m_authBackEnd.getCount());
		assertEquals("second should be 1", 1, m_authBackEnd.getCount());
	}
}
