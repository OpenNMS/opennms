package org.opennms.netmgt.provision.support;

import java.net.InetAddress;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.SyncServiceDetector;

public abstract class AbstractDetector implements SyncServiceDetector {
    
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_RETRIES = 1;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_serviceName;
    
    
    @Deprecated
    protected AbstractDetector() {
        
    }
    
    protected AbstractDetector(String serviceName, int port, int timeout, int retries) {
        m_serviceName = serviceName;
        m_port = port;
        m_timeout = timeout;
        m_retries = retries;
    }

    public AbstractDetector(String serviceName, int port) {
        this(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        m_port = port;
    }

    public void init() {
        if (m_serviceName == null || m_timeout <= 0) {
            throw new IllegalStateException(String.format("ServiceName and/or timeout of %d is invalid.  ServiceName can't be null and timeout must be > 0", m_timeout));
        }
        onInit();
    }
    
    abstract protected void onInit();

    abstract public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor);
    
    abstract public void dispose();
    
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
