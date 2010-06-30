package org.opennms.netmgt.provision.support;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.SyncServiceDetector;

/**
 * <p>Abstract AbstractDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractDetector implements SyncServiceDetector {
    
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_RETRIES = 1;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_serviceName;
    
    
    /**
     * <p>Constructor for AbstractDetector.</p>
     */
    @Deprecated
    protected AbstractDetector() {
        
    }
    
    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected AbstractDetector(String serviceName, int port, int timeout, int retries) {
        m_serviceName = serviceName;
        m_port = port;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public AbstractDetector(String serviceName, int port) {
        this(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        m_port = port;
    }

    /**
     * <p>init</p>
     */
    public void init() {
        if (m_serviceName == null || m_timeout <= 0) {
            throw new IllegalStateException(String.format("ServiceName and/or timeout of %d is invalid.  ServiceName can't be null and timeout must be > 0", m_timeout));
        }
        onInit();
    }
    
    /**
     * <p>onInit</p>
     */
    abstract protected void onInit();

    /** {@inheritDoc} */
    abstract public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor);
    
    /**
     * <p>dispose</p>
     */
    abstract public void dispose();
    
    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    public int getTimeout() {
        return m_timeout;
    }

    /** {@inheritDoc} */
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
