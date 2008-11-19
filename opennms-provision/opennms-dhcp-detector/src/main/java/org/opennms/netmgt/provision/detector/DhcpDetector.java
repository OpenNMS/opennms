package org.opennms.netmgt.provision.detector;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.provision.support.dhcp.Dhcpd;


public class DhcpDetector {
	//extends BasicDetector<Request, Response>
    
    public long isService(InetAddress address, int port, int timeout) {
        try {
            long responseTime = Dhcpd.isServer(address);
            return responseTime;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
    }
}