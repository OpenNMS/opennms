package org.opennms.netmgt.ping;

import java.net.InetAddress;

import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.ping.StatisticalArrayList;

import junit.framework.TestCase;

public class PingTest extends TestCase {
	Pinger pinger        = null;
	InetAddress goodHost = null;
	InetAddress badHost  = null;
	
	public void setUp() throws Exception {
		super.setUp();
		System.setProperty("opennms.library.jicmp", "/sw/lib/libjicmp.jnilib");
		pinger = new Pinger();
		goodHost = InetAddress.getByName("truename.dyndns.org");
		badHost  = InetAddress.getByName("1.1.1.1");
	}
	
	public void testStatisticalArrayList() throws Exception {
		StatisticalArrayList sal = new StatisticalArrayList();
		
		assertNull(sal.percentNotNull());
		assertNull(sal.percentNull());
		
		sal.add(null);
		sal.add(1);
		sal.add(100);
		sal.add(15);
		sal.add(null);
		
		assertEquals(sal.countNotNull(), 3);
		assertEquals(sal.countNull(), 2);
		assertTrue(sal.percentNotNull().equals(new Integer(60)));
		assertTrue(sal.percentNull().equals(new Integer(40)));
	}
	
	/*
	public void testSinglePing() throws Exception {
		assertTrue(pinger.ping(goodHost) > 0);
		Thread.sleep(1000);
	}
	
	public void testSinglePingFailure() throws Exception {
		assertNull(pinger.ping(badHost));
		Thread.sleep(1000);
	}

	public void testParallelPing() throws Exception {
		StatisticalArrayList ret = pinger.parallelPing(goodHost, 10);
		System.out.println("pings = " + ret.size() + ", passed = " + ret.countNotNull() + " (" + ret.percentNotNull() + "%), failed = " + ret.countNull() + " (" + ret.percentNull() + "%), average = " + (ret.averageAsFloat() / 1000) + "ms");
		Thread.sleep(1000);
		assertTrue(ret.countNotNull() > 0);
	}
	
	public void testParallelPingFailure() throws Exception {
		StatisticalArrayList ret = pinger.parallelPing(badHost, 10);
		System.out.println("pings = " + ret.size() + ", passed = " + ret.countNotNull() + " (" + ret.percentNotNull() + "%), failed = " + ret.countNull() + " (" + ret.percentNull() + "%), average = " + (ret.averageAsFloat() / 1000) + "ms");
		Thread.sleep(1000);
		assertTrue(ret.countNotNull() == 0);
	}
	*/
}
