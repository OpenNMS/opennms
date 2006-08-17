package org.opennms.netmgt.poller.remote;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsServiceType;

public class TestPollerConfiguration implements PollerConfiguration {
	
	public ServicePollConfiguration[] getConfigurationForPoller(String poller) {
		
		OnmsServiceType http = new OnmsServiceType("HTTP");
		
		List serviceConfigs = new ArrayList();
		
		OnmsDistPoller distPoller = new OnmsDistPoller("locahost", "127.0.0.1");
		NetworkBuilder m_builder = new NetworkBuilder(distPoller);
		m_builder.addNode("Google");
		m_builder.addInterface("64.233.161.99");
		serviceConfigs.add(new ServicePollConfiguration(m_builder.addService(http), 3000));
		m_builder.addInterface("64.233.161.104");
		serviceConfigs.add(new ServicePollConfiguration(m_builder.addService(http), 3000));
		m_builder.addNode("OpenNMS");
		m_builder.addInterface("209.61.128.9");
		serviceConfigs.add(new ServicePollConfiguration(m_builder.addService(http), 3000));
		
		return (ServicePollConfiguration[]) serviceConfigs.toArray(new ServicePollConfiguration[serviceConfigs.size()]);
	}

}
