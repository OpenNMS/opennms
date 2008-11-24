package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.netmgt.provision.support.dhcp.DhcpdConfigFactory;

public class DhcpDetectorTest{
	
	@Test
	public void testMyDetector() throws MarshalException, ValidationException, IOException{
	    //FIXME: Finish test
//	    DhcpdConfigFactory.init();
//		DhcpDetector detect = new DhcpDetector();
//		long retVal = detect.isService(InetAddress.getByName("192.168.1.103"), 5818, 1000);
//		System.out.println("returning: " + retVal);
//		assertNotNull(retVal);
	}
	
}