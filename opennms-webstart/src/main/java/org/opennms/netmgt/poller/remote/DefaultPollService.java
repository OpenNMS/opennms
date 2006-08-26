package org.opennms.netmgt.poller.remote;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.springframework.util.Assert;

public class DefaultPollService implements PollService {
	
	Map m_monitors;
	
	public void setMonitors(Map monitors) {
		m_monitors = monitors;
	}

	public PollStatus poll(final OnmsMonitoredService monitoredService, Map monitorConfiguration) {
		ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(monitoredService.getServiceName());
		Assert.notNull(monitor, "Unable to find monitor for service "+monitoredService.getServiceName());
		
		MonitoredService monSvcWrapper = new MonitoredService() {

			public InetAddress getAddress() {
				return monitoredService.getIpInterface().getInetAddress();
			}

			public String getIpAddr() {
				return monitoredService.getIpAddress();
			}

			public NetworkInterface getNetInterface() {
				return new IPv4NetworkInterface(getAddress());
			}

			public int getNodeId() {
				return monitoredService.getNodeId();
			}

			public String getNodeLabel() {
				return monitoredService.getIpInterface().getNode().getLabel();
			}

			public String getSvcName() {
				return monitoredService.getServiceName();
			}
			
		};
		
		return monitor.poll(monSvcWrapper, monitorConfiguration, null);
	}

}
