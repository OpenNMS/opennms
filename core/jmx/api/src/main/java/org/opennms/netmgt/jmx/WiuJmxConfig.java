package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.BeanInfo;

import java.net.InetAddress;
import java.util.Map;

public class WiuJmxConfig {

    private InetAddress agentAddress;

    private Map<String, Object> properties;

    private int retries;

    private Map<String, BeanInfo> mbeans;

    private boolean useMbeanForRrds;

    private Map<String, WiuJmxDataSource> dsMap;
    private String resourceName;

    public InetAddress getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(InetAddress agentAddress) {
        this.agentAddress = agentAddress;
    }

    public Map<String, Object> getServiceProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public Map<String, BeanInfo> getMbeans() {
        return mbeans;
    }

    public void setMbeans(Map<String, BeanInfo> mbeans) {
        this.mbeans = mbeans;
    }

    public boolean isUseMbeanForRrds() {
        return useMbeanForRrds;
    }

    public void setUseMbeanForRrds(boolean useMbeanForRrds) {
        this.useMbeanForRrds = useMbeanForRrds;
    }

    public Map<String, WiuJmxDataSource> getDataSourceMap() {
        return dsMap;
    }

    public void setDataSourceMap(Map<String, WiuJmxDataSource> dsMap) {
        this.dsMap = dsMap;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
