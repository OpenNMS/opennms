package org.opennms.netmgt.poller;

public interface ServiceMonitorLocator {
    
    String getServiceName();
    
    String getServiceLocatorKey();
    
    ServiceMonitor getServiceMonitor();
    

}
