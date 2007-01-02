package org.opennms.netmgt.model;


public class LocationMonitorIpInterface {
    private OnmsLocationMonitor m_locationMonitor;
    private OnmsIpInterface m_ipInterface;

    public LocationMonitorIpInterface(OnmsLocationMonitor locationMonitor, OnmsIpInterface ipInterface) {
        m_locationMonitor = locationMonitor;
        m_ipInterface = ipInterface;
    }

    public OnmsIpInterface getIpInterface() {
        return m_ipInterface;
    }

    public OnmsLocationMonitor getLocationMonitor() {
        return m_locationMonitor;
    }
}

