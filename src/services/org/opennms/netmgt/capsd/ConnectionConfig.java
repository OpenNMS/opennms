/*
 * Created on Apr 27, 2004
 *
 * TODO Need to javadoc this class.
 * 
 */
package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.opennms.netmgt.utils.ParameterMap;

/**
 * @author brozow
 * 
 * TODO Need to javadoc this class
 * 
 */
public class ConnectionConfig {
    InetAddress m_inetAddress;

    Map m_qualifiers;

    int m_port;

    int m_timeout;

    int m_retry;

    /**
     * @param address
     * @param qualifiers
     * @param default_port
     * @param default_timeout
     * @param default_retry
     */
    public ConnectionConfig(InetAddress inetAddress, Map qualifiers, int port, int timeout, int retry) {
        m_inetAddress = inetAddress;
        m_qualifiers = qualifiers;
        m_port = port;
        m_timeout = timeout;
        m_retry = retry;
    }

    public ConnectionConfig(InetAddress address, int port) {
        m_inetAddress = address;
        m_port = port;

    }

    public ConnectionConfig(InetAddress inetAddress, int port, int timeout, int retry) {
        this(inetAddress, null, port, timeout, retry);
    }

    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(getInetAddress(), getPort());
    }

    /**
     * @return Returns the address.
     */
    public InetAddress getInetAddress() {
        return m_inetAddress;
    }

    /**
     * @param inetAddress
     *            The inetAddresss to set.
     */
    public void setInetAddress(InetAddress inetAddress) {
        m_inetAddress = inetAddress;
    }

    public int getKeyedInteger(String key, int defaultVal) {
        if (m_qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedInteger(m_qualifiers, key, defaultVal);
    }

    public void saveKeyedInteger(String key, int value) {
        if (m_qualifiers != null && !m_qualifiers.containsKey(key))
            m_qualifiers.put(key, new Integer(value));
    }

    /**
     * @return Returns the port.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * @return Returns the retries.
     */
    public int getRetry() {
        return m_retry;
    }

    /**
     * @param retry
     *            The retries to set.
     */
    public void setRetry(int retry) {
        m_retry = retry;
    }

    /**
     * @return Returns the timeout.
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * @param timeout
     *            The timeout to set.
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
}
