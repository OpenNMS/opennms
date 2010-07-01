package org.opennms.mock.snmp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * <p>AgentConfigData class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AgentConfigData {
    public Resource m_moFile;
    public InetAddress m_listenAddr;
    public long m_listenPort;

    /**
     * <p>Constructor for AgentConfigData.</p>
     */
    public AgentConfigData() {
    }
    
    /**
     * <p>Constructor for AgentConfigData.</p>
     *
     * @param moFileSpec a {@link java.lang.String} object.
     * @param listenAddr a {@link java.lang.String} object.
     * @param listenPort a long.
     * @throws java.net.UnknownHostException if any.
     * @throws java.net.MalformedURLException if any.
     */
    protected AgentConfigData(String moFileSpec, String listenAddr, long listenPort) throws UnknownHostException, MalformedURLException {
        if (moFileSpec.contains("://")) {
            m_moFile = new UrlResource(moFileSpec);
        } else {
            m_moFile = new FileSystemResource(moFileSpec);
        }
        m_listenAddr = InetAddress.getByName(listenAddr);
        m_listenPort = listenPort;
    }

    /**
     * <p>getMoFile</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getMoFile() {
        return m_moFile;
    }

    /**
     * <p>setMoFile</p>
     *
     * @param moFile a {@link org.springframework.core.io.Resource} object.
     */
    public void setMoFile(Resource moFile) {
        m_moFile = moFile;
    }

    /**
     * <p>getListenAddr</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getListenAddr() {
        return m_listenAddr;
    }

    /**
     * <p>setListenAddr</p>
     *
     * @param listenAddr a {@link java.net.InetAddress} object.
     */
    public void setListenAddr(InetAddress listenAddr) {
        m_listenAddr = listenAddr;
    }

    /**
     * <p>getListenPort</p>
     *
     * @return a long.
     */
    public long getListenPort() {
        return m_listenPort;
    }

    /**
     * <p>setListenPort</p>
     *
     * @param listenPort a long.
     */
    public void setListenPort(long listenPort) {
        m_listenPort = listenPort;
    }
}
