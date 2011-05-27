package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.mock.MockMonitoredService;

public abstract class MonitorTestUtils {

    public static MockMonitoredService getMonitoredService(int nodeId, InetAddress addr, String svcName) throws UnknownHostException {
        return new MockMonitoredService(nodeId, InetAddressUtils.str(addr), addr, svcName);
    }

    public static MockMonitoredService getMonitoredService(int nodeId, String hostname, String svcName) throws UnknownHostException {
        return getMonitoredService(nodeId, hostname, svcName, false);
    }

    public static MockMonitoredService getMonitoredService(int nodeId, String hostname, String svcName, boolean preferInet6Address) throws UnknownHostException {
        InetAddress myAddress = InetAddressUtils.resolveHostname(hostname, preferInet6Address);
        return new MockMonitoredService(nodeId, hostname, myAddress, svcName);
    }

}
