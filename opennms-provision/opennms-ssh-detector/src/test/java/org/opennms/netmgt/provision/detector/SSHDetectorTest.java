package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;

public class SSHDetectorTest{
	//Tested on a local server with SSH
    
	@Test
	public void testMyDetector() throws UnknownHostException{
		//SshDetector detector = new SshDetector();
		//assertTrue(detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
	}
	
}