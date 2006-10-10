package org.opennms.netmgt.poller.remote;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class DemoPollerConfiguration implements PollerConfiguration {
    
    Date m_timestamp = new Date();
    PolledService[] m_polledServices;
    
    DemoPollerConfiguration() {
        OnmsServiceType http = new OnmsServiceType("HTTP");
        
        List<PolledService> polledServices = new ArrayList<PolledService>();
        
        OnmsDistPoller distPoller = new OnmsDistPoller("locahost", "127.0.0.1");
        NetworkBuilder m_builder = new NetworkBuilder(distPoller);
        m_builder.addNode("Google").setId(1);
        m_builder.addInterface("64.233.161.99").setId(11);
        polledServices.add(createPolledService(111, m_builder.addService(http), new HashMap(), 3000));
        m_builder.addInterface("64.233.161.104").setId(12);
        polledServices.add(createPolledService(121, m_builder.addService(http), new HashMap(), 3000));
        m_builder.addNode("OpenNMS").setId(2);
        m_builder.addInterface("209.61.128.9").setId(21);
        polledServices.add(createPolledService(211, m_builder.addService(http), new HashMap(), 3000));
        
        m_polledServices = (PolledService[]) polledServices.toArray(new PolledService[polledServices.size()]);
        
    }
	
    public Date getConfigurationTimestamp() {
        return m_timestamp;
    }

    public PolledService[] getPolledServices() {
        return m_polledServices;
    }

    private PolledService createPolledService(int serviceID, OnmsMonitoredService service, Map monitorConfiguration, long interval) {
        service.setId(serviceID);
        return new PolledService(service, monitorConfiguration, new OnmsPollModel(interval));
    }
    
    public int getFirstId() {
        return getFirstService().getServiceId();
    }

    public PolledService getFirstService() {
        return m_polledServices[0];
    }
    
    

}
