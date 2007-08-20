package org.opennms.netmgt.ping;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import org.opennms.core.utils.CollectionMath;
import org.opennms.netmgt.ping.Pinger;

import junit.framework.TestCase;

public class PingTest extends TestCase {
	Pinger pinger        = null;
	InetAddress goodHost = null;
	InetAddress badHost  = null;

	public void testDummy() throws Exception {
	}

	public void nosetUp() throws Exception {
		super.setUp();
		pinger = new Pinger();
		goodHost = InetAddress.getByName("www.google.com");
		badHost  = InetAddress.getByName("1.1.1.1");
	}

	public void notestSinglePing() throws Exception {
		assertTrue(pinger.ping(goodHost) > 0);
		Thread.sleep(1000);
	}
	
	public void notestSinglePingFailure() throws Exception {
		assertNull(pinger.ping(badHost));
		Thread.sleep(1000);
	}

	public void notestParallelPing() throws Exception {
		Map<String,Number> ret = pinger.parallelPing(goodHost, 10);
		ArrayList<Number> items = new ArrayList<Number>();
		for (String key : ret.keySet()) {
			items.add(ret.get(key));
			System.out.println(key + ": " + ret.get(key));
		}
		ret.remove("loss");
		ret.remove("median");
		ret.remove("response-time");
		System.out.println("pings = " + items.size() + ", passed = " + CollectionMath.countNotNull(items) + " (" + CollectionMath.percentNotNull(items) + "%), failed = " + CollectionMath.countNull(items) + " (" + CollectionMath.percentNull(items) + "%), average = " + (CollectionMath.average(items).floatValue() / 1000F) + "ms");
		Thread.sleep(1000);
		assertTrue(CollectionMath.countNotNull(items) > 0);
	}
	
	public void notestParallelPingFailure() throws Exception {
		Map<String,Number> ret = pinger.parallelPing(badHost, 10);
		ret.remove("loss");
		ret.remove("median");
		ret.remove("response-time");
		ArrayList<Number>items = new ArrayList<Number>();
		System.out.println("pings = " + items.size() + ", passed = " + CollectionMath.countNotNull(items) + " (" + CollectionMath.percentNotNull(items) + "%), failed = " + CollectionMath.countNull(items) + " (" + CollectionMath.percentNull(items) + "%), average = " + (CollectionMath.average(items).floatValue() / 1000F) + "ms");
		Thread.sleep(1000);
		assertTrue(CollectionMath.countNotNull(items) == 0);
	}
}
