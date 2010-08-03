package org.opennms.netmgt.poller.monitors;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.poller.MonitoredService;

public abstract class MonitorTestUtils {

    public static MonitoredService getMonitoredService(int nodeId, String hostname, String svcName) throws UnknownHostException {
        return getMonitoredService(nodeId, hostname, svcName, false);
    }

    public static MonitoredService getMonitoredService(int nodeId, String hostname, String svcName, boolean preferInet6Address) throws UnknownHostException {
        InetAddress myAddress = null;
        InetAddress[] addresses = InetAddress.getAllByName(hostname);
        for (InetAddress address : addresses) {
            myAddress = address;
            if (!preferInet6Address && myAddress instanceof Inet4Address) break;
            if (preferInet6Address && myAddress instanceof Inet6Address) break;
        }
        if (preferInet6Address && !(myAddress instanceof Inet6Address)) {
            throw new UnknownHostException("No IPv6 address could be found for the hostname: " + hostname);
        }
        return new MockMonitoredService(nodeId, hostname, myAddress, svcName);
    }

}
