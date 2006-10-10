package org.opennms.netmgt.poller.remote;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

public class PolledService implements MonitoredService {
    
    IPv4NetworkInterface m_netInterface;
    Map m_monitorConfiguration;
    OnmsPollModel m_pollModel;
    private Integer m_serviceId;
    private Integer m_nodeId;
    private String m_nodeLabel;
    private String m_svcName;
	
	public PolledService(OnmsMonitoredService monitoredService, Map monitorConfiguration, OnmsPollModel pollModel) {
        m_serviceId = monitoredService.getId();
        m_nodeId = monitoredService.getNodeId();
        m_nodeLabel = monitoredService.getIpInterface().getNode().getLabel();
        m_svcName = monitoredService.getServiceName();
        m_netInterface = new IPv4NetworkInterface(monitoredService.getIpInterface().getInetAddress());
		m_monitorConfiguration = monitorConfiguration;
		m_pollModel = pollModel;
	}
	
	public Integer getServiceId() {
		return m_serviceId;
	}

    public InetAddress getAddress() {
        return m_netInterface.getInetAddress();
    }

    public String getIpAddr() {
        return m_netInterface.getInetAddress().getHostAddress();
    }

    public NetworkInterface getNetInterface() {
        return m_netInterface;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public String getSvcName() {
        return m_svcName;
    }
	
	public Map getMonitorConfiguration() {
        return m_monitorConfiguration;
    }
    
    public OnmsPollModel getPollModel() {
        return m_pollModel;
    }

    @Override
    public String toString() {
        return getNodeId()+":"+getIpAddr()+":"+getSvcName();
    }
    
    
}
