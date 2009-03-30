package org.opennms.serviceregistration;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.opennms.serviceregistration.strategies.JMDNSStrategy;

public class JMDNSRegistrationTest extends TestCase {
	private ServiceRegistrationStrategy a;
	private static int timeout = 8000;

	@Before
	public void setUp() throws Exception {
	    a = new JMDNSStrategy();
	}

	@Test
	public void testMdnsShort() throws Exception {
		a.initialize("http", "Short Test", 1010);
		a.register();
		Thread.sleep(timeout);
		a.unregister();
	}
	
    @Test
	public void testMdnsLong() throws Exception {
		Hashtable<String,String> properties = new Hashtable<String,String>();
		properties.put("path", "/opennms");
		a.initialize("http", "Long Test", 1011, properties);
		a.register();
		Thread.sleep(timeout);
		a.unregister();
	}
}
