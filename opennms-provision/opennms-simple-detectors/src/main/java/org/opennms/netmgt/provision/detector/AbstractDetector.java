package org.opennms.netmgt.provision.detector;

import java.net.InetAddress;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.ServiceDetector;

public abstract class AbstractDetector implements ServiceDetector {
    
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_serviceName;
    
    
    abstract public void init();

    abstract public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor);

    public void setPort(int port) {
        m_port = port;
    }

    public int getPort() {
        return m_port;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public int getRetries() {
        return m_retries;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    public String getServiceName() {
        return m_serviceName;
    }

}
