package org.opennms.mock.snmp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class AgentConfigData {
    public Resource m_moFile;
    public InetAddress m_listenAddr;
    public long m_listenPort;

    public AgentConfigData() {
    }
    
    protected AgentConfigData(String moFileSpec, String listenAddr, long listenPort) throws UnknownHostException, MalformedURLException {
        if (moFileSpec.contains("://")) {
            m_moFile = new UrlResource(moFileSpec);
        } else {
            m_moFile = new FileSystemResource(moFileSpec);
        }
        m_listenAddr = InetAddress.getByName(listenAddr);
        m_listenPort = listenPort;
    }

    public Resource getMoFile() {
        return m_moFile;
    }

    public void setMoFile(Resource moFile) {
        m_moFile = moFile;
    }

    public InetAddress getListenAddr() {
        return m_listenAddr;
    }

    public void setListenAddr(InetAddress listenAddr) {
        m_listenAddr = listenAddr;
    }

    public long getListenPort() {
        return m_listenPort;
    }

    public void setListenPort(long listenPort) {
        m_listenPort = listenPort;
    }
}