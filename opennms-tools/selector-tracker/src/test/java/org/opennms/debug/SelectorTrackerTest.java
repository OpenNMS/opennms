package org.opennms.debug;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

import org.junit.Test;


public class SelectorTrackerTest {

	@Test
	public void test() throws IOException {
		
		Selector selector = Selector.open();
		
		assertTrue(selector.isOpen());

		selector.close();
		
		assertFalse(selector.isOpen());
		
		
		DatagramChannel c = DatagramChannel.open();
		DatagramSocket s = c.socket();
		s.setSoTimeout(1000);
		
		byte[] buf = new byte[1024];
		
		DatagramPacket p = new DatagramPacket(buf, 1024, InetAddress.getLocalHost(), 7);
		
		s.send(p);
		
		
	}

}
