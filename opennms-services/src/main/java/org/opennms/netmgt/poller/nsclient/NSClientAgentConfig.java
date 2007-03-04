package org.opennms.netmgt.poller.nsclient;

import java.net.InetAddress;

public class NSClientAgentConfig {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_PORT = 1248;
    public static final int DEFAULT_RETRIES = 1;
    public static final String DEFAULT_PASSWORD = "None";
    
    private InetAddress m_address;
    private int m_timeout;
    private int m_retries;
    private int m_port;
    private String m_password;

    
    public NSClientAgentConfig() {
        setDefaults();
    }
    
    public NSClientAgentConfig(InetAddress agentAddress) {
        m_address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_timeout = DEFAULT_TIMEOUT;
        m_retries = DEFAULT_RETRIES;
        m_port = DEFAULT_PORT;
        m_password = DEFAULT_PASSWORD;
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+m_address);
        buff.append(", Port: "+m_port);
        buff.append(", Password: "+String.valueOf(m_password)); //use valueOf to handle null values of m_password
        buff.append(", Timeout: "+m_timeout);
        buff.append(", Retries: "+m_retries);
        buff.append("]");
        return buff.toString();
    }


    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress address) {
        m_address = address;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getRetries() {
        return m_retries;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }
}
