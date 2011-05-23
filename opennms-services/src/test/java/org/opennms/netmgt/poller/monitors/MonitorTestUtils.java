package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.poller.MonitoredService;

public abstract class MonitorTestUtils {

    public static MonitoredService getMonitoredService(int nodeId, String hostname, String svcName) throws UnknownHostException {
        return getMonitoredService(nodeId, hostname, svcName, false);
    }

    public static MonitoredService getMonitoredService(int nodeId, String hostname, String svcName, boolean preferInet6Address) throws UnknownHostException {
        InetAddress myAddress = InetAddressUtils.resolveHostname(hostname, preferInet6Address);
        return new MockMonitoredService(nodeId, hostname, myAddress, svcName);
    }

}
