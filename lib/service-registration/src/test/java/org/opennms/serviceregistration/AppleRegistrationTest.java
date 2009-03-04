package org.opennms.serviceregistration;

import java.util.Hashtable;

import org.opennms.serviceregistration.strategies.AppleStrategy;

import junit.framework.TestCase;

public class AppleRegistrationTest extends TestCase {
	private AppleStrategy a;
	private static int timeout = 8000;
	
	public AppleRegistrationTest() throws Exception {
		a = new AppleStrategy();
	}
	
	public void testMdnsShort() throws Throwable {
		a.initialize("http", "Short Test", 1010);
		a.register();
		Thread.sleep(timeout);
		a.unregister();
	}
	
	public void testMdnsLong() throws Throwable {
		Hashtable<String,String> properties = new Hashtable<String,String>();
		properties.put("path", "/opennms");
		a.initialize("http", "Long Test", 1011, properties);
		a.register();
		Thread.sleep(timeout);
		a.unregister();
	}
}
