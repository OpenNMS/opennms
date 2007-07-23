package org.opennms.netmgt.ping;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.savarese.vserv.tcpip.ICMPEchoPacket;

import junit.framework.TestCase;

public class PingTest extends TestCase {
	InetAddress host;
	Pinger pinger;
	private static final int TIMEOUT = 1000;
	
	public PingTest() throws UnknownHostException, IOException {
		host = InetAddress.getByName("localhost");
		pinger = new Pinger(TIMEOUT);
	}
	
	public void testPing() throws IOException {
		long responseTime = pinger.ping(host);
	}
	
	public void testSmokePing() throws IOException {
		PingCallback pc = new DefaultPingCallback();
		pinger.ping(host, 20, pc);
	}


}

