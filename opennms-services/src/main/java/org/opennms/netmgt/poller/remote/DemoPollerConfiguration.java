package org.opennms.netmgt.poller.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsServiceType;

public class DemoPollerConfiguration implements PollerConfiguration {
	
	public PollConfiguration[] getConfigurationForPoller() {
		
		OnmsServiceType http = new OnmsServiceType("HTTP");
		
		List serviceConfigs = new ArrayList();
		
		OnmsDistPoller distPoller = new OnmsDistPoller("locahost", "127.0.0.1");
		NetworkBuilder m_builder = new NetworkBuilder(distPoller);
		m_builder.addNode("Google");
		m_builder.addInterface("64.233.161.99");
		serviceConfigs.add(new PollConfiguration(m_builder.addService(http), new HashMap(), 3000));
		m_builder.addInterface("64.233.161.104");
		serviceConfigs.add(new PollConfiguration(m_builder.addService(http), new HashMap(), 3000));
		m_builder.addNode("OpenNMS");
		m_builder.addInterface("209.61.128.9");
		serviceConfigs.add(new PollConfiguration(m_builder.addService(http), new HashMap(), 3000));
		
		return (PollConfiguration[]) serviceConfigs.toArray(new PollConfiguration[serviceConfigs.size()]);
	}

}
