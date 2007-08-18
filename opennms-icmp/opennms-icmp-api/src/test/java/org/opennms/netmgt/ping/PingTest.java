package org.opennms.netmgt.ping;

import java.net.InetAddress;
import java.util.Collection;

import org.opennms.core.utils.CollectionMath;
import org.opennms.netmgt.ping.Pinger;

import junit.framework.TestCase;

public class PingTest extends TestCase {
	Pinger pinger        = null;
	InetAddress goodHost = null;
	InetAddress badHost  = null;
	
	public void setUp() throws Exception {
		super.setUp();
		pinger = new Pinger();
		goodHost = InetAddress.getByName("www.google.com");
		badHost  = InetAddress.getByName("1.1.1.1");
	}

	public void testSinglePing() throws Exception {
		assertTrue(pinger.ping(goodHost) > 0);
		Thread.sleep(1000);
	}
	
	public void testSinglePingFailure() throws Exception {
		assertNull(pinger.ping(badHost));
		Thread.sleep(1000);
	}

	public void testParallelPing() throws Exception {
		Collection<Long> ret = pinger.parallelPing(goodHost, 10);
		int count = 0;
		for (Long entry : ret) {
			System.out.println(++count + ": " + entry);
		}
		System.out.println("pings = " + ret.size() + ", passed = " + CollectionMath.countNotNull(ret) + " (" + CollectionMath.percentNotNull(ret) + "%), failed = " + CollectionMath.countNull(ret) + " (" + CollectionMath.percentNull(ret) + "%), average = " + (CollectionMath.average(ret).floatValue() / 1000F) + "ms");
		Thread.sleep(1000);
		assertTrue(CollectionMath.countNotNull(ret) > 0);
	}
	
	public void testParallelPingFailure() throws Exception {
		Collection<Long> ret = pinger.parallelPing(badHost, 10);
		System.out.println("pings = " + ret.size() + ", passed = " + CollectionMath.countNotNull(ret) + " (" + CollectionMath.percentNotNull(ret) + "%), failed = " + CollectionMath.countNull(ret) + " (" + CollectionMath.percentNull(ret) + "%), average = " + (CollectionMath.average(ret).floatValue() / 1000F) + "ms");
		Thread.sleep(1000);
		assertTrue(CollectionMath.countNotNull(ret) == 0);
	}
}
