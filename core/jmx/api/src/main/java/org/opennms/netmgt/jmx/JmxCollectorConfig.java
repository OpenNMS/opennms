package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.collectd.jmx.JmxCollection;

import java.util.Map;

public class JmxCollectorConfig {

    private String connectionName;

    private String agentAddress;

    private int retries;

    private Map<String, String> serviceProperties;
    private JmxCollection jmxCollection;

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public Map<String, String> getServiceProperties() {
        return serviceProperties;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setServiceProperties(Map<String, String> serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    public void setJmxCollection(JmxCollection jmxCollection) {
        this.jmxCollection = jmxCollection;
    }

    public JmxCollection getJmxCollection() {
        return jmxCollection;
    }
}
