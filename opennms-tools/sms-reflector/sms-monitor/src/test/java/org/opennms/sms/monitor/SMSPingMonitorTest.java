package org.opennms.sms.monitor;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.smslib.test.TestGateway;

@RunWith(JUnit4TestRunner.class)
public class SMSPingMonitorTest {

	public class TestPingGateway extends TestGateway {

		public TestPingGateway(String id) {
			super(id);
		}
		
	}

    @Configuration
    public static Option[] configuration(){
        return options(equinox(), provision(
                mavenBundle().groupId("org.ops4j.pax.logging").artifactId("pax-logging-service"),
                mavenBundle().groupId("org.ops4j.pax.logging").artifactId("pax-logging-api"),
                mavenBundle().groupId("org.opennms.sms-reflector").artifactId("provision").version("1.0.0-SNAPSHOT")
        ));
    }

    @Inject
    private BundleContext m_bundleContext;

	private TestPingGateway m_gateway;

    @Test
    @Ignore
    public void myFirstTest(){
        assertNotNull(m_bundleContext);
    }

	@Before
	public void setUp() {
		m_gateway = new TestPingGateway("test");
		m_gateway.setInbound(true);
		m_gateway.setOutbound(true);
	}
	
	@Test
	@Ignore
	public void testPing() {
		
	}
}