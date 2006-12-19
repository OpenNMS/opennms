package org.opennms.tools.groovy;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

public class ManuallyMonitoredService implements MonitoredService {
    private int m_nodeId;
    private String m_nodeLabel;
    private String m_ipAddr;
    private String m_svcName;
    private InetAddress m_inetAddr;

    public void setSvcName(String svcName) {
    	m_svcName = svcName;
    }

    public String getSvcName() {
        return m_svcName;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public NetworkInterface getNetInterface() {
        return new IPv4NetworkInterface(getAddress());
    }

    public InetAddress getAddress() {
        return m_inetAddr;
    }

	public void setIpAddr(String ipAddr) throws UnknownHostException {
		m_ipAddr = ipAddr;
		m_inetAddr = InetAddress.getByName(ipAddr);
	}

	public void setNodeId(int nodeId) {
		m_nodeId = nodeId;
	}

	public void setNodeLabel(String nodeLabel) {
		m_nodeLabel = nodeLabel;
	}

}
