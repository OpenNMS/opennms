package org.opennms.secret.web.snmpinfo;

import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

@XmlRootElement(name="snmp-info")
public class SnmpInfo {

    private String m_community;
    private String m_version;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    
    public SnmpInfo() {
        
    }

    /**
     * @param config
     */
    public SnmpInfo(SnmpAgentConfig config) {
        m_community = config.getReadCommunity();
        m_port = config.getPort();
        m_timeout = config.getTimeout();
        m_retries = config.getRetries();
        m_version = config.getVersionAsString();
    }

    /**
     * @return the community
     */
    public String getCommunity() {
        return m_community;
    }

    /**
     * @param community the community to set
     */
    public void setCommunity(String community) {
        m_community = community;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return m_version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        m_version = version;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return m_port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * @return the retries
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * @return
     */
    public SnmpEventInfo createEventInfo(String ipAddr) throws UnknownHostException {
        SnmpEventInfo eventInfo = new SnmpEventInfo();
        eventInfo.setCommunityString(m_community);
        eventInfo.setVersion(m_version);
        eventInfo.setPort(m_port);
        eventInfo.setTimeout(m_timeout);
        eventInfo.setRetryCount(m_retries);
        eventInfo.setFirstIPAddress(ipAddr);
        return eventInfo;
    }
    
    
}

